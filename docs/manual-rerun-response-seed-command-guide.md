# 운영용 조회 응답 준비 데이터 적용 보조 명령 가이드

## 문서 목적
이 문서는 운영용 조회 응답 대표 검증에서 준비 데이터 SQL을 적용하고, 실제 초안 파일로 재실행/재시도 요청을 실행하고, 앱 종료 뒤 H2 실행 근거를 확인하는 기본 절차를 정리한다.
현재 단계에서는 `prepare-seed.sh`, `run-rerun.sh`, `run-retry.sh`, `collect-evidence.sh`를 실제로 쓰는 흐름을 기준으로 본다.

## 이 문서를 먼저 읽어야 하는 경우
- 대표 검증을 다시 돌릴 때 어떤 스크립트부터 실행해야 하는지 바로 감이 안 잡힐 때
- 예시 SQL은 준비됐지만 실제 검증에서는 어떤 식으로 임시 복사본을 만들어 써야 하는지 헷갈릴 때
- 재시도 응답에서 얻은 파생 실행 키를 이후 조회와 H2 확인에 어디까지 다시 써야 하는지 헷갈릴 때
- 스크립트가 멈춘 뒤 어디까지는 파일이 남고, 어디서부터 사람이 의미를 직접 판단해야 하는지 다시 확인하고 싶을 때

## 같이 읽는 문서
- 시나리오별 준비 데이터 파일 선택 기준과 파일 머리말은 [manual-rerun-response-seed-guide.md](/home/seaung13/workspace/agile-runner/docs/manual-rerun-response-seed-guide.md) 에서 먼저 본다.
- 응답 의미와 대표 검증 결과를 어떻게 읽는지는 [manual-rerun-response-guide.md](/home/seaung13/workspace/agile-runner/docs/manual-rerun-response-guide.md) 에서 본다.
- 준비 데이터 SQL과 실제 앱/H2 대표 검증이 실제로 어떻게 맞았는지는 [TASK-0004-seed-representative-application-verified.md](/home/seaung13/workspace/agile-runner/.agents/outer-loop/retrospectives/SPEC-0025/TASK-0004-seed-representative-application-verified.md) 를 참고한다.

## 시작 전에 먼저 확인할 근거
- 직전 단계 전체 판단은 [SPEC-0030-summary.md](/home/seaung13/workspace/agile-runner/.agents/outer-loop/retrospectives/SPEC-0030/SPEC-0030-summary.md) 에서 먼저 본다.
- 마지막 대표 검증에서 어떤 실행 키, 전달 식별자, 출력 파일을 함께 남겼는지는 [TASK-0004-script-draft-representative-verified.md](/home/seaung13/workspace/agile-runner/.agents/outer-loop/retrospectives/SPEC-0030/TASK-0004-script-draft-representative-verified.md) 에서 먼저 본다.
- 현재 자동 검증 안전망은 아래 테스트가 맡는다.
  - [ManualRerunRunFlowScriptTest.java](/home/seaung13/workspace/agile-runner/src/test/java/com/agilerunner/client/agentruntime/ManualRerunRunFlowScriptTest.java)
  - [ManualRerunSeedCommandScriptTest.java](/home/seaung13/workspace/agile-runner/src/test/java/com/agilerunner/client/agentruntime/ManualRerunSeedCommandScriptTest.java)
  - [ManualRerunResponseSeedSqlTest.java](/home/seaung13/workspace/agile-runner/src/test/java/com/agilerunner/client/agentruntime/ManualRerunResponseSeedSqlTest.java)
  - [ManualRerunResponseSeedEvidenceSqlTest.java](/home/seaung13/workspace/agile-runner/src/test/java/com/agilerunner/client/agentruntime/ManualRerunResponseSeedEvidenceSqlTest.java)
- 위 근거와 테스트가 그대로 유효하면, 다음 단계는 새 안전망을 만드는 대신 현재 가이드 정리부터 시작해도 된다.

