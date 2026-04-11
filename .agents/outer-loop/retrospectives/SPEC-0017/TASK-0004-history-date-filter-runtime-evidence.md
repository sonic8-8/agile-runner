---
spec_id: SPEC-0017
task_id: TASK-0004
github_issue_number: 67
criteria_keys:
  - manual-rerun-control-history-date-filter-runtime-evidence-aligned
delivery_ids:
  - MANUAL_RERUN_DELIVERY:0414f9ba-50db-4675-a972-7fdcf25db09d
execution_keys:
  - EXECUTION:MANUAL_RERUN:0414f9ba-50db-4675-a972-7fdcf25db09d
test_evidence_ref:
  - "targeted: ./scripts/gradlew-java21.sh --no-daemon test --tests 'com.agilerunner.api.controller.review.ManualRerunControllerTest' --tests 'com.agilerunner.api.service.review.ManualRerunControlActionServiceTest' --tests 'com.agilerunner.api.service.review.ManualRerunControlActionHistoryServiceTest' --tests 'com.agilerunner.client.agentruntime.AgentRuntimeRepositoryTest' --console=plain"
  - "full: ./scripts/gradlew-java21.sh --no-daemon cleanTest test --console=plain"
  - "actual-app: POST /reviews/rerun -> POST /reviews/rerun/{executionKey}/actions (ACKNOWLEDGE, UNACKNOWLEDGE) -> GET /reviews/rerun/{executionKey}/actions/history -> GET /reviews/rerun/{executionKey}/actions/history?appliedAtFrom=... -> GET /reviews/rerun/{executionKey}/actions/history?appliedAtTo=... -> GET /reviews/rerun/{executionKey}/actions/history?appliedAtFrom=...&appliedAtTo=... -> 앱 종료 -> H2 조회"
diff_ref: "git diff -- .agents/outer-loop/retrospectives/SPEC-0017/TASK-0004-history-date-filter-runtime-evidence.md .agents/outer-loop/retrospectives/SPEC-0017/SPEC-0017-summary.md .agents/outer-loop/registry.json"
failure_summary: "service/repository seam은 닫혔지만, 실제 앱에서 무필터 history, 시작 시각 필터 history, 종료 시각 필터 history, 범위 필터 history가 H2 audit evidence와 같은 execution 기준으로 일치하는지 최종 확인이 필요했다."
root_cause: "기간 필터는 입력 경계와 selection seam만 맞아도 충분하지 않고, 실제 운영 HTTP 응답 subset과 H2 audit row subset이 execution 단위로 같은 의미를 가져야 한다."
agents_check_findings:
  - "representative actual app/H2 검증을 `앱 기동 -> 새 execution 생성 -> ACKNOWLEDGE/UNACKNOWLEDGE 실행 -> 무필터/기간 필터 history 조회 -> 앱 종료 -> H2 조회` 순서로 수행했다."
  - "대표 검증에는 새 delivery_id, execution_key를 사용했다."
  - "targeted test와 full cleanTest test를 순차 실행했다."
next_task_warnings:
  - "다음 spec에서 관리자 액션 이력에 추가 필터나 정렬 기준을 붙이면, 무필터 timeline과 필터 적용 subset이 representative execution 기준으로 다시 일치하는지 확인해야 한다."
  - "기간 필터는 inclusive 조건이므로 representative 검증에서도 실제 appliedAt 값을 그대로 써서 기대 row를 고정하는 편이 안전하다."
error_signature: "GITHUB_APP_CONFIGURATION_MISSING"
test_result_summary: "targeted test, full cleanTest test, representative actual app/H2 verification이 모두 통과했고, 무필터/시작 시각/종료 시각/범위 필터 history 응답이 같은 execution 기준으로 H2 audit evidence와 일치했다."
snapshot_hashes: []
---

# 작업 회고

