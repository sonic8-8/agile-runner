package com.agilerunner.api.service;

import com.agilerunner.api.service.github.request.GitHubEventServiceRequest;
import com.agilerunner.client.github.auth.GitHubClientFactory;
import com.agilerunner.domain.ParsedFilePatch;
import com.agilerunner.domain.Review;
import com.agilerunner.domain.executioncontrol.ExecutionControlMode;
import com.agilerunner.domain.exception.AgileRunnerException;
import com.agilerunner.domain.exception.ErrorCode;
import com.agilerunner.domain.exception.FailureDisposition;
import com.agilerunner.domain.exception.FailureDispositionPolicy;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.kohsuke.github.GHPullRequest;
import org.kohsuke.github.GHRepository;
import org.kohsuke.github.GitHub;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static com.agilerunner.GitHubEventType.PULL_REQUEST;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class OpenAiServiceTest {

    @DisplayName("OpenAI 설정이 없으면 리뷰 생성에 실패한다.")
    @Test
    void generateReview_failsWhenChatClientIsMissing() throws Exception {
        // given
        @SuppressWarnings("unchecked")
        ObjectProvider<ChatClient> chatClientProvider = mock(ObjectProvider.class);
        GitHubClientFactory gitHubClientFactory = mock(GitHubClientFactory.class);
        GitHubPatchService gitHubPatchService = mock(GitHubPatchService.class);
        ObjectMapper objectMapper = new ObjectMapper();
        OpenAiService service = new OpenAiService(chatClientProvider, gitHubClientFactory, gitHubPatchService, objectMapper);

        GitHub gitHub = mock(GitHub.class);
        GHRepository repository = mock(GHRepository.class);
        GHPullRequest pullRequest = mock(GHPullRequest.class);
        GitHubEventServiceRequest request = GitHubEventServiceRequest.of(PULL_REQUEST, buildPayload(), 100L);

        ReflectionTestUtils.setField(service, "basePrompt", "{DIFF_JSON}");
        when(chatClientProvider.getIfAvailable()).thenReturn(null);
        when(gitHubClientFactory.createGitHubClient(100L)).thenReturn(gitHub);
        when(gitHub.getRepository("owner/repo")).thenReturn(repository);
        when(repository.getPullRequest(12)).thenReturn(pullRequest);
        when(gitHubPatchService.buildParsedFilePatches(pullRequest)).thenReturn(List.of(ParsedFilePatch.of("src/Main.java", List.of())));

        // when & then
        assertThatThrownBy(() -> service.generateReview(request))
                .isInstanceOfSatisfying(AgileRunnerException.class, exception -> {
                    assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.OPENAI_CLIENT_MISSING);
                });
    }

    @DisplayName("OpenAI 설정 누락 실패는 같은 대응 기준으로 분류된다.")
    @Test
    void generateReview_missingChatClient_isClassifiedConsistently() throws Exception {
        // given
        @SuppressWarnings("unchecked")
        ObjectProvider<ChatClient> chatClientProvider = mock(ObjectProvider.class);
        GitHubClientFactory gitHubClientFactory = mock(GitHubClientFactory.class);
        GitHubPatchService gitHubPatchService = mock(GitHubPatchService.class);
        ObjectMapper objectMapper = new ObjectMapper();
        OpenAiService service = new OpenAiService(chatClientProvider, gitHubClientFactory, gitHubPatchService, objectMapper);
        FailureDispositionPolicy policy = new FailureDispositionPolicy();

        GitHub gitHub = mock(GitHub.class);
        GHRepository repository = mock(GHRepository.class);
        GHPullRequest pullRequest = mock(GHPullRequest.class);
        GitHubEventServiceRequest request = GitHubEventServiceRequest.of(PULL_REQUEST, buildPayload(), 100L);

        ReflectionTestUtils.setField(service, "basePrompt", "{DIFF_JSON}");
        when(chatClientProvider.getIfAvailable()).thenReturn(null);
        when(gitHubClientFactory.createGitHubClient(100L)).thenReturn(gitHub);
        when(gitHub.getRepository("owner/repo")).thenReturn(repository);
        when(repository.getPullRequest(12)).thenReturn(pullRequest);
        when(gitHubPatchService.buildParsedFilePatches(pullRequest)).thenReturn(List.of(ParsedFilePatch.of("src/Main.java", List.of())));

        // when & then
        assertThatThrownBy(() -> service.generateReview(request))
                .isInstanceOfSatisfying(AgileRunnerException.class, exception -> {
                    assertThat(policy.classify(exception)).isEqualTo(FailureDisposition.MANUAL_ACTION_REQUIRED);
                });
    }

    @DisplayName("선택 파일 경로가 있으면 리뷰 생성 입력은 해당 경로 patch만 포함한다.")
    @Test
    void generateReview_limitsPromptInputToSelectedPaths() throws Exception {
        // given
        @SuppressWarnings("unchecked")
        ObjectProvider<ChatClient> chatClientProvider = mock(ObjectProvider.class);
        ChatClient chatClient = mock(ChatClient.class);
        ChatClient.ChatClientRequestSpec requestSpec = mock(ChatClient.ChatClientRequestSpec.class);
        ChatClient.CallResponseSpec responseSpec = mock(ChatClient.CallResponseSpec.class);
        GitHubClientFactory gitHubClientFactory = mock(GitHubClientFactory.class);
        GitHubPatchService gitHubPatchService = mock(GitHubPatchService.class);
        ObjectMapper objectMapper = new ObjectMapper();
        OpenAiService service = new OpenAiService(chatClientProvider, gitHubClientFactory, gitHubPatchService, objectMapper);

        GitHub gitHub = mock(GitHub.class);
        GHRepository repository = mock(GHRepository.class);
        GHPullRequest pullRequest = mock(GHPullRequest.class);
        GitHubEventServiceRequest request = GitHubEventServiceRequest.of(
                PULL_REQUEST,
                buildPayload(),
                100L,
                ExecutionControlMode.NORMAL,
                List.of("src/Main.java")
        );

        ReflectionTestUtils.setField(service, "basePrompt", "{DIFF_JSON}");
        when(chatClientProvider.getIfAvailable()).thenReturn(chatClient);
        when(chatClient.prompt()).thenReturn(requestSpec);
        when(requestSpec.user(anyString())).thenReturn(requestSpec);
        when(requestSpec.call()).thenReturn(responseSpec);
        when(gitHubClientFactory.createGitHubClient(100L)).thenReturn(gitHub);
        when(gitHub.getRepository("owner/repo")).thenReturn(repository);
        when(repository.getPullRequest(12)).thenReturn(pullRequest);
        when(gitHubPatchService.buildParsedFilePatches(pullRequest)).thenReturn(List.of(
                ParsedFilePatch.of("src/Main.java", List.of()),
                ParsedFilePatch.of("src/Test.java", List.of())
        ));
        when(responseSpec.content()).thenReturn("{\"reviewBody\":\"리뷰 본문\",\"inlineComments\":[]}");

        // when
        Review review = service.generateReview(request);

        // then
        assertThat(review.getRepositoryName()).isEqualTo("owner/repo");
        verify(requestSpec).user("[{\"path\":\"src/Main.java\",\"hunks\":[]}]");
    }

    @DisplayName("선택 파일 경로가 PR diff와 매칭되지 않으면 리뷰 생성 입력은 빈 목록으로 처리한다.")
    @Test
    void generateReview_usesEmptyPromptInputWhenSelectedPathsDoNotMatchDiff() throws Exception {
        // given
        @SuppressWarnings("unchecked")
        ObjectProvider<ChatClient> chatClientProvider = mock(ObjectProvider.class);
        ChatClient chatClient = mock(ChatClient.class);
        ChatClient.ChatClientRequestSpec requestSpec = mock(ChatClient.ChatClientRequestSpec.class);
        ChatClient.CallResponseSpec responseSpec = mock(ChatClient.CallResponseSpec.class);
        GitHubClientFactory gitHubClientFactory = mock(GitHubClientFactory.class);
        GitHubPatchService gitHubPatchService = mock(GitHubPatchService.class);
        ObjectMapper objectMapper = new ObjectMapper();
        OpenAiService service = new OpenAiService(chatClientProvider, gitHubClientFactory, gitHubPatchService, objectMapper);

        GitHub gitHub = mock(GitHub.class);
        GHRepository repository = mock(GHRepository.class);
        GHPullRequest pullRequest = mock(GHPullRequest.class);
        GitHubEventServiceRequest request = GitHubEventServiceRequest.of(
                PULL_REQUEST,
                buildPayload(),
                100L,
                ExecutionControlMode.NORMAL,
                List.of("docs/README.md")
        );

        ReflectionTestUtils.setField(service, "basePrompt", "{DIFF_JSON}");
        when(chatClientProvider.getIfAvailable()).thenReturn(chatClient);
        when(chatClient.prompt()).thenReturn(requestSpec);
        when(requestSpec.user(anyString())).thenReturn(requestSpec);
        when(requestSpec.call()).thenReturn(responseSpec);
        when(gitHubClientFactory.createGitHubClient(100L)).thenReturn(gitHub);
        when(gitHub.getRepository("owner/repo")).thenReturn(repository);
        when(repository.getPullRequest(12)).thenReturn(pullRequest);
        when(gitHubPatchService.buildParsedFilePatches(pullRequest)).thenReturn(List.of(
                ParsedFilePatch.of("src/Main.java", List.of())
        ));
        when(responseSpec.content()).thenReturn("{\"reviewBody\":\"리뷰 본문\",\"inlineComments\":[]}");

        // when
        service.generateReview(request);

        // then
        verify(requestSpec).user("[]");
    }

    @DisplayName("선택 파일 경로가 비어 있으면 리뷰 생성 입력은 기존 전체 patch 목록을 유지한다.")
    @Test
    void generateReview_keepsFullPromptInputWhenSelectedPathsAreEmpty() throws Exception {
        // given
        @SuppressWarnings("unchecked")
        ObjectProvider<ChatClient> chatClientProvider = mock(ObjectProvider.class);
        ChatClient chatClient = mock(ChatClient.class);
        ChatClient.ChatClientRequestSpec requestSpec = mock(ChatClient.ChatClientRequestSpec.class);
        ChatClient.CallResponseSpec responseSpec = mock(ChatClient.CallResponseSpec.class);
        GitHubClientFactory gitHubClientFactory = mock(GitHubClientFactory.class);
        GitHubPatchService gitHubPatchService = mock(GitHubPatchService.class);
        ObjectMapper objectMapper = new ObjectMapper();
        OpenAiService service = new OpenAiService(chatClientProvider, gitHubClientFactory, gitHubPatchService, objectMapper);

        GitHub gitHub = mock(GitHub.class);
        GHRepository repository = mock(GHRepository.class);
        GHPullRequest pullRequest = mock(GHPullRequest.class);
        GitHubEventServiceRequest request = GitHubEventServiceRequest.of(
                PULL_REQUEST,
                buildPayload(),
                100L,
                ExecutionControlMode.NORMAL,
                List.of()
        );

        ReflectionTestUtils.setField(service, "basePrompt", "{DIFF_JSON}");
        when(chatClientProvider.getIfAvailable()).thenReturn(chatClient);
        when(chatClient.prompt()).thenReturn(requestSpec);
        when(requestSpec.user(anyString())).thenReturn(requestSpec);
        when(requestSpec.call()).thenReturn(responseSpec);
        when(gitHubClientFactory.createGitHubClient(100L)).thenReturn(gitHub);
        when(gitHub.getRepository("owner/repo")).thenReturn(repository);
        when(repository.getPullRequest(12)).thenReturn(pullRequest);
        when(gitHubPatchService.buildParsedFilePatches(pullRequest)).thenReturn(List.of(
                ParsedFilePatch.of("src/Main.java", List.of()),
                ParsedFilePatch.of("src/Test.java", List.of())
        ));
        when(responseSpec.content()).thenReturn("{\"reviewBody\":\"리뷰 본문\",\"inlineComments\":[]}");

        // when
        service.generateReview(request);

        // then
        verify(requestSpec).user("[{\"path\":\"src/Main.java\",\"hunks\":[]},{\"path\":\"src/Test.java\",\"hunks\":[]}]");
    }

    private Map<String, Object> buildPayload() {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("action", "opened");
        payload.put("repository", Map.of("full_name", "owner/repo"));
        payload.put("pull_request", Map.of("number", 12));
        return payload;
    }
}
