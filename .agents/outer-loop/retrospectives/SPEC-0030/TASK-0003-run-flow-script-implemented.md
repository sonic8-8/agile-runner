---
spec_id: SPEC-0030
task_id: TASK-0003
github_issue_number: 118
criteria_keys:
  - manual-rerun-script-draft-run-flow-implemented
delivery_ids: []
execution_keys: []
test_evidence_ref:
  - "대상 테스트: ./scripts/gradlew-java21.sh --no-daemon test --console=plain --tests 'com.agilerunner.client.agentruntime.ManualRerunRunFlowScriptTest' --tests 'com.agilerunner.client.agentruntime.ManualRerunSeedCommandScriptTest' --tests 'com.agilerunner.client.agentruntime.ManualRerunResponseSeedSqlTest' --tests 'com.agilerunner.client.agentruntime.ManualRerunResponseSeedEvidenceSqlTest'"
  - "전체 테스트: ./scripts/gradlew-java21.sh --no-daemon cleanTest test --console=plain"
diff_ref: "scripts/manual-rerun-response/run-rerun.sh, scripts/manual-rerun-response/run-retry.sh, scripts/manual-rerun-response/collect-evidence.sh, src/test/java/com/agilerunner/client/agentruntime/ManualRerunRunFlowScriptTest.java, .agents/outer-loop/retrospectives/SPEC-0030/TASK-0003-run-flow-script-implemented.md, .agents/outer-loop/registry.json"
failure_summary: "첫 구현에서는 collect-evidence.sh가 H2 명령줄 도구의 실패를 종료 코드만으로 판단해 41/42 분기가 흔들렸고, 테스트도 비어 있는 DB와 이름만 맞는 H2 프로세스를 정답처럼 고정해 현실성이 부족했다. 이후 잠금 시그니처 기반 분기, 비잠금 41 역방향 케이스, 시작 PID 생존 확인까지 보강한 뒤 대상 테스트와 전체 테스트가 모두 통과했다."
root_cause: "스크립트 초안 구현은 문서 흐름을 옮기는 작업처럼 보여도 실제 도구 특성까지 반영하지 않으면 종료 코드가 쉽게 오판된다. 특히 H2 명령줄 도구는 SQL 오류를 표준 출력으로 남기고 0을 반환할 수 있고, 포트 기반 기동 확인도 시작한 PID 생존 여부를 같이 보지 않으면 오판을 만들 수 있다."
agents_check_findings:
  - "문서 경계 리뷰는 TASK-0003가 재실행, 재시도, 실행 근거 수집 초안 파일과 종료 흐름까지만 닫고 실제 앱/H2 대표 검증은 TASK-0004로 남겼다는 점을 근거로 통과 판정을 줬다."
  - "검증 근거 리뷰는 20~24, 30~33, 40~42 종료 코드, 비잠금 41 역방향 케이스, H2 잠금 시그니처 기반 42 분기, 대상 테스트와 전체 테스트 통과까지 확인한 뒤 통과 판정을 줬다."
  - "가독성 리뷰는 로그 문구와 테스트 이름을 한국어 문장형으로 정리하고 실패 의미를 직접 드러낸 뒤 통과 판정을 줬다."
next_task_warnings:
  - "TASK-0004는 이번 초안 파일을 실제 대표 재실행/재시도 검증에 연결하되, 실행 흐름 스크립트 자체를 다시 확장하지 말고 실제 앱/H2 근거 확인에 집중해야 한다."
  - "collect-evidence.sh의 41과 42는 잠금 시그니처 기준으로 갈라지므로 TASK-0004 실제 검증에서도 잠금 오류와 일반 조회 실패를 같은 범주로 묶지 않아야 한다."
  - "보조 명령 가이드와 준비 데이터 가이드의 입력 파일 이름, 출력 파일 이름, 종료 흐름 설명은 이번 초안 파일과 계속 같은 용어로 유지해야 한다."
error_signature: "H2 명령줄 도구 SQL 오류 표준 출력 0 종료, H2 파일 DB 잠금 오판, 시작 PID 생존 확인 누락"
test_result_summary: "대상 테스트와 전체 테스트가 모두 통과했다. 실제 앱/H2 대표 검증은 이번 작업 비대상으로 생략했고, 이유는 실행 흐름 초안 파일 구현과 임시 H2 조회 결과 저장 검증만 TASK-0003 범위에 포함되기 때문이다."
snapshot_hashes: []
---

# 작업 회고

## 작업 요약
- `run-rerun.sh`, `run-retry.sh`, `collect-evidence.sh`를 추가해 재실행 요청, 재시도 요청, 파생 실행 키 추출, 앱 종료 후 H2 조회 결과 저장 흐름을 실제 파일로 옮겼다.
- `ManualRerunRunFlowScriptTest`를 추가해 재실행 20~24, 재시도 30~33, 실행 근거 수집 40~42 종료 코드를 각각 검증했다.
- 성공 경로에서는 출력 파일 생성과 임시 H2 파일 DB 조회 결과를 확인했고, 실패 경로에서는 후속 파일 미생성과 종료 코드를 함께 닫았다.

