---
proposal_id: WORKFLOW-PROP-0013
status: accepted
source_spec: SPEC-0018
source_tasks:
  - TASK-0004
source_retrospectives:
  - .agents/outer-loop/retrospectives/SPEC-0018/TASK-0004-history-order-page-runtime-evidence.md
target_document: AGENTS.md, .agents/skills/agile-runner-task-loop/SKILL.md
target_version:
decision_date: 2026-04-12
applied_commit:
applied_pr:
---

# Workflow 수정 제안서

## 현재 문제
- representative actual app/H2 검증을 마친 뒤 같은 H2 file DB에 대한 Shell 조회를 병렬로 실행하면 file lock으로 false negative가 날 수 있다.
- 이번 `TASK-0004`에서도 `WEBHOOK_EXECUTION` 조회와 `MANUAL_RERUN_CONTROL_ACTION_AUDIT` 조회를 병렬로 시도했을 때 H2 lock 오류가 한 번 발생했다.

## 수정 제안
- local H2 file DB를 Shell 또는 SQL CLI로 확인하는 representative verification 후속 조회는 같은 파일 대상일 때 기본적으로 순차 실행한다고 workflow에 명시한다.
- H2 Shell 조회가 lock으로 실패하면 schema/runtime failure로 단정하기 전에 아래를 먼저 확인한다고 명시한다.
  - 앱 프로세스가 완전히 종료됐는지
  - 같은 H2 file에 대한 다른 Shell 조회가 동시에 열려 있지 않은지

## 근거
- `.agents/outer-loop/retrospectives/SPEC-0018/TASK-0004-history-order-page-runtime-evidence.md`
- 같은 execution에 대한 첫 H2 조회는 통과했지만, 두 번째 H2 Shell 조회를 병렬로 띄웠을 때 file lock 오류가 재현됐다. 앱 종료와 Shell 단일화 후에는 같은 evidence를 순차 조회로 정상 확인했다.

## 예상 효과
- representative actual app/H2 검증에서 H2 file lock 때문에 생기는 false negative를 줄일 수 있다.
- schema/runtime failure와 verification tooling failure를 더 빨리 구분할 수 있다.

## 승인 메모
