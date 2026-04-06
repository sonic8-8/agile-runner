# 현재 활성 Spec

## 문서 목적
이 문서는 현재 활성 구현 범위와 후속 구현 범위를 고정하는 `Spec` 문서다.
`ValidationCriteria`와 `Task`는 이 문서의 활성 spec을 기준으로 작성하며, GitHub Issue는 각 `Task`를 외부에서 추적하기 위한 수단으로만 사용한다.

## 현재 활성 Spec
### ID
SPEC-0004

### 이름
실패 대응 강화

### 목표
- 실패한 `WebhookExecution`을 후속 대응 기준으로 더 명확하게 분류한다.
- 같은 `ErrorCode`라도 재시도 가능 여부와 수동 개입 필요 여부를 구분해 운영 판단 기준을 더 빠르게 만든다.
- `agent-runtime` 실행 근거에서 실패 원인뿐 아니라 실패 대응 방향까지 코드 단위로 식별할 수 있게 한다.

### 대상 문제
- 현재는 `ErrorCode`와 오류 메시지까지는 남지만, 이후에 어떤 대응을 해야 하는지 기준이 실행 근거에 없다.
- 같은 실패라도 재시도로 회복 가능한지, 설정 보정이나 수동 확인이 필요한지 빠르게 구분하기 어렵다.
- representative failure evidence는 남지만, 회고와 운영 판단에서 반복적으로 사람이 해석해야 하는 부분이 많다.

### 범위
- 실패 대응 분류 기준을 `재시도 가능`, `재시도 불가`, `수동 조치 필요` 같은 논리 개념으로 정리한다.
- 현재 `ErrorCode`를 실패 대응 분류 기준에 매핑하는 정책을 만든다.
- `WebhookExecution`과 `AgentExecutionLog`에 실패 대응 분류를 남길 수 있도록 모델, 저장소, H2 스키마를 정리한다.
- 예외 체계 정리 이후 유지된 `/webhook/github` 성공 응답과 조기 종료 계약은 그대로 유지한다.
- 로컬 프로필 실제 기동 후 H2 파일 DB에서 representative failure의 대응 분류가 적재되는지 확인한다.
- 이번 spec에서 우선 다루는 핵심 `ErrorCode`는 아래 6개다.
  - `GITHUB_INSTALLATION_ID_MISSING`
  - `OPENAI_CLIENT_MISSING`
  - `OPENAI_REVIEW_FAILED`
  - `GITHUB_APP_CONFIGURATION_MISSING`
  - `GITHUB_COMMENT_PREPARATION_FAILED`
  - `GITHUB_COMMENT_POST_FAILED`

### 비대상
- 실제 자동 재시도 실행
- 실패 보류 큐나 dead-letter 저장소 도입
- 웹훅 외부 응답 계약 전면 개편
- 운영 대시보드, 조회 API, 장기 저장소 도입

### 외부 계약
- `/webhook/github` 엔드포인트의 성공 응답 계약은 유지한다.
- 같은 delivery_id 조기 종료, `pull_request` 외 이벤트 조기 종료, 성공 코멘트 응답 형태는 그대로 둔다.
- 이번 단계에서는 외부 에러 응답 스키마나 실제 재시도 API를 도입하지 않는다.

### 핵심 시나리오
1. 실패 대응 기대 동작 고정
   - 기존 웹훅 성공/조기 종료 계약은 그대로 유지한다.
   - 핵심 `ErrorCode`가 어떤 실패 대응 분류로 이어져야 하는지 먼저 테스트로 고정한다.
2. 실패 대응 분류 도입
   - `ErrorCode`를 대응 분류 기준에 연결하는 정책을 만든다.
   - controller/service 경계와 runtime 기록 입력 기준에서 같은 실패가 같은 대응 분류로 해석되도록 정리한다.
3. 실행 근거 정렬
   - `WebhookExecution`, `AgentExecutionLog`에 실패 대응 분류를 남긴다.
   - representative failure evidence에서 오류 코드와 대응 분류를 같이 확인할 수 있게 한다.
4. 동작 유지 검증
   - 실패 대응 강화 이후에도 웹훅 성공 응답과 조기 종료 계약은 깨지지 않아야 한다.
   - 로컬 프로필 실제 기동 후 대표 실패 실행이 H2 파일 DB에 대응 분류와 함께 적재되어야 한다.

### Task 분해 기준
- `TASK-0001` 실패 대응 안전망 고정
- `TASK-0002` 실패 대응 분류 정책 도입
- `TASK-0003` 실행 근거 대응 분류 적재와 실제 검증

### 연결될 ValidationCriteria
- `webhook-contract-preserved-after-failure-hardening`
- `failure-disposition-classified-consistently`
- `runtime-evidence-records-failure-disposition`

### 필수 테스트 시나리오
- 실패 대응 강화 이후에도 `/webhook/github`의 성공 응답과 조기 종료 계약은 유지된다.
- 핵심 `ErrorCode`가 `재시도 가능`, `재시도 불가`, `수동 조치 필요` 기준으로 일관되게 분류된다.
- 로컬 프로필 실제 앱 기동 시 대표 실패 실행의 `WebhookExecution`과 `AgentExecutionLog`에 대응 분류가 적재된다.

## 후속 Spec 후보
### ID
SPEC-0005

### 이름
실행 제어 기능

### 시작 조건
- `현재 활성 Spec`이 완료되고, 실패 대응 분류와 representative failure evidence가 안정적으로 정리된 뒤 시작한다.

### 목표
- 사용자가 리뷰 실행 범위를 더 직접 제어할 수 있는 기반을 만든다.
- rerun, dry-run, 선택 실행 같은 제어 기능을 붙일 수 있도록 현재 webhook 흐름과 실행 근거를 더 안전하게 분리한다.

### 후속 변경 범위
- dry-run 실행 검토
- label/comment 기반 rerun 검토
- 특정 action 또는 조건 기반 선택 실행 검토

### 후속 변경 비대상
- 운영 대시보드 구축
- 장기 저장소 도입
- 자동 패치 생성 기능

### 후속 검증 방향
- 사용자가 제어한 실행 범위가 기존 webhook 계약을 깨지 않는다.
- 선택 실행이나 rerun 같은 제어 기능이 현재 실행 근거 체계와 자연스럽게 연결된다.
