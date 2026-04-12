# 현재 활성 Task 집합

## 문서 목적
이 문서는 현재 활성 `Spec`을 실제 구현 단위로 분해한 `Task` 문서다.
각 `Task`는 하나의 명확한 결과, 검증 기준, GitHub Issue 연결 규칙을 가져야 한다.

## 현재 활성 Spec
- ID: `SPEC-0021`
- 이름: `운영용 조회 응답 예시 자동 검증`
- 기준 문서:
  - `.agents/active/spec.md`
  - `.agents/criteria/SPEC-0021-manual-rerun-response-example-validation.json`

## 공통 규칙
- 구현 순서는 `TASK-0001 -> TASK-0002 -> TASK-0003 -> TASK-0004`로 고정한다.
- 각 `Task` 시작 전 해당 task용 GitHub Issue를 새로 연결한다.
- 각 `Task`는 연결된 `ValidationCriteria`와 테스트 근거가 없으면 완료로 보지 않는다.
- 기존 `POST /reviews/rerun`, `POST /reviews/rerun/{executionKey}/retry`, `GET /reviews/rerun/{executionKey}`, `GET /reviews/rerun/executions`, `GET /reviews/rerun/{executionKey}/actions/history`, `POST /reviews/rerun/{executionKey}/actions` 계약은 유지한다.
- 이번 spec은 운영용 조회 응답 가이드 예시를 자동 검증 대상으로 연결하는 데 집중한다.
- `TASK-0001` 시작 전 기존 controller/service black-box 테스트가 이번 spec의 safety-net으로 충분한지 먼저 검토한다.

## 요약 표
| Task | 이름 | 핵심 목표 | 연결 ValidationCriteria | 핵심 검증 | Issue |
| --- | --- | --- | --- | --- | --- |
| `TASK-0001` | 예시 자동 검증 안전망 고정 | 기존 조회 응답 계약 유지 확인 | `manual-rerun-response-example-contract-preserved` | 회귀 테스트 | 새 Issue |
| `TASK-0002` | 예시 fixture와 문서 매핑 구조 도입 | guide 예시와 검증 source 연결 구조 정의 | `manual-rerun-response-example-source-defined` | 문서 리뷰 + 테스트 구조 확인 | 새 Issue |
| `TASK-0003` | 예시 자동 검증 테스트 도입 | rerun/retry/query/list/history/action 예시 drift를 자동 검출하는 테스트 추가 | `manual-rerun-response-example-tests-defined` | black-box + fixture 검증 테스트 | 새 Issue |
| `TASK-0004` | drift 검증과 문서 마감 | 예시 drift가 targeted/full test에서 실제로 잡히는지 확인하고 문서 마감 | `manual-rerun-response-example-drift-detected` | targeted/full test + 문서 검토 | 새 Issue |

## TASK-0001
### 이름
예시 자동 검증 안전망 고정

### 목표
- 예시 자동 검증 spec을 시작하기 전에 기존 rerun/query/list/history/action 계약이 이미 충분히 테스트로 고정돼 있는지 확인한다.

### 구현 범위
- 기존 `ManualRerunControllerTest`, `ManualRerunServiceTest`, `ManualRerunRetryServiceTest`, `ManualRerunQueryServiceTest`, `ManualRerunExecutionListServiceTest`, `ManualRerunControlActionHistoryServiceTest`, `ManualRerunControlActionServiceTest`를 우선 재사용한다.
- 아래 기준을 먼저 확인한다.
  - rerun/retry 응답 기본 계약 유지
  - query 단건 상태 의미 유지
  - list 목록 요약 의미 유지
  - history timeline 의미 유지
  - action 결과 응답 의미 유지
- 기존 안전망이 충분하면 근거를 회고에 남기고, 부족한 경우만 최소 테스트를 추가한다.

### 비대상
- fixture 파일 추가
- guide 문서 구조 변경
- 예시 자동 검증 테스트 추가

### 연결 ValidationCriteria
- `manual-rerun-response-example-contract-preserved`

### 완료 조건
- 예시 자동 검증을 도입하기 전에도 기존 rerun/query/list/history/action 계약이 회귀 테스트로 고정된다.

### 검증
- rerun/query/list/history/action 회귀 테스트 실행 통과

### GitHub Issue
- 새 Issue 생성
- 권장 제목: `[BE] 예시 자동 검증 안전망 고정`

## TASK-0002
### 이름
예시 fixture와 문서 매핑 구조 도입

### 목표
- guide 문서의 각 예시가 어떤 fixture 또는 검증 source와 연결되는지 먼저 구조적으로 정리한다.

### 구현 범위
- guide 문서와 테스트 자산이 만나는 위치를 정의한다.
- 예시 자동 검증 source를 둘 위치를 정한다.
  - 예: `src/test/resources/...`
  - 또는 예시 전용 fixture helper
- guide 문서에 아래 연결 기준을 명시한다.
  - rerun 예시는 어떤 source를 기준으로 유지되는가
  - retry 예시는 어떤 source를 기준으로 유지되는가
  - query/list/history/action 예시는 어떤 source를 기준으로 유지되는가
- 이 단계는 구조와 연결 규칙만 닫고, 실제 drift 검출 테스트는 다음 단계로 넘긴다.

