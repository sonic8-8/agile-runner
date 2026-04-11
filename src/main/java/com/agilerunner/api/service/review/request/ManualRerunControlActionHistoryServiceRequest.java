package com.agilerunner.api.service.review.request;

import com.agilerunner.domain.review.ManualRerunControlAction;
import com.agilerunner.domain.review.ManualRerunControlActionStatus;
import lombok.Getter;

@Getter
public class ManualRerunControlActionHistoryServiceRequest {
    private final String executionKey;
    private final ManualRerunControlAction action;
    private final ManualRerunControlActionStatus actionStatus;

    private ManualRerunControlActionHistoryServiceRequest(String executionKey,
                                                         ManualRerunControlAction action,
                                                         ManualRerunControlActionStatus actionStatus) {
        this.executionKey = executionKey;
        this.action = action;
        this.actionStatus = actionStatus;
    }

    public static ManualRerunControlActionHistoryServiceRequest of(String executionKey) {
        return new ManualRerunControlActionHistoryServiceRequest(executionKey, null, null);
    }

    public static ManualRerunControlActionHistoryServiceRequest of(String executionKey,
                                                                   ManualRerunControlAction action,
                                                                   ManualRerunControlActionStatus actionStatus) {
        return new ManualRerunControlActionHistoryServiceRequest(
                executionKey,
                action,
                actionStatus
        );
    }
}
