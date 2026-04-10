# 현재 활성 Spec

## 문서 목적
이 문서는 현재 활성 구현 범위와 후속 구현 범위를 고정하는 `Spec` 문서다.
`ValidationCriteria`와 `Task`는 이 문서의 활성 spec을 기준으로 작성하며, GitHub Issue는 각 `Task`를 외부에서 추적하기 위한 수단으로만 사용한다.

## 현재 활성 Spec
### ID
SPEC-0013

### 이름
운영용 관리자 제어 액션 다변화

### 목표
- 운영자가 `ACKNOWLEDGE`로 확인 완료 처리한 실행을 필요 시 다시 열 수 있게 만들어, 관리자 액션이 단방향 상태 변경에만 머물지 않도록 확장한다.
- 목록 조회와 단건 조회가 `ACKNOWLEDGE`, `UNACKNOWLEDGE` 상태를 반영해 `availableActions`를 일관되게 계산하도록 만든다.
- 추가 관리자 액션이 들어와도 action 응답, query/list 의미, H2 audit evidence를 같은 방식으로 해석할 수 있는 기준을 잡는다.

### 대상 문제
- 현재 관리자 액션은 `ACKNOWLEDGE` 하나뿐이라, 운영자가 실수로 확인 완료 처리했거나 다시 조치 대상으로 되돌려야 할 때 취소 수단이 없다.
- `ACKNOWLEDGE` 이후에는 `availableActions=[]`가 되기 때문에, 운영자는 “이 실행을 다시 열 수 있는지”를 읽을 수 없다.
- 다음 액션 종류를 늘리려면, 추가 액션 허용 조건과 query/list의 `availableActions` 재계산 규칙을 먼저 안정적으로 정리해야 한다.

### 범위
- 두 번째 관리자 액션으로 `UNACKNOWLEDGE`를 도입한다.
- `UNACKNOWLEDGE`는 아래 조건을 만족하는 execution에서만 허용한다.
  - `executionStartType = MANUAL_RERUN`
  - `executionStatus = FAILED`
  - `failureDisposition = MANUAL_ACTION_REQUIRED`
  - 같은 execution에 이미 `ACKNOWLEDGE` audit evidence가 있다
- 관리자 제어 액션 응답은 계속 최소한 아래를 포함한다.
  - `executionKey`
  - `action`
  - `actionStatus`
  - `availableActions`
  - `note`
- `ACKNOWLEDGE` 이후 query/list의 `availableActions`는 `UNACKNOWLEDGE`를 포함할 수 있어야 한다.
- `UNACKNOWLEDGE` 실행 후 query/list의 `availableActions`는 다시 `ACKNOWLEDGE`를 포함할 수 있어야 한다.
- local profile 실제 앱/H2 기준으로 representative execution에서 `ACKNOWLEDGE -> UNACKNOWLEDGE` 흐름을 수행하고, 응답/조회 결과/H2 audit evidence가 같은 의미를 가지는지 확인한다.

### 비대상
- 사용자용 UI
- 여러 execution 대상 bulk action
- `UNACKNOWLEDGE` 외 추가 관리자 액션 동시 도입
- 장기 저장소 도입
- 인증/권한 체계 추가

### 외부 계약
- `/webhook/github`, `POST /reviews/rerun`, `GET /reviews/rerun/{executionKey}`, `POST /reviews/rerun/{executionKey}/retry`, `GET /reviews/rerun/executions`, `POST /reviews/rerun/{executionKey}/actions` 기존 계약은 유지한다.
- 관리자 제어 액션 결과는 기존 retry eligibility 정책, 목록 응답의 `availableActions`, 단건 조회 응답 의미와 모순되지 않아야 한다.
- action detail은 계속 action 응답에서만 읽고, 단건 조회와 목록 조회는 `availableActions` 재계산 결과만 반환한다.

