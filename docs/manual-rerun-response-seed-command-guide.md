# 운영용 조회 응답 준비 데이터 적용 보조 명령 가이드

## 문서 목적
이 문서는 운영용 조회 응답 대표 검증에서 준비 데이터 SQL을 적용하고, 실제 초안 파일로 재실행/재시도 요청을 실행하고, 앱 종료 뒤 H2 실행 근거를 확인하는 기본 절차를 정리한다.
현재 단계에서는 `prepare-seed.sh`, `run-rerun.sh`, `run-retry.sh`, `collect-evidence.sh`를 실제로 쓰는 흐름을 기준으로 본다.

## 이 문서를 먼저 읽어야 하는 경우
- 대표 검증을 다시 돌릴 때 어떤 스크립트부터 실행해야 하는지 바로 감이 안 잡힐 때
- 예시 SQL은 준비됐지만 실제 검증에서는 어떤 식으로 임시 복사본을 만들어 써야 하는지 헷갈릴 때
- 재시도 응답에서 얻은 파생 실행 키를 이후 조회와 H2 확인에 어디까지 다시 써야 하는지 헷갈릴 때
- 스크립트가 멈춘 뒤 어디까지는 파일이 남고, 어디서부터 사람이 의미를 직접 판단해야 하는지 다시 확인하고 싶을 때

