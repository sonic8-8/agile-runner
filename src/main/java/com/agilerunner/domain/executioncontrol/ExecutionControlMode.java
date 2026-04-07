package com.agilerunner.domain.executioncontrol;

public enum ExecutionControlMode {
    NORMAL(true),
    DRY_RUN(false);

    private final boolean allowsWrite;

    ExecutionControlMode(boolean allowsWrite) {
        this.allowsWrite = allowsWrite;
    }

    public boolean allowsWrite() {
        return allowsWrite;
    }
}
