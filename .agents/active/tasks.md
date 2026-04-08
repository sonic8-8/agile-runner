# 현재 활성 Task 집합

## 문서 목적
이 문서는 현재 활성 `Spec`을 실제 구현 단위로 분해한 `Task` 문서다.
각 `Task`는 하나의 명확한 결과, 검증 기준, GitHub Issue 연결 규칙을 가져야 한다.

## 현재 활성 Spec
- ID: `SPEC-0006`
- 이름: `수동 재실행 기능 기반 마련`
- 기준 문서:
  - `.agents/active/spec.md`
  - `.agents/criteria/SPEC-0006-manual-rerun-foundation.json`

## 공통 규칙
- 구현 순서는 `TASK-0001 -> TASK-0002 -> TASK-0003 -> TASK-0004`로 고정한다.
- 각 `Task` 시작 전 해당 task용 GitHub Issue를 새로 연결한다.
- 각 `Task`는 연결된 `ValidationCriteria`와 테스트 근거가 없으면 완료로 보지 않는다.
- 수동 재실행 기능 도입 중에도 `/webhook/github` 성공 응답과 조기 종료 계약은 유지돼야 한다.
- `TASK-0001` 시작 전 기존 webhook 회귀 안전망과 representative runtime evidence를 먼저 확인하고, 필요한 범위만 새 테스트를 추가한다.
- 수동 재실행 경로에서도 `ExecutionControlMode.NORMAL`, `ExecutionControlMode.DRY_RUN` 해석을 그대로 재사용한다.
- 이번 spec에서는 수동 재실행 진입점과 실행 근거 구분값까지만 닫고, label/comment 기반 rerun과 선택 실행은 다음 spec으로 넘긴다.
- 수동 재실행 진입점은 이번 spec에서 내부/관리자용 기반 HTTP 진입점으로만 다루고, 공개 권한 모델은 확정하지 않는다.

## 요약 표
| Task | 이름 | 핵심 목표 | 연결 ValidationCriteria | 핵심 검증 | Issue |
| --- | --- | --- | --- | --- | --- |
| `TASK-0001` | 수동 재실행 안전망 고정 | 기존 webhook 계약과 재사용할 회귀 안전망을 먼저 고정 | `webhook-contract-preserved-during-manual-rerun` | webhook 회귀 테스트 | 새 Issue |
| `TASK-0002` | 수동 재실행 입력 모델과 진입점 도입 | 수동 재실행 요청 모델과 controller/service 진입점 정리 | `manual-rerun-entrypoint-contract-defined`, `manual-rerun-request-resolved-consistently` | 입력 모델 정합성, 수동 재실행 요청 해석, 응답 계약 테스트 | 새 Issue |
| `TASK-0003` | 수동 재실행 실행 경로 연결 | 수동 재실행 경로를 기존 리뷰 생성, 코멘트 작성, dry-run 분기와 연결 | `manual-rerun-respects-execution-control` | `NORMAL`, `DRY_RUN` 실행 경로 재사용 검증 | 새 Issue |
| `TASK-0004` | 실행 근거 재실행 구분값 적재와 실제 검증 | runtime evidence에 실행 시작 유형을 남기고 실제 앱/H2로 검증 | `runtime-evidence-distinguishes-manual-rerun` | 저장소/H2 스키마, runtime 적재, representative manual rerun 검증 | 새 Issue |

## TASK-0001
### 이름
수동 재실행 안전망 고정

### 목표
- 수동 재실행 기능을 넣기 전에 기존 webhook 계약과 재사용할 회귀 안전망을 테스트로 먼저 고정한다.

### 구현 범위
- 기존 `GitHubWebhookControllerTest`, `GitHubCommentServiceTest`, `OpenAiServiceTest`, `AgentRuntimeServiceTest`를 재사용하거나 필요한 경우만 보강한다.
- 이후 task에서 사용할 수동 재실행 기대 동작 초안은 체크리스트로만 정리하고, 실제 진입점 black-box 테스트는 `TASK-0002`에서 고정한다.
- 대상은 아래를 우선 포함한다.
  - 기존 webhook 성공 응답 계약
  - 같은 `delivery_id` 조기 종료
  - `pull_request` 외 이벤트 조기 종료
- 기존 테스트가 이미 충분한 안전망이면 그 근거를 문서에 남기고, 부족한 경우만 새 테스트를 추가한다.
- 실제 입력 모델 도입과 실행 경로 연결은 `TASK-0002`, `TASK-0003`에서 닫고, `TASK-0001`은 webhook 회귀 안전망만 먼저 고정한다.

### 비대상
- 수동 재실행 요청 DTO 구현
- 수동 재실행 실제 실행 경로 구현
- runtime evidence 스키마 변경

