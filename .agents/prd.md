# Agile Runner PRD

## 문서 목적
이 문서는 `agile-runner`의 현재 제품 상태, 핵심 문제, 제약 조건, 확장 방향을 정리하는 기준 문서다.
앞으로 생성할 `Spec`, `ValidationCriteria`, `Task`와 이를 추적하는 GitHub Issue는 이 문서를 기준으로 범위와 완료 조건을 해석한다.

## 제품 개요
`Agile Runner`는 GitHub Pull Request webhook을 받아 PR diff를 분석하고, OpenAI를 통해 리뷰 내용을 생성한 뒤 GitHub 코멘트로 다시 등록하는 백엔드 서비스다.
현재는 로컬 개발 환경에서 `agent-runtime` H2 저장소를 통해 `TaskRuntimeState`, `WebhookExecution`, `ValidationCriteria`, `Execution Log`를 수집할 수 있고, 운영 환경에서는 이 수집 기능을 기본 비활성화한다.

## 현재 목표
현재 우선 목표는 사용자 제어 기능을 넓히는 것보다, webhook 기반 리뷰 흐름을 안정화하고 실패 원인과 실행 흔적을 추적 가능하게 만드는 데 있다.
즉, 지금 단계의 제품 목표는 `자동 리뷰 기능의 확장`보다 `운영 안정성`, `실행 가시성`, `개발 작업 체계화`에 더 가깝고, 이 안정화 결과가 Pull Request 작성자와 리뷰어 경험 개선으로 이어지게 만드는 데 있다.

## 용어 정의
- `Issue`: `Task` 분해 이후 각 `Task`를 외부에서 추적하기 위해 생성하는 GitHub 이슈 단위다. 문제, 목표, 제약 조건, 제외 범위, 완료 기준을 담는다.
- `Spec`: 구현해야 할 동작과 제약을 고정하는 문서다. 작업 분해나 회고 내용은 포함하지 않는다.
- `ValidationCriteria`: 통과/실패를 판단하는 검증 기준이다. 외부에서 관찰 가능한 조건과 테스트 관점을 기준으로 작성한다.
- `Task`: `Spec`을 완료하기 위해 수행하는 실행 작업 단위다. 하나의 `Task`는 하나의 명확한 결과를 가져야 하며, 각 `Task`는 하나의 GitHub Issue로 추적한다.
- `WebhookExecution`: GitHub webhook 요청 1건의 처리 단위다. 현재 구현에는 일부 `ReviewRun` 이름이 남아 있지만, 본 문서에서는 `WebhookExecution`으로 설명한다.
- `TaskRuntimeState`: 로컬 `agent-runtime` 저장소에서 `Task`의 진행 상태와 재시도 맥락을 기록하는 논리 개념이다. 현재 구현에는 일부 `TaskState` 이름이 남아 있다.
- `Artifact`: `Issue`, `Spec`, `ValidationCriteria`, `Task`, prompt snapshot, 실행 로그, diff, 테스트 결과, 에러 요약처럼 작업과 실행의 근거가 되는 산출물이다.
- `개발 피드백 루프`: 작업 결과와 실행 근거를 모아 다음 `Issue`, `AGENTS.md`, 개발 가이드를 개선하는 내부 피드백 체계다. 실제 문서 저장 경로는 `.agents/outer-loop/`다.

## 주요 사용자
- 1차 운영자는 `Agile Runner`를 개발하고 운영하는 내부 개발자다.
- 1차 수혜자는 자동 리뷰 코멘트를 받는 Pull Request 작성자와 리뷰어다.
- 현재 PRD의 우선순위는 1차 수혜자 대상 기능 확장보다 1차 운영자의 안정성 확보와 실행 추적 강화에 둔다.

## 문제
- PR 리뷰 자동화의 기본 흐름은 존재하지만, 운영 안정성과 실패 원인 추적이 아직 충분히 강하지 않다.
- 실행 중 어떤 단계에서 실패했는지, 어떤 기준을 통과하지 못했는지, 다음에 무엇을 개선해야 하는지 한 번에 파악하기 어렵다.
- 현재 구조는 기능 추가는 가능하지만, webhook 검증, 재시도, 리뷰 품질 개선, 사용자 제어 기능을 확장하기 위한 기준 문서가 부족하다.
- 개발 작업을 `PRD -> Spec -> ValidationCriteria -> Task`로 일관되게 분해하고, 각 `Task`를 GitHub Issue로 추적하기 위한 상위 제품 정의가 아직 없다.

