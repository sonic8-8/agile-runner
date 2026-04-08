package com.agilerunner.api.service.review;

import com.agilerunner.api.service.GitHubCommentService;
import com.agilerunner.api.service.OpenAiService;
import com.agilerunner.api.service.agentruntime.AgentRuntimeService;
import com.agilerunner.api.service.dto.GitHubCommentResponse;
import com.agilerunner.api.service.github.request.GitHubEventServiceRequest;
import com.agilerunner.api.service.github.response.GitHubCommentExecutionResult;
import com.agilerunner.api.service.review.request.ManualRerunServiceRequest;
import com.agilerunner.api.service.review.response.ManualRerunServiceResponse;
import com.agilerunner.domain.Review;
import com.agilerunner.domain.agentruntime.WebhookExecution;
import com.agilerunner.domain.exception.AgileRunnerException;
import com.agilerunner.domain.exception.ErrorCode;
import com.agilerunner.domain.executioncontrol.ExecutionControlMode;
import com.agilerunner.domain.executioncontrol.GitHubWriteSkipReason;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ManualRerunServiceTest {

    @DisplayName("NORMAL 수동 재실행은 기존 리뷰 생성과 GitHub 코멘트 작성 흐름을 재사용한다.")
    @Test
    void rerun_reusesReviewGenerationAndCommentExecutionInNormalMode() {
        // given
        OpenAiService openAiService = mock(OpenAiService.class);
        GitHubCommentService gitHubCommentService = mock(GitHubCommentService.class);
        AgentRuntimeService agentRuntimeService = mock(AgentRuntimeService.class);
        ManualRerunService service = new ManualRerunService(openAiService, gitHubCommentService, agentRuntimeService);
        ManualRerunServiceRequest request = ManualRerunServiceRequest.of(
                "owner/repo",
                12,
                100L,
                ExecutionControlMode.NORMAL
        );
        Review review = Review.of("owner/repo", 12, "리뷰 본문", List.of());
        WebhookExecution runtimeExecution = mock(WebhookExecution.class);
        GitHubCommentExecutionResult executionResult = GitHubCommentExecutionResult.written(
                ExecutionControlMode.NORMAL,
                0,
                GitHubCommentResponse.of(101L, "https://github.com/comment/101", List.of(), "ok")
        );
        when(runtimeExecution.getExecutionKey()).thenReturn("EXECUTION:manual-rerun-12");
        when(agentRuntimeService.startManualRerunExecution(request)).thenReturn(runtimeExecution);
        when(openAiService.generateReview(any(GitHubEventServiceRequest.class))).thenReturn(review);
        when(gitHubCommentService.execute(any(Review.class), any(GitHubEventServiceRequest.class))).thenReturn(executionResult);

        // when
        ManualRerunServiceResponse response = service.rerun(request);

        // then
        ArgumentCaptor<GitHubEventServiceRequest> requestCaptor = ArgumentCaptor.forClass(GitHubEventServiceRequest.class);
        verify(openAiService).generateReview(requestCaptor.capture());
        verify(gitHubCommentService).execute(review, requestCaptor.getValue());

        GitHubEventServiceRequest serviceRequest = requestCaptor.getValue();
        assertThat(serviceRequest.getRepositoryName()).isEqualTo("owner/repo");
        assertThat(serviceRequest.getPullRequestNumber()).isEqualTo(12);
        assertThat(serviceRequest.getInstallationId()).isEqualTo(100L);
        assertThat(serviceRequest.getExecutionControlMode()).isEqualTo(ExecutionControlMode.NORMAL);
        assertThat(serviceRequest.getAction()).isEqualTo("manual_rerun");

        verify(agentRuntimeService).recordReviewGenerated(runtimeExecution, review);
        verify(agentRuntimeService).recordExecutionResult(runtimeExecution, executionResult);

        assertThat(response.getExecutionKey()).isEqualTo("EXECUTION:manual-rerun-12");
        assertThat(response.getExecutionControlMode()).isEqualTo(ExecutionControlMode.NORMAL);
        assertThat(response.isWritePerformed()).isTrue();
    }

    @DisplayName("DRY_RUN 수동 재실행은 기존 dry-run 분기를 재사용해 write 없이 응답한다.")
    @Test
    void rerun_reusesDryRunBranchWithoutGitHubWrite() {
        // given
        OpenAiService openAiService = mock(OpenAiService.class);
        GitHubCommentService gitHubCommentService = mock(GitHubCommentService.class);
        AgentRuntimeService agentRuntimeService = mock(AgentRuntimeService.class);
        ManualRerunService service = new ManualRerunService(openAiService, gitHubCommentService, agentRuntimeService);
        ManualRerunServiceRequest request = ManualRerunServiceRequest.of(
                "owner/repo",
                12,
                100L,
                ExecutionControlMode.DRY_RUN
        );
        Review review = Review.of("owner/repo", 12, "리뷰 본문", List.of());
        WebhookExecution runtimeExecution = mock(WebhookExecution.class);
        GitHubCommentExecutionResult executionResult = GitHubCommentExecutionResult.skipped(
                ExecutionControlMode.DRY_RUN,
                GitHubWriteSkipReason.DRY_RUN,
                0
        );
        when(runtimeExecution.getExecutionKey()).thenReturn("EXECUTION:manual-rerun-13");
        when(agentRuntimeService.startManualRerunExecution(request)).thenReturn(runtimeExecution);
        when(openAiService.generateReview(any(GitHubEventServiceRequest.class))).thenReturn(review);
        when(gitHubCommentService.execute(any(Review.class), any(GitHubEventServiceRequest.class))).thenReturn(executionResult);

        // when
        ManualRerunServiceResponse response = service.rerun(request);

        // then
        ArgumentCaptor<GitHubEventServiceRequest> requestCaptor = ArgumentCaptor.forClass(GitHubEventServiceRequest.class);
        verify(openAiService).generateReview(requestCaptor.capture());
        verify(gitHubCommentService).execute(review, requestCaptor.getValue());

        GitHubEventServiceRequest serviceRequest = requestCaptor.getValue();
        assertThat(serviceRequest.getRepositoryName()).isEqualTo("owner/repo");
        assertThat(serviceRequest.getPullRequestNumber()).isEqualTo(12);
        assertThat(serviceRequest.getInstallationId()).isEqualTo(100L);
        assertThat(serviceRequest.getExecutionControlMode()).isEqualTo(ExecutionControlMode.DRY_RUN);
        assertThat(serviceRequest.getAction()).isEqualTo("manual_rerun");

        verify(agentRuntimeService).recordReviewGenerated(runtimeExecution, review);
        verify(agentRuntimeService).recordExecutionResult(runtimeExecution, executionResult);

        assertThat(response.getExecutionKey()).isEqualTo("EXECUTION:manual-rerun-13");
        assertThat(response.getExecutionControlMode()).isEqualTo(ExecutionControlMode.DRY_RUN);
        assertThat(response.isWritePerformed()).isFalse();
    }

    @DisplayName("수동 재실행 중 리뷰 생성이 실패해도 runtime execution key와 no-write 응답을 반환한다.")
    @Test
    void rerun_returnsRuntimeExecutionKeyWhenReviewGenerationFails() {
        // given
        OpenAiService openAiService = mock(OpenAiService.class);
        GitHubCommentService gitHubCommentService = mock(GitHubCommentService.class);
        AgentRuntimeService agentRuntimeService = mock(AgentRuntimeService.class);
        ManualRerunService service = new ManualRerunService(openAiService, gitHubCommentService, agentRuntimeService);
        ManualRerunServiceRequest request = ManualRerunServiceRequest.of(
                "owner/repo",
                12,
                100L,
                ExecutionControlMode.DRY_RUN
        );
        WebhookExecution runtimeExecution = mock(WebhookExecution.class);
        AgileRunnerException failure = new AgileRunnerException(
                ErrorCode.GITHUB_APP_CONFIGURATION_MISSING,
                "GitHub App ID가 설정되지 않았습니다."
        );
        when(runtimeExecution.getExecutionKey()).thenReturn("EXECUTION:manual-rerun-14");
        when(agentRuntimeService.startManualRerunExecution(request)).thenReturn(runtimeExecution);
        when(openAiService.generateReview(any(GitHubEventServiceRequest.class))).thenThrow(failure);

        // when
        ManualRerunServiceResponse response = service.rerun(request);

        // then
        verify(agentRuntimeService).recordFailure(runtimeExecution, AgentRuntimeService.STEP_REVIEW_GENERATED, failure);
        verify(gitHubCommentService, never()).execute(any(Review.class), any(GitHubEventServiceRequest.class));

        assertThat(response.getExecutionKey()).isEqualTo("EXECUTION:manual-rerun-14");
        assertThat(response.getExecutionControlMode()).isEqualTo(ExecutionControlMode.DRY_RUN);
        assertThat(response.isWritePerformed()).isFalse();
    }
}