### 연결 ValidationCriteria
- `webhook-contract-preserved-during-manual-rerun`

### 완료 조건
- 수동 재실행 기능 도입 전 기존 webhook 성공 계약과 이후 task가 재사용할 회귀 안전망이 테스트와 체크리스트로 고정된다.
- 이후 task에서 같은 테스트를 그대로 재사용할 수 있다.

### 검증
- 컨트롤러/서비스 통합 회귀 테스트 실행 통과
- 현재 유지 계약과 테스트 근거 대응 관계 점검

### GitHub Issue
- 새 Issue 생성
- 권장 제목: `[BE] 수동 재실행 안전망 고정`

## TASK-0002
### 이름
수동 재실행 입력 모델과 진입점 도입

### 목표
- 수동 재실행 요청을 명시적으로 표현하는 입력 모델과 controller/service 진입점을 만든다.

### 구현 범위
- 수동 재실행 request DTO 정의
- service 경계에서 사용할 rerun service request 정의
- 수동 재실행용 controller/service entrypoint 추가
- 요청은 최소한 저장소 이름, PR 번호, 설치 ID, 실행 제어 모드를 다룬다.
- 기존 webhook 경로와 성공 응답 계약은 그대로 유지한다.
- `DRY_RUN` 값은 수동 재실행 입력 모델까지 명시적으로 전달할 수 있게 열어두되, 실제 실행 분기와 write 차단은 아직 구현하지 않는다.

### 관련 파일 후보
- `src/main/java/com/agilerunner/api/controller/review/ManualRerunController.java`
- `src/main/java/com/agilerunner/api/controller/review/request/ManualRerunRequest.java`
- `src/main/java/com/agilerunner/api/controller/review/response/ManualRerunResponse.java`
- `src/main/java/com/agilerunner/api/service/review/ManualRerunService.java`
- `src/main/java/com/agilerunner/api/service/review/request/ManualRerunServiceRequest.java`
- `src/main/java/com/agilerunner/api/service/review/response/ManualRerunServiceResponse.java`
- `src/main/java/com/agilerunner/domain/executioncontrol/ExecutionControlMode.java`
- `src/test/java/com/agilerunner/api/controller/review/ManualRerunControllerTest.java`
- `src/test/java/com/agilerunner/api/controller/review/request/ManualRerunRequestTest.java`

### 비대상
- 수동 재실행 실제 리뷰 생성/코멘트 작성 연결
- runtime evidence 스키마 변경
- label/comment 기반 rerun

### 연결 ValidationCriteria
- `manual-rerun-entrypoint-contract-defined`
- `manual-rerun-request-resolved-consistently`

### 완료 조건
- 수동 재실행 요청이 저장소 이름, PR 번호, 설치 ID, 실행 제어 모드를 일관되게 해석한다.
- controller/service 경계에서 수동 재실행 요청을 명시적으로 다룰 수 있다.
- 수동 재실행 진입점의 성공 응답 계약이 black-box 테스트로 고정된다.
- `TASK-0001`에서 고정한 webhook 회귀 테스트가 계속 통과한다.

### 검증
- 수동 재실행 진입점 응답 계약 테스트
- 수동 재실행 request DTO 테스트
- service request 변환 테스트
- 기존 webhook 회귀 테스트
- 관련 단위/통합 테스트 실행 통과

### GitHub Issue
- 새 Issue 생성
- 권장 제목: `[BE] 수동 재실행 입력 모델 도입`

## TASK-0003
### 이름
수동 재실행 실행 경로 연결

### 목표
- 수동 재실행 경로가 기존 리뷰 생성, 코멘트 작성, dry-run 분기를 재사용하도록 연결한다.

### 구현 범위
- 수동 재실행용 service orchestration 도입
- 수동 재실행 `NORMAL` 경로에서는 기존과 같이 실제 GitHub 코멘트 write 수행
- 수동 재실행 `DRY_RUN` 경로에서는 기존 dry-run 분기를 재사용해 no-write 유지
- webhook 경로와 수동 재실행 경로가 같은 핵심 리뷰 생성 로직을 공유하도록 정리
- 외부 webhook 응답 계약은 바꾸지 않는다.

### 관련 파일 후보
- `src/main/java/com/agilerunner/api/controller/review/ManualRerunController.java`
- `src/main/java/com/agilerunner/api/service/OpenAiService.java`
- `src/main/java/com/agilerunner/api/service/GitHubCommentService.java`
- `src/main/java/com/agilerunner/api/service/review/ManualRerunService.java`
- `src/main/java/com/agilerunner/api/service/review/request/ManualRerunServiceRequest.java`
- `src/main/java/com/agilerunner/api/controller/review/response/ManualRerunResponse.java`
- `src/test/java/com/agilerunner/api/controller/review/ManualRerunControllerTest.java`
- `src/test/java/com/agilerunner/api/service/review/ManualRerunServiceTest.java`
- `src/test/java/com/agilerunner/api/service/GitHubCommentServiceTest.java`

