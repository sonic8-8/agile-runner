package com.agilerunner.api.controller.review.response;

import com.agilerunner.api.service.review.response.ManualRerunServiceResponse;
import com.agilerunner.domain.exception.ErrorCode;
import com.agilerunner.domain.exception.FailureDisposition;
import com.agilerunner.domain.executioncontrol.ExecutionControlMode;
import com.agilerunner.domain.review.RerunExecutionStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ManualRerunResponseTest {

    @DisplayName("service response를 controller response로 변환하면 확장된 rerun 응답 필드를 그대로 유지한다.")
    @Test
    void from_keepsExpandedResponseFields() {
        // given
        ManualRerunServiceResponse serviceResponse = ManualRerunServiceResponse.of(
                "EXECUTION:MANUAL_RERUN:owner/repo#12:1",
                ExecutionControlMode.NORMAL,
                false,
                RerunExecutionStatus.FAILED,
                ErrorCode.GITHUB_COMMENT_POST_FAILED,
                FailureDisposition.RETRYABLE
        );

        // when
        ManualRerunResponse response = ManualRerunResponse.from(serviceResponse);

        // then
        assertThat(response.getExecutionKey()).isEqualTo("EXECUTION:MANUAL_RERUN:owner/repo#12:1");
        assertThat(response.getExecutionControlMode()).isEqualTo(ExecutionControlMode.NORMAL);
        assertThat(response.isWritePerformed()).isFalse();
        assertThat(response.getExecutionStatus()).isEqualTo(RerunExecutionStatus.FAILED);
        assertThat(response.getErrorCode()).isEqualTo(ErrorCode.GITHUB_COMMENT_POST_FAILED);
        assertThat(response.getFailureDisposition()).isEqualTo(FailureDisposition.RETRYABLE);
    }
}
