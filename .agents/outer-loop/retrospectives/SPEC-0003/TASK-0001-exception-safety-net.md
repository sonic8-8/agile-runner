---
spec_id: SPEC-0003
task_id: TASK-0001
github_issue_number: 10
criteria_keys:
  - webhook-contract-preserved-after-exception-refactor
delivery_ids: []
execution_keys: []
test_evidence_ref:
  - "targeted: GRADLE_USER_HOME=/home/seaung13/workspace/agile-runner/.gradle-local scripts/gradlew-java21.sh --no-daemon test --tests 'com.agilerunner.api.controller.GitHubWebhookControllerTest' --tests 'com.agilerunner.api.service.GitHubCommentServiceTest' --tests 'com.agilerunner.api.service.OpenAiServiceTest' --console=plain"
  - "full: GRADLE_USER_HOME=/home/seaung13/workspace/agile-runner/.gradle-local scripts/gradlew-java21.sh --no-daemon test --console=plain"
diff_ref: "git diff -- .agents/active/spec.md .agents/active/tasks.md .agents/criteria/SPEC-0003-exception-system-alignment.json src/test/java/com/agilerunner/api/controller/GitHubWebhookControllerTest.java src/test/java/com/agilerunner/api/service/GitHubCommentServiceTest.java src/test/java/com/agilerunner/api/service/OpenAiServiceTest.java"
failure_summary: "예외 체계 도입 전 핵심 실패 경로 안전망이 일부 비어 있었고, controller/service black-box 범위를 넘는 내부 호출 검증을 어떻게 다룰지 리뷰 과정에서 조정이 필요했다."
root_cause: "SPEC-0003의 첫 task는 새 예외 구현보다 안전망 고정이 목적이지만, 문서와 테스트 초안에서 최종 예외 분류와 내부 orchestration 검증이 섞이면서 범위 경계가 흐려졌다."
agents_check_findings:
  - "TASK-0001은 최종 `AgileRunnerException + ErrorCode` 충족이 아니라 기대 분류 기준 고정에 집중하도록 정리했다."
  - "Tester 1차는 controller/service black-box 안전망에 집중하고, 내부 runtime 기록 호출 검증은 제외했다."
next_task_warnings:
  - "TASK-0002에서는 TASK-0001이 고정한 테스트를 그대로 유지하면서 예외 타입과 오류 코드 치환만 수행해야 한다."
  - "OpenAI 설정 누락, 설치 요청 정보 누락, GitHub App 설정 문제, 코멘트 준비/등록 실패 경로는 기존 테스트 의미를 바꾸지 않도록 주의해야 한다."
error_signature: "문서와 테스트 초안이 안전망 고정과 최종 예외 분류를 한 task 안에서 동시에 닫으려 하면서 범위 충돌 발생"
test_result_summary: "controller/service targeted test와 전체 ./gradlew test 모두 통과"
snapshot_hashes: []
---

# 작업 회고

## 작업 요약
- `SPEC-0003`의 첫 task로 현재 웹훅 성공/조기 종료 계약과 핵심 실패 경로를 테스트 기준으로 고정했다.
- 기존 `GitHubWebhookControllerTest`, `GitHubCommentServiceTest` 안전망을 재사용하고, 부족한 실패 경로만 보강했다.
- `OpenAiServiceTest`를 추가해 OpenAI 설정 누락 실패를 서비스 경계에서 고정했다.

## 실패 요약
- 첫 초안에서는 `TASK-0001`이 최종 예외 분류 criteria까지 닫는 것처럼 읽혔다.
- `리뷰 생성 실패 -> runtime 기록 호출`까지 직접 검증할지 여부에서 subagent 의견이 갈렸다.
- 그 결과 `TASK-0001`은 black-box 안전망 고정에 집중하고, 내부 호출 검증은 제거하는 쪽으로 수렴했다.

## Root Cause
- 안전망 고정 task와 실제 예외 체계 도입 task의 경계를 문서와 테스트에서 동시에 명확히 잡지 못했다.
- `Tester 1차`의 black-box 원칙과 orchestration 내부 확인 욕구가 섞이면서 초안이 과해졌다.

## AGENTS 체크 결과
- `Tester`는 production code를 수정하지 않았다.
- controller/service 중심의 black-box 테스트를 우선 작성했다.
- task 정의 후 issue 연결 규칙을 지켰다.
- targeted test 이후 전체 `./gradlew test`까지 확인했다.

## 근거 Artifact
- `.agents/active/spec.md`
- `.agents/active/tasks.md`
- `.agents/criteria/SPEC-0003-exception-system-alignment.json`
- `src/test/java/com/agilerunner/api/controller/GitHubWebhookControllerTest.java`
- `src/test/java/com/agilerunner/api/service/GitHubCommentServiceTest.java`
- `src/test/java/com/agilerunner/api/service/OpenAiServiceTest.java`

## 다음 Task 경고사항
- `TASK-0002`는 새 예외 타입과 오류 코드 도입에 집중하고, 성공/조기 종료 계약 테스트 의미를 바꾸지 않는다.
- `TASK-0001`에서 추가한 안전망을 우회하기 위해 테스트를 약하게 수정하지 않는다.

## 제안 필요 여부
- 없음
- 이번 task는 새 workflow나 AGENTS 규칙 변경보다, 기존 black-box 원칙 안에서 범위를 바로잡는 수준으로 정리됐다.
