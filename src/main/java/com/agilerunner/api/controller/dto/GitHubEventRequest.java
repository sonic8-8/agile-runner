package com.agilerunner.api.controller.dto;

import com.agilerunner.GitHubEventType;
import com.agilerunner.api.service.dto.GitHubEventServiceRequest;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

public record GitHubEventRequest(
        @RequestHeader("X-GitHub-Event") String eventType,
        @RequestBody String payload
) {

    public static GitHubEventRequest of(String eventType, String payload) {
        return new GitHubEventRequest(eventType, payload);
    }

    public GitHubEventServiceRequest toService() {
        return GitHubEventServiceRequest.of(GitHubEventType.of(eventType), payload);
    }

}
