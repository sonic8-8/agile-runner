# 현재 활성 Task 집합

## 문서 목적
이 문서는 현재 활성 `Spec`을 실제 구현 단위로 분해한 `Task` 문서다.
각 `Task`는 하나의 명확한 결과, 검증 기준, GitHub Issue 연결 규칙을 가져야 한다.

## 현재 활성 Spec
- ID: `SPEC-0012`
- 이름: `운영용 관리자 제어 액션 확장`
- 기준 문서:
  - `.agents/active/spec.md`
  - `.agents/criteria/SPEC-0012-admin-control-action-extension.json`

## 공통 규칙
- 구현 순서는 `TASK-0001 -> TASK-0002 -> TASK-0003 -> TASK-0004`로 고정한다.
- 각 `Task` 시작 전 해당 task용 GitHub Issue를 새로 연결한다.
- 각 `Task`는 연결된 `ValidationCriteria`와 테스트 근거가 없으면 완료로 보지 않는다.
- `/webhook/github`, `POST /reviews/rerun`, `GET /reviews/rerun/{executionKey}`, `POST /reviews/rerun/{executionKey}/retry`, `GET /reviews/rerun/executions` 기존 계약은 유지한다.
- 관리자 제어 액션은 내부/관리자용 최소 진입점으로만 열고, 사용자용 UI나 bulk action은 다루지 않는다.
- 첫 관리자 제어 액션은 `ACKNOWLEDGE` 하나만 지원한다.
- `TASK-0001` 시작 전 기존 rerun query, retry, 목록 조회, runtime evidence 안전망이 관리자 액션 추가에도 충분한지 먼저 검토한다.

## 요약 표
| Task | 이름 | 핵심 목표 | 연결 ValidationCriteria | 핵심 검증 | Issue |
| --- | --- | --- | --- | --- | --- |
| `TASK-0001` | 관리자 제어 액션 안전망 고정 | 기존 rerun/query/retry/list/webhook 계약을 액션 추가 전 먼저 고정 | `manual-rerun-control-contract-preserved` | rerun/query/retry/list/webhook 회귀 테스트 | 새 Issue |
| `TASK-0002` | 액션 입력 모델과 진입점 도입 | `POST /reviews/rerun/{executionKey}/actions` request DTO와 controller/service 경계 도입 | `manual-rerun-control-action-defined` | controller/service black-box 테스트 | 새 Issue |
| `TASK-0003` | 액션 실행과 audit state 연결 | `ACKNOWLEDGE` 실행 조건, audit evidence 저장, 조회 반영 연결 | `manual-rerun-control-response-maps-audit-state` | controller/service black-box 테스트 | 새 Issue |
| `TASK-0004` | 액션 응답과 실행 근거 정합성 검증 | representative 관리자 제어 액션 응답과 H2 audit evidence 정합성 확인 | `manual-rerun-control-runtime-evidence-aligned` | 실제 앱/H2 representative 검증 | 새 Issue |

## TASK-0001
### 이름
관리자 제어 액션 안전망 고정

### 목표
- 운영용 관리자 제어 액션 기능을 열기 전에 현재 rerun query, retry, 목록 조회, webhook 계약이 유지된다는 안전망을 먼저 고정한다.

### 구현 범위
- 기존 `ManualRerunControllerTest`, `ManualRerunQueryServiceTest`, `ManualRerunRetryServiceTest`, `ManualRerunExecutionListServiceTest`, `GitHubWebhookControllerTest`를 재사용하거나 필요한 범위만 보강한다.
- 아래 기준을 우선 고정한다.
  - `GET /reviews/rerun/{executionKey}` 기존 계약 유지
  - `POST /reviews/rerun/{executionKey}/retry` 기존 계약 유지
  - `GET /reviews/rerun/executions` 기존 계약 유지
  - `failureDisposition`과 retry eligibility 의미 유지
  - `/webhook/github` 계약 비영향
- 기존 안전망이 충분하면 그 근거를 문서와 회고에 남기고, 부족한 경우만 새 테스트를 추가한다.

