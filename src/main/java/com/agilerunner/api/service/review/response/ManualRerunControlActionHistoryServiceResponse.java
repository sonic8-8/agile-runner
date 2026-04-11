package com.agilerunner.api.service.review.response;

import com.agilerunner.domain.review.ManualRerunControlAction;
import com.agilerunner.domain.review.ManualRerunAvailableAction;
import com.agilerunner.domain.review.ManualRerunControlActionStatus;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
public class ManualRerunControlActionHistoryServiceResponse {
    private final String executionKey;
    private final CurrentActionState currentActionState;
    private final List<ActionHistorySummary> actions;

    private ManualRerunControlActionHistoryServiceResponse(String executionKey,
                                                           CurrentActionState currentActionState,
                                                           List<ActionHistorySummary> actions) {
        this.executionKey = executionKey;
        this.currentActionState = currentActionState;
        this.actions = List.copyOf(actions);
    }

    public static ManualRerunControlActionHistoryServiceResponse of(String executionKey,
                                                                    List<ActionHistorySummary> actions) {
        return new ManualRerunControlActionHistoryServiceResponse(
                executionKey,
                CurrentActionState.empty(),
                actions
        );
    }

    public static ManualRerunControlActionHistoryServiceResponse of(String executionKey,
                                                                    CurrentActionState currentActionState,
                                                                    List<ActionHistorySummary> actions) {
        return new ManualRerunControlActionHistoryServiceResponse(executionKey, currentActionState, actions);
    }

    @Getter
    public static class ActionHistorySummary {
        private final ManualRerunControlAction action;
        private final ManualRerunControlActionStatus actionStatus;
        private final String note;
        private final LocalDateTime appliedAt;

        private ActionHistorySummary(ManualRerunControlAction action,
                                     ManualRerunControlActionStatus actionStatus,
                                     String note,
                                     LocalDateTime appliedAt) {
            this.action = action;
            this.actionStatus = actionStatus;
            this.note = note;
            this.appliedAt = appliedAt;
        }

        public static ActionHistorySummary of(ManualRerunControlAction action,
                                              ManualRerunControlActionStatus actionStatus,
                                              String note,
                                              LocalDateTime appliedAt) {
            return new ActionHistorySummary(action, actionStatus, note, appliedAt);
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

        public static CurrentActionState of(ManualRerunControlAction latestAction,
                                            ManualRerunControlActionStatus latestActionStatus,
                                            LocalDateTime latestActionAppliedAt,
                                            List<ManualRerunAvailableAction> availableActions) {
            return new CurrentActionState(
                    latestAction,
                    latestActionStatus,
                    latestActionAppliedAt,
                    availableActions
            );
        }

        public static CurrentActionState empty() {
            return new CurrentActionState(null, null, null, List.of());
        }
    }
}
