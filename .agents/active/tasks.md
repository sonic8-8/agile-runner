# 현재 활성 Task 집합

## 문서 목적
이 문서는 현재 활성 `Spec`을 실제 구현 단위로 분해한 `Task` 문서다.
각 `Task`는 하나의 명확한 결과, 검증 기준, GitHub Issue 연결 규칙을 가져야 한다.

## 현재 활성 Spec
- ID: `SPEC-0025`
- 이름: `운영용 조회 응답 준비 데이터 실제 SQL 보강`
- 기준 문서:
  - `.agents/active/spec.md`
  - `.agents/criteria/SPEC-0025-manual-rerun-seed-real-sql.json`

## 공통 규칙
- 구현 순서는 `TASK-0001 -> TASK-0002 -> TASK-0003 -> TASK-0004`로 고정한다.
- 각 `Task` 시작 전 해당 task용 GitHub Issue를 새로 연결한다.
- 각 `Task`는 연결된 `ValidationCriteria`와 테스트 근거가 없으면 완료로 보지 않는다.
- 기존 `POST /reviews/rerun`, `POST /reviews/rerun/{executionKey}/retry`, `GET /reviews/rerun/{executionKey}`, `GET /reviews/rerun/executions`, `GET /reviews/rerun/{executionKey}/actions/history`, `POST /reviews/rerun/{executionKey}/actions` 계약은 유지한다.
- 이번 단계는 준비 데이터 예시 SQL 파일을 실제로 쓸 수 있는 최소 실행 가능 상태로 올리고 대표 검증과 직접 연결하는 데 집중한다.
- `TASK-0001` 시작 전 현재 회고와 대표 검증 기반이 실제 SQL 보강 단계의 기존 안전망으로 충분한지 먼저 검토한다.

## 요약 표
| Task | 이름 | 핵심 목표 | 연결 ValidationCriteria | 핵심 검증 | Issue |
| --- | --- | --- | --- | --- | --- |
| `TASK-0001` | 실제 SQL 보강 시작 전 기존 근거 확인 | 실제 SQL 보강 시작 전에 기존 대표 검증 근거와 자동 검증 유지 확인 | `manual-rerun-seed-safety-net-preserved` | 회귀 테스트 + 대표 검증 회고 근거 확인 | 새 Issue |
| `TASK-0002` | 입력 준비 데이터 실제 SQL 보강 | 준비 데이터 입력 SQL을 실제 실행 가능한 형태로 보강 | `manual-rerun-seed-input-sql-executable` | SQL 실행 근거 + 관련 테스트 | 새 Issue |
| `TASK-0003` | 실행 근거 확인 SQL과 대표 검증 절차 연결 | 실행 근거 확인 SQL과 대표 검증 절차 연결 | `manual-rerun-seed-evidence-sql-executable` | SQL 실행 근거 + 관련 테스트 | 새 Issue |
| `TASK-0004` | 실제 준비 데이터 적용 검증과 문서 마감 | 실제 대표 검증으로 준비 데이터 SQL 실행 가능성 확인 | `manual-rerun-seed-representative-application-verified` | 대상 테스트 + 전체 테스트 + 실제 앱/H2 검증 | 새 Issue |

## TASK-0001
### 이름
실제 SQL 보강 시작 전 기존 근거 확인

### 목표
- 준비 데이터 실제 SQL 보강 단계를 시작하기 전에 대표 검증 회고와 자동 검증이 현재 단계의 시작 근거로 충분한지 확인한다.

### 구현 범위
- 기존 회고와 관련 controller/service 테스트를 우선 재사용한다.
- 아래 기준을 먼저 확인한다.
  - 대표 검증에서 어떤 준비 데이터가 실제로 필요했는지 정리
  - schema나 enum 값 어긋남이 준비 오류를 만들었던 사례 정리
  - 자동 검증 테스트 유지
  - rerun/retry/단건 조회/목록 조회/이력 조회/관리자 조치 대표 검증 근거 유지
- 이 작업은 준비 데이터 가이드 문서를 바꾸는 단계가 아니라, 실제 SQL 보강 전에 기존 대표 검증 근거가 충분한지 재확인하는 단계로 본다.
- 기존 안전망이 충분하면 근거를 회고에 남기고, 부족한 경우만 최소 문서/테스트 보강을 검토한다.

### 비대상
- 예시 SQL 실제 SQL 보강
- 대표 실제 앱/H2 검증 재실행

### 연결 ValidationCriteria
- `manual-rerun-seed-safety-net-preserved`

### 완료 조건
- 실제 SQL 보강을 시작하기 전에도 대표 검증 근거와 자동 검증 테스트가 시작 근거로 충분하다는 근거가 남는다.
- 대표 검증 근거 자료 경로가 회고 문서에 명시적으로 남는다.

### 검증
- 관련 대상 테스트 실행 통과
- 전체 `cleanTest 테스트` 통과
- 기존 대표 검증 회고 경로 명시
  - `SPEC-0020 / TASK-0004`
  - 필요 시 `SPEC-0023`, `SPEC-0024` 요약 문서를 보조 근거로 함께 참조

### GitHub Issue
- 새 Issue 생성
- 권장 제목: `[BE] 실제 SQL 보강 시작 전 기존 근거 확인`

## TASK-0002
### 이름
입력 준비 데이터 실제 SQL 보강

### 목표
- retry 원본 실행 준비 데이터와 rerun 조치 이력 준비 데이터를 실제 로컬 H2 준비에 쓸 수 있는 최소 실행 가능 SQL로 보강한다.

