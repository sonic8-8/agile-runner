---
spec_id: SPEC-0005
task_id: TASK-0004
github_issue_number: 19
criteria_keys:
  - runtime-evidence-records-execution-control
delivery_ids:
  - task-0004-verify-20260407-214108-001
execution_keys:
  - EXECUTION:task-0004-verify-20260407-214108-001
test_evidence_ref:
  - "targeted: GRADLE_USER_HOME=/home/seaung13/workspace/agile-runner/.gradle-local scripts/gradlew-java21.sh --no-daemon test --tests 'com.agilerunner.api.service.agentruntime.AgentRuntimeServiceTest' --tests 'com.agilerunner.client.agentruntime.AgentRuntimeRepositoryTest' --tests 'com.agilerunner.api.controller.GitHubWebhookControllerTest' --tests 'com.agilerunner.api.service.GitHubCommentServiceTest' --tests 'com.agilerunner.api.service.OpenAiServiceTest' --console=plain"
  - "full: GRADLE_USER_HOME=/home/seaung13/workspace/agile-runner/.gradle-local scripts/gradlew-java21.sh --no-daemon test --console=plain"
  - "representative app/H2(normal path): local bootRun(port=18080) -> POST /webhook/github with delivery_id=task-0004-verify-20260407-214108-001 -> HTTP 500(GITHUB_APP_CONFIGURATION_MISSING) -> app shutdown -> H2 Shell query"
diff_ref: "git diff -- .agents/active/tasks.md src/main/java/com/agilerunner/api/controller/GitHubWebhookController.java src/main/java/com/agilerunner/api/service/agentruntime/AgentRuntimeService.java src/main/java/com/agilerunner/client/agentruntime/AgentRuntimeRepository.java src/main/java/com/agilerunner/domain/agentruntime/WebhookExecution.java src/main/java/com/agilerunner/domain/agentruntime/AgentExecutionLog.java src/main/resources/agent-runtime/schema.sql src/test/java/com/agilerunner/api/service/agentruntime/AgentRuntimeServiceTest.java src/test/java/com/agilerunner/client/agentruntime/AgentRuntimeRepositoryTest.java src/test/java/com/agilerunner/api/controller/GitHubWebhookControllerTest.java"
failure_summary: "초기 구현은 runtime evidence에 execution control 필드가 없었고, comment posting 결과만 GitHubCommentResponse 기준으로 기록해 dry-run과 normal execution evidence를 같은 execution_key 기준으로 구분할 수 없었다. 이후 실제 representative NORMAL 실행 검증에서는 review-generated 단계에서 GitHub App 설정 누락으로 실패했지만, execution control evidence 자체는 정상 적재됐다."
root_cause: "실행 제어 모드를 service-level 결과와 runtime persistence 두 층에서 동시에 다뤄야 한다는 점이 늦게 닫혔다. 특히 controller가 response만 runtime service에 넘기고 있어서 execution result의 mode/write 정보를 persistence 계층까지 전달하지 못했다."
agents_check_findings:
  - "runtime evidence 필드는 WebhookExecution과 AgentExecutionLog에 함께 두고, controller는 execution result를 runtime service에 전달만 하도록 정리했다."
  - "writeSkipReason은 의도적 no-write에만 채우는 nullable 의미로 정리했고, 완료 조건 문구도 그 의미에 맞게 좁혔다."
  - "대표 NORMAL 실제 앱 검증은 실패 응답이어도 same execution_key 기준 executionControlMode와 writePerformed가 적재되면 종료 근거로 사용했다."
next_task_warnings:
  - "다음 spec에서 외부 rerun 또는 공개 dry-run 진입점을 도입하면 representative actual-app verification에도 dry-run path를 포함할지 다시 정의해야 한다."
  - "execution result를 runtime service에 넘기는 seam이 생겼으므로, 이후 controller 변경 task에서는 response 계약과 runtime persistence 경계를 섞지 않도록 주의해야 한다."
  - "representative verification은 계속 fresh delivery_id를 사용하고 앱 종료 후 H2를 조회해야 한다."
