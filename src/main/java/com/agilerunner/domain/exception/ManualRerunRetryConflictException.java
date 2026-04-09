package com.agilerunner.domain.exception;

import lombok.Getter;

@Getter
public class ManualRerunRetryConflictException extends RuntimeException {
    private final String executionKey;
    private final FailureDisposition failureDisposition;

    public ManualRerunRetryConflictException(String executionKey,
                                            FailureDisposition failureDisposition,
                                            String message) {
        super(message);
        this.executionKey = executionKey;
        this.failureDisposition = failureDisposition;
    }
}
