package com.agilerunner.api.service.review;

import com.agilerunner.api.service.review.request.ManualRerunControlActionHistoryServiceRequest;
import com.agilerunner.api.service.review.response.ManualRerunControlActionHistoryServiceResponse;
import com.agilerunner.client.agentruntime.AgentRuntimeRepository;
import com.agilerunner.domain.agentruntime.ExecutionStartType;
import com.agilerunner.domain.agentruntime.WebhookExecution;
import com.agilerunner.domain.exception.ManualRerunQueryNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.lang.Nullable;

@Service
public class ManualRerunControlActionHistoryService {
    private final AgentRuntimeRepository agentRuntimeRepository;

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

        return ManualRerunControlActionHistoryServiceResponse.of(
                webhookExecution.getExecutionKey(),
                agentRuntimeRepository.findManualRerunControlActionAudits(webhookExecution.getExecutionKey()).stream()
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
}
