---
spec_id: SPEC-0036
task_id: TASK-0001
github_issue_number: 140
criteria_keys:
  - manual-rerun-script-application-example-safety-net-preserved
delivery_ids: []
execution_keys: []
test_evidence_ref:
  - "대상 테스트: ./scripts/gradlew-java21.sh --no-daemon test --console=plain --tests 'com.agilerunner.client.agentruntime.ManualRerunRunFlowScriptTest' --tests 'com.agilerunner.client.agentruntime.ManualRerunSeedCommandScriptTest' --tests 'com.agilerunner.client.agentruntime.ManualRerunResponseSeedSqlTest' --tests 'com.agilerunner.client.agentruntime.ManualRerunResponseSeedEvidenceSqlTest'"
  - "전체 테스트: ./scripts/gradlew-java21.sh --no-daemon cleanTest test --console=plain"
diff_ref: "git diff -- .agents/prd.md .agents/active/spec.md .agents/active/tasks.md .agents/criteria/SPEC-0036-manual-rerun-script-application-example.json .agents/outer-loop/retrospectives/SPEC-0036/TASK-0001-application-example-safety-net.md .agents/outer-loop/registry.json"
failure_summary: "실행 실패는 없었다. SPEC-0035 단계 요약, 빠른 참조 문서, 자동 검증 테스트를 다시 읽고 돌려 이번 단계에 바로 쓸 수 있는지 확인했다."
root_cause: "빠른 참조 카드는 이미 정리됐지만, 실제 실패 상황에 적용하는 예시를 붙이기 전에 직전 단계 문서와 테스트를 다시 확인할 필요가 있었다. 적용 예시를 바로 쓰기 시작하면 카드 자체를 다시 바꾸는 단계와 적용 예시를 붙이는 단계가 섞일 수 있었다."
agents_check_findings:
  - "문서 경계 리뷰는 PRD, 활성 단계 문서, 작업 문서, 검증 기준이 모두 `적용 예시 정리` 단계로 같은 범위를 가리킨다고 보고 PASS를 줬다."
  - "검증 근거 리뷰는 문서와 테스트만 다시 확인하는 단계에 맞는 증거 수준으로 대상 테스트와 전체 테스트, 회고 근거만 요구한다고 보고 PASS를 줬다."
  - "가독성 리뷰는 운영자가 무엇을 더 빨리 하게 되는지 `어떤 카드를 먼저 펼치고 다음에 어느 상세 예시 표로 내려가는지`로 직접 읽힌다고 보고 PASS를 줬다."
next_task_warnings:
  - "TASK-0002에서는 종료 코드 빠른 참조 표를 다시 만들지 않고, 실제 실패 상황에서 어느 표를 먼저 펼치고 다음에 어느 상세 예시 표로 내려갈지 예시만 정리한다."
  - "실제 앱/H2 대표 검증 재실행은 이번 단계 비대상이다."
error_signature: ""
test_result_summary: "대상 테스트와 전체 테스트가 모두 순차 실행으로 통과했다. 실제 앱/H2 대표 검증은 이번 task 비대상으로 수행하지 않았다."
snapshot_hashes: []
---

# 작업 회고

## 작업 요약
- `SPEC-0035` 단계 요약과 빠른 참조 문서를 다시 읽었다.
- 관련 자동 검증 테스트를 다시 돌려 그대로 통과하는지 확인했다.
- 이번 작업에서는 새 적용 예시를 아직 쓰지 않고, 직전 단계 문서와 테스트만 다시 확인했다.

## 실패 요약
- 실행 실패는 없었다.
- 이슈 생성 초안에서 백틱이 셸에서 깨져 `#140` 본문 일부가 비었다.
- 중복 이슈는 생기지 않았고, 같은 `#140` 본문만 바로 수정해 같은 이슈 하나로 유지했다.

## 근본 원인
- 적용 예시 단계는 빠른 참조 카드 자체를 다시 만드는 단계가 아니라, 이미 있는 카드를 실제 상황에 대입하는 단계다.
- 그래서 시작 전에 직전 단계 문서와 테스트를 다시 확인하지 않으면, 새 예시를 붙이면서 카드 기준 자체를 함께 바꾸는 실수가 생길 수 있다.
- 먼저 문서와 테스트를 다시 읽고 돌려 현재 기준을 고정하는 것이 필요했다.

## AGENTS 체크 결과
- 연결 이슈 `#140`을 이번 작업과 1:1로 연결했다.
- 대상 테스트와 전체 테스트를 순차 실행했고, 같은 workspace 산출물을 공유하는 병렬 테스트는 만들지 않았다.
- 이번 작업은 빠른 참조 문서와 테스트를 다시 확인하는 단계라 실제 스크립트 재실행이나 H2 확인까지는 하지 않았다.
- 3개 서브에이전트는 문서 경계, 검증 근거, 가독성 관점으로 리뷰했고 최종 PASS를 줬다.

## 근거 자료
- [prd.md](/home/seaung13/workspace/agile-runner/.agents/prd.md)
- [spec.md](/home/seaung13/workspace/agile-runner/.agents/active/spec.md)
- [tasks.md](/home/seaung13/workspace/agile-runner/.agents/active/tasks.md)
- [SPEC-0036-manual-rerun-script-application-example.json](/home/seaung13/workspace/agile-runner/.agents/criteria/SPEC-0036-manual-rerun-script-application-example.json)
- [SPEC-0035-summary.md](/home/seaung13/workspace/agile-runner/.agents/outer-loop/retrospectives/SPEC-0035/SPEC-0035-summary.md)
- [manual-rerun-response-seed-command-guide.md](/home/seaung13/workspace/agile-runner/docs/manual-rerun-response-seed-command-guide.md)

## 다음 작업 경고사항
- 종료 코드 빠른 참조 표는 다시 쓰지 않는다.
- `TASK-0002`에서는 종료 코드 실패 상황에서 어느 표를 먼저 펼치고 다음에 어느 상세 예시 표로 내려갈지만 예시로 정리한다.
- 이번 단계 첫 작업은 문서와 테스트를 다시 확인하는 단계였고, 실제 앱/H2 대표 검증 재실행은 하지 않았다.

## 제안 필요 여부
- 없음
- 새 제안 없음. 이번 교훈은 적용 예시 단계로 넘어갈 때 먼저 직전 단계 빠른 참조 문서와 자동 검증 테스트를 다시 읽고 돌려 카드 기준을 고정해야 한다는 점이었다.
