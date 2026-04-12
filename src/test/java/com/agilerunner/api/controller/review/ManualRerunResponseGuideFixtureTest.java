package com.agilerunner.api.controller.review;

import com.agilerunner.api.service.review.ManualRerunControlActionHistoryService;
import com.agilerunner.api.service.review.ManualRerunControlActionService;
import com.agilerunner.api.service.review.ManualRerunExecutionListService;
import com.agilerunner.api.service.review.ManualRerunQueryService;
import com.agilerunner.api.service.review.ManualRerunRetryService;
import com.agilerunner.api.service.review.ManualRerunService;
import com.agilerunner.api.service.review.request.ManualRerunControlActionHistoryServiceRequest;
import com.agilerunner.api.service.review.request.ManualRerunControlActionServiceRequest;
import com.agilerunner.api.service.review.request.ManualRerunExecutionListServiceRequest;
import com.agilerunner.api.service.review.request.ManualRerunQueryServiceRequest;
import com.agilerunner.api.service.review.request.ManualRerunRetryServiceRequest;
import com.agilerunner.api.service.review.request.ManualRerunServiceRequest;
import com.agilerunner.api.service.review.response.ManualRerunControlActionHistoryServiceResponse;
import com.agilerunner.api.service.review.response.ManualRerunControlActionServiceResponse;
import com.agilerunner.api.service.review.response.ManualRerunExecutionListServiceResponse;
import com.agilerunner.api.service.review.response.ManualRerunQueryServiceResponse;
import com.agilerunner.api.service.review.response.ManualRerunRetryServiceResponse;
import com.agilerunner.api.service.review.response.ManualRerunServiceResponse;
import com.agilerunner.domain.agentruntime.ExecutionStartType;
import com.agilerunner.domain.exception.ErrorCode;
import com.agilerunner.domain.exception.FailureDisposition;
import com.agilerunner.domain.executioncontrol.ExecutionControlMode;
import com.agilerunner.domain.review.ManualRerunAvailableAction;
import com.agilerunner.domain.review.ManualRerunControlAction;
import com.agilerunner.domain.review.ManualRerunControlActionStatus;
import com.agilerunner.domain.review.RerunExecutionStatus;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ManualRerunController.class)
class ManualRerunResponseGuideFixtureTest {

    private static final String RERUN_EXECUTION_KEY = "EXECUTION:MANUAL_RERUN:example-rerun";
    private static final String RETRY_SOURCE_EXECUTION_KEY = "EXECUTION:MANUAL_RERUN:example-retry-source";
    private static final String RETRY_DERIVED_EXECUTION_KEY = "EXECUTION:MANUAL_RERUN:example-retry-derived";
    private static final LocalDateTime ACKNOWLEDGED_AT = LocalDateTime.of(2026, 4, 12, 13, 15, 0);

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

