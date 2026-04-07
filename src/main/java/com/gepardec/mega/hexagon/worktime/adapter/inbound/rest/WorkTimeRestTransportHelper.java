package com.gepardec.mega.hexagon.worktime.adapter.inbound.rest;

import com.gepardec.mega.hexagon.worktime.adapter.inbound.rest.error.WorkTimeRequestValidationException;
import jakarta.enterprise.context.ApplicationScoped;

import java.time.YearMonth;
import java.time.format.DateTimeParseException;
import java.util.Objects;

@ApplicationScoped
public class WorkTimeRestTransportHelper {

    public YearMonth parsePayrollMonth(String payrollMonth) {
        try {
            return YearMonth.parse(Objects.requireNonNull(payrollMonth, "payrollMonth must not be null"));
        } catch (DateTimeParseException | NullPointerException exception) {
            throw new WorkTimeRequestValidationException("invalid payrollMonth format: " + payrollMonth, exception);
        }
    }
}
