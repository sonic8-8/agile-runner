# 현재 활성 작업 집합

## 문서 목적
이 문서는 현재 활성 범위를 실제 구현 단위로 분해한 작업 문서다.
각 작업은 하나의 명확한 결과, 검증 기준, GitHub 이슈 연결 규칙을 가져야 한다.

## 현재 활성 단계
- ID: `SPEC-0035`
- 이름: `운영용 조회 응답 반복 검증 스크립트 실패 사례 빠른 참조 정리`
- 기준 문서:
  - `.agents/active/spec.md`
  - `.agents/criteria/SPEC-0035-manual-rerun-script-quick-reference.json`

## 공통 규칙
- 구현 순서는 `TASK-0001 -> TASK-0002 -> TASK-0003 -> TASK-0004`로 고정한다.
- 각 작업 시작 전 해당 작업용 GitHub 이슈를 새로 연결한다.
- 각 작업은 연결된 검증 기준과 테스트 근거가 없으면 완료로 보지 않는다.
- 기존 재실행, 재시도, 단건 조회, 목록 조회, 이력 조회, 관리자 조치 계약은 유지한다.
- 이번 단계는 상세 실패 사례 문서를 대체하지 않고, 종료 코드, 출력 파일 누락, H2 조회 실패를 먼저 가르는 카드와 표를 만드는 데 집중한다.
- `TASK-0001` 시작 전 `SPEC-0034` 단계 요약 문서와 현재 실패 사례 예시 문서, 자동 검증 테스트가 이번 단계 확인 기준으로 충분한지 먼저 확인한다.

## 요약 표
| 작업 | 이름 | 핵심 목표 | 연결 검증 기준 | 핵심 검증 | 이슈 |
| --- | --- | --- | --- | --- | --- |
| `TASK-0001` | 빠른 참조 정리 전에 이전 실패 예시 문서와 테스트 기준 확인 | 직전 단계 문서와 테스트가 이번 단계 확인 기준으로 충분한지 확인 | `manual-rerun-script-quick-reference-safety-net-preserved` | 회귀 테스트 + 직전 단계 요약과 실패 사례 예시 문서 근거 확인 | 새 이슈 |
| `TASK-0002` | 종료 코드 실패 사례 빠른 참조 정리 | 종료 코드 10~33 실패 유형을 짧은 표로 먼저 좁히게 정리 | `manual-rerun-script-stop-code-quick-reference-documented` | 대상 테스트 + 전체 테스트 + 문서만 보고 먼저 볼 표를 다시 찾을 수 있는지 확인 | 새 이슈 |
| `TASK-0003` | 출력 파일 누락과 H2 조회 실패 빠른 참조 정리 | 출력 파일 누락 카드와 H2 조회 실패 카드를 따로 정리 | `manual-rerun-script-output-missing-and-h2-quick-reference-documented` | 대상 테스트 + 전체 테스트 + 문서만 보고 먼저 펼칠 카드를 다시 고를 수 있는지 확인 | 새 이슈 |
| `TASK-0004` | 빠른 참조 마감과 상세 문서 연결 정리 | 빠른 참조를 본 뒤 상세 문서로 내려가는 경로와 단계 마감 근거 정리 | `manual-rerun-script-quick-reference-closeout-documented` | 대상 테스트 실행 통과 + 저장소 표준 전체 테스트 명령 통과 + 문서 마감 리뷰 통과 | 새 이슈 |

## TASK-0001
### 이름
빠른 참조 정리 전에 이전 실패 예시 문서와 테스트 기준 확인

### 목표
- 반복 검증 스크립트 실패 사례 빠른 참조 정리 단계를 시작하기 전에 `SPEC-0034` 단계 요약, 현재 실패 사례 예시 문서, 자동 검증 테스트가 이번 단계 확인 기준으로 충분한지 확인한다.

### 구현 범위
- 기존 단계 요약, 마지막 회고, 실패 사례 예시 문서, 관련 테스트를 우선 재사용한다.
- 아래 기준을 먼저 확인한다.
  - `SPEC-0034` 단계 요약과 마지막 회고를 다시 읽으면 바로 다음 단계로 넘어갈 수 있는지 확인
  - 현재 실패 사례 예시 문서를 다시 읽으면 빠른 참조 카드로 줄일 수 있는지 확인
  - 자동 검증 테스트가 그대로 유지되는지 확인
  - 다음 작업에서 바로 참조할 문서 경로를 회고에 남김
- 직전 단계 문서와 테스트가 충분하면 근거를 회고에 남기고, 부족한 경우만 최소 문서 보강을 검토한다.

### 비대상
- 새 스크립트 구현
- 실제 앱/H2 대표 검증 재실행

### 연결 검증 기준
- `manual-rerun-script-quick-reference-safety-net-preserved`

### 완료 조건
- 이전 단계 문서와 테스트만 다시 확인했다는 회고와 테스트 근거가 남는다.

### 검증
- 관련 대상 테스트 실행 통과
- 저장소 표준 전체 테스트 명령 통과
- `SPEC-0034` 단계 요약 문서와 실패 사례 예시 문서 경로 명시

### GitHub 이슈
- 새 이슈 생성
- 권장 제목: `[BE] 빠른 참조 정리 전에 이전 실패 예시 문서와 테스트 기준 확인`

## TASK-0002
### 이름
종료 코드 실패 사례 빠른 참조 정리

