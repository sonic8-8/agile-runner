package com.agilerunner.domain.review;

import com.agilerunner.domain.exception.FailureDisposition;
import lombok.Getter;

@Getter
public class ManualRerunRetryEligibility {
    private final boolean retryAllowed;
    private final FailureDisposition failureDisposition;
    private final String message;

    private ManualRerunRetryEligibility(boolean retryAllowed,
                                        FailureDisposition failureDisposition,
                                        String message) {
        this.retryAllowed = retryAllowed;
        this.failureDisposition = failureDisposition;
        this.message = message;
    }

    public static ManualRerunRetryEligibility allowed(FailureDisposition failureDisposition) {
        return new ManualRerunRetryEligibility(true, failureDisposition, null);
    }

    public static ManualRerunRetryEligibility rejected(FailureDisposition failureDisposition, String message) {
        return new ManualRerunRetryEligibility(false, failureDisposition, message);
    }
}
