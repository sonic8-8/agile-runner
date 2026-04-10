---
spec_id: SPEC-0013
task_id: TASK-0003
github_issue_number: 50
criteria_keys:
  - manual-rerun-control-secondary-action-maps-audit-state
delivery_ids: []
execution_keys: []
test_evidence_ref:
  - "targeted: ./scripts/gradlew-java21.sh --no-daemon test --tests 'com.agilerunner.domain.review.ManualRerunAvailableActionPolicyTest' --tests 'com.agilerunner.api.service.review.ManualRerunControlActionServiceTest' --tests 'com.agilerunner.api.service.review.ManualRerunQueryServiceTest' --tests 'com.agilerunner.api.service.review.ManualRerunExecutionListServiceTest' --tests 'com.agilerunner.client.agentruntime.AgentRuntimeRepositoryTest' --tests 'com.agilerunner.api.controller.review.ManualRerunControllerTest' --console=plain"
  - "full: ./scripts/gradlew-java21.sh --no-daemon cleanTest test --console=plain"
diff_ref: "git diff -- src/main/java/com/agilerunner/domain/review/ManualRerunAvailableAction.java src/main/java/com/agilerunner/domain/review/ManualRerunAvailableActionPolicy.java src/main/java/com/agilerunner/api/service/review/ManualRerunControlActionService.java src/main/java/com/agilerunner/api/service/review/ManualRerunQueryService.java src/main/java/com/agilerunner/api/service/review/ManualRerunExecutionListService.java src/main/java/com/agilerunner/client/agentruntime/AgentRuntimeRepository.java src/test/java/com/agilerunner/domain/review/ManualRerunAvailableActionPolicyTest.java src/test/java/com/agilerunner/api/service/review/ManualRerunControlActionServiceTest.java src/test/java/com/agilerunner/api/service/review/ManualRerunQueryServiceTest.java src/test/java/com/agilerunner/api/service/review/ManualRerunExecutionListServiceTest.java src/test/java/com/agilerunner/client/agentruntime/AgentRuntimeRepositoryTest.java"
failure_summary: "TASK-0002까지는 ACKNOWLEDGE 적용 여부를 boolean으로만 읽었기 때문에, ACKNOWLEDGE 이후 UNACKNOWLEDGE로 다시 여는 상태 전환을 표현할 수 없었다."
root_cause: "관리자 액션이 하나일 때는 'ACKNOWLEDGE가 적용됐는가'만으로 충분했지만, 두 번째 액션이 생기면서 query/list와 service 정책은 마지막으로 적용된 액션이 무엇인지 알아야 했다. boolean 방식은 ACKNOWLEDGE와 UNACKNOWLEDGE를 번갈아 적용하는 상태를 담지 못했다."
agents_check_findings:
  - "3개 서브에이전트 모두 마지막 applied action을 기준으로 정책과 availableActions를 계산하는 방향이 TASK-0003 범위에 맞다고 확인했다."
  - "representative actual app/H2 검증은 TASK-0004에 남기고, 이번 task는 policy/repository/query-list 의미를 테스트로 먼저 닫는 구성이 적절하다고 정리됐다."
next_task_warnings:
  - "TASK-0004에서는 representative execution 하나에서 ACKNOWLEDGE -> UNACKNOWLEDGE 요청, query/list 응답, H2 audit evidence가 같은 executionKey 기준으로 일치하는지 실제 앱에서 확인해야 한다."
  - "대표 검증에서 query/list는 action detail을 직접 노출하지 않고 availableActions 재계산 결과만 확인해야 한다."
error_signature: "state-model gap: acknowledgeApplied boolean could not represent UNACKNOWLEDGE transition"
test_result_summary: "policy, repository, service, query/list targeted test와 full cleanTest test가 모두 통과했고, TASK-0003 범위의 상태 전환 의미를 코드 수준에서 닫았다."
snapshot_hashes: []
---

# 작업 회고

## 작업 요약
- `UNACKNOWLEDGE`를 query/list가 노출할 수 있도록 `ManualRerunAvailableAction`을 확장했다.
- `ACKNOWLEDGE가 적용됐는가` 대신 `마지막으로 적용된 관리자 액션이 무엇인가`를 읽는 repository seam으로 바꿨다.
- control action service, query service, list service가 모두 같은 기준으로 `ACKNOWLEDGE ↔ UNACKNOWLEDGE`를 계산하도록 연결했다.
- policy, repository, service, query/list 테스트를 보강해 상태 전환 의미를 먼저 잠갔다.

## 실패 요약
- 기능 실패라기보다 상태 모델 한계가 드러났다.
- 기존 boolean 방식은 `UNACKNOWLEDGE` 이후 다시 `ACKNOWLEDGE`가 가능해지는 전환 상태를 표현하지 못했다.

## Root Cause
- 관리자 액션이 하나일 때는 `ACKNOWLEDGE 적용 여부`만으로 충분했지만, 두 번째 액션이 생기면서 마지막 applied action을 기준으로 상태를 해석해야 했다.
- 그래서 정책, 저장 seam, query/list가 같은 상태 모델을 보도록 한 번에 바꾸는 것이 필요했다.

## AGENTS 체크 결과
- Tester는 policy, repository, service, query/list 중심 targeted test를 먼저 추가했다.
- Constructor는 `UNACKNOWLEDGE` 정책, audit state 조회 seam, query/list availableActions 계산을 함께 정리했다.
- targeted test와 full cleanTest test를 순차 실행했다.
- representative actual app/H2 검증은 spec 문서 기준대로 TASK-0004에 남기고, 그 사유를 회고에 명시했다.

## 근거 Artifact
- `src/main/java/com/agilerunner/domain/review/ManualRerunAvailableAction.java`
- `src/main/java/com/agilerunner/domain/review/ManualRerunAvailableActionPolicy.java`
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
- `TASK-0004`에서는 같은 executionKey 기준으로 action 응답, query/list availableActions, H2 audit row가 모두 맞는지 representative actual app/H2에서 확인해야 한다.
- `UNACKNOWLEDGE` 이후 query/list는 `ACKNOWLEDGE`를 다시 노출해야 하고, action detail을 query/list에 직접 추가하지 말아야 한다.

## 제안 필요 여부
- 없음
- 이번 task의 교훈은 새 규칙 부족이 아니라, 다중 관리자 액션에서 boolean 대신 마지막 applied action 기준으로 상태를 읽어야 한다는 설계 보정이었다.
