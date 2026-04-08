package com.agilerunner.api.service.github.request;

import com.agilerunner.domain.executioncontrol.ExecutionControlMode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.LinkedHashMap;
import java.util.Map;

import static com.agilerunner.GitHubEventType.PULL_REQUEST;
import static org.assertj.core.api.Assertions.assertThat;

class GitHubEventServiceRequestTest {

    @DisplayName("명시적 DRY_RUN 입력으로 service request를 만들면 실행 제어 모드, payload 해석, 선택 파일 경로가 유지된다.")
    @Test
    void of_preservesExplicitDryRunModeAndPayloadInterpretation() {
        // given
        Map<String, Object> payload = buildPayload();

        // when
        GitHubEventServiceRequest request = GitHubEventServiceRequest.of(
                PULL_REQUEST,
                payload,
                100L,
                ExecutionControlMode.DRY_RUN,
                List.of("src/main/App.java", "src/test/AppTest.java")
        );

        // then
        assertThat(request.getExecutionControlMode()).isEqualTo(ExecutionControlMode.DRY_RUN);
        assertThat(request.getExecutionControlMode().allowsWrite()).isFalse();
        assertThat(request.getInstallationId()).isEqualTo(100L);
        assertThat(request.getRepositoryName()).isEqualTo("owner/repo");
        assertThat(request.getPullRequestNumber()).isEqualTo(12);
        assertThat(request.getAction()).isEqualTo("opened");
        assertThat(request.getSelectedPaths()).containsExactly(
                "src/main/App.java",
                "src/test/AppTest.java"
        );
    }

    @DisplayName("선택 파일 경로를 명시하지 않으면 service request는 빈 목록으로 해석한다.")
    @Test
    void of_defaultsSelectedPathsToEmptyList() {
        // given
        Map<String, Object> payload = buildPayload();

        // when
        GitHubEventServiceRequest request = GitHubEventServiceRequest.of(
                PULL_REQUEST,
                payload,
                100L,
                ExecutionControlMode.NORMAL
        );

        // then
        assertThat(request.getSelectedPaths()).isEmpty();
    }

    private Map<String, Object> buildPayload() {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("action", "opened");
        payload.put("repository", Map.of("full_name", "owner/repo"));
        payload.put("pull_request", Map.of("number", 12));
        return payload;
    }
}
