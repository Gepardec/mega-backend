package com.gepardec.mega.hexagon.worktime.domain.services.warning;

import com.gepardec.mega.hexagon.worktime.domain.model.JourneyBooking;
import com.gepardec.mega.hexagon.worktime.domain.model.JourneyDirection;
import com.gepardec.mega.hexagon.worktime.domain.model.ProjectBooking;
import com.gepardec.mega.hexagon.worktime.domain.model.Task;
import com.gepardec.mega.hexagon.worktime.domain.model.Vehicle;
import com.gepardec.mega.hexagon.worktime.domain.model.WorkTimeBooking;
import com.gepardec.mega.hexagon.worktime.domain.model.WorkTimeWarningType;
import com.gepardec.mega.hexagon.worktime.domain.model.WorkingLocation;
import com.gepardec.mega.hexagon.worktime.domain.model.WorkTimeBookings;
import com.gepardec.mega.hexagon.worktime.domain.model.WorkTimeWarning;
import org.instancio.Instancio;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Stream;

import static com.gepardec.mega.hexagon.worktime.domain.services.warning.WarningTestBookingBuilder.bookings;
import static com.gepardec.mega.hexagon.worktime.domain.services.warning.WarningTestBookingBuilder.journeyBookingBuilder;
import static com.gepardec.mega.hexagon.worktime.domain.services.warning.WarningTestBookingBuilder.projectBookingBuilder;
import static org.assertj.core.api.Assertions.assertThat;
import static org.instancio.Select.field;

class WorkTimeWarningCalculatorsTest {
    private static final LocalDate DATE = LocalDate.of(2026, 5, 4);
    private static final LocalDate HOLIDAY = LocalDate.of(2026, 5, 1);
    private static final LocalDate SATURDAY = LocalDate.of(2026, 5, 2);
    private static final LocalDate MONDAY = LocalDate.of(2026, 5, 4);
    private static final LocalDate TUESDAY = LocalDate.of(2026, 5, 5);

    @Test
    void coreWorkingHours_preservesOutsideCoreHoursRule() {
        assertType(new CoreWorkingHoursCalculator(), List.of(project(DATE.atTime(5, 0), DATE.atTime(8, 0))),
                WorkTimeWarningType.OUTSIDE_CORE_WORKING_TIME);
    }

    @Test
    void timeOverlap_preservesOverlapRule() {
        assertType(new TimeOverlapCalculator(), List.of(
                project(DATE.atTime(8, 0), DATE.atTime(12, 0)),
                project(DATE.atTime(11, 0), DATE.atTime(13, 0))), WorkTimeWarningType.TIME_OVERLAP);
    }

    @Test
    void holiday_preservesHolidayRule() {
        LocalDate holiday = LocalDate.of(2026, 5, 1);
        assertType(new HolidayCalculator(), List.of(project(holiday.atTime(8, 0), holiday.atTime(9, 0))),
                WorkTimeWarningType.HOLIDAY);
    }

    @Test
    void weekend_preservesWeekendRule() {
        LocalDate saturday = LocalDate.of(2026, 5, 2);
        assertType(new WeekendCalculator(), List.of(project(saturday.atTime(8, 0), saturday.atTime(9, 0))),
                WorkTimeWarningType.WEEKEND);
    }

    @Test
    void doctorAppointment_preservesAllowedWindowRule() {
        ProjectBooking booking = Instancio.of(ProjectBooking.class)
                .set(field(ProjectBooking::from), DATE.atTime(7, 0))
                .set(field(ProjectBooking::to), DATE.atTime(8, 0))
                .set(field(ProjectBooking::task), Task.BEARBEITEN)
                .set(field(ProjectBooking::workingLocation), WorkingLocation.MAIN)
                .set(field(ProjectBooking::process), "233")
                .create();
        assertType(new DoctorAppointmentCalculator(), List.of(booking), WorkTimeWarningType.WRONG_DOCTOR_APPOINTMENT);
    }

    @Test
    void maximumHours_preservesQuantifiedExcessRule() {
        var warning = new ExceededMaximumWorkingHoursPerDayCalculator()
                .calculate(bookings(project(DATE.atTime(7, 0), DATE.atTime(18, 0)))).getFirst();
        assertThat(warning.type()).isEqualTo(WorkTimeWarningType.EXCESS_WORKING_TIME_PRESENT);
        assertThat(warning.hours()).isEqualTo(1d);
    }

