---
spec_id: SPEC-0025
task_id: TASK-0003
github_issue_number: 98
criteria_keys:
  - manual-rerun-seed-evidence-sql-executable
delivery_ids: []
execution_keys:
  - EXECUTION:MANUAL_RERUN:example-rerun
  - EXECUTION:MANUAL_RERUN:example-retry-derived
test_evidence_ref:
  - "대상 테스트: ./scripts/gradlew-java21.sh --no-daemon test --tests 'com.agilerunner.client.agentruntime.ManualRerunResponseSeedSqlTest' --tests 'com.agilerunner.client.agentruntime.ManualRerunResponseSeedEvidenceSqlTest' --tests 'com.agilerunner.api.controller.review.ManualRerunResponseGuideFixtureTest' --tests 'com.agilerunner.api.controller.review.ManualRerunControllerTest' --tests 'com.agilerunner.api.service.review.ManualRerunServiceTest' --tests 'com.agilerunner.api.service.review.ManualRerunRetryServiceTest' --tests 'com.agilerunner.api.service.review.ManualRerunQueryServiceTest' --tests 'com.agilerunner.api.service.review.ManualRerunExecutionListServiceTest' --tests 'com.agilerunner.api.service.review.ManualRerunControlActionHistoryServiceTest' --tests 'com.agilerunner.api.service.review.ManualRerunControlActionServiceTest' --console=plain"
  - "전체 테스트: ./scripts/gradlew-java21.sh --no-daemon cleanTest test --console=plain"
diff_ref: "git diff -- docs/manual-rerun-response-seed-guide.md src/test/resources/manual-rerun-response-seed/runtime-evidence/rerun-runtime-evidence-check.example.sql src/test/resources/manual-rerun-response-seed/runtime-evidence/retry-runtime-evidence-check.example.sql && git status --short src/test/java/com/agilerunner/client/agentruntime/ManualRerunResponseSeedEvidenceSqlTest.java"
failure_summary: "첫 리뷰에서는 실행 근거 확인 SQL이 내보내는 컬럼을 테스트가 전부 보증하지 못했고, guide 본문에 영어식 표현이 남아 두 관점에서 NEEDS_CHANGES가 나왔다."
root_cause: "실행 근거 확인 SQL에 실제 SELECT를 넣는 것만으로 기준이 닫힌다고 보기 쉬웠지만, projection이 늘어나면 테스트도 그 의미를 함께 닫아야 false positive를 막을 수 있다. 또 guide는 파일명 규칙과 설명 문장을 분리해야 새 작업자가 읽기 쉬운데, 처음엔 파일명 표현이 본문까지 그대로 남았다."
agents_check_findings:
  - "문서 경계 리뷰는 runtime-evidence SQL과 H2 메모리 검증 테스트가 representative actual app/H2 재검증을 대신하지 않고 TASK-0003 범위 안에 머무는지 확인한 뒤 PASS를 줬다."
  - "검증 근거 리뷰는 rerun audit의 note, applied_at과 retry log의 step_name, error_code, failure_disposition까지 검증한 뒤 PASS를 줬다."
  - "가독성 리뷰는 guide 설명 문장에서 row, file, Shell, CLI, source execution seed 같은 표현을 결과 행, H2 파일, 셸, 명령줄 도구, 원본 실행 준비 데이터 파일로 풀어 쓴 뒤 PASS를 줬다."
next_task_warnings:
  - "TASK-0004에서는 example execution key가 아니라 실제 representative delivery_id, execution_key를 새 값으로 만들고, seed SQL 적용 -> 앱 기동 -> 요청 실행 -> 앱 종료 -> H2 조회 순서를 지켜야 한다."
  - "실제 앱/H2 검증에서는 runtime-evidence SQL을 바로 재사용하기보다 representative execution_key에 맞는 조회 방법과 H2 lock 여부 확인 순서를 retrospective에 함께 남겨야 한다."
error_signature:
test_result_summary: "runtime-evidence SQL 두 파일에 실제 SELECT를 넣고, H2 메모리 DB에서 rerun은 WEBHOOK_EXECUTION과 MANUAL_RERUN_CONTROL_ACTION_AUDIT, retry는 WEBHOOK_EXECUTION과 AGENT_EXECUTION_LOG를 조회하는 검증 테스트를 추가했다. 대상 테스트와 전체 cleanTest 테스트는 모두 통과했다."
snapshot_hashes: []
---

# 작업 회고

## 작업 요약
- rerun, retry 대표 검증 뒤에 확인할 실행 근거 SQL 두 파일에 실제 SELECT를 추가했다.
- H2 메모리 DB에서 schema와 준비 데이터 SQL을 적용한 뒤, 실행 근거 확인 SQL 파일을 그대로 읽어 실제로 실행하는 검증 테스트를 추가했다.
- guide 문서에는 실행 근거 확인 파일의 역할과 준비 데이터 파일, 기준 파일, 대표 검증 결과의 경계를 현재 단계에 맞게 정리했다.

