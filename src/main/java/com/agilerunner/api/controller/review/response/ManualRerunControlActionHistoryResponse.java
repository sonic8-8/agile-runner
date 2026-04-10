package com.agilerunner.api.controller.review.response;

import com.agilerunner.api.service.review.response.ManualRerunControlActionHistoryServiceResponse;
import com.agilerunner.domain.review.ManualRerunControlAction;
import com.agilerunner.domain.review.ManualRerunControlActionStatus;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
public class ManualRerunControlActionHistoryResponse {
    private final String executionKey;
    private final List<ActionHistoryItem> actions;

    private ManualRerunControlActionHistoryResponse(String executionKey,
                                                   List<ActionHistoryItem> actions) {
        this.executionKey = executionKey;
        this.actions = List.copyOf(actions);
    }

    public static ManualRerunControlActionHistoryResponse from(ManualRerunControlActionHistoryServiceResponse response) {
        return new ManualRerunControlActionHistoryResponse(
                response.getExecutionKey(),
                response.getActions().stream()
                        .map(ActionHistoryItem::from)
                        .toList()
        );
    }

    @Getter
    public static class ActionHistoryItem {
        private final ManualRerunControlAction action;
        private final ManualRerunControlActionStatus actionStatus;
        private final String note;
        private final LocalDateTime appliedAt;

        private ActionHistoryItem(ManualRerunControlAction action,
                                  ManualRerunControlActionStatus actionStatus,
                                  String note,
                                  LocalDateTime appliedAt) {
            this.action = action;
            this.actionStatus = actionStatus;
            this.note = note;
            this.appliedAt = appliedAt;
        }

        private static ActionHistoryItem from(ManualRerunControlActionHistoryServiceResponse.ActionHistorySummary summary) {
            return new ActionHistoryItem(
                    summary.getAction(),
                    summary.getActionStatus(),
                    summary.getNote(),
                    summary.getAppliedAt()
            );
        }
    }
}
