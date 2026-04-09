package com.agilerunner.api.service.review;

import com.agilerunner.api.service.review.request.ManualRerunQueryServiceRequest;
import com.agilerunner.api.service.review.response.ManualRerunQueryServiceResponse;
import com.agilerunner.client.agentruntime.AgentRuntimeRepository;
import com.agilerunner.domain.agentruntime.ExecutionStartType;
import com.agilerunner.domain.agentruntime.WebhookExecution;
import com.agilerunner.domain.agentruntime.WebhookExecutionStatus;
import com.agilerunner.domain.exception.ErrorCode;
import com.agilerunner.domain.exception.FailureDisposition;
import com.agilerunner.domain.exception.ManualRerunQueryNotFoundException;
import com.agilerunner.domain.executioncontrol.ExecutionControlMode;
import com.agilerunner.domain.executioncontrol.GitHubWriteSkipReason;
import com.agilerunner.domain.review.RerunExecutionStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ManualRerunQueryServiceTest {

    @DisplayName("manual rerun execution key 조회는 runtime evidence 값을 조회 응답 의미로 연결한다.")
    @Test
    void find_returnsResponseFromWebhookExecution() {
        // given
        AgentRuntimeRepository repository = mock(AgentRuntimeRepository.class);
        ManualRerunQueryService service = new ManualRerunQueryService(repository);
        WebhookExecution webhookExecution = WebhookExecution.start(
                "EXECUTION:MANUAL_RERUN:query-1",
                "PR_REVIEW:owner/repo#12",
                "MANUAL_RERUN_DELIVERY:query-1",
                "owner/repo",
                12,
                "PULL_REQUEST",
                "manual_rerun",
                LocalDateTime.of(2026, 4, 9, 12, 0)
        ).withExecutionStartType(
                ExecutionStartType.MANUAL_RERUN
        ).withExecutionControl(
                ExecutionControlMode.DRY_RUN,
                false,
                GitHubWriteSkipReason.DRY_RUN
        ).complete(
                WebhookExecutionStatus.FAILED,
                "GitHub App ID missing",
                ErrorCode.GITHUB_APP_CONFIGURATION_MISSING,
                FailureDisposition.MANUAL_ACTION_REQUIRED,
                LocalDateTime.of(2026, 4, 9, 12, 1)
        );
        when(repository.findWebhookExecution("EXECUTION:MANUAL_RERUN:query-1")).thenReturn(Optional.of(webhookExecution));

        // when
        ManualRerunQueryServiceResponse response = service.find(
                ManualRerunQueryServiceRequest.of("EXECUTION:MANUAL_RERUN:query-1")
        );

        // then
        assertThat(response.getExecutionKey()).isEqualTo("EXECUTION:MANUAL_RERUN:query-1");
        assertThat(response.getExecutionControlMode()).isEqualTo(ExecutionControlMode.DRY_RUN);
        assertThat(response.isWritePerformed()).isFalse();
        assertThat(response.getExecutionStatus()).isEqualTo(RerunExecutionStatus.FAILED);
        assertThat(response.getErrorCode()).isEqualTo(ErrorCode.GITHUB_APP_CONFIGURATION_MISSING);
        assertThat(response.getFailureDisposition()).isEqualTo(FailureDisposition.MANUAL_ACTION_REQUIRED);
    }

    @DisplayName("manual rerun execution이 없으면 조회는 not found 예외를 던진다.")
    @Test
    void find_throwsNotFoundWhenExecutionDoesNotExist() {
        // given
        AgentRuntimeRepository repository = mock(AgentRuntimeRepository.class);
        ManualRerunQueryService service = new ManualRerunQueryService(repository);
        when(repository.findWebhookExecution("EXECUTION:MANUAL_RERUN:missing")).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> service.find(ManualRerunQueryServiceRequest.of("EXECUTION:MANUAL_RERUN:missing")))
                .isInstanceOf(ManualRerunQueryNotFoundException.class)
                .hasMessage("재실행 결과를 찾을 수 없습니다.");
    }
}
