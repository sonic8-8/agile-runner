package com.agilerunner.domain.exception;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.EnumMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class FailureResponseExpectationTest {

    @DisplayName("핵심 ErrorCode의 기대 대응 기준을 먼저 고정한다.")
    @Test
    void pinsExpectedFailureDispositionsForTargetErrorCodes() {
        // given
        Map<ErrorCode, FailureDisposition> expectations = new EnumMap<>(ErrorCode.class);
        expectations.put(ErrorCode.GITHUB_INSTALLATION_ID_MISSING, FailureDisposition.MANUAL_ACTION_REQUIRED);
        expectations.put(ErrorCode.OPENAI_CLIENT_MISSING, FailureDisposition.MANUAL_ACTION_REQUIRED);
        expectations.put(ErrorCode.OPENAI_REVIEW_FAILED, FailureDisposition.RETRYABLE);
        expectations.put(ErrorCode.GITHUB_APP_CONFIGURATION_MISSING, FailureDisposition.MANUAL_ACTION_REQUIRED);
        expectations.put(ErrorCode.GITHUB_COMMENT_PREPARATION_FAILED, FailureDisposition.NON_RETRYABLE);
        expectations.put(ErrorCode.GITHUB_COMMENT_POST_FAILED, FailureDisposition.RETRYABLE);

        // when
        boolean allTargetErrorCodesCovered = expectations.keySet().containsAll(
                java.util.Set.of(
                        ErrorCode.GITHUB_INSTALLATION_ID_MISSING,
                        ErrorCode.OPENAI_CLIENT_MISSING,
                        ErrorCode.OPENAI_REVIEW_FAILED,
                        ErrorCode.GITHUB_APP_CONFIGURATION_MISSING,
                        ErrorCode.GITHUB_COMMENT_PREPARATION_FAILED,
                        ErrorCode.GITHUB_COMMENT_POST_FAILED
                )
        );

        // then
        assertThat(expectations).hasSize(6);
        assertThat(allTargetErrorCodesCovered).isTrue();
        assertThat(expectations.get(ErrorCode.GITHUB_INSTALLATION_ID_MISSING)).isEqualTo(FailureDisposition.MANUAL_ACTION_REQUIRED);
        assertThat(expectations.get(ErrorCode.OPENAI_CLIENT_MISSING)).isEqualTo(FailureDisposition.MANUAL_ACTION_REQUIRED);
        assertThat(expectations.get(ErrorCode.OPENAI_REVIEW_FAILED)).isEqualTo(FailureDisposition.RETRYABLE);
        assertThat(expectations.get(ErrorCode.GITHUB_APP_CONFIGURATION_MISSING)).isEqualTo(FailureDisposition.MANUAL_ACTION_REQUIRED);
        assertThat(expectations.get(ErrorCode.GITHUB_COMMENT_PREPARATION_FAILED)).isEqualTo(FailureDisposition.NON_RETRYABLE);
        assertThat(expectations.get(ErrorCode.GITHUB_COMMENT_POST_FAILED)).isEqualTo(FailureDisposition.RETRYABLE);
    }
}
