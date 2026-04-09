package com.agilerunner.api.controller.review.response;

import com.agilerunner.api.service.review.response.ManualRerunControlActionServiceResponse;
import com.agilerunner.domain.review.ManualRerunControlAction;
import com.agilerunner.domain.review.ManualRerunControlActionStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ManualRerunControlActionResponseTest {

    @DisplayName("service response를 controller response로 변환하면 관리자 제어 액션 응답 필드를 그대로 유지한다.")
    @Test
    void from_keepsControlActionResponseFields() {
        // given
        ManualRerunControlActionServiceResponse serviceResponse = ManualRerunControlActionServiceResponse.of(
                "EXECUTION:MANUAL_RERUN:action-1",
                ManualRerunControlAction.ACKNOWLEDGE,
                ManualRerunControlActionStatus.APPLIED,
                List.of(),
                "운영자 확인 완료"
        );

        // when
        ManualRerunControlActionResponse response = ManualRerunControlActionResponse.from(serviceResponse);

        // then
        assertThat(response.getExecutionKey()).isEqualTo("EXECUTION:MANUAL_RERUN:action-1");
        assertThat(response.getAction()).isEqualTo(ManualRerunControlAction.ACKNOWLEDGE);
        assertThat(response.getActionStatus()).isEqualTo(ManualRerunControlActionStatus.APPLIED);
        assertThat(response.getAvailableActions()).isEmpty();
        assertThat(response.getNote()).isEqualTo("운영자 확인 완료");
    }
}
