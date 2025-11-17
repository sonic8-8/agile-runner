package com.agilerunner.util;

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

    public List<Integer> extractCommentableLines(String patch) {
        ArrayList<Integer> result = new ArrayList<>();

        if (patch == null || patch.isBlank()) {
            return result;
        }

        String[] lines = patch.split("\n");

        int newLine = 0;

        for (String line : lines) {
            Matcher matcher = HUNK_HEADER.matcher(line);
            if (matcher.find()) {
                String newStartStr = matcher.group(3);
                newLine = Integer.parseInt(newStartStr);

                continue;
            }

            if (line.startsWith("+") && !line.startsWith("+++")) {
                result.add(newLine);
                newLine++;
            } else if (line.startsWith(" ") || line.isEmpty()) {
                result.add(newLine);
                newLine++;
            } else if (line.startsWith("-") && !line.startsWith("---")) {

            }
        }
        return result;
    }
}
