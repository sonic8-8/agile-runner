package com.agilerunner.api.service.review.response;

import com.agilerunner.domain.review.ManualRerunAvailableAction;
import com.agilerunner.domain.review.ManualRerunControlAction;
import com.agilerunner.domain.review.ManualRerunControlActionStatus;
import lombok.Getter;

import java.util.List;

@Getter
public class ManualRerunControlActionServiceResponse {
    private final String executionKey;
    private final ManualRerunControlAction action;
    private final ManualRerunControlActionStatus actionStatus;
    private final List<ManualRerunAvailableAction> availableActions;
    private final String note;

    private ManualRerunControlActionServiceResponse(String executionKey,
                                                    ManualRerunControlAction action,
                                                    ManualRerunControlActionStatus actionStatus,
                                                    List<ManualRerunAvailableAction> availableActions,
                                                    String note) {
        this.executionKey = executionKey;
        this.action = action;
        this.actionStatus = actionStatus;
        this.availableActions = List.copyOf(availableActions);
        this.note = note;
    }

    public static ManualRerunControlActionServiceResponse of(String executionKey,
                                                             ManualRerunControlAction action,
                                                             ManualRerunControlActionStatus actionStatus,
                                                             List<ManualRerunAvailableAction> availableActions,
                                                             String note) {
        return new ManualRerunControlActionServiceResponse(
                executionKey,
                action,
                actionStatus,
                availableActions,
                note
        );
    }
}
