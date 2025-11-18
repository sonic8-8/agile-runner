package com.agilerunner.domain;

public class HunkLine {
    private Integer oldLine;
    private Integer newLine;
    private char type;
    private int position;

    private HunkLine(Integer oldLine, Integer newLine, char type, int position) {
        this.oldLine = oldLine;
        this.newLine = newLine;
        this.type = type;
        this.position = position;
    }

    public static HunkLine of(Integer oldLine, Integer newLine, char type, int position) {
        return new HunkLine(oldLine, newLine, type, position);
    }
}
