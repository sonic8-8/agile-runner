---
spec_id: SPEC-0026
task_id: TASK-0002
github_issue_number: 101
criteria_keys:
  - manual-rerun-seed-apply-command-documented
delivery_ids: []
execution_keys: []
test_evidence_ref:
  - "대상 테스트: ./scripts/gradlew-java21.sh --no-daemon test --tests 'com.agilerunner.client.agentruntime.ManualRerunResponseSeedSqlTest' --tests 'com.agilerunner.client.agentruntime.ManualRerunResponseSeedEvidenceSqlTest' --tests 'com.agilerunner.api.controller.review.ManualRerunControllerTest' --tests 'com.agilerunner.api.service.review.ManualRerunServiceTest' --tests 'com.agilerunner.api.service.review.ManualRerunRetryServiceTest' --tests 'com.agilerunner.api.service.review.ManualRerunQueryServiceTest' --tests 'com.agilerunner.api.service.review.ManualRerunExecutionListServiceTest' --tests 'com.agilerunner.api.service.review.ManualRerunControlActionHistoryServiceTest' --tests 'com.agilerunner.api.service.review.ManualRerunControlActionServiceTest' --console=plain"
  - "전체 테스트: ./scripts/gradlew-java21.sh --no-daemon cleanTest test --console=plain"
  - "문서 재구성 리뷰: Ptolemy PASS, Dirac PASS, Hilbert PASS"
diff_ref: "git diff -- docs/manual-rerun-response-seed-command-guide.md .agents/active/tasks.md .agents/outer-loop/retrospectives/SPEC-0026/TASK-0002-seed-apply-command-guide.md .agents/outer-loop/registry.json"
failure_summary: "새 실패는 없었다. 적용/정리 명령 범위를 문서로 한정하고, 실행 키 추출과 H2 조회는 다음 task로 분리했다."
root_cause: "대표 검증 helper command는 한 문서에 모두 몰아두면 적용/정리 단계와 실행 키 추출/H2 조회 단계가 섞이기 쉽다. 이번 task는 apply/reset과 앱 기동 경계까지만 닫아 중간 문서 사용성 근거를 먼저 확보하는 방식이 더 안전했다."
agents_check_findings:
  - "문서 경계 리뷰는 새 명령 가이드가 apply/reset/app boot 경계까지만 다루고 실행 키 추출/H2 조회를 다음 task로 넘긴 구성을 PASS로 봤다."
  - "검증 근거 리뷰는 시나리오별 시작 순서와 `여기서 멈춘다` 문구가 문서만 읽고 절차를 재구성하는 근거로 충분하다고 봤다."
  - "가독성 리뷰는 `앱 기동까지`, `다음 단계 문서에서 다룸` 같은 경계 문장이 새 작업자 입장에서 자연스럽게 읽히는지 확인한 뒤 PASS를 줬다."
next_task_warnings:
  - "TASK-0003에서는 실행 키 추출과 H2 조회만 다뤄야 한다. seed apply/reset 명령을 다시 늘리지 않는 편이 낫다."
  - "retry 파생 실행 키를 응답에서 바로 기록하는 절차를 helper command로 정리하지 않으면 TASK-0004 representative 재현성이 다시 흔들릴 수 있다."
error_signature: ""
test_result_summary: "관련 대상 테스트와 전체 cleanTest test가 모두 통과했다. actual app/H2 representative 재실행은 이번 task 범위가 아니며, 문서만으로 적용 절차를 재구성할 수 있는 review evidence를 함께 남겼다."
snapshot_hashes: []
---

# 작업 회고

## 작업 요약
- seed apply/reset과 앱 기동 전후 경계만 다루는 새 명령 가이드를 추가했다.
- retry 원본 실행 준비, rerun acknowledge 상태 준비, 공통 정리 명령, 앱 기동 명령을 한 문서에서 읽을 수 있게 정리했다.
- 실행 키 추출과 H2 조회는 다음 task 범위로 명확히 넘겼다.

## 실패 요약
- 새 실패는 없었다.
- 중간 리뷰에서 실행 키 추출과 H2 조회 범위가 슬쩍 섞인다는 지적이 있어, 기존 seed guide 링크를 되돌리고 command guide 책임만 좁혔다.

## Root Cause
- representative 절차를 빠르게 따라가게 하려면 명령을 한 곳에 모아야 하지만, 동시에 모든 단계를 다 넣으면 apply/reset과 query 단계가 섞인다.
- 이번 task에서는 apply/reset과 앱 기동 경계까지만 문서화해 중간 사용성 근거를 먼저 확보하는 쪽이 더 안전했다.

## AGENTS 체크 결과
- linked issue `#101`을 `TASK-0002`와 1:1로 유지했다.
- 대상 테스트와 전체 테스트를 순차 실행했다.
- 이번 task는 문서와 절차 정리 단계라 actual app/H2 representative 재실행은 비대상으로 유지했다.
- 3개 서브에이전트는 문서 경계, 검증 근거, 가독성 관점으로 리뷰했고 최종 PASS로 수렴했다.

## 근거 자료
- 새 명령 가이드
  - [manual-rerun-response-seed-command-guide.md](/home/seaung13/workspace/agile-runner/docs/manual-rerun-response-seed-command-guide.md)
- 준비 데이터 가이드
  - [manual-rerun-response-seed-guide.md](/home/seaung13/workspace/agile-runner/docs/manual-rerun-response-seed-guide.md)
- representative 시작 근거
  - [TASK-0004-seed-representative-application-verified.md](/home/seaung13/workspace/agile-runner/.agents/outer-loop/retrospectives/SPEC-0025/TASK-0004-seed-representative-application-verified.md)

## 다음 Task 경고사항
- `TASK-0003`에서는 `retry` 응답에서 받은 파생 실행 키를 어디에 다시 쓰는지와 H2 실행 근거 조회 명령만 다루는 편이 경계가 선명하다.
- 명령 예시를 늘릴 때 `앱 기동까지`와 `앱 종료 후 조회` 경계가 다시 섞이지 않도록 주의해야 한다.

## 제안 필요 여부
- 없음
- 새 제안 없음. 이번 교훈은 새 규칙 부족이 아니라, 이미 있는 task 경계를 문서 책임에 맞게 더 단단히 지키는 쪽이었다.
