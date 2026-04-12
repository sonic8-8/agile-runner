---
spec_id: SPEC-0023
task_id: TASK-0002
github_issue_number: 89
criteria_keys:
  - manual-rerun-seed-naming-rules-defined
delivery_ids: []
execution_keys: []
test_evidence_ref:
  - "targeted: ./scripts/gradlew-java21.sh --no-daemon test --tests '*ManualRerunResponseGuideFixtureTest' --tests '*ManualRerunControllerTest' --tests '*ManualRerunServiceTest' --tests '*ManualRerunRetryServiceTest' --tests '*ManualRerunQueryServiceTest' --tests '*ManualRerunExecutionListServiceTest' --tests '*ManualRerunControlActionHistoryServiceTest' --tests '*ManualRerunControlActionServiceTest' --console=plain"
  - "full: ./scripts/gradlew-java21.sh --no-daemon cleanTest test --console=plain"
diff_ref: "git diff -- docs/manual-rerun-response-seed-guide.md src/test/resources/manual-rerun-response-seed .agents/outer-loop/retrospectives/SPEC-0023/TASK-0002-seed-naming-and-location-rules.md .agents/outer-loop/registry.json"
failure_summary: "구현 실패는 없었다. 중간에 response guide까지 함께 건드리면 TASK-0003 경계를 당겨온다는 리뷰가 있었고, 그 수정 후 범위를 다시 좁혔다."
root_cause: "준비 데이터 위치를 기존 response guide에 같이 설명하려다가, 이름 규칙과 저장 위치 기준 정리 task가 갱신 절차와 경계 설명 task로 번질 뻔했다. 준비 데이터 guide와 준비 데이터 파일 구조만 남기도록 범위를 다시 잘라야 했다."
agents_check_findings:
  - "문서 경계 리뷰는 response guide 수정이 남아 있으면 TASK-0003 범위를 당겨온다고 지적했고, 해당 수정을 제거한 뒤 PASS로 수렴했다."
  - "검증 근거 리뷰는 actual app/H2 비대상 판단은 타당하지만 테스트 외에 가이드 생성, 디렉토리 생성, 예시 파일 뼈대 존재 자체를 artifact 근거로 함께 남겨야 한다고 지적했고 반영했다."
  - "가독성 리뷰는 준비 데이터 가이드가 저장 위치, 디렉토리 단위, 파일 이름 규칙, 파일 단위 기준 순서로 읽혀 새 작업자가 바로 이해할 수 있다고 확인했다."
next_task_warnings:
  - "TASK-0003에서는 준비 데이터 적용 순서와 schema/enum 선확인 순서를 적지 말고, 준비 데이터를 언제 새로 만들고 언제 기존 파일을 갱신하는지와 대표 검증 결과를 언제 섞지 않는지만 다뤄야 한다."
  - "현재 example.sql 파일은 이름 규칙을 보여 주는 뼈대다. 실제 SQL 구문과 적용 순서는 후속 spec으로 미뤄야 한다."
error_signature:
test_result_summary: "targeted test와 full cleanTest test가 모두 통과했다. 이번 task는 이름 규칙, 저장 위치, 파일 단위 기준 정리 단계라 actual app/H2 대표 검증은 비대상으로 유지했다."
snapshot_hashes: []
---

# 작업 회고

## 작업 요약
- 운영용 조회 응답 대표 검증 준비 데이터를 위한 전용 가이드와 전용 디렉토리를 처음 만들었다.
- 준비 데이터 위치를 `src/test/resources/manual-rerun-response-seed/` 아래로 고정하고, `source-execution`, `control-action-history`, `runtime-evidence` 세 단위로 나눴다.
- 각 디렉토리에 파일 이름 규칙을 바로 읽을 수 있는 예시 SQL 뼈대를 추가해, 새 작업자가 파일 이름만 보고도 용도를 추론할 수 있게 했다.

