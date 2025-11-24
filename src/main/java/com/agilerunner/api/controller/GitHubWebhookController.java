package com.agilerunner.api.controller;

import com.agilerunner.api.controller.dto.GitHubEventRequest;
import com.agilerunner.api.service.GitHubCommentService;
import com.agilerunner.api.service.OpenAiService;
import com.agilerunner.api.service.dto.GitHubCommentResponse;
import com.agilerunner.api.service.dto.GitHubEventServiceRequest;
import com.agilerunner.domain.Review;
import com.agilerunner.util.WebhookDeliveryCache;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/webhook/github")
public class GitHubWebhookController {

    private static final String PULL_REQUEST_EVENT = "pull_request";

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

        Review review = openAiService.generateReview(serviceRequest);
        if (review == null) {
            return ResponseEntity.ok().build();
        }

        GitHubCommentResponse response = gitHubCommentService.comment(review, serviceRequest);
        deliveryCache.record(deliveryId);

        return ResponseEntity.ok(response);
    }
}