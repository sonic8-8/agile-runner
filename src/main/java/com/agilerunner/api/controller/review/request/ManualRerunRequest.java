package com.agilerunner.api.controller.review.request;

import com.agilerunner.api.service.review.request.ManualRerunServiceRequest;
import com.agilerunner.domain.executioncontrol.ExecutionControlMode;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

import java.util.List;

@Getter
public class ManualRerunRequest {
    private final String repositoryName;
    private final int pullRequestNumber;
    private final long installationId;
    private final ExecutionControlMode executionControlMode;
    private final List<String> selectedPaths;

    private ManualRerunRequest(String repositoryName,
                               int pullRequestNumber,
                               long installationId,
                               ExecutionControlMode executionControlMode,
                               List<String> selectedPaths) {
        this.repositoryName = repositoryName;
        this.pullRequestNumber = pullRequestNumber;
        this.installationId = installationId;
        this.executionControlMode = executionControlMode;
        this.selectedPaths = List.copyOf(selectedPaths);
    }

    @JsonCreator
    public static ManualRerunRequest of(@JsonProperty("repositoryName") String repositoryName,
                                        @JsonProperty("pullRequestNumber") int pullRequestNumber,
                                        @JsonProperty("installationId") long installationId,
                                        @JsonProperty("executionControlMode") ExecutionControlMode executionControlMode,
                                        @JsonProperty("selectedPaths") List<String> selectedPaths) {
        return new ManualRerunRequest(
                repositoryName,
                pullRequestNumber,
                installationId,
                executionControlMode,
                normalizeSelectedPaths(selectedPaths)
        );
    }

    public static ManualRerunRequest of(String repositoryName,
                                        int pullRequestNumber,
                                        long installationId,
                                        ExecutionControlMode executionControlMode) {
        return of(repositoryName, pullRequestNumber, installationId, executionControlMode, List.of());
    }

    public ManualRerunServiceRequest toServiceRequest() {
        return ManualRerunServiceRequest.of(
                repositoryName,
                pullRequestNumber,
                installationId,
                executionControlMode,
                selectedPaths
        );
    }

    private static List<String> normalizeSelectedPaths(List<String> selectedPaths) {
        if (selectedPaths == null) {
            return List.of();
        }

        return selectedPaths;
    }
}
