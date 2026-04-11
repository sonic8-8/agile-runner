# 현재 활성 Task 집합

## 문서 목적
이 문서는 현재 활성 `Spec`을 실제 구현 단위로 분해한 `Task` 문서다.
각 `Task`는 하나의 명확한 결과, 검증 기준, GitHub Issue 연결 규칙을 가져야 한다.

## 현재 활성 Spec
- ID: `SPEC-0019`
- 이름: `운영용 목록 조회와 이력 조회 조합 정리`
- 기준 문서:
  - `.agents/active/spec.md`
  - `.agents/criteria/SPEC-0019-admin-query-history-composition.json`

## 공통 규칙
- 구현 순서는 `TASK-0001 -> TASK-0002 -> TASK-0003 -> TASK-0004`로 고정한다.
- 각 `Task` 시작 전 해당 task용 GitHub Issue를 새로 연결한다.
- 각 `Task`는 연결된 `ValidationCriteria`와 테스트 근거가 없으면 완료로 보지 않는다.
- 기존 `GET /reviews/rerun/executions`, `GET /reviews/rerun/{executionKey}/actions/history`, `GET /reviews/rerun/{executionKey}`, `POST /reviews/rerun/{executionKey}/actions` 계약은 유지한다.
- 이번 spec은 list와 history 응답의 의미 연결에만 집중한다.
- list는 현재 상태 요약, history는 과거 timeline이라는 경계를 유지한다.
- `TASK-0001` 시작 전 기존 list/query/history/action 안전망이 이번 조합 정리에도 충분한지 먼저 검토한다.

## 요약 표
| Task | 이름 | 핵심 목표 | 연결 ValidationCriteria | 핵심 검증 | Issue |
| --- | --- | --- | --- | --- | --- |
| `TASK-0001` | 목록 조회와 이력 조회 조합 안전망 고정 | 조합 정리 전 기존 list/query/history/action 계약 유지 확인 | `manual-rerun-list-history-composition-contract-preserved` | 회귀 테스트 | 새 Issue |
| `TASK-0002` | 목록 row와 history 현재 조치 상태 요약 응답 모델 도입 | list row와 history 응답이 최신 action 요약을 담도록 응답 경계 도입 | `manual-rerun-list-history-composition-response-defined` | controller/service black-box 테스트 | 새 Issue |
| `TASK-0003` | 최신 관리자 액션 요약과 timeline 연결 | list row와 history 현재 조치 상태 요약이 최신 applied audit row 기준으로 계산되도록 연결 | `manual-rerun-list-history-composition-maps-latest-action` | service/repository black-box 테스트 | 새 Issue |
| `TASK-0004` | 목록 요약과 이력 timeline 실행 근거 정합성 검증 | representative execution에서 list row, history 현재 조치 상태 요약, H2 audit evidence 정합성 확인 | `manual-rerun-list-history-composition-runtime-evidence-aligned` | 실제 앱/H2 representative 검증 | 새 Issue |

## TASK-0001
### 이름
목록 조회와 이력 조회 조합 안전망 고정

### 목표
- list/history 조합 기준을 도입하기 전에 기존 action/query/list/history 계약이 충분히 고정돼 있는지 먼저 확인한다.

### 구현 범위
- 기존 `ManualRerunControllerTest`, `ManualRerunExecutionListServiceTest`, `ManualRerunControlActionHistoryServiceTest`, `ManualRerunQueryServiceTest`, `ManualRerunControlActionServiceTest`를 우선 재사용한다.
- 아래 기준을 먼저 확인한다.
  - list row 현재 상태 요약 계약 유지
  - history timeline 기본 구조와 정렬/페이지 의미 유지
  - query/action 응답 의미 유지
- 기존 안전망이 충분하면 근거를 문서와 회고에 남기고, 부족한 경우만 새 테스트를 추가한다.

### 비대상
- 새 응답 필드 추가
- 최신 action 계산 연결
- representative actual app/H2 검증

