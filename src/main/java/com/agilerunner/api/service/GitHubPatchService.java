package com.agilerunner.api.service;

import com.agilerunner.domain.ParsedFilePatch;
import com.agilerunner.util.GitHubPatchParser;
import lombok.RequiredArgsConstructor;
import org.kohsuke.github.GHPullRequest;
import org.kohsuke.github.GHPullRequestFileDetail;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@RequiredArgsConstructor
public class GitHubPatchService {

    private final GitHubPatchParser gitHubPatchParser;

    public List<ParsedFilePatch> buildFilePatches(GHPullRequest pullRequest) {
        List<ParsedFilePatch> parsedFilePatches = new ArrayList<>();

        for (GHPullRequestFileDetail file : pullRequest.listFiles()) {
            String patch = file.getPatch();
            if (patch == null || patch.isBlank()) {
                continue;
            }

            ParsedFilePatch filePatch = gitHubPatchParser.parse(file.getFilename(), patch);
            parsedFilePatches.add(filePatch);
        }

        return parsedFilePatches;
    }

    public Map<String, ParsedFilePatch> buildPathToPatch(List<ParsedFilePatch> parsedFilePatches) {
        Map<String, ParsedFilePatch> pathToParsedFilePatches = new HashMap<>();

        for (ParsedFilePatch filePatch  : parsedFilePatches) {
            pathToParsedFilePatches.put(filePatch.getPath(), filePatch);
        }

        return pathToParsedFilePatches;
    }

    public Map<String, Set<Integer>> buildPathToCommentableLines(List<ParsedFilePatch> parsedFilePatches) {
        HashMap<String, Set<Integer>> pathToCommentableLines = new HashMap<>();

        for (ParsedFilePatch parsedFilePatch : parsedFilePatches) {
            pathToCommentableLines.put(
                    parsedFilePatch.path(),
                    new HashSet<>(parsedFilePatch.commentableLines())
            );
        }
        return pathToCommentableLines;
    }
}
