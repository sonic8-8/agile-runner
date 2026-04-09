package com.agilerunner.domain.agentruntime;

import com.agilerunner.domain.executioncontrol.ExecutionControlMode;
import com.agilerunner.domain.executioncontrol.GitHubWriteSkipReason;
import com.agilerunner.domain.exception.ErrorCode;
import com.agilerunner.domain.exception.FailureDisposition;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class WebhookExecution {
    private final String executionKey;
    private final String taskKey;
    private final String deliveryId;
    private final ExecutionStartType executionStartType;
    private final String retrySourceExecutionKey;
    private final String repositoryName;
    private final int pullRequestNumber;
    private final String eventType;
    private final String action;
    private final WebhookExecutionStatus status;
    private final String errorMessage;
    private final ErrorCode errorCode;
    private final FailureDisposition failureDisposition;
    private final ExecutionControlMode executionControlMode;
    private final Boolean writePerformed;
    private final GitHubWriteSkipReason writeSkipReason;
    private final Boolean selectionApplied;
    private final String selectedPathsSummary;
    private final LocalDateTime startedAt;
    private final LocalDateTime finishedAt;

    private WebhookExecution(String executionKey,
                      String taskKey,
                      String deliveryId,
                      ExecutionStartType executionStartType,
                      String retrySourceExecutionKey,
                      String repositoryName,
                      int pullRequestNumber,
                      String eventType,
                      String action,
                      WebhookExecutionStatus status,
                      String errorMessage,
                      ErrorCode errorCode,
                      FailureDisposition failureDisposition,
                      ExecutionControlMode executionControlMode,
                      Boolean writePerformed,
                      GitHubWriteSkipReason writeSkipReason,
                      Boolean selectionApplied,
                      String selectedPathsSummary,
                      LocalDateTime startedAt,
                      LocalDateTime finishedAt) {
        this.executionKey = executionKey;
        this.taskKey = taskKey;
        this.deliveryId = deliveryId;
        this.executionStartType = executionStartType;
        this.retrySourceExecutionKey = retrySourceExecutionKey;
        this.repositoryName = repositoryName;
        this.pullRequestNumber = pullRequestNumber;
        this.eventType = eventType;
        this.action = action;
        this.status = status;
        this.errorMessage = errorMessage;
        this.errorCode = errorCode;
        this.failureDisposition = failureDisposition;
        this.executionControlMode = executionControlMode;
        this.writePerformed = writePerformed;
        this.writeSkipReason = writeSkipReason;
        this.selectionApplied = selectionApplied;
        this.selectedPathsSummary = selectedPathsSummary;
        this.startedAt = startedAt;
        this.finishedAt = finishedAt;
    }

    public static WebhookExecution start(String executionKey,
                                  String taskKey,
                                  String deliveryId,
                                  String repositoryName,
                                  int pullRequestNumber,
                                  String eventType,
                                  String action,
                                  LocalDateTime startedAt) {
        return new WebhookExecution(
                executionKey,
                taskKey,
                deliveryId,
                null,
                null,
                repositoryName,
                pullRequestNumber,
                eventType,
                action,
                WebhookExecutionStatus.STARTED,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                startedAt,
                null
        );
    }

    public WebhookExecution withExecutionControl(ExecutionControlMode executionControlMode,
                                                 Boolean writePerformed,
                                                 GitHubWriteSkipReason writeSkipReason) {
        return new WebhookExecution(
                executionKey,
                taskKey,
                deliveryId,
                executionStartType,
                retrySourceExecutionKey,
                repositoryName,
                pullRequestNumber,
                eventType,
                action,
                status,
                errorMessage,
                errorCode,
                failureDisposition,
                executionControlMode,
                writePerformed,
                writeSkipReason,
                selectionApplied,
                selectedPathsSummary,
                startedAt,
                finishedAt
        );
    }

    public WebhookExecution withSelectionScope(Boolean selectionApplied, String selectedPathsSummary) {
        return new WebhookExecution(
                executionKey,
                taskKey,
                deliveryId,
                executionStartType,
                retrySourceExecutionKey,
                repositoryName,
                pullRequestNumber,
                eventType,
                action,
                status,
                errorMessage,
                errorCode,
                failureDisposition,
                executionControlMode,
                writePerformed,
                writeSkipReason,
                selectionApplied,
                selectedPathsSummary,
                startedAt,
                finishedAt
        );
    }

    public WebhookExecution withExecutionStartType(ExecutionStartType executionStartType) {
        return new WebhookExecution(
                executionKey,
                taskKey,
                deliveryId,
                executionStartType,
                retrySourceExecutionKey,
                repositoryName,
                pullRequestNumber,
                eventType,
                action,
                status,
                errorMessage,
                errorCode,
                failureDisposition,
                executionControlMode,
                writePerformed,
                writeSkipReason,
                selectionApplied,
                selectedPathsSummary,
                startedAt,
                finishedAt
        );
    }

    public WebhookExecution complete(WebhookExecutionStatus status,
                                     String errorMessage,
                                     ErrorCode errorCode,
                                     LocalDateTime finishedAt) {
        return complete(status, errorMessage, errorCode, null, finishedAt);
    }

    public WebhookExecution complete(WebhookExecutionStatus status,
                                     String errorMessage,
                                     ErrorCode errorCode,
                                     FailureDisposition failureDisposition,
                                     LocalDateTime finishedAt) {
        return new WebhookExecution(
                executionKey,
                taskKey,
                deliveryId,
                executionStartType,
                retrySourceExecutionKey,
                repositoryName,
                pullRequestNumber,
                eventType,
                action,
                status,
                errorMessage,
                errorCode,
                failureDisposition,
                executionControlMode,
                writePerformed,
                writeSkipReason,
                selectionApplied,
                selectedPathsSummary,
                startedAt,
                finishedAt
        );
    }

    public WebhookExecution withRetrySourceExecutionKey(String retrySourceExecutionKey) {
        return new WebhookExecution(
                executionKey,
                taskKey,
                deliveryId,
                executionStartType,
                retrySourceExecutionKey,
                repositoryName,
                pullRequestNumber,
                eventType,
                action,
                status,
                errorMessage,
                errorCode,
                failureDisposition,
                executionControlMode,
                writePerformed,
                writeSkipReason,
                selectionApplied,
                selectedPathsSummary,
                startedAt,
                finishedAt
        );
    }
}
