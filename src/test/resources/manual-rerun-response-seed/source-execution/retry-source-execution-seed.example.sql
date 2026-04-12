-- 시나리오: retry 대표 검증용 원본 실행 준비 데이터 예시 파일
-- 적용 시점: 앱 기동 전에 로컬 H2에 먼저 넣는다.
-- 다음 단계: 앱 기동 뒤 retry 요청을 실행하고, 앱 종료 후 retry-runtime-evidence-check.example.sql 로 결과 행을 확인한다.
-- 이번 단계에서는 현재 schema와 enum 값에 맞는 실제 INSERT를 넣고, 실행 근거 확인 SQL은 다음 단계에서 보강한다.

INSERT INTO WEBHOOK_EXECUTION (
    execution_key,
    task_key,
    delivery_id,
    retry_source_execution_key,
    repository_name,
    pull_request_number,
    event_type,
    action,
    status,
    error_message,
    error_code,
    failure_disposition,
    execution_start_type,
    execution_control_mode,
    write_performed,
    write_skip_reason,
    selection_applied,
    selected_paths_summary,
    started_at,
    finished_at
) VALUES (
    'EXECUTION:MANUAL_RERUN:example-retry-source',
    'PR_REVIEW:owner/repo#12',
    'MANUAL_RERUN_DELIVERY:example-retry-source',
    NULL,
    'owner/repo',
    12,
    'PULL_REQUEST',
    'manual_rerun',
    'FAILED',
    'retry source failed',
    'GITHUB_APP_CONFIGURATION_MISSING',
    'RETRYABLE',
    'MANUAL_RERUN',
    'DRY_RUN',
    FALSE,
    'DRY_RUN',
    FALSE,
    NULL,
    TIMESTAMP '2026-04-12 13:00:00',
    TIMESTAMP '2026-04-12 13:01:00'
);
