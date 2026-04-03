---
proposal_id: WORKFLOW-PROP-0001
status: accepted
source_spec: SPEC-0001
source_tasks:
  - TASK-0002
source_retrospectives:
  - .agents/outer-loop/retrospectives/SPEC-0001/TASK-0002-comment-posting-sequence.md
target_document: AGENTS.md
target_version:
decision_date: 2026-04-03
applied_commit:
applied_pr:
---

# Workflow 수정 제안서

## 현재 문제
- 지금까지 task 진행에서는 task 관련 targeted test를 먼저 고정하고 통과시키는 흐름은 있었지만, 전체 테스트 실행과 실제 애플리케이션 실행 검증은 명시 규칙이 아니었다.
- 그 결과 `Tester`가 black-box 기준을 잘 잡더라도, 실제 로컬 프로필에서 애플리케이션이 뜨는지와 `agent-runtime` H2 파일 DB에 데이터가 남는지는 task 종료 전에 빠질 수 있었다.
- 이번 확인에서도 targeted test는 green이었지만, 별도 검증 전까지는 실제 앱 실행과 H2 적재 여부를 확인하지 않은 상태였다.

## 수정 제안
- `Tester 2차` 단계에 아래를 명시 규칙으로 추가한다.
  - task 관련 targeted test를 통과시킨 뒤, 가능하면 저장소 표준 전체 테스트 실행 명령까지 확인한다.
  - 전체 테스트를 생략하면 생략 사유를 retrospective에 남긴다.
- `Orchestrator 종료 판정 전` 아래 조건에 해당하는 task는 실제 애플리케이션 검증을 추가한다.
  - `/webhook/github` 흐름을 변경한 task
  - controller orchestration을 변경한 task
  - `agent-runtime` 저장 또는 runtime failure handling을 변경한 task
- 실제 애플리케이션 검증의 최소 기준은 아래로 둔다.
  - 로컬 프로필로 애플리케이션이 실제 기동된다.
  - H2 file DB가 생성된다.
  - representative webhook 요청 또는 동등한 실행으로 `agent-runtime` 데이터가 적재된다.
  - 불가하면 왜 불가한지와 남은 위험을 retrospective에 남긴다.

## 근거
- 이번 로컬 검증에서 `SPRING_PROFILES_ACTIVE=local`로 실제 애플리케이션을 기동했고, `/home/seaung13/.agile-runner/agent-runtime/agile-runner.mv.db` 파일 생성까지 확인했다.
- representative webhook 요청 `verify-h2-001`은 외부 설정 부재로 `500`을 반환했지만, H2 query 결과 아래 데이터 적재를 확인했다.
  - `TASK_STATE`: 1건
  - `EVALUATION_CRITERIA`: 3건
  - `REVIEW_RUN`: 1건
  - `AGENT_EXECUTION_LOG`: 2건
- 저장된 row에서는 `webhook-accepted` 성공과 `review-generated` 실패가 함께 남아, 실제 실패 경로에서도 runtime 적재가 동작함을 확인했다.

## 예상 효과
- task 종료 시점의 검증 신뢰도가 올라간다.
- `agent-runtime`이나 webhook orchestration 관련 회귀를 테스트 green만으로 놓치는 일을 줄인다.
- retrospective가 실제 실행 근거까지 포함하게 되어 개발 피드백 루프의 품질이 올라간다.

## 승인 메모
- `AGENTS.md`에 전체 테스트 및 실제 앱/H2 검증 규칙을 반영한다.
- `.agents/skills/agile-runner-task-loop/SKILL.md`에도 같은 종료 검증 규칙을 함께 동기화한다.
