package com.agilerunner.api.service.github.request;

import com.agilerunner.GitHubEventType;
import com.agilerunner.domain.executioncontrol.ExecutionControlMode;
import lombok.Getter;

import java.util.Map;

@Getter
public class GitHubEventServiceRequest {
    private static final String KEY_ACTION = "action";
    private static final String KEY_REPOSITORY = "repository";
    private static final String KEY_PULL_REQUEST = "pull_request";
    private static final String KEY_FULL_NAME = "full_name";
    private static final String KEY_NUMBER = "number";

    private final GitHubEventType gitHubEventType;
    private final Map<String, Object> payload;
    private final long installationId;
    private final ExecutionControlMode executionControlMode;

    private GitHubEventServiceRequest(GitHubEventType gitHubEventType,
                                      Map<String, Object> payload,
                                      long installationId,
                                      ExecutionControlMode executionControlMode) {
        this.gitHubEventType = gitHubEventType;
        this.payload = payload;
        this.installationId = installationId;
        this.executionControlMode = executionControlMode;
    }

    public static GitHubEventServiceRequest of(GitHubEventType gitHubEventType, Map<String, Object> payload, long installationId) {
        return new GitHubEventServiceRequest(gitHubEventType, payload, installationId, ExecutionControlMode.NORMAL);
    }

    public String getRepositoryName() {
        Map<String, Object> repository = getMapValue(KEY_REPOSITORY);
        return (String) repository.get(KEY_FULL_NAME);
    }

    public int getPullRequestNumber() {
        Map<String, Object> pullRequest = getMapValue(KEY_PULL_REQUEST);
        return ((Number) pullRequest.get(KEY_NUMBER)).intValue();
    }

    public String getAction() {
        Object action = payload.get(KEY_ACTION);
        if (action == null) {
            return "";
        }

        return action.toString();
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> getMapValue(String key) {
        return (Map<String, Object>) payload.get(key);
    }
}
