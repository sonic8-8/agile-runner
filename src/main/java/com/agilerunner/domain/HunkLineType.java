package com.agilerunner.domain;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum HunkLineType {
    ADDED('+'),
    REMOVED('-'),
    CONTEXT(' ');

    private final char symbol;
}
