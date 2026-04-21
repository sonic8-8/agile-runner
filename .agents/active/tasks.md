# 현재 활성 작업 집합

## 문서 목적
이 문서는 현재 활성 범위를 실제 구현 단위로 분해한 작업 문서다.
각 작업은 하나의 명확한 결과, 검증 기준, GitHub 이슈 연결 규칙을 가져야 한다.

## 현재 활성 단계
- ID: `SPEC-0037`
- 이름: `운영용 조회 응답 반복 검증 스크립트 실패 사례 빠른 참조 적용 유지 기준 정리`
- 기준 문서:
  - `.agents/active/spec.md`
  - `.agents/criteria/SPEC-0037-manual-rerun-script-application-maintenance.json`

## 공통 규칙
- 구현 순서는 `TASK-0001 -> TASK-0002 -> TASK-0003 -> TASK-0004`로 고정한다.
- 각 작업 시작 전 해당 작업용 GitHub 이슈를 새로 연결한다.
- 각 작업은 연결된 검증 기준과 테스트 근거가 없으면 완료로 보지 않는다.
- 기존 재실행, 재시도, 단건 조회, 목록 조회, 이력 조회, 관리자 조치 계약은 유지한다.
- 이번 단계는 새 실패 유형이나 새 스크립트를 만들지 않고, 적용 예시를 나중에 고칠 때 무엇을 먼저 보고 무엇을 같이 갱신할지 정리하는 데 집중한다.
- `TASK-0001` 시작 전 `SPEC-0036` 단계 요약 문서, 현재 적용 예시 문서, 자동 검증 테스트를 다시 읽고 돌려 이번 단계에서 먼저 볼 문서를 정한다.

## 요약 표
| 작업 | 이름 | 핵심 목표 | 연결 검증 기준 | 핵심 검증 | 이슈 |
| --- | --- | --- | --- | --- | --- |
| `TASK-0001` | 유지 기준 정리 전에 적용 예시 문서와 테스트 기준 확인 | 직전 단계 요약, 적용 예시 문서, 자동 검증 테스트를 다시 읽고 돌려 이번 단계에서 먼저 볼 문서를 확인하기 | `manual-rerun-script-application-maintenance-safety-net-preserved` | 회귀 테스트 + 직전 단계 요약과 적용 예시 문서 재확인 | 새 이슈 |
| `TASK-0002` | 적용 예시 수정 시 먼저 볼 문서와 같이 갱신할 파일 정리 | 적용 예시를 고칠 때 무엇을 먼저 보고 어떤 문서와 기준 파일을 같이 열어야 하는지 정리 | `manual-rerun-script-application-maintenance-update-boundary-documented` | 대상 테스트 + 전체 테스트 + 문서만 보고 먼저 볼 문서와 같이 갱신할 파일을 고를 수 있는지 확인 | 새 이슈 |
| `TASK-0003` | 적용 예시와 상세 예시 표 어긋남 점검 순서 정리 | 적용 예시, 상세 예시 표, 보조 문서가 어긋날 때 어디서부터 다시 볼지 정리 | `manual-rerun-script-application-maintenance-drift-order-documented` | 대상 테스트 + 전체 테스트 + 문서만 보고 점검 순서를 다시 고를 수 있는지 확인 | 새 이슈 |
| `TASK-0004` | 유지 보수 체크리스트와 단계 마감 정리 | 적용 예시 유지 작업을 닫을 때 마지막 확인 질문과 체크리스트 정리 | `manual-rerun-script-application-maintenance-closeout-documented` | 대상 테스트 실행 통과 + 저장소 표준 전체 테스트 명령 통과 + 문서 마감 리뷰 통과 | 새 이슈 |

## TASK-0001
### 이름
유지 기준 정리 전에 적용 예시 문서와 테스트 기준 확인

### 목표
- 반복 검증 스크립트 실패 사례 빠른 참조 적용 예시를 고칠 기준 정리를 시작하기 전에 `SPEC-0036` 단계 요약, 현재 적용 예시 문서, 자동 검증 테스트를 다시 읽고 돌려 이번 단계에서 먼저 볼 문서를 정한다.

### 구현 범위
- 기존 단계 요약, 마지막 회고, 적용 예시 문서, 관련 테스트를 우선 재사용한다.
- 아래 기준을 먼저 확인한다.
  - `SPEC-0036` 단계 요약과 마지막 회고를 다시 읽고 이번 단계에서도 먼저 참고할 문서인지 확인
  - 현재 적용 예시 문서를 다시 읽고 어느 위치에 유지 기준을 적을지 바로 보이는지 확인
  - 자동 검증 테스트를 다시 돌려 그대로 유지되는지 확인
  - 다음 작업에서 바로 열 문서 경로를 회고에 남김
- 직전 단계 문서와 테스트만 다시 본다는 근거를 회고에 남기고, 부족한 경우만 최소 문서 보강을 검토한다.

### 비대상
- 새 스크립트 구현
- 실제 앱/H2 대표 검증 재실행

### 연결 검증 기준
- `manual-rerun-script-application-maintenance-safety-net-preserved`

### 완료 조건
- 이전 단계 적용 예시 문서와 테스트만 다시 읽고 돌렸다는 회고와 테스트 근거가 남는다.

### 검증
- 관련 대상 테스트 실행 통과
- 저장소 표준 전체 테스트 명령 통과
- `SPEC-0036` 단계 요약 문서와 적용 예시 문서 경로 명시

### GitHub 이슈
- 새 이슈 생성
- 권장 제목: `[BE] 유지 기준 정리 전에 적용 예시 문서와 테스트 기준 확인`

## TASK-0002
### 이름
적용 예시 수정 시 먼저 볼 문서와 같이 갱신할 파일 정리

