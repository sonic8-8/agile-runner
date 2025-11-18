package com.agilerunner.api.service;

import com.agilerunner.domain.FileDiff;
import com.agilerunner.api.service.dto.GitHubEventServiceRequest;
import com.agilerunner.api.service.dto.ReviewResponse;
import com.agilerunner.config.GitHubClientFactory;
import com.agilerunner.domain.Review;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
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
import java.util.Set;

@Service
@RequiredArgsConstructor
@Slf4j
public class OpenAiService {

    private final ChatClient chatClient;
    private final GitHubClientFactory gitHubClientFactory;
    private final GitHubDiffService gitHubDiffService;
    private final ObjectMapper objectMapper;

    @Value("classpath:prompts/review-pr-prompt.txt")
    private Resource promptResource;

    public Review generateReview(GitHubEventServiceRequest request) {
        String repositoryName = extractRepositoryNameFrom(request);
        Integer pullRequestNumber = extractPullRequestNumberFrom(request);

        try {
            GitHub gitHub = gitHubClientFactory.createGitHubClient(request.installationId());
            GHRepository repository = gitHub.getRepository(repositoryName);
            GHPullRequest pullRequest = repository.getPullRequest(pullRequestNumber);

            List<FileDiff> fileDiffs = gitHubDiffService.buildFileDiffs(pullRequest);
            String prompt = buildPromptFrom(fileDiffs);

            ReviewResponse reviewResponse = callOpenAiWith(prompt);
            Map<String, Set<Integer>> pathToCommentableLines = gitHubDiffService.buildPathToCommentableLines(fileDiffs);

            return Review.from(repositoryName, pullRequestNumber, reviewResponse, pathToCommentableLines);
        } catch (Exception e) {
            log.error("리뷰 생성 실패, repository={}, PR={}", repositoryName, pullRequestNumber, e);
            throw new RuntimeException("리뷰 생성에 실패했습니다.");
        }
    }

    private static String extractRepositoryNameFrom(GitHubEventServiceRequest request) {
        Map<String, Object> payload = request.payload();
        Map<String, Object> repository = (Map<String, Object>) payload.get("repository");
        return (String) repository.get("full_name");
    }

    private static Integer extractPullRequestNumberFrom(GitHubEventServiceRequest request) {
        Map<String, Object> payload = request.payload();
        Map<String, Object> pullRequest = (Map<String, Object>) payload.get("pull_request");
        return ((Number) pullRequest.get("number")).intValue();
    }

    private String buildPromptFrom(List<FileDiff> fileDiffs) throws IOException {
        String diffJson = objectMapper.writeValueAsString(fileDiffs);
        String basePrompt = promptResource.getContentAsString(StandardCharsets.UTF_8);
        return basePrompt.replace("{DIFF_JSON}", diffJson);
    }

    private ReviewResponse callOpenAiWith(String prompt) throws JsonProcessingException {
        String responseJson = chatClient.prompt()
                .user(prompt)
                .call()
                .content();

        return objectMapper.readValue(responseJson, ReviewResponse.class);
    }
}
