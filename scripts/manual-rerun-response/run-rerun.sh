#!/usr/bin/env bash

set -u

OUTPUT_DIR="${OUTPUT_DIR:-}"
APP_PORT="${APP_PORT:-}"
BASE_URL="${BASE_URL:-}"
RERUN_EXECUTION_KEY="${RERUN_EXECUTION_KEY:-}"
RERUN_ACTION_BODY="${RERUN_ACTION_BODY:-}"
APP_START_CMD="${APP_START_CMD:-SPRING_PROFILES_ACTIVE=local SERVER_PORT=${APP_PORT} ./scripts/gradlew-java21.sh bootRun --console=plain}"
APP_START_TIMEOUT_SECONDS="${APP_START_TIMEOUT_SECONDS:-20}"

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
ensure_required "BASE_URL" "${BASE_URL}"
ensure_required "RERUN_EXECUTION_KEY" "${RERUN_EXECUTION_KEY}"
ensure_required "RERUN_ACTION_BODY" "${RERUN_ACTION_BODY}"

mkdir -p "${OUTPUT_DIR}"
RUN_LOG="${OUTPUT_DIR}/run-rerun.log"
APP_PID_FILE="${OUTPUT_DIR}/app.pid"
RERUN_QUERY_BEFORE_FILE="${OUTPUT_DIR}/rerun-query-before.json"
RERUN_HISTORY_FILE="${OUTPUT_DIR}/rerun-history.json"
RERUN_ACTION_FILE="${OUTPUT_DIR}/rerun-action.json"
RERUN_QUERY_AFTER_FILE="${OUTPUT_DIR}/rerun-query-after.json"
: > "${RUN_LOG}"

log() {
  printf '%s\n' "$1" | tee -a "${RUN_LOG}"
}

is_process_alive() {
  local pid="$1"
  if ! kill -0 "${pid}" >/dev/null 2>&1; then
    return 1
  fi

  local stat
  stat="$(ps -o stat= -p "${pid}" 2>/dev/null | tr -d '[:space:]')"
  if [[ -z "${stat}" ]]; then
    return 1
  fi

  if [[ "${stat}" == Z* ]]; then
    return 1
  fi

  return 0
}

start_app() {
  bash -lc "${APP_START_CMD}" >> "${RUN_LOG}" 2>&1 &
  APP_PID=$!
  printf '%s\n' "${APP_PID}" > "${APP_PID_FILE}"
}

wait_for_port() {
  local remaining="${APP_START_TIMEOUT_SECONDS}"
  while [[ "${remaining}" -gt 0 ]]; do
    if ! is_process_alive "${APP_PID}"; then
      return 1
    fi
    if bash -lc "</dev/tcp/127.0.0.1/${APP_PORT}" >/dev/null 2>&1; then
      return 0
    fi
    sleep 1
    remaining=$((remaining - 1))
  done
  return 1
}

curl_to_file() {
  local output_file="$1"
  shift
  curl -sS --fail-with-body "$@" -o "${output_file}" >> "${RUN_LOG}" 2>&1
}

start_app
log "앱 기동 시작"
if ! wait_for_port; then
  log "앱 기동 시간 안에 포트 확인 실패"
  exit 20
fi

log "재실행 단건 조회 시작"
if ! curl_to_file "${RERUN_QUERY_BEFORE_FILE}" "${BASE_URL}/reviews/rerun/${RERUN_EXECUTION_KEY}"; then
  log "재실행 단건 조회 실패"
  exit 21
fi

log "재실행 이력 조회 시작"
if ! curl_to_file "${RERUN_HISTORY_FILE}" "${BASE_URL}/reviews/rerun/${RERUN_EXECUTION_KEY}/actions/history"; then
  log "재실행 이력 조회 실패"
  exit 22
fi

log "재실행 관리자 조치 시작"
if ! curl_to_file \
  "${RERUN_ACTION_FILE}" \
  -X POST \
  -H 'Content-Type: application/json' \
  -d "${RERUN_ACTION_BODY}" \
  "${BASE_URL}/reviews/rerun/${RERUN_EXECUTION_KEY}/actions"; then
  log "재실행 관리자 조치 실패"
  exit 23
fi

log "재실행 조치 후 단건 조회 시작"
if ! curl_to_file "${RERUN_QUERY_AFTER_FILE}" "${BASE_URL}/reviews/rerun/${RERUN_EXECUTION_KEY}"; then
  log "재실행 조치 후 단건 조회 실패"
  exit 24
fi

log "재실행 요청 흐름 완료"
