# 운영용 조회 응답 준비 데이터 적용 보조 명령 가이드

## 문서 목적
이 문서는 운영용 조회 응답 대표 검증 전에 준비 데이터 SQL을 적용하고 정리할 때 반복해서 쓰는 기본 명령 예시를 정리한다.
현재 단계에서는 `rerun` 준비 실행과 `retry` 원본 실행을 로컬 H2 파일 DB에 넣거나 정리하는 명령, retry 응답에서 파생 실행 키를 읽는 명령, 앱 종료 뒤 H2 실행 근거를 확인하는 명령을 한 흐름으로 정리하는 데 집중한다.

## 이 문서를 먼저 읽어야 하는 경우
- 대표 검증 전에 어떤 명령부터 실행해야 하는지 바로 감이 안 잡힐 때
- 준비 데이터 SQL 파일은 준비됐지만 로컬 H2에 어떤 순서로 적용해야 하는지 다시 확인하고 싶을 때
- 기존 예시 결과 행이나 이전 대표 검증 실행 흔적을 어디까지 정리해야 하는지 헷갈릴 때
- retry 응답에서 받은 실행 키를 이후 조회와 H2 확인에 어디까지 다시 써야 하는지 헷갈릴 때

## 같이 읽는 문서
- 시나리오별 파일 선택 기준과 준비 순서는 [manual-rerun-response-seed-guide.md](/home/seaung13/workspace/agile-runner/docs/manual-rerun-response-seed-guide.md) 에서 먼저 본다.
- 응답 의미와 대표 검증 실행 결과를 어떻게 읽는지는 [manual-rerun-response-guide.md](/home/seaung13/workspace/agile-runner/docs/manual-rerun-response-guide.md) 에서 본다.
- 준비 데이터 SQL이 실제 대표 검증 절차에서 어떻게 쓰였는지는 [TASK-0004-seed-representative-application-verified.md](/home/seaung13/workspace/agile-runner/.agents/outer-loop/retrospectives/SPEC-0025/TASK-0004-seed-representative-application-verified.md) 를 참고한다.

## 공통 전제
- 앱은 `local` 프로필 기준으로 띄운다.
- 대표 검증 전 준비 데이터 적용과 정리는 앱 기동 전에 마친다.
- H2 파일을 외부에서 여는 보조 명령은 앱 종료 후에만 수행한다.
- 같은 H2 파일을 여러 셸에서 동시에 열지 않는다.

## 이 문서에서 스크립트 후보로 보는 단계와 수동 확인 단계
이 문서는 아직 실제 스크립트를 제공하지 않는다.
대신 현재 대표 검증 절차를 나중에 스크립트로 묶는다면 어디까지가 후보이고, 어디부터는 사람이 직접 실행 근거를 읽어야 하는지 경계를 먼저 정리한다.

| 단계 | 주 입력 | 주 출력 | 현재 판단 | 사람이 직접 확인할 것 |
| --- | --- | --- | --- | --- |
| 시작 전 포트/H2 프로세스 확인 | `APP_PORT`, 현재 실행 중인 프로세스 목록 | 포트 사용 여부, H2 명령줄 도구 실행 여부 | 스크립트 후보 | 포트 충돌이나 H2 셸 중복 실행이 실제 검증 실패로 이어질 위험이 있는지 판단 |
| 준비 데이터 정리 SQL 실행 | reset SQL 파일, `JDBC_URL` | 정리 완료된 H2 상태 | 스크립트 후보 | 삭제 범위가 현재 대표 검증 시나리오와 맞는지 확인 |
| 준비 데이터 적용 SQL 실행 | 준비 데이터 SQL 파일 경로, `JDBC_URL` | 대표 검증 시작 전 H2 입력 상태 | 스크립트 후보 | 현재 시나리오에 맞는 파일을 골랐는지 확인 |
| 앱 기동 | `SPRING_PROFILES_ACTIVE`, `SERVER_PORT` | local 프로필로 실행 중인 앱 | 스크립트 후보 | 앱이 실제로 기동됐는지, 시작 실패가 없는지 확인 |
| 대표 검증 HTTP 요청 실행 | `BASE_URL`, 실행 키 값, 요청 본문 | 응답 본문 파일, HTTP 상태 | 스크립트 후보 | 응답 값이 기대 시나리오와 맞는지 판단 |
| retry 파생 실행 키 추출 | retry 응답 파일 | 파생 실행 키 값 | 스크립트 후보 | 추출된 실행 키가 비어 있지 않은지 확인 |
| 앱 종료 | bootRun 프로세스 | 종료된 앱 상태 | 스크립트 후보 | 종료가 실제로 완료됐는지 확인 |
| H2 조회 명령 실행 | `JDBC_URL`, 실행 키, 조회 SQL | H2 결과 텍스트 | 스크립트 후보 | 잠금 오류인지, 실제 스키마 문제인지 실행 흐름 문제인지 구분 |
| 응답과 H2 결과 의미 해석 | 응답 파일, H2 조회 결과 | 대표 검증 결론, 회고 근거 | 수동 확인 단계 | `availableActions`, `retrySourceExecutionKey`, 감사 이력 행 의미가 같은 실행 기준으로 맞는지 판단 |

