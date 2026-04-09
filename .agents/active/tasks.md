# 현재 활성 Task 집합

## 문서 목적
이 문서는 현재 활성 `Spec`을 실제 구현 단위로 분해한 `Task` 문서다.
각 `Task`는 하나의 명확한 결과, 검증 기준, GitHub Issue 연결 규칙을 가져야 한다.

## 현재 활성 Spec
- ID: `SPEC-0010`
- 이름: `재실행 재시도 정책 정교화`
- 기준 문서:
  - `.agents/active/spec.md`
  - `.agents/criteria/SPEC-0010-manual-rerun-retry-policy.json`

## 공통 규칙
- 구현 순서는 `TASK-0001 -> TASK-0002 -> TASK-0003 -> TASK-0004`로 고정한다.
- 각 `Task` 시작 전 해당 task용 GitHub Issue를 새로 연결한다.
- 각 `Task`는 연결된 `ValidationCriteria`와 테스트 근거가 없으면 완료로 보지 않는다.
- `POST /reviews/rerun`, `GET /reviews/rerun/{executionKey}`, `/webhook/github` 기존 계약은 유지한다.
- 재시도 기능은 내부/관리자용 최소 진입점으로만 열고, 자동 재시도나 운영 대시보드는 다루지 않는다.
- source execution이 `MANUAL_RERUN + FAILED + RETRYABLE`인 경우에만 재시도를 허용하는 방향을 기본 정책으로 검토한다.
- retry 입력의 `selectedPaths`가 비어 있으면 source execution의 선택 경로를 재사용하지 않고 전체 실행으로 해석한다.
- `TASK-0001` 시작 전 기존 rerun query, failure disposition, runtime evidence 안전망이 재시도 기능 추가에도 충분한지 먼저 검토한다.

## 요약 표
| Task | 이름 | 핵심 목표 | 연결 ValidationCriteria | 핵심 검증 | Issue |
| --- | --- | --- | --- | --- | --- |
| `TASK-0001` | 재시도 정책 안전망 고정 | 기존 rerun query와 failure disposition 의미를 재시도 기능 추가 전 먼저 고정 | `manual-rerun-retry-contract-preserved` | rerun/query/webhook 회귀 테스트 | 새 Issue |
| `TASK-0002` | 재시도 가능 여부 정책 도입 | source execution 상태와 failure disposition 기반 허용/거부 정책 도입 | `manual-rerun-retry-eligibility-defined` | policy/service 테스트 | 새 Issue |
| `TASK-0003` | 재시도 요청 경로 연결 | `executionKey` 기반 retry endpoint와 source context 재사용 흐름 연결 | `manual-rerun-retry-request-reuses-source-context` | controller/service black-box 테스트 | 새 Issue |
| `TASK-0004` | 재시도 응답과 실행 이력 관계 검증 | representative retry 요청 응답과 H2 evidence의 source/new execution 관계 일치 확인 | `manual-rerun-retry-response-links-source-execution` | 실제 앱/H2 representative 검증 | 새 Issue |

## TASK-0001
### 이름
재시도 정책 안전망 고정

### 목표
- 재시도 기능을 열기 전에 현재 rerun query 응답, failure disposition 의미, webhook 계약이 유지된다는 안전망을 먼저 고정한다.

### 구현 범위
- 기존 `ManualRerunControllerTest`, `ManualRerunQueryServiceTest`, `ManualRerunServiceTest`, `GitHubWebhookControllerTest`, `FailureDispositionPolicyTest`를 재사용하거나 필요한 범위만 보강한다.
- 아래 기준을 우선 고정한다.
  - `POST /reviews/rerun` 기존 계약 유지
  - `GET /reviews/rerun/{executionKey}` 기존 계약 유지
  - `failureDisposition`의 `RETRYABLE`, `NON_RETRYABLE`, `MANUAL_ACTION_REQUIRED` 의미 유지
  - `/webhook/github` 계약 비영향
- 기존 안전망이 충분하면 그 근거를 문서와 회고에 남기고, 부족한 경우만 새 테스트를 추가한다.

