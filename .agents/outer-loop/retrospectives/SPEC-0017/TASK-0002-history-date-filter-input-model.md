---
spec_id: SPEC-0017
task_id: TASK-0002
github_issue_number: 65
criteria_keys:
  - manual-rerun-control-history-date-filter-input-defined
delivery_ids: []
execution_keys: []
test_evidence_ref:
  - "targeted: ./scripts/gradlew-java21.sh --no-daemon test --tests 'com.agilerunner.api.controller.review.ManualRerunControllerTest' --tests 'com.agilerunner.api.service.review.ManualRerunControlActionHistoryServiceTest' --console=plain"
  - "full: ./scripts/gradlew-java21.sh --no-daemon cleanTest test --console=plain"
diff_ref: "git diff -- src/main/java/com/agilerunner/api/controller/review/request/ManualRerunControlActionHistoryRequest.java src/main/java/com/agilerunner/api/service/review/request/ManualRerunControlActionHistoryServiceRequest.java src/test/java/com/agilerunner/api/controller/review/ManualRerunControllerTest.java src/test/java/com/agilerunner/api/service/review/ManualRerunControlActionHistoryServiceTest.java .agents/outer-loop/retrospectives/SPEC-0017/TASK-0002-history-date-filter-input-model.md .agents/outer-loop/registry.json"
failure_summary: "history 조회 경계가 아직 executionKey, action, actionStatus만 읽고 있어 기간 필터 입력을 이후 selection 단계로 전달할 수 없었다."
root_cause: "관리자 액션 이력 조회는 기존에 최소 필터까지만 지원했고, controller request와 service request에 기간 필터 입력 모델이 없었다."
agents_check_findings:
  - "이번 task는 controller/service 입력 경계만 여는 단계로 유지했고, 실제 audit row 기간 필터 selection은 TASK-0003으로 넘겼다."
  - "targeted test와 full cleanTest test를 순차 실행했다."
  - "실제 앱/H2 representative 검증은 spec 문서상 TASK-0004 범위이므로 이번 task에서는 수행하지 않았다."
next_task_warnings:
  - "TASK-0003는 appliedAtFrom, appliedAtTo 필터를 실제 audit selection에 연결해야 한다."
  - "기간 필터가 비어 있을 때 기존 전체 timeline 의미는 그대로 유지해야 한다."
  - "없는 execution은 기존 not-found 의미를 유지하고, execution은 있지만 기간 필터 결과 0건이면 빈 timeline을 반환하는 경계를 분리해야 한다."
error_signature: "NONE"
test_result_summary: "controller/service black-box 테스트와 full cleanTest test가 모두 통과했고, history 조회가 기간 필터 입력을 읽되 아직 timeline 의미를 바꾸지 않는 경계를 확보했다."
snapshot_hashes: []
---

# 작업 회고

## 작업 요약
- history 조회 controller 경계에 `appliedAtFrom`, `appliedAtTo` query param 입력 모델을 추가했다.
- service request가 같은 기간 필터 값을 보존하도록 확장했다.
- 기간 필터가 비어 있으면 기존 전체 timeline 의미를 유지하는 현재 단계 경계를 테스트로 고정했다.

## 실패 요약
- 기능 구현 실패는 없었다.
- 이번 task의 핵심 위험은 입력 경계를 열면서 실제 기간 필터 selection까지 앞당겨 TASK-0003 범위를 침범하는 것이었다.

## Root Cause
- 기존 history 조회는 `executionKey`, `action`, `actionStatus`까지만 받아 전체 timeline 또는 기존 최소 필터 조회만 수행했다.
- 기간 필터 입력 모델이 controller/service 경계에 없어서 이후 task에서 selection을 연결할 준비가 되어 있지 않았다.

## AGENTS 체크 결과
- `PRD -> Spec -> ValidationCriteria -> Task -> Issue` 흐름에 맞춰 `#65`를 1:1 issue로 연결했다.
- Tester는 production code 수정 없이 controller/service black-box 테스트를 먼저 추가했다.
- targeted test와 full `cleanTest test`를 순차 실행했다.
- 실제 앱/H2 representative 검증은 spec 문서상 `TASK-0004` 범위이므로 이번 task에서는 수행하지 않고 회고에 남겼다.

## 근거 Artifact
- `.agents/active/spec.md`
- `.agents/active/tasks.md`
- `.agents/criteria/SPEC-0017-admin-control-history-date-filter.json`
- `src/main/java/com/agilerunner/api/controller/review/request/ManualRerunControlActionHistoryRequest.java`
- `src/main/java/com/agilerunner/api/service/review/request/ManualRerunControlActionHistoryServiceRequest.java`
- `src/test/java/com/agilerunner/api/controller/review/ManualRerunControllerTest.java`
- `src/test/java/com/agilerunner/api/service/review/ManualRerunControlActionHistoryServiceTest.java`

## 다음 Task 경고사항
- 다음 task는 입력 모델을 실제 audit row selection에 연결하는 단계로만 제한해야 한다.
- 기간 필터가 비어 있으면 전체 timeline 의미가 유지되어야 한다.
- 없는 execution과 필터 결과 0건은 실제 selection 단계에서 다르게 해석해야 한다.
- representative actual app/H2 검증은 `TASK-0004`에서 같은 execution 기준으로 수행해야 한다.

## 제안 필요 여부
- 없음
