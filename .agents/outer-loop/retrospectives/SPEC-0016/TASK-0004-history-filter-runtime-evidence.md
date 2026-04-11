---
spec_id: SPEC-0016
task_id: TASK-0004
github_issue_number: 63
criteria_keys:
  - manual-rerun-control-history-filter-runtime-evidence-aligned
delivery_ids:
  - MANUAL_RERUN_DELIVERY:7e397b93-3bb9-470e-ad20-201a91ee0426
execution_keys:
  - EXECUTION:MANUAL_RERUN:7e397b93-3bb9-470e-ad20-201a91ee0426
test_evidence_ref:
  - "targeted: ./scripts/gradlew-java21.sh --no-daemon test --tests 'com.agilerunner.api.controller.review.ManualRerunControllerTest' --tests 'com.agilerunner.api.service.review.ManualRerunControlActionServiceTest' --tests 'com.agilerunner.api.service.review.ManualRerunControlActionHistoryServiceTest' --tests 'com.agilerunner.api.service.review.ManualRerunQueryServiceTest' --tests 'com.agilerunner.api.service.review.ManualRerunExecutionListServiceTest' --tests 'com.agilerunner.client.agentruntime.AgentRuntimeRepositoryTest' --console=plain"
  - "full: ./scripts/gradlew-java21.sh --no-daemon cleanTest test --console=plain"
  - "actual-app: POST /reviews/rerun -> POST /reviews/rerun/{executionKey}/actions (ACKNOWLEDGE, UNACKNOWLEDGE) -> GET /reviews/rerun/{executionKey}/actions/history -> GET /reviews/rerun/{executionKey}/actions/history?action=ACKNOWLEDGE&actionStatus=APPLIED -> GET /reviews/rerun/{executionKey}/actions/history?action=UNACKNOWLEDGE&actionStatus=APPLIED -> 앱 종료 -> H2 조회"
diff_ref: "git diff -- .agents/outer-loop/retrospectives/SPEC-0016/TASK-0004-history-filter-runtime-evidence.md .agents/outer-loop/retrospectives/SPEC-0016/SPEC-0016-summary.md .agents/outer-loop/registry.json"
failure_summary: "service/repository selection seam은 닫혔지만, 실제 앱에서 무필터 history 응답, 필터 적용 history 응답, H2 audit evidence가 같은 execution 기준으로 일치하는지 최종 확인이 필요했다."
root_cause: "이전 task에서 controller/service/repository 경계는 닫혔지만, 운영자가 실제로 보는 history 응답과 H2 audit row subset이 representative execution에서 같은 의미를 가지는지는 별도 검증이 필요했다."
agents_check_findings:
  - "representative actual app/H2 검증을 `앱 기동 -> 새 execution 생성 -> action 2건 실행 -> history 무필터/필터 조회 -> 앱 종료 -> H2 조회` 순서로 수행했다."
  - "이번 task는 production code 변경 없이 representative verification과 문서 정리로 닫았다."
  - "targeted test와 full cleanTest test를 순차 실행했다."
next_task_warnings:
  - "다음 spec에서 history 날짜 범위나 추가 필터를 붙이면, 무필터 timeline과 필터 적용 subset 의미를 다시 representative execution으로 검증해야 한다."
  - "운영 화면이 붙더라도 history 조회는 현재 상태 요약이 아니라 timeline과 subset 조회에 집중하는 경계를 유지해야 한다."
error_signature: "GITHUB_APP_CONFIGURATION_MISSING"
test_result_summary: "targeted test, full cleanTest test, representative actual app/H2 verification이 모두 통과했다. 같은 execution 기준으로 무필터 history, 필터 적용 history, H2 audit evidence가 일치했다."
snapshot_hashes: []
---

# 작업 회고

## 작업 요약
- representative manual rerun execution 하나를 만들고 `ACKNOWLEDGE`, `UNACKNOWLEDGE` action을 순서대로 적용했다.
- 같은 `executionKey` 기준으로 무필터 history 응답과 `action`, `actionStatus` 필터가 적용된 history 응답을 모두 확인했다.
- 앱 종료 후 H2 file DB를 조회해 `WEBHOOK_EXECUTION`과 `MANUAL_RERUN_CONTROL_ACTION_AUDIT` row가 history 응답 의미와 일치하는지 검증했다.

## 실패 요약
- 기능 실패는 없었다.
- 이번 task의 핵심 위험은 service/repository seam은 맞아도, 실제 앱 기준으로 history 응답과 H2 audit evidence가 execution 단위로 어긋날 수 있다는 점이었다.

## Root Cause
- history 필터 확장은 응답 subset 의미를 바꾸는 작업이라, H2 audit row subset과 실제 HTTP 응답을 같은 execution 기준으로 한 번 더 맞춰 봐야 했다.
- 이전 task들에서 seam이 충분히 닫혔기 때문에 이번 task는 representative verification 자체에 집중하는 구성이 맞았다.

## AGENTS 체크 결과
- `PRD -> Spec -> ValidationCriteria -> Task -> Issue` 흐름에 맞춰 `#63`을 1:1 issue로 연결했다.
- targeted test와 full `cleanTest test`를 순차 실행했다.
- 실제 앱/H2 representative 검증은 새 `delivery_id`, `execution_key`로 수행했고, 앱 종료 후 H2 조회 순서를 지켰다.

## 근거 Artifact
- representative `executionKey`: `EXECUTION:MANUAL_RERUN:7e397b93-3bb9-470e-ad20-201a91ee0426`
- representative `delivery_id`: `MANUAL_RERUN_DELIVERY:7e397b93-3bb9-470e-ad20-201a91ee0426`
- rerun 응답:
  - `FAILED / GITHUB_APP_CONFIGURATION_MISSING / MANUAL_ACTION_REQUIRED / writePerformed=false`
- action 응답:
  - `ACKNOWLEDGE / APPLIED / availableActions=[UNACKNOWLEDGE]`
  - `UNACKNOWLEDGE / APPLIED / availableActions=[ACKNOWLEDGE]`
- history 무필터 응답:
  - `ACKNOWLEDGE`, `UNACKNOWLEDGE` 2건
- history 필터 응답:
  - `action=ACKNOWLEDGE&actionStatus=APPLIED` -> `ACKNOWLEDGE` 1건
  - `action=UNACKNOWLEDGE&actionStatus=APPLIED` -> `UNACKNOWLEDGE` 1건
- H2 `WEBHOOK_EXECUTION`:
  - `FAILED / GITHUB_APP_CONFIGURATION_MISSING / MANUAL_ACTION_REQUIRED / MANUAL_RERUN / DRY_RUN / FALSE`
- H2 `MANUAL_RERUN_CONTROL_ACTION_AUDIT`:
  - `ACKNOWLEDGE / APPLIED / 운영자 확인 완료`
  - `UNACKNOWLEDGE / APPLIED / 운영자 확인 취소`

## 다음 Task 경고사항
- 날짜 범위나 추가 필터를 붙일 때는 무필터 전체 timeline과 필터 적용 subset이 모두 유지되는지 representative execution으로 다시 검증해야 한다.
- history 응답은 timeline과 subset 조회를 담당하고, 현재 상태 요약은 query/list에 두는 경계를 계속 유지해야 한다.

## 제안 필요 여부
- 없음
