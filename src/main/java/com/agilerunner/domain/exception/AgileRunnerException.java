package com.agilerunner.domain.exception;

public class AgileRunnerException extends RuntimeException {
    private final ErrorCode errorCode;

    public AgileRunnerException(ErrorCode errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    public AgileRunnerException(ErrorCode errorCode, String message, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
    }

    public ErrorCode getErrorCode() {
        return errorCode;
    }
}
