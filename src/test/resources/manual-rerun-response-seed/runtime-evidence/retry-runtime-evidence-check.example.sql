-- 시나리오: retry 대표 검증 뒤 실행 근거를 확인하는 SQL 예시 파일
-- 적용 시점: 앱 종료 후에만 실행한다.
-- 선행 단계: 앱 기동 전 retry-source-execution-seed.example.sql 을 적용하고, 앱 기동 뒤 retry 요청을 실행한다.
-- 예시 실행 키: EXECUTION:MANUAL_RERUN:example-retry-derived

SELECT
    execution_key,
    retry_source_execution_key,
    status,
    error_code,
    failure_disposition,
    execution_start_type,
    execution_control_mode,
    write_performed
FROM WEBHOOK_EXECUTION
WHERE execution_key = 'EXECUTION:MANUAL_RERUN:example-retry-derived';

SELECT
    execution_key,
    retry_source_execution_key,
    step_name,
    status,
    error_code,
    failure_disposition
FROM AGENT_EXECUTION_LOG
WHERE execution_key = 'EXECUTION:MANUAL_RERUN:example-retry-derived'
ORDER BY id ASC;
