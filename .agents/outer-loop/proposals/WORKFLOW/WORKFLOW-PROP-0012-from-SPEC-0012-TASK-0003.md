---
proposal_id: WORKFLOW-PROP-0012
status: proposed
source_spec: SPEC-0012
source_tasks:
  - TASK-0003
source_retrospectives:
  - .agents/outer-loop/retrospectives/SPEC-0012/TASK-0003-control-action-audit-state.md
target_document: AGENTS.md, .agents/skills/agile-runner-task-loop/SKILL.md
target_version:
decision_date:
applied_commit:
applied_pr:
---

# Workflow 수정 제안서

## 현재 문제
- `agent-runtime` schema 또는 repository seam을 추가하는 task는 현재 workflow상 representative actual app/H2 검증 대상처럼 읽힐 수 있다.
- 하지만 일부 spec은 의도적으로 `정책/저장 seam 연결`과 `representative actual app/H2 정합성 검증`을 다음 task로 분리한다.
- 이 우선순위가 명시되지 않으면, TASK-0003와 TASK-0004 사이에서 검증 범위 해석이 흔들릴 수 있다.

## 수정 제안
- 현재 spec이 `정책/저장 seam 연결 task`와 `representative actual app/H2 검증 task`를 명시적으로 분리했다면, 앞선 task는 아래 근거로 닫을 수 있다고 workflow에 명시한다.
- controller/service/repository targeted test
- 전체 `cleanTest test`
- repository 또는 H2 mem 수준의 저장 seam 검증
- retrospective에 representative verification을 다음 task로 넘긴 이유와 경고사항 기록
- representative actual app/H2 검증은 뒤 task에서 여전히 필수로 수행한다.

## 근거
- `.agents/outer-loop/retrospectives/SPEC-0012/TASK-0003-control-action-audit-state.md`
- `SPEC-0012`는 `TASK-0003`에서 ACKNOWLEDGE 정책, audit evidence 저장, query/list availableActions 연결을 닫고, representative verification은 `TASK-0004`로 분리해 정의돼 있다.

## 예상 효과
- spec task boundary와 runtime-storage verification rule이 충돌할 때 우선순위를 더 일관되게 해석할 수 있다.
- schema/repository seam task를 representative verification task와 분리한 spec에서도 false scope expansion 없이 종료 기준을 고정할 수 있다.

## 승인 메모
