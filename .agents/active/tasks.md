# 현재 활성 작업 집합

## 문서 목적
이 문서는 현재 활성 범위를 실제 구현 단위로 분해한 작업 문서다.
각 작업은 하나의 명확한 결과, 검증 기준, GitHub 이슈 연결 규칙을 가져야 한다.

## 현재 활성 단계
- ID: `SPEC-0026`
- 이름: `운영용 조회 응답 준비 데이터 적용 보조 명령 정리`
- 기준 문서:
  - `.agents/active/spec.md`
  - `.agents/criteria/SPEC-0026-manual-rerun-seed-helper-command.json`

## 공통 규칙
- 구현 순서는 `TASK-0001 -> TASK-0002 -> TASK-0003 -> TASK-0004`로 고정한다.
- 각 작업 시작 전 해당 작업용 GitHub 이슈를 새로 연결한다.
- 각 작업은 연결된 검증 기준과 테스트 근거가 없으면 완료로 보지 않는다.
- 기존 `POST /reviews/rerun`, `POST /reviews/rerun/{executionKey}/retry`, `GET /reviews/rerun/{executionKey}`, `GET /reviews/rerun/executions`, `GET /reviews/rerun/{executionKey}/actions/history`, `POST /reviews/rerun/{executionKey}/actions` 계약은 유지한다.
- 이번 단계는 대표 검증을 더 쉽게 반복하기 위한 보조 명령 정리에 집중한다.
- `TASK-0001` 시작 전 `SPEC-0025` 대표 검증 회고와 현재 준비 데이터 가이드 문서가 보조 명령 정리 단계의 안전망으로 충분한지 먼저 확인한다.

## 요약 표
| 작업 | 이름 | 핵심 목표 | 연결 검증 기준 | 핵심 검증 | 이슈 |
| --- | --- | --- | --- | --- | --- |
| `TASK-0001` | 보조 명령 시작 전 기존 근거 확인 | 보조 명령 정리 단계의 기존 근거와 자동 검증 유지 확인 | `manual-rerun-seed-helper-safety-net-preserved` | 회귀 테스트 + 기존 대표 검증 회고 근거 확인 | 새 이슈 |
| `TASK-0002` | 준비 데이터 적용과 정리 보조 명령 정리 | 대표 검증 전후 준비 데이터 적용과 정리 명령 예시 문서화 | `manual-rerun-seed-apply-command-documented` | 문서 재구성 근거 + 관련 테스트 | 새 이슈 |
| `TASK-0003` | 실행 키 추출과 H2 조회 보조 명령 정리 | retry 파생 실행 키 추출과 H2 실행 근거 확인 명령 표준화 | `manual-rerun-seed-query-command-documented` | 문서 재구성 근거 + 관련 테스트 | 새 이슈 |
| `TASK-0004` | 보조 명령 기반 대표 검증과 문서 마감 | 문서화한 명령만으로 대표 검증 절차를 다시 수행하고 현재 단계 마감 | `manual-rerun-seed-helper-representative-verified` | 대상 테스트 + 전체 테스트 + 실제 앱/H2 검증 | 새 이슈 |

## TASK-0001
### 이름
보조 명령 시작 전 기존 근거 확인

### 목표
- 대표 검증 보조 명령 정리 단계를 시작하기 전에 `SPEC-0025` 대표 검증 근거와 현재 가이드가 시작 안전망으로 충분한지 확인한다.

### 구현 범위
- 기존 회고와 관련 controller/service 테스트를 우선 재사용한다.
- 아래 기준을 먼저 확인한다.
  - 대표 검증에서 실제로 반복 비용이 컸던 단계 정리
  - 추가 도구 `jq` 의존처럼 환경별로 흔들린 명령 사례 정리
  - 자동 검증 테스트 유지
  - rerun/retry 대표 검증 근거 유지
- 기존 안전망이 충분하면 근거를 회고에 남기고, 부족한 경우만 최소 문서 보강을 검토한다.

### 비대상
- 실제 보조 명령 문서 추가
- 실제 앱/H2 대표 검증 재실행

### 연결 검증 기준
- `manual-rerun-seed-helper-safety-net-preserved`

### 완료 조건
- 보조 명령 정리 단계의 시작 근거가 충분하다는 회고와 테스트 근거가 남는다.

### 검증
- 관련 대상 테스트 실행 통과
- 저장소 표준 전체 테스트 명령 통과
- `SPEC-0025 / TASK-0004` 회고 경로 명시

### GitHub 이슈
- 새 이슈 생성
- 권장 제목: `[BE] 보조 명령 정리 시작 전 기존 근거 확인`

## TASK-0002
### 이름
준비 데이터 적용과 정리 보조 명령 정리

### 목표
- 대표 검증 전후에 반복적으로 쓰는 준비 데이터 적용, 준비 데이터 정리, 앱 기동 전 준비 명령을 문서로 정리한다.

