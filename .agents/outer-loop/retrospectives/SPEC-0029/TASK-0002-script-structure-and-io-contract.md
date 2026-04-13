---
spec_id: SPEC-0029
task_id: TASK-0002
github_issue_number: 113
criteria_keys:
  - manual-rerun-script-implementation-draft-structure-documented
delivery_ids: []
execution_keys: []
test_evidence_ref:
  - "대상 테스트: ./scripts/gradlew-java21.sh --no-daemon test --tests 'com.agilerunner.client.agentruntime.ManualRerunResponseSeedSqlTest' --tests 'com.agilerunner.client.agentruntime.ManualRerunResponseSeedEvidenceSqlTest' --tests 'com.agilerunner.api.controller.review.ManualRerunResponseGuideFixtureTest' --tests 'com.agilerunner.api.controller.review.ManualRerunControllerTest' --tests 'com.agilerunner.api.service.review.ManualRerunServiceTest' --tests 'com.agilerunner.api.service.review.ManualRerunRetryServiceTest' --tests 'com.agilerunner.api.service.review.ManualRerunQueryServiceTest' --tests 'com.agilerunner.api.service.review.ManualRerunExecutionListServiceTest' --tests 'com.agilerunner.api.service.review.ManualRerunControlActionHistoryServiceTest' --tests 'com.agilerunner.api.service.review.ManualRerunControlActionServiceTest' --console=plain"
  - "전체 테스트: ./scripts/gradlew-java21.sh --no-daemon cleanTest test --console=plain"
diff_ref: "git diff -- docs/manual-rerun-response-seed-command-guide.md .agents/outer-loop/retrospectives/SPEC-0029/TASK-0002-script-structure-and-io-contract.md .agents/outer-loop/registry.json"
failure_summary: "코드 실패는 없었다. 다만 첫 문서 초안에는 파일 후보 구조는 들어갔지만, 절 이름 참조와 표현 톤이 아직 SPEC-0028 문장을 일부 끌고 와서 새 작업자가 읽을 때 멈출 수 있었다."
root_cause: "기존 보조 명령 가이드 위에 새 절을 빠르게 얹으면서 실제 절 제목, 파일 역할, 입력/출력 계약 표현을 현재 작업 용어로 완전히 다시 맞추기 전에 초안이 먼저 만들어졌다."
agents_check_findings:
  - "문서 경계 리뷰는 파일 구조, 입력/출력 계약, 책임 분리 절이 이번 작업 범위에 머무르고 다음 작업의 종료 흐름을 당겨오지 않는다고 봤다."
  - "검증 근거 리뷰는 guide 문서만으로 파일 후보 위치, 입력 인자, 출력 파일, 직접 다룰 명령 묶음을 재구성할 수 있어 이번 작업의 검증 기준을 닫는 근거가 충분하다고 봤다."
  - "가독성 리뷰는 `이번 작업`, `입력/출력 계약`, 실제 절 제목 참조로 표현을 낮춘 뒤 통과가 났다."
next_task_warnings:
  - "다음 작업은 종료 상태 기준과 수동 확인 인계 지점을 정리하는 단계라, 이번 작업에서 파일이 멈춰야 하는 조건까지 자세히 끌어오면 범위가 겹친다."
  - "파일 후보 위치와 출력 파일 이름을 고정했더라도 실제 종료 코드와 실패 분기 판단은 다음 작업에서 따로 닫아야 한다."
error_signature: ""
test_result_summary: "대상 테스트와 전체 테스트가 모두 통과했다. 실제 앱/H2 대표 검증은 이번 작업 비대상으로 생략했고, 그 이유는 파일 구조와 입력/출력 계약을 문서로 정리하는 단계였기 때문이다."
snapshot_hashes: []
---

# 작업 회고

## 작업 요약
- 보조 명령 가이드에 초안 파일 후보 구조를 추가했다.
- 파일별 입력 인자와 출력 파일 계약을 문서로 고정했다.
- 기존 보조 명령 문서가 계속 설명 문서로 남고, 초안 파일 후보는 명령 실행과 결과 파일 저장만 담당한다는 책임 경계를 분리했다.

## 실패 요약
- 실행 실패는 없었다.
- 다만 첫 문서 초안에는 절 제목 참조와 표현 통일이 덜 돼 있어, 지금 단계가 실제 구현이 아니라 파일 구조와 입력/출력 계약 정리라는 점이 한 번에 읽히지 않았다.

## Root Cause
- 기존 보조 명령 가이드 위에 새 절을 추가하는 방식으로 정리하면서, 새 절 제목과 기존 절 참조를 함께 바꿔야 한다는 점이 한 번에 잠기지 않았다.
- 파일 후보 구조를 먼저 넣은 뒤에야 어떤 표현이 이번 작업 범위를 넘기고 있는지가 리뷰에서 분명해졌다.

## AGENTS 체크 결과
- 이번 작업과 이슈 `#113`을 1:1로 연결했다.
- 3개 서브에이전트는 문서 경계, 검증 근거, 가독성 관점으로 리뷰했고 최종 전원 통과가 났다.
- 대상 테스트와 전체 테스트를 순차 실행했고, 같은 작업 공간 산출물을 공유하는 테스트 병렬 실행은 만들지 않았다.
- 실제 앱/H2 대표 검증은 이번 작업 비대상으로 생략했고, 그 이유를 회고에 남겼다.

## 근거 자료
- 현재 보조 명령 가이드
  - [manual-rerun-response-seed-command-guide.md](/home/seaung13/workspace/agile-runner/docs/manual-rerun-response-seed-command-guide.md)
- 현재 단계 문서
  - [spec.md](/home/seaung13/workspace/agile-runner/.agents/active/spec.md)
- 현재 작업 문서
  - [tasks.md](/home/seaung13/workspace/agile-runner/.agents/active/tasks.md)
- 직전 단계 요약 문서
  - [SPEC-0028-summary.md](/home/seaung13/workspace/agile-runner/.agents/outer-loop/retrospectives/SPEC-0028/SPEC-0028-summary.md)

## 다음 단계 경고사항
- 다음 작업은 종료 상태 기준과 수동 확인 인계 지점을 따로 닫는 단계다. 이번 작업에서 파일 구조와 입력/출력 계약을 정리했다는 이유로 종료 흐름까지 이미 결정된 것처럼 쓰면 안 된다.
- `prepare-seed.sh`, `run-rerun.sh`, `run-retry.sh`, `collect-evidence.sh` 후보는 실제 파일 추가가 아니라 후보 구조다. 다음 단계에서도 계속 문서 후보로 다뤄야 한다.

## 제안 필요 여부
- 없음
- 새 제안 없음. 이번 교훈은 새 규칙 추가보다, 보조 명령 가이드 문서 안에서 절 제목과 책임 경계를 현재 작업 이름과 같은 말로 맞춰 읽게 만드는 쪽에 있었다.
