package com.agilerunner.api.service;

import com.agilerunner.api.service.dto.ReviewResponse;
import com.agilerunner.api.service.dto.GitHubEventServiceRequest;
import com.agilerunner.config.GitHubClientFactory;
import com.agilerunner.domain.Review;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.kohsuke.github.GHPullRequest;
import org.kohsuke.github.GHPullRequestFileDetail;
import org.kohsuke.github.GHRepository;
import org.kohsuke.github.GitHub;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class OpenAiService {

    private final ChatClient chatClient;
    private final GitHubClientFactory gitHubClientFactory;
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
                log.info("페이로드 key 목록: {}", request.payload().keySet());
                return null;
            }
            int pullRequestNumber = ((Number) pullRequestData.get("number")).intValue();

            GHRepository repo = gitHub.getRepository(repositoryName);
            GHPullRequest pullRequest = repo.getPullRequest(pullRequestNumber);

            StringBuilder differenceBuilder = new StringBuilder();
            for (GHPullRequestFileDetail file : pullRequest.listFiles()) {
                differenceBuilder.append("### File: ").append(file.getFilename()).append("\n");
                differenceBuilder.append(file.getPatch()).append("\n\n");
            }

            String basePrompt = promptResource.getContentAsString(StandardCharsets.UTF_8);
            String prompt = basePrompt.replace("{PR_CONTENT}", differenceBuilder.toString());

            String json = chatClient.prompt()
                    .user(prompt)
                    .call()
                    .content();

            ReviewResponse reviewResponse = objectMapper.readValue(json, ReviewResponse.class);

            return Review.from(repositoryName, pullRequestNumber, reviewResponse);

        } catch (Exception e) {
            throw new RuntimeException("리뷰 생성 실패", e);
        }
    }
}
