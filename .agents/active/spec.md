# 현재 활성 Spec

## 문서 목적
이 문서는 현재 활성 구현 범위와 후속 구현 범위를 고정하는 `Spec` 문서다.
`ValidationCriteria`와 `Task`는 이 문서의 활성 spec을 기준으로 작성하며, GitHub Issue는 각 `Task`를 외부에서 추적하기 위한 수단으로만 사용한다.

## 현재 활성 Spec
### ID
SPEC-0019

### 이름
운영용 목록 조회와 이력 조회 조합 정리

### 목표
- 운영자가 실행 목록과 관리자 액션 이력을 함께 볼 때, 현재 상태 요약과 과거 timeline이 더 자연스럽게 이어지도록 응답 조합 기준을 정리한다.
- 목록 row가 최신 관리자 액션 요약을 바로 보여 주고, history 응답도 같은 실행의 현재 조치 상태를 함께 보여 주도록 맞춘다.
- representative actual app/H2 검증에서 목록 요약, history 현재 상태 요약, H2 audit evidence가 같은 execution 기준으로 일치하는지 확인한다.

### 대상 문제
- 현재 실행 목록은 `executionStatus`, `failureDisposition`, `availableActions`는 보여 주지만, 마지막으로 어떤 관리자 액션이 적용됐는지는 바로 읽기 어렵다.
- 현재 history 응답은 timeline만 보여 주기 때문에, 운영자는 마지막 action row를 보고 현재 조치 상태를 직접 추론해야 한다.
- list는 현재 상태 요약이고 history는 과거 이력이라는 역할은 맞지만, 둘을 함께 읽을 때 연결 지점이 약해서 운영자가 같은 execution의 현재 조치 상태를 두 응답 사이에서 다시 해석해야 한다.

### 범위
- `GET /reviews/rerun/executions` 목록 row에 아래 현재 조치 상태 요약 필드를 추가한다.
  - `latestAction`
  - `latestActionStatus`
  - `latestActionAppliedAt`
  - `historyAvailable`
- `historyAvailable`은 해당 execution에 관리자 액션 audit row가 하나 이상 있으면 `true`, 없으면 `false`로 해석한다.
- `GET /reviews/rerun/{executionKey}/actions/history` 응답에 아래 현재 조치 상태 요약 객체를 추가한다.
  - `currentActionState.latestAction`
  - `currentActionState.latestActionStatus`
  - `currentActionState.latestActionAppliedAt`
  - `currentActionState.availableActions`
- `currentActionState`는 같은 execution의 최신 applied action과 현재 가능한 관리자 액션을 함께 보여 준다.
- `currentActionState`와 목록 row 최신 action 요약은 현재 요청의 history filter, sort, page window 결과와 무관하게 같은 execution 전체 audit timeline의 최신 applied action을 기준으로 계산한다.
- 최신 action은 같은 execution 전체 audit timeline에서 가장 마지막 applied row를 기준으로 계산한다.
- action audit가 하나도 없으면 목록 row의 최신 action 요약 필드는 `null`, `historyAvailable=false`, history 응답의 `currentActionState` 최신 action 필드는 `null`로 유지한다.
- 기존 `actions[]` timeline은 그대로 유지하고, list 응답에 전체 timeline을 싣지 않는다.
- `GET /reviews/rerun/{executionKey}` query 응답은 이번 spec의 직접 변경 범위에 포함하지 않는다.

### 비대상
- 새 관리자 액션 추가
- 사용자용 UI
- bulk action
- 장기 저장소 도입
- query 응답에 history timeline 전체를 합치는 작업

### 외부 계약
- 기존 `GET /reviews/rerun/executions`와 `GET /reviews/rerun/{executionKey}/actions/history` 경로는 유지한다.
- 기존 list row의 `executionKey`, `executionStatus`, `failureDisposition`, `availableActions` 의미는 유지한다.
- 기존 history 응답의 `actions[]` timeline 구조와 정렬/페이지 의미는 유지한다.
- `currentActionState`는 현재 요청의 history filter/page 결과가 아니라 execution 전체의 현재 조치 상태 요약을 보여 준다.
- 최신 action 요약은 기존 audit row에서 계산된 결과를 그대로 보여 주고, 별도 write side effect를 만들지 않는다.
- representative actual app/H2 검증은 같은 `executionKey` 기준으로 list row, history 응답, H2 audit evidence를 비교한다.

