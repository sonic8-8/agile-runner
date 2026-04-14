---
spec_id: SPEC-0030
task_id: TASK-0004
github_issue_number: 119
criteria_keys:
  - manual-rerun-script-draft-representative-verified
delivery_ids:
  - MANUAL_RERUN_DELIVERY:spec0030-rerun-20260414-090240
  - MANUAL_RERUN_DELIVERY:spec0030-retry-source-20260414-090240
  - MANUAL_RERUN_DELIVERY:5eb07004-ff90-412c-9db6-438ae51a6140
execution_keys:
  - EXECUTION:MANUAL_RERUN:spec0030-rerun-20260414-090240
  - EXECUTION:MANUAL_RERUN:spec0030-retry-source-20260414-090240
  - EXECUTION:MANUAL_RERUN:5eb07004-ff90-412c-9db6-438ae51a6140
test_evidence_ref:
  - "스크립트 문법 확인: bash -n scripts/manual-rerun-response/collect-evidence.sh"
  - "대상 테스트: ./scripts/gradlew-java21.sh --no-daemon test --console=plain --tests 'com.agilerunner.client.agentruntime.ManualRerunRunFlowScriptTest' --tests 'com.agilerunner.client.agentruntime.ManualRerunSeedCommandScriptTest' --tests 'com.agilerunner.client.agentruntime.ManualRerunResponseSeedSqlTest' --tests 'com.agilerunner.client.agentruntime.ManualRerunResponseSeedEvidenceSqlTest'"
  - "전체 테스트: ./scripts/gradlew-java21.sh --no-daemon cleanTest test --console=plain"
  - "실제 앱/H2 대표 검증: .tmp/spec-0030-task-0004-20260414-090240/summary.json"
diff_ref: "docs/manual-rerun-response-seed-command-guide.md, scripts/manual-rerun-response/collect-evidence.sh, src/test/java/com/agilerunner/client/agentruntime/ManualRerunRunFlowScriptTest.java, .agents/outer-loop/retrospectives/SPEC-0030/TASK-0004-script-draft-representative-verified.md, .agents/outer-loop/retrospectives/SPEC-0030/SPEC-0030-summary.md, .agents/outer-loop/registry.json"
failure_summary: "첫 대표 검증 기준에서는 재시도 실행 근거 파일이 파생 실행 키 중심으로만 보여서 어떤 전달 식별자를 기록했는지 즉시 읽기 어려웠다. 이후 `collect-evidence.sh`와 관련 테스트를 보강하고 새 대표 재실행/재시도 검증을 다시 실행해 재실행, 재시도 원본, 재시도 파생 전달 식별자까지 함께 남기는 방식으로 닫았다."
root_cause: "TASK-0004는 대표 검증을 실제로 다시 수행하는 단계라, 실행 키만 남겨도 충분해 보였던 근거 파일이 운영자 관점에서는 전달 식별자 연결까지 바로 읽혀야 했다. 특히 재시도는 원본 실행 키, 파생 실행 키, 파생 단건 조회 응답, H2 `WEBHOOK_EXECUTION` 행이 서로 다른 위치에 흩어져 있어, 어떤 전달 식별자를 기준으로 실행 근거를 봤는지 명시하지 않으면 대표 검증 근거가 약해진다."
agents_check_findings:
  - "문서 경계 리뷰는 가이드가 실제 초안 파일 책임과 수동 확인 경계를 유지한 채 재실행/재시도 대표 흐름을 TASK-0004 범위 안에서 닫았다고 보고 통과 판정을 줬다."
  - "검증 근거 리뷰는 대상 테스트, 전체 테스트, 새 대표 재실행/재시도 검증, 재실행/재시도 전달 식별자와 실행 키 동시 기록까지 확인한 뒤 통과 판정을 줬다."
  - "가독성 리뷰는 문서와 회고가 재실행/재시도 대표 검증 흐름과 수동 판단 경계를 한국어로 자연스럽게 설명한다고 보고 최종 통과 판정을 줬다."
next_task_warnings:
  - "다음 단계에서 실제 초안 파일을 더 확장하더라도 재시도 대표 검증은 원본 실행 키, 원본 전달 식별자, 파생 실행 키, 파생 전달 식별자를 항상 한 세트로 남겨야 한다."
  - "재시도 파생 단건 조회 응답에는 `retrySourceExecutionKey`가 없으므로, 원본/파생 연결을 재시도 응답과 H2 `WEBHOOK_EXECUTION` 행에서 다시 확인하는 규칙을 유지해야 한다."
  - "H2 실행 근거 조회는 앱 종료 뒤 순차 실행을 유지하고, 잠금 오류가 나면 스키마 실패로 단정하지 말고 먼저 파일 잠금 여부를 확인해야 한다."
