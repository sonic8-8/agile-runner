---
spec_id: SPEC-0009
task_id: TASK-0002
github_issue_number: 33
criteria_keys:
  - manual-rerun-query-input-and-not-found-policy-defined
delivery_ids: []
execution_keys: []
test_evidence_ref:
  - src/test/java/com/agilerunner/api/controller/review/ManualRerunControllerTest.java
  - src/test/java/com/agilerunner/api/service/review/ManualRerunServiceTest.java
  - src/test/java/com/agilerunner/api/controller/GitHubWebhookControllerTest.java
diff_ref:
  - src/main/java/com/agilerunner/api/controller/review/ManualRerunController.java
  - src/main/java/com/agilerunner/api/controller/review/response/ManualRerunQueryResponse.java
  - src/main/java/com/agilerunner/api/controller/review/response/ManualRerunQueryNotFoundResponse.java
  - src/main/java/com/agilerunner/api/service/review/ManualRerunQueryService.java
  - src/main/java/com/agilerunner/api/service/review/request/ManualRerunQueryServiceRequest.java
  - src/main/java/com/agilerunner/api/service/review/response/ManualRerunQueryServiceResponse.java
  - src/main/java/com/agilerunner/domain/exception/ManualRerunQueryNotFoundException.java
failure_summary: executionKey 기반 조회 controller/service 진입점과 not found 정책이 없어 API 경계가 열려 있지 않았다.
root_cause: rerun 결과를 다시 읽는 기능은 필요해졌지만, 실제 runtime evidence 조회를 붙이기 전에 controller/service seam과 404 정책을 먼저 고정하는 단계가 비어 있었다.
agents_check_findings:
  - controller는 GET 진입점, 응답 변환, not found 예외 매핑만 담당하고 service가 placeholder not found 정책을 가진다.
  - TASK-0002에서는 runtime evidence 의미 매핑을 끌어오지 않고 placeholder 성공 응답과 not found 정책만 고정했다.
next_task_warnings:
  - TASK-0003은 ManualRerunQueryService의 placeholder not found를 runtime evidence 조회로 치환하되 공통 필드 의미를 rerun 응답과 맞춰야 한다.
  - TASK-0004에서 representative manual rerun 생성 후 같은 executionKey로 GET 조회와 H2 evidence 정합성을 닫아야 한다.
error_signature: NONE
test_result_summary: ManualRerunControllerTest, ManualRerunServiceTest, GitHubWebhookControllerTest targeted 통과, full cleanTest test 통과
snapshot_hashes: []
---

# 작업 회고

## 작업 요약
- `GET /reviews/rerun/{executionKey}` 진입점과 query service/request/response 경계를 추가했다.
- 성공 조회는 `executionKey`를 service request로 전달하고 placeholder 응답을 반환하도록 열었다.
- 존재하지 않는 `executionKey`는 `404 Not Found + executionKey + message` 정책으로 고정했다.

## 실패 요약
- 기능 결함은 없었다.
- 다만 첫 Tester 초안에서 실패 의미 필드 조합까지 고정해 `TASK-0003` 범위를 앞당긴 부분이 있었고, 3-agent 리뷰 후 placeholder 응답 수준으로 줄였다.

## Root Cause
- 조회 기능은 아직 runtime evidence 실제 조회를 붙이지 않는 단계인데, rerun 응답에서 이미 쓰는 실패 의미 필드를 그대로 검증하면 task 경계가 흐려질 수 있었다.
- 그래서 이번 단계는 입력 해석, controller/service seam, not found 정책까지만 닫고, 의미 매핑은 다음 task로 미루는 구성이 더 적절했다.

## AGENTS 체크 결과
- controller는 요청 수신, path variable 해석, service 호출, 응답/예외 매핑만 담당한다.
- service는 placeholder not found 정책만 담당하고 runtime evidence 실제 조회는 아직 하지 않는다.
- targeted test와 전체 test는 순차 실행했다.
- production code, `/webhook/github` orchestration, agent-runtime 저장 구조 변경이 아니어서 실제 앱/H2 대표 검증은 비대상으로 판단했다.

## 근거 Artifact
- targeted test:
  - `ManualRerunControllerTest`
  - `ManualRerunServiceTest`
  - `GitHubWebhookControllerTest`
- full test:
  - `./scripts/gradlew-java21.sh --no-daemon cleanTest test --console=plain`
- 핵심 검증:
  - `GET /reviews/rerun/{executionKey}` 성공 placeholder 응답
  - `404 Not Found + executionKey + message`
  - 기존 `POST /reviews/rerun`과 `/webhook/github` 회귀 없음

## 다음 Task 경고사항
- `TASK-0003`은 runtime evidence 조회와 응답 의미 연결만 닫아야 하며, not found 정책은 다시 흔들지 않아야 한다.
- query success 응답의 `executionStatus`, `errorCode`, `failureDisposition`, `writePerformed` 의미는 rerun 응답과 같은 해석으로 맞춰야 한다.

## 제안 필요 여부
- 없음
- 이번 교훈은 새 규칙 부족보다 task 경계를 더 엄격히 지키는 쪽에 가까웠다.
