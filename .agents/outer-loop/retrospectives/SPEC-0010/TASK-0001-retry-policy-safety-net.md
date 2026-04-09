---
spec_id: SPEC-0010
task_id: TASK-0001
github_issue_number: 36
criteria_keys:
  - manual-rerun-retry-contract-preserved
delivery_ids: []
execution_keys: []
test_evidence_ref:
  - src/test/java/com/agilerunner/api/service/review/ManualRerunQueryServiceTest.java
  - src/test/java/com/agilerunner/api/controller/review/ManualRerunControllerTest.java
  - src/test/java/com/agilerunner/api/service/review/ManualRerunServiceTest.java
  - src/test/java/com/agilerunner/domain/exception/FailureDispositionPolicyTest.java
  - src/test/java/com/agilerunner/api/controller/GitHubWebhookControllerTest.java
  - ./scripts/gradlew-java21.sh --no-daemon test --tests 'com.agilerunner.api.service.review.ManualRerunQueryServiceTest' --tests 'com.agilerunner.api.controller.review.ManualRerunControllerTest' --tests 'com.agilerunner.api.service.review.ManualRerunServiceTest' --tests 'com.agilerunner.domain.exception.FailureDispositionPolicyTest' --tests 'com.agilerunner.api.controller.GitHubWebhookControllerTest' --console=plain
  - ./scripts/gradlew-java21.sh --no-daemon cleanTest test --console=plain
diff_ref:
  - src/test/java/com/agilerunner/api/service/review/ManualRerunQueryServiceTest.java
failure_summary: 기존 안전망은 rerun/query/webhook 계약과 failure disposition 의미는 이미 잘 커버했지만, manual rerun query가 webhook execution을 조회 대상으로 오인하지 않는 경계가 비어 있었다.
root_cause: `ManualRerunQueryService`는 `ExecutionStartType.MANUAL_RERUN` 필터를 갖고 있었지만, 이를 직접 고정하는 safety-net 테스트가 없어서 다음 spec에서 조회 범위 경계가 흔들릴 여지가 있었다.
agents_check_findings:
  - 기존 rerun, query, webhook 회귀 테스트와 failure disposition 정책 테스트를 먼저 재점검했다.
  - production code 변경 없이 `manual rerun이 아닌 execution은 조회 대상이 아님` 테스트 1건만 최소 보강했다.
  - runtime 저장 구조, controller orchestration, representative actual app/H2 검증은 이번 task 범위에 포함하지 않았다.
next_task_warnings:
  - `TASK-0002`는 policy 또는 service 판단까지만 닫고 `409 Conflict` controller 계약은 `TASK-0003`에서 고정해야 한다.
  - retry 입력의 `selectedPaths` 공백 의미는 전체 실행으로 해석한다는 문서 기준을 구현 단계에서도 그대로 유지해야 한다.
error_signature: 없음
test_result_summary: targeted test와 full cleanTest test 통과, actual app/H2 representative 검증은 production/runtime 변경이 없는 safety-net task라서 생략
snapshot_hashes: []
---

# 작업 회고

## 작업 요약
- `SPEC-0010` 시작 전에 기존 rerun/query/webhook 계약과 failure disposition 의미가 재시도 기능 추가에도 충분한지 다시 점검했다.
- 기존 테스트 집합 위에 `manual rerun이 아닌 execution key는 조회 대상이 아니므로 not found 예외를 던진다`는 안전망 테스트 1건만 추가했다.
- production code 변경 없이 현재 query 범위 경계를 먼저 고정했다.

## 실패 요약
- 실제 기능 실패는 없었다.
- 다만 `ManualRerunQueryService`의 조회 범위가 manual rerun execution만 대상으로 제한된다는 점을 직접 고정하는 테스트가 비어 있었다.

## Root Cause
- 기존 테스트는 rerun 응답 계약, query 응답 계약, webhook 비영향, failure disposition 정책은 잘 다루고 있었지만, query 대상이 `ExecutionStartType.MANUAL_RERUN`에 한정된다는 경계는 직접 검증하지 않았다.

## AGENTS 체크 결과
- Tester 1차는 production code 변경 없이 black-box/service 테스트 1건만 보강하는 범위로 고정했다.
- targeted test와 전체 테스트를 순차 실행했다.
- 이번 task는 safety-net 보강만 수행했기 때문에 실제 앱/H2 representative 검증은 생략했고, 그 사유를 회고에 남겼다.

## 근거 Artifact
- targeted test:
  - `ManualRerunQueryServiceTest`
  - `ManualRerunControllerTest`
  - `ManualRerunServiceTest`
  - `FailureDispositionPolicyTest`
  - `GitHubWebhookControllerTest`
- full test:
  - `./scripts/gradlew-java21.sh --no-daemon cleanTest test --console=plain`
- 추가 안전망:
  - `ManualRerunQueryServiceTest.find_throwsNotFoundWhenExecutionIsNotManualRerun`

## 다음 Task 경고사항
- `TASK-0002`는 retry 허용/거부 정책을 policy/service 수준으로 먼저 닫고, `409 Conflict` controller 계약은 `TASK-0003`로 넘겨야 한다.
- `selectedPaths` 공백 의미를 source 경로 재사용이 아니라 전체 실행으로 해석하는 정책이 구현에서 흔들리지 않게 주의해야 한다.

## 제안 필요 여부
- 없음
- 이번 task의 교훈은 새 규칙 부족이 아니라, 기존 safety-net 기준을 manual rerun query 범위까지 한 단계 더 명시한 수준이었다.