## 공통 전제
- 앱은 `local` 프로필 기준으로 띄운다.
- 준비 데이터 정리와 적용은 앱 기동 전에 마친다.
- 실제 앱/H2 대표 검증은 `준비 데이터 정리/적용 -> 앱 기동 -> 요청 실행 -> 앱 종료 -> H2 조회` 순서로 진행한다.
- H2 파일 DB는 앱 종료 뒤에만 외부에서 조회한다.
- 같은 H2 파일을 여러 셸에서 동시에 열지 않는다.
- 실제 앱/H2 대표 검증은 기존 예시 키를 그대로 재사용하지 않고, 임시 SQL 복사본에 새 접미사를 넣어 새 실행 키와 전달 식별자를 만든다.

## 실제 초안 파일이 맡는 단계와 수동 확인 단계
| 단계 | 주 입력 | 주 출력 | 현재 담당 | 사람이 직접 확인할 것 |
| --- | --- | --- | --- | --- |
| 시작 전 포트/H2 프로세스 확인과 준비 데이터 적용 | `APP_PORT`, `JDBC_URL`, `H2_JAR`, 정리/적용 SQL | `prepare.log`, 정리/적용된 H2 상태 | `prepare-seed.sh` | 선택한 SQL 파일 경로와 시나리오 일치 여부 |
| 재실행 대표 요청 실행 | `BASE_URL`, `RERUN_EXECUTION_KEY`, `RERUN_ACTION_BODY` | `rerun-query-before.json`, `rerun-history.json`, `rerun-action.json`, `rerun-query-after.json`, `app.pid` | `run-rerun.sh` | 응답 의미 비교 |
| 재시도 대표 요청 실행 | `BASE_URL`, `RETRY_SOURCE_EXECUTION_KEY`, `RETRY_REQUEST_BODY` | `retry-response.json`, `retry-derived-execution-key.txt`, `retry-derived-query.json`, `app.pid` | `run-retry.sh` | 재시도 응답과 파생 실행 의미 비교 |
| 앱 종료 뒤 실행 근거 수집 | `JDBC_URL`, `H2_JAR`, `EVIDENCE_MODE`, 실행 키 | 재실행 또는 재시도 H2 조회 결과 텍스트 | `collect-evidence.sh` | H2 결과 의미 해석 |
| 응답과 H2 결과 결론 정리 | 응답 파일, H2 조회 결과, 회고 경로 | 대표 검증 결론 | 수동 확인 단계 | 응답과 H2 의미 일치 여부, 회고 작성 |

- 초안 파일은 명령 실행과 결과 파일 저장까지 맡는다.
- 대표 검증 결론, H2 결과 의미 해석, 회고와 제안 필요 여부 판단은 계속 사람이 직접 닫는다.

## 공통 환경 변수 예시
```bash
export APP_PORT=18080
export BASE_URL="http://localhost:${APP_PORT}"
export H2_DB_PATH="$HOME/.agile-runner/agent-runtime/agile-runner"
export JDBC_URL="jdbc:h2:file:${H2_DB_PATH};MODE=PostgreSQL;DB_CLOSE_ON_EXIT=FALSE"
export H2_JAR="$(find \"$HOME/.gradle/caches/modules-2/files-2.1/com.h2database/h2\" -name 'h2-*.jar' | sort | tail -n 1)"
export VERIFY_TS="$(date +%Y%m%d-%H%M%S)"
export OUTPUT_ROOT="$PWD/.tmp/manual-rerun-response-${VERIFY_TS}"
export RERUN_DIR="${OUTPUT_ROOT}/rerun"
export RETRY_DIR="${OUTPUT_ROOT}/retry"
mkdir -p "${RERUN_DIR}" "${RETRY_DIR}"
```

