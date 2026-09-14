package com.gepardec.mega.hexagon.worktime.domain.services.warning;

import com.gepardec.mega.hexagon.worktime.domain.model.ProjectBooking;
import com.gepardec.mega.hexagon.worktime.domain.model.Task;
import com.gepardec.mega.hexagon.worktime.domain.model.WorkTimeBookings;
import com.gepardec.mega.hexagon.worktime.domain.model.WorkTimeWarning;
import com.gepardec.mega.hexagon.worktime.domain.model.WorkTimeWarningType;
import com.gepardec.mega.hexagon.worktime.domain.model.WorkingLocation;
import org.instancio.Instancio;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.instancio.Select.field;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class WorkTimeWarningAssemblerTest {
    @Test
    void assemble_keepsDifferentWarningTypesOnSameDate() {
        LocalDate date = LocalDate.of(2026, 5, 2);
        var booking = Instancio.of(ProjectBooking.class)
                .set(field(ProjectBooking::from), date.atTime(5, 0))
                .set(field(ProjectBooking::to), date.atTime(12, 0))
                .set(field(ProjectBooking::task), Task.BEARBEITEN)
                .set(field(ProjectBooking::workingLocation), WorkingLocation.MAIN)
                .create();

        assertThat(new WorkTimeWarningAssembler().assemble(new WorkTimeBookings(List.of(booking)),
                Set.of(date), Set.of(), date.plusDays(1)))
                .extracting(WorkTimeWarning::type)
                .contains(WorkTimeWarningType.OUTSIDE_CORE_WORKING_TIME, WorkTimeWarningType.WEEKEND);
    }

    @Test
    void assemble_emptyBookingsReturnsSingleMonthWarning() {
        assertThat(new WorkTimeWarningAssembler().assemble(WorkTimeBookings.empty(), Set.of(), Set.of(), LocalDate.now()))
                .containsExactly(new WorkTimeWarning(null, WorkTimeWarningType.EMPTY_ENTRY_LIST, null));
    }

    @Test
    void assemble_forwardsSameBookingsAndBookedDatesAndKeepsFlatWarnings() {
        LocalDate bookedDate = LocalDate.of(2026, 5, 4);
        LocalDate expectedDate = bookedDate.plusDays(1);
        LocalDate today = expectedDate.plusDays(1);
        var booking = Instancio.of(ProjectBooking.class)
                .set(field(ProjectBooking::from), bookedDate.atTime(8, 0))
                .set(field(ProjectBooking::to), bookedDate.atTime(9, 0))
                .create();
        WorkTimeBookings bookings = new WorkTimeBookings(List.of(booking));
        WorkTimeWarning calculatorWarning = new WorkTimeWarning(bookedDate, WorkTimeWarningType.TIME_OVERLAP, null);
        WorkTimeWarning noEntryWarning = new WorkTimeWarning(expectedDate, WorkTimeWarningType.NO_TIME_ENTRY, null);
        WorkTimeWarningCalculator calculator = mock(WorkTimeWarningCalculator.class);
        NoEntryWarningCalculator noEntryCalculator = mock(NoEntryWarningCalculator.class);
        when(calculator.calculate(same(bookings))).thenReturn(List.of(calculatorWarning));
        when(noEntryCalculator.calculate(Set.of(expectedDate), bookings.bookedDates(), Set.of(), today))
                .thenReturn(List.of(noEntryWarning));

        assertThat(new WorkTimeWarningAssembler(List.of(calculator), noEntryCalculator)
                .assemble(bookings, Set.of(expectedDate), Set.of(), today))
                .containsExactly(calculatorWarning, noEntryWarning);
        verify(calculator).calculate(same(bookings));
        verify(noEntryCalculator).calculate(Set.of(expectedDate), Set.of(bookedDate), Set.of(), today);
    }
}
