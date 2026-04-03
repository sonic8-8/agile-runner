package com.agilerunner.domain.agentruntime;

import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class AgentExecutionLog {
    private final String taskKey;
    private final Long issueNumber;
    private final String runKey;
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
                              String runKey,
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
        this.runKey = runKey;
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
                                       String runKey,
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
                runKey,
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
