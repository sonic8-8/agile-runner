---
spec_id: SPEC-0011
task_ids:
  - TASK-0001
  - TASK-0002
  - TASK-0003
  - TASK-0004
generated_from_retrospectives:
  - .agents/outer-loop/retrospectives/SPEC-0011/TASK-0001-list-query-safety-net.md
  - .agents/outer-loop/retrospectives/SPEC-0011/TASK-0002-list-query-input-model.md
  - .agents/outer-loop/retrospectives/SPEC-0011/TASK-0003-list-response-control-state.md
  - .agents/outer-loop/retrospectives/SPEC-0011/TASK-0004-list-runtime-evidence-alignment.md
summary_status: final
generated_at: 2026-04-09T18:05:00+09:00
---

# Spec 요약

## Spec 요약
- `SPEC-0011 운영용 조회와 관리자 제어 기능 확장`은 manual rerun execution 단건 조회 흐름 위에 운영용 목록 조회를 추가하고, 각 row에서 현재 상태와 `RETRY` 가능 여부를 읽을 수 있도록 정리했다.
- `TASK-0002`에서 `GET /reviews/rerun/executions` 최소 진입점과 필터 입력 모델을 열고, `TASK-0003`에서 runtime evidence를 목록 응답 row 의미로 매핑해 `availableActions(RETRY)`까지 연결했다.
- `TASK-0004`에서는 retryable row와 non-retryable row를 representative execution으로 준비한 뒤 실제 앱/H2 검증을 수행해 목록 응답의 필터 결과, 상태 값, 제어 가능 상태가 runtime evidence와 같은지 닫았다.

## 반복된 실패 패턴
- 목록 조회처럼 응답 의미가 많은 spec은 입력 모델과 응답 의미 매핑을 한 task에 몰기보다, 입력 경계와 의미 매핑을 분리할 때 검증이 훨씬 안정적이었다.
- representative actual app/H2 검증에서 deterministic 상태 조합이 필요하면 synthetic execution seed가 필요하고, 이 준비 단계가 없으면 availableActions나 필터 결과를 충분히 대조하기 어렵다.

## 승인된 제안
- 없음

## 열린 위험
- 현재 목록 조회 representative 검증은 synthetic execution row로 `RETRY` 가능 여부를 대조한다. 실제 운영 환경에서 더 복잡한 관리자 액션 조합이 늘어나면 representative row 조합도 함께 늘어날 가능성이 있다.
- 목록 조회 응답은 `RETRY`만 지원한다. 다음 spec에서 관리자 액션이 늘어나면 availableActions 계산 규칙과 representative 검증 조합을 다시 설계해야 한다.

## 다음 Spec 결정사항
- 다음 spec은 운영용 관리자 제어 액션 확장 방향이 자연스럽다.
- 다만 representative verification이 필요한 액션 조합부터 먼저 고정하고, synthetic execution seed가 필요한지 여부를 task 분해 전에 먼저 점검하는 편이 안전하다.
