package com.agilerunner.api.service;

import com.agilerunner.domain.ParsedFilePatch;
import com.agilerunner.util.GitHubPatchParser;
import lombok.RequiredArgsConstructor;
import org.kohsuke.github.GHPullRequest;
import org.kohsuke.github.GHPullRequestFileDetail;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class GitHubPatchService {

    private final GitHubPatchParser gitHubPatchParser;

    public List<ParsedFilePatch> buildParsedFilePatches(GHPullRequest pullRequest) {
        List<ParsedFilePatch> parsedFilePatches = new ArrayList<>();

        for (GHPullRequestFileDetail file : pullRequest.listFiles()) {
            handlePatch(file, parsedFilePatches);
        }

        return parsedFilePatches;
    }

    public Map<String, ParsedFilePatch> buildPathToPatch(List<ParsedFilePatch> parsedFilePatches) {
        Map<String, ParsedFilePatch> pathToParsedFilePatches = new HashMap<>();

        for (ParsedFilePatch filePatch : parsedFilePatches) {
            pathToParsedFilePatches.put(filePatch.getPath(), filePatch);
        }

        return pathToParsedFilePatches;
    }

    private void handlePatch(GHPullRequestFileDetail file, List<ParsedFilePatch> parsedFilePatches) {
        String patch = file.getPatch();
        if (isEmptyPatch(patch)) {
            return;
        }

        parsedFilePatches.add(getParsedFilePatch(file, patch));
    }

    private boolean isEmptyPatch(String patch) {
        return patch == null || patch.isBlank();
    }

    private ParsedFilePatch getParsedFilePatch(GHPullRequestFileDetail file, String patch) {
        return gitHubPatchParser.parse(file.getFilename(), patch);
    }
}
