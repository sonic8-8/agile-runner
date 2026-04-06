---
spec_id: SPEC-0003
task_id: TASK-0003
github_issue_number: 12
criteria_keys:
  - runtime-evidence-records-error-code
delivery_ids:
  - task-0003-verify-003
execution_keys:
  - EXECUTION:task-0003-verify-003
test_evidence_ref:
  - "targeted: GRADLE_USER_HOME=/home/seaung13/workspace/agile-runner/.gradle-local scripts/gradlew-java21.sh --no-daemon test --tests 'com.agilerunner.api.service.agentruntime.AgentRuntimeServiceTest' --tests 'com.agilerunner.client.agentruntime.AgentRuntimeRepositoryTest' --tests 'com.agilerunner.api.controller.GitHubWebhookControllerTest' --tests 'com.agilerunner.api.service.GitHubCommentServiceTest' --tests 'com.agilerunner.api.service.OpenAiServiceTest' --console=plain"
  - "full: GRADLE_USER_HOME=/home/seaung13/workspace/agile-runner/.gradle-local scripts/gradlew-java21.sh --no-daemon test --console=plain"
  - "runtime: local profile bootRun + POST /webhook/github with delivery_id=task-0003-verify-003"
diff_ref: "git diff -- src/main/java/com/agilerunner/domain/agentruntime/WebhookExecution.java src/main/java/com/agilerunner/domain/agentruntime/AgentExecutionLog.java src/main/java/com/agilerunner/api/service/agentruntime/AgentRuntimeService.java src/main/java/com/agilerunner/client/agentruntime/AgentRuntimeRepository.java src/main/java/com/agilerunner/api/controller/GitHubWebhookController.java src/main/resources/agent-runtime/schema.sql src/test/java/com/agilerunner/api/service/agentruntime/AgentRuntimeServiceTest.java src/test/java/com/agilerunner/client/agentruntime/AgentRuntimeRepositoryTest.java src/test/java/com/agilerunner/api/controller/GitHubWebhookControllerTest.java"
failure_summary: "실패 실행 근거에 errorCode를 적재하려면 저장소와 H2 스키마뿐 아니라, controller에 남아 있던 일반 예외 분기까지 AgileRunnerException 기준으로 맞춰야 했다."
root_cause: "초기 구현은 recordFailure 경로와 저장소 적재에는 초점을 맞췄지만, null review처럼 일반 예외를 직접 만드는 분기가 남아 있어 representative failure에서 error_code가 비어 있을 가능성을 완전히 닫지 못했다."
agents_check_findings:
  - "오류 코드 적재 범위는 실패 경로로 제한하고, 성공 경로는 null 유지 확인으로만 닫았다."
  - "대표 검증은 fresh delivery_id와 같은 execution_key 기준으로 WEBHOOK_EXECUTION, AGENT_EXECUTION_LOG를 함께 확인했다."
  - "controller, service, repository, schema 변경은 TASK-0003 범위를 넘지 않고 runtime evidence 적재에 집중했다."
next_task_warnings:
  - "SPEC-0003 summary 단계에서 예외 체계 task들은 실제 실행 근거까지 확인했는지 다시 점검해야 한다."
  - "대표 실패 검증은 실패 시나리오를 고정하고, 같은 execution_key 기준으로 모든 evidence 테이블을 함께 확인해야 한다."
error_signature: "일반 예외 분기 하나만 남아 있어도 runtime error_code가 null로 남을 수 있음"
test_result_summary: "targeted rerun과 전체 ./gradlew test 모두 통과했고, local profile 실제 앱 기동 후 H2에서 WEBHOOK_EXECUTION/AGENT_EXECUTION_LOG 오류 코드를 직접 확인했다."
snapshot_hashes: []
---

# 작업 회고

## 작업 요약
- `WebhookExecution`과 `AgentExecutionLog`에 `errorCode`를 추가하고, `AgentRuntimeRepository`와 H2 스키마를 이에 맞게 정리했다.
- `AgentRuntimeService.recordFailure(...)`가 `AgileRunnerException`의 `ErrorCode`를 실행 근거에 남기도록 바꿨다.
- 대표 실패 실행 `task-0003-verify-003`에서 `WEBHOOK_EXECUTION.error_code`와 `AGENT_EXECUTION_LOG.error_code`를 같은 `execution_key` 기준으로 확인했다.

## 실패 요약
- 첫 구현에서는 저장소와 service 적재 경로는 맞췄지만, controller의 `review == null` 분기가 일반 예외를 만들어 runtime errorCode가 비는 경로가 남아 있었다.
- Constructor 리뷰에서 이 누락이 드러나 추가 보정과 회귀 테스트가 필요했다.

## Root Cause
- 실패 실행 근거 적재 task를 진행하면서도 "모든 실패 분기가 이미 `AgileRunnerException + ErrorCode`로 정리돼 있다"는 가정을 너무 일찍 했다.
- 실제 representative verification 관점에서는 controller/service/domain 저장소를 함께 훑어봐야 null errorCode 누수를 막을 수 있었다.

## AGENTS 체크 결과
- `TASK-0003` 범위는 실행 근거 적재와 검증으로 제한했고, 예외 응답 스키마나 재시도 정책으로 확장하지 않았다.
- `Tester`는 runtime service/repository 테스트와 controller/service 회귀를 먼저 고정했고, 그 뒤 Constructor가 적재 로직을 맞췄다.
- 종료 검증에서 targeted/full test와 local profile 실제 앱/H2 representative verification을 모두 수행했다.

## 근거 Artifact
- `src/main/java/com/agilerunner/domain/agentruntime/WebhookExecution.java`
- `src/main/java/com/agilerunner/domain/agentruntime/AgentExecutionLog.java`
- `src/main/java/com/agilerunner/api/service/agentruntime/AgentRuntimeService.java`
- `src/main/java/com/agilerunner/client/agentruntime/AgentRuntimeRepository.java`
- `src/main/resources/agent-runtime/schema.sql`
- `src/main/java/com/agilerunner/api/controller/GitHubWebhookController.java`
- `src/test/java/com/agilerunner/api/service/agentruntime/AgentRuntimeServiceTest.java`
- `src/test/java/com/agilerunner/client/agentruntime/AgentRuntimeRepositoryTest.java`
- `src/test/java/com/agilerunner/api/controller/GitHubWebhookControllerTest.java`

## 다음 Task 경고사항
- `SPEC-0003` summary에서는 예외 체계가 코드 수준뿐 아니라 runtime evidence까지 일관되게 남는지 다시 요약해야 한다.
- 이후 실패 대응 강화 spec에서는 errorCode만 믿지 말고 대표 실패 시나리오와 execution_key 기준 evidence 묶음까지 함께 남겨야 한다.

## 제안 필요 여부
- 없음
- 이번 task의 교훈은 기존 workflow 안에서 representative verification 범위를 더 엄밀히 적용한 수준으로 정리됐다.
