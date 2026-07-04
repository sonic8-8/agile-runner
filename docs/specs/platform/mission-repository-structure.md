# Mission Repository Structure Spec

상태: Draft v0.2
작성일: 2026-06-13
수정일: 2026-06-29
범위: 학습자가 fork하는 공식 Mission repository 구조

## 결론

MVP의 fork 단위는 Mission repository다.

- repository 하나는 Mission 하나를 담는다.
- Mission 안에는 여러 Stage가 있다.
- Stage는 CLI 검사와 진행 상태가 기록되는 최소 단위다.
- 각 Stage directory는 자체 `.agile/stage.yml`을 가진다.
- `agrun check`는 Stage 안에서 실행하는 것을 기본 흐름으로 둔다.

예:

```text
http-server-mission/
├── README.md
├── .gitignore
├── stages/
│   ├── 01-request-message-parser/
│   ├── 02-url-path/
│   └── 03-response-message/
└── shared/
```

`shared/`는 처음부터 만들지 않는다. 두 개 이상의 Stage에서 실제 중복이 생길 때만 추가한다.

## 1. 목적

이 문서는 Agile Runner의 공식 Mission repository를 어떤 단위로 나누고, Stage를 repository 안에서 어떻게 배치할지 정의한다.

MVP에서는 Stage 하나마다 GitHub repository를 만들지 않는다. Mission을 fork 가능한 repository로 제공하고, 그 안에 여러 Stage directory를 둔다.

## 2. 용어

### Mission Repository

사용자가 fork하는 미션 저장소다.

예:

- `http-server-mission`
- `di-container-mission`
- `sqlite-mission`

Mission repository는 하나의 큰 구현 목표와 여러 Stage를 담는다.

### Stage Directory

Mission repository 안에서 실제로 검사하고 통과하는 개별 Stage 디렉터리다.

예:

```text
stages/01-request-message-parser/
```

Stage directory는 code, 테스트, `README.md`, `.agile/stage.yml`, 회고 파일을 포함한다.

### Stage Manifest

Stage directory의 `.agile/stage.yml` 파일이다.

manifest 안의 파일 경로는 Stage directory 기준으로 해석한다. 예를 들어 `src/main/java/**/*.java`, `.agile/reflection.md`, `README.md`는 모두 현재 Stage 기준 경로다.

## 3. 설계 원칙

- fork 단위는 Mission으로 둔다.
- check 단위는 Stage로 둔다.
- Stage는 독립적으로 실행할 수 있어야 한다.
- root README는 Mission 전체 안내를 담당한다.
- MVP에서는 root Gradle multi-project 구성을 사용하지 않는다.
- 각 Stage는 자신의 Gradle wrapper와 파일을 가져도 된다.
- 중복 제거보다 학습자의 실행 단순성을 우선한다.
- `.agile/check-result.json`은 생성물이며 Git에 올리지 않는다.

## 4. Repository 구조

공식 Mission repository의 기본 구조는 다음이다.

```text
http-server-mission/
├── README.md
├── .gitignore
├── stages/
│   ├── 01-request-message-parser/
│   │   ├── README.md
│   │   ├── .gitignore
│   │   ├── .agile/
│   │   │   ├── stage.yml
│   │   │   └── reflection.md
│   │   ├── build.gradle
│   │   ├── settings.gradle
│   │   ├── gradlew
│   │   ├── gradlew.bat
│   │   ├── gradle/
│   │   │   └── wrapper/
│   │   └── src/
│   │       ├── main/java/...
│   │       └── test/java/...
│   ├── 02-url-path/
│   └── 03-response-message/
└── shared/
```

Stage를 독립 Gradle project로 두는 이유는 다음이다.

