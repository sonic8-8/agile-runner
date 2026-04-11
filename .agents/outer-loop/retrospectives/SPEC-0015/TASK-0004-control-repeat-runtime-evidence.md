---
spec_id: SPEC-0015
task_id: TASK-0004
github_issue_number: 59
criteria_keys:
  - manual-rerun-control-repeat-runtime-evidence-aligned
delivery_ids:
  - MANUAL_RERUN_DELIVERY:db4ace65-8950-4270-99b1-101f530c3380
execution_keys:
  - EXECUTION:MANUAL_RERUN:db4ace65-8950-4270-99b1-101f530c3380
test_evidence_ref:
  - "targeted: ./scripts/gradlew-java21.sh --no-daemon test --tests 'com.agilerunner.api.controller.review.ManualRerunControllerTest' --tests 'com.agilerunner.api.service.review.ManualRerunControlActionServiceTest' --tests 'com.agilerunner.api.service.review.ManualRerunControlActionHistoryServiceTest' --tests 'com.agilerunner.api.service.review.ManualRerunQueryServiceTest' --tests 'com.agilerunner.api.service.review.ManualRerunExecutionListServiceTest' --tests 'com.agilerunner.client.agentruntime.AgentRuntimeRepositoryTest' --console=plain"
  - "full: ./scripts/gradlew-java21.sh --no-daemon cleanTest test --console=plain"
  - "actual: POST /reviews/rerun -> POST /reviews/rerun/{executionKey}/actions (ACKNOWLEDGE) -> POST /reviews/rerun/{executionKey}/actions (UNACKNOWLEDGE) -> POST /reviews/rerun/{executionKey}/actions (ACKNOWLEDGE) -> GET /reviews/rerun/{executionKey}/actions/history -> GET /reviews/rerun/{executionKey} -> GET /reviews/rerun/executions"
  - "H2 query: java -cp ~/.gradle/.../h2-2.3.232.jar org.h2.tools.Shell -url jdbc:h2:file:/home/seaung13/.agile-runner/agent-runtime/agile-runner"
diff_ref: "git diff -- .agents/outer-loop/retrospectives/SPEC-0015/TASK-0004-control-repeat-runtime-evidence.md .agents/outer-loop/retrospectives/SPEC-0015/SPEC-0015-summary.md .agents/outer-loop/registry.json"
failure_summary: "반복 액션 정책은 테스트로는 닫혔지만, 실제 앱과 H2 file DB 기준으로 action 응답, query/list, history, audit evidence가 같은 의미를 가지는지 마지막 확인이 필요했다."
root_cause: "반복 액션은 저장 규칙, 현재 상태 요약, 전체 timeline이 함께 맞아야 의미가 완성된다. representative execution에서 세 번의 액션 전환을 실제로 수행해 보지 않으면 이 의미가 런타임에서 어긋나는지 확신할 수 없다."
agents_check_findings:
  - "actual representative 검증은 `앱 기동 -> 새 execution 준비 -> ACKNOWLEDGE -> UNACKNOWLEDGE -> ACKNOWLEDGE -> history/query/list 조회 -> 앱 종료 -> H2 조회` 순서로 수행했다."
  - "current schema.sql을 local H2 file DB에 먼저 적용한 뒤 representative execution을 생성했다."
next_task_warnings:
  - "다음 spec이 action history 필터 또는 관리 UI 후보를 다루더라도, current state(query/list)와 full timeline(history) 역할 분리를 유지해야 한다."
  - "representative 검증에서는 executionKey 하나를 기준으로 action 응답, query/list, history, H2 audit row를 함께 대조하는 패턴을 유지하는 편이 안전하다."
error_signature: "NONE"
test_result_summary: "targeted test, full cleanTest test, actual app/H2 representative verification이 모두 통과했고, ACKNOWLEDGE -> UNACKNOWLEDGE -> ACKNOWLEDGE 흐름이 응답/조회/H2 evidence에서 같은 execution 기준으로 일치했다."
snapshot_hashes: []
---

# 작업 회고

## 작업 요약
- representative execution 1건을 실제로 만들고 `ACKNOWLEDGE -> UNACKNOWLEDGE -> ACKNOWLEDGE` 흐름을 수행했다.
- action 응답, query 응답, 목록 응답, history 응답, H2 `MANUAL_RERUN_CONTROL_ACTION_AUDIT` row 세 건이 같은 `executionKey` 기준으로 일치하는지 확인했다.
- 마지막 applied action이 `ACKNOWLEDGE`일 때 query/list의 `availableActions`가 `UNACKNOWLEDGE`만 남는다는 점도 actual app/H2 기준으로 확인했다.

## 실패 요약
- 기능 실패는 없었고, 핵심 위험은 반복 액션 저장 허용 이후 실제 앱/H2에서 응답 의미와 audit timeline이 어긋날 수 있다는 점이었다.

## Root Cause
- 반복 액션 정책은 단순히 저장만 되면 끝나는 문제가 아니라, 현재 상태 요약과 과거 timeline이 함께 맞아야 운영자가 혼동하지 않는다.
- 그래서 representative actual app/H2 검증으로 응답과 audit evidence를 같은 execution 기준으로 한 번에 대조하는 것이 마지막 단계로 필요했다.

## AGENTS 체크 결과
- targeted test와 전체 테스트를 순차 실행했다.
- representative actual app/H2 검증에 fresh `delivery_id`, `execution_key`를 사용했다.
- 앱 종료 후 H2 file DB를 조회해 audit row 세 건을 확인했다.

## 근거 Artifact
- `.agents/outer-loop/retrospectives/SPEC-0015/TASK-0004-control-repeat-runtime-evidence.md`
- `src/main/resources/agent-runtime/schema.sql`
- `/tmp/spec0015-rerun.json`
- `/tmp/spec0015-action1.json`
- `/tmp/spec0015-action2.json`
- `/tmp/spec0015-action3.json`
- `/tmp/spec0015-query.json`
- `/tmp/spec0015-history.json`
- `/tmp/spec0015-list.json`

## 다음 Task 경고사항
- 없음

## 제안 필요 여부
- 없음
