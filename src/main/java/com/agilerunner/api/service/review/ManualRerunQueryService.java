package com.agilerunner.api.service.review;

import com.agilerunner.api.service.review.request.ManualRerunQueryServiceRequest;
import com.agilerunner.api.service.review.response.ManualRerunQueryServiceResponse;
import com.agilerunner.domain.exception.ManualRerunQueryNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class ManualRerunQueryService {

    public ManualRerunQueryServiceResponse find(ManualRerunQueryServiceRequest request) {
        throw new ManualRerunQueryNotFoundException(
                request.getExecutionKey(),
                "재실행 결과를 찾을 수 없습니다."
        );
    }
}
