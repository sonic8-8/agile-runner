package com.agilerunner.api.controller.github.request;

import com.agilerunner.domain.exception.AgileRunnerException;
import com.agilerunner.domain.exception.ErrorCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class GitHubEventRequestTest {

    @DisplayName("installation 객체에 id가 없으면 공통 예외와 오류 코드로 분류한다.")
    @Test
    void getInstallationId_throwsAgileRunnerExceptionWhenInstallationIdIsMissing() {
        // given
        GitHubEventRequest request = GitHubEventRequest.of("pull_request", Map.of("installation", Map.of()));

        // when & then
        assertThatThrownBy(request::getInstallationId)
                .isInstanceOfSatisfying(AgileRunnerException.class, exception -> {
                    assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.GITHUB_INSTALLATION_ID_MISSING);
                });
    }
}