## 실패 요약
- 첫 구현에서는 `collect-evidence.sh` 실행 근거 수집 스크립트가 H2 명령줄 도구 쿼리 실패를 종료 코드만으로 판단해, 비어 있는 DB와 잠금 상황을 정확히 나누지 못했다.
- 첫 구현 테스트도 `missing-lock-db`와 이름만 맞는 H2 프로세스를 조합해 `42`를 기대하는 방식이라, 실제 잠금 시그니처를 확인하지 않고도 통과할 수 있었다.
- `run-rerun.sh`, `run-retry.sh`는 포트만 열리면 성공으로 판단해, 시작한 PID가 죽었는데 다른 서버가 같은 포트를 열고 있는 오판을 막지 못했다.

## Root Cause
- H2 명령줄 도구는 SQL 오류를 표준 출력으로 남기고 종료 코드 0을 반환할 수 있어, 명령줄 도구 특성을 모른 채 종료 코드만 보면 실패 분기가 틀어진다.
- 실행 흐름 스크립트는 포트 확인만으로 기동 성공을 보면 안 되고, 시작한 PID가 실제로 살아 있는지도 같이 봐야 한다.
- 테스트가 도구의 실제 실패 방식과 다른 시나리오를 정답으로 고정하면, 스크립트 구현도 그 오판을 그대로 따라가게 된다.

## AGENTS 체크 결과
- 연결 이슈 `#118`을 `TASK-0003`와 1:1로 유지했다.
- 세 서브에이전트는 문서 경계, 검증 근거, 운영자 가독성 관점으로 리뷰했고 최종 전원 통과 판정을 줬다.
- 대상 테스트와 전체 테스트를 순차 실행했고, 같은 작업 공간 산출물을 공유하는 병렬 테스트 실행은 만들지 않았다.
- 실제 앱/H2 대표 검증은 이번 작업 비대상으로 생략했고, 그 이유를 회고에 남겼다.

## 근거 Artifact
- 현재 활성 작업 문서
  - [tasks.md](/home/seaung13/workspace/agile-runner/.agents/active/tasks.md)
- 실행 흐름 초안 파일
  - [run-rerun.sh](/home/seaung13/workspace/agile-runner/scripts/manual-rerun-response/run-rerun.sh)
  - [run-retry.sh](/home/seaung13/workspace/agile-runner/scripts/manual-rerun-response/run-retry.sh)
  - [collect-evidence.sh](/home/seaung13/workspace/agile-runner/scripts/manual-rerun-response/collect-evidence.sh)
- 실행 흐름 검증 테스트
  - [ManualRerunRunFlowScriptTest.java](/home/seaung13/workspace/agile-runner/src/test/java/com/agilerunner/client/agentruntime/ManualRerunRunFlowScriptTest.java)
- 준비 데이터/근거 SQL 테스트
  - [ManualRerunSeedCommandScriptTest.java](/home/seaung13/workspace/agile-runner/src/test/java/com/agilerunner/client/agentruntime/ManualRerunSeedCommandScriptTest.java)
  - [ManualRerunResponseSeedSqlTest.java](/home/seaung13/workspace/agile-runner/src/test/java/com/agilerunner/client/agentruntime/ManualRerunResponseSeedSqlTest.java)
  - [ManualRerunResponseSeedEvidenceSqlTest.java](/home/seaung13/workspace/agile-runner/src/test/java/com/agilerunner/client/agentruntime/ManualRerunResponseSeedEvidenceSqlTest.java)
- 보조 명령 가이드
  - [manual-rerun-response-seed-command-guide.md](/home/seaung13/workspace/agile-runner/docs/manual-rerun-response-seed-command-guide.md)

## 다음 Task 경고사항
- `TASK-0004`는 이번 초안 파일을 실제 대표 재실행/재시도 검증에 연결하는 단계이므로, 준비 데이터 정리/적용과 실행 흐름 스크립트 수정까지 다시 섞으면 작업 경계가 깨진다.
- 재시도 파생 실행 키는 `run-retry.sh` 출력 파일에서 읽고, 실행 근거 수집은 앱 종료 후 `collect-evidence.sh`로만 이어가야 대표 검증 흐름이 흔들리지 않는다.
- H2 잠금 오류는 이제 42로 좁혀졌으므로, 실제 검증에서 일반 조회 실패와 잠금 오류를 구분해서 회고에 남겨야 한다.

## 제안 필요 여부
- 없음
- 새 제안 없음. 이번 교훈은 새 절차가 비어 있었다기보다, 이미 있는 종료 코드/잠금 확인 규칙을 스크립트와 테스트에 실제 도구 특성대로 옮겨야 한다는 점을 현재 작업 절차 안에서 다시 확인한 것이다.
