package com.gepardec.mega.hexagon.worktime.domain.services.warning;

import com.gepardec.mega.hexagon.worktime.domain.model.JourneyBooking;
import com.gepardec.mega.hexagon.worktime.domain.model.JourneyDirection;
import com.gepardec.mega.hexagon.worktime.domain.model.ProjectBooking;
import com.gepardec.mega.hexagon.worktime.domain.model.Task;
import com.gepardec.mega.hexagon.worktime.domain.model.WorkTimeBooking;
import com.gepardec.mega.hexagon.worktime.domain.model.WorkTimeBookings;
import com.gepardec.mega.hexagon.worktime.domain.model.WorkTimeEvaluation;
import com.gepardec.mega.hexagon.worktime.domain.model.WorkTimeWarning;
import com.gepardec.mega.hexagon.worktime.domain.model.WorkTimeWarningType;
import com.gepardec.mega.hexagon.worktime.domain.model.WorkingLocation;
import org.instancio.Instancio;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
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
                .set(field(ProjectBooking::billable), true)
                .set(field(ProjectBooking::note), "Test")
                .create();

        assertThat(assemble(new WorkTimeBookings(List.of(booking)),
                YearMonth.from(date.plusDays(1)), Set.of(date), Set.of(), Set.of(), Set.of(), date.plusDays(1)))
                .extracting(WorkTimeWarning::type)
                .contains(WorkTimeWarningType.OUTSIDE_CORE_WORKING_TIME, WorkTimeWarningType.WEEKEND);
    }

    @Test
    void assemble_keepsFirstWarningPerTypeAndDateInOriginalOrder() {
        LocalDate date = LocalDate.of(2026, 5, 4);
        WorkTimeEvaluation evaluation = evaluation(new WorkTimeBookings(List.of(Instancio.create(ProjectBooking.class))),
                YearMonth.from(date), Set.of(), Set.of(), Set.of(), Set.of(), date);
        WorkTimeWarning first = new WorkTimeWarning(date, WorkTimeWarningType.WRONG_DOCTOR_APPOINTMENT, null);
        WorkTimeWarning otherType = new WorkTimeWarning(date, WorkTimeWarningType.TIME_OVERLAP, null);
        WorkTimeWarning otherDate = new WorkTimeWarning(date.plusDays(1), WorkTimeWarningType.WRONG_DOCTOR_APPOINTMENT, null);
        WorkTimeWarning duplicate = new WorkTimeWarning(date, WorkTimeWarningType.WRONG_DOCTOR_APPOINTMENT, null);
        WorkTimeWarningCalculator calculator = mock(WorkTimeWarningCalculator.class);
        WorkTimeWarningCalculator otherCalculator = mock(WorkTimeWarningCalculator.class);
        when(calculator.calculate(same(evaluation))).thenReturn(List.of(first, otherType, duplicate, otherDate));
        when(otherCalculator.calculate(same(evaluation))).thenReturn(List.of(duplicate));

        assertThat(new WorkTimeWarningAssembler(List.of(calculator, otherCalculator)).assemble(evaluation))
                .containsExactly(first, otherType, otherDate);
    }

    @Test
    void assemble_whenSeveralBookingsOnOneDayHaveAnInvalidWorkingLocation_thenOneWarningForThatDay() {
        LocalDate date = LocalDate.of(2026, 5, 4);
        List<WorkTimeBooking> bookingsAwayFromMain = List.of(
                projectAt(date.atTime(8, 0), date.atTime(10, 0), WorkingLocation.A),
                projectAt(date.atTime(10, 0), date.atTime(12, 0), WorkingLocation.A),
                projectAt(date.atTime(13, 0), date.atTime(16, 0), WorkingLocation.A));

        assertThat(assemble(new WorkTimeBookings(bookingsAwayFromMain), YearMonth.from(date.plusDays(1)), Set.of(date), Set.of(), Set.of(), Set.of(), date.plusDays(1)))
                .filteredOn(warning -> warning.type() == WorkTimeWarningType.INVALID_WORKING_LOCATION)
                .containsExactly(new WorkTimeWarning(date, WorkTimeWarningType.INVALID_WORKING_LOCATION, null));
    }

    @Test
    void assemble_whenBillableBookingIsMarkedInternalFromOctober2026_thenInternalNoteBillableWarning() {
        LocalDate date = LocalDate.of(2026, 10, 6);
        var booking = Instancio.of(ProjectBooking.class)
                .set(field(ProjectBooking::from), date.atTime(8, 0))
                .set(field(ProjectBooking::to), date.atTime(9, 0))
                .set(field(ProjectBooking::task), Task.BEARBEITEN)
                .set(field(ProjectBooking::workingLocation), WorkingLocation.MAIN)
                .set(field(ProjectBooking::billable), true)
                .set(field(ProjectBooking::note), "Intern - JFX")
                .create();

        assertThat(assemble(new WorkTimeBookings(List.of(booking)),
                YearMonth.from(date.plusDays(1)), Set.of(date), Set.of(), Set.of(), Set.of(), date.plusDays(1)))
                .contains(new WorkTimeWarning(date, WorkTimeWarningType.INTERNAL_NOTE_BILLABLE, null));
    }

    @Test
    void assemble_whenNonBillableBookingWithoutMarkerIsOnBillableProjectFromOctober2026_thenBothConventionWarnings() {
        LocalDate date = LocalDate.of(2026, 10, 6);
        var booking = Instancio.of(ProjectBooking.class)
                .set(field(ProjectBooking::from), date.atTime(8, 0))
                .set(field(ProjectBooking::to), date.atTime(9, 0))
                .set(field(ProjectBooking::task), Task.BEARBEITEN)
                .set(field(ProjectBooking::workingLocation), WorkingLocation.MAIN)
                .set(field(ProjectBooking::billable), false)
                .set(field(ProjectBooking::note), "JFX")
                .set(field(ProjectBooking::projectZepId), 42)
                .create();

        assertThat(assemble(new WorkTimeBookings(List.of(booking)),
                YearMonth.from(date.plusDays(1)), Set.of(date), Set.of(), Set.of(42), Set.of(), date.plusDays(1)))
                .contains(
                        new WorkTimeWarning(date, WorkTimeWarningType.INTERNAL_NOTE_MISSING, null),
                        new WorkTimeWarning(date, WorkTimeWarningType.NON_BILLABLE_ON_BILLABLE_PROJECT, null));
    }

    @Test
    void assemble_whenTravelAllowanceBookedWithoutJourneyFromOctober2026_thenTravelAllowanceExceeded() {
        LocalDate date = LocalDate.of(2026, 10, 6);
        var booking = Instancio.of(ProjectBooking.class)
                .set(field(ProjectBooking::from), date.atTime(8, 0))
                .set(field(ProjectBooking::to), date.atTime(8, 30))
                .set(field(ProjectBooking::task), Task.BEARBEITEN)
                .set(field(ProjectBooking::workingLocation), WorkingLocation.MAIN)
                .set(field(ProjectBooking::process), "873")
                .set(field(ProjectBooking::billable), false)
                .set(field(ProjectBooking::note), "Intern - Reisepauschale")
                .set(field(ProjectBooking::projectZepId), 1)
                .create();

        assertThat(assemble(new WorkTimeBookings(List.of(booking)),
                YearMonth.from(date.plusDays(1)), Set.of(date), Set.of(), Set.of(), Set.of(), date.plusDays(1)))
                .contains(new WorkTimeWarning(date, WorkTimeWarningType.TRAVEL_ALLOWANCE_EXCEEDED, 0.5));
    }

    @Test
    void assemble_whenDayTripWithoutMealEntryFromOctober2026_thenMealEntryMissing() {
        LocalDate date = LocalDate.of(2026, 10, 6);
        var outbound = journeyAt(date.atTime(8, 0), date.atTime(9, 0), JourneyDirection.TO);
        var back = journeyAt(date.atTime(16, 0), date.atTime(17, 0), JourneyDirection.BACK);

        assertThat(assemble(new WorkTimeBookings(List.of(outbound, back)),
                YearMonth.of(2026, 10), Set.of(date), Set.of(), Set.of(), Set.of(), date.plusDays(1)))
                .contains(new WorkTimeWarning(date, WorkTimeWarningType.MEAL_ENTRY_MISSING, null));
    }

    @Test
    void assemble_whenDayTripHasMealEntry_thenNoMealEntryMissing() {
        LocalDate date = LocalDate.of(2026, 10, 6);
        var outbound = journeyAt(date.atTime(8, 0), date.atTime(9, 0), JourneyDirection.TO);
        var back = journeyAt(date.atTime(16, 0), date.atTime(17, 0), JourneyDirection.BACK);

        assertThat(assemble(new WorkTimeBookings(List.of(outbound, back)),
                YearMonth.of(2026, 10), Set.of(date), Set.of(), Set.of(), Set.of(date), date.plusDays(1)))
                .extracting(WorkTimeWarning::type)
                .doesNotContain(WorkTimeWarningType.MEAL_ENTRY_MISSING);
    }

    @Test
    void assemble_emptyBookingsReturnsSingleMonthWarning() {
        assertThat(assemble(WorkTimeBookings.empty(), YearMonth.now(),
                Set.of(), Set.of(), Set.of(), Set.of(), LocalDate.now()))
                .containsExactly(new WorkTimeWarning(null, WorkTimeWarningType.EMPTY_ENTRY_LIST, null));
    }

    @Test
    void assemble_forwardsSameEvaluationToEveryCalculatorAndKeepsFlatWarnings() {
        LocalDate bookedDate = LocalDate.of(2026, 5, 4);
        LocalDate expectedDate = bookedDate.plusDays(1);
        LocalDate today = expectedDate.plusDays(1);
        var booking = Instancio.of(ProjectBooking.class)
                .set(field(ProjectBooking::from), bookedDate.atTime(8, 0))
                .set(field(ProjectBooking::to), bookedDate.atTime(9, 0))
                .set(field(ProjectBooking::billable), true)
                .set(field(ProjectBooking::note), "Test")
                .create();
        WorkTimeEvaluation evaluation = evaluation(new WorkTimeBookings(List.of(booking)),
                YearMonth.from(today), Set.of(expectedDate), Set.of(), Set.of(), Set.of(), today);
        WorkTimeWarning calculatorWarning = new WorkTimeWarning(bookedDate, WorkTimeWarningType.TIME_OVERLAP, null);
        WorkTimeWarning noEntryWarning = new WorkTimeWarning(expectedDate, WorkTimeWarningType.NO_TIME_ENTRY, null);
        WorkTimeWarningCalculator calculator = mock(WorkTimeWarningCalculator.class);
        NoEntryWarningCalculator noEntryCalculator = mock(NoEntryWarningCalculator.class);
        when(calculator.calculate(same(evaluation))).thenReturn(List.of(calculatorWarning));
        when(noEntryCalculator.calculate(same(evaluation))).thenReturn(List.of(noEntryWarning));

        assertThat(new WorkTimeWarningAssembler(List.of(calculator, noEntryCalculator)).assemble(evaluation))
                .containsExactly(calculatorWarning, noEntryWarning);
        verify(calculator).calculate(same(evaluation));
        verify(noEntryCalculator).calculate(same(evaluation));
    }

    private static List<WorkTimeWarning> assemble(WorkTimeBookings bookings, YearMonth month, Set<LocalDate> expectedWorkingDays,
                                                  Set<LocalDate> excusedDates, Set<Integer> billableProjectZepIds,
                                                  Set<LocalDate> mealEntryDates, LocalDate today) {
        return new WorkTimeWarningAssembler().assemble(
                evaluation(bookings, month, expectedWorkingDays, excusedDates, billableProjectZepIds, mealEntryDates, today));
    }

    private static WorkTimeEvaluation evaluation(WorkTimeBookings bookings, YearMonth month, Set<LocalDate> expectedWorkingDays,
                                                 Set<LocalDate> excusedDates, Set<Integer> billableProjectZepIds,
                                                 Set<LocalDate> mealEntryDates, LocalDate today) {
        return new WorkTimeEvaluation(bookings, month, today, expectedWorkingDays, excusedDates, billableProjectZepIds,
                mealEntryDates);
    }

    private static JourneyBooking journeyAt(LocalDateTime from, LocalDateTime to, JourneyDirection direction) {
        return Instancio.of(JourneyBooking.class)
                .set(field(JourneyBooking::from), from)
                .set(field(JourneyBooking::to), to)
                .set(field(JourneyBooking::direction), direction)
                .create();
    }

    private static ProjectBooking projectAt(LocalDateTime from, LocalDateTime to, WorkingLocation location) {
        return Instancio.of(ProjectBooking.class)
                .set(field(ProjectBooking::from), from)
                .set(field(ProjectBooking::to), to)
                .set(field(ProjectBooking::task), Task.BEARBEITEN)
                .set(field(ProjectBooking::workingLocation), location)
                .set(field(ProjectBooking::workLocationProjectRelevant), false)
                .set(field(ProjectBooking::billable), true)
                .set(field(ProjectBooking::note), "Test")
                .create();
    }
}
