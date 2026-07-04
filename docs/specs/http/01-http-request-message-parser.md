# HTTP Stage 01. HTTP 요청 메시지 파서 만들기

상태: Draft v0.3
작성일: 2026-06-13
수정일: 2026-06-29
Mission: HTTP 서버 직접 만들기
범위: 첫 Stage 상세 설계

## 결론

첫 Stage는 `HTTP 요청 메시지 파서 만들기`다.

대상은 Spring Boot CRUD API 경험은 있지만 HTTP 요청이 Controller까지 도달하기 전 과정을 직접 설명하기 어려운 학습자다.

쉽게 말하면, 브라우저가 보낸 글자 덩어리를 Java 객체로 바꾸는 연습이다.

- 구현 대상은 request line, headers, blank line, body를 파싱하는 `HttpRequestParser`다.
- TCP socket server, HTTP response, routing, JSON parser, HTTP/2는 범위에서 제외한다.
- Spring Web, Servlet API, Netty, Tomcat 같은 HTTP abstraction은 사용하지 않는다.
- 완료 기준은 정상/실패 테스트, 금지 조건 준수, CLI 검사 결과, 선택 회고다.

## 대상 학습자

Spring Boot로 간단한 API를 만들어본 적은 있지만, HTTP 요청이 Controller 메서드에 도달하기 전에 어떤 처리 과정을 거치는지 직접 설명하기 어려운 학습자를 대상으로 한다.

전제:

- Java 기본 문법, 클래스, 인터페이스, 예외를 사용할 수 있다.
- JUnit으로 간단한 테스트를 작성하거나 실행할 수 있다.
- Spring Boot에서 `@RestController`, `@GetMapping`, `@PostMapping`을 사용해본 적이 있다.

## 학습 목표

이 Stage를 마치면 다음을 설명할 수 있어야 한다.

- HTTP 요청 메시지가 request line, headers, blank line, body로 구성된다는 점
- request line에서 method, request target, HTTP version을 분리하는 방법
- request target에서 path와 query string을 분리하는 방법
- header 이름과 값을 파싱하는 방법
- `Content-Length` 기준으로 body를 읽고 검증하는 방법
- Spring MVC에서는 이 책임이 WAS, servlet request, DispatcherServlet 앞단으로 이동한다는 점

## 구현 범위

HTTP 요청은 크게 세 부분으로 나뉜다. 첫 줄은 요청의 목적을 말하고, header는 부가 정보를 담고, 빈 줄 뒤에는 body가 온다.

### HttpRequestParser

`HttpRequestParser`는 raw HTTP request 문자열을 입력으로 받아 `HttpRequest` 객체를 반환한다.

첫 Stage에서는 문자열 입력 기반 구현을 기본으로 한다. `InputStream` 기반 구현은 선택 확장으로 둔다.

`HttpRequest`는 최소한 다음 값을 가진다.

- method
- request target
- path
- query string
- HTTP version
- headers
- body

### Request line 파싱

첫 줄은 다음 형식을 따른다.

```http
METHOD REQUEST_TARGET HTTP_VERSION
```

예:

```http
GET /missions/1?tab=result HTTP/1.1
```

파서는 위 요청에서 다음 값을 분리해야 한다.

- method: `GET`
- request target: `/missions/1?tab=result`
- path: `/missions/1`
- query string: `tab=result`
- HTTP version: `HTTP/1.1`

### Header 파싱

request line 다음부터 빈 줄 전까지는 header로 처리한다.

```http
Host: agile-runner.dev
Content-Type: application/json
Content-Length: 16
```

이름과 값을 `:` 기준으로 나눈다. 이름과 값 주변의 불필요한 공백은 제거한다.

첫 Stage에서는 header 이름을 대소문자 구분 없이 조회할 수 있으면 충분하다. 같은 header가 여러 번 등장하는 경우는 범위에서 제외한다.

### Body 파싱

빈 줄 이후의 내용은 body로 처리한다. `Content-Length`가 있으면 해당 길이만큼 body를 읽는다.

첫 Stage에서는 chunked transfer encoding을 다루지 않는다.

예:

```http
POST /missions HTTP/1.1
Host: agile-runner.dev
Content-Type: application/json
Content-Length: 16

{"title":"HTTP"}
```

### 오류 처리

잘못된 요청은 `MalformedHttpRequestException` 같은 명확한 예외로 표현한다.

최소 오류 케이스:

- request line이 비어 있음
- request line이 세 부분으로 나뉘지 않음
- HTTP version이 `HTTP/{major}.{minor}` 형식이 아님
- header에 `:`가 없음
- `Content-Length`가 숫자가 아님
- `Content-Length`보다 body가 짧음

첫 Stage에서는 모든 HTTP 오류를 status code로 변환하지 않는다. 오류를 HTTP response로 바꾸는 일은 이후 Stage에서 다룬다.

## 제약 조건

다음은 사용하지 않는다.

- Spring MVC request parsing 기능
- Tomcat, Jetty, Undertow, Netty의 HTTP parser
- 외부 HTTP parser 라이브러리
- 웹 프레임워크가 제공하는 request 객체

다음은 지켜야 한다.

- Java 21에서 실행 가능해야 한다.
- 파싱 로직은 테스트 가능한 순수 Java 코드로 분리한다.
- 정상 케이스와 실패 케이스를 모두 테스트한다.
- 테스트를 통과하기 위해 요구사항을 우회하지 않는다.

## CLI 통과 조건

`agrun check`는 `.agile/stage.yml`에 정의된 규칙을 사용한다.

MVP에서 확인할 항목:

- `./gradlew test`가 성공한다.
- 금지 dependency가 없다.
- 금지 import가 없다.
- `HttpRequestParser` 관련 테스트가 존재한다.
- 실패 케이스 테스트가 최소 기준을 만족한다.
- `.agile/reflection.md`가 존재한다.

`agrun check`는 학습자의 이해를 완전히 채점하지 않는다. Stage를 제대로 수행했는지 확인할 수 있는 최소 근거를 수집한다.

## 회고

회고는 강제 공개 대상이 아니다. 학습자가 원할 때만 작성하고 공개한다.

권장 질문:

- 직접 구현해보니 HTTP 요청 파싱에서 가장 실수하기 쉬운 부분은 무엇이었는가?
- Spring MVC에서는 이 책임이 어디로 이동한다고 이해했는가?
- AI Agent를 사용했다면, 어떤 부분은 직접 판단했고 어떤 부분은 도움을 받았는가?

## 결과물

Stage 완료 후 남는 결과물:

- 구현 코드
- 테스트 코드
- `.agile/check-result.json`
- fork repository URL
- 선택 회고

Mission 완료 후 사용자는 풀이를 선택적으로 공개할 수 있다.

## 비범위

이 Stage에서 제외할 것은 다음이다.

- TCP socket server
- HTTP response writer
- routing
- JSON parser
- HTTP/2
- chunked transfer encoding
- Servlet API
- Spring MVC
- hidden test 기반 자동 채점

## 다음 Stage 후보

- URL path와 query parameter 처리 고도화
- HTTP response message 만들기
- 간단한 router 만들기
- TCP socket으로 request/response 연결하기
