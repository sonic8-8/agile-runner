package com.agilerunner.api.service.review;

import com.agilerunner.api.service.review.request.ManualRerunExecutionListServiceRequest;
import com.agilerunner.api.service.review.response.ManualRerunExecutionListServiceResponse;
import com.agilerunner.client.agentruntime.AgentRuntimeRepository;
import com.agilerunner.domain.agentruntime.ExecutionStartType;
import com.agilerunner.domain.agentruntime.WebhookExecution;
import com.agilerunner.domain.agentruntime.WebhookExecutionStatus;
import com.agilerunner.domain.exception.ErrorCode;
import com.agilerunner.domain.exception.FailureDisposition;
import com.agilerunner.domain.executioncontrol.ExecutionControlMode;
import com.agilerunner.domain.review.ManualRerunAvailableAction;
import com.agilerunner.domain.review.ManualRerunControlAction;
import com.agilerunner.domain.review.RerunExecutionStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ManualRerunExecutionListServiceTest {

    @DisplayName("목록 조회는 값이 있는 필터 조건만 적용해 execution을 줄인다.")
    @Test
    void list_appliesGivenFilters() {
        // given
        AgentRuntimeRepository repository = mock(AgentRuntimeRepository.class);
        ManualRerunExecutionListService service = new ManualRerunExecutionListService(repository);
        when(repository.findManualRerunExecutions()).thenReturn(List.of(
                manualRerunExecution(
                        "EXECUTION:MANUAL_RERUN:1",
                        null,
                        "owner/repo",
                        12,
                        WebhookExecutionStatus.FAILED,
                        FailureDisposition.RETRYABLE,
                        ExecutionControlMode.DRY_RUN,
                        false
                ),
                manualRerunExecution(
                        "EXECUTION:MANUAL_RERUN:2",
                        null,
                        "owner/repo",
                        12,
                        WebhookExecutionStatus.FAILED,
                        FailureDisposition.MANUAL_ACTION_REQUIRED,
                        ExecutionControlMode.DRY_RUN,
                        false
                ),
                manualRerunExecution(
                        "EXECUTION:MANUAL_RERUN:3",
                        null,
                        "other/repo",
                        99,
                        WebhookExecutionStatus.SUCCEEDED,
                        null,
                        ExecutionControlMode.NORMAL,
                        true
                )
        ));

        // when
        ManualRerunExecutionListServiceResponse response = service.list(
                ManualRerunExecutionListServiceRequest.of(
                        "owner/repo",
                        12,
                        ExecutionStartType.MANUAL_RERUN,
                        WebhookExecutionStatus.FAILED,
                        FailureDisposition.RETRYABLE
                )
        );

        // then
        assertThat(response.getExecutions())
                .extracting(ManualRerunExecutionListServiceResponse.ExecutionSummary::getExecutionKey)
                .containsExactly("EXECUTION:MANUAL_RERUN:1");
    }

    @DisplayName("목록 조회는 비어 있는 필터를 미적용으로 해석하고 manual rerun execution 전체를 반환한다.")
    @Test
    void list_ignoresMissingFilters() {
        // given
        AgentRuntimeRepository repository = mock(AgentRuntimeRepository.class);
        ManualRerunExecutionListService service = new ManualRerunExecutionListService(repository);
        when(repository.findManualRerunExecutions()).thenReturn(List.of(
                manualRerunExecution(
                        "EXECUTION:MANUAL_RERUN:10",
                        null,
                        "owner/repo",
                        10,
                        WebhookExecutionStatus.FAILED,
                        FailureDisposition.RETRYABLE,
                        ExecutionControlMode.DRY_RUN,
                        false
                ),
                manualRerunExecution(
                        "EXECUTION:MANUAL_RERUN:11",
                        null,
                        "owner/repo",
                        11,
                        WebhookExecutionStatus.SUCCEEDED,
                        null,
                        ExecutionControlMode.NORMAL,
                        true
                )
        ));

        // when
        ManualRerunExecutionListServiceResponse response = service.list(
                ManualRerunExecutionListServiceRequest.of(null, null, null, null, null)
        );

        // then
        assertThat(response.getExecutions())
                .extracting(ManualRerunExecutionListServiceResponse.ExecutionSummary::getExecutionKey)
                .containsExactly("EXECUTION:MANUAL_RERUN:10", "EXECUTION:MANUAL_RERUN:11");
    }

    @DisplayName("목록 조회 응답 row는 runtime 상태 필드와 RETRY 가능 여부를 함께 반환한다.")
    @Test
    void list_mapsRuntimeFieldsAndAvailableActions() {
        // given
        AgentRuntimeRepository repository = mock(AgentRuntimeRepository.class);
        ManualRerunExecutionListService service = new ManualRerunExecutionListService(repository);
        when(repository.findManualRerunExecutions()).thenReturn(List.of(
                manualRerunExecution(
                        "EXECUTION:MANUAL_RERUN:20",
                        "EXECUTION:MANUAL_RERUN:source-20",
                        "owner/repo",
                        20,
                        WebhookExecutionStatus.FAILED,
                        FailureDisposition.RETRYABLE,
                        ExecutionControlMode.DRY_RUN,
                        false
                ),
                manualRerunExecution(
                        "EXECUTION:MANUAL_RERUN:21",
                        null,
                        "owner/repo",
                        21,
                        WebhookExecutionStatus.SUCCEEDED,
                        null,
                        ExecutionControlMode.NORMAL,
                        true
                )
        ));

        // when
        ManualRerunExecutionListServiceResponse response = service.list(
                ManualRerunExecutionListServiceRequest.of(null, null, null, null, null)
        );

        // then
        assertThat(response.getExecutions()).hasSize(2);

        ManualRerunExecutionListServiceResponse.ExecutionSummary retryable = response.getExecutions().get(0);
        assertThat(retryable.getExecutionKey()).isEqualTo("EXECUTION:MANUAL_RERUN:20");
        assertThat(retryable.getRetrySourceExecutionKey()).isEqualTo("EXECUTION:MANUAL_RERUN:source-20");
        assertThat(retryable.getExecutionStartType()).isEqualTo(ExecutionStartType.MANUAL_RERUN);
        assertThat(retryable.getExecutionStatus()).isEqualTo(RerunExecutionStatus.FAILED);
        assertThat(retryable.getExecutionControlMode()).isEqualTo(ExecutionControlMode.DRY_RUN);
        assertThat(retryable.isWritePerformed()).isFalse();
        assertThat(retryable.getErrorCode()).isEqualTo(ErrorCode.GITHUB_COMMENT_POST_FAILED);
        assertThat(retryable.getFailureDisposition()).isEqualTo(FailureDisposition.RETRYABLE);
        assertThat(retryable.getAvailableActions()).containsExactly(ManualRerunAvailableAction.RETRY);

        ManualRerunExecutionListServiceResponse.ExecutionSummary completed = response.getExecutions().get(1);
        assertThat(completed.getExecutionKey()).isEqualTo("EXECUTION:MANUAL_RERUN:21");
        assertThat(completed.getRetrySourceExecutionKey()).isNull();
        assertThat(completed.getExecutionStartType()).isEqualTo(ExecutionStartType.MANUAL_RERUN);
        assertThat(completed.getExecutionStatus()).isEqualTo(RerunExecutionStatus.SUCCEEDED);
        assertThat(completed.getExecutionControlMode()).isEqualTo(ExecutionControlMode.NORMAL);
        assertThat(completed.isWritePerformed()).isTrue();
        assertThat(completed.getErrorCode()).isNull();
        assertThat(completed.getFailureDisposition()).isNull();
        assertThat(completed.getAvailableActions()).isEmpty();
    }

    @DisplayName("목록 조회 응답은 MANUAL_ACTION_REQUIRED 실행에 ACKNOWLEDGE 가능 여부를 반영한다.")
    @Test
    void list_mapsAcknowledgeAvailability() {
        // given
        AgentRuntimeRepository repository = mock(AgentRuntimeRepository.class);
        ManualRerunExecutionListService service = new ManualRerunExecutionListService(repository);
        when(repository.findManualRerunExecutions()).thenReturn(List.of(
                manualRerunExecution(
                        "EXECUTION:MANUAL_RERUN:30",
                        null,
                        "owner/repo",
                        30,
                        WebhookExecutionStatus.FAILED,
                        FailureDisposition.MANUAL_ACTION_REQUIRED,
                        ExecutionControlMode.DRY_RUN,
                        false
                ),
                manualRerunExecution(
                        "EXECUTION:MANUAL_RERUN:31",
                        null,
                        "owner/repo",
                        31,
                        WebhookExecutionStatus.FAILED,
                        FailureDisposition.MANUAL_ACTION_REQUIRED,
                        ExecutionControlMode.DRY_RUN,
                        false
                )
        ));
        when(repository.findLatestAppliedManualRerunControlAction("EXECUTION:MANUAL_RERUN:30"))
                .thenReturn(Optional.empty());
        when(repository.findLatestAppliedManualRerunControlAction("EXECUTION:MANUAL_RERUN:31"))
                .thenReturn(Optional.of(ManualRerunControlAction.ACKNOWLEDGE));

        // when
        ManualRerunExecutionListServiceResponse response = service.list(
                ManualRerunExecutionListServiceRequest.of(null, null, null, null, null)
        );

        // then
        assertThat(response.getExecutions()).hasSize(2);
        assertThat(response.getExecutions().get(0).getAvailableActions())
                .containsExactly(ManualRerunAvailableAction.ACKNOWLEDGE);
        assertThat(response.getExecutions().get(1).getAvailableActions())
                .containsExactly(ManualRerunAvailableAction.UNACKNOWLEDGE);
    }

    @DisplayName("목록 조회 응답은 마지막 applied action이 UNACKNOWLEDGE면 ACKNOWLEDGE를 다시 노출한다.")
    @Test
    void list_exposesAcknowledgeAgainWhenLatestActionIsUnacknowledge() {
        // given
        AgentRuntimeRepository repository = mock(AgentRuntimeRepository.class);
        ManualRerunExecutionListService service = new ManualRerunExecutionListService(repository);
        when(repository.findManualRerunExecutions()).thenReturn(List.of(
                manualRerunExecution(
                        "EXECUTION:MANUAL_RERUN:32",
                        null,
                        "owner/repo",
                        32,
                        WebhookExecutionStatus.FAILED,
                        FailureDisposition.MANUAL_ACTION_REQUIRED,
                        ExecutionControlMode.DRY_RUN,
                        false
                )
        ));
        when(repository.findLatestAppliedManualRerunControlAction("EXECUTION:MANUAL_RERUN:32"))
                .thenReturn(Optional.of(ManualRerunControlAction.UNACKNOWLEDGE));

        // when
        ManualRerunExecutionListServiceResponse response = service.list(
                ManualRerunExecutionListServiceRequest.of(null, null, null, null, null)
        );

        // then
        assertThat(response.getExecutions()).hasSize(1);
        assertThat(response.getExecutions().get(0).getAvailableActions())
                .containsExactly(ManualRerunAvailableAction.ACKNOWLEDGE);
    }

    private WebhookExecution manualRerunExecution(String executionKey,
                                                  String retrySourceExecutionKey,
                                                  String repositoryName,
                                                  int pullRequestNumber,
                                                  WebhookExecutionStatus status,
                                                  FailureDisposition failureDisposition,
                                                  ExecutionControlMode executionControlMode,
                                                  boolean writePerformed) {
        return WebhookExecution.start(
                executionKey,
                "PR_REVIEW:" + repositoryName + "#" + pullRequestNumber,
                "MANUAL_RERUN_DELIVERY:" + executionKey,
                repositoryName,
                pullRequestNumber,
                "PULL_REQUEST",
                "manual_rerun",
                LocalDateTime.of(2026, 4, 9, 18, 0)
        ).withRetrySourceExecutionKey(
                retrySourceExecutionKey
        ).withExecutionStartType(
                ExecutionStartType.MANUAL_RERUN
        ).withExecutionControl(
                executionControlMode,
                writePerformed,
                null
        ).complete(
                status,
                status == WebhookExecutionStatus.FAILED ? "failure" : null,
                status == WebhookExecutionStatus.FAILED ? ErrorCode.GITHUB_COMMENT_POST_FAILED : null,
                failureDisposition,
                LocalDateTime.of(2026, 4, 9, 18, 1)
        );
    }
}
