---
spec_id: SPEC-0026
task_id: TASK-0004
github_issue_number: 103
criteria_keys:
  - manual-rerun-seed-helper-representative-verified
delivery_ids:
  - MANUAL_RERUN_DELIVERY:example-rerun
  - MANUAL_RERUN_DELIVERY:example-retry-source
  - MANUAL_RERUN_DELIVERY:d6cd15d0-b9bb-4c6b-9fe9-4f40ccaa46ab
execution_keys:
  - EXECUTION:MANUAL_RERUN:example-rerun
  - EXECUTION:MANUAL_RERUN:example-retry-source
  - EXECUTION:MANUAL_RERUN:d6cd15d0-b9bb-4c6b-9fe9-4f40ccaa46ab
test_evidence_ref:
  - "대상 테스트: ./scripts/gradlew-java21.sh --no-daemon test --tests 'com.agilerunner.client.agentruntime.ManualRerunResponseSeedSqlTest' --tests 'com.agilerunner.client.agentruntime.ManualRerunResponseSeedEvidenceSqlTest' --tests 'com.agilerunner.api.controller.review.ManualRerunResponseGuideFixtureTest' --tests 'com.agilerunner.api.controller.review.ManualRerunControllerTest' --tests 'com.agilerunner.api.service.review.ManualRerunServiceTest' --tests 'com.agilerunner.api.service.review.ManualRerunRetryServiceTest' --tests 'com.agilerunner.api.service.review.ManualRerunQueryServiceTest' --tests 'com.agilerunner.api.service.review.ManualRerunExecutionListServiceTest' --tests 'com.agilerunner.api.service.review.ManualRerunControlActionHistoryServiceTest' --tests 'com.agilerunner.api.service.review.ManualRerunControlActionServiceTest' --console=plain"
  - "전체 테스트: ./scripts/gradlew-java21.sh --no-daemon cleanTest test --console=plain"
  - "actual-app: seed reset/apply -> bootRun(local, port=18080) -> GET /reviews/rerun/EXECUTION:MANUAL_RERUN:example-rerun -> GET /reviews/rerun/EXECUTION:MANUAL_RERUN:example-rerun/actions/history -> POST /reviews/rerun/EXECUTION:MANUAL_RERUN:example-rerun/actions UNACKNOWLEDGE -> GET /reviews/rerun/EXECUTION:MANUAL_RERUN:example-rerun -> POST /reviews/rerun/EXECUTION:MANUAL_RERUN:example-retry-source/retry -> GET /reviews/rerun/EXECUTION:MANUAL_RERUN:d6cd15d0-b9bb-4c6b-9fe9-4f40ccaa46ab -> app shutdown -> H2 Shell query"
diff_ref: "git diff -- docs/manual-rerun-response-seed-command-guide.md .agents/outer-loop/retrospectives/SPEC-0026/TASK-0004-helper-command-representative-verified.md .agents/outer-loop/retrospectives/SPEC-0026/SPEC-0026-summary.md .agents/outer-loop/registry.json"
failure_summary: "실행 자체 실패는 없었다. representative 검증을 실제로 따라가면서 command guide에 rerun query/history/action/query와 retry derived query 명령이 빠져 있다는 문서 gap을 발견해 task 안에서 보강했다."
root_cause: "TASK-0003까지는 실행 키 추출과 H2 query 명령을 정리했지만, representative 요청 자체를 어떤 순서와 어떤 파일 경로로 남길지는 여전히 암묵지로 남아 있었다. 실제 문서만 보고 따라가 보니 request 단계 명령도 command guide 안에 있어야 전체 절차가 닫혔다."
agents_check_findings:
  - "문서 경계 리뷰는 command guide가 이제 seed apply/reset, representative 요청, 실행 키 추출, 앱 종료 후 H2 조회까지 한 흐름을 담고 있고 비대상은 자동화 스크립트와 결과 반영 작업으로 남긴 구성을 PASS로 봤다."
  - "검증 근거 리뷰는 rerun과 retry representative 응답, 파생 실행 키, H2 Shell 결과가 같은 실행 키 기준으로 연결된다고 판단하고 PASS를 줬다."
  - "가독성 리뷰는 새 작업자가 command guide 하나로 representative 흐름을 따라갈 수 있는지 확인하고 PASS를 줬다."
next_task_warnings:
  - "다음 단계에서 helper command를 더 자동화하려면 지금 문서에 고정한 파일 경로와 변수 이름을 바꾸지 않는 편이 안전하다."
  - "retry representative는 source 실행 키와 파생 실행 키를 혼동하면 H2 evidence가 엇갈리므로, 파생 실행 키를 응답 직후 바로 기록하는 규칙을 유지해야 한다."
error_signature: ""
test_result_summary: "관련 대상 테스트와 전체 cleanTest test가 모두 통과했다. actual app/H2 representative 검증도 통과했고, rerun query/history/action/query 응답과 retry response/query 응답, H2 WEBHOOK_EXECUTION/AGENT_EXECUTION_LOG/MANUAL_RERUN_CONTROL_ACTION_AUDIT가 같은 실행 키 기준으로 연결됐다."
snapshot_hashes: []
---

# 작업 회고

