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

import static com.gepardec.mega.hexagon.worktime.domain.model.WorkTimeWarningType.TRAVEL_ALLOWANCE_EXCEEDED;
import static com.gepardec.mega.hexagon.worktime.domain.model.WorkTimeWarningType.TRAVEL_ALLOWANCE_WRONG_PROJECT;
import static com.gepardec.mega.hexagon.worktime.domain.services.warning.WarningTestBookingBuilder.bookings;
import static com.gepardec.mega.hexagon.worktime.domain.services.warning.WarningTestBookingBuilder.journeyBookingBuilder;
import static com.gepardec.mega.hexagon.worktime.domain.services.warning.WarningTestBookingBuilder.projectBookingBuilder;
import static org.assertj.core.api.Assertions.assertThat;
import static org.instancio.Select.field;

class TravelAllowanceCalculatorTest {
    private static final LocalDate DATE = LocalDate.of(2026, 10, 5);
    private static final int INTERN = 1;
    private static final int CUSTOMER = 10;
    private static final Set<Integer> BILLABLE_PROJECTS = Set.of(CUSTOMER);

    private TravelAllowanceCalculator calculator;

    @BeforeEach
    void setUp() {
        calculator = new TravelAllowanceCalculator();
    }

    @Test
    void calculate_whenAllowanceWithinAllowedOfRoundTrip_thenNoWarning() {
        assertThat(calculate(bookings(
                passive(DATE, 6, "Wien", "Linz", INTERN), passive(DATE, 16, "Linz", "Wien", INTERN),
                allowance(DATE, 60)), BILLABLE_PROJECTS)).isEmpty();
    }

    @Test
    void calculate_whenAllowanceExceedsShortRoute_thenExceededWithExcessHours() {
        assertThat(calculate(bookings(
                passive(DATE, 6, "St. Pölten", "Wien", INTERN), allowance(DATE, 30)), BILLABLE_PROJECTS))
                .containsExactly(new WorkTimeWarning(DATE, TRAVEL_ALLOWANCE_EXCEEDED, 0.25));
    }

    @Test
    void calculate_whenNoJourney_thenExceededWithFullAllowance() {
        assertThat(calculate(bookings(allowance(DATE, 30)), BILLABLE_PROJECTS))
                .containsExactly(new WorkTimeWarning(DATE, TRAVEL_ALLOWANCE_EXCEEDED, 0.5));
    }

    @Test
    void calculate_whenOnlyActiveJourneys_thenExceeded() {
        assertThat(calculate(bookings(
                journey(DATE, 6, Vehicle.CAR_ACTIVE, "Wien", "Linz", INTERN), allowance(DATE, 30)), BILLABLE_PROJECTS))
                .containsExactly(new WorkTimeWarning(DATE, TRAVEL_ALLOWANCE_EXCEEDED, 0.5));
    }

    @Test
    void calculate_whenActiveReturnJourney_thenOnlyPassiveJourneyCounts() {
        assertThat(calculate(bookings(
                passive(DATE, 6, "Wien", "Linz", INTERN),
                journey(DATE, 16, Vehicle.CAR_ACTIVE, "Linz", "Wien", INTERN),
                allowance(DATE, 30)), BILLABLE_PROJECTS)).isEmpty();
    }

    @Test
    void calculate_whenOnwardJourney_thenAllowancesAdd() {
        assertThat(calculate(bookings(
                passive(DATE, 6, "Wien", "Linz", INTERN), passive(DATE, 10, "Linz", "St. Pölten", INTERN),
                allowance(DATE, 45)), BILLABLE_PROJECTS)).isEmpty();
        assertThat(calculate(bookings(
                passive(DATE, 6, "Wien", "Linz", INTERN), passive(DATE, 10, "Linz", "St. Pölten", INTERN),
                allowance(DATE, 60)), BILLABLE_PROJECTS))
                .containsExactly(new WorkTimeWarning(DATE, TRAVEL_ALLOWANCE_EXCEEDED, 0.25));
    }

    @Test
    void calculate_whenMultiDayTrip_thenEachDayIsJudgedOnItsOwn() {
        LocalDate back = DATE.plusDays(2);
        assertThat(calculate(bookings(
                passive(DATE, 6, "Wien", "Linz", INTERN), passive(back, 16, "Linz", "Wien", INTERN),
                allowance(DATE, 30), allowance(DATE.plusDays(1), 30), allowance(back, 30)), BILLABLE_PROJECTS))
                .containsExactly(new WorkTimeWarning(DATE.plusDays(1), TRAVEL_ALLOWANCE_EXCEEDED, 0.5));
    }

