package com.agilerunner.api.service.review.request;

import com.agilerunner.domain.executioncontrol.ExecutionControlMode;
import lombok.Getter;

@Getter
public class ManualRerunServiceRequest {
    private final String repositoryName;
    private final int pullRequestNumber;
    private final long installationId;
    private final ExecutionControlMode executionControlMode;

    private ManualRerunServiceRequest(String repositoryName,
                                      int pullRequestNumber,
                                      long installationId,
                                      ExecutionControlMode executionControlMode) {
        this.repositoryName = repositoryName;
        this.pullRequestNumber = pullRequestNumber;
        this.installationId = installationId;
        this.executionControlMode = executionControlMode;
    }

    public static ManualRerunServiceRequest of(String repositoryName,
                                               int pullRequestNumber,
                                               long installationId,
                                               ExecutionControlMode executionControlMode) {
        return new ManualRerunServiceRequest(
                repositoryName,
                pullRequestNumber,
                installationId,
                executionControlMode
        );
    }
}
