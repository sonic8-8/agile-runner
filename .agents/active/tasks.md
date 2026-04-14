# 현재 활성 작업 집합

## 문서 목적
이 문서는 현재 활성 범위를 실제 구현 단위로 분해한 작업 문서다.
각 작업은 하나의 명확한 결과, 검증 기준, GitHub 이슈 연결 규칙을 가져야 한다.

## 현재 활성 단계
- ID: `SPEC-0032`
- 이름: `운영용 조회 응답 반복 검증 스크립트 유지 기준 정리`
- 기준 문서:
  - `.agents/active/spec.md`
  - `.agents/criteria/SPEC-0032-manual-rerun-script-maintenance.json`

## 공통 규칙
- 구현 순서는 `TASK-0001 -> TASK-0002 -> TASK-0003 -> TASK-0004`로 고정한다.
- 각 작업 시작 전 해당 작업용 GitHub 이슈를 새로 연결한다.
- 각 작업은 연결된 검증 기준과 테스트 근거가 없으면 완료로 보지 않는다.
- 기존 재실행, 재시도, 단건 조회, 목록 조회, 이력 조회, 관리자 조치 계약은 유지한다.
- 이번 단계는 스크립트와 가이드, 기준 파일, 테스트의 유지 기준 정리에 집중한다.
- `TASK-0001` 시작 전 `SPEC-0031` 단계 요약 문서와 현재 보조 명령 가이드, 자동 검증 테스트가 유지 기준 정리 시작 안전망으로 충분한지 먼저 확인한다.

## 요약 표
| 작업 | 이름 | 핵심 목표 | 연결 검증 기준 | 핵심 검증 | 이슈 |
| --- | --- | --- | --- | --- | --- |
| `TASK-0001` | 유지 기준 시작 전 기존 근거와 안전망 확인 | 유지 기준 정리를 시작해도 되는 기존 판단과 자동 검증 유지 확인 | `manual-rerun-script-maintenance-safety-net-preserved` | 회귀 테스트 + 직전 단계 요약과 가이드 근거 확인 | 새 이슈 |
| `TASK-0002` | 변경 시 함께 갱신해야 하는 문서와 기준 파일 정리 | 스크립트 변경 시 같이 봐야 하는 문서와 기준 파일 범위 고정 | `manual-rerun-script-maintenance-sync-boundary-documented` | 대상 테스트 + 전체 테스트 + 갱신 범위 재구성 근거 확인 | 새 이슈 |
| `TASK-0003` | 출력 파일 이름 변경과 문서 어긋남 감지 기준 정리 | 출력 파일 변경 시 어디서 먼저 어긋남을 잡아야 하는지 정리 | `manual-rerun-script-maintenance-drift-detection-documented` | 대상 테스트 + 전체 테스트 + 감지 순서 재구성 근거 확인 | 새 이슈 |
| `TASK-0004` | 유지 보수 체크리스트와 단계 마감 | 이후 변경 시 따라야 하는 점검 순서와 마감 기준 정리 | `manual-rerun-script-maintenance-checklist-closeout-documented` | 대상 테스트 + 전체 테스트 + 문서 마감 리뷰 | 새 이슈 |

## TASK-0001
### 이름
유지 기준 시작 전 기존 근거와 안전망 확인

### 목표
- 반복 검증 스크립트 유지 기준 정리 단계를 시작하기 전에 `SPEC-0031` 마감 근거와 현재 가이드, 자동 검증이 시작 안전망으로 충분한지 확인한다.

### 구현 범위
- 기존 단계 요약, 마지막 회고, 보조 명령 가이드, 관련 테스트를 우선 재사용한다.
- 아래 기준을 먼저 확인한다.
  - 스크립트 유지 기준을 정리할 수 있는 기존 근거 확보
  - 실제 유지 기준 정리 단계로 넘어가도 되는 판단 근거 정리
  - 자동 검증 테스트 유지
  - `SPEC-0031` 단계 요약 문서 연결
- 기존 안전망이 충분하면 근거를 회고에 남기고, 부족한 경우만 최소 문서 보강을 검토한다.

### 비대상
- 새 스크립트 구현
- 실제 앱/H2 대표 검증 재실행

### 연결 검증 기준
- `manual-rerun-script-maintenance-safety-net-preserved`

### 완료 조건
- 스크립트 유지 기준 정리 단계의 시작 근거가 충분하다는 회고와 테스트 근거가 남는다.

### 검증
- 관련 대상 테스트 실행 통과
- 저장소 표준 전체 테스트 명령 통과
- `SPEC-0031` 단계 요약 문서와 대표 검증 회고 경로 명시

### GitHub 이슈
- 새 이슈 생성
- 권장 제목: `[BE] 유지 기준 시작 전 기존 근거와 안전망 확인`

## TASK-0002
### 이름
변경 시 함께 갱신해야 하는 문서와 기준 파일 정리

### 목표
- 스크립트나 가이드를 바꿀 때 어떤 문서, 기준 파일, 테스트를 같이 봐야 하는지 한 번에 찾게 정리한다.

