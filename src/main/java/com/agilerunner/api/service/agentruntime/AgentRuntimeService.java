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
import com.agilerunner.domain.agentruntime.EvaluationCriteria;
import com.agilerunner.domain.agentruntime.ReviewRun;
import com.agilerunner.domain.agentruntime.ReviewRunStatus;
import com.agilerunner.domain.agentruntime.TaskState;
import com.agilerunner.domain.agentruntime.TaskStateStatus;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
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

    public AgentRuntimeService(@Autowired(required = false) @Nullable AgentRuntimeRepository agentRuntimeRepository,
                               ObjectMapper objectMapper) {
        this.agentRuntimeRepository = agentRuntimeRepository;
        this.objectMapper = objectMapper;
    }

    public ReviewRun startReviewRun(String deliveryId, GitHubEventServiceRequest request) {
        LocalDateTime now = LocalDateTime.now();
        String repositoryName = request.getRepositoryName();
        int pullRequestNumber = request.getPullRequestNumber();
        long issueNumber = pullRequestNumber;
        String taskKey = buildTaskKey(repositoryName, pullRequestNumber);
        String runKey = buildRunKey(deliveryId);

        ReviewRun reviewRun = ReviewRun.start(
                runKey,
                taskKey,
                deliveryId,
                repositoryName,
                pullRequestNumber,
                request.getGitHubEventType().name(),
                request.getAction(),
                now
        );

        if (!isEnabled()) {
            return reviewRun;
        }

        TaskState existingTaskState = agentRuntimeRepository.findTaskState(taskKey).orElse(null);
        TaskState taskState = TaskState.of(
                taskKey,
                issueNumber,
                buildTaskTitle(repositoryName, pullRequestNumber),
                TaskStateStatus.IN_PROGRESS,
                getNextRetryCount(existingTaskState),
                AgentRole.ORCHESTRATOR,
                getStartedAt(existingTaskState, now),
                null
        );

        agentRuntimeRepository.upsertTaskState(taskState);
        agentRuntimeRepository.replaceEvaluationCriteria(
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
        agentRuntimeRepository.upsertReviewRun(reviewRun);
        agentRuntimeRepository.appendExecutionLog(
                AgentExecutionLog.of(
                        taskKey,
                        issueNumber,
                        runKey,
                        AgentRole.ORCHESTRATOR,
                        STEP_WEBHOOK_ACCEPTED,
                        AgentExecutionStatus.SUCCEEDED,
                        "GitHub pull_request webhook accepted",
                        "task and review run initialized",
                        null,
                        toJson(buildWebhookSnapshot(deliveryId, request)),
                        now,
                        now
                )
        );

        return reviewRun;
    }

    public void recordReviewGenerated(ReviewRun reviewRun, Review review) {
        if (!isEnabled()) {
            return;
        }

        LocalDateTime now = LocalDateTime.now();
        long issueNumber = reviewRun.getPullRequestNumber();

        agentRuntimeRepository.replaceEvaluationCriteria(
                reviewRun.getTaskKey(),
                buildCriteria(
                        reviewRun.getTaskKey(),
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
                        reviewRun.getTaskKey(),
                        issueNumber,
                        reviewRun.getRunKey(),
                        AgentRole.ORCHESTRATOR,
                        STEP_REVIEW_GENERATED,
                        AgentExecutionStatus.SUCCEEDED,
                        "Review prompt executed",
                        buildReviewOutputSummary(review),
                        null,
                        toJson(buildReviewSnapshot(review)),
                        now,
                        now
                )
        );
    }

    public void recordCommentPosted(ReviewRun reviewRun, GitHubCommentResponse response) {
        if (!isEnabled()) {
            return;
        }

        LocalDateTime now = LocalDateTime.now();
        long issueNumber = reviewRun.getPullRequestNumber();
        TaskState existingTaskState = agentRuntimeRepository.findTaskState(reviewRun.getTaskKey()).orElse(null);
        ReviewRun completedRun = reviewRun.complete(ReviewRunStatus.SUCCEEDED, null, now);

        agentRuntimeRepository.replaceEvaluationCriteria(
                reviewRun.getTaskKey(),
                buildCriteria(
                        reviewRun.getTaskKey(),
                        CriteriaStatus.PASSED,
                        CriteriaStatus.PASSED,
                        CriteriaStatus.PASSED,
                        "Webhook payload accepted",
                        "Review generated successfully",
                        buildCommentEvidence(response)
                )
        );
        agentRuntimeRepository.upsertReviewRun(completedRun);
        agentRuntimeRepository.upsertTaskState(
                TaskState.of(
                        reviewRun.getTaskKey(),
                        issueNumber,
                        buildTaskTitle(reviewRun.getRepositoryName(), reviewRun.getPullRequestNumber()),
                        TaskStateStatus.DONE,
                        getCurrentRetryCount(existingTaskState),
                        AgentRole.ORCHESTRATOR,
                        getStartedAt(existingTaskState, reviewRun.getStartedAt()),
                        now
                )
        );
        agentRuntimeRepository.appendExecutionLog(
                AgentExecutionLog.of(
                        reviewRun.getTaskKey(),
                        issueNumber,
                        reviewRun.getRunKey(),
                        AgentRole.ORCHESTRATOR,
                        STEP_COMMENT_POSTED,
                        AgentExecutionStatus.SUCCEEDED,
                        "GitHub review comments posted",
                        buildCommentOutputSummary(response),
                        null,
                        toJson(buildCommentSnapshot(response)),
                        now,
                        now
                )
        );
    }

    public void recordFailure(ReviewRun reviewRun, String stepName, Exception exception) {
        if (!isEnabled()) {
            return;
        }

        LocalDateTime now = LocalDateTime.now();
        long issueNumber = reviewRun.getPullRequestNumber();
        TaskState existingTaskState = agentRuntimeRepository.findTaskState(reviewRun.getTaskKey()).orElse(null);
        ReviewRun failedRun = reviewRun.complete(ReviewRunStatus.FAILED, exception.getMessage(), now);

        agentRuntimeRepository.replaceEvaluationCriteria(
                reviewRun.getTaskKey(),
                buildCriteria(
                        reviewRun.getTaskKey(),
                        CriteriaStatus.PASSED,
                        resolveReviewCriteriaStatus(stepName),
                        resolveCommentCriteriaStatus(stepName),
                        "Webhook payload accepted",
                        resolveReviewEvidence(stepName, exception),
                        resolveCommentEvidence(stepName, exception)
                )
        );
        agentRuntimeRepository.upsertReviewRun(failedRun);
        agentRuntimeRepository.upsertTaskState(
                TaskState.of(
                        reviewRun.getTaskKey(),
                        issueNumber,
                        buildTaskTitle(reviewRun.getRepositoryName(), reviewRun.getPullRequestNumber()),
                        TaskStateStatus.FAILED,
                        getCurrentRetryCount(existingTaskState),
                        AgentRole.ORCHESTRATOR,
                        getStartedAt(existingTaskState, reviewRun.getStartedAt()),
                        now
                )
        );
        agentRuntimeRepository.appendExecutionLog(
                AgentExecutionLog.of(
                        reviewRun.getTaskKey(),
                        issueNumber,
                        reviewRun.getRunKey(),
                        AgentRole.ORCHESTRATOR,
                        stepName,
                        AgentExecutionStatus.FAILED,
                        "Runtime step failed",
                        null,
                        buildErrorSummary(exception),
                        toJson(buildErrorSnapshot(exception)),
                        now,
                        now
                )
        );
    }

    private boolean isEnabled() {
        return agentRuntimeRepository != null;
    }

    private int getNextRetryCount(@Nullable TaskState existingTaskState) {
        if (existingTaskState == null) {
            return 0;
        }

        return existingTaskState.getRetryCount() + 1;
    }

    private int getCurrentRetryCount(@Nullable TaskState existingTaskState) {
        if (existingTaskState == null) {
            return 0;
        }

        return existingTaskState.getRetryCount();
    }

    private LocalDateTime getStartedAt(@Nullable TaskState existingTaskState, LocalDateTime fallback) {
        if (existingTaskState == null || existingTaskState.getStartedAt() == null) {
            return fallback;
        }

        return existingTaskState.getStartedAt();
    }

    private List<EvaluationCriteria> buildCriteria(String taskKey,
                                                   CriteriaStatus payloadAcceptedStatus,
                                                   CriteriaStatus reviewGeneratedStatus,
                                                   CriteriaStatus commentPostedStatus,
                                                   String payloadEvidence,
                                                   String reviewEvidence,
                                                   String commentEvidence) {
        List<EvaluationCriteria> criteria = new ArrayList<>();
        criteria.add(EvaluationCriteria.of(
                taskKey,
                CRITERIA_PAYLOAD_ACCEPTED,
                CriteriaCategory.REQUIRED,
                "Webhook payload can be parsed into repository, pull request, and installation context.",
                payloadAcceptedStatus,
                payloadEvidence
        ));
        criteria.add(EvaluationCriteria.of(
                taskKey,
                CRITERIA_REVIEW_GENERATED,
                CriteriaCategory.REQUIRED,
                "OpenAI review output is generated for the target pull request.",
                reviewGeneratedStatus,
                reviewEvidence
        ));
        criteria.add(EvaluationCriteria.of(
                taskKey,
                CRITERIA_COMMENT_POSTED,
                CriteriaCategory.REQUIRED,
                "Generated review comments are posted back to GitHub.",
                commentPostedStatus,
                commentEvidence
        ));
        return criteria;
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

    private String buildRunKey(String deliveryId) {
        return "RUN:" + deliveryId;
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
