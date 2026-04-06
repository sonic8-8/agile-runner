---
spec_id: SPEC-0004
task_id: TASK-0002
github_issue_number: 14
criteria_keys:
  - failure-disposition-classified-consistently
delivery_ids: []
execution_keys: []
test_evidence_ref:
  - "targeted: GRADLE_USER_HOME=/home/seaung13/workspace/agile-runner/.gradle-local scripts/gradlew-java21.sh --no-daemon test --tests 'com.agilerunner.domain.exception.FailureDispositionPolicyTest' --tests 'com.agilerunner.domain.exception.FailureResponseExpectationTest' --tests 'com.agilerunner.api.service.OpenAiServiceTest' --tests 'com.agilerunner.api.controller.GitHubWebhookControllerTest' --tests 'com.agilerunner.api.service.GitHubCommentServiceTest' --console=plain"
  - "full: GRADLE_USER_HOME=/home/seaung13/workspace/agile-runner/.gradle-local scripts/gradlew-java21.sh --no-daemon test --console=plain"
diff_ref: "git diff -- .agents/active/spec.md .agents/active/tasks.md .agents/criteria/SPEC-0004-failure-response-hardening.json src/main/java/com/agilerunner/domain/exception/FailureDispositionPolicy.java src/test/java/com/agilerunner/domain/exception/FailureDispositionPolicyTest.java src/test/java/com/agilerunner/api/service/OpenAiServiceTest.java"
failure_summary: "처음에는 정책 객체 자체 매핑만 고정했고, 실제 서비스 소비 경계에서 같은 분류 기준으로 해석되는지까지는 잡지 못했다."
root_cause: "실패 대응 분류 정책을 순수 도메인 정책으로만 보면서, AGENTS가 요구하는 controller/service integration 중심 기대 동작 고정을 충분히 반영하지 못했다."
agents_check_findings:
  - "domain/exception 아래에 순수 정책 객체만 추가해 계층 책임을 넘지 않았다."
  - "핵심 6개 ErrorCode만 명시적으로 분류하고, runtime 적재와 H2 검증은 TASK-0003으로 남겨 현재 task 범위를 유지했다."
  - "Tester 1 보강으로 OpenAiService 실제 실패를 같은 분류 기준으로 해석하는 소비 경계 테스트를 추가했다."
next_task_warnings:
  - "TASK-0003은 분류 정책을 다시 바꾸지 말고, WebhookExecution과 AgentExecutionLog 적재 및 실제 H2 검증에 집중해야 한다."
  - "실제 앱/H2 representative verification에는 이전 검증과 겹치지 않는 fresh delivery_id를 사용해야 한다."
error_signature: "정책 자체 테스트만으로는 service boundary 소비 기준이 충분히 드러나지 않음"
test_result_summary: "targeted test와 전체 ./gradlew test 모두 통과, 추가 proposal 없음"
snapshot_hashes: []
---

# 작업 회고

## 작업 요약
- 핵심 6개 `ErrorCode`를 `FailureDisposition`으로 분류하는 정책 객체를 도입했다.
- `AgileRunnerException` 입력도 같은 기준으로 분류하도록 오버로드를 추가했다.
- `OpenAiService` 실제 실패를 같은 정책으로 해석하는 테스트를 넣어 서비스 소비 경계까지 고정했다.

## 실패 요약
- 처음에는 `FailureDispositionPolicy` 자체의 매핑만 테스트로 고정했다.
- 그 상태로는 실제 서비스 실패가 같은 기준으로 해석되는지 드러나지 않아, Tester 1 재리뷰에서 보강 요청이 나왔다.

## Root Cause
- 실패 대응 분류 정책을 순수 도메인 규칙으로만 보고, 기존 AGENTS의 consumer boundary 확인 규칙을 충분히 적용하지 못했다.
- `TASK-0002` 범위를 좁게 유지하려다 보니 서비스 경계 검증이 빠졌고, 그 결과 정책 단위 테스트만 남았다.

## AGENTS 체크 결과
- `domain/exception` 아래 순수 정책 객체로 유지해 패키지 책임을 넘지 않았다.
- 기존 웹훅 성공/조기 종료 회귀 테스트를 같이 돌려 현재 계약이 깨지지 않았음을 확인했다.
- 전체 `./gradlew test`까지 통과했고, 이번 task는 webhook/controller/orchestration/runtime 저장 변경이 아니라 실제 앱/H2 검증은 생략했다.

## 근거 Artifact
- `.agents/active/spec.md`
- `.agents/active/tasks.md`
- `.agents/criteria/SPEC-0004-failure-response-hardening.json`
- `src/main/java/com/agilerunner/domain/exception/FailureDispositionPolicy.java`
- `src/test/java/com/agilerunner/domain/exception/FailureDispositionPolicyTest.java`
- `src/test/java/com/agilerunner/domain/exception/FailureResponseExpectationTest.java`
- `src/test/java/com/agilerunner/api/service/OpenAiServiceTest.java`
- `src/test/java/com/agilerunner/api/controller/GitHubWebhookControllerTest.java`
- `src/test/java/com/agilerunner/api/service/GitHubCommentServiceTest.java`

## 다음 Task 경고사항
- `TASK-0003`에서는 분류 정책 자체를 다시 바꾸지 말고, 실행 근거 적재와 실제 앱/H2 검증으로 범위를 고정해야 한다.
- representative failure 검증은 기존 local H2 row와 겹치지 않는 fresh `delivery_id`를 먼저 정한 뒤 진행해야 한다.

## 제안 필요 여부
- 없음
- 이번 task의 보정은 기존 AGENTS의 consumer boundary 확인 규칙을 더 정확히 적용한 수준이라 새 규칙 제안까지는 필요하지 않았다.
