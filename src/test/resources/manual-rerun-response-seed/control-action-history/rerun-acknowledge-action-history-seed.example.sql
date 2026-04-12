-- 시나리오: rerun-acknowledge 대표 검증용 관리자 조치 이력 예시 파일
-- 적용 시점: 앱 기동 전에 로컬 H2에 넣어 조치 이력 상태를 준비한다.
-- 다음 단계: 앱 기동 뒤 rerun 단건 조회, 목록 조회, 이력 조회 또는 관리자 조치 응답을 확인하고, 앱 종료 후 rerun-runtime-evidence-check.example.sql 로 근거 행을 확인한다.
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
    'EXECUTION:MANUAL_RERUN:example-rerun',
    'PR_REVIEW:owner/repo#12',
    'MANUAL_RERUN_DELIVERY:example-rerun',
    NULL,
    'owner/repo',
    12,
    'PULL_REQUEST',
    'manual_rerun',
    'FAILED',
    'review generation failed',
    'GITHUB_APP_CONFIGURATION_MISSING',
    'MANUAL_ACTION_REQUIRED',
    'MANUAL_RERUN',
    'DRY_RUN',
    FALSE,
    'DRY_RUN',
    FALSE,
    NULL,
    TIMESTAMP '2026-04-12 13:10:00',
    TIMESTAMP '2026-04-12 13:11:00'
);

INSERT INTO MANUAL_RERUN_CONTROL_ACTION_AUDIT (
    execution_key,
    action,
    action_status,
    note,
    applied_at
) VALUES (
    'EXECUTION:MANUAL_RERUN:example-rerun',
    'ACKNOWLEDGE',
    'APPLIED',
    '운영자 확인 완료',
    TIMESTAMP '2026-04-12 13:15:00'
);
