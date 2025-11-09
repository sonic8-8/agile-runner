package com.agilerunner;

import java.util.Arrays;

public enum GitHubEventType {
    PUSH("pull"),
    PULL_REQUEST("pull_request"),
    ISSUE_COMMENT("issue_comment"),
    PULL_REQUEST_REVIEW("pull_request_review"),
    UNKNOWN("unknown");

    private final String value;

    GitHubEventType(String value) {
        this.value = value;
    }

    public static GitHubEventType of(String eventType) {
        return Arrays.stream(values())
                .filter(type -> type.value.equals(eventType))
                .findFirst()
                .orElse(UNKNOWN);
    }
}
