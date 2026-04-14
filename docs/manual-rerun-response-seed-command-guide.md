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
- 실제 초안 파일 기준 대표 검증이 어떻게 맞았는지는 [TASK-0004-script-application-representative-verified.md](/home/seaung13/workspace/agile-runner/.agents/outer-loop/retrospectives/SPEC-0031/TASK-0004-script-application-representative-verified.md) 를 참고한다.

## 시작 전에 먼저 확인할 근거
- 직전 단계 전체 판단은 [SPEC-0031-summary.md](/home/seaung13/workspace/agile-runner/.agents/outer-loop/retrospectives/SPEC-0031/SPEC-0031-summary.md) 에서 먼저 본다.
- 마지막 대표 검증에서 어떤 실행 키, 전달 식별자, 출력 파일을 함께 남겼는지는 [TASK-0004-script-application-representative-verified.md](/home/seaung13/workspace/agile-runner/.agents/outer-loop/retrospectives/SPEC-0031/TASK-0004-script-application-representative-verified.md) 에서 먼저 본다.
- 현재 자동 검증 안전망은 아래 테스트가 맡는다.
  - [ManualRerunRunFlowScriptTest.java](/home/seaung13/workspace/agile-runner/src/test/java/com/agilerunner/client/agentruntime/ManualRerunRunFlowScriptTest.java)
  - [ManualRerunSeedCommandScriptTest.java](/home/seaung13/workspace/agile-runner/src/test/java/com/agilerunner/client/agentruntime/ManualRerunSeedCommandScriptTest.java)
  - [ManualRerunResponseSeedSqlTest.java](/home/seaung13/workspace/agile-runner/src/test/java/com/agilerunner/client/agentruntime/ManualRerunResponseSeedSqlTest.java)
  - [ManualRerunResponseSeedEvidenceSqlTest.java](/home/seaung13/workspace/agile-runner/src/test/java/com/agilerunner/client/agentruntime/ManualRerunResponseSeedEvidenceSqlTest.java)
- 위 근거와 테스트가 그대로 유효하면, 다음 단계는 새 안전망을 만드는 대신 현재 가이드 정리부터 시작해도 된다.


## 변경 시 함께 갱신해야 하는 문서와 기준 파일
| 변경한 대상 | 같이 갱신할 문서 또는 기준 파일 | 같이 확인할 자동 검증 | 이유 |
| --- | --- | --- | --- |
| `prepare-seed.sh` 또는 준비 데이터 적용 방식 | 이 문서의 `빠른 적용 순서`, [manual-rerun-response-seed-guide.md](/home/seaung13/workspace/agile-runner/docs/manual-rerun-response-seed-guide.md), 준비 데이터 예시 SQL | [ManualRerunSeedCommandScriptTest.java](/home/seaung13/workspace/agile-runner/src/test/java/com/agilerunner/client/agentruntime/ManualRerunSeedCommandScriptTest.java), [ManualRerunResponseSeedSqlTest.java](/home/seaung13/workspace/agile-runner/src/test/java/com/agilerunner/client/agentruntime/ManualRerunResponseSeedSqlTest.java), [ManualRerunResponseSeedEvidenceSqlTest.java](/home/seaung13/workspace/agile-runner/src/test/java/com/agilerunner/client/agentruntime/ManualRerunResponseSeedEvidenceSqlTest.java) | 준비 데이터 적용 순서와 SQL 예시, H2 조회 근거가 함께 어긋나기 쉽다. |
| `run-rerun.sh`, `run-retry.sh`, `collect-evidence.sh` | 이 문서의 `빠른 적용 순서`, `대표 검증 결과를 읽는 순서`, [manual-rerun-response-guide.md](/home/seaung13/workspace/agile-runner/docs/manual-rerun-response-guide.md), 마지막 대표 검증 회고 | [ManualRerunRunFlowScriptTest.java](/home/seaung13/workspace/agile-runner/src/test/java/com/agilerunner/client/agentruntime/ManualRerunRunFlowScriptTest.java) | 요청 순서, 출력 파일, H2 근거 수집 방식이 같이 변하므로 한 파일만 바꾸면 의미가 끊긴다. |
| 응답 해석 기준 또는 실행 키 읽는 위치 | [manual-rerun-response-guide.md](/home/seaung13/workspace/agile-runner/docs/manual-rerun-response-guide.md), 이 문서의 `대표 검증 결과를 읽는 순서`, 마지막 대표 검증 회고 | controller/service 관련 응답 테스트, [ManualRerunRunFlowScriptTest.java](/home/seaung13/workspace/agile-runner/src/test/java/com/agilerunner/client/agentruntime/ManualRerunRunFlowScriptTest.java) | 응답 의미가 바뀌면 보조 명령 가이드의 읽는 순서와 마지막 판단 근거도 함께 달라진다. |

