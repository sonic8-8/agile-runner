---
spec_id: SPEC-0012
task_id: TASK-0002
github_issue_number: 45
criteria_keys:
  - manual-rerun-control-action-defined
delivery_ids: []
execution_keys: []
test_evidence_ref:
  - "targeted: ./scripts/gradlew-java21.sh --no-daemon test --tests 'com.agilerunner.api.controller.review.ManualRerunControllerTest' --tests 'com.agilerunner.api.controller.review.request.ManualRerunControlActionRequestTest' --tests 'com.agilerunner.api.controller.review.response.ManualRerunControlActionResponseTest' --tests 'com.agilerunner.api.service.review.ManualRerunControlActionServiceTest' --tests 'com.agilerunner.api.service.review.ManualRerunQueryServiceTest' --tests 'com.agilerunner.api.service.review.ManualRerunRetryServiceTest' --tests 'com.agilerunner.api.service.review.ManualRerunExecutionListServiceTest' --tests 'com.agilerunner.api.controller.GitHubWebhookControllerTest' --console=plain"
  - "full: ./scripts/gradlew-java21.sh --no-daemon cleanTest test --console=plain"
diff_ref: "git diff -- src/main/java/com/agilerunner/api/controller/review/ManualRerunController.java src/main/java/com/agilerunner/api/controller/review/request/ManualRerunControlActionRequest.java src/main/java/com/agilerunner/api/controller/review/response/ManualRerunControlActionResponse.java src/main/java/com/agilerunner/api/service/review/ManualRerunControlActionService.java src/main/java/com/agilerunner/api/service/review/request/ManualRerunControlActionServiceRequest.java src/main/java/com/agilerunner/api/service/review/response/ManualRerunControlActionServiceResponse.java src/main/java/com/agilerunner/domain/review/ManualRerunControlAction.java src/main/java/com/agilerunner/domain/review/ManualRerunControlActionStatus.java src/test/java/com/agilerunner/api/controller/review/ManualRerunControllerTest.java src/test/java/com/agilerunner/api/controller/review/request/ManualRerunControlActionRequestTest.java src/test/java/com/agilerunner/api/controller/review/response/ManualRerunControlActionResponseTest.java src/test/java/com/agilerunner/api/service/review/ManualRerunControlActionServiceTest.java .agents/outer-loop/retrospectives/SPEC-0012/TASK-0002-control-action-input-model.md .agents/outer-loop/registry.json"
failure_summary: "관리자 제어 액션 경계 추가 전에는 `POST /reviews/rerun/{executionKey}/actions` request/response seam이 없어 action 입력과 최소 응답 계약을 고정할 수 없었다."
root_cause: "SPEC-0012는 관리자 제어 액션 확장을 목표로 하지만, 이전 spec까지는 query/list/retry 흐름만 있었고 action request, action response, action service seam이 아직 분리돼 있지 않았다."
agents_check_findings:
  - "3개 서브에이전트 리뷰 결과, 이번 task는 `ACKNOWLEDGE` request/response seam과 `actionStatus=APPLIED` 성공 응답 계약까지만 열고 audit evidence 저장과 query/list 반영은 다음 task로 남기는 구성이 맞다고 정리됐다."
  - "controller/service/request/response 테스트만으로 현재 단계의 black-box 기준을 충분히 닫을 수 있다고 확인됐다."
  - "새 workflow 또는 AGENTS proposal은 필요 없고, 현재 경계만 유지하면 된다고 확인됐다."
next_task_warnings:
  - "TASK-0003는 action 실행 조건과 audit evidence 저장, query/list의 availableActions 재계산을 함께 닫아야 한다."
  - "이번 task에서 도입한 `actionStatus=APPLIED` 성공 응답 계약은 유지하고, conflict/not-found 정책은 그 위에서 확장해야 한다."
