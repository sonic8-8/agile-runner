---
spec_id: SPEC-0025
task_id: TASK-0002
github_issue_number: 97
criteria_keys:
  - manual-rerun-seed-input-sql-executable
delivery_ids: []
execution_keys:
  - EXECUTION:MANUAL_RERUN:example-retry-source
  - EXECUTION:MANUAL_RERUN:example-rerun
test_evidence_ref:
  - "대상 테스트: ./scripts/gradlew-java21.sh --no-daemon test --tests 'com.agilerunner.client.agentruntime.ManualRerunResponseSeedSqlTest' --tests 'com.agilerunner.api.controller.review.ManualRerunResponseGuideFixtureTest' --tests 'com.agilerunner.api.controller.review.ManualRerunControllerTest' --tests 'com.agilerunner.api.service.review.ManualRerunServiceTest' --tests 'com.agilerunner.api.service.review.ManualRerunRetryServiceTest' --tests 'com.agilerunner.api.service.review.ManualRerunQueryServiceTest' --tests 'com.agilerunner.api.service.review.ManualRerunExecutionListServiceTest' --tests 'com.agilerunner.api.service.review.ManualRerunControlActionHistoryServiceTest' --tests 'com.agilerunner.api.service.review.ManualRerunControlActionServiceTest' --console=plain"
  - "전체 테스트: ./scripts/gradlew-java21.sh --no-daemon cleanTest test --console=plain"
diff_ref: "git diff -- src/test/resources/manual-rerun-response-seed/source-execution/retry-source-execution-seed.example.sql src/test/resources/manual-rerun-response-seed/control-action-history/rerun-acknowledge-action-history-seed.example.sql src/test/java/com/agilerunner/client/agentruntime/ManualRerunResponseSeedSqlTest.java .agents/outer-loop/retrospectives/SPEC-0025/TASK-0002-seed-input-sql-executable.md .agents/outer-loop/registry.json"
failure_summary: "코드나 SQL 실행 자체는 실패하지 않았다. 다만 첫 리뷰에서는 가이드 문서 범위를 넘긴 수정과 enum 값 일치 검증 누락 때문에 두 차례 NEEDS_CHANGES가 나왔다."
root_cause: "입력 준비 데이터 SQL을 실제 INSERT로 채우는 것만으로 기준이 닫힌다고 보기 쉬웠지만, 현재 스키마가 VARCHAR 중심이라 INSERT 성공만으로는 enum 값이 어긋나도 거짓 통과처럼 보이는 위험을 막지 못했다. 또 가이드 문구를 같이 손보려다 TASK-0003 범위를 당겨오는 경계 침범이 생겼다."
agents_check_findings:
  - "문서 경계 리뷰는 가이드 문서 변경을 빼고 입력 준비 데이터 SQL 2개와 H2 메모리 검증 테스트 1개로 범위를 고정한 뒤 PASS를 줬다."
  - "검증 근거 리뷰는 failure_disposition, status, execution_start_type, execution_control_mode, write_skip_reason, error_code, action, action_status를 Enum.valueOf(...)로 모두 검증한 뒤 PASS를 줬다."
  - "가독성 리뷰는 seed 파일 머리말을 결과 행, 근거 행, 단건 조회, 목록 조회, 이력 조회, 관리자 조치 응답처럼 풀어 쓴 뒤 PASS를 줬다."
next_task_warnings:
  - "TASK-0003에서는 입력 준비 데이터 INSERT를 다시 늘리기보다 실행 근거 확인 SQL의 실제 SELECT에만 집중해야 한다."
  - "실행 근거 확인 SQL도 현재 스키마 문자열을 그대로 읽는 데 그치지 말고, 실제 확인에 필요한 execution_key 기준 SELECT를 파일 수준에서 바로 읽을 수 있게 써야 한다."
error_signature:
test_result_summary: "입력 준비 데이터 SQL 2개를 현재 스키마 기준 실제 INSERT로 보강했고, H2 메모리 검증 테스트에서 enum 민감 컬럼까지 함께 확인했다. 대상 테스트와 전체 cleanTest 테스트는 모두 통과했다."
snapshot_hashes: []
---

# 작업 회고

## 작업 요약
- retry 원본 실행 준비 데이터와 rerun acknowledge 조치 이력 준비 데이터를 실제 INSERT로 보강했다.
- 입력 준비 데이터 SQL 두 파일이 현재 스키마와 enum 값에 맞는지 H2 메모리 DB에서 직접 실행하는 검증 테스트를 추가했다.
- 현재 작업 범위를 입력 준비 데이터 SQL 실행 가능성에만 고정하고, 가이드 문서 변경과 실행 근거 SELECT 보강은 다음 작업으로 넘겼다.

