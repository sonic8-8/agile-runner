---
spec_id: SPEC-0015
task_id: TASK-0002
github_issue_number: 57
criteria_keys:
  - manual-rerun-control-repeat-policy-allows-reapply
delivery_ids: []
execution_keys: []
test_evidence_ref:
  - "targeted: ./scripts/gradlew-java21.sh --no-daemon test --tests 'com.agilerunner.domain.review.ManualRerunAvailableActionPolicyTest' --tests 'com.agilerunner.api.service.review.ManualRerunControlActionServiceTest' --tests 'com.agilerunner.client.agentruntime.AgentRuntimeRepositoryTest' --console=plain"
  - "full: ./scripts/gradlew-java21.sh --no-daemon cleanTest test --console=plain"
diff_ref: "git diff -- src/main/resources/agent-runtime/schema.sql src/test/java/com/agilerunner/domain/review/ManualRerunAvailableActionPolicyTest.java src/test/java/com/agilerunner/api/service/review/ManualRerunControlActionServiceTest.java src/test/java/com/agilerunner/client/agentruntime/AgentRuntimeRepositoryTest.java .agents/outer-loop/retrospectives/SPEC-0015/TASK-0002-control-repeat-storage-policy.md .agents/outer-loop/registry.json"
failure_summary: "반복 액션 정책은 이미 마지막 applied action 기준으로 동작할 수 있었지만, audit unique constraint 때문에 같은 action 재적용이 저장 단계에서 막히고 있었다."
root_cause: "도메인 정책은 `UNACKNOWLEDGE` 이후 `ACKNOWLEDGE` 재적용을 허용할 수 있었지만, `MANUAL_RERUN_CONTROL_ACTION_AUDIT`의 `(execution_key, action)` unique constraint가 실제 저장을 막고 있었다."
agents_check_findings:
  - "이번 task는 정책과 repository seam, 물리 스키마 정리에 집중하고 query/list/history 의미 보강은 TASK-0003으로 넘겼다."
  - "현재 spec은 저장 seam 연결 task와 representative actual app/H2 검증 task를 명시적으로 분리하고 있으므로, 이번 task는 targeted test와 full cleanTest test, repository/H2 mem 검증으로 닫았다."
next_task_warnings:
  - "TASK-0003은 query/list가 마지막 applied action 기준 현재 상태를, history가 반복 액션 전체 timeline을 보여준다는 점을 black-box 테스트로 다시 고정해야 한다."
  - "TASK-0004 representative 검증에서는 ACKNOWLEDGE -> UNACKNOWLEDGE -> ACKNOWLEDGE 전환이 실제 H2 audit row 세 건으로 남는지 확인해야 한다."
error_signature: "NONE"
test_result_summary: "policy/service/repository targeted test와 full cleanTest test가 모두 통과했고, 반복 액션 저장 허용과 즉시 중복 금지 기준을 함께 고정했다."
snapshot_hashes: []
---

# 작업 회고

## 작업 요약
- 반복 액션 허용을 막던 audit unique constraint를 제거했다.
- `ACKNOWLEDGE -> UNACKNOWLEDGE -> ACKNOWLEDGE` 흐름이 정책, 서비스, repository/H2 mem 저장 기준으로 허용된다는 점을 테스트로 고정했다.
- query/list/history 의미 보강과 실제 representative 검증은 다음 task로 분리했다.

## 실패 요약
- 기능 실패는 없었고, 핵심 문제는 정책이 아니라 물리 스키마 제약이 실제 운영 흐름을 막고 있다는 점이었다.

## Root Cause
- 마지막 applied action 기반 정책은 이미 반복 액션 흐름과 호환될 수 있었지만, 저장소 스키마가 이를 표현하지 못하고 있었다.
- 따라서 먼저 schema와 repository seam을 정리하는 것이 자연스러운 순서였다.

## AGENTS 체크 결과
- targeted test와 전체 테스트를 순차 실행했다.
- 현재 spec이 저장 seam 연결과 representative actual app/H2 검증을 분리하고 있으므로, 이번 task는 seam 검증까지만 닫고 그 이유를 회고에 남겼다.

## 근거 Artifact
- `src/main/resources/agent-runtime/schema.sql`
- `src/test/java/com/agilerunner/domain/review/ManualRerunAvailableActionPolicyTest.java`
- `src/test/java/com/agilerunner/api/service/review/ManualRerunControlActionServiceTest.java`
- `src/test/java/com/agilerunner/client/agentruntime/AgentRuntimeRepositoryTest.java`

## 다음 Task 경고사항
- query/list는 마지막 applied action 기준 현재 상태만 보여줘야 한다.
- history는 반복 액션 전체 timeline을 순서대로 보여줘야 한다.
- representative 검증은 `TASK-0004`에서 실제 앱/H2 기준으로 세 번의 action transition을 모두 확인해야 한다.

## 제안 필요 여부
- 없음
