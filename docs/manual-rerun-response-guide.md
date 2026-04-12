# 운영용 조회 응답 가이드

## 문서 목적
이 문서는 운영자가 수동 재실행 관련 응답을 읽을 때 각 응답이 어떤 질문에 답하는지 빠르게 이해하도록 돕는 가이드다.
이 문서의 목적은 새 기능을 정의하는 것이 아니라, 이미 존재하는 rerun, retry, query, list, history, action 응답의 역할을 운영자 관점에서 구분하는 것이다.

## 먼저 정리할 기본 원칙
- 같은 `executionKey`라도 모든 응답이 같은 역할을 가지지 않는다.
- `query`와 `list`는 현재 상태를 읽기 위한 응답이다.
- `history`는 과거 관리자 조치 timeline을 읽기 위한 응답이다.
- `action` 응답은 방금 수행한 관리자 조치 결과를 읽기 위한 응답이다.
- `rerun`과 `retry` 응답은 새 실행을 시작한 직후의 결과를 읽기 위한 응답이다.

## representative 검증에서 읽는 기준
- representative rerun execution 1건은 `rerun -> query -> action -> history -> list` 순서로 읽는다.
- representative rerun execution에서는 `query`를 관리자 조치 전 현재 상태로 읽고, `action`, `history`, `list`는 `ACKNOWLEDGE` 적용 직후 현재 상태로 읽는다.
- representative retry execution 1건은 `retry` 응답을 기준으로 읽고, 필요하면 `list` row에서 `retrySourceExecutionKey`가 같은 의미로 노출되는지 함께 확인한다.
- 즉, 같은 execution이라도 응답을 읽는 시점이 다를 수 있고, `query.availableActions`와 `action/list/history.currentActionState.availableActions`가 다르게 보이면 먼저 조치 전/후 시점 차이를 확인한다.
- representative rerun execution에서는 `query.availableActions`, `action.availableActions`, `history.currentActionState.availableActions`, `list.availableActions`와 `list.latestAction*`, `history.currentActionState.latestAction*`가 같은 흐름으로 이어지는지 함께 본다.
- representative retry execution에서는 `retry.executionKey`, `retry.retrySourceExecutionKey`, `list.retrySourceExecutionKey`, `list.executionStartType`가 서로 같은 관계를 설명하는지 함께 본다.

## 예시 기준 파일 위치
아래 예시는 이후 자동 검증에서 기준 파일로 재사용할 수 있도록 `src/test/resources/manual-rerun-response-guide/` 아래에 같이 둔다.

| 예시 | 예시 기준 파일 |
| --- | --- |
| rerun 시작 응답 | `src/test/resources/manual-rerun-response-guide/rerun-start-response.json` |
| retry 시작 응답 | `src/test/resources/manual-rerun-response-guide/retry-start-response.json` |
| rerun query 응답 | `src/test/resources/manual-rerun-response-guide/rerun-query-before-acknowledge.json` |
| rerun list 응답 | `src/test/resources/manual-rerun-response-guide/rerun-list-after-acknowledge.json` |
| retry list row | `src/test/resources/manual-rerun-response-guide/retry-list-row.json` |
| rerun history 응답 | `src/test/resources/manual-rerun-response-guide/rerun-history-after-acknowledge.json` |
| rerun action 응답 | `src/test/resources/manual-rerun-response-guide/rerun-action-after-acknowledge.json` |

이 기준 파일은 guide 예시와 1:1로 대응하는 문서용 fixture다. 다음 task에서는 이 기준 파일과 controller/service black-box 기대값을 연결해 drift를 자동 검출한다.

## 응답별로 답하는 질문

| 응답 | 운영자가 확인하는 질문 |
| --- | --- |
| `POST /reviews/rerun` | 새 수동 재실행을 시작했는가 |
| `POST /reviews/rerun/{executionKey}/retry` | 기존 실행을 기준으로 재시도를 시작했는가 |
| `GET /reviews/rerun/{executionKey}` | 특정 실행의 현재 상태가 무엇인가 |
| `GET /reviews/rerun/executions` | 여러 실행 중 지금 봐야 할 실행이 무엇인가 |
| `GET /reviews/rerun/{executionKey}/actions/history` | 이 실행에 대해 과거에 어떤 관리자 조치가 있었는가 |
| `POST /reviews/rerun/{executionKey}/actions` | 방금 요청한 관리자 조치가 적용됐는가 |

