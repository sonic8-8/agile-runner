# 현재 활성 작업 집합

## 문서 목적
이 문서는 현재 활성 범위를 실제 구현 단위로 분해한 작업 문서다.
각 작업은 하나의 명확한 결과, 검증 기준, GitHub 이슈 연결 규칙을 가져야 한다.

## 현재 활성 단계
- ID: `SPEC-0030`
- 이름: `운영용 조회 응답 준비 데이터 반복 검증 스크립트 초안 구현`
- 기준 문서:
  - `.agents/active/spec.md`
  - `.agents/criteria/SPEC-0030-manual-rerun-script-draft-implementation.json`

## 공통 규칙
- 구현 순서는 `TASK-0001 -> TASK-0002 -> TASK-0003 -> TASK-0004`로 고정한다.
- 각 작업 시작 전 해당 작업용 GitHub 이슈를 새로 연결한다.
- 각 작업은 연결된 검증 기준과 테스트 근거가 없으면 완료로 보지 않는다.
- 기존 `POST /reviews/rerun`, `POST /reviews/rerun/{executionKey}/retry`, `GET /reviews/rerun/{executionKey}`, `GET /reviews/rerun/executions`, `GET /reviews/rerun/{executionKey}/actions/history`, `POST /reviews/rerun/{executionKey}/actions` 계약은 유지한다.
- 이번 단계는 대표 검증 스크립트 초안 파일 구현과 대표 검증에 집중한다.
- `TASK-0001` 시작 전 `SPEC-0029` 단계 요약 문서와 현재 가이드 문서가 실제 초안 파일 구현 시작 안전망으로 충분한지 먼저 확인한다.

## 요약 표
| 작업 | 이름 | 핵심 목표 | 연결 검증 기준 | 핵심 검증 | 이슈 |
| --- | --- | --- | --- | --- | --- |
| `TASK-0001` | 초안 구현 시작 전 기존 기준과 안전망 확인 | 실제 파일 구현 시작 전 기존 판단과 자동 검증 유지 확인 | `manual-rerun-script-draft-safety-net-preserved` | 회귀 테스트 + 직전 단계 요약과 가이드 근거 확인 | 새 이슈 |
| `TASK-0002` | 준비와 정리 초안 파일 구현 | 준비 데이터 정리와 적용 흐름을 실제 파일로 옮기기 | `manual-rerun-script-draft-prepare-implemented` | 대상 테스트 + 전체 테스트 + 임시 출력 파일 생성 확인 | 새 이슈 |
| `TASK-0003` | rerun, retry, 실행 근거 수집 초안 파일 구현 | rerun/retry 요청과 실행 근거 수집 흐름을 실제 파일로 옮기기 | `manual-rerun-script-draft-run-flow-implemented` | 대상 테스트 + 전체 테스트 + 출력 파일과 종료 흐름 확인 | 새 이슈 |
| `TASK-0004` | 초안 파일 대표 검증과 단계 마감 | 실제 초안 파일로 대표 재실행 검증과 대표 재시도 검증을 닫기 | `manual-rerun-script-draft-representative-verified` | 대상 테스트 + 전체 테스트 + 실제 앱/H2 대표 검증 | 새 이슈 |

## TASK-0001
### 이름
초안 구현 시작 전 기존 기준과 안전망 확인

### 목표
- 대표 검증 스크립트 초안 구현 단계를 시작하기 전에 `SPEC-0029` 마감 근거와 현재 가이드가 시작 안전망으로 충분한지 확인한다.

### 구현 범위
- 기존 회고와 관련 controller/service 테스트를 우선 재사용한다.
- 아래 기준을 먼저 확인한다.
  - 대표 검증에서 실제 초안 파일로 옮길 명령 묶음 정리
  - 실제 구현으로 넘어가도 되는 판단 근거 정리
  - 자동 검증 테스트 유지
  - 재실행/재시도 대표 검증 근거 유지
  - `SPEC-0029` 단계 요약 문서 연결
- 기존 안전망이 충분하면 근거를 회고에 남기고, 부족한 경우만 최소 문서 보강을 검토한다.

### 비대상
- 실제 초안 파일 추가
- 실제 앱/H2 대표 검증 재실행

### 연결 검증 기준
- `manual-rerun-script-draft-safety-net-preserved`

### 완료 조건
- 스크립트 초안 구현 단계의 시작 근거가 충분하다는 회고와 테스트 근거가 남는다.

### 검증
- 관련 대상 테스트 실행 통과
- 저장소 표준 전체 테스트 명령 통과
- `SPEC-0029` 단계 요약 문서와 `SPEC-0026 / TASK-0004` 회고 경로 명시

### GitHub 이슈
- 새 이슈 생성
- 권장 제목: `[BE] 초안 구현 시작 전 기존 기준과 안전망 확인`

## TASK-0002
### 이름
준비와 정리 초안 파일 구현

### 목표
- 시작 전 점검, 정리 SQL 실행, 준비 데이터 적용 SQL 실행 흐름을 `prepare-seed.sh` 초안 파일로 옮긴다.

### 구현 범위
- 아래 기준을 실제 파일에 반영한다.
  - `scripts/manual-rerun-response/prepare-seed.sh` 추가
  - 입력 인자와 출력 파일 이름 반영
  - 시작 전 점검, 정리 SQL 실행, 준비 데이터 적용 SQL 실행 흐름 반영
  - 기존 보조 명령 가이드와 책임 경계 유지

### 관련 파일 후보
- `docs/manual-rerun-response-seed-command-guide.md`
- `docs/manual-rerun-response-seed-guide.md`
- `scripts/manual-rerun-response/prepare-seed.sh`
- `.agents/outer-loop/retrospectives/SPEC-0029/SPEC-0029-summary.md`
- 위 단계 요약 문서는 실제 초안 구현 단계로 넘어가도 된다는 마감 문서다.

