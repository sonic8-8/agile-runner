---
spec_id: SPEC-0022
task_id: TASK-0002
github_issue_number: 85
criteria_keys:
  - manual-rerun-fixture-naming-rules-defined
delivery_ids: []
execution_keys: []
test_evidence_ref:
  - "targeted: ./scripts/gradlew-java21.sh --no-daemon test --console=plain --tests 'com.agilerunner.api.controller.review.ManualRerunResponseGuideFixtureTest' --tests 'com.agilerunner.api.controller.review.ManualRerunControllerTest' --tests 'com.agilerunner.api.service.review.ManualRerunServiceTest' --tests 'com.agilerunner.api.service.review.ManualRerunRetryServiceTest' --tests 'com.agilerunner.api.service.review.ManualRerunQueryServiceTest' --tests 'com.agilerunner.api.service.review.ManualRerunExecutionListServiceTest' --tests 'com.agilerunner.api.service.review.ManualRerunControlActionHistoryServiceTest' --tests 'com.agilerunner.api.service.review.ManualRerunControlActionServiceTest'"
  - "full: ./scripts/gradlew-java21.sh --no-daemon cleanTest test --console=plain"
diff_ref: "working-tree:docs/manual-rerun-response-guide.md,src/test/resources/manual-rerun-response-guide/,src/test/java/com/agilerunner/api/controller/review/ManualRerunResponseGuideFixtureTest.java,.agents/outer-loop/retrospectives/SPEC-0022/TASK-0002-fixture-naming-rules.md,.agents/outer-loop/registry.json"
failure_summary: "새 runtime failure는 없었다. list 관련 기준 파일 이름이 서로 비대칭이어서 naming 규칙을 문서로 읽기 어려웠고, 이를 파일 이름과 guide 설명, 테스트 경로를 함께 맞추는 방식으로 정리했다."
root_cause: "기준 파일이 점진적으로 늘어나면서 `rerun-list-after-acknowledge.json`과 `retry-list-row.json`처럼 list 예시 파일 이름 규칙이 완전히 맞지 않았다. 이름 규칙과 파일 단위 기준이 문서로 고정돼 있지 않아 새 작업자가 패턴을 추론해야 했다."
agents_check_findings:
  - "문서 경계 리뷰: 갱신 절차와 대표 검증 경계는 TASK-0003으로 남기고, 이번 task를 이름 규칙과 파일 단위 기준 정리로만 닫은 구성이 적절하다는 PASS"
  - "검증 리뷰: guide의 기준 파일 경로와 ManualRerunResponseGuideFixtureTest fixture 경로가 모두 일치하고, list row 파일 단위 기준이 테스트 구조와 맞는다는 PASS"
  - "운영자 가독성 리뷰: '실행 종류 - 응답 종류 - 시점' 규칙과 list row 파일 단위 기준이 새 작업자에게 바로 읽히는 수준이라는 PASS"
next_task_warnings:
  - "TASK-0003에서는 naming 규칙을 넘어서 guide, 기준 파일, 자동 검증 테스트를 어떤 순서로 함께 수정해야 하는지 절차를 분명히 적을 것"
  - "대표 실제 앱 검증 결과를 언제 기준 파일로 바로 옮기지 않는지 경계 문장은 TASK-0003에서 다루고, TASK-0002 회고에는 naming과 파일 단위 기준만 남길 것"
error_signature: "NONE"
test_result_summary: "기준 파일 이름 변경 후 ManualRerunResponseGuideFixtureTest 포함 targeted test green, full cleanTest test green"
snapshot_hashes: []
---

# 작업 회고

## 작업 요약
- 운영용 조회 응답 guide에 `기준 파일 이름 규칙`과 `파일 단위 기준` 섹션을 추가했다.
- list 관련 기준 파일 두 개의 이름을 더 일관된 형태로 맞췄다.
  - `rerun-list-row-after-acknowledge.json`
  - `retry-list-row-after-retry.json`
- `ManualRerunResponseGuideFixtureTest`의 fixture 경로도 새 이름으로 함께 정리했다.

## 실패 요약
- 코드나 테스트 실패는 없었다.
- 1차 리뷰에서는 guide 안에 갱신 절차까지 같이 읽힐 수 있는 문장이 남아 있어, 현재 task 범위보다 한 단계 앞선다는 지적이 있었다.

## Root Cause
- `SPEC-0021`에서 drift 검증까지 닫은 뒤 guide와 기준 파일은 연결됐지만, naming 규칙과 파일 단위 기준 자체는 별도 문서 규칙으로 고정되지 않았다.
- 그 상태에서 list 관련 파일 이름이 서로 다르게 읽히면서 새 작업자가 패턴을 추론해야 했다.

## AGENTS 체크 결과
- linked issue `#85`는 TASK-0002와 1:1 범위를 유지했다.
- targeted test와 full cleanTest test를 순차 실행했다.
- 이번 task는 production code, controller orchestration, runtime 저장 구조 변경이 없어서 actual app/H2 대표 검증은 비대상으로 유지했다.
- 서로 다른 관점의 3개 리뷰어가 문서 경계, 검증 구조, 가독성을 각각 확인한 뒤 모두 PASS로 수렴했다.

## 근거 Artifact
- guide: [manual-rerun-response-guide.md](/home/seaung13/workspace/agile-runner/docs/manual-rerun-response-guide.md)
- 자동 검증 테스트: [ManualRerunResponseGuideFixtureTest.java](/home/seaung13/workspace/agile-runner/src/test/java/com/agilerunner/api/controller/review/ManualRerunResponseGuideFixtureTest.java)
- 기준 파일:
  - [rerun-list-row-after-acknowledge.json](/home/seaung13/workspace/agile-runner/src/test/resources/manual-rerun-response-guide/rerun-list-row-after-acknowledge.json)
  - [retry-list-row-after-retry.json](/home/seaung13/workspace/agile-runner/src/test/resources/manual-rerun-response-guide/retry-list-row-after-retry.json)

## 다음 Task 경고사항
- `TASK-0003`에서는 기준 파일 이름 규칙을 반복 설명하기보다, guide/기준 파일/자동 검증 테스트를 함께 수정하는 절차와 대표 실제 앱 검증 경계를 분명히 적는 데 집중해야 한다.
- 이후 새 list 예시가 추가되면 `list-row-<시점>` 패턴을 유지하고, 단건 응답 전체와 row 예시 파일을 섞지 않게 주의해야 한다.

## 제안 필요 여부
- 없음
- 이번 교훈은 새 workflow 부족보다 naming 규칙과 파일 단위 기준이 문서로 분리돼 있지 않았던 문제에 가까웠다.
