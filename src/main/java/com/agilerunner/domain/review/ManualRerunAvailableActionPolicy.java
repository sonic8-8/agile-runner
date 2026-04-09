package com.agilerunner.domain.review;

import com.agilerunner.domain.agentruntime.ExecutionStartType;
import com.agilerunner.domain.agentruntime.WebhookExecution;
import com.agilerunner.domain.agentruntime.WebhookExecutionStatus;
import com.agilerunner.domain.exception.FailureDisposition;

import java.util.ArrayList;
import java.util.List;

public class ManualRerunAvailableActionPolicy {
    private final ManualRerunRetryEligibilityPolicy retryEligibilityPolicy = new ManualRerunRetryEligibilityPolicy();

    public List<ManualRerunAvailableAction> resolve(WebhookExecution execution, boolean acknowledgeApplied) {
        List<ManualRerunAvailableAction> availableActions = new ArrayList<>();

        if (retryEligibilityPolicy.evaluate(execution).isRetryAllowed()) {
            availableActions.add(ManualRerunAvailableAction.RETRY);
        }

        if (evaluateAcknowledge(execution, acknowledgeApplied).isActionAllowed()) {
            availableActions.add(ManualRerunAvailableAction.ACKNOWLEDGE);
        }

        return List.copyOf(availableActions);
    }

    public ManualRerunControlActionEligibility evaluateAcknowledge(WebhookExecution execution, boolean acknowledgeApplied) {
        if (execution.getExecutionStartType() != ExecutionStartType.MANUAL_RERUN) {
            return ManualRerunControlActionEligibility.rejected(
                    execution.getFailureDisposition(),
                    "수동 재실행으로 생성된 실행만 확인 완료 처리할 수 있습니다."
            );
        }

        if (execution.getStatus() != WebhookExecutionStatus.FAILED) {
            return ManualRerunControlActionEligibility.rejected(
                    execution.getFailureDisposition(),
                    "실패한 수동 재실행만 확인 완료 처리할 수 있습니다."
            );
        }

        if (execution.getFailureDisposition() != FailureDisposition.MANUAL_ACTION_REQUIRED) {
            return ManualRerunControlActionEligibility.rejected(
                    execution.getFailureDisposition(),
                    "수동 조치가 필요한 실행만 확인 완료 처리할 수 있습니다."
            );
        }

        if (acknowledgeApplied) {
            return ManualRerunControlActionEligibility.rejected(
                    execution.getFailureDisposition(),
                    "이미 확인 완료 처리된 실행입니다."
            );
        }

        return ManualRerunControlActionEligibility.allowed();
    }
}
