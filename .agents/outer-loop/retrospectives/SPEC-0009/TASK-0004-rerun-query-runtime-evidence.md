---
spec_id: SPEC-0009
task_id: TASK-0004
github_issue_number: 35
criteria_keys:
  - manual-rerun-query-response-matches-runtime-evidence
delivery_ids:
  - MANUAL_RERUN_DELIVERY:dab45700-e55a-4d15-8dfd-5921ae7786ca
execution_keys:
  - EXECUTION:MANUAL_RERUN:dab45700-e55a-4d15-8dfd-5921ae7786ca
test_evidence_ref:
  - src/test/java/com/agilerunner/api/service/review/ManualRerunQueryServiceTest.java
  - src/test/java/com/agilerunner/api/controller/review/ManualRerunControllerTest.java
  - src/test/java/com/agilerunner/api/service/review/ManualRerunServiceTest.java
  - src/test/java/com/agilerunner/client/agentruntime/AgentRuntimeRepositoryTest.java
  - ./scripts/gradlew-java21.sh --no-daemon cleanTest test --console=plain
diff_ref:
  - 없음
failure_summary: representative manual rerun 생성 후 같은 executionKey로 GET 조회한 응답과 H2 evidence가 같은 의미를 갖는지 확인해야 했다.
root_cause: query response 의미 연결은 끝났지만 실제 앱 기준으로 POST rerun 응답, GET query 응답, H2 evidence가 모두 같은 값을 보여 주는지 닫는 단계가 남아 있었다.
agents_check_findings:
  - representative 검증은 `manual rerun 생성 -> 같은 executionKey로 GET 조회 -> 앱 종료 -> H2 조회` 순서를 지켰다.
  - fresh execution key와 delivery id를 사용했고, 앱 종료 후 H2 조회를 수행했다.
  - 새 runtime 스키마 컬럼 추가 없이 기존 evidence 필드 정합성만 검증했다.
next_task_warnings:
  - 다음 spec에서도 rerun 관련 응답을 확장하면 representative executionKey 기준으로 응답과 H2 evidence 정합성을 다시 닫아야 한다.
  - 실제 GitHub 설정이 있는 환경에서는 success path representative 검증을 별도 보강할 수 있다.
error_signature: GITHUB_APP_CONFIGURATION_MISSING
test_result_summary: targeted test와 full cleanTest test 통과, actual app/H2 representative verification 통과
snapshot_hashes: []
---

# 작업 회고

## 작업 요약
- local profile 실제 앱을 기동해 fresh manual rerun 1건을 생성했다.
- rerun 응답으로 받은 `executionKey=EXECUTION:MANUAL_RERUN:dab45700-e55a-4d15-8dfd-5921ae7786ca`로 `GET /reviews/rerun/{executionKey}`를 호출했다.
- 앱 종료 후 H2 `WEBHOOK_EXECUTION`, `AGENT_EXECUTION_LOG`를 같은 key로 조회해 query 응답과 evidence 정합성을 확인했다.

## 실패 요약
- representative 실행 자체는 실패 시나리오였다.
- 원인은 기존과 동일하게 `GITHUB_APP_CONFIGURATION_MISSING`였고, 이 값이 rerun 응답, query 응답, H2 evidence에 모두 같은 의미로 남는지 확인하는 것이 이번 task의 핵심이었다.

## Root Cause
- query response 의미 연결만으로는 실제 앱 기준 정합성이 닫히지 않는다.
- 특히 manual rerun은 `POST /reviews/rerun` 응답과 `GET /reviews/rerun/{executionKey}` 응답, H2 evidence 세 지점이 모두 같은 값을 보여야 운영자가 추적에 쓸 수 있다.

## AGENTS 체크 결과
- targeted test와 full test를 순차 실행했다.
- representative 검증은 `manual rerun 생성 -> GET 조회 -> 앱 종료 -> H2 조회` 순서를 유지했다.
- representative `delivery_id`와 `execution_key`를 회고에 남겼다.
- 이번 task는 검증 중심이어서 추가 proposal 없이 기존 workflow 규칙 안에서 닫을 수 있었다.

## 근거 Artifact
- targeted test:
  - `ManualRerunQueryServiceTest`
  - `ManualRerunControllerTest`
  - `ManualRerunServiceTest`
  - `AgentRuntimeRepositoryTest`
- full test:
  - `./scripts/gradlew-java21.sh --no-daemon cleanTest test --console=plain`
- representative actual app/H2:
  - POST `/reviews/rerun` 응답:
    - `executionKey=EXECUTION:MANUAL_RERUN:dab45700-e55a-4d15-8dfd-5921ae7786ca`
    - `executionControlMode=DRY_RUN`
    - `writePerformed=false`
    - `executionStatus=FAILED`
    - `errorCode=GITHUB_APP_CONFIGURATION_MISSING`
    - `failureDisposition=MANUAL_ACTION_REQUIRED`
  - GET `/reviews/rerun/{executionKey}` 응답:
    - `200 OK`
    - 같은 `executionKey`
    - `DRY_RUN / false / FAILED / GITHUB_APP_CONFIGURATION_MISSING / MANUAL_ACTION_REQUIRED`
  - H2 `WEBHOOK_EXECUTION`:
    - `MANUAL_RERUN / FAILED / GITHUB_APP_CONFIGURATION_MISSING / MANUAL_ACTION_REQUIRED / DRY_RUN / FALSE`
  - H2 `AGENT_EXECUTION_LOG`:
    - `manual-rerun-accepted / SUCCEEDED / DRY_RUN / FALSE`
    - `review-generated / FAILED / GITHUB_APP_CONFIGURATION_MISSING / MANUAL_ACTION_REQUIRED / DRY_RUN / FALSE`

## 다음 Task 경고사항
- 없음
- `SPEC-0009`는 이번 task로 종료 가능하다.

## 제안 필요 여부
- 없음
- representative 검증은 기존 workflow 규칙을 그대로 적용해 닫을 수 있었고, 이번 task에서 새 규칙 부족으로 생긴 문제는 없었다.
- `SPEC-0009`에서 승인된 workflow 제안은 `TASK-0003`에서 나온 `WORKFLOW-PROP-0010` 1건이며, spec summary에서 함께 정리한다.
