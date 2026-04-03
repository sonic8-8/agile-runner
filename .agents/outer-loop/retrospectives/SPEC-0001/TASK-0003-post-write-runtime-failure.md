---
spec_id: SPEC-0001
task_id: TASK-0003
github_issue_number: 5
criteria_keys:
  - same-delivery-comment-idempotent
  - post-write-runtime-failure-tolerated
delivery_ids:
  - task-0003-verify-001
execution_keys:
  - RUN:task-0003-verify-001
test_evidence_ref: "targeted controller test: GRADLE_USER_HOME=/home/seaung13/workspace/agile-runner/.gradle-local scripts/gradlew-java21.sh --no-daemon test --tests 'com.agilerunner.api.controller.GitHubWebhookControllerTest' --console=plain; service test stabilization: GRADLE_USER_HOME=/home/seaung13/workspace/agile-runner/.gradle-local scripts/gradlew-java21.sh --no-daemon test --tests 'com.agilerunner.api.service.GitHubCommentServiceTest' --console=plain; full suite: GRADLE_USER_HOME=/home/seaung13/workspace/agile-runner/.gradle-local scripts/gradlew-java21.sh --no-daemon test --console=plain; runtime verification: bootRun(local, port 18080) -> curl task-0003-verify-001 -> H2 query"
diff_ref: "src/main/java/com/agilerunner/api/controller/GitHubWebhookController.java, src/test/java/com/agilerunner/api/controller/GitHubWebhookControllerTest.java, src/test/java/com/agilerunner/api/service/GitHubCommentServiceTest.java"
failure_summary: "GitHub 코멘트 등록 성공 이후 runtime 기록 실패가 외부 요청 실패와 cache 미기록으로 함께 번질 수 있었다. 전체 테스트에서는 github-api comment 오버로드를 Mockito로 stubbing한 service test도 함께 드러났다."
root_cause: "controller가 comment 성공, runtime 기록, delivery cache 기록을 같은 성공 경로에 묶고 있었고, post-write runtime failure를 별도 비차단 경계로 나누지 않았다. 또한 GitHub API 오버로드 메서드를 Mockito return stubbing으로 직접 다룬 테스트가 full suite에서 취약했다."
agents_check_findings:
  - "Tester는 production code를 수정하지 않고 controller/service integration 중심 black-box 테스트를 먼저 작성했다."
  - "Constructor는 controller 한 파일에서만 post-write 경계를 최소 수정으로 분리했다."
  - "종료 전 targeted test, 전체 테스트, 실제 앱/H2/runtime 검증까지 수행했다."
next_task_warnings:
  - "TASK-0004에서는 targeted test green만으로 종료하지 않고 full suite와 실제 앱/H2 검증 근거까지 함께 묶어야 한다."
  - "github-api처럼 오버로드/브리지 메서드가 있는 타입은 Mockito stubbing보다 fake object를 우선 검토한다."
  - "후속 예외 체계 정리 spec으로 넘어갈 때도 내부 기록 실패와 외부 응답 계약을 다시 섞지 않는다."
error_signature: "ServletException caused by RuntimeException when recordCommentPosted(...) failed after comment success; CannotStubVoidMethodWithReturnValue on GHPullRequest/GHIssue comment(String) stubbing during full suite."
test_result_summary: "GitHubWebhookControllerTest targeted rerun green, GitHubCommentServiceTest stabilized and green, full suite green, local app boot + H2 file DB + representative runtime row 확인 완료."
snapshot_hashes: []
---

# 작업 회고

## 작업 요약
- `GitHubWebhookController`에서 successful comment 이후 경계를 분리했다.
- GitHub 코멘트 등록 성공 직후 `deliveryCache.record(deliveryId)`를 먼저 수행하고, `agentRuntimeService.recordCommentPosted(...)`는 비차단으로 처리하도록 바꿨다.
- controller black-box 테스트로 `200 OK`와 기존 `GitHubCommentResponse` 유지, same-delivery 재요청 시 추가 comment posting 미발생을 고정했다.
- 전체 테스트에서 드러난 `github-api` stubbing 취약점은 `GitHubCommentServiceTest`의 fake pull request 방식으로 안정화했다.

## 실패 요약
- 기존 구현은 GitHub 코멘트 등록 성공 이후 runtime 기록 실패가 그대로 예외 전파로 이어질 수 있었다.
- 그 결과 동일 delivery 재처리 시 추가 comment posting이 발생할 수 있는 빈틈이 있었다.
- targeted controller test는 의도대로 이 실패를 재현했고, 전체 테스트에서는 별도로 `CannotStubVoidMethodWithReturnValue`가 드러났다.

## Root Cause
- controller orchestration에서 성공 응답 계약과 내부 runtime 기록을 같은 성공 경로로 묶어두었다.
- `deliveryCache.record(deliveryId)`도 같은 try 안에 있어 runtime 기록 실패가 곧 cache 미기록으로 이어졌다.
- 외부 라이브러리 타입의 애매한 오버로드 메서드를 Mockito로 직접 stubbing한 테스트는 full suite에서 안정성이 떨어졌다.

## AGENTS 체크 결과
- `Tester`는 production code를 수정하지 않고 controller/service integration 중심 테스트부터 작성했다.
- `Constructor`는 controller 한 파일에서만 최소 범위로 post-write 경계를 분리했다.
- task 종료 전에 targeted test, 전체 테스트, 실제 앱/H2/runtime 검증까지 모두 수행했다.
- 새로운 `util`이나 과한 추상화는 추가하지 않았다.

## 근거 Artifact
- 기준 문서:
  - `.agents/active/spec.md`
  - `.agents/criteria/SPEC-0001-webhook-review-stabilization.json`
  - `.agents/active/tasks.md`
- 코드 변경:
  - `src/main/java/com/agilerunner/api/controller/GitHubWebhookController.java`
  - `src/test/java/com/agilerunner/api/controller/GitHubWebhookControllerTest.java`
  - `src/test/java/com/agilerunner/api/service/GitHubCommentServiceTest.java`
- 실행 근거:
  - `GitHubWebhookControllerTest` targeted rerun
  - `GitHubCommentServiceTest` rerun
  - 전체 `./gradlew test`
  - local profile bootRun
  - representative webhook delivery `task-0003-verify-001`
  - H2 query 결과 `RUN:task-0003-verify-001` row 확인

## 다음 Task 경고사항
- `TASK-0004`에서는 current spec의 필수 시나리오를 한 번에 묶되, full suite와 실제 앱/H2 검증 근거를 빠뜨리지 않는다.
- 외부 라이브러리 타입이 애매한 오버로드/브리지 메서드를 가지면 Mockito return stubbing보다 fake object를 먼저 검토한다.
- 후속 예외 체계 정리 spec에서도 내부 runtime 기록 실패와 외부 응답 계약을 같은 실패 분류로 묶지 않는다.

## 제안 필요 여부
- 있음
- `AGENTS-PROP-0002`로 외부 라이브러리 오버로드 메서드 테스트 더블 규칙 보강 제안을 남긴다.
