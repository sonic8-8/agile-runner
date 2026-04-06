package com.agilerunner.domain.exception;

import java.util.EnumMap;
import java.util.Map;

public class FailureDispositionPolicy {
    private final Map<ErrorCode, FailureDisposition> dispositions;

    public FailureDispositionPolicy() {
        EnumMap<ErrorCode, FailureDisposition> values = new EnumMap<>(ErrorCode.class);
        values.put(ErrorCode.GITHUB_INSTALLATION_ID_MISSING, FailureDisposition.MANUAL_ACTION_REQUIRED);
        values.put(ErrorCode.OPENAI_CLIENT_MISSING, FailureDisposition.MANUAL_ACTION_REQUIRED);
        values.put(ErrorCode.OPENAI_REVIEW_FAILED, FailureDisposition.RETRYABLE);
        values.put(ErrorCode.GITHUB_APP_CONFIGURATION_MISSING, FailureDisposition.MANUAL_ACTION_REQUIRED);
        values.put(ErrorCode.GITHUB_COMMENT_PREPARATION_FAILED, FailureDisposition.NON_RETRYABLE);
        values.put(ErrorCode.GITHUB_COMMENT_POST_FAILED, FailureDisposition.RETRYABLE);
        this.dispositions = values;
    }

    public FailureDisposition classify(AgileRunnerException exception) {
        return classify(exception.getErrorCode());
    }

    public FailureDisposition classify(ErrorCode errorCode) {
        FailureDisposition disposition = dispositions.get(errorCode);

        if (disposition != null) {
            return disposition;
        }

        throw new IllegalArgumentException("Unsupported error code: " + errorCode);
    }
}
