# 현재 활성 Spec

## 문서 목적
이 문서는 현재 활성 구현 범위와 후속 구현 범위를 고정하는 `Spec` 문서다.
`ValidationCriteria`와 `Task`는 이 문서의 활성 spec을 기준으로 작성하며, GitHub Issue는 각 `Task`를 외부에서 추적하기 위한 수단으로만 사용한다.

## 현재 활성 Spec
### ID
SPEC-0018

### 이름
운영용 관리자 제어 이력 정렬과 페이지 기준 정리

### 목표
- 운영자가 관리자 액션 이력을 원하는 순서로 읽을 수 있도록 최소 정렬 기준을 도입한다.
- 긴 history timeline을 나눠 읽을 수 있도록 page size와 cursor 기준을 정리한다.
- representative actual app/H2 검증에서 정렬/페이지가 적용된 history 응답과 H2 audit evidence가 같은 execution 기준으로 일치하는지 확인한다.

### 대상 문제
- 현재 관리자 액션 이력은 기간 필터까지는 지원하지만, 정렬 기준이 고정돼 있고 page window가 없어 row가 많아질수록 읽기 부담이 커진다.
- 운영자는 최근 조치를 먼저 보거나, 일정 개수 단위로 나눠 읽고 싶어도 현재는 전체 timeline을 한 번에 내려받아야 한다.
- query/list는 현재 상태 요약이고 history는 timeline이라 역할은 분리돼 있지만, history 내부의 정렬과 page 기준은 아직 없다.

### 범위
- `GET /reviews/rerun/{executionKey}/actions/history`에 아래 입력을 추가한다.
  - `sortDirection`
  - `pageSize`
  - `cursorAppliedAt`
- `sortDirection`은 `ASC`, `DESC`만 허용한다.
- `pageSize` 또는 `cursorAppliedAt`만 있고 `sortDirection`이 비어 있으면 `DESC`로 해석한다.
- `pageSize`가 비어 있으면 기존처럼 전체 timeline을 반환한다.
- `pageSize`가 있으면 `sortDirection` 기준 앞쪽부터 최대 개수만 반환한다.
- `cursorAppliedAt`가 있으면 `sortDirection` 기준 다음 window만 반환한다.
- `cursorAppliedAt`는 배타 경계로 해석한다.
  - `ASC`면 `appliedAt > cursorAppliedAt`
  - `DESC`면 `appliedAt < cursorAppliedAt`
- 같은 `appliedAt` row는 `id`를 같은 방향의 2차 정렬 기준으로 사용하고, `cursorAppliedAt`와 같은 시각 row는 다음 window에서 모두 제외한다.
- 기존 `action`, `actionStatus`, `appliedAtFrom`, `appliedAtTo` 필터와 새 정렬/페이지 기준은 함께 동작해야 한다.
- representative actual app/H2 검증에서 ascending/descending 응답, page window 응답, H2 audit evidence가 같은 execution 기준으로 일치하는지 확인한다.

### 비대상
- repository 또는 PR 단위 history 검색
- action/actionStatus/appliedAt 범위 외 추가 검색 필드
- 사용자용 UI
- bulk action
- 장기 저장소 도입

### 외부 계약
- 기존 `GET /reviews/rerun/{executionKey}/actions/history` 경로는 유지한다.
- 정렬/페이지 입력을 주지 않으면 기존과 동일하게 전체 timeline을 반환한다.
- action/query/list/action response의 기존 응답 구조와 의미는 유지한다.
- `appliedAtFrom`, `appliedAtTo`, `cursorAppliedAt`는 ISO-8601 날짜-시간 문자열로 읽는다.
- representative actual app/H2 검증은 `cursorAppliedAt` 경계가 흔들리지 않도록 서로 다른 `appliedAt` 값을 가진 action timeline을 사용한다.

### 핵심 시나리오
1. 관리자 액션 이력 정렬과 페이지 기준 안전망 고정
   - 기존 history 응답, action 응답, query/list 상태 계약이 정렬/페이지 기준 확장 전에도 유지된다는 점을 먼저 고정한다.
   - 이미 충분한 안전망이 있으면 그 근거를 남기고, 부족한 경우만 최소 테스트를 추가한다.
