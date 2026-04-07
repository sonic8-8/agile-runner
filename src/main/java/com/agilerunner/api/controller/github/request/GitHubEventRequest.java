package com.agilerunner.api.controller.github.request;

import com.agilerunner.GitHubEventType;
import com.agilerunner.api.service.github.request.GitHubEventServiceRequest;
import com.agilerunner.domain.exception.AgileRunnerException;
import com.agilerunner.domain.exception.ErrorCode;

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
        if (installation == null) {
            throw new AgileRunnerException(
                    ErrorCode.GITHUB_INSTALLATION_ID_MISSING,
                    "payload에서 installation id를 찾을 수 없습니다."
            );
        }

        Object installationId = installation.get("id");
        if (installationId instanceof Number number) {
            return number.longValue();
        }

        throw new AgileRunnerException(
                ErrorCode.GITHUB_INSTALLATION_ID_MISSING,
                "payload에서 installation id를 찾을 수 없습니다."
        );
    }

    public GitHubEventServiceRequest toService() {
        return GitHubEventServiceRequest.of(GitHubEventType.of(eventType), payload, getInstallationId());
    }

}
