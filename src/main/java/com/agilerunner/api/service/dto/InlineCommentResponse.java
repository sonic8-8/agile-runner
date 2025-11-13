package com.agilerunner.api.service.dto;

public record InlineCommentResponse(
        String path,
        int line,
        String body
) {
}
