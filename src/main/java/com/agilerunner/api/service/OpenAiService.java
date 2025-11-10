package com.agilerunner.api.service;

import com.agilerunner.api.service.dto.GitHubEventServiceRequest;
import com.agilerunner.domain.Review;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.kohsuke.github.GHPullRequest;
import org.kohsuke.github.GHPullRequestFileDetail;
import org.kohsuke.github.GHRepository;
import org.kohsuke.github.GitHub;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class OpenAiService {

    private final ChatClient chatClient;
    private final ObjectMapper objectMapper;
    private final GitHub gitHub;

    public Review generateReview(GitHubEventServiceRequest request) {
        try {
            JsonNode payload = objectMapper.readTree(request.payload());

            String repositoryName = payload.path("repository").path("full_name").asText();
            int pullRequestNumber = payload.path("pull_request").path("number").asInt();

            GHRepository repository = gitHub.getRepository(repositoryName);
            GHPullRequest pullRequest = repository.getPullRequest(pullRequestNumber);

            StringBuilder stringBuilder = new StringBuilder();
            for (GHPullRequestFileDetail file : pullRequest.listFiles()) {
                stringBuilder.append("### File: ").append(file.getFilename()).append("\n");
                stringBuilder.append(file.getPatch()).append("\n\n");
            }

            String prompt = "프롬프트임"
                    .formatted(stringBuilder);

            String review = chatClient.prompt()
                    .user(prompt)
                    .call()
                    .content();

            return Review.of(repositoryName, pullRequestNumber, review);

        } catch (Exception e) {
            throw new RuntimeException("리뷰 생성 실패", e);
        }
    }
}
