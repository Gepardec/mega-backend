package com.gepardec.mega.domain.model.monthlyreport;

import java.util.function.Function;

public enum MappedTimeWarningTypes {
    EXCESSWORK(w -> (w.getExcessWorkTime().toString())),
    MISSINGREST(w -> (w.getMissingRestTime().toString())),
    MISSINGBREAK(w -> (w.getMissingBreakTime().toString())),
    DOCTOR(w -> ""),
    NO_TIME_ENTRY(w -> ""),
    OUTSIDE_CORE_WORKING_TIME(w -> ""),
    TIME_OVERLAP(w -> ""),
    EMPTY_ENTRY_LIST(w -> ""),
    HOLIDAY(w -> ""),
    WEEKEND(w -> "");

    private Function<TimeWarning, String> resolver;

    MappedTimeWarningTypes(Function<TimeWarning, String> resolver) {
        this.resolver = resolver;
    }

    public Function<TimeWarning, String> getTemplateValue() {
        return resolver;
    }
}
