---
spec_id: SPEC-0014
task_id: TASK-0003
github_issue_number: 54
criteria_keys:
  - manual-rerun-control-history-maps-audit-timeline
delivery_ids: []
execution_keys: []
test_evidence_ref:
  - "targeted: ./scripts/gradlew-java21.sh --no-daemon test --tests 'com.agilerunner.api.controller.review.ManualRerunControllerTest' --tests 'com.agilerunner.api.service.review.ManualRerunControlActionHistoryServiceTest' --tests 'com.agilerunner.client.agentruntime.AgentRuntimeRepositoryTest' --tests 'com.agilerunner.api.service.review.ManualRerunQueryServiceTest' --tests 'com.agilerunner.api.service.review.ManualRerunExecutionListServiceTest' --console=plain"
  - "full: ./scripts/gradlew-java21.sh --no-daemon cleanTest test --console=plain"
diff_ref: "git diff -- src/main/java/com/agilerunner/api/service/review/ManualRerunControlActionHistoryService.java src/main/java/com/agilerunner/client/agentruntime/AgentRuntimeRepository.java src/main/java/com/agilerunner/domain/review/ManualRerunControlActionAudit.java src/test/java/com/agilerunner/api/controller/review/ManualRerunControllerTest.java src/test/java/com/agilerunner/api/service/review/ManualRerunControlActionHistoryServiceTest.java src/test/java/com/agilerunner/client/agentruntime/AgentRuntimeRepositoryTest.java .agents/outer-loop/retrospectives/SPEC-0014/TASK-0003-action-history-audit-timeline.md .agents/outer-loop/registry.json"
failure_summary: "history 조회 경로는 열렸지만 실제 audit row를 읽지 않아 action timeline을 응답으로 해석할 수 없는 상태였다."
root_cause: "TASK-0002는 입력과 최소 응답 seam만 여는 단계였기 때문에 빈 actions placeholder를 사용했다. 실제 action timeline과 not-found 정책은 별도 task로 분리돼 있었고, 이 단계에서 repository seam과 history 응답 의미를 연결해야 했다."
agents_check_findings:
  - "history 응답만 action detail을 읽고 query/list는 계속 현재 상태와 availableActions만 반환하도록 경계를 유지했다."
  - "현재 spec이 저장 seam task와 representative 실제 앱/H2 검증 task를 분리하고 있으므로, 이번 task는 targeted test + full test + repository seam 검증으로 닫고 representative 검증은 TASK-0004로 넘겼다."
next_task_warnings:
  - "TASK-0004에서는 representative execution 하나를 준비하고 action 응답, history 응답, query/list, H2 audit evidence를 같은 executionKey 기준으로 대조해야 한다."
  - "history 응답 정렬과 H2 audit row 순서를 실제 앱 기준으로 다시 확인해야 한다."
error_signature: "NONE"
test_result_summary: "history controller/service/repository targeted test와 전체 cleanTest test가 모두 통과했다. representative actual app/H2 검증은 spec task 분리에 따라 다음 task로 이관했다."
snapshot_hashes: []
---

# 작업 회고

## 작업 요약
- history 조회 service가 실제 `MANUAL_RERUN_CONTROL_ACTION_AUDIT` row를 읽어 `action`, `actionStatus`, `note`, `appliedAt`을 반환하도록 연결했다.
- repository에 action audit timeline 조회 메서드를 추가하고, `appliedAt` 기준 시간 순서 정렬을 고정했다.
- execution이 없거나 manual rerun이 아닌 경우에는 기존 404 정책으로 닫았다.

## 실패 요약
- 기능 실패는 없었다.
- 주요 위험은 history 응답이 query/list와 역할이 섞이거나, 실제 representative 검증까지 이번 task로 끌어와 task 경계가 무너지는 점이었다.

## Root Cause
- action history는 새 read 경계지만 source of truth는 H2 audit row이므로, controller seam만으로는 의미가 완성되지 않았다.
- 현재 spec은 저장 seam 연결과 representative 검증을 분리해 두었기 때문에, 이번 task는 repository/service/controller 수준 정합성까지만 닫는 것이 맞았다.

## AGENTS 체크 결과
- controller는 history path variable을 service request로 전달하고 응답만 반환했다.
- service는 manual rerun execution 존재 여부와 history row 매핑만 담당했다.
- targeted test와 full test를 순차 실행했다.
- representative actual app/H2 검증은 다음 task 범위로 남기고 그 이유를 회고에 기록했다.

## 근거 Artifact
- `src/main/java/com/agilerunner/api/service/review/ManualRerunControlActionHistoryService.java`
- `src/main/java/com/agilerunner/client/agentruntime/AgentRuntimeRepository.java`
- `src/main/java/com/agilerunner/domain/review/ManualRerunControlActionAudit.java`
- `src/test/java/com/agilerunner/api/controller/review/ManualRerunControllerTest.java`
- `src/test/java/com/agilerunner/api/service/review/ManualRerunControlActionHistoryServiceTest.java`
- `src/test/java/com/agilerunner/client/agentruntime/AgentRuntimeRepositoryTest.java`

## 다음 Task 경고사항
- representative 검증은 실제 action 실행 후 history 응답과 H2 audit row를 같은 `executionKey`로 대조해야 한다.
- list/query는 current state만, history는 action detail만 읽는 역할 분리가 실제 앱 응답에서도 유지되는지 확인해야 한다.

## 제안 필요 여부
- 없음
