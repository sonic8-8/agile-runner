---
spec_id: SPEC-0009
task_ids:
  - TASK-0001
  - TASK-0002
  - TASK-0003
  - TASK-0004
generated_from_retrospectives:
  - .agents/outer-loop/retrospectives/SPEC-0009/TASK-0001-rerun-query-safety-net.md
  - .agents/outer-loop/retrospectives/SPEC-0009/TASK-0002-rerun-query-input-model.md
  - .agents/outer-loop/retrospectives/SPEC-0009/TASK-0003-rerun-query-response-mapping.md
  - .agents/outer-loop/retrospectives/SPEC-0009/TASK-0004-rerun-query-runtime-evidence.md
summary_status: final
generated_at: 2026-04-09T10:10:00+09:00
---

# Spec 요약

## Spec 요약
- `SPEC-0009 재실행 결과 조회 기능 기반 마련`은 `GET /reviews/rerun/{executionKey}` 최소 조회 경로를 열고, manual rerun runtime evidence를 조회 응답 의미로 연결했다.
- `TASK-0002`에서 조회 진입점과 `404 Not Found + executionKey + message` 정책을 먼저 고정했고, `TASK-0003`에서 runtime evidence 값과 조회 응답 의미를 맞췄다.
- 마지막 task에서는 representative manual rerun 1건을 실제 앱으로 생성한 뒤, 같은 `executionKey=EXECUTION:MANUAL_RERUN:dab45700-e55a-4d15-8dfd-5921ae7786ca`로 query 응답과 H2 evidence 정합성을 닫았다.

## 반복된 실패 패턴
- `TASK-0002`와 `TASK-0003` 경계에서 응답 의미 매핑을 너무 빨리 끌어오려는 경향이 있었고, 3-agent 리뷰 후 다시 입력/진입점 단계와 의미 매핑 단계를 분리했다.
- `TASK-0003`에서는 targeted test만 볼 때는 보이지 않던 조건부 bean 의존 문제가 full suite에서 드러났다.

## 승인된 제안
- [WORKFLOW-PROP-0010-from-SPEC-0009-TASK-0003.md](/home/seaung13/workspace/agile-runner/.agents/outer-loop/proposals/WORKFLOW/WORKFLOW-PROP-0010-from-SPEC-0009-TASK-0003.md)
  - 조건부로 비활성화될 수 있는 bean에 새 의존성을 추가하는 task는 기본 컨텍스트 기동 근거도 함께 확인

## 열린 위험
- 현재 representative 검증은 GitHub 설정 부재를 이용한 실패 시나리오 중심이다. 실제 GitHub 설정이 있는 환경에서는 success path representative 검증을 따로 보강할 여지가 있다.
- rerun query 응답 필드가 더 늘어나면 rerun 응답, query 응답, H2 evidence 세 지점 정합성이 다시 흔들릴 수 있다.

## 다음 Spec 결정사항
- 다음 spec은 `재실행 결과를 기준으로 수동 재시도 요청 정책 정교화` 또는 `운영용 조회/관리자 제어 기능 확장`이 자연스럽다.
- 다만 시작 전에 [prd.md](/home/seaung13/workspace/agile-runner/.agents/prd.md) 기준 후보와 현재 운영 우선순위를 다시 맞추는 편이 안전하다.
