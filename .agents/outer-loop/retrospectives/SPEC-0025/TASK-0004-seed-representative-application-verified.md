---
spec_id: SPEC-0025
task_id: TASK-0004
github_issue_number: 99
criteria_keys:
  - manual-rerun-seed-representative-application-verified
delivery_ids:
  - MANUAL_RERUN_DELIVERY:example-rerun
  - MANUAL_RERUN_DELIVERY:example-retry-source
  - MANUAL_RERUN_DELIVERY:d52874b9-cef8-4b14-af68-958f98a72489
execution_keys:
  - EXECUTION:MANUAL_RERUN:example-rerun
  - EXECUTION:MANUAL_RERUN:example-retry-source
  - EXECUTION:MANUAL_RERUN:d52874b9-cef8-4b14-af68-958f98a72489
test_evidence_ref:
  - "대상 테스트: ./scripts/gradlew-java21.sh --no-daemon test --tests 'com.agilerunner.client.agentruntime.ManualRerunResponseSeedSqlTest' --tests 'com.agilerunner.client.agentruntime.ManualRerunResponseSeedEvidenceSqlTest' --tests 'com.agilerunner.api.controller.review.ManualRerunResponseGuideFixtureTest' --tests 'com.agilerunner.api.controller.review.ManualRerunControllerTest' --tests 'com.agilerunner.api.service.review.ManualRerunServiceTest' --tests 'com.agilerunner.api.service.review.ManualRerunRetryServiceTest' --tests 'com.agilerunner.api.service.review.ManualRerunQueryServiceTest' --tests 'com.agilerunner.api.service.review.ManualRerunExecutionListServiceTest' --tests 'com.agilerunner.api.service.review.ManualRerunControlActionHistoryServiceTest' --tests 'com.agilerunner.api.service.review.ManualRerunControlActionServiceTest' --console=plain"
  - "전체 테스트: ./scripts/gradlew-java21.sh --no-daemon cleanTest test --console=plain"
  - "actual-app: H2 seed apply -> bootRun(local, port=18080) -> GET /reviews/rerun/EXECUTION:MANUAL_RERUN:example-rerun -> GET /reviews/rerun/EXECUTION:MANUAL_RERUN:example-rerun/actions/history -> POST /reviews/rerun/EXECUTION:MANUAL_RERUN:example-rerun/actions UNACKNOWLEDGE -> GET /reviews/rerun/EXECUTION:MANUAL_RERUN:example-rerun -> POST /reviews/rerun/EXECUTION:MANUAL_RERUN:example-retry-source/retry -> GET /reviews/rerun/{derivedExecutionKey} -> app shutdown -> H2 query"
diff_ref: "git diff -- docs/manual-rerun-response-seed-guide.md .agents/outer-loop/retrospectives/SPEC-0025/TASK-0004-seed-representative-application-verified.md .agents/outer-loop/retrospectives/SPEC-0025/SPEC-0025-summary.md .agents/outer-loop/registry.json"
failure_summary: "첫 representative 확인 중 jq가 없는 환경에서 retry 응답 실행 키를 추출하려다 파생 실행이 한 건 더 생성됐다. 앱 종료 뒤 seed와 파생 row를 정리하고 절차를 다시 실행해 clean run으로 닫았다."
root_cause: "실제 representative 검증 절차는 seed 적용과 HTTP 요청 순서만 맞으면 된다고 보기 쉬웠지만, 응답에서 파생 실행 키를 추출하는 도구 가정까지 포함되면 절차가 환경 의존적으로 흔들릴 수 있다. 이번에는 jq 설치를 전제로 한 임시 명령이 문제였다."
agents_check_findings:
  - "문서 경계 리뷰는 guide가 rerun은 seed 실행 키를 그대로 읽고 retry는 응답에서 받은 새 실행 키를 따라간다는 차이만 보강하고, 후속 helper command 정리는 다음 spec으로 남긴 점을 확인한 뒤 PASS를 줬다."
  - "검증 근거 리뷰는 representative 응답과 H2 evidence가 같은 실행 키 기준으로 맞는지, rerun audit 2건과 derived retry log 2건까지 포함해 확인한 뒤 PASS를 줬다."
  - "가독성 리뷰는 guide의 새 섹션이 실제 representative 검증에서 어떤 실행 키를 따라가야 하는지 바로 읽히는지 확인한 뒤 PASS를 줬다."
next_task_warnings:
  - "후속 spec에서는 jq 같은 외부 도구를 전제하지 않는 seed apply/H2 query 보조 명령 예시를 정리하는 편이 안전하다."
  - "retry representative는 응답에서 받은 파생 실행 키를 즉시 기록하지 않으면 H2 evidence 조회가 흔들릴 수 있으니, helper command 단계에서 키 추출 방법을 표준화해야 한다."
error_signature: "jq: command not found"
test_result_summary: "seed SQL 적용 뒤 targeted test, full cleanTest test, representative actual app/H2 verification이 모두 통과했다. rerun seeded execution은 query/history/action/query와 H2 audit row가, retry source seeded execution은 retry 응답, derived query, H2 webhook/log row가 같은 실행 키 기준으로 연결됐다."
snapshot_hashes: []
---

# 작업 회고

