---
spec_id: SPEC-0014
summary_status: completed
generated_at: 2026-04-10T23:23:30+09:00
task_ids:
  - TASK-0001
  - TASK-0002
  - TASK-0003
  - TASK-0004
proposal_ids: []
---

# Spec 요약

## 범위 요약
- `SPEC-0014 운영용 관리자 제어 이력 조회 확장`은 manual rerun execution별 관리자 액션 이력을 읽는 전용 조회 경로를 열고, history 응답과 query/list/current state 역할을 분리하는 데 집중했다.
- 이번 spec에서 새로 열린 경로는 `GET /reviews/rerun/{executionKey}/actions/history`다.

## 완료된 Task
- `TASK-0001` 관리자 액션 이력 조회 안전망 고정
- `TASK-0002` 관리자 액션 이력 입력 모델과 진입점 도입
- `TASK-0003` 관리자 액션 이력 응답과 audit timeline 연결
- `TASK-0004` 관리자 액션 이력과 실행 근거 정합성 검증

## 주요 결과
- action history 응답은 `executionKey`, `actions[].action`, `actions[].actionStatus`, `actions[].note`, `actions[].appliedAt`를 반환한다.
- query/list는 계속 현재 상태와 `availableActions`만 반환하고, action detail은 history 응답에서만 읽는다.
- representative actual app/H2 검증에서 action 응답, history 응답, query/list 상태, H2 audit row가 같은 execution 기준으로 일치했다.

## 검증 요약
- targeted test: 통과
- full `cleanTest test`: 통과
- representative actual app/H2 verification: 통과

## 이번 spec에서 얻은 점
- 운영용 current state 조회와 action history 조회를 분리하면, UI나 대시보드가 붙더라도 “지금 가능한 액션”과 “이미 수행된 액션”을 서로 다른 읽기 경계로 유지할 수 있다.
- representative verification은 action 응답, history 응답, query/list, H2 audit row를 같은 execution 기준으로 한 번에 대조해야 의미 차이를 빨리 잡을 수 있다.

## 남은 위험
- 현재 `MANUAL_RERUN_CONTROL_ACTION_AUDIT`는 `(execution_key, action)` unique 제약을 가지므로 같은 action 반복 허용은 아직 다루지 않았다.
- 다음 spec에서 action 반복 허용 여부를 결정하면, audit 저장 규칙과 history timeline 의미를 함께 다시 설계해야 한다.

## 다음 spec 후보
- `SPEC-0015 운영용 관리자 제어 액션 반복 정책 정교화`
