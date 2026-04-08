# 현재 활성 Task 집합

## 문서 목적
이 문서는 현재 활성 `Spec`을 실제 구현 단위로 분해한 `Task` 문서다.
각 `Task`는 하나의 명확한 결과, 검증 기준, GitHub Issue 연결 규칙을 가져야 한다.

## 현재 활성 Spec
- ID: `SPEC-0007`
- 이름: `선택 실행 기능 기반 마련`
- 기준 문서:
  - `.agents/active/spec.md`
  - `.agents/criteria/SPEC-0007-selective-execution-foundation.json`

## 공통 규칙
- 구현 순서는 `TASK-0001 -> TASK-0002 -> TASK-0003 -> TASK-0004`로 고정한다.
- 각 `Task` 시작 전 해당 task용 GitHub Issue를 새로 연결한다.
- 각 `Task`는 연결된 `ValidationCriteria`와 테스트 근거가 없으면 완료로 보지 않는다.
- 선택 실행 기능 도입 중에도 `/webhook/github` 성공 응답과 조기 종료 계약, `POST /reviews/rerun`의 기존 성공 응답 계약은 유지돼야 한다.
- `TASK-0001` 시작 전 기존 webhook/manual rerun 회귀 안전망과 representative runtime evidence를 먼저 확인하고, 필요한 범위만 새 테스트를 추가한다.
- 선택 실행 조건은 이번 spec에서 `파일 경로 목록`만 허용하고, 비어 있으면 전체 실행으로 해석한다.
- 선택 실행 기능은 우선 manual rerun 경로에서만 다루고, webhook 입력 형식은 바꾸지 않는다.
- 이번 spec에서는 선택 조건 입력, 경로 필터링, 실행 근거 적재까지만 닫고, glob 패턴이나 line 단위 선택은 후속 spec으로 넘긴다.

## 요약 표
| Task | 이름 | 핵심 목표 | 연결 ValidationCriteria | 핵심 검증 | Issue |
| --- | --- | --- | --- | --- | --- |
| `TASK-0001` | 선택 실행 안전망 고정 | 기존 webhook/manual rerun 계약과 재사용할 안전망 먼저 고정 | `webhook-and-rerun-contract-preserved-during-selective-execution` | webhook/manual rerun 회귀 테스트 | 새 Issue |
| `TASK-0002` | 선택 실행 입력 모델 도입 | manual rerun 요청과 service request에 선택 파일 경로 목록 추가 | `selection-input-resolved-consistently` | 입력 모델 정합성, 빈 목록 기본 해석, 기존 응답 계약 유지 | 새 Issue |
| `TASK-0003` | 선택 경로 기준 리뷰와 코멘트 제한 | 선택 파일 경로만 리뷰 입력과 경로 기반 inline comment 대상에 반영 | `selected-paths-limit-review-and-comment-scope` | 선택 경로만 남는 리뷰 입력과 경로 기반 inline comment 대상 검증 | 새 Issue |
| `TASK-0004` | 선택 실행 근거 적재와 실제 검증 | runtime evidence에 선택 실행 여부와 정렬된 경로 요약 적재 | `runtime-evidence-records-selection-scope` | 저장소/H2 스키마, representative 선택 실행 검증 | 새 Issue |

## TASK-0001
### 이름
선택 실행 안전망 고정

### 목표
- 선택 실행 기능을 넣기 전에 기존 webhook 계약과 manual rerun 계약, 그리고 이후 task가 재사용할 회귀 안전망을 테스트로 먼저 고정한다.

### 구현 범위
- 기존 `GitHubWebhookControllerTest`, `GitHubCommentServiceTest`, `ManualRerunControllerTest`, `ManualRerunServiceTest`, `AgentRuntimeServiceTest`를 재사용하거나 필요한 경우만 보강한다.
- 기존 안전망이 이미 충분하면 그 근거를 문서와 회고에 남기고, 부족한 경우만 새 테스트를 추가한다.
- 대상은 아래를 우선 포함한다.
  - `/webhook/github` 성공 응답과 조기 종료 계약
  - `POST /reviews/rerun` 성공 응답 계약
  - manual rerun `NORMAL`, `DRY_RUN` 기본 경로
- 실제 선택 파일 경로 입력과 경로 제한 구현은 `TASK-0002`, `TASK-0003`에서 닫는다.

### 비대상
- 선택 파일 경로 입력 DTO 구현
- 선택 경로 기반 리뷰/코멘트 제한
- runtime evidence 스키마 변경

