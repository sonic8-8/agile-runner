# 현재 활성 Task 집합

## 문서 목적
이 문서는 현재 활성 `Spec`을 실제 구현 단위로 분해한 `Task` 문서다.
각 `Task`는 하나의 명확한 결과, 검증 기준, GitHub Issue 연결 규칙을 가져야 한다.

## 현재 활성 Spec
- ID: `SPEC-0005`
- 이름: `실행 제어 기능 기반 마련`
- 기준 문서:
  - `.agents/active/spec.md`
  - `.agents/criteria/SPEC-0005-execution-control-foundation.json`

## 공통 규칙
- 구현 순서는 `TASK-0001 -> TASK-0002 -> TASK-0003 -> TASK-0004`로 고정한다.
- 각 `Task` 시작 전 해당 task용 GitHub Issue를 새로 연결한다.
- 각 `Task`는 연결된 `ValidationCriteria`와 테스트 근거가 없으면 완료로 보지 않는다.
- 실행 제어 기능 도입 중에도 `/webhook/github` 성공 응답과 조기 종료 계약은 유지돼야 한다.
- `TASK-0001` 시작 전 기존 회귀 안전망과 representative runtime evidence를 먼저 확인하고, 필요한 범위만 새 테스트를 추가한다.
- 현재 webhook 요청은 별도 제어 입력이 없으면 기본적으로 `NORMAL` 모드로 유지한다.
- 이번 spec에서는 `DRY_RUN` 모드와 no-write 보장까지만 닫고, label/comment 기반 rerun과 선택 실행은 다음 spec으로 넘긴다.

## 요약 표
| Task | 이름 | 핵심 목표 | 연결 ValidationCriteria | 핵심 검증 | Issue |
| --- | --- | --- | --- | --- | --- |
| `TASK-0001` | 실행 제어 안전망 고정 | 기존 webhook 계약과 dry-run 기대 기준을 먼저 테스트로 고정 | `webhook-contract-preserved-after-execution-control` | 성공/조기 종료 회귀, dry-run 기대 기준 테스트 | 새 Issue |
| `TASK-0002` | 실행 제어 모드와 입력 모델 도입 | `NORMAL`, `DRY_RUN` 모드와 서비스 입력 모델을 정리하고 현재 webhook을 `NORMAL`로 해석 | `execution-control-mode-resolved-consistently` | 입력 모델 정합성, 기존 webhook 회귀 유지 | 새 Issue |
| `TASK-0003` | dry-run no-write 분기 도입 | dry-run에서 리뷰 생성은 유지하고 GitHub write는 차단 | `dry-run-skips-write-and-preserves-review-flow` | no-write 보장, review 생성 경로 유지, normal mode 영향 없음 | 새 Issue |
| `TASK-0004` | 실행 근거 제어 모드 적재와 실제 검증 | runtime evidence에 실행 제어 모드와 write 수행 근거를 남기고 실제 앱/H2로 검증 | `runtime-evidence-records-execution-control` | 저장소/H2 스키마, runtime 적재, representative normal 실행 검증 | 새 Issue |

## TASK-0001
### 이름
실행 제어 안전망 고정

### 목표
- 실행 제어 기능을 넣기 전에 현재 webhook 성공 계약과 dry-run 기대 기준을 테스트로 먼저 고정한다.

### 구현 범위
- 기존 `GitHubWebhookControllerTest`, `GitHubCommentServiceTest`, `OpenAiServiceTest`, `AgentRuntimeServiceTest`를 재사용하거나 필요한 경우만 보강한다.
- 이후 `TASK-0002`가 바로 사용할 수 있도록 실행 제어 모드를 표현하는 최소 타입(enum/class)은 먼저 둘 수 있다.
- 대상은 아래를 우선 포함한다.
  - 성공 코멘트 응답 계약
  - 같은 `delivery_id` 조기 종료
  - `pull_request` 외 이벤트 조기 종료
  - 현재 webhook 요청의 기본 `NORMAL` 실행 기대값
  - dry-run no-write 기대 기준
- 기존 테스트가 이미 안전망 역할을 하면 그 근거를 정리하고, 부족한 경우만 새 테스트를 추가한다.
- 최종 실행 제어 입력 모델과 분기 구현은 `TASK-0002`, `TASK-0003`에서 닫고, `TASK-0001`은 기대 기준과 회귀 안전망만 먼저 고정한다.

### 비대상
- 실행 제어 입력 모델 도입
- dry-run 실제 분기 구현
- runtime evidence 스키마 변경

### 연결 ValidationCriteria
- `webhook-contract-preserved-after-execution-control`

### 완료 조건
- 실행 제어 기능 도입 전 현재 webhook 성공 계약과 dry-run 기대 기준이 테스트와 체크리스트로 고정된다.
- 이후 실행 제어 리팩터링 task에서 동일 테스트를 그대로 재사용할 수 있다.

### 검증
- 컨트롤러/서비스 통합 회귀 테스트 실행 통과
- dry-run 기대 기준 테스트 실행 통과
- 현재 유지 계약과 테스트 근거 대응 관계 점검

### GitHub Issue
- 새 Issue 생성
- 권장 제목: `[BE] 실행 제어 안전망 고정`

## TASK-0002
### 이름
실행 제어 모드와 입력 모델 도입

### 목표
- 현재 webhook 요청이 `NORMAL` 모드로 일관되게 해석되고, 서비스 경계에서 `NORMAL`, `DRY_RUN` 같은 실행 제어 모드를 명시적으로 전달할 수 있게 정리한다.

