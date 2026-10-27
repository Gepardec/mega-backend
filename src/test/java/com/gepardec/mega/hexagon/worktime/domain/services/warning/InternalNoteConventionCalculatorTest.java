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
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

import static com.gepardec.mega.hexagon.worktime.domain.model.WorkTimeWarningType.INTERNAL_NOTE_BILLABLE;
import static com.gepardec.mega.hexagon.worktime.domain.model.WorkTimeWarningType.INTERNAL_NOTE_MISSING;
import static com.gepardec.mega.hexagon.worktime.domain.services.warning.WarningTestBookingBuilder.bookings;
import static com.gepardec.mega.hexagon.worktime.domain.services.warning.WarningTestBookingBuilder.journeyBookingBuilder;
import static com.gepardec.mega.hexagon.worktime.domain.services.warning.WarningTestBookingBuilder.projectBookingBuilder;
import static org.assertj.core.api.Assertions.assertThat;
import static org.instancio.Select.field;

class InternalNoteConventionCalculatorTest {
    private static final LocalDate DATE = LocalDate.of(2026, 10, 5);
    private static final LocalDate OTHER_DATE = LocalDate.of(2026, 10, 20);
    private static final int BILLABLE_PROJECT = 10;

    private InternalNoteConventionCalculator calculator;

    @BeforeEach
    void setUp() {
        calculator = new InternalNoteConventionCalculator();
    }

    @ParameterizedTest
    @ValueSource(strings = {"Intern - Jenkins aufräumen", "intern - JFX", "Intern -JFX", "Intern– JFX", "  Intern - JFX", "INTERN-JFX"})
    void calculate_whenBillableProjectBookingIsMarkedInternal_thenBillableWarning(String note) {
        assertThat(calculate(bookings(project(DATE, true, note))))
                .containsExactly(new WorkTimeWarning(DATE, INTERNAL_NOTE_BILLABLE, null));
    }

    @ParameterizedTest
    @ValueSource(strings = {"Intern - Jenkins aufräumen", "intern - JFX", "Intern -JFX", "Intern– JFX", "  Intern - JFX", "INTERN-JFX"})
    void calculate_whenNonBillableProjectBookingIsMarkedInternal_thenNoWarning(String note) {
        assertThat(calculate(bookings(project(DATE, false, note)))).isEmpty();
    }

    @ParameterizedTest
    @ValueSource(strings = {"JFX (Intern - Vorbereitung)", "Internes Meeting", "Intern JFX", "Intern: JFX", "Heapdump analysiert"})
    void calculate_whenNoteIsNotMarkedInternal_thenBillableBookingHasNoWarningAndNonBillableBookingIsMissing(String note) {
        assertThat(calculate(bookings(project(DATE, true, note)))).isEmpty();
        assertThat(calculate(bookings(project(DATE, false, note), billableWork())))
                .containsExactly(new WorkTimeWarning(DATE, INTERNAL_NOTE_MISSING, null));
    }

    @ParameterizedTest
    @NullAndEmptySource
    void calculate_whenNoteIsMissingOrEmpty_thenNonBillableBookingIsMissing(String note) {
        assertThat(calculate(bookings(project(DATE, false, note), billableWork())))
                .containsExactly(new WorkTimeWarning(DATE, INTERNAL_NOTE_MISSING, null));
        assertThat(calculate(bookings(project(DATE, true, note)))).isEmpty();
    }

    @Test
    void calculate_whenBillableJourneyBookingIsMarkedInternal_thenBillableWarning() {
        assertThat(calculate(bookings(journey(DATE, true, "Intern - Fahrt zum Office"))))
                .containsExactly(new WorkTimeWarning(DATE, INTERNAL_NOTE_BILLABLE, null));
    }

    @Test
    void calculate_whenNonBillableJourneyBookingIsNotMarkedInternal_thenMissingWarning() {
        assertThat(calculate(bookings(journey(DATE, false, "Nach Linz"), billableWork())))
                .containsExactly(new WorkTimeWarning(DATE, INTERNAL_NOTE_MISSING, null));
    }

