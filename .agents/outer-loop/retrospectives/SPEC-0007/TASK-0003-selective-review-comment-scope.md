---
spec_id: SPEC-0007
task_id: TASK-0003
github_issue_number: 26
criteria_keys:
  - selected-paths-limit-review-and-comment-scope
delivery_ids: []
execution_keys: []
test_evidence_ref:
  - "targeted: ./scripts/gradlew-java21.sh --no-daemon cleanTest test --tests 'com.agilerunner.api.service.OpenAiServiceTest' --tests 'com.agilerunner.api.service.GitHubCommentServiceTest' --tests 'com.agilerunner.api.service.review.ManualRerunServiceTest' --tests 'com.agilerunner.api.controller.review.ManualRerunControllerTest' --tests 'com.agilerunner.api.controller.GitHubWebhookControllerTest' --console=plain"
  - "full: ./scripts/gradlew-java21.sh --no-daemon cleanTest test --console=plain"
diff_ref: "git diff -- src/main/java/com/agilerunner/api/service/OpenAiService.java src/main/java/com/agilerunner/api/service/GitHubCommentService.java src/test/java/com/agilerunner/api/service/OpenAiServiceTest.java src/test/java/com/agilerunner/api/service/GitHubCommentServiceTest.java"
failure_summary: "없음"
root_cause: "해당 없음"
agents_check_findings:
  - "선택 경로 필터링은 `OpenAiService`, `GitHubCommentService`에만 추가돼 과도한 범위 확장이 없었다."
  - "runtime evidence 적재와 actual-app/H2 representative 검증은 `TASK-0004` 경계로 유지했다."
  - "선택 경로 비어 있음은 기존 전체 실행 유지, no-match는 빈 리뷰 입력과 경로 기반 inline comment 없음으로 정리했다."
next_task_warnings:
  - "TASK-0004는 runtime evidence 적재와 representative actual-app/H2 검증만 다루고, 선택 경로 제한 로직 자체를 다시 건드리지 않아야 한다."
  - "대표 검증에는 fresh execution key와 선택 경로 1~2개를 사용하고, 앱 종료 후 H2를 조회해야 한다."
error_signature: "none"
test_result_summary: "selected path filtering 관련 targeted test와 전체 test가 모두 green이었다."
snapshot_hashes: []
---

# 작업 회고

## 작업 요약
- `OpenAiService`에서 `selectedPaths` 기준으로 리뷰 생성 입력 patch 목록을 필터링했다.
- `GitHubCommentService`에서 `selectedPaths` 기준으로 경로 기반 inline comment 대상과 path map을 필터링했다.
- `OpenAiServiceTest`, `GitHubCommentServiceTest`에 선택 경로 적용, 빈 목록 유지, no-match 성공 계약을 black-box 기준으로 고정했다.

## 실패 요약
- 없음

## Root Cause
- 해당 없음

## AGENTS 체크 결과
- 구현 범위는 `OpenAiService`, `GitHubCommentService`와 관련 테스트로 제한했다.
- `selectedPaths`가 비어 있으면 기존 전체 실행을 유지하는 기준을 테스트로 직접 고정했다.
- actual-app/H2 representative 검증은 runtime evidence 적재 변경이 없는 task라 비대상으로 정리했다.

## 근거 Artifact
- `src/main/java/com/agilerunner/api/service/OpenAiService.java`
- `src/main/java/com/agilerunner/api/service/GitHubCommentService.java`
- `src/test/java/com/agilerunner/api/service/OpenAiServiceTest.java`
- `src/test/java/com/agilerunner/api/service/GitHubCommentServiceTest.java`
- `src/test/java/com/agilerunner/api/service/review/ManualRerunServiceTest.java`
- `src/test/java/com/agilerunner/api/controller/review/ManualRerunControllerTest.java`
- `src/test/java/com/agilerunner/api/controller/GitHubWebhookControllerTest.java`

## 다음 Task 경고사항
- `TASK-0004`는 선택 실행 근거 적재와 representative actual-app/H2 검증에 집중하고, 이번 task의 필터링 로직 범위를 다시 넓히지 않아야 한다.
- representative 검증은 fresh `delivery_id` 또는 `executionKey`를 사용하고 앱 종료 후 H2를 조회해야 한다.

## 제안 필요 여부
- 없음
- 이번 교훈은 새 workflow 부족이 아니라, 이미 정해진 task 경계를 그대로 지켜 구현과 검증을 닫은 사례였다.
