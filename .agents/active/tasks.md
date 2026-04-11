# 현재 활성 Task 집합

## 문서 목적
이 문서는 현재 활성 `Spec`을 실제 구현 단위로 분해한 `Task` 문서다.
각 `Task`는 하나의 명확한 결과, 검증 기준, GitHub Issue 연결 규칙을 가져야 한다.

## 현재 활성 Spec
- ID: `SPEC-0020`
- 이름: `운영용 조회 응답 문서 기준 정리`
- 기준 문서:
  - `.agents/active/spec.md`
  - `.agents/criteria/SPEC-0020-manual-rerun-response-documentation.json`

## 공통 규칙
- 구현 순서는 `TASK-0001 -> TASK-0002 -> TASK-0003 -> TASK-0004`로 고정한다.
- 각 `Task` 시작 전 해당 task용 GitHub Issue를 새로 연결한다.
- 각 `Task`는 연결된 `ValidationCriteria`와 테스트 근거가 없으면 완료로 보지 않는다.
- 기존 `POST /reviews/rerun`, `POST /reviews/rerun/{executionKey}/retry`, `GET /reviews/rerun/{executionKey}`, `GET /reviews/rerun/executions`, `GET /reviews/rerun/{executionKey}/actions/history`, `POST /reviews/rerun/{executionKey}/actions` 계약은 유지한다.
- 이번 spec은 새 기능 추가보다 운영용 응답 문서와 예시 기준 정리에 집중한다.
- `TASK-0001` 시작 전 기존 rerun/query/list/history/action 안전망이 이번 문서 기준 정리에도 충분한지 먼저 검토한다.

## 요약 표
| Task | 이름 | 핵심 목표 | 연결 ValidationCriteria | 핵심 검증 | Issue |
| --- | --- | --- | --- | --- | --- |
| `TASK-0001` | 조회 응답 문서 기준 안전망 고정 | 기존 rerun/query/list/history/action 계약 유지 확인 | `manual-rerun-response-doc-contract-preserved` | 회귀 테스트 | 새 Issue |
| `TASK-0002` | 응답 역할 구분과 문서 골격 도입 | rerun/retry/query/list/history/action 응답이 답하는 질문과 문서 구조 정의 | `manual-rerun-response-doc-role-defined` | 문서 리뷰 + controller/service black-box 확인 | 새 Issue |
| `TASK-0003` | 응답 예시와 필드 의미, 중복 요약 기준 정리 | 문서 예시와 필드 의미를 실제 DTO/응답 기준으로 채우고 중복 요약 기준 고정 | `manual-rerun-response-doc-field-meaning-defined` | 문서 리뷰 + controller/service black-box 확인 | 새 Issue |
| `TASK-0004` | 문서 기준과 실제 응답 정합성 검증 | representative rerun 1건, retry 1건과 query/list/history/action 응답 정합성 확인 | `manual-rerun-response-doc-runtime-aligned` | 실제 앱 representative 검증 | 새 Issue |

## TASK-0001
### 이름
조회 응답 문서 기준 안전망 고정

### 목표
- 문서 기준을 정리하기 전에 기존 rerun/query/list/history/action 계약이 충분히 테스트로 고정돼 있는지 먼저 확인한다.

### 구현 범위
- 기존 `ManualRerunControllerTest`, `ManualRerunServiceTest`, `ManualRerunRetryServiceTest`, `ManualRerunQueryServiceTest`, `ManualRerunExecutionListServiceTest`, `ManualRerunControlActionHistoryServiceTest`, `ManualRerunControlActionServiceTest`를 우선 재사용한다.
- 아래 기준을 먼저 확인한다.
  - rerun/retry 응답 기본 계약 유지
  - query 단건 상태 의미 유지
  - list 목록 요약 의미 유지
  - history timeline 의미 유지
  - action 결과 응답 의미 유지
- 기존 안전망이 충분하면 근거를 문서와 회고에 남기고, 부족한 경우만 새 테스트를 추가한다.

