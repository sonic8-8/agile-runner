package com.agilerunner.api.service;

import com.agilerunner.api.service.dto.GitHubCommentResponse;
import com.agilerunner.api.service.dto.GitHubEventServiceRequest;
import com.agilerunner.api.service.dto.PostedInlineCommentResponse;
import com.agilerunner.config.GitHubClientFactory;
import com.agilerunner.domain.InlineComment;
import com.agilerunner.domain.Review;
import lombok.RequiredArgsConstructor;
import org.kohsuke.github.*;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.ThreadLocalRandom;

@Service
@RequiredArgsConstructor
public class GitHubCommentService {

    private final GitHubClientFactory gitHubClientFactory;

    public GitHubCommentResponse comment(Review review, GitHubEventServiceRequest request) {
        try {
            GitHub gitHub = gitHubClientFactory.createGitHubClient(request.installationId());

            GHRepository repository = gitHub.getRepository(review.getRepositoryName());
            GHPullRequest pullRequest = repository.getPullRequest(review.getPullRequestNumber());

            GHIssueComment comment = pullRequest.comment(review.getReviewBody());

            ArrayList<PostedInlineCommentResponse> postedInlineCommentResponses = new ArrayList<>();

            String commitId = pullRequest.getHead().getSha();
            for (InlineComment inlineComment : review.getInlineComments()) {

                randomDelay();

                GHPullRequestReviewComment reviewComment =
                        retryCreateReviewComment(pullRequest, inlineComment, commitId);

                postedInlineCommentResponses.add(new PostedInlineCommentResponse(
                        reviewComment.getId(),
                        reviewComment.getHtmlUrl().toString()
                ));
            }

            return GitHubCommentResponse.of(
                    comment.getId(),
                    comment.getHtmlUrl().toString(),
                    postedInlineCommentResponses,
                    "리뷰 코멘트가 성공적으로 등록되었습니다."
            );
        } catch (Exception e) {
            throw new RuntimeException("GitHub 코멘트 등록 실패", e);
        }
    }

    private void randomDelay() {
        try {
            long delay = ThreadLocalRandom.current().nextLong(350, 650);
            Thread.sleep(delay);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private GHPullRequestReviewComment retryCreateReviewComment(
            GHPullRequest pullRequest,
            InlineComment inlineComment,
            String commitId
    ) throws InterruptedException {

        int attempts = 0;
        int maxAttempts = 5;

        while (true) {
            try {
                return pullRequest.createReviewComment(
                        inlineComment.getBody(),
                        commitId,
                        inlineComment.getPath(),
                        inlineComment.getLine()
                );

            } catch (IOException e) {
                attempts++;
                if (attempts >= maxAttempts) {
                    throw new RuntimeException("GitHub API 재시도 초과", e);
                }

                long backoff = (long) (Math.pow(2, attempts) * 1000L);
                Thread.sleep(backoff);
            }
        }
    }
}