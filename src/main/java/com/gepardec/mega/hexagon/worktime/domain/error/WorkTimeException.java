package com.gepardec.mega.hexagon.worktime.domain.error;

import com.gepardec.mega.hexagon.shared.domain.error.DomainException;

public class WorkTimeException extends DomainException {

    public WorkTimeException(WorkTimeErrorCode errorCode, String message) {
        super(errorCode, message);
    }

    public WorkTimeException(WorkTimeErrorCode errorCode, String message, Throwable cause) {
        super(errorCode, message, cause);
    }
}
