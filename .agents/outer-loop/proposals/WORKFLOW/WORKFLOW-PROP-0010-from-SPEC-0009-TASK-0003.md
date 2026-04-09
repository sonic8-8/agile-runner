---
proposal_id: WORKFLOW-PROP-0010
status: accepted
source_spec: SPEC-0009
source_tasks:
  - TASK-0003
source_retrospectives:
  - .agents/outer-loop/retrospectives/SPEC-0009/TASK-0003-rerun-query-response-mapping.md
target_document: AGENTS.md, .agents/skills/agile-runner-task-loop/SKILL.md
target_version:
decision_date: 2026-04-09
applied_commit:
applied_pr:
---

# Workflow 수정 제안서

## 현재 문제
- `agent-runtime`처럼 설정에 따라 비활성화될 수 있는 bean에 새 의존성을 추가했을 때, 로컬 targeted test만으로는 기본 컨텍스트 기동 조건이 깨지는 문제가 늦게 드러날 수 있다.
- 이번 `TASK-0003`에서도 `ManualRerunQueryService`가 `AgentRuntimeRepository` bean을 항상 존재한다고 가정해 full suite의 `AgileRunnerApplicationTests`에서만 `NoSuchBeanDefinitionException`이 드러났다.

## 수정 제안
- 조건부 bean 또는 프로필에 따라 비활성화될 수 있는 bean(`agent-runtime` 등)에 새 의존성을 추가하는 task는 구현 중에 아래를 함께 점검한다.
- 기본 컨텍스트가 떠야 하는 환경에서 해당 의존성이 비활성일 수 있는지 먼저 확인
- Tester 2차 또는 종료 검증에서 기본 컨텍스트 기동 근거를 함께 확인

## 근거
- `.agents/outer-loop/retrospectives/SPEC-0009/TASK-0003-rerun-query-response-mapping.md`
- full suite에서만 `NoSuchBeanDefinitionException: AgentRuntimeRepository`가 드러났고, optional 주입으로 해결됐다.

## 예상 효과
- 조건부 bean 의존으로 인한 `contextLoads` 실패를 더 이른 단계에서 발견할 수 있다.
- 로컬 중심 구현과 운영 기본 비활성 설정 사이의 충돌을 줄일 수 있다.

## 승인 메모
