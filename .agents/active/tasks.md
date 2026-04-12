# 현재 활성 Task 집합

## 문서 목적
이 문서는 현재 활성 `Spec`을 실제 구현 단위로 분해한 `Task` 문서다.
각 `Task`는 하나의 명확한 결과, 검증 기준, GitHub Issue 연결 규칙을 가져야 한다.

## 현재 활성 Spec
- ID: `SPEC-0024`
- 이름: `운영용 조회 응답 준비 데이터 적용 절차 정리`
- 기준 문서:
  - `.agents/active/spec.md`
  - `.agents/criteria/SPEC-0024-manual-rerun-seed-application-procedure.json`

## 공통 규칙
- 구현 순서는 `TASK-0001 -> TASK-0002 -> TASK-0003 -> TASK-0004`로 고정한다.
- 각 `Task` 시작 전 해당 task용 GitHub Issue를 새로 연결한다.
- 각 `Task`는 연결된 `ValidationCriteria`와 테스트 근거가 없으면 완료로 보지 않는다.
- 기존 `POST /reviews/rerun`, `POST /reviews/rerun/{executionKey}/retry`, `GET /reviews/rerun/{executionKey}`, `GET /reviews/rerun/executions`, `GET /reviews/rerun/{executionKey}/actions/history`, `POST /reviews/rerun/{executionKey}/actions` 계약은 유지한다.
- 이번 spec은 대표 실제 앱/H2 검증 전에 준비 데이터를 어떤 순서로 적용하고 무엇을 먼저 점검하는지 문서로 고정하는 데 집중한다.
- `TASK-0001` 시작 전 현재 회고와 자동 검증 기반이 준비 데이터 적용 절차 spec의 기존 안전망으로 충분한지 먼저 검토한다.

## 요약 표
| Task | 이름 | 핵심 목표 | 연결 ValidationCriteria | 핵심 검증 | Issue |
| --- | --- | --- | --- | --- | --- |
| `TASK-0001` | 준비 데이터 적용 절차 안전망 확인 | 현재 대표 검증 절차 근거 유지 확인 | `manual-rerun-seed-application-safety-net-preserved` | 회귀 테스트 + 회고 근거 확인 | 새 Issue |
| `TASK-0002` | 준비 데이터 적용 순서와 명령 기준 정리 | 준비 데이터 적용 순서와 명령 기준 고정 | `manual-rerun-seed-application-order-defined` | 문서 리뷰 | 새 Issue |
| `TASK-0003` | schema와 enum 값 점검 순서, 대표 검증 체크리스트 정리 | 대표 검증 전 점검 체크리스트 고정 | `manual-rerun-seed-precheck-checklist-defined` | 문서 리뷰 | 새 Issue |
| `TASK-0004` | 준비 데이터 적용 절차 문서 마감과 정합성 확인 | 새 작업자가 절차 문서를 따라갈 수 있는지 확인하고 마감 | `manual-rerun-seed-application-guide-readiness-verified` | targeted/full test + 문서 검토 | 새 Issue |

## TASK-0001
### 이름
준비 데이터 적용 절차 안전망 확인

### 목표
- 준비 데이터 적용 절차 spec을 시작하기 전에 대표 검증에서 어떤 적용 순서와 점검 항목이 실제로 필요했는지와 기존 근거가 이미 충분한 기존 안전망인지 확인한다.

### 구현 범위
- 기존 회고와 관련 controller/service 테스트를 우선 재사용한다.
- 아래 기준을 먼저 확인한다.
  - 대표 검증에서 어떤 적용 순서가 실제로 필요했는지 정리
  - schema나 enum 값 점검 누락이 준비 오류를 만들었던 사례 정리
  - 자동 검증 테스트 유지
  - rerun/retry/query/list/history/action 대표 검증 근거 유지
- 기존 안전망이 충분하면 근거를 회고에 남기고, 부족한 경우만 최소 문서/테스트 보강을 검토한다.

### 비대상
- 준비 데이터 적용 순서 정의
- 점검 체크리스트 정의
- 실제 앱/H2 대표 검증 재실행

### 연결 ValidationCriteria
- `manual-rerun-seed-application-safety-net-preserved`

### 완료 조건
- 준비 데이터 적용 절차를 정리하기 전에도 대표 검증 근거와 자동 검증 테스트가 기존 안전망으로 충분하다는 근거가 남는다.

### 검증
- 관련 targeted test 실행 통과
- full `cleanTest test` 통과

### GitHub Issue
- 새 Issue 생성
- 권장 제목: `[BE] 준비 데이터 적용 절차 안전망 확인`

## TASK-0002
### 이름
준비 데이터 적용 순서와 명령 기준 정리

### 목표
- 대표 검증 전에 준비 데이터 파일을 어떤 순서로 적용하는지와 어떤 명령 단위로 다루는지 새 작업자도 바로 읽을 수 있게 정리한다.

