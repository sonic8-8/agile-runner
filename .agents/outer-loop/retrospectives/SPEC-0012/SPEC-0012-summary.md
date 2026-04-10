---
spec_id: SPEC-0012
task_ids:
  - TASK-0001
  - TASK-0002
  - TASK-0003
  - TASK-0004
generated_from_retrospectives:
  - .agents/outer-loop/retrospectives/SPEC-0012/TASK-0001-control-action-safety-net.md
  - .agents/outer-loop/retrospectives/SPEC-0012/TASK-0002-control-action-input-model.md
  - .agents/outer-loop/retrospectives/SPEC-0012/TASK-0003-control-action-audit-state.md
  - .agents/outer-loop/retrospectives/SPEC-0012/TASK-0004-control-action-runtime-evidence.md
summary_status: final
generated_at: 2026-04-10T22:10:00+09:00
---

# Spec 요약

## Spec 요약
- `SPEC-0012 운영용 관리자 제어 액션 확장`은 manual rerun execution에 대해 운영자가 `ACKNOWLEDGE`를 실행할 수 있는 최소 관리자 제어 액션 경로를 열고, query/list의 `availableActions`와 H2 audit evidence까지 같은 의미로 연결했다.
- `TASK-0002`에서 action 입력 모델과 controller/service 진입점을 열고, `TASK-0003`에서 ACKNOWLEDGE 정책, audit evidence 저장, query/list 반영을 연결했다.
- `TASK-0004`에서는 representative manual rerun execution 1건을 실제 앱에서 확인 완료 처리하고, action 응답, query/list 결과, H2 audit row가 같은 executionKey 기준으로 일치하는지 검증했다.

## 반복된 실패 패턴
- 관리자 제어 액션처럼 실제 상태는 저장되지만 query/list는 action detail을 숨기는 설계에서는, representative 검증이 action 응답과 query/list availableActions, audit row를 서로 다른 읽기 방식으로 함께 대조해야 했다.
- 저장 seam 연결 task와 representative actual app/H2 verification task를 분리하지 않으면, TASK-0003에서 실제 검증 범위까지 끌어와 task 경계가 흔들릴 수 있었다.

## 승인된 제안
- `WORKFLOW-PROP-0012`
  - 저장 seam 연결 task와 representative actual app/H2 검증 task를 spec이 명시적으로 분리한 경우, 앞 task는 seam 검증과 retrospective 경고사항을 근거로 닫고 representative 검증은 뒤 task에서 필수 수행하도록 규칙 보강

## 열린 위험
- 현재 관리자 제어 액션은 `ACKNOWLEDGE` 하나만 지원한다. 다음 spec에서 액션이 늘어나면 availableActions 계산 규칙과 representative verification 조합이 함께 복잡해질 수 있다.
- query/list는 action detail을 직접 노출하지 않으므로, 액션 종류가 늘어날수록 representative 검증에서 action 응답과 query/list 의미를 함께 해석하는 작업이 더 중요해진다.

## 다음 Spec 결정사항
- 다음 spec은 관리자 제어 액션 다변화가 자연스럽다.
- 시작 전에는 늘릴 액션 종류와 각 액션이 representative verification에서 어떤 availableActions 변화와 audit evidence를 남겨야 하는지 먼저 고정하는 편이 안전하다.
