# 현재 활성 Spec

## 문서 목적
이 문서는 현재 활성 구현 범위와 후속 구현 범위를 고정하는 `Spec` 문서다.
`ValidationCriteria`와 `Task`는 이 문서의 활성 spec을 기준으로 작성하며, GitHub Issue는 각 `Task`를 외부에서 추적하기 위한 수단으로만 사용한다.

## 현재 활성 Spec
### ID
SPEC-0003

### 이름
예외 체계 정리

### 목표
- `RuntimeException`, `IllegalStateException` 중심의 임시 예외 사용을 줄이고, 애플리케이션 경계에서 해석 가능한 예외 체계를 만든다.
- `AgileRunnerException`과 `ErrorCode`를 기준으로 설정 오류, 요청 정보 해석 오류, 외부 연동 오류, 내부 처리 오류를 더 일관되게 분류한다.
- 웹훅, 컨트롤러, 서비스 경계와 `agent-runtime` 실행 근거에서 실패 원인을 코드 단위로 식별할 수 있게 한다.

### 대상 문제
- 현재 설정 누락, 요청 정보 해석 실패, 리뷰 생성 실패, GitHub 코멘트 등록 실패가 `RuntimeException`, `IllegalStateException`으로 흩어져 있다.
- 같은 실패라도 어느 계층에서 어떤 의도로 던진 예외인지 코드만 보고 바로 구분하기 어렵다.
- `WebhookExecution`, `AgentExecutionLog`에는 현재 오류 메시지만 남고 있어, 실패 원인을 코드 단위로 추적하기 어렵다.

### 범위
- `AgileRunnerException` 기반 공통 예외 타입을 도입한다.
- `ErrorCode`를 정의하고, 설정 누락, 요청 정보 해석, OpenAI 리뷰 생성, GitHub 코멘트 등록, 패치와 위치 계산 실패를 같은 기준으로 분류한다.
- 현재 핵심 실패 경로에서 직접 던지는 `RuntimeException`, `IllegalStateException`을 새 예외 체계로 치환한다.
- `WebhookExecution`과 `AgentExecutionLog`에 오류 코드를 남길 수 있도록 모델, 저장소, H2 스키마를 정리한다.
- 예외 체계 정리 이후에도 `/webhook/github`의 성공 응답과 조기 종료 계약은 그대로 유지한다.
- 로컬 프로필 실제 기동 후 H2 파일 DB에서 오류 코드가 적재되는지 확인한다.

### 비대상
- 공통 `ApiResponse` 래퍼 도입
- 웹훅 외부 응답 계약의 전면 개편
- GitHub signature 검증이나 action 화이트리스트 강화
- 재시도 정책과 실패 보류 보관 전략 도입
- 운영 대시보드, 조회 API, 장기 저장소 도입

### 외부 계약
- `/webhook/github` 엔드포인트의 성공 응답 계약은 유지한다.
- 같은 delivery_id 조기 종료, `pull_request` 외 이벤트 조기 종료, 성공 코멘트 응답 형태는 그대로 둔다.
- 이번 단계에서는 공통 예외 응답 처리나 별도 에러 응답 스키마를 도입하지 않는다.

### 핵심 시나리오
1. 실패 경로 기대 동작 고정
   - 기존 웹훅 성공/조기 종료 계약은 그대로 유지한다.
   - 설정 누락, 요청 정보 해석 실패, 리뷰 생성 실패, GitHub 코멘트 준비/등록 실패가 어떤 예외 타입과 오류 코드로 분류돼야 하는지 먼저 테스트로 고정한다.
2. 공통 예외 체계 도입
   - `AgileRunnerException`과 `ErrorCode`를 도입한다.
   - 설정, 컨트롤러 요청 해석, 서비스 경계의 핵심 실패 경로를 새 예외 체계로 치환한다.
3. 실행 근거 정렬
   - `WebhookExecution`, `AgentExecutionLog`에 오류 코드를 남긴다.
   - 실패 시 H2 파일 DB와 실행 로그에서 오류 코드를 확인할 수 있게 한다.
4. 동작 유지 검증
   - 예외 체계 도입 이후에도 웹훅 성공 응답과 조기 종료 계약은 깨지지 않아야 한다.
   - 로컬 프로필 실제 기동 후 대표 실패 웹훅 처리 결과가 H2 파일 DB에 오류 코드와 함께 적재되어야 한다.

### Task 분해 기준
- `TASK-0001` 실패 경로 안전망 고정
- `TASK-0002` `AgileRunnerException`과 `ErrorCode` 도입
- `TASK-0003` 실행 근거 오류 코드 적재와 실제 검증

### 연결될 ValidationCriteria
- `webhook-contract-preserved-after-exception-refactor`
- `core-failure-paths-mapped-to-error-codes`
- `runtime-evidence-records-error-code`

### 필수 테스트 시나리오
- 예외 체계 정리 이후에도 `/webhook/github`의 성공 응답과 조기 종료 계약은 유지된다.
- 설정 누락, 요청 정보 해석 실패, OpenAI 리뷰 생성 실패, GitHub 코멘트 준비/등록 실패가 `AgileRunnerException + ErrorCode` 기준으로 일관되게 분류된다.
- 로컬 프로필 실제 앱 기동 시 대표 실패 실행의 `WebhookExecution`과 `AgentExecutionLog`에 오류 코드가 적재된다.

## 후속 Spec 후보
### ID
SPEC-0004

### 이름
실패 대응 강화

### 시작 조건
- `현재 활성 Spec`이 완료되고, 오류 코드 기반 예외 분류와 실행 근거 적재가 안정적으로 적용된 뒤 시작한다.

### 목표
- 실패한 `WebhookExecution`을 더 명확하게 분류하고, 재시도/보류 전략을 설계할 수 있는 기반을 만든다.
- 같은 실패 분류에 대해 반복되는 장애를 더 빠르게 식별하고 수동 개입 기준을 정한다.

### 후속 변경 범위
- 재시도 가능/불가능 실패 분류 정리
- 실패 보류 보관 또는 재처리 기준 검토
- 대표 실행 검증과 실제 운영 장애 해석 기준 보강

### 후속 변경 비대상
- 사용자 제어 기능 전면 도입
- 운영 대시보드 구축
- 장기 저장소 도입

### 후속 검증 방향
- 실패 유형별 후속 대응이 더 명확하게 분류된다.
- 같은 실패 유형의 반복 여부를 회고 문서와 실행 근거에서 빠르게 확인할 수 있다.
