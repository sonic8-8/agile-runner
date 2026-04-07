# 현재 활성 Spec

## 문서 목적
이 문서는 현재 활성 구현 범위와 후속 구현 범위를 고정하는 `Spec` 문서다.
`ValidationCriteria`와 `Task`는 이 문서의 활성 spec을 기준으로 작성하며, GitHub Issue는 각 `Task`를 외부에서 추적하기 위한 수단으로만 사용한다.

## 현재 활성 Spec
### ID
SPEC-0005

### 이름
실행 제어 기능 기반 마련

### 목표
- 현재 webhook 기반 리뷰 흐름에 `NORMAL`, `DRY_RUN` 같은 실행 제어 모드를 도입해 쓰기 여부를 명시적으로 다룰 수 있게 한다.
- dry-run 실행에서 리뷰 생성은 유지하면서 GitHub write side effect는 막아, 이후 rerun과 선택 실행 기능을 붙일 수 있는 기반을 만든다.
- `agent-runtime` 실행 근거에서 어떤 제어 모드로 실행됐는지와 write 수행 여부, write 생략 이유를 식별할 수 있게 한다.

### 대상 문제
- 현재는 webhook 요청이 들어오면 기본적으로 리뷰 생성 후 GitHub 코멘트 쓰기로 바로 이어져, 쓰기 없는 검증 실행을 명시적으로 다루기 어렵다.
- `dry-run` 같은 제어 모드가 없어 리뷰 생성 결과를 안전하게 미리 확인하거나, 이후 수동 재실행 기능을 붙일 기반이 부족하다.
- 실행 근거에도 어떤 제어 모드로 실행됐는지 남지 않아, normal 실행과 no-write 실행을 회고나 운영 판단에서 구분하기 어렵다.

### 범위
- 실행 제어 모드 타입과 입력 모델을 정리한다.
- 현재 webhook 기반 실행은 기본적으로 `NORMAL` 모드로 해석되게 유지한다.
- `DRY_RUN` 모드에서는 리뷰 생성과 준비 단계는 수행하되, GitHub 본문/인라인 코멘트 write는 수행하지 않는다.
- `WebhookExecution`과 `AgentExecutionLog`에 실행 제어 모드와 write 수행 여부, write 생략 이유를 남길 수 있게 정리한다.
- `/webhook/github` 성공 응답과 조기 종료 계약은 유지한다.
- 실제 앱/H2 대표 `NORMAL` 실행 검증으로 실행 근거가 적재되는지 확인하고, dry-run no-write 경로와 write 생략 이유는 테스트로 고정한다.

### 비대상
- label/comment 기반 수동 rerun
- 특정 파일, 경로, action만 선택 실행하는 기능
- 운영 대시보드, 조회 API, 장기 저장소 도입
- 자동 재시도, dead-letter 저장소 도입
- 외부 공개 dry-run API 확정

### 외부 계약
- `/webhook/github` 엔드포인트의 성공 응답 계약은 유지한다.
- 같은 delivery_id 조기 종료, `pull_request` 외 이벤트 조기 종료, 성공 코멘트 응답 형태는 그대로 둔다.
- 현재 webhook 요청은 별도 제어 입력이 없으면 기본적으로 `NORMAL` 모드로 처리한다.
- 이번 spec에서는 외부 rerun API나 공개 dry-run API를 확정하지 않는다.

### 핵심 시나리오
1. 실행 제어 안전망 고정
   - 기존 webhook 성공/조기 종료 계약은 그대로 유지한다.
   - 이후 실행 제어 기능을 넣어도 깨지면 안 되는 현재 동작과 no-write 기대 동작을 먼저 테스트로 고정한다.
2. 실행 제어 입력 모델 도입
   - 현재 webhook 요청은 `NORMAL` 모드로 해석되도록 정리한다.
   - 서비스 경계에서 실행 제어 모드를 명시적으로 전달할 수 있게 입력 모델을 정리한다.
3. dry-run 분기 도입
   - `DRY_RUN` 모드에서는 리뷰 생성 결과를 만들 수 있지만 GitHub write는 수행하지 않는다.
   - 기존 `NORMAL` 모드는 그대로 코멘트 write까지 이어져야 한다.
4. 실행 근거 정렬
   - `WebhookExecution`, `AgentExecutionLog`에 실행 제어 모드와 write 수행 여부, write 생략 이유를 남긴다.
   - 실제 앱/H2 기준으로 대표 `NORMAL` 실행 근거를 검증하고, dry-run write 생략 이유는 테스트로 검증한다.

### Task 분해 기준
- `TASK-0001` 실행 제어 안전망 고정
- `TASK-0002` 실행 제어 모드와 입력 모델 도입
- `TASK-0003` dry-run no-write 분기 도입
- `TASK-0004` 실행 근거 제어 모드 적재와 실제 검증

### 연결될 ValidationCriteria
- `webhook-contract-preserved-after-execution-control`
- `execution-control-mode-resolved-consistently`
- `dry-run-skips-write-and-preserves-review-flow`
- `runtime-evidence-records-execution-control`

### 필수 테스트 시나리오
- 실행 제어 기능을 넣은 뒤에도 `/webhook/github`의 성공 응답과 조기 종료 계약은 유지된다.
- 현재 webhook 요청은 별도 제어 입력이 없으면 일관되게 `NORMAL` 모드로 해석된다.
- `DRY_RUN` 모드에서는 리뷰 생성은 가능하지만 GitHub 본문/인라인 코멘트 write는 수행하지 않는다.
- 로컬 프로필 실제 앱 기동 시 대표 `NORMAL` 실행의 `WebhookExecution`과 `AgentExecutionLog`에 실행 제어 모드와 write 수행 여부가 적재된다.
- dry-run 경로에서는 write 생략 이유가 테스트에서 검증 가능해야 한다.

## 후속 Spec 후보
### ID
SPEC-0006

### 이름
수동 재실행 기능

### 시작 조건
- `현재 활성 Spec`이 완료되고, `NORMAL`과 `DRY_RUN` 모드 해석 및 실행 근거 적재가 안정적으로 정리된 뒤 시작한다.

### 목표
- 사용자가 기존 webhook 이벤트와 별개로 리뷰 실행을 다시 요청할 수 있는 수동 재실행 기반을 만든다.
- 이후 label/comment 기반 rerun 또는 관리자용 제어 진입점을 붙일 수 있게 실행 제어 입력을 확장한다.

### 후속 변경 범위
- label/comment 기반 rerun 검토
- 재실행 요청 입력 모델 검토
- 대표 재실행 근거와 기존 webhook 실행 근거 구분 검토

### 후속 변경 비대상
- 선택 실행 고도화
- 운영 대시보드 구축
- 장기 저장소 도입

### 후속 검증 방향
- 수동 재실행이 기존 webhook 계약을 깨지 않는다.
- 재실행 요청의 실행 근거가 기존 webhook 실행과 자연스럽게 구분된다.
