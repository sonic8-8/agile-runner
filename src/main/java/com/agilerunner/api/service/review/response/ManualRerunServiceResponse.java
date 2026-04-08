package com.agilerunner.api.service.review.response;

import com.agilerunner.domain.executioncontrol.ExecutionControlMode;
import lombok.Getter;

@Getter
public class ManualRerunServiceResponse {
    private final String executionKey;
    private final ExecutionControlMode executionControlMode;
    private final boolean writePerformed;

    private ManualRerunServiceResponse(String executionKey,
                                       ExecutionControlMode executionControlMode,
                                       boolean writePerformed) {
        this.executionKey = executionKey;
        this.executionControlMode = executionControlMode;
        this.writePerformed = writePerformed;
    }

    public static ManualRerunServiceResponse of(String executionKey,
                                                ExecutionControlMode executionControlMode,
                                                boolean writePerformed) {
        return new ManualRerunServiceResponse(executionKey, executionControlMode, writePerformed);
    }
}