error_signature: "재시도 대표 검증 실행 근거의 전달 식별자 기준 불명확"
test_result_summary: "스크립트 문법 확인, 대상 테스트, 전체 테스트가 모두 통과했다. 새 재실행/재시도 실제 앱/H2 대표 검증도 다시 수행했고, 재실행, 재시도 원본, 재시도 파생 실행의 실행 키와 전달 식별자를 함께 남겼다. 재시도 파생 단건 조회 응답에는 원본 실행 키 필드인 `retrySourceExecutionKey`가 없다는 점도 재시도 응답과 H2 `WEBHOOK_EXECUTION` 행 대조로 다시 확인했다."
snapshot_hashes: []
---

# 작업 회고

## 작업 요약
- `collect-evidence.sh`가 재실행/재시도 `WEBHOOK_EXECUTION` 조회에서 전달 식별자를 함께 남기도록 보강했다.
- `ManualRerunRunFlowScriptTest`를 보강해 재실행/재시도 실행 근거 파일이 실행 키뿐 아니라 전달 식별자까지 포함하는지 확인했다.
- 보조 명령 가이드에 재시도 대표 검증에서 원본 실행 키, 파생 실행 키, 전달 식별자, H2 실행 근거를 어떻게 다시 연결해 읽는지 정리했다.
- 새 대표 재실행/재시도 검증을 다시 수행해 응답, 출력 파일, H2 실행 근거가 같은 기준으로 맞는지 확인했다.

## 실패 요약
- 첫 대표 검증 기준에서는 재시도 실행 근거가 실행 키 중심으로만 정리돼 있어, 파생 실행의 전달 식별자를 어떤 기준으로 읽어야 하는지 즉시 드러나지 않았다.
- 이 상태로는 대표 검증을 다시 읽는 사람이 재시도 원본 전달 식별자와 파생 전달 식별자를 한 번에 따라가기 어렵다.

## 근본 원인
- 재실행은 실행 키와 전달 식별자가 1:1이라 큰 문제가 없지만, 재시도는 원본 실행, 원본 전달 식별자, 파생 실행, 파생 전달 식별자가 분리된다.
- 파생 단건 조회 응답에 `retrySourceExecutionKey`가 없기 때문에, 재시도 대표 검증은 재시도 응답과 H2 `WEBHOOK_EXECUTION` 행을 함께 봐야 한다. 이 연결 정보를 실행 근거 파일에 더 직접적으로 남기지 않으면 대표 검증 문서만으로는 해석 비용이 올라간다.

## AGENTS 체크 결과
- 연결 이슈 `#119`를 `TASK-0004`와 1:1로 유지했다.
- 스크립트 문법 확인, 대상 테스트, 전체 테스트를 순차 실행했고, 실제 앱/H2 대표 검증도 새 식별자로 다시 수행했다.
- 대표 검증에는 아래 식별자를 함께 남겼다.
  - rerun: `EXECUTION:MANUAL_RERUN:spec0030-rerun-20260414-090240`, `MANUAL_RERUN_DELIVERY:spec0030-rerun-20260414-090240`
  - retry source: `EXECUTION:MANUAL_RERUN:spec0030-retry-source-20260414-090240`, `MANUAL_RERUN_DELIVERY:spec0030-retry-source-20260414-090240`
  - retry derived: `EXECUTION:MANUAL_RERUN:5eb07004-ff90-412c-9db6-438ae51a6140`, `MANUAL_RERUN_DELIVERY:5eb07004-ff90-412c-9db6-438ae51a6140`

## 근거 자료
- 보조 명령 가이드
  - [manual-rerun-response-seed-command-guide.md](/home/seaung13/workspace/agile-runner/docs/manual-rerun-response-seed-command-guide.md)
- 실행 근거 수집 초안 파일
  - [collect-evidence.sh](/home/seaung13/workspace/agile-runner/scripts/manual-rerun-response/collect-evidence.sh)
- 대표 검증 스크립트 테스트
  - [ManualRerunRunFlowScriptTest.java](/home/seaung13/workspace/agile-runner/src/test/java/com/agilerunner/client/agentruntime/ManualRerunRunFlowScriptTest.java)
