package com.agilerunner.api.service.dto;

public record PostedInlineComment(
        long id,
        String htmlUrl
) {
    public static PostedInlineComment of(long id, String htmlUrl) {
        return new PostedInlineComment(id, htmlUrl);
    }
}