### 비대상
- retry endpoint 추가
- retry 응답 DTO 추가
- runtime evidence source relation 추가

### 연결 ValidationCriteria
- `manual-rerun-retry-contract-preserved`

### 완료 조건
- 재시도 기능 추가 전에도 rerun query 응답과 failure disposition 의미가 회귀 테스트로 고정된다.
- webhook 계약 비영향도 테스트와 체크리스트로 확인된다.

### 검증
- rerun/query/webhook 회귀 테스트 실행 통과
- failure disposition 정책 테스트 확인

### GitHub Issue
- 새 Issue 생성
- 권장 제목: `[BE] 재시도 정책 안전망 고정`

## TASK-0002
### 이름
재시도 가능 여부 정책 도입

### 목표
- source execution의 상태와 `failureDisposition`을 기준으로 재시도 허용/거부 정책을 명확히 도입한다.

### 구현 범위
- retry eligibility 정책 클래스를 도입하거나 기존 policy 경계에 붙인다.
- 최소 허용/거부 기준을 고정한다.
  - `MANUAL_RERUN + FAILED + RETRYABLE` => 허용
  - `FAILED + MANUAL_ACTION_REQUIRED` => 거부
  - `FAILED + NON_RETRYABLE` => 거부
  - `SUCCEEDED` => 거부
- 허용되지 않는 source execution의 policy/service 판단 기준을 정리한다.

### 관련 파일 후보
- `src/main/java/com/agilerunner/domain/exception/FailureDispositionPolicy.java`
- `src/main/java/com/agilerunner/api/service/review/ManualRerunQueryService.java`
- `src/main/java/com/agilerunner/api/controller/review/ManualRerunController.java`
- `src/test/java/com/agilerunner/domain/exception/FailureDispositionPolicyTest.java`
- `src/test/java/com/agilerunner/api/controller/review/ManualRerunControllerTest.java`

### 비대상
- 실제 retry execution 시작
- source/new execution relation 적재
- representative actual app/H2 검증

### 연결 ValidationCriteria
- `manual-rerun-retry-eligibility-defined`

### 완료 조건
- 재시도 허용/거부 기준이 policy와 service 테스트로 고정된다.
- `MANUAL_RERUN + FAILED + RETRYABLE` 외 조합은 재시도 불가로 해석된다는 점이 읽힌다.

### 검증
- policy unit test
- service 테스트
- 기존 rerun query 회귀 테스트

### GitHub Issue
- 새 Issue 생성
- 권장 제목: `[BE] 재실행 재시도 가능 여부 정책 도입`

## TASK-0003
### 이름
재시도 요청 경로 연결

### 목표
- `executionKey` 기반 retry request 경계와 controller/service 진입점을 열고, source execution의 repository/pullRequest 컨텍스트를 재사용해 새 rerun execution을 시작한다.

### 구현 범위
- `POST /reviews/rerun/{executionKey}/retry` controller 진입점 추가
- retry request DTO와 service request DTO 추가
- source execution 조회 후 repository/pullRequest 컨텍스트를 읽어 기존 `ManualRerunService` 흐름을 재사용
- `selectedPaths`가 비어 있으면 전체 실행으로 해석하고, 값이 있으면 retry 입력 값으로 선택 실행을 적용
- not found 정책과 ineligible `409 Conflict + executionKey + failureDisposition + message` 정책을 controller/service 경계에서 연결
- retry 성공 응답은 최소한 `executionKey`, `retrySourceExecutionKey`, `executionControlMode`, `writePerformed`, `executionStatus`, `errorCode`, `failureDisposition`를 포함하도록 정리

