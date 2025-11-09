package com.agilerunner.api.service;

import com.agilerunner.api.service.dto.GitHubCommentResponse;
import com.agilerunner.domain.Review;
import org.springframework.stereotype.Service;

@Service
public class GitHubCommentService {
    public GitHubCommentResponse comment(Review review) {
        return new GitHubCommentResponse();
    }
}
