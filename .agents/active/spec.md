# 현재 활성 Spec

## 문서 목적
이 문서는 현재 활성 구현 범위와 후속 구현 범위를 고정하는 `Spec` 문서다.
`ValidationCriteria`와 `Task`는 이 문서의 활성 spec을 기준으로 작성하며, GitHub Issue는 각 `Task`를 외부에서 추적하기 위한 수단으로만 사용한다.

## 현재 활성 Spec
### ID
SPEC-0011

### 이름
운영용 조회와 관리자 제어 기능 확장

### 목표
- 운영자가 manual rerun execution 이력을 목록으로 조회하고, 필요한 필터를 적용해 좁혀 볼 수 있게 만든다.
- 각 execution row에서 현재 상태와 failure disposition을 바탕으로 어떤 관리자 제어가 가능한지 읽을 수 있게 만든다.
- 목록 응답과 runtime evidence가 같은 의미를 가지도록 정리해 이후 관리자 제어 액션 확장의 기준으로 삼는다.

### 대상 문제
- 현재는 `GET /reviews/rerun/{executionKey}` 단건 조회만 가능해서 운영자가 전체 execution 흐름을 한 번에 보기가 어렵다.
- source execution과 파생 execution 관계는 추적 가능해졌지만, 어떤 execution이 재시도 가능한지 목록 관점에서 바로 읽을 수는 없다.
- 다음 단계의 관리자 제어 액션을 붙이려면, 먼저 목록 조회 응답에서 상태와 허용 가능한 제어 정보를 안정적으로 읽을 수 있어야 한다.

### 범위
- 내부/관리자용 `GET /reviews/rerun/executions` 목록 조회 진입점을 도입한다.
- 목록 조회 입력은 최소한 아래를 받는다.
  - `repositoryName`
  - `pullRequestNumber`
  - `executionStartType`
  - `executionStatus`
  - `failureDisposition`
- 필터 값이 비어 있으면 해당 조건은 미적용으로 해석한다.
- 목록 응답 row는 최소한 아래를 포함한다.
  - `executionKey`
  - `retrySourceExecutionKey`
  - `executionStartType`
  - `executionStatus`
  - `executionControlMode`
  - `writePerformed`
  - `errorCode`
  - `failureDisposition`
  - `availableActions`
- `availableActions`는 우선 `RETRY` 하나만 지원하고, 현재 execution이 manual rerun retry 정책상 재시도 가능할 때만 포함한다.
- 로컬 프로필 실제 앱/H2 기준으로 representative execution 2건 이상을 준비한 뒤, 목록 조회 응답의 필터 결과와 `availableActions` 의미가 runtime evidence와 같은지 확인한다.

### 비대상
- 사용자용 UI
- bulk action 실행
- 자동 재시도 스케줄러
- 장기 저장소 도입
- webhook 실행 제어

### 외부 계약
- `/webhook/github`, `POST /reviews/rerun`, `GET /reviews/rerun/{executionKey}`, `POST /reviews/rerun/{executionKey}/retry` 기존 계약은 유지한다.
- `GET /reviews/rerun/executions`는 내부/관리자용 목록 조회 진입점으로만 사용한다.
- 목록 응답의 상태와 제어 가능 여부는 기존 query/retry 정책과 모순되지 않아야 한다.

### 핵심 시나리오
1. 목록 조회 안전망 고정
   - 기존 rerun 단건 query와 retry 정책 계약이 유지된다는 점을 먼저 테스트로 고정한다.
   - 목록 조회 기능이 webhook과 기존 rerun 흐름에 영향을 주지 않는다는 점도 같이 고정한다.
2. 목록 조회 입력 모델과 진입점 도입
   - 필터 입력 DTO와 controller/service 진입점을 도입한다.
   - 이 단계는 입력 해석과 기본 목록 응답 경계까지만 닫고, `availableActions` 의미 매핑은 다음 단계에서 고정한다.
3. 목록 응답과 관리자 제어 상태 연결
   - runtime evidence를 목록 응답 row로 매핑하고, `availableActions`에 `RETRY` 포함 여부를 eligibility policy 기준으로 연결한다.
   - 필터 조합에 따른 목록 축소와 응답 의미를 controller/service black-box 테스트로 고정한다.
4. 목록 응답과 실행 근거 정합성 검증
   - representative execution 여러 건을 local H2에 준비한 뒤 실제 앱에서 목록 조회를 호출하고, 응답 row와 H2 evidence의 `executionStatus`, `failureDisposition`, `availableActions` 의미가 같은지 확인한다.

### Task 분해 기준
- `TASK-0001` 목록 조회 안전망 고정
- `TASK-0002` 목록 조회 입력 모델과 진입점 도입
- `TASK-0003` 목록 응답과 관리자 제어 상태 연결
- `TASK-0004` 목록 응답과 실행 근거 정합성 검증

### 연결될 ValidationCriteria
- `manual-rerun-list-contract-preserved`
- `manual-rerun-list-filter-defined`
- `manual-rerun-list-response-maps-control-state`
- `manual-rerun-list-runtime-evidence-aligned`

### 필수 테스트 시나리오
- 목록 조회 기능을 추가해도 `/webhook/github`, `POST /reviews/rerun`, `GET /reviews/rerun/{executionKey}`, `POST /reviews/rerun/{executionKey}/retry` 기존 계약은 유지된다.
- 목록 조회 필터는 비어 있는 값은 무시하고, 값이 있는 조건만 적용한다.
- 목록 응답 row는 runtime evidence의 `executionStartType`, `executionStatus`, `executionControlMode`, `writePerformed`, `errorCode`, `failureDisposition`, `retrySourceExecutionKey`를 같은 의미로 반환한다.
- `availableActions`의 `RETRY` 포함 여부는 기존 manual rerun retry eligibility 정책과 같은 기준으로 해석된다.
- representative 목록 조회 1건 이상에서 응답 row와 H2 evidence가 같은 필터 결과와 제어 가능 상태를 가진다.

## 후속 Spec 후보
### ID
SPEC-0012

### 이름
운영용 관리자 제어 액션 확장

### 시작 조건
- `현재 활성 Spec`이 완료되고, 운영용 execution 목록 조회와 `availableActions` 해석이 안정적으로 닫힌 뒤 시작한다.

### 목표
- 운영자가 목록 조회 결과를 바탕으로 실제 관리자 제어 액션을 더 넓게 수행할 수 있게 만든다.

### 후속 변경 범위
- retry 외 관리자 제어 액션 추가
- 관리자 제어 액션 결과 응답 정교화
- 제어 액션별 audit evidence 확장

### 후속 변경 비대상
- 사용자용 UI
- 장기 저장소 도입
- 자동 스케줄러 도입

### 후속 검증 방향
- 목록 응답과 관리자 제어 액션이 기존 rerun/query/retry 계약과 충돌하지 않는다.
- 운영자가 execution 상태와 가능한 제어 액션을 일관되게 읽고 실행할 수 있다.
