package com.agilerunner.api.service;

import com.agilerunner.domain.ParsedFilePatch;
import com.agilerunner.api.service.github.request.GitHubEventServiceRequest;
import com.agilerunner.api.service.dto.ReviewResponse;
import com.agilerunner.client.github.auth.GitHubClientFactory;
import com.agilerunner.domain.Review;
import com.agilerunner.domain.exception.AgileRunnerException;
import com.agilerunner.domain.exception.ErrorCode;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.kohsuke.github.GHPullRequest;
import org.kohsuke.github.GHRepository;
import org.kohsuke.github.GitHub;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class OpenAiService {

    private static final String DIFF_JSON_PLACEHOLDER = "{DIFF_JSON}";

    private final ObjectProvider<ChatClient> chatClientProvider;
    private final GitHubClientFactory gitHubClientFactory;
    private final GitHubPatchService gitHubPatchService;
    private final ObjectMapper objectMapper;

    @Value("classpath:prompts/review-pr-prompt.txt")
    private Resource promptResource;

    private String basePrompt;

    @PostConstruct
    void loadBasePrompt() throws IOException {
        this.basePrompt = promptResource.getContentAsString(StandardCharsets.UTF_8);
    }

    public Review generateReview(GitHubEventServiceRequest request) {
        String repositoryName = request.getRepositoryName();
        int pullRequestNumber = request.getPullRequestNumber();

        try {
            ReviewResponse reviewResponse = requestOpenAiReview(request, repositoryName, pullRequestNumber);
            return Review.from(repositoryName, pullRequestNumber, reviewResponse);
        } catch (AgileRunnerException exception) {
            log.error("리뷰 생성 실패, repository={}, PR={}", repositoryName, pullRequestNumber, exception);
            throw exception;
        } catch (Exception e) {
            log.error("리뷰 생성 실패, repository={}, PR={}", repositoryName, pullRequestNumber, e);
            throw new AgileRunnerException(
                    ErrorCode.OPENAI_REVIEW_FAILED,
                    "리뷰 생성에 실패했습니다.",
                    e
            );
        }
    }

    private ReviewResponse requestOpenAiReview(GitHubEventServiceRequest request, String repositoryName, int pullRequestNumber) throws Exception {
        GHPullRequest pullRequest = loadPullRequest(request, repositoryName, pullRequestNumber);
        List<ParsedFilePatch> parsedFilePatches = gitHubPatchService.buildParsedFilePatches(pullRequest);
        String prompt = buildReviewPromptFrom(parsedFilePatches);
        return callOpenAiWith(prompt);
    }

    private GHPullRequest loadPullRequest(GitHubEventServiceRequest request, String repositoryName, int pullRequestNumber) throws Exception {
        GitHub gitHub = gitHubClientFactory.createGitHubClient(request.getInstallationId());
        GHRepository repository = gitHub.getRepository(repositoryName);
        return repository.getPullRequest(pullRequestNumber);
    }

    private String buildReviewPromptFrom(List<ParsedFilePatch> parsedFilePatches) throws IOException {
        String diffJson = objectMapper.writeValueAsString(parsedFilePatches);
        return basePrompt.replace(DIFF_JSON_PLACEHOLDER, diffJson);
    }

    private ReviewResponse callOpenAiWith(String prompt) throws JsonProcessingException {
        ChatClient chatClient = loadChatClient();

        String responseJson = chatClient.prompt()
                .user(prompt)
                .call()
                .content();

        return objectMapper.readValue(responseJson, ReviewResponse.class);
    }

    private ChatClient loadChatClient() {
        ChatClient chatClient = chatClientProvider.getIfAvailable();
        if (chatClient == null) {
            throw new AgileRunnerException(
                    ErrorCode.OPENAI_CLIENT_MISSING,
                    "OpenAI API Key가 설정되지 않았습니다."
            );
        }

        return chatClient;
    }
}
