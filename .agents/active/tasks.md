# 현재 활성 Task 집합

## 문서 목적
이 문서는 현재 활성 `Spec`을 실제 구현 단위로 분해한 `Task` 문서다.
각 `Task`는 하나의 명확한 결과, 검증 기준, GitHub Issue 연결 규칙을 가져야 한다.

## 현재 활성 Spec
- ID: `SPEC-0003`
- 이름: `예외 체계 정리`
- 기준 문서:
  - `.agents/active/spec.md`
  - `.agents/criteria/SPEC-0003-exception-system-alignment.json`

## 공통 규칙
- 구현 순서는 `TASK-0001 -> TASK-0002 -> TASK-0003`으로 고정한다.
- 각 `Task` 시작 전 해당 task용 GitHub Issue를 새로 연결한다.
- 각 `Task`는 연결된 `ValidationCriteria`와 테스트 근거가 없으면 완료로 보지 않는다.
- 예외 체계 정리 중에도 `/webhook/github` 성공 응답과 조기 종료 계약은 유지돼야 한다.
- `TASK-0001` 시작 전 기존 웹훅 회귀 안전망 목록을 먼저 확인하고, 필요한 범위만 새 테스트를 추가한다.
- 오류 코드를 실행 근거에 남기는 범위는 `TASK-0003`에서만 수행하고, `TASK-0002`에서는 예외 타입과 분류 기준 정리까지 닫는다.

## 요약 표
| Task | 이름 | 핵심 목표 | 연결 ValidationCriteria | 핵심 검증 | Issue |
| --- | --- | --- | --- | --- | --- |
| `TASK-0001` | 실패 경로 안전망 고정 | 성공 계약을 유지하면서 핵심 실패 경로의 기대 분류 기준을 먼저 테스트로 고정 | `webhook-contract-preserved-after-exception-refactor` | 웹훅 성공/조기 종료 회귀 + 핵심 실패 경로 테스트 | 새 Issue |
| `TASK-0002` | `AgileRunnerException`과 `ErrorCode` 도입 | 설정, 요청 정보, OpenAI, GitHub 코멘트 실패를 공통 예외 체계로 치환 | `core-failure-paths-mapped-to-error-codes` | 예외 타입/코드 정합성, 기존 웹훅 회귀 테스트 유지 | 새 Issue |
| `TASK-0003` | 실행 근거 오류 코드 적재와 실제 검증 | 실패 실행 기록에 오류 코드를 적재하고 실제 앱/H2로 검증 | `runtime-evidence-records-error-code` | 실행 근거 스키마, 저장소, 테스트와 로컬 앱/H2 검증 | 새 Issue |

## TASK-0001
### 이름
실패 경로 안전망 고정

### 목표
- 예외 체계 정리를 시작하기 전에 현재 웹훅 성공 계약과 핵심 실패 경로의 기대 예외 타입, 오류 코드, 분류 기준을 테스트로 먼저 고정한다.

### 구현 범위
- 기존 `GitHubWebhookControllerTest`, `GitHubCommentServiceTest` 회귀 안전망을 재사용하거나 필요한 경우만 보강한다.
- 대상은 아래를 우선 포함한다.
  - 성공 코멘트 응답 계약
  - 같은 delivery_id 조기 종료
  - `pull_request` 외 이벤트 조기 종료
  - OpenAI 리뷰 생성 실패와 설정 누락
  - GitHub App 설정 누락
  - 설치 요청 정보 누락
  - 패치와 위치 계산 등 GitHub 코멘트 작성 준비 실패
  - GitHub 코멘트 등록 실패
- 기존 테스트가 이미 안전망 역할을 하면 그 근거를 정리하고, 부족한 경우만 새 테스트를 추가한다.

### 비대상
- 공통 예외 클래스 도입
- 실행 근거 스키마 변경
- 웹훅 에러 응답 스키마 재설계

### 연결 ValidationCriteria
- `webhook-contract-preserved-after-exception-refactor`

