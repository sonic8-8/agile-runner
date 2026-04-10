package com.agilerunner.api.service.review;

import com.agilerunner.api.service.review.request.ManualRerunControlActionHistoryServiceRequest;
import com.agilerunner.api.service.review.response.ManualRerunControlActionHistoryServiceResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ManualRerunControlActionHistoryServiceTest {

    @DisplayName("관리자 액션 이력 조회 서비스는 최소 응답으로 executionKey와 비어 있는 actions를 반환한다.")
    @Test
    void find_returnsEmptyHistoryResponse() {
        // given
        ManualRerunControlActionHistoryService service = new ManualRerunControlActionHistoryService();

        // when
        ManualRerunControlActionHistoryServiceResponse response = service.find(
                ManualRerunControlActionHistoryServiceRequest.of("EXECUTION:MANUAL_RERUN:history-1")
        );

        // then
        assertThat(response.getExecutionKey()).isEqualTo("EXECUTION:MANUAL_RERUN:history-1");
        assertThat(response.getActions()).isEmpty();
    }
}
