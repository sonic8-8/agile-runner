# 현재 활성 Task 집합

## 문서 목적
이 문서는 현재 활성 `Spec`을 실제 구현 단위로 분해한 `Task` 문서다.
각 `Task`는 하나의 명확한 결과, 검증 기준, GitHub Issue 연결 규칙을 가져야 한다.

## 현재 활성 Spec
- ID: `SPEC-0009`
- 이름: `재실행 결과 조회 기능 기반 마련`
- 기준 문서:
  - `.agents/active/spec.md`
  - `.agents/criteria/SPEC-0009-rerun-result-query-foundation.json`

## 공통 규칙
- 구현 순서는 `TASK-0001 -> TASK-0002 -> TASK-0003 -> TASK-0004`로 고정한다.
- 각 `Task` 시작 전 해당 task용 GitHub Issue를 새로 연결한다.
- 각 `Task`는 연결된 `ValidationCriteria`와 테스트 근거가 없으면 완료로 보지 않는다.
- `POST /reviews/rerun`과 `/webhook/github` 기존 계약은 유지한다.
- 조회 기능은 내부/관리자용 최소 진입점으로만 열고, 운영 대시보드나 장기 저장소는 다루지 않는다.
- 조회 응답은 rerun 응답과 모순되지 않아야 하고, runtime evidence에서 읽은 값과 같은 의미를 가져야 한다.
- `TASK-0001` 시작 전 기존 rerun 응답 및 runtime evidence 안전망이 조회 기능 추가에도 충분한지 먼저 검토한다.

## 요약 표
| Task | 이름 | 핵심 목표 | 연결 ValidationCriteria | 핵심 검증 | Issue |
| --- | --- | --- | --- | --- | --- |
| `TASK-0001` | 재실행 결과 조회 안전망 고정 | 기존 rerun 계약과 runtime evidence 정합성 기준을 조회 기능 추가 전 먼저 고정 | `manual-rerun-query-contract-preserved` | rerun/webhook 회귀 테스트 | 새 Issue |
| `TASK-0002` | 재실행 결과 조회 입력 모델과 진입점 도입 | `executionKey` 기반 조회 controller/service 경계와 not found 정책 도입 | `manual-rerun-query-input-and-not-found-policy-defined` | controller/service black-box 테스트 | 새 Issue |
| `TASK-0003` | 재실행 결과 조회 응답 연결 | runtime evidence 값을 조회 응답 DTO에 같은 의미로 연결 | `manual-rerun-query-response-matches-rerun-meaning` | service/controller black-box 테스트 | 새 Issue |
| `TASK-0004` | 조회 응답과 실행 근거 정합성 검증 | representative rerun 생성 후 같은 `executionKey` 조회 응답과 H2 evidence 일치 확인 | `manual-rerun-query-response-matches-runtime-evidence` | 실제 앱/H2 representative 검증 | 새 Issue |

## TASK-0001
### 이름
재실행 결과 조회 안전망 고정

### 목표
- 조회 기능을 열기 전에 현재 rerun 응답, runtime evidence, webhook 계약이 유지된다는 안전망을 먼저 고정한다.

### 구현 범위
- 기존 `ManualRerunControllerTest`, `ManualRerunServiceTest`, `AgentRuntimeServiceTest`, `GitHubWebhookControllerTest`를 재사용하거나 필요한 범위만 보강한다.
- 아래 기준을 우선 고정한다.
  - `POST /reviews/rerun` 기존 계약 유지
  - runtime evidence의 `executionKey`, `executionStatus`, `errorCode`, `failureDisposition`, `writePerformed` 의미 유지
  - `/webhook/github` 계약 비영향
- 기존 안전망이 충분하면 그 근거를 문서와 회고에 남기고, 부족한 경우만 새 테스트를 추가한다.

### 비대상
- 조회 endpoint 추가
- 조회 response DTO 추가
- runtime evidence 매핑 변경

### 연결 ValidationCriteria
- `manual-rerun-query-contract-preserved`

### 완료 조건
- 조회 기능 추가 전에도 rerun 응답과 runtime evidence 정합성 기준이 회귀 테스트로 고정된다.
- webhook 계약 비영향도 테스트와 체크리스트로 확인된다.

### 검증
- rerun/webhook 회귀 테스트 실행 통과
- 현재 유지 계약과 테스트 근거 대응 관계 점검

### GitHub Issue
- 새 Issue 생성
- 권장 제목: `[BE] 재실행 결과 조회 안전망 고정`

## TASK-0002
### 이름
재실행 결과 조회 입력 모델과 진입점 도입

### 목표
- `executionKey` 기반 조회 controller/service 입력 모델과 내부 조회 endpoint를 연다.

### 구현 범위
- `GET /reviews/rerun/{executionKey}` controller 진입점 추가
- service request DTO와 response placeholder 경계 추가
- 존재하지 않는 `executionKey`는 `404 Not Found + executionKey + message` 정책으로 고정
- 기존 rerun 응답 계약은 그대로 유지

