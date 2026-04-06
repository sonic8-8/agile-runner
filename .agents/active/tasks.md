# 현재 활성 Task 집합

## 문서 목적
이 문서는 현재 활성 `Spec`을 실제 구현 단위로 분해한 `Task` 문서다.
각 `Task`는 하나의 명확한 결과, 검증 기준, GitHub Issue 연결 규칙을 가져야 한다.

## 현재 활성 Spec
- ID: `SPEC-0004`
- 이름: `실패 대응 강화`
- 기준 문서:
  - `.agents/active/spec.md`
  - `.agents/criteria/SPEC-0004-failure-response-hardening.json`

## 공통 규칙
- 구현 순서는 `TASK-0001 -> TASK-0002 -> TASK-0003`으로 고정한다.
- 각 `Task` 시작 전 해당 task용 GitHub Issue를 새로 연결한다.
- 각 `Task`는 연결된 `ValidationCriteria`와 테스트 근거가 없으면 완료로 보지 않는다.
- 실패 대응 강화 중에도 `/webhook/github` 성공 응답과 조기 종료 계약은 유지돼야 한다.
- `TASK-0001` 시작 전 기존 회귀 안전망과 representative failure evidence를 먼저 확인하고, 필요한 범위만 새 테스트를 추가한다.
- 실제 대응 정책 실행은 이번 spec에 포함하지 않고, 대응 분류 기준과 실행 근거 적재까지만 닫는다.
- 이번 spec에서 우선 다루는 핵심 `ErrorCode`는 아래 6개다.
  - `GITHUB_INSTALLATION_ID_MISSING`
  - `OPENAI_CLIENT_MISSING`
  - `OPENAI_REVIEW_FAILED`
  - `GITHUB_APP_CONFIGURATION_MISSING`
  - `GITHUB_COMMENT_PREPARATION_FAILED`
  - `GITHUB_COMMENT_POST_FAILED`

## 요약 표
| Task | 이름 | 핵심 목표 | 연결 ValidationCriteria | 핵심 검증 | Issue |
| --- | --- | --- | --- | --- | --- |
| `TASK-0001` | 실패 대응 안전망 고정 | 성공 계약을 유지하면서 핵심 `ErrorCode`의 기대 대응 분류를 먼저 테스트로 고정 | `webhook-contract-preserved-after-failure-hardening` | 웹훅 성공/조기 종료 회귀 + 대표 실패 분류 기대값 테스트 | 새 Issue |
| `TASK-0002` | 실패 대응 분류 정책 도입 | 핵심 `ErrorCode`를 `재시도 가능`, `재시도 불가`, `수동 조치 필요` 기준으로 일관되게 분류 | `failure-disposition-classified-consistently` | 분류 정책 정합성, 기존 웹훅 회귀 유지 | 새 Issue |
| `TASK-0003` | 실행 근거 대응 분류 적재와 실제 검증 | 실패 실행 기록에 대응 분류를 적재하고 실제 앱/H2로 검증 | `runtime-evidence-records-failure-disposition` | 저장소/H2 스키마, runtime 적재, 대표 실패 검증 | 새 Issue |

## TASK-0001
### 이름
실패 대응 안전망 고정

### 목표
- 실패 대응 강화에 들어가기 전에 현재 웹훅 성공 계약과 핵심 `ErrorCode`의 기대 대응 분류 기준을 테스트로 먼저 고정한다.

### 구현 범위
- 기존 `GitHubWebhookControllerTest`, `GitHubCommentServiceTest`, `OpenAiServiceTest`, `AgentRuntimeServiceTest`를 재사용하거나 필요한 경우만 보강한다.
- 이후 `TASK-0002`가 바로 사용할 수 있도록 실패 대응 분류 개념을 표현하는 최소 타입(enum/class)은 먼저 둘 수 있다.
- 대상은 아래를 우선 포함한다.
  - 성공 코멘트 응답 계약
  - 같은 `delivery_id` 조기 종료
  - `pull_request` 외 이벤트 조기 종료
  - `GITHUB_INSTALLATION_ID_MISSING`
  - `OPENAI_CLIENT_MISSING`
  - `OPENAI_REVIEW_FAILED`
  - `GITHUB_APP_CONFIGURATION_MISSING`
  - `GITHUB_COMMENT_PREPARATION_FAILED`
  - `GITHUB_COMMENT_POST_FAILED`
- 기존 테스트가 이미 안전망 역할을 하면 그 근거를 정리하고, 부족한 경우만 새 테스트를 추가한다.
- 최종 대응 분류 정책 자체는 `TASK-0002`에서 닫고, `TASK-0001`은 기대 분류 기준과 회귀 안전망만 먼저 고정한다.

### 비대상
- 실패 대응 분류 정책 도입
- 실행 근거 스키마 변경
- 실제 재시도 실행

### 연결 ValidationCriteria
- `webhook-contract-preserved-after-failure-hardening`

