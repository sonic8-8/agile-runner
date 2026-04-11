package com.agilerunner.api.service.review;

import com.agilerunner.api.service.review.request.ManualRerunExecutionListServiceRequest;
import com.agilerunner.api.service.review.response.ManualRerunExecutionListServiceResponse;
import com.agilerunner.client.agentruntime.AgentRuntimeRepository;
import com.agilerunner.domain.agentruntime.WebhookExecution;
import com.agilerunner.domain.agentruntime.WebhookExecutionStatus;
import com.agilerunner.domain.review.ManualRerunAvailableAction;
import com.agilerunner.domain.review.ManualRerunControlAction;
import com.agilerunner.domain.review.ManualRerunControlActionAudit;
import com.agilerunner.domain.review.ManualRerunAvailableActionPolicy;
import com.agilerunner.domain.review.RerunExecutionStatus;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ManualRerunExecutionListService {
    private final AgentRuntimeRepository agentRuntimeRepository;
    private final ManualRerunAvailableActionPolicy availableActionPolicy = new ManualRerunAvailableActionPolicy();

    public ManualRerunExecutionListService(@Nullable AgentRuntimeRepository agentRuntimeRepository) {
        this.agentRuntimeRepository = agentRuntimeRepository;
    }

    public ManualRerunExecutionListServiceResponse list(ManualRerunExecutionListServiceRequest request) {
        if (agentRuntimeRepository == null) {
            return ManualRerunExecutionListServiceResponse.of(List.of());
        }

        List<ManualRerunExecutionListServiceResponse.ExecutionSummary> executions =
                agentRuntimeRepository.findManualRerunExecutions().stream()
                        .filter(execution -> matchesRepositoryName(request, execution))
                        .filter(execution -> matchesPullRequestNumber(request, execution))
                        .filter(execution -> matchesExecutionStartType(request, execution))
                        .filter(execution -> matchesExecutionStatus(request, execution))
                        .filter(execution -> matchesFailureDisposition(request, execution))
                        .map(this::toExecutionSummary)
                        .toList();

        return ManualRerunExecutionListServiceResponse.of(executions);
    }

    private boolean matchesRepositoryName(ManualRerunExecutionListServiceRequest request, WebhookExecution execution) {
        if (request.getRepositoryName() == null) {
            return true;
        }

        return request.getRepositoryName().equals(execution.getRepositoryName());
    }

    private boolean matchesPullRequestNumber(ManualRerunExecutionListServiceRequest request, WebhookExecution execution) {
        if (request.getPullRequestNumber() == null) {
            return true;
        }

        return request.getPullRequestNumber() == execution.getPullRequestNumber();
    }

    private boolean matchesExecutionStartType(ManualRerunExecutionListServiceRequest request, WebhookExecution execution) {
        if (request.getExecutionStartType() == null) {
            return true;
        }

        return request.getExecutionStartType() == execution.getExecutionStartType();
    }

    private boolean matchesExecutionStatus(ManualRerunExecutionListServiceRequest request, WebhookExecution execution) {
        if (request.getExecutionStatus() == null) {
            return true;
        }

        return request.getExecutionStatus() == execution.getStatus();
    }

    private boolean matchesFailureDisposition(ManualRerunExecutionListServiceRequest request, WebhookExecution execution) {
        if (request.getFailureDisposition() == null) {
            return true;
        }

        return request.getFailureDisposition() == execution.getFailureDisposition();
    }

    private ManualRerunExecutionListServiceResponse.ExecutionSummary toExecutionSummary(WebhookExecution execution) {
        ManualRerunControlActionAudit latestAppliedAudit = findLatestAppliedAudit(execution.getExecutionKey()).orElse(null);
        return ManualRerunExecutionListServiceResponse.ExecutionSummary.of(
                execution.getExecutionKey(),
                execution.getRetrySourceExecutionKey(),
                execution.getExecutionStartType(),
                toRerunExecutionStatus(execution),
                execution.getExecutionControlMode(),
                Boolean.TRUE.equals(execution.getWritePerformed()),
                execution.getErrorCode(),
                execution.getFailureDisposition(),
                latestAppliedAudit == null ? null : latestAppliedAudit.getAction(),
                latestAppliedAudit == null ? null : latestAppliedAudit.getActionStatus(),
                latestAppliedAudit == null ? null : latestAppliedAudit.getAppliedAt(),
                latestAppliedAudit != null,
                resolveAvailableActions(execution)
        );
    }

    private RerunExecutionStatus toRerunExecutionStatus(WebhookExecution execution) {
        if (execution.getStatus() == WebhookExecutionStatus.FAILED) {
            return RerunExecutionStatus.FAILED;
        }

        return RerunExecutionStatus.SUCCEEDED;
    }

    private List<ManualRerunAvailableAction> resolveAvailableActions(WebhookExecution execution) {
        if (agentRuntimeRepository == null) {
            return List.of();
        }

        ManualRerunControlAction latestAppliedAction = agentRuntimeRepository.findLatestAppliedManualRerunControlAction(
                execution.getExecutionKey()
        ).orElse(null);
        return availableActionPolicy.resolve(execution, latestAppliedAction);
    }

    private java.util.Optional<ManualRerunControlActionAudit> findLatestAppliedAudit(String executionKey) {
        if (agentRuntimeRepository == null) {
            return java.util.Optional.empty();
        }

        java.util.Optional<ManualRerunControlActionAudit> latestAppliedAudit =
                agentRuntimeRepository.findLatestAppliedManualRerunControlActionAudit(executionKey);
        if (latestAppliedAudit == null) {
            return java.util.Optional.empty();
        }

        return latestAppliedAudit;
    }
}
