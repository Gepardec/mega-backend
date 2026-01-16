package com.gepardec.mega.domain.model.monthlyreport;

public enum TimeWarningType implements WarningType {
    OUTSIDE_CORE_WORKING_TIME,
    TIME_OVERLAP,
    NO_TIME_ENTRY,
    EMPTY_ENTRY_LIST,
    HOLIDAY,
    WEEKEND,
    WRONG_DOCTOR_APPOINTMENT,
    EXCESS_WORKING_TIME_PRESENT,
    MISSING_REST_TIME,
    MISSING_BREAK_TIME;

    @Override
    public String warningType() {
        return "time";
    }
}
