package com.gepardec.mega.hexagon.worktime.domain.services.warning;

import com.gepardec.mega.hexagon.worktime.domain.model.JourneyBooking;
import com.gepardec.mega.hexagon.worktime.domain.model.JourneyDirection;
import com.gepardec.mega.hexagon.worktime.domain.model.ProjectBooking;
import com.gepardec.mega.hexagon.worktime.domain.model.Task;
import com.gepardec.mega.hexagon.worktime.domain.model.Vehicle;
import com.gepardec.mega.hexagon.worktime.domain.model.WorkTimeWarning;
import com.gepardec.mega.hexagon.worktime.domain.model.WorkingLocation;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static com.gepardec.mega.hexagon.worktime.domain.model.WorkTimeWarningType.INTERNAL_PREFIX_MISSING;
import static com.gepardec.mega.hexagon.worktime.domain.model.WorkTimeWarningType.INTERNAL_PREFIX_ON_BILLABLE_PROJECT;
import static com.gepardec.mega.hexagon.worktime.domain.services.warning.WarningTestBookingBuilder.bookings;
import static com.gepardec.mega.hexagon.worktime.domain.services.warning.WarningTestBookingBuilder.journeyBookingBuilder;
import static com.gepardec.mega.hexagon.worktime.domain.services.warning.WarningTestBookingBuilder.projectBookingBuilder;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNullPointerException;

class InternalNotePrefixCalculatorTest {
    private static final LocalDate DATE = LocalDate.of(2026, 5, 4);
    private static final int BILLABLE_PROJECT = 1;
    private static final int INTERNAL_PROJECT = 2;
    private static final int UNKNOWN_PROJECT = 99;
    private static final Map<Integer, Boolean> BILLABILITY = Map.of(BILLABLE_PROJECT, true, INTERNAL_PROJECT, false);

    private InternalNotePrefixCalculator calculator;

    @BeforeEach
    void init() {
        calculator = new InternalNotePrefixCalculator();
    }

    @Nested
    class BillableProject {

        @ParameterizedTest
        @ValueSource(strings = {"Intern - Teammeeting", "intern-Teammeeting", "INTERN - Teammeeting",
                "  Intern - Teammeeting", "Intern – Teammeeting", "Intern—Teammeeting"})
        void calculate_whenNoteCarriesPrefixVariant_thenWarning(String note) {
            List<WorkTimeWarning> warnings = calculator.calculate(bookings(project(BILLABLE_PROJECT, note, 8)), BILLABILITY);

            assertThat(warnings).containsExactly(new WorkTimeWarning(DATE, INTERNAL_PREFIX_ON_BILLABLE_PROJECT, null));
        }

        @ParameterizedTest
        @ValueSource(strings = {"Kickoff", "Interne Schulung", "Internet-Recherche"})
        void calculate_whenNoteCarriesNoPrefix_thenNoWarning(String note) {
            List<WorkTimeWarning> warnings = calculator.calculate(bookings(project(BILLABLE_PROJECT, note, 8)), BILLABILITY);

            assertThat(warnings).isEmpty();
        }

        @Test
        void calculate_whenSeveralPrefixedBookingsOnOneDay_thenOneWarning() {
            List<WorkTimeWarning> warnings = calculator.calculate(bookings(
                    project(BILLABLE_PROJECT, "Intern - A", 8),
                    project(BILLABLE_PROJECT, "Intern - B", 10)), BILLABILITY);

            assertThat(warnings).containsExactly(new WorkTimeWarning(DATE, INTERNAL_PREFIX_ON_BILLABLE_PROJECT, null));
        }
    }

    @Nested
    class InternalProject {

        @ParameterizedTest
        @ValueSource(strings = {"Teammeeting", "Interne Schulung", "Internet-Recherche"})
        void calculate_whenNoteCarriesNoPrefix_thenWarning(String note) {
            List<WorkTimeWarning> warnings = calculator.calculate(bookings(project(INTERNAL_PROJECT, note, 8)), BILLABILITY);

            assertThat(warnings).containsExactly(new WorkTimeWarning(DATE, INTERNAL_PREFIX_MISSING, null));
        }

        @ParameterizedTest
        @NullAndEmptySource
        @ValueSource(strings = {"   "})
        void calculate_whenNoteIsAbsentOrBlank_thenWarning(String note) {
            List<WorkTimeWarning> warnings = calculator.calculate(bookings(project(INTERNAL_PROJECT, note, 8)), BILLABILITY);

            assertThat(warnings).containsExactly(new WorkTimeWarning(DATE, INTERNAL_PREFIX_MISSING, null));
        }

