package com.agilerunner.api.service.review;

import com.agilerunner.api.service.GitHubCommentService;
import com.agilerunner.api.service.OpenAiService;
import com.agilerunner.api.service.github.request.GitHubEventServiceRequest;
import com.agilerunner.api.service.github.response.GitHubCommentExecutionResult;
import com.agilerunner.api.service.review.request.ManualRerunServiceRequest;
import com.agilerunner.api.service.review.response.ManualRerunServiceResponse;
import com.agilerunner.domain.Review;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.Map;

import static com.agilerunner.GitHubEventType.PULL_REQUEST;

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

    public ManualRerunServiceResponse rerun(ManualRerunServiceRequest request) {
        GitHubEventServiceRequest serviceRequest = buildServiceRequest(request);
        Review review = openAiService.generateReview(serviceRequest);
        GitHubCommentExecutionResult executionResult = gitHubCommentService.execute(review, serviceRequest);

        return ManualRerunServiceResponse.awaitingRuntimeEvidence(
                request.getExecutionControlMode(),
                executionResult.isWritePerformed()
        );
    }

    private GitHubEventServiceRequest buildServiceRequest(ManualRerunServiceRequest request) {
        return GitHubEventServiceRequest.of(
                PULL_REQUEST,
                buildPayload(request),
                request.getInstallationId(),
                request.getExecutionControlMode()
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
