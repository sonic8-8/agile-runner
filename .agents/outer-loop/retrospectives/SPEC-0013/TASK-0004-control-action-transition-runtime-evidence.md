---
spec_id: SPEC-0013
task_id: TASK-0004
github_issue_number: 51
criteria_keys:
  - manual-rerun-control-secondary-runtime-evidence-aligned
delivery_ids:
  - MANUAL_RERUN_DELIVERY:a547ffbe-6e9e-448f-9b4b-59eabb92337f
execution_keys:
  - EXECUTION:MANUAL_RERUN:a547ffbe-6e9e-448f-9b4b-59eabb92337f
test_evidence_ref:
  - "targeted: ./scripts/gradlew-java21.sh --no-daemon test --tests 'com.agilerunner.domain.review.ManualRerunAvailableActionPolicyTest' --tests 'com.agilerunner.api.service.review.ManualRerunControlActionServiceTest' --tests 'com.agilerunner.api.service.review.ManualRerunQueryServiceTest' --tests 'com.agilerunner.api.service.review.ManualRerunExecutionListServiceTest' --tests 'com.agilerunner.client.agentruntime.AgentRuntimeRepositoryTest' --tests 'com.agilerunner.api.controller.review.ManualRerunControllerTest' --console=plain"
  - "full: ./scripts/gradlew-java21.sh --no-daemon cleanTest test --console=plain"
  - "actual app: SPRING_PROFILES_ACTIVE=local SERVER_PORT=18080 GRADLE_USER_HOME=/home/seaung13/workspace/agile-runner/.gradle-local ./scripts/gradlew-java21.sh --no-daemon bootRun --console=plain"
  - "representative HTTP: POST /reviews/rerun -> POST /reviews/rerun/{executionKey}/actions ACKNOWLEDGE -> GET /reviews/rerun/{executionKey} -> GET /reviews/rerun/executions -> POST /reviews/rerun/{executionKey}/actions UNACKNOWLEDGE -> GET /reviews/rerun/{executionKey} -> GET /reviews/rerun/executions"
  - "H2 query: java -cp ~/.gradle/.../h2-2.3.232.jar org.h2.tools.Shell -url jdbc:h2:file:/home/seaung13/.agile-runner/agent-runtime/agile-runner"
diff_ref: "git diff -- .agents/outer-loop/retrospectives/SPEC-0013/TASK-0004-control-action-transition-runtime-evidence.md .agents/outer-loop/retrospectives/SPEC-0013/SPEC-0013-summary.md .agents/outer-loop/registry.json"
failure_summary: "코드 수정 없이 representative actual app/H2 흐름이 통과했다. 중간 실패는 없었고, local 환경의 GITHUB_APP_CONFIGURATION_MISSING가 예상대로 MANUAL_ACTION_REQUIRED 실행을 만들어 관리자 액션 전환 검증에 사용됐다."
root_cause: "TASK-0003에서 마지막 applied action 기반 상태 계산으로 policy/query/list 의미를 맞춰둔 덕분에, representative execution에서도 ACKNOWLEDGE와 UNACKNOWLEDGE 전환이 같은 executionKey 기준으로 자연스럽게 정렬됐다."
agents_check_findings:
  - "3개 서브에이전트 기준으로 이번 task는 실제 앱/H2 representative 검증 전담 단계라 추가 workflow proposal 없이 마감해도 된다고 정리됐다."
  - "query/list는 action detail을 직접 노출하지 않고 availableActions만 재계산하는 현재 경계를 유지한 점이 적절하다고 확인됐다."
next_task_warnings:
  - "후속 spec에서 관리자 액션 이력을 조회로 확장할 때는 query/list availableActions와 action history가 서로 다른 역할을 가진다는 점을 유지해야 한다."
  - "대표 검증에 쓰는 executionKey와 delivery_id는 summary에도 같이 남겨 다음 spec 온보딩에 재사용하지 않도록 해야 한다."
