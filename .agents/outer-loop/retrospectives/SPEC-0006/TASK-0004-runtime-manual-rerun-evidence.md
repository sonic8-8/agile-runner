---
spec_id: SPEC-0006
task_id: TASK-0004
github_issue_number: 23
criteria_keys:
  - runtime-evidence-distinguishes-manual-rerun
delivery_ids:
  - MANUAL_RERUN_DELIVERY:8dbdf886-cdb7-492d-b273-612210f8acdb
execution_keys:
  - EXECUTION:MANUAL_RERUN:8dbdf886-cdb7-492d-b273-612210f8acdb
test_evidence_ref:
  - "targeted: ./scripts/gradlew-java21.sh --no-daemon test --tests 'com.agilerunner.api.controller.review.ManualRerunControllerTest' --tests 'com.agilerunner.api.service.review.ManualRerunServiceTest' --tests 'com.agilerunner.api.service.agentruntime.AgentRuntimeServiceTest' --tests 'com.agilerunner.client.agentruntime.AgentRuntimeRepositoryTest' --tests 'com.agilerunner.api.controller.GitHubWebhookControllerTest' --console=plain"
  - "full: ./scripts/gradlew-java21.sh --no-daemon cleanTest test --console=plain"
  - "actual app: POST /reviews/rerun -> 200 / executionKey=EXECUTION:MANUAL_RERUN:8dbdf886-cdb7-492d-b273-612210f8acdb"
diff_ref: "git diff -- .agents/active/tasks.md src/main/java/com/agilerunner/api/service/review src/main/java/com/agilerunner/api/service/agentruntime src/main/java/com/agilerunner/domain/agentruntime src/main/java/com/agilerunner/client/agentruntime src/main/resources/agent-runtime src/test/java/com/agilerunner/api/service/review src/test/java/com/agilerunner/api/service/agentruntime src/test/java/com/agilerunner/client/agentruntime src/test/java/com/agilerunner/api/controller/review"
failure_summary: "초기 구현은 manual rerun runtime execution을 H2에 남기더라도 local 설정 누락 시 controller가 500을 반환해 response executionKey 기반 representative verification을 닫지 못했다."
root_cause: "수동 재실행 경로에서 runtime evidence는 먼저 시작했지만, review/comment 단계 예외를 그대로 다시 던져 response executionKey와 actual-app representative verification의 연결이 끊어져 있었다."
agents_check_findings:
  - "ExecutionStartType를 도입해 webhook과 manual rerun을 runtime evidence에서 구분했다."
  - "ManualRerunService는 AgentRuntimeService.startManualRerunExecution(...)에서 받은 executionKey를 응답으로 그대로 반환하도록 정리했다."
  - "local 설정 누락으로 review-generated는 FAILED였지만 response executionKey 기준으로 WEBHOOK_EXECUTION과 AGENT_EXECUTION_LOG evidence를 조회할 수 있게 됐다."
next_task_warnings:
  - "수동 재실행 응답은 현재 내부/관리자용 기반 진입점이므로, 실패 이유를 외부에 노출할지 여부는 후속 spec에서 별도 결정해야 한다."
  - "manual rerun representative verification은 실제 응답 executionKey와 H2 evidence 두 축을 항상 같이 남겨야 한다."
  - "local H2 조회는 앱 종료 후 수행하고, lock 발생 시 짧은 대기 후 재조회한다."
error_signature: "response executionKey unavailable during manual rerun failure, GITHUB_APP_CONFIGURATION_MISSING, H2 file lock timing after app shutdown"
test_result_summary: "targeted test와 전체 test는 모두 통과했다. local profile actual app에서 /reviews/rerun은 200과 executionKey를 반환했고, 같은 executionKey 기준으로 WEBHOOK_EXECUTION과 AGENT_EXECUTION_LOG에 MANUAL_RERUN / DRY_RUN evidence가 적재된 것을 확인했다."
snapshot_hashes: []
---

# 작업 회고

## 작업 요약
- `ExecutionStartType`를 추가해 runtime evidence가 webhook 시작과 manual rerun 시작을 구분하도록 만들었다.
- `ManualRerunService`는 runtime execution을 먼저 시작하고, 같은 executionKey를 응답으로 그대로 반환하도록 정리했다.
- local profile actual app에서 대표 manual rerun 1건을 실행한 뒤, 응답 executionKey 기준으로 H2 evidence를 직접 확인했다.

## 실패 요약
- 첫 구현에서는 local GitHub 설정 누락으로 manual rerun이 500을 반환해 대표 검증에서 response executionKey를 확보하지 못했다.
- 앱 종료 직후 H2 Shell 조회도 file lock 타이밍 때문에 한 번 실패했다.

## Root Cause
- runtime evidence 저장과 HTTP 응답 계약을 분리해 생각하면서, 실패 시에도 response executionKey가 남아야 representative verification이 닫힌다는 점을 충분히 반영하지 못했다.
- 실제 앱/H2 representative verification 절차를 따르더라도 앱 종료 직후 H2 lock 타이밍이 잠깐 남을 수 있다는 점을 체감적으로 과소평가했다.

## AGENTS 체크 결과
- `ManualRerunServiceTest`, `AgentRuntimeServiceTest`, `AgentRuntimeRepositoryTest`로 response executionKey, execution start type, repository round-trip을 먼저 고정했다.
- `ManualRerunService`는 controller/service 경계에서 orchestration만 담당하고, runtime evidence 저장은 `AgentRuntimeService`가 맡도록 책임을 유지했다.
- `schema.sql`과 `AgentRuntimeRepository`는 `execution_start_type`를 최소 범위로 추가해 기존 webhook 경로 회귀를 크게 건드리지 않았다.

## 근거 Artifact
- `src/main/java/com/agilerunner/api/service/review/ManualRerunService.java`
- `src/main/java/com/agilerunner/api/service/agentruntime/AgentRuntimeService.java`
- `src/main/java/com/agilerunner/domain/agentruntime/ExecutionStartType.java`
- `src/main/java/com/agilerunner/domain/agentruntime/WebhookExecution.java`
- `src/main/java/com/agilerunner/domain/agentruntime/AgentExecutionLog.java`
- `src/main/java/com/agilerunner/client/agentruntime/AgentRuntimeRepository.java`
- `src/main/resources/agent-runtime/schema.sql`
- `src/test/java/com/agilerunner/api/service/review/ManualRerunServiceTest.java`
- `src/test/java/com/agilerunner/api/service/agentruntime/AgentRuntimeServiceTest.java`
- `src/test/java/com/agilerunner/client/agentruntime/AgentRuntimeRepositoryTest.java`

## 다음 Task 경고사항
- manual rerun은 현재 실패해도 executionKey를 반환하는 내부/관리자용 진입점으로 동작하므로, 후속 spec에서 관리자 응답 모델과 실패 노출 정책을 명확히 정리해야 한다.
- representative verification은 actual app 응답값과 H2 evidence를 같은 executionKey로 묶어 기록해야 한다.
- 앱 종료 직후 H2 조회가 잠깐 잠길 수 있으므로 workflow 규칙대로 lock 여부를 먼저 확인해야 한다.

## 제안 필요 여부
- 없음
- 이번 교훈은 새 AGENTS/workflow 제안이 아니라, 이미 accepted 된 representative verification 규칙을 manual rerun 경로에 맞게 적용해 닫은 사례에 가깝다.
