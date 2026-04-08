package com.agilerunner.api.service.review;

import com.agilerunner.api.service.review.request.ManualRerunServiceRequest;
import com.agilerunner.api.service.review.response.ManualRerunServiceResponse;
import org.springframework.stereotype.Service;

@Service
public class ManualRerunService {

    public ManualRerunServiceResponse rerun(ManualRerunServiceRequest request) {
        return ManualRerunServiceResponse.pending(request.getExecutionControlMode());
    }
}
