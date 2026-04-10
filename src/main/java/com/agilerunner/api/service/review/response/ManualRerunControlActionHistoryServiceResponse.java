package com.agilerunner.api.service.review.response;

import com.agilerunner.domain.review.ManualRerunControlAction;
import com.agilerunner.domain.review.ManualRerunControlActionStatus;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
public class ManualRerunControlActionHistoryServiceResponse {
    private final String executionKey;
    private final List<ActionHistorySummary> actions;

    private ManualRerunControlActionHistoryServiceResponse(String executionKey,
                                                           List<ActionHistorySummary> actions) {
        this.executionKey = executionKey;
        this.actions = List.copyOf(actions);
    }

    public static ManualRerunControlActionHistoryServiceResponse of(String executionKey,
                                                                    List<ActionHistorySummary> actions) {
        return new ManualRerunControlActionHistoryServiceResponse(executionKey, actions);
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
}
