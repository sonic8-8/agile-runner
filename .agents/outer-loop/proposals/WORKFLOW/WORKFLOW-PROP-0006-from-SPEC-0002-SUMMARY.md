---
proposal_id: WORKFLOW-PROP-0006
status: accepted
source_spec: SPEC-0002
source_tasks: []
source_retrospectives:
  - .agents/outer-loop/retrospectives/SPEC-0002/SPEC-0002-summary.md
target_document: AGENTS.md, .agents/skills/agile-runner-task-loop/SKILL.md
target_version:
decision_date: 2026-04-03T17:45:00+09:00
applied_commit:
applied_pr:
---

# Workflow 수정 제안서

## 현재 문제
- 이름 정리 spec에서 코드 용어 정리와 물리 스키마 변경, 실제 앱/H2 검증을 한 task에 모두 넣으면 원인 추적과 종료 판단이 어려워질 수 있다.
- 이번 `SPEC-0002`는 `TASK-0002`에서 코드 이름 정리를, `TASK-0003`에서 물리 스키마 정리와 실제 앱/H2 검증을 분리했기 때문에 검증 범위와 실패 원인을 더 명확하게 구분할 수 있었다.

## 수정 제안
- 코드 용어 정리와 물리 스키마 정리가 함께 필요한 spec은 두 단계를 나눌 필요가 있는지 먼저 검토한다.
- 먼저 코드 이름 정리와 외부 공개 시그니처 정합성을 닫고, 그다음 물리 스키마 이름 정리와 실제 앱/H2 검증을 수행하는 구성을 우선 검토한다.
- 두 단계 모두 같은 webhook 외부 계약 회귀 안전망을 공유하고, 물리 스키마 정리 단계에서만 대표 실행 검증을 요구하는 방식을 우선 검토한다.

## 근거
- `TASK-0002`에서는 코드 이름 정리만 수행해 webhook 계약과 새 용어 정합성을 먼저 닫을 수 있었다.
- `TASK-0003`에서는 schema.sql, repository SQL, row mapper, H2 조회 검증을 한 번에 맞추고 실제 앱/H2 검증까지 수행해 바뀐 스키마 기준 적재를 분리해서 검증했다.
- summary 기준으로 보면 이 분리 덕분에 "용어 불일치"와 "물리 스키마/검증 환경 문제"를 서로 다른 단계에서 해석할 수 있었다.

## 예상 효과
- 이름 정리 spec의 실패 원인과 종료 기준이 더 또렷해진다.
- 코드 이름 정리와 물리 스키마 정리가 섞여 생기는 리뷰 부담을 줄일 수 있다.
- 실제 앱/H2 검증이 필요한 범위를 마지막 단계로 좁혀 task 종료 판단이 쉬워진다.

## 승인 메모
- 승인 시 `AGENTS.md`와 `SKILL.md`에 이름 정리 spec의 기본 분해 원칙으로 반영한다.
