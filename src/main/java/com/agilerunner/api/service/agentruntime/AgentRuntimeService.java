package com.agilerunner.api.service.agentruntime;

import com.agilerunner.api.service.dto.GitHubCommentResponse;
import com.agilerunner.api.service.github.request.GitHubEventServiceRequest;
import com.agilerunner.api.service.github.response.GitHubCommentExecutionResult;
import com.agilerunner.api.service.review.request.ManualRerunServiceRequest;
import com.agilerunner.client.agentruntime.AgentRuntimeRepository;
import com.agilerunner.domain.Review;
import com.agilerunner.domain.agentruntime.AgentExecutionLog;
import com.agilerunner.domain.agentruntime.AgentExecutionStatus;
import com.agilerunner.domain.agentruntime.AgentRole;
import com.agilerunner.domain.agentruntime.CriteriaCategory;
import com.agilerunner.domain.agentruntime.CriteriaStatus;
import com.agilerunner.domain.agentruntime.ExecutionStartType;
import com.agilerunner.domain.agentruntime.ValidationCriteria;
import com.agilerunner.domain.agentruntime.WebhookExecution;
import com.agilerunner.domain.agentruntime.WebhookExecutionStatus;
import com.agilerunner.domain.agentruntime.TaskRuntimeState;
import com.agilerunner.domain.agentruntime.TaskRuntimeStatus;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.agilerunner.domain.exception.AgileRunnerException;
import com.agilerunner.domain.exception.ErrorCode;
import com.agilerunner.domain.exception.FailureDisposition;
import com.agilerunner.domain.exception.FailureDispositionPolicy;
import com.agilerunner.domain.executioncontrol.ExecutionControlMode;
import com.agilerunner.domain.executioncontrol.GitHubWriteSkipReason;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class AgentRuntimeService {
    public static final String STEP_WEBHOOK_ACCEPTED = "webhook-accepted";
    public static final String STEP_MANUAL_RERUN_ACCEPTED = "manual-rerun-accepted";
    public static final String STEP_REVIEW_GENERATED = "review-generated";
    public static final String STEP_COMMENT_POSTED = "comment-posted";

    private static final String CRITERIA_PAYLOAD_ACCEPTED = "payload-accepted";
    private static final String CRITERIA_REVIEW_GENERATED = "review-generated";
    private static final String CRITERIA_COMMENT_POSTED = "comment-posted";

    @Nullable
    private final AgentRuntimeRepository agentRuntimeRepository;
    private final ObjectMapper objectMapper;
    private final FailureDispositionPolicy failureDispositionPolicy;

    public AgentRuntimeService(@Autowired(required = false) @Nullable AgentRuntimeRepository agentRuntimeRepository,
                               ObjectMapper objectMapper) {
        this.agentRuntimeRepository = agentRuntimeRepository;
        this.objectMapper = objectMapper;
        this.failureDispositionPolicy = new FailureDispositionPolicy();
    }

    public WebhookExecution startWebhookExecution(String deliveryId, GitHubEventServiceRequest request) {
        LocalDateTime now = LocalDateTime.now();
        String repositoryName = request.getRepositoryName();
        int pullRequestNumber = request.getPullRequestNumber();
        long issueNumber = pullRequestNumber;
        String taskKey = buildTaskKey(repositoryName, pullRequestNumber);
        String executionKey = buildExecutionKey(deliveryId);

        WebhookExecution webhookExecution = WebhookExecution.start(
                executionKey,
                taskKey,
                deliveryId,
                repositoryName,
                pullRequestNumber,
                request.getGitHubEventType().name(),
                request.getAction(),
                now
        ).withExecutionStartType(ExecutionStartType.WEBHOOK)
                .withExecutionControl(request.getExecutionControlMode(), false, null);

        if (!isEnabled()) {
            return webhookExecution;
        }

        TaskRuntimeState existingTaskRuntimeState = agentRuntimeRepository.findTaskRuntimeState(taskKey).orElse(null);
        TaskRuntimeState taskRuntimeState = TaskRuntimeState.of(
                taskKey,
                issueNumber,
                buildTaskTitle(repositoryName, pullRequestNumber),
                TaskRuntimeStatus.IN_PROGRESS,
                getNextRetryCount(existingTaskRuntimeState),
                AgentRole.ORCHESTRATOR,
                getStartedAt(existingTaskRuntimeState, now),
                null
        );

        agentRuntimeRepository.upsertTaskRuntimeState(taskRuntimeState);
        agentRuntimeRepository.replaceValidationCriteria(
                taskKey,
                buildCriteria(
                        taskKey,
                        CriteriaStatus.PASSED,
                        CriteriaStatus.PENDING,
                        CriteriaStatus.PENDING,
                        "Webhook payload accepted",
                        null,
                        null
                )
        );
        agentRuntimeRepository.upsertWebhookExecution(webhookExecution);
        agentRuntimeRepository.appendExecutionLog(
                AgentExecutionLog.of(
                        taskKey,
                        issueNumber,
                        executionKey,
                        ExecutionStartType.WEBHOOK,
                        AgentRole.ORCHESTRATOR,
                        STEP_WEBHOOK_ACCEPTED,
                        AgentExecutionStatus.SUCCEEDED,
                        "GitHub pull_request webhook accepted",
                        "task and webhook execution initialized",
                        null,
                        null,
                        toJson(buildWebhookSnapshot(deliveryId, request)),
                        now,
                        now
                ).withExecutionControl(request.getExecutionControlMode(), false, null)
        );

        return webhookExecution;
    }

    public WebhookExecution startManualRerunExecution(ManualRerunServiceRequest request) {
        LocalDateTime now = LocalDateTime.now();
        String taskKey = buildTaskKey(request.getRepositoryName(), request.getPullRequestNumber());
        long issueNumber = request.getPullRequestNumber();
        String manualRerunId = UUID.randomUUID().toString();
        String deliveryId = buildManualRerunDeliveryId(manualRerunId);
        String executionKey = buildManualRerunExecutionKey(manualRerunId);

        WebhookExecution runtimeExecution = WebhookExecution.start(
                executionKey,
                taskKey,
                deliveryId,
                request.getRepositoryName(),
                request.getPullRequestNumber(),
                "PULL_REQUEST",
                "manual_rerun",
                now
        ).withExecutionStartType(ExecutionStartType.MANUAL_RERUN)
                .withExecutionControl(request.getExecutionControlMode(), false, null);

        if (!isEnabled()) {
            return runtimeExecution;
        }

        TaskRuntimeState existingTaskRuntimeState = agentRuntimeRepository.findTaskRuntimeState(taskKey).orElse(null);
        TaskRuntimeState taskRuntimeState = TaskRuntimeState.of(
                taskKey,
                issueNumber,
                buildTaskTitle(request.getRepositoryName(), request.getPullRequestNumber()),
                TaskRuntimeStatus.IN_PROGRESS,
                getNextRetryCount(existingTaskRuntimeState),
                AgentRole.ORCHESTRATOR,
                getStartedAt(existingTaskRuntimeState, now),
                null
        );

        agentRuntimeRepository.upsertTaskRuntimeState(taskRuntimeState);
        agentRuntimeRepository.replaceValidationCriteria(
                taskKey,
                buildCriteria(
                        taskKey,
                        CriteriaStatus.PASSED,
                        CriteriaStatus.PENDING,
                        CriteriaStatus.PENDING,
                        "Manual rerun request accepted",
                        null,
                        null
                )
        );
        agentRuntimeRepository.upsertWebhookExecution(runtimeExecution);
        agentRuntimeRepository.appendExecutionLog(
                AgentExecutionLog.of(
                        taskKey,
                        issueNumber,
                        executionKey,
                        ExecutionStartType.MANUAL_RERUN,
                        AgentRole.ORCHESTRATOR,
                        STEP_MANUAL_RERUN_ACCEPTED,
                        AgentExecutionStatus.SUCCEEDED,
                        "Manual rerun request accepted",
                        "task and manual rerun execution initialized",
                        null,
                        null,
                        toJson(buildManualRerunSnapshot(request, deliveryId)),
                        now,
                        now
                ).withExecutionControl(request.getExecutionControlMode(), false, null)
        );

        return runtimeExecution;
    }

    public void recordReviewGenerated(WebhookExecution webhookExecution, Review review) {
        if (!isEnabled()) {
            return;
        }

        LocalDateTime now = LocalDateTime.now();
        long issueNumber = webhookExecution.getPullRequestNumber();

        agentRuntimeRepository.replaceValidationCriteria(
                webhookExecution.getTaskKey(),
                buildCriteria(
                        webhookExecution.getTaskKey(),
                        CriteriaStatus.PASSED,
                        CriteriaStatus.PASSED,
                        CriteriaStatus.PENDING,
                        buildPayloadAcceptedEvidence(webhookExecution),
                        buildReviewEvidence(review),
                        null
                )
        );
        agentRuntimeRepository.appendExecutionLog(
                AgentExecutionLog.of(
                        webhookExecution.getTaskKey(),
                        issueNumber,
                        webhookExecution.getExecutionKey(),
                        webhookExecution.getExecutionStartType(),
                        AgentRole.ORCHESTRATOR,
                        STEP_REVIEW_GENERATED,
                        AgentExecutionStatus.SUCCEEDED,
                        "Review prompt executed",
                        buildReviewOutputSummary(review),
                        null,
                        null,
                        toJson(buildReviewSnapshot(review)),
                        now,
                        now
                ).withExecutionControl(webhookExecution.getExecutionControlMode(), webhookExecution.getWritePerformed(), webhookExecution.getWriteSkipReason())
        );
    }

    public void recordExecutionResult(WebhookExecution webhookExecution, GitHubCommentExecutionResult executionResult) {
        if (!isEnabled()) {
            return;
        }

        LocalDateTime now = LocalDateTime.now();
        long issueNumber = webhookExecution.getPullRequestNumber();
        TaskRuntimeState existingTaskRuntimeState = agentRuntimeRepository.findTaskRuntimeState(webhookExecution.getTaskKey()).orElse(null);
        WebhookExecution completedWebhookExecution = webhookExecution
                .withExecutionControl(
                        executionResult.getExecutionControlMode(),
                        executionResult.isWritePerformed(),
                        executionResult.getWriteSkipReason()
                )
                .complete(WebhookExecutionStatus.SUCCEEDED, null, null, now);

        replaceSuccessfulCommentCriteria(webhookExecution, executionResult);
        agentRuntimeRepository.upsertWebhookExecution(completedWebhookExecution);
        agentRuntimeRepository.upsertTaskRuntimeState(
                TaskRuntimeState.of(
                        webhookExecution.getTaskKey(),
                        issueNumber,
                        buildTaskTitle(webhookExecution.getRepositoryName(), webhookExecution.getPullRequestNumber()),
                        TaskRuntimeStatus.DONE,
                        getCurrentRetryCount(existingTaskRuntimeState),
                        AgentRole.ORCHESTRATOR,
                        getStartedAt(existingTaskRuntimeState, webhookExecution.getStartedAt()),
                        now
                )
        );
        appendSuccessfulCommentLog(webhookExecution, executionResult, issueNumber, now);
    }

    public void recordFailure(WebhookExecution webhookExecution, String stepName, Exception exception) {
        if (!isEnabled()) {
            return;
        }

        LocalDateTime now = LocalDateTime.now();
        long issueNumber = webhookExecution.getPullRequestNumber();
        TaskRuntimeState existingTaskRuntimeState = agentRuntimeRepository.findTaskRuntimeState(webhookExecution.getTaskKey()).orElse(null);
        ErrorCode errorCode = resolveErrorCode(exception);
        FailureDisposition failureDisposition = resolveFailureDisposition(errorCode);
        WebhookExecution failedWebhookExecution = webhookExecution.complete(
                WebhookExecutionStatus.FAILED,
                exception.getMessage(),
                errorCode,
                failureDisposition,
                now
        );

        agentRuntimeRepository.replaceValidationCriteria(
                webhookExecution.getTaskKey(),
                buildCriteria(
                        webhookExecution.getTaskKey(),
                        CriteriaStatus.PASSED,
                        resolveReviewCriteriaStatus(stepName),
                        resolveCommentCriteriaStatus(stepName),
                        buildPayloadAcceptedEvidence(webhookExecution),
                        resolveReviewEvidence(stepName, exception),
                        resolveCommentEvidence(stepName, exception)
                )
        );
        agentRuntimeRepository.upsertWebhookExecution(failedWebhookExecution);
        agentRuntimeRepository.upsertTaskRuntimeState(
                TaskRuntimeState.of(
                        webhookExecution.getTaskKey(),
                        issueNumber,
                        buildTaskTitle(webhookExecution.getRepositoryName(), webhookExecution.getPullRequestNumber()),
                        TaskRuntimeStatus.FAILED,
                        getCurrentRetryCount(existingTaskRuntimeState),
                        AgentRole.ORCHESTRATOR,
                        getStartedAt(existingTaskRuntimeState, webhookExecution.getStartedAt()),
                        now
                )
        );
        agentRuntimeRepository.appendExecutionLog(
                AgentExecutionLog.of(
                        webhookExecution.getTaskKey(),
                        issueNumber,
                        webhookExecution.getExecutionKey(),
                        webhookExecution.getExecutionStartType(),
                        AgentRole.ORCHESTRATOR,
                        stepName,
                        AgentExecutionStatus.FAILED,
                        "Runtime step failed",
                        null,
                        buildErrorSummary(exception),
                        errorCode,
                        failureDisposition,
                        toJson(buildErrorSnapshot(exception)),
                        now,
                        now
                ).withExecutionControl(
                        webhookExecution.getExecutionControlMode(),
                        webhookExecution.getWritePerformed(),
                        webhookExecution.getWriteSkipReason()
                )
        );
    }

    private boolean isEnabled() {
        return agentRuntimeRepository != null;
    }

    private int getNextRetryCount(@Nullable TaskRuntimeState existingTaskRuntimeState) {
        if (existingTaskRuntimeState == null) {
            return 0;
        }

        return existingTaskRuntimeState.getRetryCount() + 1;
    }

    private int getCurrentRetryCount(@Nullable TaskRuntimeState existingTaskRuntimeState) {
        if (existingTaskRuntimeState == null) {
            return 0;
        }

        return existingTaskRuntimeState.getRetryCount();
    }

    private LocalDateTime getStartedAt(@Nullable TaskRuntimeState existingTaskRuntimeState, LocalDateTime fallback) {
        if (existingTaskRuntimeState == null || existingTaskRuntimeState.getStartedAt() == null) {
            return fallback;
        }

        return existingTaskRuntimeState.getStartedAt();
    }

    private List<ValidationCriteria> buildCriteria(String taskKey,
                                                   CriteriaStatus payloadAcceptedStatus,
                                                   CriteriaStatus reviewGeneratedStatus,
                                                   CriteriaStatus commentPostedStatus,
                                                   String payloadEvidence,
                                                   String reviewEvidence,
                                                   String commentEvidence) {
        List<ValidationCriteria> validationCriteria = new ArrayList<>();
        validationCriteria.add(ValidationCriteria.of(
                taskKey,
                CRITERIA_PAYLOAD_ACCEPTED,
                CriteriaCategory.REQUIRED,
                "Execution input can be resolved into repository, pull request, and installation context.",
                payloadAcceptedStatus,
                payloadEvidence
        ));
        validationCriteria.add(ValidationCriteria.of(
                taskKey,
                CRITERIA_REVIEW_GENERATED,
                CriteriaCategory.REQUIRED,
                "OpenAI review output is generated for the target pull request.",
                reviewGeneratedStatus,
                reviewEvidence
        ));
        validationCriteria.add(ValidationCriteria.of(
                taskKey,
                CRITERIA_COMMENT_POSTED,
                CriteriaCategory.REQUIRED,
                "Generated review comments are posted back to GitHub.",
                commentPostedStatus,
                commentEvidence
        ));
        return validationCriteria;
    }

    private void replaceSuccessfulCommentCriteria(WebhookExecution webhookExecution,
                                                  GitHubCommentExecutionResult executionResult) {
        agentRuntimeRepository.replaceValidationCriteria(
                webhookExecution.getTaskKey(),
                buildCriteria(
                        webhookExecution.getTaskKey(),
                        CriteriaStatus.PASSED,
                        CriteriaStatus.PASSED,
                        CriteriaStatus.PASSED,
                        buildPayloadAcceptedEvidence(webhookExecution),
                        "Review generated successfully",
                        buildCommentEvidence(executionResult)
                )
        );
    }

    private void appendSuccessfulCommentLog(WebhookExecution webhookExecution,
                                            GitHubCommentExecutionResult executionResult,
                                            long issueNumber,
                                            LocalDateTime now) {
        agentRuntimeRepository.appendExecutionLog(
                AgentExecutionLog.of(
                        webhookExecution.getTaskKey(),
                        issueNumber,
                        webhookExecution.getExecutionKey(),
                        webhookExecution.getExecutionStartType(),
                        AgentRole.ORCHESTRATOR,
                        STEP_COMMENT_POSTED,
                        AgentExecutionStatus.SUCCEEDED,
                        buildCommentInputSummary(executionResult),
                        buildCommentOutputSummary(executionResult),
                        null,
                        null,
                        toJson(buildCommentSnapshot(executionResult)),
                        now,
                        now
                ).withExecutionControl(
                        executionResult.getExecutionControlMode(),
                        executionResult.isWritePerformed(),
                        executionResult.getWriteSkipReason()
                )
        );
    }

    private CriteriaStatus resolveReviewCriteriaStatus(String stepName) {
        if (STEP_COMMENT_POSTED.equals(stepName)) {
            return CriteriaStatus.PASSED;
        }

        return CriteriaStatus.FAILED;
    }

    private CriteriaStatus resolveCommentCriteriaStatus(String stepName) {
        if (STEP_COMMENT_POSTED.equals(stepName)) {
            return CriteriaStatus.FAILED;
        }

        return CriteriaStatus.PENDING;
    }

    private String resolveReviewEvidence(String stepName, Exception exception) {
        if (STEP_COMMENT_POSTED.equals(stepName)) {
            return "Review generated successfully";
        }

        return buildErrorSummary(exception);
    }

    private String resolveCommentEvidence(String stepName, Exception exception) {
        if (STEP_COMMENT_POSTED.equals(stepName)) {
            return buildErrorSummary(exception);
        }

        return null;
    }

    private String buildTaskKey(String repositoryName, int pullRequestNumber) {
        return "PR_REVIEW:" + repositoryName.replace("/", ":") + "#" + pullRequestNumber;
    }

    private ErrorCode resolveErrorCode(Exception exception) {
        if (!(exception instanceof AgileRunnerException agileRunnerException)) {
            return null;
        }

        return agileRunnerException.getErrorCode();
    }

    private FailureDisposition resolveFailureDisposition(@Nullable ErrorCode errorCode) {
        if (errorCode == null) {
            return null;
        }

        return failureDispositionPolicy.classify(errorCode);
    }

    private String buildExecutionKey(String deliveryId) {
        return "EXECUTION:" + deliveryId;
    }

    private String buildManualRerunExecutionKey(String manualRerunId) {
        return "EXECUTION:MANUAL_RERUN:" + manualRerunId;
    }

    private String buildManualRerunDeliveryId(String manualRerunId) {
        return "MANUAL_RERUN_DELIVERY:" + manualRerunId;
    }

    private String buildTaskTitle(String repositoryName, int pullRequestNumber) {
        return "GitHub PR review for " + repositoryName + "#" + pullRequestNumber;
    }

    private String buildReviewEvidence(Review review) {
        return "bodyLength=" + review.getReviewBody().length() + ", inlineComments=" + review.getInlineComments().size();
    }

    private String buildPayloadAcceptedEvidence(WebhookExecution webhookExecution) {
        if (webhookExecution.getExecutionStartType() == ExecutionStartType.MANUAL_RERUN) {
            return "Manual rerun request accepted";
        }

        return "Webhook payload accepted";
    }

    private String buildCommentEvidence(GitHubCommentExecutionResult executionResult) {
        if (!executionResult.isWritePerformed()) {
            return "executionControlMode=" + executionResult.getExecutionControlMode().name()
                    + ", writePerformed=false, writeSkipReason=" + executionResult.getWriteSkipReason().name()
                    + ", preparedInlineComments=" + executionResult.getPreparedInlineCommentCount();
        }

        GitHubCommentResponse response = executionResult.requireGitHubCommentResponse();
        return "reviewCommentId=" + response.reviewCommentId() + ", inlineComments=" + response.postedInlineComments().size();
    }

    private String buildReviewOutputSummary(Review review) {
        return "review generated with " + review.getInlineComments().size() + " inline comments";
    }

    private String buildCommentInputSummary(GitHubCommentExecutionResult executionResult) {
        return "execution control result recorded";
    }

    private String buildCommentOutputSummary(GitHubCommentExecutionResult executionResult) {
        if (!executionResult.isWritePerformed()) {
            return "write skipped because " + executionResult.getWriteSkipReason().name();
        }

        GitHubCommentResponse response = executionResult.requireGitHubCommentResponse();
        return "main comment " + response.reviewCommentId() + " and " + response.postedInlineComments().size() + " inline comments posted";
    }

    private String buildErrorSummary(Exception exception) {
        return exception.getClass().getSimpleName() + ": " + exception.getMessage();
    }

    private Map<String, Object> buildWebhookSnapshot(String deliveryId, GitHubEventServiceRequest request) {
        Map<String, Object> snapshot = new LinkedHashMap<>();
        snapshot.put("deliveryId", deliveryId);
        snapshot.put("executionStartType", ExecutionStartType.WEBHOOK.name());
        snapshot.put("eventType", request.getGitHubEventType().name());
        snapshot.put("action", request.getAction());
        snapshot.put("repositoryName", request.getRepositoryName());
        snapshot.put("pullRequestNumber", request.getPullRequestNumber());
        snapshot.put("installationId", request.getInstallationId());
        snapshot.put("executionControlMode", request.getExecutionControlMode().name());
        return snapshot;
    }

    private Map<String, Object> buildManualRerunSnapshot(ManualRerunServiceRequest request, String deliveryId) {
        Map<String, Object> snapshot = new LinkedHashMap<>();
        snapshot.put("deliveryId", deliveryId);
        snapshot.put("executionStartType", ExecutionStartType.MANUAL_RERUN.name());
        snapshot.put("eventType", "PULL_REQUEST");
        snapshot.put("action", "manual_rerun");
        snapshot.put("repositoryName", request.getRepositoryName());
        snapshot.put("pullRequestNumber", request.getPullRequestNumber());
        snapshot.put("installationId", request.getInstallationId());
        snapshot.put("executionControlMode", request.getExecutionControlMode().name());
        return snapshot;
    }

    private Map<String, Object> buildReviewSnapshot(Review review) {
        Map<String, Object> snapshot = new LinkedHashMap<>();
        snapshot.put("repositoryName", review.getRepositoryName());
        snapshot.put("pullRequestNumber", review.getPullRequestNumber());
        snapshot.put("reviewBodyLength", review.getReviewBody().length());
        snapshot.put("inlineCommentCount", review.getInlineComments().size());
        return snapshot;
    }

    private Map<String, Object> buildCommentSnapshot(GitHubCommentExecutionResult executionResult) {
        Map<String, Object> snapshot = new LinkedHashMap<>();
        snapshot.put("executionControlMode", executionResult.getExecutionControlMode().name());
        snapshot.put("writePerformed", executionResult.isWritePerformed());
        snapshot.put("writeSkipReason", getWriteSkipReasonName(executionResult.getWriteSkipReason()));
        snapshot.put("preparedInlineCommentCount", executionResult.getPreparedInlineCommentCount());
        if (executionResult.isWritePerformed()) {
            GitHubCommentResponse response = executionResult.requireGitHubCommentResponse();
            snapshot.put("reviewCommentId", response.reviewCommentId());
            snapshot.put("reviewCommentUrl", response.reviewCommentUrl());
            snapshot.put("postedInlineCommentCount", response.postedInlineComments().size());
        }
        return snapshot;
    }

    private Map<String, Object> buildErrorSnapshot(Exception exception) {
        Map<String, Object> snapshot = new LinkedHashMap<>();
        snapshot.put("errorType", exception.getClass().getName());
        snapshot.put("message", exception.getMessage());
        return snapshot;
    }

    private String toJson(Map<String, Object> payload) {
        try {
            return objectMapper.writeValueAsString(payload);
        } catch (JsonProcessingException exception) {
            return "{\"serializationError\":\"agent-runtime-payload\"}";
        }
    }

    private String getWriteSkipReasonName(@Nullable GitHubWriteSkipReason writeSkipReason) {
        if (writeSkipReason == null) {
            return null;
        }

        return writeSkipReason.name();
    }
}
