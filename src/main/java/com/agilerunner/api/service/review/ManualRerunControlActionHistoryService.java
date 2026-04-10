package com.agilerunner.api.service.review;

import com.agilerunner.api.service.review.request.ManualRerunControlActionHistoryServiceRequest;
import com.agilerunner.api.service.review.response.ManualRerunControlActionHistoryServiceResponse;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ManualRerunControlActionHistoryService {

    public ManualRerunControlActionHistoryServiceResponse find(ManualRerunControlActionHistoryServiceRequest request) {
        return ManualRerunControlActionHistoryServiceResponse.of(request.getExecutionKey(), List.of());
    }
}
