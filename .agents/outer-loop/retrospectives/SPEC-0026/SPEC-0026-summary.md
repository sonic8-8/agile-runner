---
spec_id: SPEC-0026
summary_status: completed
generated_at: 2026-04-13T10:40:00+09:00
task_ids:
  - TASK-0001
  - TASK-0002
  - TASK-0003
  - TASK-0004
---

# Spec 요약

## 범위 요약
- `SPEC-0026 운영용 조회 응답 준비 데이터 적용 보조 명령 정리`는 representative 검증을 더 쉽게 반복하기 위한 helper command를 문서로 고정하는 작업이었다.
- 이번 spec은 seed apply/reset, retry 파생 실행 키 추출, rerun/retry H2 evidence query, representative 검증 재실행 순서를 단계별로 닫는 데 집중했다.

## 완료된 Task
- `TASK-0001` 보조 명령 시작 전 기존 근거 확인
- `TASK-0002` 준비 데이터 적용과 정리 보조 명령 정리
- `TASK-0003` 실행 키 추출과 H2 조회 보조 명령 정리
- `TASK-0004` 보조 명령 기반 대표 검증과 문서 마감

## 주요 결과
- [manual-rerun-response-seed-command-guide.md](/home/seaung13/workspace/agile-runner/docs/manual-rerun-response-seed-command-guide.md)에 seed reset/apply, retry 파생 실행 키 추출, rerun representative 요청, retry representative query, 앱 종료 후 H2 Shell query까지 한 흐름으로 정리했다.
- [manual-rerun-response-seed-guide.md](/home/seaung13/workspace/agile-runner/docs/manual-rerun-response-seed-guide.md)는 규칙과 파일 선택 기준 문서로 남기고, 실제 명령은 command guide에서 따라가도록 역할을 분리했다.
- representative actual app/H2 검증을 다시 수행해 rerun seeded execution과 retry derived execution이 응답과 H2 evidence에서 같은 실행 키 기준으로 연결되는지 확인했다.

## 검증 요약
- targeted test: 통과
- full `cleanTest test`: 통과
- actual app/H2 representative 검증: 통과
- linked issues `#100`, `#101`, `#102`, `#103`: 모두 `CLOSED`

## 이번 spec에서 얻은 점
- representative 절차를 실제로 반복 가능하게 만들려면 seed SQL, response guide, evidence SQL만으로는 부족하고, request/response/H2 query를 잇는 helper command 문서가 필요하다.
- retry는 source 실행 키와 파생 실행 키가 다르므로, 응답 직후 파생 실행 키를 저장해 다시 쓰는 규칙을 문서에서 명확히 보여 주는 것이 중요하다.
- seed guide와 command guide 역할을 나누면 규칙 문서와 실제 명령 문서를 섞지 않고 유지할 수 있다.

## 남은 위험
- helper command는 문서화됐지만 아직 반복 실행 스크립트로 자동화되지는 않았다.
- representative 검증을 더 자주 반복할수록 임시 파일 경로와 로컬 H2 정리 순서를 더 강하게 고정할 필요가 있다.

## 다음 spec 후보
- `SPEC-0027 운영용 조회 응답 준비 데이터 반복 검증 스크립트 검토`
