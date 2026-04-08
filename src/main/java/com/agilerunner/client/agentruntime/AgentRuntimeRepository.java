package com.agilerunner.client.agentruntime;

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
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "agile-runner.agent-runtime", name = "enabled", havingValue = "true")
public class AgentRuntimeRepository {
    private static final String UPSERT_TASK_RUNTIME_STATE_SQL = """
            MERGE INTO TASK_RUNTIME_STATE (
                task_key,
                issue_number,
                title,
                status,
                retry_count,
                owner_role,
                started_at,
                finished_at,
                updated_at
            ) KEY (task_key)
            VALUES (
                :taskKey,
                :issueNumber,
                :title,
                :status,
                :retryCount,
                :ownerRole,
                :startedAt,
                :finishedAt,
                CURRENT_TIMESTAMP
            )
            """;
    private static final String FIND_TASK_RUNTIME_STATE_SQL = """
            SELECT task_key, issue_number, title, status, retry_count, owner_role, started_at, finished_at
            FROM TASK_RUNTIME_STATE
            WHERE task_key = :taskKey
            """;
    private static final String DELETE_VALIDATION_CRITERIA_SQL = """
            DELETE FROM VALIDATION_CRITERIA
            WHERE task_key = :taskKey
            """;
    private static final String INSERT_VALIDATION_CRITERIA_SQL = """
            INSERT INTO VALIDATION_CRITERIA (
                task_key,
                criteria_key,
                category,
                description,
                status,
                evidence,
                updated_at
            ) VALUES (
                :taskKey,
                :criteriaKey,
                :category,
                :description,
                :status,
                :evidence,
                CURRENT_TIMESTAMP
            )
            """;
    private static final String FIND_VALIDATION_CRITERIA_SQL = """
            SELECT task_key, criteria_key, category, description, status, evidence
            FROM VALIDATION_CRITERIA
            WHERE task_key = :taskKey
            ORDER BY id ASC
            """;
    private static final String UPSERT_WEBHOOK_EXECUTION_SQL = """
            MERGE INTO WEBHOOK_EXECUTION (
                execution_key,
                task_key,
                delivery_id,
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
                finished_at,
                updated_at
            ) KEY (execution_key)
            VALUES (
                :executionKey,
                :taskKey,
                :deliveryId,
                :repositoryName,
                :pullRequestNumber,
                :eventType,
                :action,
                :status,
                :errorMessage,
                :errorCode,
                :failureDisposition,
                :executionStartType,
                :executionControlMode,
                :writePerformed,
                :writeSkipReason,
                :selectionApplied,
                :selectedPathsSummary,
                :startedAt,
                :finishedAt,
                CURRENT_TIMESTAMP
            )
            """;
    private static final String FIND_WEBHOOK_EXECUTION_SQL = """
            SELECT execution_key, task_key, delivery_id, repository_name, pull_request_number, event_type, action, status, error_message, error_code, failure_disposition, execution_start_type, execution_control_mode, write_performed, write_skip_reason, selection_applied, selected_paths_summary, started_at, finished_at
            FROM WEBHOOK_EXECUTION
            WHERE execution_key = :executionKey
            """;
    private static final String INSERT_AGENT_EXECUTION_LOG_SQL = """
            INSERT INTO AGENT_EXECUTION_LOG (
                task_key,
                issue_number,
                execution_key,
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
            ) VALUES (
                :taskKey,
                :issueNumber,
                :executionKey,
                :agentRole,
                :stepName,
                :status,
                :inputSummary,
                :outputSummary,
                :errorMessage,
                :errorCode,
                :failureDisposition,
                :executionStartType,
                :executionControlMode,
                :writePerformed,
                :writeSkipReason,
                :selectionApplied,
                :selectedPathsSummary,
                :payloadJson,
                :startedAt,
                :endedAt
            )
            """;
    private static final String FIND_AGENT_EXECUTION_LOGS_SQL = """
            SELECT task_key, issue_number, execution_key, agent_role, step_name, status, input_summary, output_summary, error_message, error_code, failure_disposition, execution_start_type, execution_control_mode, write_performed, write_skip_reason, selection_applied, selected_paths_summary, payload_json, started_at, ended_at
            FROM AGENT_EXECUTION_LOG
            WHERE task_key = :taskKey
            ORDER BY id ASC
            """;

    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    public void upsertTaskRuntimeState(TaskRuntimeState taskRuntimeState) {
        namedParameterJdbcTemplate.update(UPSERT_TASK_RUNTIME_STATE_SQL, toTaskRuntimeStateParameters(taskRuntimeState));
    }

