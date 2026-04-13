---
spec_id: SPEC-0027
task_id: TASK-0002
github_issue_number: 105
criteria_keys:
  - manual-rerun-script-boundary-documented
delivery_ids: []
execution_keys: []
test_evidence_ref:
  - "대상 테스트: ./scripts/gradlew-java21.sh --no-daemon test --tests 'com.agilerunner.client.agentruntime.ManualRerunResponseSeedSqlTest' --tests 'com.agilerunner.client.agentruntime.ManualRerunResponseSeedEvidenceSqlTest' --tests 'com.agilerunner.api.controller.review.ManualRerunResponseGuideFixtureTest' --tests 'com.agilerunner.api.controller.review.ManualRerunControllerTest' --tests 'com.agilerunner.api.service.review.ManualRerunServiceTest' --tests 'com.agilerunner.api.service.review.ManualRerunRetryServiceTest' --tests 'com.agilerunner.api.service.review.ManualRerunQueryServiceTest' --tests 'com.agilerunner.api.service.review.ManualRerunExecutionListServiceTest' --tests 'com.agilerunner.api.service.review.ManualRerunControlActionHistoryServiceTest' --tests 'com.agilerunner.api.service.review.ManualRerunControlActionServiceTest' --console=plain"
  - "전체 테스트: ./scripts/gradlew-java21.sh --no-daemon cleanTest test --console=plain"
diff_ref: "git diff -- docs/manual-rerun-response-seed-command-guide.md docs/manual-rerun-response-seed-guide.md .agents/outer-loop/retrospectives/SPEC-0027/TASK-0002-script-boundary-documented.md .agents/outer-loop/registry.json"
failure_summary: "실행 실패는 없었다. 문서가 어떤 명령을 보여 주는지는 충분했지만, 어디까지를 스크립트 후보로 보고 어디부터는 사람이 실행 근거를 직접 확인해야 하는지가 명확하지 않은 상태였다."
root_cause: "기존 보조 명령 가이드는 명령 예시를 한 흐름으로 보여 주는 데는 충분했지만, 스크립트 검토 단계에서 필요한 `입력 값`, `출력 근거 자료`, `수동 확인 포인트` 분리가 없었다. 준비 데이터 가이드와 보조 명령 가이드의 역할 경계도 새 작업자 입장에서는 암묵지로 남아 있었다."
agents_check_findings:
  - "문서 경계 리뷰는 보조 명령 가이드가 스크립트 후보 단계와 수동 확인 단계를 정리하는 수준에 머물고, 준비 데이터 가이드와 역할 분리가 유지된다고 판단해 PASS를 줬다."
  - "검증 근거 리뷰는 입력 값, 출력 근거 자료, 수동 확인 단계가 문서만으로 재구성 가능해 현재 기준을 닫는다고 판단해 PASS를 줬다."
  - "가독성 리뷰는 대표 검증, 실행 키 같은 설명 문장을 한국어 중심으로 낮추고 목록 형식을 바로 읽히게 정리한 뒤 PASS를 줬다."
next_task_warnings:
  - "TASK-0003에서는 이 문서 경계를 바탕으로 이점과 위험만 정리해야 하고, 실제 스크립트 초안이나 새 보조 명령을 추가하면 안 된다."
  - "스크립트 후보 단계라고 적어 둔 명령도 결과 의미 해석과 회고 작성까지 자동화 대상으로 넓히면 현재 범위를 넘는다."
error_signature: ""
test_result_summary: "대상 테스트와 전체 테스트가 모두 통과했다. 실제 앱/H2 대표 검증은 이번 작업 비대상으로 생략했고, 그 이유는 입력/출력 경계와 수동 확인 포인트를 문서화하는 단계라 새 실행 근거를 만들 필요가 없기 때문이다."
snapshot_hashes: []
---

# 작업 회고

## 작업 요약
- 보조 명령 가이드에 `스크립트 후보 단계`, `수동 확인 단계`, `입력 값`, `출력 근거 자료`를 한 표로 정리했다.
- 준비 데이터 가이드에 파일 선택/순서 기준 문서와 보조 명령 가이드의 역할 경계를 추가했다.
- 새 작업자가 두 문서만 읽고도 어떤 단계는 스크립트 후보이고, 어떤 결과는 사람이 직접 확인해야 하는지 재구성할 수 있게 정리했다.

## 실패 요약
- 실행 실패는 없었다.
- 다만 처음 문서에는 `대표 검증 절차를 따라가는 명령`만 있고, `어디까지가 자동화 후보인지`와 `어디부터는 사람이 직접 해석해야 하는지`가 분리돼 있지 않았다.

## Root Cause
- 보조 명령 가이드는 명령 예시를 빠르게 따라가게 해 주지만, 스크립트 검토 단계에서는 그보다 더 좁은 질문이 필요했다.
- 즉 `무슨 명령이 있나`보다 `어떤 입력을 받아 어떤 출력 근거 자료를 남기고, 어떤 판단은 사람이 직접 해야 하나`가 먼저 보여야 했다.
- 준비 데이터 가이드와 보조 명령 가이드가 각각 무엇을 책임지는지 문서 안에서 한 번 더 풀어써야 새 작업자가 범위를 잘못 넓히지 않는다.

## AGENTS 체크 결과
- 연결 이슈 `#105`를 `TASK-0002`와 1:1로 연결했다.
- 3개 서브에이전트는 문서 경계, 검증 근거, 가독성 관점으로 리뷰했고 최종 전원 PASS가 나왔다.
- 대상 테스트와 전체 테스트를 순차 실행했고, 같은 workspace 산출물을 공유하는 테스트 병렬 실행은 만들지 않았다.
- 실제 앱/H2 대표 검증은 이번 작업 비대상으로 생략했고, 그 이유를 회고에 남겼다.

## 근거 자료
- 보조 명령 가이드
  - [manual-rerun-response-seed-command-guide.md](/home/seaung13/workspace/agile-runner/docs/manual-rerun-response-seed-command-guide.md)
- 준비 데이터 가이드
  - [manual-rerun-response-seed-guide.md](/home/seaung13/workspace/agile-runner/docs/manual-rerun-response-seed-guide.md)
- 현재 활성 작업 문서
  - [tasks.md](/home/seaung13/workspace/agile-runner/.agents/active/tasks.md)
- 현재 기준 문서
  - [SPEC-0027-manual-rerun-script-review.json](/home/seaung13/workspace/agile-runner/.agents/criteria/SPEC-0027-manual-rerun-script-review.json)
- 시작 근거 회고
  - [TASK-0004-helper-command-representative-verified.md](/home/seaung13/workspace/agile-runner/.agents/outer-loop/retrospectives/SPEC-0026/TASK-0004-helper-command-representative-verified.md)

## 다음 Task 경고사항
- `TASK-0003`는 반복 비용 절감, 실행 근거 가시성, H2 lock 대응, 유지 비용 같은 이점과 위험만 정리해야 한다.
- 스크립트 후보 단계가 정리됐다고 해서 실제 스크립트 초안이나 자동 명령 추가를 당겨오면 범위가 깨진다.

## 제안 필요 여부
- 없음
- 새 제안 없음. 이번 교훈은 새 절차 공백이 아니라, 현재 문서 두 개의 역할 경계와 수동 확인 지점을 명시적으로 적어 두면 충분하다는 점을 재확인한 것이다.
