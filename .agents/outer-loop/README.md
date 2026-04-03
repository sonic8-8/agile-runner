# 개발 피드백 루프 문서 저장소

## 목적
이 저장소는 task 종료 후 생성되는 회고와 개선 제안을 보관하는 문서 저장소다.
원시 실행 로그는 H2 `agent-runtime`에 두고, 이 디렉토리에는 판단과 개선에 필요한 요약 결과만 남긴다.
디렉토리 이름은 `.agents/outer-loop/`로 고정하고, 문서 개념명은 `개발 피드백 루프`로 통일한다.

## 구조
| 경로 | 역할 | 비고 |
| --- | --- | --- |
| `registry.json` | 최근 회고와 수정 제안서 경로 진입점 | 상태는 저장하지 않음 |
| `retrospectives/SPEC-xxxx/TASK-xxxx-<slug>.md` | task 단위 회고 | retrospective |
| `retrospectives/SPEC-xxxx/SPEC-xxxx-summary.md` | spec 단위 요약 회고 | spec summary |
| `proposals/AGENTS/AGENTS-PROP-xxxx-from-SPEC-xxxx-TASK-xxxx.md` | `AGENTS.md` 수정 제안서 | task 기반 proposal |
| `proposals/WORKFLOW/WORKFLOW-PROP-xxxx-from-SPEC-xxxx-TASK-xxxx.md` | workflow 수정 제안서 | task 기반 proposal |
| `proposals/WORKFLOW/WORKFLOW-PROP-xxxx-from-SPEC-xxxx-SUMMARY.md` | workflow 수정 제안서 | spec summary 기반 proposal |
| `templates/task-retrospective-template.md` | 작업 회고 템플릿 | retrospective 작성 시작점 |
| `templates/spec-summary-template.md` | spec 요약 템플릿 | spec summary 작성 시작점 |
| `templates/agents-proposal-template.md` | `AGENTS.md` 수정 제안서 템플릿 | AGENTS proposal 작성 시작점 |
| `templates/workflow-proposal-template.md` | workflow 수정 제안서 템플릿 | WORKFLOW proposal 작성 시작점 |

## 작성 규칙
- `retrospective`는 task 종료 직후 작성한다.
- `SPEC-xxxx-summary.md`는 해당 spec의 마지막 task retrospective 직후 또는 spec 중단 시점에만 작성한다.
- spec summary 기반 proposal은 `SPEC-xxxx-summary.md` 작성 직후 만들고, 다음 spec 시작 전에 승인 또는 보류 상태를 정리한다.
- 수정 제안서 파일은 이동하지 않는다. 상태는 파일 내부 YAML frontmatter로 관리한다.
- 수정 제안서 상태는 `proposed`, `accepted`, `rejected`, `superseded`만 사용한다.
- rejected 또는 superseded 제안도 삭제하지 않는다.
- retrospective와 수정 제안서에는 H2 ref만 두지 말고 `error_signature`, `test_result_summary`, `snapshot_hashes` 같은 영속 요약 증거도 함께 남긴다.
- `registry.json`은 수정 제안서 상태를 저장하지 않는다. 상태의 단일 진실 원천은 수정 제안서 파일의 YAML frontmatter다.

## registry.json 규격
| 키 | 의미 |
| --- | --- |
| `version` | registry 형식 버전 |
| `latest.spec_id` | 가장 최근 retrospective 또는 spec summary가 속한 spec id |
| `latest.task_id` | 가장 최근 retrospective가 속한 task id |
| `latest.retrospective_path` | 가장 최근 task retrospective 문서 경로 |
| `latest.spec_summary_path` | 가장 최근 spec summary 문서 경로 |
| `proposals` | 수정 제안서 파일 목록 |
| `proposals[].proposal_id` | 수정 제안서 YAML frontmatter의 `proposal_id` |
| `proposals[].path` | 수정 제안서 상대 경로 |

## retrospective 최소 필드
| 필드 | 의미 |
| --- | --- |
| `spec_id` | 연결된 spec id |
| `task_id` | 연결된 task id |
| `github_issue_number` | 연결된 GitHub Issue 번호 |
| `criteria_keys` | 연결된 ValidationCriteria key 목록 |
| `delivery_ids` | 관련 delivery id 목록 |
| `execution_keys` | 관련 execution key 목록 |
| `test_evidence_ref` | 테스트 결과 참조 |
| `diff_ref` | diff 참조 |
| `failure_summary` | 실패 요약 |
| `root_cause` | root cause |
| `agents_check_findings` | AGENTS 점검 결과 |
| `next_task_warnings` | 다음 task 경고사항 |
| `error_signature` | 오류 signature |
| `test_result_summary` | 테스트 결과 요약 |
| `snapshot_hashes` | 근거 snapshot hash 목록 |

## 수정 제안서 최소 메타데이터
| 필드 | 의미 |
| --- | --- |
| `proposal_id` | 수정 제안서 id |
| `status` | 제안 상태 |
| `source_spec` | 출처 spec id |
| `source_tasks` | 출처 task id 목록 |
| `source_retrospectives` | 출처 retrospective 목록 |
| `target_document` | 수정 대상 문서 |
| `target_version` | 수정 대상 문서 버전 |
| `decision_date` | 승인 또는 반려 일시 |
| `applied_commit` | 반영 커밋 |
| `applied_pr` | 반영 PR |

`proposal_id`는 `AGENTS-PROP-xxxx` 또는 `WORKFLOW-PROP-xxxx` 형식을 사용한다.

## 역할 흐름
| 역할 | 입력 | 출력 |
| --- | --- | --- |
| `Orchestrator` | task 종료 신호 | retrospective 작성 트리거 |
| `Tester` | 테스트 결과, AGENTS 체크 | retrospective 입력 |
| `Constructor` | 구현 메모, tradeoff | retrospective 입력 |
| `개발 피드백 루프` | retrospective | 수정 제안서, `registry.json` 갱신 |
