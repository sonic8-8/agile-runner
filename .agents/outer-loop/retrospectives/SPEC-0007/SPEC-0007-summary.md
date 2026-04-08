---
spec_id: SPEC-0007
task_ids:
  - TASK-0001
  - TASK-0002
  - TASK-0003
  - TASK-0004
generated_from_retrospectives:
  - .agents/outer-loop/retrospectives/SPEC-0007/TASK-0001-selective-execution-safety-net.md
  - .agents/outer-loop/retrospectives/SPEC-0007/TASK-0002-selective-execution-input-model.md
  - .agents/outer-loop/retrospectives/SPEC-0007/TASK-0003-selective-review-comment-scope.md
  - .agents/outer-loop/retrospectives/SPEC-0007/TASK-0004-selection-scope-evidence.md
summary_status: final
generated_at: 2026-04-08T20:53:00+09:00
---

# Spec 요약

## Spec 요약
- `SPEC-0007 선택 실행 기능 기반 마련`은 manual rerun 경로에서 `selectedPaths` 입력 seam, 선택 경로 기준 리뷰/경로 기반 코멘트 제한, selection scope runtime evidence 적재까지 닫았다.
- webhook 입력 형식과 `/webhook/github` 성공 응답/조기 종료 계약은 바꾸지 않았고, 선택 실행 기능은 manual rerun 경로에만 열어 두었다.
- representative actual app/H2 검증에서는 `executionKey=EXECUTION:MANUAL_RERUN:f179162d-68b2-417d-ba8b-938d7f5315bf` 기준으로 `selection_applied=true`, `selected_paths_summary=src/Main.java|src/Test.java`를 확인했다.

## 반복된 실패 패턴
- 같은 workspace의 targeted test와 full test를 병렬 실행하려다 `build/test-results` XML 충돌이 재발했다.
- 이 실패는 기능 오류가 아니라, 이미 채택된 순차 실행 규칙을 도구 호출 수준에서 다시 어긴 사례였다.

## 승인된 제안
- `WORKFLOW-PROP-0009`
  - 같은 workspace 산출물을 공유하는 테스트 명령은 `multi_tool_use.parallel`로 묶지 않고 순차 실행한다.
  - 병렬이 필요하면 출력 경로나 workspace를 먼저 분리한다.

## 열린 위험
- 선택 실행 기능은 현재 manual rerun 경로에만 열려 있어, 이후 다른 진입점으로 확장할 때 selection scope 의미와 evidence 일관성을 다시 점검해야 한다.
- `selected_paths_summary` 문자열 포맷은 현재 `정렬 + 중복 제거 + | 연결` 규칙으로 고정됐으므로, 다음 spec에서 응답 모델을 손대더라도 이 evidence 의미를 바꾸지 않는 쪽을 우선 검토해야 한다.

## 다음 Spec 결정사항
- 다음 spec은 `SPEC-0008 재실행 응답 모델 정교화`로 이어가는 것이 자연스럽다.
- 우선 검토 범위는 manual rerun 응답의 실패 상태 표현, 내부/관리자용 응답 정책, runtime evidence와 응답 모델의 모순 방지다.
