package com.agilerunner.util;

import com.agilerunner.domain.Hunk;
import com.agilerunner.domain.HunkLine;
import com.agilerunner.domain.HunkLineType;
import com.agilerunner.domain.ParsedFilePatch;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class GitHubPatchParser {
    private static final Pattern HUNK_HEADER = Pattern.compile(
            "@@\\s+\\-(\\d+),?(\\d+)?\\s+\\+(\\d+),?(\\d+)?\\s+@@"
    );
    private static final int ORIGINAL_START_LINE = 1;
    private static final int START_LINE = 3;
    public static final int HUNK_LINE_TYPE_INDEX = 0;
    public static final int HUNK_LINE_CONTEXT_START = 1;
    public static final String PATCH_LINE_SEPARATOR = "\n";

    public ParsedFilePatch parse(String filePath, String patch) {
        if (hasNoPatchContent(patch)) {
            return ParsedFilePatch.of(filePath, List.of());
        }

        String[] lines = splitPatchIntoLines(patch);
        ParseContext context = ParseContext.create();

        context.collectHunksFrom(lines);

        return ParsedFilePatch.of(filePath, context.getHunks());
    }

    private String[] splitPatchIntoLines(String patch) {
        return patch.split(PATCH_LINE_SEPARATOR);
    }

    private boolean hasNoPatchContent(String patch) {
        return patch == null || patch.isBlank();
    }

    private static class ParseContext {
        private final List<Hunk> hunks = new ArrayList<>();

        private List<HunkLine> pendingHunkLines;
        private Integer originalStartLine;
        private Integer startLine;
        private int pendingOriginalLine;
        private int pendingLine;

        private ParseContext() {

        }

        public static ParseContext create() {
            return new ParseContext();
        }

        public List<Hunk> getHunks() {
            return hunks;
        }

        public void collectHunksFrom(String[] lines) {
            for (String line : lines) {
                handlePatchLine(line);
            }

            flushPendingHunkIfExists();
        }

        private void handlePatchLine(String line) {
            if (tryStartNewHunk(line)) {
                return;
            }

            if (hasNoPendingHunk()) {
                return;
            }

            addHunkLine(line);
        }

        private boolean tryStartNewHunk(String line) {
            Matcher matcher = HUNK_HEADER.matcher(line);
            if (!matcher.find()) {
                return false;
            }

            flushPendingHunkIfExists();
            initNewHunk(matcher);
            return true;
        }

        private void flushPendingHunkIfExists() {
            if (hasPendingHunk()) {
                flushPendingHunk();
            }
        }

        private boolean hasPendingHunk() {
            return pendingHunkLines != null;
        }

        private void flushPendingHunk() {
            hunks.add(Hunk.of(originalStartLine, startLine, new ArrayList<>(pendingHunkLines)));
            pendingHunkLines = null;
        }

        private void initNewHunk(Matcher matcher) {
            originalStartLine = Integer.parseInt(matcher.group(ORIGINAL_START_LINE));
            startLine = Integer.parseInt(matcher.group(START_LINE));

            pendingOriginalLine = originalStartLine;
            pendingLine = startLine;
            pendingHunkLines = new ArrayList<>();
        }

        private boolean hasNoPendingHunk() {
            return pendingHunkLines == null;
        }

        private void addHunkLine(String line) {
            char symbol = line.charAt(HUNK_LINE_TYPE_INDEX);
            String lineContent = line.substring(HUNK_LINE_CONTEXT_START);

            if (HunkLineType.isAdded(symbol)) {
                addAddedLine(lineContent);
                return;
            }
            if (HunkLineType.isContext(symbol)) {
                addContextLine(lineContent);
                return;
            }
            if (HunkLineType.isRemoved(symbol)) {
                addRemovedLine(lineContent);
            }
        }

        private void addAddedLine(String lineContent) {
            pendingHunkLines.add(
                    HunkLine.of(null, pendingLine, HunkLineType.ADDED, lineContent)
            );
            pendingLine++;
        }

        private void addContextLine(String lineContent) {
            pendingHunkLines.add(
                    HunkLine.of(pendingOriginalLine, pendingLine, HunkLineType.CONTEXT, lineContent)
            );
            pendingOriginalLine++;
            pendingLine++;
        }

        private void addRemovedLine(String lineContent) {
            pendingHunkLines.add(
                    HunkLine.of(pendingOriginalLine, null, HunkLineType.REMOVED, lineContent)
            );
            pendingOriginalLine++;
        }
    }
}
