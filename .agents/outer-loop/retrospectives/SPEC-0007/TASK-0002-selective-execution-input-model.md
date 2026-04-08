---
spec_id: SPEC-0007
task_id: TASK-0002
github_issue_number: 25
criteria_keys:
  - selection-input-resolved-consistently
delivery_ids: []
execution_keys: []
test_evidence_ref:
  - "targeted: ./scripts/gradlew-java21.sh --no-daemon cleanTest test --tests 'com.agilerunner.api.controller.review.request.ManualRerunRequestTest' --tests 'com.agilerunner.api.controller.review.ManualRerunControllerTest' --tests 'com.agilerunner.api.service.github.request.GitHubEventServiceRequestTest' --tests 'com.agilerunner.api.controller.GitHubWebhookControllerTest' --tests 'com.agilerunner.api.service.review.ManualRerunServiceTest' --console=plain"
  - "full: ./scripts/gradlew-java21.sh --no-daemon cleanTest test --console=plain"
diff_ref: "git diff -- src/main/java/com/agilerunner/api/controller/review/request/ManualRerunRequest.java src/main/java/com/agilerunner/api/service/review/request/ManualRerunServiceRequest.java src/main/java/com/agilerunner/api/service/github/request/GitHubEventServiceRequest.java src/main/java/com/agilerunner/api/service/review/ManualRerunService.java src/test/java/com/agilerunner/api/controller/review/request/ManualRerunRequestTest.java src/test/java/com/agilerunner/api/controller/review/ManualRerunControllerTest.java src/test/java/com/agilerunner/api/service/github/request/GitHubEventServiceRequestTest.java src/test/java/com/agilerunner/api/service/review/ManualRerunServiceTest.java"
failure_summary: "targeted test와 full test를 같은 workspace에서 다시 동시에 실행해 XML 결과 파일 충돌이 재발했다."
root_cause: "이미 accepted 된 순차 실행 규칙이 있었지만, 실제 도구 호출에서는 `multi_tool_use.parallel`로 두 테스트 명령을 함께 실행해 같은 workspace의 `build/test-results` 산출물을 충돌시켰다."
agents_check_findings:
  - "선택 파일 경로 목록은 `ManualRerunRequest -> ManualRerunServiceRequest -> GitHubEventServiceRequest` seam에서만 추가됐다."
  - "실제 경로 제한 로직과 runtime evidence 적재는 건드리지 않아 `TASK-0003`, `TASK-0004` 범위를 앞당기지 않았다."
  - "테스트는 request/controller/downstream request seam 보존 위주로 고정했고, `ManualRerunServiceTest`는 기존 실행 경계 회귀 수준으로 유지했다."
next_task_warnings:
  - "종료 검증에서 같은 workspace의 테스트 명령을 `multi_tool_use.parallel`로 묶지 말아야 한다."
  - "TASK-0003은 실제 선택 경로 제한 로직만 다루고, 입력 seam 해석 재정의는 다시 건드리지 말아야 한다."
  - "선택 경로가 하나도 매칭되지 않을 때도 성공 응답 계약은 유지한다는 기준을 이후 task에서 계속 지켜야 한다."
error_signature: "parallel gradle test xml output collision in same workspace"
test_result_summary: "입력 seam 관련 targeted test와 전체 test는 순차 재실행 기준으로 모두 green이었다."
snapshot_hashes: []
---

# 작업 회고

## 작업 요약
- `selectedPaths`를 `ManualRerunRequest`, `ManualRerunServiceRequest`, `GitHubEventServiceRequest`에 추가해 선택 실행 입력 seam을 열었다.
- 선택 경로 목록이 비어 있을 때는 빈 목록으로 해석해 기존 전체 실행 기본 해석을 유지하도록 정리했다.
- `ManualRerunControllerTest`, `ManualRerunRequestTest`, `GitHubEventServiceRequestTest`를 중심으로 request/controller/downstream request seam 보존을 고정했다.

## 실패 요약
- 종료 검증 중 targeted test와 full test를 같은 workspace에서 다시 동시에 실행해 XML 결과 파일 충돌이 재발했다.

## Root Cause
- 이미 accepted 된 순차 실행 규칙이 있었지만, 실제 도구 호출에서 `multi_tool_use.parallel`을 사용해 같은 workspace 산출물을 공유하는 테스트 명령을 병렬로 돌렸다.
- 규칙 문구는 있었지만, 도구 선택 수준에서 더 직접적인 금지 표현이 없어서 같은 실수를 다시 냈다.

## AGENTS 체크 결과
- 입력 seam 추가만 수행했고, 실제 경로 제한 로직과 runtime evidence 적재는 건드리지 않았다.
- `ManualRerunServiceTest`는 기존 실행 경계 회귀 수준으로 유지하고, 새 선택 경로 기대는 request/controller/downstream request seam 테스트에만 남겼다.
- actual-app/H2 representative 검증은 runtime 저장 구조 변경이 없어 비대상으로 정리했다.

## 근거 Artifact
- `src/main/java/com/agilerunner/api/controller/review/request/ManualRerunRequest.java`
- `src/main/java/com/agilerunner/api/service/review/request/ManualRerunServiceRequest.java`
- `src/main/java/com/agilerunner/api/service/github/request/GitHubEventServiceRequest.java`
- `src/main/java/com/agilerunner/api/service/review/ManualRerunService.java`
- `src/test/java/com/agilerunner/api/controller/review/request/ManualRerunRequestTest.java`
- `src/test/java/com/agilerunner/api/controller/review/ManualRerunControllerTest.java`
- `src/test/java/com/agilerunner/api/service/github/request/GitHubEventServiceRequestTest.java`
- `src/test/java/com/agilerunner/api/service/review/ManualRerunServiceTest.java`

## 다음 Task 경고사항
- `TASK-0003`은 실제 선택 경로 제한 로직만 다루고, 입력 seam 자체를 다시 넓히지 않아야 한다.
- 선택 경로가 하나도 매칭되지 않을 때는 빈 결과 처리와 성공 응답 계약 유지 기준을 그대로 지켜야 한다.
- 같은 workspace의 테스트 명령은 `multi_tool_use.parallel`로 병렬 실행하지 말아야 한다.

## 제안 필요 여부
- 있음
- 기존 순차 실행 규칙은 있었지만, 실제 도구 사용 수준에서 `같은 workspace 테스트 명령의 parallel 래핑 금지`를 더 직접적으로 남길 필요가 있다.
