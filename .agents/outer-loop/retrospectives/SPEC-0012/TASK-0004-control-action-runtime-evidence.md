---
spec_id: SPEC-0012
task_id: TASK-0004
github_issue_number: 47
criteria_keys:
  - manual-rerun-control-runtime-evidence-aligned
delivery_ids:
  - MANUAL_RERUN_DELIVERY:d9ab9d9e-133c-4cc0-b503-8c78170d3555
execution_keys:
  - EXECUTION:MANUAL_RERUN:d9ab9d9e-133c-4cc0-b503-8c78170d3555
test_evidence_ref:
  - "targeted: ./scripts/gradlew-java21.sh --no-daemon test --tests 'com.agilerunner.api.service.review.ManualRerunControlActionServiceTest' --tests 'com.agilerunner.api.service.review.ManualRerunQueryServiceTest' --tests 'com.agilerunner.api.service.review.ManualRerunExecutionListServiceTest' --tests 'com.agilerunner.api.controller.review.ManualRerunControllerTest' --tests 'com.agilerunner.client.agentruntime.AgentRuntimeRepositoryTest' --console=plain"
  - "full: ./scripts/gradlew-java21.sh --no-daemon cleanTest test --console=plain"
  - "actual app/H2: POST /reviews/rerun -> POST /reviews/rerun/{executionKey}/actions -> GET /reviews/rerun/{executionKey} -> GET /reviews/rerun/executions -> app shutdown -> H2 Shell query by executionKey"
diff_ref: "git diff -- .agents/outer-loop/retrospectives/SPEC-0012/TASK-0004-control-action-runtime-evidence.md .agents/outer-loop/retrospectives/SPEC-0012/SPEC-0012-summary.md .agents/outer-loop/registry.json"
failure_summary: "대표 관리자 제어 액션 검증은 통과했고, action 응답의 actionStatus와 note, query/list의 availableActions, H2 audit evidence가 같은 executionKey 기준으로 일치했다."
root_cause: "이번 task의 핵심은 query/list가 action detail을 직접 노출하지 않는 설계를 유지하면서도, 같은 executionKey 기준으로 action 응답과 H2 audit evidence를 대조해 의미 일치를 확인하는 것이었다."
agents_check_findings:
  - "3개 서브에이전트 리뷰 결과, TASK-0004는 representative actual app/H2 verification 전담 범위로 충분히 잠겨 있고 추가 black-box 테스트 없이 representative 검증으로 바로 들어가도 된다고 정리됐다."
  - "대표 검증에서는 action 응답, query 응답, 목록 row, H2 audit row를 모두 같은 executionKey 기준으로 대조해야 한다는 점을 회고에 명시하는 구성이 적절하다고 확인됐다."
  - "새 workflow proposal은 필요 없고, 이미 채택된 대표 검증 규칙 안에서 현재 흐름을 그대로 적용한 사례로 정리됐다."
next_task_warnings:
  - "다음 관리자 액션 spec에서 query/list가 action detail을 계속 숨긴다면, representative 검증도 같은 executionKey 기준의 action 응답 + availableActions + audit row 대조 방식으로 유지하는 편이 안전하다."
  - "대표 관리자 액션 검증은 app shutdown 이후 H2 조회를 기본 순서로 유지해야 file lock 오해를 줄일 수 있다."
error_signature: "verification note: compare action response, query/list availableActions, and H2 audit row by same executionKey"
test_result_summary: "targeted test, full cleanTest test, local actual app/H2 representative control action verification이 모두 기대 결과로 닫혔다."
snapshot_hashes: []
---

# 작업 회고

## 작업 요약
- local profile 실제 앱에서 `POST /reviews/rerun`으로 `MANUAL_ACTION_REQUIRED` manual rerun execution 1건을 준비했다.
- 같은 `executionKey`로 `POST /reviews/rerun/{executionKey}/actions`를 호출해 `ACKNOWLEDGE`를 적용하고, 응답의 `action`, `actionStatus`, `note`, `availableActions`를 확인했다.
- 같은 `executionKey`를 기준으로 단건 조회와 목록 조회를 확인한 뒤, 앱 종료 후 H2 `WEBHOOK_EXECUTION`과 `MANUAL_RERUN_CONTROL_ACTION_AUDIT`를 조회해 응답/조회 결과와 audit evidence 의미가 일치하는지 검증했다.

## 실패 요약
- representative 관리자 제어 액션 검증 자체는 실패하지 않았고, targeted test, full test, actual app/H2 representative verification이 모두 통과했다.
- 이번 task의 핵심은 새 구현보다 실제 응답과 audit evidence를 같은 executionKey 기준으로 비교해 의미 일치를 닫는 것이었다.

## Root Cause
- query/list는 이번 spec에서 `action`, `actionStatus`, `note`를 직접 노출하지 않고 `availableActions`만 갱신한다.
- 그래서 representative 검증에서도 action 응답은 action detail로, query/list는 `availableActions` 변화로, H2는 audit row와 execution row로 같은 결과를 읽는다는 점을 executionKey 기준으로 함께 확인해야 했다.

## AGENTS 체크 결과
- Tester는 targeted test와 full cleanTest test를 순차 실행한 뒤 representative actual app/H2 verification을 수행했다.
- Constructor는 production 코드를 더 넓히지 않고 representative verification에 필요한 현재 흐름만 확인했다.
- representative 검증에서 사용한 `delivery_id`, `execution_key`, app shutdown 후 H2 조회 순서를 모두 회고에 남겼다.

## 근거 Artifact
- `/tmp/spec0012-task0004/rerun.json`
- `/tmp/spec0012-task0004/action.json`
- `/tmp/spec0012-task0004/query.json`
- `/tmp/spec0012-task0004/list.json`
- `src/main/java/com/agilerunner/api/controller/review/ManualRerunController.java`
- `src/main/java/com/agilerunner/api/service/review/ManualRerunControlActionService.java`
- `src/main/java/com/agilerunner/api/service/review/ManualRerunQueryService.java`
- `src/main/java/com/agilerunner/api/service/review/ManualRerunExecutionListService.java`
- `src/main/java/com/agilerunner/client/agentruntime/AgentRuntimeRepository.java`

## 다음 Task 경고사항
- 다음 관리자 액션 spec에서 action 종류가 늘어나면 representative 검증도 action 응답, query/list availableActions, audit row를 같은 executionKey 기준으로 비교하는 방식을 유지하는 편이 안전하다.
- 목록 조회는 여러 execution row를 반환할 수 있으므로 representative row를 찾을 때는 repository/pull request 조건과 executionKey를 함께 기록하는 편이 좋다.

## 제안 필요 여부
- 없음
- 이번 task의 교훈은 새 workflow 규칙 부족이 아니라, 이미 승인된 representative verification 규칙과 TASK-0003/TASK-0004 경계 분리 규칙을 정확히 적용한 쪽이었다.
