# 현재 활성 작업 집합

## 문서 목적
이 문서는 현재 활성 범위를 실제 구현 단위로 분해한 작업 문서다.
각 작업은 하나의 명확한 결과, 검증 기준, GitHub 이슈 연결 규칙을 가져야 한다.

## 현재 활성 단계
- ID: `SPEC-0029`
- 이름: `운영용 조회 응답 준비 데이터 반복 검증 스크립트 초안 구현 검토`
- 기준 문서:
  - `.agents/active/spec.md`
  - `.agents/criteria/SPEC-0029-manual-rerun-script-implementation-review.json`

## 공통 규칙
- 구현 순서는 `TASK-0001 -> TASK-0002 -> TASK-0003 -> TASK-0004`로 고정한다.
- 각 작업 시작 전 해당 작업용 GitHub 이슈를 새로 연결한다.
- 각 작업은 연결된 검증 기준과 테스트 근거가 없으면 완료로 보지 않는다.
- 기존 `POST /reviews/rerun`, `POST /reviews/rerun/{executionKey}/retry`, `GET /reviews/rerun/{executionKey}`, `GET /reviews/rerun/executions`, `GET /reviews/rerun/{executionKey}/actions/history`, `POST /reviews/rerun/{executionKey}/actions` 계약은 유지한다.
- 이번 단계는 대표 검증 스크립트 초안 구현 검토와 인계 지점 정리에 집중한다.
- `TASK-0001` 시작 전 `SPEC-0028` 단계 요약 문서와 현재 가이드 문서가 스크립트 초안 구현 검토 단계의 안전망으로 충분한지 먼저 확인한다.

## 요약 표
| 작업 | 이름 | 핵심 목표 | 연결 검증 기준 | 핵심 검증 | 이슈 |
| --- | --- | --- | --- | --- | --- |
| `TASK-0001` | 초안 구현 검토 시작 전 기존 판단과 근거 확인 | 초안 구현 검토 단계의 기존 판단과 자동 검증 유지 확인 | `manual-rerun-script-implementation-review-safety-net-preserved` | 회귀 테스트 + 기존 대표 검증 회고와 단계 요약 문서 근거 확인 | 새 이슈 |
| `TASK-0002` | 초안 파일 구조와 입력/출력 계약 정리 | 어떤 파일 구조와 어떤 입력/출력 계약으로 초안을 시작할지 정리 | `manual-rerun-script-implementation-draft-structure-documented` | 문서 재구성 근거 + 관련 테스트 | 새 이슈 |
| `TASK-0003` | 실패 종료 흐름과 수동 확인 인계 지점 정리 | 어디서 초안이 종료되고 무엇을 계속 사람이 닫아야 하는지 정리 | `manual-rerun-script-implementation-handoff-documented` | 문서 재구성 근거 + 관련 테스트 | 새 이슈 |
| `TASK-0004` | 초안 구현 검토 판단과 단계 마감 | 현재 단계에서 구현 검토 판단을 내리고 다음 단계 후보까지 닫기 | `manual-rerun-script-implementation-review-closeout-completed` | 대상 테스트 + 전체 테스트 + 마감 근거 | 새 이슈 |

## TASK-0001
### 이름
초안 구현 검토 시작 전 기존 판단과 근거 확인

### 목표
- 대표 검증 스크립트 초안 구현 검토 단계를 시작하기 전에 `SPEC-0028` 마감 근거와 현재 가이드가 시작 안전망으로 충분한지 확인한다.

### 구현 범위
- 기존 회고와 관련 controller/service 테스트를 우선 재사용한다.
- 아래 기준을 먼저 확인한다.
  - 대표 검증에서 실제 초안 파일 후보로 이어질 수 있는 명령 묶음 정리
  - 실제 구현을 보류하고 구현 검토 단계로 한 번 더 나눈 이유 정리
  - 자동 검증 테스트 유지
  - rerun/retry 대표 검증 근거 유지
  - SPEC-0028 단계 요약 문서 연결
- 기존 안전망이 충분하면 근거를 회고에 남기고, 부족한 경우만 최소 문서 보강을 검토한다.

### 비대상
- 실제 스크립트 파일 추가
- 실제 앱/H2 대표 검증 재실행

### 연결 검증 기준
- `manual-rerun-script-implementation-review-safety-net-preserved`

### 완료 조건
- 스크립트 초안 구현 검토 단계의 시작 근거가 충분하다는 회고와 테스트 근거가 남는다.

### 검증
- 관련 대상 테스트 실행 통과
- 저장소 표준 전체 테스트 명령 통과
- `SPEC-0028` 단계 요약 문서와 `SPEC-0026 / TASK-0004` 회고 경로 명시

### GitHub 이슈
- 새 이슈 생성
- 권장 제목: `[BE] 초안 구현 검토 시작 전 기존 판단과 근거 확인`

## TASK-0002
### 이름
초안 파일 구조와 입력/출력 계약 정리

### 목표
- 현재 대표 검증 절차에서 실제 초안 파일을 만든다면 어떤 위치, 어떤 입력 인자, 어떤 출력 파일 구조로 시작할지 정리한다.