### 비대상
- 관리자 제어 액션 endpoint 추가
- action request/response DTO 추가
- audit evidence 저장 추가

### 연결 ValidationCriteria
- `manual-rerun-control-contract-preserved`

### 완료 조건
- 관리자 제어 액션 추가 전에도 rerun query, retry, 목록 조회, failure disposition, webhook 계약이 회귀 테스트로 고정된다.

### 검증
- rerun/query/retry/list/webhook 회귀 테스트 실행 통과

### GitHub Issue
- 새 Issue 생성
- 권장 제목: `[BE] 관리자 제어 액션 안전망 고정`

## TASK-0002
### 이름
액션 입력 모델과 진입점 도입

### 목표
- `POST /reviews/rerun/{executionKey}/actions` request DTO와 controller/service 진입점을 도입한다.

### 구현 범위
- 관리자 제어 액션 endpoint 추가
- action request DTO와 service request DTO 추가
- request는 최소한 아래를 읽는다.
  - `action`
  - `note`
- 첫 지원 action은 `ACKNOWLEDGE` 하나만 허용한다.
- 이 단계는 request 해석과 최소 응답 경계까지만 닫고, 성공 응답의 `actionStatus=APPLIED` 계약을 먼저 고정한다.
- audit evidence 저장과 조회 반영은 다음 task로 넘긴다.

### 관련 파일 후보
- `src/main/java/com/agilerunner/api/controller/review/ManualRerunController.java`
- `src/main/java/com/agilerunner/api/controller/review/request/ManualRerunControlActionRequest.java`
- `src/main/java/com/agilerunner/api/controller/review/response/ManualRerunControlActionResponse.java`
- `src/main/java/com/agilerunner/api/service/review/ManualRerunControlActionService.java`
- `src/main/java/com/agilerunner/api/service/review/request/ManualRerunControlActionServiceRequest.java`
- `src/main/java/com/agilerunner/api/service/review/response/ManualRerunControlActionServiceResponse.java`
- `src/test/java/com/agilerunner/api/controller/review/ManualRerunControllerTest.java`
- `src/test/java/com/agilerunner/api/service/review/ManualRerunControlActionServiceTest.java`

### 비대상
- audit evidence 저장
- 목록/단건 조회에 action 결과 반영
- representative actual app/H2 검증

### 연결 ValidationCriteria
- `manual-rerun-control-action-defined`

### 완료 조건
- 관리자 제어 액션 입력이 controller/service 경계에서 읽히고, `ACKNOWLEDGE` 요청의 성공 응답이 최소한 `executionKey`, `action`, `actionStatus=APPLIED`, `availableActions`, `note`를 반환한다.
- 기존 rerun/query/retry/list 계약은 유지된다.

### 검증
- controller black-box 테스트
- service black-box 테스트

### GitHub Issue
- 새 Issue 생성
- 권장 제목: `[BE] 관리자 제어 액션 입력 모델과 진입점 도입`

## TASK-0003
### 이름
액션 실행과 audit state 연결

### 목표
- `ACKNOWLEDGE` 실행 조건을 정책으로 고정하고, 실행 결과를 audit evidence로 저장하며, 목록/단건 조회가 이 상태를 반영하도록 연결한다.

### 구현 범위
- `ACKNOWLEDGE` 허용 조건 정책 추가 또는 기존 정책 확장
- action audit evidence 저장 구조 추가
- action 응답은 최소한 아래를 포함한다.
  - `executionKey`
  - `action`
  - `actionStatus`
  - `availableActions`
  - `note`
- action 실행 후 단건 조회와 목록 조회가 최신 audit state를 반영해 `availableActions`를 다시 계산한다.
- 단건 조회와 목록 조회는 이번 spec에서 `action`, `actionStatus`, `note`를 직접 노출하지 않고, audit state가 반영된 `availableActions`만 반환한다.
- 허용되지 않은 execution 상태 조합은 성공으로 처리하지 않는다.

