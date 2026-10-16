package com.gepardec.mega.hexagon.user.domain.error;

import com.gepardec.mega.hexagon.shared.domain.error.DomainException;

public class UserException extends DomainException {

    public UserException(UserErrorCode errorCode, String message) {
        super(errorCode, message);
    }

    public UserException(UserErrorCode errorCode, String message, Throwable cause) {
        super(errorCode, message, cause);
    }
}