## 목표 동작
- GitHub Pull Request 이벤트 중 리뷰 대상 이벤트만 안정적으로 수신하고 처리할 수 있어야 한다.
- 리뷰 생성, 코멘트 등록, 실패 기록이 단계별로 구분되어 추적 가능해야 한다.
- 로컬 개발에서는 `agent-runtime` 데이터를 기반으로 실행 흔적과 개선 포인트를 수집할 수 있어야 한다.
- 운영 환경에서는 기존 webhook 처리 계약을 유지하면서도, 향후 검증 강화와 확장 기능을 안전하게 붙일 수 있어야 한다.
- 이후 작업은 이 PRD를 기준으로 `Spec -> ValidationCriteria -> Task`로 분해되고, 각 `Task`를 GitHub Issue로 연결할 수 있어야 한다.

## 제약 조건
- 기술 스택은 현재의 Java, Spring Boot, Gradle 기반을 유지한다.
- 외부 연동은 GitHub App/Webhook, OpenAI API 중심 구조를 유지한다.
- webhook 응답 계약은 외부 서비스와의 연동 안정성을 해치지 않는 범위에서만 변경한다.
- 로컬 개발 환경에서는 `agent-runtime` 로그 수집을 기본 활성화하고, 운영 환경에서는 기본 비활성화한다.
- 개발 작업은 `PRD -> Spec -> ValidationCriteria -> Task`로 먼저 분해하고, 각 `Task`는 GitHub Issue로 추적한다.
- 공통 `ApiResponse` 래퍼는 기본 규칙으로 사용하지 않는다.

## 제외 범위
- 별도의 관리자 UI 또는 대시보드 구축
- 팀 공용 장기 저장소(PostgreSQL 등)와 중앙 분석 인프라 운영
- Jira, GitLab 등 외부 협업 도구와의 직접 연동
- PR 코드를 자동 수정하거나 패치를 생성하는 기능
- AGENTS.md 자동 갱신을 포함한 개발 피드백 루프 완전 자동화

## 현재 단계 완료 기준
- [ ] 로컬 및 테스트 환경에서 외부 secret 없이도 기본 애플리케이션 컨텍스트가 기동 가능하다.
- [ ] GitHub webhook 요청 1건의 처리 흐름에서 리뷰 생성, 코멘트 등록, 실패 지점이 추적 가능하다.
- [ ] 로컬 환경에서 `agent-runtime` 저장소에 `TaskRuntimeState`, `ValidationCriteria`, `WebhookExecution`, `Execution Log`가 남는다.
- [ ] 운영 환경에서는 `agent-runtime` 수집 기능이 기본 비활성화된다.
- [ ] 다음 단계 작업을 `PRD -> Spec -> ValidationCriteria -> Task`로 분해하고 각 `Task`를 GitHub Issue로 연결할 수 있을 정도로 제품 목표와 제약이 정리되어 있다.

## 성공 지표
- 동일 webhook 재처리 회귀 시나리오에서 중복 본문 코멘트가 발생하지 않는다.
- 성공 또는 실패한 `WebhookExecution`은 상태, `ValidationCriteria`, `Execution Log`를 빠짐없이 남긴다.
- 로컬 및 테스트 환경의 기본 컨텍스트 기동은 외부 secret 없이 재현 가능하다.
- 실패한 webhook 처리 흐름은 어느 단계에서 실패했는지 한 번에 식별 가능하다.

## 현재 상태
- `/webhook/github` 엔드포인트가 GitHub webhook을 수신한다.
- 최상위 `pull_request` 이벤트만 리뷰 후보로 처리하며, action 화이트리스트는 아직 없다.
- 중복 delivery는 애플리케이션 메모리 캐시로 일정 시간 동안만 방지한다.
- PR patch를 조회하고 파싱한 뒤, prompt 파일을 기반으로 OpenAI 리뷰를 생성한다.
- GitHub 본문 코멘트와 인라인 코멘트를 등록한다.
- 인라인 코멘트 일부 실패는 전체 요청 실패 대신 개별 skip 처리된다.
- 로컬 환경에서 `agent-runtime` H2 저장소에 `TaskRuntimeState`, `ValidationCriteria`, `WebhookExecution`, `Execution Log`를 저장할 수 있다.

## 현재 한계
- webhook signature 검증이 아직 없다.
- `pull_request` 이벤트 내부의 action 필터링 정책이 충분히 구체적이지 않다.
- 중복 delivery 방지는 메모리 캐시에 의존하므로 프로세스 재시작 이후에는 이어지지 않는다.
- 본문 코멘트 등록 이후 인라인 코멘트 단계에서 실패하면 재시도 시 중복 코멘트가 생길 수 있다.
- 실패한 `WebhookExecution`을 다시 조회하거나 재처리하는 명시적 기능이 없다.
- prompt 버전, artifact, retrospective를 연결하는 개발 피드백 루프는 아직 초기 단계다.
- 문서 용어와 현재 `agent-runtime` 코드 용어가 아직 완전히 정렬되지 않았다. 현재 구현에는 `TaskState`, `ReviewRun` 같은 이전 이름이 남아 있다.

