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
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

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
                manualRerunExecution("EXECUTION:MANUAL_RERUN:1", "owner/repo", 12, WebhookExecutionStatus.FAILED, FailureDisposition.RETRYABLE),
                manualRerunExecution("EXECUTION:MANUAL_RERUN:2", "owner/repo", 12, WebhookExecutionStatus.FAILED, FailureDisposition.MANUAL_ACTION_REQUIRED),
                manualRerunExecution("EXECUTION:MANUAL_RERUN:3", "other/repo", 99, WebhookExecutionStatus.SUCCEEDED, null)
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
                manualRerunExecution("EXECUTION:MANUAL_RERUN:10", "owner/repo", 10, WebhookExecutionStatus.FAILED, FailureDisposition.RETRYABLE),
                manualRerunExecution("EXECUTION:MANUAL_RERUN:11", "owner/repo", 11, WebhookExecutionStatus.SUCCEEDED, null)
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

    private WebhookExecution manualRerunExecution(String executionKey,
                                                  String repositoryName,
                                                  int pullRequestNumber,
                                                  WebhookExecutionStatus status,
                                                  FailureDisposition failureDisposition) {
        return WebhookExecution.start(
                executionKey,
                "PR_REVIEW:" + repositoryName + "#" + pullRequestNumber,
                "MANUAL_RERUN_DELIVERY:" + executionKey,
                repositoryName,
                pullRequestNumber,
                "PULL_REQUEST",
                "manual_rerun",
                LocalDateTime.of(2026, 4, 9, 18, 0)
        ).withExecutionStartType(
                ExecutionStartType.MANUAL_RERUN
        ).withExecutionControl(
                ExecutionControlMode.DRY_RUN,
                false,
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
