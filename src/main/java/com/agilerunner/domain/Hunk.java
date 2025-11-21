package com.agilerunner.domain;

import java.util.List;

public class Hunk {
    private int originalStartLine;
    private int startLine;
    private List<HunkLine> hunkLines;

    private Hunk(int originalStartLine, int startLine, List<HunkLine> hunkLines) {
        this.originalStartLine = originalStartLine;
        this.startLine = startLine;
        this.hunkLines = hunkLines;
    }

    public static Hunk of(int originalStartLine, int startLine, List<HunkLine> hunkLines) {
        return new Hunk(originalStartLine, startLine, hunkLines);
    }

    public List<HunkLine> getHunkLines() {
        return hunkLines;
    }
}
