package com.agilerunner.api.controller.review.response;

import lombok.Getter;

@Getter
public class ManualRerunRetryNotFoundResponse {
    private final String executionKey;
    private final String message;

    private ManualRerunRetryNotFoundResponse(String executionKey, String message) {
        this.executionKey = executionKey;
        this.message = message;
    }

    public static ManualRerunRetryNotFoundResponse of(String executionKey, String message) {
        return new ManualRerunRetryNotFoundResponse(executionKey, message);
    }
}
