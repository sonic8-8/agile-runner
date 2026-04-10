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
import com.agilerunner.domain.review.ManualRerunAvailableAction;
import com.agilerunner.domain.review.ManualRerunControlAction;
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
        when(repository.findLatestAppliedManualRerunControlAction("EXECUTION:MANUAL_RERUN:query-1"))
                .thenReturn(Optional.empty());

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
        assertThat(response.getAvailableActions()).containsExactly(ManualRerunAvailableAction.ACKNOWLEDGE);
    }

    @DisplayName("이미 ACKNOWLEDGE가 적용된 manual rerun execution은 조회 응답에서 해당 액션을 다시 노출하지 않는다.")
    @Test
    void find_excludesAcknowledgeWhenAuditExists() {
        // given
        AgentRuntimeRepository repository = mock(AgentRuntimeRepository.class);
        ManualRerunQueryService service = new ManualRerunQueryService(repository);
        WebhookExecution webhookExecution = WebhookExecution.start(
                "EXECUTION:MANUAL_RERUN:query-acknowledged",
                "PR_REVIEW:owner/repo#12",
                "MANUAL_RERUN_DELIVERY:query-acknowledged",
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
        when(repository.findWebhookExecution("EXECUTION:MANUAL_RERUN:query-acknowledged")).thenReturn(Optional.of(webhookExecution));
        when(repository.findLatestAppliedManualRerunControlAction("EXECUTION:MANUAL_RERUN:query-acknowledged"))
                .thenReturn(Optional.of(ManualRerunControlAction.ACKNOWLEDGE));

        // when
        ManualRerunQueryServiceResponse response = service.find(
                ManualRerunQueryServiceRequest.of("EXECUTION:MANUAL_RERUN:query-acknowledged")
        );

        // then
        assertThat(response.getAvailableActions()).containsExactly(ManualRerunAvailableAction.UNACKNOWLEDGE);
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

    @DisplayName("manual rerun이 아닌 execution key는 조회 대상이 아니므로 not found 예외를 던진다.")
    @Test
    void find_throwsNotFoundWhenExecutionIsNotManualRerun() {
        // given
        AgentRuntimeRepository repository = mock(AgentRuntimeRepository.class);
        ManualRerunQueryService service = new ManualRerunQueryService(repository);
        WebhookExecution webhookExecution = WebhookExecution.start(
                "EXECUTION:delivery-99",
                "PR_REVIEW:owner/repo#12",
                "delivery-99",
                "owner/repo",
                12,
                "PULL_REQUEST",
                "opened",
                LocalDateTime.of(2026, 4, 9, 12, 30)
        ).withExecutionStartType(ExecutionStartType.WEBHOOK);
        when(repository.findWebhookExecution("EXECUTION:delivery-99")).thenReturn(Optional.of(webhookExecution));

        // when & then
        assertThatThrownBy(() -> service.find(ManualRerunQueryServiceRequest.of("EXECUTION:delivery-99")))
                .isInstanceOf(ManualRerunQueryNotFoundException.class)
                .hasMessage("재실행 결과를 찾을 수 없습니다.");
    }
}
