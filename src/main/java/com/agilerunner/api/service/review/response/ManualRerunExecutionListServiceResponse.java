package com.agilerunner.api.service.review.response;

import com.agilerunner.domain.agentruntime.ExecutionStartType;
import com.agilerunner.domain.exception.ErrorCode;
import com.agilerunner.domain.exception.FailureDisposition;
import com.agilerunner.domain.executioncontrol.ExecutionControlMode;
import com.agilerunner.domain.review.ManualRerunAvailableAction;
import com.agilerunner.domain.review.RerunExecutionStatus;
import lombok.Getter;

import java.util.List;

@Getter
public class ManualRerunExecutionListServiceResponse {
    private final List<ExecutionSummary> executions;

    private ManualRerunExecutionListServiceResponse(List<ExecutionSummary> executions) {
        this.executions = List.copyOf(executions);
    }

    public static ManualRerunExecutionListServiceResponse of(List<ExecutionSummary> executions) {
        return new ManualRerunExecutionListServiceResponse(executions);
    }

    @Getter
    public static class ExecutionSummary {
        private final String executionKey;
        private final String retrySourceExecutionKey;
        private final ExecutionStartType executionStartType;
        private final RerunExecutionStatus executionStatus;
        private final ExecutionControlMode executionControlMode;
        private final boolean writePerformed;
        private final ErrorCode errorCode;
        private final FailureDisposition failureDisposition;
        private final List<ManualRerunAvailableAction> availableActions;

        private ExecutionSummary(String executionKey,
                                 String retrySourceExecutionKey,
                                 ExecutionStartType executionStartType,
                                 RerunExecutionStatus executionStatus,
                                 ExecutionControlMode executionControlMode,
                                 boolean writePerformed,
                                 ErrorCode errorCode,
                                 FailureDisposition failureDisposition,
                                 List<ManualRerunAvailableAction> availableActions) {
            this.executionKey = executionKey;
            this.retrySourceExecutionKey = retrySourceExecutionKey;
            this.executionStartType = executionStartType;
            this.executionStatus = executionStatus;
            this.executionControlMode = executionControlMode;
            this.writePerformed = writePerformed;
            this.errorCode = errorCode;
            this.failureDisposition = failureDisposition;
            this.availableActions = List.copyOf(availableActions);
        }

        public static ExecutionSummary of(String executionKey,
                                          String retrySourceExecutionKey,
                                          ExecutionStartType executionStartType,
                                          RerunExecutionStatus executionStatus,
                                          ExecutionControlMode executionControlMode,
                                          boolean writePerformed,
                                          ErrorCode errorCode,
                                          FailureDisposition failureDisposition,
                                          List<ManualRerunAvailableAction> availableActions) {
            return new ExecutionSummary(
                    executionKey,
                    retrySourceExecutionKey,
                    executionStartType,
                    executionStatus,
                    executionControlMode,
                    writePerformed,
                    errorCode,
                    failureDisposition,
                    availableActions
            );
        }
    }
}
