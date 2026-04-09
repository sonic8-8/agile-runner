package com.agilerunner.api.controller.review.request;

import com.agilerunner.api.service.review.request.ManualRerunExecutionListServiceRequest;
import com.agilerunner.domain.agentruntime.ExecutionStartType;
import com.agilerunner.domain.agentruntime.WebhookExecutionStatus;
import com.agilerunner.domain.exception.FailureDisposition;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ManualRerunExecutionListRequest {
    private String repositoryName;
    private Integer pullRequestNumber;
    private ExecutionStartType executionStartType;
    private WebhookExecutionStatus executionStatus;
    private FailureDisposition failureDisposition;

    public ManualRerunExecutionListServiceRequest toServiceRequest() {
        return ManualRerunExecutionListServiceRequest.of(
                repositoryName,
                pullRequestNumber,
                executionStartType,
                executionStatus,
                failureDisposition
        );
    }
}
