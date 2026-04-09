package com.agilerunner.domain.review;

import com.agilerunner.domain.agentruntime.ExecutionStartType;
import com.agilerunner.domain.agentruntime.WebhookExecution;
import com.agilerunner.domain.agentruntime.WebhookExecutionStatus;
import com.agilerunner.domain.exception.FailureDisposition;

public class ManualRerunRetryEligibilityPolicy {

    public ManualRerunRetryEligibility evaluate(WebhookExecution sourceExecution) {
        if (sourceExecution.getExecutionStartType() != ExecutionStartType.MANUAL_RERUN) {
            return ManualRerunRetryEligibility.rejected(
                    sourceExecution.getFailureDisposition(),
                    "수동 재실행으로 생성된 실행만 재시도할 수 있습니다."
            );
        }

        if (sourceExecution.getStatus() != WebhookExecutionStatus.FAILED) {
            return ManualRerunRetryEligibility.rejected(
                    sourceExecution.getFailureDisposition(),
                    "실패한 재실행만 재시도할 수 있습니다."
            );
        }

        if (sourceExecution.getFailureDisposition() == FailureDisposition.RETRYABLE) {
            return ManualRerunRetryEligibility.allowed(sourceExecution.getFailureDisposition());
        }

        return ManualRerunRetryEligibility.rejected(
                sourceExecution.getFailureDisposition(),
                getRejectionMessage(sourceExecution.getFailureDisposition())
        );
    }

    private String getRejectionMessage(FailureDisposition failureDisposition) {
        if (failureDisposition == FailureDisposition.MANUAL_ACTION_REQUIRED) {
            return "수동 조치가 필요한 실행은 바로 재시도할 수 없습니다.";
        }

        if (failureDisposition == FailureDisposition.NON_RETRYABLE) {
            return "재시도 불가 실행입니다.";
        }

        return "재시도 가능 여부를 판단할 수 없습니다.";
    }
}
