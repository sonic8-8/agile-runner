---
spec_id: SPEC-0026
task_id: TASK-0003
github_issue_number: 102
criteria_keys:
  - manual-rerun-seed-query-command-documented
delivery_ids: []
execution_keys: []
test_evidence_ref:
  - "대상 테스트: ./scripts/gradlew-java21.sh --no-daemon test --tests 'com.agilerunner.client.agentruntime.ManualRerunResponseSeedSqlTest' --tests 'com.agilerunner.client.agentruntime.ManualRerunResponseSeedEvidenceSqlTest' --tests 'com.agilerunner.api.controller.review.ManualRerunResponseGuideFixtureTest' --tests 'com.agilerunner.api.controller.review.ManualRerunControllerTest' --tests 'com.agilerunner.api.service.review.ManualRerunServiceTest' --tests 'com.agilerunner.api.service.review.ManualRerunRetryServiceTest' --tests 'com.agilerunner.api.service.review.ManualRerunQueryServiceTest' --tests 'com.agilerunner.api.service.review.ManualRerunExecutionListServiceTest' --tests 'com.agilerunner.api.service.review.ManualRerunControlActionHistoryServiceTest' --tests 'com.agilerunner.api.service.review.ManualRerunControlActionServiceTest' --console=plain"
  - "전체 테스트: ./scripts/gradlew-java21.sh --no-daemon cleanTest test --console=plain"
  - "문서 재구성 리뷰: Ptolemy PASS, Dirac PASS, Hilbert PASS"
diff_ref: "git diff -- docs/manual-rerun-response-seed-command-guide.md docs/manual-rerun-response-seed-guide.md .agents/outer-loop/retrospectives/SPEC-0026/TASK-0003-execution-key-and-h2-query-guide.md .agents/outer-loop/registry.json"
failure_summary: "새 실패는 없었다. 중간 리뷰에서 command guide 범위 설명과 seed guide 역할 설명이 엇갈린다는 지적이 있었고, 문구를 다시 맞춰 닫았다."
root_cause: "helper command 문서는 apply/reset, 실행 키 추출, H2 조회를 한 흐름으로 묶어야 재구성이 쉬워지지만, 역할 설명이 느슨하면 seed guide와 command guide 경계가 다시 섞인다. 이번 task에서는 명령 순서를 추가한 뒤 문서 책임을 다시 선명하게 맞추는 작업이 필요했다."
agents_check_findings:
  - "문서 경계 리뷰는 command guide가 retry 파생 실행 키 추출과 앱 종료 뒤 H2 조회까지 다루고, seed guide는 규칙과 선택 기준 문서로 남는 구조를 PASS로 봤다."
  - "검증 근거 리뷰는 `jq` 없이도 retry 파생 실행 키를 읽고, rerun/retry H2 evidence를 같은 실행 키 기준으로 다시 조회하는 흐름이 기존 representative evidence와 충돌하지 않는다고 판단했다."
  - "가독성 리뷰는 `원본 실행 키`, `대표 검증`, `관리자 조치 요청과 확인 흐름`처럼 새 작업자가 바로 읽히는 표현으로 정리된 뒤 PASS를 줬다."
next_task_warnings:
  - "TASK-0004에서는 문서만으로 representative rerun/retry 검증을 다시 수행해야 하므로, 변수 이름과 실제 명령 순서를 문서와 정확히 같게 써야 한다."
  - "앱 종료 뒤 H2 조회는 같은 파일을 여러 셸에서 동시에 열지 않는 규칙을 다시 지켜야 한다."
error_signature: ""
test_result_summary: "관련 대상 테스트와 전체 cleanTest test가 모두 통과했다. actual app/H2 representative 재실행은 이번 task 범위가 아니며, 문서만으로 실행 키 추출과 H2 조회 순서를 재구성할 수 있는 review evidence를 남겼다."
snapshot_hashes: []
---

# 작업 회고

## 작업 요약
- retry 응답에서 파생 실행 키를 읽는 명령 예시를 command guide에 추가했다.
- rerun 고정 실행 키와 retry 파생 실행 키를 어디에서 다시 쓰는지 문서로 연결했다.
- 앱 종료 뒤 H2 `WEBHOOK_EXECUTION`, `AGENT_EXECUTION_LOG`, `MANUAL_RERUN_CONTROL_ACTION_AUDIT`를 확인하는 보조 명령 예시를 정리했다.
- seed guide는 규칙과 선택 기준 문서, command guide는 실제 명령 문서라는 역할 분리를 다시 맞췄다.

## 실패 요약
- 새 실패는 없었다.
- 중간 리뷰에서 command guide 범위 설명과 seed guide 역할 설명이 아직 느슨하다는 지적이 나와, 같은 단계 안에서 문구를 다시 정리했다.

## Root Cause
- 실행 키 추출과 H2 조회 절차를 문서로 한 번에 이어 두면 재구성은 쉬워지지만, 역할 설명이 느슨하면 seed guide와 command guide가 다시 같은 일을 하는 것처럼 보일 수 있다.
- 이번 task는 명령 자체를 더 추가하는 것보다, 어느 문서가 규칙을 설명하고 어느 문서가 실제 명령을 설명하는지 분리하는 작업이 중요했다.

## AGENTS 체크 결과
- linked issue `#102`를 `TASK-0003`과 1:1로 유지했다.
- 대상 테스트와 전체 테스트를 순차 실행했다.
- 이번 task는 문서와 helper command 정리 단계라 actual app/H2 representative 재실행은 비대상으로 유지했다.
- 3개 서브에이전트는 문서 경계, 검증 근거, 가독성 관점으로 리뷰했고 최종 PASS로 수렴했다.

## 근거 자료
- 보조 명령 가이드
  - [manual-rerun-response-seed-command-guide.md](/home/seaung13/workspace/agile-runner/docs/manual-rerun-response-seed-command-guide.md)
- 준비 데이터 가이드
  - [manual-rerun-response-seed-guide.md](/home/seaung13/workspace/agile-runner/docs/manual-rerun-response-seed-guide.md)
- 기존 representative 근거
  - [TASK-0004-seed-representative-application-verified.md](/home/seaung13/workspace/agile-runner/.agents/outer-loop/retrospectives/SPEC-0025/TASK-0004-seed-representative-application-verified.md)

## 다음 Task 경고사항
- `TASK-0004`에서는 문서에 적은 변수 이름과 명령 순서를 그대로 써서 representative 검증을 다시 닫는 편이 안전하다.
- retry는 응답에서 받은 파생 실행 키를 즉시 기록하지 않으면 H2 evidence 조회가 흔들릴 수 있으므로, command guide 순서를 건너뛰지 않는 편이 낫다.

## 제안 필요 여부
- 없음
- 새 제안 없음. 이번 교훈은 새 규칙 부족이 아니라, 이미 있는 문서 역할 분리와 순차 검증 규칙을 문구 수준에서 더 정확히 맞추는 쪽이었다.
