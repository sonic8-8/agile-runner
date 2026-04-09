---
spec_id: SPEC-0010
task_ids:
  - TASK-0001
  - TASK-0002
  - TASK-0003
  - TASK-0004
generated_from_retrospectives:
  - .agents/outer-loop/retrospectives/SPEC-0010/TASK-0001-retry-policy-safety-net.md
  - .agents/outer-loop/retrospectives/SPEC-0010/TASK-0002-retry-eligibility-policy.md
  - .agents/outer-loop/retrospectives/SPEC-0010/TASK-0003-retry-request-path.md
  - .agents/outer-loop/retrospectives/SPEC-0010/TASK-0004-retry-response-runtime-relation.md
summary_status: final
generated_at: 2026-04-09T16:55:00+09:00
---

# Spec 요약

## Spec 요약
- `SPEC-0010 재실행 재시도 정책 정교화`는 manual rerun 결과 조회 기반 위에 `POST /reviews/rerun/{executionKey}/retry` 최소 재시도 경로와 retry eligibility 정책을 얹고, 마지막에는 retry 응답과 runtime evidence의 source relation까지 같은 의미로 닫았다.
- `TASK-0002`에서 `MANUAL_RERUN + FAILED + RETRYABLE` 조합만 허용하는 policy를 먼저 고정했고, `TASK-0003`에서 `404`, `409`, 성공 응답 계약과 source context 재사용 경계를 controller/service 기준으로 연결했다.
- `TASK-0004`에서는 representative retryable source execution을 local H2에 준비한 뒤 실제 앱에서 retry 요청을 수행하고, 응답의 `retrySourceExecutionKey`와 새 `executionKey`가 H2 `WEBHOOK_EXECUTION`, `AGENT_EXECUTION_LOG`에 같은 의미로 남는지 확인했다.

## 반복된 실패 패턴
- 재시도 정책을 controller 계약까지 한 번에 열기보다, `TASK-0002`에서 policy 경계를 먼저 고정하고 `TASK-0003`에서 request path를 연결하는 식으로 단계를 나눌 때 검증이 훨씬 안정적이었다.
- representative actual app/H2 검증에서 synthetic source execution seed가 필요한데 local H2 file DB가 새 runtime schema를 아직 반영하지 않은 상태면, business/runtime failure처럼 보이는 preparation false negative가 먼저 드러났다.

## 승인된 제안
- [WORKFLOW-PROP-0011-from-SPEC-0010-TASK-0004.md](/home/seaung13/workspace/agile-runner/.agents/outer-loop/proposals/WORKFLOW/WORKFLOW-PROP-0011-from-SPEC-0010-TASK-0004.md)
  - synthetic source execution 또는 synthetic runtime evidence seed가 필요한 representative 검증이고, 같은 task에서 `agent-runtime` 물리 스키마를 바꾸면 seed 전에 현재 `schema.sql`을 local H2 file DB에 먼저 적용하는 절차를 우선 검토

## 열린 위험
- 현재 representative retry 검증은 GitHub 설정이 없는 local 환경에서 실패 경로를 이용해 relation을 확인한다. 실제 GitHub App 설정이 있는 환경에서는 success path retry representative 검증을 별도로 보강할 여지가 있다.
- retry 응답과 runtime evidence의 relation은 manual rerun 중심으로 닫혔지만, 다음 운영용 조회/제어 spec에서 source/derived execution 목록이 늘어나면 relation 읽기 규칙이 다시 흔들릴 수 있다.

## 다음 Spec 결정사항
- 다음 spec은 운영자 기준 조회와 제어를 더 확장하는 방향이 자연스럽다.
- 다만 representative verification이 synthetic seed를 필요로 하는지, runtime schema 변경이 같이 들어오는지부터 먼저 점검하고 task를 나누는 편이 안전하다.
