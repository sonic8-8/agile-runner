package com.agilerunner.domain.review;

import com.agilerunner.domain.exception.FailureDisposition;
import lombok.Getter;

@Getter
public class ManualRerunControlActionEligibility {
    private final boolean actionAllowed;
    private final FailureDisposition failureDisposition;
    private final String message;

    private ManualRerunControlActionEligibility(boolean actionAllowed,
                                                FailureDisposition failureDisposition,
                                                String message) {
        this.actionAllowed = actionAllowed;
        this.failureDisposition = failureDisposition;
        this.message = message;
    }

    public static ManualRerunControlActionEligibility allowed() {
        return new ManualRerunControlActionEligibility(true, null, null);
    }

    public static ManualRerunControlActionEligibility rejected(FailureDisposition failureDisposition, String message) {
        return new ManualRerunControlActionEligibility(false, failureDisposition, message);
    }
}
