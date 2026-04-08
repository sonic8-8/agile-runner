package com.agilerunner.api.controller.review.response;

import com.agilerunner.api.service.review.response.ManualRerunServiceResponse;
import com.agilerunner.domain.executioncontrol.ExecutionControlMode;
import lombok.Getter;

@Getter
public class ManualRerunResponse {
    private final String executionKey;
    private final ExecutionControlMode executionControlMode;
    private final boolean writePerformed;

    private ManualRerunResponse(String executionKey,
                                ExecutionControlMode executionControlMode,
                                boolean writePerformed) {
        this.executionKey = executionKey;
        this.executionControlMode = executionControlMode;
        this.writePerformed = writePerformed;
    }

    public static ManualRerunResponse from(ManualRerunServiceResponse response) {
        return new ManualRerunResponse(
                response.getExecutionKey(),
                response.getExecutionControlMode(),
                response.isWritePerformed()
        );
    }
}
