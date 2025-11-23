package com.agilerunner.domain;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum HunkLineType {
    ADDED('+'),
    REMOVED('-'),
    CONTEXT(' ');

    private final char symbol;

    public char getSymbol() {
        return symbol;
    }

    public static boolean isAdded(char symbol) {
        return ADDED.symbol == symbol;
    }

    public static boolean isRemoved(char symbol) {
        return REMOVED.symbol == symbol;
    }

    public static boolean isContext(char symbol) {
        return CONTEXT.symbol == symbol;
    }

    public boolean isRemoved() {
        return this == REMOVED;
    }
}