## 문서와 기준 파일 역할 경계
| 파일 | 먼저 바꿔야 하는 경우 | 이 파일만 바꾸면 안 되는 경우 |
| --- | --- | --- |
| [manual-rerun-response-seed-command-guide.md](/home/seaung13/workspace/agile-runner/docs/manual-rerun-response-seed-command-guide.md) | 적용 순서, 입력·출력 흐름, 출력 파일 읽는 순서가 달라질 때 | 스크립트나 출력 파일 이름이 바뀌었는데 회고, 응답 가이드, 자동 검증은 그대로 둘 때 |
| [manual-rerun-response-guide.md](/home/seaung13/workspace/agile-runner/docs/manual-rerun-response-guide.md) | 응답 필드 의미, 현재 상태 해석, 조치 응답 읽는 기준이 달라질 때 | 보조 명령 가이드의 읽는 순서와 대표 검증 회고를 그대로 둘 때 |
| `src/test/java/com/agilerunner/client/agentruntime/` 아래 스크립트 관련 테스트 | 스크립트 입력, 출력 파일 이름, 실행 키 추출, H2 조회 방식이 달라질 때 | 문서만 바꾸고 자동 검증 기대값은 그대로 둘 때 |
| 마지막 대표 검증 회고와 단계 요약 | 대표 검증 결과 파일 경로, 대표 실행 키, 전달 식별자, 마지막 판단 문장이 달라질 때 | 새 기준을 문서에만 적고 마지막 실제 근거 문서는 갱신하지 않을 때 |

여기까지가 이번 작업에서 직접 정리한 범위다. 아래의 `빠른 적용 순서`, `대표 검증 결과를 읽는 순서`, `파일별 역할과 마지막 판단 기준`은 기존 참고 기준으로 유지하고, 출력 파일 이름 변경과 어긋남 감지 기준은 다음 작업에서 따로 닫는다.

## 빠른 적용 순서
### 재실행 대표 검증
1. 공통 환경 변수와 출력 디렉토리를 먼저 잡는다.
2. 재실행용 임시 SQL 복사본과 정리 SQL을 만든다.
3. `prepare-seed.sh`로 정리 SQL과 적용 SQL을 실행한다.
4. `run-rerun.sh`로 앱 기동, 단건 조회, 이력 조회, 관리자 조치, 조치 후 단건 조회를 한 번에 실행한다.
5. `collect-evidence.sh`로 앱 종료 뒤 H2 실행 근거를 모은다.
6. `rerun-query-before.json`, `rerun-history.json`, `rerun-action.json`, `rerun-query-after.json`, `rerun-webhook-execution.txt`, `rerun-action-audit.txt`를 같은 실행 키 기준으로 읽는다.

