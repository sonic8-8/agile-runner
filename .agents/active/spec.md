# 현재 활성 Spec

## 문서 목적
이 문서는 현재 활성 구현 범위와 후속 구현 범위를 고정하는 `Spec` 문서다.
`ValidationCriteria`와 `Task`는 이 문서의 활성 spec을 기준으로 작성하며, GitHub Issue는 각 `Task`를 외부에서 추적하기 위한 수단으로만 사용한다.

## 현재 활성 Spec
### 이름
웹훅 리뷰 흐름 안정화

### 목표
- GitHub webhook 처리 흐름에서 중복 본문 코멘트가 생길 수 있는 실패 경로를 제거한다.
- GitHub 코멘트 등록이 성공한 뒤 내부 runtime 기록에 실패하더라도 외부 응답 계약은 유지한다.
- 현재 유지해야 하는 기존 동작과 회귀 위험을 명시적으로 고정한다.

### 대상 문제
- 본문 코멘트 등록 이후 후속 단계에서 실패하면 동일 delivery 재처리 시 중복 본문 코멘트가 발생할 수 있다.
- 현재 코멘트 등록 성공과 runtime 기록 성공이 같은 성공 경로에 묶여 있어, 내부 기록 실패가 외부 요청 실패로 번질 수 있다.
- comment posting 시퀀스를 조정할 때 인라인 코멘트 개별 skip 정책이나 기존 조기 종료 경로가 함께 깨질 수 있다.

### 범위
- GitHub 쓰기 전 preflight 단계를 명시적으로 분리한다.
- preflight가 끝나기 전에는 어떤 GitHub 쓰기도 수행하지 않는다.
- GitHub 코멘트 등록 성공 이후 runtime 기록 실패는 non-blocking으로 처리한다.
- GitHub 코멘트 등록이 성공한 경우 `deliveryCache.record(deliveryId)`는 runtime 기록 성공 여부와 무관하게 반드시 수행한다.
- 성공 응답의 HTTP 상태와 `GitHubCommentResponse` 형태는 유지한다.
- 인라인 코멘트 개별 skip 정책은 유지한다.

### 비대상
- `agent-runtime` 타입/스키마 이름 정렬
- webhook signature 검증
- `pull_request` action 화이트리스트 정리
- 메모리 캐시를 넘는 중복 delivery 영속화
- rerun, dry-run, 조회 API 같은 사용자 제어 기능

### 외부 계약
- `/webhook/github`의 엔드포인트와 기본 응답 흐름은 유지한다.
- duplicate delivery는 기존처럼 `200 OK`로 조기 종료한다.
- `pull_request`가 아닌 이벤트는 기존처럼 `200 OK`로 조기 종료한다.
- successful comment 경로의 응답 body는 기존 `GitHubCommentResponse`를 유지한다.

### 핵심 시나리오
1. webhook 수신 후 preflight 준비
   - PR 로드
   - patch 조회/파싱
   - path 확인
   - line -> position 변환
   - 게시 가능한 인라인 코멘트 후보 구성
   - 위 단계 중 하나라도 실패하면 GitHub 본문 코멘트와 인라인 코멘트는 모두 등록되지 않아야 한다.
2. successful comment 경로
   - preflight 성공 후에만 본문 코멘트를 등록한다.
   - 본문 코멘트 등록 이후 게시 가능한 인라인 코멘트를 등록한다.
3. post-write runtime failure 경로
   - GitHub 코멘트 등록이 끝난 뒤 `agent-runtime` 기록이 실패해도 외부 응답은 성공 경로를 유지한다.
   - 이 경우에도 `deliveryCache.record(deliveryId)`는 수행돼야 한다.
4. 기존 유지 경로
   - duplicate delivery 조기 종료 유지
   - non-`pull_request` 조기 종료 유지
   - 인라인 코멘트 일부 skip 허용 유지

### Task 분해 기준
- `TASK-0001` preflight 준비 완료 전 GitHub 쓰기 금지
- `TASK-0002` comment posting 시퀀스 고정
- `TASK-0003` runtime failure non-blocking 처리와 delivery cache 기록 보장
- `TASK-0004` 회귀 테스트 추가

### 연결될 ValidationCriteria
- `payload-accepted`
- `comment-write-preflight-completed`
- `comment-posting-sequence-fixed`
- `successful-comment-response-preserved`
- `same-delivery-comment-idempotent`
- `post-write-runtime-failure-tolerated`

