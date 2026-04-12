# 운영용 조회 응답 준비 데이터 가이드

## 문서 목적
이 문서는 운영용 조회 응답 대표 검증을 준비할 때 필요한 준비 데이터를 어떤 이름과 어떤 위치로 관리하는지, 그리고 어떤 순서로 다뤄야 하는지 정리하는 가이드다.
현재 단계에서는 준비 데이터 파일의 이름 규칙과 저장 위치, 파일 단위 기준 위에 기본 적용 순서와 명령 경계를 먼저 고정한다.
schema와 enum 값 상세 점검 체크리스트는 다음 단계에서 따로 보강한다.

## 이 문서를 먼저 읽어야 하는 경우
- 대표 검증 전에 로컬 H2에 어떤 준비 데이터를 넣어야 하는지 감이 잡히지 않을 때
- 새 준비 데이터 파일을 만들어야 하는지, 기존 파일을 찾아야 하는지 먼저 판단해야 할 때
- 운영용 조회 응답 가이드의 예시와 준비 데이터 파일을 혼동하지 않으려 할 때

## 같이 읽는 문서
- 운영용 응답 의미와 예시 읽기 기준은 [manual-rerun-response-guide.md](/home/seaung13/workspace/agile-runner/docs/manual-rerun-response-guide.md) 에서 본다.
- 실제 대표 검증에서 어떤 준비 오류가 있었는지는 [TASK-0004-response-doc-runtime-alignment.md](/home/seaung13/workspace/agile-runner/.agents/outer-loop/retrospectives/SPEC-0020/TASK-0004-response-doc-runtime-alignment.md) 를 먼저 참고한다.
- 기준 파일과 자동 검증을 같이 관리하는 방법은 [SPEC-0022-summary.md](/home/seaung13/workspace/agile-runner/.agents/outer-loop/retrospectives/SPEC-0022/SPEC-0022-summary.md) 를 참고한다.
- 실제 적용 순서와 점검 체크리스트를 더 세밀하게 읽을 때는 현재 활성 단계 문서와 task 문서를 함께 본다.

## 빠르게 따라가는 기본 순서
1. 대표 검증 시나리오를 먼저 정한다.
   - `retry` 시나리오
   - `rerun-acknowledge` 시나리오
   - `rerun-unacknowledge` 시나리오
2. 해당 시나리오에 필요한 준비 데이터 파일을 고른다.
   - retry 원본 실행 준비 데이터
   - 관리자 조치 이력 준비 데이터
   - 실행 근거 확인 SQL
3. 앱을 띄우기 전에 로컬 H2에 넣어야 하는 준비 데이터부터 적용한다.
4. 앱을 띄운 뒤 대표 요청을 실행한다.
5. 앱을 종료한 뒤 실행 근거 확인 SQL로 결과 row를 확인한다.

이 문서의 핵심은 “무엇을 어느 시점에 다루는가”를 먼저 고정하는 것이다.
세부 점검 체크리스트와 schema, enum 값 확인 순서는 다음 단계에서 따로 정리한다.

## 저장 위치
- 준비 데이터 파일은 `src/test/resources/manual-rerun-response-seed/` 아래에 둔다.
- 이 디렉토리는 운영용 조회 응답 대표 검증을 준비하기 위한 입력 자료와 확인용 SQL 파일만 둔다.
- 문서 예시 기준 파일은 `src/test/resources/manual-rerun-response-guide/` 아래에 둔다.
- 즉, `...response-seed/`는 대표 검증 준비용이고 `...response-guide/`는 문서 예시용이다.

## 디렉토리 단위 기준
- `source-execution/`
  - 대표 검증 전에 로컬 H2에 직접 넣는 원본 실행 데이터 파일을 둔다.
  - `retry` 대표 검증처럼 기존 실행 1건이 먼저 있어야 시작되는 경우에 쓴다.
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
- 용도는 이 파일이 입력용 파일인지, 확인용 파일인지 구분한다.
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

## 준비 데이터 적용 순서
- 기본 순서는 아래와 같다.
  1. 대표 검증 시나리오 선택
  2. 필요한 준비 데이터 파일 확인
  3. 앱 기동 전 준비 데이터 적용
  4. 앱 기동
5. 대표 요청 실행
  6. 앱 종료
  7. 실행 근거 확인 SQL 실행
- `retry` 시나리오는 원본 실행 준비 데이터가 먼저 있어야 하므로 `source-execution/` 아래 파일을 가장 먼저 적용한다.
- rerun 조치 이력 시나리오는 조치 이력 row가 먼저 있어야 하므로 `control-action-history/` 아래 파일을 앱 기동 전에 적용한다.
- `runtime-evidence/` 아래 SQL은 준비 데이터를 넣는 단계가 아니라, 대표 요청 실행 뒤 결과 row를 확인하는 마지막 단계에 사용한다.

