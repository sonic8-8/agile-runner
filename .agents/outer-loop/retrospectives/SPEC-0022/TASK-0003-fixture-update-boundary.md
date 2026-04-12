---
spec_id: SPEC-0022
task_id: TASK-0003
github_issue_number: 86
criteria_keys:
  - manual-rerun-fixture-update-boundary-defined
delivery_ids: []
execution_keys: []
test_evidence_ref:
  - "targeted: ./scripts/gradlew-java21.sh --no-daemon test --console=plain --tests 'com.agilerunner.api.controller.review.ManualRerunResponseGuideFixtureTest' --tests 'com.agilerunner.api.controller.review.ManualRerunControllerTest' --tests 'com.agilerunner.api.service.review.ManualRerunServiceTest' --tests 'com.agilerunner.api.service.review.ManualRerunRetryServiceTest' --tests 'com.agilerunner.api.service.review.ManualRerunQueryServiceTest' --tests 'com.agilerunner.api.service.review.ManualRerunExecutionListServiceTest' --tests 'com.agilerunner.api.service.review.ManualRerunControlActionHistoryServiceTest' --tests 'com.agilerunner.api.service.review.ManualRerunControlActionServiceTest'"
  - "full: ./scripts/gradlew-java21.sh --no-daemon cleanTest test --console=plain"
diff_ref: "working-tree:docs/manual-rerun-response-guide.md,.agents/outer-loop/retrospectives/SPEC-0022/TASK-0003-fixture-update-boundary.md,.agents/outer-loop/registry.json"
failure_summary: "guide에 기준 파일 갱신 순서와 대표 실제 앱 검증 경계를 추가했다. 중간에 targeted test와 full test를 병렬로 띄워 XML 결과 파일 충돌 거짓 실패가 한 번 났지만, 규칙대로 순차 재실행한 뒤 모두 통과했다."
root_cause: "문서 자체의 빈틈은 guide, 기준 파일, 기준 파일 비교 테스트를 함께 수정하는 순서와 대표 실제 앱 검증 결과를 기준 파일에 그대로 옮기지 않는 경계가 분리돼 적혀 있지 않았던 점이다. 검증 실패는 새 workflow 부족이 아니라, 기존 '같은 workspace 테스트 순차 실행' 규칙을 지키지 않아 생겼다."
agents_check_findings:
  - "문서 경계 리뷰: 새 섹션이 TASK-0003 범위 안에서 guide, 기준 파일, 기준 파일 비교 테스트 갱신 절차와 대표 검증 경계만 다루고 TASK-0004 마감 범위를 당겨오지 않았다는 PASS"
  - "검증 리뷰: guide -> 기준 파일 -> ManualRerunResponseGuideFixtureTest -> targeted/full test 순서가 명확하고, 대표 실제 앱 검증 값은 evidence에만 남긴다는 경계가 충분하다는 PASS"
  - "운영자 가독성 리뷰: '같은 예시 비교 테스트', '문서 설명에 맞춰 남겨 두는 예시 응답', '대표 실제 앱 검증 값'처럼 추상도를 낮춘 뒤 PASS로 수렴"
next_task_warnings:
  - "TASK-0004에서는 새 작업자가 guide만 읽고 절차를 따라갈 수 있는지 점검하는 데 집중하고, actual app/H2 대표 검증을 이번 spec 비대상으로 두는 이유를 retrospective와 summary에 같이 남길 것"
  - "종료 검증에서는 targeted test와 full cleanTest test를 다시 병렬로 띄우지 말고 반드시 순차 실행할 것"
error_signature: "GRADLE_TEST_RESULT_XML_WRITE_CONFLICT"
test_result_summary: "초기 병렬 실행에서 build/test-results XML write conflict가 있었고, 이후 targeted test green, full cleanTest test green으로 순차 재검증 완료"
snapshot_hashes: []
---

# 작업 회고

## 작업 요약
- guide에 `기준 파일 갱신 순서`와 `대표 실제 앱 검증과 기준 파일의 경계` 섹션을 추가했다.
- guide, 기준 파일, 기준 파일 비교 테스트를 한 변경으로 같이 닫아야 한다는 점을 문서에 고정했다.
- 대표 실제 앱 검증 값은 evidence와 retrospective에 남기고, 기준 파일에는 안정적인 예시 값만 둔다는 경계를 문서에 적었다.

## 실패 요약
- 문서 내용 자체로 인한 테스트 실패는 없었다.
- 다만 작업 중 targeted test와 full test를 병렬로 띄워 `build/test-results` XML 출력 충돌 거짓 실패가 한 번 발생했다.
- 같은 명령을 순차 재실행한 뒤에는 targeted test와 full cleanTest test가 모두 통과했다.

## Root Cause
- 새 작업자는 guide, 기준 파일, 기준 파일 비교 테스트의 관계를 읽을 수 있었지만, 어떤 순서로 함께 수정해야 하는지와 대표 실제 앱 검증을 기준 파일과 어떻게 분리해야 하는지는 문서에 직접 적혀 있지 않았다.
- 검증 실패는 코드나 문서 변경 때문이 아니라, 기존 AGENTS/skill 규칙에 이미 있던 순차 실행 원칙을 지키지 않아 생긴 검증 절차 위반이었다.

## AGENTS 체크 결과
- linked issue `#86`은 TASK-0003과 1:1 범위를 유지했다.
- 서로 다른 관점의 3개 리뷰어가 문서 경계, 검증 근거, 가독성을 각각 확인했고 최종 PASS로 수렴했다.
- actual app/H2 대표 검증은 TASK-0003 비대상으로 유지했다. 이번 task는 갱신 절차와 대표 검증 경계 문서화가 목표였고, 실제 대표 검증 정합성 재확인은 TASK-0004 마감 판단 범위도 아니었다.
- 종료 검증은 최종적으로 targeted test와 full cleanTest test를 순차 실행 기준으로 다시 맞췄다.

## 근거 Artifact
- guide: [manual-rerun-response-guide.md](/home/seaung13/workspace/agile-runner/docs/manual-rerun-response-guide.md)
- 자동 검증 테스트: [ManualRerunResponseGuideFixtureTest.java](/home/seaung13/workspace/agile-runner/src/test/java/com/agilerunner/api/controller/review/ManualRerunResponseGuideFixtureTest.java)
- 기준 파일 디렉토리: [manual-rerun-response-guide](/home/seaung13/workspace/agile-runner/src/test/resources/manual-rerun-response-guide)

## 다음 Task 경고사항
- `TASK-0004`는 새 규칙을 더 추가하기보다, 현재 guide만 읽고 새 작업자가 절차를 따라갈 수 있는지 점검하고 targeted/full test 재확인으로 마감해야 한다.
- 이번 spec은 문서/운영 규칙 정리 spec이므로 actual app/H2 대표 검증 비대상 사유를 spec summary에도 반복해서 남기는 편이 안전하다.
- 다음 종료 검증에서는 테스트를 절대 병렬로 띄우지 않는다.

## 제안 필요 여부
- 없음
- 이번 교훈은 새 workflow 부족이 아니라, 이미 있는 순차 실행 규칙을 지키지 않아 생긴 검증 절차 위반에 가까웠다.
