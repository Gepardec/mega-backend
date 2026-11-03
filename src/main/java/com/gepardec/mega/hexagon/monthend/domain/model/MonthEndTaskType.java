package com.gepardec.mega.hexagon.monthend.domain.model;

import com.gepardec.mega.hexagon.shared.domain.model.Capacity;

public enum MonthEndTaskType {
    EMPLOYEE_TIME_CHECK(MonthEndCompletionPolicy.INDIVIDUAL_ACTOR),
    LEISTUNGSNACHWEIS(MonthEndCompletionPolicy.ANY_ELIGIBLE_ACTOR),
    PROJECT_LEAD_REVIEW(MonthEndCompletionPolicy.ANY_ELIGIBLE_ACTOR),
    ABRECHNUNG(MonthEndCompletionPolicy.ANY_ELIGIBLE_ACTOR);

    private final MonthEndCompletionPolicy completionPolicy;

    MonthEndTaskType(MonthEndCompletionPolicy completionPolicy) {
        this.completionPolicy = completionPolicy;
    }

    public MonthEndCompletionPolicy completionPolicy() {
        return completionPolicy;
    }

    /**
     * The capacity in which this task type is offered as completable on reads.
     * It never restricts who may complete the task.
     */
    public Capacity dutyOf() {
        return this == EMPLOYEE_TIME_CHECK ? Capacity.EMPLOYEE : Capacity.CONTROLLING;
    }

    public boolean isProjectLeadBulkCompletable() {
        return this == LEISTUNGSNACHWEIS || this == PROJECT_LEAD_REVIEW;
    }
}
