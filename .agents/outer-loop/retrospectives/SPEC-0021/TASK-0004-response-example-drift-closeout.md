---
spec_id: SPEC-0021
task_id: TASK-0004
github_issue_number: 83
criteria_keys:
  - manual-rerun-response-example-drift-detected
delivery_ids: []
execution_keys: []
test_evidence_ref:
  - "targeted: ./scripts/gradlew-java21.sh --no-daemon test --console=plain --tests 'com.agilerunner.api.controller.review.ManualRerunResponseGuideFixtureTest' --tests 'com.agilerunner.api.controller.review.ManualRerunControllerTest' --tests 'com.agilerunner.api.service.review.ManualRerunServiceTest' --tests 'com.agilerunner.api.service.review.ManualRerunRetryServiceTest' --tests 'com.agilerunner.api.service.review.ManualRerunQueryServiceTest' --tests 'com.agilerunner.api.service.review.ManualRerunExecutionListServiceTest' --tests 'com.agilerunner.api.service.review.ManualRerunControlActionHistoryServiceTest' --tests 'com.agilerunner.api.service.review.ManualRerunControlActionServiceTest'"
  - "full: ./scripts/gradlew-java21.sh --no-daemon cleanTest test --console=plain"
diff_ref: "working-tree:docs/manual-rerun-response-guide.md,.agents/outer-loop/retrospectives/SPEC-0021/TASK-0004-response-example-drift-closeout.md,.agents/outer-loop/retrospectives/SPEC-0021/SPEC-0021-summary.md,.agents/outer-loop/registry.json"
failure_summary: "새 runtime failure는 없었다. 이번 task는 drift 검증 문서 마감 단계였고, guide에서 기준 파일과 자동 검증 테스트를 함께 보도록 안내를 보강한 뒤 targeted/full test를 다시 통과시켰다."
root_cause: "이전 task까지 fixture와 자동 검증 테스트는 들어왔지만, guide만 단독으로 고치면 drift 보호 범위를 놓칠 수 있었다. 마지막 task에서 guide, 기준 파일, 자동 검증 테스트를 하나의 변경 단위로 묶는 안내가 필요했다."
agents_check_findings:
  - "문서 경계 리뷰: actual app/H2 재검증을 다시 끌어오지 않고 docs/test drift 보호 마감으로 해석한 범위가 적절하다는 PASS"
  - "검증 리뷰: ManualRerunResponseGuideFixtureTest 포함 targeted test와 full cleanTest test를 순차 실행한 근거가 충분하다는 PASS"
  - "운영자 가독성 리뷰: guide에 기준 파일과 자동 검증 테스트를 함께 확인해야 한다는 문장이 추가돼 새 작업자도 drift 보호 경로를 따라가기 쉬워졌다는 PASS"
next_task_warnings:
  - "후속 spec에서 fixture 생성 규칙을 정리할 때는 guide, 기준 파일, 자동 검증 테스트를 항상 같은 변경 묶음으로 다룬다는 원칙을 먼저 문서화할 것"
  - "실제 응답 의미를 바꾸는 spec이 나오면 SPEC-0020 representative 근거와 SPEC-0021 자동 검증 근거를 함께 갱신해야 drift 오판을 줄일 수 있다"
error_signature: "NONE"
test_result_summary: "ManualRerunResponseGuideFixtureTest 포함 targeted test green, full cleanTest test green"
snapshot_hashes: []
---

# 작업 회고

## 작업 요약
- guide 문서에 자동 검증 테스트 위치와 기준 파일, 테스트를 함께 확인해야 한다는 안내를 추가했다.
- ManualRerunResponseGuideFixtureTest가 현재 guide 예시 drift를 targeted/full test 흐름에서 실제로 잡을 수 있는지 다시 확인했다.
- 이번 spec은 docs/test 자산 정리 단계이므로 actual app/H2 representative 재검증은 수행하지 않고, 그 판단 근거를 회고와 summary에 남기기로 고정했다.

## 실패 요약
- 코드나 테스트 실패는 없었다.
- 마지막 남은 위험은 guide만 따로 수정하고 기준 파일과 자동 검증 테스트를 놓치는 문서 drift였고, 이를 문장과 검증 근거로 닫았다.

## Root Cause
- TASK-0003에서 자동 검증 테스트는 들어갔지만, 가이드 문서만 읽는 작업자는 어느 파일과 테스트를 함께 봐야 하는지 아직 한 번에 알기 어려웠다.
- 문서 수정 경로를 명시하지 않으면 이후 변경자가 guide 본문만 바꾸고 기준 파일이나 테스트를 놓치는 drift가 다시 생길 수 있었다.

## AGENTS 체크 결과
- linked issue `#83`는 TASK-0004와 1:1 범위를 유지했다.
- targeted test와 full cleanTest test를 순차 실행했다.
- production code, controller orchestration, runtime 저장 구조 변경이 없어서 actual app/H2 representative 검증은 비대상으로 유지했고, 그 사유를 회고와 summary에 함께 남긴다.
- 서로 다른 관점의 3개 리뷰어가 문서 경계, 검증 흐름, 운영자 가독성을 각각 확인한 뒤 모두 PASS로 수렴했다.

## 근거 Artifact
- guide 문서: [manual-rerun-response-guide.md](/home/seaung13/workspace/agile-runner/docs/manual-rerun-response-guide.md)
- 자동 검증 테스트: [ManualRerunResponseGuideFixtureTest.java](/home/seaung13/workspace/agile-runner/src/test/java/com/agilerunner/api/controller/review/ManualRerunResponseGuideFixtureTest.java)
- 기준 파일 디렉토리: [manual-rerun-response-guide](/home/seaung13/workspace/agile-runner/src/test/resources/manual-rerun-response-guide)

## 다음 Task 경고사항
- 후속 spec은 fixture 생성 규칙과 갱신 절차를 다룰 가능성이 높으므로, guide/기준 파일/자동 검증 테스트를 한 묶음으로 수정한다는 규칙을 먼저 명시해야 한다.
- 이후 실제 응답 필드가 바뀌면 guide 문장 보정만으로 끝내지 말고 기준 파일과 ManualRerunResponseGuideFixtureTest 기대값도 함께 갱신해야 한다.

## 제안 필요 여부
- 없음
- 이번 교훈은 새 workflow 부족보다, 이미 만든 자동 검증 기반을 문서 수정 경로에 명시하는 마감 작업에 가까웠다.
