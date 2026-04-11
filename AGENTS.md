## 범위
- 이 규칙은 `agile-runner` 저장소의 Java/Spring 백엔드 코드와 작업 절차에 적용한다.
- 현재 사용하지 않는 기술 규칙은 강제하지 않고 `해당 시` 참고 규칙으로만 둔다.

## 작업 시작 규칙
- 의미 있는 개발 작업은 `PRD -> Spec -> ValidationCriteria -> Task`를 먼저 고정한 뒤, 해당 `Task`에 GitHub Issue를 연결한다.
- 예외는 오타 수정, 주석 정리, 로컬 실험처럼 추적 비용이 더 큰 아주 작은 작업뿐이다.
- Issue는 최소한 아래 항목을 포함한다.
  - `문제`
  - `목표`
  - `제약 조건`
  - `제외 범위`
  - `완료 기준`
- 작업자는 `PRD`, `Spec`, `ValidationCriteria`, `Task`, Issue를 함께 기준으로 구현 범위와 완료 조건을 해석한다.
- 기존 Issue를 재사용할 경우, 구현 시작 전에 Issue 제목과 본문이 현재 `Task` 범위와 1:1로 맞는지 먼저 확인한다.
- Issue 생성이 깨지거나 같은 `Task`에 잘못된 Issue가 중복 생성되면, 작업을 이어가기 전에 canonical Issue 하나만 남기고 나머지는 즉시 정리한다.
- 하나의 `Task`가 하나의 GitHub Issue와 1:1로 연결된 경우, task 완료와 개발 피드백 루프 산출물 작성까지 끝나면 해당 Issue를 닫는다.
- 하나의 GitHub Issue가 여러 `Task`를 포함하면, 현재 task만 끝났더라도 Issue는 닫지 않고 상태만 갱신한다.
- 로컬 개발 환경에서는 agent runtime 로그 수집을 기본 활성화한다.
- 운영 환경에서는 agent runtime 로그 수집을 기본 비활성화한다.
- Issue에는 최소한 `⌨️ BE` 라벨과 작업 성격 라벨을 붙인다.
- 작업 성격 라벨은 기본적으로 아래 중 하나를 사용한다.
  - `✨ Feature`
  - `🚨 Bug`
  - `🔧 Refactor`
  - `⚙️ Chore`
- 필요할 때만 아래 보조 라벨을 추가한다.
  - `🌏 Infra`
  - `🧪 Test`
  - `📄 Docs`