### 비대상
- runtime evidence 스키마 변경
- 선택 실행 기능
- label/comment 기반 rerun

### 연결 ValidationCriteria
- `manual-rerun-respects-execution-control`

### 완료 조건
- 수동 재실행 `NORMAL` 경로는 실제 GitHub 코멘트 write까지 이어진다.
- 수동 재실행 `DRY_RUN` 경로는 write 없이 기존 review 생성 흐름을 재사용한다.
- 기존 webhook 경로는 그대로 유지된다.

### 검증
- 수동 재실행 `NORMAL` 통합 테스트
- 수동 재실행 `DRY_RUN` no-write 테스트
- 기존 webhook 회귀 테스트
- 관련 단위/통합 테스트 실행 통과

### GitHub Issue
- 새 Issue 생성
- 권장 제목: `[BE] 수동 재실행 실행 경로 연결`

## TASK-0004
### 이름
실행 근거 재실행 구분값 적재와 실제 검증

### 목표
- `WebhookExecution`과 `AgentExecutionLog`에 실행 시작 유형을 남기고, 실제 앱/H2 기준으로 대표 수동 재실행 1건을 검증한다.

### 구현 범위
- runtime evidence에 webhook 실행과 수동 재실행을 구분하는 값 추가
- 수동 재실행 성공 응답의 `executionKey`를 placeholder가 아니라 실제 runtime evidence 조회에 사용할 `execution_key`와 같은 값으로 맞춘다.
- 저장소 SQL, 행 매퍼, 스키마를 새 필드에 맞춰 정리
- controller/service orchestration이 새 runtime evidence 적재를 막지 않도록 필요한 범위에서 함께 정리
- 대표 수동 재실행 검증은 성공 응답에서 받은 fresh `executionKey`를 기준으로 진행한다.
- 로컬 프로필 실제 앱 기동 후 대표 수동 재실행 결과를 H2에서 같은 `executionKey` 기준으로 확인한다.

### 관련 파일 후보
- `src/main/java/com/agilerunner/domain/agentruntime/WebhookExecution.java`
- `src/main/java/com/agilerunner/domain/agentruntime/AgentExecutionLog.java`
- `src/main/java/com/agilerunner/api/service/agentruntime/AgentRuntimeService.java`
- `src/main/java/com/agilerunner/api/controller/GitHubWebhookController.java`
- `src/main/java/com/agilerunner/api/controller/review/ManualRerunController.java`
- `src/main/java/com/agilerunner/api/service/review/ManualRerunService.java`
- `src/main/java/com/agilerunner/api/service/review/response/ManualRerunServiceResponse.java`
- `src/main/java/com/agilerunner/api/controller/review/response/ManualRerunResponse.java`
- `src/main/java/com/agilerunner/client/agentruntime/AgentRuntimeRepository.java`
- `src/main/resources/agent-runtime/schema.sql`
- `src/test/java/com/agilerunner/api/controller/GitHubWebhookControllerTest.java`
- `src/test/java/com/agilerunner/api/service/agentruntime/AgentRuntimeServiceTest.java`
- `src/test/java/com/agilerunner/client/agentruntime/AgentRuntimeRepositoryTest.java`
- `src/test/java/com/agilerunner/api/controller/review/ManualRerunControllerTest.java`

### 비대상
- 장기 저장소 도입
- 운영 대시보드 구축
- 선택 실행 기능

### 연결 ValidationCriteria
- `runtime-evidence-distinguishes-manual-rerun`

### 완료 조건
- `WebhookExecution`과 `AgentExecutionLog`에 실행 시작 유형이 적재된다.
- 수동 재실행 성공 응답의 `executionKey`가 placeholder가 아니라 실제 runtime evidence의 `execution_key`와 같게 유지된다.
- H2 저장소 왕복 테스트와 전체 테스트가 통과한다.
- 로컬 프로필 실제 앱 기동 후 대표 수동 재실행 응답의 `executionKey`를 기준으로 실행 시작 유형과 실행 제어 모드를 확인할 수 있다.

### 검증
- 수동 재실행 응답 `executionKey`와 runtime evidence `execution_key` 일치 테스트
- 실행 근거 저장소/서비스 왕복 테스트
- 컨트롤러/서비스 회귀 테스트
- 전체 테스트 실행
- 로컬 프로필 실제 앱 기동 후 representative manual rerun 실제 검증

### GitHub Issue
- 새 Issue 생성
- 권장 제목: `[BE] 재실행 실행 근거 구분값 적재`
