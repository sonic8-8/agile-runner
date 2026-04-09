---
spec_id: SPEC-0012
task_id: TASK-0003
github_issue_number: 46
criteria_keys:
  - manual-rerun-control-response-maps-audit-state
delivery_ids: []
execution_keys: []
test_evidence_ref:
  - "targeted: ./scripts/gradlew-java21.sh --no-daemon test --tests 'com.agilerunner.api.service.review.ManualRerunControlActionServiceTest' --tests 'com.agilerunner.api.service.review.ManualRerunQueryServiceTest' --tests 'com.agilerunner.api.service.review.ManualRerunExecutionListServiceTest' --tests 'com.agilerunner.api.controller.review.ManualRerunControllerTest' --tests 'com.agilerunner.client.agentruntime.AgentRuntimeRepositoryTest' --console=plain"
  - "full: ./scripts/gradlew-java21.sh --no-daemon cleanTest test --console=plain"
diff_ref: "git diff -- src/main/java/com/agilerunner/api/controller/review/ManualRerunController.java src/main/java/com/agilerunner/api/controller/review/response/ManualRerunControlActionConflictResponse.java src/main/java/com/agilerunner/api/controller/review/response/ManualRerunControlActionNotFoundResponse.java src/main/java/com/agilerunner/api/controller/review/response/ManualRerunQueryResponse.java src/main/java/com/agilerunner/api/service/review/ManualRerunControlActionService.java src/main/java/com/agilerunner/api/service/review/ManualRerunExecutionListService.java src/main/java/com/agilerunner/api/service/review/ManualRerunQueryService.java src/main/java/com/agilerunner/api/service/review/response/ManualRerunQueryServiceResponse.java src/main/java/com/agilerunner/client/agentruntime/AgentRuntimeRepository.java src/main/java/com/agilerunner/domain/exception/ManualRerunControlActionConflictException.java src/main/java/com/agilerunner/domain/exception/ManualRerunControlActionNotFoundException.java src/main/java/com/agilerunner/domain/review/ManualRerunAvailableAction.java src/main/java/com/agilerunner/domain/review/ManualRerunAvailableActionPolicy.java src/main/java/com/agilerunner/domain/review/ManualRerunControlActionAudit.java src/main/java/com/agilerunner/domain/review/ManualRerunControlActionEligibility.java src/main/resources/agent-runtime/schema.sql src/test/java/com/agilerunner/api/controller/review/ManualRerunControllerTest.java src/test/java/com/agilerunner/api/service/review/ManualRerunControlActionServiceTest.java src/test/java/com/agilerunner/api/service/review/ManualRerunExecutionListServiceTest.java src/test/java/com/agilerunner/api/service/review/ManualRerunQueryServiceTest.java src/test/java/com/agilerunner/client/agentruntime/AgentRuntimeRepositoryTest.java"
failure_summary: "agent-runtime schema와 repository seam이 추가된 task인데 representative actual app/H2 검증은 TASK-0004로 미뤄져, 종료 검증 규칙과 task 경계 사이에 해석 여지가 생겼다."
root_cause: "현재 AGENTS workflow는 agent-runtime 저장 변경 시 representative actual app/H2 검증을 요구하지만, SPEC-0012의 task 분해는 policy와 audit state 연결을 TASK-0003에서 닫고 representative verification을 TASK-0004에 배치했다. 이 우선순위가 명시적으로 적혀 있지 않았다."
agents_check_findings:
  - "3개 서브에이전트 리뷰 결과, ACKNOWLEDGE 정책, audit evidence 저장, query/list availableActions 재계산, controller 404/409 매핑까지는 TASK-0003 범위로 적절하다고 확인됐다."
  - "query/list가 action detail을 직접 노출하지 않고 availableActions만 갱신하는 경계를 유지했다."
  - "대표 actual app/H2 검증을 아직 끌어오지 않은 점은 spec task 경계를 지킨 것으로 판단됐다."
next_task_warnings:
  - "TASK-0004는 MANUAL_ACTION_REQUIRED 이고 ACKNOWLEDGE 가능한 representative execution을 준비한 뒤 action 응답, query/list 결과, H2 audit evidence를 같은 executionKey 기준으로 확인해야 한다."
  - "TASK-0004에서 schema seed 또는 local H2 migration이 필요하면 기존 representative verification 규칙을 먼저 적용해야 한다."