### 완료 조건
- 예외 체계 정리 전 현재 성공 계약과 핵심 실패 경로 기대값이 테스트와 체크리스트로 고정된다.
- `AgileRunnerException + ErrorCode`의 최종 충족 여부는 `TASK-0002`에서 닫고, `TASK-0001`은 그 기대 분류 기준을 먼저 고정한다.
- 이후 예외 리팩터링 task에서 동일 테스트를 그대로 재사용할 수 있다.

### 검증
- 컨트롤러/서비스 통합 회귀 테스트 실행 통과
- 설정/서비스/요청 해석 실패 경로 테스트 실행 통과
- 현재 유지 계약과 테스트 근거 대응 관계 점검

### GitHub Issue
- 새 Issue 생성
- 권장 제목: `[BE] 예외 체계 안전망 고정`

## TASK-0002
### 이름
`AgileRunnerException`과 `ErrorCode` 도입

### 목표
- 핵심 실패 경로에서 임시 예외 대신 공통 예외 타입과 오류 코드를 사용하도록 정리한다.

### 구현 범위
- `AgileRunnerException` 도입
- `ErrorCode` 정의
- 설정 누락, 요청 정보 해석 실패, OpenAI 리뷰 생성 실패, GitHub 코멘트 준비/등록 실패를 새 예외 체계로 치환
- 설정, 컨트롤러 요청 해석, 서비스 경계에서 직접 던지는 `RuntimeException`, `IllegalStateException`을 새 기준으로 정리
- 기존 예외 메시지는 디버깅 가능한 수준으로 유지

### 비대상
- 실행 근거 스키마 변경
- 공통 예외 응답 처리 도입
- 웹훅 에러 응답 바디 재설계

### 연결 ValidationCriteria
- `core-failure-paths-mapped-to-error-codes`

### 완료 조건
- 핵심 실패 경로가 `AgileRunnerException + ErrorCode` 기준으로 일관되게 분류된다.
- 기존 성공 응답과 조기 종료 계약은 유지된다.
- `TASK-0001`에서 고정한 테스트가 계속 통과한다.

### 검증
- 실패 경로 테스트
- 웹훅 성공/조기 종료 회귀 테스트
- 관련 단위/통합 테스트 컴파일과 실행 통과

### GitHub Issue
- 새 Issue 생성
- 권장 제목: `[BE] 공통 예외 체계 도입`

## TASK-0003
### 이름
실행 근거 오류 코드 적재와 실제 검증

### 목표
- 실패한 `WebhookExecution`과 `AgentExecutionLog`에 오류 코드를 남기고, 실제 앱/H2 기준으로 적재를 검증한다.

### 구현 범위
- `WebhookExecution`에 오류 코드 저장
- `AgentExecutionLog`에 오류 코드 저장
- 저장소 SQL, 행 매퍼, 스키마를 새 필드에 맞춰 정리
- `AgentRuntimeService.recordFailure(...)`에서 오류 코드를 실행 근거에 남기도록 정리
- 로컬 프로필 실제 앱 기동 후 대표 웹훅 처리 결과를 H2에서 오류 코드 기준으로 확인

### 비대상
- 재시도 정책 도입
- 실패 보류 보관 도입
- 웹훅 에러 응답 재설계

### 연결 ValidationCriteria
- `runtime-evidence-records-error-code`

### 완료 조건
- 실패한 `WebhookExecution`과 `AgentExecutionLog`에 오류 코드가 적재된다.
- H2 저장소 왕복 테스트와 전체 테스트가 통과한다.
- 로컬 프로필 실제 앱 기동 후 대표 실행 근거에 오류 코드가 확인된다.

### 검증
- 실행 근거 저장소/서비스 왕복 테스트
- 컨트롤러/서비스 회귀 테스트
- 저장소 표준 전체 테스트
- 로컬 프로필 실제 앱 기동 후 H2 파일 DB 조회로 실패 실행 기록의 오류 코드 확인

### GitHub Issue
- 새 Issue 생성
- 권장 제목: `[BE] 실행 오류 코드 적재`
