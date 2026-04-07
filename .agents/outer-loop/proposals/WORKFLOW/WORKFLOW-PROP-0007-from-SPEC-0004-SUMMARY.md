---
proposal_id: WORKFLOW-PROP-0007
status: accepted
source_spec: SPEC-0004
source_tasks: []
source_retrospectives:
  - .agents/outer-loop/retrospectives/SPEC-0004/TASK-0003-runtime-failure-disposition-evidence.md
  - .agents/outer-loop/retrospectives/SPEC-0004/SPEC-0004-summary.md
target_document: .agents/skills/agile-runner-task-loop/SKILL.md
target_version:
decision_date: 2026-04-07T09:21:04+09:00
applied_commit: 62a049e
applied_pr:
---

# Workflow 수정 제안서

## 현재 문제
- representative runtime verification에서 local H2 file DB를 Shell로 바로 조회하려 하면, 애플리케이션이 파일 잠금을 잡고 있어 false failure처럼 보일 수 있다.
- 지금 규칙은 fresh `delivery_id`, `execution_key`, representative verification 자체는 고정하고 있지만, `앱 종료 후 H2 조회` 순서는 명시하지 않는다.
- 그 결과 검증 절차를 처음 따르는 작업자는 schema/runtime failure와 H2 file lock을 혼동할 수 있다.

## 수정 제안
- local H2 file DB를 Shell로 조회하는 representative runtime verification은 기본 순서를 아래로 고정한다.
  - 앱 기동
  - fresh `delivery_id` 요청 실행
  - representative runtime 실행 확인
  - 앱 종료
  - H2 Shell 조회
- 애플리케이션 실행 중 Shell 조회 실패는 schema/runtime failure로 바로 단정하지 않고, H2 file lock 가능성을 먼저 확인한다.
- 다른 순서를 사용할 경우 retrospective에 이유를 남긴다.

## 근거
- `TASK-0003`에서 representative failure 자체는 성공적으로 적재됐지만, 애플리케이션이 H2 file DB를 사용 중일 때는 외부 Shell 조회가 바로 되지 않았다.
- 앱 종료 후 같은 `execution_key` 기준으로 `WEBHOOK_EXECUTION`과 `AGENT_EXECUTION_LOG`를 읽어 `error_code`와 `failure_disposition`을 정상 확인했다.
- `SPEC-0004` summary에서도 이 패턴이 반복된 검증 주의사항으로 남았다.
- 대표 근거:
  - `delivery_id=task-0003-verify-20260406-2144-001`
  - `execution_key=EXECUTION:task-0003-verify-20260406-2144-001`

## 예상 효과
- representative runtime verification에서 H2 file lock으로 인한 false failure를 줄일 수 있다.
- schema/runtime failure와 환경 잠금 문제를 더 빨리 구분할 수 있다.
- 실제 앱/H2 검증 절차가 작업자와 에이전트 모두에게 더 일관되게 적용된다.

## 승인 메모
- 승인 시 `SKILL.md`를 workflow 기준으로 먼저 반영하고, `AGENTS.md`도 같은 내용으로 동기화한다.