    public Optional<TaskRuntimeState> findTaskRuntimeState(String taskKey) {
        List<TaskRuntimeState> taskRuntimeStates = namedParameterJdbcTemplate.query(
                FIND_TASK_RUNTIME_STATE_SQL,
                new MapSqlParameterSource("taskKey", taskKey),
                this::mapTaskRuntimeState
        );

        if (taskRuntimeStates.isEmpty()) {
            return Optional.empty();
        }

        return Optional.of(taskRuntimeStates.getFirst());
    }

    public void replaceValidationCriteria(String taskKey, List<ValidationCriteria> validationCriteria) {
        namedParameterJdbcTemplate.update(
                DELETE_VALIDATION_CRITERIA_SQL,
                new MapSqlParameterSource("taskKey", taskKey)
        );

        if (validationCriteria.isEmpty()) {
            return;
        }

        MapSqlParameterSource[] batchParameters = validationCriteria.stream()
                .map(this::toValidationCriteriaParameters)
                .toArray(MapSqlParameterSource[]::new);

        namedParameterJdbcTemplate.batchUpdate(INSERT_VALIDATION_CRITERIA_SQL, batchParameters);
    }

    public List<ValidationCriteria> findValidationCriteria(String taskKey) {
        return namedParameterJdbcTemplate.query(
                FIND_VALIDATION_CRITERIA_SQL,
                new MapSqlParameterSource("taskKey", taskKey),
                this::mapValidationCriteria
        );
    }

    public void upsertWebhookExecution(WebhookExecution webhookExecution) {
        namedParameterJdbcTemplate.update(UPSERT_WEBHOOK_EXECUTION_SQL, toWebhookExecutionParameters(webhookExecution));
    }

    public Optional<WebhookExecution> findWebhookExecution(String executionKey) {
        List<WebhookExecution> webhookExecutions = namedParameterJdbcTemplate.query(
                FIND_WEBHOOK_EXECUTION_SQL,
                new MapSqlParameterSource("executionKey", executionKey),
                this::mapWebhookExecution
        );

        if (webhookExecutions.isEmpty()) {
            return Optional.empty();
        }

        return Optional.of(webhookExecutions.getFirst());
    }

    public void appendExecutionLog(AgentExecutionLog executionLog) {
        namedParameterJdbcTemplate.update(INSERT_AGENT_EXECUTION_LOG_SQL, toExecutionLogParameters(executionLog));
    }

    public List<AgentExecutionLog> findExecutionLogs(String taskKey) {
        return namedParameterJdbcTemplate.query(
                FIND_AGENT_EXECUTION_LOGS_SQL,
                new MapSqlParameterSource("taskKey", taskKey),
                this::mapExecutionLog
        );
    }

    private MapSqlParameterSource toTaskRuntimeStateParameters(TaskRuntimeState taskRuntimeState) {
        return new MapSqlParameterSource()
                .addValue("taskKey", taskRuntimeState.getTaskKey())
                .addValue("issueNumber", taskRuntimeState.getIssueNumber())
                .addValue("title", taskRuntimeState.getTitle())
                .addValue("status", taskRuntimeState.getStatus().name())
                .addValue("retryCount", taskRuntimeState.getRetryCount())
                .addValue("ownerRole", getAgentRoleName(taskRuntimeState.getOwnerRole()))
                .addValue("startedAt", taskRuntimeState.getStartedAt())
                .addValue("finishedAt", taskRuntimeState.getFinishedAt());
    }

    private MapSqlParameterSource toValidationCriteriaParameters(ValidationCriteria validationCriteria) {
        return new MapSqlParameterSource()
                .addValue("taskKey", validationCriteria.getTaskKey())
                .addValue("criteriaKey", validationCriteria.getCriteriaKey())
                .addValue("category", validationCriteria.getCategory().name())
                .addValue("description", validationCriteria.getDescription())
                .addValue("status", validationCriteria.getStatus().name())
                .addValue("evidence", validationCriteria.getEvidence());
    }

