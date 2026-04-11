---
spec_id: SPEC-0018
task_id: TASK-0004
github_issue_number: 71
criteria_keys:
  - manual-rerun-control-history-order-page-runtime-evidence-aligned
delivery_ids:
  - MANUAL_RERUN_DELIVERY:d94d27f8-3c25-4037-857b-b8cdfca9eac3
execution_keys:
  - EXECUTION:MANUAL_RERUN:d94d27f8-3c25-4037-857b-b8cdfca9eac3
test_evidence_ref:
  - "targeted: ./scripts/gradlew-java21.sh --no-daemon test --tests 'com.agilerunner.api.controller.review.ManualRerunControllerTest' --tests 'com.agilerunner.api.service.review.ManualRerunControlActionHistoryServiceTest' --tests 'com.agilerunner.client.agentruntime.AgentRuntimeRepositoryTest' --console=plain"
  - "full: ./scripts/gradlew-java21.sh --no-daemon cleanTest test --console=plain"
  - "actual-app: POST /reviews/rerun -> GET /reviews/rerun/{executionKey} -> POST /reviews/rerun/{executionKey}/actions (ACKNOWLEDGE, UNACKNOWLEDGE, ACKNOWLEDGE, UNACKNOWLEDGE) -> GET /reviews/rerun/{executionKey}/actions/history -> GET /reviews/rerun/{executionKey}/actions/history?sortDirection=ASC -> GET /reviews/rerun/{executionKey}/actions/history?sortDirection=DESC&pageSize=2 -> GET /reviews/rerun/{executionKey}/actions/history?pageSize=2 -> GET /reviews/rerun/{executionKey}/actions/history?sortDirection=DESC&pageSize=2&cursorAppliedAt=2026-04-11T23:00:15.2341 -> GET /reviews/rerun/{executionKey}/actions/history?sortDirection=DESC&pageSize=2&cursorAppliedAt=2026-04-11T23:00:13.152566 -> 앱 종료 -> H2 조회"
diff_ref: "git diff -- .agents/outer-loop/retrospectives/SPEC-0018/TASK-0004-history-order-page-runtime-evidence.md .agents/outer-loop/retrospectives/SPEC-0018/SPEC-0018-summary.md .agents/outer-loop/proposals/WORKFLOW/WORKFLOW-PROP-0013-from-SPEC-0018-TASK-0004.md .agents/outer-loop/registry.json"
failure_summary: "기능 실패는 없었지만, 같은 H2 file DB에 대해 Shell 조회를 병렬로 실행하면 file lock으로 false negative가 발생할 수 있다는 점이 representative verification 중 드러났다."
root_cause: "history 정렬과 페이지 기준은 HTTP 응답 검증만으로 끝나지 않고, 같은 execution 기준으로 H2 audit timeline과 subset 의미가 정확히 맞는지 확인해야 한다. 이때 H2 file DB는 Shell 조회를 병렬로 열면 lock 충돌이 날 수 있어 순차 조회가 필요하다."
agents_check_findings:
  - "representative execution 하나에 ACKNOWLEDGE -> UNACKNOWLEDGE -> ACKNOWLEDGE -> UNACKNOWLEDGE 순서의 action timeline 4건을 서로 다른 appliedAt으로 생성했다."
  - "무필터 전체 조회, explicit ASC 조회, explicit DESC page 조회, sortDirection 없는 기본 DESC 조회, cursor 다음 page 조회, 빈 window 조회를 같은 execution 기준으로 확인했다."
  - "앱 종료 후 WEBHOOK_EXECUTION과 MANUAL_RERUN_CONTROL_ACTION_AUDIT를 H2에서 조회해 HTTP 응답 subset과 같은 의미인지 확인했다."
  - "H2 Shell 병렬 조회에서 file lock이 발생해 lock 여부를 먼저 확인했고, 이후 순차 조회로 같은 evidence를 다시 확인했다."
next_task_warnings:
  - "다음 spec에서 history 조회에 새 cursor 또는 page 기준을 추가하면 representative execution 기준으로 기본 정렬 방향과 빈 window 의미를 다시 확인해야 한다."
  - "같은 H2 file DB를 Shell 또는 SQL 도구로 확인할 때는 병렬 조회보다 순차 조회를 기본으로 둬야 한다."
error_signature: "H2_FILE_LOCK_ON_PARALLEL_SHELL_QUERY"
test_result_summary: "targeted test, full cleanTest test, representative actual app/H2 verification이 모두 통과했고, full ASC timeline, explicit DESC page, 기본 DESC page, cursor 다음 page, 빈 window 응답이 같은 execution 기준으로 H2 audit evidence와 일치했다."
snapshot_hashes: []
---

# 작업 회고

