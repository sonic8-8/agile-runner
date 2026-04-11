# 현재 활성 Task 집합

## 문서 목적
이 문서는 현재 활성 `Spec`을 실제 구현 단위로 분해한 `Task` 문서다.
각 `Task`는 하나의 명확한 결과, 검증 기준, GitHub Issue 연결 규칙을 가져야 한다.

## 현재 활성 Spec
- ID: `SPEC-0015`
- 이름: `운영용 관리자 제어 액션 반복 정책 정교화`
- 기준 문서:
  - `.agents/active/spec.md`
  - `.agents/criteria/SPEC-0015-admin-control-repeat-policy.json`

## 공통 규칙
- 구현 순서는 `TASK-0001 -> TASK-0002 -> TASK-0003 -> TASK-0004`로 고정한다.
- 각 `Task` 시작 전 해당 task용 GitHub Issue를 새로 연결한다.
- 각 `Task`는 연결된 `ValidationCriteria`와 테스트 근거가 없으면 완료로 보지 않는다.
- 기존 `POST /reviews/rerun/{executionKey}/actions`, `GET /reviews/rerun/{executionKey}`, `GET /reviews/rerun/executions`, `GET /reviews/rerun/{executionKey}/actions/history` 계약은 유지한다.
- 이번 spec은 기존 관리자 액션 `ACKNOWLEDGE`, `UNACKNOWLEDGE`의 반복 적용 정책 정교화에만 집중한다.
- `TASK-0001` 시작 전 기존 action/query/list/history 안전망이 반복 정책 변경에도 충분한지 먼저 검토한다.

## 요약 표
| Task | 이름 | 핵심 목표 | 연결 ValidationCriteria | 핵심 검증 | Issue |
| --- | --- | --- | --- | --- | --- |
| `TASK-0001` | 관리자 액션 반복 정책 안전망 고정 | 반복 정책 정교화 전 기존 action/query/list/history 계약 유지 확인 | `manual-rerun-control-repeat-contract-preserved` | action/query/list/history 회귀 테스트 | 새 Issue |
| `TASK-0002` | 관리자 액션 반복 정책과 audit 저장 규칙 정리 | 반복 액션이 저장 단계에서 막히지 않도록 정책과 schema 정리 | `manual-rerun-control-repeat-policy-allows-reapply` | policy/service/repository black-box 테스트 | 새 Issue |
| `TASK-0003` | 반복 액션 이후 query/list/history 상태 연결 | 마지막 applied action 기준 현재 상태와 전체 timeline 연결 | `manual-rerun-control-repeat-history-reflects-latest-state` | query/list/history black-box 테스트 | 새 Issue |
| `TASK-0004` | 반복 액션과 실행 근거 정합성 검증 | representative execution에서 action/query/list/history/H2 정합성 확인 | `manual-rerun-control-repeat-runtime-evidence-aligned` | 실제 앱/H2 representative 검증 | 새 Issue |

## TASK-0001
### 이름
관리자 액션 반복 정책 안전망 고정

### 목표
- 반복 액션 정책을 정교화하기 전에 기존 action 응답, query/list 상태, history 응답 계약이 유지된다는 안전망을 먼저 고정한다.

### 구현 범위
- 기존 `ManualRerunControllerTest`, `ManualRerunControlActionServiceTest`, `ManualRerunQueryServiceTest`, `ManualRerunExecutionListServiceTest`, `ManualRerunControlActionHistoryServiceTest`, `ManualRerunAvailableActionPolicyTest`를 우선 재사용한다.
- 아래 기준을 먼저 확인한다.
  - action 응답 계약 유지
  - query/list의 현재 상태와 `availableActions` 의미 유지
  - history 응답 구조와 timeline 의미 유지
  - 마지막 applied action 기준 정책 해석 유지
- 기존 안전망이 충분하면 근거를 문서와 회고에 남기고, 부족한 경우만 새 테스트를 추가한다.

### 비대상
- schema 변경
- 반복 액션 저장 허용
- representative actual app/H2 검증

### 연결 ValidationCriteria
- `manual-rerun-control-repeat-contract-preserved`

### 완료 조건
- 반복 정책 정교화 전에도 기존 action/query/list/history 계약이 회귀 테스트로 고정된다.

### 검증
- action/query/list/history 회귀 테스트 실행 통과

### GitHub Issue
- 새 Issue 생성
- 권장 제목: `[BE] 관리자 액션 반복 정책 안전망 고정`

## TASK-0002
### 이름
관리자 액션 반복 정책과 audit 저장 규칙 정리

### 목표
- `ACKNOWLEDGE -> UNACKNOWLEDGE -> ACKNOWLEDGE` 흐름이 저장 단계에서 막히지 않도록 정책과 물리 스키마를 정리한다.

### 구현 범위
- 같은 execution에서 기존 관리자 액션을 반복 저장할 수 있도록 audit 저장 규칙을 정리한다.
- 마지막 applied action 기준 정책은 유지하되, 반대 액션 이후 같은 액션 재적용이 허용되도록 연결한다.
- service/repository/policy black-box 테스트로 반복 액션 허용 기준을 고정한다.

