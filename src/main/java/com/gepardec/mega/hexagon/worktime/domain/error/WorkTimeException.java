package com.gepardec.mega.hexagon.worktime.domain.error;

public abstract class WorkTimeException extends RuntimeException {

    protected WorkTimeException(String message) {
        super(message);
    }

    protected WorkTimeException(String message, Throwable cause) {
        super(message, cause);
    }
}
