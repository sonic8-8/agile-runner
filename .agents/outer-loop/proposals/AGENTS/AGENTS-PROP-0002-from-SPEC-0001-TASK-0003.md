---
proposal_id: AGENTS-PROP-0002
status: accepted
source_spec: SPEC-0001
source_tasks:
  - TASK-0003
source_retrospectives:
  - .agents/outer-loop/retrospectives/SPEC-0001/TASK-0003-post-write-runtime-failure.md
target_document: AGENTS.md
target_version:
decision_date: 2026-04-03
applied_commit:
applied_pr:
---

# AGENTS 수정 제안서

## 현재 문제
- 외부 라이브러리 타입이 애매한 오버로드나 브리지 메서드를 가질 때 Mockito return stubbing이 focused rerun에서는 통과해도 full suite에서 취약할 수 있다.
- 이번 task에서도 `GHPullRequest/GHIssue.comment(String)` stubbing이 `CannotStubVoidMethodWithReturnValue`로 다시 드러났고, task 자체의 controller fix와 별개로 전체 테스트 종료를 막았다.
- 현재 `AGENTS.md` 테스트 규칙에는 이런 타입에서 fake object나 override 기반 test double을 우선 검토한다는 지침이 없다.

## 수정 제안
- `AGENTS.md` 테스트 섹션에 아래 규칙을 추가한다.
  - 외부 라이브러리 타입이 오버로드, 브리지, final 메서드 때문에 Mockito stubbing이 애매한 경우 fake object 또는 override 기반 test double을 우선 검토한다.
  - focused rerun만 통과하고 full suite에서 흔들리는 stubbing은 안정적인 test double로 치환한다.
- 특히 GitHub API처럼 SDK 객체가 풍부한 라이브러리는 “mock method return stubbing”보다 “가벼운 fake object + 호출 기록”을 우선 검토한다.

## 근거
- `TASK-0003`의 실제 bug fix는 controller 경계 수정으로 해결됐지만, 전체 테스트에서는 별도로 `GitHubCommentServiceTest`가 `CannotStubVoidMethodWithReturnValue`로 실패했다.
- 같은 테스트는 fake `GHPullRequest`로 바꾼 뒤 single rerun과 full suite에서 모두 안정적으로 green이 됐다.
- 이는 “focused rerun green이면 충분하다”가 아니라 “테스트 더블 방식 자체가 불안정할 수 있다”는 근거다.

## 예상 효과
- 외부 SDK 객체를 다루는 테스트의 flakiness를 줄일 수 있다.
- task 종료 직전에 unrelated test double 문제로 전체 테스트가 깨지는 일을 줄일 수 있다.
- black-box 테스트와 full suite 검증 사이의 신뢰도가 올라간다.

## 승인 메모
- `AGENTS.md` 테스트 규칙에 fake object / override 기반 test double 우선 검토 규칙을 반영한다.
- `.agents/skills/agile-runner-task-loop/SKILL.md`에도 outer loop 보고/질문 규칙과 함께 같은 테스트 더블 규칙을 반영한다.
