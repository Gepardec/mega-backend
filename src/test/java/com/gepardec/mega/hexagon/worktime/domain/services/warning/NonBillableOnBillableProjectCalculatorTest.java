package com.gepardec.mega.hexagon.worktime.domain.services.warning;

import com.gepardec.mega.hexagon.worktime.domain.model.JourneyDirection;
import com.gepardec.mega.hexagon.worktime.domain.model.Task;
import com.gepardec.mega.hexagon.worktime.domain.model.Vehicle;
import com.gepardec.mega.hexagon.worktime.domain.model.WorkTimeBooking;
import com.gepardec.mega.hexagon.worktime.domain.model.WorkTimeBookings;
import com.gepardec.mega.hexagon.worktime.domain.model.WorkTimeEvaluation;
import com.gepardec.mega.hexagon.worktime.domain.model.WorkTimeWarning;
import com.gepardec.mega.hexagon.worktime.domain.model.WorkingLocation;
import org.instancio.Instancio;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

import static com.gepardec.mega.hexagon.worktime.domain.model.WorkTimeWarningType.NON_BILLABLE_ON_BILLABLE_PROJECT;
import static com.gepardec.mega.hexagon.worktime.domain.services.warning.WarningTestBookingBuilder.bookings;
import static com.gepardec.mega.hexagon.worktime.domain.services.warning.WarningTestBookingBuilder.journeyBookingBuilder;
import static com.gepardec.mega.hexagon.worktime.domain.services.warning.WarningTestBookingBuilder.projectBookingBuilder;
import static org.assertj.core.api.Assertions.assertThat;
import static org.instancio.Select.field;

class NonBillableOnBillableProjectCalculatorTest {
    private static final LocalDate DATE = LocalDate.of(2026, 10, 5);
    private static final int BILLABLE_PROJECT = 10;
    private static final Set<Integer> BILLABLE_PROJECTS = Set.of(BILLABLE_PROJECT);

    private NonBillableOnBillableProjectCalculator calculator;

    @BeforeEach
    void setUp() {
        calculator = new NonBillableOnBillableProjectCalculator();
    }

    @Test
    void calculate_whenNonBillableProjectBookingIsOnBillableProject_thenWarning() {
        assertThat(calculate(bookings(project(DATE, false, "JFX", BILLABLE_PROJECT)), BILLABLE_PROJECTS))
                .containsExactly(new WorkTimeWarning(DATE, NON_BILLABLE_ON_BILLABLE_PROJECT, null));
    }

    @Test
    void calculate_whenNonBillableJourneyBookingIsOnBillableProject_thenWarning() {
        assertThat(calculate(bookings(journey(DATE, false, BILLABLE_PROJECT)), BILLABLE_PROJECTS))
                .containsExactly(new WorkTimeWarning(DATE, NON_BILLABLE_ON_BILLABLE_PROJECT, null));
    }

    @Test
    void calculate_whenBookingIsMarkedInternal_thenStillWarning() {
        assertThat(calculate(bookings(project(DATE, false, "Intern - JFX", BILLABLE_PROJECT)), BILLABLE_PROJECTS))
                .containsExactly(new WorkTimeWarning(DATE, NON_BILLABLE_ON_BILLABLE_PROJECT, null));
    }

    @Test
    void calculate_whenBookingIsBillable_thenNoWarning() {
        assertThat(calculate(bookings(
                project(DATE, true, "JFX", BILLABLE_PROJECT), journey(DATE, true, BILLABLE_PROJECT)), BILLABLE_PROJECTS)).isEmpty();
    }

    @Test
    void calculate_whenProjectIsNotInBillableSet_thenNoWarning() {
        assertThat(calculate(bookings(project(DATE, false, "JFX", 99)), BILLABLE_PROJECTS)).isEmpty();
        assertThat(calculate(bookings(project(DATE, false, "JFX", BILLABLE_PROJECT)), Set.of())).isEmpty();
    }

    @Test
    void calculate_whenBookingHasNoProjectReference_thenNoWarning() {
        assertThat(calculate(bookings(
                project(DATE, false, "JFX", null), journey(DATE, false, null)), BILLABLE_PROJECTS)).isEmpty();
    }

    @Test
    void calculate_whenSeveralBookingsOnOneDayAreAffected_thenOneWarningPerDay() {
        assertThat(calculate(bookings(
                project(DATE, false, "A", BILLABLE_PROJECT), journey(DATE, false, BILLABLE_PROJECT)), BILLABLE_PROJECTS))
                .containsExactly(new WorkTimeWarning(DATE, NON_BILLABLE_ON_BILLABLE_PROJECT, null));
    }

    private static WorkTimeBooking project(LocalDate date, boolean billable, String note, Integer projectZepId) {
        return projectBookingBuilder()
                .fromTime(date.atTime(8, 0)).toTime(date.atTime(9, 0))
                .task(Task.BEARBEITEN).workingLocation(WorkingLocation.MAIN)
                .billable(billable).note(note).projectZepId(projectZepId)
                .build();
    }

    private static WorkTimeBooking journey(LocalDate date, boolean billable, Integer projectZepId) {
        return journeyBookingBuilder()
                .fromTime(date.atTime(10, 0)).toTime(date.atTime(11, 0))
                .task(Task.REISEN).workingLocation(WorkingLocation.MAIN)
                .journeyDirection(JourneyDirection.TO).vehicle(Vehicle.CAR_ACTIVE)
                .billable(billable).note("Fahrt").projectZepId(projectZepId)
                .build();
    }

    private List<WorkTimeWarning> calculate(WorkTimeBookings bookings, Set<Integer> billableProjectZepIds) {
        return calculator.calculate(Instancio.of(WorkTimeEvaluation.class)
                .set(field(WorkTimeEvaluation::bookings), bookings)
                .set(field(WorkTimeEvaluation::billableProjectZepIds), billableProjectZepIds)
                .create());
    }
}
