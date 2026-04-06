---
spec_id: SPEC-0003
task_ids:
  - TASK-0001
  - TASK-0002
  - TASK-0003
generated_from_retrospectives:
  - .agents/outer-loop/retrospectives/SPEC-0003/TASK-0001-exception-safety-net.md
  - .agents/outer-loop/retrospectives/SPEC-0003/TASK-0002-common-exception-system.md
  - .agents/outer-loop/retrospectives/SPEC-0003/TASK-0003-runtime-error-code-evidence.md
summary_status: final
generated_at: 2026-04-06T20:50:00+09:00
---

# Spec 요약

## Spec 요약
- `SPEC-0003 예외 체계 정리`는 세 단계로 닫혔다.
- `TASK-0001`에서 기존 웹훅 성공 계약과 핵심 실패 경로 안전망을 먼저 고정했다.
- `TASK-0002`에서 `AgileRunnerException`과 `ErrorCode`를 도입하고, controller request DTO와 GitHub client 위치를 AGENTS 기준에 맞게 정리했다.
- `TASK-0003`에서 `WebhookExecution`과 `AgentExecutionLog`에 오류 코드를 적재하고, local profile 실제 앱 기동과 H2 file DB 조회로 representative failure evidence를 확인했다.

## 반복된 실패 패턴
- 회귀 안전망이 있어도 대표 실패 실행까지 확인하지 않으면 일반 예외 분기 하나가 남아 `error_code = null` 누수가 생길 수 있었다.
- 저장소/H2 스키마 변경 task는 실패 경로 적재뿐 아니라 성공 경로 `null 유지`도 같이 잠가야 mapper 회귀를 막을 수 있었다.
- 실제 representative verification에서는 fresh `delivery_id`와 같은 `execution_key` 기준 조회가 아니면 false negative가 섞일 수 있었다.

## 승인된 제안
- 없음
- `SPEC-0003` 자체에서는 새 AGENTS/workflow proposal까지는 필요하지 않았고, 기존 규칙을 더 엄밀하게 적용하는 쪽으로 정리됐다.

## 열린 위험
- 현재는 오류 코드를 적재하지만, 같은 오류 코드라도 재시도 가능/불가능 분류나 후속 대응 기준은 아직 없다.
- 웹훅 실패 응답은 여전히 예외 전파 중심이라 외부 계약 차원의 에러 응답 설계는 남아 있다.
- representative failure 검증은 수동 절차이므로, 반복 가능성을 더 높이려면 스크립트화 여지가 있다.

## 다음 Spec 결정사항
- 다음 활성 spec은 `SPEC-0004 실패 대응 강화`로 이동한다.
- 시작 시점에는 `errorCode` 기반 실패 분류를 재시도 가능/불가능, 수동 개입 필요 여부 같은 운영 판단 기준으로 확장한다.
