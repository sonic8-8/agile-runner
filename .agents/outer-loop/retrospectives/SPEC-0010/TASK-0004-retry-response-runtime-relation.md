---
spec_id: SPEC-0010
task_id: TASK-0004
github_issue_number: 39
criteria_keys:
  - manual-rerun-retry-response-links-source-execution
delivery_ids:
  - MANUAL_RERUN_DELIVERY:retry-source-20260409-155001
  - MANUAL_RERUN_DELIVERY:5cee3f59-a5e6-44fb-8add-86ceaedc593c
execution_keys:
  - EXECUTION:MANUAL_RERUN:retry-source-20260409-155001
  - EXECUTION:MANUAL_RERUN:5cee3f59-a5e6-44fb-8add-86ceaedc593c
test_evidence_ref:
  - "targeted: ./scripts/gradlew-java21.sh --no-daemon test --tests 'com.agilerunner.api.controller.review.ManualRerunControllerTest' --tests 'com.agilerunner.api.service.review.ManualRerunRetryServiceTest' --tests 'com.agilerunner.api.service.agentruntime.AgentRuntimeServiceTest' --tests 'com.agilerunner.client.agentruntime.AgentRuntimeRepositoryTest' --console=plain"
  - "full: ./scripts/gradlew-java21.sh --no-daemon cleanTest test --console=plain"
  - "actual app/H2: H2 schema.sql 적용 -> retryable source execution seed -> bootRun(local, port=18080) -> POST /reviews/rerun/{executionKey}/retry -> HTTP 200 + executionKey -> app shutdown -> H2 Shell query by executionKey"
diff_ref: "git diff -- src/main/java/com/agilerunner/api/service/review/request/ManualRerunServiceRequest.java src/main/java/com/agilerunner/api/service/review/ManualRerunRetryService.java src/main/java/com/agilerunner/api/service/agentruntime/AgentRuntimeService.java src/main/java/com/agilerunner/client/agentruntime/AgentRuntimeRepository.java src/main/java/com/agilerunner/domain/agentruntime/WebhookExecution.java src/main/java/com/agilerunner/domain/agentruntime/AgentExecutionLog.java src/main/resources/agent-runtime/schema.sql src/test/java/com/agilerunner/api/service/agentruntime/AgentRuntimeServiceTest.java src/test/java/com/agilerunner/client/agentruntime/AgentRuntimeRepositoryTest.java src/test/java/com/agilerunner/api/service/review/ManualRerunRetryServiceTest.java .agents/outer-loop/retrospectives/SPEC-0010/TASK-0004-retry-response-runtime-relation.md .agents/outer-loop/proposals/WORKFLOW/WORKFLOW-PROP-0011-from-SPEC-0010-TASK-0004.md .agents/outer-loop/registry.json"
failure_summary: "실제 representative 검증 준비 중 local H2 file DB에 새 runtime relation 컬럼이 아직 반영되지 않아 source execution seed SQL이 한 번 실패했다."
root_cause: "이번 task가 agent-runtime schema 자체를 바꿨는데, local H2 file DB는 app start 전까지 새 schema.sql이 자동 반영되지 않았다. representative retry 검증은 synthetic retryable source execution을 먼저 seed해야 해서 stale schema에 바로 INSERT를 시도하면 false negative가 난다."
agents_check_findings:
  - "Tester 1차는 runtime relation 저장 경계만 잠그고 representative actual app/H2 검증은 마지막 단계로 남겼다."
  - "실제 검증은 retryable source execution을 seed해야 해서 기본 representative 검증 순서 앞에 schema 적용과 source 준비 단계가 추가됐다."
  - "source relation 필드는 WebhookExecution, AgentExecutionLog, repository/schema round-trip, actual app response/H2 evidence까지 같은 의미로 닫혔다."
next_task_warnings:
  - "다음 spec에서 local H2 file DB에 synthetic source row를 미리 넣어야 하는 representative 검증이 나오면, runtime schema 변경 여부를 먼저 확인해야 한다."
  - "response와 runtime evidence를 execution key 하나로 연결하는 task는 source relation뿐 아니라 accepted step log까지 같은 key로 남는지 함께 보는 편이 안전하다."
