package com.agilerunner.domain.agentruntime;

import com.agilerunner.domain.exception.ErrorCode;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class WebhookExecution {
    private final String executionKey;
    private final String taskKey;
    private final String deliveryId;
    private final String repositoryName;
    private final int pullRequestNumber;
    private final String eventType;
    private final String action;
    private final WebhookExecutionStatus status;
    private final String errorMessage;
    private final ErrorCode errorCode;
    private final LocalDateTime startedAt;
    private final LocalDateTime finishedAt;

    private WebhookExecution(String executionKey,
                      String taskKey,
                      String deliveryId,
                      String repositoryName,
                      int pullRequestNumber,
                      String eventType,
                      String action,
                      WebhookExecutionStatus status,
                      String errorMessage,
                      ErrorCode errorCode,
                      LocalDateTime startedAt,
                      LocalDateTime finishedAt) {
        this.executionKey = executionKey;
        this.taskKey = taskKey;
        this.deliveryId = deliveryId;
        this.repositoryName = repositoryName;
        this.pullRequestNumber = pullRequestNumber;
        this.eventType = eventType;
        this.action = action;
        this.status = status;
        this.errorMessage = errorMessage;
        this.errorCode = errorCode;
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
                repositoryName,
                pullRequestNumber,
                eventType,
                action,
                WebhookExecutionStatus.STARTED,
                null,
                null,
                startedAt,
                null
        );
    }

    public WebhookExecution complete(WebhookExecutionStatus status,
                                     String errorMessage,
                                     ErrorCode errorCode,
                                     LocalDateTime finishedAt) {
        return new WebhookExecution(
                executionKey,
                taskKey,
                deliveryId,
                repositoryName,
                pullRequestNumber,
                eventType,
                action,
                status,
                errorMessage,
                errorCode,
                startedAt,
                finishedAt
        );
    }
}
