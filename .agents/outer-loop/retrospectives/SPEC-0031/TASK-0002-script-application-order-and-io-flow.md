---
spec_id: SPEC-0031
task_id: TASK-0002
github_issue_number: 121
criteria_keys:
  - manual-rerun-script-application-order-documented
delivery_ids: []
execution_keys: []
test_evidence_ref:
  - "대상 테스트: ./scripts/gradlew-java21.sh --no-daemon test --console=plain --tests 'com.agilerunner.client.agentruntime.ManualRerunRunFlowScriptTest' --tests 'com.agilerunner.client.agentruntime.ManualRerunSeedCommandScriptTest' --tests 'com.agilerunner.client.agentruntime.ManualRerunResponseSeedSqlTest' --tests 'com.agilerunner.client.agentruntime.ManualRerunResponseSeedEvidenceSqlTest'"
  - "전체 테스트: ./scripts/gradlew-java21.sh --no-daemon cleanTest test --console=plain"
diff_ref: "git diff -- docs/manual-rerun-response-seed-command-guide.md .agents/outer-loop/retrospectives/SPEC-0031/TASK-0002-script-application-order-and-io-flow.md .agents/outer-loop/registry.json"
failure_summary: "초기 검증에서 내가 대상 테스트와 전체 테스트를 같은 workspace에서 병렬로 띄워 대상 테스트가 거짓 실패를 냈다. 규칙대로 전체 테스트를 마친 뒤 대상 테스트를 단독 재실행해 둘 다 green으로 다시 확인했다."
root_cause: "이번 task는 문서 재구성 단계였지만, 테스트 실행도 기존 AGENTS 규칙을 그대로 따라야 했다. 내가 순차 실행 규칙을 어기면서 build/test-results 산출물이 겹쳤고, 실제 코드 문제가 아닌 검증 흐름 문제를 만들었다."
agents_check_findings:
  - "문서 경계 리뷰는 상단을 적용 순서와 입력/출력 흐름으로 잠그고, 실행 키 해석과 출력 파일 해석은 참고 섹션으로 내려 TASK-0003/0004 경계가 분명해졌다고 보고 PASS를 줬다."
  - "검증 근거 리뷰는 문서만 읽고 `prepare-seed.sh -> run-rerun.sh/run-retry.sh -> 파생 실행 키 재사용 -> collect-evidence.sh` 흐름을 재구성할 수 있고, 이번 task가 actual app/H2 재실행을 끌어오지 않았다고 보고 PASS를 줬다."
  - "가독성 리뷰는 `시작 전에 먼저 확인할 근거 -> 빠른 적용 순서 -> 빠른 적용 순서에서 다시 쓰는 입력, 출력, 다음 단계 입력 -> 참고 섹션` 흐름이 새 작업자에게 직접적이라고 보고 PASS를 줬다."
next_task_warnings:
  - "TASK-0003은 출력 파일과 요약 파일, 응답 가이드, 회고를 어떤 순서로 읽는지 정리하는 단계여야 한다. 이번 task처럼 적용 순서와 입력/출력 흐름 정리만 다루고 실제 해석 기준은 거기서 닫는다."
  - "테스트는 대상 테스트와 전체 테스트를 다시 병렬로 띄우지 않는다. 같은 workspace 산출물을 공유하므로 항상 순차 실행한다."
error_signature: "parallel-test-workspace-collision"
test_result_summary: "전체 테스트는 첫 실행에서 통과했다. 대상 테스트는 병렬 실행 겹침으로 한 번 거짓 실패가 났지만, 단독 재실행에서 통과했고 최종 근거는 둘 다 green이다. 실제 앱/H2 대표 검증은 이번 task 비대상으로 생략했다."
snapshot_hashes: []
---

# 작업 회고

## 작업 요약
- 보조 명령 가이드 상단에 `빠른 적용 순서`와 `빠른 적용 순서에서 다시 쓰는 입력, 출력, 다음 단계 입력`을 추가해 새 작업자가 어떤 순서로 스크립트를 적용하고 어떤 값과 파일을 다음 단계에 다시 쓰는지 한 번에 읽게 정리했다.
- 실제 초안 파일 기준 대표 검증 근거 링크를 최신 대표 검증 회고로 맞췄다.
- 실행 키 재사용, 출력 파일 해석, 종료 코드와 마감 판단은 `참고` 섹션으로 분리해 `TASK-0002`가 적용 순서와 입력/출력 흐름 정리 단계라는 경계를 분명히 했다.

## 실패 요약
- 초기 검증에서 내가 대상 테스트와 전체 테스트를 병렬로 띄워 대상 테스트가 거짓 실패를 냈다.
- 전체 테스트는 이미 통과했고, 대상 테스트는 단독 재실행에서 통과했다.

## Root Cause
- 규칙이 없는 게 아니라 내가 `같은 workspace 산출물을 공유하는 테스트는 병렬로 실행하지 않는다`는 기존 규칙을 어겼다.
- 문서 task라도 종료 검증은 테스트 실행 규칙을 그대로 따라야 한다.

## AGENTS 체크 결과
- linked issue `#121`을 `TASK-0002`와 1:1로 연결했다.
- 3개 서브에이전트는 문서 경계, 검증 근거, 가독성 관점으로 리뷰했고 최종 전원 PASS가 나왔다.
- 대상 테스트와 전체 테스트는 최종적으로 순차 근거로 다시 닫았다.
- 실제 앱/H2 대표 검증은 이번 task 비대상으로 생략했고, 그 이유를 회고에 남겼다.

## 근거 자료
- 보조 명령 가이드
  - [manual-rerun-response-seed-command-guide.md](/home/seaung13/workspace/agile-runner/docs/manual-rerun-response-seed-command-guide.md)
- 현재 활성 단계 문서
  - [spec.md](/home/seaung13/workspace/agile-runner/.agents/active/spec.md)
- 현재 활성 작업 문서
  - [tasks.md](/home/seaung13/workspace/agile-runner/.agents/active/tasks.md)
- 직전 대표 검증 회고
  - [TASK-0004-script-draft-representative-verified.md](/home/seaung13/workspace/agile-runner/.agents/outer-loop/retrospectives/SPEC-0030/TASK-0004-script-draft-representative-verified.md)

## 다음 Task 경고사항
- `TASK-0003`는 적용 순서 자체가 아니라 출력 파일, 응답 가이드, 요약 파일, 회고를 어떤 순서로 읽는지 정리하는 단계다.
- actual app/H2 대표 검증은 여전히 `TASK-0004`에서만 닫는다.
- 테스트 실행은 다시 병렬로 띄우지 않는다.

## 제안 필요 여부
- 없음
- 새 제안 없음. 이번 문제는 새 규칙 부족이 아니라 기존 순차 실행 규칙을 내가 어긴 사례였다.
