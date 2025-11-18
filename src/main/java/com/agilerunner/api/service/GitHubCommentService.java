package com.agilerunner.api.service;

import com.agilerunner.api.service.dto.GitHubCommentResponse;
import com.agilerunner.api.service.dto.GitHubEventServiceRequest;
import com.agilerunner.api.service.dto.PostedInlineComment;
import com.agilerunner.config.GitHubClientFactory;
import com.agilerunner.domain.InlineComment;
import com.agilerunner.domain.Review;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.kohsuke.github.*;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

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

            GHIssueComment comment = pullRequest.comment(review.getReviewBody());
            List<PostedInlineComment> postedInlineComments = postInlineComments(review, pullRequest);

            return GitHubCommentResponse.of(
                    comment.getId(),
                    comment.getHtmlUrl().toString(),
                    postedInlineComments,
                    "리뷰 코멘트가 성공적으로 등록되었습니다."
            );
        } catch (Exception e) {
            log.error("GitHub 코멘트 등록 실패, repository={}, PR={}",
                    review.getRepositoryName(),
                    review.getPullRequestNumber(),
                    e);
            throw new RuntimeException("GitHub 코멘트 등록 실패");
        }
    }

    private static List<PostedInlineComment> postInlineComments(Review review, GHPullRequest pullRequest) {
        List<PostedInlineComment> postedInlineComments = new ArrayList<>();

        String headSha = pullRequest.getHead().getSha();

        for (InlineComment inlineComment : review.getInlineComments()) {
            try {
                GHPullRequestReviewComment reviewComment =
                        pullRequest.createReviewComment(
                                inlineComment.getBody(),
                                headSha,
                                inlineComment.getPath(),
                                inlineComment.getLine()
                        );

                postedInlineComments.add(
                        PostedInlineComment.of(
                                reviewComment.getId(),
                                reviewComment.getHtmlUrl().toString()
                        ));
            } catch (IOException e) {
                log.error("인라인 코멘트 작성 실패", e);
            }
        }
        return postedInlineComments;
    }
}