## 대표 검증용 임시 SQL 복사본 만들기
### 재실행 준비 데이터 임시 복사본
```bash
export RERUN_SUFFIX="spec0030-rerun-${VERIFY_TS}"
export RERUN_EXECUTION_KEY="EXECUTION:MANUAL_RERUN:${RERUN_SUFFIX}"
export RERUN_DELIVERY_ID="MANUAL_RERUN_DELIVERY:${RERUN_SUFFIX}"
export RERUN_APPLY_SQL="${RERUN_DIR}/rerun-apply.sql"
export RERUN_RESET_SQL="${RERUN_DIR}/rerun-reset.sql"

sed "s/example-rerun/${RERUN_SUFFIX}/g" \
  src/test/resources/manual-rerun-response-seed/control-action-history/rerun-acknowledge-action-history-seed.example.sql \
  > "${RERUN_APPLY_SQL}"

cat <<EOF > "${RERUN_RESET_SQL}"
DELETE FROM MANUAL_RERUN_CONTROL_ACTION_AUDIT
WHERE execution_key = '${RERUN_EXECUTION_KEY}';

DELETE FROM AGENT_EXECUTION_LOG
WHERE execution_key = '${RERUN_EXECUTION_KEY}'
   OR retry_source_execution_key = '${RERUN_EXECUTION_KEY}';

DELETE FROM WEBHOOK_EXECUTION
WHERE execution_key = '${RERUN_EXECUTION_KEY}'
   OR retry_source_execution_key = '${RERUN_EXECUTION_KEY}';
EOF
```

### 재시도 원본 실행 임시 복사본
```bash
export RETRY_SOURCE_SUFFIX="spec0030-retry-source-${VERIFY_TS}"
export RETRY_SOURCE_EXECUTION_KEY="EXECUTION:MANUAL_RERUN:${RETRY_SOURCE_SUFFIX}"
export RETRY_SOURCE_DELIVERY_ID="MANUAL_RERUN_DELIVERY:${RETRY_SOURCE_SUFFIX}"
export RETRY_APPLY_SQL="${RETRY_DIR}/retry-apply.sql"
export RETRY_RESET_SQL="${RETRY_DIR}/retry-reset.sql"

sed "s/example-retry-source/${RETRY_SOURCE_SUFFIX}/g" \
  src/test/resources/manual-rerun-response-seed/source-execution/retry-source-execution-seed.example.sql \
  > "${RETRY_APPLY_SQL}"

cat <<EOF > "${RETRY_RESET_SQL}"
DELETE FROM MANUAL_RERUN_CONTROL_ACTION_AUDIT
WHERE execution_key = '${RETRY_SOURCE_EXECUTION_KEY}'
   OR execution_key IN (
     SELECT execution_key
     FROM WEBHOOK_EXECUTION
     WHERE retry_source_execution_key = '${RETRY_SOURCE_EXECUTION_KEY}'
   );

DELETE FROM AGENT_EXECUTION_LOG
WHERE execution_key = '${RETRY_SOURCE_EXECUTION_KEY}'
   OR retry_source_execution_key = '${RETRY_SOURCE_EXECUTION_KEY}';

DELETE FROM WEBHOOK_EXECUTION
WHERE execution_key = '${RETRY_SOURCE_EXECUTION_KEY}'
   OR retry_source_execution_key = '${RETRY_SOURCE_EXECUTION_KEY}';
EOF
```

## 재실행 대표 검증 흐름
### 1. 준비 데이터 정리와 적용
```bash
OUTPUT_DIR="${RERUN_DIR}" \
APP_PORT="${APP_PORT}" \
SEED_RESET_SQL="${RERUN_RESET_SQL}" \
SEED_APPLY_SQL="${RERUN_APPLY_SQL}" \
JDBC_URL="${JDBC_URL}" \
H2_JAR="${H2_JAR}" \
./scripts/manual-rerun-response/prepare-seed.sh
```

