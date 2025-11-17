package com.agilerunner.api.service;

import com.agilerunner.api.service.dto.FileDiff;
import com.agilerunner.api.service.dto.GitHubEventServiceRequest;
import com.agilerunner.api.service.dto.ReviewResponse;
import com.agilerunner.config.GitHubClientFactory;
import com.agilerunner.domain.Review;
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
        try {
            GitHub gitHub = gitHubClientFactory.createGitHubClient(request.installationId());

            Map<String, Object> payload = request.payload();
            Map<String, Object> repository = (Map<String, Object>) payload.get("repository");
            String repositoryName = (String) repository.get("full_name");

            Map<String, Object> pullRequestData = (Map<String, Object>) payload.get("pull_request");
            if (pullRequestData == null) {
                log.info("PR 이벤트가 아닙니다. \n 이벤트 타입: {}", request.gitHubEventType());
                return null;
            }
            int pullRequestNumber = ((Number) pullRequestData.get("number")).intValue();

            GHRepository repo = gitHub.getRepository(repositoryName);
            GHPullRequest pullRequest = repo.getPullRequest(pullRequestNumber);

            List<FileDiff> fileDiffs = gitHubDiffService.buildFileDiffs(pullRequest);

            String diffJson = objectMapper.writeValueAsString(fileDiffs);

            String basePrompt = promptResource.getContentAsString(StandardCharsets.UTF_8);
            String prompt = basePrompt.replace("{DIFF_JSON}", diffJson);

            String responseJson = chatClient.prompt()
                    .user(prompt)
                    .call()
                    .content();

            ReviewResponse reviewResponse = objectMapper.readValue(responseJson, ReviewResponse.class);

            Map<String, Set<Integer>> pathToCommentableLines = gitHubDiffService.buildPathToCommentableLines(fileDiffs);

            return Review.from(repositoryName, pullRequestNumber, reviewResponse, pathToCommentableLines);

        } catch (Exception e) {
            log.error("OpenAI 리뷰 생성 실패", e);
            return null;
        }
    }
}