## 실패 요약
- 기능이나 테스트 실패는 없었다.
- 다만 초안에서는 `manual-rerun-response-guide.md`까지 같이 수정해 준비 데이터 갱신 절차와 대표 검증 경계 설명을 일부 당겨오려 했고, 리뷰에서 `TASK-0003` 범위를 침범한다는 지적이 나왔다.
- response guide 수정을 제거하고, 이번 task는 seed guide와 seed 디렉토리만 남기도록 다시 좁혔다.

## Root Cause
- 준비 데이터 위치를 기존 응답 가이드와 함께 설명하면 사용자는 편하지만, 이번 task 범위는 이름 규칙과 저장 위치, 파일 단위 기준까지만 닫는 단계였다.
- 따라서 위치 참조를 기존 guide에 더 붙이는 대신, 전용 seed guide와 seed 디렉토리만으로 기준을 읽게 만드는 쪽이 task 경계에 더 맞았다.

## AGENTS 체크 결과
- linked issue `#89`를 `TASK-0002`와 1:1로 유지했다.
- targeted test와 full `cleanTest test`를 순차 실행했다.
- 이번 task는 문서와 파일 구조 정리 단계이므로 actual app/H2 대표 검증은 비대상으로 두는 판단이 타당하다는 PASS를 받았다.
- 산출물 근거는 테스트뿐 아니라 seed guide 생성, seed 디렉토리 생성, 예시 파일 뼈대 생성 자체를 함께 남겼다.

## 근거 Artifact
- 문서
  - [manual-rerun-response-seed-guide.md](/home/seaung13/workspace/agile-runner/docs/manual-rerun-response-seed-guide.md)
- 준비 데이터 디렉토리
  - [retry-source-execution-seed.example.sql](/home/seaung13/workspace/agile-runner/src/test/resources/manual-rerun-response-seed/source-execution/retry-source-execution-seed.example.sql)
  - [rerun-acknowledge-action-history-seed.example.sql](/home/seaung13/workspace/agile-runner/src/test/resources/manual-rerun-response-seed/control-action-history/rerun-acknowledge-action-history-seed.example.sql)
  - [rerun-runtime-evidence-check.example.sql](/home/seaung13/workspace/agile-runner/src/test/resources/manual-rerun-response-seed/runtime-evidence/rerun-runtime-evidence-check.example.sql)
  - [retry-runtime-evidence-check.example.sql](/home/seaung13/workspace/agile-runner/src/test/resources/manual-rerun-response-seed/runtime-evidence/retry-runtime-evidence-check.example.sql)
- 검증 명령
  - targeted: `./scripts/gradlew-java21.sh --no-daemon test --tests '*ManualRerunResponseGuideFixtureTest' --tests '*ManualRerunControllerTest' --tests '*ManualRerunServiceTest' --tests '*ManualRerunRetryServiceTest' --tests '*ManualRerunQueryServiceTest' --tests '*ManualRerunExecutionListServiceTest' --tests '*ManualRerunControlActionHistoryServiceTest' --tests '*ManualRerunControlActionServiceTest' --console=plain`
  - full: `./scripts/gradlew-java21.sh --no-daemon cleanTest test --console=plain`

## 다음 Task 경고사항
- 다음 task는 준비 데이터 갱신 기준과 대표 검증 경계를 다루는 단계다. 실제 적용 순서와 actual app/H2 재검증은 여전히 후속 spec 범위다.
- 현재 seed guide가 설명하는 예시 파일은 이름 규칙 뼈대일 뿐이므로, 실제 SQL 내용과 schema/enum 선확인 기준은 `TASK-0003` 또는 후속 spec 전까지 섞지 말아야 한다.

## 제안 필요 여부
- 없음
- 이번 교훈은 새 workflow 규칙 부족이 아니라, response guide와 seed guide의 역할을 같은 task에서 섞지 말아야 한다는 현재 spec 경계 적용 문제에 가까웠다.
