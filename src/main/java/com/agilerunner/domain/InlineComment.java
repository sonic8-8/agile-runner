package com.agilerunner.domain;

public class InlineComment {
    private String path;
    private int line;
    private String body;

    private InlineComment(String path, int line, String body) {
        this.path = path;
        this.line = line;
        this.body = body;
    }

    public static InlineComment of(String path, int line, String body) {
        return new InlineComment(path, line, body);
    }

    public String getPath() {
        return path;
    }

    public int getLine() {
        return line;
    }

    public String getBody() {
        return body;
    }
}
