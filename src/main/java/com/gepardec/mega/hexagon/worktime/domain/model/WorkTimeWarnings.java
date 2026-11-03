package com.gepardec.mega.hexagon.worktime.domain.model;

import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * The warnings of one employee and month. Holds at most one warning per type and date;
 * on duplicates the first one wins and the original order is kept.
 */
public record WorkTimeWarnings(List<WorkTimeWarning> values) {

    public WorkTimeWarnings {
        Objects.requireNonNull(values, "values must not be null");
        if (values.stream().anyMatch(Objects::isNull)) {
            throw new NullPointerException("values must not contain null");
        }
        values = List.copyOf(values.stream()
                .collect(Collectors.toMap(
                        warning -> new TypeAndDate(warning.type(), warning.date()),
                        Function.identity(),
                        (first, duplicate) -> first,
                        LinkedHashMap::new))
                .values());
    }

    /** A month without any booking yields only the month-level empty-entry-list warning. */
    public static WorkTimeWarnings noEntries() {
        return new WorkTimeWarnings(List.of(new WorkTimeWarning(null, WorkTimeWarningType.EMPTY_ENTRY_LIST, null)));
    }

    private record TypeAndDate(WorkTimeWarningType type, LocalDate date) {
    }
}