- 위 표에서 `스크립트 후보`는 명령 실행 자체를 묶는 후보라는 뜻이다.
- `수동 확인 단계`는 명령이 끝난 뒤 결과 의미를 해석하고 회고에 남기는 단계다.
- 즉, 명령 실행은 스크립트가 도울 수 있어도 `응답 값이 맞는지`, `H2 결과가 무엇을 뜻하는지`, `실패가 잠금 문제인지 코드 문제인지`는 사람이 직접 닫아야 한다.

## 공통 환경 변수 예시
```bash
export APP_PORT=18080
export BASE_URL="http://localhost:${APP_PORT}"
export H2_DB_PATH="$HOME/.agile-runner/agent-runtime/agile-runner"
export JDBC_URL="jdbc:h2:file:${H2_DB_PATH};MODE=PostgreSQL;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE"
export H2_JAR="$(find \"$HOME/.gradle/caches/modules-2/files-2.1/com.h2database/h2\" -name 'h2-*.jar' | sort | tail -n 1)"
export RERUN_EXECUTION_KEY="EXECUTION:MANUAL_RERUN:example-rerun"
export RETRY_SOURCE_EXECUTION_KEY="EXECUTION:MANUAL_RERUN:example-retry-source"
export RETRY_RESPONSE_FILE="/tmp/manual-rerun-retry-response.json"
export RERUN_QUERY_BEFORE_FILE="/tmp/manual-rerun-rerun-query-before.json"
export RERUN_HISTORY_FILE="/tmp/manual-rerun-rerun-history.json"
export RERUN_ACTION_FILE="/tmp/manual-rerun-rerun-action.json"
export RERUN_QUERY_AFTER_FILE="/tmp/manual-rerun-rerun-query-after.json"
export RETRY_DERIVED_QUERY_FILE="/tmp/manual-rerun-retry-derived-query.json"
```

## 시작 전 확인 명령
```bash
lsof -i :"${APP_PORT}" || true
pgrep -af "org.h2.tools.RunScript|org.h2.tools.Shell" || true
```

- 포트가 비어 있고 H2 명령줄 도구가 떠 있지 않은 상태에서 시작한다.
- 여기서는 프로세스 존재 여부만 확인한다. H2 결과 조회 명령은 아직 이 문서 범위가 아니다.

## 대표 검증 전에 공통으로 쓰는 정리 명령
다음 정리 SQL은 고정 예시 실행과, 그 예시를 원본으로 삼아 생성된 retry 파생 실행을 함께 지우는 기본 예시다.

