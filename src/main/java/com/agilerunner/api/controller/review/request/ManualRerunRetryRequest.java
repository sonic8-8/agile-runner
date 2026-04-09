package com.agilerunner.api.controller.review.request;

import com.agilerunner.api.service.review.request.ManualRerunRetryServiceRequest;
import com.agilerunner.domain.executioncontrol.ExecutionControlMode;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

import java.util.List;

@Getter
public class ManualRerunRetryRequest {
    private final long installationId;
    private final ExecutionControlMode executionControlMode;
    private final List<String> selectedPaths;

    private ManualRerunRetryRequest(long installationId,
                                    ExecutionControlMode executionControlMode,
                                    List<String> selectedPaths) {
        this.installationId = installationId;
        this.executionControlMode = executionControlMode;
        this.selectedPaths = List.copyOf(selectedPaths);
    }

    @JsonCreator
    public static ManualRerunRetryRequest of(@JsonProperty("installationId") long installationId,
                                             @JsonProperty("executionControlMode") ExecutionControlMode executionControlMode,
                                             @JsonProperty("selectedPaths") List<String> selectedPaths) {
        return new ManualRerunRetryRequest(
                installationId,
                executionControlMode,
                normalizeSelectedPaths(selectedPaths)
        );
    }

    public ManualRerunRetryServiceRequest toServiceRequest(String sourceExecutionKey) {
        return ManualRerunRetryServiceRequest.of(
                sourceExecutionKey,
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
