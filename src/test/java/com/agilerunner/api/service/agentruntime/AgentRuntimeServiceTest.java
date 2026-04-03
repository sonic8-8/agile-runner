package com.agilerunner.api.service.agentruntime;

import com.agilerunner.api.service.dto.GitHubCommentResponse;
import com.agilerunner.api.service.dto.GitHubEventServiceRequest;
import com.agilerunner.client.agentruntime.AgentRuntimeRepository;
import com.agilerunner.domain.agentruntime.AgentExecutionLog;
import com.agilerunner.domain.agentruntime.AgentExecutionStatus;
import com.agilerunner.domain.agentruntime.CriteriaStatus;
import com.agilerunner.domain.agentruntime.EvaluationCriteria;
import com.agilerunner.domain.agentruntime.ReviewRun;
import com.agilerunner.domain.agentruntime.ReviewRunStatus;
import com.agilerunner.domain.agentruntime.TaskState;
import com.agilerunner.domain.agentruntime.TaskStateStatus;
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

    @DisplayName("review run 시작 시 task, criteria, run, 실행 로그를 함께 저장한다.")
    @Test
    void startReviewRun_persistsTaskRunAndCriteria() {
        // given
        AgentRuntimeRepository repository = mock(AgentRuntimeRepository.class);
        when(repository.findTaskState("PR_REVIEW:sonic8-8:agile-runner#11")).thenReturn(Optional.empty());
        AgentRuntimeService service = new AgentRuntimeService(repository, new ObjectMapper());
        GitHubEventServiceRequest request = GitHubEventServiceRequest.of(PULL_REQUEST, buildPayload("sonic8-8/agile-runner", 11, "opened"), 999L);

        // when
        ReviewRun reviewRun = service.startReviewRun("delivery-11", request);

        // then
        ArgumentCaptor<TaskState> taskCaptor = ArgumentCaptor.forClass(TaskState.class);
        ArgumentCaptor<ReviewRun> reviewRunCaptor = ArgumentCaptor.forClass(ReviewRun.class);
        ArgumentCaptor<AgentExecutionLog> logCaptor = ArgumentCaptor.forClass(AgentExecutionLog.class);
        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<EvaluationCriteria>> criteriaCaptor = ArgumentCaptor.forClass(List.class);

        verify(repository).upsertTaskState(taskCaptor.capture());
        verify(repository).replaceEvaluationCriteria(eq("PR_REVIEW:sonic8-8:agile-runner#11"), criteriaCaptor.capture());
        verify(repository).upsertReviewRun(reviewRunCaptor.capture());
        verify(repository).appendExecutionLog(logCaptor.capture());

        TaskState savedTask = taskCaptor.getValue();
        assertThat(savedTask.getStatus()).isEqualTo(TaskStateStatus.IN_PROGRESS);
        assertThat(savedTask.getIssueNumber()).isEqualTo(11L);
        assertThat(savedTask.getTitle()).isEqualTo("GitHub PR review for sonic8-8/agile-runner#11");

        List<EvaluationCriteria> criteria = criteriaCaptor.getValue();
        assertThat(criteria).hasSize(3);
        assertThat(criteria.get(0).getStatus()).isEqualTo(CriteriaStatus.PASSED);
        assertThat(criteria.get(1).getStatus()).isEqualTo(CriteriaStatus.PENDING);
        assertThat(criteria.get(2).getStatus()).isEqualTo(CriteriaStatus.PENDING);

        ReviewRun savedRun = reviewRunCaptor.getValue();
        assertThat(savedRun.getRunKey()).isEqualTo("RUN:delivery-11");
        assertThat(savedRun.getStatus()).isEqualTo(ReviewRunStatus.STARTED);

        AgentExecutionLog executionLog = logCaptor.getValue();
        assertThat(executionLog.getRunKey()).isEqualTo("RUN:delivery-11");
        assertThat(executionLog.getStepName()).isEqualTo(AgentRuntimeService.STEP_WEBHOOK_ACCEPTED);
        assertThat(executionLog.getStatus()).isEqualTo(AgentExecutionStatus.SUCCEEDED);

        assertThat(reviewRun.getTaskKey()).isEqualTo("PR_REVIEW:sonic8-8:agile-runner#11");
    }

    @DisplayName("comment posting 성공 시 task와 run을 완료 상태로 기록한다.")
    @Test
    void recordCommentPosted_marksTaskAndRunAsDone() {
        // given
        AgentRuntimeRepository repository = mock(AgentRuntimeRepository.class);
        TaskState existingTask = TaskState.of(
                "PR_REVIEW:sonic8-8:agile-runner#12",
                12L,
                "GitHub PR review for sonic8-8/agile-runner#12",
                TaskStateStatus.IN_PROGRESS,
                1,
                null,
                LocalDateTime.of(2026, 4, 2, 10, 0),
                null
        );
        when(repository.findTaskState("PR_REVIEW:sonic8-8:agile-runner#12")).thenReturn(Optional.of(existingTask));
        AgentRuntimeService service = new AgentRuntimeService(repository, new ObjectMapper());
        ReviewRun reviewRun = ReviewRun.start(
                "RUN:delivery-12",
                "PR_REVIEW:sonic8-8:agile-runner#12",
                "delivery-12",
                "sonic8-8/agile-runner",
                12,
                "PULL_REQUEST",
                "synchronize",
                LocalDateTime.of(2026, 4, 2, 10, 1)
        );
        GitHubCommentResponse response = GitHubCommentResponse.of(123L, "https://github.com/comment/123", List.of(), "ok");

        // when
        service.recordCommentPosted(reviewRun, response);

        // then
        ArgumentCaptor<TaskState> taskCaptor = ArgumentCaptor.forClass(TaskState.class);
        ArgumentCaptor<ReviewRun> reviewRunCaptor = ArgumentCaptor.forClass(ReviewRun.class);
        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<EvaluationCriteria>> criteriaCaptor = ArgumentCaptor.forClass(List.class);

        verify(repository).replaceEvaluationCriteria(eq("PR_REVIEW:sonic8-8:agile-runner#12"), criteriaCaptor.capture());
        verify(repository).upsertReviewRun(reviewRunCaptor.capture());
        verify(repository).upsertTaskState(taskCaptor.capture());

        TaskState savedTask = taskCaptor.getValue();
        assertThat(savedTask.getStatus()).isEqualTo(TaskStateStatus.DONE);
        assertThat(savedTask.getRetryCount()).isEqualTo(1);
        assertThat(savedTask.getStartedAt()).isEqualTo(LocalDateTime.of(2026, 4, 2, 10, 0));
        assertThat(savedTask.getFinishedAt()).isNotNull();

        ReviewRun savedRun = reviewRunCaptor.getValue();
        assertThat(savedRun.getStatus()).isEqualTo(ReviewRunStatus.SUCCEEDED);
        assertThat(savedRun.getFinishedAt()).isNotNull();

        List<EvaluationCriteria> criteria = criteriaCaptor.getValue();
        assertThat(criteria).allMatch(criterion -> criterion.getStatus() == CriteriaStatus.PASSED);
    }

    @DisplayName("review generation 실패 시 task와 run을 실패 상태로 기록한다.")
    @Test
    void recordFailure_marksTaskAndRunAsFailed() {
        // given
        AgentRuntimeRepository repository = mock(AgentRuntimeRepository.class);
        when(repository.findTaskState("PR_REVIEW:sonic8-8:agile-runner#13"))
                .thenReturn(Optional.of(TaskState.of(
                        "PR_REVIEW:sonic8-8:agile-runner#13",
                        13L,
                        "GitHub PR review for sonic8-8/agile-runner#13",
                        TaskStateStatus.IN_PROGRESS,
                        0,
                        null,
                        LocalDateTime.of(2026, 4, 2, 11, 0),
                        null
                )));
        AgentRuntimeService service = new AgentRuntimeService(repository, new ObjectMapper());
        ReviewRun reviewRun = ReviewRun.start(
                "RUN:delivery-13",
                "PR_REVIEW:sonic8-8:agile-runner#13",
                "delivery-13",
                "sonic8-8/agile-runner",
                13,
                "PULL_REQUEST",
                "synchronize",
                LocalDateTime.of(2026, 4, 2, 11, 1)
        );

        // when
        service.recordFailure(reviewRun, AgentRuntimeService.STEP_REVIEW_GENERATED, new RuntimeException("openai failed"));

        // then
        ArgumentCaptor<TaskState> taskCaptor = ArgumentCaptor.forClass(TaskState.class);
        ArgumentCaptor<ReviewRun> reviewRunCaptor = ArgumentCaptor.forClass(ReviewRun.class);
        ArgumentCaptor<AgentExecutionLog> logCaptor = ArgumentCaptor.forClass(AgentExecutionLog.class);
        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<EvaluationCriteria>> criteriaCaptor = ArgumentCaptor.forClass(List.class);

        verify(repository).replaceEvaluationCriteria(eq("PR_REVIEW:sonic8-8:agile-runner#13"), criteriaCaptor.capture());
        verify(repository).upsertReviewRun(reviewRunCaptor.capture());
        verify(repository).upsertTaskState(taskCaptor.capture());
        verify(repository).appendExecutionLog(logCaptor.capture());

        TaskState savedTask = taskCaptor.getValue();
        assertThat(savedTask.getStatus()).isEqualTo(TaskStateStatus.FAILED);
        assertThat(savedTask.getFinishedAt()).isNotNull();

        ReviewRun savedRun = reviewRunCaptor.getValue();
        assertThat(savedRun.getStatus()).isEqualTo(ReviewRunStatus.FAILED);
        assertThat(savedRun.getErrorMessage()).isEqualTo("openai failed");

        List<EvaluationCriteria> criteria = criteriaCaptor.getValue();
        assertThat(criteria.get(1).getStatus()).isEqualTo(CriteriaStatus.FAILED);
        assertThat(criteria.get(2).getStatus()).isEqualTo(CriteriaStatus.PENDING);

        AgentExecutionLog executionLog = logCaptor.getValue();
        assertThat(executionLog.getStepName()).isEqualTo(AgentRuntimeService.STEP_REVIEW_GENERATED);
        assertThat(executionLog.getStatus()).isEqualTo(AgentExecutionStatus.FAILED);
        assertThat(executionLog.getErrorMessage()).contains("openai failed");
    }

    private Map<String, Object> buildPayload(String repositoryName, int pullRequestNumber, String action) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("action", action);
        payload.put("repository", Map.of("full_name", repositoryName));
        payload.put("pull_request", Map.of("number", pullRequestNumber));
        return payload;
    }
}