### 관련 파일 후보
- `src/main/java/com/agilerunner/api/controller/review/ManualRerunController.java`
- `src/main/java/com/agilerunner/api/controller/review/response/ManualRerunQueryResponse.java`
- `src/main/java/com/agilerunner/api/service/review/ManualRerunQueryService.java`
- `src/main/java/com/agilerunner/api/service/review/request/ManualRerunQueryServiceRequest.java`
- `src/main/java/com/agilerunner/api/service/review/response/ManualRerunQueryServiceResponse.java`
- `src/test/java/com/agilerunner/api/controller/review/ManualRerunControllerTest.java`

### 비대상
- runtime evidence 실제 조회 로직 연결
- 조회 응답의 최종 의미 매핑
- actual app/H2 representative 검증

### 연결 ValidationCriteria
- `manual-rerun-query-input-and-not-found-policy-defined`

### 완료 조건
- `executionKey` 기반 조회 요청이 controller/service 경계에서 해석된다.
- 존재하지 않는 `executionKey`의 not found 정책이 `404 Not Found + executionKey + message` 기준으로 black-box 테스트에 고정된다.
- 기존 rerun 요청 경로는 유지된다.

### 검증
- controller black-box 테스트
- service request/response 테스트
- 기존 rerun 회귀 테스트

### GitHub Issue
- 새 Issue 생성
- 권장 제목: `[BE] 재실행 결과 조회 입력 모델과 진입점 도입`

## TASK-0003
### 이름
재실행 결과 조회 응답 연결

### 목표
- runtime evidence에 저장된 값을 조회 응답 DTO에 같은 의미로 연결한다.

### 구현 범위
- `executionKey` 기반 `WebhookExecution`, 필요 최소 `AgentExecutionLog` 조회
- 조회 응답이 `executionKey`, `executionControlMode`, `writePerformed`, `executionStatus`, `errorCode`, `failureDisposition`를 같은 의미로 반환하도록 정리
- rerun 응답과 조회 응답의 공통 필드 의미가 일치하도록 유지

### 관련 파일 후보
- `src/main/java/com/agilerunner/api/service/review/ManualRerunQueryService.java`
- `src/main/java/com/agilerunner/client/agentruntime/AgentRuntimeRepository.java`
- `src/main/java/com/agilerunner/api/controller/review/response/ManualRerunQueryResponse.java`
- `src/main/java/com/agilerunner/api/service/review/response/ManualRerunQueryServiceResponse.java`
- `src/test/java/com/agilerunner/api/service/review/ManualRerunQueryServiceTest.java`
- `src/test/java/com/agilerunner/api/controller/review/ManualRerunControllerTest.java`

### 비대상
- 새 runtime 스키마 컬럼 추가
- rerun 요청 응답 모델 재설계
- 운영 대시보드

### 연결 ValidationCriteria
- `manual-rerun-query-response-matches-rerun-meaning`

### 완료 조건
- 조회 응답에서 rerun 응답과 같은 공통 필드 의미를 읽을 수 있다.
- not found가 아닌 조회 성공 경로는 runtime evidence를 기준으로 값을 반환한다.
- 기존 rerun 응답 계약은 유지된다.

### 검증
- service black-box 테스트
- controller black-box 테스트
- repository 기반 조회 테스트

### GitHub Issue
- 새 Issue 생성
- 권장 제목: `[BE] 재실행 결과 조회 응답 연결`

## TASK-0004
### 이름
조회 응답과 실행 근거 정합성 검증

### 목표
- representative manual rerun 1건을 실제 앱으로 만든 뒤, 같은 `executionKey`로 조회한 응답과 runtime evidence의 의미 일치를 확인한다.

### 구현 범위
- local profile 실제 앱/H2 representative 검증 수행
- representative 검증은 `fresh manual rerun 요청 1건 생성 -> 응답으로 받은 executionKey로 GET 조회 1건 실행 -> 앱 종료 -> H2 조회` 순서를 따른다.
- 조회 응답과 runtime evidence의 `executionStatus`, `errorCode`, `failureDisposition`, `writePerformed` 정합성 확인
- 정합성 검증에 꼭 필요한 최소 매핑 보정만 허용

### 관련 파일 후보
- `src/main/java/com/agilerunner/api/controller/review/ManualRerunController.java`
- `src/main/java/com/agilerunner/api/service/review/ManualRerunQueryService.java`
- `src/main/java/com/agilerunner/client/agentruntime/AgentRuntimeRepository.java`
- `src/test/java/com/agilerunner/api/controller/review/ManualRerunControllerTest.java`
- `src/test/java/com/agilerunner/api/service/review/ManualRerunQueryServiceTest.java`
- `src/test/java/com/agilerunner/client/agentruntime/AgentRuntimeRepositoryTest.java`

### 비대상
- 운영 대시보드
- 장기 저장소 도입
- 자동 재시도 정책

### 연결 ValidationCriteria
- `manual-rerun-query-response-matches-runtime-evidence`

### 완료 조건
- representative manual rerun 1건의 `executionKey`로 조회한 응답이 runtime evidence와 같은 의미를 가진다.
- targeted test, 전체 테스트, 실제 앱/H2 representative 검증이 모두 통과한다.

### 검증
- query service/controller 회귀 테스트
- 전체 테스트 실행
- 로컬 프로필 실제 앱 기동 후 representative rerun 생성 + query 검증

### GitHub Issue
- 새 Issue 생성
- 권장 제목: `[BE] 재실행 결과 조회와 실행 기록 정합성 검증`
