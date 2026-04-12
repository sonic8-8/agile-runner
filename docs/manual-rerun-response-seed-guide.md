# 운영용 조회 응답 준비 데이터 가이드

## 문서 목적
이 문서는 운영용 조회 응답 대표 검증을 준비할 때 필요한 준비 데이터를 어떤 이름과 어떤 위치로 관리하는지 정리하는 가이드다.
이 문서의 목적은 실제 적용 순서를 설명하는 것이 아니라, 준비 데이터 파일의 이름 규칙과 저장 위치, 파일 단위 기준을 먼저 고정하는 것이다.

## 이 문서를 먼저 읽어야 하는 경우
- 대표 검증 전에 local H2에 어떤 준비 데이터를 넣어야 하는지 감이 잡히지 않을 때
- 새 준비 데이터 파일을 만들어야 하는지, 기존 파일을 찾아야 하는지 먼저 판단해야 할 때
- 운영용 조회 응답 가이드의 예시와 준비 데이터 파일을 혼동하지 않으려 할 때

## 같이 읽는 문서
- 운영용 응답 의미와 예시 읽기 기준은 [manual-rerun-response-guide.md](/home/seaung13/workspace/agile-runner/docs/manual-rerun-response-guide.md) 에서 본다.
- 실제 대표 검증에서 어떤 준비 오류가 있었는지는 [TASK-0004-response-doc-runtime-alignment.md](/home/seaung13/workspace/agile-runner/.agents/outer-loop/retrospectives/SPEC-0020/TASK-0004-response-doc-runtime-alignment.md) 를 먼저 참고한다.
- 기준 파일과 자동 검증을 같이 관리하는 방법은 [SPEC-0022-summary.md](/home/seaung13/workspace/agile-runner/.agents/outer-loop/retrospectives/SPEC-0022/SPEC-0022-summary.md) 를 참고한다.

## 저장 위치
- 준비 데이터 파일은 `src/test/resources/manual-rerun-response-seed/` 아래에 둔다.
- 이 디렉토리는 운영용 조회 응답 대표 검증을 준비하기 위한 입력 자료와 확인용 SQL 파일만 둔다.
- 문서 예시 기준 파일은 `src/test/resources/manual-rerun-response-guide/` 아래에 둔다.
- 즉, `...response-seed/`는 대표 검증 준비용이고 `...response-guide/`는 문서 예시용이다.

## 디렉토리 단위 기준
- `source-execution/`
  - 대표 검증 전에 local H2에 직접 넣는 원본 실행 데이터 파일을 둔다.
  - retry 대표 검증처럼 기존 실행 1건이 먼저 있어야 시작되는 경우에 쓴다.
- `control-action-history/`
  - 관리자 조치 이력 row를 준비해야 하는 경우의 파일을 둔다.
  - `ACKNOWLEDGE`, `UNACKNOWLEDGE` 같은 조치 이력을 미리 넣어야 하는 대표 검증에 쓴다.
- `runtime-evidence/`
  - 대표 검증 뒤에 어떤 실행 근거를 확인할지 정리한 SQL 파일을 둔다.
  - 실행 자체를 미리 넣는 파일이 아니라, 결과 row를 확인하는 기준 SQL 파일이다.

## 파일 이름 규칙
- 파일 이름은 `대표 검증 시나리오 - 준비 데이터 종류 - 용도` 순서를 기본으로 잡는다.
- 대표 검증 시나리오는 `retry`, `rerun-acknowledge`, `rerun-unacknowledge`처럼 무엇을 검증하려는지 먼저 드러낸다.
- 준비 데이터 종류는 `source-execution`, `action-history`, `runtime-evidence`처럼 어떤 파일인지 적는다.
- 용도는 `seed`, `check`처럼 이 파일이 넣는 파일인지 확인 파일인지 구분한다.
- 예시
  - `retry-source-execution-seed.example.sql`
  - `rerun-acknowledge-action-history-seed.example.sql`
  - `retry-runtime-evidence-check.example.sql`

## 파일 단위 기준
- 원본 실행 데이터는 execution 하나를 준비하는 SQL 파일 하나로 둔다.
- 관리자 조치 이력은 action timeline 하나를 준비하는 SQL 파일 하나로 둔다.
- 실행 근거 확인 SQL은 대표 검증 하나를 확인하는 SQL 파일 하나로 둔다.
- 서로 다른 대표 검증 시나리오를 한 파일에 섞지 않는다.
- 즉 retry 준비 파일과 rerun 조치 이력 파일은 분리하고, 결과 확인 SQL도 시나리오별로 나눈다.

## 현재 만드는 첫 파일 뼈대
- `source-execution/retry-source-execution-seed.example.sql`
  - retry 대표 검증에 필요한 원본 실행 데이터 이름 규칙 예시
- `control-action-history/rerun-acknowledge-action-history-seed.example.sql`
  - rerun 조치 이력 대표 검증에 필요한 action history 이름 규칙 예시
- `runtime-evidence/rerun-runtime-evidence-check.example.sql`
  - rerun 대표 검증 후 결과 확인 SQL 이름 규칙 예시
- `runtime-evidence/retry-runtime-evidence-check.example.sql`
  - retry 대표 검증 후 결과 확인 SQL 이름 규칙 예시

## 이번 단계에서 하지 않는 것
- 준비 데이터 적용 순서 정리
- schema, enum 선확인 순서 정리
- actual app/H2 대표 검증 재실행
- 대표 검증 결과를 문서 예시 기준 파일로 옮기는 작업

이 내용은 후속 `SPEC-0024`에서 다룬다.
