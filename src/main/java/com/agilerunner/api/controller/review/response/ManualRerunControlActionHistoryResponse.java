package com.agilerunner.api.controller.review.response;

import com.agilerunner.api.service.review.response.ManualRerunControlActionHistoryServiceResponse;
import com.agilerunner.domain.review.ManualRerunAvailableAction;
import com.agilerunner.domain.review.ManualRerunControlAction;
import com.agilerunner.domain.review.ManualRerunControlActionStatus;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
public class ManualRerunControlActionHistoryResponse {
    private final String executionKey;
    private final CurrentActionState currentActionState;
    private final List<ActionHistoryItem> actions;

    private ManualRerunControlActionHistoryResponse(String executionKey,
                                                   CurrentActionState currentActionState,
                                                   List<ActionHistoryItem> actions) {
        this.executionKey = executionKey;
        this.currentActionState = currentActionState;
        this.actions = List.copyOf(actions);
    }

    public static ManualRerunControlActionHistoryResponse from(ManualRerunControlActionHistoryServiceResponse response) {
        return new ManualRerunControlActionHistoryResponse(
                response.getExecutionKey(),
                CurrentActionState.from(response.getCurrentActionState()),
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

    @Getter
    public static class CurrentActionState {
        private final ManualRerunControlAction latestAction;
        private final ManualRerunControlActionStatus latestActionStatus;
        private final LocalDateTime latestActionAppliedAt;
        private final List<ManualRerunAvailableAction> availableActions;

        private CurrentActionState(ManualRerunControlAction latestAction,
                                   ManualRerunControlActionStatus latestActionStatus,
                                   LocalDateTime latestActionAppliedAt,
                                   List<ManualRerunAvailableAction> availableActions) {
            this.latestAction = latestAction;
            this.latestActionStatus = latestActionStatus;
            this.latestActionAppliedAt = latestActionAppliedAt;
            this.availableActions = List.copyOf(availableActions);
        }

        private static CurrentActionState from(ManualRerunControlActionHistoryServiceResponse.CurrentActionState state) {
            return new CurrentActionState(
                    state.getLatestAction(),
                    state.getLatestActionStatus(),
                    state.getLatestActionAppliedAt(),
                    state.getAvailableActions()
            );
        }
    }
}
