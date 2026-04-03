package com.agilerunner.domain.agentruntime;

import lombok.Getter;

@Getter
public class ValidationCriteria {
    private final String taskKey;
    private final String criteriaKey;
    private final CriteriaCategory category;
    private final String description;
    private final CriteriaStatus status;
    private final String evidence;

    private ValidationCriteria(String taskKey,
                               String criteriaKey,
                               CriteriaCategory category,
                               String description,
                               CriteriaStatus status,
                               String evidence) {
        this.taskKey = taskKey;
        this.criteriaKey = criteriaKey;
        this.category = category;
        this.description = description;
        this.status = status;
        this.evidence = evidence;
    }

    public static ValidationCriteria of(String taskKey,
                                        String criteriaKey,
                                        CriteriaCategory category,
                                        String description,
                                        CriteriaStatus status,
                                        String evidence) {
        return new ValidationCriteria(taskKey, criteriaKey, category, description, status, evidence);
    }
}
