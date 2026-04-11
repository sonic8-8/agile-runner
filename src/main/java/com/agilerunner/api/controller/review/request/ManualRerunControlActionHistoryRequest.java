package com.agilerunner.api.controller.review.request;

import com.agilerunner.api.service.review.request.ManualRerunControlActionHistoryServiceRequest;
import com.agilerunner.domain.review.ManualRerunControlAction;
import com.agilerunner.domain.review.ManualRerunControlActionStatus;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ManualRerunControlActionHistoryRequest {
    private ManualRerunControlAction action;
    private ManualRerunControlActionStatus actionStatus;

    public ManualRerunControlActionHistoryServiceRequest toServiceRequest(String executionKey) {
        return ManualRerunControlActionHistoryServiceRequest.of(
                executionKey,
                action,
                actionStatus
        );
    }
}
