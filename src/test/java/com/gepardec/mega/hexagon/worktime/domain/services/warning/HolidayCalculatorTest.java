package com.gepardec.mega.hexagon.worktime.domain.services.warning;

import com.gepardec.mega.hexagon.worktime.domain.model.ProjectBooking;
import com.gepardec.mega.hexagon.worktime.domain.model.Task;
import com.gepardec.mega.hexagon.worktime.domain.model.WorkTimeBooking;
import com.gepardec.mega.hexagon.worktime.domain.model.WorkTimeWarning;
import com.gepardec.mega.hexagon.worktime.domain.model.WorkingLocation;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Stream;

import static com.gepardec.mega.hexagon.worktime.domain.services.warning.WarningTestBookingBuilder.bookings;
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
        WorkTimeBooking entry = createEntry(8);

        List<WorkTimeWarning> result = calculator.calculate(bookings(entry));

        assertThat(result).hasSize(1);
    }

    @ParameterizedTest
    @MethodSource("streamOfHolidays")
    void calculate_whenEntryIsAHoliday_thenReturnsHolidayWarning(int day) {
        WorkTimeBooking entry = createEntry(day);

        List<WorkTimeWarning> result = calculator.calculate(bookings(entry));

        assertThat(result).hasSize(1);
    }

    @ParameterizedTest
    @MethodSource("streamOfNonHolidays")
    void calculate_whenEntryIsntAHoliday_thenReturnsNoHolidayWarning(int day) {
        WorkTimeBooking entry = createEntry(day);

        List<WorkTimeWarning> result = calculator.calculate(bookings(entry));

        assertThat(result).isEmpty();
    }

    @Test
    void calculate_whenMultipleEntriesOnHoliday_thenReturnsSingleHolidayWarning() {
        WorkTimeBooking entry1 = createEntry(8);
        WorkTimeBooking entry2 = createEntry(8);

        List<WorkTimeWarning> result = calculator.calculate(bookings(entry1, entry2));

        assertThat(result).hasSize(1);
    }

    private ProjectBooking createEntry(int day) {
        return WarningTestBookingBuilder.projectBookingBuilder()
                .fromTime(LocalDateTime.of(2021, 12, day, 8, 0))
                .toTime(LocalDateTime.of(2021, 12, day, 12, 0))
                .task(Task.BEARBEITEN)
                .workingLocation(WorkingLocation.MAIN)
                .build();
    }
}
