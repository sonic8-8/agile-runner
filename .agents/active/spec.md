# 현재 활성 Spec

## 문서 목적
이 문서는 현재 활성 구현 범위와 후속 구현 범위를 고정하는 `Spec` 문서다.
`ValidationCriteria`와 `Task`는 이 문서의 활성 spec을 기준으로 작성하며, GitHub Issue는 각 `Task`를 외부에서 추적하기 위한 수단으로만 사용한다.

## 현재 활성 Spec
### ID
SPEC-0012

### 이름
운영용 관리자 제어 액션 확장

### 목표
- 운영자가 execution 목록과 단건 조회에서 읽은 상태를 바탕으로 실제 관리자 제어 액션을 실행할 수 있게 만든다.
- 첫 관리자 제어 액션으로 `ACKNOWLEDGE`를 도입해, `MANUAL_ACTION_REQUIRED` 실행을 운영자가 확인 완료 처리할 수 있게 만든다.
- 관리자 제어 액션 응답, 목록/단건 조회 상태, runtime audit evidence가 같은 의미를 가지도록 정리해 이후 액션 확장의 기준으로 삼는다.

### 대상 문제
- 현재 운영용 조회는 `availableActions`로 `RETRY` 가능 여부만 읽을 수 있고, `MANUAL_ACTION_REQUIRED` 실행에 대해 운영자가 남길 수 있는 제어 액션이 없다.
- 운영자가 실행을 확인 완료했는지, 어떤 액션을 언제 적용했는지 남는 audit evidence가 부족하다.
- 다음 단계에서 관리자 액션 종류를 늘리려면, 먼저 액션 요청 경계, 응답 모델, audit evidence 저장 방식을 안정적으로 열어야 한다.

### 범위
- 내부/관리자용 `POST /reviews/rerun/{executionKey}/actions` 진입점을 도입한다.
- 제어 액션 request는 최소한 아래를 받는다.
  - `action`
  - `note`
- 첫 지원 액션은 `ACKNOWLEDGE` 하나로 제한한다.
- `ACKNOWLEDGE`는 아래 조건을 만족하는 execution에서만 허용한다.
  - `executionStartType = MANUAL_RERUN`
  - `executionStatus = FAILED`
  - `failureDisposition = MANUAL_ACTION_REQUIRED`
  - 아직 같은 execution에 `ACKNOWLEDGE` audit evidence가 없다
- 관리자 제어 액션 응답은 최소한 아래를 포함한다.
  - `executionKey`
  - `action`
  - `actionStatus`
  - `availableActions`
  - `note`
- 성공 응답의 `actionStatus`는 이번 spec에서 `APPLIED` 하나로 고정한다.
- 관리자 제어 액션 실행 후에는 목록 조회와 단건 조회에서 최신 audit state를 반영해 `availableActions`를 다시 계산한다.
- 목록 조회와 단건 조회는 이번 spec에서 `action`, `actionStatus`, `note`를 직접 노출하지 않고, audit state 반영 결과로 계산된 `availableActions`만 갱신한다.
- local profile 실제 앱/H2 기준으로 representative execution을 준비한 뒤, 액션 응답과 목록/단건 조회 결과, H2 audit evidence가 같은 의미를 가지는지 확인한다.

### 비대상
- 사용자용 UI
- 여러 execution 대상 bulk action
- retry 외 다중 관리자 액션 동시 도입
- 장기 저장소 도입
- 인증/권한 체계 추가

### 외부 계약
- `/webhook/github`, `POST /reviews/rerun`, `GET /reviews/rerun/{executionKey}`, `POST /reviews/rerun/{executionKey}/retry`, `GET /reviews/rerun/executions` 기존 계약은 유지한다.
- `POST /reviews/rerun/{executionKey}/actions`는 내부/관리자용 최소 제어 진입점으로만 사용한다.
- 관리자 제어 액션 결과는 기존 retry eligibility 정책, 목록 응답의 `availableActions`, 단건 조회 응답 의미와 모순되지 않아야 한다.

