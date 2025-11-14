package com.agilerunner.util;

import java.util.ArrayList;
import java.util.List;

public class GitHubPatchParser {
    public List<Integer> extractCommentableLines(String patch) {
        ArrayList<Integer> lines = new ArrayList<>();
        if (patch == null) {
            return lines;
        }

        int currentLine = 0;

        String[] split = patch.split("\n");
        for (String line : split) {
            if (line.startsWith("@@")) {
                int plusIndex = line.indexOf("+");
                int commaIndex = line.indexOf(",", plusIndex);
                currentLine = Integer.parseInt(line.substring(plusIndex + 1, commaIndex));
                continue;
            }

            if (line.startsWith("+") || line.startsWith(" ")) {
                lines.add(currentLine);
                currentLine++;
            } else if (line.startsWith("-")) {

            }
        }
        return lines;
    }
}
