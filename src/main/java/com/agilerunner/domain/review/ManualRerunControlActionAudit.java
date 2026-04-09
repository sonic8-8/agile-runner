package com.agilerunner.domain.review;

import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class ManualRerunControlActionAudit {
    private final String executionKey;
    private final ManualRerunControlAction action;
    private final ManualRerunControlActionStatus actionStatus;
    private final String note;
    private final LocalDateTime appliedAt;

    private ManualRerunControlActionAudit(String executionKey,
                                          ManualRerunControlAction action,
                                          ManualRerunControlActionStatus actionStatus,
                                          String note,
                                          LocalDateTime appliedAt) {
        this.executionKey = executionKey;
        this.action = action;
        this.actionStatus = actionStatus;
        this.note = note;
        this.appliedAt = appliedAt;
    }

    public static ManualRerunControlActionAudit applied(String executionKey,
                                                        ManualRerunControlAction action,
                                                        String note,
                                                        LocalDateTime appliedAt) {
        return new ManualRerunControlActionAudit(
                executionKey,
                action,
                ManualRerunControlActionStatus.APPLIED,
                note,
                appliedAt
        );
    }
}
