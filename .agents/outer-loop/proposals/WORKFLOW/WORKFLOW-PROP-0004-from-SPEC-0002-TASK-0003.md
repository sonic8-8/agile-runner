---
proposal_id: WORKFLOW-PROP-0004
status: accepted
source_spec: SPEC-0002
source_tasks:
  - TASK-0003
source_retrospectives:
  - .agents/outer-loop/retrospectives/SPEC-0002/TASK-0003-schema-rename-runtime-verification.md
target_document: AGENTS.md / .agents/skills/agile-runner-task-loop/SKILL.md
target_version:
decision_date: 2026-04-03T17:18:00+09:00
applied_commit: c8ee7ac
applied_pr:
---

# workflow 수정 제안

## 제안 요약
- 실제 앱/H2 대표 검증 단계에서 사용하는 `delivery_id`는 task별 새 값으로 고정한다.

## 제안 배경
- `TASK-0003` 종료 검증 중 첫 대표 `delivery_id`인 `task-0003-verify-001`은 이전 local H2 row와 충돌해 unique delivery 오류를 만들었다.
- 이 오류는 스키마 이름 정리 실패가 아니라 검증 식별자 재사용 때문에 검증 실패로 오인된 사례였다.

## 제안 내용
- `/webhook/github` 대표 검증 시 `delivery_id`는 현재 task와 이전 검증 이력을 고려해 새 값으로 생성한다.
- retrospective에는 실제 사용한 `delivery_id`와 `execution_key`를 함께 기록한다.
- 대표 검증 실패 시 먼저 `delivery_id` 재사용 충돌 여부를 확인한 뒤 스키마 또는 runtime failure로 해석한다.

## 기대 효과
- local file DB 재사용 환경에서도 대표 검증이 검증 실패로 오인되는 사례를 줄일 수 있다.
- 스키마 또는 runtime failure와 `delivery_id` 충돌을 더 빠르게 구분할 수 있다.
