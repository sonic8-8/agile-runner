package com.agilerunner.api.service.dto;

import java.util.List;

public class FileDiff {
    private String path;
    private String patch;
    private List<Integer> commentableLines;

    private FileDiff(String path, String patch, List<Integer> commentableLines) {
        this.path = path;
        this.patch = patch;
        this.commentableLines = commentableLines;
    }

    public static FileDiff of(String filepath, String patch, List<Integer> commentableLines) {
        return new FileDiff(filepath, patch, commentableLines);
    }

    public String getPath() {
        return path;
    }

    public String getPatch() {
        return patch;
    }

    public List<Integer> getCommentableLines() {
        return commentableLines;
    }
}
