---
spec_id: SPEC-0011
task_id: TASK-0002
github_issue_number: 41
criteria_keys:
  - manual-rerun-list-filter-defined
delivery_ids: []
execution_keys: []
test_evidence_ref:
  - "targeted: ./scripts/gradlew-java21.sh --no-daemon test --tests 'com.agilerunner.api.controller.review.ManualRerunControllerTest' --tests 'com.agilerunner.api.controller.review.request.ManualRerunExecutionListRequestTest' --tests 'com.agilerunner.api.service.review.ManualRerunExecutionListServiceTest' --console=plain"
  - "full: ./scripts/gradlew-java21.sh --no-daemon cleanTest test --console=plain"
  - "actual app: bootRun(local, port=18080) -> GET /reviews/rerun/executions?repositoryName=owner/repo&pullRequestNumber=12&executionStartType=MANUAL_RERUN&executionStatus=FAILED&failureDisposition=RETRYABLE -> HTTP 200 + executions=[]"
diff_ref: "git diff -- src/main/java/com/agilerunner/api/controller/review/ManualRerunController.java src/main/java/com/agilerunner/api/controller/review/request/ManualRerunExecutionListRequest.java src/main/java/com/agilerunner/api/controller/review/response/ManualRerunExecutionListResponse.java src/main/java/com/agilerunner/api/service/review/ManualRerunExecutionListService.java src/main/java/com/agilerunner/api/service/review/request/ManualRerunExecutionListServiceRequest.java src/main/java/com/agilerunner/api/service/review/response/ManualRerunExecutionListServiceResponse.java src/main/java/com/agilerunner/client/agentruntime/AgentRuntimeRepository.java src/test/java/com/agilerunner/api/controller/review/ManualRerunControllerTest.java src/test/java/com/agilerunner/api/controller/review/request/ManualRerunExecutionListRequestTest.java src/test/java/com/agilerunner/api/service/review/ManualRerunExecutionListServiceTest.java"
failure_summary: "목록 조회 입력 seam이 없어서 controller/service black-box 테스트가 compile 단계에서 막혔다."
root_cause: "SPEC-0011 TASK-0002는 필터 DTO, service request, controller 진입점, repository 조회 seam을 함께 추가해야 하는데, 기존 rerun 흐름에는 목록 조회 전용 경계가 아직 없었다."
agents_check_findings:
  - "3개 서브에이전트 리뷰 결과, 이번 task는 executionKey만 가진 최소 목록 응답으로 경계를 닫고 `availableActions`와 row 의미 확장은 다음 task로 넘기는 구성이 맞다고 정리됐다."
  - "controller는 요청 수신과 응답 반환만 담당하고, service는 manual rerun execution 조회와 값이 있는 필터만 적용하는 책임으로 제한됐다."
  - "조건부 bean인 AgentRuntimeRepository가 비활성화되면 빈 목록을 반환해 기본 컨텍스트 기동과 local 진입점 확인을 유지했다."
next_task_warnings:
  - "TASK-0003는 목록 응답 row에 runtime 상태와 `availableActions`를 추가하되, 필터 해석 규칙을 다시 건드리지 않아야 한다."
  - "RETRY 포함 여부는 기존 manual rerun retry eligibility 정책을 그대로 재사용하고 새 판단 규칙을 만들지 않아야 한다."
error_signature: "compile failure: 목록 조회 controller/service/request/repository seam 부재"
test_result_summary: "targeted test와 full cleanTest test 통과. actual app에서는 새 목록 조회 endpoint가 200과 빈 executions 배열을 반환했다. runtime 저장 구조 변경은 없어서 H2 evidence 확인은 비대상으로 정리했다."
snapshot_hashes: []
---

# 작업 회고

## 작업 요약
- `GET /reviews/rerun/executions` 진입점과 목록 조회 request/service DTO를 추가했다.
- 비어 있는 필터는 미적용으로 해석하고, 값이 있는 조건만 manual rerun execution 목록에 적용하는 기본 service 경계를 도입했다.
- 이번 task에서는 최소 응답 계약으로 `executionKey`만 노출하고, 관리자 제어 상태와 row 의미 확장은 다음 task로 남겼다.

## 실패 요약
- 구현 전에는 목록 조회 전용 controller/service/request seam이 없어서 테스트가 compile 단계에서 실패했다.
- 구현 후에는 targeted test, full test, 실제 앱 목록 조회 모두 정상 통과했다.

## Root Cause
- 기존 rerun 흐름은 단건 query와 retry 중심이라, 운영용 목록 조회를 붙이기 위한 입력 모델과 service boundary가 아직 분리돼 있지 않았다.

## AGENTS 체크 결과
- Tester 1차는 controller/service black-box 테스트로 필터 전달과 빈 필터 미적용 해석을 먼저 고정했다.
- Constructor 단계에서는 최소 응답 계약만 도입하고 `availableActions` 의미 확장을 끌어오지 않았다.
- targeted test와 전체 테스트를 순차 실행했다.
- 새 controller 진입점이라 실제 앱에서 endpoint 200 응답을 한 번 확인했고, runtime 저장 구조 변경은 없어서 H2 evidence 조회는 생략했다.

## 근거 Artifact
- `src/main/java/com/agilerunner/api/controller/review/ManualRerunController.java`
- `src/main/java/com/agilerunner/api/controller/review/request/ManualRerunExecutionListRequest.java`
- `src/main/java/com/agilerunner/api/controller/review/response/ManualRerunExecutionListResponse.java`
- `src/main/java/com/agilerunner/api/service/review/ManualRerunExecutionListService.java`
- `src/main/java/com/agilerunner/api/service/review/request/ManualRerunExecutionListServiceRequest.java`
- `src/main/java/com/agilerunner/api/service/review/response/ManualRerunExecutionListServiceResponse.java`
- `src/main/java/com/agilerunner/client/agentruntime/AgentRuntimeRepository.java`
- `src/test/java/com/agilerunner/api/controller/review/ManualRerunControllerTest.java`
- `src/test/java/com/agilerunner/api/controller/review/request/ManualRerunExecutionListRequestTest.java`
- `src/test/java/com/agilerunner/api/service/review/ManualRerunExecutionListServiceTest.java`

## 다음 Task 경고사항
- `TASK-0003`는 목록 응답 row에 상태 필드와 `availableActions`를 연결하되, 이번 task에서 고정한 필터 해석 규칙을 흔들지 않아야 한다.
- `availableActions`의 `RETRY` 포함 여부는 기존 retry eligibility 정책을 그대로 재사용해야 한다.

## 제안 필요 여부
- 없음
- 이번 task의 교훈은 새 workflow 규칙 부족이 아니라, 목록 조회를 붙일 때 입력 모델과 최소 응답 계약을 먼저 닫고 의미 확장은 다음 task로 넘기는 현재 작업 경계가 적절하다는 점이었다.
