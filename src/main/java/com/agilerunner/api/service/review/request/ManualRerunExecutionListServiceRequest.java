package com.agilerunner.api.service.review.request;

import com.agilerunner.domain.agentruntime.ExecutionStartType;
import com.agilerunner.domain.agentruntime.WebhookExecutionStatus;
import com.agilerunner.domain.exception.FailureDisposition;
import lombok.Getter;

@Getter
public class ManualRerunExecutionListServiceRequest {
    private final String repositoryName;
    private final Integer pullRequestNumber;
    private final ExecutionStartType executionStartType;
    private final WebhookExecutionStatus executionStatus;
    private final FailureDisposition failureDisposition;

    private ManualRerunExecutionListServiceRequest(String repositoryName,
                                                   Integer pullRequestNumber,
                                                   ExecutionStartType executionStartType,
                                                   WebhookExecutionStatus executionStatus,
                                                   FailureDisposition failureDisposition) {
        this.repositoryName = repositoryName;
        this.pullRequestNumber = pullRequestNumber;
        this.executionStartType = executionStartType;
        this.executionStatus = executionStatus;
        this.failureDisposition = failureDisposition;
    }

    public static ManualRerunExecutionListServiceRequest of(String repositoryName,
                                                            Integer pullRequestNumber,
                                                            ExecutionStartType executionStartType,
                                                            WebhookExecutionStatus executionStatus,
                                                            FailureDisposition failureDisposition) {
        return new ManualRerunExecutionListServiceRequest(
                repositoryName,
                pullRequestNumber,
                executionStartType,
                executionStatus,
                failureDisposition
        );
    }
}
