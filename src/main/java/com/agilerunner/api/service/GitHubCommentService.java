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
            GHPullRequest pullRequest = loadPullRequest(review, request);
            String headSha = pullRequest.getHead().getSha();
            GHIssueComment mainComment = pullRequest.comment(review.getReviewBody());
            Map<String, ParsedFilePatch> pathToParsedFilePatches = buildParsedFilePatches(pullRequest);
            InlineCommentContext context = InlineCommentContext.of(pullRequest, headSha, pathToParsedFilePatches);
            List<PostedInlineComment> postedInlineComments = postInlineComments(review, context);

            return buildResponse(mainComment, postedInlineComments);
        } catch (Exception e) {
            log.error("GitHub 코멘트 등록 실패, repository={}, PR={}",
                    review.getRepositoryName(), review.getPullRequestNumber(), e);
            throw new RuntimeException("GitHub 코멘트 등록 실패", e);
        }
    }

    private Map<String, ParsedFilePatch> buildParsedFilePatches(GHPullRequest pullRequest) {
        List<ParsedFilePatch> parsedFilePatches = gitHubPatchService.buildParsedFilePatches(pullRequest);
        return gitHubPatchService.buildPathToPatch(parsedFilePatches);
    }

    private GHPullRequest loadPullRequest(Review review, GitHubEventServiceRequest request) throws Exception {
        GitHub gitHub = gitHubClientFactory.createGitHubClient(request.installationId());
        GHRepository repository = gitHub.getRepository(review.getRepositoryName());
        return repository.getPullRequest(review.getPullRequestNumber());
    }

    private List<PostedInlineComment> postInlineComments(Review review, InlineCommentContext context) {
        List<PostedInlineComment> postedInlineComments = new ArrayList<>();

        for (InlineComment inlineComment : review.getInlineComments()) {
            postInlineComment(inlineComment, context)
                    .ifPresent(postedInlineComments::add);
        }

        return postedInlineComments;
    }

    private Optional<PostedInlineComment> postInlineComment(InlineComment inlineComment, InlineCommentContext context) {
        String path = inlineComment.getPath();
        int line = inlineComment.getLine();

        ParsedFilePatch parsedFilePatch = context.getPathToParsedFilePatches().get(path);
        if (parsedFilePatch == null) {
            log.warn("해당 path에 대한 patch가 없습니다. path={}", path);
            return Optional.empty();
        }

        OptionalInt optionalPosition = gitHubPositionConverter.toPosition(parsedFilePatch, line);
        if (optionalPosition.isEmpty()) {
            log.warn("position 계산 실패 path={}, line={}", path, line);
            return Optional.empty();
        }

        int position = optionalPosition.getAsInt();
        return createReviewComment(inlineComment, context, path, position);
    }

    private Optional<PostedInlineComment> createReviewComment(InlineComment inlineComment, InlineCommentContext context, String path, int position) {
        try {
            GHPullRequestReviewComment reviewComment =
                    context.getPullRequest().createReviewComment(
                            inlineComment.getBody(),
                            context.getHeadSha(),
                            path,
                            position
                    );

            return Optional.of(
                    PostedInlineComment.of(
                            reviewComment.getId(),
                            reviewComment.getHtmlUrl().toString()
                    )
            );
        } catch (IOException e) {
            log.error("인라인 코멘트 작성 실패 path={}, position={}", path, position, e);
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

    private static class InlineCommentContext {
        private GHPullRequest pullRequest;
        private String headSha;
        private Map<String, ParsedFilePatch> pathToParsedFilePatches;

        private InlineCommentContext(GHPullRequest pullRequest, String headSha, Map<String, ParsedFilePatch> pathToParsedFilePatches) {
            this.pullRequest = pullRequest;
            this.headSha = headSha;
            this.pathToParsedFilePatches = pathToParsedFilePatches;
        }

        public static InlineCommentContext of(GHPullRequest pullRequest, String headSha, Map<String, ParsedFilePatch> pathToParsedFilePatches) {
            return new InlineCommentContext(pullRequest, headSha, pathToParsedFilePatches);
        }

        public GHPullRequest getPullRequest() {
            return pullRequest;
        }

        public String getHeadSha() {
            return headSha;
        }

        public Map<String, ParsedFilePatch> getPathToParsedFilePatches() {
            return pathToParsedFilePatches;
        }
    }
}