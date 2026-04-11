# 현재 활성 Spec

## 문서 목적
이 문서는 현재 활성 구현 범위와 후속 구현 범위를 고정하는 `Spec` 문서다.
`ValidationCriteria`와 `Task`는 이 문서의 활성 spec을 기준으로 작성하며, GitHub Issue는 각 `Task`를 외부에서 추적하기 위한 수단으로만 사용한다.

## 현재 활성 Spec
### ID
SPEC-0020

### 이름
운영용 조회 응답 문서 기준 정리

### 목표
- 운영자가 `query`, `list`, `history`, `action` 응답을 읽을 때 각 응답이 어떤 질문에 답하는지 한 번에 이해할 수 있게 문서 기준을 정리한다.
- 같은 execution을 다루는 여러 응답이 현재 상태, 과거 이력, 방금 수행한 조치 결과를 각각 어디까지 책임지는지 분명하게 구분한다.
- 실제 앱 응답과 문서 예시가 어긋나지 않도록 representative 검증으로 문서 기준을 고정한다.

### 대상 문제
- 현재 운영 API는 기능 자체는 갖춰졌지만, `GET /reviews/rerun/{executionKey}`, `GET /reviews/rerun/executions`, `GET /reviews/rerun/{executionKey}/actions/history`, `POST /reviews/rerun/{executionKey}/actions`가 각각 어떤 질문에 답하는지 문서로 바로 읽기 어렵다.
- 비슷한 필드가 여러 응답에 반복되면서 운영자가 `현재 상태 요약`, `과거 timeline`, `방금 수행한 액션 결과`를 다시 해석해야 한다.
- 지금까지는 테스트와 representative 검증으로 계약을 고정해 왔지만, 운영자 관점의 설명 문서와 응답 예시는 충분히 정리되지 않았다.

### 범위
- `docs/manual-rerun-response-guide.md` 문서를 새로 만들고 아래 응답의 역할을 정리한다.
  - `POST /reviews/rerun`
  - `POST /reviews/rerun/{executionKey}/retry`
  - `GET /reviews/rerun/{executionKey}`
  - `GET /reviews/rerun/executions`
  - `GET /reviews/rerun/{executionKey}/actions/history`
  - `POST /reviews/rerun/{executionKey}/actions`
- 각 응답이 답하는 질문을 아래 축으로 구분한다.
  - 실행 시작 결과
  - 단건 현재 상태 조회
  - 여러 실행 목록 요약
  - 관리자 액션 이력 timeline
  - 방금 수행한 관리자 액션 결과
- 응답별 핵심 필드 의미를 정리한다.
  - `executionKey`
  - `executionStatus`
  - `failureDisposition`
  - `availableActions`
  - `latestAction`
  - `currentActionState`
  - `actions[]`
  - `actionStatus`
- 응답 간 중복 요약 필드의 기준을 정리한다.
  - `query`는 단건 현재 상태
  - `list`는 여러 execution의 현재 상태 요약
  - `history`는 과거 관리자 액션 timeline과 현재 조치 상태 요약
  - `action`은 방금 수행한 관리자 액션의 결과
- representative actual app 검증으로 문서 예시와 실제 응답이 같은 의미를 가지는지 확인한다.

### 비대상
- 새 관리자 액션 추가
- 응답 필드 rename
- 사용자용 UI
- bulk action
- 장기 저장소 도입
- OpenAPI 또는 Swagger 도입

### 외부 계약
- 기존 rerun/query/list/history/action 경로와 HTTP status 계약은 유지한다.
- 기존 응답 필드 의미는 바꾸지 않고, 문서 설명과 예시를 추가하는 방식으로 정리한다.
- representative 검증은 같은 execution을 기준으로 query, list, history, action 응답을 비교한다.
- 문서 예시는 실제 representative 응답에서 검증된 값 구조를 기준으로 작성한다.

### 핵심 시나리오
1. 조회 응답 문서 기준 안전망 고정
   - 기존 query/list/history/action 계약이 이미 충분히 테스트로 고정돼 있는지 먼저 확인한다.
   - 안전망이 충분하면 근거만 남기고, 부족할 때만 최소 테스트를 추가한다.
2. 응답 역할 구분과 문서 골격 도입
   - 운영자가 “어느 응답을 언제 봐야 하는지” 이해할 수 있도록 rerun/retry/query/list/history/action 역할 구분 표와 문서 골격을 만든다.
   - 이 단계는 문서 구조와 역할 설명까지만 닫고, 예시 세부값과 중복 필드 기준은 다음 단계로 넘긴다.
3. 응답 예시와 필드 의미, 중복 요약 기준 정리
   - 실제 DTO와 현재 계약을 기준으로 rerun/retry/query/list/history/action 응답 예시와 필드 의미를 채운다.
   - `rerun`, `retry`, `query`, `list`, `history`, `action` 사이에서 같은 execution을 어떻게 읽어야 하는지 중복 요약 기준을 분명하게 적는다.
4. 문서 기준과 실제 응답 정합성 검증
   - representative actual app 실행으로 rerun execution 1건과 retry execution 1건을 분리해 검증한다.
   - rerun execution은 `rerun` 응답, `query`, `list`, `history`, `action` 응답 비교 기준으로 사용한다.
   - retry execution은 `retry` 응답과 `retrySourceExecutionKey` 의미 검증 기준으로 사용한다.

### Task 분해 기준
- `TASK-0001` 조회 응답 문서 기준 안전망 고정
- `TASK-0002` 응답 역할 구분과 문서 골격 도입
- `TASK-0003` 응답 예시와 필드 의미, 중복 요약 기준 정리
- `TASK-0004` 문서 기준과 실제 응답 정합성 검증

### 연결될 ValidationCriteria
- `manual-rerun-response-doc-contract-preserved`
- `manual-rerun-response-doc-role-defined`
- `manual-rerun-response-doc-field-meaning-defined`
- `manual-rerun-response-doc-runtime-aligned`

### 필수 테스트 시나리오
- 문서 기준 정리 중에도 기존 rerun/query/list/history/action 계약은 유지된다.
- 문서가 query/list/history/action 응답의 역할을 서로 겹치지 않게 설명한다.
- 문서가 rerun/retry/query/list/history/action 응답의 역할을 서로 겹치지 않게 설명한다.
- 문서 예시와 필드 의미가 현재 DTO/응답 계약과 모순되지 않는다.
- representative actual app 응답이 문서 예시와 같은 해석 기준을 가진다.

## 후속 Spec 후보
### ID
SPEC-0021

### 이름
운영용 조회 응답 예시 자동 검증

### 시작 조건
- `현재 활성 Spec`이 완료되고, 운영용 조회 응답 문서가 실제 응답 의미와 맞게 정리된 뒤 시작한다.

### 목표
- 운영용 응답 문서의 예시가 이후 변경에서도 자동으로 깨지지 않도록 검증 기반을 추가한다.

### 후속 변경 범위
- 문서 예시와 테스트 fixture 연결
- representative 응답 스냅샷 또는 예시 자동 검증 기반 검토
- 문서 예시 drift 감지 기준 정리

### 후속 변경 비대상
- 사용자용 UI
- bulk action
- 장기 저장소 도입

### 후속 검증 방향
- 문서 예시와 실제 응답이 장기적으로 어긋나지 않는다.