## 베이스라인 하네스
- `Orchestrator`는 현재 활성 `Spec`과 `Task`를 읽고, 이번에 수행할 `Task` 하나를 선택해 task packet을 고정한다.
- 리팩터링 또는 이름 정리 중심 `Spec`을 시작할 때는 첫 `Task`를 만들기 전에 기존 회귀 안전망 목록 확인 절차를 우선 검토한다.
- 기존 컨트롤러/서비스 통합 테스트와 유지 계약 대응 관계가 이미 충분하면, 별도 안전망 `Task`를 새로 만들기보다 현재 `Spec` 또는 `Task` 문서에 그 근거만 기록하는 방식을 우선 검토한다.
- task packet에는 최소한 task 목표, 연결된 `ValidationCriteria`, 관련 파일 후보, 비대상, AGENTS 체크 항목이 포함돼야 한다.
- 코드 용어 정리와 물리 스키마 정리가 함께 필요한 `Spec`은 두 단계를 나눌 필요가 있는지 먼저 검토한다.
- 먼저 코드 이름 정리와 외부 공개 시그니처 정합성을 닫고, 그다음 물리 스키마 이름 정리와 실제 앱/H2 검증을 수행하는 구성을 우선 검토한다.
- `Tester`는 연결된 `ValidationCriteria`를 테스트와 체크리스트로 고정하고, AGENTS.md 컨벤션 위반 여부를 함께 검증한다.
- `Tester`는 production code를 수정하지 않고, controller/service integration 중심의 black-box 테스트 코드를 먼저 작성해 기대 동작을 고정한다.
- task 완료 조건이 `write 이전 준비 완료` 또는 `side effect 미발생`이면, `Tester`는 black-box 테스트에서 모든 외부 write 경로가 미발생인지 함께 검증한다.
- `Constructor`는 task packet과 tester 검증 기준을 바탕으로 구현과 작은 Red -> Green 루프를 수행한다.
- `Tester`는 구현 후 behavior 통과 여부와 AGENTS.md 컨벤션 통과 여부를 다시 확인한다.
- `Tester` 2차 단계에서는 task 관련 targeted test를 확인한 뒤, 가능하면 저장소 표준 전체 테스트 실행 명령까지 확인한다.
- `agent-runtime`처럼 설정이나 프로필에 따라 비활성화될 수 있는 bean에 새 의존성을 추가한 task는, 종료 검증에서 기본 컨텍스트 기동 근거도 함께 확인한다.
- task 종료 검증에서 targeted test와 전체 테스트는 기본적으로 순차 실행하고, 같은 workspace에서 동시에 실행하지 않는다.
- 같은 workspace 산출물을 공유하는 테스트 명령은 `multi_tool_use.parallel`로 묶지 않는다.
- 병렬 실행이 필요하면 테스트 결과 출력 경로를 분리하거나, 같은 workspace 산출물을 공유하지 않는 방식만 허용한다.
- 저장소 표준 전체 테스트 실행을 생략하면 생략 사유를 retrospective에 남긴다.
- 실패 시 작업은 `Constructor`로 되돌리고, 수정 후 다시 검증한다.
- `/webhook/github` 흐름, controller orchestration, `agent-runtime` 저장 또는 runtime failure handling을 변경한 task는 `Orchestrator` 종료 판정 전 실제 애플리케이션 기동과 H2 file DB 생성, 실제 앱/H2 대표 검증을 확인한다.
- 실제 앱/H2 대표 검증에 사용하는 `delivery_id`는 이전 검증과 겹치지 않는 새 값으로 정하고, 기존 local H2 row와 충돌하지 않게 관리한다.
- 실제 앱/H2 대표 검증이 실패하면 schema 또는 runtime failure로 단정하기 전에 `delivery_id` 재사용 충돌 여부를 먼저 확인한다.
- local H2 file DB를 외부에서 조회하는 실제 앱/H2 대표 검증은 기본적으로 `앱 기동 -> 새로운 delivery_id로 대표 검증 요청 1건 실행 -> HTTP 결과 확인 -> 앱 종료 -> H2 CLI 또는 SQL 조회 도구로 evidence 확인` 순서로 진행한다.
- local H2 file DB를 Shell 또는 SQL CLI로 후속 확인할 때, 같은 H2 file을 대상으로 하는 조회는 기본적으로 순차 실행한다.
- 같은 H2 file에 대한 Shell 또는 SQL CLI 조회를 병렬로 열지 않는다.
- 실제 앱/H2 대표 검증이 synthetic source execution 또는 synthetic runtime evidence seed를 먼저 필요로 하고, 현재 task가 `agent-runtime` 물리 스키마를 바꾸면 seed 전에 현재 `schema.sql`을 local H2 file DB에 먼저 적용할지 우선 검토한다.
- 현재 `Spec`이 `정책 또는 저장 seam 연결 task`와 `representative 실제 앱/H2 검증 task`를 명시적으로 분리했다면, 앞선 task는 `targeted test + 전체 cleanTest test + repository 또는 H2 mem 수준 저장 seam 검증 + retrospective 경고사항 기록`을 근거로 종료할 수 있다.
- 이 경우에도 representative 실제 앱/H2 검증은 뒤 task에서 필수로 수행해야 하며, 앞선 task의 retrospective에 검증을 다음 task로 넘긴 이유와 남은 위험을 함께 남긴다.
- 애플리케이션 실행 중 H2 조회가 실패하면 schema 또는 runtime failure로 바로 단정하지 말고, 먼저 H2 file lock 여부를 확인한다.
- 앱 종료 후 H2 Shell 또는 SQL CLI 조회가 lock으로 실패하면 schema 또는 runtime failure로 단정하지 말고, 먼저 앱 프로세스 종료 여부와 다른 H2 Shell 조회 동시 실행 여부를 확인한다.
- 다른 순서로 검증하면 그 이유를 retrospective에 남긴다.
- 실제 애플리케이션 검증이 불가하면 사유와 남은 위험을 retrospective에 남긴다.
- `Orchestrator`는 테스트 근거, AGENTS 체크 결과, artifact 연결이 모두 충족될 때만 task 완료를 선언한다.

