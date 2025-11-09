package com.agilerunner.api.controller;

import com.agilerunner.api.controller.dto.GitHubEventRequest;
import com.agilerunner.api.service.GitHubCommentService;
import com.agilerunner.api.service.OpenAiService;
import com.agilerunner.api.service.dto.GitHubCommentResponse;
import com.agilerunner.domain.Review;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/webhook/github")
public class GitHubWebhookController {

    private final OpenAiService openAiService;
    private final GitHubCommentService gitHubCommentService;

    @PostMapping
    public ResponseEntity<GitHubCommentResponse> handleGitHubEvent(GitHubEventRequest request) {
        Review review = openAiService.generateReview(request.toService());
        GitHubCommentResponse response = gitHubCommentService.comment(review);
        return ResponseEntity.ok(response);
    }
}