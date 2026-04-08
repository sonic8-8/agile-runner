---
spec_id: SPEC-0006
task_id: TASK-0003
github_issue_number: 22
criteria_keys:
  - manual-rerun-respects-execution-control
delivery_ids: []
execution_keys: []
test_evidence_ref:
  - "targeted: ./scripts/gradlew-java21.sh --no-daemon test --tests 'com.agilerunner.api.service.review.ManualRerunServiceTest' --tests 'com.agilerunner.api.controller.review.ManualRerunControllerTest' --tests 'com.agilerunner.api.controller.review.request.ManualRerunRequestTest' --tests 'com.agilerunner.api.controller.GitHubWebhookControllerTest' --tests 'com.agilerunner.api.service.GitHubCommentServiceTest' --tests 'com.agilerunner.api.service.OpenAiServiceTest' --console=plain"
  - "full: ./scripts/gradlew-java21.sh --no-daemon cleanTest test --console=plain"
  - "actual app: POST /reviews/rerun -> 500 / GITHUB_APP_CONFIGURATION_MISSING"
diff_ref: "git diff -- src/main/java/com/agilerunner/api/service/review src/test/java/com/agilerunner/api/service/review .agents/active/tasks.md"
failure_summary: "초기 구현은 UUID 기반 execution key를 만들어 실제 runtime key처럼 보이게 했고, Tester 2에서는 또다시 targeted/full test를 동시에 실행해 XML 출력 충돌 false negative를 한 번 재현했다."
root_cause: "수동 재실행 응답의 executionKey와 TASK-0004의 runtime evidence key 경계를 충분히 분리하지 않은 채 구현을 시작했다. 또한 방금 채택한 순차 실행 규칙을 검증 단계에서 즉시 지키지 못했다."
agents_check_findings:
  - "ManualRerunService는 OpenAiService.generateReview와 GitHubCommentService.execute를 재사용해 NORMAL/DRY_RUN 경로를 service orchestration 수준에서 연결했다."
  - "response executionKey는 MANUAL_RERUN:PENDING_RUNTIME_KEY placeholder로 낮춰 TASK-0004 runtime evidence 의미를 선점하지 않게 조정했다."
  - "실제 앱 검증에서는 /reviews/rerun 경로가 OpenAiService까지 이어지고, 로컬 환경 제약으로 GITHUB_APP_CONFIGURATION_MISSING이 발생함을 확인했다."
next_task_warnings:
  - "TASK-0004에서는 응답 executionKey와 runtime evidence executionKey를 같은 대표 실행 기준으로 연결해야 한다."
  - "실제 앱 검증과 전체 테스트는 반드시 순차 실행해야 한다."
  - "runtime evidence 적재 전까지 PENDING_RUNTIME_KEY는 응답용 placeholder로만 해석해야 한다."
error_signature: "real-looking executionKey before runtime persistence, concurrent Gradle test XML write collision, GITHUB_APP_CONFIGURATION_MISSING on local app"
test_result_summary: "manual rerun service/controller targeted test, 기존 webhook 회귀 targeted test, 전체 cleanTest test 통과. 실제 앱 /reviews/rerun 호출은 500이며 원인은 GITHUB_APP_CONFIGURATION_MISSING"
snapshot_hashes: []
---

# 작업 회고

## 작업 요약
- `ManualRerunService`를 기존 `OpenAiService.generateReview(...)`와 `GitHubCommentService.execute(...)`에 연결했다.
- 수동 재실행 `NORMAL`/`DRY_RUN` 요청이 같은 review 생성 흐름을 재사용하고, 코멘트 write 여부는 기존 `GitHubCommentExecutionResult`에서 그대로 가져오게 만들었다.
- 응답의 `executionKey`는 `MANUAL_RERUN:PENDING_RUNTIME_KEY` placeholder로 두어, 실제 runtime evidence key 적재는 `TASK-0004`로 넘겼다.

## 실패 요약
- 첫 구현에서는 UUID 기반 execution key를 만들어 실제 runtime evidence key처럼 보이게 했다.
- Tester 2 검증에서는 accepted proposal이 있는 상태에서도 targeted/full test를 같은 workspace에서 동시에 실행해 XML 출력 파일 충돌 false negative를 재현했다.

## Root Cause
- 응답 계약과 runtime evidence 계약의 경계를 충분히 분리하지 않은 상태에서 service 응답을 설계했다.
- 새로 반영된 workflow 규칙을 실제 검증 루프에 아직 습관적으로 적용하지 못했다.

## AGENTS 체크 결과
- `ManualRerunServiceTest`로 service orchestration 기준을 먼저 고정했다.
- `ManualRerunService`는 controller/service/domain 경계를 넘지 않고 orchestration만 담당한다.
- runtime evidence 적재와 H2 검증은 아직 하지 않았고, 이 범위는 `TASK-0004`로 남겼다.
- 실제 앱 검증은 `/reviews/rerun` 경로가 실제 service execution path를 타는지 확인하는 수준으로 수행했다.

## 근거 Artifact
- `src/main/java/com/agilerunner/api/service/review/ManualRerunService.java`
- `src/main/java/com/agilerunner/api/service/review/response/ManualRerunServiceResponse.java`
- `src/test/java/com/agilerunner/api/service/review/ManualRerunServiceTest.java`
- `src/test/java/com/agilerunner/api/controller/review/ManualRerunControllerTest.java`
- `src/test/java/com/agilerunner/api/service/GitHubCommentServiceTest.java`
- `src/test/java/com/agilerunner/api/service/OpenAiServiceTest.java`

## 다음 Task 경고사항
- `TASK-0004`는 runtime evidence에 수동 재실행 구분값과 실제 execution key를 적재하고, representative manual rerun 결과를 H2에서 확인해야 한다.
- 응답 executionKey placeholder는 `TASK-0004`에서 실제 runtime evidence key로 전환되기 전까지 계약용 식별자일 뿐이다.
- 테스트는 targeted/full을 순차 실행해야 한다.

## 제안 필요 여부
- 없음
- 이번 교훈은 새 workflow 제안이 아니라, 이미 accepted 된 `WORKFLOW-PROP-0008`을 실제로 지키는 운영 문제에 가깝다.
