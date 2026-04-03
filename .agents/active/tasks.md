# 현재 활성 Task 집합

## 문서 목적
이 문서는 현재 활성 `Spec`을 실제 구현 단위로 분해한 `Task` 문서다.
각 `Task`는 하나의 명확한 결과, 검증 기준, GitHub Issue 연결 규칙을 가져야 한다.

## 현재 활성 Spec
- 이름: 웹훅 리뷰 흐름 안정화
- 기준 문서:
  - `.agents/active/spec.md`
  - `.agents/criteria/SPEC-0001-webhook-review-stabilization.json`

## 공통 규칙
- 구현 순서는 `TASK-0001 -> TASK-0002 -> TASK-0003 -> TASK-0004`로 고정한다.
- 각 `Task` 시작 전 해당 task용 GitHub Issue를 연결한다.
- `TASK-0001`은 기존 `#1` 이슈를 범위 축소 후 재사용한다.
- `TASK-0002`부터는 task별로 새 GitHub Issue를 발행한다.
- 각 `Task`는 연결된 `ValidationCriteria`와 테스트 근거가 없으면 완료로 보지 않는다.
- 개발 피드백 루프 파생 task는 product `ValidationCriteria` 대신 accepted proposal과 실제 검증 근거를 기준으로 완료를 판단한다.
- 별도 `ValidationCriteria` 키가 없는 유지 동작은 spec 수준 회귀 테스트 근거가 있어야 완료로 본다.

## 요약 표
| Task | 이름 | 핵심 목표 | 연결 기준 | 핵심 검증 | Issue |
| --- | --- | --- | --- | --- | --- |
| `TASK-0001` | preflight 단계 분리와 GitHub 쓰기 전 준비 완료 | preflight 결과 없이 GitHub write가 일어나지 않게 구조 분리 | `payload-accepted`, `comment-write-preflight-completed` | preflight 실패 시 본문/인라인 코멘트 미등록 | 기존 `#1` 재사용 |
| `TASK-0002` | comment posting 시퀀스 고정 | preflight 성공 후 본문 -> 인라인 순서 고정, 성공 응답 계약 유지 | `comment-posting-sequence-fixed`, `successful-comment-response-preserved` | 본문 -> 인라인 순서 확인, `200 OK`와 `GitHubCommentResponse` 유지 | 새 Issue |
| `TASK-0003` | post-write runtime failure non-blocking 처리와 delivery cache 기록 보장 | runtime 기록 실패가 응답 실패로 번지지 않게 하고 same-delivery 재처리 방지 | `same-delivery-comment-idempotent`, `post-write-runtime-failure-tolerated` | runtime 실패 non-blocking, delivery cache 기록 보장 | 새 Issue |
| `TASK-0004` | 회귀 테스트 보강 | 활성 spec의 핵심 시나리오를 자동화 테스트로 고정 | 현재 활성 spec의 모든 `ValidationCriteria` | preflight, success response, same-delivery, skip, 조기 종료 회귀 고정 | 새 Issue |

## TASK-0001
### 이름
preflight 단계 분리와 GitHub 쓰기 전 준비 완료

### 목표
- GitHub 쓰기 전에 필요한 준비 단계를 별도 단계로 분리하고, 준비 단계 실패 시 어떤 GitHub 코멘트도 등록되지 않게 만든다.

### 구현 범위
- comment posting 이전에 사용할 preflight 준비 단계를 명시적으로 분리한다.
- comment service 내부에서 preflight 결과를 표현하는 준비 단계와 실제 write 단계를 분리한다.
- preflight 단계에는 아래를 포함한다.
  - PR 로드
  - patch 조회/파싱
  - path 확인
  - line -> position 변환
  - 게시 가능한 인라인 코멘트 후보 구성
- preflight 단계 중 하나라도 실패하면 본문 코멘트와 인라인 코멘트는 모두 등록하지 않는다.

### 연결 기준
- `payload-accepted`
- `comment-write-preflight-completed`

### 완료 조건
- preflight 준비 단계와 write 단계가 코드 구조상 분리된다.
- preflight가 실패한 경우 본문 코멘트와 인라인 코멘트가 모두 등록되지 않는다.
- write 단계는 preflight 결과를 입력으로 받아서만 동작한다.

### 검증
- 유효한 `pull_request` webhook payload가 service request로 변환되는 테스트
- preflight 실패 시 본문 코멘트와 인라인 코멘트가 모두 미등록되는 테스트

### GitHub Issue
- `#1` 재사용
- 제목과 본문은 `TASK-0001` 범위에 맞게 축소

## TASK-0002
### 이름
comment posting 시퀀스 고정

