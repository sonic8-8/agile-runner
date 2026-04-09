package com.agilerunner.api.controller.review.request;

import com.agilerunner.api.service.review.request.ManualRerunControlActionServiceRequest;
import com.agilerunner.domain.review.ManualRerunControlAction;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

@Getter
public class ManualRerunControlActionRequest {
    private final ManualRerunControlAction action;
    private final String note;

    private ManualRerunControlActionRequest(ManualRerunControlAction action, String note) {
        this.action = action;
        this.note = note;
    }

    @JsonCreator
    public static ManualRerunControlActionRequest of(@JsonProperty("action") ManualRerunControlAction action,
                                                     @JsonProperty("note") String note) {
        return new ManualRerunControlActionRequest(action, note);
    }

    public ManualRerunControlActionServiceRequest toServiceRequest(String executionKey) {
        return ManualRerunControlActionServiceRequest.of(executionKey, action, note);
    }
}