### 관련 파일 후보
- `src/main/java/com/agilerunner/domain/review/ManualRerunAvailableActionPolicy.java`
- `src/main/java/com/agilerunner/api/service/review/ManualRerunControlActionService.java`
- `src/main/java/com/agilerunner/client/agentruntime/AgentRuntimeRepository.java`
- `src/main/resources/agent-runtime/schema.sql`
- `src/test/java/com/agilerunner/domain/review/ManualRerunAvailableActionPolicyTest.java`
- `src/test/java/com/agilerunner/api/service/review/ManualRerunControlActionServiceTest.java`
- `src/test/java/com/agilerunner/client/agentruntime/AgentRuntimeRepositoryTest.java`

### 비대상
- query/list/history 응답 의미 보강
- representative actual app/H2 검증

### 연결 ValidationCriteria
- `manual-rerun-control-repeat-policy-allows-reapply`

### 완료 조건
- 반대 액션 이후 같은 액션 재적용이 저장 단계에서 막히지 않는다.
- 마지막 applied action 기준으로 허용/비허용 정책이 일관된다.

### 검증
- policy black-box 테스트
- service black-box 테스트
- repository/H2 mem 저장 테스트

### GitHub Issue
- 새 Issue 생성
- 권장 제목: `[BE] 관리자 액션 반복 정책과 저장 규칙 정리`

## TASK-0003
### 이름
반복 액션 이후 query/list/history 상태 연결

### 목표
- query/list는 마지막 applied action 기준 현재 상태를 보여주고, history는 반복 액션 전체 timeline을 시간 순서대로 반환하도록 연결한다.

### 구현 범위
- query/list가 마지막 applied action 기준 `availableActions`를 계산하는지 고정한다.
- history 응답이 반복 액션 전체 row를 순서대로 반환하는지 고정한다.
- 반복 액션 이후에도 action 응답, query/list, history가 서로 다른 역할을 유지하는지 검증한다.

### 관련 파일 후보
- `src/main/java/com/agilerunner/api/service/review/ManualRerunQueryService.java`
- `src/main/java/com/agilerunner/api/service/review/ManualRerunExecutionListService.java`
- `src/main/java/com/agilerunner/api/service/review/ManualRerunControlActionHistoryService.java`
- `src/test/java/com/agilerunner/api/service/review/ManualRerunQueryServiceTest.java`
- `src/test/java/com/agilerunner/api/service/review/ManualRerunExecutionListServiceTest.java`
- `src/test/java/com/agilerunner/api/service/review/ManualRerunControlActionHistoryServiceTest.java`
- `src/test/java/com/agilerunner/api/controller/review/ManualRerunControllerTest.java`

### 비대상
- 새 관리자 액션 추가
- representative actual app/H2 검증

### 연결 ValidationCriteria
- `manual-rerun-control-repeat-history-reflects-latest-state`

### 완료 조건
- query/list는 마지막 applied action 기준 현재 상태를 보여주고, history는 반복 액션 전체 timeline을 순서대로 반환한다.
- 반복 액션 이후에도 `availableActions`와 history 의미가 충돌하지 않는다.

### 검증
- controller black-box 테스트
- query/list/history service black-box 테스트

### GitHub Issue
- 새 Issue 생성
- 권장 제목: `[BE] 반복 액션 이후 현재 상태와 이력 연결`

## TASK-0004
### 이름
반복 액션과 실행 근거 정합성 검증

### 목표
- representative execution에서 `ACKNOWLEDGE -> UNACKNOWLEDGE -> ACKNOWLEDGE` 흐름을 실제로 수행하고, action 응답, query/list, history, H2 audit evidence가 같은 execution 기준으로 같은 의미를 가지는지 확인한다.

### 구현 범위
- local profile 실제 앱/H2 representative 검증 수행
- representative 검증은 `실행 준비 -> ACKNOWLEDGE -> UNACKNOWLEDGE -> ACKNOWLEDGE -> history 조회 -> query/list 조회 -> 앱 종료 -> H2 조회` 순서를 따른다.
- 정합성 검증에 꼭 필요한 최소 query/runtime 보정만 허용한다.

### 관련 파일 후보
- `src/main/java/com/agilerunner/api/controller/review/ManualRerunController.java`
- `src/main/java/com/agilerunner/api/service/review/ManualRerunControlActionService.java`
- `src/main/java/com/agilerunner/api/service/review/ManualRerunControlActionHistoryService.java`
- `src/main/java/com/agilerunner/api/service/review/ManualRerunQueryService.java`
- `src/main/java/com/agilerunner/api/service/review/ManualRerunExecutionListService.java`
- `src/main/java/com/agilerunner/client/agentruntime/AgentRuntimeRepository.java`

### 비대상
- 사용자용 UI
- bulk action
- 장기 저장소 도입

### 연결 ValidationCriteria
- `manual-rerun-control-repeat-runtime-evidence-aligned`

### 완료 조건
- representative execution에서 action 응답, history 응답, query/list 상태, H2 audit evidence가 같은 execution 기준으로 일치한다.
- targeted test, 전체 테스트, 실제 앱/H2 representative 검증이 모두 통과한다.

### 검증
- action/history/query/list 회귀 테스트
- 전체 테스트 실행
- 로컬 프로필 실제 앱 기동 후 반복 액션 실행 + history/query/list 조회 + H2 검증

### GitHub Issue
- 새 Issue 생성
- 권장 제목: `[BE] 반복 액션과 실행 근거 정합성 검증`