### 핵심 시나리오
1. 관리자 제어 액션 안전망 고정
   - 기존 rerun query, retry, 목록 조회, webhook 계약이 관리자 제어 액션 추가 전에도 유지된다는 점을 먼저 테스트로 고정한다.
   - `ACKNOWLEDGE`가 필요한 상태 조합을 문서로 명확히 좁힌다.
2. 액션 입력 모델과 진입점 도입
   - `POST /reviews/rerun/{executionKey}/actions` request DTO와 controller/service 진입점을 도입한다.
   - 이 단계는 request 해석과 최소 응답 경계까지만 닫고, 성공 응답의 `actionStatus=APPLIED` 계약을 먼저 고정한다.
   - audit evidence 저장과 조회 반영은 다음 단계에서 고정한다.
3. 액션 실행과 audit state 연결
   - `ACKNOWLEDGE` 실행 조건을 정책으로 고정하고, 실행 결과를 audit evidence로 저장한다.
   - 단건 조회와 목록 조회가 audit state를 반영해 `availableActions`를 다시 계산하도록 연결한다.
   - 조회 응답은 action detail 자체를 새로 노출하지 않고, 최신 audit state 반영 결과만 `availableActions`로 보여준다.
4. 액션 응답과 실행 근거 정합성 검증
   - representative execution과 action audit row를 기준으로 실제 앱에서 관리자 제어 액션을 실행하고, 응답/조회 결과/H2 evidence가 같은 의미를 가지는지 확인한다.

### Task 분해 기준
- `TASK-0001` 관리자 제어 액션 안전망 고정
- `TASK-0002` 액션 입력 모델과 진입점 도입
- `TASK-0003` 액션 실행과 audit state 연결
- `TASK-0004` 액션 응답과 실행 근거 정합성 검증

### 연결될 ValidationCriteria
- `manual-rerun-control-contract-preserved`
- `manual-rerun-control-action-defined`
- `manual-rerun-control-response-maps-audit-state`
- `manual-rerun-control-runtime-evidence-aligned`

### 필수 테스트 시나리오
- 관리자 제어 액션 기능을 추가해도 `/webhook/github`, `POST /reviews/rerun`, `GET /reviews/rerun/{executionKey}`, `POST /reviews/rerun/{executionKey}/retry`, `GET /reviews/rerun/executions` 기존 계약은 유지된다.
- `POST /reviews/rerun/{executionKey}/actions`는 `ACKNOWLEDGE` 요청을 읽고, 성공 응답에서 `actionStatus=APPLIED`를 반환하며, 허용되지 않은 상태 조합에서는 성공으로 처리하지 않는다.
- 액션 응답은 `action`, `actionStatus`, `note`를 반환하고, 단건 조회와 목록 조회는 audit state를 반영해 같은 `availableActions`와 상태 의미를 반환한다.
- representative 관리자 제어 액션 1건 이상에서 응답과 H2 audit evidence가 같은 결과를 가진다.

## 후속 Spec 후보
### ID
SPEC-0013

### 이름
운영용 관리자 제어 액션 다변화

### 시작 조건
- `현재 활성 Spec`이 완료되고, `ACKNOWLEDGE` 액션의 request/response/audit evidence 구조가 안정적으로 닫힌 뒤 시작한다.

### 목표
- 운영자가 `ACKNOWLEDGE` 외 추가 관리자 제어 액션을 일관되게 읽고 실행할 수 있게 만든다.

### 후속 변경 범위
- 추가 관리자 제어 액션 정의
- 액션별 conflict/not-found 응답 정교화
- 액션 종류별 representative verification 조합 확장

### 후속 변경 비대상
- 사용자용 UI
- 장기 저장소 도입
- 자동 스케줄러 도입

### 후속 검증 방향
- 관리자 제어 액션 종류가 늘어나도 목록/단건 조회와 audit evidence 의미가 충돌하지 않는다.
- 운영자가 실행 상태와 가능한 관리자 액션을 일관되게 읽고 실행할 수 있다.
