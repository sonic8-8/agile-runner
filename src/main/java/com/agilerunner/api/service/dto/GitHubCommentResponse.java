package com.agilerunner.api.service.dto;

import java.util.List;

public record GitHubCommentResponse(
        long reviewCommentId,
        String reviewCommentUrl,
        List<PostedInlineCommentResponse> postedInlineCommentResponses,
        String message
) {

    public static GitHubCommentResponse of(
            long reviewId,
            String reviewUrl,
            List<PostedInlineCommentResponse> inlineComments,
            String message) {
        return new GitHubCommentResponse(reviewId, reviewUrl, inlineComments, message);
    }
}