### 비대상
- 새 문서 파일 생성
- 응답 예시 작성
- representative actual app 검증

### 연결 ValidationCriteria
- `manual-rerun-response-doc-contract-preserved`

### 완료 조건
- 문서 기준 정리 전에도 기존 rerun/query/list/history/action 계약이 회귀 테스트로 고정된다.

### 검증
- rerun/query/list/history/action 회귀 테스트 실행 통과

### GitHub Issue
- 새 Issue 생성
- 권장 제목: `[BE] 조회 응답 문서 기준 안전망 고정`

## TASK-0002
### 이름
응답 역할 구분과 문서 골격 도입

### 목표
- 운영자가 rerun/retry/query/list/history/action 응답을 어떤 상황에서 읽어야 하는지 문서 구조와 역할 설명으로 먼저 고정한다.

### 구현 범위
- `docs/manual-rerun-response-guide.md` 문서 골격을 추가한다.
- 최소한 아래 섹션을 포함한다.
  - rerun/retry 시작 응답 역할
  - 응답별 질문
  - endpoint별 역할
  - 같은 execution을 읽는 기본 순서
  - 응답 간 겹치지 않는 책임
- `rerun`, `retry`, `query`, `list`, `history`, `action` 각각이 답하는 질문을 한 줄 설명으로 고정한다.
- 이 단계는 문서 구조와 역할 정의까지만 닫고, 예시 JSON과 세부 필드 의미는 다음 단계로 넘긴다.

### 관련 파일 후보
- `docs/manual-rerun-response-guide.md`
- `src/main/java/com/agilerunner/api/controller/review/ManualRerunController.java`
- `src/main/java/com/agilerunner/api/controller/review/response/ManualRerunResponse.java`
- `src/main/java/com/agilerunner/api/controller/review/response/ManualRerunRetryResponse.java`
- `src/main/java/com/agilerunner/api/controller/review/response/ManualRerunQueryResponse.java`
- `src/main/java/com/agilerunner/api/controller/review/response/ManualRerunExecutionListResponse.java`
- `src/main/java/com/agilerunner/api/controller/review/response/ManualRerunControlActionHistoryResponse.java`
- `src/main/java/com/agilerunner/api/controller/review/response/ManualRerunControlActionResponse.java`
- `src/test/java/com/agilerunner/api/controller/review/ManualRerunControllerTest.java`

### 비대상
- 예시 JSON 세부값 작성
- representative actual app 검증

### 연결 ValidationCriteria
- `manual-rerun-response-doc-role-defined`

### 완료 조건
- 문서가 rerun/retry/query/list/history/action 응답의 역할과 읽는 순서를 서로 겹치지 않게 설명한다.

### 검증
- 문서 리뷰
- controller/service black-box 확인

### GitHub Issue
- 새 Issue 생성
- 권장 제목: `[BE] 운영용 조회 응답 역할과 문서 골격 정리`

## TASK-0003
### 이름
응답 예시와 필드 의미, 중복 요약 기준 정리

### 목표
- 문서에 실제 DTO/응답 기준 예시와 필드 의미를 채우고, 여러 응답 사이에서 중복 요약 필드를 어떻게 읽어야 하는지 명확히 정리한다.

### 구현 범위
- `docs/manual-rerun-response-guide.md`에 각 응답 예시를 추가한다.
- 아래 필드 의미를 명시한다.
- `executionKey`
- `retrySourceExecutionKey`
- `executionStatus`
- `failureDisposition`
- `availableActions`
- `latestAction`
- `currentActionState`
- `actions[]`
- `actionStatus`
- 같은 execution 기준으로 아래 중복 요약 기준을 정리한다.
  - `rerun`은 새 실행 시작 결과
  - `retry`는 재시도 시작 결과와 원본 실행 연결
  - `query`는 단건 현재 상태
  - `list`는 여러 execution의 현재 상태 요약
  - `history.currentActionState`는 현재 조치 상태 요약
  - `history.actions[]`는 과거 timeline
  - `action` 응답은 방금 수행한 조치 결과
