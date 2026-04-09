package com.agilerunner.api.service.review;

import com.agilerunner.api.service.review.request.ManualRerunControlActionServiceRequest;
import com.agilerunner.api.service.review.response.ManualRerunControlActionServiceResponse;
import com.agilerunner.client.agentruntime.AgentRuntimeRepository;
import com.agilerunner.domain.agentruntime.ExecutionStartType;
import com.agilerunner.domain.agentruntime.WebhookExecution;
import com.agilerunner.domain.agentruntime.WebhookExecutionStatus;
import com.agilerunner.domain.exception.ErrorCode;
import com.agilerunner.domain.exception.FailureDisposition;
import com.agilerunner.domain.exception.ManualRerunControlActionConflictException;
import com.agilerunner.domain.exception.ManualRerunControlActionNotFoundException;
import com.agilerunner.domain.executioncontrol.ExecutionControlMode;
import com.agilerunner.domain.review.ManualRerunAvailableAction;
import com.agilerunner.domain.review.ManualRerunControlAction;
import com.agilerunner.domain.review.ManualRerunControlActionAudit;
import com.agilerunner.domain.review.ManualRerunControlActionStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ManualRerunControlActionServiceTest {

    @DisplayName("ACKNOWLEDGE 요청은 허용된 실행에 audit evidence를 남기고 응답을 반환한다.")
    @Test
    void execute_returnsAppliedResponseContract() {
        // given
        AgentRuntimeRepository repository = mock(AgentRuntimeRepository.class);
        ManualRerunControlActionService service = new ManualRerunControlActionService(repository);
        ManualRerunControlActionServiceRequest request = ManualRerunControlActionServiceRequest.of(
                "EXECUTION:MANUAL_RERUN:action-1",
                ManualRerunControlAction.ACKNOWLEDGE,
                "운영자 확인 완료"
        );
        when(repository.findWebhookExecution("EXECUTION:MANUAL_RERUN:action-1"))
                .thenReturn(Optional.of(manualActionRequiredExecution("EXECUTION:MANUAL_RERUN:action-1")));
        when(repository.hasAppliedManualRerunControlAction(
                "EXECUTION:MANUAL_RERUN:action-1",
                ManualRerunControlAction.ACKNOWLEDGE
        )).thenReturn(false);

        // when
        ManualRerunControlActionServiceResponse response = service.execute(request);

        // then
        assertThat(response.getExecutionKey()).isEqualTo("EXECUTION:MANUAL_RERUN:action-1");
        assertThat(response.getAction()).isEqualTo(ManualRerunControlAction.ACKNOWLEDGE);
        assertThat(response.getActionStatus()).isEqualTo(ManualRerunControlActionStatus.APPLIED);
        assertThat(response.getAvailableActions()).isEmpty();
        assertThat(response.getNote()).isEqualTo("운영자 확인 완료");
        verify(repository).appendManualRerunControlActionAudit(any(ManualRerunControlActionAudit.class));
    }

    @DisplayName("존재하지 않는 executionKey의 관리자 제어 액션 요청은 not found 예외를 던진다.")
    @Test
    void execute_throwsNotFoundWhenExecutionDoesNotExist() {
        // given
        AgentRuntimeRepository repository = mock(AgentRuntimeRepository.class);
        ManualRerunControlActionService service = new ManualRerunControlActionService(repository);
        ManualRerunControlActionServiceRequest request = ManualRerunControlActionServiceRequest.of(
                "EXECUTION:MANUAL_RERUN:missing",
                ManualRerunControlAction.ACKNOWLEDGE,
                "운영자 확인 완료"
        );
        when(repository.findWebhookExecution("EXECUTION:MANUAL_RERUN:missing")).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> service.execute(request))
                .isInstanceOf(ManualRerunControlActionNotFoundException.class)
                .hasMessage("관리자 제어 액션 대상 실행을 찾을 수 없습니다.");
    }

    @DisplayName("수동 조치가 필요하지 않은 실행의 ACKNOWLEDGE 요청은 conflict 예외를 던진다.")
    @Test
    void execute_throwsConflictWhenExecutionIsNotEligible() {
        // given
        AgentRuntimeRepository repository = mock(AgentRuntimeRepository.class);
        ManualRerunControlActionService service = new ManualRerunControlActionService(repository);
        ManualRerunControlActionServiceRequest request = ManualRerunControlActionServiceRequest.of(
                "EXECUTION:MANUAL_RERUN:retryable",
                ManualRerunControlAction.ACKNOWLEDGE,
                "운영자 확인 완료"
        );
        when(repository.findWebhookExecution("EXECUTION:MANUAL_RERUN:retryable"))
                .thenReturn(Optional.of(retryableExecution("EXECUTION:MANUAL_RERUN:retryable")));
        when(repository.hasAppliedManualRerunControlAction(
                "EXECUTION:MANUAL_RERUN:retryable",
                ManualRerunControlAction.ACKNOWLEDGE
        )).thenReturn(false);

        // when & then
        assertThatThrownBy(() -> service.execute(request))
                .isInstanceOfSatisfying(ManualRerunControlActionConflictException.class, exception -> {
                    assertThat(exception.getExecutionKey()).isEqualTo("EXECUTION:MANUAL_RERUN:retryable");
                    assertThat(exception.getFailureDisposition()).isEqualTo(FailureDisposition.RETRYABLE);
                });
    }

    @DisplayName("이미 ACKNOWLEDGE가 적용된 실행의 중복 요청은 conflict 예외를 던진다.")
    @Test
    void execute_throwsConflictWhenActionAlreadyApplied() {
        // given
        AgentRuntimeRepository repository = mock(AgentRuntimeRepository.class);
        ManualRerunControlActionService service = new ManualRerunControlActionService(repository);
        ManualRerunControlActionServiceRequest request = ManualRerunControlActionServiceRequest.of(
                "EXECUTION:MANUAL_RERUN:acked",
                ManualRerunControlAction.ACKNOWLEDGE,
                "운영자 확인 완료"
        );
        when(repository.findWebhookExecution("EXECUTION:MANUAL_RERUN:acked"))
                .thenReturn(Optional.of(manualActionRequiredExecution("EXECUTION:MANUAL_RERUN:acked")));
        when(repository.hasAppliedManualRerunControlAction(
                "EXECUTION:MANUAL_RERUN:acked",
                ManualRerunControlAction.ACKNOWLEDGE
        )).thenReturn(true);

        // when & then
        assertThatThrownBy(() -> service.execute(request))
                .isInstanceOfSatisfying(ManualRerunControlActionConflictException.class, exception -> {
                    assertThat(exception.getExecutionKey()).isEqualTo("EXECUTION:MANUAL_RERUN:acked");
                    assertThat(exception.getFailureDisposition()).isEqualTo(FailureDisposition.MANUAL_ACTION_REQUIRED);
                });
    }

    private WebhookExecution manualActionRequiredExecution(String executionKey) {
        return baseExecution(executionKey).complete(
                WebhookExecutionStatus.FAILED,
                "manual action required",
                ErrorCode.GITHUB_APP_CONFIGURATION_MISSING,
                FailureDisposition.MANUAL_ACTION_REQUIRED,
                LocalDateTime.of(2026, 4, 10, 10, 1)
        );
    }

    private WebhookExecution retryableExecution(String executionKey) {
        return baseExecution(executionKey).complete(
                WebhookExecutionStatus.FAILED,
                "retryable failure",
                ErrorCode.GITHUB_COMMENT_POST_FAILED,
                FailureDisposition.RETRYABLE,
                LocalDateTime.of(2026, 4, 10, 10, 1)
        );
    }

    private WebhookExecution baseExecution(String executionKey) {
        return WebhookExecution.start(
                executionKey,
                "PR_REVIEW:owner/repo#10",
                "MANUAL_RERUN_DELIVERY:" + executionKey,
                "owner/repo",
                10,
                "PULL_REQUEST",
                "manual_rerun",
                LocalDateTime.of(2026, 4, 10, 10, 0)
        ).withExecutionStartType(ExecutionStartType.MANUAL_RERUN)
                .withExecutionControl(ExecutionControlMode.DRY_RUN, false, null);
    }
}