### 목표
- preflight 성공 이후의 comment posting 순서를 고정하고, 성공 경로의 외부 응답 계약을 유지한다.

### 구현 범위
- preflight 성공 후에만 본문 코멘트를 등록한다.
- 본문 코멘트 등록 이후 게시 가능한 인라인 코멘트를 등록한다.
- successful comment 경로의 응답은 기존과 같이 `200 OK`와 `GitHubCommentResponse`를 유지한다.

### 비대상
- `TASK-0003` 범위인 runtime 기록 실패 non-blocking 처리
- `TASK-0003` 범위인 `deliveryCache.record(deliveryId)` 보장
- `TASK-0003` 범위인 same-delivery idempotency 보장

### 연결 기준
- `comment-posting-sequence-fixed`
- `successful-comment-response-preserved`

### 완료 조건
- preflight 성공 후 본문 코멘트가 먼저 등록되고, 그 다음 게시 가능한 인라인 코멘트가 등록된다.
- successful comment 경로에서 응답 body 형태가 바뀌지 않는다.
- 시퀀스 변경이 preflight 계약을 깨지 않는다.

### 검증
- successful comment 경로에서 본문 코멘트 후 인라인 코멘트 순서를 검증하는 테스트
- successful comment 경로 응답 유지 테스트

### GitHub Issue
- 새 Issue 생성
- 권장 제목: `[BE] comment posting 시퀀스 고정`

## TASK-0003
### 이름
post-write runtime failure non-blocking 처리와 delivery cache 기록 보장

### 목표
- GitHub 코멘트 등록 성공 이후 내부 runtime 기록 실패가 외부 요청 실패로 번지지 않게 만들고, 같은 delivery 재처리 시 추가 comment posting이 일어나지 않게 한다.

### 구현 범위
- `gitHubCommentService.comment(...)` 성공 이후 후속 처리를 분리한다.
- `agentRuntimeService.recordCommentPosted(...)`는 별도 try/catch로 감싸고, 실패해도 응답 실패로 전파하지 않는다.
- GitHub 코멘트 등록이 성공한 경우 `deliveryCache.record(deliveryId)`는 runtime 기록 성공 여부와 무관하게 반드시 수행한다.

### 비대상
- `TASK-0001`에서 고정한 preflight 계약 자체를 다시 바꾸지 않는다.
- `TASK-0002`에서 고정한 본문 코멘트 이후 인라인 코멘트 순서와 기존 성공 응답 계약 자체를 다시 바꾸지 않는다.
- 프로세스 재시작 이후까지 보장하는 영속 idempotency는 이번 task 범위에 포함하지 않는다.
- `TASK-0004`에서 다룰 회귀 테스트 확장 자체는 이번 task 범위에 포함하지 않는다.

### 연결 기준
- `same-delivery-comment-idempotent`
- `post-write-runtime-failure-tolerated`

### 완료 조건
- runtime 기록 실패가 성공 응답을 깨지 않는다.
- `recordCommentPosted(...)` 실패 시에도 기존과 동일한 `200 OK`와 `GitHubCommentResponse`가 유지된다.
- successful comment 이후 동일 delivery 재요청 시 추가 comment posting이 발생하지 않는다.
- delivery cache 기록 보장이 runtime 기록 실패 여부에 의존하지 않는다.
- GitHub 코멘트 등록 성공 이후 `deliveryCache.record(deliveryId)`가 수행된다는 직접 근거가 있다.
- webhook/controller orchestration/agent-runtime 관련 task 종료 검증 규칙에 따라 실제 앱 기동, H2 file DB 생성, representative runtime 적재 확인까지 끝난다.

### 검증
- successful comment 이후 `recordCommentPosted(...)` 실패가 발생해도 `200 OK`와 기존 `GitHubCommentResponse`가 유지되는 테스트
- successful comment 이후 동일 delivery 재요청 시 추가 comment posting이 없는 테스트
- successful comment 이후 `deliveryCache.record(deliveryId)` 호출 또는 동일 delivery 조기 종료를 확인하는 테스트
- local profile 실제 앱 기동 후 H2 file DB 생성과 representative runtime 적재를 확인하는 종료 검증

### GitHub Issue
- 새 Issue 생성
- 권장 제목: `[BE] post-write runtime failure non-blocking 처리`

## TASK-0004
### 이름
회귀 테스트 보강

### 목표
- `TASK-0001`부터 `TASK-0003`까지의 변경을 안전하게 고정할 테스트 세트를 추가한다.

