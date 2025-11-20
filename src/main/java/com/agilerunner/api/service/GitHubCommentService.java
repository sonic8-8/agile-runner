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
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.OptionalInt;

@Service
@RequiredArgsConstructor
@Slf4j
public class GitHubCommentService {

    private final GitHubClientFactory gitHubClientFactory;
    private final GitHubPatchService gitHubPatchService;
    private final GitHubPositionConverter gitHubPositionConverter;

    public GitHubCommentResponse comment(Review review, GitHubEventServiceRequest request) {
        try {
            GitHub gitHub = gitHubClientFactory.createGitHubClient(request.installationId());

            GHRepository repository = gitHub.getRepository(review.getRepositoryName());
            GHPullRequest pullRequest = repository.getPullRequest(review.getPullRequestNumber());

            String headSha = pullRequest.getHead().getSha();

            // 메인 리뷰 코멘트
            GHIssueComment comment = pullRequest.comment(review.getReviewBody());

            // 인라인 리뷰 코멘트
            List<PostedInlineComment> postedInlineComments = new ArrayList<>();

            List<ParsedFilePatch> parsedFilePatches = gitHubPatchService.buildParsedFilePatches(pullRequest);
            Map<String, ParsedFilePatch> pathToParsedFilePatches = gitHubPatchService.buildPathToPatch(parsedFilePatches);

            for (InlineComment inlineComment : review.getInlineComments()) {
                String path = inlineComment.getPath();
                int newLine = inlineComment.getLine();

                ParsedFilePatch parsedFilePatch = pathToParsedFilePatches.get(path);
                if (parsedFilePatch == null) {
                    log.warn("해당 path에 대한 patch가 없습니다. path={}", path);
                    continue;
                }

                OptionalInt optionalGitHubPosition = gitHubPositionConverter.toGitHubPosition(parsedFilePatch, newLine);

                if (optionalGitHubPosition.isEmpty()) {
                    log.warn("position 계산 실패 path={}, line={}", path, newLine);
                    continue;
                }

                int position = optionalGitHubPosition.getAsInt();

                try {
                    GHPullRequestReviewComment reviewComment =
                            pullRequest.createReviewComment(
                                    inlineComment.getBody(),
                                    headSha,
                                    path,
                                    position
                            );

                    postedInlineComments.add(
                            PostedInlineComment.of(
                                    reviewComment.getId(),
                                    reviewComment.getHtmlUrl().toString()
                            ));
                } catch (IOException e) {
                    log.error("인라인 코멘트 작성 실패 path={}, position={}", path, position, e);
                }
            }

            return GitHubCommentResponse.of(
                    comment.getId(),
                    comment.getHtmlUrl().toString(),
                    postedInlineComments,
                    "리뷰 코멘트가 성공적으로 등록되었습니다."
            );
        } catch (Exception e) {
            log.error("GitHub 코멘트 등록 실패, repository={}, PR={}",
                    review.getRepositoryName(),
                    review.getPullRequestNumber(),
                    e);
            throw new RuntimeException("GitHub 코멘트 등록 실패");
        }
    }
}