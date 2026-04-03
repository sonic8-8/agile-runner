---
proposal_id: WORKFLOW-PROP-0003
status: accepted
source_spec: SPEC-0001
source_tasks: []
source_retrospectives:
  - .agents/outer-loop/retrospectives/SPEC-0001/SPEC-0001-summary.md
target_document: AGENTS.md, .agents/skills/agile-runner-task-loop/SKILL.md
target_version:
decision_date: 2026-04-03
applied_commit: pending
applied_pr:
---

# Workflow 수정 제안서

## 현재 문제
- task와 outer loop는 정리됐지만 commit/push 시점은 명시 규칙이 아니어서 실제 반영이 뒤로 밀렸다.
- 특히 개인 프로젝트에서는 PR 없이 direct push를 쓰더라도, 언제 커밋하고 언제 push할지 기준이 없으면 이력이 늦게 정리된다.

## 수정 제안
- 각 task는 retrospective와 proposal 처리까지 끝난 뒤 커밋한다.
- 현재 활성 spec이 끝나면 spec summary와 proposal 처리까지 마친 뒤 push 여부를 확인한다.
- 혼자 진행하는 개인 프로젝트는 PR 없이 direct push를 기본으로 하고, 큰 단위 변경이나 추가 검토가 필요할 때만 선택적으로 PR을 사용한다.

## 근거
- `SPEC-0001`에서는 issue와 outer loop는 진행됐지만 commit/push는 뒤늦게 정리됐다.
- task와 git history의 대응 관계를 뒤늦게 맞추면서 push 타이밍을 다시 확인하는 비용이 생겼다.

## 예상 효과
- task와 git history의 대응 관계가 더 또렷해진다.
- spec 종료 시점에 push 여부를 명시적으로 확인해 로컬/원격 이력 차이를 줄일 수 있다.
- 개인 프로젝트에도 과한 PR 절차를 강제하지 않으면서 push 타이밍은 놓치지 않게 된다.

## 승인 메모
- `AGENTS.md`와 `SKILL.md`에 task 종료 후 commit, spec 종료 후 push 여부 확인 규칙을 반영한다.
