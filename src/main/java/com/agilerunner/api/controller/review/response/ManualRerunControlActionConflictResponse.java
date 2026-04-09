package com.agilerunner.api.controller.review.response;

import com.agilerunner.domain.exception.FailureDisposition;
import lombok.Getter;

@Getter
public class ManualRerunControlActionConflictResponse {
    private final String executionKey;
    private final FailureDisposition failureDisposition;
    private final String message;

    private ManualRerunControlActionConflictResponse(String executionKey,
                                                     FailureDisposition failureDisposition,
                                                     String message) {
        this.executionKey = executionKey;
        this.failureDisposition = failureDisposition;
        this.message = message;
    }

    public static ManualRerunControlActionConflictResponse of(String executionKey,
                                                              FailureDisposition failureDisposition,
                                                              String message) {
        return new ManualRerunControlActionConflictResponse(executionKey, failureDisposition, message);
    }
}
