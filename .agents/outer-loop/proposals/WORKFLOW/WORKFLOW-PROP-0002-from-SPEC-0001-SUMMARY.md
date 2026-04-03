---
proposal_id: WORKFLOW-PROP-0002
status: accepted
source_spec: SPEC-0001
source_tasks: []
source_retrospectives:
  - .agents/outer-loop/retrospectives/SPEC-0001/SPEC-0001-summary.md
target_document: AGENTS.md, .agents/skills/agile-runner-task-loop/SKILL.md, .agents/outer-loop/README.md
target_version:
decision_date: 2026-04-03
applied_commit: pending
applied_pr:
---

# Workflow 수정 제안서

## 현재 문제
- task retrospective는 꾸준히 작성됐지만, spec 종료 시점의 summary 작성과 `registry.json`의 `spec_summary_path` 갱신은 명시 규칙이 약했다.
- 그 결과 현재처럼 모든 task가 끝났는데도 spec summary가 비어 있는 상태가 생길 수 있었다.

## 수정 제안
- 현재 활성 spec의 마지막 task가 끝나면 다음 spec으로 넘어가기 전에 `SPEC-xxxx-summary.md`를 작성한다.
- spec summary 작성 직후 `registry.json`의 `latest.spec_summary_path`를 갱신한다.
- spec summary가 비어 있으면 다음 spec의 `Task`나 Issue를 열지 않는다.

## 근거
- `SPEC-0001`은 `TASK-0001`부터 `TASK-0004`, `TASK-WF-0001`까지 모두 닫혔지만 `registry.json`의 `spec_summary_path`는 `null` 상태였다.
- task 단위 회고만으로는 spec 수준에서 반복된 실패 패턴과 승인된 제안을 한 번에 읽기 어려웠다.

## 예상 효과
- spec 종료 시점의 의사결정 근거가 한 문서로 정리된다.
- 다음 spec 시작 전에 열린 위험과 우선순위를 더 명확하게 보고 판단할 수 있다.
- `registry.json`이 task latest뿐 아니라 spec closure까지 가리키게 된다.

## 승인 메모
- `AGENTS.md`, `SKILL.md`, `.agents/outer-loop/README.md`에 spec summary 작성 및 `spec_summary_path` 갱신 규칙을 반영한다.
