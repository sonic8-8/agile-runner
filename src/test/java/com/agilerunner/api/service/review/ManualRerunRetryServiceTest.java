package com.agilerunner.api.service.review;

import com.agilerunner.api.service.review.request.ManualRerunRetryServiceRequest;
import com.agilerunner.api.service.review.request.ManualRerunServiceRequest;
import com.agilerunner.api.service.review.response.ManualRerunRetryServiceResponse;
import com.agilerunner.api.service.review.response.ManualRerunServiceResponse;
import com.agilerunner.client.agentruntime.AgentRuntimeRepository;
import com.agilerunner.domain.agentruntime.ExecutionStartType;
import com.agilerunner.domain.agentruntime.WebhookExecution;
import com.agilerunner.domain.agentruntime.WebhookExecutionStatus;
import com.agilerunner.domain.exception.ErrorCode;
import com.agilerunner.domain.exception.FailureDisposition;
import com.agilerunner.domain.exception.ManualRerunRetryConflictException;
import com.agilerunner.domain.exception.ManualRerunRetryNotFoundException;
import com.agilerunner.domain.executioncontrol.ExecutionControlMode;
import com.agilerunner.domain.review.RerunExecutionStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ManualRerunRetryServiceTest {

    @DisplayName("허용된 재시도 요청은 source execution의 저장소와 PR 문맥을 재사용한다.")
    @Test
    void retry_reusesSourceRepositoryAndPullRequestContext() {
        // given
        AgentRuntimeRepository repository = mock(AgentRuntimeRepository.class);
        ManualRerunService manualRerunService = mock(ManualRerunService.class);
        ManualRerunRetryService service = new ManualRerunRetryService(repository, manualRerunService);
        WebhookExecution sourceExecution = sourceExecution(
                "EXECUTION:MANUAL_RERUN:source-1",
                FailureDisposition.RETRYABLE,
                true,
                "src/keep/FromSource.java"
        );
        ManualRerunRetryServiceRequest request = ManualRerunRetryServiceRequest.of(
                "EXECUTION:MANUAL_RERUN:source-1",
                300L,
                ExecutionControlMode.DRY_RUN,
                List.of("src/main/App.java", "src/test/AppTest.java")
        );
        ManualRerunServiceResponse rerunResponse = ManualRerunServiceResponse.of(
                "EXECUTION:MANUAL_RERUN:retry-1",
                ExecutionControlMode.DRY_RUN,
                false,
                RerunExecutionStatus.SUCCEEDED,
                null,
                null
        );
        when(repository.findWebhookExecution("EXECUTION:MANUAL_RERUN:source-1")).thenReturn(Optional.of(sourceExecution));
        when(manualRerunService.rerun(any(ManualRerunServiceRequest.class))).thenReturn(rerunResponse);

        // when
        ManualRerunRetryServiceResponse response = service.retry(request);

        // then
        ArgumentCaptor<ManualRerunServiceRequest> requestCaptor = ArgumentCaptor.forClass(ManualRerunServiceRequest.class);
        verify(manualRerunService).rerun(requestCaptor.capture());
        assertThat(requestCaptor.getValue().getRepositoryName()).isEqualTo("owner/repo");
        assertThat(requestCaptor.getValue().getPullRequestNumber()).isEqualTo(27);
        assertThat(requestCaptor.getValue().getInstallationId()).isEqualTo(300L);
        assertThat(requestCaptor.getValue().getExecutionControlMode()).isEqualTo(ExecutionControlMode.DRY_RUN);
        assertThat(requestCaptor.getValue().getSelectedPaths()).containsExactly(
                "src/main/App.java",
                "src/test/AppTest.java"
        );
        assertThat(requestCaptor.getValue().getRetrySourceExecutionKey()).isEqualTo("EXECUTION:MANUAL_RERUN:source-1");

        assertThat(response.getExecutionKey()).isEqualTo("EXECUTION:MANUAL_RERUN:retry-1");
        assertThat(response.getRetrySourceExecutionKey()).isEqualTo("EXECUTION:MANUAL_RERUN:source-1");
        assertThat(response.getExecutionControlMode()).isEqualTo(ExecutionControlMode.DRY_RUN);
        assertThat(response.isWritePerformed()).isFalse();
        assertThat(response.getExecutionStatus()).isEqualTo(RerunExecutionStatus.SUCCEEDED);
        assertThat(response.getErrorCode()).isNull();
        assertThat(response.getFailureDisposition()).isNull();
    }

    @DisplayName("selectedPaths가 비어 있으면 source execution의 선택 경로를 재사용하지 않고 전체 실행으로 해석한다.")
    @Test
    void retry_treatsEmptySelectedPathsAsFullExecution() {
        // given
        AgentRuntimeRepository repository = mock(AgentRuntimeRepository.class);
        ManualRerunService manualRerunService = mock(ManualRerunService.class);
        ManualRerunRetryService service = new ManualRerunRetryService(repository, manualRerunService);
        WebhookExecution sourceExecution = sourceExecution(
                "EXECUTION:MANUAL_RERUN:source-2",
                FailureDisposition.RETRYABLE,
                true,
                "src/source/Only.java"
        );
        ManualRerunRetryServiceRequest request = ManualRerunRetryServiceRequest.of(
                "EXECUTION:MANUAL_RERUN:source-2",
                301L,
                ExecutionControlMode.NORMAL,
                List.of()
        );
        ManualRerunServiceResponse rerunResponse = ManualRerunServiceResponse.of(
                "EXECUTION:MANUAL_RERUN:retry-2",
                ExecutionControlMode.NORMAL,
                true,
                RerunExecutionStatus.SUCCEEDED,
                null,
                null
        );
        when(repository.findWebhookExecution("EXECUTION:MANUAL_RERUN:source-2")).thenReturn(Optional.of(sourceExecution));
        when(manualRerunService.rerun(any(ManualRerunServiceRequest.class))).thenReturn(rerunResponse);

        // when
        service.retry(request);

        // then
        ArgumentCaptor<ManualRerunServiceRequest> requestCaptor = ArgumentCaptor.forClass(ManualRerunServiceRequest.class);
        verify(manualRerunService).rerun(requestCaptor.capture());
        assertThat(requestCaptor.getValue().getSelectedPaths()).isEmpty();
    }

    @DisplayName("존재하지 않는 source execution은 재시도 not found로 처리한다.")
    @Test
    void retry_throwsNotFoundWhenSourceExecutionMissing() {
        // given
        AgentRuntimeRepository repository = mock(AgentRuntimeRepository.class);
        ManualRerunService manualRerunService = mock(ManualRerunService.class);
        ManualRerunRetryService service = new ManualRerunRetryService(repository, manualRerunService);
        ManualRerunRetryServiceRequest request = ManualRerunRetryServiceRequest.of(
                "EXECUTION:MANUAL_RERUN:missing",
                302L,
                ExecutionControlMode.DRY_RUN,
                List.of()
        );
        when(repository.findWebhookExecution("EXECUTION:MANUAL_RERUN:missing")).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> service.retry(request))
                .isInstanceOf(ManualRerunRetryNotFoundException.class)
                .hasMessage("재시도 대상 실행을 찾을 수 없습니다.");
    }

    @DisplayName("재시도 불가 source execution은 conflict와 failureDisposition을 함께 반환한다.")
    @Test
    void retry_throwsConflictWhenSourceExecutionIsIneligible() {
        // given
        AgentRuntimeRepository repository = mock(AgentRuntimeRepository.class);
        ManualRerunService manualRerunService = mock(ManualRerunService.class);
        ManualRerunRetryService service = new ManualRerunRetryService(repository, manualRerunService);
        WebhookExecution sourceExecution = sourceExecution(
                "EXECUTION:MANUAL_RERUN:source-3",
                FailureDisposition.MANUAL_ACTION_REQUIRED,
                false,
                null
        );
        ManualRerunRetryServiceRequest request = ManualRerunRetryServiceRequest.of(
                "EXECUTION:MANUAL_RERUN:source-3",
                303L,
                ExecutionControlMode.NORMAL,
                List.of()
        );
        when(repository.findWebhookExecution("EXECUTION:MANUAL_RERUN:source-3")).thenReturn(Optional.of(sourceExecution));

        // when & then
        assertThatThrownBy(() -> service.retry(request))
                .isInstanceOfSatisfying(ManualRerunRetryConflictException.class, exception -> {
                    assertThat(exception.getExecutionKey()).isEqualTo("EXECUTION:MANUAL_RERUN:source-3");
                    assertThat(exception.getFailureDisposition()).isEqualTo(FailureDisposition.MANUAL_ACTION_REQUIRED);
                    assertThat(exception.getMessage()).isEqualTo("수동 조치가 필요한 실행은 바로 재시도할 수 없습니다.");
                });
    }

    private WebhookExecution sourceExecution(String executionKey,
                                             FailureDisposition failureDisposition,
                                             boolean selectionApplied,
                                             String selectedPathsSummary) {
        return WebhookExecution.start(
                executionKey,
                "PR_REVIEW:owner/repo#27",
                "MANUAL_RERUN_DELIVERY:" + executionKey,
                "owner/repo",
                27,
                "PULL_REQUEST",
                "manual_rerun",
                LocalDateTime.of(2026, 4, 9, 22, 0)
        ).withExecutionStartType(ExecutionStartType.MANUAL_RERUN)
                .withExecutionControl(ExecutionControlMode.NORMAL, false, null)
                .withSelectionScope(selectionApplied, selectedPathsSummary)
                .complete(
                        WebhookExecutionStatus.FAILED,
                        "retry source failed",
                        ErrorCode.GITHUB_COMMENT_POST_FAILED,
                        failureDisposition,
                        LocalDateTime.of(2026, 4, 9, 22, 1)
                );
    }
}
