# 현재 활성 Task 집합

## 문서 목적
이 문서는 현재 활성 `Spec`을 실제 구현 단위로 분해한 `Task` 문서다.
각 `Task`는 하나의 명확한 결과, 검증 기준, GitHub Issue 연결 규칙을 가져야 한다.

## 현재 활성 Spec
- ID: `SPEC-0022`
- 이름: `운영용 조회 응답 문서와 기준 파일 생성 규칙 정리`
- 기준 문서:
  - `.agents/active/spec.md`
  - `.agents/criteria/SPEC-0022-manual-rerun-fixture-governance.json`

## 공통 규칙
- 구현 순서는 `TASK-0001 -> TASK-0002 -> TASK-0003 -> TASK-0004`로 고정한다.
- 각 `Task` 시작 전 해당 task용 GitHub Issue를 새로 연결한다.
- 각 `Task`는 연결된 `ValidationCriteria`와 테스트 근거가 없으면 완료로 보지 않는다.
- 기존 `POST /reviews/rerun`, `POST /reviews/rerun/{executionKey}/retry`, `GET /reviews/rerun/{executionKey}`, `GET /reviews/rerun/executions`, `GET /reviews/rerun/{executionKey}/actions/history`, `POST /reviews/rerun/{executionKey}/actions` 계약은 유지한다.
- 이번 spec은 운영용 조회 응답 guide와 기준 파일, 자동 검증 테스트를 어떻게 관리하는지 문서 규칙을 고정하는 데 집중한다.
- `TASK-0001` 시작 전 현재 guide, 기준 파일, 자동 검증 테스트가 safety-net으로 충분한지 먼저 검토한다.

## 요약 표
| Task | 이름 | 핵심 목표 | 연결 ValidationCriteria | 핵심 검증 | Issue |
| --- | --- | --- | --- | --- | --- |
| `TASK-0001` | 기준 파일 생성 규칙 안전망 확인 | 현재 자동 검증 기반 유지 확인 | `manual-rerun-fixture-governance-safety-net-preserved` | 회귀 테스트 | 새 Issue |
| `TASK-0002` | 기준 파일 이름과 파일 단위 규칙 정리 | 파일 이름과 파일 위치 기준 고정 | `manual-rerun-fixture-naming-rules-defined` | 문서 리뷰 + 테스트 구조 확인 | 새 Issue |
| `TASK-0003` | 기준 파일 갱신 절차와 대표 검증 경계 정리 | guide, 기준 파일, 테스트 갱신 순서와 실제 앱 검증 경계 고정 | `manual-rerun-fixture-update-boundary-defined` | 문서 리뷰 | 새 Issue |
| `TASK-0004` | 규칙 문서 마감과 정합성 확인 | 새 작업자가 규칙을 따라갈 수 있는지 확인하고 마감 | `manual-rerun-fixture-guide-readiness-verified` | targeted/full test + 문서 검토 | 새 Issue |

## TASK-0001
### 이름
기준 파일 생성 규칙 안전망 확인

### 목표
- 기준 파일 운영 규칙 spec을 시작하기 전에 현재 guide, 기준 파일, 자동 검증 테스트가 이미 충분한 safety-net인지 확인한다.

### 구현 범위
- 기존 `ManualRerunResponseGuideFixtureTest`와 관련 controller/service 테스트를 우선 재사용한다.
- 아래 기준을 먼저 확인한다.
  - guide와 기준 파일 경로 연결 유지
  - 자동 검증 테스트 유지
  - rerun/retry/query/list/history/action 예시 drift 보호 유지
- 기존 안전망이 충분하면 근거를 회고에 남기고, 부족한 경우만 최소 문서/테스트 보강을 검토한다.

### 비대상
- 기준 파일 이름 규칙 정의
- 기준 파일 갱신 절차 정의
- 대표 검증 경계 문서화

### 연결 ValidationCriteria
- `manual-rerun-fixture-governance-safety-net-preserved`

### 완료 조건
- 기준 파일 운영 규칙을 정리하기 전에도 현재 guide, 기준 파일, 자동 검증 테스트가 회귀 안전망으로 충분하다는 근거가 남는다.

### 검증
- 관련 targeted test 실행 통과
- full `cleanTest test` 통과

### GitHub Issue
- 새 Issue 생성
- 권장 제목: `[BE] 기준 파일 생성 규칙 안전망 확인`

## TASK-0002
### 이름
기준 파일 이름과 파일 단위 규칙 정리

### 목표
- 운영용 조회 응답 기준 파일 이름과 파일 단위 기준을 새 작업자도 바로 읽을 수 있게 정리한다.

