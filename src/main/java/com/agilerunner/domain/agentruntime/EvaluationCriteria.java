package com.agilerunner.domain.agentruntime;

import lombok.Getter;

@Getter
public class EvaluationCriteria {
    private final String taskKey;
    private final String criteriaKey;
    private final CriteriaCategory category;
    private final String description;
    private final CriteriaStatus status;
    private final String evidence;

    private EvaluationCriteria(String taskKey,
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

    public static EvaluationCriteria of(String taskKey,
                                        String criteriaKey,
                                        CriteriaCategory category,
                                        String description,
                                        CriteriaStatus status,
                                        String evidence) {
        return new EvaluationCriteria(taskKey, criteriaKey, category, description, status, evidence);
    }
}