## 운영자가 읽는 기본 순서
1. 여러 실행 중 대상을 찾을 때는 `list`를 먼저 본다.
2. 특정 실행의 현재 상태를 확인할 때는 `query`를 본다.
3. 과거 관리자 조치 흐름을 확인할 때는 `history`를 본다.
4. 새 수동 재실행을 시작한 직후 결과는 `rerun` 응답에서 확인한다.
5. 기존 실행을 기준으로 재시도를 시작한 직후 결과는 `retry` 응답에서 확인한다.
6. 방금 누른 관리자 조치 적용 결과는 `action` 응답에서 확인한다.

## 응답별 책임

### rerun 응답
- 새 수동 재실행을 시작한 직후 결과를 보여 준다.
- 핵심 관심사는 실행이 시작됐는지와 어떤 실행 모드로 시작됐는지다.
- 자세한 현재 상태 조회는 이후 `query`에서 다시 확인한다.
- 아래 예시는 representative rerun execution을 시작한 직후 응답이다.
- 예시 기준 파일: `src/test/resources/manual-rerun-response-guide/rerun-start-response.json`

예시:
```json
{
  "executionKey": "EXECUTION:MANUAL_RERUN:example-rerun",
  "executionControlMode": "DRY_RUN",
  "writePerformed": false,
  "executionStatus": "FAILED",
  "errorCode": "GITHUB_APP_CONFIGURATION_MISSING",
  "failureDisposition": "MANUAL_ACTION_REQUIRED"
}
```

### retry 응답
- 기존 실행을 기준으로 새 재시도를 시작한 직후 결과를 보여 준다.
- 핵심 관심사는 새 execution과 원본 execution이 어떻게 연결되는지다.
- 자세한 현재 상태 조회는 이후 `query`에서 다시 확인한다.
- 아래 예시는 representative retry execution을 시작한 직후 응답이다.
- 예시 기준 파일: `src/test/resources/manual-rerun-response-guide/retry-start-response.json`

예시:
```json
{
  "executionKey": "EXECUTION:MANUAL_RERUN:example-retry-derived",
  "retrySourceExecutionKey": "EXECUTION:MANUAL_RERUN:example-retry-source",
  "executionControlMode": "DRY_RUN",
  "writePerformed": false,
  "executionStatus": "FAILED",
  "errorCode": "GITHUB_APP_CONFIGURATION_MISSING",
  "failureDisposition": "MANUAL_ACTION_REQUIRED"
}
```

### query 응답
- 특정 execution 하나의 현재 상태를 보여 준다.
- 운영자는 이 응답으로 현재 실패 상태, 오류 코드, 조치 필요 여부를 읽는다.
- 과거 관리자 조치 timeline 전체는 포함하지 않는다.
- 아래 예시는 representative rerun execution을 관리자 조치 전에 읽은 응답이다.
- 예시 기준 파일: `src/test/resources/manual-rerun-response-guide/rerun-query-before-acknowledge.json`

예시:
```json
{
  "executionKey": "EXECUTION:MANUAL_RERUN:example-rerun",
  "executionControlMode": "DRY_RUN",
  "writePerformed": false,
  "executionStatus": "FAILED",
  "errorCode": "GITHUB_APP_CONFIGURATION_MISSING",
  "failureDisposition": "MANUAL_ACTION_REQUIRED",
  "availableActions": [
    "ACKNOWLEDGE"
  ]
}
```

### list 응답
- 여러 execution의 현재 상태를 한 번에 요약해서 보여 준다.
- 운영자는 이 응답으로 지금 어떤 execution을 더 자세히 봐야 하는지 고른다.
- 개별 execution의 과거 timeline 전체는 포함하지 않는다.
- 아래 예시는 representative rerun execution에 `ACKNOWLEDGE`를 적용한 뒤 list row를 읽은 상태다.
- 예시 기준 파일: `src/test/resources/manual-rerun-response-guide/rerun-list-after-acknowledge.json`