### 필수 테스트 시나리오
- 유효한 `pull_request` webhook payload는 정상적으로 파싱되고 처리 흐름에 진입한다.
- preflight 실패 시 본문 코멘트와 인라인 코멘트가 모두 등록되지 않는다.
- successful comment 이후 runtime 기록 실패가 요청 실패로 번지지 않는다.
- successful comment 이후 동일 delivery 재요청 시 추가 comment posting이 발생하지 않는다.
- successful comment 경로에서 본문 코멘트가 먼저 등록되고, 그 다음 게시 가능한 인라인 코멘트가 등록된다.
- successful comment 경로에서 응답은 기존과 같이 `200 OK`와 `GitHubCommentResponse`를 유지한다.
- duplicate delivery와 non-`pull_request` 이벤트는 기존처럼 `200 OK`로 조기 종료한다.
- 인라인 코멘트 일부 skip 정책이 유지된다.

## 후속 Spec 후보
### 이름
agent-runtime 용어 및 스키마 정렬

### 시작 조건
- `현재 활성 Spec`이 완료되고, 관련 회귀 테스트가 안정적으로 통과한 뒤 시작한다.

### 목표
- PRD 기준 용어를 코드, 저장소 API, 스키마 이름에 맞춘다.
- runtime 데이터 모델의 의미 충돌을 줄인다.

### 후속 변경 범위
- `ReviewRun` -> `WebhookExecution`
- `ReviewRunStatus` -> `WebhookExecutionStatus`
- `TaskState` -> `TaskRuntimeState`
- `TaskStateStatus` -> `TaskRuntimeStatus`
- `EvaluationCriteria` -> `ValidationCriteria`
- `TASK_STATE` -> `TASK_RUNTIME_STATE`
- `EVALUATION_CRITERIA` -> `VALIDATION_CRITERIA`
- `REVIEW_RUN` -> `WEBHOOK_EXECUTION`
- `run_key` -> `execution_key`
- `AGENT_EXECUTION_LOG.run_key` -> `AGENT_EXECUTION_LOG.execution_key`
- controller, service, repository, tests까지 같은 용어로 일괄 정리
- `TaskState`, `AgentExecutionLog` 도메인 필드와 생성자 시그니처까지 rename 범위에 포함한다.

### 후속 변경 비대상
- 현재 활성 spec의 bug fix 동작 변경
- 외부 webhook 응답 계약 변경
- `issue_number` 의미 재설계나 추적 모델 변경

### 후속 검증 방향
- renamed schema round-trip
- renamed repository/service API 정합성
- renamed domain type과 controller import 정합성

### 이름
예외 체계 정리

### 시작 조건
- `현재 활성 Spec`이 완료되고, 관련 회귀 테스트와 실제 앱/H2 검증 규칙이 안정적으로 적용된 뒤 시작한다.

### 목표
- `RuntimeException`, `IllegalStateException` 중심의 임시 예외 사용을 줄이고, 애플리케이션 경계에서 해석 가능한 예외 체계를 만든다.
- `AgileRunnerException`과 `ErrorCode`를 기준으로 설정 오류, payload 오류, 외부 연동 오류, 내부 처리 오류를 더 일관되게 분류한다.
- webhook/controller/service 경계에서 예외 변환, 로깅, runtime evidence 기록 기준을 명확히 한다.

### 후속 변경 범위
- `AgileRunnerException` 기반 애플리케이션 공통 예외 도입
- `ErrorCode` 정의와 코드별 의미, 로그 기준, 사용자 노출 정책 정리
- 설정 누락, payload 파싱, OpenAI 호출, GitHub 연동, comment posting, runtime 기록 실패의 분류 기준 정리
- 현재 `RuntimeException`, `IllegalStateException` 사용 지점을 새 예외 체계로 치환
- webhook/controller/service 경계별 예외 변환과 runtime evidence 기록 규칙 정리

### 후속 변경 비대상
- 공통 `ApiResponse` 래퍼 도입
- webhook 외부 응답 계약의 전면 개편
- 도메인 모델 전체를 business exception 중심으로 재구성하는 작업
- 에러 UI나 별도 운영 대시보드 도입

### 후속 검증 방향
- 주요 실패 경로가 `ErrorCode`로 일관되게 분류된다.
- 예외 변환 후에도 `/webhook/github` 외부 응답 계약이 불필요하게 깨지지 않는다.
- runtime evidence와 로그에서 실패 원인을 코드 단위로 빠르게 식별할 수 있다.