### 재시도 대표 검증
1. 공통 환경 변수와 출력 디렉토리를 먼저 잡는다.
2. 재시도 원본 실행용 임시 SQL 복사본과 정리 SQL을 만든다.
3. `prepare-seed.sh`로 정리 SQL과 적용 SQL을 실행한다.
4. `run-retry.sh`로 앱 기동, 재시도 요청, 파생 실행 키 추출, 파생 실행 단건 조회를 한 번에 실행한다.
5. `retry-derived-execution-key.txt`에서 파생 실행 키를 다시 읽어 환경 변수로 잡는다.
6. `collect-evidence.sh`로 앱 종료 뒤 파생 실행 기준 H2 실행 근거를 모은다.
7. `retry-response.json`, `retry-derived-query.json`, `retry-webhook-execution.txt`, `retry-agent-execution-log.txt`를 파생 실행 키 기준으로 읽고, 원본 실행 연결은 `retrySourceExecutionKey`와 `WEBHOOK_EXECUTION.retry_source_execution_key`에서 다시 확인한다.

## 빠른 적용 순서에서 다시 쓰는 입력, 출력, 다음 단계 입력
| 단계 | 바로 넣는 입력 | 바로 남는 출력 | 다음 단계에서 다시 쓰는 값 |
| --- | --- | --- | --- |
| 공통 환경 준비 | `APP_PORT`, `BASE_URL`, `JDBC_URL`, `H2_JAR`, `VERIFY_TS`, `OUTPUT_ROOT` | `RERUN_DIR`, `RETRY_DIR` | 재실행/재시도 모든 단계 |
| 재실행 임시 SQL 준비 | `RERUN_SUFFIX`, 예시 SQL 경로 | `rerun-apply.sql`, `rerun-reset.sql`, `RERUN_EXECUTION_KEY`, `RERUN_DELIVERY_ID` | `prepare-seed.sh`, `run-rerun.sh`, `collect-evidence.sh` |
| 재시도 임시 SQL 준비 | `RETRY_SOURCE_SUFFIX`, 예시 SQL 경로 | `retry-apply.sql`, `retry-reset.sql`, `RETRY_SOURCE_EXECUTION_KEY`, `RETRY_SOURCE_DELIVERY_ID` | `prepare-seed.sh`, `run-retry.sh` |
| `prepare-seed.sh` | `OUTPUT_DIR`, `SEED_RESET_SQL`, `SEED_APPLY_SQL`, `JDBC_URL`, `H2_JAR` | `prepare.log` | 다음 단계가 정리/적용 성공 여부를 여기서 먼저 확인 |
| `run-rerun.sh` | `OUTPUT_DIR`, `BASE_URL`, `RERUN_EXECUTION_KEY`, `RERUN_ACTION_BODY` | `rerun-query-before.json`, `rerun-history.json`, `rerun-action.json`, `rerun-query-after.json`, `app.pid` | `collect-evidence.sh`, 수동 의미 확인 |
| `run-retry.sh` | `OUTPUT_DIR`, `BASE_URL`, `RETRY_SOURCE_EXECUTION_KEY`, `RETRY_REQUEST_BODY` | `retry-response.json`, `retry-derived-execution-key.txt`, `retry-derived-query.json`, `app.pid` | 파생 실행 키 추출, `collect-evidence.sh`, 수동 의미 확인 |
| 파생 실행 키 다시 읽기 | `retry-derived-execution-key.txt` | `RETRY_DERIVED_EXECUTION_KEY` | `collect-evidence.sh`, H2 조회 해석 |
| `collect-evidence.sh` 재실행 | `OUTPUT_DIR`, `EVIDENCE_MODE=rerun`, `RERUN_EXECUTION_KEY` | `rerun-webhook-execution.txt`, `rerun-action-audit.txt` | 최종 수동 판단 |
| `collect-evidence.sh` 재시도 | `OUTPUT_DIR`, `EVIDENCE_MODE=retry`, `RETRY_DERIVED_EXECUTION_KEY` | `retry-webhook-execution.txt`, `retry-agent-execution-log.txt` | 최종 수동 판단 |

