---
spec_id: SPEC-0014
task_id: TASK-0004
github_issue_number: 55
criteria_keys:
  - manual-rerun-control-history-runtime-evidence-aligned
delivery_ids:
  - MANUAL_RERUN_DELIVERY:de8788b5-a424-40ea-8360-e16cc04ab53e
execution_keys:
  - EXECUTION:MANUAL_RERUN:de8788b5-a424-40ea-8360-e16cc04ab53e
test_evidence_ref:
  - "targeted: ./scripts/gradlew-java21.sh --no-daemon test --tests 'com.agilerunner.api.controller.review.ManualRerunControllerTest' --tests 'com.agilerunner.api.service.review.ManualRerunControlActionServiceTest' --tests 'com.agilerunner.api.service.review.ManualRerunControlActionHistoryServiceTest' --tests 'com.agilerunner.api.service.review.ManualRerunQueryServiceTest' --tests 'com.agilerunner.api.service.review.ManualRerunExecutionListServiceTest' --tests 'com.agilerunner.client.agentruntime.AgentRuntimeRepositoryTest' --console=plain"
  - "full: ./scripts/gradlew-java21.sh --no-daemon cleanTest test --console=plain"
  - "actual-app: POST /reviews/rerun -> POST /reviews/rerun/{executionKey}/actions -> GET /reviews/rerun/{executionKey}/actions/history -> GET /reviews/rerun/{executionKey} -> GET /reviews/rerun/executions?repositoryName=owner/repo&pullRequestNumber=314&executionStartType=MANUAL_RERUN"
diff_ref: "git diff -- .agents/outer-loop/retrospectives/SPEC-0014/TASK-0004-action-history-runtime-evidence.md .agents/outer-loop/retrospectives/SPEC-0014/SPEC-0014-summary.md .agents/outer-loop/registry.json"
failure_summary: "history 응답, query/list 상태, action 응답, H2 audit evidence가 실제 앱 기준으로 같은 execution에 대해 같은 의미를 가지는지 최종 확인이 필요했다."
root_cause: "이전 task에서 controller/service/repository seam은 닫혔지만, 운영자가 실제로 보는 응답과 H2 audit row가 representative execution에서 일치하는지는 별도 검증이 필요했다."
agents_check_findings:
  - "실제 앱/H2 대표 검증을 `앱 기동 -> 새 execution 생성 -> action 실행 -> history/query/list 확인 -> 앱 종료 -> H2 조회` 순서로 수행했다."
  - "이번 task는 runtime 저장 구조를 바꾸지 않았고, representative 검증과 문서 정리만 수행했다."
next_task_warnings:
  - "다음 spec에서 같은 action 반복 허용을 다루면 audit unique constraint와 history timeline 의미를 함께 재설계해야 한다."
  - "운영용 화면이 붙더라도 query/list는 current state만, history는 action detail만 읽는 경계를 유지해야 한다."
error_signature: "GITHUB_APP_CONFIGURATION_MISSING"
test_result_summary: "targeted test, full cleanTest test, representative actual app/H2 verification이 모두 통과했다. 대표 execution 기준으로 action 응답, history 응답, query/list 상태, H2 audit row가 일치했다."
snapshot_hashes: []
---

# 작업 회고

## 작업 요약
- representative execution 하나를 만들고 `ACKNOWLEDGE` action을 적용했다.
- 같은 `executionKey` 기준으로 action 응답, history 조회, 단건 조회, 목록 조회를 확인했다.
- 앱 종료 후 H2 file DB를 조회해 `WEBHOOK_EXECUTION`과 `MANUAL_RERUN_CONTROL_ACTION_AUDIT` row가 응답 의미와 일치하는지 검증했다.

## 실패 요약
- 기능 실패는 없었다.
- representative 검증에서 중요한 위험은 history 응답은 맞아도 query/list나 H2 audit row 의미가 서로 엇갈릴 수 있다는 점이었다.

## Root Cause
- action history 조회는 새로운 read 경계라서, 실제 앱 기준으로 action 응답과 history/query/list/H2를 한 번에 대조하지 않으면 운영자가 읽는 의미가 어긋날 수 있었다.
- 이전 task에서 seam은 닫혔으므로 이번 task는 실제 representative verification만 집중해서 수행하는 것이 맞았다.

## AGENTS 체크 결과
- targeted test와 full test를 순차 실행했다.
- 실제 앱/H2 representative 검증은 새 `delivery_id`, `execution_key`로 수행했다.
- 앱 종료 후 H2 Shell 조회 순서를 지켰다.

## 근거 Artifact
- representative `executionKey`: `EXECUTION:MANUAL_RERUN:de8788b5-a424-40ea-8360-e16cc04ab53e`
- representative `delivery_id`: `MANUAL_RERUN_DELIVERY:de8788b5-a424-40ea-8360-e16cc04ab53e`
- action 응답:
  - `ACKNOWLEDGE / APPLIED / note=운영자 확인 완료 / availableActions=[UNACKNOWLEDGE]`
- history 응답:
  - `ACKNOWLEDGE / APPLIED / note=운영자 확인 완료 / appliedAt=2026-04-10T23:19:05.994656`
- query 응답:
  - `FAILED / GITHUB_APP_CONFIGURATION_MISSING / MANUAL_ACTION_REQUIRED / availableActions=[UNACKNOWLEDGE]`
- list 응답:
  - 같은 `executionKey` row에서 `availableActions=[UNACKNOWLEDGE]`
- H2 `WEBHOOK_EXECUTION`:
  - `FAILED / GITHUB_APP_CONFIGURATION_MISSING / MANUAL_ACTION_REQUIRED / MANUAL_RERUN / DRY_RUN / FALSE`
- H2 `MANUAL_RERUN_CONTROL_ACTION_AUDIT`:
  - `ACKNOWLEDGE / APPLIED / 운영자 확인 완료`

## 다음 Task 경고사항
- 후속 spec에서 같은 action 반복 허용을 다루면 현재 unique constraint와 history 조회 응답 의미를 같이 손봐야 한다.

## 제안 필요 여부
- 없음