```bash
cat <<'SQL' >/tmp/manual-rerun-response-seed-reset.sql
DELETE FROM MANUAL_RERUN_CONTROL_ACTION_AUDIT
WHERE execution_key = 'EXECUTION:MANUAL_RERUN:example-rerun';

DELETE FROM AGENT_EXECUTION_LOG
WHERE execution_key = 'EXECUTION:MANUAL_RERUN:example-rerun'
   OR execution_key = 'EXECUTION:MANUAL_RERUN:example-retry-source'
   OR retry_source_execution_key = 'EXECUTION:MANUAL_RERUN:example-retry-source';

DELETE FROM WEBHOOK_EXECUTION
WHERE execution_key = 'EXECUTION:MANUAL_RERUN:example-rerun'
   OR execution_key = 'EXECUTION:MANUAL_RERUN:example-retry-source'
   OR retry_source_execution_key = 'EXECUTION:MANUAL_RERUN:example-retry-source';
SQL

java -cp "${H2_JAR}" org.h2.tools.RunScript \
  -url "${JDBC_URL}" \
  -user sa \
  -script /tmp/manual-rerun-response-seed-reset.sql
```

- retry 파생 실행은 실행 키가 매번 달라질 수 있으므로, 정리할 때는 `retry_source_execution_key` 기준 삭제를 같이 쓴다.
- 이 정리 명령은 대표 검증 시작 전에 한 번 실행하는 기본 예시다.

## retry 원본 실행 준비 명령
```bash
java -cp "${H2_JAR}" org.h2.tools.RunScript \
  -url "${JDBC_URL}" \
  -user sa \
  -script src/test/resources/manual-rerun-response-seed/source-execution/retry-source-execution-seed.example.sql
```

- 이 명령은 `retry` 대표 검증을 시작하기 전에 실행한다.
- 실제 retry 요청과 파생 실행 키 추출은 아래 후속 섹션에서 이어진다.

## rerun acknowledge 상태 준비 명령
```bash
java -cp "${H2_JAR}" org.h2.tools.RunScript \
  -url "${JDBC_URL}" \
  -user sa \
  -script src/test/resources/manual-rerun-response-seed/control-action-history/rerun-acknowledge-action-history-seed.example.sql
```

- 이 명령은 `rerun-acknowledge`, `rerun-unacknowledge` 대표 검증을 시작하기 전에 실행한다.
- 실제 단건 조회, 이력 조회, 관리자 조치 요청과 확인 흐름은 아래 시나리오 순서에서 이어진다.

## 앱 기동 명령
```bash
SPRING_PROFILES_ACTIVE=local \
SERVER_PORT="${APP_PORT}" \
./scripts/gradlew-java21.sh bootRun --console=plain
```

## retry 응답 저장과 파생 실행 키 추출 명령
```bash
curl -sS -X POST \
  "${BASE_URL}/reviews/rerun/${RETRY_SOURCE_EXECUTION_KEY}/retry" \
  -H 'Content-Type: application/json' \
  -d '{"executionControlMode":"DRY_RUN"}' \
  > "${RETRY_RESPONSE_FILE}"

export RETRY_DERIVED_EXECUTION_KEY="$(
  tr -d '\n' < "${RETRY_RESPONSE_FILE}" \
    | sed -n 's/.*"executionKey":"\([^"]*\)".*/\1/p'
)"

test -n "${RETRY_DERIVED_EXECUTION_KEY}"
printf '%s\n' "${RETRY_DERIVED_EXECUTION_KEY}"
```

- `jq` 없이도 `curl`, `tr`, `sed`만으로 파생 실행 키를 읽는 기본 예시다.
- `RETRY_RESPONSE_FILE`은 retry 응답을 한 번 더 확인하거나 회고에 근거를 남길 때 같이 쓸 수 있다.
- 비어 있는 값이 나오면 retry 응답이 실제로 실행 키 값을 반환했는지 먼저 확인한다.

