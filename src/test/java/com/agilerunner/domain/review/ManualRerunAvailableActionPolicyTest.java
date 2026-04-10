package com.agilerunner.domain.review;

import com.agilerunner.domain.agentruntime.ExecutionStartType;
import com.agilerunner.domain.agentruntime.WebhookExecution;
import com.agilerunner.domain.agentruntime.WebhookExecutionStatus;
import com.agilerunner.domain.exception.ErrorCode;
import com.agilerunner.domain.exception.FailureDisposition;
import com.agilerunner.domain.executioncontrol.ExecutionControlMode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class ManualRerunAvailableActionPolicyTest {

    private final ManualRerunAvailableActionPolicy policy = new ManualRerunAvailableActionPolicy();

    @DisplayName("수동 조치가 필요한 실행은 마지막 applied action이 없으면 ACKNOWLEDGE를 반환한다.")
    @Test
    void resolve_returnsAcknowledgeWhenNoAppliedActionExists() {
        WebhookExecution execution = manualActionRequiredExecution("EXECUTION:MANUAL_RERUN:policy-1");

        assertThat(policy.resolve(execution, null))
                .containsExactly(ManualRerunAvailableAction.ACKNOWLEDGE);
    }

    @DisplayName("마지막 applied action이 ACKNOWLEDGE면 UNACKNOWLEDGE를 반환한다.")
    @Test
    void resolve_returnsUnacknowledgeWhenAcknowledgeWasApplied() {
        WebhookExecution execution = manualActionRequiredExecution("EXECUTION:MANUAL_RERUN:policy-2");

        assertThat(policy.resolve(execution, ManualRerunControlAction.ACKNOWLEDGE))
                .containsExactly(ManualRerunAvailableAction.UNACKNOWLEDGE);
    }

    @DisplayName("UNACKNOWLEDGE 평가는 마지막 applied action이 ACKNOWLEDGE일 때만 허용한다.")
    @Test
    void evaluate_allowsUnacknowledgeOnlyAfterAcknowledge() {
        WebhookExecution execution = manualActionRequiredExecution("EXECUTION:MANUAL_RERUN:policy-3");

        ManualRerunControlActionEligibility allowed = policy.evaluate(
                execution,
                ManualRerunControlAction.UNACKNOWLEDGE,
                ManualRerunControlAction.ACKNOWLEDGE
        );
        ManualRerunControlActionEligibility rejected = policy.evaluate(
                execution,
                ManualRerunControlAction.UNACKNOWLEDGE,
                null
        );

        assertThat(allowed.isActionAllowed()).isTrue();
        assertThat(rejected.isActionAllowed()).isFalse();
        assertThat(rejected.getMessage()).isEqualTo("확인 완료 처리된 실행만 확인 취소할 수 있습니다.");
    }

    private WebhookExecution manualActionRequiredExecution(String executionKey) {
        return WebhookExecution.start(
                executionKey,
                "PR_REVIEW:owner/repo#10",
                "MANUAL_RERUN_DELIVERY:" + executionKey,
                "owner/repo",
                10,
                "PULL_REQUEST",
                "manual_rerun",
                LocalDateTime.of(2026, 4, 10, 10, 0)
        ).withExecutionStartType(ExecutionStartType.MANUAL_RERUN)
                .withExecutionControl(ExecutionControlMode.DRY_RUN, false, null)
                .complete(
                        WebhookExecutionStatus.FAILED,
                        "manual action required",
                        ErrorCode.GITHUB_APP_CONFIGURATION_MISSING,
                        FailureDisposition.MANUAL_ACTION_REQUIRED,
                        LocalDateTime.of(2026, 4, 10, 10, 1)
                );
    }
}