### 구현 범위
- 아래 기준을 문서에 반영한다.
  - rerun 준비 실행 명령 예시
  - retry 원본 실행 준비 명령 예시
  - 준비 데이터 정리 또는 기존 대표 검증 결과 행 정리 명령 예시
  - 앱 기동 전 단계와 앱 종료 후 단계의 명령 경계
- 추가 도구 설치를 전제하지 않는 기본 명령을 우선 검토한다.

### 관련 파일 후보
- `docs/manual-rerun-response-seed-command-guide.md`
- `src/test/resources/manual-rerun-response-seed/`

### 비대상
- retry 파생 실행 키 추출 명령
- H2 실행 근거 조회 명령
- 실제 앱/H2 대표 검증 재실행

### 연결 검증 기준
- `manual-rerun-seed-apply-command-documented`

### 완료 조건
- 준비 데이터 적용과 정리 절차를 새 작업자가 문서만 읽고 따라갈 수 있는 기본 명령 예시가 남는다.
- 문서만 읽고 적용 절차를 재구성했을 때 빠진 단계가 없는지 확인한 리뷰 근거가 남는다.

### 검증
- 관련 대상 테스트 실행 통과
- 문서 경계와 명령 시점 리뷰 통과
- 문서만 읽고 준비 데이터 적용과 정리 순서를 재구성한 리뷰 근거

### GitHub 이슈
- 새 이슈 생성
- 권장 제목: `[BE] 준비 데이터 적용과 정리 보조 명령 정리`

## TASK-0003
### 이름
실행 키 추출과 H2 조회 보조 명령 정리

### 목표
- retry 응답에서 파생 실행 키를 읽는 방법과 대표 검증 뒤 H2 실행 근거를 확인하는 명령을 문서화한다.

### 구현 범위
- 아래 기준을 문서에 반영한다.
  - 추가 도구 `jq` 없이도 쓸 수 있는 실행 키 추출 예시
  - rerun 준비 실행 H2 조회 명령 예시
  - retry 파생 실행 H2 조회 명령 예시
  - 어떤 응답에서 받은 실행 키를 어디에 다시 쓰는지 연결 설명
  - H2 lock과 순차 조회 규칙을 어기지 않는 명령 흐름

### 관련 파일 후보
- `docs/manual-rerun-response-seed-guide.md`
- `docs/manual-rerun-response-seed-command-guide.md`
- `.agents/outer-loop/retrospectives/SPEC-0025/TASK-0004-seed-representative-application-verified.md`

### 비대상
- 새 준비 데이터 SQL 파일 추가
- 실제 앱/H2 대표 검증 재실행

### 연결 검증 기준
- `manual-rerun-seed-query-command-documented`

### 완료 조건
- retry 파생 실행 키 추출과 H2 실행 근거 확인 절차를 추가 도구 설치 없이 문서로 따라갈 수 있다.
- 문서만 읽고 실행 키 추출과 H2 조회 순서를 재구성했을 때 빠진 단계가 없는지 확인한 리뷰 근거가 남는다.

### 검증
- 관련 대상 테스트 실행 통과
- 문서 경계와 가독성 리뷰 통과
- 문서만 읽고 실행 키 추출과 H2 조회 순서를 재구성한 리뷰 근거

### GitHub 이슈
- 새 이슈 생성
- 권장 제목: `[BE] 실행 키 추출과 H2 조회 보조 명령 정리`

## TASK-0004
### 이름
보조 명령 기반 대표 검증과 문서 마감

### 목표
- 문서화한 보조 명령만으로 대표 rerun/retry 검증을 다시 수행하고, 현재 단계를 마감한다.

### 구현 범위
- 대표 rerun 준비 실행 1건과 retry 원본 실행 1건을 문서화한 보조 명령 기준으로 다시 검증한다.
- 앱 기동, 대표 요청, 앱 종료, H2 실행 근거 조회를 규칙대로 수행한다.
- 대상 테스트와 저장소 표준 전체 테스트 명령을 다시 돌려 문서 정리로 기존 자동 검증 흐름이 깨지지 않았는지 확인한다.
- 필요하면 보조 명령 문서를 최소 보정한다.

### 관련 파일 후보
- `docs/manual-rerun-response-seed-guide.md`
- `docs/manual-rerun-response-seed-command-guide.md`
- `src/test/resources/manual-rerun-response-seed/`

### 비대상
- 새로운 자동화 스크립트 추가
- 준비 데이터 SQL 구조 변경

### 연결 검증 기준
- `manual-rerun-seed-helper-representative-verified`

### 완료 조건
- 보조 명령 문서만으로 대표 검증을 다시 수행할 수 있고, rerun/retry 응답과 H2 실행 근거가 같은 실행 키 기준으로 연결된다.
- 대상 테스트와 저장소 표준 전체 테스트 명령이 유지된다.

### 검증
- 대상 테스트
- 저장소 표준 전체 테스트 명령
- 실제 앱/H2 대표 검증

### GitHub 이슈
- 새 이슈 생성
- 권장 제목: `[BE] 보조 명령 기반 대표 검증과 문서 마감`
