package com.agilerunner.api.service.review;

import com.agilerunner.api.service.GitHubCommentService;
import com.agilerunner.api.service.OpenAiService;
import com.agilerunner.api.service.agentruntime.AgentRuntimeService;
import com.agilerunner.api.service.github.request.GitHubEventServiceRequest;
import com.agilerunner.api.service.github.response.GitHubCommentExecutionResult;
import com.agilerunner.api.service.review.request.ManualRerunServiceRequest;
import com.agilerunner.api.service.review.response.ManualRerunServiceResponse;
import com.agilerunner.domain.Review;
import com.agilerunner.domain.agentruntime.WebhookExecution;
import com.agilerunner.domain.exception.AgileRunnerException;
import com.agilerunner.domain.exception.ErrorCode;
import com.agilerunner.domain.exception.FailureDisposition;
import com.agilerunner.domain.exception.FailureDispositionPolicy;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.Map;

import static com.agilerunner.GitHubEventType.PULL_REQUEST;
import static com.agilerunner.domain.review.RerunExecutionStatus.FAILED;
import static com.agilerunner.domain.review.RerunExecutionStatus.SUCCEEDED;

@Service
@RequiredArgsConstructor
public class ManualRerunService {
    private static final String ACTION_MANUAL_RERUN = "manual_rerun";
    private static final String KEY_ACTION = "action";
    private static final String KEY_REPOSITORY = "repository";
    private static final String KEY_PULL_REQUEST = "pull_request";
    private static final String KEY_FULL_NAME = "full_name";
    private static final String KEY_NUMBER = "number";

    private final OpenAiService openAiService;
    private final GitHubCommentService gitHubCommentService;
    private final AgentRuntimeService agentRuntimeService;
    private final FailureDispositionPolicy failureDispositionPolicy = new FailureDispositionPolicy();

    public ManualRerunServiceResponse rerun(ManualRerunServiceRequest request) {
        WebhookExecution runtimeExecution = agentRuntimeService.startManualRerunExecution(request);
        GitHubEventServiceRequest serviceRequest = buildServiceRequest(request);
        return rerunWithRuntimeEvidence(request, runtimeExecution, serviceRequest);
    }

    private ManualRerunServiceResponse rerunWithRuntimeEvidence(ManualRerunServiceRequest request,
                                                                WebhookExecution runtimeExecution,
                                                                GitHubEventServiceRequest serviceRequest) {
        Review review;

        try {
            review = openAiService.generateReview(serviceRequest);
        } catch (Exception exception) {
            agentRuntimeService.recordFailure(runtimeExecution, AgentRuntimeService.STEP_REVIEW_GENERATED, exception);
            return ManualRerunServiceResponse.of(
                    runtimeExecution.getExecutionKey(),
                    request.getExecutionControlMode(),
                    false,
                    FAILED,
                    getErrorCode(exception),
                    getFailureDisposition(exception)
            );
        }

        agentRuntimeService.recordReviewGenerated(runtimeExecution, review);

        try {
            GitHubCommentExecutionResult executionResult = gitHubCommentService.execute(review, serviceRequest);
            agentRuntimeService.recordExecutionResult(runtimeExecution, executionResult);
            return ManualRerunServiceResponse.of(
                    runtimeExecution.getExecutionKey(),
                    request.getExecutionControlMode(),
                    executionResult.isWritePerformed(),
                    SUCCEEDED,
                    null,
                    null
            );
        } catch (Exception exception) {
            agentRuntimeService.recordFailure(runtimeExecution, AgentRuntimeService.STEP_COMMENT_POSTED, exception);
            return ManualRerunServiceResponse.of(
                    runtimeExecution.getExecutionKey(),
                    request.getExecutionControlMode(),
                    false,
                    FAILED,
                    getErrorCode(exception),
                    getFailureDisposition(exception)
            );
        }
    }

    private ErrorCode getErrorCode(Exception exception) {
        if (exception instanceof AgileRunnerException agileRunnerException) {
            return agileRunnerException.getErrorCode();
        }

        return null;
    }

    private FailureDisposition getFailureDisposition(Exception exception) {
        if (exception instanceof AgileRunnerException agileRunnerException) {
            return failureDispositionPolicy.classify(agileRunnerException);
        }

        return null;
    }

    private GitHubEventServiceRequest buildServiceRequest(ManualRerunServiceRequest request) {
        return GitHubEventServiceRequest.of(
                PULL_REQUEST,
                buildPayload(request),
                request.getInstallationId(),
                request.getExecutionControlMode(),
                request.getSelectedPaths()
        );
    }

    private Map<String, Object> buildPayload(ManualRerunServiceRequest request) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put(KEY_ACTION, ACTION_MANUAL_RERUN);
        payload.put(KEY_REPOSITORY, Map.of(KEY_FULL_NAME, request.getRepositoryName()));
        payload.put(KEY_PULL_REQUEST, Map.of(KEY_NUMBER, request.getPullRequestNumber()));
        return payload;
    }
}
