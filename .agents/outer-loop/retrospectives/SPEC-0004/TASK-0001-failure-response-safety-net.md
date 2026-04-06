---
spec_id: SPEC-0004
task_id: TASK-0001
github_issue_number: 13
criteria_keys:
  - webhook-contract-preserved-after-failure-hardening
delivery_ids: []
execution_keys: []
test_evidence_ref:
  - "targeted: GRADLE_USER_HOME=/home/seaung13/workspace/agile-runner/.gradle-local scripts/gradlew-java21.sh --no-daemon test --tests 'com.agilerunner.api.controller.GitHubWebhookControllerTest' --tests 'com.agilerunner.api.service.GitHubCommentServiceTest' --tests 'com.agilerunner.api.service.OpenAiServiceTest' --tests 'com.agilerunner.domain.exception.FailureResponseExpectationTest' --console=plain"
  - "full: GRADLE_USER_HOME=/home/seaung13/workspace/agile-runner/.gradle-local scripts/gradlew-java21.sh --no-daemon test --console=plain"
diff_ref: "git diff -- .agents/active/spec.md .agents/active/tasks.md .agents/criteria/SPEC-0004-failure-response-hardening.json src/main/java/com/agilerunner/domain/exception/FailureDisposition.java src/test/java/com/agilerunner/domain/exception/FailureResponseExpectationTest.java"
failure_summary: "기대 대응 기준을 안전망으로 먼저 고정하는 과정에서 production seam 없이 test-local 구조만으로는 다음 task가 바로 재사용할 기준이 부족했다."
root_cause: "처음에는 기대 대응 기준을 테스트 로컬 enum과 map으로만 고정하려 했지만, 이후 task가 같은 분류 개념을 바로 재사용하려면 최소한의 production 타입이 먼저 필요했다."
agents_check_findings:
  - "실패 대응 강화는 아직 정책 도입 전 단계이므로 기존 웹훅 회귀 안전망은 그대로 재사용했다."
  - "최소 타입 도입만 허용하도록 active task 범위를 좁게 보강했고, 실제 분류 정책과 runtime 적재는 뒤 task로 남겼다."
  - "이번 task는 webhook/controller/orchestration/runtime 저장 로직 변경이 아니어서 full test까지로 종료 검증을 닫았다."
next_task_warnings:
  - "TASK-0002는 FailureDisposition 타입을 재사용하되, 실제 ErrorCode 매핑 정책 도입까지만 진행하고 runtime evidence 적재는 TASK-0003에 남겨야 한다."
  - "기대 대응 기준 테스트는 policy 구현 후 그대로 재사용하거나 policy 대상 검증으로 연결되어야 한다."
error_signature: "test-local 기대값만으로는 다음 task에서 재사용 가능한 production seam이 부족함"
test_result_summary: "targeted test와 전체 ./gradlew test 모두 통과, 추가 proposal 없음"
snapshot_hashes: []
---

# 작업 회고

## 작업 요약
- 기존 웹훅 성공/조기 종료 회귀를 유지한 채, 핵심 `ErrorCode` 6개의 기대 대응 기준을 먼저 고정했다.
- 이후 `TASK-0002`가 바로 쓸 수 있도록 최소 production 타입 `FailureDisposition`만 먼저 도입했다.
- 기존 controller/service 회귀 테스트와 새 기대 대응 기준 테스트를 함께 돌려 safety net을 닫았다.

## 실패 요약
- 처음에는 기대 대응 기준을 테스트 로컬 enum과 map으로만 고정하려 했다.
- 하지만 그 방식만으로는 다음 task가 같은 분류 개념을 직접 재사용하기 어려워, 최소 production seam이 필요하다는 리뷰가 나왔다.

## Root Cause
- 안전망 task라도 “다음 task가 재사용할 최소 개념 타입”까지는 어디에 둘지 기준이 처음부터 명확하지 않았다.
- tester 단계와 constructor 단계의 경계를 너무 기계적으로 나누면, 최소 production seam조차 둘 수 없는 상황이 생길 수 있었다.

## AGENTS 체크 결과
- 기존 회귀 테스트를 우선 재사용했고, 부족한 부분만 새 테스트를 추가했다.
- 실제 분류 정책 도입과 runtime 적재는 `TASK-0002`, `TASK-0003`로 남겨 현재 task 범위를 넘지 않게 유지했다.
- 전체 `./gradlew test`까지 통과해 현재 기준선이 깨지지 않음을 확인했다.

## 근거 Artifact
- `.agents/active/spec.md`
- `.agents/active/tasks.md`
- `.agents/criteria/SPEC-0004-failure-response-hardening.json`
- `src/main/java/com/agilerunner/domain/exception/FailureDisposition.java`
- `src/test/java/com/agilerunner/domain/exception/FailureResponseExpectationTest.java`
- `src/test/java/com/agilerunner/api/controller/GitHubWebhookControllerTest.java`
- `src/test/java/com/agilerunner/api/service/GitHubCommentServiceTest.java`
- `src/test/java/com/agilerunner/api/service/OpenAiServiceTest.java`

## 다음 Task 경고사항
- `TASK-0002`는 `FailureDisposition` 도입을 다시 논의하지 말고, `ErrorCode -> FailureDisposition` 정책 구현에만 집중해야 한다.
- `TASK-0002`에서도 기존 웹훅 성공/조기 종료 회귀와 기대 대응 기준 테스트를 함께 유지해야 한다.

## 제안 필요 여부
- 없음
- 이번 task의 교훈은 active task 범위를 더 명확히 적용한 수준이며, 새로운 AGENTS/workflow 규칙까지는 필요하지 않았다.
