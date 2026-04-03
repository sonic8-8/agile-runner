# 현재 활성 Spec

## 문서 목적
이 문서는 현재 활성 구현 범위와 후속 구현 범위를 고정하는 `Spec` 문서다.
`ValidationCriteria`와 `Task`는 이 문서의 활성 spec을 기준으로 작성하며, GitHub Issue는 각 `Task`를 외부에서 추적하기 위한 수단으로만 사용한다.

## 현재 활성 Spec
### ID
SPEC-0002

### 이름
agent-runtime 용어 및 스키마 정렬

### 목표
- PRD에서 정의한 `WebhookExecution`, `TaskRuntimeState`, `ValidationCriteria` 용어를 코드와 H2 스키마에 맞춘다.
- `agent-runtime` 도메인, 저장소 API, 컨트롤러/서비스 시그니처, 테스트 이름을 같은 용어로 정렬한다.
- 이름 정리 이후에도 `/webhook/github` 외부 응답 계약과 실제 runtime 적재 동작은 유지한다.

### 대상 문제
- 문서에서는 `WebhookExecution`, `TaskRuntimeState`, `ValidationCriteria`를 쓰지만 구현에는 `ReviewRun`, `TaskState`, `EvaluationCriteria`가 남아 있다.
- 코드와 문서의 용어가 다르다 보니 task 회고, H2 확인, 테스트 해석 시 매번 번역이 필요하다.
- H2 스키마도 `REVIEW_RUN`, `TASK_STATE`, `EVALUATION_CRITERIA`, `run_key` 기준이라 문서 개념과 직접 대응되지 않는다.

### 범위
- `ReviewRun` 계열 이름을 `WebhookExecution` 계열로 정렬한다.
- `TaskState` 계열 이름을 `TaskRuntimeState` 계열로 정렬한다.
- `EvaluationCriteria` 이름을 `ValidationCriteria`로 정렬한다.
- H2 스키마의 `TASK_STATE`, `EVALUATION_CRITERIA`, `REVIEW_RUN`, `run_key`를 새 용어로 정렬한다.
- 관련 controller, service, repository, test 이름과 시그니처를 새 용어로 일괄 정리한다.
- 이름 정리 이후에도 실제 앱 기동, H2 file DB 생성, representative runtime 적재까지 확인한다.

### 비대상
- `/webhook/github` 외부 응답 계약 변경
- `SPEC-0001`에서 고정한 bug fix 동작 변경
- `issue_number` 의미 재설계나 추적 모델 변경
- 예외 체계 도입과 `ErrorCode` 정리
- runtime 장기 저장소 도입

### 외부 계약
- `/webhook/github` 엔드포인트와 응답 계약은 유지한다.
- duplicate delivery 조기 종료, non-`pull_request` 조기 종료, successful comment 응답 형태는 그대로 둔다.
- 이름 정리는 내부 코드와 H2 스키마 정렬에 한정한다.

### 핵심 시나리오
1. 회귀 안전망 고정
   - 리팩터링 시작 전에 webhook 외부 계약 유지 테스트를 먼저 고정한다.
   - 이름 정리 중에도 duplicate delivery, non-`pull_request`, successful comment 응답 계약이 유지되도록 안전망을 만든다.
2. 도메인과 service 용어 정렬
   - `ReviewRun`과 관련 상태/메서드/변수를 `WebhookExecution` 기준으로 정렬한다.
   - `TaskState`, `EvaluationCriteria`도 같은 방식으로 정렬한다.
3. repository API와 H2 스키마 정렬
   - 저장/조회 API 이름을 새 용어로 맞춘다.
   - 테이블과 컬럼 이름을 새 용어로 맞춘다.
4. 동작 유지 검증
   - 이름 정리 이후에도 webhook 외부 계약이 유지되어야 한다.
   - local profile 실제 기동 후 H2 file DB에서 새 테이블과 컬럼 이름으로 runtime 적재가 확인되어야 한다.

### Task 분해 기준
- `TASK-0001` webhook 계약 안전망 고정
- `TASK-0002` 도메인과 service 용어 정렬
- `TASK-0003` repository API, H2 스키마 정렬과 runtime 검증

### 연결될 ValidationCriteria
- `webhook-contract-preserved-after-rename`
- `renamed-runtime-terms-consistent`
- `renamed-schema-query-roundtrip-preserved`
- `runtime-evidence-recorded-under-renamed-schema`

### 필수 테스트 시나리오
- 리팩터링 시작 전에 webhook 외부 계약 유지 테스트가 회귀 안전망으로 고정된다.
- rename 이후 `WebhookExecution`, `TaskRuntimeState`, `ValidationCriteria` 이름이 controller, service, repository, test에 일관되게 적용된다.
- rename 이후에도 `/webhook/github`의 성공 응답과 조기 종료 계약은 깨지지 않는다.
- local profile 실제 앱 기동 시 새 H2 테이블과 컬럼 이름으로 runtime row가 적재된다.

## 후속 Spec 후보
### ID
SPEC-0003

### 이름
예외 체계 정리

### 시작 조건
- `현재 활성 Spec`이 완료되고, 관련 회귀 테스트와 실제 앱/H2 검증 규칙이 안정적으로 적용된 뒤 시작한다.

### 목표
- `RuntimeException`, `IllegalStateException` 중심의 임시 예외 사용을 줄이고, 애플리케이션 경계에서 해석 가능한 예외 체계를 만든다.
- `AgileRunnerException`과 `ErrorCode`를 기준으로 설정 오류, payload 오류, 외부 연동 오류, 내부 처리 오류를 더 일관되게 분류한다.
- webhook, controller, service 경계에서 예외 변환, 로깅, runtime evidence 기록 기준을 명확히 한다.

### 후속 변경 범위
- `AgileRunnerException` 기반 애플리케이션 공통 예외 도입
- `ErrorCode` 정의와 코드별 의미, 로그 기준, 사용자 노출 정책 정리
- 설정 누락, payload 파싱, OpenAI 호출, GitHub 연동, comment posting, runtime 기록 실패의 분류 기준 정리
- 현재 `RuntimeException`, `IllegalStateException` 사용 지점을 새 예외 체계로 치환
- webhook, controller, service 경계별 예외 변환과 runtime evidence 기록 규칙 정리

### 후속 변경 비대상
- 공통 `ApiResponse` 래퍼 도입
- webhook 외부 응답 계약의 전면 개편
- 도메인 모델 전체를 business exception 중심으로 재구성하는 작업
- 에러 UI나 별도 운영 대시보드 도입

### 후속 검증 방향
- 주요 실패 경로가 `ErrorCode`로 일관되게 분류된다.
- 예외 변환 후에도 `/webhook/github` 외부 응답 계약이 불필요하게 깨지지 않는다.
- runtime evidence와 로그에서 실패 원인을 코드 단위로 빠르게 식별할 수 있다.
