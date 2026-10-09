package com.gepardec.mega.hexagon.user.domain.error;

import com.gepardec.mega.hexagon.shared.domain.error.ErrorCategory;
import com.gepardec.mega.hexagon.shared.domain.error.ErrorCode;

public enum UserErrorCode implements ErrorCode {
    UNKNOWN_USERS(ErrorCategory.INVALID);

    private final ErrorCategory category;

    UserErrorCode(ErrorCategory category) {
        this.category = category;
    }

    @Override
    public String boundedContext() {
        return "USER";
    }

    @Override
    public ErrorCategory category() {
        return category;
    }
}
