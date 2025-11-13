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

import java.util.ArrayList;

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
                GHPullRequestReviewComment reviewComment = pullRequest.createReviewComment(
                        inlineComment.getBody(),
                        commitId,
                        inlineComment.getPath(),
                        inlineComment.getLine()
                );

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
}
