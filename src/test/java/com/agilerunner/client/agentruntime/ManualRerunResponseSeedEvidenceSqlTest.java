package com.agilerunner.client.agentruntime;

import com.agilerunner.domain.agentruntime.AgentExecutionStatus;
import com.agilerunner.domain.agentruntime.ExecutionStartType;
import com.agilerunner.domain.agentruntime.WebhookExecutionStatus;
import com.agilerunner.domain.exception.ErrorCode;
import com.agilerunner.domain.exception.FailureDisposition;
import com.agilerunner.domain.executioncontrol.ExecutionControlMode;
import com.agilerunner.domain.executioncontrol.GitHubWriteSkipReason;
import com.agilerunner.domain.review.ManualRerunControlAction;
import com.agilerunner.domain.review.ManualRerunControlActionStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;
import org.springframework.util.FileCopyUtils;

import javax.sql.DataSource;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class ManualRerunResponseSeedEvidenceSqlTest {

    @DisplayName("rerun 실행 근거 확인 SQL은 현재 스키마 기준으로 결과 행을 찾아야 한다.")
    @Test
    void rerunEvidenceCheckSql_returnsExpectedRows() {
        // given
        JdbcTemplate jdbcTemplate = jdbcTemplateAfterApplying(
                "agent-runtime/schema.sql",
                "manual-rerun-response-seed/control-action-history/rerun-acknowledge-action-history-seed.example.sql"
        );

        // when
        List<String> statements = selectStatements("manual-rerun-response-seed/runtime-evidence/rerun-runtime-evidence-check.example.sql");
        List<Map<String, Object>> executionRows = jdbcTemplate.queryForList(statements.get(0));
        List<Map<String, Object>> auditRows = jdbcTemplate.queryForList(statements.get(1));

        // then
        assertThat(executionRows).hasSize(1);
        assertThat(executionRows.getFirst().get("EXECUTION_KEY")).isEqualTo("EXECUTION:MANUAL_RERUN:example-rerun");
        assertThat(WebhookExecutionStatus.valueOf((String) executionRows.getFirst().get("STATUS")))
                .isEqualTo(WebhookExecutionStatus.FAILED);
        assertThat(ErrorCode.valueOf((String) executionRows.getFirst().get("ERROR_CODE")))
                .isEqualTo(ErrorCode.GITHUB_APP_CONFIGURATION_MISSING);
        assertThat(FailureDisposition.valueOf((String) executionRows.getFirst().get("FAILURE_DISPOSITION")))
                .isEqualTo(FailureDisposition.MANUAL_ACTION_REQUIRED);
        assertThat(ExecutionStartType.valueOf((String) executionRows.getFirst().get("EXECUTION_START_TYPE")))
                .isEqualTo(ExecutionStartType.MANUAL_RERUN);
        assertThat(ExecutionControlMode.valueOf((String) executionRows.getFirst().get("EXECUTION_CONTROL_MODE")))
                .isEqualTo(ExecutionControlMode.DRY_RUN);
        assertThat(executionRows.getFirst().get("WRITE_PERFORMED")).isEqualTo(false);

        assertThat(auditRows).hasSize(1);
        assertThat(auditRows.getFirst().get("EXECUTION_KEY")).isEqualTo("EXECUTION:MANUAL_RERUN:example-rerun");
        assertThat(ManualRerunControlAction.valueOf((String) auditRows.getFirst().get("ACTION")))
                .isEqualTo(ManualRerunControlAction.ACKNOWLEDGE);
        assertThat(ManualRerunControlActionStatus.valueOf((String) auditRows.getFirst().get("ACTION_STATUS")))
                .isEqualTo(ManualRerunControlActionStatus.APPLIED);
        assertThat(auditRows.getFirst().get("NOTE")).isEqualTo("운영자 확인 완료");
        assertThat(auditRows.getFirst().get("APPLIED_AT"))
                .isEqualTo(Timestamp.valueOf(LocalDateTime.of(2026, 4, 12, 13, 15, 0)));
    }

    @DisplayName("retry 실행 근거 확인 SQL은 현재 스키마 기준으로 결과 행을 찾아야 한다.")
    @Test
    void retryEvidenceCheckSql_returnsExpectedRows() {
        // given
        JdbcTemplate jdbcTemplate = jdbcTemplateAfterApplying(
                "agent-runtime/schema.sql",
                "manual-rerun-response-seed/source-execution/retry-source-execution-seed.example.sql"
        );
        insertRetryDerivedExecution(jdbcTemplate);
        insertRetryDerivedExecutionLogs(jdbcTemplate);

        // when
        List<String> statements = selectStatements("manual-rerun-response-seed/runtime-evidence/retry-runtime-evidence-check.example.sql");
        List<Map<String, Object>> executionRows = jdbcTemplate.queryForList(statements.get(0));
        List<Map<String, Object>> logRows = jdbcTemplate.queryForList(statements.get(1));

        // then
        assertThat(executionRows).hasSize(1);
        assertThat(executionRows.getFirst().get("EXECUTION_KEY")).isEqualTo("EXECUTION:MANUAL_RERUN:example-retry-derived");
        assertThat(executionRows.getFirst().get("RETRY_SOURCE_EXECUTION_KEY")).isEqualTo("EXECUTION:MANUAL_RERUN:example-retry-source");
        assertThat(WebhookExecutionStatus.valueOf((String) executionRows.getFirst().get("STATUS")))
                .isEqualTo(WebhookExecutionStatus.FAILED);
        assertThat(ErrorCode.valueOf((String) executionRows.getFirst().get("ERROR_CODE")))
                .isEqualTo(ErrorCode.GITHUB_APP_CONFIGURATION_MISSING);
        assertThat(FailureDisposition.valueOf((String) executionRows.getFirst().get("FAILURE_DISPOSITION")))
                .isEqualTo(FailureDisposition.MANUAL_ACTION_REQUIRED);
        assertThat(ExecutionStartType.valueOf((String) executionRows.getFirst().get("EXECUTION_START_TYPE")))
                .isEqualTo(ExecutionStartType.MANUAL_RERUN);
        assertThat(ExecutionControlMode.valueOf((String) executionRows.getFirst().get("EXECUTION_CONTROL_MODE")))
                .isEqualTo(ExecutionControlMode.DRY_RUN);
        assertThat(executionRows.getFirst().get("WRITE_PERFORMED")).isEqualTo(false);

        assertThat(logRows).hasSize(2);
        assertThat(logRows).extracting(row -> row.get("EXECUTION_KEY"))
                .containsOnly("EXECUTION:MANUAL_RERUN:example-retry-derived");
        assertThat(logRows).extracting(row -> row.get("RETRY_SOURCE_EXECUTION_KEY"))
                .containsOnly("EXECUTION:MANUAL_RERUN:example-retry-source");
        assertThat(logRows).extracting(row -> AgentExecutionStatus.valueOf((String) row.get("STATUS")))
                .containsExactly(AgentExecutionStatus.SUCCEEDED, AgentExecutionStatus.FAILED);
        assertThat(logRows).extracting(row -> row.get("STEP_NAME"))
                .containsExactly("manual-rerun-accepted", "review-generated");
        assertThat(logRows.getFirst().get("ERROR_CODE")).isNull();
        assertThat(logRows.getFirst().get("FAILURE_DISPOSITION")).isNull();
        assertThat(logRows.get(1).get("ERROR_CODE")).isEqualTo("GITHUB_APP_CONFIGURATION_MISSING");
        assertThat(FailureDisposition.valueOf((String) logRows.get(1).get("FAILURE_DISPOSITION")))
                .isEqualTo(FailureDisposition.MANUAL_ACTION_REQUIRED);
    }

    private JdbcTemplate jdbcTemplateAfterApplying(String... resourcePaths) {
        DataSource dataSource = dataSource();
        ResourceDatabasePopulator populator = new ResourceDatabasePopulator();

        for (String resourcePath : resourcePaths) {
            populator.addScript(new ClassPathResource(resourcePath));
        }

        populator.execute(dataSource);

        return new JdbcTemplate(dataSource);
    }

    private DataSource dataSource() {
        DriverManagerDataSource dataSource = new DriverManagerDataSource();
        dataSource.setDriverClassName("org.h2.Driver");
        dataSource.setUrl("jdbc:h2:mem:" + UUID.randomUUID() + ";MODE=PostgreSQL;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE");
        dataSource.setUsername("sa");
        dataSource.setPassword("");
        return dataSource;
    }

    private List<String> selectStatements(String classpathResource) {
        String script = Arrays.stream(resourceText(classpathResource).split("\n"))
                .map(String::trim)
                .filter(line -> !line.startsWith("--"))
                .reduce("", (left, right) -> left + "\n" + right);

        return Arrays.stream(script.split(";"))
                .map(String::trim)
                .filter(statement -> !statement.isBlank())
                .toList();
    }

    private String resourceText(String classpathResource) {
        ClassPathResource resource = new ClassPathResource(classpathResource);

        try (Reader reader = new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8)) {
            return FileCopyUtils.copyToString(reader);
        } catch (IOException exception) {
            throw new IllegalStateException("준비 데이터 SQL 파일을 읽을 수 없습니다: " + classpathResource, exception);
        }
    }

    private void insertRetryDerivedExecution(JdbcTemplate jdbcTemplate) {
        jdbcTemplate.update(
                """
                INSERT INTO WEBHOOK_EXECUTION (
                    execution_key,
                    task_key,
                    delivery_id,
                    retry_source_execution_key,
                    repository_name,
                    pull_request_number,
                    event_type,
                    action,
                    status,
                    error_message,
                    error_code,
                    failure_disposition,
                    execution_start_type,
                    execution_control_mode,
                    write_performed,
                    write_skip_reason,
                    selection_applied,
                    selected_paths_summary,
                    started_at,
                    finished_at
                ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                """,
                "EXECUTION:MANUAL_RERUN:example-retry-derived",
                "PR_REVIEW:owner/repo#12",
                "MANUAL_RERUN_DELIVERY:example-retry-derived",
                "EXECUTION:MANUAL_RERUN:example-retry-source",
                "owner/repo",
                12,
                "PULL_REQUEST",
                "manual_rerun",
                "FAILED",
                "retry derived failed",
                "GITHUB_APP_CONFIGURATION_MISSING",
                "MANUAL_ACTION_REQUIRED",
                "MANUAL_RERUN",
                "DRY_RUN",
                false,
                "DRY_RUN",
                false,
                null,
                Timestamp.valueOf(LocalDateTime.of(2026, 4, 12, 13, 2, 0)),
                Timestamp.valueOf(LocalDateTime.of(2026, 4, 12, 13, 3, 0))
        );
    }

    private void insertRetryDerivedExecutionLogs(JdbcTemplate jdbcTemplate) {
        jdbcTemplate.update(
                """
                INSERT INTO AGENT_EXECUTION_LOG (
                    task_key,
                    issue_number,
                    execution_key,
                    retry_source_execution_key,
                    agent_role,
                    step_name,
                    status,
                    input_summary,
                    output_summary,
                    error_message,
                    error_code,
                    failure_disposition,
                    execution_start_type,
                    execution_control_mode,
                    write_performed,
                    write_skip_reason,
                    selection_applied,
                    selected_paths_summary,
                    payload_json,
                    started_at,
                    ended_at
                ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                """,
                "PR_REVIEW:owner/repo#12",
                null,
                "EXECUTION:MANUAL_RERUN:example-retry-derived",
                "EXECUTION:MANUAL_RERUN:example-retry-source",
                "ORCHESTRATOR",
                "manual-rerun-accepted",
                "SUCCEEDED",
                "retry execution accepted",
                "accepted",
                null,
                null,
                null,
                "MANUAL_RERUN",
                "DRY_RUN",
                false,
                "DRY_RUN",
                false,
                null,
                "{}",
                Timestamp.valueOf(LocalDateTime.of(2026, 4, 12, 13, 2, 0)),
                Timestamp.valueOf(LocalDateTime.of(2026, 4, 12, 13, 2, 10))
        );
        jdbcTemplate.update(
                """
                INSERT INTO AGENT_EXECUTION_LOG (
                    task_key,
                    issue_number,
                    execution_key,
                    retry_source_execution_key,
                    agent_role,
                    step_name,
                    status,
                    input_summary,
                    output_summary,
                    error_message,
                    error_code,
                    failure_disposition,
                    execution_start_type,
                    execution_control_mode,
                    write_performed,
                    write_skip_reason,
                    selection_applied,
                    selected_paths_summary,
                    payload_json,
                    started_at,
                    ended_at
                ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                """,
                "PR_REVIEW:owner/repo#12",
                null,
                "EXECUTION:MANUAL_RERUN:example-retry-derived",
                "EXECUTION:MANUAL_RERUN:example-retry-source",
                "REVIEWER",
                "review-generated",
                "FAILED",
                "review generation",
                "failed",
                "retry derived failed",
                "GITHUB_APP_CONFIGURATION_MISSING",
                "MANUAL_ACTION_REQUIRED",
                "MANUAL_RERUN",
                "DRY_RUN",
                false,
                "DRY_RUN",
                false,
                null,
                "{}",
                Timestamp.valueOf(LocalDateTime.of(2026, 4, 12, 13, 2, 10)),
                Timestamp.valueOf(LocalDateTime.of(2026, 4, 12, 13, 3, 0))
        );
    }
}