### 관련 파일 후보
- `docs/manual-rerun-response-guide.md`
- `src/test/java/com/agilerunner/api/controller/review/ManualRerunControllerTest.java`
- `src/test/java/com/agilerunner/api/service/review/ManualRerunServiceTest.java`
- `src/test/java/com/agilerunner/api/service/review/ManualRerunRetryServiceTest.java`
- `src/test/java/com/agilerunner/api/service/review/ManualRerunQueryServiceTest.java`
- `src/test/java/com/agilerunner/api/service/review/ManualRerunExecutionListServiceTest.java`
- `src/test/java/com/agilerunner/api/service/review/ManualRerunControlActionHistoryServiceTest.java`
- `src/test/java/com/agilerunner/api/service/review/ManualRerunControlActionServiceTest.java`
- `src/test/resources/`

### 비대상
- 실제 drift 검출 assertion 추가
- representative actual app 검증

### 연결 ValidationCriteria
- `manual-rerun-response-example-source-defined`

### 완료 조건
- guide 문서만 읽어도 각 예시가 어떤 fixture 또는 검증 source와 연결되는지 찾을 수 있다.

### 검증
- 문서 리뷰
- 테스트 구조 리뷰

### GitHub Issue
- 새 Issue 생성
- 권장 제목: `[BE] 예시 fixture와 문서 매핑 구조 도입`

## TASK-0003
### 이름
예시 자동 검증 테스트 도입

### 목표
- 운영용 조회 응답 예시가 현재 DTO/응답 계약과 달라지면 자동으로 실패하는 테스트를 추가한다.

### 구현 범위
- rerun, retry, query, list, history, action 예시를 fixture 또는 expected payload로 관리한다.
- fixture와 controller/service black-box 기대값을 비교하는 테스트를 추가한다.
- 같은 execution을 서로 다른 시점으로 읽는 예시는 fixture 이름이나 검증 섹션에서 구분한다.
- drift를 유발하는 대표 필드를 최소한 아래까지 잡는다.
  - `executionKey`
  - `retrySourceExecutionKey`
  - `executionStatus`
  - `failureDisposition`
  - `availableActions`
  - `latestAction*`
  - `currentActionState`
  - `actionStatus`

### 관련 파일 후보
- `docs/manual-rerun-response-guide.md`
- `src/test/java/com/agilerunner/api/controller/review/ManualRerunControllerTest.java`
- `src/test/java/com/agilerunner/api/service/review/ManualRerunServiceTest.java`
- `src/test/java/com/agilerunner/api/service/review/ManualRerunRetryServiceTest.java`
- `src/test/java/com/agilerunner/api/service/review/ManualRerunQueryServiceTest.java`
- `src/test/java/com/agilerunner/api/service/review/ManualRerunExecutionListServiceTest.java`
- `src/test/java/com/agilerunner/api/service/review/ManualRerunControlActionHistoryServiceTest.java`
- `src/test/java/com/agilerunner/api/service/review/ManualRerunControlActionServiceTest.java`
- `src/test/resources/`

### 비대상
- 새 응답 필드 추가
- representative actual app 검증

### 연결 ValidationCriteria
- `manual-rerun-response-example-tests-defined`

### 완료 조건
- guide 예시와 현재 DTO/응답 계약이 어긋나면 자동 검증 테스트가 실패한다.

### 검증
- controller/service black-box + fixture 비교 테스트

### GitHub Issue
- 새 Issue 생성
- 권장 제목: `[BE] 운영용 조회 응답 예시 자동 검증 추가`

## TASK-0004
### 이름
drift 검증과 문서 마감

### 목표
- 예시 자동 검증 테스트가 실제로 drift를 조기에 잡는지 확인하고, guide와 회고를 마감한다.

### 구현 범위
- guide 문서와 fixture, 자동 검증 테스트를 함께 검토한다.
- targeted test와 full `cleanTest test`를 돌려 예시 자동 검증이 기존 회귀 흐름 안에서 동작하는지 확인한다.
- 필요하면 guide 문구와 fixture 표현을 최소 보정한다.
- 이번 spec에서 실제 앱 representative 검증은 비대상으로 둔다.
  - 이유: `SPEC-0020`에서 이미 representative actual app 정합성을 닫았고, 이번 spec은 그 결과를 자동 검증 기반으로 고정하는 단계이기 때문이다.
  - 따라서 이번 task는 docs/test drift 보호를 닫는 단계로 해석하고, actual app/H2 representative 검증 생략 사유를 retrospective와 spec summary에 함께 남긴다.

### 관련 파일 후보
- `docs/manual-rerun-response-guide.md`
- `src/test/java/com/agilerunner/api/controller/review/ManualRerunControllerTest.java`
- `src/test/java/com/agilerunner/api/service/review/ManualRerunServiceTest.java`
- `src/test/java/com/agilerunner/api/service/review/ManualRerunRetryServiceTest.java`
- `src/test/java/com/agilerunner/api/service/review/ManualRerunQueryServiceTest.java`
- `src/test/java/com/agilerunner/api/service/review/ManualRerunExecutionListServiceTest.java`
- `src/test/java/com/agilerunner/api/service/review/ManualRerunControlActionHistoryServiceTest.java`
- `src/test/java/com/agilerunner/api/service/review/ManualRerunControlActionServiceTest.java`
- `src/test/resources/`

### 비대상
- 새 endpoint 추가
- 실제 앱 representative 재검증

### 연결 ValidationCriteria
- `manual-rerun-response-example-drift-detected`

### 완료 조건
- 문서 예시와 fixture가 current DTO/응답 계약과 어긋나면 targeted test 또는 full `cleanTest test`에서 조기에 드러난다.
- guide와 fixture, 자동 검증 테스트 정리 결과가 retrospective와 spec summary에 남는다.

### 검증
- targeted test
- full `cleanTest test`
- 문서 검토

### GitHub Issue
- 새 Issue 생성
- 권장 제목: `[BE] 운영용 조회 응답 예시 drift 검증과 마감`