    @Test
    void calculate_whenSeveralBookingsOnOneDayAreAffected_thenOneWarningPerTypeAndDay() {
        assertThat(calculate(bookings(
                project(DATE, true, "Intern - A"), journey(DATE, true, "Intern - B"),
                project(DATE, false, "C"), journey(DATE, false, null), billableWork())))
                .containsExactlyInAnyOrder(
                        new WorkTimeWarning(DATE, INTERNAL_NOTE_BILLABLE, null),
                        new WorkTimeWarning(DATE, INTERNAL_NOTE_MISSING, null));
    }

    @Test
    void calculate_whenMonthHasOnlyNonBillableBookingsOnNonBillableProjects_thenNoMissingWarning() {
        assertThat(calculate(bookings(
                project(DATE, false, null), journey(DATE.plusDays(1), false, "Nach Linz"))))
                .isEmpty();
    }

    @Test
    void calculate_whenSingleBillableBookingIsOnAnotherDay_thenEarlierUnmarkedDaysWarn() {
        assertThat(calculate(bookings(
                project(DATE, false, null), project(DATE.plusDays(1), false, "JFX"), billableWork())))
                .containsExactlyInAnyOrder(
                        new WorkTimeWarning(DATE, INTERNAL_NOTE_MISSING, null),
                        new WorkTimeWarning(DATE.plusDays(1), INTERNAL_NOTE_MISSING, null));
    }

    @Test
    void calculate_whenNonBillableBookingIsOnBillableProject_thenCountsAsBillableWork() {
        assertThat(calculate(bookings(project(DATE, false, "JFX", BILLABLE_PROJECT)), Set.of(BILLABLE_PROJECT)))
                .containsExactly(new WorkTimeWarning(DATE, INTERNAL_NOTE_MISSING, null));
    }

    @Test
    void calculate_whenBookingHasNoProjectReference_thenDoesNotCountAsBillableWork() {
        assertThat(calculate(bookings(project(DATE, false, "JFX", null)), Set.of(BILLABLE_PROJECT))).isEmpty();
    }

    @Test
    void calculate_whenMonthHasOnlyNonBillableBookingsAndBillableMarkedInternal_thenBillableWarningStillProduced() {
        assertThat(calculate(bookings(project(DATE, true, "Intern - JFX"), project(OTHER_DATE, false, null))))
                .containsExactlyInAnyOrder(
                        new WorkTimeWarning(DATE, INTERNAL_NOTE_BILLABLE, null),
                        new WorkTimeWarning(OTHER_DATE, INTERNAL_NOTE_MISSING, null));
    }

    private List<WorkTimeWarning> calculate(WorkTimeBookings bookings) {
        return calculate(bookings, Set.of());
    }

    private List<WorkTimeWarning> calculate(WorkTimeBookings bookings, Set<Integer> billableProjectZepIds) {
        return calculator.calculate(Instancio.of(WorkTimeEvaluation.class)
                .set(field(WorkTimeEvaluation::bookings), bookings)
                .set(field(WorkTimeEvaluation::billableProjectZepIds), billableProjectZepIds)
                .create());
    }

    private static WorkTimeBooking billableWork() {
        return project(OTHER_DATE, true, "Arbeit");
    }

    private static WorkTimeBooking project(LocalDate date, boolean billable, String note, Integer projectZepId) {
        return projectBookingBuilder()
                .fromTime(date.atTime(8, 0)).toTime(date.atTime(9, 0))
                .task(Task.BEARBEITEN).workingLocation(WorkingLocation.MAIN)
                .billable(billable).note(note).projectZepId(projectZepId)
                .build();
    }

    private static WorkTimeBooking project(LocalDate date, boolean billable, String note) {
        return projectBookingBuilder()
                .fromTime(date.atTime(8, 0)).toTime(date.atTime(9, 0))
                .task(Task.BEARBEITEN).workingLocation(WorkingLocation.MAIN)
                .billable(billable).note(note)
                .build();
    }

    private static WorkTimeBooking journey(LocalDate date, boolean billable, String note) {
        return journeyBookingBuilder()
                .fromTime(date.atTime(10, 0)).toTime(date.atTime(11, 0))
                .task(Task.REISEN).workingLocation(WorkingLocation.MAIN)
                .journeyDirection(JourneyDirection.TO).vehicle(Vehicle.CAR_ACTIVE)
                .billable(billable).note(note)
                .build();
    }
}
