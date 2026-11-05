package com.gepardec.mega.hexagon.shared.domain.error;

public enum SharedErrorCode implements ErrorCode {
    FORBIDDEN(ErrorCategory.FORBIDDEN);

    private final ErrorCategory category;

    SharedErrorCode(ErrorCategory category) {
        this.category = category;
    }

    @Override
    public String boundedContext() {
        return "";
    }

    @Override
    public ErrorCategory category() {
        return category;
    }

    @Override
    public String code() {
        return name();
    }
}
