# 현재 활성 Spec

## 문서 목적
이 문서는 현재 활성 구현 범위와 후속 구현 범위를 고정하는 `Spec` 문서다.
`ValidationCriteria`와 `Task`는 이 문서의 활성 spec을 기준으로 작성하며, GitHub Issue는 각 `Task`를 외부에서 추적하기 위한 수단으로만 사용한다.

## 현재 활성 Spec
### ID
SPEC-0009

### 이름
재실행 결과 조회 기능 기반 마련

### 목표
- `executionKey`를 기준으로 최근 manual rerun 결과를 다시 조회할 수 있는 최소 기능을 연다.
- rerun 직후 응답과 rerun 결과 조회 응답이 서로 모순되지 않도록 응답 정책을 고정한다.
- 내부/관리자용 조회 진입점에서 어떤 실행 근거까지 노출할지 최소 기준을 정한다.

### 대상 문제
- 현재는 rerun 요청 직후 응답만 있고, 나중에 같은 `executionKey`로 결과를 다시 확인하려면 H2 evidence를 직접 조회해야 한다.
- rerun 응답과 runtime evidence는 정합성을 갖게 됐지만, 운영자는 이를 API로 다시 읽을 수 없다.
- 조회 기능이 없으면 후속 운영성 기능을 붙일 때도 `executionKey` 기반 추적 경계를 다시 설계해야 한다.

### 범위
- 내부/관리자용 `GET /reviews/rerun/{executionKey}` 최소 조회 진입점을 도입한다.
- 조회 응답은 최소한 `executionKey`, `executionControlMode`, `writePerformed`, `executionStatus`, `errorCode`, `failureDisposition`를 포함한다.
- query response는 manual rerun runtime evidence에서 읽은 값을 그대로 전달하는 방향으로 정리한다.
- 존재하지 않는 `executionKey`는 `404 Not Found`와 최소 본문 `executionKey`, `message`로 정리한다.
- 로컬 프로필 실제 앱/H2 기준으로 대표 manual rerun 1건 생성 후 같은 `executionKey`로 조회 응답과 runtime evidence 정합성을 검증한다.

### 비대상
- 운영 대시보드 구축
- 장기 저장소 도입
- 자동 재시도 정책 추가
- webhook 조회 API 추가
- rerun 요청 응답 모델 재설계

### 외부 계약
- `/webhook/github` 계약은 유지한다.
- `POST /reviews/rerun`의 `200 OK` 계약과 기존 응답 필드는 유지한다.
- `GET /reviews/rerun/{executionKey}`는 내부/관리자용 최소 조회 진입점으로만 사용한다.
- 조회 응답은 현재 rerun 응답과 모순되지 않아야 하고, runtime evidence와 같은 의미를 가져야 한다.

### 핵심 시나리오
1. 재실행 결과 조회 안전망 고정
   - 현재 rerun 응답과 runtime evidence 정합성 기준이 유지된다는 점을 먼저 테스트로 고정한다.
   - 조회 기능 추가가 webhook 계약에 영향을 주지 않는다는 점도 같이 고정한다.
2. 재실행 결과 조회 입력 모델과 진입점 도입
   - `executionKey` 기반 조회 request 경계와 controller/service 진입점을 연다.
   - not found 정책도 이 단계에서 `404 Not Found + executionKey + message` 기준으로 고정한다.
3. 재실행 결과 조회 응답 연결
   - runtime evidence에서 읽은 값을 조회 응답 DTO에 같은 의미로 연결한다.
   - rerun 응답과 조회 응답의 공통 필드 의미가 같게 유지되도록 정리한다.
4. 조회 응답과 실행 근거 정합성 검증
   - representative manual rerun 1건을 실제 앱으로 실행한 뒤, 같은 `executionKey`로 조회 응답과 H2 evidence 일치를 확인한다.

### Task 분해 기준
- `TASK-0001` 재실행 결과 조회 안전망 고정
- `TASK-0002` 재실행 결과 조회 입력 모델과 진입점 도입
- `TASK-0003` 재실행 결과 조회 응답 연결
- `TASK-0004` 조회 응답과 실행 근거 정합성 검증

### 연결될 ValidationCriteria
- `manual-rerun-query-contract-preserved`
- `manual-rerun-query-input-and-not-found-policy-defined`
- `manual-rerun-query-response-matches-rerun-meaning`
- `manual-rerun-query-response-matches-runtime-evidence`

### 필수 테스트 시나리오
- 조회 기능을 추가해도 `/webhook/github`와 `POST /reviews/rerun` 계약은 유지된다.
- `GET /reviews/rerun/{executionKey}`는 최소 응답 필드와 not found 정책을 일관되게 반환한다.
- 존재하지 않는 `executionKey`는 `404 Not Found + executionKey + message`로 읽을 수 있어야 한다.
- 조회 응답은 `executionStatus`, `errorCode`, `failureDisposition`, `writePerformed`를 rerun 응답과 같은 의미로 보여 준다.
- representative manual rerun 1건의 `executionKey`로 조회했을 때 응답 본문과 runtime evidence의 값이 같은 의미로 남아 있다.
- representative 검증은 `manual rerun 1건 생성 -> 같은 executionKey로 GET 조회 -> 앱 종료 -> H2 조회` 순서를 따른다.

## 후속 Spec 후보
### ID
SPEC-0010

### 이름
재실행 재시도 정책 정교화

### 시작 조건
- `현재 활성 Spec`이 완료되고, execution key 기반 결과 조회가 안정적으로 닫힌 뒤 시작한다.

### 목표
- failure disposition과 실행 결과를 바탕으로 rerun 재시도 정책을 더 구체화한다.

### 후속 변경 범위
- retryable/manual action required 분기별 재시도 정책
- 재시도 요청 입력 정책
- 실행 이력과 재시도 관계 정리

### 후속 변경 비대상
- 운영 대시보드 구축
- 장기 저장소 도입
- 자동 스케줄러 도입

### 후속 검증 방향
- 재시도 정책이 failure disposition과 모순되지 않는다.
- 재실행 결과 조회 응답과 재시도 입력 정책이 함께 읽힐 수 있다.
