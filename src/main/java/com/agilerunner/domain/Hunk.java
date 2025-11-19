package com.agilerunner.domain;

import java.util.List;

public class Hunk {
    private int oldStart;
    private int newStart;
    private List<HunkLine> hunkLines;

    private Hunk(int oldStart, int newStart, List<HunkLine> hunkLines) {
        this.oldStart = oldStart;
        this.newStart = newStart;
        this.hunkLines = hunkLines;
    }

    public static Hunk of(int oldStart, int newStart, List<HunkLine> hunkLines) {
        return new Hunk(oldStart, newStart, hunkLines);
    }

    public int getOldStart() {
        return oldStart;
    }

    public int getNewStart() {
        return newStart;
    }

    public List<HunkLine> getHunkLines() {
        return hunkLines;
    }
}
