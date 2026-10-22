package com.gepardec.mega.hexagon.worktime.domain.services.warning;

import com.gepardec.mega.hexagon.worktime.domain.model.JourneyDirection;
import com.gepardec.mega.hexagon.worktime.domain.model.Task;
import com.gepardec.mega.hexagon.worktime.domain.model.Vehicle;
import com.gepardec.mega.hexagon.worktime.domain.model.WorkTimeBooking;
import com.gepardec.mega.hexagon.worktime.domain.model.WorkTimeWarning;
import com.gepardec.mega.hexagon.worktime.domain.model.WorkingLocation;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import java.time.LocalDate;

import static com.gepardec.mega.hexagon.worktime.domain.model.WorkTimeWarningType.INTERNAL_NOTE_BILLABLE;
import static com.gepardec.mega.hexagon.worktime.domain.model.WorkTimeWarningType.INTERNAL_NOTE_MISSING;
import static com.gepardec.mega.hexagon.worktime.domain.services.warning.WarningTestBookingBuilder.bookings;
import static com.gepardec.mega.hexagon.worktime.domain.services.warning.WarningTestBookingBuilder.journeyBookingBuilder;
import static com.gepardec.mega.hexagon.worktime.domain.services.warning.WarningTestBookingBuilder.projectBookingBuilder;
import static org.assertj.core.api.Assertions.assertThat;

class InternalNoteConventionCalculatorTest {
    private static final LocalDate DATE = LocalDate.of(2026, 10, 5);

    private InternalNoteConventionCalculator calculator;

    @BeforeEach
    void setUp() {
        calculator = new InternalNoteConventionCalculator();
    }

    @ParameterizedTest
    @ValueSource(strings = {"Intern - Jenkins aufräumen", "intern - JFX", "Intern -JFX", "Intern– JFX", "  Intern - JFX", "INTERN-JFX"})
    void calculate_whenBillableProjectBookingIsMarkedInternal_thenBillableWarning(String note) {
        assertThat(calculator.calculate(bookings(project(DATE, true, note))))
                .containsExactly(new WorkTimeWarning(DATE, INTERNAL_NOTE_BILLABLE, null));
    }

    @ParameterizedTest
    @ValueSource(strings = {"Intern - Jenkins aufräumen", "intern - JFX", "Intern -JFX", "Intern– JFX", "  Intern - JFX", "INTERN-JFX"})
    void calculate_whenNonBillableProjectBookingIsMarkedInternal_thenNoWarning(String note) {
        assertThat(calculator.calculate(bookings(project(DATE, false, note)))).isEmpty();
    }

    @ParameterizedTest
    @ValueSource(strings = {"JFX (Intern - Vorbereitung)", "Internes Meeting", "Intern JFX", "Intern: JFX", "Heapdump analysiert"})
    void calculate_whenNoteIsNotMarkedInternal_thenBillableBookingHasNoWarningAndNonBillableBookingIsMissing(String note) {
        assertThat(calculator.calculate(bookings(project(DATE, true, note)))).isEmpty();
        assertThat(calculator.calculate(bookings(project(DATE, false, note))))
                .containsExactly(new WorkTimeWarning(DATE, INTERNAL_NOTE_MISSING, null));
    }

    @ParameterizedTest
    @NullAndEmptySource
    void calculate_whenNoteIsMissingOrEmpty_thenNonBillableBookingIsMissing(String note) {
        assertThat(calculator.calculate(bookings(project(DATE, false, note))))
                .containsExactly(new WorkTimeWarning(DATE, INTERNAL_NOTE_MISSING, null));
        assertThat(calculator.calculate(bookings(project(DATE, true, note)))).isEmpty();
    }

    @Test
    void calculate_whenBillableJourneyBookingIsMarkedInternal_thenBillableWarning() {
        assertThat(calculator.calculate(bookings(journey(DATE, true, "Intern - Fahrt zum Office"))))
                .containsExactly(new WorkTimeWarning(DATE, INTERNAL_NOTE_BILLABLE, null));
    }

    @Test
    void calculate_whenNonBillableJourneyBookingIsNotMarkedInternal_thenMissingWarning() {
        assertThat(calculator.calculate(bookings(journey(DATE, false, "Nach Linz"))))
                .containsExactly(new WorkTimeWarning(DATE, INTERNAL_NOTE_MISSING, null));
    }

    @Test
    void calculate_whenSeveralBookingsOnOneDayAreAffected_thenOneWarningPerTypeAndDay() {
        assertThat(calculator.calculate(bookings(
                project(DATE, true, "Intern - A"), journey(DATE, true, "Intern - B"),
                project(DATE, false, "C"), journey(DATE, false, null))))
                .containsExactlyInAnyOrder(
                        new WorkTimeWarning(DATE, INTERNAL_NOTE_BILLABLE, null),
                        new WorkTimeWarning(DATE, INTERNAL_NOTE_MISSING, null));
    }

    @Test
    void calculate_whenBookingIsBeforeConventionStart_thenNoWarning() {
        LocalDate beforeStart = LocalDate.of(2026, 9, 30);

        assertThat(calculator.calculate(bookings(
                project(beforeStart, false, null), project(beforeStart, true, "Intern - JFX")))).isEmpty();
    }

    @Test
    void calculate_whenBookingIsOnConventionStart_thenWarning() {
        LocalDate start = LocalDate.of(2026, 10, 1);

        assertThat(calculator.calculate(bookings(project(start, false, null))))
                .containsExactly(new WorkTimeWarning(start, INTERNAL_NOTE_MISSING, null));
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
