package com.agilerunner.api.controller.review;

import com.agilerunner.api.service.review.ManualRerunService;
import com.agilerunner.api.service.review.ManualRerunQueryService;
import com.agilerunner.api.service.review.ManualRerunExecutionListService;
import com.agilerunner.api.service.review.ManualRerunControlActionHistoryService;
import com.agilerunner.api.service.review.ManualRerunControlActionService;
import com.agilerunner.api.service.review.ManualRerunRetryService;
import com.agilerunner.api.service.review.request.ManualRerunControlActionHistoryServiceRequest;
import com.agilerunner.api.service.review.request.ManualRerunControlActionServiceRequest;
import com.agilerunner.api.service.review.request.ManualRerunExecutionListServiceRequest;
import com.agilerunner.api.service.review.request.ManualRerunServiceRequest;
import com.agilerunner.api.service.review.request.ManualRerunQueryServiceRequest;
import com.agilerunner.api.service.review.request.ManualRerunRetryServiceRequest;
import com.agilerunner.api.service.review.response.ManualRerunControlActionHistoryServiceResponse;
import com.agilerunner.api.service.review.response.ManualRerunControlActionServiceResponse;
import com.agilerunner.api.service.review.response.ManualRerunExecutionListServiceResponse;
import com.agilerunner.api.service.review.response.ManualRerunServiceResponse;
import com.agilerunner.api.service.review.response.ManualRerunQueryServiceResponse;
import com.agilerunner.api.service.review.response.ManualRerunRetryServiceResponse;
import com.agilerunner.domain.exception.ManualRerunQueryNotFoundException;
import com.agilerunner.domain.exception.ErrorCode;
import com.agilerunner.domain.exception.FailureDisposition;
import com.agilerunner.domain.exception.ManualRerunControlActionConflictException;
import com.agilerunner.domain.exception.ManualRerunControlActionNotFoundException;
import com.agilerunner.domain.exception.ManualRerunRetryConflictException;
import com.agilerunner.domain.exception.ManualRerunRetryNotFoundException;
import com.agilerunner.domain.agentruntime.ExecutionStartType;
import com.agilerunner.domain.agentruntime.WebhookExecutionStatus;
import com.agilerunner.domain.executioncontrol.ExecutionControlMode;
import com.agilerunner.domain.review.ManualRerunAvailableAction;
import com.agilerunner.domain.review.ManualRerunControlAction;
import com.agilerunner.domain.review.ManualRerunControlActionStatus;
import com.agilerunner.domain.review.RerunExecutionStatus;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.hamcrest.Matchers.nullValue;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ManualRerunController.class)
class ManualRerunControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private ManualRerunService manualRerunService;

    @MockitoBean
    private ManualRerunQueryService manualRerunQueryService;

    @MockitoBean
    private ManualRerunRetryService manualRerunRetryService;

    @MockitoBean
    private ManualRerunExecutionListService manualRerunExecutionListService;

    @MockitoBean
    private ManualRerunControlActionHistoryService manualRerunControlActionHistoryService;

    @MockitoBean
    private ManualRerunControlActionService manualRerunControlActionService;

    @DisplayName("수동 재실행 요청은 선택 파일 경로를 service request로 전달하고 확장된 응답 계약을 유지한다.")
    @Test
    void rerun_returnsResponseContract() throws Exception {
        // given
        ManualRerunServiceResponse response = ManualRerunServiceResponse.of(
                "EXECUTION:MANUAL_RERUN:owner/repo#12:1",
                ExecutionControlMode.DRY_RUN,
                false,
                RerunExecutionStatus.SUCCEEDED,
                null,
                null
        );
        when(manualRerunService.rerun(any(ManualRerunServiceRequest.class))).thenReturn(response);

        // when & then
        mockMvc.perform(post("/reviews/rerun")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "repositoryName", "owner/repo",
                                "pullRequestNumber", 12,
                                "installationId", 100L,
                                "executionControlMode", "DRY_RUN",
                                "selectedPaths", List.of("src/main/App.java", "src/test/AppTest.java")
                        ))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.executionKey").value("EXECUTION:MANUAL_RERUN:owner/repo#12:1"))
                .andExpect(jsonPath("$.executionControlMode").value("DRY_RUN"))
                .andExpect(jsonPath("$.writePerformed").value(false))
                .andExpect(jsonPath("$.executionStatus").value("SUCCEEDED"))
                .andExpect(jsonPath("$.errorCode").value(nullValue()))
                .andExpect(jsonPath("$.failureDisposition").value(nullValue()));

        ArgumentCaptor<ManualRerunServiceRequest> requestCaptor = ArgumentCaptor.forClass(ManualRerunServiceRequest.class);
        verify(manualRerunService).rerun(requestCaptor.capture());
        assertThat(requestCaptor.getValue().getRepositoryName()).isEqualTo("owner/repo");
        assertThat(requestCaptor.getValue().getPullRequestNumber()).isEqualTo(12);
        assertThat(requestCaptor.getValue().getInstallationId()).isEqualTo(100L);
        assertThat(requestCaptor.getValue().getExecutionControlMode()).isEqualTo(ExecutionControlMode.DRY_RUN);
        assertThat(requestCaptor.getValue().getSelectedPaths()).containsExactly(
                "src/main/App.java",
                "src/test/AppTest.java"
        );
    }

    @DisplayName("수동 재실행 실패 응답은 실행 상태와 실패 정보를 함께 반환한다.")
    @Test
    void rerun_returnsFailureResponseFields() throws Exception {
        // given
        ManualRerunServiceResponse response = ManualRerunServiceResponse.of(
                "EXECUTION:MANUAL_RERUN:owner/repo#12:2",
                ExecutionControlMode.NORMAL,
                false,
                RerunExecutionStatus.FAILED,
                ErrorCode.GITHUB_COMMENT_POST_FAILED,
                FailureDisposition.RETRYABLE
        );
        when(manualRerunService.rerun(any(ManualRerunServiceRequest.class))).thenReturn(response);

        // when & then
        mockMvc.perform(post("/reviews/rerun")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "repositoryName", "owner/repo",
                                "pullRequestNumber", 12,
                                "installationId", 100L,
                                "executionControlMode", "NORMAL",
                                "selectedPaths", List.of()
                        ))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.executionKey").value("EXECUTION:MANUAL_RERUN:owner/repo#12:2"))
                .andExpect(jsonPath("$.executionControlMode").value("NORMAL"))
                .andExpect(jsonPath("$.writePerformed").value(false))
                .andExpect(jsonPath("$.executionStatus").value("FAILED"))
                .andExpect(jsonPath("$.errorCode").value("GITHUB_COMMENT_POST_FAILED"))
                .andExpect(jsonPath("$.failureDisposition").value("RETRYABLE"));
    }

    @DisplayName("재실행 결과 조회 요청은 executionKey를 service request로 전달하고 최소 조회 응답 계약을 유지한다.")
    @Test
    void getRerunResult_returnsResponseContract() throws Exception {
        // given
        ManualRerunQueryServiceResponse response = ManualRerunQueryServiceResponse.of(
                "EXECUTION:MANUAL_RERUN:2b5fe092-4365-4e94-a291-0b89e9184c9d",
                ExecutionControlMode.DRY_RUN,
                false,
                RerunExecutionStatus.FAILED,
                ErrorCode.GITHUB_APP_CONFIGURATION_MISSING,
                FailureDisposition.MANUAL_ACTION_REQUIRED,
                List.of(ManualRerunAvailableAction.ACKNOWLEDGE)
        );
        when(manualRerunQueryService.find(any(ManualRerunQueryServiceRequest.class))).thenReturn(response);

        // when & then
        mockMvc.perform(get("/reviews/rerun/{executionKey}", "EXECUTION:MANUAL_RERUN:2b5fe092-4365-4e94-a291-0b89e9184c9d"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.executionKey").value("EXECUTION:MANUAL_RERUN:2b5fe092-4365-4e94-a291-0b89e9184c9d"))
                .andExpect(jsonPath("$.writePerformed").value(false))
                .andExpect(jsonPath("$.executionControlMode").value("DRY_RUN"))
                .andExpect(jsonPath("$.executionStatus").value("FAILED"))
                .andExpect(jsonPath("$.errorCode").value("GITHUB_APP_CONFIGURATION_MISSING"))
                .andExpect(jsonPath("$.failureDisposition").value("MANUAL_ACTION_REQUIRED"))
                .andExpect(jsonPath("$.availableActions[0]").value("ACKNOWLEDGE"));

        ArgumentCaptor<ManualRerunQueryServiceRequest> requestCaptor = ArgumentCaptor.forClass(ManualRerunQueryServiceRequest.class);
        verify(manualRerunQueryService).find(requestCaptor.capture());
        assertThat(requestCaptor.getValue().getExecutionKey()).isEqualTo("EXECUTION:MANUAL_RERUN:2b5fe092-4365-4e94-a291-0b89e9184c9d");
    }

    @DisplayName("목록 조회 요청은 응답 row 상태와 availableActions를 함께 반환한다.")
    @Test
    void listExecutions_returnsResponseContract() throws Exception {
        // given
        ManualRerunExecutionListServiceResponse response = ManualRerunExecutionListServiceResponse.of(
                List.of(
                        ManualRerunExecutionListServiceResponse.ExecutionSummary.of(
                                "EXECUTION:MANUAL_RERUN:100",
                                "EXECUTION:MANUAL_RERUN:source-100",
                                ExecutionStartType.MANUAL_RERUN,
                                RerunExecutionStatus.FAILED,
                                ExecutionControlMode.DRY_RUN,
                                false,
                                ErrorCode.GITHUB_COMMENT_POST_FAILED,
                                FailureDisposition.RETRYABLE,
                                List.of(ManualRerunAvailableAction.RETRY)
                        ),
                        ManualRerunExecutionListServiceResponse.ExecutionSummary.of(
                                "EXECUTION:MANUAL_RERUN:101",
                                null,
                                ExecutionStartType.MANUAL_RERUN,
                                RerunExecutionStatus.SUCCEEDED,
                                ExecutionControlMode.NORMAL,
                                true,
                                null,
                                null,
                                List.of()
                        )
                )
        );
        when(manualRerunExecutionListService.list(any(ManualRerunExecutionListServiceRequest.class))).thenReturn(response);

        // when & then
        mockMvc.perform(get("/reviews/rerun/executions")
                        .queryParam("repositoryName", "owner/repo")
                        .queryParam("pullRequestNumber", "12")
                        .queryParam("executionStartType", "MANUAL_RERUN")
                        .queryParam("executionStatus", "FAILED")
                        .queryParam("failureDisposition", "RETRYABLE"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.executions[0].executionKey").value("EXECUTION:MANUAL_RERUN:100"))
                .andExpect(jsonPath("$.executions[0].retrySourceExecutionKey").value("EXECUTION:MANUAL_RERUN:source-100"))
                .andExpect(jsonPath("$.executions[0].executionStartType").value("MANUAL_RERUN"))
                .andExpect(jsonPath("$.executions[0].executionStatus").value("FAILED"))
                .andExpect(jsonPath("$.executions[0].executionControlMode").value("DRY_RUN"))
                .andExpect(jsonPath("$.executions[0].writePerformed").value(false))
                .andExpect(jsonPath("$.executions[0].errorCode").value("GITHUB_COMMENT_POST_FAILED"))
                .andExpect(jsonPath("$.executions[0].failureDisposition").value("RETRYABLE"))
                .andExpect(jsonPath("$.executions[0].availableActions[0]").value("RETRY"))
                .andExpect(jsonPath("$.executions[1].executionKey").value("EXECUTION:MANUAL_RERUN:101"))
                .andExpect(jsonPath("$.executions[1].retrySourceExecutionKey").value(nullValue()))
                .andExpect(jsonPath("$.executions[1].executionStatus").value("SUCCEEDED"))
                .andExpect(jsonPath("$.executions[1].executionControlMode").value("NORMAL"))
                .andExpect(jsonPath("$.executions[1].writePerformed").value(true))
                .andExpect(jsonPath("$.executions[1].errorCode").value(nullValue()))
                .andExpect(jsonPath("$.executions[1].failureDisposition").value(nullValue()))
                .andExpect(jsonPath("$.executions[1].availableActions").isArray());

        ArgumentCaptor<ManualRerunExecutionListServiceRequest> requestCaptor =
                ArgumentCaptor.forClass(ManualRerunExecutionListServiceRequest.class);
        verify(manualRerunExecutionListService).list(requestCaptor.capture());
        assertThat(requestCaptor.getValue().getRepositoryName()).isEqualTo("owner/repo");
        assertThat(requestCaptor.getValue().getPullRequestNumber()).isEqualTo(12);
        assertThat(requestCaptor.getValue().getExecutionStartType()).isEqualTo(ExecutionStartType.MANUAL_RERUN);
        assertThat(requestCaptor.getValue().getExecutionStatus()).isEqualTo(WebhookExecutionStatus.FAILED);
        assertThat(requestCaptor.getValue().getFailureDisposition()).isEqualTo(FailureDisposition.RETRYABLE);
    }

    @DisplayName("목록 조회 요청에 query param이 없으면 service request의 필터는 모두 미적용으로 해석한다.")
    @Test
    void listExecutions_treatsMissingQueryParamsAsNoFilters() throws Exception {
        // given
        ManualRerunExecutionListServiceResponse response = ManualRerunExecutionListServiceResponse.of(List.of());
        when(manualRerunExecutionListService.list(any(ManualRerunExecutionListServiceRequest.class))).thenReturn(response);

        // when & then
        mockMvc.perform(get("/reviews/rerun/executions"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.executions").isArray());

        ArgumentCaptor<ManualRerunExecutionListServiceRequest> requestCaptor =
                ArgumentCaptor.forClass(ManualRerunExecutionListServiceRequest.class);
        verify(manualRerunExecutionListService).list(requestCaptor.capture());
        assertThat(requestCaptor.getValue().getRepositoryName()).isNull();
        assertThat(requestCaptor.getValue().getPullRequestNumber()).isNull();
        assertThat(requestCaptor.getValue().getExecutionStartType()).isNull();
        assertThat(requestCaptor.getValue().getExecutionStatus()).isNull();
        assertThat(requestCaptor.getValue().getFailureDisposition()).isNull();
    }

    @DisplayName("관리자 액션 이력 조회 요청은 executionKey를 service request로 전달하고 최소 응답 계약을 유지한다.")
    @Test
    void getActionHistory_returnsResponseContract() throws Exception {
        // given
        LocalDateTime appliedAt = LocalDateTime.of(2026, 4, 10, 12, 15);
        ManualRerunControlActionHistoryServiceResponse response = ManualRerunControlActionHistoryServiceResponse.of(
                "EXECUTION:MANUAL_RERUN:history-1",
                List.of(
                        ManualRerunControlActionHistoryServiceResponse.ActionHistorySummary.of(
                                ManualRerunControlAction.ACKNOWLEDGE,
                                ManualRerunControlActionStatus.APPLIED,
                                "운영자 확인 완료",
                                appliedAt
                        )
                )
        );
        when(manualRerunControlActionHistoryService.find(any(ManualRerunControlActionHistoryServiceRequest.class)))
                .thenReturn(response);

        // when & then
        mockMvc.perform(get("/reviews/rerun/{executionKey}/actions/history", "EXECUTION:MANUAL_RERUN:history-1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.executionKey").value("EXECUTION:MANUAL_RERUN:history-1"))
                .andExpect(jsonPath("$.actions[0].action").value("ACKNOWLEDGE"))
                .andExpect(jsonPath("$.actions[0].actionStatus").value("APPLIED"))
                .andExpect(jsonPath("$.actions[0].note").value("운영자 확인 완료"))
                .andExpect(jsonPath("$.actions[0].appliedAt").value("2026-04-10T12:15:00"));

        ArgumentCaptor<ManualRerunControlActionHistoryServiceRequest> requestCaptor =
                ArgumentCaptor.forClass(ManualRerunControlActionHistoryServiceRequest.class);
        verify(manualRerunControlActionHistoryService).find(requestCaptor.capture());
        assertThat(requestCaptor.getValue().getExecutionKey()).isEqualTo("EXECUTION:MANUAL_RERUN:history-1");
    }

    @DisplayName("관리자 제어 액션 요청은 executionKey와 note를 service request로 전달하고 최소 성공 응답 계약을 유지한다.")
    @Test
    void executeAction_returnsResponseContract() throws Exception {
        // given
        ManualRerunControlActionServiceResponse response = ManualRerunControlActionServiceResponse.of(
                "EXECUTION:MANUAL_RERUN:action-1",
                ManualRerunControlAction.ACKNOWLEDGE,
                ManualRerunControlActionStatus.APPLIED,
                List.of(),
                "운영자 확인 완료"
        );
        when(manualRerunControlActionService.execute(any(ManualRerunControlActionServiceRequest.class))).thenReturn(response);

        // when & then
        mockMvc.perform(post("/reviews/rerun/{executionKey}/actions", "EXECUTION:MANUAL_RERUN:action-1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "action", "ACKNOWLEDGE",
                                "note", "운영자 확인 완료"
                        ))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.executionKey").value("EXECUTION:MANUAL_RERUN:action-1"))
                .andExpect(jsonPath("$.action").value("ACKNOWLEDGE"))
                .andExpect(jsonPath("$.actionStatus").value("APPLIED"))
                .andExpect(jsonPath("$.availableActions").isArray())
                .andExpect(jsonPath("$.note").value("운영자 확인 완료"));

        ArgumentCaptor<ManualRerunControlActionServiceRequest> requestCaptor =
                ArgumentCaptor.forClass(ManualRerunControlActionServiceRequest.class);
        verify(manualRerunControlActionService).execute(requestCaptor.capture());
        assertThat(requestCaptor.getValue().getExecutionKey()).isEqualTo("EXECUTION:MANUAL_RERUN:action-1");
        assertThat(requestCaptor.getValue().getAction()).isEqualTo(ManualRerunControlAction.ACKNOWLEDGE);
        assertThat(requestCaptor.getValue().getNote()).isEqualTo("운영자 확인 완료");
    }

    @DisplayName("관리자 제어 액션 요청은 UNACKNOWLEDGE 입력을 service에 전달하고 최소 성공 응답 계약을 유지한다.")
    @Test
    void executeAction_returnsUnacknowledgeResponseContract() throws Exception {
        // given
        ManualRerunControlActionServiceResponse response = ManualRerunControlActionServiceResponse.of(
                "EXECUTION:MANUAL_RERUN:action-2",
                ManualRerunControlAction.UNACKNOWLEDGE,
                ManualRerunControlActionStatus.APPLIED,
                List.of(ManualRerunAvailableAction.ACKNOWLEDGE),
                "운영자 확인 취소"
        );
        when(manualRerunControlActionService.execute(any(ManualRerunControlActionServiceRequest.class)))
                .thenReturn(response);

        // when & then
        mockMvc.perform(post("/reviews/rerun/{executionKey}/actions", "EXECUTION:MANUAL_RERUN:action-2")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "action", "UNACKNOWLEDGE",
                                "note", "운영자 확인 취소"
                        ))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.executionKey").value("EXECUTION:MANUAL_RERUN:action-2"))
                .andExpect(jsonPath("$.action").value("UNACKNOWLEDGE"))
                .andExpect(jsonPath("$.actionStatus").value("APPLIED"))
                .andExpect(jsonPath("$.availableActions[0]").value("ACKNOWLEDGE"))
                .andExpect(jsonPath("$.note").value("운영자 확인 취소"));

        ArgumentCaptor<ManualRerunControlActionServiceRequest> requestCaptor =
                ArgumentCaptor.forClass(ManualRerunControlActionServiceRequest.class);
        verify(manualRerunControlActionService).execute(requestCaptor.capture());
        assertThat(requestCaptor.getValue().getExecutionKey()).isEqualTo("EXECUTION:MANUAL_RERUN:action-2");
        assertThat(requestCaptor.getValue().getAction()).isEqualTo(ManualRerunControlAction.UNACKNOWLEDGE);
        assertThat(requestCaptor.getValue().getNote()).isEqualTo("운영자 확인 취소");
    }

    @DisplayName("존재하지 않는 executionKey의 관리자 제어 액션 요청은 404와 executionKey, message를 반환한다.")
    @Test
    void executeAction_returnsNotFoundPolicy() throws Exception {
        // given
        when(manualRerunControlActionService.execute(any(ManualRerunControlActionServiceRequest.class)))
                .thenThrow(new ManualRerunControlActionNotFoundException(
                        "EXECUTION:MANUAL_RERUN:missing",
                        "관리자 제어 액션 대상 실행을 찾을 수 없습니다."
                ));

        // when & then
        mockMvc.perform(post("/reviews/rerun/{executionKey}/actions", "EXECUTION:MANUAL_RERUN:missing")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "action", "ACKNOWLEDGE",
                                "note", "운영자 확인 완료"
                        ))))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.executionKey").value("EXECUTION:MANUAL_RERUN:missing"))
                .andExpect(jsonPath("$.message").value("관리자 제어 액션 대상 실행을 찾을 수 없습니다."));
    }

    @DisplayName("허용되지 않은 관리자 제어 액션 요청은 409와 failureDisposition, message를 반환한다.")
    @Test
    void executeAction_returnsConflictPolicy() throws Exception {
        // given
        when(manualRerunControlActionService.execute(any(ManualRerunControlActionServiceRequest.class)))
                .thenThrow(new ManualRerunControlActionConflictException(
                        "EXECUTION:MANUAL_RERUN:conflict",
                        FailureDisposition.MANUAL_ACTION_REQUIRED,
                        "이미 확인 완료 처리된 실행입니다."
                ));

        // when & then
        mockMvc.perform(post("/reviews/rerun/{executionKey}/actions", "EXECUTION:MANUAL_RERUN:conflict")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "action", "ACKNOWLEDGE",
                                "note", "운영자 확인 완료"
                        ))))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.executionKey").value("EXECUTION:MANUAL_RERUN:conflict"))
                .andExpect(jsonPath("$.failureDisposition").value("MANUAL_ACTION_REQUIRED"))
                .andExpect(jsonPath("$.message").value("이미 확인 완료 처리된 실행입니다."));
    }

    @DisplayName("존재하지 않는 executionKey 조회는 404와 executionKey, message를 반환한다.")
    @Test
    void getRerunResult_returnsNotFoundPolicy() throws Exception {
        // given
        when(manualRerunQueryService.find(any(ManualRerunQueryServiceRequest.class)))
                .thenThrow(new ManualRerunQueryNotFoundException(
                        "EXECUTION:MANUAL_RERUN:missing",
                        "재실행 결과를 찾을 수 없습니다."
                ));

        // when & then
        mockMvc.perform(get("/reviews/rerun/{executionKey}", "EXECUTION:MANUAL_RERUN:missing"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.executionKey").value("EXECUTION:MANUAL_RERUN:missing"))
                .andExpect(jsonPath("$.message").value("재실행 결과를 찾을 수 없습니다."));
    }

    @DisplayName("재실행 재시도 요청은 source execution과 새 execution 정보를 함께 반환한다.")
    @Test
    void retry_returnsSuccessResponseContract() throws Exception {
        // given
        ManualRerunRetryServiceResponse response = ManualRerunRetryServiceResponse.of(
                "EXECUTION:MANUAL_RERUN:retry-1",
                "EXECUTION:MANUAL_RERUN:source-1",
                ExecutionControlMode.DRY_RUN,
                false,
                RerunExecutionStatus.SUCCEEDED,
                null,
                null
        );
        when(manualRerunRetryService.retry(any(ManualRerunRetryServiceRequest.class))).thenReturn(response);

        // when & then
        mockMvc.perform(post("/reviews/rerun/{executionKey}/retry", "EXECUTION:MANUAL_RERUN:source-1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "installationId", 200L,
                                "executionControlMode", "DRY_RUN",
                                "selectedPaths", List.of("src/main/App.java", "src/test/AppTest.java")
                        ))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.executionKey").value("EXECUTION:MANUAL_RERUN:retry-1"))
                .andExpect(jsonPath("$.retrySourceExecutionKey").value("EXECUTION:MANUAL_RERUN:source-1"))
                .andExpect(jsonPath("$.executionControlMode").value("DRY_RUN"))
                .andExpect(jsonPath("$.writePerformed").value(false))
                .andExpect(jsonPath("$.executionStatus").value("SUCCEEDED"))
                .andExpect(jsonPath("$.errorCode").value(nullValue()))
                .andExpect(jsonPath("$.failureDisposition").value(nullValue()));

        ArgumentCaptor<ManualRerunRetryServiceRequest> requestCaptor =
                ArgumentCaptor.forClass(ManualRerunRetryServiceRequest.class);
        verify(manualRerunRetryService).retry(requestCaptor.capture());
        assertThat(requestCaptor.getValue().getSourceExecutionKey()).isEqualTo("EXECUTION:MANUAL_RERUN:source-1");
        assertThat(requestCaptor.getValue().getInstallationId()).isEqualTo(200L);
        assertThat(requestCaptor.getValue().getExecutionControlMode()).isEqualTo(ExecutionControlMode.DRY_RUN);
        assertThat(requestCaptor.getValue().getSelectedPaths()).containsExactly(
                "src/main/App.java",
                "src/test/AppTest.java"
        );
    }

    @DisplayName("존재하지 않는 source execution 재시도 요청은 404와 executionKey, message를 반환한다.")
    @Test
    void retry_returnsNotFoundPolicy() throws Exception {
        // given
        when(manualRerunRetryService.retry(any(ManualRerunRetryServiceRequest.class)))
                .thenThrow(new ManualRerunRetryNotFoundException(
                        "EXECUTION:MANUAL_RERUN:missing",
                        "재시도 대상 실행을 찾을 수 없습니다."
                ));

        // when & then
        mockMvc.perform(post("/reviews/rerun/{executionKey}/retry", "EXECUTION:MANUAL_RERUN:missing")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "installationId", 200L,
                                "executionControlMode", "DRY_RUN",
                                "selectedPaths", List.of()
                        ))))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.executionKey").value("EXECUTION:MANUAL_RERUN:missing"))
                .andExpect(jsonPath("$.message").value("재시도 대상 실행을 찾을 수 없습니다."));
    }

    @DisplayName("재시도 불가 source execution은 409와 failureDisposition, message를 반환한다.")
    @Test
    void retry_returnsConflictPolicy() throws Exception {
        // given
        when(manualRerunRetryService.retry(any(ManualRerunRetryServiceRequest.class)))
                .thenThrow(new ManualRerunRetryConflictException(
                        "EXECUTION:MANUAL_RERUN:source-2",
                        FailureDisposition.MANUAL_ACTION_REQUIRED,
                        "수동 조치가 필요한 실행은 바로 재시도할 수 없습니다."
                ));

        // when & then
        mockMvc.perform(post("/reviews/rerun/{executionKey}/retry", "EXECUTION:MANUAL_RERUN:source-2")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "installationId", 200L,
                                "executionControlMode", "NORMAL",
                                "selectedPaths", List.of()
                        ))))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.executionKey").value("EXECUTION:MANUAL_RERUN:source-2"))
                .andExpect(jsonPath("$.failureDisposition").value("MANUAL_ACTION_REQUIRED"))
                .andExpect(jsonPath("$.message").value("수동 조치가 필요한 실행은 바로 재시도할 수 없습니다."));
    }
}
