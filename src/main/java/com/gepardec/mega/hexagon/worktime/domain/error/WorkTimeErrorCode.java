package com.gepardec.mega.hexagon.worktime.domain.error;

import com.gepardec.mega.hexagon.shared.domain.error.ErrorCategory;
import com.gepardec.mega.hexagon.shared.domain.error.ErrorCode;

public enum WorkTimeErrorCode implements ErrorCode {
    USER_NOT_FOUND(ErrorCategory.UNPROCESSABLE);

    private final ErrorCategory category;

    WorkTimeErrorCode(ErrorCategory category) {
        this.category = category;
    }

    @Override
    public String boundedContext() {
        return "WORKTIME";
    }

    @Override
    public ErrorCategory category() {
        return category;
    }
}
