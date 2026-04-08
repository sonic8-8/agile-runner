---
proposal_id: WORKFLOW-PROP-0008
status: accepted
source_spec: SPEC-0006
source_tasks:
  - TASK-0002
source_retrospectives:
  - .agents/outer-loop/retrospectives/SPEC-0006/TASK-0002-manual-rerun-input-model.md
target_document: .agents/skills/agile-runner-task-loop/SKILL.md
target_version: current
decision_date: 2026-04-08
applied_commit:
applied_pr:
---

# Workflow 수정 제안서

## 현재 문제
- task 종료 검증에서 targeted test와 full test를 같은 workspace에서 동시에 실행하면 `build/test-results` XML 출력 파일이 충돌할 수 있다.
- 이 경우 코드나 테스트가 정상이어도 false negative가 발생해 outer loop 판단을 흐릴 수 있다.

## 수정 제안
- task 종료 검증의 테스트 단계에서는 targeted test와 full test를 기본적으로 순차 실행한다.
- 병렬 실행이 필요하면 서로 다른 결과 출력 경로를 분리하거나, 같은 workspace 산출물을 공유하지 않는 방식만 허용한다.
- 승인 시 `AGENTS.md`에도 같은 규칙을 동기화한다.

## 근거
- `TASK-0002` 검증 중 `ManualRerunControllerTest`와 전체 `test`를 동시에 실행했을 때 `Could not write XML test results ...` 오류가 발생했다.
- 같은 명령을 `cleanTest test`로 단독 재실행했을 때는 정상 통과했다.

## 예상 효과
- task 종료 검증의 false negative 감소
- retrospective에서 테스트 실패 원인 분리 용이
- outer loop proposal 판단의 신뢰도 향상

## 승인 메모
- 2026-04-08 채택
- `AGENTS.md`와 `.agents/skills/agile-runner-task-loop/SKILL.md`에 테스트 순차 실행 규칙 반영 예정
