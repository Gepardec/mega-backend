package com.gepardec.mega.hexagon.shared.application.security;

import com.gepardec.mega.hexagon.shared.domain.error.DomainException;
import com.gepardec.mega.hexagon.shared.domain.error.SharedErrorCode;

public class ForbiddenException extends DomainException {

    public ForbiddenException(String message) {
        super(SharedErrorCode.FORBIDDEN, message);
    }

    public ForbiddenException(String message, Throwable cause) {
        super(SharedErrorCode.FORBIDDEN, message, cause);
    }
}