### 2. 재실행 요청 흐름 실행
```bash
OUTPUT_DIR="${RERUN_DIR}" \
APP_PORT="${APP_PORT}" \
BASE_URL="${BASE_URL}" \
RERUN_EXECUTION_KEY="${RERUN_EXECUTION_KEY}" \
RERUN_ACTION_BODY='{"action":"UNACKNOWLEDGE","note":"대표 검증용 확인 해제"}' \
APP_START_TIMEOUT_SECONDS=60 \
./scripts/manual-rerun-response/run-rerun.sh
```

### 3. 앱 종료 뒤 실행 근거 수집
```bash
OUTPUT_DIR="${RERUN_DIR}" \
JDBC_URL="${JDBC_URL}" \
H2_JAR="${H2_JAR}" \
EVIDENCE_MODE=rerun \
RERUN_EXECUTION_KEY="${RERUN_EXECUTION_KEY}" \
./scripts/manual-rerun-response/collect-evidence.sh
```

## 재시도 대표 검증 흐름
### 1. 준비 데이터 정리와 적용
```bash
OUTPUT_DIR="${RETRY_DIR}" \
APP_PORT="${APP_PORT}" \
SEED_RESET_SQL="${RETRY_RESET_SQL}" \
SEED_APPLY_SQL="${RETRY_APPLY_SQL}" \
JDBC_URL="${JDBC_URL}" \
H2_JAR="${H2_JAR}" \
./scripts/manual-rerun-response/prepare-seed.sh
```

### 2. 재시도 요청 흐름 실행
```bash
OUTPUT_DIR="${RETRY_DIR}" \
APP_PORT="${APP_PORT}" \
BASE_URL="${BASE_URL}" \
RETRY_SOURCE_EXECUTION_KEY="${RETRY_SOURCE_EXECUTION_KEY}" \
RETRY_REQUEST_BODY='{"executionControlMode":"DRY_RUN"}' \
APP_START_TIMEOUT_SECONDS=60 \
./scripts/manual-rerun-response/run-retry.sh
```

### 3. 파생 실행 키 읽기
```bash
export RETRY_DERIVED_EXECUTION_KEY="$(cat "${RETRY_DIR}/retry-derived-execution-key.txt")"
printf '%s\n' "${RETRY_DERIVED_EXECUTION_KEY}"
```

### 4. 앱 종료 뒤 실행 근거 수집
```bash
OUTPUT_DIR="${RETRY_DIR}" \
JDBC_URL="${JDBC_URL}" \
H2_JAR="${H2_JAR}" \
EVIDENCE_MODE=retry \
RETRY_DERIVED_EXECUTION_KEY="${RETRY_DERIVED_EXECUTION_KEY}" \
./scripts/manual-rerun-response/collect-evidence.sh
```

## 어떤 실행 키를 어디에 다시 쓰는가
- 재실행 대표 검증
  - `RERUN_EXECUTION_KEY`를 준비 데이터 적용, 단건 조회, 이력 조회, 관리자 조치, 조치 후 단건 조회, H2 조회까지 그대로 쓴다.
  - `RERUN_DELIVERY_ID`는 회고에 함께 남기는 대표 전달 식별자다.
- 재시도 대표 검증
  - `RETRY_SOURCE_EXECUTION_KEY`는 준비 데이터 적용과 retry 요청 시작점에 쓴다.
  - 재시도 응답 뒤에는 `retry-derived-execution-key.txt`에 저장된 `RETRY_DERIVED_EXECUTION_KEY`를 단건 조회와 H2 조회 기준으로 다시 쓴다.
  - 파생 실행 단건 조회 응답에는 원본 실행 키 필드인 `retrySourceExecutionKey`가 없으므로, 원본 실행과 파생 실행의 연결은 재시도 응답과 H2 `WEBHOOK_EXECUTION` 행에서 다시 확인한다.
  - `RETRY_SOURCE_DELIVERY_ID`는 원본 실행 전달 식별자다.
