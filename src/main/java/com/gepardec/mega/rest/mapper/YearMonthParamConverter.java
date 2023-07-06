package com.gepardec.mega.rest.mapper;

import jakarta.ws.rs.ext.ParamConverter;

import java.time.YearMonth;

public class YearMonthParamConverter implements ParamConverter<YearMonth> {
    @Override
    public YearMonth fromString(String value) {
        if (value == null) {
            return null;
        }
        return YearMonth.parse(value);
    }

    @Override
    public String toString(YearMonth value) {
        if (value == null) {
            return null;
        }
        return value.toString();
    }
}
