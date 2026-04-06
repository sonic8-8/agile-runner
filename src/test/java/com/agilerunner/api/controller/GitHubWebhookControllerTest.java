package com.agilerunner.api.controller;

import com.agilerunner.api.service.GitHubCommentService;
import com.agilerunner.api.service.OpenAiService;
import com.agilerunner.api.service.agentruntime.AgentRuntimeService;
import com.agilerunner.api.service.dto.GitHubCommentResponse;
import com.agilerunner.api.service.dto.GitHubEventServiceRequest;
import com.agilerunner.api.service.dto.PostedInlineComment;
import com.agilerunner.domain.Review;
import com.agilerunner.domain.agentruntime.WebhookExecution;
import com.agilerunner.domain.exception.AgileRunnerException;
import com.agilerunner.domain.exception.ErrorCode;
import com.agilerunner.util.WebhookDeliveryCache;
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
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doThrow;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(GitHubWebhookController.class)
class GitHubWebhookControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private AgentRuntimeService agentRuntimeService;

    @MockitoBean
    private OpenAiService openAiService;

    @MockitoBean
    private GitHubCommentService gitHubCommentService;

    @MockitoBean
    private WebhookDeliveryCache webhookDeliveryCache;

    @DisplayName("유효한 pull_request webhook payload를 service request로 변환한다.")
    @Test
    void handleGitHubEvent_convertsPayloadToServiceRequest() throws Exception {
        // given
        String deliveryId = "delivery-1";
        Map<String, Object> payload = buildPayload();
        WebhookExecution webhookExecution = WebhookExecution.start(
                "EXECUTION:delivery-1",
                "PR_REVIEW:owner/repo#12",
                deliveryId,
                "owner/repo",
                12,
                "PULL_REQUEST",
                "opened",
                LocalDateTime.of(2026, 4, 3, 10, 0)
        );
        Review review = Review.of("owner/repo", 12, "리뷰 본문", List.of());
        GitHubCommentResponse response = GitHubCommentResponse.of(11L, "https://github.com/comment/11", List.of(), "ok");

        when(webhookDeliveryCache.isDuplicate(deliveryId)).thenReturn(false);
        when(agentRuntimeService.startWebhookExecution(eq(deliveryId), any(GitHubEventServiceRequest.class))).thenReturn(webhookExecution);
        when(openAiService.generateReview(any(GitHubEventServiceRequest.class))).thenReturn(review);
        when(gitHubCommentService.comment(eq(review), any(GitHubEventServiceRequest.class))).thenReturn(response);

        // when
        mockMvc.perform(post("/webhook/github")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-GitHub-Event", "pull_request")
                        .header("X-GitHub-Delivery", deliveryId)
                        .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isOk());

        // then
        ArgumentCaptor<GitHubEventServiceRequest> requestCaptor = ArgumentCaptor.forClass(GitHubEventServiceRequest.class);
        verify(agentRuntimeService).startWebhookExecution(eq(deliveryId), requestCaptor.capture());

        GitHubEventServiceRequest serviceRequest = requestCaptor.getValue();
        assertThat(serviceRequest.getRepositoryName()).isEqualTo("owner/repo");
        assertThat(serviceRequest.getPullRequestNumber()).isEqualTo(12);
        assertThat(serviceRequest.getInstallationId()).isEqualTo(100L);
        assertThat(serviceRequest.getAction()).isEqualTo("opened");
    }

    @DisplayName("successful comment 경로에서는 기존 응답 계약을 유지한다.")
    @Test
    void handleGitHubEvent_returnsGitHubCommentResponse() throws Exception {
        // given
        String deliveryId = "delivery-2";
        Map<String, Object> payload = buildPayload();
        WebhookExecution webhookExecution = WebhookExecution.start(
                "EXECUTION:delivery-2",
                "PR_REVIEW:owner/repo#12",
                deliveryId,
                "owner/repo",
                12,
                "PULL_REQUEST",
                "opened",
                LocalDateTime.of(2026, 4, 3, 10, 5)
        );
        Review review = Review.of("owner/repo", 12, "리뷰 본문", List.of());
        GitHubCommentResponse response = GitHubCommentResponse.of(
                11L,
                "https://github.com/comment/11",
                List.of(PostedInlineComment.of(22L, "https://github.com/comment/22")),
                "리뷰 코멘트가 성공적으로 등록되었습니다."
        );

        when(webhookDeliveryCache.isDuplicate(deliveryId)).thenReturn(false);
        when(agentRuntimeService.startWebhookExecution(eq(deliveryId), any(GitHubEventServiceRequest.class))).thenReturn(webhookExecution);
        when(openAiService.generateReview(any(GitHubEventServiceRequest.class))).thenReturn(review);
        when(gitHubCommentService.comment(eq(review), any(GitHubEventServiceRequest.class))).thenReturn(response);

        // when & then
        mockMvc.perform(post("/webhook/github")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-GitHub-Event", "pull_request")
                        .header("X-GitHub-Delivery", deliveryId)
                        .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.reviewCommentId").value(11L))
                .andExpect(jsonPath("$.reviewCommentUrl").value("https://github.com/comment/11"))
                .andExpect(jsonPath("$.postedInlineComments[0].id").value(22L))
                .andExpect(jsonPath("$.postedInlineComments[0].htmlUrl").value("https://github.com/comment/22"))
                .andExpect(jsonPath("$.message").value("리뷰 코멘트가 성공적으로 등록되었습니다."));
    }

    @DisplayName("successful comment 이후 runtime 기록에 실패해도 기존 성공 응답을 유지한다.")
    @Test
    void handleGitHubEvent_keepsSuccessResponseWhenRuntimeRecordingFailsAfterCommentPosting() throws Exception {
        // given
        String deliveryId = "delivery-3";
        Map<String, Object> payload = buildPayload();
        WebhookExecution webhookExecution = WebhookExecution.start(
                "EXECUTION:delivery-3",
                "PR_REVIEW:owner/repo#12",
                deliveryId,
                "owner/repo",
                12,
                "PULL_REQUEST",
                "opened",
                LocalDateTime.of(2026, 4, 3, 10, 10)
        );
        Review review = Review.of("owner/repo", 12, "리뷰 본문", List.of());
        GitHubCommentResponse response = GitHubCommentResponse.of(
                11L,
                "https://github.com/comment/11",
                List.of(PostedInlineComment.of(22L, "https://github.com/comment/22")),
                "리뷰 코멘트가 성공적으로 등록되었습니다."
        );

        when(webhookDeliveryCache.isDuplicate(deliveryId)).thenReturn(false);
        when(agentRuntimeService.startWebhookExecution(eq(deliveryId), any(GitHubEventServiceRequest.class))).thenReturn(webhookExecution);
        when(openAiService.generateReview(any(GitHubEventServiceRequest.class))).thenReturn(review);
        when(gitHubCommentService.comment(eq(review), any(GitHubEventServiceRequest.class))).thenReturn(response);
        doThrow(new RuntimeException("runtime write failed"))
                .when(agentRuntimeService)
                .recordCommentPosted(webhookExecution, response);

        // when & then
        mockMvc.perform(post("/webhook/github")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-GitHub-Event", "pull_request")
                        .header("X-GitHub-Delivery", deliveryId)
                        .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.reviewCommentId").value(11L))
                .andExpect(jsonPath("$.reviewCommentUrl").value("https://github.com/comment/11"))
                .andExpect(jsonPath("$.postedInlineComments[0].id").value(22L))
                .andExpect(jsonPath("$.postedInlineComments[0].htmlUrl").value("https://github.com/comment/22"))
                .andExpect(jsonPath("$.message").value("리뷰 코멘트가 성공적으로 등록되었습니다."));

        verify(webhookDeliveryCache).record(deliveryId);
    }

    @DisplayName("successful comment 이후 runtime 기록이 실패해도 같은 delivery 재요청에서는 추가 comment posting이 발생하지 않는다.")
    @Test
    void handleGitHubEvent_preventsDuplicateCommentPostingWhenRuntimeRecordingFails() throws Exception {
        // given
        String deliveryId = "delivery-4";
        Set<String> recordedDeliveries = new HashSet<>();
        Map<String, Object> payload = buildPayload();
        WebhookExecution webhookExecution = WebhookExecution.start(
                "EXECUTION:delivery-4",
                "PR_REVIEW:owner/repo#12",
                deliveryId,
                "owner/repo",
                12,
                "PULL_REQUEST",
                "opened",
                LocalDateTime.of(2026, 4, 3, 10, 15)
        );
        Review review = Review.of("owner/repo", 12, "리뷰 본문", List.of());
        GitHubCommentResponse response = GitHubCommentResponse.of(11L, "https://github.com/comment/11", List.of(), "ok");

        when(webhookDeliveryCache.isDuplicate(anyString()))
                .thenAnswer(invocation -> recordedDeliveries.contains(invocation.getArgument(0)));
        doAnswer(invocation -> {
            recordedDeliveries.add(invocation.getArgument(0));
            return null;
        }).when(webhookDeliveryCache).record(anyString());
        when(agentRuntimeService.startWebhookExecution(eq(deliveryId), any(GitHubEventServiceRequest.class))).thenReturn(webhookExecution);
        when(openAiService.generateReview(any(GitHubEventServiceRequest.class))).thenReturn(review);
        when(gitHubCommentService.comment(eq(review), any(GitHubEventServiceRequest.class))).thenReturn(response);
        doThrow(new RuntimeException("runtime write failed"))
                .when(agentRuntimeService)
                .recordCommentPosted(webhookExecution, response);

        // when
        mockMvc.perform(post("/webhook/github")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-GitHub-Event", "pull_request")
                        .header("X-GitHub-Delivery", deliveryId)
                        .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isOk());

        // then
        mockMvc.perform(post("/webhook/github")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-GitHub-Event", "pull_request")
                        .header("X-GitHub-Delivery", deliveryId)
                        .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isOk())
                .andExpect(content().string(""));

        verify(gitHubCommentService, times(1)).comment(eq(review), any(GitHubEventServiceRequest.class));
        verify(agentRuntimeService, times(1)).startWebhookExecution(eq(deliveryId), any(GitHubEventServiceRequest.class));
    }

    @DisplayName("duplicate delivery는 기존처럼 조기 종료한다.")
    @Test
    void handleGitHubEvent_returnsOkWhenDeliveryIsDuplicate() throws Exception {
        // given
        String deliveryId = "delivery-duplicate";

        when(webhookDeliveryCache.isDuplicate(deliveryId)).thenReturn(true);

        // when & then
        mockMvc.perform(post("/webhook/github")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-GitHub-Event", "pull_request")
                        .header("X-GitHub-Delivery", deliveryId)
                        .content(objectMapper.writeValueAsString(buildPayload())))
                .andExpect(status().isOk())
                .andExpect(content().string(""));

        verify(agentRuntimeService, never()).startWebhookExecution(anyString(), any(GitHubEventServiceRequest.class));
        verify(openAiService, never()).generateReview(any(GitHubEventServiceRequest.class));
        verify(gitHubCommentService, never()).comment(any(Review.class), any(GitHubEventServiceRequest.class));
    }

    @DisplayName("pull_request가 아닌 이벤트는 기존처럼 조기 종료한다.")
    @Test
    void handleGitHubEvent_returnsOkWhenEventIsNotPullRequest() throws Exception {
        // given
        String deliveryId = "delivery-non-pr";

        when(webhookDeliveryCache.isDuplicate(deliveryId)).thenReturn(false);

        // when & then
        mockMvc.perform(post("/webhook/github")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-GitHub-Event", "push")
                        .header("X-GitHub-Delivery", deliveryId)
                        .content(objectMapper.writeValueAsString(buildPayload())))
                .andExpect(status().isOk())
                .andExpect(content().string(""));

        verify(agentRuntimeService, never()).startWebhookExecution(anyString(), any(GitHubEventServiceRequest.class));
        verify(openAiService, never()).generateReview(any(GitHubEventServiceRequest.class));
        verify(gitHubCommentService, never()).comment(any(Review.class), any(GitHubEventServiceRequest.class));
    }

    @DisplayName("installation 정보가 없으면 실패 응답으로 종료하고 다음 흐름으로 진행하지 않는다.")
    @Test
    void handleGitHubEvent_failsWhenInstallationIsMissing() throws Exception {
        // given
        String deliveryId = "delivery-missing-installation";
        Map<String, Object> payload = buildPayload();
        payload.remove("installation");

        when(webhookDeliveryCache.isDuplicate(deliveryId)).thenReturn(false);

        // when & then
        assertThatThrownBy(() -> mockMvc.perform(post("/webhook/github")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-GitHub-Event", "pull_request")
                        .header("X-GitHub-Delivery", deliveryId)
                        .content(objectMapper.writeValueAsString(payload))))
                .rootCause()
                .isInstanceOfSatisfying(AgileRunnerException.class, exception -> {
                    assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.GITHUB_INSTALLATION_ID_MISSING);
                });

        verify(agentRuntimeService, never()).startWebhookExecution(anyString(), any(GitHubEventServiceRequest.class));
        verify(openAiService, never()).generateReview(any(GitHubEventServiceRequest.class));
        verify(gitHubCommentService, never()).comment(any(Review.class), any(GitHubEventServiceRequest.class));
    }

    @DisplayName("installation 정보가 없으면 공통 예외와 오류 코드로 분류한다.")
    @Test
    void handleGitHubEvent_mapsMissingInstallationToAgileRunnerException() throws Exception {
        // given
        String deliveryId = "delivery-missing-installation-error-code";
        Map<String, Object> payload = buildPayload();
        payload.remove("installation");

        when(webhookDeliveryCache.isDuplicate(deliveryId)).thenReturn(false);

        // when & then
        assertThatThrownBy(() -> mockMvc.perform(post("/webhook/github")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-GitHub-Event", "pull_request")
                        .header("X-GitHub-Delivery", deliveryId)
                        .content(objectMapper.writeValueAsString(payload))))
                .rootCause()
                .isInstanceOfSatisfying(AgileRunnerException.class, exception -> {
                    assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.GITHUB_INSTALLATION_ID_MISSING);
                });
    }

    @DisplayName("리뷰 생성에 실패하면 실패 응답으로 종료하고 코멘트 등록은 진행하지 않는다.")
    @Test
    void handleGitHubEvent_failsWhenReviewGenerationFails() throws Exception {
        // given
        String deliveryId = "delivery-review-failure";
        Map<String, Object> payload = buildPayload();
        WebhookExecution webhookExecution = WebhookExecution.start(
                "EXECUTION:delivery-review-failure",
                "PR_REVIEW:owner/repo#12",
                deliveryId,
                "owner/repo",
                12,
                "PULL_REQUEST",
                "opened",
                LocalDateTime.of(2026, 4, 6, 12, 0)
        );

        when(webhookDeliveryCache.isDuplicate(deliveryId)).thenReturn(false);
        when(agentRuntimeService.startWebhookExecution(eq(deliveryId), any(GitHubEventServiceRequest.class))).thenReturn(webhookExecution);
        when(openAiService.generateReview(any(GitHubEventServiceRequest.class)))
                .thenThrow(new AgileRunnerException(ErrorCode.OPENAI_REVIEW_FAILED, "리뷰 생성에 실패했습니다."));

        // when & then
        assertThatThrownBy(() -> mockMvc.perform(post("/webhook/github")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-GitHub-Event", "pull_request")
                        .header("X-GitHub-Delivery", deliveryId)
                        .content(objectMapper.writeValueAsString(payload))))
                .rootCause()
                .isInstanceOfSatisfying(AgileRunnerException.class, exception -> {
                    assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.OPENAI_REVIEW_FAILED);
                });

        verify(gitHubCommentService, never()).comment(any(Review.class), any(GitHubEventServiceRequest.class));
    }

    @DisplayName("리뷰 생성 결과가 null이면 오류 코드를 남기고 빈 응답으로 종료한다.")
    @Test
    void handleGitHubEvent_recordsErrorCodeWhenReviewIsNull() throws Exception {
        // given
        String deliveryId = "delivery-null-review";
        Map<String, Object> payload = buildPayload();
        WebhookExecution webhookExecution = WebhookExecution.start(
                "EXECUTION:delivery-null-review",
                "PR_REVIEW:owner/repo#12",
                deliveryId,
                "owner/repo",
                12,
                "PULL_REQUEST",
                "opened",
                LocalDateTime.of(2026, 4, 6, 12, 30)
        );

        when(webhookDeliveryCache.isDuplicate(deliveryId)).thenReturn(false);
        when(agentRuntimeService.startWebhookExecution(eq(deliveryId), any(GitHubEventServiceRequest.class))).thenReturn(webhookExecution);
        when(openAiService.generateReview(any(GitHubEventServiceRequest.class))).thenReturn(null);

        // when & then
        mockMvc.perform(post("/webhook/github")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-GitHub-Event", "pull_request")
                        .header("X-GitHub-Delivery", deliveryId)
                        .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isOk())
                .andExpect(content().string(""));

        ArgumentCaptor<Exception> exceptionCaptor = ArgumentCaptor.forClass(Exception.class);
        verify(agentRuntimeService).recordFailure(
                eq(webhookExecution),
                eq(AgentRuntimeService.STEP_REVIEW_GENERATED),
                exceptionCaptor.capture()
        );
        assertThat(exceptionCaptor.getValue())
                .isInstanceOfSatisfying(AgileRunnerException.class, exception ->
                        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.OPENAI_REVIEW_FAILED)
                );
        verify(gitHubCommentService, never()).comment(any(Review.class), any(GitHubEventServiceRequest.class));
    }

    private Map<String, Object> buildPayload() {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("action", "opened");
        payload.put("installation", Map.of("id", 100L));
        payload.put("repository", Map.of("full_name", "owner/repo"));
        payload.put("pull_request", Map.of("number", 12));
        return payload;
    }
}
