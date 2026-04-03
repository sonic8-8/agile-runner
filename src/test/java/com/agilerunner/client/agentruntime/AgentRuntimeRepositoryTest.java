package com.agilerunner.client.agentruntime;

import com.agilerunner.config.AgentRuntimeDataSourceConfig;
import com.agilerunner.domain.agentruntime.AgentExecutionLog;
import com.agilerunner.domain.agentruntime.AgentExecutionStatus;
import com.agilerunner.domain.agentruntime.AgentRole;
import com.agilerunner.domain.agentruntime.CriteriaCategory;
import com.agilerunner.domain.agentruntime.CriteriaStatus;
import com.agilerunner.domain.agentruntime.ValidationCriteria;
import com.agilerunner.domain.agentruntime.WebhookExecution;
import com.agilerunner.domain.agentruntime.WebhookExecutionStatus;
import com.agilerunner.domain.agentruntime.TaskRuntimeState;
import com.agilerunner.domain.agentruntime.TaskRuntimeStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class AgentRuntimeRepositoryTest {
    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withUserConfiguration(AgentRuntimeDataSourceConfig.class, AgentRuntimeRepository.class)
            .withPropertyValues(
                    "agile-runner.agent-runtime.enabled=true",
                    "agile-runner.agent-runtime.datasource.url=jdbc:h2:mem:agent-runtime;MODE=PostgreSQL;DB_CLOSE_DELAY=-1",
                    "agile-runner.agent-runtime.datasource.driver-class-name=org.h2.Driver",
                    "agile-runner.agent-runtime.datasource.username=sa",
                    "agile-runner.agent-runtime.datasource.password="
            );

    @DisplayName("task runtime state를 저장하고 다시 조회할 수 있다.")
    @Test
    void upsertTaskRuntimeState_andFind() {
        contextRunner.run(context -> {
            // given
            AgentRuntimeRepository repository = context.getBean(AgentRuntimeRepository.class);
            NamedParameterJdbcTemplate jdbcTemplate = context.getBean(NamedParameterJdbcTemplate.class);
            TaskRuntimeState taskRuntimeState = TaskRuntimeState.of(
                    "TASK-101",
                    101L,
                    "agent runtime schema bootstrap",
                    TaskRuntimeStatus.IN_PROGRESS,
                    1,
                    AgentRole.ORCHESTRATOR,
                    LocalDateTime.of(2026, 4, 2, 12, 0),
                    null
            );

            // when
            repository.upsertTaskRuntimeState(taskRuntimeState);
            Optional<TaskRuntimeState> found = repository.findTaskRuntimeState("TASK-101");
            Integer storedRows = jdbcTemplate.getJdbcTemplate()
                    .queryForObject(
                            "SELECT COUNT(*) FROM TASK_RUNTIME_STATE WHERE task_key = 'TASK-101'",
                            Integer.class
                    );

            // then
            assertThat(found).isPresent();
            assertThat(found.get().getIssueNumber()).isEqualTo(101L);
            assertThat(found.get().getStatus()).isEqualTo(TaskRuntimeStatus.IN_PROGRESS);
            assertThat(found.get().getOwnerRole()).isEqualTo(AgentRole.ORCHESTRATOR);
            assertThat(storedRows).isEqualTo(1);
        });
    }

    @DisplayName("validation criteria를 교체 저장하고 다시 조회할 수 있다.")
    @Test
    void replaceValidationCriteria_andFind() {
        contextRunner.run(context -> {
            // given
            AgentRuntimeRepository repository = context.getBean(AgentRuntimeRepository.class);
            NamedParameterJdbcTemplate jdbcTemplate = context.getBean(NamedParameterJdbcTemplate.class);
            List<ValidationCriteria> criteria = List.of(
                    ValidationCriteria.of(
                            "TASK-102",
                            "C1",
                            CriteriaCategory.REQUIRED,
                            "task runtime state가 저장된다.",
                            CriteriaStatus.PENDING,
                            null
                    ),
                    ValidationCriteria.of(
                            "TASK-102",
                            "O1",
                            CriteriaCategory.OPTIONAL,
                            "추가 아티팩트가 저장된다.",
                            CriteriaStatus.PENDING,
                            null
                    )
            );

            // when
            repository.replaceValidationCriteria("TASK-102", criteria);
            List<ValidationCriteria> found = repository.findValidationCriteria("TASK-102");
            Integer storedRows = jdbcTemplate.getJdbcTemplate()
                    .queryForObject(
                            "SELECT COUNT(*) FROM VALIDATION_CRITERIA WHERE task_key = 'TASK-102'",
                            Integer.class
                    );

            // then
            assertThat(found).hasSize(2);
            assertThat(found.getFirst().getCriteriaKey()).isEqualTo("C1");
            assertThat(found.getFirst().getCategory()).isEqualTo(CriteriaCategory.REQUIRED);
            assertThat(storedRows).isEqualTo(2);
        });
    }

    @DisplayName("agent execution log를 누적 저장하고 다시 조회할 수 있다.")
    @Test
    void appendExecutionLog_andFind() {
        contextRunner.run(context -> {
            // given
            AgentRuntimeRepository repository = context.getBean(AgentRuntimeRepository.class);
            NamedParameterJdbcTemplate jdbcTemplate = context.getBean(NamedParameterJdbcTemplate.class);
            AgentExecutionLog executionLog = AgentExecutionLog.of(
                    "TASK-103",
                    103L,
                    "EXECUTION-103",
                    AgentRole.TESTER,
                    "acceptance-test",
                    AgentExecutionStatus.FAILED,
                    "spec 기반 외부 테스트 실행",
                    "실패 테스트 2건",
                    "assertion mismatch",
                    "{\"failed\":2}",
                    LocalDateTime.of(2026, 4, 2, 13, 0),
                    LocalDateTime.of(2026, 4, 2, 13, 1)
            );

            // when
            repository.appendExecutionLog(executionLog);
            List<AgentExecutionLog> found = repository.findExecutionLogs("TASK-103");
            String storedExecutionKey = jdbcTemplate.getJdbcTemplate()
                    .queryForObject(
                            "SELECT execution_key FROM AGENT_EXECUTION_LOG WHERE task_key = 'TASK-103'",
                            String.class
                    );

            // then
            assertThat(found).hasSize(1);
            assertThat(found.getFirst().getExecutionKey()).isEqualTo("EXECUTION-103");
            assertThat(found.getFirst().getAgentRole()).isEqualTo(AgentRole.TESTER);
            assertThat(found.getFirst().getStatus()).isEqualTo(AgentExecutionStatus.FAILED);
            assertThat(found.getFirst().getPayloadJson()).isEqualTo("{\"failed\":2}");
            assertThat(storedExecutionKey).isEqualTo("EXECUTION-103");
        });
    }

    @DisplayName("webhook execution을 저장하고 다시 조회할 수 있다.")
    @Test
    void upsertWebhookExecution_andFind() {
        contextRunner.run(context -> {
            // given
            AgentRuntimeRepository repository = context.getBean(AgentRuntimeRepository.class);
            NamedParameterJdbcTemplate jdbcTemplate = context.getBean(NamedParameterJdbcTemplate.class);
            WebhookExecution webhookExecution = WebhookExecution.start(
                    "EXECUTION:201",
                    "TASK-201",
                    "delivery-201",
                    "sonic8-8/agile-runner",
                    201,
                    "PULL_REQUEST",
                    "synchronize",
                    LocalDateTime.of(2026, 4, 2, 14, 0)
            ).complete(
                    WebhookExecutionStatus.SUCCEEDED,
                    null,
                    LocalDateTime.of(2026, 4, 2, 14, 3)
            );

            // when
            repository.upsertWebhookExecution(webhookExecution);
            Optional<WebhookExecution> found = repository.findWebhookExecution("EXECUTION:201");
            String storedStatus = jdbcTemplate.getJdbcTemplate()
                    .queryForObject(
                            "SELECT status FROM WEBHOOK_EXECUTION WHERE execution_key = 'EXECUTION:201'",
                            String.class
                    );

            // then
            assertThat(found).isPresent();
            assertThat(found.get().getTaskKey()).isEqualTo("TASK-201");
            assertThat(found.get().getDeliveryId()).isEqualTo("delivery-201");
            assertThat(found.get().getStatus()).isEqualTo(WebhookExecutionStatus.SUCCEEDED);
            assertThat(storedStatus).isEqualTo("SUCCEEDED");
        });
    }
}
