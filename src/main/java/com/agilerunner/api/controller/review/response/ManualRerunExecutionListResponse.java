package com.agilerunner.api.controller.review.response;

import com.agilerunner.api.service.review.response.ManualRerunExecutionListServiceResponse;
import lombok.Getter;

import java.util.List;

@Getter
public class ManualRerunExecutionListResponse {
    private final List<ExecutionSummary> executions;

    private ManualRerunExecutionListResponse(List<ExecutionSummary> executions) {
        this.executions = List.copyOf(executions);
    }

    public static ManualRerunExecutionListResponse from(ManualRerunExecutionListServiceResponse response) {
        return new ManualRerunExecutionListResponse(
                response.getExecutions().stream()
                        .map(execution -> ExecutionSummary.of(execution.getExecutionKey()))
                        .toList()
        );
    }

    @Getter
    public static class ExecutionSummary {
        private final String executionKey;

        private ExecutionSummary(String executionKey) {
            this.executionKey = executionKey;
        }

        public static ExecutionSummary of(String executionKey) {
            return new ExecutionSummary(executionKey);
        }
    }
}
