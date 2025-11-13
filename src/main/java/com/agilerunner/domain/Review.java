package com.agilerunner.domain;

import com.agilerunner.api.service.dto.ReviewResponse;

import java.util.List;

public class Review {
    private String repositoryName;
    private int pullRequestNumber;
    private String reviewBody;
    private List<InlineComment> inlineComments;

    public Review(String repositoryName, int pullRequestNumber, String reviewBody, List<InlineComment> inlineComments) {
        this.repositoryName = repositoryName;
        this.pullRequestNumber = pullRequestNumber;
        this.reviewBody = reviewBody;
        this.inlineComments = inlineComments;
    }

    public String getRepositoryName() {
        return repositoryName;
    }

    public int getPullRequestNumber() {
        return pullRequestNumber;
    }

    public String getReviewBody() {
        return reviewBody;
    }

    public List<InlineComment> getInlineComments() {
        return inlineComments;
    }

    public static Review of(String repositoryName, int pullRequestNumber, String review, List<InlineComment> inlineComments) {
        return new Review(repositoryName, pullRequestNumber, review, inlineComments);
    }

    public static Review from(String repositoryName, int pullRequestNumber, ReviewResponse response) {
        List<InlineComment> inlineComments = response.inlineComments().stream()
                .map(inlineCommentResponse
                                -> InlineComment.of(
                                inlineCommentResponse.path(),
                                inlineCommentResponse.line(),
                                inlineCommentResponse.body()
                        )
                )
                .toList();

        return Review.of(repositoryName, pullRequestNumber, response.reviewBody(), inlineComments);
    }
}