- 예시와 설명은 현재 DTO/서비스 응답 구조와 모순되지 않게 맞춘다.

### 관련 파일 후보
- `docs/manual-rerun-response-guide.md`
- `src/main/java/com/agilerunner/api/controller/review/response/ManualRerunResponse.java`
- `src/main/java/com/agilerunner/api/controller/review/response/ManualRerunRetryResponse.java`
- `src/main/java/com/agilerunner/api/controller/review/response/ManualRerunQueryResponse.java`
- `src/main/java/com/agilerunner/api/controller/review/response/ManualRerunExecutionListResponse.java`
- `src/main/java/com/agilerunner/api/controller/review/response/ManualRerunControlActionHistoryResponse.java`
- `src/main/java/com/agilerunner/api/controller/review/response/ManualRerunControlActionResponse.java`
- `src/test/java/com/agilerunner/api/controller/review/ManualRerunControllerTest.java`

### 비대상
- 새 응답 필드 추가
- representative actual app 검증

### 연결 ValidationCriteria
- `manual-rerun-response-doc-field-meaning-defined`

### 완료 조건
- 문서 예시와 필드 의미가 현재 DTO/응답 계약과 모순되지 않고, rerun/retry/query/list/history/action 사이의 중복 요약 기준이 읽기 쉽게 정리된다.

### 검증
- 문서 리뷰
- controller/service black-box 확인

### GitHub Issue
- 새 Issue 생성
- 권장 제목: `[BE] 운영용 조회 응답 예시와 필드 의미 정리`

## TASK-0004
### 이름
문서 기준과 실제 응답 정합성 검증

### 목표
- representative actual app 실행으로 문서 예시와 실제 응답 의미가 같은 execution 기준으로 맞는지 확인한다.

### 구현 범위
- local profile 실제 앱을 기동한다.
- representative rerun execution 1건과 representative retry execution 1건을 따로 준비한다.
- representative rerun execution으로 아래 응답을 실제로 확인한다.
  - rerun 응답 1건
  - query 응답
  - list 응답
  - history 응답
  - action 응답
- representative retry execution으로 아래 응답을 실제로 확인한다.
  - retry 응답 1건
- representative retry execution은 `retrySourceExecutionKey`가 원본 execution과 올바르게 연결되는지 확인하는 기준으로 사용한다.
- `docs/manual-rerun-response-guide.md`의 예시와 설명이 representative 응답과 같은 의미를 가지는지 확인한다.
- 필요하면 문서 예시의 세부값, 설명 문구, 비교 순서만 최소 보정한다.

### 관련 파일 후보
- `docs/manual-rerun-response-guide.md`
- `src/main/java/com/agilerunner/api/controller/review/ManualRerunController.java`
- `src/test/java/com/agilerunner/api/controller/review/ManualRerunControllerTest.java`
- `src/test/java/com/agilerunner/api/service/review/ManualRerunQueryServiceTest.java`
- `src/test/java/com/agilerunner/api/service/review/ManualRerunExecutionListServiceTest.java`
- `src/test/java/com/agilerunner/api/service/review/ManualRerunControlActionHistoryServiceTest.java`
- `src/test/java/com/agilerunner/api/service/review/ManualRerunControlActionServiceTest.java`

### 비대상
- 사용자용 UI
- bulk action
- 장기 저장소 도입

### 연결 ValidationCriteria
- `manual-rerun-response-doc-runtime-aligned`

### 완료 조건
- representative actual app의 rerun execution과 retry execution이 문서 예시/설명과 같은 의미를 가지고, rerun 응답은 query/list/history/action과, retry 응답은 `retrySourceExecutionKey` 설명과 서로 모순되지 않는다.
- targeted test, 전체 테스트, representative actual app 검증이 모두 통과한다.

### 검증
- rerun/query/list/history/action 회귀 테스트
- 전체 테스트 실행
- representative actual app 응답 비교

### GitHub Issue
- 새 Issue 생성
- 권장 제목: `[BE] 운영용 조회 응답 문서와 실제 응답 정합성 검증`
