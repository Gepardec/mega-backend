package com.gepardec.mega.hexagon.project.domain.error;

import com.gepardec.mega.hexagon.shared.domain.error.ErrorCategory;
import com.gepardec.mega.hexagon.shared.domain.error.ErrorCode;

public enum ProjectErrorCode implements ErrorCode {
    NOT_FOUND(ErrorCategory.NOT_FOUND),
    LEISTUNGSNACHWEIS_NOT_APPLICABLE(ErrorCategory.INVALID),
    ACTOR_NOT_LEAD(ErrorCategory.FORBIDDEN);

    private final ErrorCategory category;

    ProjectErrorCode(ErrorCategory category) {
        this.category = category;
    }

    @Override
    public String boundedContext() {
        return "PROJECT";
    }

    @Override
    public ErrorCategory category() {
        return category;
    }
}