error_signature: "Representative verification sequencing ambiguity between AGENTS runtime-storage rule and spec task boundary"
test_result_summary: "ACKNOWLEDGE 정책, action 404/409 응답, query/list availableActions 반영, audit repository seam을 포함한 targeted test와 full cleanTest test가 모두 통과했다. representative actual app/H2 검증은 spec task 경계에 따라 TASK-0004로 남겼다."
snapshot_hashes: []
---

# 작업 회고

## 작업 요약
- `ACKNOWLEDGE` 허용 조건을 reusable policy로 고정했다.
- control action audit evidence 저장 seam을 `agent-runtime` repository와 schema에 추가했다.
- action 응답, 단건 조회, 목록 조회가 audit state를 반영한 `availableActions`를 같은 규칙으로 계산하도록 연결했다.
- action endpoint의 not-found/conflict 응답 정책을 controller 경계에 고정했다.

## 실패 요약
- 구현 실패는 없었지만, `agent-runtime` schema/repository seam이 추가된 task를 representative actual app/H2 검증 없이 닫을 수 있는지 workflow 해석이 애매했다.

## Root Cause
- 현재 workflow는 `agent-runtime` 저장 변경 시 representative actual app/H2 검증을 강하게 요구한다.
- 반면 `SPEC-0012`의 task 분해는 `TASK-0003`에서 policy/audit/query-list 연결을 닫고, representative verification은 `TASK-0004`로 분리했다.
- 어떤 경우에 spec의 task boundary를 우선하고, 어떤 경우에 runtime-storage rule을 즉시 적용하는지 명시가 부족했다.

## AGENTS 체크 결과
- Orchestrator는 `TASK-0003` 범위를 `ACKNOWLEDGE` 정책, audit evidence, query/list availableActions 재계산으로 유지했고 representative verification을 끌어오지 않았다.
- Tester는 controller/service/repository black-box 테스트로 success, conflict, not-found, query/list 반영을 고정했다.
- Constructor는 query/list에 action detail을 직접 노출하지 않고 `availableActions`만 갱신하는 경계를 지켰다.
- 3개 서브에이전트 리뷰는 최종 모두 `PASS`였다.

## 근거 Artifact
- `.agents/active/spec.md`
- `.agents/active/tasks.md`
- `.agents/criteria/SPEC-0012-admin-control-action-extension.json`
- `src/main/java/com/agilerunner/api/controller/review/ManualRerunController.java`
- `src/main/java/com/agilerunner/api/service/review/ManualRerunControlActionService.java`
- `src/main/java/com/agilerunner/api/service/review/ManualRerunExecutionListService.java`
- `src/main/java/com/agilerunner/api/service/review/ManualRerunQueryService.java`
- `src/main/java/com/agilerunner/client/agentruntime/AgentRuntimeRepository.java`
- `src/main/resources/agent-runtime/schema.sql`
- `src/test/java/com/agilerunner/api/controller/review/ManualRerunControllerTest.java`
- `src/test/java/com/agilerunner/api/service/review/ManualRerunControlActionServiceTest.java`
- `src/test/java/com/agilerunner/api/service/review/ManualRerunExecutionListServiceTest.java`
- `src/test/java/com/agilerunner/api/service/review/ManualRerunQueryServiceTest.java`
- `src/test/java/com/agilerunner/client/agentruntime/AgentRuntimeRepositoryTest.java`

## 다음 Task 경고사항
- `TASK-0004`는 representative execution 준비, action 요청, query/list 확인, 앱 종료 후 H2 evidence 조회를 같은 executionKey 기준으로 묶어야 한다.
- 이번 task에서 추가한 audit table과 availableActions policy가 actual app/H2 representative 검증에서도 같은 의미를 유지하는지 확인해야 한다.

## 제안 필요 여부
- 있음
- `agent-runtime` schema/repository seam을 추가하는 task가 spec에서 representative verification과 분리돼 있을 때, repository/H2 proof로 TASK-0003를 닫고 representative actual app/H2 검증은 TASK-0004로 넘길 수 있는지 workflow 규칙을 명시할 필요가 있다.
