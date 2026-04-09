package com.agilerunner.domain.exception;

import lombok.Getter;

@Getter
public class ManualRerunQueryNotFoundException extends RuntimeException {
    private final String executionKey;

    public ManualRerunQueryNotFoundException(String executionKey, String message) {
        super(message);
        this.executionKey = executionKey;
    }
}
