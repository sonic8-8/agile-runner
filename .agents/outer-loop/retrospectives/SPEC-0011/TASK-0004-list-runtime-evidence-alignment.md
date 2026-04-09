---
spec_id: SPEC-0011
task_id: TASK-0004
github_issue_number: 43
criteria_keys:
  - manual-rerun-list-runtime-evidence-aligned
delivery_ids:
  - MANUAL_RERUN_DELIVERY:list-20260409-174716-retry
  - MANUAL_RERUN_DELIVERY:list-20260409-174716-blocked
execution_keys:
  - EXECUTION:MANUAL_RERUN:list-20260409-174716-retry
  - EXECUTION:MANUAL_RERUN:list-20260409-174716-blocked
test_evidence_ref:
  - "targeted: ./scripts/gradlew-java21.sh --no-daemon test --tests 'com.agilerunner.api.controller.review.ManualRerunControllerTest' --tests 'com.agilerunner.api.service.review.ManualRerunExecutionListServiceTest' --tests 'com.agilerunner.api.service.review.ManualRerunQueryServiceTest' --tests 'com.agilerunner.api.service.review.ManualRerunRetryServiceTest' --console=plain"
  - "full: ./scripts/gradlew-java21.sh --no-daemon cleanTest test --console=plain"
  - "actual app/H2: representative execution seed -> bootRun(local, port=18080) -> GET /reviews/rerun/executions?repositoryName=owner/repo&pullRequestNumber=901&executionStartType=MANUAL_RERUN&executionStatus=FAILED -> HTTP 200 응답 확인 -> app shutdown -> H2 Shell query by executionKey"
diff_ref: "git diff -- .agents/outer-loop/retrospectives/SPEC-0011/TASK-0004-list-runtime-evidence-alignment.md .agents/outer-loop/retrospectives/SPEC-0011/SPEC-0011-summary.md .agents/outer-loop/registry.json"
failure_summary: "대표 목록 조회 검증은 통과했지만, deterministic representative row 2건을 준비해야 해서 기본 representative 검증 순서 앞에 synthetic execution seed 단계가 필요했다."
root_cause: "TASK-0004의 목표가 실제 앱 목록 응답과 H2 evidence의 의미 정합성 확인이었기 때문에, retryable execution과 non-retryable execution을 local H2에 미리 준비하지 않으면 `availableActions`와 필터 결과를 안정적으로 대조할 수 없었다."
agents_check_findings:
  - "3개 서브에이전트 리뷰 결과, 이번 task는 목록 응답 의미와 H2 evidence 정합성 검증만 닫고 새 관리자 액션이나 목록 계약 변경은 열지 않는 현재 범위가 맞다고 정리됐다."
  - "대표 검증은 synthetic execution seed를 app 기동 전에 넣는 확장 순서를 사용했지만, runtime schema 변경 task가 아니어서 이미 승인된 schema 선적용 규칙과 충돌하지 않는다고 확인됐다."
  - "RETRY 가능 row와 불가능 row를 함께 준비해 availableActions, executionStatus, failureDisposition을 한 번에 대조한 현재 검증 구성이 적절하다고 확인됐다."
next_task_warnings:
  - "다음 관리자 제어 spec에서 representative 목록 검증이 필요하면, 실제로 읽고 싶은 제어 상태 조합을 먼저 seed하고 그다음 HTTP 응답/H2 evidence 대조를 수행하는 구성이 안전하다."
  - "대표 목록 검증에 synthetic row를 넣을 때는 seed가 runtime schema 변경과 함께 들어오는지 먼저 점검해야 한다."
error_signature: "verification note: representative list response requires deterministic retryable/non-retryable execution seed"
test_result_summary: "targeted test, full cleanTest test, local actual app/H2 representative list verification이 모두 기대 결과로 닫혔다."
snapshot_hashes: []
---

# 작업 회고

## 작업 요약
- local H2 file DB에 retryable manual rerun execution 1건과 non-retryable manual rerun execution 1건을 representative row로 준비했다.
- 실제 앱에서 `GET /reviews/rerun/executions`를 호출해 응답 row 2건이 `executionStatus`, `failureDisposition`, `availableActions` 의미를 올바르게 반환하는지 확인했다.
- 앱 종료 후 H2 `WEBHOOK_EXECUTION`, `AGENT_EXECUTION_LOG`를 같은 `execution_key` 기준으로 조회해 응답 의미와 runtime evidence가 일치하는지 검증했다.

## 실패 요약
- 대표 검증 자체는 실패하지 않았고, targeted test, full test, actual app/H2 representative verification이 모두 통과했다.
- 다만 representative row 2건을 deterministic하게 준비해야 해서 기본 representative 검증 순서 앞에 synthetic execution seed 단계가 추가됐다.

## Root Cause
- 이번 task의 핵심은 목록 응답 row의 `availableActions(RETRY)`, `executionStatus`, `failureDisposition` 의미를 실제 runtime evidence와 대조하는 것이었다.
- 이 의미를 안정적으로 대조하려면 retryable row와 blocked row를 local H2에 미리 준비해야 했고, 그래서 `execution 준비 -> 앱 기동 -> HTTP 검증 -> 앱 종료 -> H2 조회` 흐름으로 검증을 확장했다.

## AGENTS 체크 결과
- Tester는 controller/service black-box 테스트와 full test를 순차 실행한 뒤 representative actual app/H2 검증으로 마무리했다.
- Constructor는 production code를 더 넓히지 않고, 대표 검증과 회고 정리에 필요한 최소 범위만 유지했다.
- representative 검증 순서가 기본 순서보다 확장된 이유를 회고에 남겼고, delivery/execution key도 함께 기록했다.

## 근거 Artifact
- `/tmp/spec0011-task0004-list-response.json`
- `src/main/java/com/agilerunner/api/controller/review/ManualRerunController.java`
- `src/main/java/com/agilerunner/api/service/review/ManualRerunExecutionListService.java`
- `src/main/java/com/agilerunner/client/agentruntime/AgentRuntimeRepository.java`
- `src/test/java/com/agilerunner/api/controller/review/ManualRerunControllerTest.java`
- `src/test/java/com/agilerunner/api/service/review/ManualRerunExecutionListServiceTest.java`
- `src/test/java/com/agilerunner/api/service/review/ManualRerunQueryServiceTest.java`
- `src/test/java/com/agilerunner/api/service/review/ManualRerunRetryServiceTest.java`

## 다음 Task 경고사항
- 다음 spec에서 관리자 액션을 더 확장하면, representative 목록 검증용 synthetic row가 어떤 제어 상태를 표현해야 하는지 먼저 고정하는 편이 안전하다.
- representative 검증이 synthetic row를 요구할 때는 runtime schema 변경 여부를 먼저 확인하고, 검증 순서가 기본 순서와 달라지면 회고에 이유를 명시해야 한다.

## 제안 필요 여부
- 없음
- 이번 task의 교훈은 새 workflow 규칙 부족이 아니라, 이미 채택된 representative verification 규칙 안에서 representative row seed와 응답/H2 대조를 정확히 수행하는 쪽이었다.