## 작업 요약
- command guide만 따라 rerun 준비 실행과 retry 원본 실행 대표 검증을 다시 수행했다.
- rerun은 단건 조회, 이력 조회, `UNACKNOWLEDGE` 조치, 조치 후 단건 조회까지 확인했다.
- retry는 source 실행 기준 retry 요청을 보내 새 파생 실행 키를 받고, 그 키로 단건 조회와 H2 evidence를 확인했다.
- representative 요청 자체를 따라가기 위한 curl 명령과 응답 파일 경로 예시를 command guide에 추가했다.

## 실패 요약
- 실행 자체 실패는 없었다.
- 다만 representative 절차를 문서만 따라가 보니 request 단계 명령이 command guide에 빠져 있어, task 안에서 문서를 한 번 더 보강했다.

## Root Cause
- helper command 정리는 seed apply/reset, 실행 키 추출, H2 query를 따로따로 정리하는 것만으로는 충분하지 않았다.
- representative 검증을 실제로 다시 닫으려면 rerun query/history/action/query와 retry derived query까지 같은 문서 안에서 이어져야 새 작업자가 중간 추론 없이 따라갈 수 있었다.

## AGENTS 체크 결과
- linked issue `#103`을 `TASK-0004`와 1:1로 유지했다.
- 대상 테스트와 전체 테스트를 순차 실행했다.
- actual app/H2 representative 검증은 `seed reset/apply -> 앱 기동 -> representative 요청 -> 앱 종료 -> H2 Shell query` 순서를 지켰다.
- H2 조회는 앱 종료 뒤 순차 실행했고, 같은 H2 file에 대한 동시 조회는 만들지 않았다.
- 3개 서브에이전트는 문서 경계, 검증 근거, 가독성 관점으로 리뷰했고 최종 PASS로 수렴했다.

## 근거 자료
- command guide
  - [manual-rerun-response-seed-command-guide.md](/home/seaung13/workspace/agile-runner/docs/manual-rerun-response-seed-command-guide.md)
- representative rerun execution
  - `executionKey`: `EXECUTION:MANUAL_RERUN:example-rerun`
  - `delivery_id`: `MANUAL_RERUN_DELIVERY:example-rerun`
  - query 응답(조치 전): `FAILED / GITHUB_APP_CONFIGURATION_MISSING / MANUAL_ACTION_REQUIRED / availableActions=[UNACKNOWLEDGE]`
  - history 응답: `latestAction=ACKNOWLEDGE / note=운영자 확인 완료`
  - action 응답: `UNACKNOWLEDGE / APPLIED / availableActions=[ACKNOWLEDGE] / note=대표 검증용 확인 해제`
  - query 응답(조치 후): `availableActions=[ACKNOWLEDGE]`
  - H2 `WEBHOOK_EXECUTION`: `FAILED / GITHUB_APP_CONFIGURATION_MISSING / MANUAL_ACTION_REQUIRED / MANUAL_RERUN / DRY_RUN / FALSE`
  - H2 `MANUAL_RERUN_CONTROL_ACTION_AUDIT`: `ACKNOWLEDGE APPLIED 운영자 확인 완료`, `UNACKNOWLEDGE APPLIED 대표 검증용 확인 해제`
- representative retry execution
  - source `executionKey`: `EXECUTION:MANUAL_RERUN:example-retry-source`
  - source `delivery_id`: `MANUAL_RERUN_DELIVERY:example-retry-source`
  - derived `executionKey`: `EXECUTION:MANUAL_RERUN:d6cd15d0-b9bb-4c6b-9fe9-4f40ccaa46ab`
  - derived `delivery_id`: `MANUAL_RERUN_DELIVERY:d6cd15d0-b9bb-4c6b-9fe9-4f40ccaa46ab`
  - retry 응답: `FAILED / GITHUB_APP_CONFIGURATION_MISSING / MANUAL_ACTION_REQUIRED / DRY_RUN / writePerformed=false / retrySourceExecutionKey=EXECUTION:MANUAL_RERUN:example-retry-source`
  - derived query 응답: `FAILED / GITHUB_APP_CONFIGURATION_MISSING / MANUAL_ACTION_REQUIRED / availableActions=[ACKNOWLEDGE]`
  - H2 `WEBHOOK_EXECUTION`: `retry_source_execution_key=EXECUTION:MANUAL_RERUN:example-retry-source / FAILED / GITHUB_APP_CONFIGURATION_MISSING / MANUAL_ACTION_REQUIRED / MANUAL_RERUN / DRY_RUN / FALSE`
  - H2 `AGENT_EXECUTION_LOG`: `manual-rerun-accepted SUCCEEDED`, `review-generated FAILED`, 두 row 모두 같은 `retry_source_execution_key` 유지

## 다음 Task 경고사항
- 다음 spec에서 helper command를 스크립트로 자동화하려면, 지금 문서에 고정한 임시 파일 경로와 변수 이름을 바꾸지 않는 편이 안전하다.
- representative 실제 실행과 문서 예시 기준 파일은 여전히 다른 artifact이므로, 새 UUID나 시각을 기준 파일에 바로 옮기지 않아야 한다.

## 제안 필요 여부
- 없음
- 새 제안 없음. 이번 교훈은 새 workflow 규칙보다 command guide의 representative 요청 명령을 실제 사용 수준으로 채우는 작업에서 흡수됐다.
