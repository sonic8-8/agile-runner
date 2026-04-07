---
spec_id: SPEC-0005
task_ids:
  - TASK-0001
  - TASK-0002
  - TASK-0003
  - TASK-0004
generated_from_retrospectives:
  - .agents/outer-loop/retrospectives/SPEC-0005/TASK-0001-execution-control-safety-net.md
  - .agents/outer-loop/retrospectives/SPEC-0005/TASK-0002-execution-control-input-model.md
  - .agents/outer-loop/retrospectives/SPEC-0005/TASK-0003-dry-run-no-write-branch.md
  - .agents/outer-loop/retrospectives/SPEC-0005/TASK-0004-runtime-execution-control-evidence.md
summary_status: final
generated_at: 2026-04-07T21:50:00+09:00
---

# Spec 요약

## Spec 요약
- `SPEC-0005 실행 제어 기능 기반 마련`은 `NORMAL`, `DRY_RUN` 실행 제어 모드 seam을 서비스 경계에 열고, dry-run no-write 분기와 runtime evidence 적재까지 닫았다.
- 현재 webhook 입력은 기본적으로 `NORMAL`로 유지되고, 외부 응답 계약은 바꾸지 않은 채 service-level 결과 타입과 runtime persistence를 분리했다.
- representative `NORMAL` actual-app/H2 verification에서도 같은 `execution_key` 기준으로 execution control evidence를 확인할 수 있게 됐다.

## 반복된 실패 패턴
- 외부 webhook 응답 계약과 내부 service/result 타입 경계를 초기에 충분히 나누지 않으면 controller consumer path가 반쯤만 정리되는 문제가 반복됐다.
- representative 실제 앱 검증에서 실패 응답이 나와도, same `execution_key` evidence를 어떻게 해석할지 문서 의미가 애매하면 종료 판정이 흔들렸다.

## 승인된 제안
- 없음

## 열린 위험
- 현재는 외부 webhook 입력에 dry-run 제어값을 직접 넣지 않으므로, actual-app verification은 `NORMAL` representative 1건만 가능하다.
- `GITHUB_APP_CONFIGURATION_MISSING` 같은 환경 실패가 representative NORMAL 검증의 write path 도달을 막을 수 있으므로, 후속 spec에서 rerun 진입점을 열 때 verification 기준을 다시 다듬어야 한다.

## 다음 Spec 결정사항
- 다음 spec은 `SPEC-0006 수동 재실행 기능`으로 이어가는 것이 자연스럽다.
- 시작 전에는 `NORMAL`/`DRY_RUN` execution evidence가 이미 충분히 있는지 확인하고, rerun 입력 seam과 representative verification 범위를 먼저 고정해야 한다.