여기까지가 새 작업자가 적용 순서와 입력/출력 흐름을 다시 잡을 때 먼저 보는 기준이다.
실행 키 재사용 기준, 출력 파일 해석 기준, 종료 코드와 마감 판단은 아래 참고 섹션에 남기고, 이 문서에서는 위치를 빠르게 찾게 만드는 데까지만 정리한다.

## 대표 검증 결과를 읽는 순서
### 재실행 대표 검증
| 순서 | 먼저 여는 파일 | 여기서 먼저 확인하는 것 | 같이 대조할 문서나 파일 | 마지막에 닫는 판단 |
| --- | --- | --- | --- | --- |
| 1 | `prepare.log` | 정리 SQL과 적용 SQL이 정상 종료됐는지 | 없음 | 준비 단계 통과 여부 |
| 2 | `rerun-query-before.json` | 조치 전 현재 상태와 `availableActions` | [manual-rerun-response-guide.md](/home/seaung13/workspace/agile-runner/docs/manual-rerun-response-guide.md)의 `query` 설명 | 조치 전 현재 상태가 기대와 맞는지 |
| 3 | `rerun-history.json`, `rerun-action.json`, `rerun-query-after.json` | 조치 직후 상태가 어떻게 바뀌었는지 | 응답 가이드의 `history`, `action`, `query` 설명 | 조치 후 응답 의미가 서로 맞는지 |
| 4 | `rerun-webhook-execution.txt`, `rerun-action-audit.txt` | H2 실행 근거와 audit row가 남았는지 | 위 응답 파일과 같은 실행 키 | 응답과 H2가 같은 실행을 설명하는지 |
| 5 | `TASK-0004-script-application-representative-verified.md` | 실제 대표 검증에서 어떤 결론으로 닫았는지 | 위 출력 파일 전부 | 최종 사람 판단 근거 |

### 재시도 대표 검증
| 순서 | 먼저 여는 파일 | 여기서 먼저 확인하는 것 | 같이 대조할 문서나 파일 | 마지막에 닫는 판단 |
| --- | --- | --- | --- | --- |
| 1 | `prepare.log` | 정리 SQL과 적용 SQL이 정상 종료됐는지 | 없음 | 준비 단계 통과 여부 |
| 2 | `retry-response.json` | 원본 실행 기준 재시도 응답과 파생 실행 키 | 응답 가이드의 `retry` 설명 | 어떤 파생 실행을 이후 단계에서 계속 볼지 |
| 3 | `retry-derived-execution-key.txt`, `retry-derived-query.json` | 파생 실행 키와 파생 실행 현재 상태 | 응답 가이드의 `query` 설명 | 파생 실행 응답 의미가 기대와 맞는지 |
| 4 | `retry-webhook-execution.txt`, `retry-agent-execution-log.txt` | H2에서 파생 실행과 원본 실행 연결이 보이는지 | `retry-response.json`, `retrySourceExecutionKey` | 원본 실행과 파생 실행 연결이 맞는지 |
| 5 | `TASK-0004-script-application-representative-verified.md` | 실제 대표 검증에서 어떤 결론으로 닫았는지 | 위 출력 파일 전부 | 최종 사람 판단 근거 |