## 실패 요약
- 코드나 SQL 실행 자체는 실패하지 않았다.
- 첫 리뷰에서는 retry log query가 내보내는 step_name, error_code, failure_disposition과 rerun audit query의 note, applied_at이 테스트로 닫히지 않아 false positive 여지가 있다는 지적이 나왔다.
- guide 문장도 설명 파일 본문에 영어식 표현이 남아 있어 새 작업자가 바로 읽기 어렵다는 지적이 있었다.

## Root Cause
- 실행 근거 확인 SQL은 대표 검증 뒤 어떤 열을 볼지 정리하는 역할이라, SELECT 문만 추가하면 끝났다고 보기 쉬웠다.
- 하지만 실제 검증은 SQL projection과 테스트 검증 범위가 같이 닫혀야 의미가 있다. projection이 늘어났는데 테스트가 일부 열만 보면 문서와 SQL이 넓어진 만큼의 실행 근거를 보증하지 못한다.
- guide는 파일명 규칙을 그대로 설명 문장에 옮기면 빠르게 읽기 어렵다. 파일명과 문장 역할을 분리해 풀어 써야 운영 문서로서 읽기 쉬워진다.

## AGENTS 체크 결과
- linked issue `#98`을 `TASK-0003`와 1:1로 유지했다.
- 3개 서브에이전트는 문서 경계, 검증 근거, 가독성으로 역할을 나눠 리뷰했고, 보정 뒤 최종 PASS로 수렴했다.
- 대상 테스트와 전체 `cleanTest test`를 순차 실행했다.
- 실제 앱/H2 representative 검증은 이번 작업 비대상으로 유지했고, 그 이유와 다음 단계 경계를 문서와 회고에 함께 남겼다.

## 근거 자료
- guide 문서
  - [manual-rerun-response-seed-guide.md](/home/seaung13/workspace/agile-runner/docs/manual-rerun-response-seed-guide.md)
- 실행 근거 확인 SQL
  - [rerun-runtime-evidence-check.example.sql](/home/seaung13/workspace/agile-runner/src/test/resources/manual-rerun-response-seed/runtime-evidence/rerun-runtime-evidence-check.example.sql)
  - [retry-runtime-evidence-check.example.sql](/home/seaung13/workspace/agile-runner/src/test/resources/manual-rerun-response-seed/runtime-evidence/retry-runtime-evidence-check.example.sql)
- 검증 테스트
  - [ManualRerunResponseSeedEvidenceSqlTest.java](/home/seaung13/workspace/agile-runner/src/test/java/com/agilerunner/client/agentruntime/ManualRerunResponseSeedEvidenceSqlTest.java)
- 검증 명령
  - 대상 테스트: `./scripts/gradlew-java21.sh --no-daemon test --tests 'com.agilerunner.client.agentruntime.ManualRerunResponseSeedSqlTest' --tests 'com.agilerunner.client.agentruntime.ManualRerunResponseSeedEvidenceSqlTest' --tests 'com.agilerunner.api.controller.review.ManualRerunResponseGuideFixtureTest' --tests 'com.agilerunner.api.controller.review.ManualRerunControllerTest' --tests 'com.agilerunner.api.service.review.ManualRerunServiceTest' --tests 'com.agilerunner.api.service.review.ManualRerunRetryServiceTest' --tests 'com.agilerunner.api.service.review.ManualRerunQueryServiceTest' --tests 'com.agilerunner.api.service.review.ManualRerunExecutionListServiceTest' --tests 'com.agilerunner.api.service.review.ManualRerunControlActionHistoryServiceTest' --tests 'com.agilerunner.api.service.review.ManualRerunControlActionServiceTest' --console=plain`
  - 전체 테스트: `./scripts/gradlew-java21.sh --no-daemon cleanTest test --console=plain`

## 다음 Task 경고사항
- `TASK-0004`는 representative 실제 앱/H2 검증이 핵심이므로, example execution key가 아니라 새 representative delivery_id와 execution_key를 기준으로 evidence를 확인해야 한다.
- 실제 H2 file 조회는 앱 종료 뒤 순차 실행해야 하며, lock 오류가 나면 코드 문제로 단정하기 전에 앱 종료 여부와 다른 조회 프로세스를 먼저 확인해야 한다.

## 제안 필요 여부
- 없음
- 새 제안 없음. 이번 교훈은 새 절차 추가보다 projection과 테스트 범위를 맞추는 실행 문제였고, H2 조회 순차 규칙도 기존 workflow로 충분히 커버된다.
