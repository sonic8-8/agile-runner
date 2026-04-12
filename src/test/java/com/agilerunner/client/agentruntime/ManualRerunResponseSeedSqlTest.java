package com.agilerunner.client.agentruntime;

import com.agilerunner.domain.agentruntime.ExecutionStartType;
import com.agilerunner.domain.agentruntime.WebhookExecutionStatus;
import com.agilerunner.domain.exception.FailureDisposition;
import com.agilerunner.domain.exception.ErrorCode;
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

import javax.sql.DataSource;

import static org.assertj.core.api.Assertions.assertThat;

class ManualRerunResponseSeedSqlTest {

    @DisplayName("retry 원본 실행 준비 데이터 SQL은 현재 스키마 기준으로 실행 가능해야 한다.")
    @Test
    void retrySourceExecutionSeed_executesAgainstCurrentSchema() {
        // given
        JdbcTemplate jdbcTemplate = jdbcTemplateAfterApplying(
                "agent-runtime/schema.sql",
                "manual-rerun-response-seed/source-execution/retry-source-execution-seed.example.sql"
        );

        // when
        Integer storedRows = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM WEBHOOK_EXECUTION WHERE execution_key = 'EXECUTION:MANUAL_RERUN:example-retry-source'",
                Integer.class
        );
        String storedFailureDisposition = jdbcTemplate.queryForObject(
                "SELECT failure_disposition FROM WEBHOOK_EXECUTION WHERE execution_key = 'EXECUTION:MANUAL_RERUN:example-retry-source'",
                String.class
        );
        String storedStatus = jdbcTemplate.queryForObject(
                "SELECT status FROM WEBHOOK_EXECUTION WHERE execution_key = 'EXECUTION:MANUAL_RERUN:example-retry-source'",
                String.class
        );
        String storedExecutionStartType = jdbcTemplate.queryForObject(
                "SELECT execution_start_type FROM WEBHOOK_EXECUTION WHERE execution_key = 'EXECUTION:MANUAL_RERUN:example-retry-source'",
                String.class
        );
        String storedExecutionControlMode = jdbcTemplate.queryForObject(
                "SELECT execution_control_mode FROM WEBHOOK_EXECUTION WHERE execution_key = 'EXECUTION:MANUAL_RERUN:example-retry-source'",
                String.class
        );
        String storedErrorCode = jdbcTemplate.queryForObject(
                "SELECT error_code FROM WEBHOOK_EXECUTION WHERE execution_key = 'EXECUTION:MANUAL_RERUN:example-retry-source'",
                String.class
        );
        String storedWriteSkipReason = jdbcTemplate.queryForObject(
                "SELECT write_skip_reason FROM WEBHOOK_EXECUTION WHERE execution_key = 'EXECUTION:MANUAL_RERUN:example-retry-source'",
                String.class
        );

        // then
        assertThat(storedRows).isEqualTo(1);
        assertThat(FailureDisposition.valueOf(storedFailureDisposition)).isEqualTo(FailureDisposition.RETRYABLE);
        assertThat(WebhookExecutionStatus.valueOf(storedStatus)).isEqualTo(WebhookExecutionStatus.FAILED);
        assertThat(ExecutionStartType.valueOf(storedExecutionStartType)).isEqualTo(ExecutionStartType.MANUAL_RERUN);
        assertThat(ExecutionControlMode.valueOf(storedExecutionControlMode)).isEqualTo(ExecutionControlMode.DRY_RUN);
        assertThat(ErrorCode.valueOf(storedErrorCode)).isEqualTo(ErrorCode.GITHUB_APP_CONFIGURATION_MISSING);
        assertThat(GitHubWriteSkipReason.valueOf(storedWriteSkipReason)).isEqualTo(GitHubWriteSkipReason.DRY_RUN);
    }

    @DisplayName("rerun acknowledge 준비 데이터 SQL은 현재 스키마 기준으로 실행 가능해야 한다.")
    @Test
    void rerunAcknowledgeActionHistorySeed_executesAgainstCurrentSchema() {
        // given
        JdbcTemplate jdbcTemplate = jdbcTemplateAfterApplying(
                "agent-runtime/schema.sql",
                "manual-rerun-response-seed/control-action-history/rerun-acknowledge-action-history-seed.example.sql"
        );

        // when
        Integer storedExecutionRows = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM WEBHOOK_EXECUTION WHERE execution_key = 'EXECUTION:MANUAL_RERUN:example-rerun'",
                Integer.class
        );
        Integer storedAuditRows = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM MANUAL_RERUN_CONTROL_ACTION_AUDIT WHERE execution_key = 'EXECUTION:MANUAL_RERUN:example-rerun'",
                Integer.class
        );
        String storedFailureDisposition = jdbcTemplate.queryForObject(
                "SELECT failure_disposition FROM WEBHOOK_EXECUTION WHERE execution_key = 'EXECUTION:MANUAL_RERUN:example-rerun'",
                String.class
        );
        String storedStatus = jdbcTemplate.queryForObject(
                "SELECT status FROM WEBHOOK_EXECUTION WHERE execution_key = 'EXECUTION:MANUAL_RERUN:example-rerun'",
                String.class
        );
        String storedExecutionControlMode = jdbcTemplate.queryForObject(
                "SELECT execution_control_mode FROM WEBHOOK_EXECUTION WHERE execution_key = 'EXECUTION:MANUAL_RERUN:example-rerun'",
                String.class
        );
        String storedErrorCode = jdbcTemplate.queryForObject(
                "SELECT error_code FROM WEBHOOK_EXECUTION WHERE execution_key = 'EXECUTION:MANUAL_RERUN:example-rerun'",
                String.class
        );
        String storedAction = jdbcTemplate.queryForObject(
                "SELECT action FROM MANUAL_RERUN_CONTROL_ACTION_AUDIT WHERE execution_key = 'EXECUTION:MANUAL_RERUN:example-rerun'",
                String.class
        );
        String storedActionStatus = jdbcTemplate.queryForObject(
                "SELECT action_status FROM MANUAL_RERUN_CONTROL_ACTION_AUDIT WHERE execution_key = 'EXECUTION:MANUAL_RERUN:example-rerun'",
                String.class
        );

        // then
        assertThat(storedExecutionRows).isEqualTo(1);
        assertThat(storedAuditRows).isEqualTo(1);
        assertThat(FailureDisposition.valueOf(storedFailureDisposition)).isEqualTo(FailureDisposition.MANUAL_ACTION_REQUIRED);
        assertThat(WebhookExecutionStatus.valueOf(storedStatus)).isEqualTo(WebhookExecutionStatus.FAILED);
        assertThat(ExecutionControlMode.valueOf(storedExecutionControlMode)).isEqualTo(ExecutionControlMode.DRY_RUN);
        assertThat(ErrorCode.valueOf(storedErrorCode)).isEqualTo(ErrorCode.GITHUB_APP_CONFIGURATION_MISSING);
        assertThat(ManualRerunControlAction.valueOf(storedAction)).isEqualTo(ManualRerunControlAction.ACKNOWLEDGE);
        assertThat(ManualRerunControlActionStatus.valueOf(storedActionStatus)).isEqualTo(ManualRerunControlActionStatus.APPLIED);
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
        dataSource.setUrl("jdbc:h2:mem:manual-rerun-seed-sql;MODE=PostgreSQL;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE");
        dataSource.setUsername("sa");
        dataSource.setPassword("");
        return dataSource;
    }
}
