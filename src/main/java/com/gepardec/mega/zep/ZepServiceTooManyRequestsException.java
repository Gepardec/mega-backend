package com.gepardec.mega.zep;

public class ZepServiceTooManyRequestsException extends RuntimeException {

    public ZepServiceTooManyRequestsException() {
    }

    public ZepServiceTooManyRequestsException(String message) {
        super(message);
    }

    public ZepServiceTooManyRequestsException(String message, Throwable cause) {
        super(message, cause);
    }
}