### 관련 파일 후보
- `src/main/java/com/agilerunner/api/controller/review/ManualRerunController.java`
- `src/main/java/com/agilerunner/api/controller/review/request/ManualRerunRetryRequest.java`
- `src/main/java/com/agilerunner/api/controller/review/response/ManualRerunRetryResponse.java`
- `src/main/java/com/agilerunner/api/controller/review/response/ManualRerunRetryConflictResponse.java`
- `src/main/java/com/agilerunner/api/service/review/ManualRerunRetryService.java`
- `src/main/java/com/agilerunner/api/service/review/request/ManualRerunRetryServiceRequest.java`
- `src/main/java/com/agilerunner/api/service/review/response/ManualRerunRetryServiceResponse.java`
- `src/test/java/com/agilerunner/api/controller/review/ManualRerunControllerTest.java`
- `src/test/java/com/agilerunner/api/service/review/ManualRerunRetryServiceTest.java`

### 비대상
- 새 runtime 스키마 컬럼 추가
- 운영 대시보드
- 자동 재시도 스케줄러

### 연결 ValidationCriteria
- `manual-rerun-retry-request-reuses-source-context`

### 완료 조건
- retry 요청이 source execution을 기준으로 새 manual rerun execution을 시작한다.
- repository/pullRequest 컨텍스트는 source execution에서 읽고, installationId/executionControlMode/selectedPaths는 retry 입력에서 읽는다.
- `selectedPaths`가 비어 있으면 전체 실행으로 해석되고, `retrySourceExecutionKey`는 응답 필드로 노출된다.
- 기존 rerun/query 계약은 유지된다.

### 검증
- controller black-box 테스트
- service black-box 테스트
- 기존 rerun/query 회귀 테스트

### GitHub Issue
- 새 Issue 생성
- 권장 제목: `[BE] 재실행 재시도 요청 경로 연결`

## TASK-0004
### 이름
재시도 응답과 실행 이력 관계 검증

### 목표
- representative retryable failure execution 1건을 source로 실제 앱에서 retry 요청을 수행한 뒤, 응답과 runtime evidence에서 source/new execution 관계가 같은 의미로 남는지 확인한다.

### 구현 범위
- local profile 실제 앱/H2 representative 검증 수행
- representative 검증은 `retryable source execution 준비 -> source executionKey로 retry 요청 1건 실행 -> HTTP 결과 확인 -> 앱 종료 -> H2 조회` 순서를 따른다.
- retry 응답과 runtime evidence의 `retrySourceExecutionKey`, `executionStatus`, `errorCode`, `failureDisposition`, `writePerformed` 정합성 확인
- 정합성 검증에 꼭 필요한 최소 runtime relation 보정만 허용
- `retrySourceExecutionKey`의 runtime evidence 적재는 이 단계에서 닫는다.

### 관련 파일 후보
- `src/main/java/com/agilerunner/api/controller/review/ManualRerunController.java`
- `src/main/java/com/agilerunner/api/service/review/ManualRerunRetryService.java`
- `src/main/java/com/agilerunner/api/service/agentruntime/AgentRuntimeService.java`
- `src/main/java/com/agilerunner/client/agentruntime/AgentRuntimeRepository.java`
- `src/main/java/com/agilerunner/domain/agentruntime/WebhookExecution.java`
- `src/main/java/com/agilerunner/domain/agentruntime/AgentExecutionLog.java`
- `src/test/java/com/agilerunner/api/controller/review/ManualRerunControllerTest.java`
- `src/test/java/com/agilerunner/api/service/review/ManualRerunRetryServiceTest.java`
- `src/test/java/com/agilerunner/client/agentruntime/AgentRuntimeRepositoryTest.java`

### 비대상
- 운영 대시보드
- 장기 저장소 도입
- 자동 재시도 스케줄러

### 연결 ValidationCriteria
- `manual-rerun-retry-response-links-source-execution`

### 완료 조건
- representative retry 요청 1건의 응답과 runtime evidence가 같은 `retrySourceExecutionKey`와 새 `executionKey` 관계를 가진다.
- targeted test, 전체 테스트, 실제 앱/H2 representative 검증이 모두 통과한다.

### 검증
- retry service/controller 회귀 테스트
- 전체 테스트 실행
- 로컬 프로필 실제 앱 기동 후 representative retry 요청 + H2 검증

### GitHub Issue
- 새 Issue 생성
- 권장 제목: `[BE] 재실행 재시도 응답과 실행 이력 관계 검증`