error_signature: "representative verification success: ACKNOWLEDGE -> UNACKNOWLEDGE response/query-list/H2 audit alignment"
test_result_summary: "targeted test, full cleanTest test, 실제 앱/H2 representative 검증이 모두 통과했다."
snapshot_hashes: []
---

# 작업 회고

## 작업 요약
- local profile 실제 앱에서 manual rerun execution 1건을 만들고 `ACKNOWLEDGE -> UNACKNOWLEDGE`를 순서대로 실행했다.
- action 응답, 단건 query, 목록 query, H2 `WEBHOOK_EXECUTION`, `MANUAL_RERUN_CONTROL_ACTION_AUDIT`를 같은 executionKey 기준으로 대조했다.
- `ACKNOWLEDGE` 후에는 `UNACKNOWLEDGE`, `UNACKNOWLEDGE` 후에는 다시 `ACKNOWLEDGE`가 노출되는지 실제 응답에서 확인했다.

## 실패 요약
- 대표 검증 자체는 실패 없이 통과했다.
- rerun 실행은 local 환경 특성상 `GITHUB_APP_CONFIGURATION_MISSING`로 실패했지만, 이 실패가 `MANUAL_ACTION_REQUIRED` 상태를 만들어 관리자 액션 전환 검증에는 오히려 적절한 representative source가 되었다.

## Root Cause
- `TASK-0003`에서 boolean 대신 마지막 applied action을 기준으로 상태를 계산하도록 바꾼 덕분에, 실제 앱에서도 응답과 query/list, H2 audit row가 같은 의미를 유지할 수 있었다.
- `ACKNOWLEDGE`와 `UNACKNOWLEDGE`가 모두 같은 executionKey에 누적되고, query/list는 audit 최신 상태만 보고 availableActions를 계산하도록 정리한 점이 핵심이었다.

## AGENTS 체크 결과
- targeted test와 full cleanTest test를 순차 실행했다.
- representative actual app/H2 검증은 `앱 기동 -> rerun 생성 -> ACKNOWLEDGE -> query/list 확인 -> UNACKNOWLEDGE -> query/list 확인 -> 앱 종료 -> H2 조회` 순서를 따랐다.
- 앱 종료 후 H2 CLI로 evidence를 확인했고, file lock 문제는 발생하지 않았다.

## 근거 Artifact
- `.agents/outer-loop/retrospectives/SPEC-0013/TASK-0004-control-action-transition-runtime-evidence.md`
- `src/main/java/com/agilerunner/api/service/review/ManualRerunControlActionService.java`
- `src/main/java/com/agilerunner/api/service/review/ManualRerunQueryService.java`
- `src/main/java/com/agilerunner/api/service/review/ManualRerunExecutionListService.java`
- `src/main/java/com/agilerunner/client/agentruntime/AgentRuntimeRepository.java`
- `src/test/java/com/agilerunner/domain/review/ManualRerunAvailableActionPolicyTest.java`
- `src/test/java/com/agilerunner/api/service/review/ManualRerunControlActionServiceTest.java`
- `src/test/java/com/agilerunner/api/service/review/ManualRerunQueryServiceTest.java`
- `src/test/java/com/agilerunner/api/service/review/ManualRerunExecutionListServiceTest.java`
- `src/test/java/com/agilerunner/client/agentruntime/AgentRuntimeRepositoryTest.java`

## 다음 Task 경고사항
- 후속 spec이 관리자 액션 이력 조회를 열면, action 응답과 query/list, audit history가 각각 어떤 역할을 가지는지 명확히 나눠야 한다.
- representative verification에서는 같은 executionKey 기준으로 action 응답, query/list, H2 audit row를 항상 함께 대조하는 흐름을 유지하는 편이 안전하다.

## 제안 필요 여부
- 없음
- 이번 task는 새 규칙 부족보다, 이미 정리된 representative verification 규칙을 관리자 액션 전환 흐름에 그대로 적용해 마감한 사례였다.
