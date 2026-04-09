package com.agilerunner.api.controller.review.response;

import lombok.Getter;

@Getter
public class ManualRerunControlActionNotFoundResponse {
    private final String executionKey;
    private final String message;

    private ManualRerunControlActionNotFoundResponse(String executionKey, String message) {
        this.executionKey = executionKey;
        this.message = message;
    }

    public static ManualRerunControlActionNotFoundResponse of(String executionKey, String message) {
        return new ManualRerunControlActionNotFoundResponse(executionKey, message);
    }
}
