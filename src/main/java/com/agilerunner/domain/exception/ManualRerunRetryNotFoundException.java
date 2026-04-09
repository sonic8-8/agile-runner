package com.agilerunner.domain.exception;

import lombok.Getter;

@Getter
public class ManualRerunRetryNotFoundException extends RuntimeException {
    private final String executionKey;

    public ManualRerunRetryNotFoundException(String executionKey, String message) {
        super(message);
        this.executionKey = executionKey;
    }
}
