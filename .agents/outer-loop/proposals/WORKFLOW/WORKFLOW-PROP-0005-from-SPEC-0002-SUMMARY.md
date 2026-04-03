---
proposal_id: WORKFLOW-PROP-0005
status: accepted
source_spec: SPEC-0002
source_tasks: []
source_retrospectives:
  - .agents/outer-loop/retrospectives/SPEC-0002/SPEC-0002-summary.md
target_document: AGENTS.md, .agents/skills/agile-runner-task-loop/SKILL.md
target_version:
decision_date: 2026-04-03T17:45:00+09:00
applied_commit: c56aeb2
applied_pr:
---

# Workflow 수정 제안서

## 현재 문제
- 새 spec을 시작할 때 이미 있는 회귀 안전망을 먼저 확인하지 않고 별도 안전망 task를 만들면, 실제 변경이 없는 재확인 작업이 생길 수 있다.
- 이번 `SPEC-0002`의 `TASK-0001`은 webhook 계약 안전망을 다시 만들려 했지만, `SPEC-0001`에서 이미 충분한 외부 동작 테스트가 고정돼 있어 사실상 실제 변경 없는 확인 작업으로 끝났다.

## 수정 제안
- 리팩터링 또는 이름 정리 중심 spec을 시작할 때는 첫 task를 만들기 전에 기존 회귀 안전망 목록 확인 절차를 우선 검토한다.
- 기존 컨트롤러/서비스 통합 테스트와 유지 계약 대응 관계가 이미 충분하면, 별도 안전망 task를 새로 만들기보다 현재 spec 또는 task 문서에 그 근거만 기록하는 방식을 우선 검토한다.
- 안전망 task를 만들 때는 "새 외부 동작 테스트 추가"와 "기존 안전망 재확인"을 구분해 task 목표를 명시한다.

## 근거
- `SPEC-0002`의 `TASK-0001` retrospective는 새 외부 동작 테스트를 추가하지 않고 기존 안전망을 공식 근거로 확인하는 task로 정리됐다.
- summary 기준으로 보면 이 단계는 유효한 확인 작업이긴 했지만, 실제로는 spec 시작 전 기존 안전망 목록 확인 절차가 있었다면 별도 task로 분리하지 않아도 됐다.

## 예상 효과
- 리팩터링 spec에서 실제 변경 없는 재확인 task를 줄일 수 있다.
- task 집합이 실제 변경 단위에 더 가깝게 유지된다.
- Tester가 기존 안전망과 새 안전망 필요 범위를 더 빨리 구분할 수 있다.

## 승인 메모
- 승인 시 `AGENTS.md`와 `SKILL.md`에 "리팩터링 spec 시작 전 기존 회귀 안전망 목록 확인" 규칙을 추가한다.
