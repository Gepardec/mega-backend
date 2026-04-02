package com.gepardec.mega.hexagon.monthend.domain.error;

public abstract class MonthEndException extends RuntimeException {

    protected MonthEndException(String message) {
        super(message);
    }

    protected MonthEndException(String message, Throwable cause) {
        super(message, cause);
    }
}
