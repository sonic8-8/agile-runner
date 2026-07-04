# Check Result JSON Spec

상태: Draft v0.3
작성일: 2026-06-13
수정일: 2026-06-29
범위: `.agile/check-result.json` 구조 정의

## 결론

Check result JSON은 `agrun check`가 만드는 검사 결과 성적표 파일이다.

- 기본 위치는 Stage root 기준 `.agile/check-result.json`이다.
- 전체 status는 `passed`, `passed_with_warnings`, `needs_changes`, `blocked`, `error` 중 하나다.
- 개별 check status는 `passed`, `failed`, `skipped`, `error` 중 하나다.
- 서버는 원본 JSON을 `StageCheckResult.rawJson`으로 저장하되 그대로 화면에 출력하지 않는다.
- MVP에서는 local path, OS, Node version 같은 환경 정보는 저장하지 않는다.

## 목적

이 파일은 두 가지 용도로만 쓴다.

1. 사용자가 로컬에서 어떤 검사를 통과했는지 확인한다.
2. 서버가 Stage 상태를 갱신할 근거로 사용한다.

## 위치

```text
.agile/check-result.json
```

`agrun check`는 실행할 때마다 Stage root의 파일을 새로 쓴다.

## Top-level 구조

```json
{
  "schemaVersion": 1,
  "mission": {
    "id": "http-server",
    "title": "HTTP 서버 직접 만들기"
  },
  "stage": {
    "id": "request-message-parser",
    "sequence": 1,
    "title": "HTTP 요청 메시지 파서 만들기"
  },
  "status": "needs_changes",
  "summary": {
    "total": 2,
    "passed": 1,
    "failed": 1,
    "warnings": 0,
    "errors": 0
  },
  "checks": [],
  "metadata": {
    "generatedAt": "2026-06-13T00:00:00Z",
    "cliVersion": "0.0.0",
    "manifestVersion": 1,
    "manifestPath": ".agile/stage.yml"
  }
}
```

## Top-level 필드

| 필드 | 설명 |
| --- | --- |
| `schemaVersion` | check result schema version. MVP에서는 `1`만 사용한다. |
| `mission` | 검사 대상 Stage가 속한 Mission 정보 |
| `stage` | 검사 대상 Stage 정보 |
| `status` | 전체 Stage check 상태 |
| `summary` | check 개수 요약. 서버는 별도 컬럼으로 분리하지 않는다. |
| `checks` | 개별 check 결과 목록 |
| `metadata` | 생성 시각, CLI version, manifest version |

## Status 매핑

| JSON status | StageCheckResult | UserMissionStage | 의미 |
| --- | --- | --- | --- |
| `passed` | `PASSED` | `COMPLETED` | 통과 |
| `passed_with_warnings` | `PASSED_WITH_WARNINGS` | `COMPLETED` | 경고는 있지만 완료 가능 |
| `needs_changes` | `NEEDS_CHANGES` | `NEEDS_CHANGES` | 보완 필요 |
| `blocked` | `BLOCKED` | `BLOCKED` | 검사 실행 전제 부족 |
| `error` | `ERROR` | `ERROR` | CLI 또는 서버 오류 |

## Check 항목

예:

```json
{
  "id": "test-command",
  "type": "command",
  "name": "테스트 실행",
  "status": "passed",
  "severity": "error",
  "message": "./gradlew test completed successfully.",
  "details": {
    "command": "./gradlew test",
    "exitCode": 0,
    "durationMs": 3421
  },
  "remediation": null
}
```

개별 status:

- `passed`: check가 통과했다.
- `failed`: check가 실행됐고 조건을 만족하지 못했다.
- `skipped`: 선행 조건이 없어 실행하지 않았다.
- `error`: check 실행 중 CLI 또는 환경 오류가 발생했다.

## 보안과 저장 규칙

- 서버는 JSON 최대 크기를 제한한다.
- 서버는 schema version, Mission, Stage, UserMission 소유권을 검증한다.
- 서버는 raw JSON 전체를 로그에 남기지 않는다.
- 화면에는 raw JSON을 그대로 출력하지 않는다.
- 화면에는 서버가 검증한 status와 필요한 파생 정보만 사용한다.
