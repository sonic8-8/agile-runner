# 현재 활성 Spec

## 문서 목적
이 문서는 현재 활성 구현 범위와 후속 구현 범위를 고정하는 `Spec` 문서다.
`ValidationCriteria`와 `Task`는 이 문서의 활성 spec을 기준으로 작성하며, GitHub Issue는 각 `Task`를 외부에서 추적하기 위한 수단으로만 사용한다.

## 현재 활성 Spec
### ID
SPEC-0014

### 이름
운영용 관리자 제어 이력 조회 확장

### 목표
- 운영자가 execution별 관리자 액션 이력을 시간 순서대로 읽고, 각 action의 `note`, `actionStatus`, `appliedAt`을 확인할 수 있게 만든다.
- 단건 조회와 목록 조회가 계속 현재 상태와 `availableActions`만 보여주고, 새 이력 조회 API는 “무슨 액션이 언제 적용됐는지”만 읽도록 역할을 분리한다.
- representative actual app/H2 검증에서 action 응답, query/list 상태, action history, H2 audit evidence가 같은 execution 기준으로 같은 의미를 가지는지 확인한다.

### 대상 문제
- 현재는 단건 조회와 목록 조회에서 `availableActions`만 볼 수 있고, 운영자가 실제로 어떤 관리자 액션이 언제 적용됐는지는 읽을 수 없다.
- action 응답은 즉시 결과를 보여주지만, 나중에 다시 확인할 때는 H2 audit table을 직접 보지 않는 한 action timeline을 읽기 어렵다.
- 운영용 화면이나 대시보드를 붙이려면, 현재 상태와 별도로 “이미 수행된 관리자 액션 이력”을 읽는 API 경계가 먼저 정리돼야 한다.

### 범위
- `GET /reviews/rerun/{executionKey}/actions/history` 조회 경로를 추가한다.
- 관리자 액션 이력 응답은 최소한 아래를 포함한다.
  - `executionKey`
  - `actions`
  - `actions[].action`
  - `actions[].actionStatus`
  - `actions[].note`
  - `actions[].appliedAt`
- action history는 같은 execution의 관리자 액션을 `appliedAt` 기준 시간 순서대로 반환한다.
- 단건 조회와 목록 조회는 계속 현재 상태와 `availableActions`만 반환하고, action detail은 새 history 응답에서만 읽는다.
- local profile 실제 앱/H2 기준 representative execution에서 action 응답, history 조회 응답, 단건 조회, 목록 조회, H2 audit evidence가 같은 execution 기준으로 일치하는지 확인한다.

### 비대상
- 사용자용 UI
- bulk action
- 동일 execution에서 같은 action 반복 허용
- 장기 저장소 도입
- 새 관리자 액션 종류 추가

### 외부 계약
- `/webhook/github`, `POST /reviews/rerun`, `GET /reviews/rerun/{executionKey}`, `POST /reviews/rerun/{executionKey}/retry`, `GET /reviews/rerun/executions`, `POST /reviews/rerun/{executionKey}/actions` 기존 계약은 유지한다.
- 새 history 조회는 기존 query/list가 반환하던 `availableActions` 의미를 바꾸지 않아야 한다.
- history 응답은 action detail을 읽는 전용 경로이고, 단건 조회와 목록 조회는 계속 현재 상태 요약만 반환한다.

### 핵심 시나리오
1. 관리자 액션 이력 조회 안전망 고정
   - 기존 action 응답, query/list, retry, webhook 계약이 action history 조회 도입 전에도 유지된다는 점을 먼저 고정한다.
   - query/list는 계속 현재 상태와 `availableActions`만 반환하고 action detail은 새 경로로 분리한다는 경계를 문서와 테스트로 확인한다.
2. 관리자 액션 이력 입력 모델과 진입점 도입
   - `GET /reviews/rerun/{executionKey}/actions/history` 경로와 최소 응답 모델을 연다.
   - 이 단계는 입력 해석과 최소 응답 경계까지만 닫고, 실제 audit timeline 매핑은 다음 단계에서 고정한다.
3. 관리자 액션 이력 응답과 audit timeline 연결
   - action history 응답이 audit row를 시간 순서대로 읽고, `action`, `actionStatus`, `note`, `appliedAt`을 반환하도록 연결한다.
   - 없는 execution 또는 history 조회 불가 상황의 not-found 정책도 함께 고정한다.
4. 관리자 액션 이력과 실행 근거 정합성 검증
   - representative execution에서 action 응답, history 응답, query/list 상태, H2 audit evidence를 함께 대조해 같은 의미를 가지는지 확인한다.

### Task 분해 기준
- `TASK-0001` 관리자 액션 이력 조회 안전망 고정
- `TASK-0002` 관리자 액션 이력 입력 모델과 진입점 도입
- `TASK-0003` 관리자 액션 이력 응답과 audit timeline 연결
- `TASK-0004` 관리자 액션 이력과 실행 근거 정합성 검증

### 연결될 ValidationCriteria
- `manual-rerun-control-history-contract-preserved`
- `manual-rerun-control-history-response-defined`
- `manual-rerun-control-history-maps-audit-timeline`
- `manual-rerun-control-history-runtime-evidence-aligned`

### 필수 테스트 시나리오
- action history 조회를 도입해도 `/webhook/github`, `POST /reviews/rerun`, `GET /reviews/rerun/{executionKey}`, `POST /reviews/rerun/{executionKey}/retry`, `GET /reviews/rerun/executions`, `POST /reviews/rerun/{executionKey}/actions` 기존 계약은 유지된다.
- `GET /reviews/rerun/{executionKey}/actions/history`는 `executionKey`를 읽고, 성공 시 `actions[].action`, `actions[].actionStatus`, `actions[].note`, `actions[].appliedAt`를 반환한다.
- history 응답은 H2 audit row 순서와 같은 timeline을 반환하고, query/list는 계속 현재 상태와 `availableActions`만 반환한다.
- representative execution 1건 이상에서 action 응답, history 조회, query/list, H2 audit evidence가 같은 결과를 가진다.

## 후속 Spec 후보
### ID
SPEC-0015

### 이름
운영용 관리자 제어 액션 반복 정책 정교화

### 시작 조건
- `현재 활성 Spec`이 완료되고, action history 조회 응답과 audit timeline 해석이 안정적으로 닫힌 뒤 시작한다.

### 목표
- 같은 execution에서 `ACKNOWLEDGE`, `UNACKNOWLEDGE`를 반복 수행할 수 있는지, 허용한다면 어떤 저장 규칙과 조회 의미를 따를지 정리한다.

### 후속 변경 범위
- 반복 action 허용 여부 결정
- audit unique constraint 재설계 검토
- repeated action timeline 해석 규칙 정리

### 후속 변경 비대상
- 사용자용 UI
- bulk action
- 장기 저장소 도입

### 후속 검증 방향
- 같은 action이 반복되더라도 current state, history, availableActions, runtime evidence 해석이 충돌하지 않는다.
- 운영자가 현재 상태와 과거 이력을 함께 읽어도 action timeline이 모순되지 않는다.
