package com.agilerunner.api.controller.review.request;

import com.agilerunner.api.service.review.request.ManualRerunServiceRequest;
import com.agilerunner.domain.executioncontrol.ExecutionControlMode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ManualRerunRequestTest {

    @DisplayName("수동 재실행 요청을 service request로 변환하면 입력값을 그대로 유지한다.")
    @Test
    void toServiceRequest_keepsInputValues() {
        // given
        ManualRerunRequest request = ManualRerunRequest.of(
                "owner/repo",
                12,
                100L,
                ExecutionControlMode.DRY_RUN
        );

        // when
        ManualRerunServiceRequest serviceRequest = request.toServiceRequest();

        // then
        assertThat(serviceRequest.getRepositoryName()).isEqualTo("owner/repo");
        assertThat(serviceRequest.getPullRequestNumber()).isEqualTo(12);
        assertThat(serviceRequest.getInstallationId()).isEqualTo(100L);
        assertThat(serviceRequest.getExecutionControlMode()).isEqualTo(ExecutionControlMode.DRY_RUN);
    }
}
