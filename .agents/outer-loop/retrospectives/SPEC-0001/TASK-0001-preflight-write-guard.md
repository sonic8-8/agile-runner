---
spec_id: SPEC-0001
task_id: TASK-0001
github_issue_number: 1
criteria_keys:
  - payload-accepted
  - comment-write-preflight-completed
delivery_ids: []
execution_keys: []
test_evidence_ref: "GRADLE_USER_HOME=/home/seaung13/workspace/agile-runner/.gradle-local scripts/gradlew-java21.sh --no-daemon test --tests 'com.agilerunner.api.service.GitHubCommentServiceTest' --tests 'com.agilerunner.api.controller.GitHubWebhookControllerTest' --console=plain"
diff_ref: "git diff -- src/main/java/com/agilerunner/api/service/GitHubCommentService.java src/test/java/com/agilerunner/api/service/GitHubCommentServiceTest.java src/test/java/com/agilerunner/api/controller/GitHubWebhookControllerTest.java"
failure_summary: "inline comment 준비 실패를 skip하면서 main comment write가 먼저 실행될 수 있었다."
root_cause: "preflight 단계의 inline 준비 실패를 Optional.empty로 흘려보내 전체 preflight failure가 아닌 부분 skip으로 처리했다."
agents_check_findings:
  - "Tester가 controller/service integration 중심 black-box 테스트를 먼저 작성했다."
  - "본문 comment뿐 아니라 inline write도 미발생인지 함께 검증하도록 테스트를 보강했다."
next_task_warnings:
  - "TASK-0002는 successful response contract 유지에만 집중하고 delivery cache나 runtime failure 처리까지 섞지 않는다."
  - "preflight failure와 inline comment write 단계의 skip 정책을 다시 혼동하지 않는다."
error_signature: "NeverWantedButInvoked: pullRequest.comment(...) was invoked when inline preparation failed."
test_result_summary: "GitHubCommentServiceTest와 GitHubWebhookControllerTest targeted rerun green."
snapshot_hashes: []
---

# 작업 회고

## 작업 요약
- `GitHubCommentService`에서 preflight 준비와 실제 GitHub write 단계를 분리했다.
- inline comment 준비 중 `path` 또는 `position` 계산 실패가 발생하면 preflight 전체를 실패로 처리하도록 바꿨다.
- `TASK-0001` 기준 black-box 테스트를 보강해 본문 comment와 inline comment가 모두 미발생인지 확인하도록 고정했다.

## 실패 요약
- 기존 구현은 patch build 실패만 write 이전 실패로 취급하고, inline comment 준비 실패는 skip 처리했다.
- 그 결과 inline 준비에 실패해도 main comment가 먼저 등록될 수 있었다.
- tester 기준에서 이 문제는 `GitHubCommentServiceTest`의 `NeverWantedButInvoked` 실패로 재현됐다.

## Root Cause
- preflight 단계의 책임 경계가 불명확했다.
- inline comment 후보 준비 실패를 "개별 skip"으로 처리하면서, 실제로는 write 이전 준비 실패인데도 전체 흐름이 계속 진행됐다.
- 이 때문에 `pullRequest.comment(...)`가 preflight 완료 보장 없이 실행될 수 있었다.

## AGENTS 체크 결과
- `Tester`는 production code를 건드리지 않고 black-box 테스트부터 작성했다.
- `Constructor`는 `GitHubCommentService` 안에서만 최소 범위로 구조를 조정했다.
- 새로운 `util` 패키지나 과한 추상화는 추가하지 않았다.
- controller 테스트는 request 변환과 흐름 진입까지만 검증하도록 범위를 좁혔다.

## 근거 Artifact
- 기준 문서:
  - `.agents/active/spec.md`
  - `.agents/criteria/SPEC-0001-webhook-review-stabilization.json`
  - `.agents/active/tasks.md`
- 코드 변경:
  - `src/main/java/com/agilerunner/api/service/GitHubCommentService.java`
  - `src/test/java/com/agilerunner/api/service/GitHubCommentServiceTest.java`
  - `src/test/java/com/agilerunner/api/controller/GitHubWebhookControllerTest.java`
- 테스트:
  - `GitHubCommentServiceTest`
  - `GitHubWebhookControllerTest`

## 다음 Task 경고사항
- `TASK-0002`에서는 response body와 HTTP status 유지 외 범위를 넓히지 않는다.
- `TASK-0003`에서 다룰 runtime failure non-blocking과 `deliveryCache.record(deliveryId)` 보장을 현재 task에 섞지 않는다.
- inline comment 작성 단계의 `IOException` skip 정책은 유지 대상이고, preflight 실패와 구분해서 다뤄야 한다.

## 제안 필요 여부
- 있음
- `AGENTS-PROP-0001`으로 issue 1:1 확인과 all-write-path 검증 체크리스트 보강 제안을 남긴다.
