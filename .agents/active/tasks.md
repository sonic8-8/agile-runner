# 현재 활성 Task 집합

## 문서 목적
이 문서는 현재 활성 `Spec`을 실제 구현 단위로 분해한 `Task` 문서다.
각 `Task`는 하나의 명확한 결과, 검증 기준, GitHub Issue 연결 규칙을 가져야 한다.

## 현재 활성 Spec
- ID: `SPEC-0008`
- 이름: `재실행 응답 모델 정교화`
- 기준 문서:
  - `.agents/active/spec.md`
  - `.agents/criteria/SPEC-0008-rerun-response-refinement.json`

## 공통 규칙
- 구현 순서는 `TASK-0001 -> TASK-0002 -> TASK-0003 -> TASK-0004`로 고정한다.
- 각 `Task` 시작 전 해당 task용 GitHub Issue를 새로 연결한다.
- 각 `Task`는 연결된 `ValidationCriteria`와 테스트 근거가 없으면 완료로 보지 않는다.
- rerun 응답 모델을 정리하는 동안에도 `/webhook/github` 계약은 그대로 유지한다.
- `POST /reviews/rerun`은 계속 내부/관리자용 진입점으로 유지하고, 성공 HTTP 상태는 기존처럼 `200 OK`를 유지한다.
- 기존 최소 응답 필드 `executionKey`, `executionControlMode`, `writePerformed`는 유지한다.
- 이번 spec에서는 rerun 응답에 실행 결과 상태와 실패 정보 표현을 추가하지만, 조회 API는 만들지 않는다.
- 응답 필드가 늘어나도 runtime evidence와 같은 의미를 가져야 한다.
- `TASK-0001` 시작 전 기존 manual rerun 회귀 안전망과 representative runtime evidence를 먼저 확인하고, 필요한 범위만 새 테스트를 추가한다.

## 요약 표
| Task | 이름 | 핵심 목표 | 연결 ValidationCriteria | 핵심 검증 | Issue |
| --- | --- | --- | --- | --- | --- |
| `TASK-0001` | 재실행 응답 안전망 고정 | 현재 rerun 응답 계약과 runtime evidence 정합성 기준 먼저 고정 | `manual-rerun-contract-preserved-while-response-expands` | rerun/controller 회귀 테스트 | 새 Issue |
| `TASK-0002` | 재실행 응답 모델 확장 | response DTO와 service response에 실행 상태/실패 정보 필드 추가 | `manual-rerun-response-exposes-execution-status-consistently` | DTO/컨트롤러 응답 계약 테스트 | 새 Issue |
| `TASK-0003` | 재실행 실패 상태 응답 연결 | rerun 실패 경로를 응답 필드에 같은 의미로 반영 | `manual-rerun-response-keeps-write-and-failure-state-readable` | service/controller black-box 테스트 | 새 Issue |
| `TASK-0004` | 재실행 응답과 실행 근거 정합성 검증 | 응답 본문과 runtime evidence의 같은 execution key 정합성 확인 | `manual-rerun-response-matches-runtime-evidence` | 실제 앱/H2 representative 검증 | 새 Issue |

## TASK-0001
### 이름
재실행 응답 안전망 고정

### 목표
- rerun 응답을 확장하기 전에 현재 `POST /reviews/rerun` 계약과 이후 task가 재사용할 안전망을 테스트로 먼저 고정한다.

### 구현 범위
- 기존 `ManualRerunControllerTest`, `ManualRerunServiceTest`, `GitHubWebhookControllerTest`, `AgentRuntimeServiceTest`를 재사용하거나 필요한 경우만 보강한다.
- 아래 기준을 우선 고정한다.
  - `POST /reviews/rerun`의 `200 OK` 유지
  - 기존 최소 응답 필드 유지
  - `executionKey`와 runtime evidence 연결 가능성 유지
  - `/webhook/github` 계약 비영향
- 기존 안전망이 충분하면 그 근거를 문서와 회고에 남기고, 부족한 경우만 새 테스트를 추가한다.

### 비대상
- response DTO 필드 추가
- rerun 실패 상태 연결 구현
- runtime evidence 스키마 변경

### 연결 ValidationCriteria
- `manual-rerun-contract-preserved-while-response-expands`

### 완료 조건
- rerun 응답 확장 전 기존 응답 계약과 이후 task가 재사용할 회귀 안전망이 테스트와 체크리스트로 고정된다.
- webhook 계약 비영향도 테스트와 체크리스트로 확인된다.

### 검증
- rerun/controller 회귀 테스트 실행 통과
- 현재 유지 계약과 테스트 근거 대응 관계 점검

### GitHub Issue
- 새 Issue 생성
- 권장 제목: `[BE] 재실행 응답 안전망 고정`

## TASK-0002
### 이름
재실행 응답 모델 확장

### 목표
- manual rerun response DTO와 service response DTO에 실행 결과 상태와 실패 정보 필드를 명시적으로 추가한다.

### 구현 범위
- controller response DTO에 `executionStatus`, `errorCode`, `failureDisposition` 필드 추가
- service response DTO에 같은 필드 추가
- `executionStatus` 허용 값은 이번 spec에서 `SUCCEEDED`, `FAILED`로 고정
- controller/service 경계에서 새 필드가 같은 의미로 전달되게 정리
- 기존 최소 응답 필드는 유지
- 응답 본문은 여전히 `200 OK`와 함께 반환

### 관련 파일 후보
- `src/main/java/com/agilerunner/api/controller/review/response/ManualRerunResponse.java`
- `src/main/java/com/agilerunner/api/service/review/response/ManualRerunServiceResponse.java`
- `src/main/java/com/agilerunner/api/controller/review/ManualRerunController.java`
- `src/test/java/com/agilerunner/api/controller/review/ManualRerunControllerTest.java`

