# 현재 활성 작업 집합

## 문서 목적
이 문서는 현재 활성 범위를 실제 구현 단위로 분해한 작업 문서다.
각 작업은 하나의 명확한 결과, 검증 기준, GitHub 이슈 연결 규칙을 가져야 한다.

## 현재 활성 단계
- ID: `SPEC-0036`
- 이름: `운영용 조회 응답 반복 검증 스크립트 실패 사례 빠른 참조 적용 예시 정리`
- 기준 문서:
  - `.agents/active/spec.md`
  - `.agents/criteria/SPEC-0036-manual-rerun-script-application-example.json`

## 공통 규칙
- 구현 순서는 `TASK-0001 -> TASK-0002 -> TASK-0003 -> TASK-0004`로 고정한다.
- 각 작업 시작 전 해당 작업용 GitHub 이슈를 새로 연결한다.
- 각 작업은 연결된 검증 기준과 테스트 근거가 없으면 완료로 보지 않는다.
- 기존 재실행, 재시도, 단건 조회, 목록 조회, 이력 조회, 관리자 조치 계약은 유지한다.
- 이번 단계는 빠른 참조 카드 자체를 늘리지 않고, 실제 실패 상황에서 어느 카드부터 펼칠지와 다음에 어느 상세 예시 표로 내려갈지 예시로 정리하는 데 집중한다.
- `TASK-0001` 시작 전 `SPEC-0035` 단계 요약 문서와 현재 빠른 참조 문서, 자동 검증 테스트를 다시 읽고 돌려 이번 단계 같은 기준을 다시 잡는다.

## 요약 표
| 작업 | 이름 | 핵심 목표 | 연결 검증 기준 | 핵심 검증 | 이슈 |
| --- | --- | --- | --- | --- | --- |
| `TASK-0001` | 적용 예시 정리 전에 빠른 참조 문서와 테스트 기준 확인 | 직전 단계 빠른 참조 문서와 테스트만 다시 읽고 돌려 이번 단계 같은 기준 다시 잡기 | `manual-rerun-script-application-example-safety-net-preserved` | 회귀 테스트 + 직전 단계 요약과 빠른 참조 문서 재확인 | 새 이슈 |
| `TASK-0002` | 종료 코드 빠른 참조 적용 예시 정리 | 종료 코드 실패 상황에서 어느 표를 먼저 보고 어디로 내려갈지 예시 정리 | `manual-rerun-script-stop-code-application-example-documented` | 대상 테스트 + 전체 테스트 + 예시만 보고 먼저 펼칠 표와 다음 상세 예시 표를 고를 수 있는지 확인 | 새 이슈 |
| `TASK-0003` | 출력 파일 누락과 H2 조회 실패 적용 예시 정리 | 출력 파일 누락과 H2 조회 실패를 실제 상황에 대입하는 예시 정리 | `manual-rerun-script-output-missing-and-h2-application-example-documented` | 대상 테스트 + 전체 테스트 + 예시만 보고 먼저 펼칠 카드와 다음 점검 대상을 고를 수 있는지 확인 | 새 이슈 |
| `TASK-0004` | 적용 예시 마감과 실제 사용 순서 연결 정리 | 적용 예시를 본 뒤 실제 사용 순서와 마지막 확인 질문 정리 | `manual-rerun-script-application-example-closeout-documented` | 대상 테스트 실행 통과 + 저장소 표준 전체 테스트 명령 통과 + 문서 마감 리뷰 통과 | 새 이슈 |

## TASK-0001
### 이름
적용 예시 정리 전에 빠른 참조 문서와 테스트 기준 확인

### 목표
- 반복 검증 스크립트 실패 사례 빠른 참조 적용 예시 단계를 시작하기 전에 `SPEC-0035` 단계 요약, 현재 빠른 참조 문서, 자동 검증 테스트를 다시 읽고 돌려 이번 단계 같은 기준을 다시 잡는다.

### 구현 범위
- 기존 단계 요약, 마지막 회고, 빠른 참조 문서, 관련 테스트를 우선 재사용한다.
- 아래 기준을 먼저 확인한다.
  - `SPEC-0035` 단계 요약과 마지막 회고를 다시 읽고 이번 단계에 그대로 이어 쓸 수 있는지 확인
  - 현재 빠른 참조 문서를 다시 읽고 실제 사용 예시를 붙일 자리가 분명한지 확인
  - 자동 검증 테스트를 다시 돌려 그대로 유지되는지 확인
  - 다음 작업에서 바로 참조할 문서 경로를 회고에 남김
- 직전 단계 문서와 테스트만 다시 본다는 근거를 회고에 남기고, 부족한 경우만 최소 문서 보강을 검토한다.

### 비대상
- 새 스크립트 구현
- 실제 앱/H2 대표 검증 재실행

### 연결 검증 기준
- `manual-rerun-script-application-example-safety-net-preserved`

### 완료 조건
- 이전 단계 빠른 참조 문서와 테스트만 다시 읽고 돌렸다는 회고와 테스트 근거가 남는다.

### 검증
- 관련 대상 테스트 실행 통과
- 저장소 표준 전체 테스트 명령 통과
- `SPEC-0035` 단계 요약 문서와 빠른 참조 문서 경로 명시

### GitHub 이슈
- 새 이슈 생성
- 권장 제목: `[BE] 적용 예시 정리 전에 빠른 참조 문서와 테스트 기준 확인`

## TASK-0002
### 이름
종료 코드 빠른 참조 적용 예시 정리

### 목표
- 스크립트 종료 코드 10부터 33까지를 만났을 때 운영자가 어느 빠른 참조 표를 먼저 보고, 다음에 어느 상세 예시 표로 내려가야 하는지 적용 예시로 정리한다.

