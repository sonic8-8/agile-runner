package com.agilerunner.domain.executioncontrol;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ExecutionControlModeTest {

    @DisplayName("NORMAL 모드는 write를 허용한다.")
    @Test
    void normalMode_allowsWrite() {
        // when & then
        assertThat(ExecutionControlMode.NORMAL.allowsWrite()).isTrue();
    }

    @DisplayName("DRY_RUN 모드는 write를 허용하지 않는다.")
    @Test
    void dryRunMode_doesNotAllowWrite() {
        // when & then
        assertThat(ExecutionControlMode.DRY_RUN.allowsWrite()).isFalse();
    }
}
