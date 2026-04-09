---
spec_id: SPEC-0009
task_id: TASK-0003
github_issue_number: 34
criteria_keys:
  - manual-rerun-query-response-matches-rerun-meaning
delivery_ids: []
execution_keys: []
test_evidence_ref:
  - src/test/java/com/agilerunner/api/service/review/ManualRerunQueryServiceTest.java
  - src/test/java/com/agilerunner/api/controller/review/ManualRerunControllerTest.java
  - src/test/java/com/agilerunner/api/service/review/ManualRerunServiceTest.java
  - src/test/java/com/agilerunner/client/agentruntime/AgentRuntimeRepositoryTest.java
  - src/test/java/com/agilerunner/AgileRunnerApplicationTests.java
diff_ref:
  - src/main/java/com/agilerunner/api/service/review/ManualRerunQueryService.java
  - src/test/java/com/agilerunner/api/service/review/ManualRerunQueryServiceTest.java
  - src/test/java/com/agilerunner/api/controller/review/ManualRerunControllerTest.java
failure_summary: query response를 runtime evidence와 연결하는 과정에서 full suite에서만 agent-runtime repository bean 의존성이 드러났다.
root_cause: ManualRerunQueryService가 runtime evidence 조회를 추가하면서 AgentRuntimeRepository bean을 항상 존재한다고 가정했고, agent runtime 비활성 환경에서도 애플리케이션 컨텍스트가 떠야 한다는 기존 조건을 놓쳤다.
agents_check_findings:
  - query service는 runtime evidence 값 조회와 응답 의미 매핑만 담당하고 representative actual app/H2 검증은 TASK-0004로 남겼다.
  - controller는 GET 응답 노출과 not found 예외 매핑만 유지했다.
  - full suite에서 드러난 bean 의존성 문제를 optional 주입으로 정리해 local/prod 기동 조건을 함께 맞췄다.
next_task_warnings:
  - TASK-0004는 fresh manual rerun 생성 후 같은 executionKey로 GET 조회, 앱 종료, H2 조회 순서를 지켜 representative 검증을 수행해야 한다.
  - representative 검증에서는 query 응답과 H2 WEBHOOK_EXECUTION 값의 executionStatus, errorCode, failureDisposition, writePerformed를 같이 맞춰야 한다.
error_signature: NoSuchBeanDefinitionException: AgentRuntimeRepository
test_result_summary: query/controller targeted test green, AgileRunnerApplicationTests 포함 재검증 green, full cleanTest test green
snapshot_hashes: []
---

# 작업 회고

## 작업 요약
- `ManualRerunQueryService`를 placeholder not found 구현에서 `executionKey -> WebhookExecution` 조회와 응답 의미 매핑으로 바꿨다.
- query success 응답이 rerun 응답과 같은 의미의 `executionControlMode`, `writePerformed`, `executionStatus`, `errorCode`, `failureDisposition`를 반환하도록 정리했다.
- manual rerun이 아닌 execution이나 조회 불가 상황은 not found 정책으로 유지했다.

## 실패 요약
- 첫 full suite에서 `AgileRunnerApplicationTests`가 `AgentRuntimeRepository` bean 부재로 실패했다.
- 원인은 기능 로직이 아니라 query service가 agent runtime 비활성 환경도 고려하지 않고 repository bean을 강하게 요구한 점이었다.

## Root Cause
- 이번 task는 `executionKey` 기반 조회 의미 연결만 의식했고, `agile-runner`가 운영에서는 agent runtime을 비활성화해도 기본 컨텍스트가 올라와야 한다는 기존 전제를 놓쳤다.
- targeted test만 보면 service/controller 경계는 green이었지만, full suite에서 contextLoads가 이 전제를 다시 검증해 줬다.

## AGENTS 체크 결과
- service는 runtime evidence 조회와 응답 의미 매핑만 담당하고, representative actual app/H2 검증은 다음 task로 넘겼다.
- controller는 요청 해석과 응답 노출만 유지했다.
- targeted test와 full test를 순차 실행했고, full suite 실패를 수정한 뒤 다시 전체를 재검증했다.
- production code, `/webhook/github` orchestration, agent-runtime 저장 구조 변경은 없어서 actual app/H2 representative 검증은 비대상으로 두었다.

## 근거 Artifact
- targeted test:
  - `ManualRerunQueryServiceTest`
  - `ManualRerunControllerTest`
  - `ManualRerunServiceTest`
  - `AgentRuntimeRepositoryTest`
  - `AgileRunnerApplicationTests`
- full test:
  - `./scripts/gradlew-java21.sh --no-daemon cleanTest test --console=plain`
- 핵심 근거:
  - runtime evidence 값이 query response 의미로 연결됨
  - controller JSON 응답이 service 의미를 그대로 노출함
  - agent runtime 비활성 환경에서도 기본 컨텍스트가 다시 정상 기동함

## 다음 Task 경고사항
- `TASK-0004`는 representative actual app/H2 검증이 핵심이므로 actual app을 띄운 뒤 fresh manual rerun 1건과 같은 executionKey 기반 GET 조회를 모두 수행해야 한다.
- representative 검증은 `manual rerun 생성 -> 같은 executionKey로 GET 조회 -> 앱 종료 -> H2 조회` 순서를 지켜야 한다.
- query success 응답과 H2 evidence의 필드 정합성만 확인하고, 새 스키마 컬럼 추가는 하지 않아야 한다.

## 제안 필요 여부
- 있음
- `WORKFLOW-PROP-0010-from-SPEC-0009-TASK-0003.md`
- `agent-runtime`처럼 조건부로 비활성화될 수 있는 bean에 새 의존성을 추가할 때는 비활성 환경의 기본 컨텍스트 기동도 함께 확인하는 규칙 제안