## 파일별 역할과 마지막 판단 기준
| 파일 또는 문서 | 기본 역할 | 이 파일만으로 끝내면 안 되는 이유 | 마지막 판단에서 맡는 위치 |
| --- | --- | --- | --- |
| `summary.json` | 대표 검증 결과를 기계적으로 다시 읽기 위한 요약 | 내부 키와 파생 값이 많아 사람 설명 없이 읽기 어렵다 | 빠른 인덱스 |
| [manual-rerun-response-guide.md](/home/seaung13/workspace/agile-runner/docs/manual-rerun-response-guide.md) | 각 응답이 어떤 질문에 답하는지 설명 | 실제 대표 검증에서 어떤 파일이 남았는지 직접 보여 주지는 않는다 | 응답 의미 해석 기준 |
| `prepare.log`, `rerun-*.json`, `retry-*.json`, `*-webhook-execution.txt`, `*-action-audit.txt`, `*-agent-execution-log.txt` | 대표 검증에서 실제로 남은 원시 근거 | 파일이 흩어져 있어 읽는 순서 없이 보면 해석 비용이 높다 | 실행별 직접 근거 |
| `TASK-0004-script-application-representative-verified.md` | 대표 검증에서 어떤 실행 키, 전달 식별자, 결론으로 닫았는지 설명 | 새 대표 검증을 다시 돌리면 값은 달라질 수 있다 | 최종 사람 판단 근거 |

## 공통 전제
- 앱은 `local` 프로필 기준으로 띄운다.
- 준비 데이터 정리와 적용은 앱 기동 전에 마친다.
- 실제 앱/H2 대표 검증은 `준비 데이터 정리/적용 -> 앱 기동 -> 요청 실행 -> 앱 종료 -> H2 조회` 순서로 진행한다.
- H2 파일 DB는 앱 종료 뒤에만 외부에서 조회한다.
- 같은 H2 파일을 여러 셸에서 동시에 열지 않는다.
- 실제 앱/H2 대표 검증은 기존 예시 키를 그대로 재사용하지 않고, 임시 SQL 복사본에 새 접미사를 넣어 새 실행 키와 전달 식별자를 만든다.

## 참고: 스크립트 책임과 수동 확인 경계
| 단계 | 주 입력 | 주 출력 | 현재 담당 | 사람이 직접 확인할 것 |
| --- | --- | --- | --- | --- |
| 시작 전 포트/H2 프로세스 확인과 준비 데이터 적용 | `APP_PORT`, `JDBC_URL`, `H2_JAR`, 정리/적용 SQL | `prepare.log`, 정리/적용된 H2 상태 | `prepare-seed.sh` | 선택한 SQL 파일 경로와 시나리오 일치 여부 |
| 재실행 대표 요청 실행 | `BASE_URL`, `RERUN_EXECUTION_KEY`, `RERUN_ACTION_BODY` | `rerun-query-before.json`, `rerun-history.json`, `rerun-action.json`, `rerun-query-after.json`, `app.pid` | `run-rerun.sh` | 응답 의미 비교 |
| 재시도 대표 요청 실행 | `BASE_URL`, `RETRY_SOURCE_EXECUTION_KEY`, `RETRY_REQUEST_BODY` | `retry-response.json`, `retry-derived-execution-key.txt`, `retry-derived-query.json`, `app.pid` | `run-retry.sh` | 재시도 응답과 파생 실행 의미 비교 |
| 앱 종료 뒤 실행 근거 수집 | `JDBC_URL`, `H2_JAR`, `EVIDENCE_MODE`, 실행 키 | 재실행 또는 재시도 H2 조회 결과 텍스트 | `collect-evidence.sh` | H2 결과 의미 해석 |
| 응답과 H2 결과 결론 정리 | 응답 파일, H2 조회 결과, 회고 경로 | 대표 검증 결론 | 수동 확인 단계 | 응답과 H2 의미 일치 여부, 회고 작성 |

- 초안 파일은 명령 실행과 결과 파일 저장까지 맡는다.
- 대표 검증 결론, H2 결과 의미 해석, 회고와 제안 필요 여부 판단은 계속 사람이 직접 닫는다.

## 기존 절차 참고