### 비대상
- rerun/retry 요청 스크립트 구현
- 실제 앱/H2 대표 검증 재실행
- 새 대표 검증 시나리오 추가

### 연결 검증 기준
- `manual-rerun-script-draft-prepare-implemented`

### 완료 조건
- `prepare-seed.sh`가 현재 문서 기준 입력 인자와 출력 파일을 사용해 실행된다.
- 시작 전 점검, 정리 SQL 실행, 준비 데이터 적용 SQL 실행 흐름이 파일 안에서 구분된다.

### 검증
- 관련 대상 테스트 실행 통과
- 저장소 표준 전체 테스트 명령 통과
- 스크립트 경계와 입력/출력 리뷰 통과
- 임시 디렉토리 기준 준비 로그 파일 생성 확인
- 임시 H2 메모리 또는 임시 H2 파일 DB 기준 정리 SQL과 준비 데이터 적용 SQL 실행 확인
- 실패 입력에서 관측 가능한 non-zero 종료 코드 확인

### GitHub 이슈
- 새 이슈 생성
- 권장 제목: `[BE] 준비와 정리 초안 파일 구현`

## TASK-0003
### 이름
rerun, retry, 실행 근거 수집 초안 파일 구현

### 목표
- rerun, retry, 실행 근거 수집 흐름을 `run-rerun.sh`, `run-retry.sh`, `collect-evidence.sh` 초안 파일로 옮긴다.

### 구현 범위
- 아래 기준을 실제 파일에 반영한다.
  - rerun/retry 요청과 응답 파일 저장
  - retry 파생 실행 키 추출과 후속 query 실행
  - 앱 종료 확인과 H2 조회 결과 저장
  - 파일별 종료 흐름과 수동 확인 인계 지점 반영

### 관련 파일 후보
- `docs/manual-rerun-response-seed-command-guide.md`
- `scripts/manual-rerun-response/run-rerun.sh`
- `scripts/manual-rerun-response/run-retry.sh`
- `scripts/manual-rerun-response/collect-evidence.sh`
- `.agents/outer-loop/retrospectives/SPEC-0029/TASK-0003-script-stop-flow-and-handoff.md`
- 위 회고 문서는 종료 흐름과 인계 지점을 실제 구현 전에 먼저 문서로 닫은 근거 문서다.

### 비대상
- 실제 앱/H2 대표 검증 재실행
- 새 대표 검증 시나리오 추가
- 단계 요약 문서 마감 판단

### 연결 검증 기준
- `manual-rerun-script-draft-run-flow-implemented`

### 완료 조건
- `run-rerun.sh`, `run-retry.sh`, `collect-evidence.sh`가 현재 문서 기준 명령 흐름과 출력 파일을 만든다.
- 종료 흐름과 수동 확인 인계 지점이 실제 파일 경계와 맞는다.

### 검증
- 관련 대상 테스트 실행 통과
- 저장소 표준 전체 테스트 명령 통과
- 스크립트 경계와 가독성 리뷰 통과
- 임시 디렉토리 기준 rerun, retry, 실행 근거 출력 파일 생성 확인
- 임시 H2 메모리 또는 임시 H2 파일 DB 기준 조회 결과 저장 확인
- 실패 입력에서 non-zero 종료 코드와 후속 파일 미생성 확인

### GitHub 이슈
- 새 이슈 생성
- 권장 제목: `[BE] rerun, retry, 실행 근거 수집 초안 파일 구현`

## TASK-0004
### 이름
초안 파일 대표 검증과 단계 마감

### 목표
- 실제 초안 파일로 대표 재실행 검증과 대표 재시도 검증을 수행하고 단계 마감 근거를 남긴다.

### 구현 범위
- 대표 검증 보조 명령 가이드와 실제 초안 파일을 함께 써 대표 재실행 검증과 대표 재시도 검증을 수행한다.
- 대표 검증에는 이전 검증과 겹치지 않는 새 전달 식별자를 사용하고, 필요하면 전달 식별자도 함께 회고에 남긴다.
- 응답 파일, 파생 실행 키, H2 실행 근거를 같은 실행 키 기준으로 확인한다.
- 앱 종료 뒤 H2 명령줄 도구 또는 SQL 조회 도구로 실행 근거를 다시 확인한다.
- 대상 테스트와 저장소 표준 전체 테스트 명령을 다시 돌려 초안 파일 추가로 기존 자동 검증 흐름이 깨지지 않았는지 확인한다.
- 필요하면 가이드와 회고를 최소 보정한다.

### 관련 파일 후보
- `docs/manual-rerun-response-seed-command-guide.md`
- `scripts/manual-rerun-response/`
- `.agents/prd.md`
- `.agents/outer-loop/retrospectives/SPEC-0029/`

### 비대상
- 새 대표 검증 시나리오 추가
- 운영자용 API 계약 변경
- 장기 저장소 도입

### 연결 검증 기준
- `manual-rerun-script-draft-representative-verified`

### 완료 조건
- 대표 재실행 검증과 대표 재시도 검증이 초안 파일로 다시 수행된다.
- 새 전달 식별자와 실행 키가 함께 기록된다.
- 앱 종료 뒤 H2 조회까지 포함해 출력 파일, 응답, H2 실행 근거, 문서 기준이 같은 실행 키 기준으로 맞는다.
- 단계 요약 문서까지 작성할 수 있는 마감 근거가 남는다.

### 검증
- 대상 테스트
- 저장소 표준 전체 테스트 명령
- 실제 앱/H2 대표 검증
- 마감 리뷰

### GitHub 이슈
- 새 이슈 생성
- 권장 제목: `[BE] 초안 파일 대표 검증과 단계 마감`
