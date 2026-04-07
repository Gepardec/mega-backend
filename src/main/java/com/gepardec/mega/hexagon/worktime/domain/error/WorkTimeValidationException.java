package com.gepardec.mega.hexagon.worktime.domain.error;

public class WorkTimeValidationException extends WorkTimeException {

    public WorkTimeValidationException(String message) {
        super(message);
    }

    public WorkTimeValidationException(String message, Throwable cause) {
        super(message, cause);
    }
}