error_signature: "execution control 정보는 service-level에만 있고 runtime persistence에는 전달되지 않거나, representative NORMAL 실패 실행에서 writePerformed 값이 비어 termination evidence가 약해짐"
test_result_summary: "targeted test, 전체 ./gradlew test, representative NORMAL actual-app/H2 verification 모두 통과했고 same execution_key 기준 NORMAL / FALSE / null evidence를 확인했다"
snapshot_hashes: []
---

# 작업 회고

## 작업 요약
- `WebhookExecution`과 `AgentExecutionLog`에 `executionControlMode`, `writePerformed`, `writeSkipReason`을 적재하도록 확장했다.
- `AgentRuntimeRepository`와 H2 스키마를 새 필드에 맞춰 정리했고, `GitHubWebhookController`가 `GitHubCommentExecutionResult`를 runtime service에 전달하도록 바꿨다.
- dry-run no-write 이유는 targeted test로 닫고, representative `NORMAL` actual-app/H2 verification으로 runtime evidence 적재를 확인했다.

## 실패 요약
- 초기 상태에서는 runtime evidence가 오류 코드와 대응 분류까지만 남아, execution control 모드와 write 수행 여부를 같은 `execution_key` 기준으로 확인할 수 없었다.
- representative `NORMAL` actual-app 검증은 `GITHUB_APP_CONFIGURATION_MISSING`로 실패했지만, 이 failure path에서도 `executionControlMode=NORMAL`, `writePerformed=FALSE`가 evidence에 남아야 했다.

## Root Cause
- execution control을 `GitHubCommentExecutionResult`로만 닫아 두고, runtime persistence까지 전달하는 경계를 함께 정리하지 못했다.
- `writeSkipReason`이 항상 채워지는 값인지, 의도적으로 write를 생략했을 때만 채워지는 값인지 문서 의미가 애매했다.

## AGENTS 체크 결과
- Tester는 production code를 수정하지 않고 `AgentRuntimeServiceTest`, `AgentRuntimeRepositoryTest`, `GitHubWebhookControllerTest`로 evidence field 기대를 먼저 고정했다.
- Constructor는 `recordExecutionResult` seam을 도입하고, domain/repository/schema/controller를 같은 필드 집합으로 정렬했다.
- targeted test와 전체 `./gradlew test`를 모두 확인했다.
- representative actual-app/H2 verification은 `NORMAL` path 1건으로 수행했고, 앱 종료 후 H2를 조회했다.

## 근거 Artifact
- `.agents/active/spec.md`
- `.agents/active/tasks.md`
- `.agents/criteria/SPEC-0005-execution-control-foundation.json`
- `src/main/java/com/agilerunner/api/controller/GitHubWebhookController.java`
- `src/main/java/com/agilerunner/api/service/agentruntime/AgentRuntimeService.java`
- `src/main/java/com/agilerunner/client/agentruntime/AgentRuntimeRepository.java`
- `src/main/java/com/agilerunner/domain/agentruntime/WebhookExecution.java`
- `src/main/java/com/agilerunner/domain/agentruntime/AgentExecutionLog.java`
- `src/main/resources/agent-runtime/schema.sql`
- `src/test/java/com/agilerunner/api/service/agentruntime/AgentRuntimeServiceTest.java`
- `src/test/java/com/agilerunner/client/agentruntime/AgentRuntimeRepositoryTest.java`
- `src/test/java/com/agilerunner/api/controller/GitHubWebhookControllerTest.java`

## 다음 Task 경고사항
- `SPEC-0006`에서 외부 rerun 또는 공개 dry-run 입력을 열면, representative actual-app verification 범위를 `NORMAL`만 둘지 다시 재정의해야 한다.
- runtime evidence는 now/failure path 모두 `executionControlMode`와 `writePerformed`를 남기는 전제를 유지해야 한다.
- `writeSkipReason`은 intentional no-write일 때만 적재하는 nullable 의미를 유지해야 한다.

## 제안 필요 여부
- 없음
- 이번 task의 교훈은 기존 task 경계와 representative verification 규칙을 적용해 completion wording을 더 명확히 한 수준이며, 새로운 AGENTS/workflow 규칙을 추가할 정도의 패턴은 나오지 않았다.
