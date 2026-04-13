#!/usr/bin/env bash

set -u

OUTPUT_DIR="${OUTPUT_DIR:-}"
JDBC_URL="${JDBC_URL:-}"
H2_JAR="${H2_JAR:-}"
EVIDENCE_MODE="${EVIDENCE_MODE:-}"
RERUN_EXECUTION_KEY="${RERUN_EXECUTION_KEY:-}"
RETRY_DERIVED_EXECUTION_KEY="${RETRY_DERIVED_EXECUTION_KEY:-}"
APP_PROCESS_PATTERN="${APP_PROCESS_PATTERN:-GradleMain|gradle.*bootRun}"
H2_PROCESS_PATTERN="${H2_PROCESS_PATTERN:-org.h2.tools.RunScript|org.h2.tools.Shell}"

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
ensure_required "JDBC_URL" "${JDBC_URL}"
ensure_required "H2_JAR" "${H2_JAR}"
ensure_required "EVIDENCE_MODE" "${EVIDENCE_MODE}"

mkdir -p "${OUTPUT_DIR}"
COLLECT_LOG="${OUTPUT_DIR}/collect-evidence.log"
APP_PID_FILE="${OUTPUT_DIR}/app.pid"
: > "${COLLECT_LOG}"

log() {
  printf '%s\n' "$1" | tee -a "${COLLECT_LOG}"
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

has_live_process_by_pattern() {
  local pattern="$1"
  local matched=1

  while read -r pid _; do
    if [[ -z "${pid}" ]]; then
      continue
    fi
    if is_process_alive "${pid}"; then
      ps -o pid=,args= -p "${pid}" >> "${COLLECT_LOG}" 2>&1 || true
      matched=0
    fi
  done < <(pgrep -af "${pattern}" || true)

  return ${matched}
}

stop_app_if_needed() {
  if [[ ! -f "${APP_PID_FILE}" ]]; then
    return 0
  fi

  local app_pid
  app_pid="$(cat "${APP_PID_FILE}")"
  if [[ -z "${app_pid}" ]]; then
    return 0
  fi

  if is_process_alive "${app_pid}"; then
    kill "${app_pid}" >/dev/null 2>&1 || true
    for _ in $(seq 1 10); do
      if ! is_process_alive "${app_pid}"; then
        break
      fi
      sleep 1
    done
    if is_process_alive "${app_pid}"; then
      kill -9 "${app_pid}" >/dev/null 2>&1 || true
    fi
  fi
}

run_query() {
  local output_file="$1"
  local sql="$2"

  java -cp "${H2_JAR}" org.h2.tools.Shell \
    -url "${JDBC_URL}" \
    -user sa \
    -sql "${sql}" > "${output_file}" 2>> "${COLLECT_LOG}"
  local exit_code=$?

  if [[ ${exit_code} -ne 0 ]]; then
    return 1
  fi

  if grep -q '^Error:' "${output_file}"; then
    return 1
  fi

  return 0
}

has_lock_signature() {
  local output_file="$1"
  if grep -Eq 'Database may be already in use|The file is locked' "${output_file}" "${COLLECT_LOG}"; then
    return 0
  fi

  return 1
}

handle_query_failure() {
  local output_file="$1"
  if has_lock_signature "${output_file}"; then
    has_live_process_by_pattern "${H2_PROCESS_PATTERN}" || true
    log "H2 잠금 의심"
    exit 42
  fi

  log "실행 근거 조회 실패"
  exit 41
}

stop_app_if_needed
log "앱 종료 확인 시작"
if has_live_process_by_pattern "${APP_PROCESS_PATTERN}"; then
  log "앱 종료 미확인"
  exit 40
fi

if [[ "${EVIDENCE_MODE}" == "rerun" ]]; then
  ensure_required "RERUN_EXECUTION_KEY" "${RERUN_EXECUTION_KEY}"
  RERUN_WEBHOOK_FILE="${OUTPUT_DIR}/rerun-webhook-execution.txt"
  RERUN_ACTION_FILE="${OUTPUT_DIR}/rerun-action-audit.txt"

  log "재실행 실행 근거 조회 시작"
  if ! run_query "${RERUN_WEBHOOK_FILE}" "SELECT execution_key, status, error_code, failure_disposition, execution_start_type, execution_control_mode, write_performed FROM WEBHOOK_EXECUTION WHERE execution_key = '${RERUN_EXECUTION_KEY}';"; then
    handle_query_failure "${RERUN_WEBHOOK_FILE}"
  fi

  if ! run_query "${RERUN_ACTION_FILE}" "SELECT execution_key, action, action_status, note, applied_at FROM MANUAL_RERUN_CONTROL_ACTION_AUDIT WHERE execution_key = '${RERUN_EXECUTION_KEY}' ORDER BY applied_at ASC, id ASC;"; then
    handle_query_failure "${RERUN_ACTION_FILE}"
  fi

  log "재실행 실행 근거 수집 완료"
  exit 0
fi

if [[ "${EVIDENCE_MODE}" == "retry" ]]; then
  ensure_required "RETRY_DERIVED_EXECUTION_KEY" "${RETRY_DERIVED_EXECUTION_KEY}"
  RETRY_WEBHOOK_FILE="${OUTPUT_DIR}/retry-webhook-execution.txt"
  RETRY_AGENT_FILE="${OUTPUT_DIR}/retry-agent-execution-log.txt"

  log "재시도 실행 근거 조회 시작"
  if ! run_query "${RETRY_WEBHOOK_FILE}" "SELECT execution_key, retry_source_execution_key, status, error_code, failure_disposition, execution_start_type, execution_control_mode, write_performed FROM WEBHOOK_EXECUTION WHERE execution_key = '${RETRY_DERIVED_EXECUTION_KEY}';"; then
    handle_query_failure "${RETRY_WEBHOOK_FILE}"
  fi

  if ! run_query "${RETRY_AGENT_FILE}" "SELECT execution_key, retry_source_execution_key, step_name, status, error_code, failure_disposition FROM AGENT_EXECUTION_LOG WHERE execution_key = '${RETRY_DERIVED_EXECUTION_KEY}' ORDER BY id ASC;"; then
    handle_query_failure "${RETRY_AGENT_FILE}"
  fi

  log "재시도 실행 근거 수집 완료"
  exit 0
fi

printf '지원하지 않는 실행 근거 수집 모드입니다: %s\n' "${EVIDENCE_MODE}" >&2
exit 1