error_signature: "JdbcSQLSyntaxErrorException: Column \"RETRY_SOURCE_EXECUTION_KEY\" not found"
test_result_summary: "runtime relation targeted test, full cleanTest test, local actual app/H2 representative retry verification이 모두 기대 결과로 닫혔다."
snapshot_hashes: []
---

# 작업 회고

## 작업 요약
- `retrySourceExecutionKey`를 manual rerun retry 응답에서만 유지하던 상태에서, runtime evidence인 `WEBHOOK_EXECUTION`과 `AGENT_EXECUTION_LOG`까지 같은 의미로 남기도록 정리했다.
- `ManualRerunRetryService`가 source execution key를 `ManualRerunServiceRequest`로 넘기고, `AgentRuntimeService`가 manual rerun accepted/review-generated/comment-posted/failure log 전 구간에서 같은 relation을 유지하도록 맞췄다.
- local profile 실제 앱에서 retryable source execution 1건을 seed한 뒤 `POST /reviews/rerun/{executionKey}/retry`를 실행하고, 응답의 `executionKey`와 `retrySourceExecutionKey`가 H2 evidence와 같은지 확인했다.

## 실패 요약
- representative 검증 준비 중 첫 source execution seed SQL이 local H2 file DB의 stale schema 때문에 실패했다.
- 오류는 `JdbcSQLSyntaxErrorException: Column "RETRY_SOURCE_EXECUTION_KEY" not found`였다.

## Root Cause
- 이번 task는 `WEBHOOK_EXECUTION`, `AGENT_EXECUTION_LOG` 물리 스키마에 새 컬럼을 추가했지만, local H2 file DB는 app start 전까지 새 schema.sql이 반영되지 않았다.
- representative retry 검증은 `RETRYABLE` source execution이 필요해서 synthetic source row를 먼저 seed해야 했고, stale schema 상태에서 바로 INSERT를 시도하면서 false negative가 발생했다.

## AGENTS 체크 결과
- task 범위는 runtime relation 저장과 representative verification으로 유지했고, retry endpoint 계약 자체는 다시 넓히지 않았다.
- targeted test와 full test는 순차 실행으로 확인했다.
- representative 검증은 기본 순서에서 source seed 단계가 추가됐고, 그 이유를 회고에 남겼다.

## 근거 Artifact
- `src/main/java/com/agilerunner/api/service/review/request/ManualRerunServiceRequest.java`
- `src/main/java/com/agilerunner/api/service/review/ManualRerunRetryService.java`
- `src/main/java/com/agilerunner/api/service/agentruntime/AgentRuntimeService.java`
- `src/main/java/com/agilerunner/client/agentruntime/AgentRuntimeRepository.java`
- `src/main/java/com/agilerunner/domain/agentruntime/WebhookExecution.java`
- `src/main/java/com/agilerunner/domain/agentruntime/AgentExecutionLog.java`
- `src/main/resources/agent-runtime/schema.sql`
- `src/test/java/com/agilerunner/api/service/agentruntime/AgentRuntimeServiceTest.java`
- `src/test/java/com/agilerunner/client/agentruntime/AgentRuntimeRepositoryTest.java`
- `src/test/java/com/agilerunner/api/service/review/ManualRerunRetryServiceTest.java`

## 다음 Task 경고사항
- local H2 representative verification이 synthetic source execution seed를 필요로 하면, task가 runtime schema를 바꾸는지 먼저 확인해야 한다.
- response와 runtime evidence를 연결하는 task는 응답 필드만 맞추지 말고 accepted/failure step log까지 같은 relation을 유지하는지 같이 봐야 한다.

## 제안 필요 여부
- 있음
- actual app/H2 representative verification에서 synthetic source execution seed가 필요한 task가 runtime schema를 같이 바꾸는 경우, schema.sql 선반영 절차를 workflow에 명시하는 편이 안전하다.
