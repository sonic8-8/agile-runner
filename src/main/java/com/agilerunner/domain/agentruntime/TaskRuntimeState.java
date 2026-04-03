package com.agilerunner.domain.agentruntime;

import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class TaskRuntimeState {
    private final String taskKey;
    private final Long issueNumber;
    private final String title;
    private final TaskRuntimeStatus status;
    private final int retryCount;
    private final AgentRole ownerRole;
    private final LocalDateTime startedAt;
    private final LocalDateTime finishedAt;

    private TaskRuntimeState(String taskKey,
                      Long issueNumber,
                      String title,
                      TaskRuntimeStatus status,
                      int retryCount,
                      AgentRole ownerRole,
                      LocalDateTime startedAt,
                      LocalDateTime finishedAt) {
        this.taskKey = taskKey;
        this.issueNumber = issueNumber;
        this.title = title;
        this.status = status;
        this.retryCount = retryCount;
        this.ownerRole = ownerRole;
        this.startedAt = startedAt;
        this.finishedAt = finishedAt;
    }

    public static TaskRuntimeState of(String taskKey,
                               Long issueNumber,
                               String title,
                               TaskRuntimeStatus status,
                               int retryCount,
                               AgentRole ownerRole,
                               LocalDateTime startedAt,
                               LocalDateTime finishedAt) {
        return new TaskRuntimeState(taskKey, issueNumber, title, status, retryCount, ownerRole, startedAt, finishedAt);
    }
}
