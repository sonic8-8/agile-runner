package com.agilerunner.util;

import com.agilerunner.domain.Hunk;
import com.agilerunner.domain.HunkLine;
import com.agilerunner.domain.HunkLineType;
import com.agilerunner.domain.ParsedFilePatch;
import org.springframework.stereotype.Component;

import java.util.OptionalInt;

@Component
public class GitHubPositionConverter {

    public OptionalInt toGitHubPosition(ParsedFilePatch patch, int newLineIndex) {
        int position = 0;

        for (Hunk hunk : patch.getHunks()) {
            position++;

            for (HunkLine hunkLine : hunk.getHunkLines()) {
                if (hunkLine.getHunkLineType() != HunkLineType.REMOVED
                        && hunkLine.getNewLineIndex() != null
                        && hunkLine.getNewLineIndex() == newLineIndex) {
                    return OptionalInt.of(position);
                }

                position++;
            }
        }
        return OptionalInt.empty();
    }
}
