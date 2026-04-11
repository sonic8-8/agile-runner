---
spec_id: SPEC-0016
summary_status: completed
generated_at: 2026-04-11T21:39:43+09:00
task_ids:
  - TASK-0001
  - TASK-0002
  - TASK-0003
  - TASK-0004
proposal_ids: []
---

# Spec 요약

## 범위 요약
- `SPEC-0016 운영용 관리자 제어 이력 필터 확장`은 기존 관리자 액션 history 조회에 `action`, `actionStatus` 필터를 추가하고, 필터가 실제 audit row subset 선택으로 이어지도록 정리하는 데 집중했다.
- 이번 spec에서 새 endpoint를 추가하지는 않았고, 기존 `GET /reviews/rerun/{executionKey}/actions/history` 경계 안에서 입력 모델과 selection 의미를 확장했다.

## 완료된 Task
- `TASK-0001` 관리자 액션 이력 필터 안전망 고정
- `TASK-0002` 관리자 액션 이력 필터 입력 모델과 진입점 도입
- `TASK-0003` 관리자 액션 이력 필터와 audit selection 연결
- `TASK-0004` 이력 필터와 실행 근거 정합성 검증

## 주요 결과
- history 조회는 이제 `action`, `actionStatus` 필터를 읽고, 필터가 비어 있으면 전체 timeline을 유지한다.
- service와 repository는 필터 값을 실제 audit row selection으로 연결한다.
- representative actual app/H2 검증에서 무필터 history, 필터 적용 history, H2 audit evidence가 같은 execution 기준으로 일치했다.

## 검증 요약
- targeted test: 통과
- full `cleanTest test`: 통과
- representative actual app/H2 verification: 통과

## 이번 spec에서 얻은 점
- history 필터 같은 read 확장은 입력 경계와 selection seam을 먼저 분리해 닫고, 마지막에 representative verification으로 실제 응답과 H2 subset 의미를 맞추는 구성이 가장 안정적이다.
- 무필터 전체 timeline과 필터 적용 subset 의미를 동시에 고정해야 운영자가 history 조회를 신뢰할 수 있다.

## 남은 위험
- 현재 history 필터는 `action`, `actionStatus`까지만 지원한다.
- 날짜 범위나 추가 필터가 들어오면 timeline과 subset 의미가 다시 흔들릴 수 있으므로 representative verification을 반복해야 한다.

## 다음 spec 후보
- `SPEC-0017 운영용 관리자 제어 이력 기간 필터 확장`
