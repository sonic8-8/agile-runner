---
spec_id: SPEC-0019
task_id: TASK-0004
github_issue_number: 75
criteria_keys:
  - manual-rerun-list-history-composition-runtime-evidence-aligned
delivery_ids:
  - MANUAL_RERUN_DELIVERY:bef2a6be-0ba7-4f0f-863c-ec946031cfd3
execution_keys:
  - EXECUTION:MANUAL_RERUN:bef2a6be-0ba7-4f0f-863c-ec946031cfd3
test_evidence_ref:
  - "targeted: ./scripts/gradlew-java21.sh --no-daemon test --tests 'com.agilerunner.api.controller.review.ManualRerunControllerTest' --tests 'com.agilerunner.api.service.review.ManualRerunExecutionListServiceTest' --tests 'com.agilerunner.api.service.review.ManualRerunControlActionHistoryServiceTest' --tests 'com.agilerunner.client.agentruntime.AgentRuntimeRepositoryTest' --console=plain"
  - "full: ./scripts/gradlew-java21.sh --no-daemon cleanTest test --console=plain"
  - "actual-app: POST /reviews/rerun -> POST /reviews/rerun/{executionKey}/actions (ACKNOWLEDGE, UNACKNOWLEDGE) -> GET /reviews/rerun/executions?repositoryName=owner/repo&pullRequestNumber=920&executionStartType=MANUAL_RERUN&executionStatus=FAILED -> GET /reviews/rerun/{executionKey}/actions/history -> 앱 종료 -> H2 조회"
diff_ref: "git diff -- .agents/active/tasks.md .agents/outer-loop/retrospectives/SPEC-0019/TASK-0004-list-history-composition-runtime-evidence.md .agents/outer-loop/retrospectives/SPEC-0019/SPEC-0019-summary.md .agents/outer-loop/registry.json"
failure_summary: "제품 기능 실패는 없었다. 이번 task의 핵심은 list 최신 action 요약, history currentActionState, H2 최신 applied audit row가 같은 execution 기준으로 실제로 일치하는지 representative actual app/H2 검증으로 닫는 것이었다."
root_cause: "이전 task에서 list/history 현재 조치 상태 요약 계산 seam은 닫혔지만, 운영자가 실제로 읽는 HTTP 응답과 H2 audit latest row가 같은 execution 기준으로 같은 의미를 가지는지는 representative 검증으로 한 번 더 확인해야 했다."
agents_check_findings:
  - "representative execution 하나에 ACKNOWLEDGE, UNACKNOWLEDGE 두 action을 순서대로 적용하고 같은 execution 기준으로 list/history/H2를 비교했다."
  - "TASK-0004 task packet에 `execution 전체 latest applied audit row 기준`과 `applied audit row가 존재하는 representative execution 기준`을 명시해 검증 기준을 잠갔다."
  - "targeted test와 full cleanTest test를 순차 실행했고, actual app/H2 검증은 `앱 기동 -> rerun/action/list/history -> 앱 종료 -> H2 조회` 순서를 지켰다."
next_task_warnings:
  - "다음 spec에서 query/list/history/action 응답 역할을 문서화할 때, list 최신 action 요약과 history currentActionState는 같은 execution 최신 applied audit row 기준이라는 점을 예시와 함께 고정해야 한다."
  - "representative actual app/H2 검증에서는 같은 execution 기준으로 비교할 응답과 audit row를 먼저 잠그고, 그 외 필터나 페이지 조건은 별도 task로 분리하는 편이 안전하다."
error_signature: "GITHUB_APP_CONFIGURATION_MISSING"
test_result_summary: "targeted test, full cleanTest test, representative actual app/H2 verification이 모두 통과했다. 같은 execution 기준으로 list 최신 action 요약과 history currentActionState의 latestAction/latestActionStatus/latestActionAppliedAt이 H2 최신 applied audit row와 일치했다."
snapshot_hashes: []
---

# 작업 회고

