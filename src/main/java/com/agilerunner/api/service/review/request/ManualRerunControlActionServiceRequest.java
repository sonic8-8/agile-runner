package com.agilerunner.api.service.review.request;

import com.agilerunner.domain.review.ManualRerunControlAction;
import lombok.Getter;

@Getter
public class ManualRerunControlActionServiceRequest {
    private final String executionKey;
    private final ManualRerunControlAction action;
    private final String note;

    private ManualRerunControlActionServiceRequest(String executionKey,
                                                   ManualRerunControlAction action,
                                                   String note) {
        this.executionKey = executionKey;
        this.action = action;
        this.note = note;
    }

    public static ManualRerunControlActionServiceRequest of(String executionKey,
                                                            ManualRerunControlAction action,
                                                            String note) {
        return new ManualRerunControlActionServiceRequest(executionKey, action, note);
    }
}