## 작업 요약
- representative manual rerun execution 하나를 만들고 `ACKNOWLEDGE`, `UNACKNOWLEDGE` action을 순서대로 적용했다.
- 같은 `executionKey` 기준으로 무필터 history, `appliedAtFrom` 필터 history, `appliedAtTo` 필터 history, 범위 필터 history를 모두 조회했다.
- 앱 종료 후 H2 file DB를 조회해 `WEBHOOK_EXECUTION`과 `MANUAL_RERUN_CONTROL_ACTION_AUDIT` row가 history 응답 의미와 일치하는지 검증했다.

## 실패 요약
- 기능 실패는 없었다.
- 이번 task의 핵심 위험은 HTTP history 응답에서 보이는 row subset과 H2 audit row subset이 실제 execution 기준으로 어긋날 수 있다는 점이었다.

## Root Cause
- 기간 필터는 selection seam만으로 끝나는 기능이 아니라, 운영자가 실제로 읽는 HTTP 응답과 H2 evidence가 같은 의미를 가져야 한다.
- 이전 task에서 seam이 충분히 닫혔기 때문에 이번 task는 representative verification에 집중하는 구성이 맞았다.

## AGENTS 체크 결과
- `PRD -> Spec -> ValidationCriteria -> Task -> Issue` 흐름에 맞춰 `#67`을 1:1 issue로 연결했다.
- targeted test와 full `cleanTest test`를 순차 실행했다.
- 실제 앱/H2 representative 검증은 새 `delivery_id`, `execution_key`로 수행했고, 앱 종료 후 H2 조회 순서를 지켰다.

## 근거 Artifact
- representative `executionKey`: `EXECUTION:MANUAL_RERUN:0414f9ba-50db-4675-a972-7fdcf25db09d`
- representative `delivery_id`: `MANUAL_RERUN_DELIVERY:0414f9ba-50db-4675-a972-7fdcf25db09d`
- rerun 응답:
  - `FAILED / GITHUB_APP_CONFIGURATION_MISSING / MANUAL_ACTION_REQUIRED / writePerformed=false`
- action 응답:
  - `ACKNOWLEDGE / APPLIED / availableActions=[UNACKNOWLEDGE]`
  - `UNACKNOWLEDGE / APPLIED / availableActions=[ACKNOWLEDGE]`
- history 무필터 응답:
  - `ACKNOWLEDGE(2026-04-11T22:20:37.195307)`
  - `UNACKNOWLEDGE(2026-04-11T22:20:38.230284)`
- history 시작 시각 필터 응답:
  - `appliedAtFrom=2026-04-11T22:20:38.230284` -> `UNACKNOWLEDGE` 1건
- history 종료 시각 필터 응답:
  - `appliedAtTo=2026-04-11T22:20:37.195307` -> `ACKNOWLEDGE` 1건
- history 범위 필터 응답:
  - `appliedAtFrom=2026-04-11T22:20:38.230284`
  - `appliedAtTo=2026-04-11T22:20:38.230284`
  - `UNACKNOWLEDGE` 1건
- H2 `WEBHOOK_EXECUTION`:
  - `FAILED / GITHUB_APP_CONFIGURATION_MISSING / MANUAL_ACTION_REQUIRED / MANUAL_RERUN / DRY_RUN / FALSE`
- H2 `MANUAL_RERUN_CONTROL_ACTION_AUDIT`:
  - `ACKNOWLEDGE / APPLIED / 운영자 확인 완료 / 2026-04-11 22:20:37.195307`
  - `UNACKNOWLEDGE / APPLIED / 운영자 확인 취소 / 2026-04-11 22:20:38.230284`

## 다음 Task 경고사항
- 다음 spec에서 기간 필터 위에 정렬 기준이나 페이지 기준을 올리면, representative execution 기준으로 subset 의미를 다시 확인해야 한다.
- history 조회는 timeline과 subset 조회 역할을 유지하고, 현재 상태 요약은 query/list에 두는 경계를 계속 유지해야 한다.

## 제안 필요 여부
- 없음