## 개발 피드백 루프
- 각 `Task` 종료 직후에는 반드시 개발 피드백 루프를 한 번 실행한다.
- 자동 원시 로그는 H2 `agent-runtime`에 저장하고, 판단과 개선 제안은 `.agents/outer-loop/` 문서 저장소에 남긴다.
- 개발 피드백 루프는 최소한 `task retrospective` 1개와 필요 시 `수정 제안서` 1개 이상을 생성한다.
- 현재 활성 `Spec`의 마지막 `Task`가 끝나면, 다음 `Spec`으로 넘어가기 전에 `SPEC-xxxx-summary.md`를 작성하고 `registry.json`의 `latest.spec_summary_path`를 갱신한다.
- 개발 피드백 루프 초안이 만들어지면, 다음 `Task`를 제안하기 전에 지금이 개발 피드백 루프 차례라는 점을 명시하고 수집된 메타 데이터를 먼저 보고한다.
- 메타 데이터 보고에는 최소한 linked Issue 상태, retrospective 경로, proposal 경로, targeted/full test 결과, 실제 앱/H2/runtime 검증 결과, representative delivery 또는 execution 근거가 포함돼야 한다.
- 실제 앱/H2 대표 검증을 수행한 retrospective에는 실제 사용한 `delivery_id`와 `execution_key`를 함께 남긴다.
- proposal이 생기면 사용자에게 채택/보류/반려 판단을 먼저 받고, 그 결정이 끝나기 전까지 다음 `Task`를 제안하지 않는다.
- retrospective에는 task 요약, 실패 요약, root cause, 근거 artifact, AGENTS 위반/보완점, 다음 task 경고사항이 포함돼야 한다.
- 수정 제안서는 `AGENTS.md` 또는 workflow 수정 제안을 문서로 남기고, 상태는 `proposed | accepted | rejected | superseded` 중 하나로 관리한다.
- `AGENTS.md`와 workflow 문서는 proposal 승인 전까지 자동 수정하지 않는다.
- rejected 또는 superseded 제안도 삭제하지 않고 상태와 근거를 유지한다.

## 브랜치 / PR / 커밋
- 브랜치 이름은 `type/issue-number-short-summary` 형식을 우선 사용한다.
- 예시:
  - `feat/123-review-run-log`
  - `fix/87-webhook-duplicate-check`
  - `refactor/132-github-client-split`
- PR 본문에는 관련 Issue를 연결한다.
- PR merge 시 자동 종료가 필요하면 `Closes #123`, 참조만 할 경우 `Refs #123`를 사용한다.
- 커밋 메시지 형식은 `[BE] type(scope): 설명`을 우선 사용한다.
- 커밋 제목은 `SPEC-0001`, `TASK-0001`, `spec`, `task` 같은 내부 식별자나 운영 용어보다 실제 변경 결과를 우선 드러낸다.
- 커밋 제목은 git 히스토리만 봐도 대략 무슨 변화인지 읽히는 표현을 사용하고, 내부 추적 정보는 본문이나 문서에 둔다.
- 운영용 조회, 관리자 제어, 대시보드 후보 기능처럼 나중에 운영 화면이나 관리 API에서 쓰일 기능은 커밋 제목에서도 그 사용 맥락이 읽히는 표현을 우선 사용한다.
- `상태`, `처리`, `근거`처럼 추상적인 말만 쓰기보다 `확인 완료 처리`, `실행 목록 조회`, `GitHub 코멘트 작성 여부`처럼 운영자가 실제로 읽거나 누르는 대상을 드러내는 표현을 우선 검토한다.
- 커밋 제목은 가능한 한 자연스러운 한국어 명사형으로 마무리한다.
- 커밋 메시지 본문 또는 PR 본문에 GitHub Issue 번호를 연결한다.
- `Task`는 개발 피드백 루프 산출물까지 끝난 뒤 커밋한다.
- 현재 활성 `Spec`이 끝나면 `SPEC-xxxx-summary.md` 작성과 proposal 처리까지 마친 뒤 push 여부를 확인한다.
- 혼자 진행하는 개인 프로젝트는 PR을 필수로 두지 않고, direct push를 기본으로 검토한다.
- 큰 단위 변경이나 추가 검토가 필요할 때만 선택적으로 PR을 사용한다.

## 핵심 규칙
- 패키지는 계층을 먼저 나누고, 계층 안에서 도메인 또는 기능별로 나눈다.
- 최상위 패키지는 `api`, `domain`, `client`, `config`를 사용한다.
- 애플리케이션 시작 클래스는 루트 패키지에 둔다.
- `config`에는 스프링 설정과 bean 구성만 둔다.
- 외부 시스템 연동 코드는 `client`에 둔다.
- 범용 `util` 패키지는 새로 만들지 않는다.
- 기존 `util` 성격 코드는 가능한 범위에서 책임이 맞는 `client`, `domain`, `api` 하위로 이동한다.
- 새 코드는 기존 구조보다 더 명확한 구조로 정리할 수 있으면 함께 정리한다.
- 과한 추상화보다 명확한 구현을 우선한다.
- `Reader`, `Provider` 같은 추가 추상화는 구현 교체 필요나 외부 시스템 경계가 분명할 때만 검토한다.