## rerun 대표 검증 요청 명령
```bash
curl -sS \
  "${BASE_URL}/reviews/rerun/${RERUN_EXECUTION_KEY}" \
  > "${RERUN_QUERY_BEFORE_FILE}"

curl -sS \
  "${BASE_URL}/reviews/rerun/${RERUN_EXECUTION_KEY}/actions/history" \
  > "${RERUN_HISTORY_FILE}"

curl -sS -X POST \
  "${BASE_URL}/reviews/rerun/${RERUN_EXECUTION_KEY}/actions" \
  -H 'Content-Type: application/json' \
  -d '{"action":"UNACKNOWLEDGE","note":"대표 검증용 확인 해제"}' \
  > "${RERUN_ACTION_FILE}"

curl -sS \
  "${BASE_URL}/reviews/rerun/${RERUN_EXECUTION_KEY}" \
  > "${RERUN_QUERY_AFTER_FILE}"
```

- rerun 준비 실행은 `RERUN_EXECUTION_KEY` 하나로 단건 조회, 이력 조회, 관리자 조치 요청, 조치 후 단건 조회를 이어간다.
- `RERUN_QUERY_BEFORE_FILE`, `RERUN_HISTORY_FILE`, `RERUN_ACTION_FILE`, `RERUN_QUERY_AFTER_FILE`은 응답 본문을 남겨 두는 기본 예시다.
- 실제 대표 검증에서는 조치 전 `availableActions=["UNACKNOWLEDGE"]`, 조치 후 `availableActions=["ACKNOWLEDGE"]`인지 같이 확인한다.

## retry 파생 실행 단건 조회 명령
```bash
curl -sS \
  "${BASE_URL}/reviews/rerun/${RETRY_DERIVED_EXECUTION_KEY}" \
  > "${RETRY_DERIVED_QUERY_FILE}"
```

- retry는 응답에서 받은 `RETRY_DERIVED_EXECUTION_KEY`를 곧바로 단건 조회와 H2 실행 근거 확인에 다시 쓴다.
- `RETRY_DERIVED_QUERY_FILE`은 파생 실행의 query 응답을 남겨 두는 기본 예시다.

## 앱 종료 예시
- `bootRun`을 앞 포그라운드 셸에서 띄웠다면 `Ctrl+C`로 종료한다.
- 앱 종료 뒤에만 `앱 종료 후 확인 전 점검 명령`과 H2 조회 명령을 실행한다.

## 앱 종료 후 확인 전 점검 명령
```bash
pgrep -af "GradleMain|gradle.*bootRun" || true
pgrep -af "org.h2.tools.RunScript|org.h2.tools.Shell" || true
```

- 앱 종료 뒤에도 `bootRun` 프로세스가 남아 있지 않은지 먼저 본다.
- H2 조회 명령줄 도구가 이미 떠 있으면 순차 조회 규칙에 맞지 않으므로 먼저 정리한다.
- H2 잠금 오류가 나면 곧바로 스키마 문제나 실행 흐름 문제로 단정하지 말고, 먼저 이 두 가지를 다시 확인한다.

## rerun 실행 근거 H2 조회 명령
```bash
java -cp "${H2_JAR}" org.h2.tools.Shell \
  -url "${JDBC_URL}" \
  -user sa \
  -sql "
SELECT execution_key, status, error_code, failure_disposition, execution_start_type, execution_control_mode, write_performed
FROM WEBHOOK_EXECUTION
WHERE execution_key = '${RERUN_EXECUTION_KEY}';

SELECT execution_key, action, action_status, note, applied_at
FROM MANUAL_RERUN_CONTROL_ACTION_AUDIT
WHERE execution_key = '${RERUN_EXECUTION_KEY}'
ORDER BY applied_at ASC, id ASC;
"
```

- rerun 대표 검증은 준비 데이터 파일이 넣은 고정 실행 키를 그대로 다시 쓴다.
- 실제 컬럼 구성은 rerun 실행 근거 확인 SQL 파일을 기준으로 읽는다.
  - `src/test/resources/manual-rerun-response-seed/runtime-evidence/rerun-runtime-evidence-check.example.sql`

