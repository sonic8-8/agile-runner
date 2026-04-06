package com.agilerunner.api.service.agentruntime;

import com.agilerunner.api.service.dto.GitHubCommentResponse;
import com.agilerunner.api.service.dto.GitHubEventServiceRequest;
import com.agilerunner.client.agentruntime.AgentRuntimeRepository;
import com.agilerunner.domain.Review;
import com.agilerunner.domain.agentruntime.AgentExecutionLog;
import com.agilerunner.domain.agentruntime.AgentExecutionStatus;
import com.agilerunner.domain.agentruntime.AgentRole;
import com.agilerunner.domain.agentruntime.CriteriaCategory;
import com.agilerunner.domain.agentruntime.CriteriaStatus;
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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class AgentRuntimeService {
    public static final String STEP_WEBHOOK_ACCEPTED = "webhook-accepted";
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
        );

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
                )
        );

        return webhookExecution;
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
                        "Webhook payload accepted",
                        buildReviewEvidence(review),
                        null
                )
        );
        agentRuntimeRepository.appendExecutionLog(
                AgentExecutionLog.of(
                        webhookExecution.getTaskKey(),
                        issueNumber,
                        webhookExecution.getExecutionKey(),
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
                )
        );
    }

    public void recordCommentPosted(WebhookExecution webhookExecution, GitHubCommentResponse response) {
        if (!isEnabled()) {
            return;
        }

        LocalDateTime now = LocalDateTime.now();
        long issueNumber = webhookExecution.getPullRequestNumber();
        TaskRuntimeState existingTaskRuntimeState = agentRuntimeRepository.findTaskRuntimeState(webhookExecution.getTaskKey()).orElse(null);
        WebhookExecution completedWebhookExecution = webhookExecution.complete(WebhookExecutionStatus.SUCCEEDED, null, null, now);

        agentRuntimeRepository.replaceValidationCriteria(
                webhookExecution.getTaskKey(),
                buildCriteria(
                        webhookExecution.getTaskKey(),
                        CriteriaStatus.PASSED,
                        CriteriaStatus.PASSED,
                        CriteriaStatus.PASSED,
                        "Webhook payload accepted",
                        "Review generated successfully",
                        buildCommentEvidence(response)
                )
        );
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
        agentRuntimeRepository.appendExecutionLog(
                AgentExecutionLog.of(
                        webhookExecution.getTaskKey(),
                        issueNumber,
                        webhookExecution.getExecutionKey(),
                        AgentRole.ORCHESTRATOR,
                        STEP_COMMENT_POSTED,
                        AgentExecutionStatus.SUCCEEDED,
                        "GitHub review comments posted",
                        buildCommentOutputSummary(response),
                        null,
                        null,
                        toJson(buildCommentSnapshot(response)),
                        now,
                        now
                )
        );
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
                        "Webhook payload accepted",
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
                "Webhook payload can be parsed into repository, pull request, and installation context.",
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

    private String buildTaskTitle(String repositoryName, int pullRequestNumber) {
        return "GitHub PR review for " + repositoryName + "#" + pullRequestNumber;
    }

    private String buildReviewEvidence(Review review) {
        return "bodyLength=" + review.getReviewBody().length() + ", inlineComments=" + review.getInlineComments().size();
    }

    private String buildCommentEvidence(GitHubCommentResponse response) {
        return "reviewCommentId=" + response.reviewCommentId() + ", inlineComments=" + response.postedInlineComments().size();
    }

    private String buildReviewOutputSummary(Review review) {
        return "review generated with " + review.getInlineComments().size() + " inline comments";
    }

    private String buildCommentOutputSummary(GitHubCommentResponse response) {
        return "main comment " + response.reviewCommentId() + " and " + response.postedInlineComments().size() + " inline comments posted";
    }

    private String buildErrorSummary(Exception exception) {
        return exception.getClass().getSimpleName() + ": " + exception.getMessage();
    }

    private Map<String, Object> buildWebhookSnapshot(String deliveryId, GitHubEventServiceRequest request) {
        Map<String, Object> snapshot = new LinkedHashMap<>();
        snapshot.put("deliveryId", deliveryId);
        snapshot.put("eventType", request.getGitHubEventType().name());
        snapshot.put("action", request.getAction());
        snapshot.put("repositoryName", request.getRepositoryName());
        snapshot.put("pullRequestNumber", request.getPullRequestNumber());
        snapshot.put("installationId", request.getInstallationId());
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

    private Map<String, Object> buildCommentSnapshot(GitHubCommentResponse response) {
        Map<String, Object> snapshot = new LinkedHashMap<>();
        snapshot.put("reviewCommentId", response.reviewCommentId());
        snapshot.put("reviewCommentUrl", response.reviewCommentUrl());
        snapshot.put("postedInlineCommentCount", response.postedInlineComments().size());
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
}
