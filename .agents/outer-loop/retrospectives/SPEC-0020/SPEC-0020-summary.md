---
spec_id: SPEC-0020
summary_status: completed
generated_at: 2026-04-12T02:40:55+09:00
task_ids:
  - TASK-0001
  - TASK-0002
  - TASK-0003
  - TASK-0004
---

# Spec 요약

## 범위 요약
- `SPEC-0020 운영용 조회 응답 문서 기준 정리`는 운영자가 manual rerun 관련 응답을 읽을 때 `시작 결과`, `현재 상태`, `과거 관리자 조치`, `방금 수행한 조치 결과`를 서로 섞지 않고 이해하도록 문서 기준을 정리하는 데 집중했다.
- 이번 spec은 새 endpoint나 응답 필드를 추가하지 않고, 이미 있는 rerun, retry, query, list, history, action 응답을 운영자 관점에서 다시 설명하고 representative actual app 검증으로 그 설명을 닫는 방식으로 진행했다.

## 완료된 Task
- `TASK-0001` 조회 응답 문서 기준 안전망 고정
- `TASK-0002` 응답 역할 구분과 문서 골격 도입
- `TASK-0003` 응답 예시와 필드 의미, 중복 요약 기준 정리
- `TASK-0004` 문서 기준과 실제 응답 정합성 검증

## 주요 결과
- `docs/manual-rerun-response-guide.md`가 rerun, retry, query, list, history, action 응답을 운영자 질문 단위로 구분해서 설명하게 됐다.
- 같은 execution이라도 `query`는 조치 전 현재 상태, `action/history/list`는 조치 후 현재 상태처럼 읽는 시점이 다를 수 있다는 기준을 문서에 직접 반영했다.
- representative rerun execution에서는 `rerun`, `query`, `action`, `history`, `list` 응답이 같은 execution을 서로 다른 시점으로 읽는다는 점을 실제 앱 응답으로 확인했다.
- representative retry execution에서는 `retry` 응답, derived execution query/list 응답, H2 `retry_source_execution_key`가 같은 source relation을 설명한다는 점을 실제 앱/H2로 확인했다.

## 검증 요약
- targeted test: 통과
- full `cleanTest test`: 통과
- representative actual app/H2 verification: 통과
- linked issues `#76`, `#77`, `#78`, `#79`: 모두 `CLOSED`

## 이번 spec에서 얻은 점
- 운영용 문서 spec도 마지막 task에서 actual app representative 검증을 붙이면 문서 drift를 줄일 수 있다.
- 같은 execution을 조치 전/후 시점으로 읽는 설명이 없으면 `availableActions`, `latestAction*`, `currentActionState`가 서로 충돌하는 것처럼 읽힐 수 있으므로, 시점 설명 자체가 문서 기준의 핵심이다.
- retry representative 검증은 `retry` 응답만 보지 말고 derived list row와 H2 relation까지 같이 보는 편이 문서 기준을 더 안정적으로 닫는다.

## 남은 위험
- 문서 예시는 현재 representative 응답 의미와 맞지만, 이후 응답 필드가 늘어나면 guide가 다시 drift될 수 있다.
- synthetic source execution seed가 필요한 representative 검증은 SQL seed 품질이 낮으면 제품 버그가 아닌 false negative가 먼저 드러날 수 있다.

## 다음 spec 후보
- `SPEC-0021 운영용 조회 응답 예시 자동 검증`
