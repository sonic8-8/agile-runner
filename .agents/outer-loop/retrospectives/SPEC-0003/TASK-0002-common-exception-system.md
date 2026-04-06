---
spec_id: SPEC-0003
task_id: TASK-0002
github_issue_number: 11
criteria_keys:
  - core-failure-paths-mapped-to-error-codes
delivery_ids:
  - task-0002-verify-002
execution_keys:
  - EXECUTION:task-0002-verify-002
test_evidence_ref:
  - "targeted: GRADLE_USER_HOME=/home/seaung13/workspace/agile-runner/.gradle-local scripts/gradlew-java21.sh --no-daemon test --tests 'com.agilerunner.api.controller.GitHubWebhookControllerTest' --tests 'com.agilerunner.api.controller.github.request.GitHubEventRequestTest' --tests 'com.agilerunner.api.service.OpenAiServiceTest' --tests 'com.agilerunner.api.service.GitHubCommentServiceTest' --console=plain"
  - "full: GRADLE_USER_HOME=/home/seaung13/workspace/agile-runner/.gradle-local scripts/gradlew-java21.sh --no-daemon test --console=plain"
  - "runtime: local profile bootRun + POST /webhook/github with delivery_id=task-0002-verify-002"
diff_ref: "git diff -- src/main/java/com/agilerunner/domain/exception src/main/java/com/agilerunner/api/controller/github/request/GitHubEventRequest.java src/main/java/com/agilerunner/api/service/OpenAiService.java src/main/java/com/agilerunner/api/service/GitHubCommentService.java src/main/java/com/agilerunner/client/github/auth/GitHubClientFactory.java src/main/java/com/agilerunner/config/OpenAiConfig.java src/test/java/com/agilerunner/api/controller/GitHubWebhookControllerTest.java src/test/java/com/agilerunner/api/controller/github/request/GitHubEventRequestTest.java src/test/java/com/agilerunner/api/service/OpenAiServiceTest.java src/test/java/com/agilerunner/api/service/GitHubCommentServiceTest.java"
failure_summary: "예외 타입과 오류 코드를 직접 고정하지 않으면 TASK-0001 안전망만으로는 공통 예외 체계 도입을 충분히 닫을 수 없었다."
root_cause: "처음에는 TASK-0001 회귀 테스트 재사용만으로 충분하다고 판단했지만, core-failure-paths-mapped-to-error-codes 기준은 실제 예외 타입과 오류 코드 매핑을 직접 검증해야 했다."
agents_check_findings:
  - "GitHubEventRequest는 controller request 패키지로 이동해 controller DTO 규칙에 맞췄다."
  - "GitHubClientFactory는 client 패키지로 이동하고 @Component로 바꿔 외부 연동 책임을 맞췄다."
  - "TASK-0002는 실행 근거 스키마를 건드리지 않고 예외 타입과 오류 코드 치환에 집중했다."
next_task_warnings:
  - "TASK-0003에서만 실행 근거 스키마와 적재 로직을 바꾸고, TASK-0002에서 만든 오류 코드 체계를 그대로 재사용해야 한다."
  - "실제 앱/H2 대표 검증은 항상 fresh delivery_id를 사용해야 한다."
error_signature: "기존 safety net만으로는 오류 코드 매핑 기준을 닫지 못해 대표 실패 경로별 매핑 테스트가 추가로 필요했음"
test_result_summary: "targeted test와 전체 ./gradlew test 모두 통과, local profile 실제 앱 기동과 H2 적재 확인 완료"
snapshot_hashes: []
---

# 작업 회고

## 작업 요약
- `AgileRunnerException`과 `ErrorCode`를 도입해 요청 정보 해석, OpenAI 설정/리뷰 생성, GitHub App 설정, 코멘트 준비/등록 실패를 공통 예외 체계로 정리했다.
- `GitHubEventRequest`를 controller request 위치로 옮기고, `GitHubClientFactory`를 `client` 패키지로 이동해 책임을 맞췄다.
- `TASK-0001` 안전망 위에 예외 타입과 오류 코드 매핑 테스트를 추가해 `TASK-0002` 기준을 직접 닫았다.

## 실패 요약
- 첫 판단에서는 `TASK-0001`의 안전망 재사용만으로 충분하다고 봤지만, 실제로는 오류 코드 매핑을 직접 고정하는 테스트가 더 필요했다.
- 구현 중에는 `installation.id` 누락/형식 오류와 `GitHubClientFactory`의 위치/설정 해석 예외가 추가 정리 포인트로 드러났다.

## Root Cause
- 회귀 테스트와 예외 매핑 테스트의 역할을 처음에 분리하지 못했다.
- 기존 구조에 남아 있던 패키지 책임 어긋남과 요청 정보 해석의 예외 누수가 공통 예외 체계 도입 과정에서 함께 드러났다.

## AGENTS 체크 결과
- controller request DTO 위치를 `api/controller/github/request`로 맞췄다.
- 외부 시스템 연동 구현을 `client/github/auth`로 옮겼다.
- 예외 체계 도입 범위를 실행 근거 스키마 변경 전 단계로 제한했다.
- targeted test와 전체 `./gradlew test`를 모두 확인했다.

## 근거 Artifact
- `src/main/java/com/agilerunner/domain/exception/AgileRunnerException.java`
- `src/main/java/com/agilerunner/domain/exception/ErrorCode.java`
- `src/main/java/com/agilerunner/api/controller/github/request/GitHubEventRequest.java`
- `src/main/java/com/agilerunner/api/service/OpenAiService.java`
- `src/main/java/com/agilerunner/api/service/GitHubCommentService.java`
- `src/main/java/com/agilerunner/client/github/auth/GitHubClientFactory.java`
- `src/main/java/com/agilerunner/config/OpenAiConfig.java`
- `src/test/java/com/agilerunner/api/controller/GitHubWebhookControllerTest.java`
- `src/test/java/com/agilerunner/api/controller/github/request/GitHubEventRequestTest.java`
- `src/test/java/com/agilerunner/api/service/OpenAiServiceTest.java`
- `src/test/java/com/agilerunner/api/service/GitHubCommentServiceTest.java`

## 다음 Task 경고사항
- `TASK-0003`에서는 오류 코드 체계를 다시 바꾸지 말고 실행 근거 적재에만 집중한다.
- representative runtime 검증은 같은 delivery_id를 재사용하지 않는다.

## 제안 필요 여부
- 없음
- 이번 task의 교훈은 기존 AGENTS 규칙 안에서 파일 위치와 테스트 범위를 더 정확히 적용한 수준으로 정리됐다.
