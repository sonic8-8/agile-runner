package com.agilerunner.api.controller.review.response;

import com.agilerunner.api.service.review.response.ManualRerunServiceResponse;
import com.agilerunner.domain.exception.ErrorCode;
import com.agilerunner.domain.exception.FailureDisposition;
import com.agilerunner.domain.executioncontrol.ExecutionControlMode;
import com.agilerunner.domain.review.RerunExecutionStatus;
import lombok.Getter;

@Getter
public class ManualRerunResponse {
    private final String executionKey;
    private final ExecutionControlMode executionControlMode;
    private final boolean writePerformed;
    private final RerunExecutionStatus executionStatus;
    private final ErrorCode errorCode;
    private final FailureDisposition failureDisposition;

    private ManualRerunResponse(String executionKey,
                                ExecutionControlMode executionControlMode,
                                boolean writePerformed,
                                RerunExecutionStatus executionStatus,
                                ErrorCode errorCode,
                                FailureDisposition failureDisposition) {
        this.executionKey = executionKey;
        this.executionControlMode = executionControlMode;
        this.writePerformed = writePerformed;
        this.executionStatus = executionStatus;
        this.errorCode = errorCode;
        this.failureDisposition = failureDisposition;
    }

    public static ManualRerunResponse from(ManualRerunServiceResponse response) {
        return new ManualRerunResponse(
                response.getExecutionKey(),
                response.getExecutionControlMode(),
                response.isWritePerformed(),
                response.getExecutionStatus(),
                response.getErrorCode(),
                response.getFailureDisposition()
        );
    }
}
