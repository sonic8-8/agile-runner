package com.agilerunner.api.service;

import com.agilerunner.api.service.dto.GitHubCommentResponse;
import com.agilerunner.api.service.github.request.GitHubEventServiceRequest;
import com.agilerunner.api.service.github.response.GitHubCommentExecutionResult;
import com.agilerunner.client.github.auth.GitHubClientFactory;
import com.agilerunner.domain.InlineComment;
import com.agilerunner.domain.ParsedFilePatch;
import com.agilerunner.domain.Review;
import com.agilerunner.domain.executioncontrol.ExecutionControlMode;
import com.agilerunner.domain.executioncontrol.GitHubWriteSkipReason;
import com.agilerunner.domain.exception.AgileRunnerException;
import com.agilerunner.domain.exception.ErrorCode;
import com.agilerunner.util.GitHubPositionConverter;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.kohsuke.github.GHCommitPointer;
import org.kohsuke.github.GHIssueComment;
import org.kohsuke.github.GHPullRequest;
import org.kohsuke.github.GHPullRequestReviewComment;
import org.kohsuke.github.GHRepository;
import org.kohsuke.github.GitHub;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.OptionalInt;

import static com.agilerunner.GitHubEventType.PULL_REQUEST;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
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
                .isInstanceOfSatisfying(AgileRunnerException.class, exception -> {
                    assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.GITHUB_COMMENT_PREPARATION_FAILED);
                });

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
                .isInstanceOfSatisfying(AgileRunnerException.class, exception -> {
                    assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.GITHUB_COMMENT_PREPARATION_FAILED);
                });

        verify(pullRequest, never()).comment(anyString());
        verify(pullRequest, never()).createReviewComment(anyString(), anyString(), anyString(), anyInt());
    }

    @DisplayName("GitHub App 설정 문제로 클라이언트를 만들지 못하면 코멘트 등록에 실패한다.")
    @Test
    void comment_failsWhenGitHubClientCreationFails() throws Exception {
        // given
        GitHubClientFactory gitHubClientFactory = mock(GitHubClientFactory.class);
        GitHubPatchService gitHubPatchService = mock(GitHubPatchService.class);
        GitHubPositionConverter gitHubPositionConverter = mock(GitHubPositionConverter.class);
        GitHubCommentService service = new GitHubCommentService(gitHubClientFactory, gitHubPatchService, gitHubPositionConverter);

        Review review = Review.of(
                "owner/repo",
                12,
                "리뷰 본문",
                List.of(InlineComment.of("src/Main.java", 10, "라인 코멘트"))
        );
        GitHubEventServiceRequest request = GitHubEventServiceRequest.of(PULL_REQUEST, Map.of("action", "opened"), 100L);

        when(gitHubClientFactory.createGitHubClient(100L))
                .thenThrow(new AgileRunnerException(
                        ErrorCode.GITHUB_APP_CONFIGURATION_MISSING,
                        "GitHub App ID가 설정되지 않았습니다."
                ));

        // when & then
        assertThatThrownBy(() -> service.comment(review, request))
                .isInstanceOfSatisfying(AgileRunnerException.class, exception -> {
                    assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.GITHUB_APP_CONFIGURATION_MISSING);
                });
    }

    @DisplayName("successful comment 경로에서는 본문 코멘트 후 인라인 코멘트를 등록하고 응답을 유지한다.")
    @Test
    void comment_postsMainCommentBeforeInlineCommentsAndBuildsResponse() throws Exception {
        // given
        GitHubClientFactory gitHubClientFactory = mock(GitHubClientFactory.class);
        GitHubPatchService gitHubPatchService = mock(GitHubPatchService.class);
        GitHubPositionConverter gitHubPositionConverter = mock(GitHubPositionConverter.class);
        GitHubCommentService service = new GitHubCommentService(gitHubClientFactory, gitHubPatchService, gitHubPositionConverter);

        GitHub gitHub = mock(GitHub.class);
        GHRepository repository = mock(GHRepository.class);
        GHCommitPointer head = mock(GHCommitPointer.class);
        GHIssueComment mainComment = new GHIssueComment();
        GHPullRequestReviewComment reviewComment = new GHPullRequestReviewComment();
        ParsedFilePatch parsedFilePatch = ParsedFilePatch.of("src/Main.java", List.of());
        List<String> commentCalls = new ArrayList<>();
        GHPullRequest pullRequest = new StubPullRequest(head, mainComment, reviewComment, commentCalls);

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
        when(head.getSha()).thenReturn("head-sha");
        when(gitHubPatchService.buildParsedFilePatches(pullRequest)).thenReturn(List.of(parsedFilePatch));
        when(gitHubPatchService.buildPathToPatch(List.of(parsedFilePatch))).thenReturn(Map.of("src/Main.java", parsedFilePatch));
        when(gitHubPositionConverter.toPosition(parsedFilePatch, 10)).thenReturn(OptionalInt.of(7));
        setField(mainComment, "id", 101L);
        setField(mainComment, "html_url", "https://github.com/comment/101");
        setField(reviewComment, "id", 202L);
        setField(reviewComment, "html_url", "https://github.com/comment/202");

        // when
        GitHubCommentResponse response = service.comment(review, request);

        // then
        assertThat(commentCalls).containsExactly(
                "main:리뷰 본문",
                "inline:라인 코멘트:head-sha:src/Main.java:7"
        );
        assertThat(response.reviewCommentId()).isEqualTo(101L);
        assertThat(response.reviewCommentUrl()).isEqualTo("https://github.com/comment/101");
        assertThat(response.postedInlineComments()).hasSize(1);
        assertThat(response.postedInlineComments().get(0).id()).isEqualTo(202L);
        assertThat(response.postedInlineComments().get(0).htmlUrl()).isEqualTo("https://github.com/comment/202");
        assertThat(response.message()).isEqualTo("리뷰 코멘트가 성공적으로 등록되었습니다.");
    }

    @DisplayName("DRY_RUN 모드에서는 사전 준비를 유지하되 GitHub write를 수행하지 않는다.")
    @Test
    void execute_skipsGitHubWritesAndPreservesPreparedResultInDryRunMode() throws Exception {
        // given
        GitHubClientFactory gitHubClientFactory = mock(GitHubClientFactory.class);
        GitHubPatchService gitHubPatchService = mock(GitHubPatchService.class);
        GitHubPositionConverter gitHubPositionConverter = mock(GitHubPositionConverter.class);
        GitHubCommentService service = new GitHubCommentService(gitHubClientFactory, gitHubPatchService, gitHubPositionConverter);

        GitHub gitHub = mock(GitHub.class);
        GHRepository repository = mock(GHRepository.class);
        GHCommitPointer head = mock(GHCommitPointer.class);
        GHIssueComment mainComment = new GHIssueComment();
        GHPullRequestReviewComment reviewComment = new GHPullRequestReviewComment();
        ParsedFilePatch parsedFilePatch = ParsedFilePatch.of("src/Main.java", List.of());
        List<String> commentCalls = new ArrayList<>();
        GHPullRequest pullRequest = new StubPullRequest(head, mainComment, reviewComment, commentCalls);

        Review review = Review.of(
                "owner/repo",
                12,
                "리뷰 본문",
                List.of(InlineComment.of("src/Main.java", 10, "라인 코멘트"))
        );
        GitHubEventServiceRequest request = GitHubEventServiceRequest.of(
                PULL_REQUEST,
                Map.of("action", "opened"),
                100L,
                ExecutionControlMode.DRY_RUN
        );

        when(gitHubClientFactory.createGitHubClient(100L)).thenReturn(gitHub);
        when(gitHub.getRepository("owner/repo")).thenReturn(repository);
        when(repository.getPullRequest(12)).thenReturn(pullRequest);
        when(head.getSha()).thenReturn("head-sha");
        when(gitHubPatchService.buildParsedFilePatches(pullRequest)).thenReturn(List.of(parsedFilePatch));
        when(gitHubPatchService.buildPathToPatch(List.of(parsedFilePatch))).thenReturn(Map.of("src/Main.java", parsedFilePatch));
        when(gitHubPositionConverter.toPosition(parsedFilePatch, 10)).thenReturn(OptionalInt.of(7));

        // when
        GitHubCommentExecutionResult result = service.execute(review, request);

        // then
        assertThat(result.getExecutionControlMode()).isEqualTo(ExecutionControlMode.DRY_RUN);
        assertThat(result.isWritePerformed()).isFalse();
        assertThat(result.getWriteSkipReason()).isEqualTo(GitHubWriteSkipReason.DRY_RUN);
        assertThat(result.getPreparedInlineCommentCount()).isEqualTo(1);
        assertThat(result.getGitHubCommentResponse()).isNull();
        assertThat(commentCalls).isEmpty();
    }

    @DisplayName("메인 코멘트 등록에 실패하면 예외를 던지고 인라인 코멘트는 등록하지 않는다.")
    @Test
    void comment_failsWhenMainCommentPostingFails() throws Exception {
        // given
        GitHubClientFactory gitHubClientFactory = mock(GitHubClientFactory.class);
        GitHubPatchService gitHubPatchService = mock(GitHubPatchService.class);
        GitHubPositionConverter gitHubPositionConverter = mock(GitHubPositionConverter.class);
        GitHubCommentService service = new GitHubCommentService(gitHubClientFactory, gitHubPatchService, gitHubPositionConverter);

        GitHub gitHub = mock(GitHub.class);
        GHRepository repository = mock(GHRepository.class);
        GHPullRequest pullRequest = mock(GHPullRequest.class);
        GHCommitPointer head = mock(GHCommitPointer.class);
        ParsedFilePatch parsedFilePatch = ParsedFilePatch.of("src/Main.java", List.of());
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
        when(gitHubPatchService.buildParsedFilePatches(pullRequest)).thenReturn(List.of(parsedFilePatch));
        when(gitHubPatchService.buildPathToPatch(List.of(parsedFilePatch))).thenReturn(Map.of("src/Main.java", parsedFilePatch));
        when(gitHubPositionConverter.toPosition(parsedFilePatch, 10)).thenReturn(OptionalInt.of(7));
        doThrow(new java.io.IOException("main comment failed")).when(pullRequest).comment("리뷰 본문");

        // when & then
        assertThatThrownBy(() -> service.comment(review, request))
                .isInstanceOfSatisfying(AgileRunnerException.class, exception -> {
                    assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.GITHUB_COMMENT_POST_FAILED);
                });

        verify(pullRequest, never()).createReviewComment(anyString(), anyString(), anyString(), anyInt());
    }

    @DisplayName("인라인 코멘트 일부 작성이 실패해도 나머지 코멘트는 유지한다.")
    @Test
    void comment_keepsSkipPolicyWhenSomeInlineCommentsFail() throws Exception {
        // given
        GitHubClientFactory gitHubClientFactory = mock(GitHubClientFactory.class);
        GitHubPatchService gitHubPatchService = mock(GitHubPatchService.class);
        GitHubPositionConverter gitHubPositionConverter = mock(GitHubPositionConverter.class);
        GitHubCommentService service = new GitHubCommentService(gitHubClientFactory, gitHubPatchService, gitHubPositionConverter);

        GitHub gitHub = mock(GitHub.class);
        GHRepository repository = mock(GHRepository.class);
        GHPullRequest pullRequest = mock(GHPullRequest.class);
        GHCommitPointer head = mock(GHCommitPointer.class);
        GHIssueComment mainComment = new GHIssueComment();
        GHPullRequestReviewComment reviewComment = new GHPullRequestReviewComment();
        ParsedFilePatch parsedFilePatch = ParsedFilePatch.of("src/Main.java", List.of());

        Review review = Review.of(
                "owner/repo",
                12,
                "리뷰 본문",
                List.of(
                        InlineComment.of("src/Main.java", 10, "라인 코멘트 1"),
                        InlineComment.of("src/Main.java", 11, "라인 코멘트 2")
                )
        );
        GitHubEventServiceRequest request = GitHubEventServiceRequest.of(PULL_REQUEST, Map.of("action", "opened"), 100L);

        when(gitHubClientFactory.createGitHubClient(100L)).thenReturn(gitHub);
        when(gitHub.getRepository("owner/repo")).thenReturn(repository);
        when(repository.getPullRequest(12)).thenReturn(pullRequest);
        when(pullRequest.getHead()).thenReturn(head);
        when(head.getSha()).thenReturn("head-sha");
        when(gitHubPatchService.buildParsedFilePatches(pullRequest)).thenReturn(List.of(parsedFilePatch));
        when(gitHubPatchService.buildPathToPatch(List.of(parsedFilePatch))).thenReturn(Map.of("src/Main.java", parsedFilePatch));
        when(gitHubPositionConverter.toPosition(parsedFilePatch, 10)).thenReturn(OptionalInt.of(7));
        when(gitHubPositionConverter.toPosition(parsedFilePatch, 11)).thenReturn(OptionalInt.of(8));
        doReturn(mainComment).when(pullRequest).comment("리뷰 본문");
        setField(mainComment, "id", 101L);
        setField(mainComment, "html_url", "https://github.com/comment/101");
        doThrow(new java.io.IOException("inline failed"))
                .doReturn(reviewComment)
                .when(pullRequest)
                .createReviewComment(anyString(), anyString(), anyString(), anyInt());
        setField(reviewComment, "id", 202L);
        setField(reviewComment, "html_url", "https://github.com/comment/202");

        // when
        GitHubCommentResponse response = service.comment(review, request);

        // then
        assertThat(response.reviewCommentId()).isEqualTo(101L);
        assertThat(response.postedInlineComments()).hasSize(1);
        assertThat(response.postedInlineComments().get(0).id()).isEqualTo(202L);
        assertThat(response.message()).isEqualTo("리뷰 코멘트가 성공적으로 등록되었습니다.");
        verify(pullRequest).comment("리뷰 본문");
        verify(pullRequest).createReviewComment("라인 코멘트 1", "head-sha", "src/Main.java", 7);
        verify(pullRequest).createReviewComment("라인 코멘트 2", "head-sha", "src/Main.java", 8);
    }

    private void setField(Object target, String fieldName, Object value) throws Exception {
        Class<?> current = target.getClass();

        while (current != null) {
            try {
                Field field = current.getDeclaredField(fieldName);
                field.setAccessible(true);
                field.set(target, value);
                return;
            } catch (NoSuchFieldException ignored) {
                current = current.getSuperclass();
            }
        }

        throw new NoSuchFieldException(fieldName);
    }

    private static class StubPullRequest extends GHPullRequest {
        private final GHCommitPointer head;
        private final GHIssueComment mainComment;
        private final GHPullRequestReviewComment reviewComment;
        private final List<String> commentCalls;

        private StubPullRequest(GHCommitPointer head,
                                GHIssueComment mainComment,
                                GHPullRequestReviewComment reviewComment,
                                List<String> commentCalls) {
            this.head = head;
            this.mainComment = mainComment;
            this.reviewComment = reviewComment;
            this.commentCalls = commentCalls;
        }

        @Override
        public GHCommitPointer getHead() {
            return head;
        }

        @Override
        public GHIssueComment comment(String body) {
            commentCalls.add("main:" + body);
            return mainComment;
        }

        @Override
        public GHPullRequestReviewComment createReviewComment(String body, String commitId, String path, int position) {
            commentCalls.add("inline:" + body + ":" + commitId + ":" + path + ":" + position);
            return reviewComment;
        }
    }
}