### 핵심 시나리오
1. 목록 조회와 이력 조회 조합 안전망 고정
   - list/query/history/action 기존 계약이 이번 조합 정리 전에도 충분히 고정돼 있는지 먼저 확인한다.
   - 이미 충분한 안전망이 있으면 근거를 남기고, 부족한 경우만 최소 테스트를 추가한다.
2. 목록 row와 history 현재 조치 상태 요약 응답 모델 도입
   - list row와 history 응답이 최신 action 요약을 담을 수 있도록 controller/service 응답 경계를 연다.
   - 이 단계는 응답 모델과 최소 경계 정의까지만 닫고, 실제 audit row 연결은 다음 단계로 넘긴다.
3. 최신 관리자 액션 요약과 timeline 연결
   - list row의 최신 action 요약과 history `currentActionState`가 실제 audit timeline과 최신 applied action 기준으로 계산되도록 연결한다.
   - 이 계산은 현재 history filter, sort, page window 결과가 아니라 execution 전체 audit timeline을 기준으로 수행한다.
   - action audit가 없는 execution에서도 `null`/`false` 의미가 일관되게 유지되는지 고정한다.
4. 목록 요약과 이력 timeline 실행 근거 정합성 검증
   - representative execution에서 list row, history `currentActionState`, H2 audit evidence가 같은 execution 기준으로 같은 의미를 가지는지 확인한다.

### Task 분해 기준
- `TASK-0001` 목록 조회와 이력 조회 조합 안전망 고정
- `TASK-0002` 목록 row와 history 현재 조치 상태 요약 응답 모델 도입
- `TASK-0003` 최신 관리자 액션 요약과 timeline 연결
- `TASK-0004` 목록 요약과 이력 timeline 실행 근거 정합성 검증

### 연결될 ValidationCriteria
- `manual-rerun-list-history-composition-contract-preserved`
- `manual-rerun-list-history-composition-response-defined`
- `manual-rerun-list-history-composition-maps-latest-action`
- `manual-rerun-list-history-composition-runtime-evidence-aligned`

### 필수 테스트 시나리오
- list/history 조합 기준을 도입해도 기존 action/query/list/history 계약은 유지된다.
- 목록 row는 최신 action 요약이 있을 때만 `latestAction`, `latestActionStatus`, `latestActionAppliedAt`를 반환하고, audit가 없으면 `null`과 `historyAvailable=false`를 유지한다.
- history 응답은 `actions[]` timeline을 그대로 유지하면서 `currentActionState`로 현재 조치 상태 요약을 함께 반환한다.
- list row 최신 action 요약과 history `currentActionState`는 같은 execution의 최신 applied audit row와 일치한다.
- representative execution에서 list row, history 응답, H2 audit evidence가 같은 execution 기준으로 같은 의미를 가진다.

## 후속 Spec 후보
### ID
SPEC-0020

### 이름
운영용 조회 응답 문서 기준 정리

### 시작 조건
- `현재 활성 Spec`이 완료되고, 목록 요약과 history 현재 조치 상태 요약의 연결 기준이 안정적으로 닫힌 뒤 시작한다.

### 목표
- 운영자가 list/query/history/action 응답을 읽을 때 각 응답이 어떤 질문에 답하는지 더 명확하게 문서와 응답 기준으로 정리한다.

### 후속 변경 범위
- query, list, history, action 응답 역할 구분 문서화
- 운영자 조회 응답 예시와 필드 의미 정리
- 조회 응답 간 중복 요약 필드 기준 정리

### 후속 변경 비대상
- 사용자용 UI
- bulk action
- 장기 저장소 도입

### 후속 검증 방향
- 운영자가 query, list, history, action 응답을 함께 봐도 각 응답의 역할과 필드 의미가 충돌하지 않는다.
