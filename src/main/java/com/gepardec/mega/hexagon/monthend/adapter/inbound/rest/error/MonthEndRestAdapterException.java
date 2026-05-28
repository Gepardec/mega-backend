package com.gepardec.mega.hexagon.monthend.adapter.inbound.rest.error;

public abstract class MonthEndRestAdapterException extends RuntimeException {

    protected MonthEndRestAdapterException(String message) {
        super(message);
    }

    protected MonthEndRestAdapterException(String message, Throwable cause) {
        super(message, cause);
    }
}
