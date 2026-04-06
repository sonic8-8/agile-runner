package com.agilerunner.domain.exception;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class FailureDispositionPolicyTest {

    @DisplayName("핵심 ErrorCode를 기대 대응 기준으로 일관되게 분류한다.")
    @Test
    void classify_returnsExpectedDispositionForTargetErrorCodes() {
        // given
        FailureDispositionPolicy policy = new FailureDispositionPolicy();

        // when & then
        assertThat(policy.classify(ErrorCode.GITHUB_INSTALLATION_ID_MISSING))
                .isEqualTo(FailureDisposition.MANUAL_ACTION_REQUIRED);
        assertThat(policy.classify(ErrorCode.OPENAI_CLIENT_MISSING))
                .isEqualTo(FailureDisposition.MANUAL_ACTION_REQUIRED);
        assertThat(policy.classify(ErrorCode.OPENAI_REVIEW_FAILED))
                .isEqualTo(FailureDisposition.RETRYABLE);
        assertThat(policy.classify(ErrorCode.GITHUB_APP_CONFIGURATION_MISSING))
                .isEqualTo(FailureDisposition.MANUAL_ACTION_REQUIRED);
        assertThat(policy.classify(ErrorCode.GITHUB_COMMENT_PREPARATION_FAILED))
                .isEqualTo(FailureDisposition.NON_RETRYABLE);
        assertThat(policy.classify(ErrorCode.GITHUB_COMMENT_POST_FAILED))
                .isEqualTo(FailureDisposition.RETRYABLE);
    }

    @DisplayName("AgileRunnerException을 직접 받아도 같은 대응 기준으로 분류한다.")
    @Test
    void classify_returnsExpectedDispositionForAgileRunnerException() {
        // given
        FailureDispositionPolicy policy = new FailureDispositionPolicy();
        AgileRunnerException exception = new AgileRunnerException(
                ErrorCode.GITHUB_COMMENT_POST_FAILED,
                "GitHub 코멘트 등록 실패"
        );

        // when
        FailureDisposition disposition = policy.classify(exception);

        // then
        assertThat(disposition).isEqualTo(FailureDisposition.RETRYABLE);
    }
}