- 대표 검증 요약
  - [summary.json](/home/seaung13/workspace/agile-runner/.tmp/spec-0030-task-0004-20260414-090240/summary.json)
- rerun 실행 근거 파일
  - [rerun-webhook-execution.txt](/home/seaung13/workspace/agile-runner/.tmp/spec-0030-task-0004-20260414-090240/rerun/rerun-webhook-execution.txt)
  - [rerun-action-audit.txt](/home/seaung13/workspace/agile-runner/.tmp/spec-0030-task-0004-20260414-090240/rerun/rerun-action-audit.txt)
- retry 실행 근거 파일
  - [retry-webhook-execution.txt](/home/seaung13/workspace/agile-runner/.tmp/spec-0030-task-0004-20260414-090240/retry/retry-webhook-execution.txt)
  - [retry-agent-execution-log.txt](/home/seaung13/workspace/agile-runner/.tmp/spec-0030-task-0004-20260414-090240/retry/retry-agent-execution-log.txt)

## 대표 검증 요약 파일 읽는 법
- [summary.json](/home/seaung13/workspace/agile-runner/.tmp/spec-0030-task-0004-20260414-090240/summary.json)은 대표 검증 결과를 다시 계산하지 않고 재사용하기 위한 기계용 요약 파일이다.
- `sourceExecutionKey`, `derivedExecutionKey`, `derivedDeliveryId`, `hasRetrySourceExecutionKey` 같은 내부 키는 자동 비교용 값이고, 사람은 이 회고와 보조 명령 가이드의 한국어 설명을 기준으로 해석한다.

## 대표 검증 결과
- 재실행 대표 검증은 `FAILED / GITHUB_APP_CONFIGURATION_MISSING / MANUAL_ACTION_REQUIRED` 상태를 유지한 채, 조치 전 `availableActions=[UNACKNOWLEDGE]`, 조치 응답 `UNACKNOWLEDGE / APPLIED / availableActions=[ACKNOWLEDGE]`, 조치 후 `currentActionState=null / availableActions=[ACKNOWLEDGE]`를 확인했다.
- 재실행 H2 실행 근거는 `WEBHOOK_EXECUTION`에서 재실행 실행 키와 재실행 전달 식별자를 함께 보여 주고, `MANUAL_RERUN_CONTROL_ACTION_AUDIT`에서 `ACKNOWLEDGE` 뒤 `UNACKNOWLEDGE`가 이어진 것을 확인했다.
- 재시도 대표 검증은 재시도 응답에서 파생 실행 키를 받고, 파생 단건 조회 응답에서 `FAILED / GITHUB_APP_CONFIGURATION_MISSING / MANUAL_ACTION_REQUIRED / writePerformed=false`를 다시 확인했다.
- 재시도 H2 실행 근거는 `WEBHOOK_EXECUTION`에서 파생 실행 키, 파생 전달 식별자, `retry_source_execution_key`를 함께 보여 주고, `AGENT_EXECUTION_LOG`에서 `manual-rerun-accepted SUCCEEDED`, `review-generated FAILED`를 확인했다.
- 재시도 파생 단건 조회 응답 자체에는 원본 실행 키 필드인 `retrySourceExecutionKey`가 없으므로, 원본/파생 연결은 재시도 응답과 H2 `WEBHOOK_EXECUTION` 행을 함께 보는 방식으로 닫았다.

## 다음 작업 경고사항
- 이후 단계에서 대표 검증을 다시 돌릴 때는 기존 예시 식별자를 재사용하지 말고 새 접미사를 넣어 새 실행 키와 전달 식별자를 만들고 회고에 함께 남겨야 한다.
- 재시도 대표 검증은 원본 실행/전달 식별자와 파생 실행/전달 식별자를 한 세트로 남기지 않으면 근거 해석 비용이 다시 올라간다.
- H2 실행 근거 조회는 앱 종료 뒤 순차로만 실행하고, 잠금 시그니처가 보이면 먼저 파일 잠금 여부를 확인해야 한다.

## 제안 필요 여부
- 없음
- 새 제안 없음. 이번 교훈은 새 절차를 추가하기보다, 이미 있는 대표 검증 절차에서 재시도 실행 근거의 전달 식별자 기준을 더 직접적으로 남겨야 한다는 점을 현재 스크립트와 문서 안에서 보강한 것이다.
