package com.agilerunner.api.controller.github.request;

import com.agilerunner.api.service.github.request.GitHubEventServiceRequest;
import com.agilerunner.domain.executioncontrol.ExecutionControlMode;
import com.agilerunner.domain.exception.AgileRunnerException;
import com.agilerunner.domain.exception.ErrorCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class GitHubEventRequestTest {

    @DisplayName("현재 webhook payload를 service request로 변환하면 실행 제어 모드는 기본적으로 NORMAL이다.")
    @Test
    void toService_defaultsExecutionControlModeToNormal() {
        // given
        GitHubEventRequest request = GitHubEventRequest.of(
                "pull_request",
                Map.of(
                        "action", "opened",
                        "installation", Map.of("id", 100L),
                        "repository", Map.of("full_name", "owner/repo"),
                        "pull_request", Map.of("number", 12)
                )
        );

        // when
        GitHubEventServiceRequest serviceRequest = request.toService();

        // then
        assertThat(serviceRequest.getExecutionControlMode()).isEqualTo(ExecutionControlMode.NORMAL);
        assertThat(serviceRequest.getExecutionControlMode().allowsWrite()).isTrue();
    }

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
