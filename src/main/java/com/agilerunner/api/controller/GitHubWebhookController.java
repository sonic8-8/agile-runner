package com.agilerunner.api.controller;

import com.agilerunner.api.controller.dto.GitHubEventRequest;
import com.agilerunner.api.service.GitHubCommentService;
import com.agilerunner.api.service.OpenAiService;
import com.agilerunner.api.service.dto.GitHubCommentResponse;
import com.agilerunner.domain.Review;
import com.agilerunner.util.WebhookDeliveryCache;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/webhook/github")
@Slf4j
public class GitHubWebhookController {

    private final OpenAiService openAiService;
    private final GitHubCommentService gitHubCommentService;
    private final WebhookDeliveryCache deliveryCache;

    @PostMapping
    public ResponseEntity<GitHubCommentResponse> handleGitHubEvent(
            @RequestHeader("X-GitHub-Event") String eventType,
            @RequestHeader("X-GitHub-Delivery") String deliveryId,
            @RequestBody Map<String, Object> payload) {

        if (deliveryCache.isProcessed(deliveryId)) {
            log.info("중복 webhook 요청 deliveryId={}", deliveryId);
            return ResponseEntity.ok(null);
        }

        GitHubEventRequest request = GitHubEventRequest.of(eventType, payload);

        Review review = openAiService.generateReview(request.toService());

        if (review == null) {
            log.info("리뷰 생성을 건너뜁니다.");
            return ResponseEntity.ok(null);
        }

        GitHubCommentResponse response = gitHubCommentService.comment(review, request.toService());

        deliveryCache.markProcessed(deliveryId);
        return ResponseEntity.ok(response);
    }
}