## 실패 로그를 바로 비교할 때 먼저 갈 곳
- 종료 코드와 멈춤 예시는 [종료 코드와 멈춤 실패 사례 예시](#종료-코드와-멈춤-실패-사례-예시) 부터 본다.
- 출력 파일이 비거나 안 남는 경우는 [출력 파일 누락 시 첫 점검 순서](#출력-파일-누락-시-첫-점검-순서) 로 바로 간다.
- H2 조회 실패가 보이면 [H2 잠금과 코드 오류를 나눠 보는 순서](#h2-잠금과-코드-오류를-나눠-보는-순서) 를 바로 본다.

## 같이 읽는 문서
- 시나리오별 준비 데이터 파일 선택 기준과 파일 머리말은 [manual-rerun-response-seed-guide.md](/home/seaung13/workspace/agile-runner/docs/manual-rerun-response-seed-guide.md) 에서 먼저 본다.
- 응답 의미와 대표 검증 결과를 어떻게 읽는지는 [manual-rerun-response-guide.md](/home/seaung13/workspace/agile-runner/docs/manual-rerun-response-guide.md) 에서 본다.
- 실제 초안 파일 기준 대표 검증이 어떻게 맞았는지는 [TASK-0004-script-application-representative-verified.md](/home/seaung13/workspace/agile-runner/.agents/outer-loop/retrospectives/SPEC-0031/TASK-0004-script-application-representative-verified.md) 를 참고한다.

## 시작 전에 먼저 확인할 근거
- 직전 단계 전체 판단은 [SPEC-0031-summary.md](/home/seaung13/workspace/agile-runner/.agents/outer-loop/retrospectives/SPEC-0031/SPEC-0031-summary.md) 에서 먼저 본다.
- 마지막 대표 검증에서 어떤 실행 키, 전달 식별자, 출력 파일을 함께 남겼는지는 [TASK-0004-script-application-representative-verified.md](/home/seaung13/workspace/agile-runner/.agents/outer-loop/retrospectives/SPEC-0031/TASK-0004-script-application-representative-verified.md) 에서 먼저 본다.
- 현재 자동 검증 안전망은 아래 테스트가 맡는다.
  - [ManualRerunRunFlowScriptTest.java](/home/seaung13/workspace/agile-runner/src/test/java/com/agilerunner/client/agentruntime/ManualRerunRunFlowScriptTest.java)
  - [ManualRerunSeedCommandScriptTest.java](/home/seaung13/workspace/agile-runner/src/test/java/com/agilerunner/client/agentruntime/ManualRerunSeedCommandScriptTest.java)
  - [ManualRerunResponseSeedSqlTest.java](/home/seaung13/workspace/agile-runner/src/test/java/com/agilerunner/client/agentruntime/ManualRerunResponseSeedSqlTest.java)
  - [ManualRerunResponseSeedEvidenceSqlTest.java](/home/seaung13/workspace/agile-runner/src/test/java/com/agilerunner/client/agentruntime/ManualRerunResponseSeedEvidenceSqlTest.java)
- 위 근거와 테스트가 그대로 유효하면, 다음 단계는 새 안전망을 만드는 대신 현재 가이드 정리부터 시작해도 된다.


## 이전 작업에서 유지하는 기준

### 변경 시 함께 갱신해야 하는 문서와 기준 파일
| 변경한 대상 | 같이 갱신할 문서 또는 기준 파일 | 같이 확인할 자동 검증 | 이유 |
| --- | --- | --- | --- |
| `prepare-seed.sh` 또는 준비 데이터 적용 방식 | 이 문서의 `빠른 적용 순서`, [manual-rerun-response-seed-guide.md](/home/seaung13/workspace/agile-runner/docs/manual-rerun-response-seed-guide.md), 준비 데이터 예시 SQL | [ManualRerunSeedCommandScriptTest.java](/home/seaung13/workspace/agile-runner/src/test/java/com/agilerunner/client/agentruntime/ManualRerunSeedCommandScriptTest.java), [ManualRerunResponseSeedSqlTest.java](/home/seaung13/workspace/agile-runner/src/test/java/com/agilerunner/client/agentruntime/ManualRerunResponseSeedSqlTest.java), [ManualRerunResponseSeedEvidenceSqlTest.java](/home/seaung13/workspace/agile-runner/src/test/java/com/agilerunner/client/agentruntime/ManualRerunResponseSeedEvidenceSqlTest.java) | 준비 데이터 적용 순서와 SQL 예시, H2 조회 근거가 함께 어긋나기 쉽다. |
| `run-rerun.sh`, `run-retry.sh`, `collect-evidence.sh` | 이 문서의 `빠른 적용 순서`, `대표 검증 결과를 읽는 순서`, [manual-rerun-response-guide.md](/home/seaung13/workspace/agile-runner/docs/manual-rerun-response-guide.md), 마지막 대표 검증 회고 | [ManualRerunRunFlowScriptTest.java](/home/seaung13/workspace/agile-runner/src/test/java/com/agilerunner/client/agentruntime/ManualRerunRunFlowScriptTest.java) | 요청 순서, 출력 파일, H2 근거 수집 방식이 같이 변하므로 한 파일만 바꾸면 의미가 끊긴다. |
| 응답 해석 기준 또는 실행 키 읽는 위치 | [manual-rerun-response-guide.md](/home/seaung13/workspace/agile-runner/docs/manual-rerun-response-guide.md), 이 문서의 `대표 검증 결과를 읽는 순서`, 마지막 대표 검증 회고 | 응답 관련 자동 검증 테스트, [ManualRerunRunFlowScriptTest.java](/home/seaung13/workspace/agile-runner/src/test/java/com/agilerunner/client/agentruntime/ManualRerunRunFlowScriptTest.java) | 응답 의미가 바뀌면 보조 명령 가이드의 읽는 순서와 마지막 판단 근거도 함께 달라진다. |

### 문서와 기준 파일 역할 경계
| 파일 | 먼저 바꿔야 하는 경우 | 이 파일만 바꾸면 안 되는 경우 |
| --- | --- | --- |
| [manual-rerun-response-seed-command-guide.md](/home/seaung13/workspace/agile-runner/docs/manual-rerun-response-seed-command-guide.md) | 적용 순서, 입력·출력 흐름, 출력 파일 읽는 순서가 달라질 때 | 스크립트나 출력 파일 이름이 바뀌었는데 회고, 응답 가이드, 자동 검증은 그대로 둘 때 |
| [manual-rerun-response-guide.md](/home/seaung13/workspace/agile-runner/docs/manual-rerun-response-guide.md) | 응답 필드 의미, 현재 상태 해석, 조치 응답 읽는 기준이 달라질 때 | 보조 명령 가이드의 읽는 순서와 대표 검증 회고를 그대로 둘 때 |
| `src/test/java/com/agilerunner/client/agentruntime/` 아래 스크립트 관련 테스트 | 스크립트 입력, 출력 파일 이름, 실행 키 추출, H2 조회 방식이 달라질 때 | 문서만 바꾸고 자동 검증 기대값은 그대로 둘 때 |
| 마지막 대표 검증 회고와 단계 요약 | 대표 검증 결과 파일 경로, 대표 실행 키, 전달 식별자, 마지막 판단 문장이 달라질 때 | 새 기준을 문서에만 적고 마지막 실제 근거 문서는 갱신하지 않을 때 |

위 두 섹션은 이전 작업에서 이미 정리한 기준을 유지하는 참고 범위다. 이번 작업에서 직접 닫는 범위는 아래 `출력 파일 이름 변경과 문서 어긋남 감지 기준`이다. 아래의 `빠른 적용 순서`, `대표 검증 결과를 읽는 순서`, `파일별 역할과 마지막 판단 기준`은 계속 참고 기준으로 유지한다.

## 출력 파일 이름 변경과 문서 어긋남 감지 기준
### 먼저 보는 순서
| 순서 | 먼저 확인할 대상 | 여기서 먼저 잡는 어긋남 | 다음으로 이어서 볼 대상 |
| --- | --- | --- | --- |
| 1 | `run-rerun.sh`, `run-retry.sh`, `collect-evidence.sh`가 저장하는 출력 파일 이름 | 스크립트가 더 이상 가이드 문서에 적힌 파일 이름을 만들지 않는 경우 | 이 문서의 `빠른 적용 순서`, `빠른 적용 순서에서 다시 쓰는 입력, 출력, 다음 단계 입력` |
| 2 | [ManualRerunRunFlowScriptTest.java](/home/seaung13/workspace/agile-runner/src/test/java/com/agilerunner/client/agentruntime/ManualRerunRunFlowScriptTest.java), [ManualRerunSeedCommandScriptTest.java](/home/seaung13/workspace/agile-runner/src/test/java/com/agilerunner/client/agentruntime/ManualRerunSeedCommandScriptTest.java) | 출력 파일 이름, 실행 키 추출, H2 조회 파일 이름이 자동 검증 기대값과 어긋난 경우 | 가이드 문서 본문과 관련 기준 파일 |
| 3 | [manual-rerun-response-seed-command-guide.md](/home/seaung13/workspace/agile-runner/docs/manual-rerun-response-seed-command-guide.md) 의 파일명 언급 구간 | 문서가 이전 파일 이름을 계속 가리키는 경우 | [manual-rerun-response-guide.md](/home/seaung13/workspace/agile-runner/docs/manual-rerun-response-guide.md), 마지막 대표 검증 회고 |
| 4 | [manual-rerun-response-guide.md](/home/seaung13/workspace/agile-runner/docs/manual-rerun-response-guide.md) 와 마지막 대표 검증 회고 | 응답 의미 설명이나 대표 검증 근거가 이전 출력 파일 이름에 묶여 있는 경우 | 단계 요약과 최종 회고 |

### 가장 먼저 잡아야 하는 어긋남
- 스크립트가 실제로 남기는 출력 파일 이름과 가이드 문서에 적힌 출력 파일 이름이 다르면, 문서보다 스크립트와 테스트 기대값을 먼저 확인한다.
- 테스트가 먼저 깨지면 문서 설명이 맞더라도 기준 파일과 가이드 문서가 함께 갱신돼야 하는 신호로 본다.
- 문서는 맞아 보이는데 마지막 대표 검증 회고가 이전 파일 이름을 계속 가리키면, 현재 절차 설명이 아니라 마지막 실제 근거 문서가 어긋난 상태로 본다.
- 출력 파일 이름을 실제로 바꾸지 않았는데 문서 표현만 바뀌면, 가이드 문서와 회고의 용어 차이인지 먼저 확인하고 실제 스크립트와 테스트 기대값은 유지한다.

### 문서만 읽고 다시 찾는 점검 순서
1. 먼저 스크립트가 어떤 파일을 만든다고 가정하는지 `빠른 적용 순서`와 `입력, 출력, 다음 단계 입력` 표에서 본다.
2. 그다음 스크립트 관련 테스트가 같은 파일 이름을 기대하는지 확인한다.
3. 이후 응답 가이드와 마지막 대표 검증 회고가 같은 파일 이름을 근거로 설명하는지 본다.
4. 마지막으로 단계 요약과 회고 문서에서 변경 이유와 현재 기준을 다시 확인한다.

### 자동 검증과 수동 점검 경계
- 자동 검증은 파일 이름, 실행 키 추출, H2 조회 결과 저장 위치가 바뀌었을 때 가장 먼저 경고를 내는 위치다.
- 수동 점검은 그 경고가 문서 어긋남인지, 대표 검증 근거 미갱신인지, 실제 스크립트 변경인지 구분하는 단계다.
- 이번 작업에서는 출력 파일 이름 자체를 바꾸지 않고, 어긋남을 어디서 먼저 확인해야 하는지만 정리한다.

## 유지 보수 체크리스트와 단계 마감 기준
### 유지 보수 체크리스트
1. 먼저 이번 변경이 `준비 데이터 적용 방식`, `대표 검증 실행 흐름`, `출력 파일 이름`, `응답 해석`, `근거 문서` 중 어디에 걸리는지 정한다.
2. 변경 대상에 맞춰 이 문서의 `변경 시 함께 갱신해야 하는 문서와 기준 파일` 표에서 같이 열어야 하는 문서와 테스트를 바로 찾는다.
3. 출력 파일 이름이나 경로와 연결되면 `출력 파일 이름 변경과 문서 어긋남 감지 기준` 순서대로 스크립트, 자동 검증, 가이드 문서, 대표 검증 회고를 차례로 확인한다.
4. 관련 대상 테스트를 먼저 순차 실행해 자동 검증 기대값이 아직 맞는지 본다.
5. 문서 설명을 고친 뒤에는 응답 가이드, 보조 명령 가이드, 마지막 대표 검증 회고 중 어떤 문서까지 같이 바꿔야 하는지 다시 확인한다.
6. 전체 테스트를 순차 실행해 문서 정리 작업이 기존 자동 검증과 충돌하지 않는지 확인한다.
7. 실제 앱/H2 대표 검증이 이번 변경 범위가 아니라면 비대상 사유를 회고에 남긴다. 실제 대표 검증 범위라면 기존 절차 순서에 따라 별도 작업에서 수행한다.
8. 마지막으로 회고, 최신 포인터, 현재 단계 요약 문서 또는 다음 단계 경고를 같이 정리한다.

### 단계 마감 기준
- 유지 기준 정리 작업은 아래가 모두 맞아야 마감으로 본다.
  - 가이드 문서에서 바뀐 기준과 유지한 기준 경계가 분명하다.
  - 관련 대상 테스트와 전체 테스트가 순차 실행으로 통과한다.
  - 실제 앱/H2 대표 검증이 비대상이면 그 이유가 회고에 남아 있다.
  - 다음 변경에서 무엇을 먼저 보고 무엇을 나중에 봐야 하는지 문서만으로 다시 따라갈 수 있다.
  - 최신 포인터와 단계 요약 문서가 현재 상태를 정확히 가리킨다.

### 마지막으로 다시 확인할 질문
- 이번 변경이 기존 스크립트 이름이나 출력 파일 이름을 실제로 바꾸는 작업인지, 아니면 유지 기준만 정리하는 작업인지 분명한가.
- 자동 검증이 먼저 잡아야 하는 어긋남과 사람이 직접 해석해야 하는 부분이 문서에서 섞이지 않았는가.
- 마지막 대표 검증 회고와 단계 요약이 여전히 현재 기준 파일 이름과 읽는 순서를 설명하는가.

## 빠른 적용 순서
### 재실행 대표 검증
1. 공통 환경 변수와 출력 디렉토리를 먼저 잡는다.
2. 재실행용 임시 SQL 복사본과 정리 SQL을 만든다.
3. `prepare-seed.sh`로 정리 SQL과 적용 SQL을 실행한다.
4. `run-rerun.sh`로 앱 기동, 단건 조회, 이력 조회, 관리자 조치, 조치 후 단건 조회를 한 번에 실행한다.
5. `collect-evidence.sh`로 앱 종료 뒤 H2 실행 근거를 모은다.
6. `rerun-query-before.json`, `rerun-history.json`, `rerun-action.json`, `rerun-query-after.json`, `rerun-webhook-execution.txt`, `rerun-action-audit.txt`를 같은 실행 키 기준으로 읽는다.

### 재시도 대표 검증
1. 공통 환경 변수와 출력 디렉토리를 먼저 잡는다.
2. 재시도 원본 실행용 임시 SQL 복사본과 정리 SQL을 만든다.
3. `prepare-seed.sh`로 정리 SQL과 적용 SQL을 실행한다.
4. `run-retry.sh`로 앱 기동, 재시도 요청, 파생 실행 키 추출, 파생 실행 단건 조회를 한 번에 실행한다.
5. `retry-derived-execution-key.txt`에서 파생 실행 키를 다시 읽어 환경 변수로 잡는다.
6. `collect-evidence.sh`로 앱 종료 뒤 파생 실행 기준 H2 실행 근거를 모은다.
7. `retry-response.json`, `retry-derived-query.json`, `retry-webhook-execution.txt`, `retry-agent-execution-log.txt`를 파생 실행 키 기준으로 읽고, 원본 실행 연결은 `retrySourceExecutionKey`와 `WEBHOOK_EXECUTION.retry_source_execution_key`에서 다시 확인한다.

## 빠른 적용 순서에서 다시 쓰는 입력, 출력, 다음 단계 입력
| 단계 | 바로 넣는 입력 | 바로 남는 출력 | 다음 단계에서 다시 쓰는 값 |
| --- | --- | --- | --- |
| 공통 환경 준비 | `APP_PORT`, `BASE_URL`, `JDBC_URL`, `H2_JAR`, `VERIFY_TS`, `OUTPUT_ROOT` | `RERUN_DIR`, `RETRY_DIR` | 재실행/재시도 모든 단계 |
| 재실행 임시 SQL 준비 | `RERUN_SUFFIX`, 예시 SQL 경로 | `rerun-apply.sql`, `rerun-reset.sql`, `RERUN_EXECUTION_KEY`, `RERUN_DELIVERY_ID` | `prepare-seed.sh`, `run-rerun.sh`, `collect-evidence.sh` |
| 재시도 임시 SQL 준비 | `RETRY_SOURCE_SUFFIX`, 예시 SQL 경로 | `retry-apply.sql`, `retry-reset.sql`, `RETRY_SOURCE_EXECUTION_KEY`, `RETRY_SOURCE_DELIVERY_ID` | `prepare-seed.sh`, `run-retry.sh` |
| `prepare-seed.sh` | `OUTPUT_DIR`, `SEED_RESET_SQL`, `SEED_APPLY_SQL`, `JDBC_URL`, `H2_JAR` | `prepare.log` | 다음 단계가 정리/적용 성공 여부를 여기서 먼저 확인 |
| `run-rerun.sh` | `OUTPUT_DIR`, `BASE_URL`, `RERUN_EXECUTION_KEY`, `RERUN_ACTION_BODY` | `rerun-query-before.json`, `rerun-history.json`, `rerun-action.json`, `rerun-query-after.json`, `app.pid` | `collect-evidence.sh`, 수동 의미 확인 |
| `run-retry.sh` | `OUTPUT_DIR`, `BASE_URL`, `RETRY_SOURCE_EXECUTION_KEY`, `RETRY_REQUEST_BODY` | `retry-response.json`, `retry-derived-execution-key.txt`, `retry-derived-query.json`, `app.pid` | 파생 실행 키 추출, `collect-evidence.sh`, 수동 의미 확인 |
| 파생 실행 키 다시 읽기 | `retry-derived-execution-key.txt` | `RETRY_DERIVED_EXECUTION_KEY` | `collect-evidence.sh`, H2 조회 해석 |
| `collect-evidence.sh` 재실행 | `OUTPUT_DIR`, `EVIDENCE_MODE=rerun`, `RERUN_EXECUTION_KEY` | `rerun-webhook-execution.txt`, `rerun-action-audit.txt` | 최종 수동 판단 |
| `collect-evidence.sh` 재시도 | `OUTPUT_DIR`, `EVIDENCE_MODE=retry`, `RETRY_DERIVED_EXECUTION_KEY` | `retry-webhook-execution.txt`, `retry-agent-execution-log.txt` | 최종 수동 판단 |

여기까지가 새 작업자가 적용 순서와 입력/출력 흐름을 다시 잡을 때 먼저 보는 기준이다.
실행 키 재사용 기준, 출력 파일 해석 기준, 종료 코드와 마감 판단은 아래 참고 섹션에 남기고, 이 문서에서는 위치를 빠르게 찾게 만드는 데까지만 정리한다.

## 대표 검증 결과를 읽는 순서
### 재실행 대표 검증
| 순서 | 먼저 여는 파일 | 여기서 먼저 확인하는 것 | 같이 대조할 문서나 파일 | 마지막에 닫는 판단 |
| --- | --- | --- | --- | --- |
| 1 | `prepare.log` | 정리 SQL과 적용 SQL이 정상 종료됐는지 | 없음 | 준비 단계 통과 여부 |
| 2 | `rerun-query-before.json` | 조치 전 현재 상태와 `availableActions` | [manual-rerun-response-guide.md](/home/seaung13/workspace/agile-runner/docs/manual-rerun-response-guide.md)의 `query` 설명 | 조치 전 현재 상태가 기대와 맞는지 |
| 3 | `rerun-history.json`, `rerun-action.json`, `rerun-query-after.json` | 조치 직후 상태가 어떻게 바뀌었는지 | 응답 가이드의 `history`, `action`, `query` 설명 | 조치 후 응답 의미가 서로 맞는지 |
| 4 | `rerun-webhook-execution.txt`, `rerun-action-audit.txt` | H2 실행 근거와 audit row가 남았는지 | 위 응답 파일과 같은 실행 키 | 응답과 H2가 같은 실행을 설명하는지 |
| 5 | `TASK-0004-script-application-representative-verified.md` | 실제 대표 검증에서 어떤 결론으로 닫았는지 | 위 출력 파일 전부 | 최종 사람 판단 근거 |

### 재시도 대표 검증
| 순서 | 먼저 여는 파일 | 여기서 먼저 확인하는 것 | 같이 대조할 문서나 파일 | 마지막에 닫는 판단 |
| --- | --- | --- | --- | --- |
| 1 | `prepare.log` | 정리 SQL과 적용 SQL이 정상 종료됐는지 | 없음 | 준비 단계 통과 여부 |
| 2 | `retry-response.json` | 원본 실행 기준 재시도 응답과 파생 실행 키 | 응답 가이드의 `retry` 설명 | 어떤 파생 실행을 이후 단계에서 계속 볼지 |
| 3 | `retry-derived-execution-key.txt`, `retry-derived-query.json` | 파생 실행 키와 파생 실행 현재 상태 | 응답 가이드의 `query` 설명 | 파생 실행 응답 의미가 기대와 맞는지 |
| 4 | `retry-webhook-execution.txt`, `retry-agent-execution-log.txt` | H2에서 파생 실행과 원본 실행 연결이 보이는지 | `retry-response.json`, `retrySourceExecutionKey` | 원본 실행과 파생 실행 연결이 맞는지 |
| 5 | `TASK-0004-script-application-representative-verified.md` | 실제 대표 검증에서 어떤 결론으로 닫았는지 | 위 출력 파일 전부 | 최종 사람 판단 근거 |

## 파일별 역할과 마지막 판단 기준
| 파일 또는 문서 | 기본 역할 | 이 파일만으로 끝내면 안 되는 이유 | 마지막 판단에서 맡는 위치 |
| --- | --- | --- | --- |
| `summary.json` | 대표 검증 결과를 기계적으로 다시 읽기 위한 요약 | 내부 키와 파생 값이 많아 사람 설명 없이 읽기 어렵다 | 빠른 인덱스 |
| [manual-rerun-response-guide.md](/home/seaung13/workspace/agile-runner/docs/manual-rerun-response-guide.md) | 각 응답이 어떤 질문에 답하는지 설명 | 실제 대표 검증에서 어떤 파일이 남았는지 직접 보여 주지는 않는다 | 응답 의미 해석 기준 |
| `prepare.log`, `rerun-*.json`, `retry-*.json`, `*-webhook-execution.txt`, `*-action-audit.txt`, `*-agent-execution-log.txt` | 대표 검증에서 실제로 남은 원시 근거 | 파일이 흩어져 있어 읽는 순서 없이 보면 해석 비용이 높다 | 실행별 직접 근거 |
| `TASK-0004-script-application-representative-verified.md` | 대표 검증에서 어떤 실행 키, 전달 식별자, 결론으로 닫았는지 설명 | 새 대표 검증을 다시 돌리면 값은 달라질 수 있다 | 최종 사람 판단 근거 |

## 공통 전제
- 앱은 `local` 프로필 기준으로 띄운다.
- 준비 데이터 정리와 적용은 앱 기동 전에 마친다.
- 실제 앱/H2 대표 검증은 `준비 데이터 정리/적용 -> 앱 기동 -> 요청 실행 -> 앱 종료 -> H2 조회` 순서로 진행한다.
- H2 파일 DB는 앱 종료 뒤에만 외부에서 조회한다.
- 같은 H2 파일을 여러 셸에서 동시에 열지 않는다.
- 실제 앱/H2 대표 검증은 기존 예시 키를 그대로 재사용하지 않고, 임시 SQL 복사본에 새 접미사를 넣어 새 실행 키와 전달 식별자를 만든다.

## 참고: 스크립트 책임과 수동 확인 경계
| 단계 | 주 입력 | 주 출력 | 현재 담당 | 사람이 직접 확인할 것 |
| --- | --- | --- | --- | --- |
| 시작 전 포트/H2 프로세스 확인과 준비 데이터 적용 | `APP_PORT`, `JDBC_URL`, `H2_JAR`, 정리/적용 SQL | `prepare.log`, 정리/적용된 H2 상태 | `prepare-seed.sh` | 선택한 SQL 파일 경로와 시나리오 일치 여부 |
| 재실행 대표 요청 실행 | `BASE_URL`, `RERUN_EXECUTION_KEY`, `RERUN_ACTION_BODY` | `rerun-query-before.json`, `rerun-history.json`, `rerun-action.json`, `rerun-query-after.json`, `app.pid` | `run-rerun.sh` | 응답 의미 비교 |
| 재시도 대표 요청 실행 | `BASE_URL`, `RETRY_SOURCE_EXECUTION_KEY`, `RETRY_REQUEST_BODY` | `retry-response.json`, `retry-derived-execution-key.txt`, `retry-derived-query.json`, `app.pid` | `run-retry.sh` | 재시도 응답과 파생 실행 의미 비교 |
| 앱 종료 뒤 실행 근거 수집 | `JDBC_URL`, `H2_JAR`, `EVIDENCE_MODE`, 실행 키 | 재실행 또는 재시도 H2 조회 결과 텍스트 | `collect-evidence.sh` | H2 결과 의미 해석 |
| 응답과 H2 결과 결론 정리 | 응답 파일, H2 조회 결과, 회고 경로 | 대표 검증 결론 | 수동 확인 단계 | 응답과 H2 의미 일치 여부, 회고 작성 |

- 초안 파일은 명령 실행과 결과 파일 저장까지 맡는다.
- 대표 검증 결론, H2 결과 의미 해석, 회고와 제안 필요 여부 판단은 계속 사람이 직접 닫는다.

## 기존 절차 참고

### 공통 환경 변수 예시
```bash
export APP_PORT=18080
export BASE_URL="http://localhost:${APP_PORT}"
export H2_DB_PATH="$HOME/.agile-runner/agent-runtime/agile-runner"
export JDBC_URL="jdbc:h2:file:${H2_DB_PATH};MODE=PostgreSQL;DB_CLOSE_ON_EXIT=FALSE"
export H2_JAR="$(find "$HOME/.gradle/caches/modules-2/files-2.1/com.h2database/h2" -name 'h2-*.jar' | sort | tail -n 1)"
export VERIFY_TS="$(date +%Y%m%d-%H%M%S)"
export OUTPUT_ROOT="$PWD/.tmp/manual-rerun-response-${VERIFY_TS}"
export RERUN_DIR="${OUTPUT_ROOT}/rerun"
export RETRY_DIR="${OUTPUT_ROOT}/retry"
mkdir -p "${RERUN_DIR}" "${RETRY_DIR}"
```

### 대표 검증용 임시 SQL 복사본 만들기
### 재실행 준비 데이터 임시 복사본
```bash
export RERUN_SUFFIX="spec0030-rerun-${VERIFY_TS}"
export RERUN_EXECUTION_KEY="EXECUTION:MANUAL_RERUN:${RERUN_SUFFIX}"
export RERUN_DELIVERY_ID="MANUAL_RERUN_DELIVERY:${RERUN_SUFFIX}"
export RERUN_APPLY_SQL="${RERUN_DIR}/rerun-apply.sql"
export RERUN_RESET_SQL="${RERUN_DIR}/rerun-reset.sql"

sed "s/example-rerun/${RERUN_SUFFIX}/g" \
  src/test/resources/manual-rerun-response-seed/control-action-history/rerun-acknowledge-action-history-seed.example.sql \
  > "${RERUN_APPLY_SQL}"

cat <<EOF > "${RERUN_RESET_SQL}"
DELETE FROM MANUAL_RERUN_CONTROL_ACTION_AUDIT
WHERE execution_key = '${RERUN_EXECUTION_KEY}';

DELETE FROM AGENT_EXECUTION_LOG
WHERE execution_key = '${RERUN_EXECUTION_KEY}'
   OR retry_source_execution_key = '${RERUN_EXECUTION_KEY}';

DELETE FROM WEBHOOK_EXECUTION
WHERE execution_key = '${RERUN_EXECUTION_KEY}'
   OR retry_source_execution_key = '${RERUN_EXECUTION_KEY}';
EOF
```

### 재시도 원본 실행 임시 복사본
```bash
export RETRY_SOURCE_SUFFIX="spec0030-retry-source-${VERIFY_TS}"
export RETRY_SOURCE_EXECUTION_KEY="EXECUTION:MANUAL_RERUN:${RETRY_SOURCE_SUFFIX}"
export RETRY_SOURCE_DELIVERY_ID="MANUAL_RERUN_DELIVERY:${RETRY_SOURCE_SUFFIX}"
export RETRY_APPLY_SQL="${RETRY_DIR}/retry-apply.sql"
export RETRY_RESET_SQL="${RETRY_DIR}/retry-reset.sql"

sed "s/example-retry-source/${RETRY_SOURCE_SUFFIX}/g" \
  src/test/resources/manual-rerun-response-seed/source-execution/retry-source-execution-seed.example.sql \
  > "${RETRY_APPLY_SQL}"

cat <<EOF > "${RETRY_RESET_SQL}"
DELETE FROM MANUAL_RERUN_CONTROL_ACTION_AUDIT
WHERE execution_key = '${RETRY_SOURCE_EXECUTION_KEY}'
   OR execution_key IN (
     SELECT execution_key
     FROM WEBHOOK_EXECUTION
     WHERE retry_source_execution_key = '${RETRY_SOURCE_EXECUTION_KEY}'
   );

DELETE FROM AGENT_EXECUTION_LOG
WHERE execution_key = '${RETRY_SOURCE_EXECUTION_KEY}'
   OR retry_source_execution_key = '${RETRY_SOURCE_EXECUTION_KEY}';

DELETE FROM WEBHOOK_EXECUTION
WHERE execution_key = '${RETRY_SOURCE_EXECUTION_KEY}'
   OR retry_source_execution_key = '${RETRY_SOURCE_EXECUTION_KEY}';
EOF
```

### 재실행 대표 검증 흐름
### 1. 준비 데이터 정리와 적용
```bash
OUTPUT_DIR="${RERUN_DIR}" \
APP_PORT="${APP_PORT}" \
SEED_RESET_SQL="${RERUN_RESET_SQL}" \
SEED_APPLY_SQL="${RERUN_APPLY_SQL}" \
JDBC_URL="${JDBC_URL}" \
H2_JAR="${H2_JAR}" \
./scripts/manual-rerun-response/prepare-seed.sh
```

### 2. 재실행 요청 흐름 실행
```bash
OUTPUT_DIR="${RERUN_DIR}" \
APP_PORT="${APP_PORT}" \
BASE_URL="${BASE_URL}" \
RERUN_EXECUTION_KEY="${RERUN_EXECUTION_KEY}" \
RERUN_ACTION_BODY='{"action":"UNACKNOWLEDGE","note":"대표 검증용 확인 해제"}' \
APP_START_TIMEOUT_SECONDS=60 \
./scripts/manual-rerun-response/run-rerun.sh
```

### 3. 앱 종료 뒤 실행 근거 수집
```bash
OUTPUT_DIR="${RERUN_DIR}" \
JDBC_URL="${JDBC_URL}" \
H2_JAR="${H2_JAR}" \
EVIDENCE_MODE=rerun \
RERUN_EXECUTION_KEY="${RERUN_EXECUTION_KEY}" \
./scripts/manual-rerun-response/collect-evidence.sh
```

### 재시도 대표 검증 흐름
### 1. 준비 데이터 정리와 적용
```bash
OUTPUT_DIR="${RETRY_DIR}" \
APP_PORT="${APP_PORT}" \
SEED_RESET_SQL="${RETRY_RESET_SQL}" \
SEED_APPLY_SQL="${RETRY_APPLY_SQL}" \
JDBC_URL="${JDBC_URL}" \
H2_JAR="${H2_JAR}" \
./scripts/manual-rerun-response/prepare-seed.sh
```

### 2. 재시도 요청 흐름 실행
```bash
OUTPUT_DIR="${RETRY_DIR}" \
APP_PORT="${APP_PORT}" \
BASE_URL="${BASE_URL}" \
RETRY_SOURCE_EXECUTION_KEY="${RETRY_SOURCE_EXECUTION_KEY}" \
RETRY_REQUEST_BODY='{"executionControlMode":"DRY_RUN"}' \
APP_START_TIMEOUT_SECONDS=60 \
./scripts/manual-rerun-response/run-retry.sh
```

### 3. 파생 실행 키 읽기
```bash
export RETRY_DERIVED_EXECUTION_KEY="$(cat "${RETRY_DIR}/retry-derived-execution-key.txt")"
printf '%s\n' "${RETRY_DERIVED_EXECUTION_KEY}"
```

### 4. 앱 종료 뒤 실행 근거 수집
```bash
OUTPUT_DIR="${RETRY_DIR}" \
JDBC_URL="${JDBC_URL}" \
H2_JAR="${H2_JAR}" \
EVIDENCE_MODE=retry \
RETRY_DERIVED_EXECUTION_KEY="${RETRY_DERIVED_EXECUTION_KEY}" \
./scripts/manual-rerun-response/collect-evidence.sh
```

## 참고: 어떤 실행 키를 어디에 다시 쓰는가
- 재실행 대표 검증
  - `RERUN_EXECUTION_KEY`를 준비 데이터 적용, 단건 조회, 이력 조회, 관리자 조치, 조치 후 단건 조회, H2 조회까지 그대로 쓴다.
  - `RERUN_DELIVERY_ID`는 회고에 함께 남기는 대표 전달 식별자다.
- 재시도 대표 검증
  - `RETRY_SOURCE_EXECUTION_KEY`는 준비 데이터 적용과 retry 요청 시작점에 쓴다.
  - 재시도 응답 뒤에는 `retry-derived-execution-key.txt`에 저장된 `RETRY_DERIVED_EXECUTION_KEY`를 단건 조회와 H2 조회 기준으로 다시 쓴다.
  - 파생 실행 단건 조회 응답에는 원본 실행 키 필드인 `retrySourceExecutionKey`가 없으므로, 원본 실행과 파생 실행의 연결은 재시도 응답과 H2 `WEBHOOK_EXECUTION` 행에서 다시 확인한다.
  - `RETRY_SOURCE_DELIVERY_ID`는 원본 실행 전달 식별자다.
- 즉 재시도는 원본 실행 키와 파생 실행 키가 서로 다르고, 대표 검증 결론은 파생 실행 키 기준으로 닫는다.

## 참고: 출력 파일과 수동 확인 포인트
| 파일 | 남는 출력 | 사람이 먼저 확인할 것 |
| --- | --- | --- |
| `prepare-seed.sh` | `prepare.log` | 포트 충돌, H2 도구 중복, 정리/적용 SQL 실패 여부 |
| `run-rerun.sh` | `rerun-query-before.json`, `rerun-history.json`, `rerun-action.json`, `rerun-query-after.json`, `app.pid` | 어떤 응답 파일까지 성공했고 어디서 끊겼는지 |
| `run-retry.sh` | `retry-response.json`, `retry-derived-execution-key.txt`, `retry-derived-query.json`, `app.pid` | 파생 실행 키가 실제로 남았는지, 마지막 응답 파일이 무엇인지 |
| `collect-evidence.sh` | `rerun-webhook-execution.txt`, `rerun-action-audit.txt`, `retry-webhook-execution.txt`, `retry-agent-execution-log.txt` | 앱 종료 여부, H2 잠금 여부, 어떤 H2 조회 파일까지 남았는지 |

## 참고: 종료 코드와 중단 조건
| 파일 | 중단 상황 | 종료 코드 |
| --- | --- | --- |
| `prepare-seed.sh` | 포트 충돌, H2 도구 중복, 정리 SQL 실패, 준비 데이터 적용 SQL 실패 | `10`, `11`, `12`, `13` |
| `run-rerun.sh` | 앱 기동 실패, 단건 조회 실패, 이력 조회 실패, 관리자 조치 실패, 조치 후 단건 조회 실패 | `20`, `21`, `22`, `23`, `24` |
| `run-retry.sh` | 앱 기동 실패, 재시도 요청 실패, 파생 실행 키 추출 실패, 파생 단건 조회 실패 | `30`, `31`, `32`, `33` |
| `collect-evidence.sh` | 앱 종료 미확인, H2 조회 실패, H2 잠금 의심 | `40`, `41`, `42` |

## 종료 코드별 다음 확인 대상
- 이 표는 전체 종료 코드 인덱스다. 아래 실패 사례 예시는 `10`부터 `33`까지를 먼저 다루고, `40`부터 `42`는 아래 `H2 잠금과 코드 오류를 나눠 보는 순서`에서 따로 본다.

| 종료 코드 | 먼저 볼 파일 또는 대상 | 여기서 먼저 확인할 것 | 다음으로 이어서 볼 것 |
| --- | --- | --- | --- |
| `10` | `prepare.log` | 시작 전 포트 충돌 문구가 남았는지 | 현재 포트를 잡고 있는 프로세스 정리 뒤 `prepare-seed.sh` 재실행 |
| `11` | `prepare.log` | H2 도구 중복 실행 문구가 남았는지 | 다른 H2 Shell 또는 CLI 종료 여부 확인 뒤 `prepare-seed.sh` 재실행 |
| `12` | `prepare.log`, `SEED_RESET_SQL` | 정리 SQL 어느 구문에서 실패했는지 | 정리 SQL 구문과 현재 H2 상태 점검 |
| `13` | `prepare.log`, `SEED_APPLY_SQL` | 준비 데이터 적용 SQL 어느 구문에서 실패했는지 | enum 값, 스키마, 삽입 대상 row 점검 |
| `20` | `app.pid` 생성 여부 | 앱 기동 자체가 실패했는지 | 앱 기동 로그와 포트 점검 |
| `21` | `rerun-query-before.json` 생성 여부 | 조치 전 단건 조회에서 멈췄는지 | `BASE_URL`, `RERUN_EXECUTION_KEY`, 단건 조회 응답 확인 |
| `22` | `rerun-history.json` 생성 여부 | 이력 조회에서 멈췄는지 | `executionKey`와 history 응답 확인 |
| `23` | `rerun-action.json` 생성 여부 | 관리자 조치 요청에서 멈췄는지 | action 요청 본문과 응답 확인 |
| `24` | `rerun-query-after.json` 생성 여부 | 조치 후 단건 조회에서 멈췄는지 | 조치 직후 상태와 `availableActions` 확인 |
| `30` | `app.pid` 생성 여부 | 앱 기동 자체가 실패했는지 | 앱 기동 로그와 포트 점검 |
| `31` | `retry-response.json` 생성 여부 | 재시도 요청에서 멈췄는지 | 요청 본문과 retry 응답 확인 |
| `32` | `retry-derived-execution-key.txt` 생성 여부 | 파생 실행 키 추출에서 멈췄는지 | retry 응답 본문에 `executionKey`가 있는지 확인 |
| `33` | `retry-derived-query.json` 생성 여부 | 파생 단건 조회에서 멈췄는지 | 파생 실행 키와 query 응답 확인 |
| `40` | `app.pid`, `collect-evidence.log` | 앱 종료가 확인되지 않았는지 | 앱 프로세스 종료 여부를 먼저 정리한 뒤 `collect-evidence.sh` 재실행 |
| `41` | H2 조회 대상 파일, `collect-evidence.log`, H2 CLI 출력 | H2 조회 단계에서 잠금 시그니처 없이 멈췄는지 | JDBC 경로, SQL 구문, 조회 대상 execution key 확인 |
| `42` | H2 CLI 출력, `collect-evidence.log` | H2 잠금 시그니처와 잠금 의심 종료 코드가 함께 남았는지 | 다른 H2 조회 프로세스와 앱 프로세스 종료 여부 확인 |

## 종료 코드와 멈춤 실패 사례 예시
### 종료 코드 실패 빠른 참조
| 종료 코드 | 먼저 열 로그 또는 파일 | 먼저 좁히는 실패 유형 |
| --- | --- | --- |
| `10` | `prepare.log` | 시작 전 포트 충돌 |
| `11` | `prepare.log` | H2 명령줄 도구 중복 실행 |
| `12` | `prepare.log` | 정리 SQL 실행 실패 |
| `13` | `prepare.log` | 준비 데이터 적용 SQL 실행 실패 |
| `20` | `run-rerun.log` | 앱 기동 확인 전 멈춤 |
| `21` | `run-rerun.log` | 재실행 단건 조회 실패 |
| `22` | `rerun-query-before.json` | 재실행 이력 조회 실패 |
| `23` | `run-rerun.log` | 재실행 관리자 조치 실패 |
| `24` | `run-rerun.log` | 재실행 조치 후 단건 조회 실패 |
| `30` | `run-retry.log` | 앱 기동 확인 전 멈춤 |
| `31` | `run-retry.log` | 재시도 요청 실패 |
| `32` | `retry-response.json` | 재시도 파생 실행 키 추출 실패 |
| `33` | `run-retry.log` | 재시도 파생 실행 단건 조회 실패 |

이 표는 종료 코드를 처음 봤을 때 실패 유형을 한 번에 좁히는 첫 분류용 표다.

### 준비 데이터 적용 단계 예시
| 종료 코드 | 실제로 먼저 보이는 문구 예시 | 같이 보는 출력 파일과 힌트 | 이 예시를 보면 이렇게 읽는다 |
| --- | --- | --- | --- |
| `10` | `시작 전 포트 충돌` | `prepare.log`는 있고, 정리 SQL이나 적용 SQL 성공 문구는 없다 | 앱 기동 전에 포트 선점 문제로 바로 멈춘 경우 |
| `11` | `H2 명령줄 도구 중복 실행` | `prepare.log`는 있고, H2 Shell 중복 확인 뒤 바로 멈춘다 | 정리 SQL로 들어가기 전에 다른 H2 CLI가 먼저 잡힌 경우 |
| `12` | `정리 SQL 실행 실패` | `prepare.log`는 있고, 적용 SQL 완료 문구는 없다 | reset SQL 구문이나 현재 H2 상태 때문에 정리 단계에서 멈춘 경우 |
| `13` | `준비 데이터 적용 SQL 실행 실패` | `prepare.log`는 있고, 정리 단계는 지났지만 적용 완료 문구는 없다 | apply SQL 단계에서 enum 값, 스키마, insert 대상 row 때문에 멈춘 경우 |

### 재실행 흐름 예시
| 종료 코드 | 실제로 먼저 보이는 문구 예시 | 같이 보는 출력 파일과 힌트 | 이 예시를 보면 이렇게 읽는다 |
| --- | --- | --- | --- |
| `20` | `앱 기동 시간 안에 포트 확인 실패` | `run-rerun.log`는 있고 `rerun-query-before.json`은 남지 않는다 | 앱이 떠야 시작할 단건 조회까지 못 간 경우 |
| `21` | `재실행 단건 조회 실패` | `run-rerun.log` 실패 문구를 먼저 보고, 다음 단계 파일인 `rerun-history.json`이 없는지 같이 본다 | 조치 전 현재 상태 조회에서 바로 멈춘 경우 |
| `22` | `재실행 이력 조회 실패` | `rerun-query-before.json`은 있고, 다음 단계 파일인 `rerun-action.json`은 없다 | 단건 조회는 성공했지만 history 조회에서 멈춘 경우 |
| `23` | `재실행 관리자 조치 실패` | `run-rerun.log` 실패 문구와 함께 `rerun-query-after.json`이 없는지 같이 본다 | 이력은 읽었지만 action 요청이 실패한 경우 |
| `24` | `재실행 조치 후 단건 조회 실패` | `run-rerun.log` 실패 문구를 먼저 보고, 조치 후 조회 응답 파일은 실패 본문이 남았는지 여부까지 같이 본다 | 조치 요청은 통과했지만 조치 후 상태 재조회에서 멈춘 경우 |

### 재시도 흐름 예시
| 종료 코드 | 실제로 먼저 보이는 문구 예시 | 같이 보는 출력 파일과 힌트 | 이 예시를 보면 이렇게 읽는다 |
| --- | --- | --- | --- |
| `30` | `앱 기동 시간 안에 포트 확인 실패` | `run-retry.log`는 있고 `retry-response.json`은 남지 않는다 | retry 요청 자체를 보내기 전에 앱 기동에서 멈춘 경우 |
| `31` | `재시도 요청 실패` | `run-retry.log` 실패 문구를 먼저 보고, 다음 단계 파일인 `retry-derived-execution-key.txt`가 없는지 같이 본다 | retry API 호출 단계에서 바로 멈춘 경우 |
| `32` | `재시도 파생 실행 키 추출 실패` | `retry-response.json`은 있고, `retry-derived-execution-key.txt`는 없다 | retry 응답은 왔지만 `executionKey`를 꺼내지 못한 경우 |
| `33` | `재시도 파생 실행 단건 조회 실패` | `retry-derived-execution-key.txt`는 있고, `run-retry.log`가 단건 조회 실패를 남기는지 같이 본다 | 파생 실행 키까지는 구했지만 파생 실행 단건 조회에서 멈춘 경우 |

- 위 예시는 [ManualRerunSeedCommandScriptTest.java](/home/seaung13/workspace/agile-runner/src/test/java/com/agilerunner/client/agentruntime/ManualRerunSeedCommandScriptTest.java) 와 [ManualRerunRunFlowScriptTest.java](/home/seaung13/workspace/agile-runner/src/test/java/com/agilerunner/client/agentruntime/ManualRerunRunFlowScriptTest.java) 에서 고정한 실제 로그 문구를 그대로 따른다.
- `40`, `41`, `42`와 H2 조회 실패 구간은 아래 [H2 잠금과 코드 오류를 나눠 보는 순서](#h2-잠금과-코드-오류를-나눠-보는-순서) 에서 이어서 본다.
- 출력 파일이 아예 비거나 안 남는 경우는 아래 [출력 파일 누락 시 첫 점검 순서](#출력-파일-누락-시-첫-점검-순서) 에서 이어서 본다.

## 종료 코드 빠른 참조 적용 예시
### 준비 단계 종료 코드 적용 예시
| 실제 상황 예시 | 먼저 펼칠 빠른 참조 표 | 다음에 바로 열 상세 예시 표 | 같이 볼 보조 문서나 파일 |
| --- | --- | --- | --- |
| `prepare.log` 첫 줄 근처에서 `시작 전 포트 충돌`이 보이고 종료 코드가 `10`이다 | 바로 위 `종료 코드 실패 빠른 참조`에서 `10` 행을 먼저 본다 | `준비 데이터 적용 단계 예시`의 `10` 행으로 바로 내려간다 | 이 문서의 `빠른 적용 순서`, `prepare.log` |
| `prepare.log`가 `H2 명령줄 도구 중복 실행`으로 멈추고 종료 코드가 `11`이다 | `종료 코드 실패 빠른 참조`에서 `11` 행을 먼저 본다 | `준비 데이터 적용 단계 예시`의 `11` 행으로 바로 내려간다 | 이 문서의 `빠른 적용 순서`, `prepare.log` |
| `prepare.log` 마지막 줄이 `정리 SQL 실행 실패`로 끝나고 종료 코드가 `12`다 | 바로 위 `종료 코드 실패 빠른 참조`에서 `12` 행을 먼저 본다 | `준비 데이터 적용 단계 예시`의 `12` 행으로 바로 내려간다 | 이 문서의 `빠른 적용 순서`, [manual-rerun-response-seed-guide.md](/home/seaung13/workspace/agile-runner/docs/manual-rerun-response-seed-guide.md) |
| `prepare.log` 마지막 줄이 `준비 데이터 적용 SQL 실행 실패`로 끝나고 종료 코드가 `13`이다 | `종료 코드 실패 빠른 참조`에서 `13` 행을 먼저 본다 | `준비 데이터 적용 단계 예시`의 `13` 행으로 바로 내려간다 | 이 문서의 `빠른 적용 순서`, [manual-rerun-response-seed-guide.md](/home/seaung13/workspace/agile-runner/docs/manual-rerun-response-seed-guide.md) |

### 재실행 종료 코드 적용 예시
| 실제 상황 예시 | 먼저 펼칠 빠른 참조 표 | 다음에 바로 열 상세 예시 표 | 같이 볼 보조 문서나 파일 |
| --- | --- | --- | --- |
| `run-rerun.log`가 `앱 기동 시간 안에 포트 확인 실패`로 끝나고 종료 코드가 `20`이다 | `종료 코드 실패 빠른 참조`에서 `20` 행을 먼저 본다 | `재실행 흐름 예시`의 `20` 행으로 바로 내려간다 | 이 문서의 `빠른 적용 순서`, `run-rerun.log` |
| `run-rerun.log`에 `재실행 단건 조회 실패`가 보이고 종료 코드가 `21`이다 | `종료 코드 실패 빠른 참조`에서 `21` 행을 먼저 본다 | `재실행 흐름 예시`의 `21` 행으로 바로 내려간다 | 이 문서의 `빠른 적용 순서`, [manual-rerun-response-guide.md](/home/seaung13/workspace/agile-runner/docs/manual-rerun-response-guide.md) |
| `rerun-query-before.json`은 남았고 `run-rerun.log`가 이력 조회 실패로 끝나 종료 코드가 `22`다 | `종료 코드 실패 빠른 참조`에서 `22` 행을 먼저 본다 | `재실행 흐름 예시`의 `22` 행으로 바로 내려간다 | 이 문서의 `대표 검증 결과를 읽는 순서`, `rerun-query-before.json` |
| `rerun-query-before.json`은 남았지만 `run-rerun.log`가 `재실행 관리자 조치 실패`로 끝나고 종료 코드가 `23`이다 | `종료 코드 실패 빠른 참조`에서 `23` 행을 먼저 본다 | `재실행 흐름 예시`의 `23` 행으로 바로 내려간다 | 이 문서의 `대표 검증 결과를 읽는 순서`, [manual-rerun-response-guide.md](/home/seaung13/workspace/agile-runner/docs/manual-rerun-response-guide.md) |
| `rerun-action.json`은 남았지만 `run-rerun.log`가 조치 후 단건 조회 실패로 끝나고 종료 코드가 `24`다 | `종료 코드 실패 빠른 참조`에서 `24` 행을 먼저 본다 | `재실행 흐름 예시`의 `24` 행으로 바로 내려간다 | 이 문서의 `대표 검증 결과를 읽는 순서`, `rerun-action.json` |

### 재시도 종료 코드 적용 예시
| 실제 상황 예시 | 먼저 펼칠 빠른 참조 표 | 다음에 바로 열 상세 예시 표 | 같이 볼 보조 문서나 파일 |
| --- | --- | --- | --- |
| `run-retry.log`가 `앱 기동 시간 안에 포트 확인 실패`로 끝나고 종료 코드가 `30`이다 | `종료 코드 실패 빠른 참조`에서 `30` 행을 먼저 본다 | `재시도 흐름 예시`의 `30` 행으로 바로 내려간다 | 이 문서의 `빠른 적용 순서`, `run-retry.log` |
| `run-retry.log`가 `재시도 요청 실패`로 끝나고 종료 코드가 `31`이다 | `종료 코드 실패 빠른 참조`에서 `31` 행을 먼저 본다 | `재시도 흐름 예시`의 `31` 행으로 바로 내려간다 | 이 문서의 `빠른 적용 순서`, `retry-response.json`이 아직 없는지 함께 본다 |
| `retry-response.json`은 남았지만 `retry-derived-execution-key.txt`가 비어 있고 종료 코드가 `32`다 | `종료 코드 실패 빠른 참조`에서 `32` 행을 먼저 본다 | `재시도 흐름 예시`의 `32` 행으로 바로 내려간다 | 이 문서의 `대표 검증 결과를 읽는 순서`, [manual-rerun-response-guide.md](/home/seaung13/workspace/agile-runner/docs/manual-rerun-response-guide.md) |
| `retry-derived-execution-key.txt`는 남았지만 `run-retry.log`가 파생 실행 단건 조회 실패로 끝나고 종료 코드가 `33`이다 | `종료 코드 실패 빠른 참조`에서 `33` 행을 먼저 본다 | `재시도 흐름 예시`의 `33` 행으로 바로 내려간다 | 이 문서의 `대표 검증 결과를 읽는 순서`, `retry-derived-execution-key.txt` |

이 적용 예시는 종료 코드 실패를 처음 만났을 때 `어느 빠른 참조 표를 먼저 펼치고, 다음에 어느 상세 예시 표로 내려갈지`만 정리한다.
출력 파일이 아예 없거나 H2 조회 실패를 먼저 만난 경우는 이번 섹션이 아니라 아래 `출력 파일 누락 시 첫 점검 순서`, `H2 잠금과 코드 오류를 나눠 보는 순서`에서 다룬다.

## 멈춤 해석 순서
1. 먼저 종료 코드를 확인한다.
2. 종료 코드가 `10`부터 `33`이면 바로 위 `종료 코드와 멈춤 실패 사례 예시`를 먼저 본다.
3. 종료 코드가 `40`부터 `42`이면 아래 `H2 잠금과 코드 오류를 나눠 보는 순서`를 먼저 본다.
4. 필요한 출력 파일이 없으면 아래 `출력 파일 누락 시 첫 점검 순서`로 바로 이동한다.
5. 출력 파일이 남아 있으면 해당 입력값과 응답 본문을 함께 다시 본다.

- 실제 앱/H2 대표 검증은 위 종료 코드가 `0`인 상태와 남은 출력 파일을 함께 보고 닫는다.

## 출력 파일 누락 시 첫 점검 순서
### 출력 파일 누락 빠른 참조 카드
| 누락된 출력 파일 | 먼저 열 로그 | 먼저 던질 질문 |
| --- | --- | --- |
| `prepare.log` | `prepare-seed.sh` 실행 출력 | 스크립트가 아예 시작되지 않았는가 |
| `rerun-query-before.json` | `run-rerun.log` | 앱 기동 직후 단건 조회 전에 멈췄는가 |
| `rerun-history.json` | `run-rerun.log` | 조치 전 단건 조회 뒤 이력 조회에서 멈췄는가 |
| `rerun-action.json` | `run-rerun.log` | 이력 조회 뒤 관리자 조치에서 멈췄는가 |
| `rerun-query-after.json` | `run-rerun.log` | 관리자 조치 뒤 조치 후 단건 조회에서 멈췄는가 |
| `retry-response.json` | `run-retry.log` | 앱 기동 뒤 재시도 요청 전에 멈췄는가 |
| `retry-derived-execution-key.txt` | `run-retry.log` | 재시도 요청 뒤 파생 실행 키 추출 전에 멈췄는가 |
| `retry-derived-query.json` | `run-retry.log`, `retry-derived-execution-key.txt` | 파생 실행 키 추출에서 멈췄는가, 아니면 파생 실행 단건 조회에서 멈췄는가 |
| `rerun-webhook-execution.txt`, `rerun-action-audit.txt`, `retry-webhook-execution.txt`, `retry-agent-execution-log.txt` | `collect-evidence.log` | 앱 종료 뒤 실행 근거 조회에서 멈췄는가 |

이 카드는 누락된 출력 파일을 먼저 어느 구간 실패로 볼지 좁히는 첫 카드다.

| 누락된 출력 파일 | 먼저 볼 스크립트와 로그 | 여기서 먼저 확인할 것 | 같이 볼 테스트와 문서 |
| --- | --- | --- | --- |
| `prepare.log` | `prepare-seed.sh`, `prepare.log` | 스크립트가 아예 시작되지 않았는지, 정리/적용 단계 문구가 남았는지 | [ManualRerunSeedCommandScriptTest.java](/home/seaung13/workspace/agile-runner/src/test/java/com/agilerunner/client/agentruntime/ManualRerunSeedCommandScriptTest.java), 이 문서의 `빠른 적용 순서` |
| `rerun-query-before.json` | `run-rerun.sh`, `run-rerun.log` | 앱 기동 직후 단건 조회 단계에서 멈췄는지 | [ManualRerunRunFlowScriptTest.java](/home/seaung13/workspace/agile-runner/src/test/java/com/agilerunner/client/agentruntime/ManualRerunRunFlowScriptTest.java), `종료 코드별 다음 확인 대상`의 `20`, `21` |
| `rerun-history.json` | `run-rerun.sh`, `run-rerun.log` | 조치 전 단건 조회까지는 끝났고 이력 조회에서 멈췄는지 | [ManualRerunRunFlowScriptTest.java](/home/seaung13/workspace/agile-runner/src/test/java/com/agilerunner/client/agentruntime/ManualRerunRunFlowScriptTest.java), `종료 코드별 다음 확인 대상`의 `22` |
| `rerun-action.json` | `run-rerun.sh`, `run-rerun.log` | 이력 조회까지는 끝났고 관리자 조치 요청에서 멈췄는지 | [ManualRerunRunFlowScriptTest.java](/home/seaung13/workspace/agile-runner/src/test/java/com/agilerunner/client/agentruntime/ManualRerunRunFlowScriptTest.java), `종료 코드별 다음 확인 대상`의 `23` |
| `rerun-query-after.json` | `run-rerun.sh`, `run-rerun.log` | 관리자 조치까지는 끝났고 조치 후 단건 조회에서 멈췄는지 | [ManualRerunRunFlowScriptTest.java](/home/seaung13/workspace/agile-runner/src/test/java/com/agilerunner/client/agentruntime/ManualRerunRunFlowScriptTest.java), `종료 코드별 다음 확인 대상`의 `24` |
| `retry-response.json` | `run-retry.sh`, `run-retry.log` | 앱 기동 직후 재시도 요청 단계에서 멈췄는지 | [ManualRerunRunFlowScriptTest.java](/home/seaung13/workspace/agile-runner/src/test/java/com/agilerunner/client/agentruntime/ManualRerunRunFlowScriptTest.java), `종료 코드별 다음 확인 대상`의 `30`, `31` |
| `retry-derived-execution-key.txt` | `run-retry.sh`, `run-retry.log`, `retry-response.json` | 재시도 요청이 바로 실패했는지, 응답은 왔지만 파생 실행 키 추출에서 멈췄는지 | [ManualRerunRunFlowScriptTest.java](/home/seaung13/workspace/agile-runner/src/test/java/com/agilerunner/client/agentruntime/ManualRerunRunFlowScriptTest.java), `종료 코드별 다음 확인 대상`의 `31`, `32` |
| `retry-derived-query.json` | `run-retry.sh`, `run-retry.log`, `retry-derived-execution-key.txt` | 파생 실행 키 추출에서 멈췄는지, 또는 파생 단건 조회 실패 로그가 남았는지 | [ManualRerunRunFlowScriptTest.java](/home/seaung13/workspace/agile-runner/src/test/java/com/agilerunner/client/agentruntime/ManualRerunRunFlowScriptTest.java), `종료 코드별 다음 확인 대상`의 `32`, `33` |
| `rerun-webhook-execution.txt`, `rerun-action-audit.txt`, `retry-webhook-execution.txt`, `retry-agent-execution-log.txt` | `collect-evidence.sh`, `collect-evidence.log` | 앱 종료 확인 뒤 실행 근거 조회 단계에서 멈췄는지 | [ManualRerunRunFlowScriptTest.java](/home/seaung13/workspace/agile-runner/src/test/java/com/agilerunner/client/agentruntime/ManualRerunRunFlowScriptTest.java), `종료 코드별 다음 확인 대상`의 `40`, `41`, `42` |

## 출력 파일 누락 실패 사례 예시
- 아래 예시는 자동 검증이 직접 고정한 누락 사례를 먼저 적고, 로그 문구만 먼저 고정된 구간은 파일 부재를 단정하지 않는다.
- `prepare.log` 자체가 비는 경우는 현재 실패 예시보다 먼저 스크립트 시작 여부를 다시 봐야 하므로 위 `출력 파일 누락 시 첫 점검 순서`의 `prepare.log` 행을 먼저 따른다.
- `rerun-webhook-execution.txt`, `rerun-action-audit.txt`, `retry-webhook-execution.txt`, `retry-agent-execution-log.txt` 누락은 H2 조회 단계와 같이 보아야 하므로 아래 `H2 잠금과 코드 오류를 나눠 보는 순서`에서 이어서 본다.

| 누락된 출력 파일 | 실제로 먼저 붙여 보는 로그 문구 예시 | 같이 보는 테스트 예시 | 이 사례를 보면 이렇게 읽는다 |
| --- | --- | --- | --- |
| `rerun-query-before.json` | `앱 기동 시간 안에 포트 확인 실패` | `stopRunRerunScriptWhenAppDoesNotStart` | 재실행 흐름이 앱 기동 단계에서 끊겨 첫 JSON 출력까지 못 간 경우 |
| `rerun-history.json` | `재실행 단건 조회 실패` | `stopRunRerunScriptWhenQueryBeforeFails` | 조치 전 단건 조회에서 멈춰 다음 단계 파일이 안 생긴 경우 |
| `rerun-action.json` | `재실행 이력 조회 실패` | `stopRunRerunScriptWhenHistoryFails` | history 조회까지 못 가서 action 응답 파일이 안 생긴 경우 |
| `rerun-query-after.json` | `재실행 관리자 조치 실패` | `stopRunRerunScriptWhenActionFails` | action 요청 단계에서 끊겨 조치 후 조회 파일이 안 생긴 경우 |
| `retry-response.json` | `앱 기동 시간 안에 포트 확인 실패` | `stopRunRetryScriptWhenAppDoesNotStart` | 재시도 흐름이 앱 기동 단계에서 끊겨 첫 retry 응답 파일까지 못 간 경우 |
| `retry-derived-execution-key.txt` | `재시도 요청 실패` | `stopRunRetryScriptWhenRetryRequestFails` | retry API 호출 단계에서 끊겨 파생 실행 키 파일이 안 생긴 경우 |
| `retry-derived-query.json` | `재시도 파생 실행 키 추출 실패` | `stopRunRetryScriptWhenDerivedExecutionKeyIsMissing` | retry 응답은 왔지만 파생 실행 키를 꺼내지 못해 다음 query 파일이 안 생긴 경우 |

- `재시도 파생 실행 단건 조회 실패`는 [ManualRerunRunFlowScriptTest.java](/home/seaung13/workspace/agile-runner/src/test/java/com/agilerunner/client/agentruntime/ManualRerunRunFlowScriptTest.java) 가 `run-retry.log` 실패 문구까지는 고정하지만, `retry-derived-query.json` 부재는 직접 단정하지 않는다. 이 경우는 로그 문구와 `retry-derived-execution-key.txt` 존재 여부를 먼저 보고, query 파일 본문은 사람이 직접 다시 확인한다.

### 같이 보는 회고와 단계 요약
- 종료 코드와 실제 로그 문구를 먼저 좁힐 때는 [TASK-0002-stop-code-failure-examples.md](/home/seaung13/workspace/agile-runner/.agents/outer-loop/retrospectives/SPEC-0034/TASK-0002-stop-code-failure-examples.md) 를 함께 본다.
- H2 실행 근거 출력 파일이 비거나 안 남는 경우는 [TASK-0004-script-h2-lock-separation-closeout.md](/home/seaung13/workspace/agile-runner/.agents/outer-loop/retrospectives/SPEC-0033/TASK-0004-script-h2-lock-separation-closeout.md) 와 [SPEC-0033-summary.md](/home/seaung13/workspace/agile-runner/.agents/outer-loop/retrospectives/SPEC-0033/SPEC-0033-summary.md) 를 함께 본다.

## 빠른 참조를 본 뒤 상세 문서로 내려가는 순서
1. 종료 코드 표로 먼저 좁혔으면 바로 아래 `종료 코드와 멈춤 실패 사례 예시` 표로 내려간다.
2. 출력 파일 누락 카드로 먼저 좁혔으면 `출력 파일 누락 실패 사례 예시` 표로 내려간다.
3. H2 조회 실패 카드로 먼저 좁혔으면 `H2 잠금 실패 사례 예시`와 `H2 조회 단계 마지막 확인 질문`으로 내려간다.
4. 응답 필드 의미나 대표 검증 응답 비교가 필요하면 [manual-rerun-response-guide.md](/home/seaung13/workspace/agile-runner/docs/manual-rerun-response-guide.md) 를 함께 연다.
5. 마지막 대표 검증 근거와 이전 판단이 필요하면 [TASK-0004-script-application-representative-verified.md](/home/seaung13/workspace/agile-runner/.agents/outer-loop/retrospectives/SPEC-0031/TASK-0004-script-application-representative-verified.md) 를 함께 본다.

## 출력 파일 누락 시 다시 보는 순서
1. 누락된 파일 이름이 어느 스크립트 구간에 속하는지 위 표에서 먼저 찾는다.
2. 그 파일을 만드는 스크립트의 로그를 먼저 열어 마지막으로 남은 단계 문구를 확인한다.
3. 같은 파일을 직접 기대하는 자동 검증 테스트가 무엇인지 함께 본다.
4. 같은 종료 코드 표에서 연결된 다음 확인 대상을 다시 따라간다.
5. 이 단계에서는 누락된 출력 파일이 어디서 끊겼는지까지만 좁힌다.

## H2 잠금과 코드 오류를 나눠 보는 순서
### H2 조회 실패 빠른 참조 카드
| 먼저 보이는 상황 | 먼저 보는 파일 | 먼저 던질 질문 |
| --- | --- | --- |
| 종료 코드 `40` | `app.pid`, `collect-evidence.log` | 앱이 아직 살아 있어 H2 조회가 밀린 것 아닌가 |
| 종료 코드 `41` | `collect-evidence.log`, 조회 SQL | 잠금 시그니처 없이 조회 대상이나 SQL 자체가 잘못된 것 아닌가 |
| 종료 코드 `42` | `collect-evidence.log`, H2 CLI 출력 | 잠금 시그니처가 보여 파일 잠금부터 풀어야 하는 것 아닌가 |
| H2 출력 파일 자체가 안 남음 | `collect-evidence.log` | 앱 종료 확인 전에 H2 조회 단계까지 못 간 것 아닌가 |

이 카드는 H2 조회 실패를 코드 오류로 볼지 잠금 문제로 볼지 먼저 가르는 첫 카드다.

| 상황 | 먼저 확인할 것 | 코드 오류로 바로 보지 않는 이유 | 다음 확인 |
| --- | --- | --- | --- |
| 종료 코드 `40` | `app.pid`, `collect-evidence.log`, 앱 프로세스 종료 여부 | 아직 앱이 살아 있으면 H2 조회 실패와 잠금 판단이 전부 뒤로 밀린다 | 앱 종료를 먼저 확인한 뒤 `collect-evidence.sh`를 다시 실행 |
| 종료 코드 `41` | `collect-evidence.log`, 조회 SQL, 조회 대상 execution key | SQL 구문, JDBC 경로, execution key 오입력만으로도 같은 종료 코드가 날 수 있다 | 조회 대상 key와 SQL 구문을 먼저 다시 확인 |
| 종료 코드 `42` | `collect-evidence.log`, H2 Shell 또는 CLI 동시 실행 여부, 앱 프로세스 종료 여부 | 잠금 시그니처가 보이면 코드 오류보다 파일 잠금 가능성을 먼저 봐야 한다 | 다른 H2 조회 프로세스와 앱 프로세스 종료 여부를 먼저 정리 |

## H2 잠금 실패 사례 예시
| 종료 코드 | 실제로 먼저 보이는 문구 예시 | 같이 보는 출력 파일과 힌트 | 이 예시를 보면 이렇게 읽는다 |
| --- | --- | --- | --- |
| `40` | `앱 종료 미확인` | `collect-evidence.log`와 `app.pid`를 먼저 보고, 앱 프로세스가 아직 살아 있는지 같이 본다 | H2 조회 자체보다 먼저 앱 종료 확인이 빠진 경우 |
| `41` | `실행 근거 조회 실패` | `collect-evidence.log`를 먼저 보고, 같은 로그에 `H2 잠금 의심` 문구가 없는지 같이 본다 | 잠금 시그니처 없이 H2 조회 자체가 실패한 경우 |
| `42` | `H2 잠금 의심` | `collect-evidence.log`, H2 Shell 또는 CLI 동시 실행 여부, 앱 프로세스 종료 여부를 같이 본다 | 코드 오류보다 파일 잠금 가능성을 먼저 좁혀야 하는 경우 |

- `41`은 같은 H2 프로세스가 떠 있어도 잠금 시그니처가 없으면 그대로 유지된다. 즉 `다른 H2 프로세스가 있다`와 `잠금 의심`은 같은 뜻이 아니다.
- `42`는 `collect-evidence.log`에 잠금 시그니처가 실제로 남을 때만 읽는다. 잠금 문구가 없으면 먼저 `41` 쪽 질문을 다시 따른다.

## H2 조회 단계 마지막 확인 질문
- 앱이 완전히 종료된 뒤에 H2 조회를 시작했는가.
- 같은 H2 file을 여는 다른 Shell 또는 CLI가 동시에 떠 있지 않은가.
- `collect-evidence.log`에 잠금 시그니처가 남았는가.
- 조회 대상 execution key와 SQL 구문이 현재 대표 검증 값과 맞는가.
- 위 네 항목을 먼저 확인하기 전에는 코드 오류로 단정하지 않았는가.

## 문서만 보고 바로 다시 고를 수 있는 것
- 종료 코드 `40`, `41`, `42`를 만났을 때 무엇을 먼저 확인해야 하는지 다시 고를 수 있어야 한다.
- 앱 종료 여부, 동시 H2 조회 여부, 잠금 시그니처, SQL 또는 execution key 확인 순서를 한 문서 안에서 다시 찾을 수 있어야 한다.
- 출력 파일 누락 점검 순서와 H2 조회 실패 분리 기준이 서로 섞이지 않아야 한다.

## 사람이 직접 비교해서 판단하는 것
- 재실행 단건 조회, 조치 응답, 이력 응답, 조치 후 단건 조회, 재시도 응답, 파생 단건 조회가 같은 실행 키 기준으로 맞는지 비교
- `availableActions`, `failureDisposition`, `currentActionState`, `retrySourceExecutionKey` 같은 필드가 현재 응답 의미와 맞는지 해석
- H2 `WEBHOOK_EXECUTION`, `AGENT_EXECUTION_LOG`, `MANUAL_RERUN_CONTROL_ACTION_AUDIT` 결과가 응답과 같은 뜻인지 비교
- H2 잠금 오류인지, 실행 순서 문제인지, 코드 문제인지 최종 판단

## 참고: 마지막 확인 포인트
- 이 문서 상단만 보면 적용 순서와 입력/출력 흐름을 다시 잡을 수 있다.
- 응답 의미 비교와 H2 결과 해석이 필요하면 [manual-rerun-response-guide.md](/home/seaung13/workspace/agile-runner/docs/manual-rerun-response-guide.md), [TASK-0004-script-application-representative-verified.md](/home/seaung13/workspace/agile-runner/.agents/outer-loop/retrospectives/SPEC-0031/TASK-0004-script-application-representative-verified.md) 를 함께 본다.

## 빠른 참조 마감 전 마지막 확인 질문
- 지금 보고 있는 실패가 종료 코드 표, 출력 파일 누락 카드, H2 조회 실패 카드 중 어디서 먼저 좁혀졌는가.
- 첫 카드에서 고른 로그 또는 파일을 실제로 다시 열어 같은 실행 키 기준으로 확인했는가.
- 상세 예시 표나 H2 마지막 확인 질문까지 내려가도 같은 실패 유형으로 읽히는가.
- 응답 의미가 헷갈리면 `manual-rerun-response-guide.md`, 마지막 대표 검증 비교가 필요하면 `TASK-0004-script-application-representative-verified.md`를 실제로 함께 열었는가.
