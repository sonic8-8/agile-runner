---
spec_id: SPEC-0019
summary_status: completed
generated_at: 2026-04-12T01:11:57+09:00
task_ids:
  - TASK-0001
  - TASK-0002
  - TASK-0003
  - TASK-0004
---

# Spec 요약

## 범위 요약
- `SPEC-0019 운영용 목록 조회와 이력 조회 조합 정리`는 운영자가 실행 목록과 관리자 액션 이력을 함께 볼 때, 현재 조치 상태 요약과 과거 timeline을 더 자연스럽게 이어 읽을 수 있도록 응답 조합 기준을 정리하는 데 집중했다.
- 이번 spec은 기존 endpoint를 유지한 채, list row에 최신 관리자 액션 요약을 추가하고 history 응답에 현재 조치 상태 요약을 더한 뒤 두 값이 같은 execution 최신 applied audit row 기준으로 계산되도록 맞췄다.

## 완료된 Task
- `TASK-0001` 목록 조회와 이력 조회 조합 안전망 고정
- `TASK-0002` 목록 row와 history 현재 조치 상태 요약 응답 모델 도입
- `TASK-0003` 최신 관리자 액션 요약과 timeline 연결
- `TASK-0004` 목록 요약과 이력 timeline 실행 근거 정합성 검증

## 주요 결과
- 목록 조회는 이제 `latestAction`, `latestActionStatus`, `latestActionAppliedAt`, `historyAvailable`를 함께 보여 준다.
- history 조회는 기존 `actions[]` timeline을 유지하면서 `currentActionState`로 현재 조치 상태 요약을 함께 보여 준다.
- list 최신 action 요약과 history 현재 조치 상태 요약은 현재 filter/page 결과가 아니라 execution 전체 최신 applied audit row를 기준으로 계산된다.
- representative actual app/H2 검증에서 list 최신 action 요약, history `currentActionState`, H2 audit latest row가 같은 execution 기준으로 일치함을 확인했다.

## 검증 요약
- targeted test: 통과
- full `cleanTest test`: 통과
- representative actual app/H2 verification: 통과

## 이번 spec에서 얻은 점
- list와 history를 함께 쓰는 조회 확장은 응답 모델을 먼저 열고, 최신 applied audit row 계산 seam을 나중에 연결한 뒤, 마지막에 representative verification으로 의미를 닫는 구성이 안정적이다.
- history `actions[]`는 timeline이고 `currentActionState`는 현재 상태 요약이라는 경계를 실제 app/H2 representative 검증까지 끌고 가야 운영자 관점에서 해석이 흔들리지 않는다.

## 남은 위험
- 현재 list/history는 같은 execution 기준 조합은 맞지만, 운영자가 응답 역할을 빠르게 이해하도록 돕는 문서나 예시가 부족하다.
- query, list, history, action 응답을 함께 설명하는 기준이 약하면 같은 데이터를 두 응답에서 왜 다르게 보여 주는지 다시 혼동할 수 있다.

## 다음 spec 후보
- `SPEC-0020 운영용 조회 응답 문서 기준 정리`
