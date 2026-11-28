package com.gepardec.mega.hexagon.monthend.domain.error;

import com.gepardec.mega.hexagon.shared.domain.error.ErrorCategory;
import com.gepardec.mega.hexagon.shared.domain.error.ErrorCode;

public enum MonthEndErrorCode implements ErrorCode {
    TASK_NOT_FOUND(ErrorCategory.NOT_FOUND),
    CLARIFICATION_NOT_FOUND(ErrorCategory.NOT_FOUND),
    ACTOR_NOT_AUTHORIZED(ErrorCategory.FORBIDDEN),
    CLARIFICATION_CLOSED(ErrorCategory.CONFLICT),
    EMPLOYEE_CONTEXT_NOT_FOUND(ErrorCategory.UNPROCESSABLE),
    EMPLOYEE_NOT_ASSIGNED_TO_PROJECT(ErrorCategory.UNPROCESSABLE),
    PROJECT_CONTEXT_NOT_FOUND(ErrorCategory.UNPROCESSABLE),
    NO_ELIGIBLE_PROJECT_LEAD(ErrorCategory.UNPROCESSABLE),
    TASK_TYPE_NOT_BULK_COMPLETABLE(ErrorCategory.UNPROCESSABLE);

    private final ErrorCategory category;

    MonthEndErrorCode(ErrorCategory category) {
        this.category = category;
    }

    @Override
    public String boundedContext() {
        return "MONTHEND";
    }

    @Override
    public ErrorCategory category() {
        return category;
    }
}
