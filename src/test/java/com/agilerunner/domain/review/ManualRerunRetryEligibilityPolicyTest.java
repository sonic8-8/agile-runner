package com.agilerunner.domain.review;

import com.agilerunner.domain.agentruntime.ExecutionStartType;
import com.agilerunner.domain.agentruntime.WebhookExecution;
import com.agilerunner.domain.agentruntime.WebhookExecutionStatus;
import com.agilerunner.domain.exception.FailureDisposition;
import com.agilerunner.domain.exception.ErrorCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class ManualRerunRetryEligibilityPolicyTest {

    @DisplayName("manual rerun 실패와 RETRYABLE 조합은 재시도를 허용한다.")
    @Test
    void evaluate_allowsRetryableManualRerunFailure() {
        // given
        ManualRerunRetryEligibilityPolicy policy = new ManualRerunRetryEligibilityPolicy();
        WebhookExecution sourceExecution = manualRerunExecution(WebhookExecutionStatus.FAILED, FailureDisposition.RETRYABLE);

        // when
        ManualRerunRetryEligibility eligibility = policy.evaluate(sourceExecution);

        // then
        assertThat(eligibility.isRetryAllowed()).isTrue();
        assertThat(eligibility.getFailureDisposition()).isEqualTo(FailureDisposition.RETRYABLE);
        assertThat(eligibility.getMessage()).isNull();
    }

    @DisplayName("manual rerun 실패와 MANUAL_ACTION_REQUIRED 조합은 재시도를 거부한다.")
    @Test
    void evaluate_rejectsManualActionRequiredFailure() {
        // given
        ManualRerunRetryEligibilityPolicy policy = new ManualRerunRetryEligibilityPolicy();
        WebhookExecution sourceExecution = manualRerunExecution(
                WebhookExecutionStatus.FAILED,
                FailureDisposition.MANUAL_ACTION_REQUIRED
        );

        // when
        ManualRerunRetryEligibility eligibility = policy.evaluate(sourceExecution);

        // then
        assertThat(eligibility.isRetryAllowed()).isFalse();
        assertThat(eligibility.getFailureDisposition()).isEqualTo(FailureDisposition.MANUAL_ACTION_REQUIRED);
        assertThat(eligibility.getMessage()).isEqualTo("수동 조치가 필요한 실행은 바로 재시도할 수 없습니다.");
    }

    @DisplayName("manual rerun 실패와 NON_RETRYABLE 조합은 재시도를 거부한다.")
    @Test
    void evaluate_rejectsNonRetryableFailure() {
        // given
        ManualRerunRetryEligibilityPolicy policy = new ManualRerunRetryEligibilityPolicy();
        WebhookExecution sourceExecution = manualRerunExecution(
                WebhookExecutionStatus.FAILED,
                FailureDisposition.NON_RETRYABLE
        );

        // when
        ManualRerunRetryEligibility eligibility = policy.evaluate(sourceExecution);

        // then
        assertThat(eligibility.isRetryAllowed()).isFalse();
        assertThat(eligibility.getFailureDisposition()).isEqualTo(FailureDisposition.NON_RETRYABLE);
        assertThat(eligibility.getMessage()).isEqualTo("재시도 불가 실행입니다.");
    }

    @DisplayName("성공한 manual rerun은 재시도를 거부한다.")
    @Test
    void evaluate_rejectsSucceededExecution() {
        // given
        ManualRerunRetryEligibilityPolicy policy = new ManualRerunRetryEligibilityPolicy();
        WebhookExecution sourceExecution = manualRerunExecution(WebhookExecutionStatus.SUCCEEDED, null);

        // when
        ManualRerunRetryEligibility eligibility = policy.evaluate(sourceExecution);

        // then
        assertThat(eligibility.isRetryAllowed()).isFalse();
        assertThat(eligibility.getFailureDisposition()).isNull();
        assertThat(eligibility.getMessage()).isEqualTo("실패한 재실행만 재시도할 수 있습니다.");
    }

    @DisplayName("manual rerun이 아닌 execution은 재시도를 거부한다.")
    @Test
    void evaluate_rejectsNonManualRerunExecution() {
        // given
        ManualRerunRetryEligibilityPolicy policy = new ManualRerunRetryEligibilityPolicy();
        WebhookExecution sourceExecution = WebhookExecution.start(
                "EXECUTION:delivery-55",
                "PR_REVIEW:owner/repo#12",
                "delivery-55",
                "owner/repo",
                12,
                "PULL_REQUEST",
                "opened",
                LocalDateTime.of(2026, 4, 9, 15, 0)
        ).withExecutionStartType(ExecutionStartType.WEBHOOK)
                .complete(
                        WebhookExecutionStatus.FAILED,
                        "GitHub comment failed",
                        ErrorCode.GITHUB_COMMENT_POST_FAILED,
                        FailureDisposition.RETRYABLE,
                        LocalDateTime.of(2026, 4, 9, 15, 1)
                );

        // when
        ManualRerunRetryEligibility eligibility = policy.evaluate(sourceExecution);

        // then
        assertThat(eligibility.isRetryAllowed()).isFalse();
        assertThat(eligibility.getFailureDisposition()).isEqualTo(FailureDisposition.RETRYABLE);
        assertThat(eligibility.getMessage()).isEqualTo("수동 재실행으로 생성된 실행만 재시도할 수 있습니다.");
    }

    private WebhookExecution manualRerunExecution(WebhookExecutionStatus status,
                                                  FailureDisposition failureDisposition) {
        return WebhookExecution.start(
                "EXECUTION:MANUAL_RERUN:policy-1",
                "PR_REVIEW:owner/repo#12",
                "MANUAL_RERUN_DELIVERY:policy-1",
                "owner/repo",
                12,
                "PULL_REQUEST",
                "manual_rerun",
                LocalDateTime.of(2026, 4, 9, 14, 0)
        ).withExecutionStartType(ExecutionStartType.MANUAL_RERUN)
                .complete(
                        status,
                        status == WebhookExecutionStatus.FAILED ? "failure" : null,
                        status == WebhookExecutionStatus.FAILED ? ErrorCode.GITHUB_COMMENT_POST_FAILED : null,
                        failureDisposition,
                        LocalDateTime.of(2026, 4, 9, 14, 1)
                );
    }
}
