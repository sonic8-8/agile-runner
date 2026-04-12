# 현재 활성 Spec

## 문서 목적
이 문서는 현재 활성 구현 범위와 후속 구현 범위를 고정하는 `Spec` 문서다.
`ValidationCriteria`와 `Task`는 이 문서의 활성 spec을 기준으로 작성하며, GitHub Issue는 각 `Task`를 외부에서 추적하기 위한 수단으로만 사용한다.

## 현재 활성 Spec
### ID
SPEC-0021

### 이름
운영용 조회 응답 예시 자동 검증

### 목표
- 운영용 조회 응답 가이드의 예시가 이후 코드 변경으로 틀어질 때 테스트에서 바로 드러나게 한다.
- `rerun`, `retry`, `query`, `list`, `history`, `action` 응답 예시를 fixture와 검증 기준에 연결해 수동 대표 검증 의존도를 줄인다.
- 문서 예시와 현재 DTO/응답 계약 사이의 drift를 사람이 뒤늦게 발견하지 않도록 자동 검증 기반을 마련한다.

### 대상 문제
- 지금은 [manual-rerun-response-guide.md](/home/seaung13/workspace/agile-runner/docs/manual-rerun-response-guide.md)에 역할과 예시가 정리돼 있지만, 이후 응답 DTO나 매핑이 바뀌면 문서 예시가 조용히 낡을 수 있다.
- `SPEC-0020`에서는 representative 실제 앱 검증으로 문서와 실제 응답 의미를 맞췄지만, 이 검증은 수동 절차라 이후 변경 때 매번 사람이 다시 확인해야 한다.
- 운영용 조회 응답은 필드가 서로 겹치는 부분이 많아, 예시 drift가 생기면 운영자가 어떤 응답을 어떻게 읽어야 하는지 다시 혼동할 수 있다.

### 범위
- 운영용 조회 응답 예시를 자동 검증할 fixture 또는 snapshot source를 정의한다.
- 아래 응답을 검증 대상에 포함한다.
  - `POST /reviews/rerun`
  - `POST /reviews/rerun/{executionKey}/retry`
  - `GET /reviews/rerun/{executionKey}`
  - `GET /reviews/rerun/executions`
  - `GET /reviews/rerun/{executionKey}/actions/history`
  - `POST /reviews/rerun/{executionKey}/actions`
- 가이드 문서의 예시와 자동 검증 대상이 어떻게 연결되는지 문서 안에서 바로 읽히게 정리한다.
- 예시 검증은 최소한 아래 축을 다시 확인할 수 있어야 한다.
  - `executionKey`
  - `retrySourceExecutionKey`
  - `executionStatus`
  - `failureDisposition`
  - `availableActions`
  - `latestAction*`
  - `currentActionState`
  - `actionStatus`
- 문서 예시와 테스트 fixture가 같은 의미를 설명하는지 검증하는 자동 테스트를 추가한다.
- 이번 spec의 자동 검증은 controller/service black-box 기대값과 fixture 비교를 중심으로 구성하고, DTO 직렬화 세부 구현만 따로 검증하는 저수준 serialization spec으로 키우지 않는다.

### 비대상
- 새 조회 endpoint 추가
- 응답 필드 rename
- 사용자용 UI
- bulk action
- OpenAPI 또는 Swagger 도입
- 실제 앱 representative 검증 절차 제거

### 외부 계약
- 기존 rerun/query/list/history/action 경로와 HTTP status 계약은 유지한다.
- 기존 응답 필드 의미는 바꾸지 않는다.
- 이번 spec은 응답 예시와 자동 검증 기준을 추가하는 작업이며, 운영 API의 동작 자체를 바꾸는 작업이 아니다.

### 핵심 시나리오
1. 예시 자동 검증 안전망 고정
   - 기존 controller/service black-box 테스트가 현재 guide 예시 자동 검증 spec에도 충분한지 먼저 확인한다.
   - 기존 안전망이 충분하면 근거만 남기고, 부족할 때만 최소 테스트를 추가한다.
2. 예시 fixture와 문서 매핑 구조 도입
   - 응답 예시를 어떤 fixture 파일 또는 검증 source와 연결할지 먼저 정리한다.
   - 운영자가 guide를 읽을 때 어느 예시가 자동 검증 대상인지 이해할 수 있게 문서 구조를 정리한다.
3. 예시 자동 검증 테스트 도입
   - controller/service black-box 기대값과 fixture를 비교하는 자동 검증 테스트를 추가한다.
   - rerun, retry, query, list, history, action 예시가 현재 계약과 다르면 테스트가 바로 깨지도록 한다.
4. drift 검증과 문서 마감
   - representative fixture와 자동 검증 테스트가 실제 guide 예시를 충분히 보호하는지 마지막으로 정리한다.
   - 필요하면 guide 예시 문구와 fixture를 최소 보정하고, drift 감지 기준을 회고와 summary에 남긴다.
   - 이번 spec은 docs/test 자산 정리 중심이므로 actual app/H2 representative 재검증은 수행하지 않고, 그 판단 근거를 retrospective와 summary에 남긴다.

### Task 분해 기준
- `TASK-0001` 예시 자동 검증 안전망 고정
- `TASK-0002` 예시 fixture와 문서 매핑 구조 도입
- `TASK-0003` 예시 자동 검증 테스트 도입
- `TASK-0004` drift 검증과 문서 마감

### 연결될 ValidationCriteria
- `manual-rerun-response-example-contract-preserved`
- `manual-rerun-response-example-source-defined`
- `manual-rerun-response-example-tests-defined`
- `manual-rerun-response-example-drift-detected`

### 필수 테스트 시나리오
- 예시 자동 검증을 추가하는 동안 기존 rerun/query/list/history/action 계약은 유지된다.
- guide 예시가 어떤 fixture 또는 검증 source를 기준으로 쓰였는지 문서에서 바로 읽을 수 있다.
- rerun/retry/query/list/history/action 예시가 현재 DTO/응답 계약과 다르면 자동 검증 테스트가 실패한다.
- 이후 문서 예시 drift가 생기면 targeted test 또는 full test에서 바로 드러난다.

## 후속 Spec 후보
### ID
SPEC-0022

### 이름
운영용 조회 응답 문서와 fixture 생성 규칙 정리

### 시작 조건
- `현재 활성 Spec`이 완료되고, 운영용 조회 응답 예시 자동 검증 기반이 안정적으로 동작한 뒤 시작한다.

### 목표
- 응답 예시 fixture를 누가, 어떤 규칙으로, 어떤 단위에서 갱신하는지 운영 규칙을 더 명확하게 정리한다.

### 후속 변경 범위
- fixture naming 규칙
- 예시 갱신 절차
- representative 응답과 fixture 갱신 경계 정리

### 후속 변경 비대상
- 사용자용 UI
- bulk action
- 장기 저장소 도입

### 후속 검증 방향
- fixture와 문서 예시 갱신 절차가 새 작업자에게도 바로 읽힌다.
