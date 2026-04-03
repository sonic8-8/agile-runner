package com.agilerunner.api.service;

import com.agilerunner.api.service.dto.GitHubEventServiceRequest;
import com.agilerunner.config.GitHubClientFactory;
import com.agilerunner.domain.InlineComment;
import com.agilerunner.domain.Review;
import com.agilerunner.util.GitHubPositionConverter;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.kohsuke.github.GHCommitPointer;
import org.kohsuke.github.GHPullRequest;
import org.kohsuke.github.GHRepository;
import org.kohsuke.github.GitHub;

import java.util.List;
import java.util.Map;

import static com.agilerunner.GitHubEventType.PULL_REQUEST;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class GitHubCommentServiceTest {

    @DisplayName("patch build가 실패하면 본문 코멘트를 먼저 등록하지 않는다.")
    @Test
    void comment_doesNotWriteMainCommentBeforePreflightCompletes() throws Exception {
        // given
        GitHubClientFactory gitHubClientFactory = mock(GitHubClientFactory.class);
        GitHubPatchService gitHubPatchService = mock(GitHubPatchService.class);
        GitHubPositionConverter gitHubPositionConverter = mock(GitHubPositionConverter.class);
        GitHubCommentService service = new GitHubCommentService(gitHubClientFactory, gitHubPatchService, gitHubPositionConverter);

        GitHub gitHub = mock(GitHub.class);
        GHRepository repository = mock(GHRepository.class);
        GHPullRequest pullRequest = mock(GHPullRequest.class);
        GHCommitPointer head = mock(GHCommitPointer.class);
        Review review = Review.of(
                "owner/repo",
                12,
                "리뷰 본문",
                List.of(InlineComment.of("src/Main.java", 10, "라인 코멘트"))
        );
        GitHubEventServiceRequest request = GitHubEventServiceRequest.of(PULL_REQUEST, Map.of("action", "opened"), 100L);

        when(gitHubClientFactory.createGitHubClient(100L)).thenReturn(gitHub);
        when(gitHub.getRepository("owner/repo")).thenReturn(repository);
        when(repository.getPullRequest(12)).thenReturn(pullRequest);
        when(pullRequest.getHead()).thenReturn(head);
        when(head.getSha()).thenReturn("head-sha");
        when(gitHubPatchService.buildParsedFilePatches(pullRequest)).thenThrow(new IllegalStateException("patch build failed"));

        // when & then
        assertThatThrownBy(() -> service.comment(review, request))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("GitHub 코멘트 등록 실패");

        verify(pullRequest, never()).comment(anyString());
        verify(pullRequest, never()).createReviewComment(anyString(), anyString(), anyString(), anyInt());
    }

    @DisplayName("inline comment 준비에 실패하면 본문 코멘트와 인라인 코멘트를 먼저 등록하지 않는다.")
    @Test
    void comment_doesNotWriteAnyCommentWhenInlinePreparationFails() throws Exception {
        // given
        GitHubClientFactory gitHubClientFactory = mock(GitHubClientFactory.class);
        GitHubPatchService gitHubPatchService = mock(GitHubPatchService.class);
        GitHubPositionConverter gitHubPositionConverter = mock(GitHubPositionConverter.class);
        GitHubCommentService service = new GitHubCommentService(gitHubClientFactory, gitHubPatchService, gitHubPositionConverter);

        GitHub gitHub = mock(GitHub.class);
        GHRepository repository = mock(GHRepository.class);
        GHPullRequest pullRequest = mock(GHPullRequest.class);
        GHCommitPointer head = mock(GHCommitPointer.class);
        Review review = Review.of(
                "owner/repo",
                12,
                "리뷰 본문",
                List.of(InlineComment.of("src/Main.java", 10, "라인 코멘트"))
        );
        GitHubEventServiceRequest request = GitHubEventServiceRequest.of(PULL_REQUEST, Map.of("action", "opened"), 100L);

        when(gitHubClientFactory.createGitHubClient(100L)).thenReturn(gitHub);
        when(gitHub.getRepository("owner/repo")).thenReturn(repository);
        when(repository.getPullRequest(12)).thenReturn(pullRequest);
        when(pullRequest.getHead()).thenReturn(head);
        when(head.getSha()).thenReturn("head-sha");
        when(gitHubPatchService.buildParsedFilePatches(pullRequest)).thenReturn(List.of());
        when(gitHubPatchService.buildPathToPatch(List.of())).thenReturn(Map.of());

        // when & then
        assertThatThrownBy(() -> service.comment(review, request))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("GitHub 코멘트 등록 실패");

        verify(pullRequest, never()).comment(anyString());
        verify(pullRequest, never()).createReviewComment(anyString(), anyString(), anyString(), anyInt());
    }
}
