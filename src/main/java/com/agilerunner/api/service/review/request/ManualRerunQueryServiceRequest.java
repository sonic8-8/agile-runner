package com.agilerunner.api.service.review.request;

import lombok.Getter;

@Getter
public class ManualRerunQueryServiceRequest {
    private final String executionKey;

    private ManualRerunQueryServiceRequest(String executionKey) {
        this.executionKey = executionKey;
    }

    public static ManualRerunQueryServiceRequest of(String executionKey) {
        return new ManualRerunQueryServiceRequest(executionKey);
    }
}
