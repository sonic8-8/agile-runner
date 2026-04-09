package com.agilerunner.api.service.review;

import com.agilerunner.api.service.review.request.ManualRerunRetryServiceRequest;
import com.agilerunner.api.service.review.request.ManualRerunServiceRequest;
import com.agilerunner.api.service.review.response.ManualRerunRetryServiceResponse;
import com.agilerunner.api.service.review.response.ManualRerunServiceResponse;
import com.agilerunner.client.agentruntime.AgentRuntimeRepository;
import com.agilerunner.domain.agentruntime.WebhookExecution;
import com.agilerunner.domain.exception.ManualRerunRetryConflictException;
import com.agilerunner.domain.exception.ManualRerunRetryNotFoundException;
import com.agilerunner.domain.review.ManualRerunRetryEligibility;
import com.agilerunner.domain.review.ManualRerunRetryEligibilityPolicy;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;

@Service
public class ManualRerunRetryService {
    private final AgentRuntimeRepository agentRuntimeRepository;
    private final ManualRerunService manualRerunService;
    private final ManualRerunRetryEligibilityPolicy retryEligibilityPolicy = new ManualRerunRetryEligibilityPolicy();

    public ManualRerunRetryService(@Nullable AgentRuntimeRepository agentRuntimeRepository,
                                   ManualRerunService manualRerunService) {
        this.agentRuntimeRepository = agentRuntimeRepository;
        this.manualRerunService = manualRerunService;
    }

    public ManualRerunRetryServiceResponse retry(ManualRerunRetryServiceRequest request) {
        WebhookExecution sourceExecution = findSourceExecution(request.getSourceExecutionKey());
        ManualRerunRetryEligibility eligibility = retryEligibilityPolicy.evaluate(sourceExecution);

        if (!eligibility.isRetryAllowed()) {
            throw new ManualRerunRetryConflictException(
                    request.getSourceExecutionKey(),
                    eligibility.getFailureDisposition(),
                    eligibility.getMessage()
            );
        }

        ManualRerunServiceResponse rerunResponse = manualRerunService.rerun(
                ManualRerunServiceRequest.of(
                        sourceExecution.getRepositoryName(),
                        sourceExecution.getPullRequestNumber(),
                        request.getInstallationId(),
                        request.getExecutionControlMode(),
                        request.getSelectedPaths()
                )
        );

        return ManualRerunRetryServiceResponse.of(
                rerunResponse.getExecutionKey(),
                request.getSourceExecutionKey(),
                rerunResponse.getExecutionControlMode(),
                rerunResponse.isWritePerformed(),
                rerunResponse.getExecutionStatus(),
                rerunResponse.getErrorCode(),
                rerunResponse.getFailureDisposition()
        );
    }

    private WebhookExecution findSourceExecution(String sourceExecutionKey) {
        if (agentRuntimeRepository == null) {
            throw new ManualRerunRetryNotFoundException(
                    sourceExecutionKey,
                    "재시도 대상 실행을 찾을 수 없습니다."
            );
        }

        return agentRuntimeRepository.findWebhookExecution(sourceExecutionKey)
                .orElseThrow(() -> new ManualRerunRetryNotFoundException(
                        sourceExecutionKey,
                        "재시도 대상 실행을 찾을 수 없습니다."
                ));
    }
}
