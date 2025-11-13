package com.agilerunner.api.controller.dto;

import com.agilerunner.GitHubEventType;
import com.agilerunner.api.service.dto.GitHubEventServiceRequest;

import java.util.Map;

public class GitHubEventRequest {
    String eventType;
    Map<String, Object> payload;

    private GitHubEventRequest(String eventType, Map<String, Object> payload) {
        this.eventType = eventType;
        this.payload = payload;
    }

    public static GitHubEventRequest of(String eventType, Map<String, Object> payload) {
        return new GitHubEventRequest(eventType, payload);
    }

    public Long getInstallationId() {
        Map<String, Object> installation = (Map<String, Object>) payload.get("installation");
        if (installation != null) {
            return ((Number) installation.get("id")).longValue();
        }
        throw new IllegalStateException("payload에서 installaion을 찾을 수 없습니다.");
    }

    public GitHubEventServiceRequest toService() {
        return GitHubEventServiceRequest.of(GitHubEventType.of(eventType), payload, getInstallationId());
    }

}
