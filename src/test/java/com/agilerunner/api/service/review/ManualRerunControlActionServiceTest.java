package com.agilerunner.api.service.review;

import com.agilerunner.api.service.review.request.ManualRerunControlActionServiceRequest;
import com.agilerunner.api.service.review.response.ManualRerunControlActionServiceResponse;
import com.agilerunner.domain.review.ManualRerunControlAction;
import com.agilerunner.domain.review.ManualRerunControlActionStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ManualRerunControlActionServiceTest {

    @DisplayName("ACKNOWLEDGE 요청은 최소 성공 응답 계약을 반환한다.")
    @Test
    void execute_returnsAppliedResponseContract() {
        // given
        ManualRerunControlActionService service = new ManualRerunControlActionService();
        ManualRerunControlActionServiceRequest request = ManualRerunControlActionServiceRequest.of(
                "EXECUTION:MANUAL_RERUN:action-1",
                ManualRerunControlAction.ACKNOWLEDGE,
                "운영자 확인 완료"
        );

        // when
        ManualRerunControlActionServiceResponse response = service.execute(request);

        // then
        assertThat(response.getExecutionKey()).isEqualTo("EXECUTION:MANUAL_RERUN:action-1");
        assertThat(response.getAction()).isEqualTo(ManualRerunControlAction.ACKNOWLEDGE);
        assertThat(response.getActionStatus()).isEqualTo(ManualRerunControlActionStatus.APPLIED);
        assertThat(response.getAvailableActions()).isEmpty();
        assertThat(response.getNote()).isEqualTo("운영자 확인 완료");
    }
}
