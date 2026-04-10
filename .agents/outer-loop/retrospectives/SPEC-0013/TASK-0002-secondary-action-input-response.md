---
spec_id: SPEC-0013
task_id: TASK-0002
github_issue_number: 49
criteria_keys:
  - manual-rerun-control-secondary-action-defined
delivery_ids: []
execution_keys: []
test_evidence_ref:
  - "targeted: ./scripts/gradlew-java21.sh --no-daemon test --tests 'com.agilerunner.api.controller.review.request.ManualRerunControlActionRequestTest' --tests 'com.agilerunner.api.controller.review.response.ManualRerunControlActionResponseTest' --tests 'com.agilerunner.api.controller.review.ManualRerunControllerTest' --tests 'com.agilerunner.api.service.review.ManualRerunControlActionServiceTest' --console=plain"
  - "full: ./scripts/gradlew-java21.sh --no-daemon cleanTest test --console=plain"
diff_ref: "git diff -- src/main/java/com/agilerunner/domain/review/ManualRerunControlAction.java src/test/java/com/agilerunner/api/controller/review/request/ManualRerunControlActionRequestTest.java src/test/java/com/agilerunner/api/controller/review/response/ManualRerunControlActionResponseTest.java src/test/java/com/agilerunner/api/controller/review/ManualRerunControllerTest.java src/test/java/com/agilerunner/api/service/review/ManualRerunControlActionServiceTest.java"
failure_summary: "Tester 1차 red 원인은 ManualRerunControlAction enum에 UNACKNOWLEDGE가 없어 입력/응답 테스트가 컴파일되지 않은 점이었다."
root_cause: "TASK-0002는 입력과 최소 응답 경계만 열려는 범위였지만, 실제 production enum은 여전히 ACKNOWLEDGE만 가지고 있었다. enum 확장만 넣으면 controller/request/response/service 경계는 green이 되었고, 정책과 audit state는 아직 다음 task로 남길 수 있었다."
agents_check_findings:
  - "3개 서브에이전트 모두 UNACKNOWLEDGE enum 확장만 넣는 최소 변경이 TASK-0002 범위에 맞다고 확인했다."
  - "정책 판정, audit evidence 저장, query/list 상태 반영은 아직 끌어오지 않고 TASK-0003으로 남기는 편이 적절하다고 정리됐다."
next_task_warnings:
  - "TASK-0003에서는 TASK-0002의 성공 응답을 정책 완료로 오해하지 말고, UNACKNOWLEDGE 허용 조건과 audit/query-list state 반영을 실제로 닫아야 한다."
  - "query/list availableActions 토글은 TASK-0003에서 다루고, TASK-0002에서는 입력 해석과 최소 응답 경계만 유지해야 한다."
error_signature: "compile error: cannot find symbol ManualRerunControlAction.UNACKNOWLEDGE"
test_result_summary: "UNACKNOWLEDGE 입력과 최소 응답 경계를 고정한 targeted test와 full cleanTest test가 모두 통과했다."
snapshot_hashes: []
---

# 작업 회고

## 작업 요약
- `UNACKNOWLEDGE`를 관리자 액션 enum에 추가했다.
- request/response/controller/service 테스트에서 `UNACKNOWLEDGE` 입력과 최소 성공 응답 계약을 고정했다.
- 정책 판정, audit evidence 저장, query/list 상태 반영은 이번 task에 끌어오지 않고 다음 task로 남겼다.

## 실패 요약
- Tester 1차에서는 `ManualRerunControlAction.UNACKNOWLEDGE`가 없어 테스트 컴파일이 실패했다.
- 최소 production 변경으로 enum만 확장한 뒤 targeted/full test를 다시 실행해 green으로 닫았다.

## Root Cause
- active spec과 tasks 문서는 `UNACKNOWLEDGE` 입력을 열도록 바뀌었지만, 실제 production enum은 아직 `ACKNOWLEDGE` 하나만 지원하고 있었다.
- 입력/응답 경계만 여는 task에서는 enum 확장이 최소 변경이었고, 그 이상은 다음 task 범위를 침범하게 된다.

## AGENTS 체크 결과
- Tester는 request/response/controller/service 경계 black-box 테스트를 먼저 추가했다.
- Constructor는 enum 확장 하나만 넣고 정책과 audit/query-list 반영은 다음 task로 남겼다.
- targeted test와 full cleanTest test를 순차 실행했다.
- 실제 앱/H2 representative 검증은 이번 task 비대상으로 생략했고, 그 사유를 회고에 남겼다.

## 근거 Artifact
- `src/main/java/com/agilerunner/domain/review/ManualRerunControlAction.java`
- `src/test/java/com/agilerunner/api/controller/review/request/ManualRerunControlActionRequestTest.java`
- `src/test/java/com/agilerunner/api/controller/review/response/ManualRerunControlActionResponseTest.java`
- `src/test/java/com/agilerunner/api/controller/review/ManualRerunControllerTest.java`
- `src/test/java/com/agilerunner/api/service/review/ManualRerunControlActionServiceTest.java`

## 다음 Task 경고사항
- `TASK-0003`에서는 `UNACKNOWLEDGE` 허용 조건과 audit evidence 저장, query/list `availableActions` 반영을 실제로 닫아야 한다.
- 이번 task의 service 성공 응답은 입력/응답 경계 확인용일 뿐이고, 정책 완료 의미로 해석하면 안 된다.

## 제안 필요 여부
- 없음
- 이번 task의 교훈은 새 workflow 규칙 부족이 아니라, enum 부재를 최소 변경으로 메우고 다음 task 경계를 지킨 사례였다.
