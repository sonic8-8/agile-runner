# 현재 활성 Task 집합

## 문서 목적
이 문서는 현재 활성 `Spec`을 실제 구현 단위로 분해한 `Task` 문서다.
각 `Task`는 하나의 명확한 결과, 검증 기준, GitHub Issue 연결 규칙을 가져야 한다.

## 현재 활성 Spec
- ID: `SPEC-0002`
- 이름: `agent-runtime 용어 및 스키마 정렬`
- 기준 문서:
  - `.agents/active/spec.md`
  - `.agents/criteria/SPEC-0002-agent-runtime-terminology-alignment.json`

## 공통 규칙
- 구현 순서는 `TASK-0001 -> TASK-0002 -> TASK-0003`으로 고정한다.
- 각 `Task` 시작 전 해당 task용 GitHub Issue를 새로 연결한다.
- 각 `Task`는 연결된 `ValidationCriteria`와 테스트 근거가 없으면 완료로 보지 않는다.
- rename 작업이더라도 `/webhook/github` 외부 계약과 실제 runtime 적재는 유지 검증이 있어야 한다.
- 리팩터링 spec에서는 `Tester`가 먼저 외부 계약 회귀 안전망을 고정하고, `Constructor`가 그 안전망을 유지한 채 이름 정리를 진행한다.

## 요약 표
| Task | 이름 | 핵심 목표 | 연결 기준 | 핵심 검증 | Issue |
| --- | --- | --- | --- | --- | --- |
| `TASK-0001` | webhook 계약 안전망 고정 | 이름 정리 전에 webhook 외부 계약을 회귀 테스트로 먼저 고정 | `webhook-contract-preserved-after-rename` | controller/service integration 회귀 안전망 | 새 Issue |
| `TASK-0002` | 도메인과 service 용어 정렬 | `ReviewRun`, `TaskState`, `EvaluationCriteria` 계열 이름을 새 용어로 정렬 | `renamed-runtime-terms-consistent` | 도메인/service/repository/test 시그니처 rename 정합성 | 새 Issue |
| `TASK-0003` | repository API, H2 스키마 정렬과 runtime 검증 | repository 저장/조회 API, 테이블, 컬럼 이름을 새 용어로 정렬하고 실제 적재 확인 | `renamed-schema-query-roundtrip-preserved`, `runtime-evidence-recorded-under-renamed-schema` | renamed schema round-trip, targeted/full test, 실제 앱/H2 검증 | 새 Issue |

## TASK-0001
### 이름
webhook 계약 안전망 고정

### 목표
- 이름 정리를 시작하기 전에 기존 webhook 외부 계약을 회귀 테스트로 먼저 고정한다.

### 구현 범위
- rename 이후에도 유지되어야 하는 webhook 외부 계약 테스트를 명시적으로 고정한다.
- 대상은 아래를 우선 포함한다.
  - successful comment 응답 계약
  - duplicate delivery 조기 종료
  - non-`pull_request` 조기 종료
  - `SPEC-0001`에서 고정한 runtime failure non-blocking 경로
- 기존 테스트가 이미 안전망 역할을 하면 그 근거를 정리하고, 부족한 경우만 black-box 테스트를 추가한다.

### 비대상
- 도메인 타입 이름 정리
- H2 물리 테이블과 컬럼 이름 정리
- 예외 체계 도입

### 연결 기준
- `webhook-contract-preserved-after-rename`

### 완료 조건
- 이름 정리 전 현재 webhook 외부 계약이 회귀 테스트로 고정된다.
- 이후 rename task에서 동일 테스트를 그대로 재사용할 수 있다.

### 검증
- controller/service integration 회귀 테스트 실행 통과
- 현재 유지 계약과 테스트 근거 대응 관계 점검

### GitHub Issue
- 새 Issue 생성
- 권장 제목: `[BE] agent-runtime 이름 정리 안전망 고정`

## TASK-0002
### 이름
도메인과 service 용어 정렬

