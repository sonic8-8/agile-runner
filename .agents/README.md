# .agents 안내

## 목적
이 디렉토리는 현재 활성 개발 문서와 개발 피드백 루프 산출물을 한 곳에서 읽기 위한 진입점이다.

## 빠른 구조
| 경로 | 역할 | 형태 | 설명 |
| --- | --- | --- | --- |
| `.agents/prd.md` | 제품 기준선 | 단일 문서 | 프로젝트 목표, 제약, 로드맵, 용어 정의 |
| `.agents/active/spec.md` | 현재 활성 spec | 단일 문서 | 지금 구현 중인 spec과 바로 다음 후속 spec |
| `.agents/active/tasks.md` | 현재 활성 task 집합 | 단일 문서 | 현재 활성 spec을 task로 분해한 실행 계획 |
| `.agents/criteria/*.json` | ValidationCriteria | 다중 파일 | spec별 검증 기준 |
| `.agents/skills/` | 로컬 스킬 | 디렉토리 | 반복 작업 워크플로우를 재사용하기 위한 skill 정의 |
| `.agents/outer-loop/` | 개발 피드백 루프 저장소 | 디렉토리 | 회고, 수정 제안서, registry, 템플릿 |

## 이름 규칙
| 이름 | 의미 | 비고 |
| --- | --- | --- |
| `Spec` | 구현해야 할 동작과 제약을 고정하는 문서 | 현재 저장소에서는 `.agents/active/spec.md` 단일 문서로 유지 |
| `ValidationCriteria` | pass/fail 판단 기준 | spec별 JSON 파일로 저장 |
| `Task` | 실행 가능한 작업 단위 | 현재 저장소에서는 `.agents/active/tasks.md` 단일 문서로 유지 |
| `WebhookExecution` | GitHub webhook 요청 1건의 처리 단위 | 코드에는 일부 이전 이름이 남아 있을 수 있음 |
| `TaskRuntimeState` | task 진행 상태와 재시도 맥락 | `agent-runtime` 저장소 논리 개념 |
| `개발 피드백 루프` | task 종료 후 회고와 개선 제안을 남기는 절차 | 실제 디렉토리 이름은 `.agents/outer-loop/`로 고정 |

## 읽기 순서
1. `.agents/prd.md`
2. `.agents/active/spec.md`
3. `.agents/criteria/*.json`
4. `.agents/active/tasks.md`
5. 필요 시 `.agents/outer-loop/README.md`