    @Test
    void insufficientBreak_preservesQuantifiedBreakRule() {
        var warning = new InsufficientBreakCalculator()
                .calculate(bookings(project(DATE.atTime(8, 0), DATE.atTime(14, 1)))).getFirst();
        assertThat(warning.type()).isEqualTo(WorkTimeWarningType.MISSING_BREAK_TIME);
        assertThat(warning.hours()).isEqualTo(0.5d);
    }

    @Test
    void insufficientRest_preservesQuantifiedRestRule() {
        var warning = new InsufficientRestCalculator().calculate(bookings(
                project(DATE.atTime(14, 0), DATE.atTime(22, 0)),
                project(DATE.plusDays(1).atTime(8, 0), DATE.plusDays(1).atTime(9, 0)))).getFirst();
        assertThat(warning.type()).isEqualTo(WorkTimeWarningType.MISSING_REST_TIME);
        assertThat(warning.hours()).isEqualTo(1d);
    }

    @Test
    void invalidJourney_preservesMissingReturnRule() {
        assertType(new InvalidJourneyCalculator(), List.of(journey(DATE.atTime(8, 0), DATE.atTime(9, 0), JourneyDirection.TO)),
                WorkTimeWarningType.BACK_MISSING);
    }

    @Test
    void invalidWorkingLocation_preservesJourneyLocationRule() {
        assertType(new InvalidWorkingLocationInJourneyCalculator(), List.of(
                        journey(DATE.atTime(8, 0), DATE.atTime(9, 0), JourneyDirection.TO),
                        projectAt(DATE.atTime(9, 0), DATE.atTime(10, 0), WorkingLocation.MAIN, false)),
                WorkTimeWarningType.INVALID_WORKING_LOCATION);
    }

    @Test
    void locationRelevant_preservesProjectRelevantRule() {
        assertType(new LocationRelevantSetJourneyCalculator(), List.of(
                        projectAt(DATE.atTime(8, 0), DATE.atTime(9, 0), WorkingLocation.MAIN, true)),
                WorkTimeWarningType.LOCATION_RELEVANT_SET);
    }

    @ParameterizedTest
    @MethodSource("bookingBasedCalculators")
    void calculator_producesIdenticalWarningsRegardlessOfInputOrder(WorkTimeWarningCalculator calculator) {
        List<WorkTimeWarning> fromChronologicalInput = calculator.calculate(new WorkTimeBookings(chronologicalMonth()));
        List<WorkTimeWarning> fromScrambledInput = calculator.calculate(new WorkTimeBookings(scrambledMonth()));

        assertThat(fromScrambledInput).containsExactlyElementsOf(fromChronologicalInput);
    }

    @Test
    void orderingFixture_triggersEnoughRulesToBeMeaningful() {
        List<WorkTimeWarning> warnings = bookingBasedCalculators()
                .flatMap(calculator -> calculator.calculate(new WorkTimeBookings(scrambledMonth())).stream())
                .toList();

        assertThat(warnings).extracting(WorkTimeWarning::type).doesNotContainNull();
        assertThat(warnings.stream().map(WorkTimeWarning::type).distinct().toList()).hasSizeGreaterThanOrEqualTo(6);
    }

    private static Stream<WorkTimeWarningCalculator> bookingBasedCalculators() {
        return Stream.of(
                new CoreWorkingHoursCalculator(),
                new TimeOverlapCalculator(),
                new HolidayCalculator(),
                new WeekendCalculator(),
                new DoctorAppointmentCalculator(),
                new ExceededMaximumWorkingHoursPerDayCalculator(),
                new InsufficientRestCalculator(),
                new InsufficientBreakCalculator(),
                new InvalidJourneyCalculator(),
                new InvalidWorkingLocationInJourneyCalculator(),
                new LocationRelevantSetJourneyCalculator());
    }

    private static List<WorkTimeBooking> chronologicalMonth() {
        return List.of(
                holidayMorning(), holidayOverlap(), weekendOverlongDay(),
                journeyOut(), doctorAppointment(), relevantLocationWork(), journeyBack(), nextMorning());
    }

