package com.agilerunner.api.service.agentruntime;

import com.agilerunner.api.service.dto.GitHubCommentResponse;
import com.agilerunner.api.service.github.request.GitHubEventServiceRequest;
import com.agilerunner.api.service.github.response.GitHubCommentExecutionResult;
import com.agilerunner.api.service.review.request.ManualRerunServiceRequest;
import com.agilerunner.client.agentruntime.AgentRuntimeRepository;
import com.agilerunner.domain.agentruntime.AgentExecutionLog;
import com.agilerunner.domain.agentruntime.AgentExecutionStatus;
import com.agilerunner.domain.agentruntime.CriteriaStatus;
import com.agilerunner.domain.agentruntime.ExecutionStartType;
import com.agilerunner.domain.agentruntime.ValidationCriteria;
import com.agilerunner.domain.agentruntime.WebhookExecution;
import com.agilerunner.domain.agentruntime.WebhookExecutionStatus;
import com.agilerunner.domain.agentruntime.TaskRuntimeState;
import com.agilerunner.domain.agentruntime.TaskRuntimeStatus;
import com.agilerunner.domain.executioncontrol.ExecutionControlMode;
import com.agilerunner.domain.executioncontrol.GitHubWriteSkipReason;
import com.agilerunner.domain.exception.AgileRunnerException;
import com.agilerunner.domain.exception.ErrorCode;
import com.agilerunner.domain.exception.FailureDisposition;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static com.agilerunner.GitHubEventType.PULL_REQUEST;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AgentRuntimeServiceTest {

    @DisplayName("webhook execution 시작 시 task runtime state, validation criteria, execution, 실행 로그를 함께 저장한다.")
    @Test
    void startWebhookExecution_persistsTaskRunAndCriteria() {
        // given
        AgentRuntimeRepository repository = mock(AgentRuntimeRepository.class);
        when(repository.findTaskRuntimeState("PR_REVIEW:sonic8-8:agile-runner#11")).thenReturn(Optional.empty());
        AgentRuntimeService service = new AgentRuntimeService(repository, new ObjectMapper());
        GitHubEventServiceRequest request = GitHubEventServiceRequest.of(PULL_REQUEST, buildPayload("sonic8-8/agile-runner", 11, "opened"), 999L);

        // when
        WebhookExecution webhookExecution = service.startWebhookExecution("delivery-11", request);

        // then
        ArgumentCaptor<TaskRuntimeState> taskCaptor = ArgumentCaptor.forClass(TaskRuntimeState.class);
        ArgumentCaptor<WebhookExecution> webhookExecutionCaptor = ArgumentCaptor.forClass(WebhookExecution.class);
        ArgumentCaptor<AgentExecutionLog> logCaptor = ArgumentCaptor.forClass(AgentExecutionLog.class);
        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<ValidationCriteria>> criteriaCaptor = ArgumentCaptor.forClass(List.class);

        verify(repository).upsertTaskRuntimeState(taskCaptor.capture());
        verify(repository).replaceValidationCriteria(eq("PR_REVIEW:sonic8-8:agile-runner#11"), criteriaCaptor.capture());
        verify(repository).upsertWebhookExecution(webhookExecutionCaptor.capture());
        verify(repository).appendExecutionLog(logCaptor.capture());

        TaskRuntimeState savedTask = taskCaptor.getValue();
        assertThat(savedTask.getStatus()).isEqualTo(TaskRuntimeStatus.IN_PROGRESS);
        assertThat(savedTask.getIssueNumber()).isEqualTo(11L);
        assertThat(savedTask.getTitle()).isEqualTo("GitHub PR review for sonic8-8/agile-runner#11");

        List<ValidationCriteria> criteria = criteriaCaptor.getValue();
        assertThat(criteria).hasSize(3);
        assertThat(criteria.get(0).getStatus()).isEqualTo(CriteriaStatus.PASSED);
        assertThat(criteria.get(1).getStatus()).isEqualTo(CriteriaStatus.PENDING);
        assertThat(criteria.get(2).getStatus()).isEqualTo(CriteriaStatus.PENDING);

        WebhookExecution savedRun = webhookExecutionCaptor.getValue();
        assertThat(savedRun.getExecutionKey()).isEqualTo("EXECUTION:delivery-11");
        assertThat(savedRun.getStatus()).isEqualTo(WebhookExecutionStatus.STARTED);

        AgentExecutionLog executionLog = logCaptor.getValue();
        assertThat(executionLog.getExecutionKey()).isEqualTo("EXECUTION:delivery-11");
        assertThat(executionLog.getStepName()).isEqualTo(AgentRuntimeService.STEP_WEBHOOK_ACCEPTED);
        assertThat(executionLog.getStatus()).isEqualTo(AgentExecutionStatus.SUCCEEDED);

        assertThat(webhookExecution.getTaskKey()).isEqualTo("PR_REVIEW:sonic8-8:agile-runner#11");
    }

    @DisplayName("manual rerun execution 시작 시 manual rerun 시작 유형과 execution key를 함께 저장한다.")
    @Test
    void startManualRerunExecution_persistsManualRerunStartType() {
        // given
        AgentRuntimeRepository repository = mock(AgentRuntimeRepository.class);
        when(repository.findTaskRuntimeState("PR_REVIEW:sonic8-8:agile-runner#21")).thenReturn(Optional.empty());
        AgentRuntimeService service = new AgentRuntimeService(repository, new ObjectMapper());
        ManualRerunServiceRequest request = ManualRerunServiceRequest.of(
                "sonic8-8/agile-runner",
                21,
                999L,
                ExecutionControlMode.DRY_RUN,
                List.of("src/Test.java", "src/Main.java")
        );

        // when
        WebhookExecution runtimeExecution = service.startManualRerunExecution(request);

        // then
        ArgumentCaptor<WebhookExecution> executionCaptor = ArgumentCaptor.forClass(WebhookExecution.class);
        ArgumentCaptor<AgentExecutionLog> logCaptor = ArgumentCaptor.forClass(AgentExecutionLog.class);

        verify(repository).upsertWebhookExecution(executionCaptor.capture());
        verify(repository).appendExecutionLog(logCaptor.capture());

        WebhookExecution savedExecution = executionCaptor.getValue();
        assertThat(savedExecution.getExecutionKey()).startsWith("EXECUTION:MANUAL_RERUN:");
        assertThat(savedExecution.getTaskKey()).isEqualTo("PR_REVIEW:sonic8-8:agile-runner#21");
        assertThat(savedExecution.getExecutionStartType()).isEqualTo(ExecutionStartType.MANUAL_RERUN);
        assertThat(savedExecution.getExecutionControlMode()).isEqualTo(ExecutionControlMode.DRY_RUN);
        assertThat(savedExecution.getWritePerformed()).isFalse();
        assertThat(savedExecution.getSelectionApplied()).isTrue();
        assertThat(savedExecution.getSelectedPathsSummary()).isEqualTo("src/Main.java|src/Test.java");

        AgentExecutionLog executionLog = logCaptor.getValue();
        assertThat(executionLog.getExecutionKey()).isEqualTo(savedExecution.getExecutionKey());
        assertThat(executionLog.getExecutionStartType()).isEqualTo(ExecutionStartType.MANUAL_RERUN);
        assertThat(executionLog.getExecutionControlMode()).isEqualTo(ExecutionControlMode.DRY_RUN);
        assertThat(executionLog.getWritePerformed()).isFalse();
        assertThat(executionLog.getSelectionApplied()).isTrue();
        assertThat(executionLog.getSelectedPathsSummary()).isEqualTo("src/Main.java|src/Test.java");

        assertThat(runtimeExecution.getExecutionKey()).isEqualTo(savedExecution.getExecutionKey());
        assertThat(runtimeExecution.getExecutionStartType()).isEqualTo(ExecutionStartType.MANUAL_RERUN);
        assertThat(runtimeExecution.getSelectionApplied()).isTrue();
        assertThat(runtimeExecution.getSelectedPathsSummary()).isEqualTo("src/Main.java|src/Test.java");
    }

    @DisplayName("manual rerun execution 시작 시 선택 경로는 공백 제거, 중복 제거, 정렬 후 요약 문자열로 저장한다.")
    @Test
    void startManualRerunExecution_normalizesSelectedPathsSummary() {
        // given
        AgentRuntimeRepository repository = mock(AgentRuntimeRepository.class);
        when(repository.findTaskRuntimeState("PR_REVIEW:sonic8-8:agile-runner#22")).thenReturn(Optional.empty());
        AgentRuntimeService service = new AgentRuntimeService(repository, new ObjectMapper());
        ManualRerunServiceRequest request = ManualRerunServiceRequest.of(
                "sonic8-8/agile-runner",
                22,
                999L,
                ExecutionControlMode.DRY_RUN,
                List.of("src/B.java", " ", "src/A.java", "src/A.java")
        );

        // when
        WebhookExecution runtimeExecution = service.startManualRerunExecution(request);

        // then
        ArgumentCaptor<WebhookExecution> executionCaptor = ArgumentCaptor.forClass(WebhookExecution.class);
        verify(repository).upsertWebhookExecution(executionCaptor.capture());

        WebhookExecution savedExecution = executionCaptor.getValue();
        assertThat(savedExecution.getSelectionApplied()).isTrue();
        assertThat(savedExecution.getSelectedPathsSummary()).isEqualTo("src/A.java|src/B.java");
        assertThat(runtimeExecution.getSelectionApplied()).isTrue();
        assertThat(runtimeExecution.getSelectedPathsSummary()).isEqualTo("src/A.java|src/B.java");
    }

    @DisplayName("manual rerun execution 시작 시 선택 경로가 비어 있으면 선택 실행 적용 없음으로 저장한다.")
    @Test
    void startManualRerunExecution_withBlankOnlyPaths_persistsSelectionDisabled() {
        // given
        AgentRuntimeRepository repository = mock(AgentRuntimeRepository.class);
        when(repository.findTaskRuntimeState("PR_REVIEW:sonic8-8:agile-runner#23")).thenReturn(Optional.empty());
        AgentRuntimeService service = new AgentRuntimeService(repository, new ObjectMapper());
        ManualRerunServiceRequest request = ManualRerunServiceRequest.of(
                "sonic8-8/agile-runner",
                23,
                999L,
                ExecutionControlMode.DRY_RUN,
                List.of(" ", "")
        );

        // when
        WebhookExecution runtimeExecution = service.startManualRerunExecution(request);

        // then
        ArgumentCaptor<WebhookExecution> executionCaptor = ArgumentCaptor.forClass(WebhookExecution.class);
        verify(repository).upsertWebhookExecution(executionCaptor.capture());

        WebhookExecution savedExecution = executionCaptor.getValue();
        assertThat(savedExecution.getSelectionApplied()).isFalse();
        assertThat(savedExecution.getSelectedPathsSummary()).isNull();
        assertThat(runtimeExecution.getSelectionApplied()).isFalse();
        assertThat(runtimeExecution.getSelectedPathsSummary()).isNull();
    }

    @DisplayName("manual rerun execution 결과 성공 시 manual rerun 시작 유형과 execution key를 유지한다.")
    @Test
    void recordExecutionResult_preservesManualRerunStartType() {
        // given
        AgentRuntimeRepository repository = mock(AgentRuntimeRepository.class);
        TaskRuntimeState existingTask = TaskRuntimeState.of(
                "PR_REVIEW:sonic8-8:agile-runner#31",
                31L,
                "GitHub PR review for sonic8-8/agile-runner#31",
                TaskRuntimeStatus.IN_PROGRESS,
                0,
                null,
                LocalDateTime.of(2026, 4, 8, 19, 0),
                null
        );
        when(repository.findTaskRuntimeState("PR_REVIEW:sonic8-8:agile-runner#31")).thenReturn(Optional.of(existingTask));
        AgentRuntimeService service = new AgentRuntimeService(repository, new ObjectMapper());
        WebhookExecution runtimeExecution = WebhookExecution.start(
                "EXECUTION:MANUAL_RERUN:delivery-31",
                "PR_REVIEW:sonic8-8:agile-runner#31",
                "manual-rerun-delivery-31",
                "sonic8-8/agile-runner",
                31,
                "PULL_REQUEST",
                "manual_rerun",
                LocalDateTime.of(2026, 4, 8, 19, 1)
        ).withExecutionStartType(
                ExecutionStartType.MANUAL_RERUN
        ).withExecutionControl(
                ExecutionControlMode.DRY_RUN,
                false,
                GitHubWriteSkipReason.DRY_RUN
        ).withSelectionScope(
                true,
                "src/Main.java|src/Test.java"
        );
        GitHubCommentExecutionResult executionResult = GitHubCommentExecutionResult.skipped(
                ExecutionControlMode.DRY_RUN,
                GitHubWriteSkipReason.DRY_RUN,
                0
        );

        // when
        service.recordExecutionResult(runtimeExecution, executionResult);

        // then
        ArgumentCaptor<WebhookExecution> executionCaptor = ArgumentCaptor.forClass(WebhookExecution.class);
        ArgumentCaptor<AgentExecutionLog> logCaptor = ArgumentCaptor.forClass(AgentExecutionLog.class);

        verify(repository).upsertWebhookExecution(executionCaptor.capture());
        verify(repository).appendExecutionLog(logCaptor.capture());

        WebhookExecution savedExecution = executionCaptor.getValue();
        assertThat(savedExecution.getExecutionKey()).isEqualTo("EXECUTION:MANUAL_RERUN:delivery-31");
        assertThat(savedExecution.getExecutionStartType()).isEqualTo(ExecutionStartType.MANUAL_RERUN);
        assertThat(savedExecution.getExecutionControlMode()).isEqualTo(ExecutionControlMode.DRY_RUN);
        assertThat(savedExecution.getWritePerformed()).isFalse();
        assertThat(savedExecution.getWriteSkipReason()).isEqualTo(GitHubWriteSkipReason.DRY_RUN);
        assertThat(savedExecution.getSelectionApplied()).isTrue();
        assertThat(savedExecution.getSelectedPathsSummary()).isEqualTo("src/Main.java|src/Test.java");

        AgentExecutionLog executionLog = logCaptor.getValue();
        assertThat(executionLog.getExecutionKey()).isEqualTo("EXECUTION:MANUAL_RERUN:delivery-31");
        assertThat(executionLog.getExecutionStartType()).isEqualTo(ExecutionStartType.MANUAL_RERUN);
        assertThat(executionLog.getExecutionControlMode()).isEqualTo(ExecutionControlMode.DRY_RUN);
        assertThat(executionLog.getWritePerformed()).isFalse();
        assertThat(executionLog.getWriteSkipReason()).isEqualTo(GitHubWriteSkipReason.DRY_RUN);
        assertThat(executionLog.getSelectionApplied()).isTrue();
        assertThat(executionLog.getSelectedPathsSummary()).isEqualTo("src/Main.java|src/Test.java");
    }

    @DisplayName("comment execution 결과 성공 시 task runtime state와 webhook execution을 완료 상태로 기록한다.")
    @Test
    void recordExecutionResult_marksTaskAndRunAsDone() {
        // given
        AgentRuntimeRepository repository = mock(AgentRuntimeRepository.class);
        TaskRuntimeState existingTask = TaskRuntimeState.of(
                "PR_REVIEW:sonic8-8:agile-runner#12",
                12L,
                "GitHub PR review for sonic8-8/agile-runner#12",
                TaskRuntimeStatus.IN_PROGRESS,
                1,
                null,
                LocalDateTime.of(2026, 4, 2, 10, 0),
                null
        );
        when(repository.findTaskRuntimeState("PR_REVIEW:sonic8-8:agile-runner#12")).thenReturn(Optional.of(existingTask));
        AgentRuntimeService service = new AgentRuntimeService(repository, new ObjectMapper());
        WebhookExecution webhookExecution = WebhookExecution.start(
                "EXECUTION:delivery-12",
                "PR_REVIEW:sonic8-8:agile-runner#12",
                "delivery-12",
                "sonic8-8/agile-runner",
                12,
                "PULL_REQUEST",
                "synchronize",
                LocalDateTime.of(2026, 4, 2, 10, 1)
        );
        GitHubCommentExecutionResult executionResult = GitHubCommentExecutionResult.written(
                ExecutionControlMode.NORMAL,
                0,
                GitHubCommentResponse.of(123L, "https://github.com/comment/123", List.of(), "ok")
        );

        // when
        service.recordExecutionResult(webhookExecution, executionResult);

        // then
        ArgumentCaptor<TaskRuntimeState> taskCaptor = ArgumentCaptor.forClass(TaskRuntimeState.class);
        ArgumentCaptor<WebhookExecution> webhookExecutionCaptor = ArgumentCaptor.forClass(WebhookExecution.class);
        ArgumentCaptor<AgentExecutionLog> logCaptor = ArgumentCaptor.forClass(AgentExecutionLog.class);
        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<ValidationCriteria>> criteriaCaptor = ArgumentCaptor.forClass(List.class);

        verify(repository).replaceValidationCriteria(eq("PR_REVIEW:sonic8-8:agile-runner#12"), criteriaCaptor.capture());
        verify(repository).upsertWebhookExecution(webhookExecutionCaptor.capture());
        verify(repository).upsertTaskRuntimeState(taskCaptor.capture());
        verify(repository).appendExecutionLog(logCaptor.capture());

        TaskRuntimeState savedTask = taskCaptor.getValue();
        assertThat(savedTask.getStatus()).isEqualTo(TaskRuntimeStatus.DONE);
        assertThat(savedTask.getRetryCount()).isEqualTo(1);
        assertThat(savedTask.getStartedAt()).isEqualTo(LocalDateTime.of(2026, 4, 2, 10, 0));
        assertThat(savedTask.getFinishedAt()).isNotNull();

        WebhookExecution savedRun = webhookExecutionCaptor.getValue();
        assertThat(savedRun.getStatus()).isEqualTo(WebhookExecutionStatus.SUCCEEDED);
        assertThat(savedRun.getFinishedAt()).isNotNull();
        assertThat(savedRun.getErrorCode()).isNull();
        assertThat(savedRun.getExecutionControlMode()).isEqualTo(ExecutionControlMode.NORMAL);
        assertThat(savedRun.getWritePerformed()).isTrue();
        assertThat(savedRun.getWriteSkipReason()).isNull();

        AgentExecutionLog executionLog = logCaptor.getValue();
        assertThat(executionLog.getStepName()).isEqualTo(AgentRuntimeService.STEP_COMMENT_POSTED);
        assertThat(executionLog.getStatus()).isEqualTo(AgentExecutionStatus.SUCCEEDED);
        assertThat(executionLog.getErrorCode()).isNull();
        assertThat(executionLog.getExecutionControlMode()).isEqualTo(ExecutionControlMode.NORMAL);
        assertThat(executionLog.getWritePerformed()).isTrue();
        assertThat(executionLog.getWriteSkipReason()).isNull();

        List<ValidationCriteria> criteria = criteriaCaptor.getValue();
        assertThat(criteria).allMatch(criterion -> criterion.getStatus() == CriteriaStatus.PASSED);
    }

    @DisplayName("normal execution result를 기록할 때 실행 제어 모드와 write 수행 여부를 함께 저장한다.")
    @Test
    void recordExecutionResult_recordsExecutionControlEvidence() {
        // given
        AgentRuntimeRepository repository = mock(AgentRuntimeRepository.class);
        TaskRuntimeState existingTask = TaskRuntimeState.of(
                "PR_REVIEW:sonic8-8:agile-runner#14",
                14L,
                "GitHub PR review for sonic8-8/agile-runner#14",
                TaskRuntimeStatus.IN_PROGRESS,
                0,
                null,
                LocalDateTime.of(2026, 4, 7, 18, 0),
                null
        );
        when(repository.findTaskRuntimeState("PR_REVIEW:sonic8-8:agile-runner#14")).thenReturn(Optional.of(existingTask));
        AgentRuntimeService service = new AgentRuntimeService(repository, new ObjectMapper());
        WebhookExecution webhookExecution = WebhookExecution.start(
                "EXECUTION:delivery-14",
                "PR_REVIEW:sonic8-8:agile-runner#14",
                "delivery-14",
                "sonic8-8/agile-runner",
                14,
                "PULL_REQUEST",
                "opened",
                LocalDateTime.of(2026, 4, 7, 18, 1)
        );
        GitHubCommentExecutionResult executionResult = GitHubCommentExecutionResult.written(
                ExecutionControlMode.NORMAL,
                1,
                GitHubCommentResponse.of(140L, "https://github.com/comment/140", List.of(), "ok")
        );

        // when
        service.recordExecutionResult(webhookExecution, executionResult);

        // then
        ArgumentCaptor<WebhookExecution> webhookExecutionCaptor = ArgumentCaptor.forClass(WebhookExecution.class);
        ArgumentCaptor<AgentExecutionLog> logCaptor = ArgumentCaptor.forClass(AgentExecutionLog.class);

        verify(repository).upsertWebhookExecution(webhookExecutionCaptor.capture());
        verify(repository).appendExecutionLog(logCaptor.capture());

        WebhookExecution savedExecution = webhookExecutionCaptor.getValue();
        assertThat(savedExecution.getExecutionControlMode()).isEqualTo(ExecutionControlMode.NORMAL);
        assertThat(savedExecution.getWritePerformed()).isTrue();
        assertThat(savedExecution.getWriteSkipReason()).isNull();

        AgentExecutionLog executionLog = logCaptor.getValue();
        assertThat(executionLog.getExecutionControlMode()).isEqualTo(ExecutionControlMode.NORMAL);
        assertThat(executionLog.getWritePerformed()).isTrue();
        assertThat(executionLog.getWriteSkipReason()).isNull();
    }

    @DisplayName("dry-run skipped result를 기록할 때 write 생략 이유를 함께 저장한다.")
    @Test
    void recordExecutionResult_recordsWriteSkipReasonForDryRun() {
        // given
        AgentRuntimeRepository repository = mock(AgentRuntimeRepository.class);
        TaskRuntimeState existingTask = TaskRuntimeState.of(
                "PR_REVIEW:sonic8-8:agile-runner#15",
                15L,
                "GitHub PR review for sonic8-8/agile-runner#15",
                TaskRuntimeStatus.IN_PROGRESS,
                0,
                null,
                LocalDateTime.of(2026, 4, 7, 18, 10),
                null
        );
        when(repository.findTaskRuntimeState("PR_REVIEW:sonic8-8:agile-runner#15")).thenReturn(Optional.of(existingTask));
        AgentRuntimeService service = new AgentRuntimeService(repository, new ObjectMapper());
        WebhookExecution webhookExecution = WebhookExecution.start(
                "EXECUTION:delivery-15",
                "PR_REVIEW:sonic8-8:agile-runner#15",
                "delivery-15",
                "sonic8-8/agile-runner",
                15,
                "PULL_REQUEST",
                "opened",
                LocalDateTime.of(2026, 4, 7, 18, 11)
        );
        GitHubCommentExecutionResult executionResult = GitHubCommentExecutionResult.skipped(
                ExecutionControlMode.DRY_RUN,
                GitHubWriteSkipReason.DRY_RUN,
                2
        );

        // when
        service.recordExecutionResult(webhookExecution, executionResult);

        // then
        ArgumentCaptor<WebhookExecution> webhookExecutionCaptor = ArgumentCaptor.forClass(WebhookExecution.class);
        ArgumentCaptor<AgentExecutionLog> logCaptor = ArgumentCaptor.forClass(AgentExecutionLog.class);

        verify(repository).upsertWebhookExecution(webhookExecutionCaptor.capture());
        verify(repository).appendExecutionLog(logCaptor.capture());

        WebhookExecution savedExecution = webhookExecutionCaptor.getValue();
        assertThat(savedExecution.getExecutionControlMode()).isEqualTo(ExecutionControlMode.DRY_RUN);
        assertThat(savedExecution.getWritePerformed()).isFalse();
        assertThat(savedExecution.getWriteSkipReason()).isEqualTo(GitHubWriteSkipReason.DRY_RUN);

        AgentExecutionLog executionLog = logCaptor.getValue();
        assertThat(executionLog.getExecutionControlMode()).isEqualTo(ExecutionControlMode.DRY_RUN);
        assertThat(executionLog.getWritePerformed()).isFalse();
        assertThat(executionLog.getWriteSkipReason()).isEqualTo(GitHubWriteSkipReason.DRY_RUN);
    }

    @DisplayName("review generation 실패 시 task runtime state와 webhook execution을 오류 코드와 대응 분류와 함께 실패 상태로 기록한다.")
    @Test
    void recordFailure_recordsFailureDispositionOnFailedExecutionEvidence() {
        // given
        AgentRuntimeRepository repository = mock(AgentRuntimeRepository.class);
        when(repository.findTaskRuntimeState("PR_REVIEW:sonic8-8:agile-runner#13"))
                .thenReturn(Optional.of(TaskRuntimeState.of(
                        "PR_REVIEW:sonic8-8:agile-runner#13",
                        13L,
                        "GitHub PR review for sonic8-8/agile-runner#13",
                        TaskRuntimeStatus.IN_PROGRESS,
                        0,
                        null,
                        LocalDateTime.of(2026, 4, 2, 11, 0),
                        null
                )));
        AgentRuntimeService service = new AgentRuntimeService(repository, new ObjectMapper());
        WebhookExecution webhookExecution = WebhookExecution.start(
                "EXECUTION:delivery-13",
                "PR_REVIEW:sonic8-8:agile-runner#13",
                "delivery-13",
                "sonic8-8/agile-runner",
                13,
                "PULL_REQUEST",
                "synchronize",
                LocalDateTime.of(2026, 4, 2, 11, 1)
        );

        // when
        service.recordFailure(
                webhookExecution,
                AgentRuntimeService.STEP_REVIEW_GENERATED,
                new AgileRunnerException(ErrorCode.OPENAI_REVIEW_FAILED, "openai failed")
        );

        // then
        ArgumentCaptor<TaskRuntimeState> taskCaptor = ArgumentCaptor.forClass(TaskRuntimeState.class);
        ArgumentCaptor<WebhookExecution> webhookExecutionCaptor = ArgumentCaptor.forClass(WebhookExecution.class);
        ArgumentCaptor<AgentExecutionLog> logCaptor = ArgumentCaptor.forClass(AgentExecutionLog.class);
        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<ValidationCriteria>> criteriaCaptor = ArgumentCaptor.forClass(List.class);

        verify(repository).replaceValidationCriteria(eq("PR_REVIEW:sonic8-8:agile-runner#13"), criteriaCaptor.capture());
        verify(repository).upsertWebhookExecution(webhookExecutionCaptor.capture());
        verify(repository).upsertTaskRuntimeState(taskCaptor.capture());
        verify(repository).appendExecutionLog(logCaptor.capture());

        TaskRuntimeState savedTask = taskCaptor.getValue();
        assertThat(savedTask.getStatus()).isEqualTo(TaskRuntimeStatus.FAILED);
        assertThat(savedTask.getFinishedAt()).isNotNull();

        WebhookExecution savedRun = webhookExecutionCaptor.getValue();
        assertThat(savedRun.getStatus()).isEqualTo(WebhookExecutionStatus.FAILED);
        assertThat(savedRun.getErrorMessage()).isEqualTo("openai failed");
        assertThat(savedRun.getErrorCode()).isEqualTo(ErrorCode.OPENAI_REVIEW_FAILED);
        assertThat(savedRun.getFailureDisposition()).isEqualTo(FailureDisposition.RETRYABLE);

        List<ValidationCriteria> criteria = criteriaCaptor.getValue();
        assertThat(criteria.get(1).getStatus()).isEqualTo(CriteriaStatus.FAILED);
        assertThat(criteria.get(2).getStatus()).isEqualTo(CriteriaStatus.PENDING);

        AgentExecutionLog executionLog = logCaptor.getValue();
        assertThat(executionLog.getStepName()).isEqualTo(AgentRuntimeService.STEP_REVIEW_GENERATED);
        assertThat(executionLog.getStatus()).isEqualTo(AgentExecutionStatus.FAILED);
        assertThat(executionLog.getErrorMessage()).contains("openai failed");
        assertThat(executionLog.getErrorCode()).isEqualTo(ErrorCode.OPENAI_REVIEW_FAILED);
        assertThat(executionLog.getFailureDisposition()).isEqualTo(FailureDisposition.RETRYABLE);
    }

    private Map<String, Object> buildPayload(String repositoryName, int pullRequestNumber, String action) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("action", action);
        payload.put("repository", Map.of("full_name", repositoryName));
        payload.put("pull_request", Map.of("number", pullRequestNumber));
        return payload;
    }
}
