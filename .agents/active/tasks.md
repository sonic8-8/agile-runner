# 현재 활성 Task 집합

## 문서 목적
이 문서는 현재 활성 `Spec`을 실제 구현 단위로 분해한 `Task` 문서다.
각 `Task`는 하나의 명확한 결과, 검증 기준, GitHub Issue 연결 규칙을 가져야 한다.

## 현재 활성 Spec
- ID: `SPEC-0014`
- 이름: `운영용 관리자 제어 이력 조회 확장`
- 기준 문서:
  - `.agents/active/spec.md`
  - `.agents/criteria/SPEC-0014-admin-control-history-query.json`

## 공통 규칙
- 구현 순서는 `TASK-0001 -> TASK-0002 -> TASK-0003 -> TASK-0004`로 고정한다.
- 각 `Task` 시작 전 해당 task용 GitHub Issue를 새로 연결한다.
- 각 `Task`는 연결된 `ValidationCriteria`와 테스트 근거가 없으면 완료로 보지 않는다.
- 기존 `/webhook/github`, `POST /reviews/rerun`, `GET /reviews/rerun/{executionKey}`, `POST /reviews/rerun/{executionKey}/retry`, `GET /reviews/rerun/executions`, `POST /reviews/rerun/{executionKey}/actions` 계약은 유지한다.
- 이번 spec은 action history 조회 추가에만 집중하고, 현재 상태 요약인 query/list 응답 의미는 유지한다.
- `TASK-0001` 시작 전 기존 관리자 액션, query/list, retry, webhook 안전망이 history 조회 도입에도 충분한지 먼저 검토한다.

## 요약 표
| Task | 이름 | 핵심 목표 | 연결 ValidationCriteria | 핵심 검증 | Issue |
| --- | --- | --- | --- | --- | --- |
| `TASK-0001` | 관리자 액션 이력 조회 안전망 고정 | history 조회 도입 전 기존 action/query/list/retry/webhook 계약 유지 확인 | `manual-rerun-control-history-contract-preserved` | query/list/action/retry/webhook 회귀 테스트 | 새 Issue |
| `TASK-0002` | 관리자 액션 이력 입력 모델과 진입점 도입 | history 조회 경로와 최소 응답 모델 도입 | `manual-rerun-control-history-response-defined` | controller/service black-box 테스트 | 새 Issue |
| `TASK-0003` | 관리자 액션 이력 응답과 audit timeline 연결 | history 응답이 audit row를 시간 순서대로 반환하도록 연결 | `manual-rerun-control-history-maps-audit-timeline` | controller/service/repository black-box 테스트 | 새 Issue |
| `TASK-0004` | 관리자 액션 이력과 실행 근거 정합성 검증 | representative execution에서 action 응답, history, query/list, H2 audit evidence 정합성 확인 | `manual-rerun-control-history-runtime-evidence-aligned` | 실제 앱/H2 representative 검증 | 새 Issue |

## TASK-0001
### 이름
관리자 액션 이력 조회 안전망 고정

### 목표
- action history 조회를 도입하기 전에 기존 action 응답, query/list, retry, webhook 계약이 유지된다는 안전망을 먼저 고정한다.

### 구현 범위
- 기존 `ManualRerunControllerTest`, `ManualRerunControlActionServiceTest`, `ManualRerunQueryServiceTest`, `ManualRerunExecutionListServiceTest`, `ManualRerunRetryServiceTest`, `GitHubWebhookControllerTest`를 우선 재사용한다.
- 아래 기준을 먼저 확인한다.
  - 관리자 액션 응답 계약 유지
  - query/list의 `availableActions` 의미 유지
  - retry eligibility 의미 유지
  - `/webhook/github` 계약 비영향
- 기존 안전망이 충분하면 근거를 문서와 회고에 남기고, 부족한 경우만 새 테스트를 추가한다.

### 비대상
- history 조회 endpoint 추가
- history response DTO 추가
- audit timeline 매핑
- representative actual app/H2 검증

### 연결 ValidationCriteria
- `manual-rerun-control-history-contract-preserved`

### 완료 조건
- action history 조회 도입 전에도 기존 관리자 액션, query/list, retry, webhook 계약이 회귀 테스트로 고정된다.

### 검증
- action/query/list/retry/webhook 회귀 테스트 실행 통과

### GitHub Issue
- 새 Issue 생성
- 권장 제목: `[BE] 관리자 액션 이력 조회 안전망 고정`

## TASK-0002
### 이름
관리자 액션 이력 입력 모델과 진입점 도입

### 목표
- `GET /reviews/rerun/{executionKey}/actions/history` 경로와 최소 응답 모델을 도입한다.

### 구현 범위
- controller/service request 해석에 history 조회 경로를 추가한다.
- history 응답 계약은 최소한 아래를 유지한다.
  - `executionKey`
  - `actions`
  - `actions[].action`
  - `actions[].actionStatus`
  - `actions[].note`
  - `actions[].appliedAt`
- 이 단계는 입력 해석과 최소 응답 경계까지만 닫고, audit row 실제 매핑과 정렬은 다음 단계로 넘긴다.