    private MapSqlParameterSource toWebhookExecutionParameters(WebhookExecution webhookExecution) {
        return new MapSqlParameterSource()
                .addValue("executionKey", webhookExecution.getExecutionKey())
                .addValue("taskKey", webhookExecution.getTaskKey())
                .addValue("deliveryId", webhookExecution.getDeliveryId())
                .addValue("repositoryName", webhookExecution.getRepositoryName())
                .addValue("pullRequestNumber", webhookExecution.getPullRequestNumber())
                .addValue("eventType", webhookExecution.getEventType())
                .addValue("action", webhookExecution.getAction())
                .addValue("status", webhookExecution.getStatus().name())
                .addValue("errorMessage", webhookExecution.getErrorMessage())
                .addValue("errorCode", getErrorCodeName(webhookExecution.getErrorCode()))
                .addValue("failureDisposition", getFailureDispositionName(webhookExecution.getFailureDisposition()))
                .addValue("executionStartType", getExecutionStartTypeName(webhookExecution.getExecutionStartType()))
                .addValue("executionControlMode", getExecutionControlModeName(webhookExecution.getExecutionControlMode()))
                .addValue("writePerformed", webhookExecution.getWritePerformed())
                .addValue("writeSkipReason", getWriteSkipReasonName(webhookExecution.getWriteSkipReason()))
                .addValue("selectionApplied", webhookExecution.getSelectionApplied())
                .addValue("selectedPathsSummary", webhookExecution.getSelectedPathsSummary())
                .addValue("startedAt", webhookExecution.getStartedAt())
                .addValue("finishedAt", webhookExecution.getFinishedAt());
    }

    private MapSqlParameterSource toExecutionLogParameters(AgentExecutionLog executionLog) {
        return new MapSqlParameterSource()
                .addValue("taskKey", executionLog.getTaskKey())
                .addValue("issueNumber", executionLog.getIssueNumber())
                .addValue("executionKey", executionLog.getExecutionKey())
                .addValue("agentRole", executionLog.getAgentRole().name())
                .addValue("stepName", executionLog.getStepName())
                .addValue("status", executionLog.getStatus().name())
                .addValue("inputSummary", executionLog.getInputSummary())
                .addValue("outputSummary", executionLog.getOutputSummary())
                .addValue("errorMessage", executionLog.getErrorMessage())
                .addValue("errorCode", getErrorCodeName(executionLog.getErrorCode()))
                .addValue("failureDisposition", getFailureDispositionName(executionLog.getFailureDisposition()))
                .addValue("executionStartType", getExecutionStartTypeName(executionLog.getExecutionStartType()))
                .addValue("executionControlMode", getExecutionControlModeName(executionLog.getExecutionControlMode()))
                .addValue("writePerformed", executionLog.getWritePerformed())
                .addValue("writeSkipReason", getWriteSkipReasonName(executionLog.getWriteSkipReason()))
                .addValue("selectionApplied", executionLog.getSelectionApplied())
                .addValue("selectedPathsSummary", executionLog.getSelectedPathsSummary())
                .addValue("payloadJson", executionLog.getPayloadJson())
                .addValue("startedAt", executionLog.getStartedAt())
                .addValue("endedAt", executionLog.getEndedAt());
    }

    private TaskRuntimeState mapTaskRuntimeState(ResultSet resultSet, int rowNum) throws SQLException {
        return TaskRuntimeState.of(
                resultSet.getString("task_key"),
                getLong(resultSet, "issue_number"),
                resultSet.getString("title"),
                TaskRuntimeStatus.valueOf(resultSet.getString("status")),
                resultSet.getInt("retry_count"),
                getAgentRole(resultSet.getString("owner_role")),
                getLocalDateTime(resultSet, "started_at"),
                getLocalDateTime(resultSet, "finished_at")
        );
    }

    private ValidationCriteria mapValidationCriteria(ResultSet resultSet, int rowNum) throws SQLException {
        return ValidationCriteria.of(
                resultSet.getString("task_key"),
                resultSet.getString("criteria_key"),
                CriteriaCategory.valueOf(resultSet.getString("category")),
                resultSet.getString("description"),
                CriteriaStatus.valueOf(resultSet.getString("status")),
                resultSet.getString("evidence")
        );
    }

