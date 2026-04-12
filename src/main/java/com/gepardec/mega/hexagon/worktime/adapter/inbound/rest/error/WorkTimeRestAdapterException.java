package com.gepardec.mega.hexagon.worktime.adapter.inbound.rest.error;

public abstract class WorkTimeRestAdapterException extends RuntimeException {

    protected WorkTimeRestAdapterException(String message) {
        super(message);
    }

    protected WorkTimeRestAdapterException(String message, Throwable cause) {
        super(message, cause);
    }
}
