package com.agilerunner.api.service;

import com.agilerunner.api.service.dto.GitHubEventServiceRequest;
import com.agilerunner.config.GitHubClientFactory;
import com.agilerunner.domain.Review;
import lombok.RequiredArgsConstructor;
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
public class OpenAiService {

    private final ChatClient chatClient;
    private final GitHubClientFactory gitHubClientFactory;

    @Value("classpath:prompts/review-pr-prompt.txt")
    private Resource promptResource;

    public Review generateReview(GitHubEventServiceRequest request) {
        try {
            GitHub gitHub = gitHubClientFactory.createGitHubClient(request.installationId());

            Map<String, Object> payload = request.payload();

            Map<String, Object> repository = (Map<String, Object>) payload.get("repository");
            String repositoryName = (String) repository.get("full_name");

            Map<String, Object> pullRequestData = (Map<String, Object>) payload.get("pull_request");
            int pullRequestNumber = ((Number) pullRequestData.get("number")).intValue();

            GHRepository repo = gitHub.getRepository(repositoryName);
            GHPullRequest pullRequest = repo.getPullRequest(pullRequestNumber);

            StringBuilder stringBuilder = new StringBuilder();
            for (GHPullRequestFileDetail file : pullRequest.listFiles()) {
                stringBuilder.append("### File: ").append(file.getFilename()).append("\n");
                stringBuilder.append(file.getPatch()).append("\n\n");
            }

            String basePrompt = promptResource.getContentAsString(StandardCharsets.UTF_8);
            String prompt = basePrompt.replace("{PR_CONTENT}", stringBuilder.toString());

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
