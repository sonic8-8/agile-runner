package com.agilerunner.domain;

import java.util.List;

public class ParsedFilePatch {
    private String path;
    private List<Hunk> hunks;

    private ParsedFilePatch(String path, List<Hunk> hunks) {
        this.path = path;
        this.hunks = hunks;
    }

    public static ParsedFilePatch of(String filepath, List<Hunk> hunks) {
        return new ParsedFilePatch(filepath, hunks);
    }

    public String getPath() {
        return path;
    }

    public List<Hunk> getHunks() {
        return hunks;
    }
}
