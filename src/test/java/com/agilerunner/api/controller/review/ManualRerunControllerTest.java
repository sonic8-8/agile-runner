package com.agilerunner.api.controller.review;

import com.agilerunner.api.service.review.ManualRerunService;
import com.agilerunner.api.service.review.request.ManualRerunServiceRequest;
import com.agilerunner.api.service.review.response.ManualRerunServiceResponse;
import com.agilerunner.domain.executioncontrol.ExecutionControlMode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
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

    @DisplayName("수동 재실행 요청은 runtime evidence 조회에 사용할 execution key, 실행 제어 모드, 쓰기 여부를 응답으로 반환한다.")
    @Test
    void rerun_returnsResponseContract() throws Exception {
        // given
        ManualRerunServiceResponse response = ManualRerunServiceResponse.of(
                "EXECUTION:MANUAL_RERUN:owner/repo#12:1",
                ExecutionControlMode.DRY_RUN,
                false
        );
        when(manualRerunService.rerun(any(ManualRerunServiceRequest.class))).thenReturn(response);

        // when & then
        mockMvc.perform(post("/reviews/rerun")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "repositoryName", "owner/repo",
                                "pullRequestNumber", 12,
                                "installationId", 100L,
                                "executionControlMode", "DRY_RUN"
                        ))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.executionKey").value("EXECUTION:MANUAL_RERUN:owner/repo#12:1"))
                .andExpect(jsonPath("$.executionControlMode").value("DRY_RUN"))
                .andExpect(jsonPath("$.writePerformed").value(false));

        ArgumentCaptor<ManualRerunServiceRequest> requestCaptor = ArgumentCaptor.forClass(ManualRerunServiceRequest.class);
        verify(manualRerunService).rerun(requestCaptor.capture());
        assertThat(requestCaptor.getValue().getRepositoryName()).isEqualTo("owner/repo");
        assertThat(requestCaptor.getValue().getPullRequestNumber()).isEqualTo(12);
        assertThat(requestCaptor.getValue().getInstallationId()).isEqualTo(100L);
        assertThat(requestCaptor.getValue().getExecutionControlMode()).isEqualTo(ExecutionControlMode.DRY_RUN);
    }
}