### 완료 조건
- 실패 대응 강화 전 현재 성공 계약과 핵심 `ErrorCode`의 기대 대응 기준이 테스트와 체크리스트로 고정된다.
- 핵심 `ErrorCode`의 최종 대응 분류 충족 여부는 `TASK-0002`에서 닫고, `TASK-0001`은 기대 분류 기준을 먼저 고정한다.
- 이후 실패 대응 리팩터링 task에서 동일 테스트를 그대로 재사용할 수 있다.

### 검증
- 컨트롤러/서비스 통합 회귀 테스트 실행 통과
- 대표 실패 경로 테스트 실행 통과
- 현재 유지 계약과 테스트 근거 대응 관계 점검

### GitHub Issue
- 새 Issue 생성
- 권장 제목: `[BE] 실패 대응 안전망 고정`

## TASK-0002
### 이름
실패 대응 분류 정책 도입

### 목표
- 핵심 `ErrorCode`가 `재시도 가능`, `재시도 불가`, `수동 조치 필요` 기준으로 일관되게 분류되도록 정책을 도입한다.

### 구현 범위
- 실패 대응 분류 타입 정의
- 핵심 `ErrorCode`와 실패 대응 분류 매핑 정책 정의
- controller/service 경계와 runtime 기록 입력 기준에서 같은 실패가 같은 분류로 해석되도록 정리
- 기존 오류 메시지와 오류 코드는 그대로 유지

### 비대상
- 실행 근거 스키마 변경
- 실제 재시도 실행
- 공통 에러 응답 재설계

### 연결 ValidationCriteria
- `failure-disposition-classified-consistently`

### 완료 조건
- 핵심 `ErrorCode`가 실패 대응 분류 기준으로 일관되게 해석된다.
- 기존 성공 응답과 조기 종료 계약은 유지된다.
- `TASK-0001`에서 고정한 테스트가 계속 통과한다.

### 검증
- 실패 대응 분류 테스트
- 웹훅 성공/조기 종료 회귀 테스트
- 관련 단위/통합 테스트 컴파일과 실행 통과

### GitHub Issue
- 새 Issue 생성
- 권장 제목: `[BE] 실패 대응 분류 도입`

## TASK-0003
### 이름
실행 근거 대응 분류 적재와 실제 검증

### 목표
- 실패한 `WebhookExecution`과 `AgentExecutionLog`에 실패 대응 분류를 남기고, 실제 앱/H2 기준으로 적재를 검증한다.

### 구현 범위
- `WebhookExecution`에 실패 대응 분류 저장
- `AgentExecutionLog`에 실패 대응 분류 저장
- 저장소 SQL, 행 매퍼, 스키마를 새 필드에 맞춰 정리
- `AgentRuntimeService.recordFailure(...)`에서 대응 분류를 실행 근거에 남기도록 정리
- representative failure 검증은 `GITHUB_APP_CONFIGURATION_MISSING` 시나리오를 기본값으로 사용한다.
- 로컬 프로필 실제 앱 기동 후 대표 웹훅 실패 결과를 H2에서 대응 분류 기준으로 확인

### 관련 파일 후보
- `src/main/java/com/agilerunner/domain/agentruntime/WebhookExecution.java`
- `src/main/java/com/agilerunner/domain/agentruntime/AgentExecutionLog.java`
- `src/main/java/com/agilerunner/api/service/agentruntime/AgentRuntimeService.java`
- `src/main/java/com/agilerunner/client/agentruntime/AgentRuntimeRepository.java`
- `src/main/resources/agent-runtime/schema.sql`
- `src/test/java/com/agilerunner/api/service/agentruntime/AgentRuntimeServiceTest.java`
- `src/test/java/com/agilerunner/client/agentruntime/AgentRuntimeRepositoryTest.java`
- `src/test/java/com/agilerunner/api/controller/GitHubWebhookControllerTest.java`

### 비대상
- 실제 재시도 실행
- 실패 보류 큐 도입
- 웹훅 에러 응답 재설계

### 연결 ValidationCriteria
- `runtime-evidence-records-failure-disposition`

### 완료 조건
- 실패한 `WebhookExecution`과 `AgentExecutionLog`에 실패 대응 분류가 적재된다.
- H2 저장소 왕복 테스트와 전체 테스트가 통과한다.
- 로컬 프로필 실제 앱 기동 후 대표 실행 근거에 실패 대응 분류가 확인된다.

### 검증
- 실행 근거 저장소/서비스 왕복 테스트
- 컨트롤러/서비스 회귀 테스트
- 전체 테스트 실행
- 로컬 프로필 실제 앱 기동 후 H2 파일 DB 조회로 같은 `execution_key` 기준의 `WebhookExecution`과 `AgentExecutionLog`에 대응 분류가 함께 적재되는지 확인

### GitHub Issue
- 새 Issue 생성
- 권장 제목: `[BE] 실행 근거 대응 분류 적재`