### 구현 범위
- 현재 기준 파일 이름을 기준으로 이름 규칙을 문서에 적는다.
- 아래 기준을 최소 포함한다.
  - 응답 종류를 이름에 어떻게 드러내는가
  - 같은 execution의 조치 전/후 시점을 이름에 어떻게 드러내는가
  - 단건 응답과 목록 row 예시를 어떤 파일 단위로 나누는가
- 필요하면 guide 또는 별도 운영 문서에 기준 파일 디렉토리 역할을 보강한다.

### 관련 파일 후보
- `docs/manual-rerun-response-guide.md`
- `src/test/resources/manual-rerun-response-guide/`
- `src/test/java/com/agilerunner/api/controller/review/ManualRerunResponseGuideFixtureTest.java`

### 비대상
- 새 기준 파일 자동 생성
- 대표 실제 앱 검증 변경

### 연결 ValidationCriteria
- `manual-rerun-fixture-naming-rules-defined`

### 완료 조건
- 기준 파일 이름과 파일 단위 기준을 문서만 읽고 이해할 수 있다.

### 검증
- 문서 리뷰
- 테스트 구조 리뷰

### GitHub Issue
- 새 Issue 생성
- 권장 제목: `[BE] 기준 파일 이름과 파일 단위 규칙 정리`

## TASK-0003
### 이름
기준 파일 갱신 절차와 대표 검증 경계 정리

### 목표
- guide, 기준 파일, 자동 검증 테스트를 어떤 순서로 함께 수정해야 하는지와 대표 실제 앱 검증과의 경계를 분명히 한다.

### 구현 범위
- 아래 기준을 문서로 정리한다.
  - guide를 수정할 때 같이 봐야 하는 파일
  - 기준 파일을 추가/수정할 때 같이 갱신할 테스트
  - 대표 실제 앱 검증 결과를 언제 기준 파일로 바로 옮기지 않는가
  - 같은 execution의 조치 전/후 시점을 문서와 기준 파일에서 어떻게 분리하는가
- 필요하면 운영용 응답 guide 또는 별도 운영 문서에 절차 섹션을 보강한다.

### 관련 파일 후보
- `docs/manual-rerun-response-guide.md`
- `src/test/resources/manual-rerun-response-guide/`
- `src/test/java/com/agilerunner/api/controller/review/ManualRerunResponseGuideFixtureTest.java`

### 비대상
- actual app/H2 대표 검증 재검증
- 새 응답 필드 추가

### 연결 ValidationCriteria
- `manual-rerun-fixture-update-boundary-defined`

### 완료 조건
- guide, 기준 파일, 자동 검증 테스트를 함께 수정하는 절차와 대표 검증 경계가 문서에서 바로 읽힌다.

### 검증
- 문서 리뷰

### GitHub Issue
- 새 Issue 생성
- 권장 제목: `[BE] 기준 파일 갱신 절차와 대표 검증 경계 정리`

## TASK-0004
### 이름
규칙 문서 마감과 정합성 확인

### 목표
- 새 작업자가 문서만 읽고 기준 파일 생성/갱신 규칙을 따라갈 수 있는지 확인하고 spec을 마감한다.

### 구현 범위
- guide와 관련 문서를 함께 검토한다.
- targeted test와 full `cleanTest test`를 다시 돌려 문서 규칙 정리로 기존 자동 검증 흐름이 깨지지 않았는지 확인한다.
- 필요하면 문구를 최소 보정한다.
- 이번 spec은 문서/운영 규칙 정리 중심이므로 actual app/H2 대표 검증은 비대상으로 둔다.
  - 이유: 대표 실제 앱 검증은 `SPEC-0020`에서 응답 의미 정합성을, `SPEC-0021`에서 예시 drift 보호를 각각 닫았기 때문이다.
  - 따라서 이번 task는 문서 규칙과 유지 절차를 닫는 단계로 해석하고, actual app/H2 대표 검증 생략 사유를 retrospective와 spec summary에 함께 남긴다.

### 관련 파일 후보
- `docs/manual-rerun-response-guide.md`
- `src/test/resources/manual-rerun-response-guide/`
- `src/test/java/com/agilerunner/api/controller/review/ManualRerunResponseGuideFixtureTest.java`

### 비대상
- 새 endpoint 추가
- actual app/H2 대표 검증 재검증

### 연결 ValidationCriteria
- `manual-rerun-fixture-guide-readiness-verified`

### 완료 조건
- 새 작업자가 문서만 읽고 기준 파일 생성/갱신 규칙을 따라갈 수 있다.
- 문서 정리 후에도 targeted test와 full `cleanTest test`에서 기존 자동 검증 흐름이 유지된다.

### 검증
- targeted test
- full `cleanTest test`
- 문서 검토

### GitHub Issue
- 새 Issue 생성
- 권장 제목: `[BE] 기준 파일 생성 규칙 문서 마감과 정합성 확인`
