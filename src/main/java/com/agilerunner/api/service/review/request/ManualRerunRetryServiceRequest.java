package com.agilerunner.api.service.review.request;

import com.agilerunner.domain.executioncontrol.ExecutionControlMode;
import lombok.Getter;

import java.util.List;

@Getter
public class ManualRerunRetryServiceRequest {
    private final String sourceExecutionKey;
    private final long installationId;
    private final ExecutionControlMode executionControlMode;
    private final List<String> selectedPaths;

    private ManualRerunRetryServiceRequest(String sourceExecutionKey,
                                           long installationId,
                                           ExecutionControlMode executionControlMode,
                                           List<String> selectedPaths) {
        this.sourceExecutionKey = sourceExecutionKey;
        this.installationId = installationId;
        this.executionControlMode = executionControlMode;
        this.selectedPaths = List.copyOf(selectedPaths);
    }

    public static ManualRerunRetryServiceRequest of(String sourceExecutionKey,
                                                    long installationId,
                                                    ExecutionControlMode executionControlMode,
                                                    List<String> selectedPaths) {
        return new ManualRerunRetryServiceRequest(
                sourceExecutionKey,
                installationId,
                executionControlMode,
                normalizeSelectedPaths(selectedPaths)
        );
    }

    private static List<String> normalizeSelectedPaths(List<String> selectedPaths) {
        if (selectedPaths == null) {
            return List.of();
        }

        return selectedPaths;
    }
}