### 구현 범위
- 실행 제어 모드 타입 정의
- 서비스 입력 모델 또는 경계 DTO에 실행 제어 모드 반영
- 현재 webhook 요청은 별도 제어 입력이 없으면 `NORMAL` 모드로 해석
- `DRY_RUN` 값은 서비스 경계까지 명시적으로 전달할 수 있게 열어두되, 실제 no-write 분기와 write 차단은 아직 구현하지 않는다.
- 기존 오류 코드, 메시지, 성공 응답 계약은 그대로 유지

### 관련 파일 후보
- `src/main/java/com/agilerunner/api/controller/github/request/GitHubEventRequest.java`
- `src/main/java/com/agilerunner/api/service/github/request/GitHubEventServiceRequest.java`
- `src/main/java/com/agilerunner/domain/executioncontrol/ExecutionControlMode.java`
- `src/main/java/com/agilerunner/api/controller/GitHubWebhookController.java`
- `src/main/java/com/agilerunner/api/service/OpenAiService.java`
- `src/main/java/com/agilerunner/api/service/GitHubCommentService.java`
- `src/test/java/com/agilerunner/api/controller/github/request/GitHubEventRequestTest.java`
- `src/test/java/com/agilerunner/api/controller/GitHubWebhookControllerTest.java`
- `src/test/java/com/agilerunner/api/service/OpenAiServiceTest.java`
- `src/test/java/com/agilerunner/api/service/GitHubCommentServiceTest.java`

### 비대상
- dry-run 실제 no-write 분기 구현
- runtime evidence 스키마 변경
- 외부 rerun API 도입

### 연결 ValidationCriteria
- `execution-control-mode-resolved-consistently`

### 완료 조건
- 현재 webhook 요청이 일관되게 `NORMAL` 모드로 해석된다.
- 서비스 경계에서 실행 제어 모드를 명시적으로 다룰 수 있다.
- `TASK-0001`에서 고정한 테스트가 계속 통과한다.

### 검증
- 실행 제어 입력 모델 테스트
- 명시적 `DRY_RUN` 입력이 서비스 경계까지 그대로 유지되는지 확인
- 웹훅 성공/조기 종료 회귀 테스트
- 관련 단위/통합 테스트 컴파일과 실행 통과

### GitHub Issue
- 새 Issue 생성
- 권장 제목: `[BE] 실행 제어 모드 도입`

## TASK-0003
### 이름
dry-run no-write 분기 도입

### 목표
- `DRY_RUN` 모드에서 리뷰 생성과 준비 단계는 유지하면서 GitHub write는 차단한다.

### 구현 범위
- dry-run 실행 분기 도입
- dry-run에서는 본문/인라인 코멘트 write 미수행
- dry-run에서도 리뷰 생성 결과 또는 준비 결과는 검증 가능하게 유지
- `NORMAL` 모드는 기존과 같이 write까지 이어지도록 유지

### 비대상
- runtime evidence 스키마 변경
- 외부 rerun API 도입
- 선택 실행 기능

### 연결 ValidationCriteria
- `dry-run-skips-write-and-preserves-review-flow`

### 완료 조건
- `DRY_RUN` 모드에서 GitHub write가 발생하지 않는다.
- `DRY_RUN` 모드에서도 controller/service 테스트에서 review 생성 결과 또는 이후 검증 가능한 결과가 관찰된다.
- `NORMAL` 모드와 기존 webhook 성공 계약은 그대로 유지된다.
- `/webhook/github` 경로에 직접 dry-run 입력을 붙이지 않는 구조라면, 실제 앱/H2 대표 검증 생략 사유를 retrospective에 남기고 테스트 근거로 종료 판정한다.

### 검증
- dry-run no-write 테스트
- normal mode 회귀 테스트
- 필요 시 실제 앱/H2 대표 검증 생략 사유 기록
- 관련 단위/통합 테스트 실행 통과

### GitHub Issue
- 새 Issue 생성
- 권장 제목: `[BE] dry-run 실행 분기 도입`

## TASK-0004
### 이름
실행 근거 제어 모드 적재와 실제 검증

### 목표
- `WebhookExecution`과 `AgentExecutionLog`에 실행 제어 모드와 write 수행 여부, write 생략 이유를 남기고, 실제 앱/H2 기준으로 적재를 검증한다.

### 구현 범위
- `WebhookExecution`에 실행 제어 모드와 write 수행 여부 저장
- `AgentExecutionLog`에 실행 제어 모드와 write 수행 여부, write 생략 이유 저장
- 저장소 SQL, 행 매퍼, 스키마를 새 필드에 맞춰 정리
- 대표 `NORMAL` 검증은 fresh `delivery_id`를 사용한다.
- 로컬 프로필 실제 앱 기동 후 대표 웹훅 실행 결과를 H2에서 실행 제어 모드 기준으로 확인한다.

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
- 외부 rerun API 도입
- 선택 실행 기능
- 운영 대시보드 구축

### 연결 ValidationCriteria
- `runtime-evidence-records-execution-control`

### 완료 조건
- `WebhookExecution`과 `AgentExecutionLog`에 실행 제어 모드와 write 수행 여부, write 생략 이유가 적재된다.
- H2 저장소 왕복 테스트와 전체 테스트가 통과한다.
- 로컬 프로필 실제 앱 기동 후 대표 `NORMAL` 실행 근거에 실행 제어 모드와 write 수행 여부가 확인된다.

### 검증
- 실행 근거 저장소/서비스 왕복 테스트
- 컨트롤러/서비스 회귀 테스트
- 전체 테스트 실행
- 로컬 프로필 실제 앱 기동 후 H2 파일 DB 조회로 같은 `execution_key` 기준의 `WebhookExecution`과 `AgentExecutionLog`에 실행 제어 모드와 write 수행 여부가 함께 적재되는지 확인

### GitHub Issue
- 새 Issue 생성
- 권장 제목: `[BE] 실행 근거 제어 모드 적재`