        @ParameterizedTest
        @ValueSource(strings = {"Intern - Teammeeting", "intern-Teammeeting", "  Intern – Teammeeting"})
        void calculate_whenNoteCarriesPrefix_thenNoWarning(String note) {
            List<WorkTimeWarning> warnings = calculator.calculate(bookings(project(INTERNAL_PROJECT, note, 8)), BILLABILITY);

            assertThat(warnings).isEmpty();
        }

        @Test
        void calculate_whenJourneyOnInternalProjectCarriesNoPrefix_thenWarning() {
            List<WorkTimeWarning> warnings = calculator.calculate(bookings(journey(INTERNAL_PROJECT, "Anreise")), BILLABILITY);

            assertThat(warnings).containsExactly(new WorkTimeWarning(DATE, INTERNAL_PREFIX_MISSING, null));
        }
    }

    @Test
    void calculate_whenProjectIsUnknown_thenNoWarning() {
        List<WorkTimeWarning> warnings = calculator.calculate(bookings(
                project(UNKNOWN_PROJECT, "Intern - Teammeeting", 8),
                project(UNKNOWN_PROJECT, "Teammeeting", 10)), BILLABILITY);

        assertThat(warnings).isEmpty();
    }

    @Test
    void calculate_whenBookingHasNoProject_thenNoWarning() {
        List<WorkTimeWarning> warnings = calculator.calculate(bookings(project(null, "Teammeeting", 8)), BILLABILITY);

        assertThat(warnings).isEmpty();
    }

    @Test
    void calculate_whenBillabilityIsNull_thenRejected() {
        assertThatNullPointerException()
                .isThrownBy(() -> calculator.calculate(bookings(project(INTERNAL_PROJECT, "Teammeeting", 8)), null))
                .withMessageContaining("billableByProjectZepId");
    }

    @Test
    void calculate_whenBothViolationsOnOneDay_thenOneWarningEach() {
        List<WorkTimeWarning> warnings = calculator.calculate(bookings(
                project(INTERNAL_PROJECT, "Teammeeting", 8),
                project(BILLABLE_PROJECT, "Intern - Kickoff", 10),
                project(INTERNAL_PROJECT, "Doku", 12)), BILLABILITY);

        assertThat(warnings).containsExactlyInAnyOrder(
                new WorkTimeWarning(DATE, INTERNAL_PREFIX_MISSING, null),
                new WorkTimeWarning(DATE, INTERNAL_PREFIX_ON_BILLABLE_PROJECT, null));
    }

    @Test
    void calculate_whenViolationsOnDifferentDays_thenOneWarningPerDay() {
        ProjectBooking nextDay = projectBookingBuilder()
                .fromTime(DATE.plusDays(1).atTime(8, 0))
                .toTime(DATE.plusDays(1).atTime(9, 0))
                .task(Task.BEARBEITEN)
                .workingLocation(WorkingLocation.MAIN)
                .projectZepId(INTERNAL_PROJECT)
                .note("Teammeeting")
                .build();

        List<WorkTimeWarning> warnings = calculator.calculate(bookings(project(INTERNAL_PROJECT, "Teammeeting", 8), nextDay), BILLABILITY);

        assertThat(warnings).containsExactly(
                new WorkTimeWarning(DATE, INTERNAL_PREFIX_MISSING, null),
                new WorkTimeWarning(DATE.plusDays(1), INTERNAL_PREFIX_MISSING, null));
    }

    private static ProjectBooking project(Integer projectZepId, String note, int startHour) {
        return projectBookingBuilder()
                .fromTime(DATE.atTime(startHour, 0))
                .toTime(DATE.atTime(startHour + 1, 0))
                .task(Task.BEARBEITEN)
                .workingLocation(WorkingLocation.MAIN)
                .projectZepId(projectZepId)
                .note(note)
                .build();
    }

    private static JourneyBooking journey(Integer projectZepId, String note) {
        return journeyBookingBuilder()
                .fromTime(DATE.atTime(7, 0))
                .toTime(DATE.atTime(8, 0))
                .task(Task.REISEN)
                .workingLocation(WorkingLocation.A)
                .journeyDirection(JourneyDirection.TO)
                .vehicle(Vehicle.CAR_ACTIVE)
                .projectZepId(projectZepId)
                .note(note)
                .build();
    }
}
