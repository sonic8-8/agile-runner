package com.agilerunner.domain;

public class HunkLine {
    private Integer originalLine;
    private Integer line;
    private HunkLineType hunkLineType;
    private String lineContent;

    private HunkLine(Integer originalLine, Integer line, HunkLineType hunkLineType, String lineContent) {
        this.originalLine = originalLine;
        this.line = line;
        this.hunkLineType = hunkLineType;
        this.lineContent = lineContent;
    }

    public static HunkLine of(Integer originalLine, Integer line, HunkLineType hunkLineType, String lineContent) {
        return new HunkLine(originalLine, line, hunkLineType, lineContent);
    }

    public Integer getLine() {
        return line;
    }

    public HunkLineType getHunkLineType() {
        return hunkLineType;
    }

    public boolean isRemoved() {
        return hunkLineType.isRemoved();
    }
}
