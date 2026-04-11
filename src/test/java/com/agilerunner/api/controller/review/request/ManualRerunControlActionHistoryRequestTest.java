package com.agilerunner.api.controller.review.request;

import com.agilerunner.api.service.review.request.ManualRerunControlActionHistoryServiceRequest;
import com.agilerunner.domain.review.ManualRerunControlAction;
import com.agilerunner.domain.review.ManualRerunControlActionHistorySortDirection;
import com.agilerunner.domain.review.ManualRerunControlActionStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class ManualRerunControlActionHistoryRequestTest {

    @DisplayName("관리자 액션 이력 조회 요청을 service request로 변환하면 정렬과 페이지 입력값을 그대로 유지한다.")
    @Test
    void toServiceRequest_keepsOrderAndPageInputs() {
        // given
        ManualRerunControlActionHistoryRequest request = new ManualRerunControlActionHistoryRequest();
        LocalDateTime appliedAtFrom = LocalDateTime.of(2026, 4, 12, 9, 0);
        LocalDateTime appliedAtTo = LocalDateTime.of(2026, 4, 12, 10, 0);
        LocalDateTime cursorAppliedAt = LocalDateTime.of(2026, 4, 12, 9, 30);
        request.setAction(ManualRerunControlAction.ACKNOWLEDGE);
        request.setActionStatus(ManualRerunControlActionStatus.APPLIED);
        request.setAppliedAtFrom(appliedAtFrom);
        request.setAppliedAtTo(appliedAtTo);
        request.setSortDirection(ManualRerunControlActionHistorySortDirection.ASC);
        request.setPageSize(20);
        request.setCursorAppliedAt(cursorAppliedAt);

        // when
        ManualRerunControlActionHistoryServiceRequest serviceRequest = request.toServiceRequest("EXECUTION:MANUAL_RERUN:test-1");

        // then
        assertThat(serviceRequest.getExecutionKey()).isEqualTo("EXECUTION:MANUAL_RERUN:test-1");
        assertThat(serviceRequest.getAction()).isEqualTo(ManualRerunControlAction.ACKNOWLEDGE);
        assertThat(serviceRequest.getActionStatus()).isEqualTo(ManualRerunControlActionStatus.APPLIED);
        assertThat(serviceRequest.getAppliedAtFrom()).isEqualTo(appliedAtFrom);
        assertThat(serviceRequest.getAppliedAtTo()).isEqualTo(appliedAtTo);
        assertThat(serviceRequest.getSortDirection()).isEqualTo(ManualRerunControlActionHistorySortDirection.ASC);
        assertThat(serviceRequest.getPageSize()).isEqualTo(20);
        assertThat(serviceRequest.getCursorAppliedAt()).isEqualTo(cursorAppliedAt);
    }

    @DisplayName("관리자 액션 이력 조회 요청에서 정렬 방향이 비어 있고 페이지 기준만 있으면 DESC를 기본값으로 사용한다.")
    @Test
    void toServiceRequest_defaultsDescendingWhenOrderIsMissing() {
        // given
        ManualRerunControlActionHistoryRequest request = new ManualRerunControlActionHistoryRequest();
        LocalDateTime cursorAppliedAt = LocalDateTime.of(2026, 4, 12, 9, 30);
        request.setPageSize(10);
        request.setCursorAppliedAt(cursorAppliedAt);

        // when
        ManualRerunControlActionHistoryServiceRequest serviceRequest = request.toServiceRequest("EXECUTION:MANUAL_RERUN:test-2");

        // then
        assertThat(serviceRequest.getSortDirection()).isEqualTo(ManualRerunControlActionHistorySortDirection.DESC);
        assertThat(serviceRequest.getPageSize()).isEqualTo(10);
        assertThat(serviceRequest.getCursorAppliedAt()).isEqualTo(cursorAppliedAt);
    }

    @DisplayName("관리자 액션 이력 조회 요청에서 정렬과 페이지 입력이 모두 없으면 기존 전체 timeline 의미를 유지한다.")
    @Test
    void toServiceRequest_keepsPageInputsNullWhenMissing() {
        // given
        ManualRerunControlActionHistoryRequest request = new ManualRerunControlActionHistoryRequest();

        // when
        ManualRerunControlActionHistoryServiceRequest serviceRequest = request.toServiceRequest("EXECUTION:MANUAL_RERUN:test-3");

        // then
        assertThat(serviceRequest.getSortDirection()).isNull();
        assertThat(serviceRequest.getPageSize()).isNull();
        assertThat(serviceRequest.getCursorAppliedAt()).isNull();
    }
}