- 즉 재시도는 원본 실행 키와 파생 실행 키가 서로 다르고, 대표 검증 결론은 파생 실행 키 기준으로 닫는다.

## 출력 파일과 수동 확인 포인트
| 파일 | 남는 출력 | 사람이 먼저 확인할 것 |
| --- | --- | --- |
| `prepare-seed.sh` | `prepare.log` | 포트 충돌, H2 도구 중복, 정리/적용 SQL 실패 여부 |
| `run-rerun.sh` | `rerun-query-before.json`, `rerun-history.json`, `rerun-action.json`, `rerun-query-after.json`, `app.pid` | 어떤 응답 파일까지 성공했고 어디서 끊겼는지 |
| `run-retry.sh` | `retry-response.json`, `retry-derived-execution-key.txt`, `retry-derived-query.json`, `app.pid` | 파생 실행 키가 실제로 남았는지, 마지막 응답 파일이 무엇인지 |
| `collect-evidence.sh` | `rerun-webhook-execution.txt`, `rerun-action-audit.txt`, `retry-webhook-execution.txt`, `retry-agent-execution-log.txt` | 앱 종료 여부, H2 잠금 여부, 어떤 H2 조회 파일까지 남았는지 |

## 종료 코드와 중단 조건
| 파일 | 중단 상황 | 종료 코드 |
| --- | --- | --- |
| `prepare-seed.sh` | 포트 충돌, H2 도구 중복, 정리 SQL 실패, 준비 데이터 적용 SQL 실패 | `10`, `11`, `12`, `13` |
| `run-rerun.sh` | 앱 기동 실패, 단건 조회 실패, 이력 조회 실패, 관리자 조치 실패, 조치 후 단건 조회 실패 | `20`, `21`, `22`, `23`, `24` |
| `run-retry.sh` | 앱 기동 실패, 재시도 요청 실패, 파생 실행 키 추출 실패, 파생 단건 조회 실패 | `30`, `31`, `32`, `33` |
| `collect-evidence.sh` | 앱 종료 미확인, H2 조회 실패, H2 잠금 의심 | `40`, `41`, `42` |

- `collect-evidence.sh`가 `42`를 반환하면 바로 코드 문제로 단정하지 않고, H2 잠금 시그니처와 동시 조회 여부를 먼저 본다.
- 실제 앱/H2 대표 검증은 위 종료 코드가 `0`인 상태와 남은 출력 파일을 함께 보고 닫는다.

## 계속 수동으로 남는 확인 단계
- 재실행 단건 조회, 조치 응답, 이력 응답, 조치 후 단건 조회와 재시도 응답, 파생 단건 조회의 의미 비교
- `availableActions`, `failureDisposition`, `currentActionState`, `retrySourceExecutionKey` 같은 필드 해석
- H2 `WEBHOOK_EXECUTION`, `AGENT_EXECUTION_LOG`, `MANUAL_RERUN_CONTROL_ACTION_AUDIT` 결과 의미 해석
- H2 잠금 오류인지, 실행 순서 문제인지, 코드 문제인지 구분
- 회고와 제안 필요 여부 판단

## 현재 단계 판단
- 현재 단계 기준으로는 실제 초안 파일 네 개와 실제 앱/H2 대표 검증 흐름이 모두 연결됐다.
- 즉 명령 실행은 초안 파일로 다시 수행할 수 있고, 사람은 응답 의미 해석과 H2 결과 비교, 회고 작성에 집중하면 된다.
- 다음 단계는 초안 파일 자체를 더 늘리는 것보다, 이 초안 파일을 더 쉽게 적용하고 유지할 수 있게 정리하는 쪽이 맞다. 현재 단계에서는 이 문장을 시작 안전망 근거로만 사용하고, 실제 활성 단계 이름과 작업 범위는 `.agents/active/spec.md`, `.agents/active/tasks.md`를 기준으로 다시 확인한다.
