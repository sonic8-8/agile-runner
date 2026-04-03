package com.agilerunner.api.service.agentruntime;

import com.agilerunner.api.service.dto.GitHubCommentResponse;
import com.agilerunner.api.service.dto.GitHubEventServiceRequest;
import com.agilerunner.client.agentruntime.AgentRuntimeRepository;
import com.agilerunner.domain.agentruntime.AgentExecutionLog;
import com.agilerunner.domain.agentruntime.AgentExecutionStatus;
import com.agilerunner.domain.agentruntime.CriteriaStatus;
import com.agilerunner.domain.agentruntime.ValidationCriteria;
import com.agilerunner.domain.agentruntime.WebhookExecution;
import com.agilerunner.domain.agentruntime.WebhookExecutionStatus;
import com.agilerunner.domain.agentruntime.TaskRuntimeState;
import com.agilerunner.domain.agentruntime.TaskRuntimeStatus;
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

    @DisplayName("comment posting 성공 시 task runtime state와 webhook execution을 완료 상태로 기록한다.")
    @Test
    void recordCommentPosted_marksTaskAndRunAsDone() {
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
        GitHubCommentResponse response = GitHubCommentResponse.of(123L, "https://github.com/comment/123", List.of(), "ok");

        // when
        service.recordCommentPosted(webhookExecution, response);

        // then
        ArgumentCaptor<TaskRuntimeState> taskCaptor = ArgumentCaptor.forClass(TaskRuntimeState.class);
        ArgumentCaptor<WebhookExecution> webhookExecutionCaptor = ArgumentCaptor.forClass(WebhookExecution.class);
        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<ValidationCriteria>> criteriaCaptor = ArgumentCaptor.forClass(List.class);

        verify(repository).replaceValidationCriteria(eq("PR_REVIEW:sonic8-8:agile-runner#12"), criteriaCaptor.capture());
        verify(repository).upsertWebhookExecution(webhookExecutionCaptor.capture());
        verify(repository).upsertTaskRuntimeState(taskCaptor.capture());

        TaskRuntimeState savedTask = taskCaptor.getValue();
        assertThat(savedTask.getStatus()).isEqualTo(TaskRuntimeStatus.DONE);
        assertThat(savedTask.getRetryCount()).isEqualTo(1);
        assertThat(savedTask.getStartedAt()).isEqualTo(LocalDateTime.of(2026, 4, 2, 10, 0));
        assertThat(savedTask.getFinishedAt()).isNotNull();

        WebhookExecution savedRun = webhookExecutionCaptor.getValue();
        assertThat(savedRun.getStatus()).isEqualTo(WebhookExecutionStatus.SUCCEEDED);
        assertThat(savedRun.getFinishedAt()).isNotNull();

        List<ValidationCriteria> criteria = criteriaCaptor.getValue();
        assertThat(criteria).allMatch(criterion -> criterion.getStatus() == CriteriaStatus.PASSED);
    }

    @DisplayName("review generation 실패 시 task runtime state와 webhook execution을 실패 상태로 기록한다.")
    @Test
    void recordFailure_marksTaskAndRunAsFailed() {
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
        service.recordFailure(webhookExecution, AgentRuntimeService.STEP_REVIEW_GENERATED, new RuntimeException("openai failed"));

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

        List<ValidationCriteria> criteria = criteriaCaptor.getValue();
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
