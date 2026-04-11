package com.agilerunner.client.agentruntime;

import com.agilerunner.config.AgentRuntimeDataSourceConfig;
import com.agilerunner.domain.agentruntime.AgentExecutionLog;
import com.agilerunner.domain.agentruntime.AgentExecutionStatus;
import com.agilerunner.domain.agentruntime.AgentRole;
import com.agilerunner.domain.agentruntime.CriteriaCategory;
import com.agilerunner.domain.agentruntime.CriteriaStatus;
import com.agilerunner.domain.agentruntime.ExecutionStartType;
import com.agilerunner.domain.agentruntime.ValidationCriteria;
import com.agilerunner.domain.agentruntime.WebhookExecution;
import com.agilerunner.domain.agentruntime.WebhookExecutionStatus;
import com.agilerunner.domain.agentruntime.TaskRuntimeState;
import com.agilerunner.domain.agentruntime.TaskRuntimeStatus;
import com.agilerunner.domain.executioncontrol.ExecutionControlMode;
import com.agilerunner.domain.executioncontrol.GitHubWriteSkipReason;
import com.agilerunner.domain.exception.ErrorCode;
import com.agilerunner.domain.exception.FailureDisposition;
import com.agilerunner.domain.review.ManualRerunControlAction;
import com.agilerunner.domain.review.ManualRerunControlActionAudit;
import com.agilerunner.domain.review.ManualRerunControlActionStatus;
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

    @DisplayName("agent execution log를 오류 코드와 대응 분류와 함께 누적 저장하고 다시 조회할 수 있다.")
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
                    ErrorCode.OPENAI_REVIEW_FAILED,
                    FailureDisposition.RETRYABLE,
                    "{\"failed\":2}",
                    LocalDateTime.of(2026, 4, 2, 13, 0),
                    LocalDateTime.of(2026, 4, 2, 13, 1)
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

            // when
            repository.appendExecutionLog(executionLog);
            List<AgentExecutionLog> found = repository.findExecutionLogs("TASK-103");
            String storedExecutionKey = jdbcTemplate.getJdbcTemplate()
                    .queryForObject(
                            "SELECT execution_key FROM AGENT_EXECUTION_LOG WHERE task_key = 'TASK-103'",
                            String.class
                    );
            String storedFailureDisposition = jdbcTemplate.getJdbcTemplate()
                    .queryForObject(
                            "SELECT failure_disposition FROM AGENT_EXECUTION_LOG WHERE task_key = 'TASK-103'",
                            String.class
                    );
            String storedExecutionControlMode = jdbcTemplate.getJdbcTemplate()
                    .queryForObject(
                            "SELECT execution_control_mode FROM AGENT_EXECUTION_LOG WHERE task_key = 'TASK-103'",
                            String.class
                    );
            String storedExecutionStartType = jdbcTemplate.getJdbcTemplate()
                    .queryForObject(
                            "SELECT execution_start_type FROM AGENT_EXECUTION_LOG WHERE task_key = 'TASK-103'",
                            String.class
                    );
            Boolean storedWritePerformed = jdbcTemplate.getJdbcTemplate()
                    .queryForObject(
                            "SELECT write_performed FROM AGENT_EXECUTION_LOG WHERE task_key = 'TASK-103'",
                            Boolean.class
                    );
            String storedWriteSkipReason = jdbcTemplate.getJdbcTemplate()
                    .queryForObject(
                            "SELECT write_skip_reason FROM AGENT_EXECUTION_LOG WHERE task_key = 'TASK-103'",
                            String.class
                    );
            Boolean storedSelectionApplied = jdbcTemplate.getJdbcTemplate()
                    .queryForObject(
                            "SELECT selection_applied FROM AGENT_EXECUTION_LOG WHERE task_key = 'TASK-103'",
                            Boolean.class
                    );
            String storedSelectedPathsSummary = jdbcTemplate.getJdbcTemplate()
                    .queryForObject(
                            "SELECT selected_paths_summary FROM AGENT_EXECUTION_LOG WHERE task_key = 'TASK-103'",
                            String.class
                    );

            // then
            assertThat(found).hasSize(1);
            assertThat(found.getFirst().getExecutionKey()).isEqualTo("EXECUTION-103");
            assertThat(found.getFirst().getAgentRole()).isEqualTo(AgentRole.TESTER);
            assertThat(found.getFirst().getStatus()).isEqualTo(AgentExecutionStatus.FAILED);
            assertThat(found.getFirst().getErrorCode()).isEqualTo(ErrorCode.OPENAI_REVIEW_FAILED);
            assertThat(found.getFirst().getFailureDisposition()).isEqualTo(FailureDisposition.RETRYABLE);
            assertThat(found.getFirst().getExecutionStartType()).isEqualTo(ExecutionStartType.MANUAL_RERUN);
            assertThat(found.getFirst().getExecutionControlMode()).isEqualTo(ExecutionControlMode.DRY_RUN);
            assertThat(found.getFirst().getWritePerformed()).isFalse();
            assertThat(found.getFirst().getWriteSkipReason()).isEqualTo(GitHubWriteSkipReason.DRY_RUN);
            assertThat(found.getFirst().getSelectionApplied()).isTrue();
            assertThat(found.getFirst().getSelectedPathsSummary()).isEqualTo("src/Main.java|src/Test.java");
            assertThat(found.getFirst().getPayloadJson()).isEqualTo("{\"failed\":2}");
            assertThat(storedExecutionKey).isEqualTo("EXECUTION-103");
            assertThat(storedFailureDisposition).isEqualTo("RETRYABLE");
            assertThat(storedExecutionStartType).isEqualTo("MANUAL_RERUN");
            assertThat(storedExecutionControlMode).isEqualTo("DRY_RUN");
            assertThat(storedWritePerformed).isFalse();
            assertThat(storedWriteSkipReason).isEqualTo("DRY_RUN");
            assertThat(storedSelectionApplied).isTrue();
            assertThat(storedSelectedPathsSummary).isEqualTo("src/Main.java|src/Test.java");
        });
    }

    @DisplayName("성공한 agent execution log는 오류 코드 null 상태로 저장하고 다시 조회할 수 있다.")
    @Test
    void appendExecutionLog_withNullErrorCode_andFind() {
        contextRunner.run(context -> {
            // given
            AgentRuntimeRepository repository = context.getBean(AgentRuntimeRepository.class);
            AgentExecutionLog executionLog = AgentExecutionLog.of(
                    "TASK-104",
                    104L,
                    "EXECUTION-104",
                    AgentRole.ORCHESTRATOR,
                    "comment-posted",
                    AgentExecutionStatus.SUCCEEDED,
                    "GitHub review comments posted",
                    "success",
                    null,
                    null,
                    "{\"posted\":true}",
                    LocalDateTime.of(2026, 4, 2, 13, 30),
                    LocalDateTime.of(2026, 4, 2, 13, 31)
            );

            // when
            repository.appendExecutionLog(executionLog);
            List<AgentExecutionLog> found = repository.findExecutionLogs("TASK-104");

            // then
            assertThat(found).hasSize(1);
            assertThat(found.getFirst().getStatus()).isEqualTo(AgentExecutionStatus.SUCCEEDED);
            assertThat(found.getFirst().getErrorCode()).isNull();
        });
    }

    @DisplayName("webhook execution을 오류 코드와 대응 분류와 함께 저장하고 다시 조회할 수 있다.")
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
            ).withExecutionStartType(
                    ExecutionStartType.MANUAL_RERUN
            ).withExecutionControl(
                    ExecutionControlMode.DRY_RUN,
                    false,
                    GitHubWriteSkipReason.DRY_RUN
            ).withSelectionScope(
                    true,
                    "src/Main.java|src/Test.java"
            ).complete(
                    WebhookExecutionStatus.FAILED,
                    "GitHub App ID missing",
                    ErrorCode.GITHUB_APP_CONFIGURATION_MISSING,
                    FailureDisposition.MANUAL_ACTION_REQUIRED,
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
            String storedErrorCode = jdbcTemplate.getJdbcTemplate()
                    .queryForObject(
                            "SELECT error_code FROM WEBHOOK_EXECUTION WHERE execution_key = 'EXECUTION:201'",
                            String.class
                    );
            String storedFailureDisposition = jdbcTemplate.getJdbcTemplate()
                    .queryForObject(
                            "SELECT failure_disposition FROM WEBHOOK_EXECUTION WHERE execution_key = 'EXECUTION:201'",
                            String.class
                    );
            String storedExecutionStartType = jdbcTemplate.getJdbcTemplate()
                    .queryForObject(
                            "SELECT execution_start_type FROM WEBHOOK_EXECUTION WHERE execution_key = 'EXECUTION:201'",
                            String.class
                    );
            String storedExecutionControlMode = jdbcTemplate.getJdbcTemplate()
                    .queryForObject(
                            "SELECT execution_control_mode FROM WEBHOOK_EXECUTION WHERE execution_key = 'EXECUTION:201'",
                            String.class
                    );
            Boolean storedWritePerformed = jdbcTemplate.getJdbcTemplate()
                    .queryForObject(
                            "SELECT write_performed FROM WEBHOOK_EXECUTION WHERE execution_key = 'EXECUTION:201'",
                            Boolean.class
                    );
            String storedWriteSkipReason = jdbcTemplate.getJdbcTemplate()
                    .queryForObject(
                            "SELECT write_skip_reason FROM WEBHOOK_EXECUTION WHERE execution_key = 'EXECUTION:201'",
                            String.class
                    );
            Boolean storedSelectionApplied = jdbcTemplate.getJdbcTemplate()
                    .queryForObject(
                            "SELECT selection_applied FROM WEBHOOK_EXECUTION WHERE execution_key = 'EXECUTION:201'",
                            Boolean.class
                    );
            String storedSelectedPathsSummary = jdbcTemplate.getJdbcTemplate()
                    .queryForObject(
                            "SELECT selected_paths_summary FROM WEBHOOK_EXECUTION WHERE execution_key = 'EXECUTION:201'",
                            String.class
                    );

            // then
            assertThat(found).isPresent();
            assertThat(found.get().getTaskKey()).isEqualTo("TASK-201");
            assertThat(found.get().getDeliveryId()).isEqualTo("delivery-201");
            assertThat(found.get().getStatus()).isEqualTo(WebhookExecutionStatus.FAILED);
            assertThat(found.get().getErrorCode()).isEqualTo(ErrorCode.GITHUB_APP_CONFIGURATION_MISSING);
            assertThat(found.get().getFailureDisposition()).isEqualTo(FailureDisposition.MANUAL_ACTION_REQUIRED);
            assertThat(found.get().getExecutionStartType()).isEqualTo(ExecutionStartType.MANUAL_RERUN);
            assertThat(found.get().getExecutionControlMode()).isEqualTo(ExecutionControlMode.DRY_RUN);
            assertThat(found.get().getWritePerformed()).isFalse();
            assertThat(found.get().getWriteSkipReason()).isEqualTo(GitHubWriteSkipReason.DRY_RUN);
            assertThat(found.get().getSelectionApplied()).isTrue();
            assertThat(found.get().getSelectedPathsSummary()).isEqualTo("src/Main.java|src/Test.java");
            assertThat(storedStatus).isEqualTo("FAILED");
            assertThat(storedErrorCode).isEqualTo("GITHUB_APP_CONFIGURATION_MISSING");
            assertThat(storedFailureDisposition).isEqualTo("MANUAL_ACTION_REQUIRED");
            assertThat(storedExecutionStartType).isEqualTo("MANUAL_RERUN");
            assertThat(storedExecutionControlMode).isEqualTo("DRY_RUN");
            assertThat(storedWritePerformed).isFalse();
            assertThat(storedWriteSkipReason).isEqualTo("DRY_RUN");
            assertThat(storedSelectionApplied).isTrue();
            assertThat(storedSelectedPathsSummary).isEqualTo("src/Main.java|src/Test.java");
        });
    }

    @DisplayName("manual rerun control action audit를 저장하고 마지막 applied action을 다시 조회할 수 있다.")
    @Test
    void appendManualRerunControlActionAudit_andFindLatestAppliedAction() {
        contextRunner.run(context -> {
            // given
            AgentRuntimeRepository repository = context.getBean(AgentRuntimeRepository.class);
            NamedParameterJdbcTemplate jdbcTemplate = context.getBean(NamedParameterJdbcTemplate.class);
            ManualRerunControlActionAudit acknowledgeAudit = ManualRerunControlActionAudit.applied(
                    "EXECUTION:MANUAL_RERUN:ack-1",
                    ManualRerunControlAction.ACKNOWLEDGE,
                    "운영자 확인 완료",
                    LocalDateTime.of(2026, 4, 10, 11, 0)
            );
            ManualRerunControlActionAudit unacknowledgeAudit = ManualRerunControlActionAudit.applied(
                    "EXECUTION:MANUAL_RERUN:ack-1",
                    ManualRerunControlAction.UNACKNOWLEDGE,
                    "운영자 확인 취소",
                    LocalDateTime.of(2026, 4, 10, 11, 1)
            );

            // when
            repository.appendManualRerunControlActionAudit(acknowledgeAudit);
            repository.appendManualRerunControlActionAudit(unacknowledgeAudit);
            Optional<ManualRerunControlAction> latestAction =
                    repository.findLatestAppliedManualRerunControlAction("EXECUTION:MANUAL_RERUN:ack-1");
            String storedAction = jdbcTemplate.getJdbcTemplate()
                    .queryForObject(
                            "SELECT action FROM MANUAL_RERUN_CONTROL_ACTION_AUDIT WHERE execution_key = 'EXECUTION:MANUAL_RERUN:ack-1' ORDER BY applied_at DESC, id DESC FETCH FIRST 1 ROW ONLY",
                            String.class
                    );
            String storedStatus = jdbcTemplate.getJdbcTemplate()
                    .queryForObject(
                            "SELECT action_status FROM MANUAL_RERUN_CONTROL_ACTION_AUDIT WHERE execution_key = 'EXECUTION:MANUAL_RERUN:ack-1' ORDER BY applied_at DESC, id DESC FETCH FIRST 1 ROW ONLY",
                            String.class
                    );

            // then
            assertThat(latestAction).contains(ManualRerunControlAction.UNACKNOWLEDGE);
            assertThat(storedAction).isEqualTo("UNACKNOWLEDGE");
            assertThat(storedStatus).isEqualTo("APPLIED");
        });
    }

    @DisplayName("manual rerun control action audit history를 저장하고 시간 순서대로 다시 조회할 수 있다.")
    @Test
    void appendManualRerunControlActionAudit_andFindHistory() {
        contextRunner.run(context -> {
            // given
            AgentRuntimeRepository repository = context.getBean(AgentRuntimeRepository.class);
            ManualRerunControlActionAudit acknowledgeAudit = ManualRerunControlActionAudit.applied(
                    "EXECUTION:MANUAL_RERUN:history-1",
                    ManualRerunControlAction.ACKNOWLEDGE,
                    "운영자 확인 완료",
                    LocalDateTime.of(2026, 4, 10, 11, 0)
            );
            ManualRerunControlActionAudit unacknowledgeAudit = ManualRerunControlActionAudit.applied(
                    "EXECUTION:MANUAL_RERUN:history-1",
                    ManualRerunControlAction.UNACKNOWLEDGE,
                    "운영자 확인 취소",
                    LocalDateTime.of(2026, 4, 10, 11, 1)
            );

            // when
            repository.appendManualRerunControlActionAudit(acknowledgeAudit);
            repository.appendManualRerunControlActionAudit(unacknowledgeAudit);
            List<ManualRerunControlActionAudit> history =
                    repository.findManualRerunControlActionAudits("EXECUTION:MANUAL_RERUN:history-1");

            // then
            assertThat(history).hasSize(2);
            assertThat(history.get(0).getAction()).isEqualTo(ManualRerunControlAction.ACKNOWLEDGE);
            assertThat(history.get(0).getActionStatus()).isEqualTo(ManualRerunControlActionStatus.APPLIED);
            assertThat(history.get(0).getNote()).isEqualTo("운영자 확인 완료");
            assertThat(history.get(0).getAppliedAt()).isEqualTo(LocalDateTime.of(2026, 4, 10, 11, 0));
            assertThat(history.get(1).getAction()).isEqualTo(ManualRerunControlAction.UNACKNOWLEDGE);
            assertThat(history.get(1).getActionStatus()).isEqualTo(ManualRerunControlActionStatus.APPLIED);
            assertThat(history.get(1).getNote()).isEqualTo("운영자 확인 취소");
            assertThat(history.get(1).getAppliedAt()).isEqualTo(LocalDateTime.of(2026, 4, 10, 11, 1));
        });
    }

    @DisplayName("manual rerun control action audit history는 action과 action status 필터에 맞는 row만 조회할 수 있다.")
    @Test
    void findManualRerunControlActionAudits_withFilters() {
        contextRunner.run(context -> {
            // given
            AgentRuntimeRepository repository = context.getBean(AgentRuntimeRepository.class);
            ManualRerunControlActionAudit acknowledgeAudit = ManualRerunControlActionAudit.applied(
                    "EXECUTION:MANUAL_RERUN:history-filter",
                    ManualRerunControlAction.ACKNOWLEDGE,
                    "운영자 확인 완료",
                    LocalDateTime.of(2026, 4, 10, 11, 0)
            );
            ManualRerunControlActionAudit unacknowledgeAudit = ManualRerunControlActionAudit.applied(
                    "EXECUTION:MANUAL_RERUN:history-filter",
                    ManualRerunControlAction.UNACKNOWLEDGE,
                    "운영자 확인 취소",
                    LocalDateTime.of(2026, 4, 10, 11, 1)
            );

            repository.appendManualRerunControlActionAudit(acknowledgeAudit);
            repository.appendManualRerunControlActionAudit(unacknowledgeAudit);

            // when
            List<ManualRerunControlActionAudit> filteredHistory =
                    repository.findManualRerunControlActionAudits(
                            "EXECUTION:MANUAL_RERUN:history-filter",
                            ManualRerunControlAction.ACKNOWLEDGE,
                            ManualRerunControlActionStatus.APPLIED
                    );

            // then
            assertThat(filteredHistory).hasSize(1);
            assertThat(filteredHistory.getFirst().getAction()).isEqualTo(ManualRerunControlAction.ACKNOWLEDGE);
            assertThat(filteredHistory.getFirst().getActionStatus()).isEqualTo(ManualRerunControlActionStatus.APPLIED);
            assertThat(filteredHistory.getFirst().getNote()).isEqualTo("운영자 확인 완료");
        });
    }

    @DisplayName("manual rerun control action audit는 같은 action을 다시 저장해도 마지막 applied action과 전체 history를 유지한다.")
    @Test
    void appendManualRerunControlActionAudit_allowsRepeatedActionAfterOppositeAction() {
        contextRunner.run(context -> {
            // given
            AgentRuntimeRepository repository = context.getBean(AgentRuntimeRepository.class);
            ManualRerunControlActionAudit firstAcknowledge = ManualRerunControlActionAudit.applied(
                    "EXECUTION:MANUAL_RERUN:repeat-1",
                    ManualRerunControlAction.ACKNOWLEDGE,
                    "첫 확인 완료",
                    LocalDateTime.of(2026, 4, 10, 11, 0)
            );
            ManualRerunControlActionAudit unacknowledge = ManualRerunControlActionAudit.applied(
                    "EXECUTION:MANUAL_RERUN:repeat-1",
                    ManualRerunControlAction.UNACKNOWLEDGE,
                    "확인 취소",
                    LocalDateTime.of(2026, 4, 10, 11, 1)
            );
            ManualRerunControlActionAudit secondAcknowledge = ManualRerunControlActionAudit.applied(
                    "EXECUTION:MANUAL_RERUN:repeat-1",
                    ManualRerunControlAction.ACKNOWLEDGE,
                    "재확인 완료",
                    LocalDateTime.of(2026, 4, 10, 11, 2)
            );

            // when
            repository.appendManualRerunControlActionAudit(firstAcknowledge);
            repository.appendManualRerunControlActionAudit(unacknowledge);
            repository.appendManualRerunControlActionAudit(secondAcknowledge);
            Optional<ManualRerunControlAction> latestAction =
                    repository.findLatestAppliedManualRerunControlAction("EXECUTION:MANUAL_RERUN:repeat-1");
            List<ManualRerunControlActionAudit> history =
                    repository.findManualRerunControlActionAudits("EXECUTION:MANUAL_RERUN:repeat-1");

            // then
            assertThat(latestAction).contains(ManualRerunControlAction.ACKNOWLEDGE);
            assertThat(history).hasSize(3);
            assertThat(history.get(0).getAction()).isEqualTo(ManualRerunControlAction.ACKNOWLEDGE);
            assertThat(history.get(0).getNote()).isEqualTo("첫 확인 완료");
            assertThat(history.get(1).getAction()).isEqualTo(ManualRerunControlAction.UNACKNOWLEDGE);
            assertThat(history.get(1).getNote()).isEqualTo("확인 취소");
            assertThat(history.get(2).getAction()).isEqualTo(ManualRerunControlAction.ACKNOWLEDGE);
            assertThat(history.get(2).getNote()).isEqualTo("재확인 완료");
            assertThat(history.get(2).getAppliedAt()).isEqualTo(LocalDateTime.of(2026, 4, 10, 11, 2));
        });
    }

    @DisplayName("성공한 webhook execution은 오류 코드 null 상태로 저장하고 다시 조회할 수 있다.")
    @Test
    void upsertWebhookExecution_withNullErrorCode_andFind() {
        contextRunner.run(context -> {
            // given
            AgentRuntimeRepository repository = context.getBean(AgentRuntimeRepository.class);
            WebhookExecution webhookExecution = WebhookExecution.start(
                    "EXECUTION:202",
                    "TASK-202",
                    "delivery-202",
                    "sonic8-8/agile-runner",
                    202,
                    "PULL_REQUEST",
                    "opened",
                    LocalDateTime.of(2026, 4, 2, 14, 10)
            ).complete(
                    WebhookExecutionStatus.SUCCEEDED,
                    null,
                    null,
                    LocalDateTime.of(2026, 4, 2, 14, 12)
            );

            // when
            repository.upsertWebhookExecution(webhookExecution);
            Optional<WebhookExecution> found = repository.findWebhookExecution("EXECUTION:202");

            // then
            assertThat(found).isPresent();
            assertThat(found.get().getStatus()).isEqualTo(WebhookExecutionStatus.SUCCEEDED);
            assertThat(found.get().getErrorCode()).isNull();
        });
    }

    @DisplayName("manual rerun execution과 실행 로그를 성공 상태로 저장하고 다시 조회할 수 있다.")
    @Test
    void upsertManualRerunExecutionAndLog_andFind() {
        contextRunner.run(context -> {
            // given
            AgentRuntimeRepository repository = context.getBean(AgentRuntimeRepository.class);
            WebhookExecution runtimeExecution = WebhookExecution.start(
                    "EXECUTION:MANUAL_RERUN:301",
                    "TASK-301",
                    "manual-rerun-delivery-301",
                    "sonic8-8/agile-runner",
                    301,
                    "PULL_REQUEST",
                    "manual_rerun",
                    LocalDateTime.of(2026, 4, 8, 21, 0)
            ).withExecutionStartType(
                    ExecutionStartType.MANUAL_RERUN
            ).withExecutionControl(
                    ExecutionControlMode.NORMAL,
                    true,
                    null
            ).withSelectionScope(
                    true,
                    "src/Main.java|src/Test.java"
            ).complete(
                    WebhookExecutionStatus.SUCCEEDED,
                    null,
                    null,
                    LocalDateTime.of(2026, 4, 8, 21, 2)
            );
            AgentExecutionLog executionLog = AgentExecutionLog.of(
                    "TASK-301",
                    301L,
                    "EXECUTION:MANUAL_RERUN:301",
                    AgentRole.ORCHESTRATOR,
                    "comment-posted",
                    AgentExecutionStatus.SUCCEEDED,
                    "manual rerun execution result recorded",
                    "GitHub comment write completed",
                    null,
                    null,
                    "{\"manualRerun\":true}",
                    LocalDateTime.of(2026, 4, 8, 21, 1),
                    LocalDateTime.of(2026, 4, 8, 21, 2)
            ).withExecutionStartType(
                    ExecutionStartType.MANUAL_RERUN
            ).withExecutionControl(
                    ExecutionControlMode.NORMAL,
                    true,
                    null
            ).withSelectionScope(
                    true,
                    "src/Main.java|src/Test.java"
            );

            // when
            repository.upsertWebhookExecution(runtimeExecution);
            repository.appendExecutionLog(executionLog);
            Optional<WebhookExecution> foundExecution = repository.findWebhookExecution("EXECUTION:MANUAL_RERUN:301");
            List<AgentExecutionLog> foundLogs = repository.findExecutionLogs("TASK-301");

            // then
            assertThat(foundExecution).isPresent();
            assertThat(foundExecution.get().getExecutionStartType()).isEqualTo(ExecutionStartType.MANUAL_RERUN);
            assertThat(foundExecution.get().getRetrySourceExecutionKey()).isNull();
            assertThat(foundExecution.get().getExecutionControlMode()).isEqualTo(ExecutionControlMode.NORMAL);
            assertThat(foundExecution.get().getWritePerformed()).isTrue();
            assertThat(foundExecution.get().getWriteSkipReason()).isNull();
            assertThat(foundExecution.get().getSelectionApplied()).isTrue();
            assertThat(foundExecution.get().getSelectedPathsSummary()).isEqualTo("src/Main.java|src/Test.java");

            assertThat(foundLogs).hasSize(1);
            assertThat(foundLogs.getFirst().getExecutionStartType()).isEqualTo(ExecutionStartType.MANUAL_RERUN);
            assertThat(foundLogs.getFirst().getRetrySourceExecutionKey()).isNull();
            assertThat(foundLogs.getFirst().getExecutionControlMode()).isEqualTo(ExecutionControlMode.NORMAL);
            assertThat(foundLogs.getFirst().getWritePerformed()).isTrue();
            assertThat(foundLogs.getFirst().getWriteSkipReason()).isNull();
            assertThat(foundLogs.getFirst().getSelectionApplied()).isTrue();
            assertThat(foundLogs.getFirst().getSelectedPathsSummary()).isEqualTo("src/Main.java|src/Test.java");
        });
    }

    @DisplayName("manual rerun 실패 execution과 실행 로그를 같은 execution key로 저장하고 다시 조회할 수 있다.")
    @Test
    void upsertManualRerunFailureExecutionAndLog_andFind() {
        contextRunner.run(context -> {
            // given
            AgentRuntimeRepository repository = context.getBean(AgentRuntimeRepository.class);
            WebhookExecution runtimeExecution = WebhookExecution.start(
                    "EXECUTION:MANUAL_RERUN:401",
                    "TASK-401",
                    "manual-rerun-delivery-401",
                    "sonic8-8/agile-runner",
                    401,
                    "PULL_REQUEST",
                    "manual_rerun",
                    LocalDateTime.of(2026, 4, 9, 11, 0)
            ).withExecutionStartType(
                    ExecutionStartType.MANUAL_RERUN
            ).withExecutionControl(
                    ExecutionControlMode.DRY_RUN,
                    false,
                    GitHubWriteSkipReason.DRY_RUN
            ).withSelectionScope(
                    true,
                    "src/Main.java"
            ).complete(
                    WebhookExecutionStatus.FAILED,
                    "GitHub App ID missing",
                    ErrorCode.GITHUB_APP_CONFIGURATION_MISSING,
                    FailureDisposition.MANUAL_ACTION_REQUIRED,
                    LocalDateTime.of(2026, 4, 9, 11, 2)
            );
            AgentExecutionLog executionLog = AgentExecutionLog.of(
                    "TASK-401",
                    401L,
                    "EXECUTION:MANUAL_RERUN:401",
                    ExecutionStartType.MANUAL_RERUN,
                    AgentRole.ORCHESTRATOR,
                    "review-generated",
                    AgentExecutionStatus.FAILED,
                    "manual rerun request accepted",
                    null,
                    "GitHub App ID missing",
                    ErrorCode.GITHUB_APP_CONFIGURATION_MISSING,
                    FailureDisposition.MANUAL_ACTION_REQUIRED,
                    "{\"manualRerun\":true}",
                    LocalDateTime.of(2026, 4, 9, 11, 1),
                    LocalDateTime.of(2026, 4, 9, 11, 2)
            ).withExecutionControl(
                    ExecutionControlMode.DRY_RUN,
                    false,
                    GitHubWriteSkipReason.DRY_RUN
            ).withSelectionScope(
                    true,
                    "src/Main.java"
            );

            // when
            repository.upsertWebhookExecution(runtimeExecution);
            repository.appendExecutionLog(executionLog);
            Optional<WebhookExecution> foundExecution = repository.findWebhookExecution("EXECUTION:MANUAL_RERUN:401");
            List<AgentExecutionLog> foundLogs = repository.findExecutionLogs("TASK-401");

            // then
            assertThat(foundExecution).isPresent();
            assertThat(foundExecution.get().getExecutionKey()).isEqualTo("EXECUTION:MANUAL_RERUN:401");
            assertThat(foundExecution.get().getExecutionStartType()).isEqualTo(ExecutionStartType.MANUAL_RERUN);
            assertThat(foundExecution.get().getStatus()).isEqualTo(WebhookExecutionStatus.FAILED);
            assertThat(foundExecution.get().getErrorCode()).isEqualTo(ErrorCode.GITHUB_APP_CONFIGURATION_MISSING);
            assertThat(foundExecution.get().getFailureDisposition()).isEqualTo(FailureDisposition.MANUAL_ACTION_REQUIRED);
            assertThat(foundExecution.get().getExecutionControlMode()).isEqualTo(ExecutionControlMode.DRY_RUN);
            assertThat(foundExecution.get().getWritePerformed()).isFalse();
            assertThat(foundExecution.get().getWriteSkipReason()).isEqualTo(GitHubWriteSkipReason.DRY_RUN);
            assertThat(foundExecution.get().getSelectionApplied()).isTrue();
            assertThat(foundExecution.get().getSelectedPathsSummary()).isEqualTo("src/Main.java");

            assertThat(foundLogs).hasSize(1);
            assertThat(foundLogs.getFirst().getExecutionKey()).isEqualTo("EXECUTION:MANUAL_RERUN:401");
            assertThat(foundLogs.getFirst().getExecutionStartType()).isEqualTo(ExecutionStartType.MANUAL_RERUN);
            assertThat(foundLogs.getFirst().getStatus()).isEqualTo(AgentExecutionStatus.FAILED);
            assertThat(foundLogs.getFirst().getErrorCode()).isEqualTo(ErrorCode.GITHUB_APP_CONFIGURATION_MISSING);
            assertThat(foundLogs.getFirst().getFailureDisposition()).isEqualTo(FailureDisposition.MANUAL_ACTION_REQUIRED);
            assertThat(foundLogs.getFirst().getExecutionControlMode()).isEqualTo(ExecutionControlMode.DRY_RUN);
            assertThat(foundLogs.getFirst().getWritePerformed()).isFalse();
            assertThat(foundLogs.getFirst().getWriteSkipReason()).isEqualTo(GitHubWriteSkipReason.DRY_RUN);
            assertThat(foundLogs.getFirst().getSelectionApplied()).isTrue();
            assertThat(foundLogs.getFirst().getSelectedPathsSummary()).isEqualTo("src/Main.java");
        });
    }

    @DisplayName("재시도 원본 execution key가 있는 manual rerun execution과 실행 로그를 저장하고 다시 조회할 수 있다.")
    @Test
    void upsertManualRerunRetryExecutionAndLog_andFind() {
        contextRunner.run(context -> {
            // given
            AgentRuntimeRepository repository = context.getBean(AgentRuntimeRepository.class);
            WebhookExecution runtimeExecution = WebhookExecution.start(
                    "EXECUTION:MANUAL_RERUN:501",
                    "TASK-501",
                    "manual-rerun-delivery-501",
                    "sonic8-8/agile-runner",
                    501,
                    "PULL_REQUEST",
                    "manual_rerun",
                    LocalDateTime.of(2026, 4, 9, 15, 0)
            ).withExecutionStartType(
                    ExecutionStartType.MANUAL_RERUN
            ).withRetrySourceExecutionKey(
                    "EXECUTION:MANUAL_RERUN:source-501"
            ).withExecutionControl(
                    ExecutionControlMode.DRY_RUN,
                    false,
                    GitHubWriteSkipReason.DRY_RUN
            ).withSelectionScope(
                    true,
                    "src/Main.java"
            ).complete(
                    WebhookExecutionStatus.FAILED,
                    "GitHub App ID missing",
                    ErrorCode.GITHUB_APP_CONFIGURATION_MISSING,
                    FailureDisposition.MANUAL_ACTION_REQUIRED,
                    LocalDateTime.of(2026, 4, 9, 15, 2)
            );
            AgentExecutionLog executionLog = AgentExecutionLog.of(
                    "TASK-501",
                    501L,
                    "EXECUTION:MANUAL_RERUN:501",
                    ExecutionStartType.MANUAL_RERUN,
                    AgentRole.ORCHESTRATOR,
                    "review-generated",
                    AgentExecutionStatus.FAILED,
                    "manual rerun request accepted",
                    null,
                    "GitHub App ID missing",
                    ErrorCode.GITHUB_APP_CONFIGURATION_MISSING,
                    FailureDisposition.MANUAL_ACTION_REQUIRED,
                    "{\"manualRerun\":true}",
                    LocalDateTime.of(2026, 4, 9, 15, 1),
                    LocalDateTime.of(2026, 4, 9, 15, 2)
            ).withRetrySourceExecutionKey(
                    "EXECUTION:MANUAL_RERUN:source-501"
            ).withExecutionControl(
                    ExecutionControlMode.DRY_RUN,
                    false,
                    GitHubWriteSkipReason.DRY_RUN
            ).withSelectionScope(
                    true,
                    "src/Main.java"
            );

            // when
            repository.upsertWebhookExecution(runtimeExecution);
            repository.appendExecutionLog(executionLog);
            Optional<WebhookExecution> foundExecution = repository.findWebhookExecution("EXECUTION:MANUAL_RERUN:501");
            List<AgentExecutionLog> foundLogs = repository.findExecutionLogs("TASK-501");

            // then
            assertThat(foundExecution).isPresent();
            assertThat(foundExecution.get().getRetrySourceExecutionKey()).isEqualTo("EXECUTION:MANUAL_RERUN:source-501");
            assertThat(foundLogs).hasSize(1);
            assertThat(foundLogs.getFirst().getRetrySourceExecutionKey()).isEqualTo("EXECUTION:MANUAL_RERUN:source-501");
        });
    }
}