### 목표
- `agent-runtime` 도메인과 service API에서 문서 용어와 다른 이전 이름을 제거한다.

### 구현 범위
- `ReviewRun` -> `WebhookExecution`
- `ReviewRunStatus` -> `WebhookExecutionStatus`
- `TaskState` -> `TaskRuntimeState`
- `TaskStateStatus` -> `TaskRuntimeStatus`
- `EvaluationCriteria` -> `ValidationCriteria`
- controller, service, repository 인터페이스/메서드 시그니처, test 코드에서 위 타입 이름을 새 용어로 맞춘다.
- H2 물리 테이블과 컬럼 이름은 아직 바꾸지 않는다.

### 비대상
- 외부 webhook 응답 계약 변경
- 예외 체계 도입
- H2 물리 테이블과 컬럼 이름 정렬

### 연결 기준
- `renamed-runtime-terms-consistent`

### 완료 조건
- 도메인, service, repository, test 코드에서 이전 이름이 새 이름으로 정렬된다.
- rename 이후 public 시그니처와 테스트 이름이 같은 용어를 사용한다.
- `TASK-0001`에서 고정한 회귀 테스트가 계속 통과한다.

### 검증
- `TASK-0001`에서 확인한 `GitHubWebhookControllerTest`, `GitHubCommentServiceTest`를 rename safety net으로 그대로 재사용한다.
- rename 이후 agent-runtime 관련 unit/integration 테스트 컴파일과 실행 통과
- `ReviewRun`, `TaskState`, `EvaluationCriteria` 계열 이름 잔존 여부 점검

### GitHub Issue
- 새 Issue 생성
- 권장 제목: `[BE] agent-runtime 도메인 용어 정렬`

## TASK-0003
### 이름
repository API, H2 스키마 정렬과 runtime 검증

### 목표
- repository 저장/조회 API와 H2 스키마 이름을 새 용어와 일치시키고, 실제 앱/H2 기준 runtime evidence를 새 이름으로 검증한다.

### 구현 범위
- `TASK_STATE` -> `TASK_RUNTIME_STATE`
- `EVALUATION_CRITERIA` -> `VALIDATION_CRITERIA`
- `REVIEW_RUN` -> `WEBHOOK_EXECUTION`
- `run_key` -> `execution_key`
- `AGENT_EXECUTION_LOG.run_key` -> `AGENT_EXECUTION_LOG.execution_key`
- repository SQL, row mapper, parameter binding, schema에 종속된 조회 메서드 이름을 새 스키마 이름과 맞춘다.
- local profile 실제 앱 기동 후 representative webhook 처리 결과를 새 테이블과 컬럼 이름으로 조회해 검증한다.

### 비대상
- 예외 체계 도입
- webhook 동작 자체의 재설계
- 새로운 feature 도입

### 연결 기준
- `renamed-schema-query-roundtrip-preserved`
- `runtime-evidence-recorded-under-renamed-schema`

### 완료 조건
- repository API 이름과 H2 스키마 이름이 새 용어 기준으로 정렬된다.
- renamed schema 기준 repository round-trip 테스트가 통과한다.
- `TASK-0001`에서 고정한 회귀 테스트와 전체 테스트가 계속 통과한다.
- 전체 테스트와 targeted 테스트가 통과한다.
- 실제 앱 기동 후 representative runtime 적재가 renamed schema 기준으로 확인된다.

### 검증
- repository/schema round-trip 테스트
- controller/service 회귀 테스트
- 저장소 표준 전체 테스트
- local profile 실제 앱 기동 후 H2 file DB 생성과 `WEBHOOK_EXECUTION`, `TASK_RUNTIME_STATE`, `VALIDATION_CRITERIA`, `AGENT_EXECUTION_LOG.execution_key` 기준 renamed schema 적재 확인

### GitHub Issue
- 새 Issue 생성
- 권장 제목: `[BE] agent-runtime 스키마 이름 정렬`
