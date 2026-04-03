---
spec_id: SPEC-0002
task_id: TASK-0003
github_issue_number: 9
criteria_keys:
  - renamed-schema-query-roundtrip-preserved
  - runtime-evidence-recorded-under-renamed-schema
delivery_ids:
  - task-0003-verify-001
  - task-0003-verify-002
execution_keys:
  - EXECUTION:task-0003-verify-002
test_evidence_ref: "targeted: GRADLE_USER_HOME=/home/seaung13/workspace/agile-runner/.gradle-local scripts/gradlew-java21.sh --no-daemon test --tests 'com.agilerunner.client.agentruntime.AgentRuntimeRepositoryTest' --tests 'com.agilerunner.api.service.agentruntime.AgentRuntimeServiceTest' --tests 'com.agilerunner.api.controller.GitHubWebhookControllerTest' --tests 'com.agilerunner.api.service.GitHubCommentServiceTest' --console=plain; full suite: GRADLE_USER_HOME=/home/seaung13/workspace/agile-runner/.gradle-local scripts/gradlew-java21.sh --no-daemon test --console=plain"
diff_ref: "src/main/resources/agent-runtime/schema.sql, src/main/java/com/agilerunner/client/agentruntime/AgentRuntimeRepository.java, src/test/java/com/agilerunner/client/agentruntime/AgentRuntimeRepositoryTest.java, .agents/active/tasks.md"
failure_summary: "첫 representative delivery `task-0003-verify-001`은 기존 local H2 row와 delivery_id가 충돌해 false negative를 만들었다. 새 delivery `task-0003-verify-002`로 재검증하자 renamed schema 기준 적재가 정상 확인됐다."
root_cause: "local agent-runtime file DB가 spec 간에도 유지되기 때문에 representative delivery를 고정 문자열로 재사용하면 물리 스키마 문제와 무관한 unique delivery 충돌이 발생할 수 있다. 또한 schema.sql은 기존 file DB까지 고려해 멱등적 rename migration을 포함해야 했다."
agents_check_findings:
  - "Tester는 repository/schema integration test로 새 물리 스키마 이름을 먼저 red로 고정했다."
  - "Constructor는 schema.sql과 repository SQL/row mapper/parameter binding만 바꾸고 외부 webhook 계약은 건드리지 않았다."
  - "3-agent constructor review에서 컬럼 rename migration의 멱등성을 보완한 뒤 PASS를 받았다."
  - "targeted test, 전체 테스트, 실제 앱/H2 검증까지 수행했다."
next_task_warnings:
  - "실제 앱/H2 representative 검증에서는 이전 task와 겹치지 않는 fresh delivery id를 사용해야 한다."
  - "schema rename task에서는 fresh DB뿐 아니라 기존 local file DB 재기동 경로의 멱등성도 같이 확인해야 한다."
error_signature: "DuplicateKeyException on delivery_id when reusing task-0003-verify-001, then IllegalStateException: GitHub App ID가 설정되지 않았습니다. after fresh delivery."
test_result_summary: "AgentRuntimeRepositoryTest green, AgentRuntimeServiceTest green, GitHubWebhookControllerTest green, GitHubCommentServiceTest green, 전체 ./gradlew test green, local boot + renamed schema migration + representative webhook/H2 확인 완료."
snapshot_hashes: []
---

# 작업 회고

## 작업 요약
- `TASK_STATE`, `EVALUATION_CRITERIA`, `REVIEW_RUN`, `run_key`를 `TASK_RUNTIME_STATE`, `VALIDATION_CRITERIA`, `WEBHOOK_EXECUTION`, `execution_key`로 정렬했다.
- repository SQL, row mapper, parameter binding, schema에 종속된 조회 메서드를 새 물리 스키마 이름 기준으로 맞췄다.
- repository integration test와 local profile 실제 앱/H2 검증으로 renamed schema round-trip과 runtime evidence 적재를 모두 확인했다.

## 실패 요약
- 첫 representative delivery `task-0003-verify-001`은 기존 local H2 file에 남아 있던 동일 delivery row와 충돌해 unique delivery 제약 오류를 만들었다.
- 같은 경로를 fresh delivery `task-0003-verify-002`로 다시 검증하자, schema migration과 runtime evidence 적재는 정상 동작했고 이후 500은 local GitHub App 설정 누락에 따른 예상 가능한 실패였다.

## Root Cause
- local agent-runtime file DB는 task 간에 유지되는데, representative delivery id를 고정 패턴으로 재사용해 false negative가 섞였다.
- 기존 local file DB까지 지원하려면 schema.sql이 fresh create만이 아니라 idempotent rename migration까지 포함해야 했다.

## AGENTS 체크 결과
- `Tester`는 production code를 수정하지 않고 repository/schema integration test를 먼저 red로 고정했다.
- `Constructor`는 schema.sql과 repository 계층에만 변경을 제한해 `TASK-0003` 범위를 지켰다.
- targeted test, 전체 테스트, 실제 앱/H2 representative 검증을 모두 수행했다.

## 근거 Artifact
- 기준 문서:
  - `.agents/active/spec.md`
  - `.agents/active/tasks.md`
  - `.agents/criteria/SPEC-0002-agent-runtime-terminology-alignment.json`
- 코드 근거:
  - `src/main/resources/agent-runtime/schema.sql`
  - `src/main/java/com/agilerunner/client/agentruntime/AgentRuntimeRepository.java`
- 테스트 근거:
  - `src/test/java/com/agilerunner/client/agentruntime/AgentRuntimeRepositoryTest.java`
  - `src/test/java/com/agilerunner/api/service/agentruntime/AgentRuntimeServiceTest.java`
  - `src/test/java/com/agilerunner/api/controller/GitHubWebhookControllerTest.java`
  - `src/test/java/com/agilerunner/api/service/GitHubCommentServiceTest.java`
- 실행 근거:
  - full suite `./gradlew test`
  - local profile app boot on `:18080`
  - H2 query 결과 `WEBHOOK_EXECUTION`, `TASK_RUNTIME_STATE`, `VALIDATION_CRITERIA`, `AGENT_EXECUTION_LOG.execution_key` 확인
  - representative delivery `task-0003-verify-002`

## 다음 Task 경고사항
- 실제 앱/H2 representative 검증에서는 task별 fresh delivery id를 사용해야 한다.
- 후속 task에서도 local file DB를 쓰는 검증이면 기존 row와 충돌하지 않는 query key를 미리 정해야 한다.

## 제안 필요 여부
- 있음
- representative runtime 검증 규칙에 fresh delivery id 사용을 명시하는 workflow 제안이 필요하다.
