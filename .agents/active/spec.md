# 현재 활성 Spec

## 문서 목적
이 문서는 현재 활성 구현 범위와 후속 구현 범위를 고정하는 `Spec` 문서다.
`ValidationCriteria`와 `Task`는 이 문서의 활성 spec을 기준으로 작성하며, GitHub Issue는 각 `Task`를 외부에서 추적하기 위한 수단으로만 사용한다.

## 현재 활성 Spec
### ID
SPEC-0016

### 이름
운영용 관리자 제어 이력 필터 확장

### 목표
- 운영자가 action history에서 필요한 action row만 골라 볼 수 있도록 최소 필터를 도입한다.
- history 응답은 계속 timeline 전용 읽기 경계를 유지하되, `action`과 `actionStatus` 기준 필터링이 가능하도록 만든다.
- representative actual app/H2 검증에서 필터 없는 history, 필터 적용 history, H2 audit evidence가 같은 execution 기준으로 일치하는지 확인한다.

### 대상 문제
- 현재 action history는 같은 execution의 전체 timeline만 반환하고, 운영자가 특정 action이나 상태만 좁혀서 볼 수 없다.
- 액션 종류와 이력 row가 늘어나면 운영자가 필요한 row만 빠르게 확인하기 어렵다.
- query/list는 현재 상태 요약이고 history는 전체 timeline이어서 역할은 분리됐지만, history 내부의 선택 읽기 기준은 아직 없다.

### 범위
- `GET /reviews/rerun/{executionKey}/actions/history`에 최소 필터를 추가한다.
- 이번 spec에서 허용하는 필터는 아래 둘이다.
  - `action`
  - `actionStatus`
- 필터가 비어 있으면 전체 timeline을 그대로 반환한다.
- `action`과 `actionStatus`를 함께 주면 두 조건을 모두 만족하는 row만 반환한다.
- representative actual app/H2 검증에서 필터 없는 history, `action=ACKNOWLEDGE` history, `actionStatus=APPLIED` history가 H2 audit evidence와 일치하는지 확인한다.

### 비대상
- 날짜 범위 필터
- repository 또는 PR 단위 history 검색
- 사용자용 UI
- bulk action
- 장기 저장소 도입

### 외부 계약
- 기존 `GET /reviews/rerun/{executionKey}/actions/history` 경로는 유지한다.
- 필터를 주지 않으면 기존과 동일하게 전체 timeline을 반환한다.
- action/query/list/action response의 기존 응답 구조와 의미는 유지한다.

### 핵심 시나리오
1. 관리자 액션 이력 필터 안전망 고정
   - 기존 history 응답, action 응답, query/list 상태 계약이 필터 확장 전에도 유지된다는 점을 먼저 고정한다.
   - 이미 충분한 안전망이 있으면 그 근거를 남기고, 부족한 경우만 최소 테스트를 추가한다.
2. 관리자 액션 이력 필터 입력 모델과 진입점 도입
   - history 조회가 `action`, `actionStatus` query param을 읽도록 controller/service request 경계를 연다.
   - 이 단계는 입력 해석과 최소 응답 경계까지만 닫고, 실제 audit selection은 다음 단계로 넘긴다.
3. 관리자 액션 이력 필터와 audit selection 연결
   - repository/service가 `action`, `actionStatus` 필터를 실제 audit row 선택에 반영하도록 연결한다.
   - 필터가 없으면 전체 timeline, 있으면 조건 일치 row만 반환하는지 고정한다.
4. 이력 필터와 실행 근거 정합성 검증
   - representative execution에서 필터 없는 history, 필터 적용 history, H2 audit evidence가 같은 execution 기준으로 같은 결과를 가지는지 확인한다.

### Task 분해 기준
- `TASK-0001` 관리자 액션 이력 필터 안전망 고정
- `TASK-0002` 관리자 액션 이력 필터 입력 모델과 진입점 도입
- `TASK-0003` 관리자 액션 이력 필터와 audit selection 연결
- `TASK-0004` 이력 필터와 실행 근거 정합성 검증

### 연결될 ValidationCriteria
- `manual-rerun-control-history-filter-contract-preserved`
- `manual-rerun-control-history-filter-input-defined`
- `manual-rerun-control-history-filter-maps-audit-selection`
- `manual-rerun-control-history-filter-runtime-evidence-aligned`

### 필수 테스트 시나리오
- 필터를 도입해도 action 응답, query/list, history 기본 응답 구조는 유지된다.
- 필터가 비어 있으면 전체 timeline을 반환한다.
- `action`, `actionStatus` 필터는 audit row 선택에 그대로 반영된다.
- representative execution에서 필터 없는 history, 필터 적용 history, H2 audit evidence가 같은 execution 기준으로 같은 결과를 가진다.

## 후속 Spec 후보
### ID
SPEC-0017

### 이름
운영용 관리자 제어 이력 기간 필터 확장

### 시작 조건
- `현재 활성 Spec`이 완료되고, 최소 필터 기준과 audit selection 해석이 안정적으로 닫힌 뒤 시작한다.

### 목표
- 운영자가 기간 조건으로 action history를 더 빠르게 좁혀 볼 수 있도록 읽기 조건을 정리한다.

### 후속 변경 범위
- `appliedAt` 기간 필터 검토
- history 조회 응답 검색성 개선 검토
- 운영용 목록/이력 조회 조합 정리

### 후속 변경 비대상
- 사용자용 UI
- bulk action
- 장기 저장소 도입

### 후속 검증 방향
- 기간 필터를 써도 현재 상태 요약과 history timeline 의미가 충돌하지 않는다.
