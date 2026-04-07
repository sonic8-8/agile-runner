---
name: agile-runner-task-loop
description: Use when executing a meaningful Agile Runner task under this repository's .agents workflow. Follow the Orchestrator -> Tester -> Constructor inner loop, run AGENTS.md-based 3-subagent review loops at every stage until all PASS, run the development feedback loop after each completed task, and close a 1:1 GitHub Issue only after the task and its feedback artifacts are done.
---

# Agile Runner Task Loop

이 스킬은 현재 저장소의 `.agents` 문서를 기준으로 task를 수행할 때 사용한다.
작업 기준 문서, 3개 서브에이전트 리뷰 루프, 개발 피드백 루프, Issue 종료 규칙을 한 번에 고정한다.

## 먼저 읽을 문서
1. `AGENTS.md`
2. `.agents/README.md`
3. `.agents/prd.md`
4. `.agents/active/spec.md`
5. 현재 spec에 연결된 `.agents/criteria/*.json`
6. `.agents/active/tasks.md`
7. task 종료 시 `.agents/outer-loop/README.md`

## 핵심 규칙
- 모든 단계의 서브에이전트 리뷰는 `AGENTS.md`를 기준으로 수행한다.
- 각 단계마다 3개 서브에이전트 리뷰를 돌리고, 하나라도 `NEEDS_CHANGES`가 나오면 수정 후 같은 3개 리뷰를 다시 돌린다.
- 리팩터링 또는 이름 정리 중심 spec을 시작할 때는 첫 task를 만들기 전에 기존 회귀 안전망 목록 확인 절차를 우선 검토한다.
- 기존 컨트롤러/서비스 통합 테스트와 유지 계약 대응 관계가 이미 충분하면, 별도 안전망 task를 새로 만들기보다 현재 spec 또는 task 문서에 그 근거만 기록하는 방식을 우선 검토한다.
- `Tester`는 production code를 수정하지 않는다.
- `Tester`는 controller/service integration 중심의 black-box 테스트 코드를 먼저 작성해 기대 동작을 고정한다.
- 외부 라이브러리 타입이 오버로드, 브리지, final 메서드 때문에 Mockito stubbing이 애매하면 fake object 또는 override 기반 test double을 우선 검토한다.
- `Constructor`는 tester가 만든 black-box 테스트를 green으로 만들기 위해 production code를 수정하고, 필요 시 작은 unit test를 추가한다.
- 코드 용어 정리와 물리 스키마 정리가 함께 필요한 spec은 두 단계를 나눌 필요가 있는지 먼저 검토한다.
- 먼저 코드 이름 정리와 외부 공개 시그니처 정합성을 닫고, 그다음 물리 스키마 이름 정리와 실제 앱/H2 검증을 수행하는 구성을 우선 검토한다.
- 현재 구조에서는 `inner-loop/TASK-xxxx/` 같은 새 디렉토리를 만들지 않는다. task 수행 중 필요한 최소 보완은 `.agents/active/tasks.md`에 반영한다.
- task 종료 후에는 반드시 개발 피드백 루프를 실행하고 retrospective를 남긴다.
- 개발 피드백 루프 문서가 준비되면 바로 다음 task로 넘어가지 않는다.
- 이 시점에는 먼저 "지금은 outer loop 차례"라고 명시하고, linked Issue 상태, retrospective 경로, proposal 경로, targeted/full test 결과, 실제 앱/H2/runtime 검증 결과를 사용자에게 보고한다.
- proposal이 있으면 사용자에게 채택/보류/반려 판단을 먼저 묻고, 그 결정이 끝날 때까지 다음 task를 제안하지 않는다.
- 현재 spec의 마지막 task가 끝나면 다음 spec으로 넘어가기 전에 `SPEC-xxxx-summary.md`를 작성하고 `registry.json`의 `latest.spec_summary_path`를 갱신한다.
- 각 task는 retrospective와 proposal 처리가 끝난 뒤 커밋한다.
- 현재 spec이 끝나면 spec summary와 proposal 처리까지 마친 뒤 push 여부를 확인한다.
- 혼자 진행하는 개인 프로젝트는 PR 없이 direct push를 기본으로 검토하고, 큰 단위 변경이나 추가 검토가 필요할 때만 PR을 선택적으로 사용한다.
- 커밋 제목은 내부 식별자나 내부 운영 용어보다 실제 변경 결과를 우선 드러내고, 내부 추적 정보는 본문이나 문서에 둔다.
- GitHub Issue는 task와 1:1일 때만 닫는다. 하나의 Issue가 여러 task를 포함하면 닫지 않고 상태만 갱신한다.
- Issue 생성이 깨지거나 같은 task에 잘못된 Issue가 중복 생성되면 canonical Issue 하나만 남기고 나머지는 즉시 정리한다.

## 단계별 흐름