## retry 파생 실행 근거 H2 조회 명령
```bash
java -cp "${H2_JAR}" org.h2.tools.Shell \
  -url "${JDBC_URL}" \
  -user sa \
  -sql "
SELECT execution_key, retry_source_execution_key, status, error_code, failure_disposition, execution_start_type, execution_control_mode, write_performed
FROM WEBHOOK_EXECUTION
WHERE execution_key = '${RETRY_DERIVED_EXECUTION_KEY}';

SELECT execution_key, retry_source_execution_key, step_name, status, error_code, failure_disposition
FROM AGENT_EXECUTION_LOG
WHERE execution_key = '${RETRY_DERIVED_EXECUTION_KEY}'
ORDER BY id ASC;
"
```

- retry 대표 검증은 준비 데이터 파일이 넣은 원본 실행 키로 요청을 시작하지만, 앱 종료 뒤 H2 조회는 응답에서 받은 파생 실행 키를 기준으로 이어간다.
- 실제 컬럼 구성은 retry 실행 근거 확인 SQL 파일을 기준으로 읽는다.
  - `src/test/resources/manual-rerun-response-seed/runtime-evidence/retry-runtime-evidence-check.example.sql`

## 어떤 응답에서 받은 실행 키를 어디에 다시 쓰는가
- `rerun-acknowledge`, `rerun-unacknowledge`
  - 준비 데이터 파일이 넣은 `RERUN_EXECUTION_KEY`를 그대로 쓴다.
  - 단건 조회, 이력 조회, 관리자 조치 요청, 앱 종료 뒤 H2 조회까지 같은 실행 키로 이어진다.
- `retry`
  - 요청 경로에는 `RETRY_SOURCE_EXECUTION_KEY`를 쓴다.
  - retry 응답에서 받은 실행 키 값을 `RETRY_DERIVED_EXECUTION_KEY`로 저장한다.
  - 이후 파생 실행 단건 조회와 H2 `WEBHOOK_EXECUTION`, `AGENT_EXECUTION_LOG` 조회는 모두 `RETRY_DERIVED_EXECUTION_KEY`를 기준으로 이어간다.
- 즉 원본 실행 키는 retry 요청 시작점이고, 파생 실행 키는 요청 직후 확인과 앱 종료 뒤 근거 확인의 기준이다.

## 입력 값과 출력 근거 자료 경계
- 입력 값
  - `APP_PORT`, `BASE_URL`, `JDBC_URL`, `H2_JAR`
  - `RERUN_EXECUTION_KEY`, `RETRY_SOURCE_EXECUTION_KEY`
  - 준비 데이터 SQL 파일 경로
  - retry 요청 본문, 관리자 조치 요청 본문
- 출력 근거 자료
  - `RETRY_RESPONSE_FILE`
  - `RERUN_QUERY_BEFORE_FILE`
  - `RERUN_HISTORY_FILE`
  - `RERUN_ACTION_FILE`
  - `RERUN_QUERY_AFTER_FILE`
  - `RETRY_DERIVED_QUERY_FILE`
  - H2 조회 결과 텍스트
- 이 문서에서 말하는 `출력 근거 자료`는 명령 결과를 임시로 담아 두는 파일이나 조회 결과다.
- 회고 문서에 남기는 최종 근거는 이 출력 근거 자료를 사람이 읽고 정리한 뒤에 만들어진다.
- 따라서 다음 단계에서 스크립트를 검토하더라도, 회고와 제안 여부 판단까지 자동으로 넘기는 범위는 현재 비대상으로 둔다.

## 스크립트 검토로 기대하는 점
- 준비 데이터 정리, 준비 데이터 적용, 앱 기동, 대표 요청, 실행 키 추출, 앱 종료, H2 조회처럼 반복되는 명령을 한 번에 다시 실행하기 쉬워진다.
- 임시 파일 경로, 환경 변수, 실행 순서를 사람이 매번 다시 떠올리지 않아도 돼 대표 검증 재현 비용을 낮출 수 있다.
- retry 파생 실행 키 추출, 앱 종료 뒤 H2 조회 같은 반복 절차를 같은 형태로 유지해 실행 근거 수집 흐름을 더 일정하게 만들 수 있다.