예시:
```json
{
  "executions": [
    {
      "executionKey": "EXECUTION:MANUAL_RERUN:example-rerun",
      "retrySourceExecutionKey": null,
      "executionStartType": "MANUAL_RERUN",
      "executionStatus": "FAILED",
      "executionControlMode": "DRY_RUN",
      "writePerformed": false,
      "errorCode": "GITHUB_APP_CONFIGURATION_MISSING",
      "failureDisposition": "MANUAL_ACTION_REQUIRED",
      "latestAction": "ACKNOWLEDGE",
      "latestActionStatus": "APPLIED",
      "latestActionAppliedAt": "2026-04-12T13:15:00",
      "historyAvailable": true,
      "availableActions": [
        "UNACKNOWLEDGE"
      ]
    }
  ]
}
```

### history 응답
- 특정 execution에 대해 과거에 어떤 관리자 조치가 있었는지 timeline으로 보여 준다.
- `currentActionState`는 현재 조치 상태 요약이고, `actions[]`는 과거 timeline이다.
- 핵심 책임은 과거 조치 이력을 보여 주는 것이다.
- 여러 execution 비교 용도는 아니다.
- 아래 예시는 representative rerun execution에 `ACKNOWLEDGE`를 적용한 뒤 history를 읽은 상태다.
- 예시 기준 파일: `src/test/resources/manual-rerun-response-guide/rerun-history-after-acknowledge.json`

예시:
```json
{
  "executionKey": "EXECUTION:MANUAL_RERUN:example-rerun",
  "currentActionState": {
    "latestAction": "ACKNOWLEDGE",
    "latestActionStatus": "APPLIED",
    "latestActionAppliedAt": "2026-04-12T13:15:00",
    "availableActions": [
      "UNACKNOWLEDGE"
    ]
  },
  "actions": [
    {
      "action": "ACKNOWLEDGE",
      "actionStatus": "APPLIED",
      "note": "운영자 확인 완료",
      "appliedAt": "2026-04-12T13:15:00"
    }
  ]
}
```

### action 응답
- 방금 수행한 관리자 조치 요청의 적용 결과를 보여 준다.
- 운영자는 이 응답으로 지금 누른 조치가 실제로 적용됐는지 확인한다.
- 과거 timeline 전체를 다시 싣는 용도는 아니다.
- 아래 예시는 representative rerun execution에 `ACKNOWLEDGE`를 적용한 직후 action 응답이다.
- 예시 기준 파일: `src/test/resources/manual-rerun-response-guide/rerun-action-after-acknowledge.json`

예시:
```json
{
  "executionKey": "EXECUTION:MANUAL_RERUN:example-rerun",
  "action": "ACKNOWLEDGE",
  "actionStatus": "APPLIED",
  "availableActions": [
    "UNACKNOWLEDGE"
  ],
  "note": "운영자 확인 완료"
}
```

## 핵심 필드 읽는 기준

### executionKey
- 하나의 실행을 식별하는 기준값이다.
- `query`, `history`, `action`은 이 값을 직접 경로로 사용한다.
- `list`는 여러 execution 중 어떤 row를 더 볼지 고르는 출발점으로 이 값을 보여 준다.

### retrySourceExecutionKey
- `retry`가 어떤 원본 execution을 기준으로 시작됐는지 보여 준다.
- `retry` 응답과 `list` row에서만 의미가 있다.
- 현재 상태 자체를 설명하는 필드는 아니고, 실행 간 연결 관계를 설명하는 필드다.

### executionStatus
- execution 자체의 현재 처리 상태다.
- `SUCCEEDED`, `FAILED` 같은 실행 결과를 읽는 필드다.
- 관리자 조치 상태와는 다른 축이다.

### failureDisposition
- 실패 execution이라면 어떤 대응이 필요한지 읽는 필드다.
- 예를 들어 `RETRYABLE`, `MANUAL_ACTION_REQUIRED`처럼 운영 후속 조치 방향을 읽는다.