    @DisplayName("rerun 응답 예시는 guide 기준 파일과 같아야 한다.")
    @Test
    void rerunResponse_matchesGuideFixture() throws Exception {
        // given
        ManualRerunServiceResponse response = ManualRerunServiceResponse.of(
                RERUN_EXECUTION_KEY,
                ExecutionControlMode.DRY_RUN,
                false,
                RerunExecutionStatus.FAILED,
                ErrorCode.GITHUB_APP_CONFIGURATION_MISSING,
                FailureDisposition.MANUAL_ACTION_REQUIRED
        );
        when(manualRerunService.rerun(any(ManualRerunServiceRequest.class))).thenReturn(response);

        // when
        String actualJson = mockMvc.perform(post("/reviews/rerun")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "repositoryName", "owner/repo",
                                "pullRequestNumber", 12,
                                "installationId", 100L,
                                "executionControlMode", "DRY_RUN",
                                "selectedPaths", List.of()
                        ))))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        // then
        assertFixtureMatches("manual-rerun-response-guide/rerun-start-response.json", actualJson);
    }

    @DisplayName("retry 응답 예시는 guide 기준 파일과 같아야 한다.")
    @Test
    void retryResponse_matchesGuideFixture() throws Exception {
        // given
        ManualRerunRetryServiceResponse response = ManualRerunRetryServiceResponse.of(
                RETRY_DERIVED_EXECUTION_KEY,
                RETRY_SOURCE_EXECUTION_KEY,
                ExecutionControlMode.DRY_RUN,
                false,
                RerunExecutionStatus.FAILED,
                ErrorCode.GITHUB_APP_CONFIGURATION_MISSING,
                FailureDisposition.MANUAL_ACTION_REQUIRED
        );
        when(manualRerunRetryService.retry(any(ManualRerunRetryServiceRequest.class))).thenReturn(response);

        // when
        String actualJson = mockMvc.perform(post("/reviews/rerun/{executionKey}/retry", RETRY_SOURCE_EXECUTION_KEY)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "installationId", 200L,
                                "executionControlMode", "DRY_RUN",
                                "selectedPaths", List.of()
                        ))))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        // then
        assertFixtureMatches("manual-rerun-response-guide/retry-start-response.json", actualJson);
    }

    @DisplayName("query 응답 예시는 guide 기준 파일과 같아야 한다.")
    @Test
    void queryResponse_matchesGuideFixture() throws Exception {
        // given
        ManualRerunQueryServiceResponse response = ManualRerunQueryServiceResponse.of(
                RERUN_EXECUTION_KEY,
                ExecutionControlMode.DRY_RUN,
                false,
                RerunExecutionStatus.FAILED,
                ErrorCode.GITHUB_APP_CONFIGURATION_MISSING,
                FailureDisposition.MANUAL_ACTION_REQUIRED,
                List.of(ManualRerunAvailableAction.ACKNOWLEDGE)
        );
        when(manualRerunQueryService.find(any(ManualRerunQueryServiceRequest.class))).thenReturn(response);

        // when
        String actualJson = mockMvc.perform(get("/reviews/rerun/{executionKey}", RERUN_EXECUTION_KEY))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        // then
        assertFixtureMatches("manual-rerun-response-guide/rerun-query-before-acknowledge.json", actualJson);
    }

    @DisplayName("rerun list 응답 예시는 guide 기준 파일과 같아야 한다.")
    @Test
    void listResponse_matchesRerunGuideFixture() throws Exception {
        // given
        ManualRerunExecutionListServiceResponse response = ManualRerunExecutionListServiceResponse.of(
                List.of(
                        ManualRerunExecutionListServiceResponse.ExecutionSummary.of(
                                RERUN_EXECUTION_KEY,
                                null,
                                ExecutionStartType.MANUAL_RERUN,
                                RerunExecutionStatus.FAILED,
                                ExecutionControlMode.DRY_RUN,
                                false,
                                ErrorCode.GITHUB_APP_CONFIGURATION_MISSING,
                                FailureDisposition.MANUAL_ACTION_REQUIRED,
                                ManualRerunControlAction.ACKNOWLEDGE,
                                ManualRerunControlActionStatus.APPLIED,
                                ACKNOWLEDGED_AT,
                                true,
                                List.of(ManualRerunAvailableAction.UNACKNOWLEDGE)
                        )
                )
        );
        when(manualRerunExecutionListService.list(any(ManualRerunExecutionListServiceRequest.class))).thenReturn(response);

        // when
        String actualJson = mockMvc.perform(get("/reviews/rerun/executions"))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        // then
        assertFixtureMatches("manual-rerun-response-guide/rerun-list-after-acknowledge.json", actualJson);
    }

    @DisplayName("retry list row 예시는 guide 기준 파일과 같아야 한다.")
    @Test
    void listResponse_matchesRetryGuideFixture() throws Exception {
        // given
        ManualRerunExecutionListServiceResponse response = ManualRerunExecutionListServiceResponse.of(
                List.of(
                        ManualRerunExecutionListServiceResponse.ExecutionSummary.of(
                                RETRY_DERIVED_EXECUTION_KEY,
                                RETRY_SOURCE_EXECUTION_KEY,
                                ExecutionStartType.MANUAL_RERUN,
                                RerunExecutionStatus.FAILED,
                                ExecutionControlMode.DRY_RUN,
                                false,
                                ErrorCode.GITHUB_APP_CONFIGURATION_MISSING,
                                FailureDisposition.MANUAL_ACTION_REQUIRED,
                                List.of(ManualRerunAvailableAction.ACKNOWLEDGE)
                        )
                )
        );
        when(manualRerunExecutionListService.list(any(ManualRerunExecutionListServiceRequest.class))).thenReturn(response);

        // when
        String actualJson = mockMvc.perform(get("/reviews/rerun/executions"))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        // then
        assertFixtureMatches("manual-rerun-response-guide/retry-list-row.json", actualJson);
    }

    @DisplayName("history 응답 예시는 guide 기준 파일과 같아야 한다.")
    @Test
    void historyResponse_matchesGuideFixture() throws Exception {
        // given
        ManualRerunControlActionHistoryServiceResponse response = ManualRerunControlActionHistoryServiceResponse.of(
                RERUN_EXECUTION_KEY,
                ManualRerunControlActionHistoryServiceResponse.CurrentActionState.of(
                        ManualRerunControlAction.ACKNOWLEDGE,
                        ManualRerunControlActionStatus.APPLIED,
                        ACKNOWLEDGED_AT,
                        List.of(ManualRerunAvailableAction.UNACKNOWLEDGE)
                ),
                List.of(
                        ManualRerunControlActionHistoryServiceResponse.ActionHistorySummary.of(
                                ManualRerunControlAction.ACKNOWLEDGE,
                                ManualRerunControlActionStatus.APPLIED,
                                "운영자 확인 완료",
                                ACKNOWLEDGED_AT
                        )
                )
        );
        when(manualRerunControlActionHistoryService.find(any(ManualRerunControlActionHistoryServiceRequest.class)))
                .thenReturn(response);

        // when
        String actualJson = mockMvc.perform(get("/reviews/rerun/{executionKey}/actions/history", RERUN_EXECUTION_KEY))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        // then
        assertFixtureMatches("manual-rerun-response-guide/rerun-history-after-acknowledge.json", actualJson);
    }

    @DisplayName("action 응답 예시는 guide 기준 파일과 같아야 한다.")
    @Test
    void actionResponse_matchesGuideFixture() throws Exception {
        // given
        ManualRerunControlActionServiceResponse response = ManualRerunControlActionServiceResponse.of(
                RERUN_EXECUTION_KEY,
                ManualRerunControlAction.ACKNOWLEDGE,
                ManualRerunControlActionStatus.APPLIED,
                List.of(ManualRerunAvailableAction.UNACKNOWLEDGE),
                "운영자 확인 완료"
        );
        when(manualRerunControlActionService.execute(any(ManualRerunControlActionServiceRequest.class)))
                .thenReturn(response);

        // when
        String actualJson = mockMvc.perform(post("/reviews/rerun/{executionKey}/actions", RERUN_EXECUTION_KEY)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "action", "ACKNOWLEDGE",
                                "note", "운영자 확인 완료"
                        ))))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        // then
        assertFixtureMatches("manual-rerun-response-guide/rerun-action-after-acknowledge.json", actualJson);
    }

    private void assertFixtureMatches(String fixturePath, String actualJson) throws Exception {
        JsonNode expected = readFixture(fixturePath);
        JsonNode actual = objectMapper.readTree(actualJson);
        assertThat(actual).isEqualTo(expected);
    }

    private JsonNode readFixture(String fixturePath) throws Exception {
        ClassPathResource resource = new ClassPathResource(fixturePath);
        try (InputStream inputStream = resource.getInputStream()) {
            return objectMapper.readTree(inputStream);
        }
    }
}
