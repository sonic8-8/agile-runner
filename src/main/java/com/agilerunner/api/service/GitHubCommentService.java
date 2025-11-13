package com.agilerunner.api.service;

import com.agilerunner.api.service.dto.GitHubCommentResponse;
import com.agilerunner.api.service.dto.GitHubEventServiceRequest;
import com.agilerunner.config.GitHubClientFactory;
import com.agilerunner.domain.Review;
import lombok.RequiredArgsConstructor;
import org.kohsuke.github.GHIssueComment;
import org.kohsuke.github.GHPullRequest;
import org.kohsuke.github.GHRepository;
import org.kohsuke.github.GitHub;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class GitHubCommentService {

    private final GitHubClientFactory gitHubClientFactory;

    public GitHubCommentResponse comment(Review review, GitHubEventServiceRequest request) {
        try {
            GitHub gitHub = gitHubClientFactory.createGitHubClient(request.installationId());

            GHRepository repository = gitHub.getRepository(review.getRepositoryName());
            GHPullRequest pullRequest = repository.getPullRequest(review.getPullRequestNumber());

            GHIssueComment comment = pullRequest.comment(review.getReview());

            return GitHubCommentResponse.of(
                    comment.getId(),
                    comment.getHtmlUrl().toString(),
                    "리뷰 코멘트가 성공적으로 등록되었습니다."
            );
        } catch (Exception e) {
            throw new RuntimeException("GitHub 코멘트 등록 실패", e);
        }
    }
}
