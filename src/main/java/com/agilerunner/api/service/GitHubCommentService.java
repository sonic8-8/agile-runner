package com.agilerunner.api.service;

import com.agilerunner.api.service.dto.GitHubCommentResponse;
import com.agilerunner.api.service.dto.GitHubEventServiceRequest;
import com.agilerunner.api.service.dto.PostedInlineComment;
import com.agilerunner.config.GitHubClientFactory;
import com.agilerunner.domain.InlineComment;
import com.agilerunner.domain.ParsedFilePatch;
import com.agilerunner.domain.Review;
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
        try {
            CommentPreflight preflight = prepareCommentPreflight(review, request);
            GHIssueComment mainComment = postMainComment(preflight.getPullRequest(), review.getReviewBody());
            List<PostedInlineComment> postedInlineComments = postInlineComments(preflight);

            return buildResponse(mainComment, postedInlineComments);
        } catch (Exception e) {
            log.error("GitHub 코멘트 등록 실패, repository={}, PR={}",
                    review.getRepositoryName(), review.getPullRequestNumber(), e);
            throw new RuntimeException("GitHub 코멘트 등록 실패", e);
        }
    }

    private CommentPreflight prepareCommentPreflight(Review review, GitHubEventServiceRequest request) throws Exception {
        GHPullRequest pullRequest = loadPullRequest(review, request);
        String headSha = pullRequest.getHead().getSha();
        Map<String, ParsedFilePatch> pathToParsedFilePatches = buildParsedFilePatches(pullRequest);
        List<PreparedInlineComment> preparedInlineComments = prepareInlineComments(review, pathToParsedFilePatches);
        return CommentPreflight.of(pullRequest, headSha, preparedInlineComments);
    }

    private Map<String, ParsedFilePatch> buildParsedFilePatches(GHPullRequest pullRequest) {
        List<ParsedFilePatch> parsedFilePatches = gitHubPatchService.buildParsedFilePatches(pullRequest);
        return gitHubPatchService.buildPathToPatch(parsedFilePatches);
    }

    private GHPullRequest loadPullRequest(Review review, GitHubEventServiceRequest request) throws Exception {
        GitHub gitHub = gitHubClientFactory.createGitHubClient(request.getInstallationId());
        GHRepository repository = gitHub.getRepository(review.getRepositoryName());
        return repository.getPullRequest(review.getPullRequestNumber());
    }

    private GHIssueComment postMainComment(GHPullRequest pullRequest, String reviewBody) throws IOException {
        return pullRequest.comment(reviewBody);
    }

    private List<PreparedInlineComment> prepareInlineComments(Review review,
                                                              Map<String, ParsedFilePatch> pathToParsedFilePatches) {
        List<PreparedInlineComment> preparedInlineComments = new ArrayList<>();

        for (InlineComment inlineComment : review.getInlineComments()) {
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
            throw new IllegalStateException("해당 path에 대한 patch가 없습니다. path=" + path);
        }

        OptionalInt optionalPosition = gitHubPositionConverter.toPosition(parsedFilePatch, line);
        if (optionalPosition.isEmpty()) {
            throw new IllegalStateException("position 계산 실패 path=" + path + ", line=" + line);
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
