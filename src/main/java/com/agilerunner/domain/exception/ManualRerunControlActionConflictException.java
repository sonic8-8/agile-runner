package com.agilerunner.domain.exception;

import lombok.Getter;

@Getter
public class ManualRerunControlActionConflictException extends RuntimeException {
    private final String executionKey;
    private final FailureDisposition failureDisposition;

    public ManualRerunControlActionConflictException(String executionKey,
                                                     FailureDisposition failureDisposition,
                                                     String message) {
        super(message);
        this.executionKey = executionKey;
        this.failureDisposition = failureDisposition;
    }
}
