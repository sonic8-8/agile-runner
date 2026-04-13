#!/usr/bin/env bash

set -u

OUTPUT_DIR="${OUTPUT_DIR:-}"
APP_PORT="${APP_PORT:-}"
BASE_URL="${BASE_URL:-}"
RETRY_SOURCE_EXECUTION_KEY="${RETRY_SOURCE_EXECUTION_KEY:-}"
RETRY_REQUEST_BODY="${RETRY_REQUEST_BODY:-}"
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
ensure_required "RETRY_SOURCE_EXECUTION_KEY" "${RETRY_SOURCE_EXECUTION_KEY}"
ensure_required "RETRY_REQUEST_BODY" "${RETRY_REQUEST_BODY}"

mkdir -p "${OUTPUT_DIR}"
RUN_LOG="${OUTPUT_DIR}/run-retry.log"
APP_PID_FILE="${OUTPUT_DIR}/app.pid"
RETRY_RESPONSE_FILE="${OUTPUT_DIR}/retry-response.json"
RETRY_DERIVED_KEY_FILE="${OUTPUT_DIR}/retry-derived-execution-key.txt"
RETRY_DERIVED_QUERY_FILE="${OUTPUT_DIR}/retry-derived-query.json"
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

extract_execution_key() {
  local value
  value="$(tr -d '\n' < "${RETRY_RESPONSE_FILE}" | sed -n 's/.*"executionKey":"\([^"]*\)".*/\1/p')"
  if [[ -z "${value}" ]]; then
    return 1
  fi

  printf '%s\n' "${value}" > "${RETRY_DERIVED_KEY_FILE}"
}

start_app
log "앱 기동 시작"
if ! wait_for_port; then
  log "앱 기동 시간 안에 포트 확인 실패"
  exit 30
fi

log "재시도 요청 시작"
if ! curl_to_file \
  "${RETRY_RESPONSE_FILE}" \
  -X POST \
  -H 'Content-Type: application/json' \
  -d "${RETRY_REQUEST_BODY}" \
  "${BASE_URL}/reviews/rerun/${RETRY_SOURCE_EXECUTION_KEY}/retry"; then
  log "재시도 요청 실패"
  exit 31
fi

log "재시도 파생 실행 키 추출 시작"
if ! extract_execution_key; then
  log "재시도 파생 실행 키 추출 실패"
  exit 32
fi

RETRY_DERIVED_EXECUTION_KEY="$(cat "${RETRY_DERIVED_KEY_FILE}")"
log "재시도 파생 실행 단건 조회 시작"
if ! curl_to_file "${RETRY_DERIVED_QUERY_FILE}" "${BASE_URL}/reviews/rerun/${RETRY_DERIVED_EXECUTION_KEY}"; then
  log "재시도 파생 실행 단건 조회 실패"
  exit 33
fi

log "재시도 요청 흐름 완료"
