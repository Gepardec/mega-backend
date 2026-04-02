package com.gepardec.mega.hexagon.monthend.domain.error;

public class MonthEndValidationException extends MonthEndException {

    public MonthEndValidationException(String message) {
        super(message);
    }

    public MonthEndValidationException(String message, Throwable cause) {
        super(message, cause);
    }
}
