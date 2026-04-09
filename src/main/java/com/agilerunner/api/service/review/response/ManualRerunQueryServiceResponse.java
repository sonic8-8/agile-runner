package com.agilerunner.api.service.review.response;

import com.agilerunner.domain.exception.ErrorCode;
import com.agilerunner.domain.exception.FailureDisposition;
import com.agilerunner.domain.executioncontrol.ExecutionControlMode;
import com.agilerunner.domain.review.RerunExecutionStatus;
import lombok.Getter;

@Getter
public class ManualRerunQueryServiceResponse {
    private final String executionKey;
    private final ExecutionControlMode executionControlMode;
    private final boolean writePerformed;
    private final RerunExecutionStatus executionStatus;
    private final ErrorCode errorCode;
    private final FailureDisposition failureDisposition;

    private ManualRerunQueryServiceResponse(String executionKey,
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

    public static ManualRerunQueryServiceResponse of(String executionKey,
                                                     ExecutionControlMode executionControlMode,
                                                     boolean writePerformed,
                                                     RerunExecutionStatus executionStatus,
                                                     ErrorCode errorCode,
                                                     FailureDisposition failureDisposition) {
        return new ManualRerunQueryServiceResponse(
                executionKey,
                executionControlMode,
                writePerformed,
                executionStatus,
                errorCode,
                failureDisposition
        );
    }
}
