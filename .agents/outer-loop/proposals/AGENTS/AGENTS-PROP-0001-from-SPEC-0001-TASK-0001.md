---
proposal_id: AGENTS-PROP-0001
status: accepted
source_spec: SPEC-0001
source_tasks:
  - TASK-0001
source_retrospectives:
  - .agents/outer-loop/retrospectives/SPEC-0001/TASK-0001-preflight-write-guard.md
target_document: AGENTS.md
target_version:
decision_date: 2026-04-03
applied_commit:
applied_pr:
---

# AGENTS 수정 제안서

## 현재 문제
- task 시작 시점에 GitHub Issue가 실제로 현재 task 범위와 1:1인지 확인하는 절차가 약했다.
- `TASK-0001`에서는 Issue `#1`이 넓은 상태에서 시작됐다가 뒤늦게 축소됐다.
- preflight/no-side-effect 성격의 task에서 tester가 처음에는 `main comment` 미발생만 확인했고, `inline write` 미발생까지는 후속 보강이 필요했다.

## 수정 제안
- Orchestrator 시작 체크리스트에 아래 항목을 추가한다.
  - 기존 Issue를 재사용할 경우, 구현 전 Issue 제목과 본문이 현재 task 범위와 1:1로 맞는지 먼저 확인한다.
- Tester 시작 체크리스트에 아래 항목을 추가한다.
  - task 완료 조건이 "write 이전 준비 완료" 또는 "side effect 미발생"이면, black-box 테스트에서 모든 외부 write 경로가 미발생인지 함께 검증한다.

## 근거
- `TASK-0001` 회고에서 inline 준비 실패가 preflight failure가 아닌 skip으로 처리되면서 main comment가 먼저 쓰일 수 있었음이 확인됐다.
- tester baseline도 처음에는 본문 comment 미발생만 검증했고, 이후 `createReviewComment(...)` 미호출 검증을 추가하면서 실패 재현력이 올라갔다.
- Issue `#1`도 task 시작 후에야 `TASK-0001` 범위로 다시 축소됐다.

## 근거 요약
- error signature: `NeverWantedButInvoked: pullRequest.comment(...) was invoked when inline preparation failed.`
- test result summary: `GitHubCommentServiceTest`와 `GitHubWebhookControllerTest` targeted rerun green

## 예상 효과
- task 시작 전에 issue/task 1:1 관계를 더 일찍 고정할 수 있다.
- preflight/no-side-effect 버그에서 tester baseline이 더 빠르게 정확한 실패를 재현한다.
- 동일한 유형의 write-before-validation 버그가 다음 task에서 반복될 가능성을 줄인다.

## 승인 메모
- `AGENTS.md` 작업 시작 규칙에 issue 1:1 확인 규칙을 반영했다.
- `AGENTS.md` 베이스라인 하네스에 all-write-path 검증 규칙을 반영했다.
