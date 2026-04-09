package com.agilerunner.api.controller.review.response;

import com.agilerunner.domain.exception.FailureDisposition;
import lombok.Getter;

@Getter
public class ManualRerunRetryConflictResponse {
    private final String executionKey;
    private final FailureDisposition failureDisposition;
    private final String message;

    private ManualRerunRetryConflictResponse(String executionKey,
                                             FailureDisposition failureDisposition,
                                             String message) {
        this.executionKey = executionKey;
        this.failureDisposition = failureDisposition;
        this.message = message;
    }

    public static ManualRerunRetryConflictResponse of(String executionKey,
                                                      FailureDisposition failureDisposition,
                                                      String message) {
        return new ManualRerunRetryConflictResponse(executionKey, failureDisposition, message);
    }
}
