package com.gepardec.mega.hexagon.monthend.domain.model;

public enum MonthEndTaskType {
    EMPLOYEE_TIME_CHECK(MonthEndCompletionPolicy.INDIVIDUAL_ACTOR),
    LEISTUNGSNACHWEIS(MonthEndCompletionPolicy.INDIVIDUAL_ACTOR),
    PROJECT_LEAD_REVIEW(MonthEndCompletionPolicy.ANY_ELIGIBLE_ACTOR),
    ABRECHNUNG(MonthEndCompletionPolicy.ANY_ELIGIBLE_ACTOR);

    private final MonthEndCompletionPolicy completionPolicy;

    MonthEndTaskType(MonthEndCompletionPolicy completionPolicy) {
        this.completionPolicy = completionPolicy;
    }

    public MonthEndCompletionPolicy completionPolicy() {
        return completionPolicy;
    }
}