### 구현 범위
- 아래 기준을 문서에 반영한다.
  - 스크립트 변경 시 같이 갱신해야 하는 문서 목록 정리
  - 기준 파일과 회고, 단계 요약, 가이드 문서의 역할 경계 정리
  - 자동 검증 테스트가 어떤 어긋남을 먼저 잡는지 정리
  - 유지 기준 문서와 기존 보조 명령 가이드 책임 경계 유지

### 관련 파일 후보
- `docs/manual-rerun-response-seed-command-guide.md`
- `docs/manual-rerun-response-guide.md`
- `.agents/outer-loop/retrospectives/SPEC-0031/SPEC-0031-summary.md`
- `.agents/outer-loop/retrospectives/SPEC-0031/TASK-0004-script-application-representative-verified.md`
- `src/test/java/com/agilerunner/client/agentruntime/`

### 비대상
- 새 스크립트 구현
- 실제 앱/H2 대표 검증 재실행
- 출력 파일 이름 변경 자체

### 연결 검증 기준
- `manual-rerun-script-maintenance-sync-boundary-documented`

### 완료 조건
- 운영자가 스크립트나 가이드 변경 시 같이 갱신해야 하는 문서와 기준 파일을 문서만으로 다시 찾을 수 있다.
- 문서만 읽고 갱신 범위를 재구성한 리뷰 근거가 회고에 남는다.

### 검증
- 관련 대상 테스트 실행 통과
- 저장소 표준 전체 테스트 명령 통과
- 문서와 기준 파일 경계 리뷰 통과

### GitHub 이슈
- 새 이슈 생성
- 권장 제목: `[BE] 변경 시 함께 갱신할 문서와 기준 파일 정리`

## TASK-0003
### 이름
출력 파일 이름 변경과 문서 어긋남 감지 기준 정리

### 목표
- 출력 파일 이름이나 경로가 바뀔 때 어떤 문서와 테스트에서 먼저 어긋남을 확인해야 하는지 정리한다.

### 구현 범위
- 아래 기준을 문서에 반영한다.
  - 출력 파일 이름 변경 시 가장 먼저 점검할 문서와 테스트 정리
  - 문서 어긋남을 빨리 발견하는 체크 포인트 정리
  - 출력 파일 이름, 기준 파일, 회고, 단계 요약 사이의 연결 고리 정리
  - 자동 검증 테스트와 수동 점검의 경계 유지

### 관련 파일 후보
- `docs/manual-rerun-response-seed-command-guide.md`
- `docs/manual-rerun-response-guide.md`
- `src/test/java/com/agilerunner/client/agentruntime/ManualRerunRunFlowScriptTest.java`
- `src/test/java/com/agilerunner/client/agentruntime/ManualRerunSeedCommandScriptTest.java`
- `.agents/outer-loop/retrospectives/SPEC-0031/`

### 비대상
- 출력 파일 이름 실제 변경
- 새 스크립트 구현
- 실제 앱/H2 대표 검증 재실행

### 연결 검증 기준
- `manual-rerun-script-maintenance-drift-detection-documented`

### 완료 조건
- 운영자가 출력 파일 이름이나 경로가 바뀔 때 어떤 문서와 테스트에서 먼저 어긋남을 찾아야 하는지 문서만으로 다시 따라갈 수 있다.
- 문서만 읽고 감지 순서를 재구성한 리뷰 근거가 회고에 남는다.

### 검증
- 관련 대상 테스트 실행 통과
- 저장소 표준 전체 테스트 명령 통과
- 감지 기준 문서 리뷰 통과

### GitHub 이슈
- 새 이슈 생성
- 권장 제목: `[BE] 출력 파일 이름 변경과 어긋남 감지 기준 정리`

## TASK-0004
### 이름
유지 보수 체크리스트와 단계 마감

### 목표
- 이후 변경 시 따라야 하는 점검 순서와 마감 판단 기준을 체크리스트로 정리하고 단계를 닫는다.

### 구현 범위
- 아래 기준을 문서에 반영한다.
  - 유지 보수 체크리스트 초안 작성
  - 체크리스트에서 문서, 기준 파일, 테스트, 회고를 어떤 순서로 확인하는지 정리
  - 이후 변경 시 마감 판단 기준 정리
  - 단계 요약 문서까지 작성해 현재 단계 마감 근거 정리

### 관련 파일 후보
- `docs/manual-rerun-response-seed-command-guide.md`
- `.agents/outer-loop/retrospectives/SPEC-0032/`
- `.agents/prd.md`
- `.agents/outer-loop/README.md`

### 비대상
- 새 대표 검증 시나리오 추가
- 운영자용 API 계약 변경
- 장기 저장소 도입

### 연결 검증 기준
- `manual-rerun-script-maintenance-checklist-closeout-documented`

### 완료 조건
- 유지 보수 체크리스트와 단계 마감 근거가 문서로 남는다.
- 이후 변경의 점검 순서를 문서만으로 다시 구성할 수 있다.
- 단계 요약 문서까지 작성할 수 있는 마감 근거가 남는다.

### 검증
- 대상 테스트
- 저장소 표준 전체 테스트 명령
- 문서 마감 리뷰

### GitHub 이슈
- 새 이슈 생성
- 권장 제목: `[BE] 유지 보수 체크리스트와 단계 마감`
