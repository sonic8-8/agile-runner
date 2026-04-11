package com.agilerunner.api.service.review.request;

import com.agilerunner.domain.review.ManualRerunControlAction;
import com.agilerunner.domain.review.ManualRerunControlActionStatus;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class ManualRerunControlActionHistoryServiceRequest {
    private final String executionKey;
    private final ManualRerunControlAction action;
    private final ManualRerunControlActionStatus actionStatus;
    private final LocalDateTime appliedAtFrom;
    private final LocalDateTime appliedAtTo;

    private ManualRerunControlActionHistoryServiceRequest(String executionKey,
                                                         ManualRerunControlAction action,
                                                         ManualRerunControlActionStatus actionStatus,
                                                         LocalDateTime appliedAtFrom,
                                                         LocalDateTime appliedAtTo) {
        this.executionKey = executionKey;
        this.action = action;
        this.actionStatus = actionStatus;
        this.appliedAtFrom = appliedAtFrom;
        this.appliedAtTo = appliedAtTo;
    }

    public static ManualRerunControlActionHistoryServiceRequest of(String executionKey) {
        return new ManualRerunControlActionHistoryServiceRequest(executionKey, null, null, null, null);
    }

    public static ManualRerunControlActionHistoryServiceRequest of(String executionKey,
                                                                   ManualRerunControlAction action,
                                                                   ManualRerunControlActionStatus actionStatus) {
        return new ManualRerunControlActionHistoryServiceRequest(
                executionKey,
                action,
                actionStatus,
                null,
                null
        );
    }

    public static ManualRerunControlActionHistoryServiceRequest of(String executionKey,
                                                                   ManualRerunControlAction action,
                                                                   ManualRerunControlActionStatus actionStatus,
                                                                   LocalDateTime appliedAtFrom,
                                                                   LocalDateTime appliedAtTo) {
        return new ManualRerunControlActionHistoryServiceRequest(
                executionKey,
                action,
                actionStatus,
                appliedAtFrom,
                appliedAtTo
        );
    }
}