### 핵심 시나리오
1. 관리자 액션 다변화 안전망 고정
   - 기존 `ACKNOWLEDGE` 흐름, query/list 의미, retry/webhook 계약이 새 액션 추가 전에도 유지된다는 점을 먼저 고정한다.
   - `UNACKNOWLEDGE`가 필요한 상태 조합을 문서로 명확히 좁힌다.
2. 추가 액션 입력과 응답 모델 확장
   - `UNACKNOWLEDGE` request 해석과 최소 성공 응답 경계를 연다.
   - 이 단계는 입력 해석과 최소 응답 경계까지만 닫고, audit evidence 반영은 다음 단계에서 고정한다.
3. 추가 액션 정책과 상태 반영 연결
   - `UNACKNOWLEDGE` 허용 조건을 정책으로 고정하고, 실행 결과를 audit evidence로 저장한다.
   - query/list가 최신 audit state를 반영해 `ACKNOWLEDGE`와 `UNACKNOWLEDGE`를 번갈아 계산하도록 연결한다.
4. 액션 전환 응답과 실행 근거 정합성 검증
   - representative execution에서 `ACKNOWLEDGE -> UNACKNOWLEDGE`를 실제 앱에서 수행하고, 응답/조회 결과/H2 audit evidence가 같은 의미를 가지는지 확인한다.

### Task 분해 기준
- `TASK-0001` 관리자 액션 다변화 안전망 고정
- `TASK-0002` 추가 액션 입력과 응답 모델 확장
- `TASK-0003` 추가 액션 정책과 상태 반영 연결
- `TASK-0004` 액션 전환 응답과 실행 근거 정합성 검증

### 연결될 ValidationCriteria
- `manual-rerun-control-multi-action-contract-preserved`
- `manual-rerun-control-secondary-action-defined`
- `manual-rerun-control-secondary-action-maps-audit-state`
- `manual-rerun-control-secondary-runtime-evidence-aligned`

### 필수 테스트 시나리오
- 추가 관리자 액션을 도입해도 `/webhook/github`, `POST /reviews/rerun`, `GET /reviews/rerun/{executionKey}`, `POST /reviews/rerun/{executionKey}/retry`, `GET /reviews/rerun/executions`, `POST /reviews/rerun/{executionKey}/actions` 기존 계약은 유지된다.
- `POST /reviews/rerun/{executionKey}/actions`는 `UNACKNOWLEDGE` 요청을 읽고, 성공 응답에서 `actionStatus=APPLIED`를 반환하며, 허용되지 않은 상태 조합에서는 성공으로 처리하지 않는다.
- `ACKNOWLEDGE` 이후 query/list의 `availableActions`는 `UNACKNOWLEDGE`를 반환하고, `UNACKNOWLEDGE` 이후에는 다시 `ACKNOWLEDGE`를 반환한다.
- representative execution 1건 이상에서 `ACKNOWLEDGE -> UNACKNOWLEDGE` 응답과 H2 audit evidence가 같은 결과를 가진다.

## 후속 Spec 후보
### ID
SPEC-0014

### 이름
운영용 관리자 제어 이력 조회 확장

### 시작 조건
- `현재 활성 Spec`이 완료되고, 복수 관리자 액션의 request/response/audit evidence 구조가 안정적으로 닫힌 뒤 시작한다.

### 목표
- 운영자가 execution별 관리자 액션 이력을 순서대로 조회하고, 각 action note와 적용 시각을 읽을 수 있게 만든다.

### 후속 변경 범위
- action audit history 조회 응답 추가
- 목록/단건 조회와 action history 관계 정리
- representative verification에서 audit timeline 대조

### 후속 변경 비대상
- 사용자용 UI
- 장기 저장소 도입
- 자동 스케줄러 도입

### 후속 검증 방향
- 복수 관리자 액션이 쌓여도 query/list 의미와 action history 해석이 충돌하지 않는다.
- 운영자가 실행 상태, 가능한 액션, 이미 수행된 액션 이력을 일관되게 읽을 수 있다.
