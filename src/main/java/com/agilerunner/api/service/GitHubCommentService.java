package com.agilerunner.api.service;

import com.agilerunner.api.service.dto.GitHubCommentResponse;
import com.agilerunner.api.service.github.request.GitHubEventServiceRequest;
import com.agilerunner.api.service.github.response.GitHubCommentExecutionResult;
import com.agilerunner.api.service.dto.PostedInlineComment;
import com.agilerunner.client.github.auth.GitHubClientFactory;
import com.agilerunner.domain.InlineComment;
import com.agilerunner.domain.ParsedFilePatch;
import com.agilerunner.domain.Review;
import com.agilerunner.domain.exception.AgileRunnerException;
import com.agilerunner.domain.exception.ErrorCode;
import com.agilerunner.domain.executioncontrol.ExecutionControlMode;
import com.agilerunner.domain.executioncontrol.GitHubWriteSkipReason;
import com.agilerunner.util.GitHubPositionConverter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.kohsuke.github.*;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class GitHubCommentService {

    private final GitHubClientFactory gitHubClientFactory;
    private final GitHubPatchService gitHubPatchService;
    private final GitHubPositionConverter gitHubPositionConverter;

    public GitHubCommentResponse comment(Review review, GitHubEventServiceRequest request) {
        GitHubCommentExecutionResult result = execute(review, request);
        return result.requireGitHubCommentResponse();
    }

    public GitHubCommentExecutionResult execute(Review review, GitHubEventServiceRequest request) {
        try {
            CommentPreflight preflight = prepareCommentPreflight(review, request);
            if (isDryRun(request)) {
                return GitHubCommentExecutionResult.skipped(
                        request.getExecutionControlMode(),
                        GitHubWriteSkipReason.DRY_RUN,
                        preflight.getPreparedInlineComments().size()
                );
            }

            GHIssueComment mainComment = postMainComment(preflight.getPullRequest(), review.getReviewBody());
            List<PostedInlineComment> postedInlineComments = postInlineComments(preflight);
            GitHubCommentResponse response = buildResponse(mainComment, postedInlineComments);
            return GitHubCommentExecutionResult.written(
                    request.getExecutionControlMode(),
                    preflight.getPreparedInlineComments().size(),
                    response
            );
        } catch (AgileRunnerException exception) {
            log.error("GitHub 코멘트 등록 실패, repository={}, PR={}",
                    review.getRepositoryName(), review.getPullRequestNumber(), exception);
            throw exception;
        } catch (Exception e) {
            log.error("GitHub 코멘트 등록 실패, repository={}, PR={}",
                    review.getRepositoryName(), review.getPullRequestNumber(), e);
            throw new AgileRunnerException(
                    ErrorCode.GITHUB_COMMENT_POST_FAILED,
                    "GitHub 코멘트 등록 실패",
                    e
            );
        }
    }

    private boolean isDryRun(GitHubEventServiceRequest request) {
        return request.getExecutionControlMode() == ExecutionControlMode.DRY_RUN;
    }

    private CommentPreflight prepareCommentPreflight(Review review, GitHubEventServiceRequest request) throws Exception {
        GHPullRequest pullRequest = loadPullRequest(review, request);
        String headSha = pullRequest.getHead().getSha();
        Map<String, ParsedFilePatch> pathToParsedFilePatches = filterParsedFilePatches(
                request,
                buildParsedFilePatches(pullRequest)
        );
        List<PreparedInlineComment> preparedInlineComments = prepareInlineComments(
                filterInlineComments(review, request),
                pathToParsedFilePatches
        );
        return CommentPreflight.of(pullRequest, headSha, preparedInlineComments);
    }

    private Map<String, ParsedFilePatch> buildParsedFilePatches(GHPullRequest pullRequest) {
        try {
            List<ParsedFilePatch> parsedFilePatches = gitHubPatchService.buildParsedFilePatches(pullRequest);
            return gitHubPatchService.buildPathToPatch(parsedFilePatches);
        } catch (AgileRunnerException exception) {
            throw exception;
        } catch (RuntimeException exception) {
            throw new AgileRunnerException(
                    ErrorCode.GITHUB_COMMENT_PREPARATION_FAILED,
                    "GitHub 코멘트 사전 준비에 실패했습니다.",
                    exception
            );
        }
    }

    private GHPullRequest loadPullRequest(Review review, GitHubEventServiceRequest request) throws Exception {
        GitHub gitHub = gitHubClientFactory.createGitHubClient(request.getInstallationId());
        GHRepository repository = gitHub.getRepository(review.getRepositoryName());
        return repository.getPullRequest(review.getPullRequestNumber());
    }

    private GHIssueComment postMainComment(GHPullRequest pullRequest, String reviewBody) throws IOException {
        return pullRequest.comment(reviewBody);
    }

    private Map<String, ParsedFilePatch> filterParsedFilePatches(GitHubEventServiceRequest request,
                                                                 Map<String, ParsedFilePatch> pathToParsedFilePatches) {
        if (request.getSelectedPaths().isEmpty()) {
            return pathToParsedFilePatches;
        }

        Map<String, ParsedFilePatch> filtered = new LinkedHashMap<>();
        for (String selectedPath : request.getSelectedPaths()) {
            ParsedFilePatch parsedFilePatch = pathToParsedFilePatches.get(selectedPath);
            if (parsedFilePatch == null) {
                continue;
            }
            filtered.put(selectedPath, parsedFilePatch);
        }

        return filtered;
    }

    private List<InlineComment> filterInlineComments(Review review, GitHubEventServiceRequest request) {
        if (request.getSelectedPaths().isEmpty()) {
            return review.getInlineComments();
        }

        Set<String> selectedPaths = new LinkedHashSet<>(request.getSelectedPaths());
        return review.getInlineComments().stream()
                .filter(inlineComment -> selectedPaths.contains(inlineComment.getPath()))
                .toList();
    }

    private List<PreparedInlineComment> prepareInlineComments(List<InlineComment> inlineComments,
                                                              Map<String, ParsedFilePatch> pathToParsedFilePatches) {
        List<PreparedInlineComment> preparedInlineComments = new ArrayList<>();

        for (InlineComment inlineComment : inlineComments) {
            preparedInlineComments.add(prepareInlineComment(inlineComment, pathToParsedFilePatches));
        }

        return preparedInlineComments;
    }

    private PreparedInlineComment prepareInlineComment(InlineComment inlineComment,
                                                       Map<String, ParsedFilePatch> pathToParsedFilePatches) {
        String path = inlineComment.getPath();
        int line = inlineComment.getLine();

        ParsedFilePatch parsedFilePatch = pathToParsedFilePatches.get(path);
        if (parsedFilePatch == null) {
            throw new AgileRunnerException(
                    ErrorCode.GITHUB_COMMENT_PREPARATION_FAILED,
                    "해당 path에 대한 patch가 없습니다. path=" + path
            );
        }

        OptionalInt optionalPosition = gitHubPositionConverter.toPosition(parsedFilePatch, line);
        if (optionalPosition.isEmpty()) {
            throw new AgileRunnerException(
                    ErrorCode.GITHUB_COMMENT_PREPARATION_FAILED,
                    "position 계산 실패 path=" + path + ", line=" + line
            );
        }

        return PreparedInlineComment.of(
                path,
                optionalPosition.getAsInt(),
                inlineComment.getBody()
        );
    }

    private List<PostedInlineComment> postInlineComments(CommentPreflight preflight) {
        List<PostedInlineComment> postedInlineComments = new ArrayList<>();

        for (PreparedInlineComment preparedInlineComment : preflight.getPreparedInlineComments()) {
            postInlineComment(preparedInlineComment, preflight)
                    .ifPresent(postedInlineComments::add);
        }

        return postedInlineComments;
    }

    private Optional<PostedInlineComment> postInlineComment(PreparedInlineComment preparedInlineComment,
                                                            CommentPreflight preflight) {
        return createReviewComment(preparedInlineComment, preflight);
    }

    private Optional<PostedInlineComment> createReviewComment(PreparedInlineComment preparedInlineComment,
                                                              CommentPreflight preflight) {
        try {
            GHPullRequestReviewComment reviewComment =
                    preflight.getPullRequest().createReviewComment(
                            preparedInlineComment.getBody(),
                            preflight.getHeadSha(),
                            preparedInlineComment.getPath(),
                            preparedInlineComment.getPosition()
                    );

            return Optional.of(
                    PostedInlineComment.of(
                            reviewComment.getId(),
                            reviewComment.getHtmlUrl().toString()
                    )
            );
        } catch (IOException e) {
            log.error("인라인 코멘트 작성 실패 path={}, position={}",
                    preparedInlineComment.getPath(),
                    preparedInlineComment.getPosition(),
                    e);
            return Optional.empty();
        }
    }

    private GitHubCommentResponse buildResponse(GHIssueComment mainComment,
                                                List<PostedInlineComment> postedInlineComments) {
        return GitHubCommentResponse.of(
                mainComment.getId(),
                mainComment.getHtmlUrl().toString(),
                postedInlineComments,
                "리뷰 코멘트가 성공적으로 등록되었습니다."
        );
    }

    private static class CommentPreflight {
        private final GHPullRequest pullRequest;
        private final String headSha;
        private final List<PreparedInlineComment> preparedInlineComments;

        private CommentPreflight(GHPullRequest pullRequest, String headSha, List<PreparedInlineComment> preparedInlineComments) {
            this.pullRequest = pullRequest;
            this.headSha = headSha;
            this.preparedInlineComments = preparedInlineComments;
        }

        public static CommentPreflight of(GHPullRequest pullRequest, String headSha, List<PreparedInlineComment> preparedInlineComments) {
            return new CommentPreflight(pullRequest, headSha, preparedInlineComments);
        }

        public GHPullRequest getPullRequest() {
            return pullRequest;
        }

        public String getHeadSha() {
            return headSha;
        }

        public List<PreparedInlineComment> getPreparedInlineComments() {
            return preparedInlineComments;
        }
    }

    private static class PreparedInlineComment {
        private final String path;
        private final int position;
        private final String body;

        private PreparedInlineComment(String path, int position, String body) {
            this.path = path;
            this.position = position;
            this.body = body;
        }

        public static PreparedInlineComment of(String path, int position, String body) {
            return new PreparedInlineComment(path, position, body);
        }

        public String getPath() {
            return path;
        }

        public int getPosition() {
            return position;
        }

        public String getBody() {
            return body;
        }
    }
}
