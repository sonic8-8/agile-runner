package com.agilerunner.domain;

public class Review {
    private String repositoryName;
    private int pullRequestNumber;
    private String review;

    private Review(String repositoryName, int pullRequestNumber, String review) {
        this.repositoryName = repositoryName;
        this.pullRequestNumber = pullRequestNumber;
        this.review = review;
    }

    public String getRepositoryName() {
        return repositoryName;
    }

    public int getPullRequestNumber() {
        return pullRequestNumber;
    }

    public String getReview() {
        return review;
    }

    public static Review of(String repositoryName, int pullRequestNumber, String review) {
        return new Review(repositoryName, pullRequestNumber, review);
    }
}
