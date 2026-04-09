# 현재 활성 Task 집합

## 문서 목적
이 문서는 현재 활성 `Spec`을 실제 구현 단위로 분해한 `Task` 문서다.
각 `Task`는 하나의 명확한 결과, 검증 기준, GitHub Issue 연결 규칙을 가져야 한다.

## 현재 활성 Spec
- ID: `SPEC-0011`
- 이름: `운영용 조회와 관리자 제어 기능 확장`
- 기준 문서:
  - `.agents/active/spec.md`
  - `.agents/criteria/SPEC-0011-admin-query-control-extension.json`

## 공통 규칙
- 구현 순서는 `TASK-0001 -> TASK-0002 -> TASK-0003 -> TASK-0004`로 고정한다.
- 각 `Task` 시작 전 해당 task용 GitHub Issue를 새로 연결한다.
- 각 `Task`는 연결된 `ValidationCriteria`와 테스트 근거가 없으면 완료로 보지 않는다.
- `/webhook/github`, `POST /reviews/rerun`, `GET /reviews/rerun/{executionKey}`, `POST /reviews/rerun/{executionKey}/retry` 기존 계약은 유지한다.
- 목록 조회는 내부/관리자용 최소 진입점으로만 열고, 사용자용 UI나 bulk action은 다루지 않는다.
- `availableActions`는 우선 `RETRY` 하나만 지원하고, 포함 여부는 기존 manual rerun retry eligibility 정책을 재사용한다.
- `TASK-0001` 시작 전 기존 rerun query, retry, runtime evidence 안전망이 목록 조회 추가에도 충분한지 먼저 검토한다.

## 요약 표
| Task | 이름 | 핵심 목표 | 연결 ValidationCriteria | 핵심 검증 | Issue |
| --- | --- | --- | --- | --- | --- |
| `TASK-0001` | 목록 조회 안전망 고정 | 기존 rerun/query/retry/webhook 계약을 목록 조회 추가 전 먼저 고정 | `manual-rerun-list-contract-preserved` | rerun/query/retry/webhook 회귀 테스트 | 새 Issue |
| `TASK-0002` | 목록 조회 입력 모델과 진입점 도입 | 필터 입력 DTO와 목록 조회 controller/service 경계 도입 | `manual-rerun-list-filter-defined` | controller/service black-box 테스트 | 새 Issue |
| `TASK-0003` | 목록 응답과 관리자 제어 상태 연결 | runtime evidence를 목록 응답 row로 매핑하고 `availableActions` 해석 연결 | `manual-rerun-list-response-maps-control-state` | controller/service black-box 테스트 | 새 Issue |
| `TASK-0004` | 목록 응답과 실행 근거 정합성 검증 | representative 목록 조회 응답과 H2 evidence의 필터/상태 의미 일치 확인 | `manual-rerun-list-runtime-evidence-aligned` | 실제 앱/H2 representative 검증 | 새 Issue |

## TASK-0001
### 이름
목록 조회 안전망 고정

### 목표
- 운영용 목록 조회 기능을 열기 전에 현재 rerun query, retry 정책, webhook 계약이 유지된다는 안전망을 먼저 고정한다.

### 구현 범위
- 기존 `ManualRerunControllerTest`, `ManualRerunQueryServiceTest`, `ManualRerunRetryServiceTest`, `GitHubWebhookControllerTest`를 재사용하거나 필요한 범위만 보강한다.
- 아래 기준을 우선 고정한다.
  - `GET /reviews/rerun/{executionKey}` 기존 계약 유지
  - `POST /reviews/rerun/{executionKey}/retry` 기존 계약 유지
  - `failureDisposition`과 retry eligibility 의미 유지
  - `/webhook/github` 계약 비영향
- 기존 안전망이 충분하면 그 근거를 문서와 회고에 남기고, 부족한 경우만 새 테스트를 추가한다.

### 비대상
- 목록 조회 endpoint 추가
- 목록 응답 DTO 추가
- runtime evidence 필터 조회 추가

### 연결 ValidationCriteria
- `manual-rerun-list-contract-preserved`

### 완료 조건
- 목록 조회 기능 추가 전에도 rerun query, retry 응답, failure disposition, webhook 계약이 회귀 테스트로 고정된다.

### 검증
- rerun/query/retry/webhook 회귀 테스트 실행 통과

### GitHub Issue
- 새 Issue 생성
- 권장 제목: `[BE] 목록 조회 안전망 고정`

## TASK-0002
### 이름
목록 조회 입력 모델과 진입점 도입

### 목표
- execution 목록 조회용 필터 입력 DTO와 controller/service 진입점을 도입한다.

### 구현 범위
- `GET /reviews/rerun/executions` controller 진입점 추가
- 목록 조회 request DTO와 service request DTO 추가
- 필터 입력은 최소한 아래를 읽는다.
  - `repositoryName`
  - `pullRequestNumber`
  - `executionStartType`
  - `executionStatus`
  - `failureDisposition`
- 비어 있는 값은 미적용으로 해석한다.
- 이 단계는 기본 목록 조회 경계와 필터 해석까지만 닫고, `availableActions` 의미 연결은 다음 task로 넘긴다.

