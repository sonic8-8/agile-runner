package com.agilerunner.domain.exception;

import lombok.Getter;

@Getter
public class ManualRerunControlActionNotFoundException extends RuntimeException {
    private final String executionKey;

    public ManualRerunControlActionNotFoundException(String executionKey, String message) {
        super(message);
        this.executionKey = executionKey;
    }
}