    /**
     * The same bookings in an order no provider guarantees: days interleaved and each day's
     * bookings reversed. Fixed by hand so the expectation is reproducible.
     */
    private static List<WorkTimeBooking> scrambledMonth() {
        return List.of(
                nextMorning(), journeyBack(), holidayOverlap(), relevantLocationWork(),
                weekendOverlongDay(), doctorAppointment(), holidayMorning(), journeyOut());
    }

    private static ProjectBooking holidayMorning() {
        return projectBookingBuilder().fromTime(HOLIDAY.atTime(5, 0)).toTime(HOLIDAY.atTime(12, 0))
                .task(Task.BEARBEITEN).workingLocation(WorkingLocation.MAIN).process("1033").build();
    }

    private static ProjectBooking holidayOverlap() {
        return projectBookingBuilder().fromTime(HOLIDAY.atTime(11, 0)).toTime(HOLIDAY.atTime(13, 0))
                .task(Task.BEARBEITEN).workingLocation(WorkingLocation.MAIN).process("1033").build();
    }

    private static ProjectBooking weekendOverlongDay() {
        return projectBookingBuilder().fromTime(SATURDAY.atTime(8, 0)).toTime(SATURDAY.atTime(19, 0))
                .task(Task.BEARBEITEN).workingLocation(WorkingLocation.MAIN).process("1033").build();
    }

    private static JourneyBooking journeyOut() {
        return journeyBookingBuilder().fromTime(MONDAY.atTime(6, 0)).toTime(MONDAY.atTime(6, 30))
                .task(Task.REISEN).workingLocation(WorkingLocation.A)
                .journeyDirection(JourneyDirection.TO).vehicle(Vehicle.CAR_ACTIVE).build();
    }

    private static ProjectBooking doctorAppointment() {
        return projectBookingBuilder().fromTime(MONDAY.atTime(7, 30)).toTime(MONDAY.atTime(8, 0))
                .task(Task.BEARBEITEN).workingLocation(WorkingLocation.MAIN).process("233").build();
    }

    private static ProjectBooking relevantLocationWork() {
        return projectBookingBuilder().fromTime(MONDAY.atTime(9, 0)).toTime(MONDAY.atTime(17, 0))
                .task(Task.BEARBEITEN).workingLocation(WorkingLocation.A)
                .workLocationIsProjectRelevant(true).process("1033").build();
    }

    private static JourneyBooking journeyBack() {
        return journeyBookingBuilder().fromTime(MONDAY.atTime(21, 0)).toTime(MONDAY.atTime(23, 0))
                .task(Task.REISEN).workingLocation(WorkingLocation.MAIN)
                .journeyDirection(JourneyDirection.BACK).vehicle(Vehicle.CAR_ACTIVE).build();
    }

    private static ProjectBooking nextMorning() {
        return projectBookingBuilder().fromTime(TUESDAY.atTime(5, 0)).toTime(TUESDAY.atTime(6, 0))
                .task(Task.BEARBEITEN).workingLocation(WorkingLocation.MAIN).process("1033").build();
    }

    private void assertType(WorkTimeWarningCalculator calculator, List<WorkTimeBooking> bookings, WorkTimeWarningType type) {
        assertThat(calculator.calculate(new WorkTimeBookings(bookings)))
                .singleElement().extracting("type").isEqualTo(type);
    }

    private static ProjectBooking project(LocalDateTime from, LocalDateTime to) {
        return projectAt(from, to, WorkingLocation.MAIN, false);
    }

    private static ProjectBooking projectAt(LocalDateTime from, LocalDateTime to, WorkingLocation location, boolean relevant) {
        return Instancio.of(ProjectBooking.class)
                .set(field(ProjectBooking::from), from)
                .set(field(ProjectBooking::to), to)
                .set(field(ProjectBooking::task), Task.BEARBEITEN)
                .set(field(ProjectBooking::workingLocation), location)
                .set(field(ProjectBooking::workLocationProjectRelevant), relevant)
                .create();
    }

    private static JourneyBooking journey(LocalDateTime from, LocalDateTime to, JourneyDirection direction) {
        return Instancio.of(JourneyBooking.class)
                .set(field(JourneyBooking::from), from)
                .set(field(JourneyBooking::to), to)
                .set(field(JourneyBooking::task), Task.REISEN)
                .set(field(JourneyBooking::workingLocation), WorkingLocation.A)
                .set(field(JourneyBooking::direction), direction)
                .set(field(JourneyBooking::vehicle), Vehicle.CAR_ACTIVE)
                .create();
    }
}
