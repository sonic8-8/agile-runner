# 현재 활성 Spec

## 문서 목적
이 문서는 현재 활성 구현 범위와 후속 구현 범위를 고정하는 `Spec` 문서다.
`ValidationCriteria`와 `Task`는 이 문서의 활성 spec을 기준으로 작성하며, GitHub Issue는 각 `Task`를 외부에서 추적하기 위한 수단으로만 사용한다.

## 현재 활성 Spec
### ID
SPEC-0006

### 이름
수동 재실행 기능 기반 마련

### 목표
- webhook 이벤트와 별개로 특정 PR 리뷰 실행을 다시 요청할 수 있는 수동 재실행 진입점을 만든다.
- 기존 `NORMAL`, `DRY_RUN` 실행 제어 모드를 수동 재실행 경로에서도 그대로 재사용할 수 있게 한다.
- `agent-runtime` 실행 근거에서 webhook 실행과 수동 재실행을 구분할 수 있게 한다.

### 대상 문제
- 현재 리뷰 실행은 GitHub webhook 요청으로만 시작할 수 있어서, 이미 열린 PR에 대해 개발자가 직접 리뷰를 다시 돌리기 어렵다.
- 실행 제어 모드가 생겼지만 이를 webhook 이외의 진입점에서 재사용할 수 있는 수동 실행 경로가 없다.
- 현재 실행 근거에는 실행이 webhook에서 시작됐는지, 수동 재실행에서 시작됐는지 구분값이 없어 운영 판단과 회고가 어렵다.

### 범위
- 수동 재실행용 요청/응답 모델과 HTTP 진입점을 정리한다.
- 수동 재실행 요청은 최소한 저장소 이름, PR 번호, 설치 ID, 실행 제어 모드를 명시적으로 다룰 수 있어야 한다.
- 수동 재실행 경로는 기존 리뷰 생성, 코멘트 작성, dry-run no-write 흐름을 재사용한다.
- 현재 webhook 경로는 그대로 유지하고, 수동 재실행은 별도 진입점으로 추가한다.
- `WebhookExecution`과 `AgentExecutionLog`에 실행 시작 유형을 남길 수 있게 정리한다.
- 로컬 프로필 실제 앱/H2 기준으로 대표 수동 재실행 1건의 실행 근거를 검증한다.

### 비대상
- label/comment 기반 rerun
- 인증/인가 고도화
- 특정 파일, 경로, action만 선택 실행하는 기능
- 운영 대시보드, 조회 API, 장기 저장소 도입
- 자동 재시도, dead-letter 저장소 도입

### 외부 계약
- `/webhook/github` 엔드포인트의 성공 응답과 조기 종료 계약은 유지한다.
- 수동 재실행은 내부/관리자용 기반 HTTP 진입점으로만 추가하고, 현재 webhook 요청 형식은 바꾸지 않는다.
- 수동 재실행 진입점은 `POST /reviews/rerun`으로 두고, 성공 시 `200 OK`와 함께 최소한 `executionKey`, `executionControlMode`, `writePerformed`를 담은 응답을 반환한다.
- 수동 재실행 경로에서도 `NORMAL`, `DRY_RUN` 실행 제어 모드를 사용할 수 있어야 한다.
- 이번 spec에서는 label/comment 기반 재실행과 외부 공개 관리자 권한 모델을 확정하지 않는다.

### 핵심 시나리오
1. 수동 재실행 안전망 고정
   - 기존 webhook 계약이 깨지지 않는다는 점을 먼저 테스트로 고정한다.
   - 새 수동 재실행 진입점의 기본 기대 동작과 응답 계약도 먼저 테스트로 고정한다.
2. 수동 재실행 입력 모델과 진입점 도입
   - 수동 재실행 요청 모델을 정의한다.
   - 수동 재실행용 controller/service 진입점을 만든다.
3. 수동 재실행 실행 경로 연결
   - 수동 재실행 요청이 기존 리뷰 생성, 코멘트 작성, dry-run 분기를 재사용하도록 연결한다.
   - `NORMAL`은 실제 write까지, `DRY_RUN`은 no-write까지 이어져야 한다.
4. 실행 근거 구분값 적재
   - 실행 근거에 webhook 실행과 수동 재실행을 구분하는 값을 남긴다.
   - 실제 앱/H2 기준으로 대표 수동 재실행 1건의 실행 근거를 검증한다.

### Task 분해 기준
- `TASK-0001` 수동 재실행 안전망 고정
- `TASK-0002` 수동 재실행 입력 모델과 진입점 도입
- `TASK-0003` 수동 재실행 실행 경로 연결
- `TASK-0004` 실행 근거 재실행 구분값 적재와 실제 검증

### 연결될 ValidationCriteria
- `webhook-contract-preserved-during-manual-rerun`
- `manual-rerun-entrypoint-contract-defined`
- `manual-rerun-request-resolved-consistently`
- `manual-rerun-respects-execution-control`
- `runtime-evidence-distinguishes-manual-rerun`

### 필수 테스트 시나리오
- 수동 재실행 기능을 넣은 뒤에도 `/webhook/github`의 성공 응답과 조기 종료 계약은 유지된다.
- 수동 재실행 성공 응답은 `200 OK`와 `executionKey`, `executionControlMode`, `writePerformed`를 담는다.
- 수동 재실행 요청은 저장소 이름, PR 번호, 설치 ID, 실행 제어 모드를 일관되게 해석한다.
- 수동 재실행 경로에서 `NORMAL`은 실제 GitHub 코멘트 write까지, `DRY_RUN`은 no-write까지 기존 흐름을 재사용한다.
- 로컬 프로필 실제 앱 기동 시 대표 수동 재실행 응답의 `executionKey`를 기준으로 `WebhookExecution`과 `AgentExecutionLog`에 실행 시작 유형과 실행 제어 모드가 적재된다.

## 후속 Spec 후보
### ID
SPEC-0007

### 이름
선택 실행 기능

### 시작 조건
- `현재 활성 Spec`이 완료되고, 수동 재실행 진입점과 실행 근거 구분값이 안정적으로 정리된 뒤 시작한다.

### 목표
- 재실행 시 전체 PR이 아니라 일부 파일 또는 특정 조건에 맞는 범위만 선택 실행할 수 있는 기반을 만든다.

### 후속 변경 범위
- 파일 또는 경로 기반 선택 실행 검토
- action 기반 실행 범위 축소 검토
- rerun 입력 모델의 선택 조건 확장 검토

### 후속 변경 비대상
- 운영 대시보드 구축
- 장기 저장소 도입
- 자동 재시도 정책

### 후속 검증 방향
- 선택 실행 기능이 기존 webhook 계약과 수동 재실행 계약을 깨지 않는다.
- 선택 조건이 실행 근거와 자연스럽게 연결된다.
