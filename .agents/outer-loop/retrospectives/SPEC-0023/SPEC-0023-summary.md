---
spec_id: SPEC-0023
summary_status: completed
generated_at: 2026-04-12T23:30:00+09:00
task_ids:
  - TASK-0001
  - TASK-0002
  - TASK-0003
  - TASK-0004
---

# Spec 요약

## 범위 요약
- `SPEC-0023 운영용 조회 응답 예시 준비 데이터 정리`는 대표 검증에 필요한 준비 데이터 규칙을 문서와 파일 구조로 고정하는 데 집중했다.
- 이번 spec은 실제 적용 순서나 actual app/H2 대표 검증 재실행이 아니라, 준비 데이터의 이름 규칙, 저장 위치, 파일 단위 기준, 갱신 기준, 대표 검증 결과와의 경계를 먼저 정리하는 작업이었다.

## 완료된 Task
- `TASK-0001` 준비 데이터 규칙 안전망 확인
- `TASK-0002` 준비 데이터 이름과 저장 위치 규칙 정리
- `TASK-0003` 준비 데이터 갱신 기준과 대표 검증 경계 정리
- `TASK-0004` 준비 데이터 규칙 문서 마감과 정합성 확인

## 주요 결과
- [manual-rerun-response-seed-guide.md](/home/seaung13/workspace/agile-runner/docs/manual-rerun-response-seed-guide.md) 를 추가해 준비 데이터 위치, 디렉토리 단위, 파일 이름 규칙, 파일 단위 기준을 한 문서로 읽을 수 있게 했다.
- `src/test/resources/manual-rerun-response-seed/` 아래에 `source-execution`, `control-action-history`, `runtime-evidence` 세 디렉토리와 예시 파일 뼈대를 만들었다.
- 준비 데이터를 손대기 전에 봐야 할 schema 컬럼과 enum 값, 새 파일 생성 기준과 기존 파일 갱신 기준, 준비 데이터 파일과 기준 파일, 대표 검증 결과를 섞지 않는 경계를 문서로 고정했다.

## 검증 요약
- targeted test: 통과
- full `cleanTest test`: 통과
- actual app/H2 대표 검증: 이번 spec 비대상
- linked issues `#88`, `#89`, `#90`, `#91`: 모두 `CLOSED`

## 이번 spec에서 얻은 점
- 준비 데이터 규칙은 이름과 위치만 적어서는 부족하고, 새 파일 생성 기준, 기존 파일 갱신 기준, 대표 검증 결과와의 경계까지 적어야 실제 유지가 가능하다.
- 실제 적용 절차를 같은 spec에서 같이 적기 시작하면 문서 범위가 빠르게 흐려진다.
- 준비 데이터 규칙 spec과 실제 적용 절차 spec을 분리해 두면, 새 작업자가 현재 문서의 역할을 더 빠르게 이해할 수 있다.

## 남은 위험
- 현재는 준비 데이터 규칙만 정리됐고, 실제로 local H2에 어떤 순서로 적용하고 무엇을 먼저 점검해야 하는지는 아직 후속 spec으로 남아 있다.
- example.sql 파일은 이름 규칙 뼈대이므로, 후속 작업에서 실제 SQL 구문을 넣을 때 schema와 enum 값 확인 없이 바로 복사하면 다시 거짓 실패가 날 수 있다.

## 다음 spec 후보
- `SPEC-0024 운영용 조회 응답 준비 데이터 적용 절차 정리`
