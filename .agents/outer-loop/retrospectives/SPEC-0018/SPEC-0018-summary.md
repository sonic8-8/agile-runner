---
spec_id: SPEC-0018
summary_status: completed
generated_at: 2026-04-11T23:03:30+09:00
task_ids:
  - TASK-0001
  - TASK-0002
  - TASK-0003
  - TASK-0004
proposal_ids:
  - WORKFLOW-PROP-0013
---

# Spec 요약

## 범위 요약
- `SPEC-0018 운영용 관리자 제어 이력 정렬과 페이지 기준 정리`는 기존 관리자 액션 history 조회에 `sortDirection`, `pageSize`, `cursorAppliedAt`을 추가하고, 정렬과 page window 의미가 실제 audit row selection으로 이어지도록 정리하는 데 집중했다.
- 이번 spec도 새 endpoint를 만들지 않고, 기존 `GET /reviews/rerun/{executionKey}/actions/history` 경계 안에서 입력 모델과 selection 의미를 확장했다.

## 완료된 Task
- `TASK-0001` 관리자 액션 이력 정렬과 페이지 기준 안전망 고정
- `TASK-0002` 관리자 액션 이력 정렬과 페이지 기준 입력 모델과 진입점 도입
- `TASK-0003` 관리자 액션 이력 정렬과 page selection 연결
- `TASK-0004` 정렬과 페이지 기준 실행 근거 정합성 검증

## 주요 결과
- history 조회는 이제 `sortDirection`, `pageSize`, `cursorAppliedAt`를 읽고, page 입력만 있을 때는 `DESC` 기본 해석을 사용한다.
- service와 repository는 정렬 방향, page size, 배타 cursor를 실제 audit row subset selection으로 연결한다.
- representative actual app/H2 검증에서 full ASC timeline, explicit DESC page, 기본 DESC page, cursor 다음 page, 빈 window가 같은 execution 기준으로 H2 audit evidence와 일치했다.

## 검증 요약
- targeted test: 통과
- full `cleanTest test`: 통과
- representative actual app/H2 verification: 통과

## 이번 spec에서 얻은 점
- history read 확장은 입력 경계와 selection seam을 먼저 분리해 닫고, 마지막에 representative verification으로 실제 HTTP subset과 H2 audit subset을 맞추는 구성이 안정적이다.
- page window representative verification에서는 `sortDirection` 없는 기본 `DESC`, explicit `DESC`, cursor 다음 page, 빈 window까지 함께 확인해야 의미가 선명하다.

## 남은 위험
- 현재 cursor 경계는 `appliedAt`만 사용하므로, 같은 `appliedAt`를 공유하는 audit row가 많은 경우 representative verification에서도 2차 정렬 기준을 더 신경 써야 한다.
- local H2 file DB는 Shell 조회를 병렬로 띄우면 file lock false negative가 날 수 있다.

## 다음 spec 후보
- `SPEC-0019 운영용 목록 조회와 이력 조회 조합 정리`
