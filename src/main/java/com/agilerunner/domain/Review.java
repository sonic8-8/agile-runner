package com.agilerunner.domain;

import com.agilerunner.api.service.dto.InlineCommentResponse;
import com.agilerunner.api.service.dto.ReviewResponse;

import java.util.List;
import java.util.Optional;

public class Review {
    public static final int MAX_INLINE_COMMENT_COUNT = 5;
    public static final int MIN_INLINE_COMMENT_LINE = 1;

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

    public static Review from(String repositoryName, int pullRequestNumber, ReviewResponse reviewResponse) {
        if (reviewResponse == null) {
            return Review.of(repositoryName, pullRequestNumber, "", List.of());
        }

        List<InlineComment> validatedInlineComments = validateInlineReviews(reviewResponse);

        return Review.of(repositoryName, pullRequestNumber, reviewResponse.reviewBody(), validatedInlineComments);
    }

    private static List<InlineComment> validateInlineReviews(ReviewResponse reviewResponse) {
        List<InlineCommentResponse> rawInlineComments = Optional.ofNullable(reviewResponse.inlineComments()).orElse(List.of());
        return rawInlineComments.stream()
                .filter(inlineCommentResponse -> inlineCommentResponse.path() != null && !inlineCommentResponse.path().isBlank())
                .filter(inlineCommentResponse -> inlineCommentResponse.line() >= MIN_INLINE_COMMENT_LINE)
                .filter(inlineCommentResponse -> inlineCommentResponse.body() != null && !inlineCommentResponse.body().isBlank())
                .map(inlineCommentResponse -> InlineComment.of(inlineCommentResponse.path(), inlineCommentResponse.line(), inlineCommentResponse.body()))
                .limit(MAX_INLINE_COMMENT_COUNT)
                .toList();
    }
}