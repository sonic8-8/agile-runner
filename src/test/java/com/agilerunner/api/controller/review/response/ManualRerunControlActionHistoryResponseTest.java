package com.agilerunner.api.controller.review.response;

import com.agilerunner.api.service.review.response.ManualRerunControlActionHistoryServiceResponse;
import com.agilerunner.domain.review.ManualRerunAvailableAction;
import com.agilerunner.domain.review.ManualRerunControlAction;
import com.agilerunner.domain.review.ManualRerunControlActionStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ManualRerunControlActionHistoryResponseTest {

    @DisplayName("service response를 controller response로 변환하면 관리자 액션 이력 필드를 그대로 유지한다.")
    @Test
    void from_keepsHistoryFields() {
        // given
        LocalDateTime appliedAt = LocalDateTime.of(2026, 4, 10, 11, 30);
        ManualRerunControlActionHistoryServiceResponse serviceResponse = ManualRerunControlActionHistoryServiceResponse.of(
                "EXECUTION:MANUAL_RERUN:history-1",
                ManualRerunControlActionHistoryServiceResponse.CurrentActionState.of(
                        ManualRerunControlAction.UNACKNOWLEDGE,
                        ManualRerunControlActionStatus.APPLIED,
                        LocalDateTime.of(2026, 4, 10, 11, 35),
                        List.of(ManualRerunAvailableAction.ACKNOWLEDGE)
                ),
                List.of(
                        ManualRerunControlActionHistoryServiceResponse.ActionHistorySummary.of(
                                ManualRerunControlAction.ACKNOWLEDGE,
                                ManualRerunControlActionStatus.APPLIED,
                                "운영자 확인 완료",
                                appliedAt
                        )
                )
        );

        // when
        ManualRerunControlActionHistoryResponse response = ManualRerunControlActionHistoryResponse.from(serviceResponse);

        // then
        assertThat(response.getExecutionKey()).isEqualTo("EXECUTION:MANUAL_RERUN:history-1");
        assertThat(response.getCurrentActionState().getLatestAction()).isEqualTo(ManualRerunControlAction.UNACKNOWLEDGE);
        assertThat(response.getCurrentActionState().getLatestActionStatus()).isEqualTo(ManualRerunControlActionStatus.APPLIED);
        assertThat(response.getCurrentActionState().getLatestActionAppliedAt()).isEqualTo(LocalDateTime.of(2026, 4, 10, 11, 35));
        assertThat(response.getCurrentActionState().getAvailableActions()).containsExactly(ManualRerunAvailableAction.ACKNOWLEDGE);
        assertThat(response.getActions()).hasSize(1);
        assertThat(response.getActions().get(0).getAction()).isEqualTo(ManualRerunControlAction.ACKNOWLEDGE);
        assertThat(response.getActions().get(0).getActionStatus()).isEqualTo(ManualRerunControlActionStatus.APPLIED);
        assertThat(response.getActions().get(0).getNote()).isEqualTo("운영자 확인 완료");
        assertThat(response.getActions().get(0).getAppliedAt()).isEqualTo(appliedAt);
    }
}
