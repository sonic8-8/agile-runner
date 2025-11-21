package com.agilerunner.domain;

public class HunkLine {
    private Integer oldLineIndex;
    private Integer newLineIndex;
    private HunkLineType hunkLineType;
    private String content;

    private HunkLine(Integer oldLineIndex, Integer newLineIndex, HunkLineType hunkLineType, String content) {
        this.oldLineIndex = oldLineIndex;
        this.newLineIndex = newLineIndex;
        this.hunkLineType = hunkLineType;
        this.content = content;
    }

    public static HunkLine of(Integer oldLineIndex, Integer newLineIndex, HunkLineType hunkLineType, String content) {
        return new HunkLine(oldLineIndex, newLineIndex, hunkLineType, content);
    }

    public Integer getNewLineIndex() {
        return newLineIndex;
    }

    public HunkLineType getHunkLineType() {
        return hunkLineType;
    }
}
