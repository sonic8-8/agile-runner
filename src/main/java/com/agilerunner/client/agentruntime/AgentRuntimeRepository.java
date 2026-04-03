package com.agilerunner.client.agentruntime;

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
    private static final String UPSERT_TASK_STATE = """
            MERGE INTO TASK_STATE (
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
    private static final String FIND_TASK_STATE = """
            SELECT task_key, issue_number, title, status, retry_count, owner_role, started_at, finished_at
            FROM TASK_STATE
            WHERE task_key = :taskKey
            """;
    private static final String DELETE_EVALUATION_CRITERIA = """
            DELETE FROM EVALUATION_CRITERIA
            WHERE task_key = :taskKey
            """;
    private static final String INSERT_EVALUATION_CRITERIA = """
            INSERT INTO EVALUATION_CRITERIA (
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
    private static final String FIND_EVALUATION_CRITERIA = """
            SELECT task_key, criteria_key, category, description, status, evidence
            FROM EVALUATION_CRITERIA
            WHERE task_key = :taskKey
            ORDER BY id ASC
            """;
    private static final String UPSERT_REVIEW_RUN = """
            MERGE INTO REVIEW_RUN (
                run_key,
                task_key,
                delivery_id,
                repository_name,
                pull_request_number,
                event_type,
                action,
                status,
                error_message,
                started_at,
                finished_at,
                updated_at
            ) KEY (run_key)
            VALUES (
                :runKey,
                :taskKey,
                :deliveryId,
                :repositoryName,
                :pullRequestNumber,
                :eventType,
                :action,
                :status,
                :errorMessage,
                :startedAt,
                :finishedAt,
                CURRENT_TIMESTAMP
            )
            """;
    private static final String FIND_REVIEW_RUN = """
            SELECT run_key, task_key, delivery_id, repository_name, pull_request_number, event_type, action, status, error_message, started_at, finished_at
            FROM REVIEW_RUN
            WHERE run_key = :runKey
            """;
    private static final String INSERT_AGENT_EXECUTION_LOG = """
            INSERT INTO AGENT_EXECUTION_LOG (
                task_key,
                issue_number,
                run_key,
                agent_role,
                step_name,
                status,
                input_summary,
                output_summary,
                error_message,
                payload_json,
                started_at,
                ended_at
            ) VALUES (
                :taskKey,
                :issueNumber,
                :runKey,
                :agentRole,
                :stepName,
                :status,
                :inputSummary,
                :outputSummary,
                :errorMessage,
                :payloadJson,
                :startedAt,
                :endedAt
            )
            """;
    private static final String FIND_AGENT_EXECUTION_LOGS = """
            SELECT task_key, issue_number, run_key, agent_role, step_name, status, input_summary, output_summary, error_message, payload_json, started_at, ended_at
            FROM AGENT_EXECUTION_LOG
            WHERE task_key = :taskKey
            ORDER BY id ASC
            """;

    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    public void upsertTaskState(TaskState taskState) {
        namedParameterJdbcTemplate.update(UPSERT_TASK_STATE, toTaskStateParameters(taskState));
    }

    public Optional<TaskState> findTaskState(String taskKey) {
        List<TaskState> taskStates = namedParameterJdbcTemplate.query(
                FIND_TASK_STATE,
                new MapSqlParameterSource("taskKey", taskKey),
                this::mapTaskState
        );

        if (taskStates.isEmpty()) {
            return Optional.empty();
        }

        return Optional.of(taskStates.getFirst());
    }

    public void replaceEvaluationCriteria(String taskKey, List<EvaluationCriteria> evaluationCriteria) {
        namedParameterJdbcTemplate.update(
                DELETE_EVALUATION_CRITERIA,
                new MapSqlParameterSource("taskKey", taskKey)
        );

        if (evaluationCriteria.isEmpty()) {
            return;
        }

        MapSqlParameterSource[] batchParameters = evaluationCriteria.stream()
                .map(this::toEvaluationCriteriaParameters)
                .toArray(MapSqlParameterSource[]::new);

        namedParameterJdbcTemplate.batchUpdate(INSERT_EVALUATION_CRITERIA, batchParameters);
    }

    public List<EvaluationCriteria> findEvaluationCriteria(String taskKey) {
        return namedParameterJdbcTemplate.query(
                FIND_EVALUATION_CRITERIA,
                new MapSqlParameterSource("taskKey", taskKey),
                this::mapEvaluationCriteria
        );
    }

    public void upsertReviewRun(ReviewRun reviewRun) {
        namedParameterJdbcTemplate.update(UPSERT_REVIEW_RUN, toReviewRunParameters(reviewRun));
    }

    public Optional<ReviewRun> findReviewRun(String runKey) {
        List<ReviewRun> reviewRuns = namedParameterJdbcTemplate.query(
                FIND_REVIEW_RUN,
                new MapSqlParameterSource("runKey", runKey),
                this::mapReviewRun
        );

        if (reviewRuns.isEmpty()) {
            return Optional.empty();
        }

        return Optional.of(reviewRuns.getFirst());
    }

    public void appendExecutionLog(AgentExecutionLog executionLog) {
        namedParameterJdbcTemplate.update(INSERT_AGENT_EXECUTION_LOG, toExecutionLogParameters(executionLog));
    }

    public List<AgentExecutionLog> findExecutionLogs(String taskKey) {
        return namedParameterJdbcTemplate.query(
                FIND_AGENT_EXECUTION_LOGS,
                new MapSqlParameterSource("taskKey", taskKey),
                this::mapExecutionLog
        );
    }

    private MapSqlParameterSource toTaskStateParameters(TaskState taskState) {
        return new MapSqlParameterSource()
                .addValue("taskKey", taskState.getTaskKey())
                .addValue("issueNumber", taskState.getIssueNumber())
                .addValue("title", taskState.getTitle())
                .addValue("status", taskState.getStatus().name())
                .addValue("retryCount", taskState.getRetryCount())
                .addValue("ownerRole", getAgentRoleName(taskState.getOwnerRole()))
                .addValue("startedAt", taskState.getStartedAt())
                .addValue("finishedAt", taskState.getFinishedAt());
    }

    private MapSqlParameterSource toEvaluationCriteriaParameters(EvaluationCriteria evaluationCriteria) {
        return new MapSqlParameterSource()
                .addValue("taskKey", evaluationCriteria.getTaskKey())
                .addValue("criteriaKey", evaluationCriteria.getCriteriaKey())
                .addValue("category", evaluationCriteria.getCategory().name())
                .addValue("description", evaluationCriteria.getDescription())
                .addValue("status", evaluationCriteria.getStatus().name())
                .addValue("evidence", evaluationCriteria.getEvidence());
    }

    private MapSqlParameterSource toReviewRunParameters(ReviewRun reviewRun) {
        return new MapSqlParameterSource()
                .addValue("runKey", reviewRun.getRunKey())
                .addValue("taskKey", reviewRun.getTaskKey())
                .addValue("deliveryId", reviewRun.getDeliveryId())
                .addValue("repositoryName", reviewRun.getRepositoryName())
                .addValue("pullRequestNumber", reviewRun.getPullRequestNumber())
                .addValue("eventType", reviewRun.getEventType())
                .addValue("action", reviewRun.getAction())
                .addValue("status", reviewRun.getStatus().name())
                .addValue("errorMessage", reviewRun.getErrorMessage())
                .addValue("startedAt", reviewRun.getStartedAt())
                .addValue("finishedAt", reviewRun.getFinishedAt());
    }

    private MapSqlParameterSource toExecutionLogParameters(AgentExecutionLog executionLog) {
        return new MapSqlParameterSource()
                .addValue("taskKey", executionLog.getTaskKey())
                .addValue("issueNumber", executionLog.getIssueNumber())
                .addValue("runKey", executionLog.getRunKey())
                .addValue("agentRole", executionLog.getAgentRole().name())
                .addValue("stepName", executionLog.getStepName())
                .addValue("status", executionLog.getStatus().name())
                .addValue("inputSummary", executionLog.getInputSummary())
                .addValue("outputSummary", executionLog.getOutputSummary())
                .addValue("errorMessage", executionLog.getErrorMessage())
                .addValue("payloadJson", executionLog.getPayloadJson())
                .addValue("startedAt", executionLog.getStartedAt())
                .addValue("endedAt", executionLog.getEndedAt());
    }

    private TaskState mapTaskState(ResultSet resultSet, int rowNum) throws SQLException {
        return TaskState.of(
                resultSet.getString("task_key"),
                getLong(resultSet, "issue_number"),
                resultSet.getString("title"),
                TaskStateStatus.valueOf(resultSet.getString("status")),
                resultSet.getInt("retry_count"),
                getAgentRole(resultSet.getString("owner_role")),
                getLocalDateTime(resultSet, "started_at"),
                getLocalDateTime(resultSet, "finished_at")
        );
    }

    private EvaluationCriteria mapEvaluationCriteria(ResultSet resultSet, int rowNum) throws SQLException {
        return EvaluationCriteria.of(
                resultSet.getString("task_key"),
                resultSet.getString("criteria_key"),
                CriteriaCategory.valueOf(resultSet.getString("category")),
                resultSet.getString("description"),
                CriteriaStatus.valueOf(resultSet.getString("status")),
                resultSet.getString("evidence")
        );
    }

    private ReviewRun mapReviewRun(ResultSet resultSet, int rowNum) throws SQLException {
        return ReviewRun.start(
                resultSet.getString("run_key"),
                resultSet.getString("task_key"),
                resultSet.getString("delivery_id"),
                resultSet.getString("repository_name"),
                resultSet.getInt("pull_request_number"),
                resultSet.getString("event_type"),
                resultSet.getString("action"),
                getLocalDateTime(resultSet, "started_at")
        ).complete(
                ReviewRunStatus.valueOf(resultSet.getString("status")),
                resultSet.getString("error_message"),
                getLocalDateTime(resultSet, "finished_at")
        );
    }

    private AgentExecutionLog mapExecutionLog(ResultSet resultSet, int rowNum) throws SQLException {
        return AgentExecutionLog.of(
                resultSet.getString("task_key"),
                getLong(resultSet, "issue_number"),
                resultSet.getString("run_key"),
                AgentRole.valueOf(resultSet.getString("agent_role")),
                resultSet.getString("step_name"),
                AgentExecutionStatus.valueOf(resultSet.getString("status")),
                resultSet.getString("input_summary"),
                resultSet.getString("output_summary"),
                resultSet.getString("error_message"),
                resultSet.getString("payload_json"),
                getLocalDateTime(resultSet, "started_at"),
                getLocalDateTime(resultSet, "ended_at")
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
}
