---
spec_id: SPEC-0025
summary_status: completed
generated_at: 2026-04-13T02:08:30+09:00
task_ids:
  - TASK-0001
  - TASK-0002
  - TASK-0003
  - TASK-0004
---

# Spec 요약

## 범위 요약
- `SPEC-0025 운영용 조회 응답 준비 데이터 실제 SQL 보강`은 준비 데이터 guide와 example SQL을 실제 representative 절차에 쓸 수 있는 수준으로 끌어올리는 작업이었다.
- 이번 spec은 입력 seed SQL, 실행 근거 확인 SQL, representative actual app/H2 검증을 순서대로 닫아 준비 데이터가 문서 뼈대가 아니라 실제 검증 입력 자료로 동작하는지 확인하는 데 집중했다.

## 완료된 Task
- `TASK-0001` 준비 데이터 실제 SQL 안전망 확인
- `TASK-0002` 입력 준비 데이터 실제 SQL 보강
- `TASK-0003` 실행 근거 확인 SQL과 대표 검증 절차 연결
- `TASK-0004` 실제 준비 데이터 적용 검증과 문서 마감

## 주요 결과
- [retry-source-execution-seed.example.sql](/home/seaung13/workspace/agile-runner/src/test/resources/manual-rerun-response-seed/source-execution/retry-source-execution-seed.example.sql), [rerun-acknowledge-action-history-seed.example.sql](/home/seaung13/workspace/agile-runner/src/test/resources/manual-rerun-response-seed/control-action-history/rerun-acknowledge-action-history-seed.example.sql)에 현재 schema와 enum 값에 맞는 실제 INSERT를 채웠다.
- [rerun-runtime-evidence-check.example.sql](/home/seaung13/workspace/agile-runner/src/test/resources/manual-rerun-response-seed/runtime-evidence/rerun-runtime-evidence-check.example.sql), [retry-runtime-evidence-check.example.sql](/home/seaung13/workspace/agile-runner/src/test/resources/manual-rerun-response-seed/runtime-evidence/retry-runtime-evidence-check.example.sql)에 실제 SELECT를 넣고, H2 메모리 DB에서 실행 가능성을 닫았다.
- representative actual app/H2 검증에서 seeded rerun execution과 seeded retry source execution을 실제 local H2 file에 적용하고, 응답과 H2 evidence가 같은 실행 키 기준으로 연결되는지 확인했다.
- [manual-rerun-response-seed-guide.md](/home/seaung13/workspace/agile-runner/docs/manual-rerun-response-seed-guide.md)에 representative 검증에서 rerun은 seed 실행 키를 그대로 읽고 retry는 응답에서 받은 새 파생 실행 키를 따라간다는 기준을 남겼다.

## 검증 요약
- targeted test: 통과
- full `cleanTest test`: 통과
- actual app/H2 representative 검증: 통과
- linked issues `#96`, `#97`, `#98`, `#99`: 모두 `CLOSED`

## 이번 spec에서 얻은 점
- 준비 데이터 SQL을 실제 representative 절차에 쓰려면 INSERT/SELECT 자체만 맞는 것으로는 부족하고, 응답에서 받은 실행 키를 H2 evidence와 다시 연결하는 기준까지 문서에 있어야 한다.
- retry representative 검증은 source execution key와 derived execution key가 다르므로, 응답 키 추출 방법이 절차 일부라는 점을 분명히 해야 한다.
- seed SQL 보강 spec과 helper command 정리 spec을 나눠 두면, 준비 데이터 자체와 반복 실행 편의 개선을 섞지 않고 관리할 수 있다.

## 남은 위험
- representative 절차는 닫혔지만, 실행 키 추출과 H2 query를 더 빠르게 반복하는 보조 명령은 아직 문서화되지 않았다.
- jq 같은 외부 도구 의존이 있으면 환경별로 절차가 흔들릴 수 있으므로, 후속 spec에서 도구 전제를 낮춘 helper command 예시가 필요하다.

## 다음 spec 후보
- `SPEC-0026 운영용 조회 응답 준비 데이터 적용 보조 명령 정리`
