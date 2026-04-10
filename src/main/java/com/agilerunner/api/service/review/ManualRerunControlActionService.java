package com.agilerunner.api.service.review;

import com.agilerunner.api.service.review.request.ManualRerunControlActionServiceRequest;
import com.agilerunner.api.service.review.response.ManualRerunControlActionServiceResponse;
import com.agilerunner.client.agentruntime.AgentRuntimeRepository;
import com.agilerunner.domain.agentruntime.WebhookExecution;
import com.agilerunner.domain.exception.ManualRerunControlActionConflictException;
import com.agilerunner.domain.exception.ManualRerunControlActionNotFoundException;
import com.agilerunner.domain.review.ManualRerunAvailableActionPolicy;
import com.agilerunner.domain.review.ManualRerunControlAction;
import com.agilerunner.domain.review.ManualRerunControlActionAudit;
import com.agilerunner.domain.review.ManualRerunControlActionEligibility;
import com.agilerunner.domain.review.ManualRerunControlActionStatus;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ManualRerunControlActionService {
    private final AgentRuntimeRepository agentRuntimeRepository;
    private final ManualRerunAvailableActionPolicy availableActionPolicy = new ManualRerunAvailableActionPolicy();

    public ManualRerunControlActionService(@Nullable AgentRuntimeRepository agentRuntimeRepository) {
        this.agentRuntimeRepository = agentRuntimeRepository;
    }

    public ManualRerunControlActionServiceResponse execute(ManualRerunControlActionServiceRequest request) {
        WebhookExecution sourceExecution = findSourceExecution(request.getExecutionKey());
        ManualRerunControlAction latestAppliedAction = findLatestAppliedAction(request.getExecutionKey());
        ManualRerunControlActionEligibility eligibility = availableActionPolicy.evaluate(
                sourceExecution,
                request.getAction(),
                latestAppliedAction
        );

        if (!eligibility.isActionAllowed()) {
            throw new ManualRerunControlActionConflictException(
                request.getExecutionKey(),
                    eligibility.getFailureDisposition(),
                    eligibility.getMessage()
            );
        }

        agentRuntimeRepository.appendManualRerunControlActionAudit(
                ManualRerunControlActionAudit.applied(
                        request.getExecutionKey(),
                        request.getAction(),
                        request.getNote(),
                        LocalDateTime.now()
                )
        );

        return ManualRerunControlActionServiceResponse.of(
                request.getExecutionKey(),
                request.getAction(),
                ManualRerunControlActionStatus.APPLIED,
                availableActionPolicy.resolve(sourceExecution, request.getAction()),
                request.getNote()
        );
    }

    private WebhookExecution findSourceExecution(String executionKey) {
        if (agentRuntimeRepository == null) {
            throw new ManualRerunControlActionNotFoundException(
                    executionKey,
                    "관리자 제어 액션 대상 실행을 찾을 수 없습니다."
            );
        }

        return agentRuntimeRepository.findWebhookExecution(executionKey)
                .orElseThrow(() -> new ManualRerunControlActionNotFoundException(
                        executionKey,
                        "관리자 제어 액션 대상 실행을 찾을 수 없습니다."
                ));
    }

    private ManualRerunControlAction findLatestAppliedAction(String executionKey) {
        if (agentRuntimeRepository == null) {
            return null;
        }

        return agentRuntimeRepository.findLatestAppliedManualRerunControlAction(executionKey)
                .orElse(null);
    }
}
