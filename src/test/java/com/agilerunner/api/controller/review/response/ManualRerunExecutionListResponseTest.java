package com.agilerunner.api.controller.review.response;

import com.agilerunner.api.service.review.response.ManualRerunExecutionListServiceResponse;
import com.agilerunner.domain.agentruntime.ExecutionStartType;
import com.agilerunner.domain.exception.ErrorCode;
import com.agilerunner.domain.exception.FailureDisposition;
import com.agilerunner.domain.executioncontrol.ExecutionControlMode;
import com.agilerunner.domain.review.ManualRerunAvailableAction;
import com.agilerunner.domain.review.ManualRerunControlAction;
import com.agilerunner.domain.review.ManualRerunControlActionStatus;
import com.agilerunner.domain.review.RerunExecutionStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ManualRerunExecutionListResponseTest {

    @DisplayName("service response를 controller response로 변환하면 목록 현재 조치 상태 요약 필드를 그대로 유지한다.")
    @Test
    void from_keepsCurrentActionSummaryFields() {
        // given
        ManualRerunExecutionListServiceResponse serviceResponse = ManualRerunExecutionListServiceResponse.of(
                List.of(
                        ManualRerunExecutionListServiceResponse.ExecutionSummary.of(
                                "EXECUTION:MANUAL_RERUN:list-1",
                                "EXECUTION:MANUAL_RERUN:source-1",
                                ExecutionStartType.MANUAL_RERUN,
                                RerunExecutionStatus.FAILED,
                                ExecutionControlMode.DRY_RUN,
                                false,
                                ErrorCode.GITHUB_APP_CONFIGURATION_MISSING,
                                FailureDisposition.MANUAL_ACTION_REQUIRED,
                                ManualRerunControlAction.ACKNOWLEDGE,
                                ManualRerunControlActionStatus.APPLIED,
                                LocalDateTime.of(2026, 4, 12, 14, 30),
                                true,
                                List.of(ManualRerunAvailableAction.UNACKNOWLEDGE)
                        )
                )
        );

        // when
        ManualRerunExecutionListResponse response = ManualRerunExecutionListResponse.from(serviceResponse);

        // then
        assertThat(response.getExecutions()).hasSize(1);
        ManualRerunExecutionListResponse.ExecutionSummary summary = response.getExecutions().getFirst();
        assertThat(summary.getExecutionKey()).isEqualTo("EXECUTION:MANUAL_RERUN:list-1");
        assertThat(summary.getLatestAction()).isEqualTo(ManualRerunControlAction.ACKNOWLEDGE);
        assertThat(summary.getLatestActionStatus()).isEqualTo(ManualRerunControlActionStatus.APPLIED);
        assertThat(summary.getLatestActionAppliedAt()).isEqualTo(LocalDateTime.of(2026, 4, 12, 14, 30));
        assertThat(summary.isHistoryAvailable()).isTrue();
        assertThat(summary.getAvailableActions()).containsExactly(ManualRerunAvailableAction.UNACKNOWLEDGE);
    }
}
