---
proposal_id: WORKFLOW-PROP-0011
status: accepted
source_spec: SPEC-0010
source_tasks:
  - TASK-0004
source_retrospectives:
  - .agents/outer-loop/retrospectives/SPEC-0010/TASK-0004-retry-response-runtime-relation.md
target_document: AGENTS.md, .agents/skills/agile-runner-task-loop/SKILL.md
target_version:
decision_date: 2026-04-09
applied_commit:
applied_pr:
---

# Workflow 수정 제안서

## 현재 문제
- representative actual app/H2 검증이 synthetic source execution seed를 먼저 필요로 하는 task에서, local H2 file DB가 아직 새 runtime schema로 migration되지 않았다면 seed SQL이 false negative로 실패할 수 있다.
- 이번 `TASK-0004`에서도 `retry_source_execution_key` 컬럼을 추가한 뒤 source execution을 seed하려다가 local H2 file DB에 컬럼이 아직 없어 `Column "RETRY_SOURCE_EXECUTION_KEY" not found`가 먼저 발생했다.

## 수정 제안
- actual app/H2 representative verification이 아래 두 조건을 동시에 만족하면, seed 전에 현재 `schema.sql`을 local H2 file DB에 먼저 적용하는 절차를 기본으로 검토한다.
- representative 검증이 synthetic source execution 또는 synthetic runtime evidence seed를 요구한다.
- 현재 task가 `agent-runtime` 물리 스키마를 변경한다.
- 이 때문에 representative 검증 순서가 기본 순서와 달라지면 retrospective에 이유를 남긴다.

## 근거
- `.agents/outer-loop/retrospectives/SPEC-0010/TASK-0004-retry-response-runtime-relation.md`
- local H2 file DB에 `retry_source_execution_key` 컬럼이 없어서 첫 seed INSERT가 실패했고, schema.sql 선반영 후 같은 검증이 정상 진행됐다.

## 예상 효과
- runtime schema 변경 task의 representative 검증에서 schema mismatch로 인한 false negative를 줄일 수 있다.
- actual app/H2 representative verification 실패를 business/runtime failure와 preparation failure로 더 빨리 구분할 수 있다.

## 승인 메모
