package com.agilerunner.api.service;

import com.agilerunner.api.service.dto.GitHubEventServiceRequest;
import com.agilerunner.config.GitHubConfig;
import com.agilerunner.domain.Review;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.kohsuke.github.GHPullRequest;
import org.kohsuke.github.GHPullRequestFileDetail;
import org.kohsuke.github.GHRepository;
import org.kohsuke.github.GitHub;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class OpenAiService {

    private final ChatClient chatClient;
    private final GitHubConfig gitHubConfig;

    public Review generateReview(GitHubEventServiceRequest request) {
        try {
            GitHub gitHub = gitHubConfig.createGitHubClient(request.installationId());

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

            String prompt = """
                    당신은 숙련된 Java 코드 리뷰어입니다.
                    아래 코드를 분석하고 개선점, 보안, 가독성, 구조 측면에서 리뷰를 작성해주세요.
                    코드:
                    %s
                    """.formatted(stringBuilder);

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