### 관련 파일 후보
- `src/main/java/com/agilerunner/api/controller/review/ManualRerunController.java`
- `src/main/java/com/agilerunner/api/controller/review/response/ManualRerunControlActionHistoryResponse.java`
- `src/main/java/com/agilerunner/api/service/review/ManualRerunControlActionHistoryService.java`
- `src/main/java/com/agilerunner/api/service/review/request/ManualRerunControlActionHistoryServiceRequest.java`
- `src/main/java/com/agilerunner/api/service/review/response/ManualRerunControlActionHistoryServiceResponse.java`
- `src/test/java/com/agilerunner/api/controller/review/ManualRerunControllerTest.java`
- `src/test/java/com/agilerunner/api/service/review/ManualRerunControlActionHistoryServiceTest.java`

### 비대상
- audit timeline 정렬
- not-found 정책 세부 해석
- representative actual app/H2 검증

### 연결 ValidationCriteria
- `manual-rerun-control-history-response-defined`

### 완료 조건
- history 조회 경로가 `executionKey`를 읽고, 성공 응답에서 최소한 `executionKey`, `actions[].action`, `actions[].actionStatus`, `actions[].note`, `actions[].appliedAt`를 반환한다.

### 검증
- controller black-box 테스트
- service black-box 테스트

### GitHub Issue
- 새 Issue 생성
- 권장 제목: `[BE] 관리자 액션 이력 입력 모델과 진입점 도입`

## TASK-0003
### 이름
관리자 액션 이력 응답과 audit timeline 연결

### 목표
- history 응답이 audit row를 시간 순서대로 읽고, query/list와 역할이 겹치지 않도록 연결한다.

### 구현 범위
- history service가 `MANUAL_RERUN_CONTROL_ACTION_AUDIT`를 읽어 response row로 매핑
- 같은 execution의 action history를 `appliedAt` 기준 시간 순서대로 반환
- action detail은 history 응답에서만 읽고, query/list는 계속 `availableActions`만 반환
- 없는 execution 또는 history 조회 불가 상황은 일관된 not-found 정책을 따른다

### 관련 파일 후보
- `src/main/java/com/agilerunner/api/controller/review/ManualRerunController.java`
- `src/main/java/com/agilerunner/api/service/review/ManualRerunControlActionHistoryService.java`
- `src/main/java/com/agilerunner/client/agentruntime/AgentRuntimeRepository.java`
- `src/main/resources/agent-runtime/schema.sql`
- `src/test/java/com/agilerunner/api/controller/review/ManualRerunControllerTest.java`
- `src/test/java/com/agilerunner/api/service/review/ManualRerunControlActionHistoryServiceTest.java`
- `src/test/java/com/agilerunner/client/agentruntime/AgentRuntimeRepositoryTest.java`

### 비대상
- 같은 action 반복 허용
- 새 관리자 액션 종류 추가
- representative actual app/H2 검증

### 연결 ValidationCriteria
- `manual-rerun-control-history-maps-audit-timeline`

### 완료 조건
- history 응답이 audit row의 `action`, `actionStatus`, `note`, `appliedAt`를 시간 순서대로 반환하고, query/list는 계속 현재 상태와 `availableActions`만 반환한다.
- 없는 execution 또는 history 조회 불가 상황에서 not-found 정책이 일관된다.

### 검증
- controller black-box 테스트
- service/repository black-box 테스트
- 기존 action/query/list/retry 회귀 테스트

### GitHub Issue
- 새 Issue 생성
- 권장 제목: `[BE] 관리자 액션 이력 응답과 audit timeline 연결`

## TASK-0004
### 이름
관리자 액션 이력과 실행 근거 정합성 검증

### 목표
- representative execution에서 action 응답, history 응답, query/list 상태, H2 audit evidence가 같은 execution 기준으로 같은 의미를 가지는지 확인한다.

### 구현 범위
- local profile 실제 앱/H2 representative 검증 수행
- representative 검증은 `execution 준비 -> action 실행 -> history 조회 -> query/list 조회 -> 앱 종료 -> H2 조회` 순서를 따른다.
- history 응답 row와 H2 audit row의 `action`, `actionStatus`, `note`, `appliedAt` 정합성 확인
- 정합성 검증에 꼭 필요한 최소 query/runtime 보정만 허용

### 관련 파일 후보
- `src/main/java/com/agilerunner/api/controller/review/ManualRerunController.java`
- `src/main/java/com/agilerunner/api/service/review/ManualRerunControlActionHistoryService.java`
- `src/main/java/com/agilerunner/client/agentruntime/AgentRuntimeRepository.java`
- `src/test/java/com/agilerunner/api/controller/review/ManualRerunControllerTest.java`
- `src/test/java/com/agilerunner/api/service/review/ManualRerunControlActionHistoryServiceTest.java`

### 비대상
- 사용자용 UI
- bulk action
- 장기 저장소 도입

### 연결 ValidationCriteria
- `manual-rerun-control-history-runtime-evidence-aligned`

### 완료 조건
- representative execution에서 action 응답, history 응답, query/list 상태, H2 audit evidence가 같은 execution 기준으로 일치한다.
- targeted test, 전체 테스트, 실제 앱/H2 representative 검증이 모두 통과한다.

### 검증
- action/history service/controller 회귀 테스트
- 전체 테스트 실행
- 로컬 프로필 실제 앱 기동 후 representative action 실행 + history/query/list 조회 + H2 검증

### GitHub Issue
- 새 Issue 생성
- 권장 제목: `[BE] 관리자 액션 이력과 실행 근거 정합성 검증`
