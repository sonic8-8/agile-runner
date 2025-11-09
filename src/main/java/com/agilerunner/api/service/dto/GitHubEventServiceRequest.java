package com.agilerunner.api.service.dto;

import com.agilerunner.GitHubEventType;

public record GitHubEventServiceRequest(
        GitHubEventType gitHubEventType,
        String payload
) {
    public static GitHubEventServiceRequest of(GitHubEventType gitHubEventType, String payload) {
        return new GitHubEventServiceRequest(gitHubEventType, payload);
    }
}