### 1. Orchestrator
- `.agents/active/tasks.md`에서 현재 task를 선택한다.
- task 범위, 비대상, 연결된 `ValidationCriteria`, 완료 조건, Issue 연결 상태를 확인한다.
- task 정의가 모호하면 새 문서를 만들기보다 `.agents/active/tasks.md`를 최소 수정해 명확히 한다.
- 수정 후 3개 서브에이전트 리뷰를 돌리고 모두 `PASS`가 될 때까지 반복한다.

### 2. Tester 1차
- black-box 기대 동작을 먼저 테스트 코드로 작성한다.
- 우선순위는 controller/service integration 수준이다.
- 함께 고정할 항목:
  - 연결된 `ValidationCriteria`
  - 기존 유지 동작
  - AGENTS.md 컨벤션 체크 포인트
- 테스트 초안 작성 후 3개 서브에이전트 리뷰를 돌리고 모두 `PASS`가 될 때까지 반복한다.

### 3. Constructor
- tester가 만든 failing test를 green으로 만드는 production code를 작성한다.
- 필요 시 내부 unit test를 추가한다.
- 구현 diff를 기준으로 3개 서브에이전트 리뷰를 돌리고 모두 `PASS`가 될 때까지 반복한다.

### 4. Tester 2차
- black-box 테스트를 다시 실행한다.
- `AGENTS.md` 컨벤션 위반 여부를 다시 확인한다.
- task 관련 targeted test를 확인한 뒤, 가능하면 저장소 표준 전체 테스트 실행 명령까지 확인한다.
- 전체 테스트 실행을 생략하면 생략 사유를 retrospective에 남긴다.
- 실패하면 `Constructor` 단계로 되돌린다.

### 5. Orchestrator 종료 판정
- `ValidationCriteria`
- 테스트 통과
- Issue 연결 상태
- 필요한 artifact 존재 여부
- `/webhook/github` 흐름, controller orchestration, `agent-runtime` 저장 또는 runtime failure handling을 변경한 task면 실제 애플리케이션 기동, H2 file DB 생성, 실제 앱/H2 대표 검증 확인
- 실제 앱/H2 대표 검증에 사용하는 `delivery_id`는 이전 검증과 겹치지 않는 새 값으로 정하고, 기존 local H2 row와 충돌하지 않게 관리
- 실제 앱/H2 대표 검증이 실패하면 schema 또는 runtime failure로 단정하기 전에 `delivery_id` 재사용 충돌 여부를 먼저 확인
- local H2 file DB를 외부에서 조회하는 실제 앱/H2 대표 검증은 기본적으로 `앱 기동 -> 새로운 delivery_id로 대표 검증 요청 1건 실행 -> HTTP 결과 확인 -> 앱 종료 -> H2 CLI 또는 SQL 조회 도구로 evidence 확인` 순서로 진행
- 애플리케이션 실행 중 H2 조회가 실패하면 schema 또는 runtime failure로 바로 단정하지 말고, 먼저 H2 file lock 여부를 확인
- 다른 순서로 검증하면 그 이유를 retrospective에 남김
- 실제 애플리케이션 검증이 불가하면 retrospective에 사유와 남은 위험 기록
- 위 항목이 모두 맞으면 task 완료로 본다.

### 6. 개발 피드백 루프
- `.agents/outer-loop/README.md` 기준으로 retrospective를 작성한다.
- 필요하면 `AGENTS` 또는 `WORKFLOW` 수정 제안서를 만든다.
- retrospective와 수정 제안서도 3개 서브에이전트 리뷰를 돌리고 모두 `PASS`가 될 때까지 반복한다.
- 그 다음에는 구현을 이어가지 말고, 수집된 메타 데이터와 proposal 후보를 사용자에게 먼저 보고한다.
- 실제 앱/H2 대표 검증을 수행한 retrospective에는 실제 사용한 `delivery_id`와 `execution_key`를 함께 남긴다.
- 사용자가 proposal 처리 방향을 정하면 그때 승인 반영 또는 보류 상태 정리를 수행한다.
- 현재 spec의 마지막 task라면 task retrospective 이후 `SPEC-xxxx-summary.md`까지 작성하고 `registry.json`을 갱신한다.
- outer loop 처리까지 끝난 뒤에만 다음 task로 넘어간다.

## Issue 종료 규칙
- 아래를 모두 만족하면 Issue를 닫는다.
  - 현재 task가 해당 Issue와 1:1로 연결되어 있다.
  - 테스트와 `ValidationCriteria`가 통과했다.
  - retrospective 작성이 끝났다.
  - 필요한 수정 제안서가 있으면 함께 남겼다.
- PR이 있으면 `Closes #번호`를 사용한다.
- PR 없이 직접 진행하는 경우에는 `gh issue close <번호>`로 닫거나, 동등한 비대화형 방식으로 닫는다.
- task가 끝났지만 Issue 범위가 더 크면 닫지 않고 진행 상태만 갱신한다.

## 기본 산출물
- 기준 문서 수정: 필요 시 `.agents/active/tasks.md`
- black-box 테스트 코드
- production code 수정
- retrospective 1개 이상
- spec 종료 시 spec summary 1개
- 필요 시 수정 제안서