- 학습자가 `cd stages/01-request-message-parser` 후 바로 `./gradlew test`를 실행할 수 있다.
- `agrun check`의 작업 디렉터리와 경로 해석이 단순해진다.
- Stage마다 금지 dependency, 테스트 명령, Java version, code 범위를 다르게 둘 수 있다.
- root Gradle multi-project 설정을 이해해야 하는 부담을 줄인다.

단점은 Gradle wrapper와 설정 파일이 반복된다는 점이다. MVP에서는 중복을 허용한다.

## 5. Root README 구조

Mission root의 `README.md`는 전체 Mission 안내만 담당한다.

포함할 내용:

- Mission 이름과 목표
- 대상 학습자와 전제 지식
- Stage 목록과 권장 순서
- repository fork 방법
- 첫 Stage 시작 방법
- 공통 환경
- Agile Runner CLI 설치와 로그인 방법
- 결과 공개와 회고 작성 원칙

root README는 특정 Stage의 상세 요구사항을 반복하지 않는다. 상세 요구사항은 Stage directory의 `README.md`에 둔다.

## 6. Stage `.agile` 구조

```text
.agile/
├── stage.yml
├── reflection.md
└── check-result.json
```

파일 역할:

- `stage.yml`: CLI와 웹 서비스가 읽는 Stage 계약
- `reflection.md`: 학습자가 작성하는 선택 회고 템플릿
- `check-result.json`: `agrun check`가 생성하는 로컬 결과 파일

`stage.yml`과 `reflection.md`는 starter repository에 포함한다. `check-result.json`은 생성물이므로 `.gitignore`에 포함한다.

## 7. `.gitignore`

Mission repository root의 `.gitignore`는 Java/Gradle 생성물과 Agile Runner check 결과를 제외한다.

최소 항목:

```gitignore
.gradle/
build/
**/.gradle/
**/build/
**/.agile/check-result.json
```

각 Stage directory에 별도 `.gitignore`를 둘 경우에도 `check-result.json`은 제외한다.

## 8. CLI 동작 기준

`agrun check`는 현재 작업 디렉터리에서 시작해 상위 디렉터리로 올라가며 가장 가까운 `.agile/stage.yml`을 찾는다.

manifest를 찾은 뒤에는 해당 `.agile` 디렉터리의 부모 디렉터리를 Stage root로 본다.

예:

```text
http-server-mission/
└── stages/
    └── 01-request-message-parser/
        ├── .agile/
        │   └── stage.yml
        └── src/main/java/...
```

사용자가 `stages/01-request-message-parser/src/main/java`에서 `agrun check`를 실행해도 CLI는 `stages/01-request-message-parser/.agile/stage.yml`을 찾아야 한다.

사용자가 Mission repository root에서 `agrun check`를 실행하면 Stage manifest가 없으므로 실행 전 중단한다. root에서 특정 Stage를 지정해 실행하는 옵션은 MVP 범위에서 제외한다.

## 9. 공식 repository 운영

공식 Mission repository는 플랫폼 코드 repository와 분리한다.

권장 구조:

```text
agile-runner-missions/http-server-mission
agile-runner-missions/di-container-mission
agile-runner-missions/sqlite-mission
```

## 10. MVP 결정 사항

- 첫 공식 Mission repository는 `http-server-mission`으로 둔다.
- 첫 Stage directory는 `stages/01-request-message-parser`로 둔다.
- 첫 Stage manifest 위치는 `stages/01-request-message-parser/.agile/stage.yml`이다.
- 첫 Stage 회고 파일 위치는 `stages/01-request-message-parser/.agile/reflection.md`이다.
- 첫 Stage result 위치는 `stages/01-request-message-parser/.agile/check-result.json`이다.
- 첫 Stage는 독립 Gradle project로 둔다.
- root Gradle multi-project 구성은 MVP에서 사용하지 않는다.

## 11. 보류 사항

- `agrun check --stage` 옵션
- 여러 Stage의 공통 코드를 `shared/`로 묶는 기준
- 사용자 제작 Mission 또는 Stage 제안 절차
