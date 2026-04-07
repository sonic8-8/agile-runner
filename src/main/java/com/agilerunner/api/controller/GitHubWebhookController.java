package com.agilerunner.api.controller;

import com.agilerunner.api.controller.github.request.GitHubEventRequest;
import com.agilerunner.api.service.agentruntime.AgentRuntimeService;
import com.agilerunner.api.service.GitHubCommentService;
import com.agilerunner.api.service.OpenAiService;
import com.agilerunner.api.service.dto.GitHubCommentResponse;
import com.agilerunner.api.service.github.response.GitHubCommentExecutionResult;
import com.agilerunner.api.service.github.request.GitHubEventServiceRequest;
import com.agilerunner.domain.Review;
import com.agilerunner.domain.agentruntime.WebhookExecution;
import com.agilerunner.domain.exception.AgileRunnerException;
import com.agilerunner.domain.exception.ErrorCode;
import com.agilerunner.util.WebhookDeliveryCache;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/webhook/github")
public class GitHubWebhookController {

    private static final String PULL_REQUEST_EVENT = "pull_request";
    private static final Logger log = LoggerFactory.getLogger(GitHubWebhookController.class);

    private final AgentRuntimeService agentRuntimeService;
    private final OpenAiService openAiService;
    private final GitHubCommentService gitHubCommentService;
    private final WebhookDeliveryCache deliveryCache;

    @PostMapping
    public ResponseEntity<GitHubCommentResponse> handleGitHubEvent(
            @RequestHeader("X-GitHub-Event") String eventType,
            @RequestHeader("X-GitHub-Delivery") String deliveryId,
            @RequestBody Map<String, Object> payload) {

        if (deliveryCache.isDuplicate(deliveryId)) {
            return ResponseEntity.ok().build();
        }
        if (isNotPullRequest(eventType)) {
            return ResponseEntity.ok().build();
        }

        GitHubEventRequest request = GitHubEventRequest.of(eventType, payload);
        return handlePullRequestEvent(deliveryId, request);
    }

    private boolean isNotPullRequest(String eventType) {
        return !PULL_REQUEST_EVENT.equals(eventType);
    }

    private ResponseEntity<GitHubCommentResponse> handlePullRequestEvent(String deliveryId, GitHubEventRequest request) {
        GitHubEventServiceRequest serviceRequest = request.toService();
        WebhookExecution webhookExecution = agentRuntimeService.startWebhookExecution(deliveryId, serviceRequest);

        Review review;
        try {
            review = openAiService.generateReview(serviceRequest);
        } catch (Exception exception) {
            agentRuntimeService.recordFailure(webhookExecution, AgentRuntimeService.STEP_REVIEW_GENERATED, exception);
            throw exception;
        }

        if (review == null) {
            AgileRunnerException exception = new AgileRunnerException(
                    ErrorCode.OPENAI_REVIEW_FAILED,
                    "리뷰 생성 결과가 비어 있습니다."
            );
            agentRuntimeService.recordFailure(
                    webhookExecution,
                    AgentRuntimeService.STEP_REVIEW_GENERATED,
                    exception
            );
            return ResponseEntity.ok().build();
        }

        agentRuntimeService.recordReviewGenerated(webhookExecution, review);
        return postComments(deliveryId, serviceRequest, webhookExecution, review);
    }

    private ResponseEntity<GitHubCommentResponse> postComments(String deliveryId,
                                                               GitHubEventServiceRequest serviceRequest,
                                                               WebhookExecution webhookExecution,
                                                               Review review) {
        try {
            GitHubCommentExecutionResult executionResult = gitHubCommentService.execute(review, serviceRequest);
            GitHubCommentResponse response = executionResult.requireGitHubCommentResponse();
            deliveryCache.record(deliveryId);
            recordCommentPostedSafely(webhookExecution, response);
            return ResponseEntity.ok(response);
        } catch (Exception exception) {
            agentRuntimeService.recordFailure(webhookExecution, AgentRuntimeService.STEP_COMMENT_POSTED, exception);
            throw exception;
        }
    }

    private void recordCommentPostedSafely(WebhookExecution webhookExecution, GitHubCommentResponse response) {
        try {
            agentRuntimeService.recordCommentPosted(webhookExecution, response);
        } catch (Exception exception) {
            log.warn("GitHub 코멘트 등록 후 runtime 기록에 실패했습니다. deliveryId={}, executionKey={}",
                    webhookExecution.getDeliveryId(),
                    webhookExecution.getExecutionKey(),
                    exception);
        }
    }
}
