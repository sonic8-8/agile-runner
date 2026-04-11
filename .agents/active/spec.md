# 현재 활성 Spec

## 문서 목적
이 문서는 현재 활성 구현 범위와 후속 구현 범위를 고정하는 `Spec` 문서다.
`ValidationCriteria`와 `Task`는 이 문서의 활성 spec을 기준으로 작성하며, GitHub Issue는 각 `Task`를 외부에서 추적하기 위한 수단으로만 사용한다.

## 현재 활성 Spec
### ID
SPEC-0017

### 이름
운영용 관리자 제어 이력 기간 필터 확장

### 목표
- 운영자가 관리자 액션 이력을 시간 범위로 좁혀 읽을 수 있도록 최소 기간 필터를 도입한다.
- history 응답은 계속 timeline 전용 읽기 경계를 유지하되, `appliedAtFrom`, `appliedAtTo` 기준 조회가 가능하도록 만든다.
- representative actual app/H2 검증에서 기간 필터가 적용된 history 응답과 H2 audit evidence가 같은 execution 기준으로 일치하는지 확인한다.

### 대상 문제
- 현재 관리자 액션 이력은 `action`, `actionStatus` 기준으로만 좁혀 볼 수 있고, 언제 수행된 액션인지 시간 범위로 골라 읽을 수 없다.
- 같은 execution에 action row가 더 쌓이면 운영자가 최근 조치만 빠르게 확인하기 어렵다.
- query/list는 현재 상태 요약이고 history는 전체 timeline이라 역할은 분리돼 있지만, history 내부의 시간 범위 읽기 기준은 아직 없다.

### 범위
- `GET /reviews/rerun/{executionKey}/actions/history`에 기간 필터를 추가한다.
- 이번 spec에서 허용하는 기간 필터는 아래 둘이다.
  - `appliedAtFrom`
  - `appliedAtTo`
- 기간 필터가 비어 있으면 기존과 동일하게 전체 timeline을 반환한다.
- `appliedAtFrom`만 주면 해당 시각 이상 row만, `appliedAtTo`만 주면 해당 시각 이하 row만 반환한다.
- 두 필터를 함께 주면 둘 다 만족하는 row만 반환한다.
- representative actual app/H2 검증에서 무필터 history, 시작 시각 필터 history, 종료 시각 필터 history가 H2 audit evidence와 일치하는지 확인한다.

### 비대상
- repository 또는 PR 단위 history 검색
- action/actionStatus 외 추가 검색 필드
- 사용자용 UI
- bulk action
- 장기 저장소 도입

### 외부 계약
- 기존 `GET /reviews/rerun/{executionKey}/actions/history` 경로는 유지한다.
- 기간 필터를 주지 않으면 기존과 동일하게 전체 timeline을 반환한다.
- action/query/list/action response의 기존 응답 구조와 의미는 유지한다.
- `appliedAtFrom`, `appliedAtTo`는 ISO-8601 날짜-시간 문자열로 읽는다.

### 핵심 시나리오
1. 관리자 액션 이력 기간 필터 안전망 고정
   - 기존 history 응답, action 응답, query/list 상태 계약이 기간 필터 확장 전에도 유지된다는 점을 먼저 고정한다.
   - 이미 충분한 안전망이 있으면 그 근거를 남기고, 부족한 경우만 최소 테스트를 추가한다.
2. 관리자 액션 이력 기간 필터 입력 모델과 진입점 도입
   - history 조회가 `appliedAtFrom`, `appliedAtTo` query param을 읽도록 controller/service request 경계를 연다.
   - 이 단계는 입력 해석과 최소 응답 경계까지만 닫고, 실제 audit selection은 다음 단계로 넘긴다.
3. 관리자 액션 이력 기간 필터와 audit selection 연결
   - repository/service가 `appliedAtFrom`, `appliedAtTo` 필터를 실제 audit row 선택에 반영하도록 연결한다.
   - 필터가 없으면 전체 timeline, 있으면 조건 일치 row만 반환하는지 고정한다.
   - 없는 execution은 기존 not-found 의미를 유지하고, execution은 있지만 기간 필터 결과가 0건이면 빈 timeline을 반환하는지 고정한다.
4. 기간 필터와 실행 근거 정합성 검증
   - representative execution에서 무필터 history, 기간 필터 적용 history, H2 audit evidence가 같은 execution 기준으로 같은 결과를 가지는지 확인한다.

### Task 분해 기준
- `TASK-0001` 관리자 액션 이력 기간 필터 안전망 고정
- `TASK-0002` 관리자 액션 이력 기간 필터 입력 모델과 진입점 도입
- `TASK-0003` 관리자 액션 이력 기간 필터와 audit selection 연결
- `TASK-0004` 기간 필터와 실행 근거 정합성 검증

### 연결될 ValidationCriteria
- `manual-rerun-control-history-date-filter-contract-preserved`
- `manual-rerun-control-history-date-filter-input-defined`
- `manual-rerun-control-history-date-filter-maps-audit-selection`
- `manual-rerun-control-history-date-filter-runtime-evidence-aligned`

### 필수 테스트 시나리오
- 기간 필터를 도입해도 action 응답, query/list, history 기본 응답 구조는 유지된다.
- 기간 필터가 비어 있으면 전체 timeline을 반환한다.
- `appliedAtFrom`, `appliedAtTo` 필터는 audit row 선택에 그대로 반영된다.
- representative execution에서 무필터 history, 기간 필터 적용 history, H2 audit evidence가 같은 execution 기준으로 같은 결과를 가진다.

## 후속 Spec 후보
### ID
SPEC-0018

### 이름
운영용 관리자 제어 이력 정렬과 페이지 기준 정리

### 시작 조건
- `현재 활성 Spec`이 완료되고, 기간 필터 기준과 audit selection 해석이 안정적으로 닫힌 뒤 시작한다.

### 목표
- 운영자가 history 조회 결과를 정렬과 페이지 기준으로 더 안정적으로 읽을 수 있도록 응답 기준을 정리한다.

### 후속 변경 범위
- history 정렬 기준 명시
- 페이지 또는 커서 기준 검토
- 운영용 목록/이력 조회 조합 정리

### 후속 변경 비대상
- 사용자용 UI
- bulk action
- 장기 저장소 도입

### 후속 검증 방향
- 정렬/페이지 기준을 써도 현재 상태 요약과 history timeline 의미가 충돌하지 않는다.
