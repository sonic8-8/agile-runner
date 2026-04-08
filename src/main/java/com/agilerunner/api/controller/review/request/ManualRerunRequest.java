package com.agilerunner.api.controller.review.request;

import com.agilerunner.api.service.review.request.ManualRerunServiceRequest;
import com.agilerunner.domain.executioncontrol.ExecutionControlMode;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

@Getter
public class ManualRerunRequest {
    private final String repositoryName;
    private final int pullRequestNumber;
    private final long installationId;
    private final ExecutionControlMode executionControlMode;

    private ManualRerunRequest(String repositoryName,
                               int pullRequestNumber,
                               long installationId,
                               ExecutionControlMode executionControlMode) {
        this.repositoryName = repositoryName;
        this.pullRequestNumber = pullRequestNumber;
        this.installationId = installationId;
        this.executionControlMode = executionControlMode;
    }

    @JsonCreator
    public static ManualRerunRequest of(@JsonProperty("repositoryName") String repositoryName,
                                        @JsonProperty("pullRequestNumber") int pullRequestNumber,
                                        @JsonProperty("installationId") long installationId,
                                        @JsonProperty("executionControlMode") ExecutionControlMode executionControlMode) {
        return new ManualRerunRequest(repositoryName, pullRequestNumber, installationId, executionControlMode);
    }

    public ManualRerunServiceRequest toServiceRequest() {
        return ManualRerunServiceRequest.of(
                repositoryName,
                pullRequestNumber,
                installationId,
                executionControlMode
        );
    }
}
