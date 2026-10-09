package com.gepardec.mega.hexagon.monthend.domain.error;

import com.gepardec.mega.hexagon.shared.domain.error.DomainException;

public class MonthEndException extends DomainException {

    public MonthEndException(MonthEndErrorCode errorCode, String message) {
        super(errorCode, message);
    }

    public MonthEndException(MonthEndErrorCode errorCode, String message, Throwable cause) {
        super(errorCode, message, cause);
    }
}
