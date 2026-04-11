---
spec_id: SPEC-0015
summary_status: completed
generated_at: 2026-04-11T19:56:00+09:00
task_ids:
  - TASK-0001
  - TASK-0002
  - TASK-0003
  - TASK-0004
proposal_ids: []
---

# Spec 요약

## 범위 요약
- `SPEC-0015 운영용 관리자 제어 액션 반복 정책 정교화`는 기존 관리자 액션 `ACKNOWLEDGE`, `UNACKNOWLEDGE`를 같은 execution에서 반복 적용할 수 있도록 저장 규칙을 정리하고, 현재 상태 요약과 전체 timeline 해석을 함께 고정하는 데 집중했다.
- 이번 spec에서 새 endpoint를 추가하지는 않았고, 기존 action/query/list/history 경계 안에서 반복 액션 의미를 정교화했다.

## 완료된 Task
- `TASK-0001` 관리자 액션 반복 정책 안전망 고정
- `TASK-0002` 관리자 액션 반복 정책과 audit 저장 규칙 정리
- `TASK-0003` 반복 액션 이후 query/list/history 상태 연결
- `TASK-0004` 반복 액션과 실행 근거 정합성 검증

## 주요 결과
- `MANUAL_RERUN_CONTROL_ACTION_AUDIT`에서 같은 execution의 같은 action 반복 저장이 가능해졌다.
- `ACKNOWLEDGE -> UNACKNOWLEDGE -> ACKNOWLEDGE` 흐름이 policy, service, repository/H2 mem 기준으로 허용된다.
- query/list는 계속 마지막 applied action 기준 현재 상태와 `availableActions`를 보여주고, history는 반복 액션 전체 timeline을 시간 순서대로 보여준다.
- representative actual app/H2 검증에서 action 응답, query/list, history, H2 audit row 세 건이 같은 execution 기준으로 일치했다.

## 검증 요약
- targeted test: 통과
- full `cleanTest test`: 통과
- representative actual app/H2 verification: 통과

## 이번 spec에서 얻은 점
- 반복 액션을 허용할 때는 저장 규칙만 풀면 끝나는 게 아니라, 현재 상태와 과거 이력의 읽기 의미를 함께 고정해야 운영자가 혼동하지 않는다.
- representative verification은 같은 `executionKey` 기준으로 action 응답, query/list, history, H2 audit row를 한 번에 대조하는 편이 가장 빠르게 의미 불일치를 잡아낸다.

## 남은 위험
- action history는 반복 저장까지는 허용하지만, 아직 timeline 필터링이나 운영 화면 편의 기능은 없다.
- 반복 액션을 더 늘리거나 관리 UI를 붙이면, 현재 상태와 history 역할 분리를 계속 유지해야 한다.

## 다음 spec 후보
- `SPEC-0016 운영용 관리자 제어 이력 필터 확장`
