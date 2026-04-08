# 현재 활성 Spec

## 문서 목적
이 문서는 현재 활성 구현 범위와 후속 구현 범위를 고정하는 `Spec` 문서다.
`ValidationCriteria`와 `Task`는 이 문서의 활성 spec을 기준으로 작성하며, GitHub Issue는 각 `Task`를 외부에서 추적하기 위한 수단으로만 사용한다.

## 현재 활성 Spec
### ID
SPEC-0008

### 이름
재실행 응답 모델 정교화

### 목표
- 수동 재실행 응답에서 실행 결과와 실패 상태를 더 분명하게 표현한다.
- 수동 재실행 응답이 runtime evidence와 모순되지 않게 정리한다.
- 내부/관리자용 진입점인 `POST /reviews/rerun`에서 어떤 정보까지 노출할지 응답 모델 기준을 고정한다.

### 대상 문제
- 현재 수동 재실행 응답은 `executionKey`, `executionControlMode`, `writePerformed`만 내려주므로, 요청이 실패했는지 단순히 쓰기를 생략한 것인지 바로 구분하기 어렵다.
- 실제 실패 원인과 후속 조치 성격은 runtime evidence에 남지만, 응답 본문에서는 충분히 드러나지 않는다.
- 내부/관리자용 진입점인데도 응답 모델이 너무 얇아서, 운영자가 rerun 결과를 확인하려면 H2 evidence를 다시 조회해야 한다.

### 범위
- `POST /reviews/rerun` 응답 모델에 `executionStatus(실행 결과 상태)`를 명시적으로 추가한다.
- rerun 응답은 `executionStatus`, `errorCode`, `failureDisposition(후속 조치 유형)`을 모두 포함하고, 성공 시에는 `errorCode`, `failureDisposition`이 `null`이 되도록 정리한다.
- `executionStatus`의 허용 값은 이번 spec에서 `SUCCEEDED`, `FAILED` 두 값으로 고정한다.
- 성공/실패 여부와 GitHub 코멘트 작성 여부가 응답에서 함께 읽히도록 정리한다.
- 응답 본문 값과 runtime evidence의 `executionKey(실행 키)`, `executionStatus`, `errorCode`, `failureDisposition`, `writePerformed`가 같은 의미를 갖도록 정리한다.
- 로컬 프로필 실제 앱/H2 기준으로 대표 manual rerun 실패 시나리오 1건의 응답과 runtime evidence 정합성을 검증한다.

### 비대상
- `/webhook/github` 응답 계약 변경
- 운영 대시보드나 조회 API 추가
- 자동 재시도 정책 추가
- 장기 저장소 도입
- label/comment 기반 rerun

### 외부 계약
- `/webhook/github`의 성공 응답과 조기 종료 계약은 유지한다.
- `POST /reviews/rerun`은 계속 내부/관리자용 진입점으로 유지한다.
- `POST /reviews/rerun`의 성공 HTTP 상태는 기존처럼 `200 OK`를 유지하되, 응답 본문은 더 풍부하게 만든다.
- 기존 최소 응답 필드인 `executionKey`, `executionControlMode`, `writePerformed`는 유지한다.
- 이번 spec에서는 rerun 응답에 `executionStatus`, `errorCode`, `failureDisposition`을 추가한다.
- rerun 응답은 runtime evidence와 모순되지 않아야 하고, 실패 상태가 있어도 `executionKey`는 계속 반환한다.

### 핵심 시나리오
1. 재실행 응답 안전망 고정
   - 현재 rerun 응답 계약과 runtime evidence 정합성 기준을 먼저 테스트로 고정한다.
   - webhook 계약에는 영향이 없다는 점도 같이 고정한다.
2. 재실행 응답 모델 확장
   - controller/service response DTO에 `executionStatus`, `errorCode`, `failureDisposition` 필드를 추가한다.
   - 내부/관리자용 응답에서 어떤 필드를 노출할지 경계를 고정한다.
3. 재실행 실패 상태 응답 연결
   - review 생성 실패와 코멘트 작성 실패를 `FAILED` 상태 아래에서 같은 의미로 응답 필드에 반영한다.
   - dry-run non-write는 `SUCCEEDED + writePerformed=false + errorCode=null + failureDisposition=null` 조합으로 읽히게 정리한다.
   - 응답의 `writePerformed`, `errorCode`, `failureDisposition`이 실제 실행 결과와 같은 의미를 갖도록 정리한다.
4. 응답과 실행 근거 정합성 검증
   - 대표 manual rerun 실패 시나리오 1건의 `executionKey`를 기준으로 runtime evidence를 조회해 응답과 같은 값이 남는지 확인한다.

### Task 분해 기준
- `TASK-0001` 재실행 응답 안전망 고정
- `TASK-0002` 재실행 응답 모델 확장
- `TASK-0003` 재실행 실패 상태 응답 연결
- `TASK-0004` 재실행 응답과 실행 근거 정합성 검증

### 연결될 ValidationCriteria
- `manual-rerun-contract-preserved-while-response-expands`
- `manual-rerun-response-exposes-execution-status-consistently`
- `manual-rerun-response-keeps-write-and-failure-state-readable`
- `manual-rerun-response-matches-runtime-evidence`

### 필수 테스트 시나리오
- rerun 응답 모델을 확장해도 `/webhook/github` 계약과 `POST /reviews/rerun`의 `200 OK` 계약은 유지된다.
- rerun 응답은 `executionKey`, `executionControlMode`, `writePerformed`를 계속 포함한다.
- rerun 응답은 `executionStatus`, `errorCode`, `failureDisposition`을 함께 포함하고, `executionStatus`는 `SUCCEEDED` 또는 `FAILED`만 사용한다.
- review 생성 실패와 코멘트 작성 실패는 rerun 응답에서 같은 `FAILED` 상태 아래 `errorCode`, `failureDisposition` 조합으로 해석 가능해야 한다.
- dry-run과 실제 쓰기 수행 여부는 `SUCCEEDED/FAILED` 상태와 `writePerformed` 조합으로 구분 가능해야 한다.
- 대표 manual rerun 실패 시나리오 1건의 `executionKey`를 기준으로 runtime evidence를 조회했을 때 `executionStatus`, `errorCode`, `failureDisposition`, `writePerformed`가 같은 의미로 남아 있어야 한다.

## 후속 Spec 후보
### ID
SPEC-0009

### 이름
재실행 결과 조회 기능 기반 마련

### 시작 조건
- `현재 활성 Spec`이 완료되고, rerun 응답 모델과 runtime evidence 정합성이 안정적으로 정리된 뒤 시작한다.

### 목표
- execution key를 기준으로 최근 재실행 결과를 다시 조회할 수 있는 최소 조회 기능 방향을 검토한다.

### 후속 변경 범위
- execution key 기반 rerun 결과 조회 모델 검토
- 내부/관리자용 결과 조회 응답 정책 검토
- rerun 응답과 조회 응답의 역할 분리 검토

### 후속 변경 비대상
- 운영 대시보드 구축
- 장기 저장소 도입
- 자동 재시도 정책

### 후속 검증 방향
- rerun 응답과 rerun 결과 조회 응답이 서로 모순되지 않는다.
- execution key를 기준으로 최소 결과 확인이 가능하다.