### 관련 파일 후보
- `src/main/java/com/agilerunner/api/controller/review/ManualRerunController.java`
- `src/main/java/com/agilerunner/api/service/review/ManualRerunControlActionService.java`
- `src/main/java/com/agilerunner/api/service/review/ManualRerunExecutionListService.java`
- `src/main/java/com/agilerunner/api/service/review/ManualRerunQueryService.java`
- `src/main/java/com/agilerunner/client/agentruntime/AgentRuntimeRepository.java`
- `src/main/resources/agent-runtime/schema.sql`
- `src/test/java/com/agilerunner/api/controller/review/ManualRerunControllerTest.java`
- `src/test/java/com/agilerunner/api/service/review/ManualRerunControlActionServiceTest.java`
- `src/test/java/com/agilerunner/api/service/review/ManualRerunExecutionListServiceTest.java`
- `src/test/java/com/agilerunner/api/service/review/ManualRerunQueryServiceTest.java`

### 비대상
- bulk action 실행
- 추가 관리자 제어 액션 도입
- representative actual app/H2 검증

### 연결 ValidationCriteria
- `manual-rerun-control-response-maps-audit-state`

### 완료 조건
- `ACKNOWLEDGE` 실행 결과와 audit state가 같은 의미로 저장되고, 목록/단건 조회가 이를 반영해 `availableActions`를 반환한다.
- 허용되지 않은 상태 조합에서는 conflict 또는 not-found 정책이 일관되게 적용된다.

### 검증
- controller black-box 테스트
- service black-box 테스트
- 기존 rerun query/retry/list 회귀 테스트

### GitHub Issue
- 새 Issue 생성
- 권장 제목: `[BE] 관리자 제어 액션 실행과 상태 반영 연결`

## TASK-0004
### 이름
액션 응답과 실행 근거 정합성 검증

### 목표
- representative 관리자 제어 액션 요청을 실제 앱에서 수행하고, 응답/조회 결과/H2 audit evidence가 같은 의미를 가지는지 확인한다.

### 구현 범위
- local profile 실제 앱/H2 representative 검증 수행
- representative 검증은 `execution 준비 -> action 요청 -> HTTP 결과 확인 -> 단건/목록 조회 확인 -> 앱 종료 -> H2 조회` 순서를 따른다.
- 최소 한 가지 representative execution을 준비한다.
  - `MANUAL_ACTION_REQUIRED` 이고 `ACKNOWLEDGE` 가능한 execution 1건
- action 응답의 `actionStatus`, `availableActions`, `note`와 단건/목록 조회 상태, H2 audit evidence 정합성 확인
- 정합성 검증에 꼭 필요한 최소 query/runtime 보정만 허용

### 관련 파일 후보
- `src/main/java/com/agilerunner/api/controller/review/ManualRerunController.java`
- `src/main/java/com/agilerunner/api/service/review/ManualRerunControlActionService.java`
- `src/main/java/com/agilerunner/api/service/review/ManualRerunExecutionListService.java`
- `src/main/java/com/agilerunner/api/service/review/ManualRerunQueryService.java`
- `src/main/java/com/agilerunner/client/agentruntime/AgentRuntimeRepository.java`
- `src/test/java/com/agilerunner/api/controller/review/ManualRerunControllerTest.java`
- `src/test/java/com/agilerunner/api/service/review/ManualRerunControlActionServiceTest.java`

### 비대상
- 사용자용 UI
- bulk action 실행
- 장기 저장소 도입

### 연결 ValidationCriteria
- `manual-rerun-control-runtime-evidence-aligned`

### 완료 조건
- representative 관리자 제어 액션 응답과 단건/목록 조회 상태, H2 audit evidence가 같은 결과를 가진다.
- targeted test, 전체 테스트, 실제 앱/H2 representative 검증이 모두 통과한다.

### 검증
- action service/controller 회귀 테스트
- 전체 테스트 실행
- 로컬 프로필 실제 앱 기동 후 representative action 요청 + query/list 조회 + H2 검증

### GitHub Issue
- 새 Issue 생성
- 권장 제목: `[BE] 관리자 제어 액션 응답과 실행 근거 정합성 검증`