### 공통 환경 변수 예시
```bash
export APP_PORT=18080
export BASE_URL="http://localhost:${APP_PORT}"
export H2_DB_PATH="$HOME/.agile-runner/agent-runtime/agile-runner"
export JDBC_URL="jdbc:h2:file:${H2_DB_PATH};MODE=PostgreSQL;DB_CLOSE_ON_EXIT=FALSE"
export H2_JAR="$(find "$HOME/.gradle/caches/modules-2/files-2.1/com.h2database/h2" -name 'h2-*.jar' | sort | tail -n 1)"
export VERIFY_TS="$(date +%Y%m%d-%H%M%S)"
export OUTPUT_ROOT="$PWD/.tmp/manual-rerun-response-${VERIFY_TS}"
export RERUN_DIR="${OUTPUT_ROOT}/rerun"
export RETRY_DIR="${OUTPUT_ROOT}/retry"
mkdir -p "${RERUN_DIR}" "${RETRY_DIR}"
```

### 대표 검증용 임시 SQL 복사본 만들기
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

### 재실행 대표 검증 흐름
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

### 재시도 대표 검증 흐름
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

## 참고: 어떤 실행 키를 어디에 다시 쓰는가
- 재실행 대표 검증
  - `RERUN_EXECUTION_KEY`를 준비 데이터 적용, 단건 조회, 이력 조회, 관리자 조치, 조치 후 단건 조회, H2 조회까지 그대로 쓴다.
  - `RERUN_DELIVERY_ID`는 회고에 함께 남기는 대표 전달 식별자다.
- 재시도 대표 검증
  - `RETRY_SOURCE_EXECUTION_KEY`는 준비 데이터 적용과 retry 요청 시작점에 쓴다.
  - 재시도 응답 뒤에는 `retry-derived-execution-key.txt`에 저장된 `RETRY_DERIVED_EXECUTION_KEY`를 단건 조회와 H2 조회 기준으로 다시 쓴다.
  - 파생 실행 단건 조회 응답에는 원본 실행 키 필드인 `retrySourceExecutionKey`가 없으므로, 원본 실행과 파생 실행의 연결은 재시도 응답과 H2 `WEBHOOK_EXECUTION` 행에서 다시 확인한다.
  - `RETRY_SOURCE_DELIVERY_ID`는 원본 실행 전달 식별자다.
- 즉 재시도는 원본 실행 키와 파생 실행 키가 서로 다르고, 대표 검증 결론은 파생 실행 키 기준으로 닫는다.

## 참고: 출력 파일과 수동 확인 포인트
| 파일 | 남는 출력 | 사람이 먼저 확인할 것 |
| --- | --- | --- |
| `prepare-seed.sh` | `prepare.log` | 포트 충돌, H2 도구 중복, 정리/적용 SQL 실패 여부 |
| `run-rerun.sh` | `rerun-query-before.json`, `rerun-history.json`, `rerun-action.json`, `rerun-query-after.json`, `app.pid` | 어떤 응답 파일까지 성공했고 어디서 끊겼는지 |
| `run-retry.sh` | `retry-response.json`, `retry-derived-execution-key.txt`, `retry-derived-query.json`, `app.pid` | 파생 실행 키가 실제로 남았는지, 마지막 응답 파일이 무엇인지 |
| `collect-evidence.sh` | `rerun-webhook-execution.txt`, `rerun-action-audit.txt`, `retry-webhook-execution.txt`, `retry-agent-execution-log.txt` | 앱 종료 여부, H2 잠금 여부, 어떤 H2 조회 파일까지 남았는지 |

## 참고: 종료 코드와 중단 조건
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

## 참고: 마지막 확인 포인트
- 이 문서 상단만 보면 적용 순서와 입력/출력 흐름을 다시 잡을 수 있다.
- 응답 의미 비교와 H2 결과 해석이 필요하면 아래 참고 섹션과 [manual-rerun-response-guide.md](/home/seaung13/workspace/agile-runner/docs/manual-rerun-response-guide.md), [TASK-0004-script-application-representative-verified.md](/home/seaung13/workspace/agile-runner/.agents/outer-loop/retrospectives/SPEC-0031/TASK-0004-script-application-representative-verified.md) 를 함께 본다.
