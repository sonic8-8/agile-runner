package com.agilerunner.api.service;

import com.agilerunner.domain.ParsedFilePatch;
import com.agilerunner.api.service.dto.GitHubEventServiceRequest;
import com.agilerunner.api.service.dto.ReviewResponse;
import com.agilerunner.config.GitHubClientFactory;
import com.agilerunner.domain.Review;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.kohsuke.github.GHPullRequest;
import org.kohsuke.github.GHRepository;
import org.kohsuke.github.GitHub;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class OpenAiService {

    private static final String KEY_REPOSITORY = "repository";
    private static final String KEY_PULL_REQUEST = "pull_request";
    private static final String KEY_NUMBER = "number";
    private static final String KEY_FULL_NAME = "full_name";
    private static final String DIFF_JSON_PLACEHOLDER = "{DIFF_JSON}";

    private final ChatClient chatClient;
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
        String repositoryName = extractRepositoryNameFrom(request);
        int pullRequestNumber = extractPullRequestNumberFrom(request);

        try {
            ReviewResponse reviewResponse = requestOpenAiReview(request, repositoryName, pullRequestNumber);
            return Review.from(repositoryName, pullRequestNumber, reviewResponse);
        } catch (Exception e) {
            log.error("리뷰 생성 실패, repository={}, PR={}", repositoryName, pullRequestNumber, e);
            throw new RuntimeException("리뷰 생성에 실패했습니다.");
        }
    }

    private String extractRepositoryNameFrom(GitHubEventServiceRequest request) {
        Map<String, Object> payload = request.payload();
        Map<String, Object> repository = (Map<String, Object>) payload.get(KEY_REPOSITORY);
        return (String) repository.get(KEY_FULL_NAME);
    }

    private int extractPullRequestNumberFrom(GitHubEventServiceRequest request) {
        Map<String, Object> payload = request.payload();
        Map<String, Object> pullRequest = (Map<String, Object>) payload.get(KEY_PULL_REQUEST);
        return ((Number) pullRequest.get(KEY_NUMBER)).intValue();
    }

    private ReviewResponse requestOpenAiReview(GitHubEventServiceRequest request, String repositoryName, int pullRequestNumber) throws Exception {
        GHPullRequest pullRequest = loadPullRequest(request, repositoryName, pullRequestNumber);
        List<ParsedFilePatch> parsedFilePatches = gitHubPatchService.buildParsedFilePatches(pullRequest);
        String prompt = buildReviewPromptFrom(parsedFilePatches);
        return callOpenAiWith(prompt);
    }

    private GHPullRequest loadPullRequest(GitHubEventServiceRequest request, String repositoryName, int pullRequestNumber) throws Exception {
        GitHub gitHub = gitHubClientFactory.createGitHubClient(request.installationId());
        GHRepository repository = gitHub.getRepository(repositoryName);
        return repository.getPullRequest(pullRequestNumber);
    }

    private String buildReviewPromptFrom(List<ParsedFilePatch> parsedFilePatches) throws IOException {
        String diffJson = objectMapper.writeValueAsString(parsedFilePatches);
        return basePrompt.replace(DIFF_JSON_PLACEHOLDER, diffJson);
    }

    private ReviewResponse callOpenAiWith(String prompt) throws JsonProcessingException {
        String responseJson = chatClient.prompt()
                .user(prompt)
                .call()
                .content();

        return objectMapper.readValue(responseJson, ReviewResponse.class);
    }
}
