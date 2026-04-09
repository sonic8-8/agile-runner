# 현재 활성 Spec

## 문서 목적
이 문서는 현재 활성 구현 범위와 후속 구현 범위를 고정하는 `Spec` 문서다.
`ValidationCriteria`와 `Task`는 이 문서의 활성 spec을 기준으로 작성하며, GitHub Issue는 각 `Task`를 외부에서 추적하기 위한 수단으로만 사용한다.

## 현재 활성 Spec
### ID
SPEC-0010

### 이름
재실행 재시도 정책 정교화

### 목표
- `failureDisposition`과 이전 실행 결과를 기준으로 수동 재시도 허용 여부를 명확히 고정한다.
- 내부/관리자용 재시도 요청 진입점에서 어떤 경우에 새 재실행을 시작하고, 어떤 경우에 거부하는지 일관된 정책을 만든다.
- 새 재시도 응답과 runtime evidence에 원본 execution과 새 execution의 관계를 함께 남겨 추적 가능하게 만든다.

### 대상 문제
- 현재는 `GET /reviews/rerun/{executionKey}`로 이전 실행의 `failureDisposition`을 읽을 수 있지만, 운영자가 그 값만으로 재시도 가능 여부를 바로 판단하거나 요청할 수는 없다.
- `RETRYABLE`, `NON_RETRYABLE`, `MANUAL_ACTION_REQUIRED`가 runtime evidence에는 남지만, 실제 수동 재시도 요청 정책으로 연결되지 않았다.
- 새 재시도가 만들어져도 어떤 이전 execution에서 파생된 것인지 응답과 runtime evidence에서 직접 읽을 수 없다.

### 범위
- 내부/관리자용 `POST /reviews/rerun/{executionKey}/retry` 최소 재시도 진입점을 도입한다.
- 재시도 허용 기준은 최소한 아래를 따른다.
  - 이전 execution이 `MANUAL_RERUN` 시작 유형이다.
  - 이전 execution 상태가 `FAILED`다.
  - 이전 execution의 `failureDisposition`이 `RETRYABLE`이다.
- 재시도 입력은 최소한 `installationId`, `executionControlMode`, `selectedPaths`를 받고, repository/pullRequest 컨텍스트는 source execution에서 읽어 재사용한다.
- `selectedPaths`가 비어 있으면 source execution의 선택 경로를 재사용하지 않고 전체 실행으로 해석한다. 선택 경로를 제한하려면 retry 입력에서 명시적으로 다시 전달한다.
- 존재하지 않는 `executionKey`는 `404 Not Found + executionKey + message`로 정리한다.
- 재시도가 허용되지 않는 source execution은 `409 Conflict + executionKey + failureDisposition + message` 기준으로 정리한다.
- 재시도 성공 응답은 최소한 `executionKey`, `retrySourceExecutionKey`, `executionControlMode`, `writePerformed`, `executionStatus`, `errorCode`, `failureDisposition`를 포함한다.
- 로컬 프로필 실제 앱/H2 기준으로 대표 retryable failure execution 1건을 source로 재시도 요청을 수행하고, 새 응답과 runtime evidence의 source 관계를 검증한다.

### 비대상
- 자동 재시도 스케줄러
- bulk retry
- 운영 대시보드 구축
- webhook 실행 재시도
- 장기 저장소 도입

### 외부 계약
- `/webhook/github` 계약은 유지한다.
- `POST /reviews/rerun`과 `GET /reviews/rerun/{executionKey}` 기존 계약은 유지한다.
- `POST /reviews/rerun/{executionKey}/retry`는 내부/관리자용 최소 재시도 진입점으로만 사용한다.
- 재시도 응답은 기존 rerun 응답과 모순되지 않아야 하고, source execution 및 새 runtime evidence와 같은 의미를 가져야 한다.

