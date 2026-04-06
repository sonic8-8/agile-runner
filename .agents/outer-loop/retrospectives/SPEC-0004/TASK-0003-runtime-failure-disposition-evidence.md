---
spec_id: SPEC-0004
task_id: TASK-0003
github_issue_number: 15
criteria_keys:
  - runtime-evidence-records-failure-disposition
delivery_ids:
  - task-0003-verify-20260406-2144-001
execution_keys:
  - EXECUTION:task-0003-verify-20260406-2144-001
test_evidence_ref:
  - "targeted: GRADLE_USER_HOME=/home/seaung13/workspace/agile-runner/.gradle-local scripts/gradlew-java21.sh --no-daemon test --tests 'com.agilerunner.api.service.agentruntime.AgentRuntimeServiceTest' --tests 'com.agilerunner.client.agentruntime.AgentRuntimeRepositoryTest' --tests 'com.agilerunner.api.controller.GitHubWebhookControllerTest' --tests 'com.agilerunner.api.service.GitHubCommentServiceTest' --console=plain"
  - "full: GRADLE_USER_HOME=/home/seaung13/workspace/agile-runner/.gradle-local scripts/gradlew-java21.sh --no-daemon test --console=plain"
  - "runtime: local profile bootRun -> POST /webhook/github -> app shutdown -> H2 shell query"
diff_ref: "git diff -- .agents/active/tasks.md src/main/java/com/agilerunner/domain/agentruntime/WebhookExecution.java src/main/java/com/agilerunner/domain/agentruntime/AgentExecutionLog.java src/main/java/com/agilerunner/api/service/agentruntime/AgentRuntimeService.java src/main/java/com/agilerunner/client/agentruntime/AgentRuntimeRepository.java src/main/resources/agent-runtime/schema.sql src/test/java/com/agilerunner/api/service/agentruntime/AgentRuntimeServiceTest.java src/test/java/com/agilerunner/client/agentruntime/AgentRuntimeRepositoryTest.java"
failure_summary: "대표 실패 실행을 실제로 검증하는 동안 H2 file DB는 애플리케이션이 붙어 있을 때 Shell 조회가 바로 되지 않았고, 앱 종료 후 evidence를 읽어야 했다."
root_cause: "local profile의 H2 file 모드는 단일 프로세스 잠금 특성이 있어, representative verification에서 애플리케이션 실행 중 외부 Shell 연결이 곧바로 되지 않는다."
agents_check_findings:
  - "실패 대응 분류 정책 자체는 TASK-0002에 두고, 이번 task는 runtime evidence 적재와 실제 H2 검증에만 범위를 고정했다."
  - "WebhookExecution과 AgentExecutionLog에 같은 execution_key 기준으로 error_code와 failure_disposition이 함께 적재되는지 확인했다."
  - "전체 ./gradlew test와 실제 앱/H2 representative verification을 모두 수행해 종료 검증을 닫았다."
next_task_warnings:
  - "다음 representative H2 verification도 fresh delivery_id를 먼저 정하고 execution_key를 retrospective에 함께 기록해야 한다."
  - "실행 중인 애플리케이션이 H2 file을 점유하므로, Shell 조회가 필요하면 앱 종료 후 evidence를 확인하는 순서를 유지해야 한다."
error_signature: "H2 file lock during live app verification"
test_result_summary: "targeted test, 전체 ./gradlew test, local profile representative verification 모두 통과, 추가 proposal 없음"
snapshot_hashes: []
---

# 작업 회고

## 작업 요약
- `WebhookExecution`과 `AgentExecutionLog`에 failure disposition 필드를 추가했다.
- `AgentRuntimeService.recordFailure(...)`가 error code와 같은 기준으로 failure disposition을 계산해 실행 근거에 적재하도록 정리했다.
- local profile 실제 앱 기동 후 fresh `delivery_id`로 representative failure를 실행하고, H2에서 같은 `execution_key` 기준 evidence를 확인했다.

## 실패 요약
- 테스트와 적재 자체는 의도대로 동작했지만, 애플리케이션이 H2 file을 사용 중일 때는 외부 Shell로 즉시 조회할 수 없었다.
- representative verification에서는 앱 종료 후 H2 evidence를 읽는 순서가 필요했다.

## Root Cause
- local profile의 H2 file DB는 실행 중 프로세스가 파일 잠금을 잡고 있어서, 외부 Shell 조회가 바로 허용되지 않는다.
- representative verification 절차에서 “요청 후 즉시 H2 Shell 조회”를 가정하면 false failure처럼 보일 수 있다.

## AGENTS 체크 결과
- 실패 대응 분류 정책 변경은 하지 않고 runtime evidence 적재 범위만 수정했다.
- 도메인 상태는 `domain/agentruntime`, orchestration은 `api/service/agentruntime`, SQL/H2 정렬은 `client/agentruntime`와 `schema.sql`에 머물러 계층 책임을 넘지 않았다.
- targeted test, full test, 실제 앱/H2 representative verification까지 모두 수행했다.

## 근거 Artifact
- `.agents/active/tasks.md`
- `src/main/java/com/agilerunner/domain/agentruntime/WebhookExecution.java`
- `src/main/java/com/agilerunner/domain/agentruntime/AgentExecutionLog.java`
- `src/main/java/com/agilerunner/api/service/agentruntime/AgentRuntimeService.java`
- `src/main/java/com/agilerunner/client/agentruntime/AgentRuntimeRepository.java`
- `src/main/resources/agent-runtime/schema.sql`
- `src/test/java/com/agilerunner/api/service/agentruntime/AgentRuntimeServiceTest.java`
- `src/test/java/com/agilerunner/client/agentruntime/AgentRuntimeRepositoryTest.java`
- `src/test/java/com/agilerunner/api/controller/GitHubWebhookControllerTest.java`
- `src/test/java/com/agilerunner/api/service/GitHubCommentServiceTest.java`

## 다음 Task 경고사항
- 다음 spec에서 representative runtime verification을 할 때도 fresh `delivery_id`와 `execution_key`를 먼저 고정해야 한다.
- H2 file Shell 조회가 필요하면 애플리케이션 종료 후 evidence를 읽는 순서를 기본으로 가져가야 한다.

## 제안 필요 여부
- 없음
- 이번 task는 기존 representative verification 규칙을 실제로 적용한 사례이고, 새 AGENTS/workflow 규칙이 필요한 패턴까지는 드러나지 않았다.
