package com.agilerunner.api.service.review.request;

import com.agilerunner.domain.executioncontrol.ExecutionControlMode;
import lombok.Getter;

import java.util.List;

@Getter
public class ManualRerunServiceRequest {
    private final String repositoryName;
    private final int pullRequestNumber;
    private final long installationId;
    private final ExecutionControlMode executionControlMode;
    private final List<String> selectedPaths;
    private final String retrySourceExecutionKey;

    private ManualRerunServiceRequest(String repositoryName,
                                      int pullRequestNumber,
                                      long installationId,
                                      ExecutionControlMode executionControlMode,
                                      List<String> selectedPaths,
                                      String retrySourceExecutionKey) {
        this.repositoryName = repositoryName;
        this.pullRequestNumber = pullRequestNumber;
        this.installationId = installationId;
        this.executionControlMode = executionControlMode;
        this.selectedPaths = List.copyOf(selectedPaths);
        this.retrySourceExecutionKey = retrySourceExecutionKey;
    }

    public static ManualRerunServiceRequest of(String repositoryName,
                                               int pullRequestNumber,
                                               long installationId,
                                               ExecutionControlMode executionControlMode,
                                               List<String> selectedPaths) {
        return of(repositoryName, pullRequestNumber, installationId, executionControlMode, selectedPaths, null);
    }

    public static ManualRerunServiceRequest of(String repositoryName,
                                               int pullRequestNumber,
                                               long installationId,
                                               ExecutionControlMode executionControlMode,
                                               List<String> selectedPaths,
                                               String retrySourceExecutionKey) {
        return new ManualRerunServiceRequest(
                repositoryName,
                pullRequestNumber,
                installationId,
                executionControlMode,
                normalizeSelectedPaths(selectedPaths),
                retrySourceExecutionKey
        );
    }

    public static ManualRerunServiceRequest of(String repositoryName,
                                               int pullRequestNumber,
                                               long installationId,
                                               ExecutionControlMode executionControlMode) {
        return of(repositoryName, pullRequestNumber, installationId, executionControlMode, List.of());
    }

    private static List<String> normalizeSelectedPaths(List<String> selectedPaths) {
        if (selectedPaths == null) {
            return List.of();
        }

        return selectedPaths;
    }
}
