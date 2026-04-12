---
spec_id: SPEC-0021
summary_status: completed
generated_at: 2026-04-12T11:21:46+09:00
task_ids:
  - TASK-0001
  - TASK-0002
  - TASK-0003
  - TASK-0004
---

# Spec 요약

## 범위 요약
- `SPEC-0021 운영용 조회 응답 예시 자동 검증`은 운영용 조회 응답 가이드의 예시가 이후 코드 변경으로 조용히 낡지 않도록 fixture와 자동 검증 테스트를 연결하는 데 집중했다.
- 이번 spec은 새 endpoint나 응답 필드를 추가하지 않고, 이미 존재하는 rerun, retry, query, list, history, action 응답 예시를 guide, 기준 파일, 자동 검증 테스트로 묶어 drift를 조기에 드러내는 기반을 만들었다.

## 완료된 Task
- `TASK-0001` 예시 자동 검증 안전망 고정
- `TASK-0002` 예시 fixture와 문서 매핑 구조 도입
- `TASK-0003` 예시 자동 검증 테스트 도입
- `TASK-0004` drift 검증과 문서 마감

## 주요 결과
- `docs/manual-rerun-response-guide.md`에서 각 예시가 어떤 기준 파일을 참고하는지 바로 읽을 수 있게 됐다.
- `src/test/resources/manual-rerun-response-guide/` 아래 기준 파일과 `ManualRerunResponseGuideFixtureTest`를 통해 rerun, retry, query, list, history, action 응답 예시 drift를 자동으로 검출하게 됐다.
- guide를 수정할 때 기준 파일과 자동 검증 테스트를 함께 봐야 한다는 문서 경로까지 정리해, 문서만 따로 바뀌는 drift 위험을 줄였다.

## 검증 요약
- targeted test: 통과
- full `cleanTest test`: 통과
- actual app/H2 representative verification: 이번 spec 비대상
- linked issues `#80`, `#81`, `#82`, `#83`: 모두 `CLOSED`

## 이번 spec에서 얻은 점
- representative actual app 검증으로 한 번 닫은 문서 기준도, fixture와 자동 검증 테스트가 없으면 다시 조용히 drift될 수 있다.
- 운영용 문서는 설명 자체보다도 `어떤 기준 파일과 어떤 테스트가 같이 움직이는가`를 함께 적어야 이후 작업자가 안전하게 갱신할 수 있다.
- @WebMvcTest 기반 JSON 비교만으로도 운영용 조회 응답 예시 drift를 충분히 빠르게 감지할 수 있다.

## 남은 위험
- guide 예시와 기준 파일은 자동 검증으로 묶였지만, 후속 작업자가 fixture naming이나 갱신 절차를 헷갈리면 유지보수 비용이 커질 수 있다.
- 현재는 기준 파일 추가/수정 절차가 암묵적이므로, 후속 spec에서 생성 규칙과 갱신 순서를 더 명확히 적을 필요가 있다.

## 다음 spec 후보
- `SPEC-0022 운영용 조회 응답 문서와 fixture 생성 규칙 정리`