### 구현 범위
- controller/service 수준에서 현재 활성 spec의 필수 테스트 시나리오를 커버한다.
- 아래 항목을 회귀 테스트로 고정한다.
  - 유효한 `pull_request` payload 처리 흐름 진입
  - preflight 실패 시 본문 코멘트와 인라인 코멘트 모두 미등록
  - successful comment 경로의 본문 코멘트 -> 인라인 코멘트 순서 고정
  - successful comment 이후 runtime 기록 실패 non-blocking
  - successful comment 이후 동일 delivery 재요청 시 추가 comment posting 없음
  - successful comment 경로의 `200 OK`와 `GitHubCommentResponse` 유지
  - duplicate delivery / non-`pull_request` 조기 종료 유지
  - 인라인 일부 skip 허용 유지

### 비대상
- production code 동작 변경 자체는 이번 task 범위에 포함하지 않는다.
- 새로운 `ValidationCriteria` 추가나 현재 활성 spec 범위 변경은 이번 task 범위에 포함하지 않는다.
- `agent-runtime` 용어/스키마 정렬, 예외 체계 정리 같은 후속 spec 구현은 이번 task 범위에 포함하지 않는다.
- workflow 규칙 변경이나 개발 피드백 루프 보강 자체는 이번 task 범위에 포함하지 않는다.

### 연결 기준
- 현재 활성 spec의 모든 `ValidationCriteria`

### 완료 조건
- 현재 활성 spec의 필수 테스트 시나리오와 모든 `ValidationCriteria`가 자동화 테스트로 직접 대응된다.
- `TASK-0001`부터 `TASK-0003`까지 고정한 동작이 controller/service 수준 회귀 테스트 세트로 묶인다.
- 이후 `TASK-0001`부터 `TASK-0003` 범위 수정 시 회귀 여부를 바로 확인할 수 있다.

### 검증
- 현재 활성 spec의 필수 테스트 시나리오를 커버하는 회귀 테스트 세트 실행 통과
- 저장소 표준 전체 테스트 실행 통과

### GitHub Issue
- 새 Issue 생성
- 권장 제목: `[BE] 웹훅 리뷰 흐름 안정화 회귀 테스트 보강`

## 후속 Task 후보
- 후속 spec인 `agent-runtime 용어 및 스키마 정렬`은 현재 활성 spec 완료 후 별도 task로 다시 분해한다.
- 후속 spec인 `예외 체계 정리`도 현재 활성 spec 완료 후 별도 task로 다시 분해한다.
- 현재 `active/tasks.md`에는 후속 spec task를 포함하지 않는다.

## 개발 피드백 루프 파생 Task
- 이 섹션의 task는 현재 활성 spec 수행 중 개발 피드백 루프에서 도출된 workflow 보강 작업이다.
- product `ValidationCriteria` 대신 accepted proposal과 실제 검증 근거를 기준으로 수행한다.

## TASK-WF-0001
### 이름
task 종료 검증 규칙 보강

### 목표
- task 종료 전에 targeted test 외에도 더 넓은 검증 근거를 확인하도록 workflow를 보강한다.
- webhook/controller orchestration/agent-runtime 관련 task는 실제 애플리케이션과 H2 file DB 기준의 실행 근거까지 남기도록 만든다.

### 입력 근거
- `WORKFLOW-PROP-0001`
- `TASK-0002` retrospective

### 구현 범위
- `AGENTS.md`에 Tester 2차의 전체 테스트 확인 규칙과 Orchestrator 종료 전 실제 앱/H2/runtime 검증 규칙을 반영한다.
- `.agents/skills/agile-runner-task-loop/SKILL.md`에 같은 종료 검증 규칙을 동기화한다.
- representative webhook 요청과 H2 query로 실제 로컬 검증 근거를 남긴다.

### 완료 조건
- accepted workflow proposal의 핵심 규칙이 `AGENTS.md`와 task loop 스킬에 반영된다.
- local profile 실제 기동, H2 file DB 생성, representative runtime 적재 근거가 남는다.
- 전체 테스트 실행 가능 여부가 확인되고, 가능하면 전체 테스트 green 결과를 남긴다.
- 전체 테스트를 생략하면 retrospective에 생략 사유가 남는다.
- 실제 앱/H2/runtime 검증이 불가하면 retrospective에 사유와 남은 위험이 남는다.

### 검증
- `AGENTS.md`, skill, proposal 간 규칙 정합성 확인
- local profile 실제 기동 및 H2 file DB 생성 확인
- representative webhook 요청 후 H2 row 적재 확인
- 저장소 표준 전체 테스트 실행 결과 확인
- 전체 테스트 미실행 시 retrospective의 생략 사유 확인
- 실제 앱/H2/runtime 검증 불가 시 retrospective의 사유와 남은 위험 기록 확인

### GitHub Issue
- 새 Issue 생성
- 권장 제목: `[BE] task 종료 검증 규칙 보강`
