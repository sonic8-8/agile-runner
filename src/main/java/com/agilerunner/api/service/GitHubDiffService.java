package com.agilerunner.api.service;

import com.agilerunner.api.service.dto.FileDiff;
import com.agilerunner.util.GitHubPatchParser;
import lombok.RequiredArgsConstructor;
import org.kohsuke.github.GHPullRequest;
import org.kohsuke.github.GHPullRequestFileDetail;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@RequiredArgsConstructor
public class GitHubDiffService {

    private final GitHubPatchParser gitHubPatchParser;

    public List<FileDiff> buildFileDiffs(GHPullRequest pullRequest) {
        List<FileDiff> fileDiffs = new ArrayList<>();

        for (GHPullRequestFileDetail file : pullRequest.listFiles()) {
            String patch = file.getPatch();
            if (patch == null || patch.isBlank()) {
                continue;
            }

            List<Integer> commentableLines = gitHubPatchParser.extractCommentableLines(patch);

            fileDiffs.add(FileDiff.of(
                    file.getFilename(),
                    patch,
                    commentableLines));
        }
        return fileDiffs;
    }

    public Map<String, Set<Integer>> buildPathToCommentableLines(List<FileDiff> fileDiffs) {
        HashMap<String, Set<Integer>> pathToCommentableLines = new HashMap<>();

        for (FileDiff fileDiff : fileDiffs) {
            pathToCommentableLines.put(
                    fileDiff.path(),
                    new HashSet<>(fileDiff.commentableLines())
            );
        }
        return pathToCommentableLines;
    }
}
