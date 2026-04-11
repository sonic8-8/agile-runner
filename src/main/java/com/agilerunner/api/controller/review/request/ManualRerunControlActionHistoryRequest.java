package com.agilerunner.api.controller.review.request;

import com.agilerunner.api.service.review.request.ManualRerunControlActionHistoryServiceRequest;
import com.agilerunner.domain.review.ManualRerunControlAction;
import com.agilerunner.domain.review.ManualRerunControlActionStatus;
import lombok.Getter;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

@Getter
@Setter
public class ManualRerunControlActionHistoryRequest {
    private ManualRerunControlAction action;
    private ManualRerunControlActionStatus actionStatus;
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime appliedAtFrom;
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime appliedAtTo;

    public ManualRerunControlActionHistoryServiceRequest toServiceRequest(String executionKey) {
        return ManualRerunControlActionHistoryServiceRequest.of(
                executionKey,
                action,
                actionStatus,
                appliedAtFrom,
                appliedAtTo
        );
    }
}
