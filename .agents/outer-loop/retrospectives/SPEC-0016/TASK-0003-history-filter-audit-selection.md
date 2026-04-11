---
spec_id: SPEC-0016
task_id: TASK-0003
github_issue_number: 62
criteria_keys:
  - manual-rerun-control-history-filter-maps-audit-selection
delivery_ids: []
execution_keys: []
test_evidence_ref:
  - "targeted: ./scripts/gradlew-java21.sh --no-daemon test --tests 'com.agilerunner.api.controller.review.ManualRerunControllerTest' --tests 'com.agilerunner.api.service.review.ManualRerunControlActionHistoryServiceTest' --tests 'com.agilerunner.client.agentruntime.AgentRuntimeRepositoryTest' --console=plain"
  - "full: ./scripts/gradlew-java21.sh --no-daemon cleanTest test --console=plain"
diff_ref: "git diff -- src/main/java/com/agilerunner/api/service/review/ManualRerunControlActionHistoryService.java src/main/java/com/agilerunner/client/agentruntime/AgentRuntimeRepository.java src/test/java/com/agilerunner/api/service/review/ManualRerunControlActionHistoryServiceTest.java src/test/java/com/agilerunner/client/agentruntime/AgentRuntimeRepositoryTest.java .agents/outer-loop/retrospectives/SPEC-0016/TASK-0003-history-filter-audit-selection.md .agents/outer-loop/registry.json"
failure_summary: "history 조회가 action, actionStatus 필터를 읽더라도 실제 audit row selection은 executionKey 기준 전체 timeline만 반환하고 있었다."
root_cause: "controller와 service 입력 경계는 TASK-0002에서 열었지만, repository selection seam은 여전히 executionKey 단일 조건만 지원해 필터 의미가 실제 row 선택으로 내려가지 못했다."
agents_check_findings:
  - "이번 task는 service와 repository selection seam까지만 닫고, representative actual app/H2 검증은 TASK-0004로 남겼다."
  - "targeted test와 full cleanTest test를 순차 실행했다."
  - "controller 경계는 기존 GET history 계약을 유지하고, service/repository black-box 테스트로 필터 selection 의미를 고정했다."
next_task_warnings:
  - "TASK-0004는 필터 없는 history, 필터 적용 history, H2 audit evidence를 같은 execution 기준으로 실제 앱에서 검증해야 한다."
  - "대표 검증은 action history를 두 건 이상 남긴 execution으로 수행해야 필터 선택 결과 차이를 분명히 볼 수 있다."
  - "필터 적용 history가 0건일 때도 기존 history 조회 의미를 깨뜨리지 않는지 representative 검증에서 함께 확인해야 한다."
error_signature: "NONE"
test_result_summary: "service/repository selection seam 연결 후 targeted test와 full cleanTest test가 모두 통과했고, no-filter 전체 timeline과 filtered selection 의미를 함께 고정했다."
snapshot_hashes: []
---

# 작업 회고

## 작업 요약
- history 조회 service가 `action`, `actionStatus` 필터를 실제 repository selection으로 전달하도록 연결했다.
- repository가 `executionKey`, `action`, `actionStatus` 조합으로 audit row를 선택하도록 SQL과 API를 확장했다.
- no-filter 전체 timeline 의미와 filtered selection 의미를 service/repository 테스트로 함께 고정했다.

## 실패 요약
- 기존 구현은 필터 입력을 읽더라도 실제 audit row 선택은 바꾸지 못했다.
- 이번 task의 핵심 위험은 필터 입력 seam을 열어 놓고도 history 응답이 계속 전체 timeline을 반환해 criteria를 만족하지 못하는 것이었다.

## Root Cause
- TASK-0002는 controller/service 입력 모델만 여는 단계였기 때문에, 실제 row selection을 수행할 repository overload가 아직 없었다.
- 이력 조회 service도 새 필터 값을 selection seam으로 넘기지 않아, 입력 의미와 응답 의미가 분리된 상태였다.

## AGENTS 체크 결과
- `PRD -> Spec -> ValidationCriteria -> Task -> Issue` 흐름에 맞춰 `#62`를 1:1 issue로 연결했다.
- Tester는 production code를 수정하지 않고 service/repository black-box 기준을 먼저 고정했다.
- targeted test와 full `cleanTest test`를 순차 실행했다.
- representative actual app/H2 검증은 spec 문서상 `TASK-0004` 범위로 남겨 두고, 그 이유와 남은 위험을 회고에 기록했다.

## 근거 Artifact
- `.agents/active/tasks.md`
- `.agents/criteria/SPEC-0016-admin-control-history-filter.json`
- `src/main/java/com/agilerunner/api/service/review/ManualRerunControlActionHistoryService.java`
- `src/main/java/com/agilerunner/client/agentruntime/AgentRuntimeRepository.java`
- `src/test/java/com/agilerunner/api/service/review/ManualRerunControlActionHistoryServiceTest.java`
- `src/test/java/com/agilerunner/client/agentruntime/AgentRuntimeRepositoryTest.java`
- `src/test/java/com/agilerunner/api/controller/review/ManualRerunControllerTest.java`

## 다음 Task 경고사항
- representative 검증은 같은 execution에 여러 audit row를 남긴 뒤 무필터 history와 필터 적용 history를 모두 조회해야 의미 차이가 드러난다.
- 필터 적용 결과가 없는 경우도 representative 검증에 포함해 현재 응답 구조가 유지되는지 확인해야 한다.
- 실제 앱/H2 검증은 `앱 기동 -> representative 실행/액션 -> history 무필터 조회 -> history 필터 조회 -> 앱 종료 -> H2 조회` 순서를 따라야 한다.

## 제안 필요 여부
- 없음
