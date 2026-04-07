package com.agilerunner.api.service.github.response;

import com.agilerunner.api.service.dto.GitHubCommentResponse;
import com.agilerunner.domain.executioncontrol.ExecutionControlMode;
import com.agilerunner.domain.executioncontrol.GitHubWriteSkipReason;

public class GitHubCommentExecutionResult {
    private final ExecutionControlMode executionControlMode;
    private final boolean writePerformed;
    private final GitHubWriteSkipReason writeSkipReason;
    private final int preparedInlineCommentCount;
    private final GitHubCommentResponse gitHubCommentResponse;

    private GitHubCommentExecutionResult(ExecutionControlMode executionControlMode,
                                         boolean writePerformed,
                                         GitHubWriteSkipReason writeSkipReason,
                                         int preparedInlineCommentCount,
                                         GitHubCommentResponse gitHubCommentResponse) {
        this.executionControlMode = executionControlMode;
        this.writePerformed = writePerformed;
        this.writeSkipReason = writeSkipReason;
        this.preparedInlineCommentCount = preparedInlineCommentCount;
        this.gitHubCommentResponse = gitHubCommentResponse;
    }

    public static GitHubCommentExecutionResult written(ExecutionControlMode executionControlMode,
                                                       int preparedInlineCommentCount,
                                                       GitHubCommentResponse gitHubCommentResponse) {
        return new GitHubCommentExecutionResult(
                executionControlMode,
                true,
                null,
                preparedInlineCommentCount,
                gitHubCommentResponse
        );
    }

    public static GitHubCommentExecutionResult skipped(ExecutionControlMode executionControlMode,
                                                       GitHubWriteSkipReason writeSkipReason,
                                                       int preparedInlineCommentCount) {
        return new GitHubCommentExecutionResult(
                executionControlMode,
                false,
                writeSkipReason,
                preparedInlineCommentCount,
                null
        );
    }

    public ExecutionControlMode getExecutionControlMode() {
        return executionControlMode;
    }

    public boolean isWritePerformed() {
        return writePerformed;
    }

    public GitHubWriteSkipReason getWriteSkipReason() {
        return writeSkipReason;
    }

    public int getPreparedInlineCommentCount() {
        return preparedInlineCommentCount;
    }

    public GitHubCommentResponse getGitHubCommentResponse() {
        return gitHubCommentResponse;
    }

    public GitHubCommentResponse requireGitHubCommentResponse() {
        if (gitHubCommentResponse != null) {
            return gitHubCommentResponse;
        }

        throw new IllegalStateException("write를 수행하지 않은 실행에는 GitHubCommentResponse가 없습니다.");
    }
}