### 연결 ValidationCriteria
- `manual-rerun-list-history-composition-contract-preserved`

### 완료 조건
- list/history 조합 기준 도입 전에도 기존 action/query/list/history 계약이 회귀 테스트로 고정된다.

### 검증
- action/query/list/history 회귀 테스트 실행 통과

### GitHub Issue
- 새 Issue 생성
- 권장 제목: `[BE] 목록 조회와 이력 조회 조합 안전망 고정`

## TASK-0002
### 이름
목록 row와 history 현재 조치 상태 요약 응답 모델 도입

### 목표
- list row와 history 응답이 최신 action 요약을 담을 수 있도록 응답 경계를 연다.

### 구현 범위
- list row에 아래 필드를 추가한다.
  - `latestAction`
  - `latestActionStatus`
  - `latestActionAppliedAt`
  - `historyAvailable`
- history 응답에 `currentActionState` 객체를 추가한다.
  - `latestAction`
  - `latestActionStatus`
  - `latestActionAppliedAt`
  - `availableActions`
- 이 단계는 응답 모델과 최소 경계 정의까지만 닫고, 실제 최신 action 계산은 다음 단계로 넘긴다.
- action audit가 없는 execution에서 `null`/`false`를 어떻게 반환할지 응답 계약을 고정한다.

### 관련 파일 후보
- `src/main/java/com/agilerunner/api/controller/review/response/ManualRerunExecutionListResponse.java`
- `src/main/java/com/agilerunner/api/controller/review/response/ManualRerunControlActionHistoryResponse.java`
- `src/main/java/com/agilerunner/api/service/review/response/ManualRerunExecutionListServiceResponse.java`
- `src/main/java/com/agilerunner/api/service/review/response/ManualRerunControlActionHistoryServiceResponse.java`
- `src/test/java/com/agilerunner/api/controller/review/ManualRerunControllerTest.java`
- `src/test/java/com/agilerunner/api/service/review/ManualRerunExecutionListServiceTest.java`
- `src/test/java/com/agilerunner/api/service/review/ManualRerunControlActionHistoryServiceTest.java`

### 비대상
- 최신 applied action 계산 로직
- representative actual app/H2 검증

### 연결 ValidationCriteria
- `manual-rerun-list-history-composition-response-defined`

### 완료 조건
- list row와 history 응답이 최신 action 요약 필드를 담을 수 있고, action audit가 없을 때의 `null`/`false` 의미도 계약으로 고정된다.

### 검증
- controller black-box 테스트
- service black-box 테스트

### GitHub Issue
- 새 Issue 생성
- 권장 제목: `[BE] 목록과 이력 조회 응답 모델에 현재 조치 상태 요약 추가`

## TASK-0003
### 이름
최신 관리자 액션 요약과 timeline 연결

### 목표
- list row와 history `currentActionState`가 최신 applied audit row 기준으로 계산되도록 연결한다.

### 구현 범위
- list row의 `latestAction`, `latestActionStatus`, `latestActionAppliedAt`, `historyAvailable`가 실제 audit timeline의 최신 applied row를 기준으로 계산되도록 연결한다.
- history `currentActionState`도 같은 최신 applied row를 기준으로 계산되도록 연결한다.
- 이 계산은 현재 history filter, sort, page window 결과가 아니라 execution 전체 audit timeline을 기준으로 수행한다.
- `currentActionState.availableActions`는 기존 정책 계산을 그대로 재사용한다.
- action audit가 없는 execution에서는 최신 action 요약이 `null`, `historyAvailable=false`로 유지되는지 고정한다.
- timeline `actions[]`와 현재 조치 상태 요약이 서로 다른 의미를 유지하는지 고정한다.

