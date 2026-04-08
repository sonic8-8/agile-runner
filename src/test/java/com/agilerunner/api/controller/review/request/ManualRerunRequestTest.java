package com.agilerunner.api.controller.review.request;

import com.agilerunner.api.service.review.request.ManualRerunServiceRequest;
import com.agilerunner.domain.executioncontrol.ExecutionControlMode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ManualRerunRequestTest {

    @DisplayName("수동 재실행 요청을 service request로 변환하면 선택 파일 경로를 포함한 입력값을 그대로 유지한다.")
    @Test
    void toServiceRequest_keepsInputValues() {
        // given
        ManualRerunRequest request = ManualRerunRequest.of(
                "owner/repo",
                12,
                100L,
                ExecutionControlMode.DRY_RUN,
                List.of("src/main/App.java", "src/test/AppTest.java")
        );

        // when
        ManualRerunServiceRequest serviceRequest = request.toServiceRequest();

        // then
        assertThat(serviceRequest.getRepositoryName()).isEqualTo("owner/repo");
        assertThat(serviceRequest.getPullRequestNumber()).isEqualTo(12);
        assertThat(serviceRequest.getInstallationId()).isEqualTo(100L);
        assertThat(serviceRequest.getExecutionControlMode()).isEqualTo(ExecutionControlMode.DRY_RUN);
        assertThat(serviceRequest.getSelectedPaths()).containsExactly(
                "src/main/App.java",
                "src/test/AppTest.java"
        );
    }

    @DisplayName("수동 재실행 요청의 선택 파일 경로가 없으면 service request에서는 빈 목록으로 해석한다.")
    @Test
    void toServiceRequest_resolvesMissingSelectedPathsAsEmptyList() {
        // given
        ManualRerunRequest request = ManualRerunRequest.of(
                "owner/repo",
                12,
                100L,
                ExecutionControlMode.NORMAL,
                null
        );

        // when
        ManualRerunServiceRequest serviceRequest = request.toServiceRequest();

        // then
        assertThat(serviceRequest.getSelectedPaths()).isEmpty();
    }
}
