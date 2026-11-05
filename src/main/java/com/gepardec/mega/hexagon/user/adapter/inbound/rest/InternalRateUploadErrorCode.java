package com.gepardec.mega.hexagon.user.adapter.inbound.rest;

import com.gepardec.mega.hexagon.shared.domain.error.ErrorCategory;
import com.gepardec.mega.hexagon.shared.domain.error.ErrorCode;

/**
 * Codes for upload failures that only the CSV parsing in {@link UserResource} detects. Unknown users are
 * detected by the use case and keep their code in {@code UserErrorCode}.
 */
enum InternalRateUploadErrorCode implements ErrorCode {
    INTERNAL_RATES_EMPTY_FILE,
    INTERNAL_RATES_BAD_FORMAT;

    @Override
    public String boundedContext() {
        return "USER";
    }

    @Override
    public ErrorCategory category() {
        return ErrorCategory.INVALID;
    }
}
