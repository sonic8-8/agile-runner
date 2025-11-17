package com.agilerunner.api.service;

import com.agilerunner.api.service.dto.GitHubCommentResponse;
import com.agilerunner.api.service.dto.GitHubEventServiceRequest;
import com.agilerunner.api.service.dto.PostedInlineCommentResponse;
import com.agilerunner.config.GitHubClientFactory;
import com.agilerunner.domain.InlineComment;
import com.agilerunner.domain.Review;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.kohsuke.github.*;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.ThreadLocalRandom;

@Service
@RequiredArgsConstructor
@Slf4j
public class GitHubCommentService {

    private final GitHubClientFactory gitHubClientFactory;

    public GitHubCommentResponse comment(Review review, GitHubEventServiceRequest request) {
        try {
            GitHub gitHub = gitHubClientFactory.createGitHubClient(request.installationId());

            GHRepository repository = gitHub.getRepository(review.getRepositoryName());
            GHPullRequest pullRequest = repository.getPullRequest(review.getPullRequestNumber());

            GHIssueComment comment = null;
            try {
                comment = pullRequest.comment(review.getReviewBody());
            } catch (HttpException e) {
                if (isSecondaryRateLimit(e)) {
                    log.warn("Secondary rate limit 발생! 전체 작업 중단");
                    return null;
                }
                log.error("리뷰 본문 코멘트 실패", e);
                return null;
            }

            if (comment == null) {
                return null;
            }

            ArrayList<PostedInlineCommentResponse> postedInlineCommentResponses = new ArrayList<>();

            String commitId = pullRequest.getHead().getSha();
            for (InlineComment inlineComment : review.getInlineComments()) {

                randomDelay();

                GHPullRequestReviewComment reviewComment =
                        tryCreateReviewComment(pullRequest, inlineComment, commitId);

                if (reviewComment == null) {
                    log.warn("인라인 코멘트 생성 실패. 계속 진행");
                    continue;
                }

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
            log.error("GitHub 코멘트 등록 실패", e);
            return null;
        }
    }


    private boolean isSecondaryRateLimit(HttpException e) {
        return e.getResponseCode() == 403 &&
                e.getMessage().contains("secondary rate limit");
    }

    private void randomDelay() {
        try {
            long delay = ThreadLocalRandom.current().nextLong(350, 650);
            Thread.sleep(delay);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private GHPullRequestReviewComment tryCreateReviewComment(
            GHPullRequest pullRequest,
            InlineComment inlineComment,
            String commitId
    ) {

        int attempts = 0;
        int maxAttempts = 5;

        while (attempts < maxAttempts) {
            try {
                return pullRequest.createReviewComment(
                        inlineComment.getBody(),
                        commitId,
                        inlineComment.getPath(),
                        inlineComment.getLine()
                );

            } catch (HttpException e) {
                if (isSecondaryRateLimit(e)) {
                    log.warn("Secondary rate limit 발생! 재시도 중단");
                    return null;
                }
                attempts++;
                exponentialBackoff(attempts);
            } catch (IOException e) {
                attempts++;
                exponentialBackoff(attempts);
            }
        }
        return null;
    }

    private void exponentialBackoff(int attempt) {
        try {
            long ms = (long) Math.pow(2, attempt) * 1000;
            Thread.sleep(ms);
        } catch (InterruptedException ignored) {
        }
    }
}