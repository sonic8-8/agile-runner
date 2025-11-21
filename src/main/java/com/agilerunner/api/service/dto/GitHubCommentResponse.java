package com.agilerunner.api.service.dto;

import java.util.List;

public record GitHubCommentResponse(
        long reviewCommentId,
        String reviewCommentUrl,
        List<PostedInlineComment> postedInlineComments,
        String message
) {

    public static GitHubCommentResponse of(
            long reviewId,
            String reviewUrl,
            List<PostedInlineComment> inlineComments,
            String message) {
        return new GitHubCommentResponse(reviewId, reviewUrl, inlineComments, message);
    }
}
