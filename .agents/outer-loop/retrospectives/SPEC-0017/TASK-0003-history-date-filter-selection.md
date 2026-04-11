---
spec_id: SPEC-0017
task_id: TASK-0003
github_issue_number: 66
criteria_keys:
  - manual-rerun-control-history-date-filter-maps-audit-selection
delivery_ids: []
execution_keys: []
test_evidence_ref:
  - "targeted: ./scripts/gradlew-java21.sh --no-daemon test --tests 'com.agilerunner.api.controller.review.ManualRerunControllerTest' --tests 'com.agilerunner.api.service.review.ManualRerunControlActionHistoryServiceTest' --tests 'com.agilerunner.client.agentruntime.AgentRuntimeRepositoryTest' --console=plain"
  - "full: ./scripts/gradlew-java21.sh --no-daemon cleanTest test --console=plain"
diff_ref: "git diff -- src/main/java/com/agilerunner/api/service/review/ManualRerunControlActionHistoryService.java src/main/java/com/agilerunner/client/agentruntime/AgentRuntimeRepository.java src/test/java/com/agilerunner/api/service/review/ManualRerunControlActionHistoryServiceTest.java src/test/java/com/agilerunner/client/agentruntime/AgentRuntimeRepositoryTest.java .agents/outer-loop/retrospectives/SPEC-0017/TASK-0003-history-date-filter-selection.md .agents/outer-loop/registry.json"
failure_summary: "history 조회가 기간 필터 입력을 받더라도 실제 audit row selection에는 반영되지 않아, 운영자가 시간 범위로 좁혀 읽을 수 없었다."
root_cause: "controller/service 입력 경계는 TASK-0002에서 열렸지만, repository SQL과 service selection seam이 여전히 action/actionStatus까지만 반영하고 있었다."
agents_check_findings:
  - "기간 필터 selection 연결은 service/repository seam까지만 닫고, representative actual app/H2 검증은 TASK-0004로 남겼다."
  - "없는 execution은 기존 not-found 의미를 유지하고, execution은 있지만 기간 필터 결과가 0건이면 빈 timeline을 반환하도록 테스트를 고정했다."
  - "targeted test와 full cleanTest test를 순차 실행했다."
next_task_warnings:
  - "TASK-0004에서는 무필터 history와 기간 필터 history, H2 audit evidence가 같은 execution 기준으로 일치하는지 실제 앱에서 검증해야 한다."
  - "기간 필터 대표 검증은 appliedAtFrom만 있는 경우, appliedAtTo만 있는 경우, 둘 다 있는 경우를 나눠 확인하는 편이 안전하다."
error_signature: "NONE"
test_result_summary: "controller/service/repository black-box 테스트와 full cleanTest test가 모두 통과했고, 기간 필터 selection seam과 빈 timeline/not-found 의미 분리가 고정됐다."
snapshot_hashes: []
---

# 작업 회고

## 작업 요약
- history 조회 service가 `appliedAtFrom`, `appliedAtTo`를 repository selection으로 전달하도록 연결했다.
- repository SQL이 기간 필터를 optional 조건으로 해석해 무필터, 시작 시각만, 종료 시각만, 범위 필터 selection을 지원하도록 정리했다.
- execution은 있지만 기간 필터 결과가 0건이면 빈 timeline을 반환하고, 없는 execution은 기존 not-found 의미를 유지하도록 테스트를 고정했다.

## 실패 요약
- 기능 구현 실패는 없었다.
- 이번 task의 핵심 위험은 기간 필터 selection을 연결하면서 `TASK-0004`의 representative actual app/H2 검증 범위를 앞당겨 끌어오는 것이었다.

## Root Cause
- TASK-0002까지는 기간 필터 입력만 열려 있었고, 실제 audit row 선택은 기존 action/actionStatus 필터에만 묶여 있었다.
- 운영자가 시간 범위로 history를 읽게 하려면 service와 repository가 같은 기간 필터 의미를 공유해야 했다.

## AGENTS 체크 결과
- `PRD -> Spec -> ValidationCriteria -> Task -> Issue` 흐름에 맞춰 `#66`을 1:1 issue로 연결했다.
- Tester는 controller/service/repository black-box 테스트를 먼저 추가했고, Constructor는 selection seam만 수정했다.
- targeted test와 full `cleanTest test`를 순차 실행했다.
- representative actual app/H2 검증은 spec 문서상 `TASK-0004` 범위라 이번 task에서는 수행하지 않고 회고에 남겼다.

## 근거 Artifact
- `src/main/java/com/agilerunner/api/service/review/ManualRerunControlActionHistoryService.java`
- `src/main/java/com/agilerunner/client/agentruntime/AgentRuntimeRepository.java`
- `src/test/java/com/agilerunner/api/service/review/ManualRerunControlActionHistoryServiceTest.java`
- `src/test/java/com/agilerunner/client/agentruntime/AgentRuntimeRepositoryTest.java`
- `src/test/java/com/agilerunner/api/controller/review/ManualRerunControllerTest.java`

## 다음 Task 경고사항
- representative actual app/H2 검증에서는 무필터 history와 기간 필터 history를 같은 execution 기준으로 비교해야 한다.
- 기간 필터 결과 0건과 execution not-found를 실제 앱 경계에서도 분리해 확인해야 한다.
- history 조회는 timeline 역할을 유지하고, 현재 상태 요약은 query/list에 두는 경계를 계속 유지해야 한다.

## 제안 필요 여부
- 없음
