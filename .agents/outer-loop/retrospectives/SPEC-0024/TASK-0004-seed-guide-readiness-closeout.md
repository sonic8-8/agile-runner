---
spec_id: SPEC-0024
task_id: TASK-0004
github_issue_number: 95
criteria_keys:
  - manual-rerun-seed-application-guide-readiness-verified
delivery_ids: []
execution_keys: []
test_evidence_ref:
  - "targeted: ./scripts/gradlew-java21.sh --no-daemon test --tests '*ManualRerunResponseGuideFixtureTest' --tests '*ManualRerunControllerTest' --tests '*ManualRerunServiceTest' --tests '*ManualRerunRetryServiceTest' --tests '*ManualRerunQueryServiceTest' --tests '*ManualRerunExecutionListServiceTest' --tests '*ManualRerunControlActionHistoryServiceTest' --tests '*ManualRerunControlActionServiceTest' --console=plain"
  - "full: ./scripts/gradlew-java21.sh --no-daemon cleanTest test --console=plain"
diff_ref: "git diff -- docs/manual-rerun-response-seed-guide.md src/test/resources/manual-rerun-response-seed .agents/outer-loop/retrospectives/SPEC-0024/TASK-0004-seed-guide-readiness-closeout.md .agents/outer-loop/retrospectives/SPEC-0024/SPEC-0024-summary.md .agents/outer-loop/registry.json"
failure_summary: "첫 문서 마감 검토에서 안내 문서와 준비 데이터 파일의 연결이 약해 `문서와 준비 데이터 파일만 읽고도 따라갈 수 있다`는 기준이 아직 약하다는 지적을 받았다. 특히 안내 문서 안에 여전히 활성 spec/task 문서를 더 보라고 읽힐 수 있는 문구가 있었고, `rerun-unacknowledge` 시나리오가 어떤 파일 조합을 재사용하는지 한눈에 보이지 않았다."
root_cause: "TASK-0004는 문서 마감 단계인데, 문서 자체의 설명만으로 충분해야 하는 기준을 회고 링크와 활성 spec 보조 문구에 기대고 있었다. 준비 데이터 예시 파일도 시나리오와 적용 시점은 드러냈지만, 시나리오별 파일 조합과 재사용 규칙을 안내 문서 안에서 닫지 않아 새 작업자가 일부를 추론해야 했다."
agents_check_findings:
  - "문서 경계 리뷰는 안내 문서에 시나리오별 파일 선택 기준, 적용 순서, 비대상이 같은 문서 안에서 닫힌 뒤 PASS를 줬다."
  - "검증 관점 리뷰는 `rerun-unacknowledge`가 현재 어떤 준비 데이터 파일 조합과 확인 SQL을 재사용하는지 안내 문서에 명시한 뒤 PASS를 줬다."
  - "가독성 리뷰는 각 example.sql 머리말에 시나리오, 적용 시점, 다음 단계/선행 단계를 직접 적은 뒤 새 작업자가 파일만 읽고도 절차를 따라갈 수 있다고 PASS를 줬다."
next_task_warnings:
  - "후속 spec에서 example.sql 실제 SQL을 넣을 때도 현재 문서가 잡아 둔 시나리오별 파일 조합과 적용 시점을 그대로 유지해야 한다."
  - "안내 문서와 준비 데이터 파일을 함께 바꿀 때는 `문서만으로 따라갈 수 있는지`를 먼저 보고, 활성 spec/task 문서 의존 문구가 다시 들어오지 않게 주의해야 한다."
error_signature:
test_result_summary: "준비 데이터 안내 문서 마감 보강 후 targeted test와 full cleanTest test가 모두 통과했다. 이번 task는 문서 마감 단계이므로 대표 실제 앱/H2 검증은 비대상으로 유지했고, 그 이유를 안내 문서와 retrospective에 함께 남겼다."
snapshot_hashes: []
---

# 작업 회고

## 작업 요약
- `manual-rerun-response-seed-guide.md`에 시나리오별 파일 선택 기준 표를 추가했다.
- 안내 문서 상단의 보조 문구를 정리해, 준비 데이터 적용 순서와 체크리스트는 이 문서와 준비 데이터 파일만으로 따라갈 수 있다는 기준을 명확히 했다.
- 각 example.sql 머리말에 시나리오, 적용 시점, 다음 단계 또는 선행 단계를 직접 적어 안내 문서와 준비 데이터 파일의 연결을 닫았다.

