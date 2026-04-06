---
spec_id: SPEC-0004
task_ids:
  - TASK-0001
  - TASK-0002
  - TASK-0003
generated_from_retrospectives:
  - .agents/outer-loop/retrospectives/SPEC-0004/TASK-0001-failure-response-safety-net.md
  - .agents/outer-loop/retrospectives/SPEC-0004/TASK-0002-failure-disposition-policy.md
  - .agents/outer-loop/retrospectives/SPEC-0004/TASK-0003-runtime-failure-disposition-evidence.md
summary_status: final
generated_at: 2026-04-06T21:46:00+09:00
---

# Spec 요약

## Spec 요약
- `SPEC-0004 실패 대응 강화`는 핵심 6개 `ErrorCode`에 대한 기대 대응 기준 고정, 분류 정책 도입, runtime evidence 적재와 representative failure 검증까지 닫았다.
- 결과적으로 같은 실패에 대해 `error_code`와 `failure_disposition`을 함께 남길 수 있게 되어, 회고와 운영 판단에서 사람 해석 비용을 줄였다.

## 반복된 실패 패턴
- 기대 동작을 순수 정책 테스트로만 좁히면 실제 소비 경계가 빠질 수 있었다.
- representative verification은 fresh `delivery_id`를 먼저 고정하지 않으면 기존 로컬 evidence와 충돌할 수 있다.
- local H2 file DB는 실행 중 Shell 조회가 바로 되지 않아, 검증 순서를 명확히 가져가야 했다.

## 승인된 제안
- 없음

## 열린 위험
- failure disposition은 아직 runtime evidence에만 남고, 실제 재시도 실행 정책까지는 연결되지 않았다.
- 외부 에러 응답이나 운영 조회 API가 없어, 현재는 문서와 H2 evidence를 직접 보는 방식에 머물러 있다.

## 다음 Spec 결정사항
- 다음 활성 spec은 `SPEC-0005 실행 제어 기능`으로 옮기기 전에, rerun/dry-run/선택 실행 중 어떤 제어 시나리오를 우선할지 먼저 좁혀야 한다.
- representative runtime verification이 필요한 task라면 fresh `delivery_id`, `execution_key`, H2 조회 순서를 task 문서에 먼저 고정하는 방식으로 이어간다.
