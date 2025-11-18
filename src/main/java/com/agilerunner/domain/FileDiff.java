package com.agilerunner.domain;

import java.util.List;

public record FileDiff(
    String path,
    String patch,
    List<Integer> commentableLines) {

    public static FileDiff of(String filepath, String patch, List<Integer> commentableLines) {
        return new FileDiff(filepath, patch, commentableLines);
    }
}
