package com.agilerunner.api.service.dto;

public record GitHubCommentResponse(long id, String html, String message) {

    public static GitHubCommentResponse of(long id, String html, String message) {
        return new GitHubCommentResponse(id, html, message);
    }
}