2. 관리자 액션 이력 정렬과 페이지 기준 입력 모델과 진입점 도입
   - history 조회가 `sortDirection`, `pageSize`, `cursorAppliedAt` query param을 읽도록 controller/service request 경계를 연다.
   - `pageSize` 또는 `cursorAppliedAt`만 있고 `sortDirection`이 비어 있으면 `DESC`로 해석하는 기준을 고정한다.
   - 이 단계는 입력 해석과 최소 응답 경계까지만 닫고, 실제 audit selection은 다음 단계로 넘긴다.
3. 관리자 액션 이력 정렬과 page selection 연결
   - repository/service가 `sortDirection`, `pageSize`, `cursorAppliedAt` 기준을 실제 audit row 선택에 반영하도록 연결한다.
   - `cursorAppliedAt`는 배타 경계로 해석하고, 같은 `appliedAt` row는 다음 window에서 제외한다.
   - 입력이 없으면 기존 전체 timeline 의미를 유지하고, 입력이 있으면 정렬과 page window가 반영된 subset을 반환하는지 고정한다.
   - 없는 execution은 기존 not-found 의미를 유지하고, execution은 있지만 page window 결과가 0건이면 빈 timeline을 반환하는지 고정한다.
4. 정렬과 페이지 기준 실행 근거 정합성 검증
   - representative execution에서 ascending/descending history, page window history, H2 audit evidence가 같은 execution 기준으로 같은 결과를 가지는지 확인한다.

### Task 분해 기준
- `TASK-0001` 관리자 액션 이력 정렬과 페이지 기준 안전망 고정
- `TASK-0002` 관리자 액션 이력 정렬과 페이지 기준 입력 모델과 진입점 도입
- `TASK-0003` 관리자 액션 이력 정렬과 page selection 연결
- `TASK-0004` 정렬과 페이지 기준 실행 근거 정합성 검증

### 연결될 ValidationCriteria
- `manual-rerun-control-history-order-page-contract-preserved`
- `manual-rerun-control-history-order-page-input-defined`
- `manual-rerun-control-history-order-page-maps-audit-selection`
- `manual-rerun-control-history-order-page-runtime-evidence-aligned`

### 필수 테스트 시나리오
- 정렬/페이지 기준을 도입해도 action 응답, query/list, history 기본 응답 구조는 유지된다.
- 정렬/페이지 입력이 비어 있으면 전체 timeline을 반환한다.
- `pageSize` 또는 `cursorAppliedAt`만 있고 `sortDirection`이 비어 있으면 `DESC`로 해석한다.
- `cursorAppliedAt`는 배타 경계로 동작하고, `sortDirection`, `pageSize`, `cursorAppliedAt` 입력은 audit row 선택에 그대로 반영된다.
- representative execution에서 ascending/descending history, page window history, H2 audit evidence가 같은 execution 기준으로 같은 결과를 가진다.

## 후속 Spec 후보
### ID
SPEC-0019

### 이름
운영용 목록 조회와 이력 조회 조합 정리

### 시작 조건
- `현재 활성 Spec`이 완료되고, history 정렬과 page 기준이 안정적으로 닫힌 뒤 시작한다.

### 목표
- 운영자가 실행 목록과 관리자 액션 이력을 함께 볼 때, 현재 상태 요약과 timeline 조회 경계가 더 자연스럽게 읽히도록 조합 기준을 정리한다.

### 후속 변경 범위
- 목록 응답과 history 응답 조합 기준 정리
- 현재 상태 요약과 과거 이력 링크 기준 정리
- 운영용 조회 응답 문서화 보강

### 후속 변경 비대상
- 사용자용 UI
- bulk action
- 장기 저장소 도입

### 후속 검증 방향
- 목록과 history를 함께 써도 현재 상태 요약과 timeline 의미가 충돌하지 않는다.
