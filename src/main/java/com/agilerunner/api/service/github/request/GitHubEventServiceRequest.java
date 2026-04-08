package com.agilerunner.api.service.github.request;

import com.agilerunner.GitHubEventType;
import com.agilerunner.domain.executioncontrol.ExecutionControlMode;
import lombok.Getter;

import java.util.List;
import java.util.Map;
import java.util.Objects;

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
    private final List<String> selectedPaths;

    private GitHubEventServiceRequest(GitHubEventType gitHubEventType,
                                      Map<String, Object> payload,
                                      long installationId,
                                      ExecutionControlMode executionControlMode,
                                      List<String> selectedPaths) {
        this.gitHubEventType = gitHubEventType;
        this.payload = payload;
        this.installationId = installationId;
        this.executionControlMode = executionControlMode;
        this.selectedPaths = List.copyOf(selectedPaths);
    }

    public static GitHubEventServiceRequest of(GitHubEventType gitHubEventType, Map<String, Object> payload, long installationId) {
        return of(gitHubEventType, payload, installationId, ExecutionControlMode.NORMAL, List.of());
    }

    public static GitHubEventServiceRequest of(GitHubEventType gitHubEventType,
                                               Map<String, Object> payload,
                                               long installationId,
                                               ExecutionControlMode executionControlMode) {
        return of(gitHubEventType, payload, installationId, executionControlMode, List.of());
    }

    public static GitHubEventServiceRequest of(GitHubEventType gitHubEventType,
                                               Map<String, Object> payload,
                                               long installationId,
                                               ExecutionControlMode executionControlMode,
                                               List<String> selectedPaths) {
        return new GitHubEventServiceRequest(
                gitHubEventType,
                payload,
                installationId,
                Objects.requireNonNull(executionControlMode),
                normalizeSelectedPaths(selectedPaths)
        );
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

    private static List<String> normalizeSelectedPaths(List<String> selectedPaths) {
        if (selectedPaths == null) {
            return List.of();
        }

        return selectedPaths;
    }
}
