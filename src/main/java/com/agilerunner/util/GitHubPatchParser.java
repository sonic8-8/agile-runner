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

    private static final int OLD_START = 1;
    private static final int OLD_LINE_COUNT = 2;
    private static final int NEW_START = 3;
    private static final int NEW_LINE_COUNT = 4;

    public ParsedFilePatch parse(String filePath, String patch) {
        List<Hunk> hunks = new ArrayList<>();

        if (patch == null || patch.isBlank()) {
            return ParsedFilePatch.of(filePath, hunks);
        }

        String[] lines = patch.split("\n");

        List<HunkLine> currentHunkLines = null;
        Integer oldStart = null;
        Integer newStart = null;
        int currentOldLine = 0;
        int currentNewLine = 0;


        for (String line : lines) {
            Matcher matcher = HUNK_HEADER.matcher(line);

            // 새로운 Hunk 시작
            if (matcher.find()) {
                // 이전 Hunk flush
                if (currentHunkLines != null) {
                    hunks.add(Hunk.of(oldStart, newStart, new ArrayList<>(currentHunkLines)));
                }

                oldStart = Integer.parseInt(matcher.group(OLD_START));
                newStart = Integer.parseInt(matcher.group(NEW_START));

                currentOldLine = oldStart;
                currentNewLine = newStart;
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
                        HunkLine.of(currentOldLine, currentNewLine, HunkLineType.CONTEXT, content)
                );
                currentOldLine++;
                currentNewLine++;
            } else if (symbol == '+') {
                currentHunkLines.add(
                        HunkLine.of(null, currentNewLine, HunkLineType.ADDED, content)
                );
                currentNewLine++;
            } else if (symbol == '-') {
                currentHunkLines.add(
                        HunkLine.of(currentOldLine, null, HunkLineType.REMOVED, content)
                );
                currentOldLine++;
            }
        }

        if (currentHunkLines != null) {
            hunks.add(Hunk.of(oldStart, newStart, currentHunkLines));
        }

        return ParsedFilePatch.of(filePath, hunks);
    }
}
