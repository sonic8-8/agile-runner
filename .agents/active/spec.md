# 현재 활성 Spec

## 문서 목적
이 문서는 현재 활성 구현 범위와 후속 구현 범위를 고정하는 `Spec` 문서다.
`ValidationCriteria`와 `Task`는 이 문서의 활성 spec을 기준으로 작성하며, GitHub Issue는 각 `Task`를 외부에서 추적하기 위한 수단으로만 사용한다.

## 현재 활성 Spec
### ID
SPEC-0015

### 이름
운영용 관리자 제어 액션 반복 정책 정교화

### 목표
- 같은 execution에서 `ACKNOWLEDGE -> UNACKNOWLEDGE -> ACKNOWLEDGE`처럼 기존 관리자 액션을 반복 적용할 수 있게 만든다.
- 현재 상태 요약은 계속 마지막 applied action 기준으로 계산하고, action history는 반복된 액션 전체 timeline을 시간 순서대로 읽을 수 있게 만든다.
- representative actual app/H2 검증에서 action 응답, query/list 상태, history 응답, H2 audit evidence가 같은 execution 기준으로 같은 의미를 가지는지 확인한다.

### 대상 문제
- 현재 audit table은 `(execution_key, action)` unique constraint 때문에 같은 execution에서 같은 action을 두 번 저장할 수 없다.
- 그래서 `ACKNOWLEDGE` 후 `UNACKNOWLEDGE`를 거친 실행을 다시 `ACKNOWLEDGE`하려고 하면 정책은 허용해도 저장 단계에서 막힌다.
- 운영자 입장에서는 현재 상태와 과거 이력을 모두 봐야 하는데, 반복 액션을 허용하지 않으면 실제 운영 흐름과 audit timeline이 어긋날 수 있다.

### 범위
- 기존 관리자 액션 종류 `ACKNOWLEDGE`, `UNACKNOWLEDGE`만 대상으로 반복 적용 정책을 정리한다.
- 반복 액션을 허용할 수 있도록 audit 저장 규칙과 물리 스키마를 정리한다.
- 단건 조회와 목록 조회는 계속 마지막 applied action 기준으로 `availableActions`를 계산한다.
- history 조회는 같은 execution의 반복 액션 전체 timeline을 `appliedAt` 기준 시간 순서대로 반환한다.
- local profile 실제 앱/H2 기준 representative execution에서 `ACKNOWLEDGE -> UNACKNOWLEDGE -> ACKNOWLEDGE` 흐름을 검증한다.

### 비대상
- 새 관리자 액션 종류 추가
- 사용자용 UI
- bulk action
- 장기 저장소 도입
- action history 페이징 또는 검색 확장

### 외부 계약
- `POST /reviews/rerun/{executionKey}/actions` 기존 경로는 유지한다.
- `GET /reviews/rerun/{executionKey}`, `GET /reviews/rerun/executions`, `GET /reviews/rerun/{executionKey}/actions/history` 경로는 유지한다.
- 반복 액션을 허용해도 action 응답 구조, query/list 응답 구조, history 응답 구조는 유지한다.
- query/list는 계속 현재 상태와 `availableActions`만 반환하고, history는 반복 액션 전체 timeline을 반환한다.

### 핵심 시나리오
1. 관리자 액션 반복 정책 안전망 고정
   - 기존 action 응답, query/list 상태, history 응답 계약이 반복 정책 정교화 전에도 유지된다는 점을 먼저 고정한다.
   - 이미 충분한 안전망이 있으면 그 근거를 남기고, 부족한 경우만 최소 테스트를 추가한다.
2. 관리자 액션 반복 정책과 audit 저장 규칙 정리
   - `ACKNOWLEDGE -> UNACKNOWLEDGE -> ACKNOWLEDGE` 흐름을 허용하도록 저장 규칙과 물리 스키마를 정리한다.
   - 마지막 applied action 기준 정책은 유지하되, 반복 액션이 저장 단계에서 막히지 않게 만든다.
3. 반복 액션 이후 query/list/history 상태 연결
   - query/list는 마지막 applied action 기준 현재 상태를 보여주고, history는 반복 액션 전체 timeline을 순서대로 반환하도록 연결한다.
   - 반복 액션 이후 `availableActions` 해석이 현재 상태와 충돌하지 않는지 고정한다.
4. 반복 액션과 실행 근거 정합성 검증
   - representative execution에서 action 응답, history 응답, query/list 상태, H2 audit evidence가 같은 execution 기준으로 같은 의미를 가지는지 확인한다.

### Task 분해 기준
- `TASK-0001` 관리자 액션 반복 정책 안전망 고정
- `TASK-0002` 관리자 액션 반복 정책과 audit 저장 규칙 정리
- `TASK-0003` 반복 액션 이후 query/list/history 상태 연결
- `TASK-0004` 반복 액션과 실행 근거 정합성 검증

### 연결될 ValidationCriteria
- `manual-rerun-control-repeat-contract-preserved`
- `manual-rerun-control-repeat-policy-allows-reapply`
- `manual-rerun-control-repeat-history-reflects-latest-state`
- `manual-rerun-control-repeat-runtime-evidence-aligned`

### 필수 테스트 시나리오
- 반복 액션 정책을 도입해도 기존 action 응답, query/list 응답, history 응답 구조는 유지된다.
- `ACKNOWLEDGE -> UNACKNOWLEDGE -> ACKNOWLEDGE` 흐름이 저장 단계에서 막히지 않고, 서비스 응답과 repository read model이 일관된다.
- query/list는 마지막 applied action 기준으로 `availableActions`를 계산하고, history는 반복 액션 전체 timeline을 시간 순서대로 반환한다.
- representative execution 1건 이상에서 action 응답, history 응답, query/list, H2 audit evidence가 같은 execution 기준으로 같은 결과를 가진다.

## 후속 Spec 후보
### ID
SPEC-0016

### 이름
운영용 관리자 제어 이력 필터 확장

### 시작 조건
- `현재 활성 Spec`이 완료되고, 반복 액션 timeline과 현재 상태 해석이 안정적으로 닫힌 뒤 시작한다.

### 목표
- 운영자가 action history를 필터링하거나 필요한 execution만 더 빠르게 찾을 수 있도록 조회 조건을 정리한다.

### 후속 변경 범위
- action history 필터 조건 검토
- execution 목록 필터 확장 검토
- 운영용 조회 응답의 검색성 개선 검토

### 후속 변경 비대상
- 사용자용 UI
- bulk action
- 장기 저장소 도입

### 후속 검증 방향
- 운영자가 현재 상태와 과거 이력을 더 빠르게 찾을 수 있고, 반복 액션 timeline 해석과 충돌하지 않는다.
