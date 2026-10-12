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

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Stream;

import static com.gepardec.mega.hexagon.worktime.domain.services.warning.WarningTestBookingBuilder.bookings;
import static org.assertj.core.api.Assertions.assertThat;

class WeekendCalculatorTest {

    private static final Integer[] BUSINESS_DAYS = {1, 2, 3, 6, 7, 8, 9, 10, 13, 14, 15, 16, 17, 20, 21, 22, 23, 24, 27, 28, 29, 30, 31};

    private static final Integer[] WEEKEND_DAYS = {4, 5, 11, 12, 18, 19, 25, 26};

    private final WeekendCalculator calculator = new WeekendCalculator();

    static Stream<Integer> streamOfBusinessDays() {
        return Stream.of(BUSINESS_DAYS);
    }

    static Stream<Integer> streamOfWeekendDays() {
        return Stream.of(WEEKEND_DAYS);
    }

    @ParameterizedTest
    @DisplayName("Tests if no warnings are created for business days")
    @MethodSource("streamOfBusinessDays")
    void calculate_whenEntryIsABusinessDay_thenReturnsEmptyWarningList(int day) {
        WorkTimeBooking entry = createEntry(day);

        List<WorkTimeWarning> result = calculator.calculate(bookings(entry));

        assertThat(result)
                .isEmpty();
    }

    @ParameterizedTest
    @DisplayName("tests if correct warnings are created for weekend days")
    @MethodSource("streamOfWeekendDays")
    void calculate_whenEntryIsAWeekendDay_thenReturnsCorrectWarning(int day) {
        WorkTimeBooking entry = createEntry(day);

        List<WorkTimeWarning> result = calculator.calculate(bookings(entry));

        assertThat(result)
                .isNotEmpty()
                .hasSize(1);
        assertThat(result.getFirst().date())
                .isEqualTo(LocalDate.of(2021, 12, day));
    }

    @Test
    void calculate_whenMultipleEntriesOnWeekendDay_thenReturnsSingleWeekendWarning() {
        WorkTimeBooking entry1 = createEntry(4);
        WorkTimeBooking entry2 = createEntry(4);

        List<WorkTimeWarning> result = calculator.calculate(bookings(entry1, entry2));

        assertThat(result)
                .isNotEmpty()
                .hasSize(1);
        assertThat(result.getFirst().date())
                .isEqualTo(LocalDate.of(2021, 12, 4));
    }

    private ProjectBooking createEntry(int day) {
        return WarningTestBookingBuilder.projectBookingBuilder()
                .fromTime(LocalDateTime.of(2021, 12, day, 8, 0))
                .toTime(LocalDateTime.of(2021, 12, day, 16, 30))
                .task(Task.BEARBEITEN)
                .workingLocation(WorkingLocation.MAIN)
                .build();
    }
}
