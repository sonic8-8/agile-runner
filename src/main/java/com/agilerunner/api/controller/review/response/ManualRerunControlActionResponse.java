package com.agilerunner.api.controller.review.response;

import com.agilerunner.api.service.review.response.ManualRerunControlActionServiceResponse;
import com.agilerunner.domain.review.ManualRerunAvailableAction;
import com.agilerunner.domain.review.ManualRerunControlAction;
import com.agilerunner.domain.review.ManualRerunControlActionStatus;
import lombok.Getter;

import java.util.List;

@Getter
public class ManualRerunControlActionResponse {
    private final String executionKey;
    private final ManualRerunControlAction action;
    private final ManualRerunControlActionStatus actionStatus;
    private final List<ManualRerunAvailableAction> availableActions;
    private final String note;

    private ManualRerunControlActionResponse(String executionKey,
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

    public static ManualRerunControlActionResponse from(ManualRerunControlActionServiceResponse response) {
        return new ManualRerunControlActionResponse(
                response.getExecutionKey(),
                response.getAction(),
                response.getActionStatus(),
                response.getAvailableActions(),
                response.getNote()
        );
    }
}
