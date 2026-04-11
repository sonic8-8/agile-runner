package com.agilerunner.api.service.review;

import com.agilerunner.api.service.review.request.ManualRerunControlActionHistoryServiceRequest;
import com.agilerunner.api.service.review.response.ManualRerunControlActionHistoryServiceResponse;
import com.agilerunner.client.agentruntime.AgentRuntimeRepository;
import com.agilerunner.domain.agentruntime.ExecutionStartType;
import com.agilerunner.domain.agentruntime.WebhookExecution;
import com.agilerunner.domain.exception.ManualRerunQueryNotFoundException;
import com.agilerunner.domain.review.ManualRerunAvailableActionPolicy;
import com.agilerunner.domain.review.ManualRerunControlAction;
import com.agilerunner.domain.review.ManualRerunControlActionAudit;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;

@Service
public class ManualRerunControlActionHistoryService {
    private final AgentRuntimeRepository agentRuntimeRepository;
    private final ManualRerunAvailableActionPolicy availableActionPolicy = new ManualRerunAvailableActionPolicy();

    public ManualRerunControlActionHistoryService(@Nullable AgentRuntimeRepository agentRuntimeRepository) {
        this.agentRuntimeRepository = agentRuntimeRepository;
    }

    public ManualRerunControlActionHistoryServiceResponse find(ManualRerunControlActionHistoryServiceRequest request) {
        if (agentRuntimeRepository == null) {
            throw new ManualRerunQueryNotFoundException(
                    request.getExecutionKey(),
                    "재실행 결과를 찾을 수 없습니다."
            );
        }

        WebhookExecution webhookExecution = agentRuntimeRepository.findWebhookExecution(request.getExecutionKey())
                .filter(this::isManualRerunExecution)
                .orElseThrow(() -> new ManualRerunQueryNotFoundException(
                        request.getExecutionKey(),
                        "재실행 결과를 찾을 수 없습니다."
                ));
        ManualRerunControlActionAudit latestAppliedAudit = findLatestAppliedAudit(webhookExecution.getExecutionKey()).orElse(null);
        ManualRerunControlAction latestAppliedAction = latestAppliedAudit == null ? null : latestAppliedAudit.getAction();

        return ManualRerunControlActionHistoryServiceResponse.of(
                webhookExecution.getExecutionKey(),
                ManualRerunControlActionHistoryServiceResponse.CurrentActionState.of(
                        latestAppliedAction,
                        latestAppliedAudit == null ? null : latestAppliedAudit.getActionStatus(),
                        latestAppliedAudit == null ? null : latestAppliedAudit.getAppliedAt(),
                        availableActionPolicy.resolve(webhookExecution, latestAppliedAction)
                ),
                agentRuntimeRepository.findManualRerunControlActionAudits(
                                webhookExecution.getExecutionKey(),
                                request.getAction(),
                                request.getActionStatus(),
                                request.getAppliedAtFrom(),
                                request.getAppliedAtTo(),
                                request.getSortDirection(),
                                request.getPageSize(),
                                request.getCursorAppliedAt()
                        ).stream()
                        .map(audit -> ManualRerunControlActionHistoryServiceResponse.ActionHistorySummary.of(
                                audit.getAction(),
                                audit.getActionStatus(),
                                audit.getNote(),
                                audit.getAppliedAt()
                        ))
                        .toList()
        );
    }

    private boolean isManualRerunExecution(WebhookExecution webhookExecution) {
        return webhookExecution.getExecutionStartType() == ExecutionStartType.MANUAL_RERUN;
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