### 관련 파일 후보
- `src/main/java/com/agilerunner/api/service/review/ManualRerunExecutionListService.java`
- `src/main/java/com/agilerunner/api/service/review/ManualRerunControlActionHistoryService.java`
- `src/main/java/com/agilerunner/api/service/review/response/ManualRerunExecutionListServiceResponse.java`
- `src/main/java/com/agilerunner/api/service/review/response/ManualRerunControlActionHistoryServiceResponse.java`
- `src/main/java/com/agilerunner/client/agentruntime/AgentRuntimeRepository.java`
- `src/test/java/com/agilerunner/api/service/review/ManualRerunExecutionListServiceTest.java`
- `src/test/java/com/agilerunner/api/service/review/ManualRerunControlActionHistoryServiceTest.java`
- `src/test/java/com/agilerunner/client/agentruntime/AgentRuntimeRepositoryTest.java`

### 비대상
- representative actual app/H2 검증

### 연결 ValidationCriteria
- `manual-rerun-list-history-composition-maps-latest-action`

### 완료 조건
- list row와 history 현재 조치 상태 요약이 현재 history page 결과가 아니라 같은 execution 전체 audit timeline의 최신 applied audit row를 기준으로 계산된다.
- action audit가 없는 execution에서도 `null`/`false` 의미가 일관되게 유지된다.
- list는 현재 요약, history는 timeline이라는 의미가 계속 유지된다.

### 검증
- service/repository black-box 테스트
- controller black-box 테스트

### GitHub Issue
- 새 Issue 생성
- 권장 제목: `[BE] 최신 관리자 액션 요약과 이력 timeline 연결`

## TASK-0004
### 이름
목록 요약과 이력 timeline 실행 근거 정합성 검증

### 목표
- representative execution에서 list row, history 현재 조치 상태 요약, H2 audit evidence가 같은 execution 기준으로 같은 의미를 가지는지 확인한다.

### 구현 범위
- local profile 실제 앱/H2 representative 검증 수행
- representative 검증은 `실행 준비 -> 관리자 액션 실행 -> 목록 조회 -> history 조회 -> 앱 종료 -> H2 조회` 순서를 따른다.
- 같은 execution 기준으로 아래를 비교한다.
  - list row 최신 action 요약
  - history `currentActionState`
  - H2 audit timeline의 최신 applied row
- history filter/page 결과에 포함된 `actions[]`와 별개로, 비교 기준은 항상 execution 전체 latest applied audit row로 고정한다.
- representative 검증 대상은 applied audit row가 존재하는 execution을 기준으로 잡고, audit row가 없는 execution의 `null`/`false` 의미는 앞선 task의 테스트 근거로 유지한다.
- 정합성 검증에 꼭 필요한 최소 query/runtime 보정만 허용한다.

### 관련 파일 후보
- `src/main/java/com/agilerunner/api/controller/review/ManualRerunController.java`
- `src/main/java/com/agilerunner/api/service/review/ManualRerunExecutionListService.java`
- `src/main/java/com/agilerunner/api/service/review/ManualRerunControlActionHistoryService.java`
- `src/main/java/com/agilerunner/client/agentruntime/AgentRuntimeRepository.java`
- `src/test/java/com/agilerunner/api/controller/review/ManualRerunControllerTest.java`
- `src/test/java/com/agilerunner/api/service/review/ManualRerunExecutionListServiceTest.java`
- `src/test/java/com/agilerunner/api/service/review/ManualRerunControlActionHistoryServiceTest.java`

### 비대상
- 사용자용 UI
- bulk action
- 장기 저장소 도입

### 연결 ValidationCriteria
- `manual-rerun-list-history-composition-runtime-evidence-aligned`

### 완료 조건
- representative execution에서 list row 최신 action 요약, history `currentActionState`, H2 audit evidence가 같은 execution 기준으로 일치한다.
- targeted test, 전체 테스트, 실제 앱/H2 representative 검증이 모두 통과한다.

### 검증
- list/history 회귀 테스트
- 전체 테스트 실행
- 로컬 프로필 실제 앱 기동 후 representative action 실행 + list 조회 + history 조회 + H2 검증

### GitHub Issue
- 새 Issue 생성
- 권장 제목: `[BE] 목록 요약과 이력 timeline 실행 근거 정합성 검증`