## 작업 요약
- representative manual rerun execution 하나를 만들고 같은 execution에 관리자 액션 4건을 순서대로 적용했다.
- 같은 execution 기준으로 무필터 전체 history, explicit ASC history, explicit DESC page, sortDirection 없는 기본 DESC page, cursor 다음 page, 빈 window를 모두 조회했다.
- 앱 종료 후 H2 file DB에서 `WEBHOOK_EXECUTION`과 `MANUAL_RERUN_CONTROL_ACTION_AUDIT`를 조회해 HTTP 응답 의미와 같은지 확인했다.

## 실패 요약
- 제품 기능 실패는 없었다.
- representative verification 중 같은 H2 file DB에 대한 Shell 조회를 병렬로 띄우면 file lock으로 false negative가 날 수 있다는 점이 드러났다.

## Root Cause
- history 정렬과 page window는 service/repository seam만 닫아도 충분하지 않고, 운영자가 실제로 읽는 HTTP 응답 subset과 H2 audit subset이 같은 execution 기준으로 일치해야 의미가 닫힌다.
- H2 file DB는 CLI/Shell 조회를 동시에 열면 lock 충돌이 날 수 있으므로 대표 검증의 evidence 조회도 순차 실행이 안전하다.

## AGENTS 체크 결과
- `#71`을 `TASK-0004`와 1:1로 연결했다.
- targeted test와 full `cleanTest test`를 순차 실행했다.
- 실제 앱/H2 representative 검증은 `앱 기동 -> 실행/액션 수행 -> history 조회 -> 앱 종료 -> H2 조회` 순서를 지켰다.
- representative execution에는 새 `delivery_id`, `execution_key`를 사용했다.

## 근거 Artifact
- representative `executionKey`: `EXECUTION:MANUAL_RERUN:d94d27f8-3c25-4037-857b-b8cdfca9eac3`
- representative `delivery_id`: `MANUAL_RERUN_DELIVERY:d94d27f8-3c25-4037-857b-b8cdfca9eac3`
- rerun query 응답:
  - `FAILED / GITHUB_APP_CONFIGURATION_MISSING / MANUAL_ACTION_REQUIRED / availableActions=[ACKNOWLEDGE]`
- action 응답:
  - `ACKNOWLEDGE / APPLIED / availableActions=[UNACKNOWLEDGE] / note=운영자 확인 1`
  - `UNACKNOWLEDGE / APPLIED / availableActions=[ACKNOWLEDGE] / note=운영자 확인 취소 1`
  - `ACKNOWLEDGE / APPLIED / availableActions=[UNACKNOWLEDGE] / note=운영자 확인 2`
  - `UNACKNOWLEDGE / APPLIED / availableActions=[ACKNOWLEDGE] / note=운영자 확인 취소 2`
- history 무필터 전체 응답:
  - `ACKNOWLEDGE(2026-04-11T23:00:13.152566)`
  - `UNACKNOWLEDGE(2026-04-11T23:00:14.193285)`
  - `ACKNOWLEDGE(2026-04-11T23:00:15.2341)`
  - `UNACKNOWLEDGE(2026-04-11T23:00:16.268825)`
- history explicit DESC page 응답:
  - `pageSize=2`
  - `UNACKNOWLEDGE(2026-04-11T23:00:16.268825)`
  - `ACKNOWLEDGE(2026-04-11T23:00:15.2341)`
- history 기본 DESC page 응답:
  - `pageSize=2`
  - explicit DESC page와 같은 두 row 확인
- history cursor 다음 page 응답:
  - `cursorAppliedAt=2026-04-11T23:00:15.2341`
  - `UNACKNOWLEDGE(2026-04-11T23:00:14.193285)`
  - `ACKNOWLEDGE(2026-04-11T23:00:13.152566)`
- history 빈 window 응답:
  - `cursorAppliedAt=2026-04-11T23:00:13.152566`
  - `actions=[]`
- H2 `WEBHOOK_EXECUTION`:
  - `FAILED / GITHUB_APP_CONFIGURATION_MISSING / MANUAL_ACTION_REQUIRED / MANUAL_RERUN / DRY_RUN / FALSE`
- H2 `MANUAL_RERUN_CONTROL_ACTION_AUDIT`:
  - `ACKNOWLEDGE / APPLIED / 운영자 확인 1 / 2026-04-11 23:00:13.152566`
  - `UNACKNOWLEDGE / APPLIED / 운영자 확인 취소 1 / 2026-04-11 23:00:14.193285`
  - `ACKNOWLEDGE / APPLIED / 운영자 확인 2 / 2026-04-11 23:00:15.2341`
  - `UNACKNOWLEDGE / APPLIED / 운영자 확인 취소 2 / 2026-04-11 23:00:16.268825`

## 다음 Task 경고사항
- 다음 spec에서 history 정렬/페이지 위에 새 cursor 규칙을 얹으면 기본 정렬 방향과 빈 window 의미를 representative execution 기준으로 다시 확인해야 한다.
- local H2 file DB는 같은 파일을 동시에 Shell 조회하지 말고 순차 조회를 기본으로 두는 편이 안전하다.

## 제안 필요 여부
- 있음
- `WORKFLOW-PROP-0013`
