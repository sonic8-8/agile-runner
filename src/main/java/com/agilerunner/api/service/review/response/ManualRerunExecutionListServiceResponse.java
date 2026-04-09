package com.agilerunner.api.service.review.response;

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

        private ExecutionSummary(String executionKey) {
            this.executionKey = executionKey;
        }

        public static ExecutionSummary of(String executionKey) {
            return new ExecutionSummary(executionKey);
        }
    }
}