## 실패 요약
- 코드나 테스트 실패는 없었다.
- 다만 첫 문서 마감 검토에서 `manual-rerun-seed-application-guide-readiness-verified` 기준을 아직 충분히 닫지 못했다는 `NEEDS_CHANGES`가 나왔다.
- 특히 `rerun-unacknowledge` 시나리오가 어떤 파일 조합을 재사용하는지 문서만으로 바로 보이지 않는 점이 핵심 지적이었다.

## Root Cause
- 문서 마감 단계에서 중요한 건 새 내용을 더 붙이는 것이 아니라, 안내 문서와 준비 데이터 파일만으로 절차를 재구성할 수 있게 경계를 닫는 것이다.
- 그런데 처음 마감 검토 상태에서는 활성 spec/task 문서를 더 보라는 보조 문구와, 시나리오별 파일 조합이 없는 안내 문서 때문에 문서만으로 읽는 기준이 약했다.
- 그래서 이번 task는 문서 범위를 넓히지 않고, 안내 문서 내부 정합성과 준비 데이터 파일 머리말 연결을 보강하는 방식으로 해결했다.

## AGENTS 체크 결과
- linked issue `#95`를 `TASK-0004`와 1:1로 유지했다.
- targeted test와 full `cleanTest test`를 순차 실행했다.
- 이번 task는 문서 마감 단계이므로 대표 실제 앱/H2 검증은 비대상으로 두는 판단이 타당하다는 3개 관점 PASS를 받았다.
- 문서와 준비 데이터 파일만으로 절차를 따라갈 수 있는지 문서 마감 검토를 한 번 더 돌리고, `NEEDS_CHANGES`를 수정한 뒤 재검증했다.

## 근거 Artifact
- 문서
  - [manual-rerun-response-seed-guide.md](/home/seaung13/workspace/agile-runner/docs/manual-rerun-response-seed-guide.md)
- 준비 데이터 파일
  - [retry-source-execution-seed.example.sql](/home/seaung13/workspace/agile-runner/src/test/resources/manual-rerun-response-seed/source-execution/retry-source-execution-seed.example.sql)
  - [rerun-acknowledge-action-history-seed.example.sql](/home/seaung13/workspace/agile-runner/src/test/resources/manual-rerun-response-seed/control-action-history/rerun-acknowledge-action-history-seed.example.sql)
  - [rerun-runtime-evidence-check.example.sql](/home/seaung13/workspace/agile-runner/src/test/resources/manual-rerun-response-seed/runtime-evidence/rerun-runtime-evidence-check.example.sql)
  - [retry-runtime-evidence-check.example.sql](/home/seaung13/workspace/agile-runner/src/test/resources/manual-rerun-response-seed/runtime-evidence/retry-runtime-evidence-check.example.sql)
- 현재 활성 기준 문서
  - [tasks.md](/home/seaung13/workspace/agile-runner/.agents/active/tasks.md)
  - [SPEC-0024-manual-rerun-seed-application-procedure.json](/home/seaung13/workspace/agile-runner/.agents/criteria/SPEC-0024-manual-rerun-seed-application-procedure.json)
- 검증 명령
  - targeted: `./scripts/gradlew-java21.sh --no-daemon test --tests '*ManualRerunResponseGuideFixtureTest' --tests '*ManualRerunControllerTest' --tests '*ManualRerunServiceTest' --tests '*ManualRerunRetryServiceTest' --tests '*ManualRerunQueryServiceTest' --tests '*ManualRerunExecutionListServiceTest' --tests '*ManualRerunControlActionHistoryServiceTest' --tests '*ManualRerunControlActionServiceTest' --console=plain`
  - full: `./scripts/gradlew-java21.sh --no-daemon cleanTest test --console=plain`

## 다음 Task 경고사항
- 후속 `SPEC-0025`에서 example.sql 실제 SQL을 보강할 때는, 이번 spec이 닫은 `시나리오별 파일 조합`, `적용 시점`, `앱 기동 전/후 경계`를 그대로 유지해야 한다.
- 안내 문서와 준비 데이터 파일을 같이 바꿀 때는 먼저 `문서와 준비 데이터 파일만으로 따라갈 수 있는지`를 다시 점검하고, 보조 문서 의존 문구가 다시 들어오지 않게 주의해야 한다.

## 제안 필요 여부
- 없음
- 이번 task의 교훈은 새 workflow 규칙 부족보다, 문서 마감 단계에서는 안내 문서 내부 정합성과 준비 데이터 파일 머리말 연결을 끝까지 닫아야 한다는 점에 가깝다.
