package com.agilerunner.api.controller.review.request;

import com.agilerunner.api.service.review.request.ManualRerunControlActionServiceRequest;
import com.agilerunner.domain.review.ManualRerunControlAction;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ManualRerunControlActionRequestTest {

    @DisplayName("관리자 제어 액션 요청을 service request로 변환하면 executionKey, action, note를 그대로 유지한다.")
    @Test
    void toServiceRequest_keepsInputValues() {
        // given
        ManualRerunControlActionRequest request = ManualRerunControlActionRequest.of(
                ManualRerunControlAction.ACKNOWLEDGE,
                "운영자 확인 완료"
        );

        // when
        ManualRerunControlActionServiceRequest serviceRequest =
                request.toServiceRequest("EXECUTION:MANUAL_RERUN:action-1");

        // then
        assertThat(serviceRequest.getExecutionKey()).isEqualTo("EXECUTION:MANUAL_RERUN:action-1");
        assertThat(serviceRequest.getAction()).isEqualTo(ManualRerunControlAction.ACKNOWLEDGE);
        assertThat(serviceRequest.getNote()).isEqualTo("운영자 확인 완료");
    }

    @DisplayName("관리자 제어 액션 요청은 UNACKNOWLEDGE 입력도 service request로 전달한다.")
    @Test
    void toServiceRequest_keepsUnacknowledgeInputValues() {
        // given
        ManualRerunControlActionRequest request = ManualRerunControlActionRequest.of(
                ManualRerunControlAction.UNACKNOWLEDGE,
                "운영자 확인 취소"
        );

        // when
        ManualRerunControlActionServiceRequest serviceRequest =
                request.toServiceRequest("EXECUTION:MANUAL_RERUN:action-2");

        // then
        assertThat(serviceRequest.getExecutionKey()).isEqualTo("EXECUTION:MANUAL_RERUN:action-2");
        assertThat(serviceRequest.getAction()).isEqualTo(ManualRerunControlAction.UNACKNOWLEDGE);
        assertThat(serviceRequest.getNote()).isEqualTo("운영자 확인 취소");
    }
}
