---
spec_id: SPEC-0018
task_id: TASK-0002
github_issue_number: 69
criteria_keys:
  - manual-rerun-control-history-order-page-input-defined
delivery_ids: []
execution_keys: []
test_evidence_ref:
  - "targeted: ./scripts/gradlew-java21.sh --no-daemon test --tests 'com.agilerunner.api.controller.review.ManualRerunControllerTest' --tests 'com.agilerunner.api.controller.review.request.ManualRerunControlActionHistoryRequestTest' --tests 'com.agilerunner.api.service.review.ManualRerunControlActionHistoryServiceTest' --console=plain"
  - "full: ./scripts/gradlew-java21.sh --no-daemon cleanTest test --console=plain"
diff_ref: "git diff -- src/main/java/com/agilerunner/domain/review/ManualRerunControlActionHistorySortDirection.java src/main/java/com/agilerunner/api/controller/review/request/ManualRerunControlActionHistoryRequest.java src/main/java/com/agilerunner/api/service/review/request/ManualRerunControlActionHistoryServiceRequest.java src/test/java/com/agilerunner/api/controller/review/ManualRerunControllerTest.java src/test/java/com/agilerunner/api/controller/review/request/ManualRerunControlActionHistoryRequestTest.java .agents/outer-loop/retrospectives/SPEC-0018/TASK-0002-history-order-page-input-model.md .agents/outer-loop/registry.json"
failure_summary: "정렬과 페이지 기준을 실제 selection에 붙이기 전에 history 조회 경계가 sortDirection, pageSize, cursorAppliedAt를 정확히 읽는지 먼저 고정할 필요가 있었다."
root_cause: "이번 spec은 입력 해석 단계와 audit selection 단계를 나눠 진행하므로, TASK-0002에서는 request 경계와 DESC 기본 해석만 닫고 selection 로직은 의도적으로 TASK-0003으로 넘겨야 했다."
agents_check_findings:
  - "controller request와 service request만 변경하고 실제 audit selection은 건드리지 않아 TASK-0002 범위를 지켰다."
  - "sortDirection이 비어 있고 pageSize 또는 cursorAppliedAt만 있을 때 DESC 기본 해석을 service request 정규화로 고정했다."
  - "이번 task는 production runtime 저장 구조와 representative 검증 경로를 바꾸지 않으므로 실제 앱/H2 검증 비대상이다."
  - "targeted test와 full cleanTest test를 순차 실행했다."
next_task_warnings:
  - "TASK-0003은 sortDirection, pageSize, cursorAppliedAt를 실제 audit selection에 반영해야 한다."
  - "입력이 모두 비어 있을 때는 기존 전체 timeline 의미를 유지해야 한다."
  - "cursorAppliedAt 배타 경계와 같은 appliedAt row 제외 기준은 TASK-0003에서 실제 selection으로 닫아야 한다."
error_signature: "NONE"
test_result_summary: "controller/request 경계 테스트와 full cleanTest test가 통과했고, history 조회 입력 모델과 DESC 기본 해석이 고정됐다."
snapshot_hashes: []
---

# 작업 회고

## 작업 요약
- history 조회에 `sortDirection`, `pageSize`, `cursorAppliedAt` 입력 경계를 추가했다.
- `pageSize` 또는 `cursorAppliedAt`만 있고 `sortDirection`이 비어 있으면 `DESC`로 해석하는 기본 규칙을 service request에 고정했다.
- selection 로직은 아직 건드리지 않고, request 경계와 최소 응답 의미만 닫았다.

## 실패 요약
- 기능 실패는 없었다.
- 핵심 위험은 입력 모델 단계에서 selection 로직까지 끌어와 task 경계를 무너뜨리거나, 반대로 기본 정렬 방향이 모호하게 남는 것이었다.

## Root Cause
- 정렬과 page window는 실제 selection 단계와 입력 해석 단계를 분리해야 이후 representative 검증이 흔들리지 않는다.
- `sortDirection` 기본 해석이 문서에만 있고 request 경계에서 바로 정규화되지 않으면 다음 task의 black-box 판정이 모호해진다.

## AGENTS 체크 결과
- `PRD -> Spec -> ValidationCriteria -> Task -> Issue` 순서를 맞춰 `#69`를 연결했다.
- Tester 1차는 controller/request 경계에만 집중했고, selection 로직은 다음 task로 넘겼다.
- targeted test와 full test를 순차 실행했다.
- 실제 앱/H2 representative 검증은 runtime 저장 구조와 selection 결과를 아직 바꾸지 않은 task라 비대상으로 두고 회고에 남겼다.

## 근거 Artifact
- `src/main/java/com/agilerunner/domain/review/ManualRerunControlActionHistorySortDirection.java`
- `src/main/java/com/agilerunner/api/controller/review/request/ManualRerunControlActionHistoryRequest.java`
- `src/main/java/com/agilerunner/api/service/review/request/ManualRerunControlActionHistoryServiceRequest.java`
- `src/test/java/com/agilerunner/api/controller/review/ManualRerunControllerTest.java`
- `src/test/java/com/agilerunner/api/controller/review/request/ManualRerunControlActionHistoryRequestTest.java`
- `src/test/java/com/agilerunner/api/service/review/ManualRerunControlActionHistoryServiceTest.java`

## 다음 Task 경고사항
- `TASK-0003`은 입력 해석을 넘어 실제 audit selection에 정렬과 page window를 반영해야 한다.
- `cursorAppliedAt`는 배타 경계이고 같은 `appliedAt` row는 다음 window에서 제외하는 기준을 실제 repository/service selection으로 닫아야 한다.
- representative 검증은 `TASK-0004`에서 수행하되, `TASK-0003`에서는 selection 의미를 controller/service/repository black-box 테스트로 충분히 고정해야 한다.

## 제안 필요 여부
- 없음
