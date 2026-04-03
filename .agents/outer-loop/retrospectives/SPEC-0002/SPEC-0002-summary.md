---
spec_id: SPEC-0002
task_ids:
  - TASK-0001
  - TASK-0002
  - TASK-0003
generated_from_retrospectives:
  - .agents/outer-loop/retrospectives/SPEC-0002/TASK-0001-webhook-contract-guard.md
  - .agents/outer-loop/retrospectives/SPEC-0002/TASK-0002-runtime-terminology-alignment.md
  - .agents/outer-loop/retrospectives/SPEC-0002/TASK-0003-schema-rename-runtime-verification.md
summary_status: final
generated_at: 2026-04-03T17:29:30+09:00
---

## 요약
- `SPEC-0002 agent-runtime 용어 및 스키마 정렬`은 웹훅 외부 계약을 유지한 채 코드 용어와 H2 물리 스키마 이름을 `WebhookExecution`, `TaskRuntimeState`, `ValidationCriteria`, `execution_key` 기준으로 맞췄다.
- 기존 안전망 재확인, 코드 이름 정리, 물리 스키마 이름 정리와 실제 앱/H2 검증을 단계적으로 분리해 변경 범위를 통제했다.
- 대상 테스트, 전체 `./gradlew test`, 로컬 프로필 실제 기동, H2 파일 DB 조회까지 수행해 바뀐 스키마 기준 실행 근거를 확인했다.
- `WORKFLOW-PROP-0004`를 채택해 실제 앱/H2 대표 검증에서는 새 `delivery_id`를 사용하고, 회고 문서에 실제 `delivery_id`와 `execution_key`를 남기는 규칙을 반영했다.

## 반복된 실패 패턴
- 새 spec을 시작할 때 이미 있는 회귀 안전망을 다시 task로 만들면 추가 변경 없는 재확인 단계가 생기기 쉽다.
- 코드 이름 정리만으로는 충분하지 않고, 보조 메서드 이름, key prefix, repository SQL, 스키마 조회문까지 같이 맞춰야 용어 불일치가 남지 않는다.
- 로컬 파일 DB를 유지한 채 대표 검증을 반복하면 `delivery_id` 재사용 충돌이 스키마 문제처럼 보이는 검증 실패로 섞일 수 있다.

## 승인된 제안
- `WORKFLOW-PROP-0004`
  - 실제 앱/H2 대표 검증에서 이전 검증과 겹치지 않는 새 `delivery_id` 사용
  - 대표 검증 실패 시 `delivery_id` 재사용 충돌 여부 선확인
  - 회고 문서에 실제 `delivery_id`, `execution_key` 기록

## 열린 위험
- `SPEC-0003`로 미뤄둔 공통 예외 체계와 `ErrorCode` 정리가 아직 없어 설정 오류, 외부 연동 오류, 내부 처리 오류를 일관된 코드로 분류하지 못한다.
- 실제 앱/H2 대표 검증은 현재 GitHub App 설정 유무에 따라 리뷰 생성 단계에서 500을 반환할 수 있어, 환경 의존 실패와 로직 실패를 계속 분리해서 봐야 한다.
- 물리 스키마 이름 정리는 끝났지만 장기 저장소나 조회 API가 없는 상태라 실행 근거 분석은 여전히 H2 shell 도구와 회고 문서에 의존한다.

## 다음 Spec 결정사항
- 다음 spec 후보는 `SPEC-0003 예외 체계 정리`다.
- 시작 전에는 `ValidationCriteria`와 `Task`를 다시 고정하고, 웹훅 외부 계약을 깨지 않는 예외 분류 범위부터 좁혀야 한다.
- `AgileRunnerException + ErrorCode`는 설정 오류, payload 오류, GitHub/OpenAI 연동 오류, runtime 기록 오류를 우선 대상으로 잡는 편이 자연스럽다.
