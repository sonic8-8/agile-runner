package com.agilerunner.api.service.review.request;

import lombok.Getter;

@Getter
public class ManualRerunControlActionHistoryServiceRequest {
    private final String executionKey;

    private ManualRerunControlActionHistoryServiceRequest(String executionKey) {
        this.executionKey = executionKey;
    }

    public static ManualRerunControlActionHistoryServiceRequest of(String executionKey) {
        return new ManualRerunControlActionHistoryServiceRequest(executionKey);
    }
}