    @Test
    void calculate_whenSeveralAllowanceBookings_thenTheyAddUp() {
        assertThat(calculate(bookings(allowance(DATE, 15), allowance(DATE, 15)), BILLABLE_PROJECTS))
                .containsExactly(new WorkTimeWarning(DATE, TRAVEL_ALLOWANCE_EXCEEDED, 0.5));
    }

    @Test
    void calculate_whenBookingOnOtherProcess_thenNoWarning() {
        assertThat(calculate(bookings(project(DATE, "233", 30, CUSTOMER)), BILLABLE_PROJECTS)).isEmpty();
    }

    @Test
    void calculate_whenJourneyOnUnknownProjectOrWithoutProject_thenInternalTrip() {
        assertThat(calculate(bookings(
                passive(DATE, 6, "Wien", "Linz", 99), allowance(DATE, 30)), BILLABLE_PROJECTS)).isEmpty();
        assertThat(calculate(bookings(
                passive(DATE, 6, "Wien", "Linz", null), allowance(DATE, 45)), BILLABLE_PROJECTS))
                .containsExactly(new WorkTimeWarning(DATE, TRAVEL_ALLOWANCE_EXCEEDED, 0.25));
    }

    @Test
    void calculate_whenCustomerTripAndAllowanceOnIntern_thenWrongProjectOnly() {
        assertThat(calculate(bookings(
                passive(DATE, 6, "Wien", "Linz", CUSTOMER), allowance(DATE, 30)), BILLABLE_PROJECTS))
                .containsExactly(new WorkTimeWarning(DATE, TRAVEL_ALLOWANCE_WRONG_PROJECT, null));
    }

    @Test
    void calculate_whenInternalAndCustomerTrip_thenCustomerTripDoesNotAddToAllowance() {
        assertThat(calculate(bookings(
                passive(DATE, 6, "Wien", "Linz", INTERN), passive(DATE, 16, "Linz", "Wien", CUSTOMER),
                allowance(DATE, 60)), BILLABLE_PROJECTS))
                .containsExactly(new WorkTimeWarning(DATE, TRAVEL_ALLOWANCE_EXCEEDED, 0.5));
    }

    @Test
    void calculate_whenCustomerTripWithoutAllowanceBooking_thenNoWarning() {
        assertThat(calculate(bookings(passive(DATE, 6, "Wien", "Linz", CUSTOMER)), BILLABLE_PROJECTS)).isEmpty();
    }

    private static WorkTimeBooking allowance(LocalDate date, int minutes) {
        return project(date, "873", minutes, INTERN);
    }

    private static WorkTimeBooking project(LocalDate date, String process, int minutes, Integer projectZepId) {
        return projectBookingBuilder()
                .fromTime(date.atTime(12, 0)).toTime(date.atTime(12, 0).plusMinutes(minutes))
                .task(Task.BEARBEITEN).workingLocation(WorkingLocation.MAIN)
                .process(process).projectZepId(projectZepId)
                .build();
    }

    private static WorkTimeBooking passive(LocalDate date, int hour, String start, String destination, Integer projectZepId) {
        return journey(date, hour, Vehicle.CAR_INACTIVE, start, destination, projectZepId);
    }

    private static WorkTimeBooking journey(LocalDate date, int hour, Vehicle vehicle, String start, String destination, Integer projectZepId) {
        return journeyBookingBuilder()
                .fromTime(date.atTime(hour, 0)).toTime(date.atTime(hour + 1, 0))
                .task(Task.REISEN).workingLocation(WorkingLocation.MAIN)
                .journeyDirection(JourneyDirection.TO).vehicle(vehicle)
                .start(start).destination(destination).projectZepId(projectZepId)
                .build();
    }

    private List<WorkTimeWarning> calculate(WorkTimeBookings bookings, Set<Integer> billableProjectZepIds) {
        return calculator.calculate(Instancio.of(WorkTimeEvaluation.class)
                .set(field(WorkTimeEvaluation::bookings), bookings)
                .set(field(WorkTimeEvaluation::billableProjectZepIds), billableProjectZepIds)
                .create());
    }
}