### 구현 범위
- 현재 준비 데이터 가이드와 schema, enum 값을 기준으로 입력용 준비 데이터 파일에 실제 SQL을 넣는다.
- 아래 기준을 최소 포함한다.
  - retry 원본 실행 준비 데이터 최소 INSERT
  - rerun acknowledge 또는 unacknowledge 준비에 필요한 관리자 조치 이력 최소 INSERT
  - 현재 schema 컬럼과 enum 값에 맞는 SQL
  - 대표 검증 절차에서 준비 데이터 파일을 어떤 순서로 적용하는지 연결 가능한 수준의 주석
- 필요하면 준비 데이터 가이드 문구를 실제 SQL 단계에 맞게 최소 보정한다.

### 관련 파일 후보
- `docs/manual-rerun-response-seed-guide.md`
- `src/test/resources/manual-rerun-response-seed/source-execution/retry-source-execution-seed.example.sql`
- `src/test/resources/manual-rerun-response-seed/control-action-history/rerun-acknowledge-action-history-seed.example.sql`
- `src/main/resources/agent-runtime/schema.sql`

### 비대상
- runtime evidence check SQL 보강
- 실제 앱/H2 대표 검증 재실행

### 연결 ValidationCriteria
- `manual-rerun-seed-input-sql-executable`

### 완료 조건
- 입력 준비 데이터 파일이 현재 schema와 enum 값에 맞는 실제 SQL을 갖고 있고, 대표 검증 준비에 쓸 수 있는 최소 실행 가능 상태가 된다.

### 검증
- 로컬 H2 또는 H2 메모리 DB 기준 SQL 실행 근거
- 관련 대상 테스트 실행 통과

### GitHub Issue
- 새 Issue 생성
- 권장 제목: `[BE] 입력 준비 데이터 실제 SQL 보강`

## TASK-0003
### 이름
실행 근거 확인 SQL과 대표 검증 절차 연결

### 목표
- rerun/retry 실행 근거 확인 SQL 파일을 실제 SELECT로 보강하고, 준비 데이터 적용과 대표 검증 절차를 직접 연결한다.

### 구현 범위
- 아래 기준을 파일과 문서에 반영한다.
  - rerun 실행 근거 확인 SQL 실제 SELECT
  - retry 실행 근거 확인 SQL 실제 SELECT
  - 대표 검증 실행 키 기준으로 결과 행을 어떻게 찾는지
  - 필요한 경우 준비 데이터 가이드의 시나리오별 파일 선택 기준과 적용 순서 문구 보정
- 필요하면 repository나 H2 메모리 DB 수준 검증 근거를 추가한다.

### 관련 파일 후보
- `docs/manual-rerun-response-seed-guide.md`
- `src/test/resources/manual-rerun-response-seed/runtime-evidence/rerun-runtime-evidence-check.example.sql`
- `src/test/resources/manual-rerun-response-seed/runtime-evidence/retry-runtime-evidence-check.example.sql`
- `src/main/resources/agent-runtime/schema.sql`

### 비대상
- 실제 앱/H2 대표 검증 재실행
- 새 응답 필드 추가
- 준비 데이터 파일 이름 규칙 변경

### 연결 ValidationCriteria
- `manual-rerun-seed-evidence-sql-executable`

### 완료 조건
- 실행 근거 확인 SQL 파일이 실제 대표 검증 결과 행을 찾을 수 있는 최소 SELECT를 갖고 있고, 대표 검증 절차와 연결된다.

### 검증
- 로컬 H2 또는 H2 메모리 DB 기준 SQL 실행 근거
- 관련 대상 테스트 실행 통과

### GitHub Issue
- 새 Issue 생성
- 권장 제목: `[BE] 실행 근거 확인 SQL과 대표 검증 절차 연결`

## TASK-0004
### 이름
실제 준비 데이터 적용 검증과 문서 마감

### 목표
- 실제 대표 검증에서 준비 데이터 SQL을 적용하고, 대표 응답과 H2 evidence가 준비 데이터 가이드와 모순되지 않는지 확인하고 spec을 마감한다.

### 구현 범위
- 대표 검증 시나리오를 정해 준비 데이터 SQL을 실제로 적용한다.
- 앱 기동, 대표 요청 실행, 앱 종료, H2 실행 근거 확인까지 규칙대로 수행한다.
- 대상 테스트와 전체 `cleanTest 테스트`를 다시 돌려 준비 데이터 SQL 보강으로 기존 자동 검증 흐름이 깨지지 않았는지 확인한다.
- 필요하면 준비 데이터 가이드와 예시 SQL 문구를 최소 보정한다.

### 관련 파일 후보
- `docs/manual-rerun-response-seed-guide.md`
- `src/test/resources/manual-rerun-response-seed/`
- `src/main/resources/agent-runtime/schema.sql`

### 비대상
- 새 endpoint 추가
- 기준 파일 자동 생성

### 연결 ValidationCriteria
- `manual-rerun-seed-representative-application-verified`

### 완료 조건
- 대표 실제 앱/H2 검증에서 준비 데이터 SQL을 실제로 적용할 수 있다.
- 대표 결과와 H2 실행 근거 확인 SQL이 같은 실행 키 기준으로 연결된다.
- 준비 데이터 SQL 보강 후에도 대상 테스트와 전체 `cleanTest 테스트`에서 기존 자동 검증 흐름이 유지된다.

### 검증
- 대상 테스트
- 전체 `cleanTest 테스트`
- 실제 앱/H2 대표 검증

### GitHub Issue
- 새 Issue 생성
- 권장 제목: `[BE] 실제 준비 데이터 적용 검증과 문서 마감`
