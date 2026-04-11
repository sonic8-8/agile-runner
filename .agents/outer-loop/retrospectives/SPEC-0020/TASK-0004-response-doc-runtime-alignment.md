---
spec_id: SPEC-0020
task_id: TASK-0004
github_issue_number: 79
criteria_keys:
  - manual-rerun-response-doc-runtime-aligned
delivery_ids:
  - MANUAL_RERUN_DELIVERY:5ab88c89-77d7-4cdc-9d12-fe3c5f3da504
  - MANUAL_RERUN_DELIVERY:retry-source-417d418c-6fbf-42ac-aa28-44f92ecc0324
  - MANUAL_RERUN_DELIVERY:56e81e77-81dd-49a2-a441-3f709b5bfda8
execution_keys:
  - EXECUTION:MANUAL_RERUN:5ab88c89-77d7-4cdc-9d12-fe3c5f3da504
  - EXECUTION:MANUAL_RERUN:retry-source-417d418c-6fbf-42ac-aa28-44f92ecc0324
  - EXECUTION:MANUAL_RERUN:56e81e77-81dd-49a2-a441-3f709b5bfda8
test_evidence_ref:
  - "targeted: ./scripts/gradlew-java21.sh --no-daemon test --tests 'com.agilerunner.api.controller.review.ManualRerunControllerTest' --tests 'com.agilerunner.api.service.review.ManualRerunServiceTest' --tests 'com.agilerunner.api.service.review.ManualRerunRetryServiceTest' --tests 'com.agilerunner.api.service.review.ManualRerunQueryServiceTest' --tests 'com.agilerunner.api.service.review.ManualRerunExecutionListServiceTest' --tests 'com.agilerunner.api.service.review.ManualRerunControlActionHistoryServiceTest' --tests 'com.agilerunner.api.service.review.ManualRerunControlActionServiceTest' --console=plain"
  - "full: ./scripts/gradlew-java21.sh --no-daemon cleanTest test --console=plain"
  - "actual-app: bootRun(local, port=18080) -> POST /reviews/rerun -> GET /reviews/rerun/{executionKey} -> GET /reviews/rerun/executions -> POST /reviews/rerun/{executionKey}/actions ACKNOWLEDGE -> GET /reviews/rerun/{executionKey}/actions/history -> GET /reviews/rerun/executions -> app shutdown"
  - "actual-app: H2 source seed -> bootRun(local, port=18080) -> POST /reviews/rerun/{executionKey}/retry -> GET /reviews/rerun/{executionKey} -> GET /reviews/rerun/executions -> app shutdown -> H2 query"
diff_ref: "git diff -- docs/manual-rerun-response-guide.md .agents/outer-loop/retrospectives/SPEC-0020/TASK-0004-response-doc-runtime-alignment.md .agents/outer-loop/retrospectives/SPEC-0020/SPEC-0020-summary.md .agents/outer-loop/registry.json"
failure_summary: "representative retry 검증 준비 중 synthetic source execution seed의 `write_skip_reason` 값이 현재 enum과 맞지 않아 첫 retry 요청이 500으로 실패했다."
root_cause: "retry representative 검증은 `RETRYABLE` source execution을 local H2에 직접 seed해야 했는데, hand-written seed SQL이 현재 `GitHubWriteSkipReason` enum 값과 어긋났다. 코드 문제보다 seed data 품질 문제였다."
agents_check_findings:
  - "3개 서브에이전트는 guide가 같은 execution의 조치 전/후 시점을 분리해서 읽는 기준을 더 명확히 적어야 한다고 지적했고, 해당 보정 후 모두 PASS로 수렴했다."
  - "representative rerun execution은 `query`를 조치 전 상태, `action`, `history`, `list`를 `ACKNOWLEDGE` 적용 직후 상태로 읽도록 문서와 검증 근거를 맞췄다."
  - "representative retry execution은 `retry` 응답뿐 아니라 derived execution의 list row에서 `retrySourceExecutionKey`와 `executionStartType=MANUAL_RERUN`까지 함께 확인했다."
next_task_warnings:
  - "다음 spec에서 문서 예시를 자동 검증 대상으로 삼으면 같은 execution의 조치 전/후 시점을 fixture 이름이나 섹션 제목으로 더 분명히 나눠야 한다."
  - "synthetic source execution seed가 필요한 representative 검증은 enum 성격 문자열 컬럼이 현재 domain enum 값과 맞는지 seed 전에 먼저 확인하는 편이 안전하다."
error_signature: "IllegalArgumentException: No enum constant GitHubWriteSkipReason.EXECUTION_CONTROL_MODE"
test_result_summary: "guide 보정 후 targeted test, full cleanTest test, representative actual app/H2 verification이 모두 기대 결과로 닫혔다."
snapshot_hashes: []
---

# 작업 회고

## 작업 요약
- representative rerun execution 1건과 representative retry execution 1건을 실제 앱에서 분리해 검증했다.
- rerun execution은 `rerun -> query -> action -> history -> list` 순서로 읽고, 같은 execution을 조치 전/후 시점으로 나눠 해석하도록 guide를 보정했다.
- retry execution은 synthetic retryable source execution 1건을 seed한 뒤 `POST /reviews/rerun/{executionKey}/retry`를 실행하고, 응답과 list row, H2 evidence가 같은 relation을 가지는지 확인했다.

