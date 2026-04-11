# 현재 활성 Task 집합

## 문서 목적
이 문서는 현재 활성 `Spec`을 실제 구현 단위로 분해한 `Task` 문서다.
각 `Task`는 하나의 명확한 결과, 검증 기준, GitHub Issue 연결 규칙을 가져야 한다.

## 현재 활성 Spec
- ID: `SPEC-0016`
- 이름: `운영용 관리자 제어 이력 필터 확장`
- 기준 문서:
  - `.agents/active/spec.md`
  - `.agents/criteria/SPEC-0016-admin-control-history-filter.json`

## 공통 규칙
- 구현 순서는 `TASK-0001 -> TASK-0002 -> TASK-0003 -> TASK-0004`로 고정한다.
- 각 `Task` 시작 전 해당 task용 GitHub Issue를 새로 연결한다.
- 각 `Task`는 연결된 `ValidationCriteria`와 테스트 근거가 없으면 완료로 보지 않는다.
- 기존 `POST /reviews/rerun/{executionKey}/actions`, `GET /reviews/rerun/{executionKey}`, `GET /reviews/rerun/executions`, `GET /reviews/rerun/{executionKey}/actions/history` 계약은 유지한다.
- 이번 spec은 history 조회의 최소 필터 `action`, `actionStatus` 추가에만 집중한다.
- `TASK-0001` 시작 전 기존 action/query/list/history 안전망이 필터 확장에도 충분한지 먼저 검토한다.

## 요약 표
| Task | 이름 | 핵심 목표 | 연결 ValidationCriteria | 핵심 검증 | Issue |
| --- | --- | --- | --- | --- | --- |
| `TASK-0001` | 관리자 액션 이력 필터 안전망 고정 | 필터 확장 전 기존 action/query/list/history 계약 유지 확인 | `manual-rerun-control-history-filter-contract-preserved` | action/query/list/history 회귀 테스트 | 새 Issue |
| `TASK-0002` | 관리자 액션 이력 필터 입력 모델과 진입점 도입 | history 조회가 `action`, `actionStatus` query param을 읽도록 경계 도입 | `manual-rerun-control-history-filter-input-defined` | controller/service black-box 테스트 | 새 Issue |
| `TASK-0003` | 관리자 액션 이력 필터와 audit selection 연결 | 필터가 실제 audit row 선택에 반영되도록 연결 | `manual-rerun-control-history-filter-maps-audit-selection` | controller/service/repository black-box 테스트 | 새 Issue |
| `TASK-0004` | 이력 필터와 실행 근거 정합성 검증 | 필터 없는 history, 필터 적용 history, H2 audit evidence 정합성 확인 | `manual-rerun-control-history-filter-runtime-evidence-aligned` | 실제 앱/H2 representative 검증 | 새 Issue |

## TASK-0001
### 이름
관리자 액션 이력 필터 안전망 고정

### 목표
- history 필터를 도입하기 전에 기존 action 응답, query/list 상태, history 응답 계약이 유지된다는 안전망을 먼저 고정한다.

### 구현 범위
- 기존 `ManualRerunControllerTest`, `ManualRerunControlActionServiceTest`, `ManualRerunQueryServiceTest`, `ManualRerunExecutionListServiceTest`, `ManualRerunControlActionHistoryServiceTest`를 우선 재사용한다.
- 아래 기준을 먼저 확인한다.
  - action 응답 계약 유지
  - query/list의 현재 상태와 `availableActions` 의미 유지
  - history 응답 기본 구조와 timeline 의미 유지
- 기존 안전망이 충분하면 근거를 문서와 회고에 남기고, 부족한 경우만 새 테스트를 추가한다.

### 비대상
- 필터 query param 도입
- audit row 필터 selection
- representative actual app/H2 검증

### 연결 ValidationCriteria
- `manual-rerun-control-history-filter-contract-preserved`

### 완료 조건
- 필터 확장 전에도 기존 action/query/list/history 계약이 회귀 테스트로 고정된다.

### 검증
- action/query/list/history 회귀 테스트 실행 통과

### GitHub Issue
- 새 Issue 생성
- 권장 제목: `[BE] 관리자 액션 이력 필터 안전망 고정`

## TASK-0002
### 이름
관리자 액션 이력 필터 입력 모델과 진입점 도입

### 목표
- `GET /reviews/rerun/{executionKey}/actions/history`가 `action`, `actionStatus` query param을 읽도록 controller/service 경계를 연다.

### 구현 범위
- controller request, service request가 `action`, `actionStatus`를 읽는다.
- 필터가 비어 있으면 기존과 동일한 전체 timeline 조회 의미를 유지한다.
- 이 단계는 입력 해석과 최소 응답 경계까지만 닫고, 실제 audit selection은 다음 단계로 넘긴다.

