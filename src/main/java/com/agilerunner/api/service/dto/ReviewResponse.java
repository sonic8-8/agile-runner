package com.agilerunner.api.service.dto;

import java.util.List;

public record ReviewResponse(
        String reviewBody,
        List<InlineCommentResponse> inlineComments
) {
}
