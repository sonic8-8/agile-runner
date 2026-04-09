package com.agilerunner.api.controller.review.response;

import lombok.Getter;

@Getter
public class ManualRerunQueryNotFoundResponse {
    private final String executionKey;
    private final String message;

    private ManualRerunQueryNotFoundResponse(String executionKey, String message) {
        this.executionKey = executionKey;
        this.message = message;
    }

    public static ManualRerunQueryNotFoundResponse of(String executionKey, String message) {
        return new ManualRerunQueryNotFoundResponse(executionKey, message);
    }
}
