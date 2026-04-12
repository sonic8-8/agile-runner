-- 시나리오: rerun 대표 검증 뒤 실행 근거를 확인하는 SQL 예시 파일
-- 적용 시점: 앱 종료 후에만 실행한다.
-- 선행 단계: rerun 요청 또는 관리자 조치 요청을 먼저 실행하고 HTTP 응답을 확인한다.
-- 예시 실행 키: EXECUTION:MANUAL_RERUN:example-rerun

SELECT
    execution_key,
    status,
    error_code,
    failure_disposition,
    execution_start_type,
    execution_control_mode,
    write_performed
FROM WEBHOOK_EXECUTION
WHERE execution_key = 'EXECUTION:MANUAL_RERUN:example-rerun';

SELECT
    execution_key,
    action,
    action_status,
    note,
    applied_at
FROM MANUAL_RERUN_CONTROL_ACTION_AUDIT
WHERE execution_key = 'EXECUTION:MANUAL_RERUN:example-rerun'
ORDER BY applied_at ASC, id ASC;