## 패키지 규칙
- Controller는 `api/controller/{domain}`에 둔다.
- Controller Request DTO는 `api/controller/{domain}/request`에 둔다.
- Controller Response DTO가 필요하면 `api/controller/{domain}/response`에 둔다.
- Service는 `api/service/{domain}`에 둔다.
- Service Request DTO는 `api/service/{domain}/request`에 둔다.
- Service Response DTO는 `api/service/{domain}/response`에 둔다.
- `domain` 하위는 애그리거트 또는 강한 응집도를 기준으로 묶는다.
- 하위 개념을 근거 없이 `domain` 바로 아래 평평하게 늘어놓지 않는다.
- 외부 API, 인증, 패치 파싱, 코멘트 작성 같은 외부 연동 세부 구현은 `client/{system}/...` 하위로 둔다.

## 계층 책임
- Controller는 HTTP 요청 수신, 입력 검증, Service 호출, HTTP 응답 반환만 담당한다.
- Webhook 엔드포인트이더라도 응답 스키마는 외부 서비스와의 계약에 맞춰 명시적으로 설계한다.
- 공통 `ApiResponse` 래퍼는 기본 규칙으로 사용하지 않는다.
- Service는 유스케이스 실행, 트랜잭션 경계, 외부 연동 orchestration, Domain과 DTO 연결을 담당한다.
- Domain은 핵심 상태와 비즈니스 규칙을 가진다.
- 외부 시스템별 세부 구현은 `client`가 담당한다.

## 네이밍
- 기본 접미사는 `Controller`, `Service`, `Client`, `Repository`, `Request`, `ServiceRequest`, `Response`, `Config`, `Test`, `TestSupport`를 사용한다.
- Service 명명은 기본적으로 `...Service`를 사용하고, 조회/변경 책임 분리가 필요해질 때 `...QueryService`, `...CommandService`로 구체화한다.
- DTO는 기본적으로 `record` 대신 `class`를 사용한다.
- Controller Request DTO와 Service DTO는 분리한다.
- 객체 생성은 가능하면 `of(...)`, `from(...)`, Builder, private 생성자 패턴을 우선 검토한다.

## 코드 컨벤션
- indent depth는 가급적 2 이하로 유지한다.
- `else`, `switch/case`, 삼항 연산자 사용은 지양하고 Early return 패턴을 우선 검토한다.
- 가급적 1메서드 1기능 원칙을 지향한다.
- 예외는 특별한 이유가 없으면 static factory보다 `throw new ExceptionType(...)` 형태를 우선 사용한다.
- 핵심 도메인의 원시값과 문자열은 VO 후보로 먼저 검토한다.
- 비즈니스 로직이 포함된 컬렉션은 일급 컬렉션으로 포장할지 검토한다.
- Validation 메시지는 DTO에 하드코딩하지 않고 메시지 키 분리를 우선 검토한다.

## 테스트
- 테스트 실행은 `./gradlew test`를 우선 사용한다.
- 테스트는 JUnit 5, AssertJ를 사용하고 `@DisplayName` 한글 문장과 `given / when / then` 구조를 우선 따른다.
- 테스트는 계층 책임에 맞춰 분리한다.
- Parser, Mapper, Policy, Converter, Calculator처럼 순수 로직 중심 클래스는 Spring 컨텍스트 없이 unit test를 우선 검토한다.
- Controller 테스트는 `@WebMvcTest` 기반 슬라이스 테스트를 기본으로 검토한다.
- Service 테스트는 실제 Spring wiring 검증이 필요할 때만 통합 테스트를 사용한다.
- 테스트 실행 시간 최적화는 Spring context cache 재사용과 불필요한 통합 테스트 축소를 우선 검토한다.
- 외부 라이브러리 타입이 오버로드, 브리지, final 메서드 때문에 Mockito stubbing이 애매한 경우 fake object 또는 override 기반 test double을 우선 검토한다.
- focused rerun만 통과하고 full suite에서 흔들리는 stubbing은 안정적인 test double로 치환한다.
- GitHub API처럼 SDK 객체가 풍부한 라이브러리는 mock method return stubbing보다 가벼운 fake object와 호출 기록 방식을 우선 검토한다.

## 해당 시 적용 규칙
- JPA 엔티티를 도입할 때는 `@Getter`, `@Entity`, `@NoArgsConstructor(access = PROTECTED)` 패턴을 우선 검토한다.
- Repository를 도입할 때는 통합 테스트와 `@ActiveProfiles("test")` 사용을 검토한다.
- 외부 HTTP Client 테스트는 `@RestClientTest`와 `MockRestServiceServer` 기반 슬라이스 테스트를 우선 검토한다.
- `@ConfigurationProperties` 테스트는 별도 property source 주입 방식을 우선 검토한다.
- Config 테스트는 `ApplicationContextRunner`를 우선 검토한다.
- REST Docs, Security, SSE 같은 기술은 실제 도입 시 그 기술에 맞는 테스트 규칙을 추가한다.