error_signature: "missing seam: control action request/response boundary absent before endpoint introduction"
test_result_summary: "targeted test와 full cleanTest test가 모두 통과했다. 이번 task는 입력 모델과 최소 성공 응답 경계 도입 단계라 actual app/H2 representative verification은 비대상으로 정리했다."
snapshot_hashes: []
---

# 작업 회고

## 작업 요약
- `POST /reviews/rerun/{executionKey}/actions` 진입점을 추가하고 `ManualRerunControlActionRequest/Response`, service request/response, `ManualRerunControlActionService`를 도입했다.
- 첫 관리자 제어 액션은 `ACKNOWLEDGE` 하나로 고정했고, 성공 응답의 `actionStatus=APPLIED` 계약을 테스트로 잠갔다.
- controller/request/response/service 테스트를 추가해 입력 전달과 최소 성공 응답 계약을 black-box 기준으로 고정했다.

## 실패 요약
- 구현 전에는 action endpoint와 request/response seam이 없어 관리자 제어 액션 입력과 최소 응답 계약을 테스트로 고정할 수 없었다.
- 구현 후에는 targeted test와 full test 모두 정상 통과했다.

## Root Cause
- 이전 spec까지는 rerun/query/retry/list 흐름만 있었고, 관리자 제어 액션을 받을 별도 경계가 없었다.
- audit evidence 저장과 조회 반영을 바로 붙이기보다, 먼저 request/response seam과 `actionStatus=APPLIED` 성공 응답을 고정하는 단계가 필요했다.

## AGENTS 체크 결과
- Tester는 controller/service black-box 테스트로 `ACKNOWLEDGE` request 전달과 최소 응답 계약을 먼저 고정했다.
- Constructor는 action endpoint와 DTO seam만 도입했고, audit evidence 저장과 query/list 반영은 미리 끌어오지 않았다.
- targeted test와 전체 테스트를 순차 실행했다.
- actual app/H2 representative verification은 runtime 저장 구조를 바꾸지 않는 단계라 비대상으로 정리했다.

## 근거 Artifact
- `src/main/java/com/agilerunner/api/controller/review/ManualRerunController.java`
- `src/main/java/com/agilerunner/api/controller/review/request/ManualRerunControlActionRequest.java`
- `src/main/java/com/agilerunner/api/controller/review/response/ManualRerunControlActionResponse.java`
- `src/main/java/com/agilerunner/api/service/review/ManualRerunControlActionService.java`
- `src/main/java/com/agilerunner/api/service/review/request/ManualRerunControlActionServiceRequest.java`
- `src/main/java/com/agilerunner/api/service/review/response/ManualRerunControlActionServiceResponse.java`
- `src/main/java/com/agilerunner/domain/review/ManualRerunControlAction.java`
- `src/main/java/com/agilerunner/domain/review/ManualRerunControlActionStatus.java`
- `src/test/java/com/agilerunner/api/controller/review/ManualRerunControllerTest.java`
- `src/test/java/com/agilerunner/api/controller/review/request/ManualRerunControlActionRequestTest.java`
- `src/test/java/com/agilerunner/api/controller/review/response/ManualRerunControlActionResponseTest.java`
- `src/test/java/com/agilerunner/api/service/review/ManualRerunControlActionServiceTest.java`

## 다음 Task 경고사항
- `TASK-0003`에서는 `ACKNOWLEDGE` 허용 조건과 audit evidence 저장을 함께 닫아야 하므로, 이번 task의 no-op service 구현을 그대로 유지하면 안 된다.
- query/list는 action detail을 직접 노출하지 않고 `availableActions` 재계산만 반영한다는 경계를 유지해야 한다.

## 제안 필요 여부
- 없음
- 이번 task의 교훈은 새 workflow 규칙 부족이 아니라, audit evidence 저장과 조회 반영을 끌어오지 않고 최소 controller/service seam만 여는 현재 task 경계를 지키는 쪽이었다.
