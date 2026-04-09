---
spec_id: SPEC-0008
task_ids:
  - TASK-0001
  - TASK-0002
  - TASK-0003
  - TASK-0004
generated_from_retrospectives:
  - .agents/outer-loop/retrospectives/SPEC-0008/TASK-0001-rerun-response-safety-net.md
  - .agents/outer-loop/retrospectives/SPEC-0008/TASK-0002-rerun-response-model-expansion.md
  - .agents/outer-loop/retrospectives/SPEC-0008/TASK-0003-rerun-failure-state-response.md
  - .agents/outer-loop/retrospectives/SPEC-0008/TASK-0004-rerun-response-runtime-evidence.md
summary_status: final
generated_at: 2026-04-09T03:08:00+09:00
---

# Spec 요약

## Spec 요약
- `SPEC-0008 재실행 응답 모델 정교화`는 manual rerun 응답에 `executionStatus`, `errorCode`, `failureDisposition`를 추가하고, rerun 실패 상태와 dry-run non-write를 응답에서 읽을 수 있게 정리했다.
- 마지막 task에서는 representative manual rerun 실패 시나리오 1건을 실제 앱으로 실행해, 응답의 `executionKey=EXECUTION:MANUAL_RERUN:b530a529-7ace-43b8-a995-ea8dcde2e405`와 H2 evidence가 같은 의미를 남는지 닫았다.
- `/webhook/github` 계약은 그대로 유지했고, rerun 응답 정교화는 내부/관리자용 `POST /reviews/rerun` 경로에만 적용했다.

## 반복된 실패 패턴
- `TASK-0002`에서 DTO 확장과 실패 의미 연결을 한 단계로 보려다 `TASK-0003` 범위를 일부 앞당겼다.
- 이후 task 경계를 다시 좁히고, `TASK-0003`에서 응답 의미 연결, `TASK-0004`에서 runtime evidence 정합성 검증으로 나눠 닫는 편이 더 안정적이었다.

## 승인된 제안
- 없음

## 열린 위험
- rerun 응답 필드가 더 늘어나면 응답 모델과 runtime evidence가 다시 어긋날 수 있으므로, 이후 spec에서도 execution key 기준 정합성 검증을 유지해야 한다.
- manual rerun representative 검증은 현재 GitHub App 설정 부재를 이용한 실패 시나리오로 닫았기 때문에, 실제 GitHub 설정이 있는 환경에서는 성공 시나리오 representative 검증을 별도로 보강할 여지가 있다.

## 다음 Spec 결정사항
- 다음 spec은 `manual rerun 결과 조회` 또는 `실패 후 재시도 정책 정교화`처럼 rerun 운영성 개선 쪽을 우선 검토하는 흐름이 자연스럽다.
- 다만 다음 spec 후보는 `PRD`와 현재 active spec 후보를 다시 맞춘 뒤 확정하는 편이 안전하다.
