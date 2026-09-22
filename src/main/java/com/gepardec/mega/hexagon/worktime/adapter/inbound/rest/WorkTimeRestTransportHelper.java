package com.gepardec.mega.hexagon.worktime.adapter.inbound.rest;

import io.quarkiverse.httpproblem.validation.Violation;
import jakarta.enterprise.context.ApplicationScoped;

import java.time.YearMonth;
import java.time.format.DateTimeParseException;

import static com.gepardec.mega.hexagon.shared.adapter.inbound.rest.RequestValidationProblems.invalid;

@ApplicationScoped
public class WorkTimeRestTransportHelper {

    public YearMonth parsePayrollMonth(String payrollMonth) {
        if (payrollMonth == null) {
            throw invalid(Violation.In.path, "payrollMonth", "must not be null");
        }
        try {
            return YearMonth.parse(payrollMonth);
        } catch (DateTimeParseException exception) {
            throw invalid(Violation.In.path, "payrollMonth", "invalid payrollMonth format: " + payrollMonth);
        }
    }
}