### 구현 범위
- 현재 준비 데이터 가이드와 준비 데이터 파일을 기준으로 적용 순서를 문서에 적는다.
- 아래 기준을 최소 포함한다.
  - retry 원본 실행 준비 데이터를 언제 먼저 넣는가
  - 관리자 조치 이력 준비 데이터를 언제 넣는가
  - 실행 근거 확인 SQL을 언제 실행하는가
  - 앱 기동 전과 앱 종료 후로 나뉘는 명령 기준
- 필요하면 준비 데이터 가이드 또는 별도 운영 문서에 절차 섹션을 추가한다.

### 관련 파일 후보
- `docs/manual-rerun-response-seed-guide.md`
- `src/test/resources/manual-rerun-response-seed/`

### 비대상
- schema와 enum 점검 체크리스트 상세화
- 기준 파일 규칙 변경
- 실제 앱/H2 대표 검증 재실행

### 연결 ValidationCriteria
- `manual-rerun-seed-application-order-defined`

### 완료 조건
- 준비 데이터 적용 순서와 명령 기준을 문서와 준비 데이터 파일만 읽고 이해할 수 있다.

### 검증
- 문서 리뷰

### GitHub Issue
- 새 Issue 생성
- 권장 제목: `[BE] 준비 데이터 적용 순서와 명령 기준 정리`

## TASK-0003
### 이름
schema와 enum 값 점검 순서, 대표 검증 체크리스트 정리

### 목표
- 준비 데이터를 적용하기 전에 schema와 enum 값을 어떤 순서로 확인해야 하는지와 대표 검증 전에 빠뜨리면 안 되는 점검 항목을 분명히 한다.
- 예: `schema 확인 -> enum 값 확인 -> 준비 데이터 적용 -> 앱 기동` 같은 기본 순서를 문서에서 바로 읽을 수 있게 한다.

### 구현 범위
- 아래 기준을 문서로 정리한다.
  - schema.sql과 enum 값을 어떤 순서로 확인하는가
  - 준비 데이터 적용 전에 확인할 체크리스트
  - 앱 기동 전 확인 항목과 앱 종료 후 확인 항목을 어떻게 구분하는가
  - 대표 검증 중 lock이나 준비 데이터 오류가 나면 무엇부터 의심해야 하는가
- 필요하면 준비 데이터 가이드 또는 관련 운영 문서에 체크리스트 섹션을 보강한다.

### 관련 파일 후보
- `docs/manual-rerun-response-guide.md`
- `docs/manual-rerun-response-seed-guide.md`
- `src/test/resources/manual-rerun-response-seed/`

### 비대상
- 실제 앱/H2 대표 검증 재검증
- 새 응답 필드 추가
- 준비 데이터 파일 이름 규칙 변경

### 연결 ValidationCriteria
- `manual-rerun-seed-precheck-checklist-defined`

### 완료 조건
- schema와 enum 값 점검 순서, 대표 검증 체크리스트가 문서에서 바로 읽힌다.

### 검증
- 문서 리뷰

### GitHub Issue
- 새 Issue 생성
- 권장 제목: `[BE] schema와 enum 값 점검 순서, 대표 검증 체크리스트 정리`

## TASK-0004
### 이름
준비 데이터 적용 절차 문서 마감과 정합성 확인

### 목표
- 새 작업자가 문서와 준비 데이터 파일만 읽고 대표 검증 준비 절차를 따라갈 수 있는지 확인하고 spec을 마감한다.

### 구현 범위
- 준비 데이터 가이드와 관련 문서, 준비 데이터 파일을 함께 검토한다.
- targeted test와 full `cleanTest test`를 다시 돌려 절차 문서 정리로 기존 자동 검증 흐름이 깨지지 않았는지 확인한다.
- 필요하면 문구를 최소 보정한다.
- 이번 spec은 준비 데이터 적용 절차와 체크리스트 정리 중심이므로 대표 실제 앱/H2 검증은 비대상으로 둔다.
  - 이유: 대표 실제 앱/H2 검증 자체는 기존 운영 조회 spec들에서 이미 닫았고, 이번 spec은 그 준비 절차를 반복 가능하게 정리하는 단계이기 때문이다.
  - 따라서 이번 task는 절차 문서와 체크리스트를 닫는 단계로 해석하고, 대표 실제 앱/H2 검증 생략 사유를 회고와 spec summary에 함께 남긴다.

### 관련 파일 후보
- `docs/manual-rerun-response-seed-guide.md`
- `src/test/resources/manual-rerun-response-seed/`

### 비대상
- 새 endpoint 추가
- 실제 앱/H2 대표 검증 재검증

### 연결 ValidationCriteria
- `manual-rerun-seed-application-guide-readiness-verified`

### 완료 조건
- 새 작업자가 문서와 준비 데이터 파일만 읽고 대표 검증 준비 절차를 따라갈 수 있다.
- 문서 정리 후에도 targeted test와 full `cleanTest test`에서 기존 자동 검증 흐름이 유지된다.

### 검증
- targeted test
- full `cleanTest test`
- 문서 검토

### GitHub Issue
- 새 Issue 생성
- 권장 제목: `[BE] 준비 데이터 적용 절차 문서 마감과 정합성 확인`
