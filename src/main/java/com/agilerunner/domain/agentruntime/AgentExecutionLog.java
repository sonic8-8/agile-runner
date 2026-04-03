package com.agilerunner.domain.agentruntime;

import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class AgentExecutionLog {
    private final String taskKey;
    private final Long issueNumber;
    private final String executionKey;
    private final AgentRole agentRole;
    private final String stepName;
    private final AgentExecutionStatus status;
    private final String inputSummary;
    private final String outputSummary;
    private final String errorMessage;
    private final String payloadJson;
    private final LocalDateTime startedAt;
    private final LocalDateTime endedAt;

    private AgentExecutionLog(String taskKey,
                              Long issueNumber,
                              String executionKey,
                              AgentRole agentRole,
                              String stepName,
                              AgentExecutionStatus status,
                              String inputSummary,
                              String outputSummary,
                              String errorMessage,
                              String payloadJson,
                              LocalDateTime startedAt,
                              LocalDateTime endedAt) {
        this.taskKey = taskKey;
        this.issueNumber = issueNumber;
        this.executionKey = executionKey;
        this.agentRole = agentRole;
        this.stepName = stepName;
        this.status = status;
        this.inputSummary = inputSummary;
        this.outputSummary = outputSummary;
        this.errorMessage = errorMessage;
        this.payloadJson = payloadJson;
        this.startedAt = startedAt;
        this.endedAt = endedAt;
    }

    public static AgentExecutionLog of(String taskKey,
                                       Long issueNumber,
                                       String executionKey,
                                       AgentRole agentRole,
                                       String stepName,
                                       AgentExecutionStatus status,
                                       String inputSummary,
                                       String outputSummary,
                                       String errorMessage,
                                       String payloadJson,
                                       LocalDateTime startedAt,
                                       LocalDateTime endedAt) {
        return new AgentExecutionLog(
                taskKey,
                issueNumber,
                executionKey,
                agentRole,
                stepName,
                status,
                inputSummary,
                outputSummary,
                errorMessage,
                payloadJson,
                startedAt,
                endedAt
        );
    }
}
