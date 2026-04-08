---
spec_id: SPEC-0006
task_id: TASK-0002
github_issue_number: 21
criteria_keys:
  - manual-rerun-entrypoint-contract-defined
  - manual-rerun-request-resolved-consistently
delivery_ids: []
execution_keys: []
test_evidence_ref:
  - "targeted: ./scripts/gradlew-java21.sh --no-daemon test --tests 'com.agilerunner.api.controller.review.ManualRerunControllerTest' --tests 'com.agilerunner.api.controller.review.request.ManualRerunRequestTest' --tests 'com.agilerunner.api.controller.GitHubWebhookControllerTest' --tests 'com.agilerunner.api.service.GitHubCommentServiceTest' --tests 'com.agilerunner.api.service.OpenAiServiceTest' --console=plain"
  - "full: ./scripts/gradlew-java21.sh --no-daemon cleanTest test --console=plain"
  - "actual app: POST /reviews/rerun -> 200 OK"
diff_ref: "git diff -- .agents/active/tasks.md src/main/java/com/agilerunner/api/controller/review src/main/java/com/agilerunner/api/service/review src/test/java/com/agilerunner/api/controller/review"
failure_summary: "초기 구현은 UUID execution key와 writePerformed=false를 실제 실행 결과처럼 반환해 TASK-0003 경계를 일부 선점했다. 또한 targeted test와 full test를 동시에 실행해 JUnit XML 출력 충돌이 발생했다."
root_cause: "수동 재실행 진입점의 성공 응답 계약을 빨리 닫으려다 보니 placeholder와 실제 실행 의미의 경계를 충분히 분리하지 못했다. 검증 단계에서는 같은 workspace에서 Gradle test 작업을 병렬 실행해 테스트 결과 파일 경합을 만들었다."
agents_check_findings:
  - "controller/request/service/response 패키지 분리는 AGENTS 규칙에 맞게 유지했다."
  - "service는 pending placeholder 응답만 반환하도록 낮춰 TASK-0002 범위 안으로 되돌렸다."
  - "실제 rerun orchestration과 runtime evidence 적재는 TASK-0003, TASK-0004로 남겼다."
next_task_warnings:
  - "TASK-0003에서는 ManualRerunService의 pending placeholder를 실제 리뷰 생성, 코멘트 작성, dry-run 분기 재사용으로 교체해야 한다."
  - "writePerformed 의미는 TASK-0003에서만 실제 side effect 기준으로 닫아야 한다."
  - "targeted test와 full test는 같은 workspace에서 동시에 실행하지 말아야 한다."
error_signature: "UUID execution key와 writePerformed=false가 실제 실행 결과처럼 보임, Gradle test result XML write collision"
test_result_summary: "manual rerun request/controller targeted test, 기존 webhook 회귀 targeted test, 전체 cleanTest test, 실제 앱 POST /reviews/rerun 200 OK 모두 확인"
snapshot_hashes: []
---

# 작업 회고

## 작업 요약
- 수동 재실행 request DTO, service request DTO, controller/service 진입점, 응답 DTO를 추가했다.
- `POST /reviews/rerun`의 `200 OK + executionKey + executionControlMode + writePerformed` 계약을 black-box 테스트로 고정했다.
- 실제 rerun orchestration은 아직 연결하지 않고, service는 `MANUAL_RERUN:PENDING` placeholder 응답만 반환하도록 두었다.

## 실패 요약
- 첫 구현은 `executionKey`를 UUID로 생성하고 `writePerformed=false`를 실제 결과처럼 반환해 `TASK-0003` 의미를 일부 앞당겼다.
- Tester 2에서 targeted test와 full test를 동시에 실행해 `build/test-results` XML 출력 충돌로 false negative가 한 번 발생했다.

## Root Cause
- 입력 모델과 진입점 계약을 닫는 단계와 실제 rerun 실행 의미를 닫는 단계를 충분히 분리하지 않은 채 구현을 시작했다.
- 전체 테스트 확인을 빠르게 끝내려다 Gradle test 작업을 같은 workspace에서 병렬로 실행했다.

## AGENTS 체크 결과
- DTO는 `class`로 만들고 controller/service DTO를 분리했다.
- controller는 request 수신, service 호출, response 반환만 담당하게 유지했다.
- service는 실제 rerun orchestration을 앞당기지 않고 placeholder 응답만 반환하도록 조정했다.
- webhook/controller/orchestration/runtime 저장 구조 변경 task는 아니어서 H2 representative 검증 대신 실제 앱의 `/reviews/rerun` 200 계약만 확인했다.

## 근거 Artifact
- `.agents/active/spec.md`
- `.agents/active/tasks.md`
- `.agents/criteria/SPEC-0006-manual-rerun-foundation.json`
- `src/main/java/com/agilerunner/api/controller/review/ManualRerunController.java`
- `src/main/java/com/agilerunner/api/controller/review/request/ManualRerunRequest.java`
- `src/main/java/com/agilerunner/api/controller/review/response/ManualRerunResponse.java`
- `src/main/java/com/agilerunner/api/service/review/ManualRerunService.java`
- `src/main/java/com/agilerunner/api/service/review/request/ManualRerunServiceRequest.java`
- `src/main/java/com/agilerunner/api/service/review/response/ManualRerunServiceResponse.java`
- `src/test/java/com/agilerunner/api/controller/review/ManualRerunControllerTest.java`
- `src/test/java/com/agilerunner/api/controller/review/request/ManualRerunRequestTest.java`

## 다음 Task 경고사항
- `TASK-0003`에서만 실제 리뷰 생성, 코멘트 작성, dry-run no-write 분기를 연결해야 한다.
- `TASK-0003` 이전에는 `MANUAL_RERUN:PENDING`과 `writePerformed=false`를 실제 실행 의미로 해석하면 안 된다.
- targeted test와 full test는 같은 workspace에서 순차 실행해야 한다.

## 제안 필요 여부
- 있음
- 같은 workspace에서 Gradle test 작업을 병렬로 실행하면 JUnit XML 출력 파일 충돌로 false negative가 생길 수 있으므로, task 종료 검증 규칙에 순차 실행 원칙을 추가할 필요가 있다.