### 관련 파일 후보
- `src/main/java/com/agilerunner/api/controller/review/ManualRerunController.java`
- `src/main/java/com/agilerunner/api/controller/review/request/ManualRerunExecutionListRequest.java`
- `src/main/java/com/agilerunner/api/controller/review/response/ManualRerunExecutionListResponse.java`
- `src/main/java/com/agilerunner/api/service/review/ManualRerunExecutionListService.java`
- `src/main/java/com/agilerunner/api/service/review/request/ManualRerunExecutionListServiceRequest.java`
- `src/main/java/com/agilerunner/api/service/review/response/ManualRerunExecutionListServiceResponse.java`
- `src/test/java/com/agilerunner/api/controller/review/ManualRerunControllerTest.java`
- `src/test/java/com/agilerunner/api/service/review/ManualRerunExecutionListServiceTest.java`

### 비대상
- `availableActions` 의미 매핑
- representative actual app/H2 검증
- 새 관리자 제어 액션 추가

### 연결 ValidationCriteria
- `manual-rerun-list-filter-defined`

### 완료 조건
- 목록 조회 필터 입력이 controller/service 경계에서 읽히고, 비어 있는 값은 미적용으로 해석된다.
- 기본 목록 조회 진입점이 열리고 기존 rerun/query/retry 계약은 유지된다.

### 검증
- controller black-box 테스트
- service black-box 테스트

### GitHub Issue
- 새 Issue 생성
- 권장 제목: `[BE] 목록 조회 입력 모델과 진입점 도입`

## TASK-0003
### 이름
목록 응답과 관리자 제어 상태 연결

### 목표
- runtime evidence를 목록 응답 row로 매핑하고, 각 execution에서 가능한 관리자 제어 상태를 `availableActions`로 읽을 수 있게 연결한다.

### 구현 범위
- 목록 응답 row DTO 추가
- `WebhookExecution` 조회 결과를 목록 응답 row로 매핑
- 응답 row는 최소한 아래를 포함한다.
  - `executionKey`
  - `retrySourceExecutionKey`
  - `executionStartType`
  - `executionStatus`
  - `executionControlMode`
  - `writePerformed`
  - `errorCode`
  - `failureDisposition`
  - `availableActions`
- `availableActions`는 현재 execution이 manual rerun retry eligibility 정책상 재시도 가능할 때만 `RETRY`를 포함한다.
- 필터 결과와 응답 row 의미를 controller/service black-box 테스트로 고정한다.

### 관련 파일 후보
- `src/main/java/com/agilerunner/api/controller/review/ManualRerunController.java`
- `src/main/java/com/agilerunner/api/controller/review/response/ManualRerunExecutionListResponse.java`
- `src/main/java/com/agilerunner/api/service/review/ManualRerunExecutionListService.java`
- `src/main/java/com/agilerunner/api/service/review/response/ManualRerunExecutionListServiceResponse.java`
- `src/main/java/com/agilerunner/client/agentruntime/AgentRuntimeRepository.java`
- `src/test/java/com/agilerunner/api/controller/review/ManualRerunControllerTest.java`
- `src/test/java/com/agilerunner/api/service/review/ManualRerunExecutionListServiceTest.java`

### 비대상
- bulk action 실행
- 운영 대시보드
- representative actual app/H2 검증

### 연결 ValidationCriteria
- `manual-rerun-list-response-maps-control-state`

### 완료 조건
- 목록 응답 row의 상태 필드와 `availableActions`가 runtime evidence와 같은 의미로 읽힌다.
- `RETRY` 포함 여부는 기존 retry eligibility 정책과 같은 기준을 따른다.

### 검증
- controller black-box 테스트
- service black-box 테스트
- 기존 rerun query/retry 회귀 테스트

### GitHub Issue
- 새 Issue 생성
- 권장 제목: `[BE] 목록 응답과 관리자 제어 상태 연결`

## TASK-0004
### 이름
목록 응답과 실행 근거 정합성 검증

### 목표
- representative execution 여러 건을 기준으로 실제 앱의 목록 조회 응답과 H2 runtime evidence가 같은 필터 결과와 제어 가능 상태를 가지는지 확인한다.

### 구현 범위
- local profile 실제 앱/H2 representative 검증 수행
- representative 검증은 `execution 준비 -> 목록 조회 요청 -> HTTP 결과 확인 -> 앱 종료 -> H2 조회` 순서를 따른다.
- 최소 두 가지 representative row를 준비한다.
  - `RETRY` 가능한 execution 1건
  - `RETRY` 불가능한 execution 1건
- 목록 응답의 필터 결과와 `availableActions`, `executionStatus`, `failureDisposition` 정합성 확인
- 정합성 검증에 꼭 필요한 최소 query/runtime 보정만 허용

### 관련 파일 후보
- `src/main/java/com/agilerunner/api/controller/review/ManualRerunController.java`
- `src/main/java/com/agilerunner/api/service/review/ManualRerunExecutionListService.java`
- `src/main/java/com/agilerunner/client/agentruntime/AgentRuntimeRepository.java`
- `src/test/java/com/agilerunner/api/controller/review/ManualRerunControllerTest.java`
- `src/test/java/com/agilerunner/api/service/review/ManualRerunExecutionListServiceTest.java`

### 비대상
- 사용자용 UI
- bulk action 실행
- 장기 저장소 도입

### 연결 ValidationCriteria
- `manual-rerun-list-runtime-evidence-aligned`

### 완료 조건
- representative 목록 조회 응답과 H2 evidence가 같은 필터 결과와 제어 가능 상태를 가진다.
- targeted test, 전체 테스트, 실제 앱/H2 representative 검증이 모두 통과한다.

### 검증
- 목록 service/controller 회귀 테스트
- 전체 테스트 실행
- 로컬 프로필 실제 앱 기동 후 representative 목록 조회 + H2 검증

### GitHub Issue
- 새 Issue 생성
- 권장 제목: `[BE] 목록 응답과 실행 근거 정합성 검증`
