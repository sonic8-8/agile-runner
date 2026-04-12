---
spec_id: SPEC-0021
task_id: TASK-0002
github_issue_number: 81
criteria_keys:
  - manual-rerun-response-example-source-defined
delivery_ids: []
execution_keys: []
test_evidence_ref: "./scripts/gradlew-java21.sh --no-daemon test --console=plain --tests 'com.agilerunner.api.controller.review.ManualRerunControllerTest' --tests 'com.agilerunner.api.service.review.ManualRerunServiceTest' --tests 'com.agilerunner.api.service.review.ManualRerunRetryServiceTest' --tests 'com.agilerunner.api.service.review.ManualRerunQueryServiceTest' --tests 'com.agilerunner.api.service.review.ManualRerunExecutionListServiceTest' --tests 'com.agilerunner.api.service.review.ManualRerunControlActionHistoryServiceTest' --tests 'com.agilerunner.api.service.review.ManualRerunControlActionServiceTest' && ./scripts/gradlew-java21.sh --no-daemon cleanTest test --console=plain"
diff_ref: "working-tree:docs/manual-rerun-response-guide.md,src/test/resources/manual-rerun-response-guide/"
failure_summary: "구조 변경 중 새 기능 실패는 없었다. 1차 3-agent 리뷰에서 fixture 이름과 retry/list 관계 source가 부족하다는 지적이 나왔고, 파일명과 source 구성을 재정리한 뒤 PASS로 닫았다."
root_cause: "처음 초안은 문서와 fixture 연결 위치는 있었지만, 다음 task에서 drift assertion을 붙일 때 어떤 실행 종류와 시점을 비교하는지 파일명만으로 충분히 드러나지 않았다."
agents_check_findings:
  - "문서 경계 리뷰: source 위치와 TASK-0003 경계는 적절하다는 PASS"
  - "검증 리뷰: retry/list 관계 source 추가와 phase 중심 naming 필요 지적 후 반영"
  - "운영자 가독성 리뷰: fixture source 대신 예시 기준 파일 표현으로 완화"
next_task_warnings:
  - "TASK-0003에서는 guide 예시를 그대로 기준으로 삼되, fixture와 controller/service black-box 기대값 비교까지 닫을 것"
  - "문서용 fixture와 production representative 값 검증을 혼합하지 말 것"
error_signature: "NONE"
test_result_summary: "fixture source 구조 변경 후 targeted test green, full cleanTest test green"
snapshot_hashes: []
---

# 작업 회고

## 작업 요약
- 운영용 조회 응답 가이드에 각 예시의 기준 파일 위치를 명시했다.
- `src/test/resources/manual-rerun-response-guide/` 아래에 rerun, retry, query, list, history, action 예시 기준 파일을 추가했다.
- retry 응답과 list row의 `retrySourceExecutionKey` 관계를 다음 task에서 바로 검증할 수 있도록 `retry-list-row.json`을 따로 뒀다.

## 실패 요약
- 코드나 테스트 실패는 없었다.
- 대신 1차 리뷰에서 fixture 이름과 source 구조가 다음 task의 drift 검증 기준으로는 부족하다는 지적이 나왔다.

## Root Cause
- 첫 구조는 문서 연결 자체는 됐지만, 실행 종류와 시점이 파일명에서 충분히 드러나지 않았다.
- 이 상태로는 자동 검증을 붙일 때 테스트 코드가 다시 문서 의미를 해석해야 할 가능성이 있었다.

## AGENTS 체크 결과
- 1차 3-agent 리뷰에서 `retry list row`용 source 부재와 phase 중심 naming 부족이 지적됐다.
- `fixture source` 표현은 운영자 문서 기준으로 다소 기술적이라는 지적을 받고 `예시 기준 파일`로 정리했다.
- 수정 후 3-agent 리뷰에서 문서 경계, 검증 기준, 운영자 가독성 모두 `PASS`가 났다.
- production code, `/webhook/github`, controller orchestration, `agent-runtime` 저장 구조 변경이 없어서 actual app/H2 representative 검증은 비대상으로 정리했다.

## 근거 Artifact
- guide 문서: [manual-rerun-response-guide.md](/home/seaung13/workspace/agile-runner/docs/manual-rerun-response-guide.md)
- 기준 파일 경로:
  - [rerun-start-response.json](/home/seaung13/workspace/agile-runner/src/test/resources/manual-rerun-response-guide/rerun-start-response.json)
  - [retry-start-response.json](/home/seaung13/workspace/agile-runner/src/test/resources/manual-rerun-response-guide/retry-start-response.json)
  - [rerun-query-before-acknowledge.json](/home/seaung13/workspace/agile-runner/src/test/resources/manual-rerun-response-guide/rerun-query-before-acknowledge.json)
  - [rerun-list-after-acknowledge.json](/home/seaung13/workspace/agile-runner/src/test/resources/manual-rerun-response-guide/rerun-list-after-acknowledge.json)
  - [retry-list-row.json](/home/seaung13/workspace/agile-runner/src/test/resources/manual-rerun-response-guide/retry-list-row.json)
  - [rerun-history-after-acknowledge.json](/home/seaung13/workspace/agile-runner/src/test/resources/manual-rerun-response-guide/rerun-history-after-acknowledge.json)
  - [rerun-action-after-acknowledge.json](/home/seaung13/workspace/agile-runner/src/test/resources/manual-rerun-response-guide/rerun-action-after-acknowledge.json)

## 다음 Task 경고사항
- `TASK-0003`은 source 구조를 다시 바꾸기보다, 지금 만든 기준 파일과 controller/service black-box 기대값을 연결하는 쪽에 집중해야 한다.
- retry/list 관계는 `retry-start-response.json`과 `retry-list-row.json`을 같이 써서 `retrySourceExecutionKey` drift를 잡아야 한다.

## 제안 필요 여부
- 없음