### 목표
- 적용 예시를 고칠 때 운영자가 무엇을 먼저 보고 어떤 문서와 기준 파일을 같이 갱신해야 하는지 정리한다.

### 구현 범위
- 아래 기준을 문서에 반영한다.
  - 적용 예시 수정 시 먼저 볼 문서 정리
  - 같이 갱신할 기준 파일과 참고 문서 정리
  - 자동 검증이 먼저 잡는 범위와 사람이 직접 판단할 범위 정리
  - 문서 수정 범위는 먼저 볼 문서와 같이 갱신할 파일 정리까지다.
  - 마지막 확인 질문과 체크리스트는 제외한다.

### 관련 파일 후보
- `docs/manual-rerun-response-seed-command-guide.md`
- `.agents/outer-loop/retrospectives/SPEC-0036/SPEC-0036-summary.md`
- `.agents/outer-loop/retrospectives/SPEC-0036/TASK-0004-application-example-closeout.md`
- `src/test/java/com/agilerunner/client/agentruntime/`

### 비대상
- 새 스크립트 구현
- 실제 앱/H2 대표 검증 재실행
- 종료 코드나 출력 파일 이름 변경 자체

### 연결 검증 기준
- `manual-rerun-script-application-maintenance-update-boundary-documented`

### 완료 조건
- 운영자가 적용 예시를 고칠 때 무엇을 먼저 보고 어떤 문서와 기준 파일을 같이 갱신해야 하는지 문서만 보고 다시 고를 수 있다.
- 자동 검증이 먼저 잡는 범위와 사람이 직접 판단해야 하는 범위가 문서에 남는다.

### 검증
- 관련 대상 테스트 실행 통과
- 저장소 표준 전체 테스트 명령 통과
- 먼저 볼 문서와 같이 고칠 파일 리뷰 통과

### GitHub 이슈
- 새 이슈 생성
- 권장 제목: `[BE] 적용 예시 수정 시 먼저 볼 문서와 같이 갱신할 파일 정리`

## TASK-0003
### 이름
적용 예시와 상세 예시 표 어긋남 점검 순서 정리

### 목표
- 적용 예시, 상세 예시 표, 보조 문서가 서로 어긋날 때 운영자가 어디서부터 다시 봐야 하는지 순서로 정리한다.

### 구현 범위
- 아래 기준을 문서에 반영한다.
  - 적용 예시와 상세 예시 표 어긋남 점검 순서 정리
  - 상세 예시 표와 보조 문서 어긋남 점검 순서 정리
  - 출력 파일 이름 어긋남과 문서 어긋남을 섞지 않는 기준 정리
  - 이번 작업 범위는 어긋남 점검 순서 정리까지다.
  - 마지막 확인 질문과 체크리스트는 제외한다.

### 관련 파일 후보
- `docs/manual-rerun-response-seed-command-guide.md`
- `.agents/outer-loop/retrospectives/SPEC-0036/SPEC-0036-summary.md`
- `.agents/outer-loop/retrospectives/SPEC-0036/TASK-0004-application-example-closeout.md`
- `src/test/java/com/agilerunner/client/agentruntime/ManualRerunRunFlowScriptTest.java`
- `src/test/java/com/agilerunner/client/agentruntime/ManualRerunSeedCommandScriptTest.java`

### 비대상
- 출력 파일 이름 실제 변경
- 새 스크립트 구현
- 실제 앱/H2 대표 검증 재실행

### 연결 검증 기준
- `manual-rerun-script-application-maintenance-drift-order-documented`

### 완료 조건
- 운영자가 적용 예시와 상세 예시 표가 어긋날 때 무엇부터 다시 볼지 문서만 보고 다시 고를 수 있다.
- 상세 예시 표와 보조 문서, 출력 파일 이름 어긋남 점검 순서가 문서에 남는다.

### 검증
- 관련 대상 테스트 실행 통과
- 저장소 표준 전체 테스트 명령 통과
- 어긋남 점검 순서 리뷰 통과

### GitHub 이슈
- 새 이슈 생성
- 권장 제목: `[BE] 적용 예시와 상세 예시 표 어긋남 점검 순서 정리`

## TASK-0004
### 이름
유지 보수 체크리스트와 단계 마감 정리

### 목표
- 적용 예시 유지 작업을 닫을 때 마지막에 무엇을 다시 확인해야 하는지와 체크리스트를 문서로 정리한다.

### 구현 범위
- 아래 기준을 문서에 반영한다.
  - 유지 보수 체크리스트 정리
  - 마지막 확인 질문 정리
  - 단계 요약 문서 실제 작성과 현재 단계 마감 기준 정리

### 관련 파일 후보
- `docs/manual-rerun-response-seed-command-guide.md`
- `.agents/outer-loop/retrospectives/SPEC-0037/`
- `.agents/prd.md`
- `.agents/outer-loop/README.md`

### 비대상
- 새 대표 검증 시나리오 추가
- 운영자용 API 계약 변경
- 실제 앱/H2 대표 검증 재실행
- 장기 저장소 도입

### 연결 검증 기준
- `manual-rerun-script-application-maintenance-closeout-documented`

### 완료 조건
- 적용 예시를 고친 뒤 마지막에 무엇을 다시 확인해야 하는지와 체크리스트가 문서로 남는다.
- 문서 수정 후 어디까지 다시 확인해야 하는지 문서만으로 다시 구성할 수 있다.
- `SPEC-0037-summary.md`가 실제로 작성되고 경로가 남는다.

### 검증
- 관련 대상 테스트 실행 통과
- 저장소 표준 전체 테스트 명령 통과
- 문서 마감 리뷰 통과

### GitHub 이슈
- 새 이슈 생성
- 권장 제목: `[BE] 유지 보수 체크리스트와 단계 마감 정리`