### 연결 ValidationCriteria
- `webhook-and-rerun-contract-preserved-during-selective-execution`

### 완료 조건
- 선택 실행 기능 도입 전 기존 webhook/manual rerun 계약과 이후 task가 재사용할 회귀 안전망이 테스트와 체크리스트로 고정된다.
- 이후 task에서 같은 테스트를 그대로 재사용할 수 있다.

### 검증
- webhook/manual rerun 회귀 테스트 실행 통과
- 현재 유지 계약과 테스트 근거 대응 관계 점검

### GitHub Issue
- 새 Issue 생성
- 권장 제목: `[BE] 선택 실행 안전망 고정`

## TASK-0002
### 이름
선택 실행 입력 모델 도입

### 목표
- manual rerun 요청과 service request에서 선택 파일 경로 목록을 명시적으로 다룰 수 있게 만든다.

### 구현 범위
- manual rerun request DTO에 선택 파일 경로 목록 추가
- service request DTO에 선택 파일 경로 목록 추가
- controller/service 경계에서 선택 파일 경로 목록 전달
- 선택 파일 경로 목록이 비어 있으면 전체 실행으로 해석하는 기본 규칙 정리
- 기존 `/reviews/rerun` 성공 응답 계약은 유지한다.
- downstream 실행 경계까지 값 전달이 필요하면 `GitHubEventServiceRequest` 같은 내부 request seam도 함께 정리한다.

### 관련 파일 후보
- `src/main/java/com/agilerunner/api/controller/review/request/ManualRerunRequest.java`
- `src/main/java/com/agilerunner/api/controller/review/ManualRerunController.java`
- `src/main/java/com/agilerunner/api/service/review/request/ManualRerunServiceRequest.java`
- `src/main/java/com/agilerunner/api/service/review/ManualRerunService.java`
- `src/main/java/com/agilerunner/api/service/github/request/GitHubEventServiceRequest.java`
- `src/test/java/com/agilerunner/api/controller/review/request/ManualRerunRequestTest.java`
- `src/test/java/com/agilerunner/api/controller/review/ManualRerunControllerTest.java`
- `src/test/java/com/agilerunner/api/service/review/ManualRerunServiceTest.java`

### 비대상
- 선택 경로 기반 리뷰/코멘트 제한
- runtime evidence 스키마 변경
- webhook 입력 형식 변경

### 연결 ValidationCriteria
- `selection-input-resolved-consistently`

### 완료 조건
- manual rerun 요청이 선택 파일 경로 목록을 controller/service 경계에서 같은 의미로 해석한다.
- 선택 파일 경로 목록이 비어 있으면 전체 실행 기본 해석을 유지한다.
- 기존 `/reviews/rerun` 성공 응답 계약이 유지된다.

### 검증
- request DTO 테스트
- controller black-box 테스트
- service request 전달 테스트
- 기존 webhook/manual rerun 회귀 테스트

### GitHub Issue
- 새 Issue 생성
- 권장 제목: `[BE] 선택 실행 입력 모델 도입`

## TASK-0003
### 이름
선택 경로 기준 리뷰와 코멘트 제한

### 목표
- 선택 파일 경로 목록이 주어지면 리뷰 생성 입력과 경로 기반 inline comment 대상이 해당 경로만 기준으로 제한되게 한다.

### 구현 범위
- review 생성 입력에서 선택 파일 경로만 남기도록 정리
- 리뷰 결과의 inline comment는 선택 파일 경로만 유지하도록 정리
- GitHub 경로 기반 inline comment 작성 대상도 선택 파일 경로만 기준으로 제한
- 선택 파일 경로 목록이 비어 있으면 기존 전체 실행 경로 유지
- 선택 파일 경로가 PR diff와 하나도 매칭되지 않으면 리뷰 입력과 경로 기반 inline comment 대상은 빈 상태로 처리하고, 기존 성공 응답 계약은 유지
- `NORMAL`, `DRY_RUN` 실행 제어는 기존 규칙 유지
- representative actual-app 검증은 `TASK-0004`에서 executionKey 기준 evidence 확인으로 닫고, 이번 task는 targeted/full test까지로 종료한다.

### 관련 파일 후보
- `src/main/java/com/agilerunner/api/service/OpenAiService.java`
- `src/main/java/com/agilerunner/api/service/GitHubCommentService.java`
- `src/main/java/com/agilerunner/api/service/review/ManualRerunService.java`
- `src/main/java/com/agilerunner/api/service/review/request/ManualRerunServiceRequest.java`
- `src/main/java/com/agilerunner/domain/Review.java`
- `src/main/java/com/agilerunner/domain/InlineComment.java`
- `src/test/java/com/agilerunner/api/service/review/ManualRerunServiceTest.java`
- `src/test/java/com/agilerunner/api/service/GitHubCommentServiceTest.java`
- `src/test/java/com/agilerunner/api/service/OpenAiServiceTest.java`

