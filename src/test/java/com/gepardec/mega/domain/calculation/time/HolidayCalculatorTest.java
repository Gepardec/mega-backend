package com.gepardec.mega.domain.calculation.time;

import com.gepardec.mega.domain.model.monthlyreport.ProjectEntry;
import com.gepardec.mega.domain.model.monthlyreport.ProjectTimeEntry;
import com.gepardec.mega.domain.model.monthlyreport.Task;
import com.gepardec.mega.domain.model.monthlyreport.TimeWarning;
import com.gepardec.mega.domain.model.monthlyreport.WorkingLocation;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

class HolidayCalculatorTest {

    private static final Integer[] NON_HOLIDAYS = {1, 2, 3, 4, 5, 6, 7, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 27, 28, 29, 30};

    private static final Integer[] HOLIDAYS = {8, 24, 25, 26, 31};

    private final HolidayCalculator calculator = new HolidayCalculator();

    static Stream<Integer> streamOfHolidays() {
        return Stream.of(HOLIDAYS);
    }

    static Stream<Integer> streamOfNonHolidays() {
        return Stream.of(NON_HOLIDAYS);
    }

    @Test
    @DisplayName("Test if correct holiday warning for 8.12.2021 Immaculate Conception is created")
    void calculate_whenEntryIsOnImmaculateConception_thenReturnsHolidayWarning() {
        ProjectEntry entry = createEntry(8);

        List<TimeWarning> result = calculator.calculate(List.of(entry));

        assertThat(result).hasSize(1);
    }

    @ParameterizedTest
    @MethodSource("streamOfHolidays")
    void calculate_whenEntryIsAHoliday_thenReturnsHolidayWarning(int day) {
        ProjectEntry entry = createEntry(day);

        List<TimeWarning> result = calculator.calculate(List.of(entry));

        assertThat(result).hasSize(1);
    }

    @ParameterizedTest
    @MethodSource("streamOfNonHolidays")
    void calculate_whenEntryIsntAHoliday_thenReturnsNoHolidayWarning(int day) {
        ProjectEntry entry = createEntry(day);

        List<TimeWarning> result = calculator.calculate(List.of(entry));

        assertThat(result).isEmpty();
    }

    private ProjectTimeEntry createEntry(int day) {
        return ProjectTimeEntry.builder()
                .fromTime(LocalDateTime.of(2021, 12, day, 8, 0))
                .toTime(LocalDateTime.of(2021, 12, day, 12, 0))
                .task(Task.BEARBEITEN)
                .workingLocation(WorkingLocation.MAIN)
                .build();
    }
}
