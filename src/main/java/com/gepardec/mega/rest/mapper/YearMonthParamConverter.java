package com.gepardec.mega.rest.mapper;

import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.ext.ParamConverter;

import java.time.YearMonth;
import java.time.format.DateTimeParseException;

public class YearMonthParamConverter implements ParamConverter<YearMonth> {
    @Override
    public YearMonth fromString(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return YearMonth.parse(value);
        } catch (DateTimeParseException e) {
            throw new BadRequestException("Invalid year-month '" + value + "', expected yyyy-MM");
        }
    }

    @Override
    public String toString(YearMonth value) {
        if (value == null) {
            return null;
        }
        return value.toString();
    }
}
