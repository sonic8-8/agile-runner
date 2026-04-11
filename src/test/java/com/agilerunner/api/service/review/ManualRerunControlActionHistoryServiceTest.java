package com.agilerunner.api.service.review;

import com.agilerunner.api.service.review.request.ManualRerunControlActionHistoryServiceRequest;
import com.agilerunner.api.service.review.response.ManualRerunControlActionHistoryServiceResponse;
import com.agilerunner.client.agentruntime.AgentRuntimeRepository;
import com.agilerunner.domain.agentruntime.ExecutionStartType;
import com.agilerunner.domain.agentruntime.WebhookExecution;
import com.agilerunner.domain.agentruntime.WebhookExecutionStatus;
import com.agilerunner.domain.exception.ManualRerunQueryNotFoundException;
import com.agilerunner.domain.review.ManualRerunControlAction;
import com.agilerunner.domain.review.ManualRerunControlActionAudit;
import com.agilerunner.domain.review.ManualRerunControlActionHistorySortDirection;
import com.agilerunner.domain.review.ManualRerunControlActionStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ManualRerunControlActionHistoryServiceTest {

    @DisplayName("관리자 액션 이력 조회 서비스는 audit timeline을 시간 순서대로 응답에 연결한다.")
    @Test
    void find_returnsHistoryResponseFromAuditTimeline() {
        // given
        AgentRuntimeRepository repository = mock(AgentRuntimeRepository.class);
        ManualRerunControlActionHistoryService service = new ManualRerunControlActionHistoryService(repository);
        WebhookExecution webhookExecution = WebhookExecution.start(
                "EXECUTION:MANUAL_RERUN:history-1",
                "PR_REVIEW:owner/repo#12",
                "MANUAL_RERUN_DELIVERY:history-1",
                "owner/repo",
                12,
                "PULL_REQUEST",
                "manual_rerun",
                LocalDateTime.of(2026, 4, 10, 12, 0)
        ).withExecutionStartType(ExecutionStartType.MANUAL_RERUN).complete(
                WebhookExecutionStatus.FAILED,
                "failed",
                null,
                LocalDateTime.of(2026, 4, 10, 12, 1)
        );
        when(repository.findWebhookExecution("EXECUTION:MANUAL_RERUN:history-1"))
                .thenReturn(Optional.of(webhookExecution));
        when(repository.findManualRerunControlActionAudits(
                "EXECUTION:MANUAL_RERUN:history-1",
                null,
                null,
                null,
                null,
                null,
                null,
                null
        ))
                .thenReturn(List.of(
                        ManualRerunControlActionAudit.of(
                                "EXECUTION:MANUAL_RERUN:history-1",
                                ManualRerunControlAction.ACKNOWLEDGE,
                                ManualRerunControlActionStatus.APPLIED,
                                "운영자 확인 완료",
                                LocalDateTime.of(2026, 4, 10, 12, 2)
                        ),
                        ManualRerunControlActionAudit.of(
                                "EXECUTION:MANUAL_RERUN:history-1",
                                ManualRerunControlAction.UNACKNOWLEDGE,
                                ManualRerunControlActionStatus.APPLIED,
                                "운영자 확인 취소",
                                LocalDateTime.of(2026, 4, 10, 12, 3)
                        )
                ));

        // when
        ManualRerunControlActionHistoryServiceResponse response = service.find(
                ManualRerunControlActionHistoryServiceRequest.of("EXECUTION:MANUAL_RERUN:history-1")
        );

        // then
        assertThat(response.getExecutionKey()).isEqualTo("EXECUTION:MANUAL_RERUN:history-1");
        assertThat(response.getActions()).hasSize(2);
        assertThat(response.getActions().get(0).getAction()).isEqualTo(ManualRerunControlAction.ACKNOWLEDGE);
        assertThat(response.getActions().get(0).getActionStatus()).isEqualTo(ManualRerunControlActionStatus.APPLIED);
        assertThat(response.getActions().get(0).getNote()).isEqualTo("운영자 확인 완료");
        assertThat(response.getActions().get(0).getAppliedAt()).isEqualTo(LocalDateTime.of(2026, 4, 10, 12, 2));
        assertThat(response.getActions().get(1).getAction()).isEqualTo(ManualRerunControlAction.UNACKNOWLEDGE);
        assertThat(response.getActions().get(1).getActionStatus()).isEqualTo(ManualRerunControlActionStatus.APPLIED);
        assertThat(response.getActions().get(1).getNote()).isEqualTo("운영자 확인 취소");
        assertThat(response.getActions().get(1).getAppliedAt()).isEqualTo(LocalDateTime.of(2026, 4, 10, 12, 3));
    }

    @DisplayName("관리자 액션 이력 조회 서비스는 action, actionStatus 필터를 audit selection에 전달한다.")
    @Test
    void find_usesFilteredAuditSelection() {
        // given
        AgentRuntimeRepository repository = mock(AgentRuntimeRepository.class);
        ManualRerunControlActionHistoryService service = new ManualRerunControlActionHistoryService(repository);
        WebhookExecution webhookExecution = WebhookExecution.start(
                "EXECUTION:MANUAL_RERUN:history-filtered",
                "PR_REVIEW:owner/repo#12",
                "MANUAL_RERUN_DELIVERY:history-filtered",
                "owner/repo",
                12,
                "PULL_REQUEST",
                "manual_rerun",
                LocalDateTime.of(2026, 4, 10, 12, 0)
        ).withExecutionStartType(ExecutionStartType.MANUAL_RERUN).complete(
                WebhookExecutionStatus.FAILED,
                "failed",
                null,
                LocalDateTime.of(2026, 4, 10, 12, 1)
        );
        when(repository.findWebhookExecution("EXECUTION:MANUAL_RERUN:history-filtered"))
                .thenReturn(Optional.of(webhookExecution));
        when(repository.findManualRerunControlActionAudits(
                "EXECUTION:MANUAL_RERUN:history-filtered",
                ManualRerunControlAction.ACKNOWLEDGE,
                ManualRerunControlActionStatus.APPLIED,
                null,
                null,
                null,
                null,
                null
        )).thenReturn(List.of(
                ManualRerunControlActionAudit.of(
                        "EXECUTION:MANUAL_RERUN:history-filtered",
                        ManualRerunControlAction.ACKNOWLEDGE,
                        ManualRerunControlActionStatus.APPLIED,
                        "운영자 확인 완료",
                        LocalDateTime.of(2026, 4, 10, 12, 2)
                )
        ));

        // when
        ManualRerunControlActionHistoryServiceResponse response = service.find(
                ManualRerunControlActionHistoryServiceRequest.of(
                        "EXECUTION:MANUAL_RERUN:history-filtered",
                        ManualRerunControlAction.ACKNOWLEDGE,
                        ManualRerunControlActionStatus.APPLIED
                )
        );

        // then
        assertThat(response.getExecutionKey()).isEqualTo("EXECUTION:MANUAL_RERUN:history-filtered");
        assertThat(response.getActions()).hasSize(1);
        assertThat(response.getActions().getFirst().getAction()).isEqualTo(ManualRerunControlAction.ACKNOWLEDGE);
        verify(repository).findManualRerunControlActionAudits(
                "EXECUTION:MANUAL_RERUN:history-filtered",
                ManualRerunControlAction.ACKNOWLEDGE,
                ManualRerunControlActionStatus.APPLIED,
                null,
                null,
                null,
                null,
                null
        );
    }

    @DisplayName("관리자 액션 이력 조회 서비스는 기간 필터를 audit selection에 함께 전달한다.")
    @Test
    void find_usesDateRangeFilteredAuditSelection() {
        // given
        AgentRuntimeRepository repository = mock(AgentRuntimeRepository.class);
        ManualRerunControlActionHistoryService service = new ManualRerunControlActionHistoryService(repository);
        LocalDateTime appliedAtFrom = LocalDateTime.of(2026, 4, 11, 10, 1);
        LocalDateTime appliedAtTo = LocalDateTime.of(2026, 4, 11, 10, 2);
        WebhookExecution webhookExecution = WebhookExecution.start(
                "EXECUTION:MANUAL_RERUN:history-date-filtered",
                "PR_REVIEW:owner/repo#12",
                "MANUAL_RERUN_DELIVERY:history-date-filtered",
                "owner/repo",
                12,
                "PULL_REQUEST",
                "manual_rerun",
                LocalDateTime.of(2026, 4, 11, 10, 0)
        ).withExecutionStartType(ExecutionStartType.MANUAL_RERUN).complete(
                WebhookExecutionStatus.FAILED,
                "failed",
                null,
                LocalDateTime.of(2026, 4, 11, 10, 1)
        );
        when(repository.findWebhookExecution("EXECUTION:MANUAL_RERUN:history-date-filtered"))
                .thenReturn(Optional.of(webhookExecution));
        when(repository.findManualRerunControlActionAudits(
                "EXECUTION:MANUAL_RERUN:history-date-filtered",
                ManualRerunControlAction.ACKNOWLEDGE,
                ManualRerunControlActionStatus.APPLIED,
                appliedAtFrom,
                appliedAtTo,
                null,
                null,
                null
        )).thenReturn(List.of(
                ManualRerunControlActionAudit.of(
                        "EXECUTION:MANUAL_RERUN:history-date-filtered",
                        ManualRerunControlAction.ACKNOWLEDGE,
                        ManualRerunControlActionStatus.APPLIED,
                        "운영자 확인 완료",
                        LocalDateTime.of(2026, 4, 11, 10, 2)
                )
        ));

        // when
        ManualRerunControlActionHistoryServiceResponse response = service.find(
                ManualRerunControlActionHistoryServiceRequest.of(
                        "EXECUTION:MANUAL_RERUN:history-date-filtered",
                        ManualRerunControlAction.ACKNOWLEDGE,
                        ManualRerunControlActionStatus.APPLIED,
                        appliedAtFrom,
                        appliedAtTo
                )
        );

        // then
        assertThat(response.getExecutionKey()).isEqualTo("EXECUTION:MANUAL_RERUN:history-date-filtered");
        assertThat(response.getActions()).hasSize(1);
        assertThat(response.getActions().getFirst().getAction()).isEqualTo(ManualRerunControlAction.ACKNOWLEDGE);
        verify(repository).findManualRerunControlActionAudits(
                "EXECUTION:MANUAL_RERUN:history-date-filtered",
                ManualRerunControlAction.ACKNOWLEDGE,
                ManualRerunControlActionStatus.APPLIED,
                appliedAtFrom,
                appliedAtTo,
                null,
                null,
                null
        );
    }

    @DisplayName("관리자 액션 이력 조회 서비스는 execution이 존재하지만 기간 필터 결과가 없으면 빈 timeline을 반환한다.")
    @Test
    void find_returnsEmptyActionsWhenDateFilterMatchesNoAuditRows() {
        // given
        AgentRuntimeRepository repository = mock(AgentRuntimeRepository.class);
        ManualRerunControlActionHistoryService service = new ManualRerunControlActionHistoryService(repository);
        LocalDateTime appliedAtFrom = LocalDateTime.of(2026, 4, 11, 10, 10);
        LocalDateTime appliedAtTo = LocalDateTime.of(2026, 4, 11, 10, 11);
        WebhookExecution webhookExecution = WebhookExecution.start(
                "EXECUTION:MANUAL_RERUN:history-date-empty",
                "PR_REVIEW:owner/repo#12",
                "MANUAL_RERUN_DELIVERY:history-date-empty",
                "owner/repo",
                12,
                "PULL_REQUEST",
                "manual_rerun",
                LocalDateTime.of(2026, 4, 11, 10, 0)
        ).withExecutionStartType(ExecutionStartType.MANUAL_RERUN);
        when(repository.findWebhookExecution("EXECUTION:MANUAL_RERUN:history-date-empty"))
                .thenReturn(Optional.of(webhookExecution));
        when(repository.findManualRerunControlActionAudits(
                "EXECUTION:MANUAL_RERUN:history-date-empty",
                null,
                null,
                appliedAtFrom,
                appliedAtTo,
                null,
                null,
                null
        )).thenReturn(List.of());

        // when
        ManualRerunControlActionHistoryServiceResponse response = service.find(
                ManualRerunControlActionHistoryServiceRequest.of(
                        "EXECUTION:MANUAL_RERUN:history-date-empty",
                        null,
                        null,
                        appliedAtFrom,
                        appliedAtTo
                )
        );

        // then
        assertThat(response.getExecutionKey()).isEqualTo("EXECUTION:MANUAL_RERUN:history-date-empty");
        assertThat(response.getActions()).isEmpty();
        verify(repository).findManualRerunControlActionAudits(
                "EXECUTION:MANUAL_RERUN:history-date-empty",
                null,
                null,
                appliedAtFrom,
                appliedAtTo,
                null,
                null,
                null
        );
    }

    @DisplayName("관리자 액션 이력 조회 서비스는 정렬과 페이지 기준을 audit selection에 함께 전달한다.")
    @Test
    void find_usesOrderAndPageSelection() {
        // given
        AgentRuntimeRepository repository = mock(AgentRuntimeRepository.class);
        ManualRerunControlActionHistoryService service = new ManualRerunControlActionHistoryService(repository);
        LocalDateTime cursorAppliedAt = LocalDateTime.of(2026, 4, 12, 10, 5);
        WebhookExecution webhookExecution = WebhookExecution.start(
                "EXECUTION:MANUAL_RERUN:history-page-filtered",
                "PR_REVIEW:owner/repo#12",
                "MANUAL_RERUN_DELIVERY:history-page-filtered",
                "owner/repo",
                12,
                "PULL_REQUEST",
                "manual_rerun",
                LocalDateTime.of(2026, 4, 12, 10, 0)
        ).withExecutionStartType(ExecutionStartType.MANUAL_RERUN);
        when(repository.findWebhookExecution("EXECUTION:MANUAL_RERUN:history-page-filtered"))
                .thenReturn(Optional.of(webhookExecution));
        when(repository.findManualRerunControlActionAudits(
                "EXECUTION:MANUAL_RERUN:history-page-filtered",
                null,
                null,
                null,
                null,
                ManualRerunControlActionHistorySortDirection.DESC,
                2,
                cursorAppliedAt
        )).thenReturn(List.of(
                ManualRerunControlActionAudit.of(
                        "EXECUTION:MANUAL_RERUN:history-page-filtered",
                        ManualRerunControlAction.UNACKNOWLEDGE,
                        ManualRerunControlActionStatus.APPLIED,
                        "확인 취소",
                        LocalDateTime.of(2026, 4, 12, 10, 4)
                )
        ));

        // when
        ManualRerunControlActionHistoryServiceResponse response = service.find(
                ManualRerunControlActionHistoryServiceRequest.of(
                        "EXECUTION:MANUAL_RERUN:history-page-filtered",
                        null,
                        null,
                        null,
                        null,
                        ManualRerunControlActionHistorySortDirection.DESC,
                        2,
                        cursorAppliedAt
                )
        );

        // then
        assertThat(response.getExecutionKey()).isEqualTo("EXECUTION:MANUAL_RERUN:history-page-filtered");
        assertThat(response.getActions()).hasSize(1);
        assertThat(response.getActions().getFirst().getAction()).isEqualTo(ManualRerunControlAction.UNACKNOWLEDGE);
        verify(repository).findManualRerunControlActionAudits(
                "EXECUTION:MANUAL_RERUN:history-page-filtered",
                null,
                null,
                null,
                null,
                ManualRerunControlActionHistorySortDirection.DESC,
                2,
                cursorAppliedAt
        );
    }

    @DisplayName("관리자 액션 이력 조회 서비스는 execution은 있지만 page window 결과가 없으면 빈 timeline을 반환한다.")
    @Test
    void find_returnsEmptyActionsWhenPageWindowMatchesNoAuditRows() {
        // given
        AgentRuntimeRepository repository = mock(AgentRuntimeRepository.class);
        ManualRerunControlActionHistoryService service = new ManualRerunControlActionHistoryService(repository);
        LocalDateTime cursorAppliedAt = LocalDateTime.of(2026, 4, 12, 11, 0);
        WebhookExecution webhookExecution = WebhookExecution.start(
                "EXECUTION:MANUAL_RERUN:history-page-empty",
                "PR_REVIEW:owner/repo#12",
                "MANUAL_RERUN_DELIVERY:history-page-empty",
                "owner/repo",
                12,
                "PULL_REQUEST",
                "manual_rerun",
                LocalDateTime.of(2026, 4, 12, 10, 0)
        ).withExecutionStartType(ExecutionStartType.MANUAL_RERUN);
        when(repository.findWebhookExecution("EXECUTION:MANUAL_RERUN:history-page-empty"))
                .thenReturn(Optional.of(webhookExecution));
        when(repository.findManualRerunControlActionAudits(
                "EXECUTION:MANUAL_RERUN:history-page-empty",
                null,
                null,
                null,
                null,
                ManualRerunControlActionHistorySortDirection.DESC,
                5,
                cursorAppliedAt
        )).thenReturn(List.of());

        // when
        ManualRerunControlActionHistoryServiceResponse response = service.find(
                ManualRerunControlActionHistoryServiceRequest.of(
                        "EXECUTION:MANUAL_RERUN:history-page-empty",
                        null,
                        null,
                        null,
                        null,
                        ManualRerunControlActionHistorySortDirection.DESC,
                        5,
                        cursorAppliedAt
                )
        );

        // then
        assertThat(response.getExecutionKey()).isEqualTo("EXECUTION:MANUAL_RERUN:history-page-empty");
        assertThat(response.getActions()).isEmpty();
        verify(repository).findManualRerunControlActionAudits(
                "EXECUTION:MANUAL_RERUN:history-page-empty",
                null,
                null,
                null,
                null,
                ManualRerunControlActionHistorySortDirection.DESC,
                5,
                cursorAppliedAt
        );
    }

    @DisplayName("반복 액션 이력 조회 서비스는 ACKNOWLEDGE, UNACKNOWLEDGE, ACKNOWLEDGE timeline을 순서대로 반환한다.")
    @Test
    void find_returnsRepeatedActionTimelineInOrder() {
        // given
        AgentRuntimeRepository repository = mock(AgentRuntimeRepository.class);
        ManualRerunControlActionHistoryService service = new ManualRerunControlActionHistoryService(repository);
        WebhookExecution webhookExecution = WebhookExecution.start(
                "EXECUTION:MANUAL_RERUN:history-repeat",
                "PR_REVIEW:owner/repo#12",
                "MANUAL_RERUN_DELIVERY:history-repeat",
                "owner/repo",
                12,
                "PULL_REQUEST",
                "manual_rerun",
                LocalDateTime.of(2026, 4, 10, 12, 0)
        ).withExecutionStartType(ExecutionStartType.MANUAL_RERUN).complete(
                WebhookExecutionStatus.FAILED,
                "failed",
                null,
                LocalDateTime.of(2026, 4, 10, 12, 1)
        );
        when(repository.findWebhookExecution("EXECUTION:MANUAL_RERUN:history-repeat"))
                .thenReturn(Optional.of(webhookExecution));
        when(repository.findManualRerunControlActionAudits(
                "EXECUTION:MANUAL_RERUN:history-repeat",
                null,
                null,
                null,
                null,
                null,
                null,
                null
        ))
                .thenReturn(List.of(
                        ManualRerunControlActionAudit.of(
                                "EXECUTION:MANUAL_RERUN:history-repeat",
                                ManualRerunControlAction.ACKNOWLEDGE,
                                ManualRerunControlActionStatus.APPLIED,
                                "첫 확인 완료",
                                LocalDateTime.of(2026, 4, 10, 12, 2)
                        ),
                        ManualRerunControlActionAudit.of(
                                "EXECUTION:MANUAL_RERUN:history-repeat",
                                ManualRerunControlAction.UNACKNOWLEDGE,
                                ManualRerunControlActionStatus.APPLIED,
                                "확인 취소",
                                LocalDateTime.of(2026, 4, 10, 12, 3)
                        ),
                        ManualRerunControlActionAudit.of(
                                "EXECUTION:MANUAL_RERUN:history-repeat",
                                ManualRerunControlAction.ACKNOWLEDGE,
                                ManualRerunControlActionStatus.APPLIED,
                                "재확인 완료",
                                LocalDateTime.of(2026, 4, 10, 12, 4)
                        )
                ));

        // when
        ManualRerunControlActionHistoryServiceResponse response = service.find(
                ManualRerunControlActionHistoryServiceRequest.of("EXECUTION:MANUAL_RERUN:history-repeat")
        );

        // then
        assertThat(response.getActions()).hasSize(3);
        assertThat(response.getActions().get(0).getAction()).isEqualTo(ManualRerunControlAction.ACKNOWLEDGE);
        assertThat(response.getActions().get(0).getNote()).isEqualTo("첫 확인 완료");
        assertThat(response.getActions().get(1).getAction()).isEqualTo(ManualRerunControlAction.UNACKNOWLEDGE);
        assertThat(response.getActions().get(1).getNote()).isEqualTo("확인 취소");
        assertThat(response.getActions().get(2).getAction()).isEqualTo(ManualRerunControlAction.ACKNOWLEDGE);
        assertThat(response.getActions().get(2).getNote()).isEqualTo("재확인 완료");
        assertThat(response.getActions().get(2).getAppliedAt()).isEqualTo(LocalDateTime.of(2026, 4, 10, 12, 4));
    }

    @DisplayName("관리자 액션 이력은 execution이 존재하지만 audit row가 없으면 빈 actions로 반환한다.")
    @Test
    void find_returnsEmptyActionsWhenAuditDoesNotExist() {
        // given
        AgentRuntimeRepository repository = mock(AgentRuntimeRepository.class);
        ManualRerunControlActionHistoryService service = new ManualRerunControlActionHistoryService(repository);
        WebhookExecution webhookExecution = WebhookExecution.start(
                "EXECUTION:MANUAL_RERUN:history-empty",
                "PR_REVIEW:owner/repo#14",
                "MANUAL_RERUN_DELIVERY:history-empty",
                "owner/repo",
                14,
                "PULL_REQUEST",
                "manual_rerun",
                LocalDateTime.of(2026, 4, 10, 12, 10)
        ).withExecutionStartType(ExecutionStartType.MANUAL_RERUN);
        when(repository.findWebhookExecution("EXECUTION:MANUAL_RERUN:history-empty"))
                .thenReturn(Optional.of(webhookExecution));
        when(repository.findManualRerunControlActionAudits(
                "EXECUTION:MANUAL_RERUN:history-empty",
                null,
                null,
                null,
                null,
                null,
                null,
                null
        ))
                .thenReturn(List.of());

        // when
        ManualRerunControlActionHistoryServiceResponse response = service.find(
                ManualRerunControlActionHistoryServiceRequest.of("EXECUTION:MANUAL_RERUN:history-empty")
        );

        // then
        assertThat(response.getExecutionKey()).isEqualTo("EXECUTION:MANUAL_RERUN:history-empty");
        assertThat(response.getActions()).isEmpty();
    }

    @DisplayName("관리자 액션 이력은 execution이 없거나 manual rerun이 아니면 not found 예외를 던진다.")
    @Test
    void find_throwsNotFoundWhenExecutionIsMissingOrNotManualRerun() {
        // given
        AgentRuntimeRepository repository = mock(AgentRuntimeRepository.class);
        ManualRerunControlActionHistoryService service = new ManualRerunControlActionHistoryService(repository);
        when(repository.findWebhookExecution("EXECUTION:MANUAL_RERUN:missing")).thenReturn(Optional.empty());

        WebhookExecution webhookExecution = WebhookExecution.start(
                "EXECUTION:delivery-99",
                "PR_REVIEW:owner/repo#12",
                "delivery-99",
                "owner/repo",
                12,
                "PULL_REQUEST",
                "opened",
                LocalDateTime.of(2026, 4, 10, 12, 30)
        ).withExecutionStartType(ExecutionStartType.WEBHOOK);
        when(repository.findWebhookExecution("EXECUTION:delivery-99")).thenReturn(Optional.of(webhookExecution));

        // when & then
        assertThatThrownBy(() -> service.find(ManualRerunControlActionHistoryServiceRequest.of("EXECUTION:MANUAL_RERUN:missing")))
                .isInstanceOf(ManualRerunQueryNotFoundException.class)
                .hasMessage("재실행 결과를 찾을 수 없습니다.");

        assertThatThrownBy(() -> service.find(ManualRerunControlActionHistoryServiceRequest.of("EXECUTION:delivery-99")))
                .isInstanceOf(ManualRerunQueryNotFoundException.class)
                .hasMessage("재실행 결과를 찾을 수 없습니다.");
    }
}