## 우선순위 로드맵
### Now
- 운영 안정성 강화
  - 현재 가장 큰 버그와 운영 리스크를 줄이기 위한 우선 영역이다.
  - webhook signature 검증
  - `pull_request` action 화이트리스트 정리
  - 프로세스 재시작 이후에도 유지되는 중복 처리 전략 검토
- 실패 대응 강화
  - 실패 원인을 바로 식별하고, 재처리 전략을 설계하기 위한 기반 영역이다.
  - `WebhookExecution` 재시도 정책
  - 실패 유형 분류와 dead-letter 성격의 보관
  - 외부 연동 실패 시 단계별 에러 가시성 강화

### Next
- 리뷰 품질 개선
  - 안정화 이후 자동 리뷰 결과의 신뢰도를 높이기 위한 단계다.
  - prompt 버전 또는 snapshot 기록
  - OpenAI 응답 형식 검증 강화
  - path/line 불일치 인라인 코멘트 보정 또는 필터링
- 예외 체계 정리
  - 실패 원인 분류와 경계별 예외 해석을 더 명확하게 만들기 위한 단계다.
  - `AgileRunnerException` 계층 도입 검토
  - `ErrorCode` 기반 예외 분류와 로깅 기준 정리
  - webhook/controller/service 경계별 예외 변환 및 응답 매핑 규칙 정리

### Later
- 사용자 제어 기능
  - 기본 안정화 이후 사용자가 실행 범위를 직접 제어할 수 있게 하는 단계다.
  - dry-run 실행
  - label 또는 comment 기반 rerun
  - 특정 action 또는 조건에 따른 선택 실행
- 관측성과 회고 지원
  - 로컬 수집 이후 조회와 분석 활용 범위를 넓히기 위한 단계다.
  - `WebhookExecution` 조회 API
  - artifact export

## 개발 피드백 루프
- 현재 범위의 개발 피드백 루프는 로컬 수집과 수동 또는 반자동 회고까지만 포함한다.
- `Spec`은 구현해야 할 동작과 제약을 고정하고, `ValidationCriteria`는 pass/fail 판단 기준만 소유한다.
- `Task`는 실행 가능한 작업 단위로 나누며, 하나의 `Task`는 하나의 명확한 결과와 검증 포인트를 가져야 한다.
- GitHub Issue는 하나의 `Task`를 외부에서 추적하기 위한 수단으로 사용하고, 구현의 기준 문서는 `Spec`, `ValidationCriteria`, `Task` 문서로 유지한다.
- 하나의 `Spec`은 1개 이상의 `Task`로 분해할 수 있다.
- 하나의 `Task`는 검증 과정에서 0개 이상의 `WebhookExecution` evidence를 가질 수 있다.
- 최소 artifact 저장 위치는 `.agents/` 문서와 로컬 `agent-runtime` H2 저장소로 나눈다.
- 문서 artifact의 현재 저장 위치는 `PRD`는 `.agents/prd.md`, `Spec`은 `.agents/active/spec.md`, `ValidationCriteria`는 `.agents/criteria/`, `Task`는 `.agents/active/tasks.md`, retrospective와 수정 제안서는 `.agents/outer-loop/`로 둔다.
- `Issue`는 문제, 목표, 제약 조건, 제외 범위, 완료 기준을 포함해야 한다.
- `Spec`은 범위, 핵심 시나리오, 비목표, 외부 계약을 포함해야 한다.
- `ValidationCriteria`는 criteria key, 검증 대상, 테스트 관점, pass/fail 조건을 포함해야 한다.
- `Task`는 입력, 기대 결과, 검증 방법, 연결된 GitHub Issue를 포함해야 한다.
- retrospective는 작업 요약, 실패 요약, root cause, 근거 artifact, 후속 action을 포함해야 한다.
- AGENTS.md와 개발 가이드 개선은 위 artifact를 근거로 수동 또는 반자동으로 수행하고, 완전 자동화는 현재 범위에 포함하지 않는다.

## 결정 규칙
- GitHub Issue는 `Task`가 정의된 뒤 해당 `Task`와 바로 연결한다.
- `Spec` 작성이 끝나기 전에는 `ValidationCriteria`와 `Task`를 확정하지 않는다.
- `ValidationCriteria`는 구현 시작 전에 고정하고, 변경이 필요하면 변경 사유와 영향 범위를 함께 남긴다.
- `Task` 완료 판정은 코드 변경, 테스트 결과, artifact 근거가 모두 연결될 때만 가능하다.
- retrospective는 `Task` 종료 직후 작성하고, 다음 작업 시작 전에 참고 가능해야 한다.
- PRD와 `AGENTS.md` 수정은 retrospective와 누적 artifact를 근거로 수동 승인 후 반영한다.