### 비대상
- runtime evidence 스키마 변경
- glob/정규식 선택 문법
- webhook 입력 형식 변경

### 연결 ValidationCriteria
- `selected-paths-limit-review-and-comment-scope`

### 완료 조건
- 선택 파일 경로 목록이 주어지면 비선택 경로는 리뷰 입력과 경로 기반 inline comment 대상에서 제외된다.
- 선택 파일 경로 목록이 비어 있으면 기존 전체 실행 경로를 유지한다.
- 선택 파일 경로가 PR diff와 하나도 매칭되지 않으면 리뷰 입력과 경로 기반 inline comment 대상은 빈 상태로 처리되고, 기존 성공 응답 계약은 유지된다.
- `NORMAL`, `DRY_RUN` 실행 제어가 함께 유지된다.
- actual-app/H2 representative 검증이 필요한 runtime evidence 확인은 `TASK-0004`에서 수행한다.

### 검증
- 선택 파일 경로 필터 테스트
- 선택 경로 no-match 처리 테스트
- manual rerun service/controller 테스트
- GitHub 경로 기반 inline comment 대상 제한 테스트
- 기존 webhook/manual rerun 회귀 테스트

### GitHub Issue
- 새 Issue 생성
- 권장 제목: `[BE] 선택 경로 기반 리뷰 제한`

## TASK-0004
### 이름
선택 실행 근거 적재와 실제 검증

### 목표
- runtime evidence에 선택 실행 여부와 정렬된 파일 경로 목록 요약 문자열을 남기고, 실제 앱/H2 기준으로 대표 선택 실행 1건을 검증한다.

### 구현 범위
- `WebhookExecution`과 `AgentExecutionLog`에 선택 실행 여부와 정렬된 파일 경로 목록 요약 문자열 필드 추가
- 저장소 SQL, 행 매퍼, 스키마를 새 필드에 맞춰 정리
- representative 선택 실행 응답의 `executionKey` 기준으로 H2 evidence 확인
- representative 검증에는 fresh `delivery_id`와 `executionKey`를 사용
- representative actual-app 검증은 `NORMAL` 1건과 선택 파일 경로 1~2개를 기준 시나리오로 삼고, `DRY_RUN` actual-app 검증은 이번 task 비대상으로 둔다.

### 관련 파일 후보
- `src/main/java/com/agilerunner/domain/agentruntime/WebhookExecution.java`
- `src/main/java/com/agilerunner/domain/agentruntime/AgentExecutionLog.java`
- `src/main/java/com/agilerunner/api/service/agentruntime/AgentRuntimeService.java`
- `src/main/java/com/agilerunner/client/agentruntime/AgentRuntimeRepository.java`
- `src/main/resources/agent-runtime/schema.sql`
- `src/main/java/com/agilerunner/api/service/review/ManualRerunService.java`
- `src/main/java/com/agilerunner/api/controller/review/ManualRerunController.java`
- `src/test/java/com/agilerunner/api/service/agentruntime/AgentRuntimeServiceTest.java`
- `src/test/java/com/agilerunner/client/agentruntime/AgentRuntimeRepositoryTest.java`
- `src/test/java/com/agilerunner/api/controller/review/ManualRerunControllerTest.java`

### 비대상
- 장기 저장소 도입
- 운영 대시보드 구축
- glob/정규식 선택 문법

### 연결 ValidationCriteria
- `runtime-evidence-records-selection-scope`

### 완료 조건
- runtime evidence에 선택 실행 여부와 정렬된 파일 경로 목록 요약 문자열이 적재된다.
- H2 저장소 왕복 테스트와 전체 테스트가 통과한다.
- 로컬 프로필 실제 앱 기동 후 대표 선택 실행 응답의 `executionKey`를 기준으로 같은 값의 runtime evidence를 확인할 수 있다.

### 검증
- runtime evidence 저장소/서비스 왕복 테스트
- controller/service 회귀 테스트
- 전체 테스트 실행
- 로컬 프로필 실제 앱 기동 후 representative 선택 실행 검증

### GitHub Issue
- 새 Issue 생성
- 권장 제목: `[BE] 선택 실행 근거 적재`