## 지금 단계에서 먼저 적어 두는 위험
- H2 조회나 응답 값 해석까지 과하게 자동화하면 잠금 오류, 스키마 문제, 실행 의미 차이를 사람이 늦게 발견할 수 있다.
- 실제 대표 검증은 `실행 키`, `availableActions`, 감사 이력 행 의미를 함께 읽어야 해서, 명령 실행만 자동화하고 의미 해석은 수동으로 남기는 경계가 흐려지면 오히려 디버깅이 어려워질 수 있다.
- 문서, 기준 파일, 준비 데이터 SQL, 보조 명령이 따로 바뀌면 스크립트가 오래된 절차를 계속 실행하는 문서 어긋남 위험이 생긴다.

## 유지 비용으로 보는 항목
- 새 스크립트를 만들면 환경 변수 이름, 임시 파일 경로, 준비 데이터 SQL 경로가 바뀔 때마다 문서와 같이 갱신해야 한다.
- 대표 검증 시나리오가 늘어나면 스크립트 입력 조합도 함께 늘어나서, 간단한 보조 명령 문서보다 유지 부담이 커질 수 있다.
- 로컬 환경 차이, 포트 충돌, H2 파일 잠금 같은 실패는 스크립트 자체보다 실행 환경 점검 비용으로 다시 돌아올 수 있다.

## 이번 단계에서 하지 않는 것
- 대표 검증 전체 절차를 한 번에 실행하는 실제 스크립트 초안
- 대표 검증 결과를 회고 문서나 기준 파일에 자동 반영하는 처리
- 응답 값과 H2 결과 의미를 자동으로 판정해 합격 여부까지 내리는 자동 평가

이 문서는 지금 단계에서 `어디까지 자동화를 검토할 가치가 있는지`와 `무엇은 계속 사람이 직접 닫아야 하는지`를 정리하는 데까지만 쓴다.

## 현재 단계 판단
- 다음 단계에서 실제 스크립트 초안 범위를 검토할 가치는 충분하다.
- 이유는 준비 데이터 정리/적용, 앱 기동/종료, 대표 요청, 실행 키 추출, H2 조회처럼 반복 명령이 이미 일정한 순서와 입력/출력 경계로 정리됐기 때문이다.
- 다만 실제 초안이 검토되더라도 `응답 값이 기대 시나리오와 맞는지`, `H2 결과가 무엇을 뜻하는지`, `잠금 오류를 코드 문제와 어떻게 구분할지`는 계속 사람이 직접 닫아야 한다.
- 따라서 다음 단계는 `명령 실행을 어디까지 스크립트 초안으로 묶을지`만 검토하고, 결과 의미 해석과 회고 작성은 수동 단계로 유지하는 방향이 적절하다.
- 이번 단계에서는 실제 스크립트 구현을 바로 시작하지 않고, 후속 단계 `운영용 조회 응답 준비 데이터 반복 검증 스크립트 초안 검토`로 넘겨 초안 범위를 먼저 좁히는 쪽으로 마감한다.

## 대표 검증 시나리오별 시작 순서
### retry
1. 시작 전 확인 명령 실행
2. 공통 정리 명령 실행
3. retry 원본 실행 준비 명령 실행
4. 앱 기동
5. retry 응답 저장과 파생 실행 키 추출 명령 실행
6. retry 파생 실행 단건 조회 명령 실행
7. 앱 종료
8. 앱 종료 후 확인 전 점검 명령 실행
9. retry 파생 실행 근거 H2 조회 명령 실행

### rerun-acknowledge / rerun-unacknowledge
1. 시작 전 확인 명령 실행
2. 공통 정리 명령 실행
3. rerun acknowledge 상태 준비 명령 실행
4. 앱 기동
5. rerun 대표 검증 요청 명령 실행
6. 앱 종료
7. 앱 종료 후 확인 전 점검 명령 실행
8. rerun 실행 근거 H2 조회 명령 실행

## 이 문서에서 아직 하지 않는 것
- 대표 검증 전체 절차를 한 번에 자동화하는 스크립트
- 실제 대표 검증에서 얻은 UUID 값을 기준 파일이나 준비 데이터 파일에 바로 반영하는 작업

이 범위는 다음 단계에서 정리한다.
