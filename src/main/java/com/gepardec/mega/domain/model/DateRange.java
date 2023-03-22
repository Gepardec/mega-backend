package com.gepardec.mega.domain.model;

import lombok.*;

import java.time.LocalDate;
import java.util.Objects;

@Getter
@AllArgsConstructor
public class DateRange {
    private final LocalDate from;
    private final LocalDate to;

    public static DateRange of(LocalDate from, LocalDate to) {
        return new DateRange(from, to);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DateRange dateRange = (DateRange) o;
        return from.equals(dateRange.from) && to.equals(dateRange.to);
    }

    @Override
    public int hashCode() {
        return Objects.hash(from, to);
    }
}