### 목표
- 스크립트 종료 코드 10부터 33까지를 만났을 때 운영자가 먼저 어떤 실패 유형을 좁혀야 하는지 짧은 비교 표로 정리한다.

### 구현 범위
- 아래 기준을 문서에 반영한다.
  - 종료 코드별 빠른 참조 표 정리
  - 어떤 실패 유형인지 먼저 좁히는 짧은 문구 정리
  - 빠른 참조 표만 보고 먼저 볼 비교 항목을 결정할 수 있게 정리
  - 상세 문서 연결 규칙과 마지막 확인 질문은 `TASK-0004`로 남김

### 관련 파일 후보
- `docs/manual-rerun-response-seed-command-guide.md`
- `.agents/outer-loop/retrospectives/SPEC-0034/SPEC-0034-summary.md`
- `.agents/outer-loop/retrospectives/SPEC-0034/TASK-0002-stop-code-failure-examples.md`
- `src/test/java/com/agilerunner/client/agentruntime/`

### 비대상
- 새 스크립트 구현
- 실제 앱/H2 대표 검증 재실행
- 종료 코드나 출력 파일 이름 변경 자체

### 연결 검증 기준
- `manual-rerun-script-stop-code-quick-reference-documented`

### 완료 조건
- 운영자가 종료 코드 실패 유형을 긴 문서를 다 읽지 않고도 먼저 좁힐 수 있다.
- 문서만 읽고 빠른 참조 표를 다시 재구성한 리뷰 근거가 회고에 남는다.

### 검증
- 관련 대상 테스트 실행 통과
- 저장소 표준 전체 테스트 명령 통과
- 빠른 참조 표 리뷰 통과

### GitHub 이슈
- 새 이슈 생성
- 권장 제목: `[BE] 종료 코드 실패 사례 빠른 참조 정리`

## TASK-0003
### 이름
출력 파일 누락과 H2 조회 실패 빠른 참조 정리

### 목표
- 대표 검증 중 출력 파일 누락과 H2 조회 실패를 운영자가 먼저 어떤 질문으로 가를지 짧은 참조 카드로 정리한다.

### 구현 범위
- 아래 기준을 문서에 반영한다.
  - 출력 파일 누락 빠른 참조 카드 정리
  - H2 조회 실패 빠른 참조 카드 정리
  - 두 카드를 서로 다른 비교 축으로 유지
  - 상세 문서 연결 규칙과 마지막 확인 질문은 `TASK-0004`로 남김

### 관련 파일 후보
- `docs/manual-rerun-response-seed-command-guide.md`
- `.agents/outer-loop/retrospectives/SPEC-0034/TASK-0003-output-missing-failure-examples.md`
- `.agents/outer-loop/retrospectives/SPEC-0034/TASK-0004-h2-lock-failure-examples-closeout.md`
- `src/test/java/com/agilerunner/client/agentruntime/ManualRerunRunFlowScriptTest.java`
- `src/test/java/com/agilerunner/client/agentruntime/ManualRerunSeedCommandScriptTest.java`

### 비대상
- 출력 파일 이름 실제 변경
- 새 스크립트 구현
- 실제 앱/H2 대표 검증 재실행

### 연결 검증 기준
- `manual-rerun-script-output-missing-and-h2-quick-reference-documented`

### 완료 조건
- 운영자가 출력 파일 누락과 H2 조회 실패를 긴 문서를 다 읽기 전에 먼저 가를 수 있다.
- 문서만 읽고 빠른 참조 카드를 다시 재구성한 리뷰 근거가 회고에 남는다.

### 검증
- 관련 대상 테스트 실행 통과
- 저장소 표준 전체 테스트 명령 통과
- 빠른 참조 카드 리뷰 통과

### GitHub 이슈
- 새 이슈 생성
- 권장 제목: `[BE] 출력 파일 누락과 H2 조회 실패 빠른 참조 정리`

## TASK-0004
### 이름
빠른 참조 마감과 상세 문서 연결 정리

### 목표
- 빠른 참조를 본 뒤 상세 문서 어디로 내려가야 하는지 연결하고 단계를 닫는다.

### 구현 범위
- 아래 기준을 문서에 반영한다.
  - 빠른 참조와 상세 문서 연결 기준 정리
  - 빠른 참조 문서 마지막 확인 질문 정리
  - 단계 요약 문서 실제 작성과 현재 단계 마감 근거 정리

### 관련 파일 후보
- `docs/manual-rerun-response-seed-command-guide.md`
- `.agents/outer-loop/retrospectives/SPEC-0035/`
- `.agents/prd.md`
- `.agents/outer-loop/README.md`

### 비대상
- 새 대표 검증 시나리오 추가
- 운영자용 API 계약 변경
- 실제 앱/H2 대표 검증 재실행
- 장기 저장소 도입

### 연결 검증 기준
- `manual-rerun-script-quick-reference-closeout-documented`

### 완료 조건
- 빠른 참조 마감 근거와 상세 문서 연결 기준이 문서로 남는다.
- 실패 유형을 먼저 좁힌 뒤 상세 문서로 내려가는 순서를 문서만으로 다시 구성할 수 있다.
- `SPEC-0035-summary.md`가 실제로 작성되고 경로가 남는다.

### 검증
- 관련 대상 테스트 실행 통과
- 저장소 표준 전체 테스트 명령 통과
- 문서 마감 리뷰 통과

### GitHub 이슈
- 새 이슈 생성
- 권장 제목: `[BE] 빠른 참조 마감과 상세 문서 연결 정리`
