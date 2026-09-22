package com.gepardec.mega.hexagon.monthend.domain.error;

import com.gepardec.mega.hexagon.shared.domain.error.ErrorCategory;
import com.gepardec.mega.hexagon.shared.domain.error.ErrorCode;

public enum MonthEndErrorCode implements ErrorCode {
    TASK_NOT_FOUND(ErrorCategory.NOT_FOUND),
    CLARIFICATION_NOT_FOUND(ErrorCategory.NOT_FOUND),
    ACTOR_NOT_AUTHORIZED(ErrorCategory.FORBIDDEN),
    CLARIFICATION_CLOSED(ErrorCategory.INVALID),
    EMPLOYEE_CONTEXT_NOT_FOUND(ErrorCategory.INVALID),
    EMPLOYEE_NOT_ASSIGNED_TO_PROJECT(ErrorCategory.INVALID),
    PROJECT_CONTEXT_NOT_FOUND(ErrorCategory.INVALID),
    VALIDATION_FAILED(ErrorCategory.INVALID);

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
