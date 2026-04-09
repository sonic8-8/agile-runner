---
spec_id: SPEC-0010
task_id: TASK-0003
github_issue_number: 38
criteria_keys:
  - manual-rerun-retry-request-reuses-source-context
delivery_ids: []
execution_keys: []
test_evidence_ref:
  - src/test/java/com/agilerunner/api/controller/review/ManualRerunControllerTest.java
  - src/test/java/com/agilerunner/api/service/review/ManualRerunRetryServiceTest.java
  - src/test/java/com/agilerunner/api/service/review/ManualRerunQueryServiceTest.java
  - src/test/java/com/agilerunner/AgileRunnerApplicationTests.java
  - ./scripts/gradlew-java21.sh --no-daemon test --tests 'com.agilerunner.api.controller.review.ManualRerunControllerTest' --tests 'com.agilerunner.api.service.review.ManualRerunRetryServiceTest' --tests 'com.agilerunner.api.service.review.ManualRerunQueryServiceTest' --tests 'com.agilerunner.AgileRunnerApplicationTests' --console=plain
  - ./scripts/gradlew-java21.sh --no-daemon cleanTest test --console=plain
diff_ref:
  - src/main/java/com/agilerunner/api/controller/review/ManualRerunController.java
  - src/main/java/com/agilerunner/api/controller/review/request/ManualRerunRetryRequest.java
  - src/main/java/com/agilerunner/api/controller/review/response/ManualRerunRetryResponse.java
  - src/main/java/com/agilerunner/api/controller/review/response/ManualRerunRetryNotFoundResponse.java
  - src/main/java/com/agilerunner/api/controller/review/response/ManualRerunRetryConflictResponse.java
  - src/main/java/com/agilerunner/api/service/review/ManualRerunRetryService.java
  - src/main/java/com/agilerunner/api/service/review/request/ManualRerunRetryServiceRequest.java
  - src/main/java/com/agilerunner/api/service/review/response/ManualRerunRetryServiceResponse.java
  - src/main/java/com/agilerunner/domain/exception/ManualRerunRetryNotFoundException.java
  - src/main/java/com/agilerunner/domain/exception/ManualRerunRetryConflictException.java
  - src/test/java/com/agilerunner/api/controller/review/ManualRerunControllerTest.java
  - src/test/java/com/agilerunner/api/service/review/ManualRerunRetryServiceTest.java
failure_summary: retry 가능 여부 정책은 도입됐지만 실제 요청 경로가 없어 운영자가 source execution을 기준으로 새 수동 재실행을 시작할 수 없었다.
root_cause: 기존 구조는 `POST /reviews/rerun`와 `GET /reviews/rerun/{executionKey}`까지만 있었고, source execution 조회와 policy 판정을 묶는 retry 전용 controller/service 경계가 없었다.
agents_check_findings:
  - retry endpoint는 `TASK-0003` 범위에 맞게 controller/service/DTO/예외 경계까지만 열었다.
  - `selectedPaths` 공백은 source selection 재사용이 아니라 전체 실행 의미로 유지했다.
  - 조건부 bean인 `AgentRuntimeRepository` 의존성 추가에 맞춰 기본 컨텍스트 기동 근거를 함께 확인했다.
  - representative actual app/H2 검증과 runtime relation 적재는 `TASK-0004`로 남겼다.
next_task_warnings:
  - `TASK-0004`는 retry 응답의 `retrySourceExecutionKey`와 runtime evidence 관계를 actual app/H2 기준으로 닫아야 한다.
  - representative 검증은 fresh source execution과 fresh delivery/execution key를 사용해야 한다.
error_signature: 없음
test_result_summary: controller/service targeted test와 기본 컨텍스트 기동, full cleanTest test 통과. actual app/H2 representative 검증은 runtime relation 단계가 아니라서 생략
snapshot_hashes: []
---

# 작업 회고

## 작업 요약
- `POST /reviews/rerun/{executionKey}/retry` 경로를 추가했다.
- source execution을 조회해 재시도 가능 여부 정책을 적용하고, 허용되면 기존 `ManualRerunService` 흐름으로 새 수동 재실행을 시작하도록 연결했다.
- `404 Not Found`, `409 Conflict`, 성공 응답의 최소 계약을 controller/service black-box 테스트로 고정했다.

## 실패 요약
- 실제 기능 실패는 없었다.
- 다만 조건부 bean인 `AgentRuntimeRepository` 의존성을 추가한 뒤 기본 컨텍스트 기동이 한 번 깨졌고, service 생성자를 단일화해 해결했다.

## Root Cause
- retry 경로를 도입하면서 `AgentRuntimeRepository`가 새 service 의존성으로 들어왔는데, 초기 구현에 생성자가 둘이라 Spring이 기본 주입 경로를 결정하지 못했다.

## AGENTS 체크 결과
- controller는 HTTP 경계와 예외 응답만 담당하고, retry 가능 여부 판단과 source context 재사용은 service로 분리했다.
- targeted test와 전체 테스트를 순차 실행했다.
- actual app/H2 representative 검증은 runtime relation 적재를 다루는 `TASK-0004` 범위라서 이번 task에서는 생략했다.

## 근거 Artifact
- targeted test:
  - `ManualRerunControllerTest`
  - `ManualRerunRetryServiceTest`
  - `ManualRerunQueryServiceTest`
  - `AgileRunnerApplicationTests`
- full test:
  - `./scripts/gradlew-java21.sh --no-daemon cleanTest test --console=plain`

## 다음 Task 경고사항
- `TASK-0004`는 retry 응답의 `retrySourceExecutionKey`와 새 runtime execution 관계를 H2 evidence까지 같은 의미로 닫아야 한다.
- representative 검증은 source execution 준비와 retry 요청을 fresh key로 분리해서 수행해야 한다.

## 제안 필요 여부
- 없음
- 이번 task의 교훈은 새 workflow 규칙보다, 이미 반영된 조건부 bean 의존성 검증 규칙과 actual app/H2 분리 원칙을 제대로 적용한 수준이었다.