### 구현 범위
- 아래 기준을 문서에 반영한다.
  - 초안 스크립트 파일 후보 위치와 역할
  - 입력 인자 묶음과 출력 파일 묶음
  - 기존 보조 명령 문서와 초안 파일 사이의 책임 경계
  - 초안 파일이 직접 실행할 명령 묶음 단위

### 관련 파일 후보
- `docs/manual-rerun-response-seed-command-guide.md`
- `docs/manual-rerun-response-seed-guide.md`
- `.agents/outer-loop/retrospectives/SPEC-0028/SPEC-0028-summary.md`
- 위 단계 요약 문서는 다음 단계가 초안 구현 검토인 이유를 정리한 마감 문서다.

### 비대상
- 실제 스크립트 구현
- 실제 앱/H2 대표 검증 재실행
- 새 보조 명령 추가

### 연결 검증 기준
- `manual-rerun-script-implementation-draft-structure-documented`

### 완료 조건
- 초안 파일 후보 위치, 입력 인자, 출력 파일, 명령 묶음 경계가 문서에서 구분된다.
- 문서만 읽고 초안 파일 구조와 입력/출력 계약을 재구성했을 때 빠진 항목이 없는지 확인한 리뷰 근거가 남는다.

### 검증
- 관련 대상 테스트 실행 통과
- 문서 경계와 입력/출력 리뷰 통과
- 문서만 읽고 초안 파일 구조와 입력/출력 계약을 재구성한 리뷰 근거

### GitHub 이슈
- 새 이슈 생성
- 권장 제목: `[BE] 초안 파일 구조와 입력 출력 계약 정리`

## TASK-0003
### 이름
실패 종료 흐름과 수동 확인 인계 지점 정리

### 목표
- 대표 검증 스크립트 초안이 어떤 실패에서 종료돼야 하고, 어떤 확인은 계속 사람이 이어받아야 하는지 정리한다.

### 구현 범위
- 아래 기준을 문서에 반영한다.
  - 잠금 오류, 명령 실패, 빈 실행 키, HTTP 실패에서 초안이 어떤 종료 상태를 가져야 하는지 검토
  - 응답 값 해석, H2 결과 의미 확인, 회고 작성처럼 계속 수동으로 남길 단계
  - 초안 출력 파일과 사람이 이어받는 인계 지점
  - 지금 단계에서는 하지 않을 비대상

### 관련 파일 후보
- `docs/manual-rerun-response-seed-command-guide.md`
- `.agents/outer-loop/retrospectives/SPEC-0028/TASK-0003-script-draft-stop-condition-documented.md`
- `.agents/prd.md`
- 위 회고 문서는 중단 조건과 수동 확인 유지 기준을 먼저 문서로 닫은 근거 문서다.

### 비대상
- 실제 스크립트 구현
- 새 명령 추가
- 실제 앱/H2 대표 검증 재실행

### 연결 검증 기준
- `manual-rerun-script-implementation-handoff-documented`

### 완료 조건
- 실패 종료 흐름과 수동 확인 인계 지점이 문서에서 분리되어 읽힌다.
- 문서만 읽고 초안 종료 지점과 수동 판단 지점을 재구성했을 때 빠진 항목이 없는지 확인한 리뷰 근거가 남는다.

### 검증
- 관련 대상 테스트 실행 통과
- 문서 경계와 가독성 리뷰 통과
- 문서만 읽고 종료 흐름과 수동 인계 지점을 재구성한 리뷰 근거

### GitHub 이슈
- 새 이슈 생성
- 권장 제목: `[BE] 초안 종료 흐름과 수동 확인 인계 지점 정리`

## TASK-0004
### 이름
초안 구현 검토 판단과 단계 마감

### 목표
- 현재 단계에서 대표 검증 스크립트 초안 구현 검토가 충분한지 판단하고, 다음 단계 방향을 마감한다.

### 구현 범위
- 현재 문서와 회고를 기준으로 스크립트 초안 구현 검토 판단을 정리한다.
- 초안 구현 검토로 충분한지, 실제 파일 추가 단계로 넘어가야 하는지 근거와 함께 남긴다.
- 대상 테스트와 저장소 표준 전체 테스트 명령을 다시 돌려 문서 정리로 기존 자동 검증 흐름이 깨지지 않았는지 확인한다.
- 필요하면 검토 문서를 최소 보정한다.

### 관련 파일 후보
- `docs/manual-rerun-response-seed-command-guide.md`
- `.agents/prd.md`
- `.agents/outer-loop/retrospectives/SPEC-0028/`

### 비대상
- 실제 스크립트 구현
- 준비 데이터 SQL 구조 변경
- 새 대표 검증 시나리오 추가

### 연결 검증 기준
- `manual-rerun-script-implementation-review-closeout-completed`

### 완료 조건
- 스크립트 초안 구현 검토 판단과 다음 단계 방향이 근거와 함께 남는다.
- 대상 테스트와 저장소 표준 전체 테스트 명령이 유지된다.
- 단계 요약 문서까지 작성할 수 있는 마감 근거가 남는다.

### 검증
- 대상 테스트
- 저장소 표준 전체 테스트 명령
- 마감 리뷰

### GitHub 이슈
- 새 이슈 생성
- 권장 제목: `[BE] 초안 구현 검토 판단과 단계 마감`
