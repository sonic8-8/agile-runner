package com.agilerunner.api.service;

import com.agilerunner.api.service.dto.GitHubEventServiceRequest;
import com.agilerunner.client.github.auth.GitHubClientFactory;
import com.agilerunner.domain.ParsedFilePatch;
import com.agilerunner.domain.exception.AgileRunnerException;
import com.agilerunner.domain.exception.ErrorCode;
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
import static org.mockito.Mockito.mock;
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

    private Map<String, Object> buildPayload() {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("action", "opened");
        payload.put("repository", Map.of("full_name", "owner/repo"));
        payload.put("pull_request", Map.of("number", 12));
        return payload;
    }
}
