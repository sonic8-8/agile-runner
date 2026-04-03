package com.agilerunner.client.agentruntime;

import com.agilerunner.config.AgentRuntimeDataSourceConfig;
import com.agilerunner.domain.agentruntime.AgentExecutionLog;
import com.agilerunner.domain.agentruntime.AgentExecutionStatus;
import com.agilerunner.domain.agentruntime.AgentRole;
import com.agilerunner.domain.agentruntime.CriteriaCategory;
import com.agilerunner.domain.agentruntime.CriteriaStatus;
import com.agilerunner.domain.agentruntime.EvaluationCriteria;
import com.agilerunner.domain.agentruntime.ReviewRun;
import com.agilerunner.domain.agentruntime.ReviewRunStatus;
import com.agilerunner.domain.agentruntime.TaskState;
import com.agilerunner.domain.agentruntime.TaskStateStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

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

    @DisplayName("task state를 저장하고 다시 조회할 수 있다.")
    @Test
    void upsertTaskState_andFind() {
        contextRunner.run(context -> {
            // given
            AgentRuntimeRepository repository = context.getBean(AgentRuntimeRepository.class);
            TaskState taskState = TaskState.of(
                    "TASK-101",
                    101L,
                    "agent runtime schema bootstrap",
                    TaskStateStatus.IN_PROGRESS,
                    1,
                    AgentRole.ORCHESTRATOR,
                    LocalDateTime.of(2026, 4, 2, 12, 0),
                    null
            );

            // when
            repository.upsertTaskState(taskState);
            Optional<TaskState> found = repository.findTaskState("TASK-101");

            // then
            assertThat(found).isPresent();
            assertThat(found.get().getIssueNumber()).isEqualTo(101L);
            assertThat(found.get().getStatus()).isEqualTo(TaskStateStatus.IN_PROGRESS);
            assertThat(found.get().getOwnerRole()).isEqualTo(AgentRole.ORCHESTRATOR);
        });
    }

    @DisplayName("evaluation criteria를 교체 저장하고 다시 조회할 수 있다.")
    @Test
    void replaceEvaluationCriteria_andFind() {
        contextRunner.run(context -> {
            // given
            AgentRuntimeRepository repository = context.getBean(AgentRuntimeRepository.class);
            List<EvaluationCriteria> criteria = List.of(
                    EvaluationCriteria.of(
                            "TASK-102",
                            "C1",
                            CriteriaCategory.REQUIRED,
                            "task state가 저장된다.",
                            CriteriaStatus.PENDING,
                            null
                    ),
                    EvaluationCriteria.of(
                            "TASK-102",
                            "O1",
                            CriteriaCategory.OPTIONAL,
                            "추가 아티팩트가 저장된다.",
                            CriteriaStatus.PENDING,
                            null
                    )
            );

            // when
            repository.replaceEvaluationCriteria("TASK-102", criteria);
            List<EvaluationCriteria> found = repository.findEvaluationCriteria("TASK-102");

            // then
            assertThat(found).hasSize(2);
            assertThat(found.getFirst().getCriteriaKey()).isEqualTo("C1");
            assertThat(found.getFirst().getCategory()).isEqualTo(CriteriaCategory.REQUIRED);
        });
    }

    @DisplayName("agent execution log를 누적 저장하고 다시 조회할 수 있다.")
    @Test
    void appendExecutionLog_andFind() {
        contextRunner.run(context -> {
            // given
            AgentRuntimeRepository repository = context.getBean(AgentRuntimeRepository.class);
            AgentExecutionLog executionLog = AgentExecutionLog.of(
                    "TASK-103",
                    103L,
                    "RUN-103",
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

            // then
            assertThat(found).hasSize(1);
            assertThat(found.getFirst().getRunKey()).isEqualTo("RUN-103");
            assertThat(found.getFirst().getAgentRole()).isEqualTo(AgentRole.TESTER);
            assertThat(found.getFirst().getStatus()).isEqualTo(AgentExecutionStatus.FAILED);
            assertThat(found.getFirst().getPayloadJson()).isEqualTo("{\"failed\":2}");
        });
    }

    @DisplayName("review run을 저장하고 다시 조회할 수 있다.")
    @Test
    void upsertReviewRun_andFind() {
        contextRunner.run(context -> {
            // given
            AgentRuntimeRepository repository = context.getBean(AgentRuntimeRepository.class);
            ReviewRun reviewRun = ReviewRun.start(
                    "RUN-201",
                    "TASK-201",
                    "delivery-201",
                    "sonic8-8/agile-runner",
                    201,
                    "PULL_REQUEST",
                    "synchronize",
                    LocalDateTime.of(2026, 4, 2, 14, 0)
            ).complete(
                    ReviewRunStatus.SUCCEEDED,
                    null,
                    LocalDateTime.of(2026, 4, 2, 14, 3)
            );

            // when
            repository.upsertReviewRun(reviewRun);
            Optional<ReviewRun> found = repository.findReviewRun("RUN-201");

            // then
            assertThat(found).isPresent();
            assertThat(found.get().getTaskKey()).isEqualTo("TASK-201");
            assertThat(found.get().getDeliveryId()).isEqualTo("delivery-201");
            assertThat(found.get().getStatus()).isEqualTo(ReviewRunStatus.SUCCEEDED);
        });
    }
}
