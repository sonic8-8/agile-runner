package com.agilerunner.api.service.dto;

import com.agilerunner.GitHubEventType;

import java.util.Map;

public record GitHubEventServiceRequest(
        GitHubEventType gitHubEventType,
        Map<String, Object> payload,
        long installationId
) {
    public static GitHubEventServiceRequest of(GitHubEventType gitHubEventType, Map<String, Object> payload, long installationId) {
        return new GitHubEventServiceRequest(gitHubEventType, payload, installationId);
    }
}
