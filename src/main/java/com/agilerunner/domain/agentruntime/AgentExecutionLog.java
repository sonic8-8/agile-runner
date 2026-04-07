package com.agilerunner.domain.agentruntime;

import com.agilerunner.domain.executioncontrol.ExecutionControlMode;
import com.agilerunner.domain.executioncontrol.GitHubWriteSkipReason;
import com.agilerunner.domain.exception.ErrorCode;
import com.agilerunner.domain.exception.FailureDisposition;
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
    private final ErrorCode errorCode;
    private final FailureDisposition failureDisposition;
    private final ExecutionControlMode executionControlMode;
    private final Boolean writePerformed;
    private final GitHubWriteSkipReason writeSkipReason;
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
                              ErrorCode errorCode,
                              FailureDisposition failureDisposition,
                              ExecutionControlMode executionControlMode,
                              Boolean writePerformed,
                              GitHubWriteSkipReason writeSkipReason,
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
        this.errorCode = errorCode;
        this.failureDisposition = failureDisposition;
        this.executionControlMode = executionControlMode;
        this.writePerformed = writePerformed;
        this.writeSkipReason = writeSkipReason;
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
                                       ErrorCode errorCode,
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
                errorCode,
                null,
                null,
                null,
                null,
                payloadJson,
                startedAt,
                endedAt
        );
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
                                       ErrorCode errorCode,
                                       FailureDisposition failureDisposition,
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
                errorCode,
                failureDisposition,
                null,
                null,
                null,
                payloadJson,
                startedAt,
                endedAt
        );
    }

    public AgentExecutionLog withExecutionControl(ExecutionControlMode executionControlMode,
                                                  Boolean writePerformed,
                                                  GitHubWriteSkipReason writeSkipReason) {
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
                errorCode,
                failureDisposition,
                executionControlMode,
                writePerformed,
                writeSkipReason,
                payloadJson,
                startedAt,
                endedAt
        );
    }
}