### 구현 범위
- 아래 기준을 문서에 반영한다.
  - 종료 코드 실패 상황별 첫 진입 예시 정리
  - 어떤 빠른 참조 표를 먼저 펼칠지 정리
  - 다음에 어느 상세 예시 표로 내려갈지 정리
  - 마지막 확인 질문은 `TASK-0004`로 남김

### 관련 파일 후보
- `docs/manual-rerun-response-seed-command-guide.md`
- `.agents/outer-loop/retrospectives/SPEC-0035/SPEC-0035-summary.md`
- `.agents/outer-loop/retrospectives/SPEC-0034/TASK-0002-stop-code-failure-examples.md`
- `src/test/java/com/agilerunner/client/agentruntime/`

### 비대상
- 새 스크립트 구현
- 실제 앱/H2 대표 검증 재실행
- 종료 코드나 출력 파일 이름 변경 자체

### 연결 검증 기준
- `manual-rerun-script-stop-code-application-example-documented`

### 완료 조건
- 운영자가 종료 코드 실패 상황을 긴 문서를 다 읽지 않고도 어떤 표를 먼저 펼쳐야 할지 고를 수 있다.
- 예시만 보고 다음에 열 상세 예시 표를 다시 고를 수 있다는 리뷰 근거가 회고에 남는다.

### 검증
- 관련 대상 테스트 실행 통과
- 저장소 표준 전체 테스트 명령 통과
- 적용 예시 표 리뷰 통과

### GitHub 이슈
- 새 이슈 생성
- 권장 제목: `[BE] 종료 코드 빠른 참조 적용 예시 정리`

## TASK-0003
### 이름
출력 파일 누락과 H2 조회 실패 적용 예시 정리

### 목표
- 대표 검증 중 출력 파일 누락과 H2 조회 실패를 만났을 때 운영자가 어느 카드를 먼저 펼치고 다음에 어디를 봐야 하는지 적용 예시로 정리한다.

### 구현 범위
- 아래 기준을 문서에 반영한다.
  - 출력 파일 누락 적용 예시 정리
  - H2 조회 실패 적용 예시 정리
  - 두 예시를 서로 다른 첫 카드 흐름으로 유지
  - 마지막 확인 질문은 `TASK-0004`로 남김

### 관련 파일 후보
- `docs/manual-rerun-response-seed-command-guide.md`
- `.agents/outer-loop/retrospectives/SPEC-0034/TASK-0003-output-missing-failure-examples.md`
- `.agents/outer-loop/retrospectives/SPEC-0034/TASK-0004-h2-lock-failure-examples-closeout.md`
- `.agents/outer-loop/retrospectives/SPEC-0035/TASK-0003-output-missing-and-h2-quick-reference.md`
- `src/test/java/com/agilerunner/client/agentruntime/ManualRerunRunFlowScriptTest.java`
- `src/test/java/com/agilerunner/client/agentruntime/ManualRerunSeedCommandScriptTest.java`

### 비대상
- 출력 파일 이름 실제 변경
- 새 스크립트 구현
- 실제 앱/H2 대표 검증 재실행

### 연결 검증 기준
- `manual-rerun-script-output-missing-and-h2-application-example-documented`

### 완료 조건
- 운영자가 출력 파일 누락과 H2 조회 실패를 실제 상황에 대입해 먼저 펼칠 카드를 다시 고를 수 있다.
- 예시만 보고 다음 점검 대상과 다음에 열 상세 예시 표를 다시 고를 수 있다는 리뷰 근거가 회고에 남는다.

### 검증
- 관련 대상 테스트 실행 통과
- 저장소 표준 전체 테스트 명령 통과
- 적용 예시 카드 리뷰 통과

### GitHub 이슈
- 새 이슈 생성
- 권장 제목: `[BE] 출력 파일 누락과 H2 조회 실패 적용 예시 정리`

## TASK-0004
### 이름
적용 예시 마감과 실제 사용 순서 연결 정리

### 목표
- 적용 예시를 본 뒤 실제 사용 순서와 마지막에 무엇을 다시 확인해야 하는지 문서로 정리한다.

### 구현 범위
- 아래 기준을 문서에 반영한다.
  - 적용 예시 뒤에 열 상세 예시 표와 보조 문서 연결 정리
  - 실제 사용 순서 기준 마지막 확인 질문 정리
  - 단계 요약 문서 실제 작성과 현재 단계 마감 기준 정리

### 관련 파일 후보
- `docs/manual-rerun-response-seed-command-guide.md`
- `.agents/outer-loop/retrospectives/SPEC-0036/`
- `.agents/prd.md`
- `.agents/outer-loop/README.md`

### 비대상
- 새 대표 검증 시나리오 추가
- 운영자용 API 계약 변경
- 실제 앱/H2 대표 검증 재실행
- 장기 저장소 도입

### 연결 검증 기준
- `manual-rerun-script-application-example-closeout-documented`

### 완료 조건
- 적용 예시를 본 뒤 다음에 열 상세 예시 표와 보조 문서, 마지막에 다시 확인할 질문이 문서로 남는다.
- 실제 실패 상황에서 빠른 참조와 상세 문서 사이를 이동하는 순서를 문서만으로 다시 구성할 수 있다.
- `SPEC-0036-summary.md`가 실제로 작성되고 경로가 남는다.

### 검증
- 관련 대상 테스트 실행 통과
- 저장소 표준 전체 테스트 명령 통과
- 문서 마감 리뷰 통과

### GitHub 이슈
- 새 이슈 생성
- 권장 제목: `[BE] 적용 예시 마감과 실제 사용 순서 연결 정리`
