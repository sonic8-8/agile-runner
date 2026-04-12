---
spec_id: SPEC-0024
summary_status: completed
generated_at: 2026-04-12T23:44:42+09:00
task_ids:
  - TASK-0001
  - TASK-0002
  - TASK-0003
  - TASK-0004
---

# Spec 요약

## 범위 요약
- `SPEC-0024 운영용 조회 응답 준비 데이터 적용 절차 정리`는 준비 데이터 파일 규칙 위에 실제 적용 순서, 점검 순서, 체크리스트, 문서 마감 검토를 얹는 작업이었다.
- 이번 spec은 실제 SQL을 채우거나 대표 실제 앱/H2 검증을 다시 수행하는 단계가 아니라, 준비 데이터를 새 작업자도 반복 가능하게 적용할 수 있는 절차 문서를 닫는 데 집중했다.

## 완료된 Task
- `TASK-0001` 준비 데이터 적용 절차 안전망 확인
- `TASK-0002` 준비 데이터 적용 순서와 명령 기준 정리
- `TASK-0003` schema와 enum 값 점검 순서, 대표 검증 체크리스트 정리
- `TASK-0004` 준비 데이터 적용 절차 문서 마감과 정합성 확인

## 주요 결과
- [manual-rerun-response-seed-guide.md](/home/seaung13/workspace/agile-runner/docs/manual-rerun-response-seed-guide.md) 에 준비 데이터 적용 순서, 앱 기동 전/후 명령 경계, schema와 enum 점검 순서, 대표 검증 전 체크리스트를 한 문서로 정리했다.
- 안내 문서 안에 `시나리오별 파일 선택 기준`과 `준비 데이터 파일 첫 줄 주석 기준`을 추가해, 새 작업자가 문서와 준비 데이터 파일만 보고도 어떤 파일을 언제 쓰는지 따라갈 수 있게 했다.
- `retry-source-execution-seed.example.sql`, `rerun-acknowledge-action-history-seed.example.sql`, `rerun-runtime-evidence-check.example.sql`, `retry-runtime-evidence-check.example.sql` 머리말을 보강해 시나리오, 적용 시점, 다음 단계 또는 선행 단계를 직접 읽을 수 있게 했다.

## 검증 요약
- targeted test: 통과
- full `cleanTest test`: 통과
- 대표 실제 앱/H2 검증: 이번 spec 비대상
- linked issues `#92`, `#93`, `#94`, `#95`: 모두 `CLOSED`

## 이번 spec에서 얻은 점
- 준비 데이터 절차 문서는 적용 순서만 적는 것으로는 부족하고, 시나리오별 파일 조합과 파일 머리말 기준까지 함께 있어야 새 작업자가 추론 없이 따라갈 수 있다.
- 문서 마감 단계에서는 안내 문서 본문이 활성 spec/task 문서에 다시 기대지 않도록 정합성을 끝까지 점검해야 한다.
- 실제 SQL 보강 spec과 절차 문서 spec을 분리해 두면, 현재 단계의 책임을 더 명확하게 유지할 수 있다.

## 남은 위험
- 현재 example.sql은 여전히 절차와 연결 순서 중심의 예시 파일이므로, 실제 SQL 구문은 후속 `SPEC-0025`에서 채워야 한다.
- 후속 작업에서 실제 SQL을 넣을 때 schema 컬럼과 enum 값을 현재 문서 기준 없이 임의로 복사하면 다시 준비 오류성 false negative가 날 수 있다.

## 다음 spec 후보
- `SPEC-0025 운영용 조회 응답 준비 데이터 실제 SQL 보강`
