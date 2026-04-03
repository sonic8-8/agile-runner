---
spec_id: SPEC-0001
task_ids:
  - TASK-0001
  - TASK-0002
  - TASK-0003
  - TASK-0004
  - TASK-WF-0001
generated_from_retrospectives:
  - .agents/outer-loop/retrospectives/SPEC-0001/TASK-0001-preflight-write-guard.md
  - .agents/outer-loop/retrospectives/SPEC-0001/TASK-0002-comment-posting-sequence.md
  - .agents/outer-loop/retrospectives/SPEC-0001/TASK-0003-post-write-runtime-failure.md
  - .agents/outer-loop/retrospectives/SPEC-0001/TASK-0004-regression-suite.md
  - .agents/outer-loop/retrospectives/SPEC-0001/TASK-WF-0001-completion-verification-rule.md
summary_status: final
generated_at: 2026-04-03T15:29:04+09:00
---

## 요약
- `SPEC-0001 웹훅 리뷰 흐름 안정화`는 쓰기 전 사전 준비 보장, 본문/인라인 코멘트 순서 고정, 쓰기 이후 `agent-runtime` 기록 실패 비차단 처리, 회귀 테스트 보강까지 닫았다.
- task 단위 버그 수정뿐 아니라 실제 앱 기동, H2 file DB 생성, 대표 webhook 실행, 전체 테스트 검증까지 수행해 종료 근거를 남겼다.
- `AGENTS.md`와 task loop skill에는 issue 1:1 확인, all-write-path 검증, fake object 우선 test double, 종료 검증 강화 규칙이 반영됐다.

## 반복된 실패 패턴
- write 이전 준비 실패와 write 이후 부분 실패가 같은 경계에서 섞여 있었다.
- focused rerun만 통과하는 테스트 더블이 full suite에서는 취약했다.
- task 종료 시 targeted test만으로 충분하다고 보기 쉬웠고, 실제 앱/H2/runtime 검증 규칙은 뒤늦게 명시됐다.
- task retrospective는 남았지만 spec summary를 닫는 절차는 명시 규칙이 약했다.

## 승인된 제안
- `AGENTS-PROP-0001`
  - 기존 Issue 재사용 시 task와 1:1 범위인지 먼저 확인
  - write 이전 준비 완료 계열 task에서 모든 외부 write 경로 미발생 검증
- `WORKFLOW-PROP-0001`
  - `Tester 2차`의 전체 테스트 확인
  - webhook/controller orchestration/agent-runtime 관련 task 종료 전 실제 앱/H2/runtime 검증
- `AGENTS-PROP-0002`
  - 오버로드/브리지/final 메서드가 있는 외부 SDK 타입에서 fake object 또는 override 기반 test double 우선 검토

## 열린 위험
- `agent-runtime` 도메인과 스키마 이름은 아직 `ReviewRun`, `TaskState`, `EvaluationCriteria` 기준이라 문서 용어와 완전히 맞지 않는다.
- 공통 예외 계층과 `ErrorCode` 체계가 아직 없어 설정/외부 연동/runtime 실패를 일관되게 분류하지 못한다.
- malformed issue 생성 같은 보조 workflow 오류는 여전히 CLI 입력 실수에 영향을 받는다.

## 다음 Spec 결정사항
- 다음 spec 후보는 `agent-runtime 용어 및 스키마 정렬`과 `예외 체계 정리` 두 가지다.
- 우선순위는 `예외 체계 정리`보다 `agent-runtime 용어 및 스키마 정렬`을 먼저 두는 편이 자연스럽다.
  - 현재 summary에서 반복적으로 드러난 문제는 문서 용어와 코드/스키마 용어 불일치다.
  - 예외 체계 정리는 그 다음 spec에서 runtime evidence와 함께 묶어도 된다.
