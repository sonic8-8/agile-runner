# Agile Runner Mission Topics

상태: Draft v0.3
작성일: 2026-06-13
수정일: 2026-06-29
범위: Java/Spring 백엔드 개발 기본기 Mission 주제 지도

## 요약

Agile Runner의 Mission은 프레임워크가 숨긴 동작 원리를 직접 구현하게 하는 데 집중한다.

쉽게 말하면, Spring이 대신 해주던 일을 작은 연습 문제로 나눠 직접 만들어보는 목록이다.

- 대상 학습자는 Java/Spring Boot CRUD 경험은 있지만 내부 동작 원리를 재현하기 어려운 신입/주니어 백엔드 개발자다.
- 첫 Mission은 `HTTP 서버 직접 만들기`다.
- 첫 Stage는 `HTTP 요청 메시지 파서 만들기`다.
- 여기서 주제 영역은 화면 탐색을 돕는 분류일 뿐, MVP DB 모델의 별도 `Track` 객체가 아니다.

## 주제 선정 기준

Mission 후보는 다음 기준으로 고른다.

- 프레임워크가 자동화해서 평소에는 보이지 않는 동작인가?
- 작은 Stage로 나누어 직접 구현할 수 있는가?
- CLI로 최소한의 통과 조건을 검사할 수 있는가?
- 구현 후 "내가 무엇을 이해했는지" 설명할 수 있는가?
- Java/Spring 백엔드 개발자에게 면접이나 실무에서 의미 있는 기본기인가?

## 첫 Mission

첫 Mission은 `HTTP 서버 직접 만들기`로 둔다.

첫 Stage는 `HTTP 요청 메시지 파서 만들기`다.

선정 이유:

- Spring MVC를 쓰면 가장 먼저 숨겨지는 HTTP 요청 처리 흐름을 다룬다.
- starter code와 CLI 검사 조건을 작게 만들 수 있다.
- Java/Spring 백엔드 개발자에게 면접과 실무 설명 모두에서 의미가 있다.
- 이후 router, response writer, DispatcherServlet로 자연스럽게 확장된다.

## Mission 후보

| 주제 영역 | Mission 후보 | 다룰 동작 원리 |
| --- | --- | --- |
| HTTP | HTTP 서버 직접 만들기 | 요청 파싱, 응답 작성, routing |
| Spring Core | DI 컨테이너 직접 만들기 | 객체 생성, 의존성 주입, 생명주기 |
| Spring MVC | mini MVC framework 만들기 | HandlerMapping, HandlerAdapter, message converter |
| Persistence | 작은 ORM 흉내내기 | ResultSet mapping, query builder, connection 관리 |
| Transaction | Transaction manager 직접 만들기 | commit, rollback, propagation |
| Concurrency | 동시 요청 처리 실험실 만들기 | race condition, lock, thread pool |
| Traffic | 트래픽 방어 패턴 직접 만들기 | rate limit, cache, retry, circuit breaker |
| Realtime | 실시간 수색 지도 만들기 | polling, SSE, WebSocket, broadcast |

## 첫 Mission의 Stage 후보

| 순서 | Stage 후보 | MVP 여부 |
| --- | --- | --- |
| 1 | HTTP 요청 메시지 파서 만들기 | MVP |
| 2 | URL path와 query string 처리하기 | 이후 |
| 3 | HTTP 응답 메시지 만들기 | 이후 |
| 4 | 간단한 router 만들기 | 이후 |
| 5 | TCP socket으로 request/response 연결하기 | 이후 |
| 6 | mini DispatcherServlet 만들기 | 이후 |

## 보류

- Mission별 난이도와 예상 시간 표기
- 성장 시스템과 XP 정책
- 사용자 제작 Mission 검수 기준
- AI Agent 사용 흐름 기록
- 대규모 트래픽 Mission의 실제 배포 환경 비용
