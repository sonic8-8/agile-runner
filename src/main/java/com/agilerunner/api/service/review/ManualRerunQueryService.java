package com.agilerunner.api.service.review;

import com.agilerunner.api.service.review.request.ManualRerunQueryServiceRequest;
import com.agilerunner.api.service.review.response.ManualRerunQueryServiceResponse;
import com.agilerunner.client.agentruntime.AgentRuntimeRepository;
import com.agilerunner.domain.agentruntime.ExecutionStartType;
import com.agilerunner.domain.agentruntime.WebhookExecution;
import com.agilerunner.domain.agentruntime.WebhookExecutionStatus;
import com.agilerunner.domain.exception.ManualRerunQueryNotFoundException;
import com.agilerunner.domain.review.RerunExecutionStatus;
import org.springframework.stereotype.Service;
import org.springframework.lang.Nullable;

@Service
public class ManualRerunQueryService {
    private final AgentRuntimeRepository agentRuntimeRepository;

    public ManualRerunQueryService(@Nullable AgentRuntimeRepository agentRuntimeRepository) {
        this.agentRuntimeRepository = agentRuntimeRepository;
    }

    public ManualRerunQueryServiceResponse find(ManualRerunQueryServiceRequest request) {
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

        return ManualRerunQueryServiceResponse.of(
                webhookExecution.getExecutionKey(),
                webhookExecution.getExecutionControlMode(),
                Boolean.TRUE.equals(webhookExecution.getWritePerformed()),
                toRerunExecutionStatus(webhookExecution),
                webhookExecution.getErrorCode(),
                webhookExecution.getFailureDisposition()
        );
    }

    private boolean isManualRerunExecution(WebhookExecution webhookExecution) {
        return webhookExecution.getExecutionStartType() == ExecutionStartType.MANUAL_RERUN;
    }

    private RerunExecutionStatus toRerunExecutionStatus(WebhookExecution webhookExecution) {
        if (webhookExecution.getStatus() == WebhookExecutionStatus.FAILED) {
            return RerunExecutionStatus.FAILED;
        }

        return RerunExecutionStatus.SUCCEEDED;
    }
}
