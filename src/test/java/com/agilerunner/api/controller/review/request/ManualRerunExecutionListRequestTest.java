package com.agilerunner.api.controller.review.request;

import com.agilerunner.api.service.review.request.ManualRerunExecutionListServiceRequest;
import com.agilerunner.domain.agentruntime.ExecutionStartType;
import com.agilerunner.domain.agentruntime.WebhookExecutionStatus;
import com.agilerunner.domain.exception.FailureDisposition;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ManualRerunExecutionListRequestTest {

    @DisplayName("목록 조회 요청을 service request로 변환하면 필터 입력값을 그대로 유지한다.")
    @Test
    void toServiceRequest_keepsInputValues() {
        // given
        ManualRerunExecutionListRequest request = new ManualRerunExecutionListRequest();
        request.setRepositoryName("owner/repo");
        request.setPullRequestNumber(12);
        request.setExecutionStartType(ExecutionStartType.MANUAL_RERUN);
        request.setExecutionStatus(WebhookExecutionStatus.FAILED);
        request.setFailureDisposition(FailureDisposition.RETRYABLE);

        // when
        ManualRerunExecutionListServiceRequest serviceRequest = request.toServiceRequest();

        // then
        assertThat(serviceRequest.getRepositoryName()).isEqualTo("owner/repo");
        assertThat(serviceRequest.getPullRequestNumber()).isEqualTo(12);
        assertThat(serviceRequest.getExecutionStartType()).isEqualTo(ExecutionStartType.MANUAL_RERUN);
        assertThat(serviceRequest.getExecutionStatus()).isEqualTo(WebhookExecutionStatus.FAILED);
        assertThat(serviceRequest.getFailureDisposition()).isEqualTo(FailureDisposition.RETRYABLE);
    }

    @DisplayName("목록 조회 요청의 필터가 비어 있으면 service request에서는 모두 null로 해석한다.")
    @Test
    void toServiceRequest_keepsMissingFiltersAsNull() {
        // given
        ManualRerunExecutionListRequest request = new ManualRerunExecutionListRequest();

        // when
        ManualRerunExecutionListServiceRequest serviceRequest = request.toServiceRequest();

        // then
        assertThat(serviceRequest.getRepositoryName()).isNull();
        assertThat(serviceRequest.getPullRequestNumber()).isNull();
        assertThat(serviceRequest.getExecutionStartType()).isNull();
        assertThat(serviceRequest.getExecutionStatus()).isNull();
        assertThat(serviceRequest.getFailureDisposition()).isNull();
    }
}