### availableActions
- 지금 시점에 운영자가 바로 수행할 수 있는 관리자 조치 목록이다.
- `query`, `list`, `history.currentActionState`, `action` 응답에서 보일 수 있다.
- 모두 “현재 시점” 기준이며, 과거 timeline 전체를 보여 주는 필드는 아니다.

### latestAction / latestActionStatus / latestActionAppliedAt
- 가장 마지막에 적용된 관리자 조치 요약이다.
- `list` row에서 현재 조치 상태를 짧게 읽을 때 사용한다.
- 과거 모든 조치 목록은 아니다.

### currentActionState
- `history` 응답 안에서 현재 조치 상태만 따로 요약한 객체다.
- `actions[]` timeline과 구분해서 읽어야 한다.

### actions[]
- 과거 관리자 조치의 timeline이다.
- 현재 상태 하나만 요약하는 필드가 아니라, 과거 순서대로 어떤 조치가 있었는지 읽는 용도다.

### actionStatus
- `action` 응답 또는 `history.actions[]`에서 개별 관리자 조치 row의 적용 결과를 읽는 필드다.
- execution 전체 상태가 아니라 한 번의 조치 결과를 나타낸다.

## 같은 execution을 함께 읽는 방법
- `query`와 `list`는 현재 상태를 읽는 응답이다.
- `history`의 `actions[]`는 과거 timeline을 읽는 응답이다.
- `history`의 현재 조치 상태 요약은 timeline 전체와 구분해서 읽어야 한다.
- `action` 응답은 과거 전체가 아니라 방금 수행한 한 번의 조치 결과다.
- `rerun`과 `retry`는 시작 결과이고, 이후 상태 변화는 `query`, `list`, `history`에서 확인한다.

## 중복 요약 필드 읽는 기준

| 응답 | 현재 상태 요약 | 과거 timeline | 방금 수행한 조치 결과 | 실행 시작 결과 |
| --- | --- | --- | --- | --- |
| `rerun` | 아니오 | 아니오 | 아니오 | 예 |
| `retry` | 아니오 | 아니오 | 아니오 | 예 |
| `query` | 예 | 아니오 | 아니오 | 아니오 |
| `list` | 예 | 아니오 | 아니오 | 아니오 |
| `history.currentActionState` | 예 | 아니오 | 아니오 | 아니오 |
| `history.actions[]` | 아니오 | 예 | 아니오 | 아니오 |
| `action` | 예, 방금 조치 직후 다음 가능 액션 요약 | 아니오 | 예 | 아니오 |

추가 기준:
- `query.availableActions`와 `list.availableActions`는 현재 시점에 가능한 조치라는 같은 의미를 가진다.
- `history.currentActionState.availableActions`도 현재 시점 기준이지만, `history.actions[]`와는 다른 요약 정보다.
- `action.availableActions`는 방금 조치 적용 직후 다음에 가능한 액션을 읽는 값이다.
- `list.latestAction*`와 `history.currentActionState.latestAction*`는 모두 현재 조치 상태 요약이지만, `list`는 여러 execution 요약, `history`는 한 execution의 현재 조치 상태를 더 자세히 보여 주는 위치다.

## representative 검증에서 같이 확인한 항목
- representative rerun execution에서는 아래 해석 기준이 서로 모순되지 않는지 함께 확인했다.
  - `rerun`은 시작 직후 결과
  - `query`는 관리자 조치 전 현재 상태
  - `action`은 방금 적용한 조치 결과
  - `query.availableActions=[ACKNOWLEDGE]`
  - `action.availableActions`, `history.currentActionState.availableActions`, `list.availableActions`는 `ACKNOWLEDGE` 적용 직후 모두 `[UNACKNOWLEDGE]`
  - `history.currentActionState.latestAction*`와 `list.latestAction*`는 `ACKNOWLEDGE` 적용 직후 같은 현재 조치 상태 요약
- representative retry execution에서는 아래 해석 기준이 서로 모순되지 않는지 함께 확인했다.
  - `retry` 응답의 `executionKey`
  - `retry` 응답의 `retrySourceExecutionKey`
  - 같은 derived execution을 반환하는 `list` row의 `retrySourceExecutionKey`
  - 같은 derived execution을 반환하는 `list` row의 `executionStartType=MANUAL_RERUN`