    private WebhookExecution mapWebhookExecution(ResultSet resultSet, int rowNum) throws SQLException {
        return WebhookExecution.start(
                resultSet.getString("execution_key"),
                resultSet.getString("task_key"),
                resultSet.getString("delivery_id"),
                resultSet.getString("repository_name"),
                resultSet.getInt("pull_request_number"),
                resultSet.getString("event_type"),
                resultSet.getString("action"),
                getLocalDateTime(resultSet, "started_at")
        ).withExecutionStartType(
                getExecutionStartType(resultSet.getString("execution_start_type"))
        ).withExecutionControl(
                getExecutionControlMode(resultSet.getString("execution_control_mode")),
                getNullableBoolean(resultSet, "write_performed"),
                getWriteSkipReason(resultSet.getString("write_skip_reason"))
        ).withSelectionScope(
                getNullableBoolean(resultSet, "selection_applied"),
                resultSet.getString("selected_paths_summary")
        ).complete(
                WebhookExecutionStatus.valueOf(resultSet.getString("status")),
                resultSet.getString("error_message"),
                getErrorCode(resultSet.getString("error_code")),
                getFailureDisposition(resultSet.getString("failure_disposition")),
                getLocalDateTime(resultSet, "finished_at")
        );
    }

    private AgentExecutionLog mapExecutionLog(ResultSet resultSet, int rowNum) throws SQLException {
        return AgentExecutionLog.of(
                resultSet.getString("task_key"),
                getLong(resultSet, "issue_number"),
                resultSet.getString("execution_key"),
                getExecutionStartType(resultSet.getString("execution_start_type")),
                AgentRole.valueOf(resultSet.getString("agent_role")),
                resultSet.getString("step_name"),
                AgentExecutionStatus.valueOf(resultSet.getString("status")),
                resultSet.getString("input_summary"),
                resultSet.getString("output_summary"),
                resultSet.getString("error_message"),
                getErrorCode(resultSet.getString("error_code")),
                getFailureDisposition(resultSet.getString("failure_disposition")),
                resultSet.getString("payload_json"),
                getLocalDateTime(resultSet, "started_at"),
                getLocalDateTime(resultSet, "ended_at")
        ).withExecutionControl(
                getExecutionControlMode(resultSet.getString("execution_control_mode")),
                getNullableBoolean(resultSet, "write_performed"),
                getWriteSkipReason(resultSet.getString("write_skip_reason"))
        ).withSelectionScope(
                getNullableBoolean(resultSet, "selection_applied"),
                resultSet.getString("selected_paths_summary")
        );
    }

    private LocalDateTime getLocalDateTime(ResultSet resultSet, String columnName) throws SQLException {
        if (resultSet.getTimestamp(columnName) == null) {
            return null;
        }

        return resultSet.getTimestamp(columnName).toLocalDateTime();
    }

    private Long getLong(ResultSet resultSet, String columnName) throws SQLException {
        long value = resultSet.getLong(columnName);
        if (resultSet.wasNull()) {
            return null;
        }

        return value;
    }

    private AgentRole getAgentRole(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }

        return AgentRole.valueOf(value);
    }

    private String getAgentRoleName(AgentRole ownerRole) {
        if (ownerRole == null) {
            return null;
        }

        return ownerRole.name();
    }

    private ErrorCode getErrorCode(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }

        return ErrorCode.valueOf(value);
    }

    private String getErrorCodeName(ErrorCode errorCode) {
        if (errorCode == null) {
            return null;
        }

        return errorCode.name();
    }

    private FailureDisposition getFailureDisposition(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }

        return FailureDisposition.valueOf(value);
    }

    private String getFailureDispositionName(FailureDisposition failureDisposition) {
        if (failureDisposition == null) {
            return null;
        }

        return failureDisposition.name();
    }

    private ExecutionStartType getExecutionStartType(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }

        return ExecutionStartType.valueOf(value);
    }

    private String getExecutionStartTypeName(ExecutionStartType executionStartType) {
        if (executionStartType == null) {
            return null;
        }

        return executionStartType.name();
    }

    private ExecutionControlMode getExecutionControlMode(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }

        return ExecutionControlMode.valueOf(value);
    }

    private String getExecutionControlModeName(ExecutionControlMode executionControlMode) {
        if (executionControlMode == null) {
            return null;
        }

        return executionControlMode.name();
    }

    private GitHubWriteSkipReason getWriteSkipReason(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }

        return GitHubWriteSkipReason.valueOf(value);
    }

    private String getWriteSkipReasonName(GitHubWriteSkipReason writeSkipReason) {
        if (writeSkipReason == null) {
            return null;
        }

        return writeSkipReason.name();
    }

    private Boolean getNullableBoolean(ResultSet resultSet, String columnName) throws SQLException {
        boolean value = resultSet.getBoolean(columnName);
        if (resultSet.wasNull()) {
            return null;
        }

        return value;
    }
}