### 핵심 시나리오
1. 재시도 정책 안전망 고정
   - 현재 rerun query 응답, failure disposition, 기존 rerun 계약이 유지된다는 점을 먼저 테스트로 고정한다.
   - 재시도 기능 추가가 webhook 계약에 영향을 주지 않는다는 점도 같이 고정한다.
2. 재시도 가능 여부 정책 도입
   - source execution의 `executionStatus`와 `failureDisposition`을 기준으로 재시도 허용/거부 기준을 도입한다.
   - 이 단계는 policy 또는 service 경계의 판단까지 닫고, `409 Conflict` controller 계약은 다음 단계에서 고정한다.
3. 재시도 요청 경로 연결
   - `executionKey` 기반 retry request 경계와 controller/service 진입점을 연다.
   - source execution에서 repository/pullRequest 컨텍스트를 읽어 기존 manual rerun 흐름을 재사용한다.
   - `retrySourceExecutionKey`는 이 단계에서 응답 필드로 먼저 고정하고, runtime evidence 관계 적재는 마지막 단계에서 닫는다.
4. 재시도 응답과 실행 이력 관계 검증
   - representative retryable failure execution 1건을 source로 실제 앱에서 retry 요청을 수행한 뒤, 응답과 H2 evidence에 `retrySourceExecutionKey` 관계가 일치하는지 확인한다.

### Task 분해 기준
- `TASK-0001` 재시도 정책 안전망 고정
- `TASK-0002` 재시도 가능 여부 정책 도입
- `TASK-0003` 재시도 요청 경로 연결
- `TASK-0004` 재시도 응답과 실행 이력 관계 검증

### 연결될 ValidationCriteria
- `manual-rerun-retry-contract-preserved`
- `manual-rerun-retry-eligibility-defined`
- `manual-rerun-retry-request-reuses-source-context`
- `manual-rerun-retry-response-links-source-execution`

### 필수 테스트 시나리오
- 재시도 기능을 추가해도 `/webhook/github`, `POST /reviews/rerun`, `GET /reviews/rerun/{executionKey}` 기존 계약은 유지된다.
- source execution이 `FAILED + RETRYABLE`일 때만 retry 요청이 허용된다.
- 존재하지 않는 `executionKey`는 `404 Not Found + executionKey + message`로 읽을 수 있어야 한다.
- 재시도가 허용되지 않는 source execution은 `409 Conflict + executionKey + failureDisposition + message`로 읽을 수 있어야 한다.
- 허용된 재시도 요청은 source execution의 repository/pullRequest 컨텍스트를 재사용해 새 rerun execution을 시작하고, `selectedPaths`가 비어 있으면 전체 실행으로 해석한다.
- representative retry 요청 1건의 응답과 runtime evidence에서 `retrySourceExecutionKey`와 새 `executionKey`가 같은 의미로 남아 있다.
- representative 검증은 `retryable source execution 준비 -> 같은 executionKey로 retry 요청 -> HTTP 결과 확인 -> 앱 종료 -> H2 조회` 순서를 따른다.

## 후속 Spec 후보
### ID
SPEC-0011

### 이름
운영용 조회와 관리자 제어 기능 확장

### 시작 조건
- `현재 활성 Spec`이 완료되고, 수동 재시도 요청 정책과 source execution 관계가 안정적으로 닫힌 뒤 시작한다.

### 목표
- 운영자가 execution 이력과 재시도 결과를 더 폭넓게 조회하고 제어할 수 있는 관리 기능을 확장한다.

### 후속 변경 범위
- execution 목록 조회
- 필터/상태 기반 조회
- 관리자용 제어 액션 확장

### 후속 변경 비대상
- 사용자용 UI
- 장기 저장소 도입
- 자동 스케줄러 도입

### 후속 검증 방향
- 조회 API와 관리자 제어 기능이 기존 rerun/query/retry 계약과 충돌하지 않는다.
- 운영자가 source execution과 파생 execution 관계를 일관되게 읽을 수 있다.
