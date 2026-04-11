package com.agilerunner.api.service.review.request;

import com.agilerunner.domain.review.ManualRerunControlAction;
import com.agilerunner.domain.review.ManualRerunControlActionHistorySortDirection;
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
    private final ManualRerunControlActionHistorySortDirection sortDirection;
    private final Integer pageSize;
    private final LocalDateTime cursorAppliedAt;

    private ManualRerunControlActionHistoryServiceRequest(String executionKey,
                                                         ManualRerunControlAction action,
                                                         ManualRerunControlActionStatus actionStatus,
                                                         LocalDateTime appliedAtFrom,
                                                         LocalDateTime appliedAtTo,
                                                         ManualRerunControlActionHistorySortDirection sortDirection,
                                                         Integer pageSize,
                                                         LocalDateTime cursorAppliedAt) {
        this.executionKey = executionKey;
        this.action = action;
        this.actionStatus = actionStatus;
        this.appliedAtFrom = appliedAtFrom;
        this.appliedAtTo = appliedAtTo;
        this.sortDirection = normalizeSortDirection(sortDirection, pageSize, cursorAppliedAt);
        this.pageSize = pageSize;
        this.cursorAppliedAt = cursorAppliedAt;
    }

    public static ManualRerunControlActionHistoryServiceRequest of(String executionKey) {
        return new ManualRerunControlActionHistoryServiceRequest(executionKey, null, null, null, null, null, null, null);
    }

    public static ManualRerunControlActionHistoryServiceRequest of(String executionKey,
                                                                   ManualRerunControlAction action,
                                                                   ManualRerunControlActionStatus actionStatus) {
        return new ManualRerunControlActionHistoryServiceRequest(
                executionKey,
                action,
                actionStatus,
                null,
                null,
                null,
                null,
                null
        );
    }

    public static ManualRerunControlActionHistoryServiceRequest of(String executionKey,
                                                                   ManualRerunControlAction action,
                                                                   ManualRerunControlActionStatus actionStatus,
                                                                   LocalDateTime appliedAtFrom,
                                                                   LocalDateTime appliedAtTo) {
        return of(executionKey, action, actionStatus, appliedAtFrom, appliedAtTo, null, null, null);
    }

    public static ManualRerunControlActionHistoryServiceRequest of(String executionKey,
                                                                   ManualRerunControlAction action,
                                                                   ManualRerunControlActionStatus actionStatus,
                                                                   LocalDateTime appliedAtFrom,
                                                                   LocalDateTime appliedAtTo,
                                                                   ManualRerunControlActionHistorySortDirection sortDirection,
                                                                   Integer pageSize,
                                                                   LocalDateTime cursorAppliedAt) {
        return new ManualRerunControlActionHistoryServiceRequest(
                executionKey,
                action,
                actionStatus,
                appliedAtFrom,
                appliedAtTo,
                sortDirection,
                pageSize,
                cursorAppliedAt
        );
    }

    private static ManualRerunControlActionHistorySortDirection normalizeSortDirection(
            ManualRerunControlActionHistorySortDirection sortDirection,
            Integer pageSize,
            LocalDateTime cursorAppliedAt
    ) {
        if (sortDirection != null) {
            return sortDirection;
        }

        if (pageSize != null || cursorAppliedAt != null) {
            return ManualRerunControlActionHistorySortDirection.DESC;
        }

        return null;
    }
}