## 작업 요약
- local H2 file에 retry source seed와 rerun acknowledge seed를 실제로 적용했다.
- 앱을 local profile로 띄운 뒤 seeded rerun execution은 query, history, UNACKNOWLEDGE action, query 순서로 확인했다.
- seeded retry source execution은 retry 요청을 실행해 새 파생 실행 키를 받고, 같은 파생 실행 키로 query와 H2 evidence를 확인했다.
- guide 문서에는 실제 representative 검증에서 어떤 실행 키를 따라가야 하는지 정리하고, 이제 끝난 내용을 후속 단계 목록에서 분리했다.

## 실패 요약
- 첫 representative 확인에서는 jq가 없는 환경에서 retry 응답의 파생 실행 키를 추출하려다 파생 실행이 한 건 더 생성됐다.
- 코드 문제는 아니었고, 앱 종료 뒤 seeded row와 파생 row를 정리한 뒤 절차를 다시 수행해 clean run으로 닫았다.

## Root Cause
- representative 검증 절차는 HTTP 응답과 H2 evidence를 같은 실행 키로 묶는 것이 핵심인데, 처음에는 응답 키 추출 도구 가정을 명시적으로 두지 않았다.
- jq가 없는 환경에서 임시 명령이 실패하면서 의도보다 retry가 한 번 더 실행됐고, clean representative evidence를 위해 다시 seed reset이 필요해졌다.

## AGENTS 체크 결과
- linked issue `#99`를 `TASK-0004`와 1:1로 유지했다.
- targeted test와 full `cleanTest test`를 순차 실행했다.
- representative actual app/H2 검증은 `seed apply -> 앱 기동 -> representative 요청 실행 -> 앱 종료 -> H2 query` 순서를 지켰고, H2 조회는 앱 종료 후 순차로 실행했다.
- 3개 서브에이전트는 문서 경계, 실행 증거, 가독성 관점으로 리뷰했고 최종 PASS로 수렴했다.

## 근거 자료
- guide 문서
  - [manual-rerun-response-seed-guide.md](/home/seaung13/workspace/agile-runner/docs/manual-rerun-response-seed-guide.md)
- representative rerun execution
  - `executionKey`: `EXECUTION:MANUAL_RERUN:example-rerun`
  - `delivery_id`: `MANUAL_RERUN_DELIVERY:example-rerun`
  - query 응답(조치 전): `FAILED / GITHUB_APP_CONFIGURATION_MISSING / MANUAL_ACTION_REQUIRED / availableActions=[UNACKNOWLEDGE]`
  - history 응답(조치 전): `latestAction=ACKNOWLEDGE / note=운영자 확인 완료`
  - action 응답: `UNACKNOWLEDGE / APPLIED / availableActions=[ACKNOWLEDGE] / note=대표 검증용 확인 해제`
  - query 응답(조치 후): `availableActions=[ACKNOWLEDGE]`
  - H2 `WEBHOOK_EXECUTION`: `FAILED / GITHUB_APP_CONFIGURATION_MISSING / MANUAL_ACTION_REQUIRED / MANUAL_RERUN / DRY_RUN / FALSE`
  - H2 `MANUAL_RERUN_CONTROL_ACTION_AUDIT`: `ACKNOWLEDGE APPLIED 운영자 확인 완료`, `UNACKNOWLEDGE APPLIED 대표 검증용 확인 해제`
- representative retry execution
  - source `executionKey`: `EXECUTION:MANUAL_RERUN:example-retry-source`
  - source `delivery_id`: `MANUAL_RERUN_DELIVERY:example-retry-source`
  - derived `executionKey`: `EXECUTION:MANUAL_RERUN:d52874b9-cef8-4b14-af68-958f98a72489`
  - derived `delivery_id`: `MANUAL_RERUN_DELIVERY:d52874b9-cef8-4b14-af68-958f98a72489`
  - retry 응답: `FAILED / GITHUB_APP_CONFIGURATION_MISSING / MANUAL_ACTION_REQUIRED / DRY_RUN / writePerformed=false / retrySourceExecutionKey=EXECUTION:MANUAL_RERUN:example-retry-source`
  - derived query 응답: `FAILED / GITHUB_APP_CONFIGURATION_MISSING / MANUAL_ACTION_REQUIRED / availableActions=[ACKNOWLEDGE]`
  - H2 `WEBHOOK_EXECUTION`: `retry_source_execution_key=EXECUTION:MANUAL_RERUN:example-retry-source / FAILED / GITHUB_APP_CONFIGURATION_MISSING / MANUAL_ACTION_REQUIRED / MANUAL_RERUN / DRY_RUN / FALSE`
  - H2 `AGENT_EXECUTION_LOG`: `manual-rerun-accepted SUCCEEDED`, `review-generated FAILED`, 두 row 모두 같은 `retry_source_execution_key` 유지

## 다음 Task 경고사항
- 다음 spec은 helper command 정리이므로, jq 같은 외부 도구를 전제하지 않는 실행 키 추출 예시와 H2 query 예시를 표준으로 제시하는 편이 안전하다.
- 실제 representative 검증은 seed SQL 자체보다 응답에서 받은 실행 키를 어디에 다시 쓰는지가 더 중요하므로, 그 연결을 문서와 보조 명령에서 동시에 드러내야 한다.

## 제안 필요 여부
- 없음
- 새 제안 없음. 이번 교훈은 환경 도구 가정 문제였고, 후속 helper command spec에서 명령 예시를 정리하면 충분히 흡수할 수 있다.
