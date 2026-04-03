package com.agilerunner.domain.agentruntime;

import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class ReviewRun {
    private final String runKey;
    private final String taskKey;
    private final String deliveryId;
    private final String repositoryName;
    private final int pullRequestNumber;
    private final String eventType;
    private final String action;
    private final ReviewRunStatus status;
    private final String errorMessage;
    private final LocalDateTime startedAt;
    private final LocalDateTime finishedAt;

    private ReviewRun(String runKey,
                      String taskKey,
                      String deliveryId,
                      String repositoryName,
                      int pullRequestNumber,
                      String eventType,
                      String action,
                      ReviewRunStatus status,
                      String errorMessage,
                      LocalDateTime startedAt,
                      LocalDateTime finishedAt) {
        this.runKey = runKey;
        this.taskKey = taskKey;
        this.deliveryId = deliveryId;
        this.repositoryName = repositoryName;
        this.pullRequestNumber = pullRequestNumber;
        this.eventType = eventType;
        this.action = action;
        this.status = status;
        this.errorMessage = errorMessage;
        this.startedAt = startedAt;
        this.finishedAt = finishedAt;
    }

    public static ReviewRun start(String runKey,
                                  String taskKey,
                                  String deliveryId,
                                  String repositoryName,
                                  int pullRequestNumber,
                                  String eventType,
                                  String action,
                                  LocalDateTime startedAt) {
        return new ReviewRun(
                runKey,
                taskKey,
                deliveryId,
                repositoryName,
                pullRequestNumber,
                eventType,
                action,
                ReviewRunStatus.STARTED,
                null,
                startedAt,
                null
        );
    }

    public ReviewRun complete(ReviewRunStatus status, String errorMessage, LocalDateTime finishedAt) {
        return new ReviewRun(
                runKey,
                taskKey,
                deliveryId,
                repositoryName,
                pullRequestNumber,
                eventType,
                action,
                status,
                errorMessage,
                startedAt,
                finishedAt
        );
    }
}
