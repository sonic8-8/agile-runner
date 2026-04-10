---
spec_id: SPEC-0013
summary_status: completed
generated_at: 2026-04-10T22:56:00+09:00
source_tasks:
  - TASK-0001
  - TASK-0002
  - TASK-0003
  - TASK-0004
linked_issues:
  - 48
  - 49
  - 50
  - 51
proposal_paths: []
---

# Spec 요약

## 요약
- `ACKNOWLEDGE`만 있던 관리자 제어 액션을 `UNACKNOWLEDGE`까지 확장했다.
- controller/service 입력 경계, 정책, query/list availableActions 계산, audit state 해석을 모두 같은 상태 모델로 정리했다.
- representative actual app/H2 검증에서 `ACKNOWLEDGE -> UNACKNOWLEDGE` 응답과 query/list, audit evidence가 같은 executionKey 기준으로 정렬되는 것을 확인했다.

## 완료된 Task
- `TASK-0001` 관리자 액션 다변화 안전망 고정
- `TASK-0002` 추가 액션 입력과 응답 모델 확장
- `TASK-0003` 추가 액션 정책과 상태 반영 연결
- `TASK-0004` 액션 전환 응답과 실행 근거 정합성 검증

## 검증 근거
- targeted test: green
- full cleanTest test: green
- representative actual app/H2 verification: green
- representative execution:
  - `delivery_id=MANUAL_RERUN_DELIVERY:a547ffbe-6e9e-448f-9b4b-59eabb92337f`
  - `execution_key=EXECUTION:MANUAL_RERUN:a547ffbe-6e9e-448f-9b4b-59eabb92337f`

## 핵심 교훈
- 다중 관리자 액션에서는 `ACKNOWLEDGE 적용 여부` 같은 boolean보다 `마지막 applied action` 기준 상태 해석이 더 정확하다.
- query/list는 계속 `availableActions`만 보여주고, action detail과 audit history는 별도 맥락으로 다루는 현재 경계가 유지 가치가 있다.
- representative verification은 action 응답, query/list, H2 audit evidence를 같은 executionKey 기준으로 함께 대조해야 안전하다.

## 후속 후보
- 관리자 액션 이력 조회 확장
- action note와 적용 시각을 운영 조회에 읽기 좋게 노출하는 응답 정교화
