package com.agilerunner.domain;

import java.util.List;

public class Hunk {
    private String header;
    private int oldStart;
    private int newStart;
    private List<HunkLine> hunkLines;

    private Hunk(String header, int oldStart, int newStart, List<HunkLine> hunkLines) {
        this.header = header;
        this.oldStart = oldStart;
        this.newStart = newStart;
        this.hunkLines = hunkLines;
    }

    public static Hunk of(String header, int oldStart, int newStart, List<HunkLine> hunkLines) {
        return new Hunk(header, oldStart, newStart, hunkLines);
    }
}
