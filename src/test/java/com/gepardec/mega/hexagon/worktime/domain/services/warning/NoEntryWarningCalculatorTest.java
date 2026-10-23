package com.gepardec.mega.hexagon.worktime.domain.services.warning;

import com.gepardec.mega.hexagon.worktime.domain.model.ProjectBooking;
import com.gepardec.mega.hexagon.worktime.domain.model.WorkTimeBookings;
import com.gepardec.mega.hexagon.worktime.domain.model.WorkTimeEvaluation;
import com.gepardec.mega.hexagon.worktime.domain.model.WorkTimeWarning;
import com.gepardec.mega.hexagon.worktime.domain.model.WorkTimeWarningType;
import org.instancio.Instancio;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.instancio.Select.field;

class NoEntryWarningCalculatorTest {
    private final NoEntryWarningCalculator calculator = new NoEntryWarningCalculator();

    @Test
    void calculate_returnsOnlyPastExpectedUnbookedUnexcusedDays() {
        LocalDate pastMissing = LocalDate.of(2026, 5, 4);
        LocalDate booked = LocalDate.of(2026, 5, 5);
        LocalDate excused = LocalDate.of(2026, 5, 6);
        LocalDate today = LocalDate.of(2026, 5, 7);
        LocalDate future = LocalDate.of(2026, 5, 8);
        var booking = Instancio.of(ProjectBooking.class)
                .set(field(ProjectBooking::from), booked.atTime(8, 0))
                .set(field(ProjectBooking::to), booked.atTime(9, 0))
                .create();
        var evaluation = Instancio.of(WorkTimeEvaluation.class)
                .set(field(WorkTimeEvaluation::bookings), new WorkTimeBookings(List.of(booking)))
                .set(field(WorkTimeEvaluation::expectedWorkingDays), Set.of(pastMissing, booked, excused, today, future))
                .set(field(WorkTimeEvaluation::excusedDates), Set.of(excused))
                .set(field(WorkTimeEvaluation::evaluationDate), today)
                .create();

        assertThat(calculator.calculate(evaluation))
                .containsExactly(new WorkTimeWarning(pastMissing, WorkTimeWarningType.NO_TIME_ENTRY, null));
    }
}