## 작업 요약
- representative manual rerun execution 하나를 만들고 `ACKNOWLEDGE`, `UNACKNOWLEDGE` action을 순서대로 적용했다.
- 같은 `executionKey` 기준으로 목록 최신 action 요약과 history `currentActionState`를 조회했다.
- 앱 종료 후 H2 file DB에서 `WEBHOOK_EXECUTION`과 `MANUAL_RERUN_CONTROL_ACTION_AUDIT`를 조회해 HTTP 응답 의미와 같은지 검증했다.

## 실패 요약
- 제품 기능 실패는 없었다.
- 이번 task의 핵심 위험은 list 최신 action 요약과 history 현재 조치 상태 요약이 실제 audit latest row와 다르게 읽힐 수 있다는 점이었다.

## Root Cause
- 이전 task들은 controller/service/repository seam을 닫는 데 집중했다.
- 하지만 운영자가 실제로 보는 것은 HTTP 응답과 H2 evidence의 조합이므로, 같은 execution 기준으로 list/history/H2를 한 번 더 맞춰 봐야 의미가 완전히 닫힌다.

## AGENTS 체크 결과
- `#75`를 `TASK-0004`와 1:1로 연결했다.
- targeted test와 full `cleanTest test`를 순차 실행했다.
- representative actual app/H2 검증은 새 `delivery_id`, `execution_key`로 수행했고, 앱 종료 후 H2 조회 순서를 지켰다.
- representative 비교 기준은 history filter/page 결과가 아니라 execution 전체 latest applied audit row로 고정했다.

## 근거 Artifact
- representative `executionKey`: `EXECUTION:MANUAL_RERUN:bef2a6be-0ba7-4f0f-863c-ec946031cfd3`
- representative `delivery_id`: `MANUAL_RERUN_DELIVERY:bef2a6be-0ba7-4f0f-863c-ec946031cfd3`
- rerun 응답:
  - `FAILED / GITHUB_APP_CONFIGURATION_MISSING`
- action 응답:
  - `ACKNOWLEDGE / APPLIED / availableActions=[UNACKNOWLEDGE] / note=운영자 확인 1`
  - `UNACKNOWLEDGE / APPLIED / availableActions=[ACKNOWLEDGE] / note=운영자 확인 취소 1`
- list row:
  - `latestAction=UNACKNOWLEDGE`
  - `latestActionStatus=APPLIED`
  - `latestActionAppliedAt=2026-04-12T01:11:19.653331`
  - `historyAvailable=true`
  - `availableActions=[ACKNOWLEDGE]`
- history `currentActionState`:
  - `latestAction=UNACKNOWLEDGE`
  - `latestActionStatus=APPLIED`
  - `latestActionAppliedAt=2026-04-12T01:11:19.653331`
  - `availableActions=[ACKNOWLEDGE]`
- history `actions[]`:
  - `ACKNOWLEDGE / APPLIED / 2026-04-12T01:11:19.633302`
  - `UNACKNOWLEDGE / APPLIED / 2026-04-12T01:11:19.653331`
- H2 `WEBHOOK_EXECUTION`:
  - `MANUAL_RERUN / FAILED / GITHUB_APP_CONFIGURATION_MISSING / MANUAL_ACTION_REQUIRED / DRY_RUN / FALSE`
- H2 `MANUAL_RERUN_CONTROL_ACTION_AUDIT` 최신 applied row:
  - `UNACKNOWLEDGE / APPLIED / 운영자 확인 취소 1 / 2026-04-12 01:11:19.653331`

## 다음 Task 경고사항
- 다음 spec이 조회 응답 문서화라면 list 최신 요약과 history 현재 조치 상태 요약의 차이를 응답 예시로 명확히 남겨야 한다.
- representative 검증에서 same execution 기준이 흐려지면 응답 의미 검증 자체가 흔들리므로, 비교 대상 row와 응답 필드를 먼저 잠그는 방식이 안전하다.

## 제안 필요 여부
- 없음