### 비대상
- rerun 실패 경로의 실제 값 연결
- runtime evidence 스키마 변경
- 조회 API 추가

### 연결 ValidationCriteria
- `manual-rerun-response-exposes-execution-status-consistently`

### 완료 조건
- rerun 응답이 `executionStatus`, `errorCode`, `failureDisposition` 필드를 포함한다.
- controller/service response DTO가 같은 의미로 필드를 전달한다.
- `executionStatus`는 `SUCCEEDED`, `FAILED`만 사용한다.
- 기존 `executionKey`, `executionControlMode`, `writePerformed` 계약은 유지된다.

### 검증
- response DTO 테스트
- controller black-box 테스트
- 기존 rerun 회귀 테스트

### GitHub Issue
- 새 Issue 생성
- 권장 제목: `[BE] 재실행 응답 모델 확장`

## TASK-0003
### 이름
재실행 실패 상태 응답 연결

### 목표
- review 생성 실패, 코멘트 작성 실패, dry-run non-write 같은 rerun 결과를 응답 필드에서 구분 가능하게 만든다.

### 구현 범위
- `ManualRerunService`가 `executionStatus`, `errorCode`, `failureDisposition`, `writePerformed`를 같은 의미로 채운다.
- review 생성 실패와 코멘트 작성 실패는 `FAILED` 상태 아래에서 `errorCode`, `failureDisposition` 조합으로 읽히게 정리한다.
- dry-run은 `SUCCEEDED + writePerformed=false + errorCode=null + failureDisposition=null` 조합으로 해석되게 정리한다.
- controller 응답과 service 응답이 같은 의미를 유지한다.

### 관련 파일 후보
- `src/main/java/com/agilerunner/api/service/review/ManualRerunService.java`
- `src/main/java/com/agilerunner/api/service/review/response/ManualRerunServiceResponse.java`
- `src/main/java/com/agilerunner/api/controller/review/response/ManualRerunResponse.java`
- `src/test/java/com/agilerunner/api/service/review/ManualRerunServiceTest.java`
- `src/test/java/com/agilerunner/api/controller/review/ManualRerunControllerTest.java`

### 비대상
- runtime evidence 컬럼 추가
- execution key 기반 조회 API
- webhook 응답 계약 변경

### 연결 ValidationCriteria
- `manual-rerun-response-keeps-write-and-failure-state-readable`

### 완료 조건
- rerun 응답에서 실패 상태와 GitHub 코멘트 작성 여부를 함께 읽을 수 있다.
- review 생성 실패, 코멘트 작성 실패, dry-run non-write가 `executionStatus`, `errorCode`, `failureDisposition`, `writePerformed` 조합으로 구분된다.
- 기존 최소 응답 필드는 유지된다.

### 검증
- service black-box 테스트
- controller black-box 테스트
- 기존 rerun/webhook 회귀 테스트

### GitHub Issue
- 새 Issue 생성
- 권장 제목: `[BE] 재실행 실패 상태 응답 연결`

## TASK-0004
### 이름
재실행 응답과 실행 근거 정합성 검증

### 목표
- 대표 manual rerun 실패 시나리오 1건의 `executionKey`를 기준으로 runtime evidence를 조회해 응답 본문과 같은 의미가 남는지 확인한다.

### 구현 범위
- rerun 응답에 담기는 `executionStatus`, `errorCode`, `failureDisposition`, `writePerformed`가 runtime evidence와 같은 의미가 되는지 검증
- 정합성 검증에 꼭 필요한 최소 매핑 보정만 허용
- 새 응답 필드 추가나 새 스키마 컬럼 추가는 비대상
- local profile 실제 앱/H2 representative 검증 수행
- representative 검증에는 fresh `delivery_id`와 fresh `executionKey`를 사용
- representative 검증은 최소 1건의 실패 시나리오를 사용해 응답과 runtime evidence를 함께 확인

### 관련 파일 후보
- `src/main/java/com/agilerunner/api/service/review/ManualRerunService.java`
- `src/main/java/com/agilerunner/api/service/agentruntime/AgentRuntimeService.java`
- `src/main/java/com/agilerunner/client/agentruntime/AgentRuntimeRepository.java`
- `src/main/java/com/agilerunner/api/controller/review/ManualRerunController.java`
- `src/test/java/com/agilerunner/api/service/review/ManualRerunServiceTest.java`
- `src/test/java/com/agilerunner/api/controller/review/ManualRerunControllerTest.java`
- `src/test/java/com/agilerunner/api/service/agentruntime/AgentRuntimeServiceTest.java`
- `src/test/java/com/agilerunner/client/agentruntime/AgentRuntimeRepositoryTest.java`

### 비대상
- 운영 대시보드 구축
- 결과 조회 API 추가
- 장기 저장소 도입

### 연결 ValidationCriteria
- `manual-rerun-response-matches-runtime-evidence`

### 완료 조건
- 대표 manual rerun 실패 시나리오 1건의 `executionKey`를 기준으로 runtime evidence를 조회했을 때 응답 본문의 `executionStatus`, `errorCode`, `failureDisposition`, `writePerformed`와 같은 의미가 확인된다.
- targeted test, 전체 테스트, 실제 앱/H2 representative 검증이 모두 통과한다.

### 검증
- runtime evidence 서비스/저장소 테스트
- controller/service 회귀 테스트
- 전체 테스트 실행
- 로컬 프로필 실제 앱 기동 후 representative rerun 검증

### GitHub Issue
- 새 Issue 생성
- 권장 제목: `[BE] 재실행 응답과 실행 기록 정합성 검증`