## 명령 경계 기준
- 앱 기동 전에 하는 일
  - 대표 검증 시나리오 선택
  - 필요한 준비 데이터 파일 확인
  - 로컬 H2에 retry 원본 실행 준비 데이터 적용
  - 로컬 H2에 관리자 조치 이력 준비 데이터 적용
- 앱이 실행 중일 때 하는 일
  - 대표 요청 실행
  - HTTP 응답 확인
- 앱 종료 후 하는 일
  - 실행 근거 확인 SQL 실행
  - 대표 검증 결과와 준비 데이터 파일 경계 확인

앱이 실행 중일 때는 준비 데이터를 추가로 넣거나 H2 확인 SQL을 바로 실행하지 않는 것을 기본으로 본다.
준비 데이터 입력은 앱 기동 전, 실행 근거 확인은 앱 종료 후로 나눠 읽는 편이 대표 검증 실패 원인을 분리하기 쉽다.

## 준비 데이터를 손대기 전에 먼저 기억할 점
- 준비 데이터 파일은 제품 버그보다 준비 오류 때문에 먼저 깨질 수 있다.
- schema와 enum 값 상세 확인 순서는 다음 단계에서 따로 정리한다.
- 이번 단계에서는 어떤 파일을 어느 시점에 다루는지와 앱 기동 전/후 명령 경계를 먼저 고정한다.

## 새 파일을 만들 때와 기존 파일을 갱신할 때
- 아래 중 하나면 새 파일을 만든다.
  - 새 대표 검증 시나리오가 생겼을 때
  - 기존 파일이 설명하지 못하는 새 준비 데이터 종류가 생겼을 때
  - 같은 시나리오라도 원본 실행 데이터, 관리자 조치 이력, 실행 근거 확인 SQL이 서로 다른 파일 단위여야 할 때
- 아래 중 하나면 기존 파일을 갱신한다.
  - 파일 이름 규칙은 유지되지만 컬럼 이름이나 enum 값이 현재 코드와 달라졌을 때
  - 같은 대표 검증 시나리오 안에서 예시 SQL 주석이나 파일 설명만 더 정확히 써야 할 때
  - 이미 있는 파일이 같은 시나리오와 같은 준비 데이터 종류를 계속 설명할 수 있을 때
- 헷갈리면 먼저 “이 변경이 새 시나리오를 추가하는가”를 본다. 아니면 기존 파일 갱신 쪽을 먼저 검토한다.

## 준비 데이터 파일, 기준 파일, 대표 검증 결과의 경계
- 준비 데이터 파일은 대표 검증을 시작하기 전에 로컬 H2에 넣거나, 검증 뒤에 어떤 row를 확인할지 정리한 입력 자료다.
- 기준 파일은 문서 설명에 맞춰 남겨 두는 응답 예시다.
- 대표 검증 결과는 실제 앱을 띄운 뒤 얻는 실행 근거다.
- 이 셋은 역할이 다르므로 바로 섞지 않는다.
- 대표 검증에서 새 `executionKey`, `delivery_id`, UUID, 시각이 나왔다고 해서 그 값을 준비 데이터 파일이나 기준 파일에 바로 복사하지 않는다.
- 준비 데이터 파일에는 반복 가능한 준비 규칙과 예시 SQL 뼈대만 남기고, 실제 실행 결과 값은 회고와 실행 근거에 남긴다.
- 기준 파일은 응답 의미가 바뀔 때만 수정한다. 준비 데이터 파일이 바뀌었다는 이유만으로 기준 파일을 같이 갱신하지 않는다.
- 반대로 대표 검증 결과가 새로 나왔다고 해서 준비 데이터 파일 이름 규칙까지 바로 바꾸지 않는다. 먼저 새 시나리오인지, 기존 파일 갱신인지부터 판단한다.

## 현재 만드는 첫 파일 뼈대
- `source-execution/retry-source-execution-seed.example.sql`
  - retry 대표 검증에 필요한 원본 실행 데이터 이름 규칙 예시
- `control-action-history/rerun-acknowledge-action-history-seed.example.sql`
  - rerun 조치 이력 대표 검증에 필요한 action history 이름 규칙 예시
- `runtime-evidence/rerun-runtime-evidence-check.example.sql`
  - rerun 대표 검증 후 결과 확인 SQL 이름 규칙 예시
- `runtime-evidence/retry-runtime-evidence-check.example.sql`
  - retry 대표 검증 후 결과 확인 SQL 이름 규칙 예시

## 이번 단계에서 아직 하지 않는 것
- schema와 enum 값 상세 확인 순서 정리
- 대표 검증 전 체크리스트 상세화
- 실제 앱/H2 대표 검증 재실행
- 대표 검증 결과를 문서 예시 기준 파일로 옮기는 작업

이 내용은 현재 활성 spec의 다음 task와 후속 `SPEC-0025`에서 이어서 다룬다.
