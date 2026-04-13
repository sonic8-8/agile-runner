#!/usr/bin/env bash

set -u

OUTPUT_DIR="${OUTPUT_DIR:-}"
APP_PORT="${APP_PORT:-}"
SEED_RESET_SQL="${SEED_RESET_SQL:-}"
SEED_APPLY_SQL="${SEED_APPLY_SQL:-}"
JDBC_URL="${JDBC_URL:-}"
H2_JAR="${H2_JAR:-}"

ensure_required() {
  local name="$1"
  local value="$2"
  if [[ -n "${value}" ]]; then
    return 0
  fi

  printf '필수 환경 변수가 비어 있습니다: %s\n' "${name}" >&2
  exit 1
}

ensure_required "OUTPUT_DIR" "${OUTPUT_DIR}"
ensure_required "APP_PORT" "${APP_PORT}"
ensure_required "SEED_RESET_SQL" "${SEED_RESET_SQL}"
ensure_required "SEED_APPLY_SQL" "${SEED_APPLY_SQL}"
ensure_required "JDBC_URL" "${JDBC_URL}"
ensure_required "H2_JAR" "${H2_JAR}"

mkdir -p "${OUTPUT_DIR}"
PREPARE_LOG="${OUTPUT_DIR}/prepare.log"
: > "${PREPARE_LOG}"

log() {
  printf '%s\n' "$1" | tee -a "${PREPARE_LOG}"
}

run_h2_script() {
  local script_path="$1"
  java -cp "${H2_JAR}" org.h2.tools.RunScript \
    -url "${JDBC_URL}" \
    -user sa \
    -script "${script_path}" >> "${PREPARE_LOG}" 2>&1
}

if lsof -i :"${APP_PORT}" >> "${PREPARE_LOG}" 2>&1; then
  log "시작 전 포트 충돌"
  exit 10
fi

if pgrep -af "org.h2.tools.RunScript|org.h2.tools.Shell" >> "${PREPARE_LOG}" 2>&1; then
  log "H2 명령줄 도구 중복 실행"
  exit 11
fi

log "정리 SQL 실행 시작"
if ! run_h2_script "${SEED_RESET_SQL}"; then
  log "정리 SQL 실행 실패"
  exit 12
fi

log "준비 데이터 적용 SQL 실행 시작"
if ! run_h2_script "${SEED_APPLY_SQL}"; then
  log "준비 데이터 적용 SQL 실행 실패"
  exit 13
fi

log "준비 데이터 적용 완료"
