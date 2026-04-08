---
spec_id: SPEC-0006
task_ids:
  - TASK-0001
  - TASK-0002
  - TASK-0003
  - TASK-0004
generated_from_retrospectives:
  - .agents/outer-loop/retrospectives/SPEC-0006/TASK-0001-manual-rerun-safety-net.md
  - .agents/outer-loop/retrospectives/SPEC-0006/TASK-0002-manual-rerun-input-model.md
  - .agents/outer-loop/retrospectives/SPEC-0006/TASK-0003-manual-rerun-execution-connection.md
  - .agents/outer-loop/retrospectives/SPEC-0006/TASK-0004-runtime-manual-rerun-evidence.md
summary_status: final
generated_at: 2026-04-08T12:45:00+09:00
---

# Spec 요약

## Spec 요약
- `SPEC-0006 수동 재실행 기능 기반 마련`은 `POST /reviews/rerun` 진입점, 입력 모델, 기존 review/comment orchestration 재사용, manual rerun runtime evidence 구분값 적재까지 닫았다.
- 수동 재실행 응답은 `executionKey`, `executionControlMode`, `writePerformed`를 고정하고, 같은 `executionKey`로 H2 evidence를 조회할 수 있게 정리했다.
- webhook 경로는 기존 회귀 테스트를 유지한 채 manual rerun 경로만 분리해 추가했다.

## 반복된 실패 패턴
- response 계약과 runtime evidence 계약을 분리해서 생각하면 representative verification 단계에서 response executionKey와 H2 evidence의 연결이 빈다.
- local actual-app 검증은 외부 설정 누락과 H2 file lock 타이밍이 함께 섞여 false negative처럼 보일 수 있다.

## 승인된 제안
- 없음

## 열린 위험
- manual rerun은 현재 내부/관리자용 기반 진입점이라, 실패 이유를 응답에 얼마나 노출할지 아직 정책이 없다.
- actual-app 대표 검증은 여전히 local GitHub/OpenAI 설정 유무에 영향을 받는다. 다만 response executionKey와 H2 evidence 연결은 확보됐다.

## 다음 Spec 결정사항
- 다음 spec은 `SPEC-0007 선택 실행 기능`으로 이어가는 것이 자연스럽다.
- 시작 전에는 manual rerun 경로의 응답 모델에 실패 상태를 별도 표기할 필요가 있는지 먼저 검토하는 편이 좋다.
