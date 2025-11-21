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

    public ParsedFilePatch parse(String filePath, String patch) {
        if (patch == null || patch.isBlank()) {
            return ParsedFilePatch.of(filePath, List.of());
        }

        String[] lines = patch.split("\n");

        List<Hunk> hunks = new ArrayList<>();
        List<HunkLine> currentHunkLines = null;
        Integer originalStartLine = null;
        Integer startLine = null;
        int currentOriginalLine = 0;
        int currentLine = 0;

        new ParseContext();

        for (String line : lines) {
            // 새로운 Hunk 시작
            if (isHunkHeader(line)) {
                // 이전 Hunk flush
                if (currentHunkLines != null) {
                    hunks.add(Hunk.of(originalStartLine, startLine, new ArrayList<>(currentHunkLines)));
                }

                originalStartLine = Integer.parseInt(matcher.group(ORIGINAL_START_LINE));
                startLine = Integer.parseInt(matcher.group(START_LINE));

                currentOriginalLine = originalStartLine;
                currentLine = startLine;
                currentHunkLines = new ArrayList<>();

                continue;
            }

            // 아직 Hunk가 시작되지 않았다면
            if (currentHunkLines == null) {
                continue;
            }

            char symbol = line.charAt(0);
            String content = line.substring(1);

            if (symbol == ' ') {
                currentHunkLines.add(
                        HunkLine.of(currentOriginalLine, currentLine, HunkLineType.CONTEXT, content)
                );
                currentOriginalLine++;
                currentLine++;
                continue;
            }

            if (symbol == '+') {
                currentHunkLines.add(
                        HunkLine.of(null, currentLine, HunkLineType.ADDED, content)
                );
                currentLine++;
                continue;
            }

            if (symbol == '-') {
                currentHunkLines.add(
                        HunkLine.of(currentOriginalLine, null, HunkLineType.REMOVED, content)
                );
                currentOriginalLine++;
            }
        }

        if (currentHunkLines != null) {
            hunks.add(Hunk.of(originalStartLine, startLine, currentHunkLines));
        }

        return ParsedFilePatch.of(filePath, hunks);
    }

    private static boolean isHunkHeader(String line) {
        Matcher matcher = HUNK_HEADER.matcher(line);
        return matcher.find();
    }

    private static class ParseContext {
        private List<Hunk> hunks;

        private List<HunkLine> currentHunkLines;
        private Integer originalStartLine;
        private Integer startLine;
        private int currentOriginalLine;
        private int currentLine;

        public ParseContext create() {
            hunks = new ArrayList<>();
        }
    }
}
