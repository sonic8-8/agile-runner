# 운영용 조회 응답 준비 데이터 적용 보조 명령 가이드

## 문서 목적
이 문서는 운영용 조회 응답 대표 검증 전에 준비 데이터 SQL을 적용하고 정리할 때 반복해서 쓰는 기본 명령 예시를 정리한다.
현재 단계에서는 `rerun` 준비 실행과 `retry` 원본 실행을 로컬 H2 파일 DB에 넣거나 정리하는 명령, 그리고 앱 기동 전후 경계를 더 빠르게 따라갈 수 있게 만드는 데 집중한다.

## 이 문서를 먼저 읽어야 하는 경우
- 대표 검증 전에 어떤 명령부터 실행해야 하는지 바로 감이 안 잡힐 때
- 준비 데이터 SQL 파일은 준비됐지만 로컬 H2에 어떤 순서로 적용해야 하는지 다시 확인하고 싶을 때
- 기존 예시 결과 행이나 이전 대표 검증 실행 흔적을 어디까지 정리해야 하는지 헷갈릴 때

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
- 이 문서 범위는 앱 기동 직전 준비까지다. 요청 실행 이후 단계는 다음 문서에서 다룬다.

## rerun acknowledge 상태 준비 명령
```bash
java -cp "${H2_JAR}" org.h2.tools.RunScript \
  -url "${JDBC_URL}" \
  -user sa \
  -script src/test/resources/manual-rerun-response-seed/control-action-history/rerun-acknowledge-action-history-seed.example.sql
```

- 이 명령은 `rerun-acknowledge`, `rerun-unacknowledge` 대표 검증을 시작하기 전에 실행한다.
- 이 문서 범위는 앱 기동 직전 준비까지다. 요청 실행 이후 단계는 다음 문서에서 다룬다.

## 앱 기동 명령
```bash
SPRING_PROFILES_ACTIVE=local \
SERVER_PORT="${APP_PORT}" \
./scripts/gradlew-java21.sh bootRun --console=plain
```

- 이 문서는 앱 기동까지 다룬다.
- 요청 실행 이후 명령, 실행 키 추출, H2 조회, 앱 종료 후 확인 명령은 다음 단계 문서에서 다룬다.

## 대표 검증 시나리오별 시작 순서
### retry
1. 시작 전 확인 명령 실행
2. 공통 정리 명령 실행
3. retry 원본 실행 준비 명령 실행
4. 앱 기동
5. 여기서 멈춘다. 다음 단계에서 retry 호출과 파생 실행 키 추출을 다룬다.

### rerun-acknowledge / rerun-unacknowledge
1. 시작 전 확인 명령 실행
2. 공통 정리 명령 실행
3. rerun acknowledge 상태 준비 명령 실행
4. 앱 기동
5. 여기서 멈춘다. 다음 단계에서 단건 조회, 이력 조회, 관리자 조치 흐름을 다룬다.

## 이 문서에서 아직 하지 않는 것
- retry 파생 실행 키 추출 명령
- H2 실행 근거 조회 명령
- 대표 검증 결과와 H2 실행 근거를 같은 실행 키 기준으로 묶는 조회 절차

이 범위는 다음 단계에서 정리한다.
