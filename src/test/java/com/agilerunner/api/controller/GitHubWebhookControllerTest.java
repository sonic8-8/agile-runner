package com.agilerunner.api.controller;

import com.agilerunner.api.service.GitHubCommentService;
import com.agilerunner.api.service.OpenAiService;
import com.agilerunner.api.service.agentruntime.AgentRuntimeService;
import com.agilerunner.api.service.dto.GitHubCommentResponse;
import com.agilerunner.api.service.dto.GitHubEventServiceRequest;
import com.agilerunner.api.service.dto.PostedInlineComment;
import com.agilerunner.domain.Review;
import com.agilerunner.domain.agentruntime.ReviewRun;
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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
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
        ReviewRun reviewRun = ReviewRun.start(
                "RUN:delivery-1",
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
        when(agentRuntimeService.startReviewRun(eq(deliveryId), any(GitHubEventServiceRequest.class))).thenReturn(reviewRun);
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
        verify(agentRuntimeService).startReviewRun(eq(deliveryId), requestCaptor.capture());

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
        ReviewRun reviewRun = ReviewRun.start(
                "RUN:delivery-2",
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
        when(agentRuntimeService.startReviewRun(eq(deliveryId), any(GitHubEventServiceRequest.class))).thenReturn(reviewRun);
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

    private Map<String, Object> buildPayload() {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("action", "opened");
        payload.put("installation", Map.of("id", 100L));
        payload.put("repository", Map.of("full_name", "owner/repo"));
        payload.put("pull_request", Map.of("number", 12));
        return payload;
    }
}
