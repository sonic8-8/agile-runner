---
proposal_id: WORKFLOW-PROP-0009
status: accepted
source_spec: SPEC-0007
source_tasks:
  - TASK-0002
source_retrospectives:
  - .agents/outer-loop/retrospectives/SPEC-0007/TASK-0002-selective-execution-input-model.md
target_document: .agents/skills/agile-runner-task-loop/SKILL.md
target_version: current
decision_date: 2026-04-08
applied_commit:
applied_pr:
---

# Workflow 수정 제안

## 제안 요약
- 같은 workspace 산출물을 공유하는 테스트 명령은 `multi_tool_use.parallel`로 묶지 않고 항상 순차 실행한다.

## 변경 이유
- `TASK-0002` 종료 검증에서도 targeted test와 full test를 같은 workspace에서 동시에 실행해 XML 결과 파일 충돌이 다시 발생했다.
- 기존 규칙은 `순차 실행`을 말하지만, 도구 사용 수준에서 `parallel 래핑 금지`까지 직접 적지 않아 같은 실수가 반복됐다.

## 제안 규칙
- `Tester 2차`와 종료 검증에서 `targeted test`, `full test`, representative verification처럼 같은 workspace 산출물을 공유하는 명령은 `multi_tool_use.parallel`로 묶지 않는다.
- 병렬 실행이 필요하면 출력 경로를 분리하거나 별도 workspace를 먼저 확보한 경우에만 허용한다.

## 기대 효과
- 같은 workspace의 `build/test-results` XML 충돌을 사전에 차단할 수 있다.
- 기존 순차 실행 규칙을 실제 도구 선택 수준까지 더 직접적으로 해석할 수 있다.
