# HTTP Stage 01 Starter Repository Draft

상태: Draft v0.3
작성일: 2026-06-13
수정일: 2026-06-29
범위: `HTTP 요청 메시지 파서 만들기` Stage starter directory 초안

## 결론

- 첫 Mission은 `http-server-mission` repository로 둔다.
- 첫 Stage는 `http-server-mission/stages/01-request-message-parser` 아래에 둔다.
- Stage directory는 독립 Gradle Java project로 구성한다.
- Java 21, JUnit Jupiter, Gradle wrapper를 기본으로 사용한다.
- Spring Boot plugin과 Spring Web dependency는 사용하지 않는다.
- starter code는 public API 형태만 제공하고 파싱 구현은 비워둔다.
- 회고 파일은 `.agile/reflection.md`, manifest는 `.agile/stage.yml`에 둔다.

## 목적

이 문서는 HTTP Mission의 첫 Stage를 실제 fork용 repository 안에 배치할 때 필요한 starter code 범위와 파일 구조를 정의한다.

첫 Stage의 목표는 Spring MVC나 Servlet API 없이 HTTP 요청 메시지의 구조를 직접 파싱해보는 것이다.

## 위치

```text
http-server-mission/
└── stages/
    └── 01-request-message-parser/
```

학습자는 이 디렉터리로 이동한 뒤 Stage를 수행한다.

```bash
cd stages/01-request-message-parser
./gradlew test
agrun check
```

## 파일 구조

```text
stages/01-request-message-parser/
├── README.md
├── .gitignore
├── .agile/
│   ├── stage.yml
│   └── reflection.md
├── build.gradle
├── settings.gradle
├── gradlew
├── gradlew.bat
├── gradle/
│   └── wrapper/
└── src/
    ├── main/java/dev/agilerunner/http/
    │   ├── HttpRequest.java
    │   ├── HttpRequestParser.java
    │   └── MalformedHttpRequestException.java
    └── test/java/dev/agilerunner/http/
        └── HttpRequestParserTest.java
```

## Gradle 설정

기본 조건:

- Java 21
- Gradle wrapper 포함
- JUnit Jupiter 사용
- Spring Boot plugin 미사용
- `java` plugin만 사용

허용 dependency는 테스트를 위한 JUnit Jupiter로 제한한다.

## Starter Code

기본 package:

```text
dev.agilerunner.http
```

핵심 클래스:

- `HttpRequestParser`: raw HTTP request 문자열을 `HttpRequest`로 변환한다.
- `HttpRequest`: method, request target, path, query string, version, headers, body를 담는 값 객체다.
- `MalformedHttpRequestException`: 잘못된 요청 메시지를 표현하는 예외다.

starter code는 public API의 형태만 제공하고, 실제 파싱 구현은 비워둔다.

## Starter Test

초기 테스트에 포함할 항목:

- request line에서 method, request target, HTTP version을 분리한다.
- request target에서 path와 query string을 분리한다.
- header를 대소문자 구분 없이 조회할 수 있다.
- `Content-Length` 기준으로 body를 읽는다.

학습자가 추가해야 할 테스트 후보:

- 잘못된 request line
- colon이 없는 header
- 숫자가 아닌 `Content-Length`
- body 길이와 `Content-Length` 불일치
- 빈 요청 문자열

`agrun check`는 테스트의 의미를 완전히 채점하지 않는다. starter test 실행, 금지 dependency, 필수 파일, 테스트 이름 pattern 같은 최소 근거만 확인한다.

## 금지 항목

첫 Stage에서는 다음을 사용하지 않는다.

- Spring Boot
- Spring Web
- Servlet API
- Netty
- 외부 HTTP parser
- 외부 URL parser

금지 목적은 난이도를 높이는 것이 아니다. 프레임워크와 라이브러리가 대신 처리하던 HTTP 요청 파싱을 직접 경험하게 하려는 것이다.

## Stage Manifest

manifest 위치:

```text
stages/01-request-message-parser/.agile/stage.yml
```

예시:

```yaml
manifestVersion: 1
mission:
  id: http-server
  title: HTTP 서버 직접 만들기
stage:
  id: request-message-parser
  sequence: 1
  title: HTTP 요청 메시지 파서 만들기
spec: README.md
checks:
  - id: test-command
    type: command
    name: 테스트 실행
    severity: error
    params:
      command: ./gradlew test
      timeoutSeconds: 120
reflection:
  path: .agile/reflection.md
```

## 회고 파일

회고 파일 위치:

```text
.agile/reflection.md
```

회고 작성은 강제 채점 대상이 아니다. 다만 `required-file` check를 warning 수준으로 둘 수 있다.

## `.gitignore`

최소 항목:

```gitignore
.gradle/
build/
.agile/check-result.json
```

`stage.yml`과 `reflection.md`는 repository에 포함한다.

## 비범위

첫 starter directory에서 제외할 것은 다음이다.

- TCP socket server
- HTTP response writer
- routing
- JSON parser
- Servlet API
- Spring MVC
- Gradle multi-project 구성
- hidden test 기반 자동 채점

## 다음 작업

실제 `http-server-mission` fork용 repository를 만들 때 다음 순서로 진행한다.

1. Mission root README 작성
2. 첫 Stage directory 생성
3. Gradle Java project scaffold 생성
4. starter class와 test 작성
5. `.agile/stage.yml` 작성
6. `.agile/reflection.md` 템플릿 작성
7. `agrun check`로 manifest 탐색과 check 결과 생성 검증