## 실패 요약
- 코드나 SQL 실행 자체는 실패하지 않았다.
- 첫 리뷰에서는 가이드 문서 하단 문구를 같이 손보려다 작업 경계를 넘긴 점이 지적됐다.
- 검증 리뷰에서는 문자열 INSERT가 성공해도 enum 값이 어긋나면 거짓 통과처럼 보이는 위험이 남는다는 지적이 나와 테스트를 한 번 더 보강했다.

## Root Cause
- 이번 작업의 핵심은 입력 준비 데이터 SQL을 실제로 실행 가능한 상태로 올리는 것이었는데, 처음에는 INSERT 성공 자체만 실행 근거로 보기 쉬웠다.
- 하지만 `WEBHOOK_EXECUTION`, `MANUAL_RERUN_CONTROL_ACTION_AUDIT`의 enum 성격 컬럼이 모두 `VARCHAR`라서, insert만 통과하면 실제 앱 매핑이 가능한 값인지까지는 보장되지 않았다.
- 또 입력 준비 데이터 SQL 단계에서 가이드 문서까지 같이 건드리면 `TASK-0003`의 실행 근거 SQL 보강 범위를 미리 끌어오게 된다.

## AGENTS 체크 결과
- linked issue `#97`을 `TASK-0002`와 1:1로 유지했다.
- 3개 서브에이전트는 서로 다른 관점으로 리뷰했고, 두 차례 수정 뒤 최종 PASS로 수렴했다.
- 대상 테스트와 전체 `cleanTest 테스트`를 순차 실행했다.
- 실제 앱/H2 대표 검증은 이번 작업 비대상으로 유지했고, 입력 준비 데이터 SQL 실행 가능성은 H2 메모리 DB 검증으로 닫았다.

## 근거 자료
- 입력 준비 데이터 SQL
  - [retry-source-execution-seed.example.sql](/home/seaung13/workspace/agile-runner/src/test/resources/manual-rerun-response-seed/source-execution/retry-source-execution-seed.example.sql)
  - [rerun-acknowledge-action-history-seed.example.sql](/home/seaung13/workspace/agile-runner/src/test/resources/manual-rerun-response-seed/control-action-history/rerun-acknowledge-action-history-seed.example.sql)
- 검증 테스트
  - [ManualRerunResponseSeedSqlTest.java](/home/seaung13/workspace/agile-runner/src/test/java/com/agilerunner/client/agentruntime/ManualRerunResponseSeedSqlTest.java)
- 검증 명령
  - 대상 테스트: `./scripts/gradlew-java21.sh --no-daemon test --tests 'com.agilerunner.client.agentruntime.ManualRerunResponseSeedSqlTest' --tests 'com.agilerunner.api.controller.review.ManualRerunResponseGuideFixtureTest' --tests 'com.agilerunner.api.controller.review.ManualRerunControllerTest' --tests 'com.agilerunner.api.service.review.ManualRerunServiceTest' --tests 'com.agilerunner.api.service.review.ManualRerunRetryServiceTest' --tests 'com.agilerunner.api.service.review.ManualRerunQueryServiceTest' --tests 'com.agilerunner.api.service.review.ManualRerunExecutionListServiceTest' --tests 'com.agilerunner.api.service.review.ManualRerunControlActionHistoryServiceTest' --tests 'com.agilerunner.api.service.review.ManualRerunControlActionServiceTest' --console=plain`
  - 전체 테스트: `./scripts/gradlew-java21.sh --no-daemon cleanTest test --console=plain`

## 다음 Task 경고사항
- `TASK-0003`은 실행 근거 확인 SQL 두 파일에 실제 SELECT를 넣고, 대표 검증 절차와 execution_key 기준 연결만 닫아야 한다.
- 입력 준비 데이터 SQL이 실제로 들어간 뒤에는 가이드 문구를 건드리더라도 실행 근거 SQL 적용 순서 설명처럼 다음 작업 범위에 직접 필요한 문장만 최소 보정해야 한다.

## 제안 필요 여부
- 없음
- 새 제안 없음. 이번 교훈은 새 절차 추가보다 입력 준비 데이터 단계의 테스트 확인 범위를 분명히 하는 실행 문제였다.
