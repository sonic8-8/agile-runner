package com.agilerunner.util;

import com.agilerunner.domain.Hunk;
import com.agilerunner.domain.HunkLine;
import com.agilerunner.domain.ParsedFilePatch;
import org.springframework.stereotype.Component;

import java.util.OptionalInt;

@Component
public class GitHubPositionConverter {

    public OptionalInt toPosition(ParsedFilePatch parsedFilePatch, int targetLine) {
        GitHubPosition position = GitHubPosition.create();
        return position.findTargetPositionInPatch(parsedFilePatch, targetLine);
    }

    private static class GitHubPosition {
        private int position = 0;

        private GitHubPosition() {
        }

        public static GitHubPosition create() {
            return new GitHubPosition();
        }

        public OptionalInt findTargetPositionInPatch(ParsedFilePatch parsedFilePatch, int targetLine) {
            return parsedFilePatch.getHunks().stream()
                    .peek(hunk -> moveToNext())
                    .map(hunk -> scanHunkForTargetPosition(hunk, targetLine))
                    .filter(OptionalInt::isPresent)
                    .findFirst()
                    .orElseGet(OptionalInt::empty);
        }

        private void moveToNext() {
            position++;
        }

        private OptionalInt scanHunkForTargetPosition(Hunk hunk, int targetLine) {
            return hunk.getHunkLines().stream()
                    .peek(hunkLine -> moveToNext())
                    .filter(hunkLine -> isTargetLine(hunkLine, targetLine))
                    .findFirst()
                    .map(hunkLine -> OptionalInt.of(position))
                    .orElseGet(OptionalInt::empty);
        }

        private boolean isTargetLine(HunkLine hunkLine, int targetLine) {
            if (hunkLine.isRemoved()) {
                return false;
            }

            Integer line = hunkLine.getLine();
            if (line == null) {
                return false;
            }

            return line == targetLine;
        }
    }
}
