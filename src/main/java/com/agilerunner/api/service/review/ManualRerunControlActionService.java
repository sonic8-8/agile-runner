package com.agilerunner.api.service.review;

import com.agilerunner.api.service.review.request.ManualRerunControlActionServiceRequest;
import com.agilerunner.api.service.review.response.ManualRerunControlActionServiceResponse;
import com.agilerunner.domain.review.ManualRerunControlActionStatus;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ManualRerunControlActionService {

    public ManualRerunControlActionServiceResponse execute(ManualRerunControlActionServiceRequest request) {
        return ManualRerunControlActionServiceResponse.of(
                request.getExecutionKey(),
                request.getAction(),
                ManualRerunControlActionStatus.APPLIED,
                List.of(),
                request.getNote()
        );
    }
}
