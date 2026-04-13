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
- 비어 있는 값이 나오면 retry 응답이 실제로 `executionKey`를 반환했는지 먼저 확인한다.

## 앱 종료 후 확인 전 점검 명령
```bash
pgrep -af "GradleMain|gradle.*bootRun" || true
pgrep -af "org.h2.tools.RunScript|org.h2.tools.Shell" || true
```

- 앱 종료 뒤에도 `bootRun` 프로세스가 남아 있지 않은지 먼저 본다.
- H2 조회 명령줄 도구가 이미 떠 있으면 순차 조회 규칙에 맞지 않으므로 먼저 정리한다.
- H2 lock 오류가 나면 schema나 runtime 문제로 단정하기 전에 이 두 가지를 다시 확인한다.

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
- 실제 컬럼 구성은 `runtime-evidence/rerun-runtime-evidence-check.example.sql`과 같은 기준으로 읽는다.

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
- 실제 컬럼 구성은 `runtime-evidence/retry-runtime-evidence-check.example.sql`과 같은 기준으로 읽는다.

## 어떤 응답에서 받은 실행 키를 어디에 다시 쓰는가
- `rerun-acknowledge`, `rerun-unacknowledge`
  - 준비 데이터 파일이 넣은 `RERUN_EXECUTION_KEY`를 그대로 쓴다.
  - 단건 조회, 이력 조회, 관리자 조치 요청, 앱 종료 뒤 H2 조회까지 같은 실행 키로 이어진다.
- `retry`
  - 요청 경로에는 `RETRY_SOURCE_EXECUTION_KEY`를 쓴다.
  - retry 응답에서 받은 `executionKey`를 `RETRY_DERIVED_EXECUTION_KEY`로 저장한다.
  - 이후 파생 실행 단건 조회와 H2 `WEBHOOK_EXECUTION`, `AGENT_EXECUTION_LOG` 조회는 모두 `RETRY_DERIVED_EXECUTION_KEY`를 기준으로 이어간다.
- 즉 원본 실행 키는 retry 요청 시작점이고, 파생 실행 키는 요청 직후 확인과 앱 종료 뒤 근거 확인의 기준이다.

## 대표 검증 시나리오별 시작 순서
### retry
1. 시작 전 확인 명령 실행
2. 공통 정리 명령 실행
3. retry 원본 실행 준비 명령 실행
4. 앱 기동
5. retry 요청 실행과 응답 파일 저장
6. 응답에서 `RETRY_DERIVED_EXECUTION_KEY` 추출
7. 앱 종료
8. 앱 종료 후 확인 전 점검 명령 실행
9. retry 파생 실행 근거 H2 조회 명령 실행

### rerun-acknowledge / rerun-unacknowledge
1. 시작 전 확인 명령 실행
2. 공통 정리 명령 실행
3. rerun acknowledge 상태 준비 명령 실행
4. 앱 기동
5. 단건 조회, 이력 조회, 관리자 조치 요청과 확인 흐름 실행
6. 앱 종료
7. 앱 종료 후 확인 전 점검 명령 실행
8. rerun 실행 근거 H2 조회 명령 실행

## 이 문서에서 아직 하지 않는 것
- 대표 검증 전체 절차를 한 번에 자동화하는 스크립트
- 실제 대표 검증에서 얻은 UUID 값을 기준 파일이나 준비 데이터 파일에 바로 반영하는 작업
- 실제 대표 검증을 다시 실행하는 일 자체

이 범위는 다음 단계에서 정리한다.