### 관련 파일 후보
- `src/main/java/com/agilerunner/api/controller/review/ManualRerunController.java`
- `src/main/java/com/agilerunner/api/controller/review/request/ManualRerunControlActionHistoryRequest.java`
- `src/main/java/com/agilerunner/api/service/review/request/ManualRerunControlActionHistoryServiceRequest.java`
- `src/main/java/com/agilerunner/api/service/review/ManualRerunControlActionHistoryService.java`
- `src/test/java/com/agilerunner/api/controller/review/ManualRerunControllerTest.java`
- `src/test/java/com/agilerunner/api/service/review/ManualRerunControlActionHistoryServiceTest.java`

### 비대상
- 실제 audit row 필터링
- representative actual app/H2 검증

### 연결 ValidationCriteria
- `manual-rerun-control-history-filter-input-defined`

### 완료 조건
- history 조회 경계가 `action`, `actionStatus` 필터를 읽고, 비어 있을 때는 전체 timeline 의미를 유지한다.

### 검증
- controller black-box 테스트
- service black-box 테스트

### GitHub Issue
- 새 Issue 생성
- 권장 제목: `[BE] 관리자 액션 이력 필터 입력 모델과 진입점 도입`

## TASK-0003
### 이름
관리자 액션 이력 필터와 audit selection 연결

### 목표
- `action`, `actionStatus` 필터가 실제 audit row 선택에 반영되도록 연결한다.

### 구현 범위
- repository/service가 `action`, `actionStatus` 필터를 실제 audit selection에 반영한다.
- 필터가 없으면 전체 timeline을, 있으면 조건 일치 row만 반환한다.
- 없는 execution 또는 필터 결과 0건은 기존 history 조회 의미를 유지한다.

### 관련 파일 후보
- `src/main/java/com/agilerunner/api/service/review/ManualRerunControlActionHistoryService.java`
- `src/main/java/com/agilerunner/api/service/review/request/ManualRerunControlActionHistoryServiceRequest.java`
- `src/main/java/com/agilerunner/client/agentruntime/AgentRuntimeRepository.java`
- `src/test/java/com/agilerunner/api/service/review/ManualRerunControlActionHistoryServiceTest.java`
- `src/test/java/com/agilerunner/api/controller/review/ManualRerunControllerTest.java`
- `src/test/java/com/agilerunner/client/agentruntime/AgentRuntimeRepositoryTest.java`

### 비대상
- 날짜 범위 필터
- representative actual app/H2 검증

### 연결 ValidationCriteria
- `manual-rerun-control-history-filter-maps-audit-selection`

### 완료 조건
- `action`, `actionStatus` 필터가 history 응답 row 선택에 반영되고, 필터가 없으면 전체 timeline 의미를 유지한다.

### 검증
- controller black-box 테스트
- service/repository black-box 테스트

### GitHub Issue
- 새 Issue 생성
- 권장 제목: `[BE] 관리자 액션 이력 필터와 audit selection 연결`

## TASK-0004
### 이름
이력 필터와 실행 근거 정합성 검증

### 목표
- representative execution에서 필터 없는 history, 필터 적용 history, H2 audit evidence가 같은 execution 기준으로 같은 의미를 가지는지 확인한다.

### 구현 범위
- local profile 실제 앱/H2 representative 검증 수행
- representative 검증은 `실행 준비 -> action 실행들 -> history 무필터 조회 -> history 필터 조회 -> 앱 종료 -> H2 조회` 순서를 따른다.
- 정합성 검증에 꼭 필요한 최소 query/runtime 보정만 허용한다.

### 관련 파일 후보
- `src/main/java/com/agilerunner/api/controller/review/ManualRerunController.java`
- `src/main/java/com/agilerunner/api/service/review/ManualRerunControlActionHistoryService.java`
- `src/main/java/com/agilerunner/client/agentruntime/AgentRuntimeRepository.java`
- `src/test/java/com/agilerunner/api/controller/review/ManualRerunControllerTest.java`
- `src/test/java/com/agilerunner/api/service/review/ManualRerunControlActionHistoryServiceTest.java`
- `src/test/java/com/agilerunner/client/agentruntime/AgentRuntimeRepositoryTest.java`

### 비대상
- 사용자용 UI
- bulk action
- 장기 저장소 도입

### 연결 ValidationCriteria
- `manual-rerun-control-history-filter-runtime-evidence-aligned`

### 완료 조건
- representative execution에서 필터 없는 history, 필터 적용 history, H2 audit evidence가 같은 execution 기준으로 일치한다.
- targeted test, 전체 테스트, 실제 앱/H2 representative 검증이 모두 통과한다.

### 검증
- action/history 회귀 테스트
- 전체 테스트 실행
- 로컬 프로필 실제 앱 기동 후 representative action 실행 + history 무필터/필터 조회 + H2 검증

### GitHub Issue
- 새 Issue 생성
- 권장 제목: `[BE] 이력 필터와 실행 근거 정합성 검증`