## 실패 요약
- 첫 retry representative 검증은 `500`으로 실패했다.
- 원인은 local H2에 seed한 source execution row의 `write_skip_reason` 값이 현재 enum과 맞지 않았기 때문이다.
- seed 값을 `DRY_RUN`으로 바로잡은 뒤 representative retry 응답과 evidence 검증은 정상 통과했다.

## Root Cause
- 이번 task는 기능 구현보다 문서 기준과 실제 응답 정합성을 닫는 단계였지만, retry representative는 `RETRYABLE` source execution이 필요해서 local H2 seed가 추가로 필요했다.
- source seed를 손으로 넣는 과정에서 현재 domain enum 값과 다른 문자열이 들어가면서 repository row mapping에서 false negative가 발생했다.

## AGENTS 체크 결과
- linked issue `#79`를 `TASK-0004`와 1:1로 유지했고, retrospective와 spec summary 작성 후 `CLOSED` 상태까지 확인했다.
- targeted test와 full `cleanTest test`를 순차 실행했다.
- representative actual app 검증은 `앱 기동 -> fresh rerun 실행 -> query/list/action/history 확인 -> 앱 종료 -> H2 조회`, `source seed -> 앱 기동 -> retry 실행 -> query/list 확인 -> 앱 종료 -> H2 조회` 순서로 나눠서 수행했다.
- local H2 file 조회는 앱 종료 후 순차 실행했고, 같은 H2 file에 대한 병렬 조회는 하지 않았다.
- 3개 서브에이전트 재리뷰 후 guide의 시점 설명과 retry relation 설명이 충분하다는 PASS를 확인했다.

## 근거 Artifact
- representative rerun execution
  - `executionKey`: `EXECUTION:MANUAL_RERUN:5ab88c89-77d7-4cdc-9d12-fe3c5f3da504`
  - `delivery_id`: `MANUAL_RERUN_DELIVERY:5ab88c89-77d7-4cdc-9d12-fe3c5f3da504`
  - rerun 응답: `FAILED / GITHUB_APP_CONFIGURATION_MISSING / MANUAL_ACTION_REQUIRED / DRY_RUN / writePerformed=false`
  - query 응답(조치 전): `availableActions=[ACKNOWLEDGE]`
  - action 응답(ACKNOWLEDGE 직후): `APPLIED / availableActions=[UNACKNOWLEDGE] / note=운영자 확인 완료`
  - history 응답: `currentActionState.latestAction=ACKNOWLEDGE`, `currentActionState.availableActions=[UNACKNOWLEDGE]`, `actions[0].note=운영자 확인 완료`
  - list 응답(조치 후): `latestAction=ACKNOWLEDGE`, `latestActionStatus=APPLIED`, `historyAvailable=true`, `availableActions=[UNACKNOWLEDGE]`
- representative retry execution
  - source `executionKey`: `EXECUTION:MANUAL_RERUN:retry-source-417d418c-6fbf-42ac-aa28-44f92ecc0324`
  - source `delivery_id`: `MANUAL_RERUN_DELIVERY:retry-source-417d418c-6fbf-42ac-aa28-44f92ecc0324`
  - derived `executionKey`: `EXECUTION:MANUAL_RERUN:56e81e77-81dd-49a2-a441-3f709b5bfda8`
  - derived `delivery_id`: `MANUAL_RERUN_DELIVERY:56e81e77-81dd-49a2-a441-3f709b5bfda8`
  - retry 응답: `FAILED / GITHUB_APP_CONFIGURATION_MISSING / MANUAL_ACTION_REQUIRED / DRY_RUN / writePerformed=false / retrySourceExecutionKey=EXECUTION:MANUAL_RERUN:retry-source-417d418c-6fbf-42ac-aa28-44f92ecc0324`
  - derived query 응답: `availableActions=[ACKNOWLEDGE]`
  - derived list row: `retrySourceExecutionKey=EXECUTION:MANUAL_RERUN:retry-source-417d418c-6fbf-42ac-aa28-44f92ecc0324`, `executionStartType=MANUAL_RERUN`, `availableActions=[ACKNOWLEDGE]`
  - H2 `WEBHOOK_EXECUTION` derived row: `FAILED / GITHUB_APP_CONFIGURATION_MISSING / MANUAL_ACTION_REQUIRED / DRY_RUN / FALSE / retry_source_execution_key=EXECUTION:MANUAL_RERUN:retry-source-417d418c-6fbf-42ac-aa28-44f92ecc0324`
  - H2 `AGENT_EXECUTION_LOG` derived row: `manual-rerun-accepted SUCCEEDED`, `review-generated FAILED`, 두 row 모두 같은 `retry_source_execution_key` 유지
- 문서 보정
  - [docs/manual-rerun-response-guide.md](/home/seaung13/workspace/agile-runner/docs/manual-rerun-response-guide.md)

## 다음 Task 경고사항
- 후속 spec이 문서 예시 자동 검증으로 가면 같은 execution의 조치 전/후 시점을 fixture 이름과 설명 문구에서 더 강하게 분리해야 한다.
- synthetic source execution seed가 필요한 representative 검증은 SQL 값이 현재 enum과 맞는지 먼저 확인하지 않으면 false negative가 다시 날 수 있다.

## 제안 필요 여부
- 없음
- 이번 교훈은 새 workflow 규칙 부족보다 synthetic source seed 값을 현재 enum에 맞게 넣지 않은 일회성 준비 오류에 가까웠다.
