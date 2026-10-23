package com.gepardec.mega.hexagon.worktime.domain.services;

import com.gepardec.mega.hexagon.worktime.domain.model.JourneyBooking;
import com.gepardec.mega.hexagon.worktime.domain.model.JourneyDirection;
import org.instancio.Instancio;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.Set;

import static com.gepardec.mega.hexagon.worktime.domain.model.JourneyDirection.BACK;
import static com.gepardec.mega.hexagon.worktime.domain.model.JourneyDirection.FURTHER;
import static com.gepardec.mega.hexagon.worktime.domain.model.JourneyDirection.TO;
import static org.assertj.core.api.Assertions.assertThat;
import static org.instancio.Select.field;

class TripDaysTest {

    private static final YearMonth OCTOBER = YearMonth.of(2026, 10);
    private static final YearMonth NOVEMBER = YearMonth.of(2026, 11);

    @Test
    void of_whenNoJourneys_thenNoTripDays() {
        assertThat(TripDays.of(List.of(), OCTOBER)).isEmpty();
    }

    @Test
    void of_whenDayTrip_thenOnlyThatDayIsTripDay() {
        Set<LocalDate> result = TripDays.of(List.of(journey(TO, 14), journey(BACK, 14)), OCTOBER);

        assertThat(result).containsExactly(date(OCTOBER, 14));
    }

    @Test
    void of_whenOutboundAndReturnOnDifferentDays_thenDaysBetweenAreTripDays() {
        Set<LocalDate> result = TripDays.of(List.of(journey(TO, 12), journey(BACK, 15)), OCTOBER);

        assertThat(result).containsExactly(date(OCTOBER, 12), date(OCTOBER, 13), date(OCTOBER, 14), date(OCTOBER, 15));
    }

    @Test
    void of_whenOnwardJourneyInBetween_thenTripContinues() {
        Set<LocalDate> result = TripDays.of(
                List.of(journey(TO, 12), journey(FURTHER, 13), journey(BACK, 15)), OCTOBER);

        assertThat(result).containsExactly(date(OCTOBER, 12), date(OCTOBER, 13), date(OCTOBER, 14), date(OCTOBER, 15));
    }

    @Test
    void of_whenTwoTrips_thenDaysBetweenTheTripsAreNotTripDays() {
        Set<LocalDate> result = TripDays.of(
                List.of(journey(TO, 5), journey(BACK, 6), journey(TO, 8), journey(BACK, 9)), OCTOBER);

        assertThat(result).containsExactly(date(OCTOBER, 5), date(OCTOBER, 6), date(OCTOBER, 8), date(OCTOBER, 9));
        assertThat(result).doesNotContain(date(OCTOBER, 7));
    }

    @Test
    void of_whenReturnJourneyInMiddleWithoutOutbound_thenOnlyItsOwnDayIsTripDay() {
        Set<LocalDate> result = TripDays.of(
                List.of(journey(TO, 5), journey(BACK, 6), journey(BACK, 20)), OCTOBER);

        assertThat(result).containsExactly(date(OCTOBER, 5), date(OCTOBER, 6), date(OCTOBER, 20));
    }

    @Test
    void of_whenOnwardJourneyInMiddleWithoutOutbound_thenOnlyItsOwnDayIsTripDay() {
        Set<LocalDate> result = TripDays.of(
                List.of(journey(TO, 5), journey(BACK, 6), journey(FURTHER, 20)), OCTOBER);

        assertThat(result).containsExactly(date(OCTOBER, 5), date(OCTOBER, 6), date(OCTOBER, 20));
    }

    @Test
    void of_whenLastTripIsOpen_thenRunsUntilEndOfMonth() {
        Set<LocalDate> result = TripDays.of(List.of(journey(TO, 29)), OCTOBER);

        assertThat(result).containsExactly(date(OCTOBER, 29), date(OCTOBER, 30), date(OCTOBER, 31));
    }

    @Test
    void of_whenLastTripHasOnwardJourneyAndIsOpen_thenRunsUntilEndOfMonth() {
        Set<LocalDate> result = TripDays.of(List.of(journey(TO, 27), journey(FURTHER, 28)), OCTOBER);

        assertThat(result).containsExactly(
                date(OCTOBER, 27), date(OCTOBER, 28), date(OCTOBER, 29), date(OCTOBER, 30), date(OCTOBER, 31));
    }

    @Test
    void of_whenFirstJourneyIsReturn_thenTripStartsOnFirstOfMonth() {
        Set<LocalDate> result = TripDays.of(List.of(journey(NOVEMBER, BACK, 3)), NOVEMBER);

        assertThat(result).containsExactly(date(NOVEMBER, 1), date(NOVEMBER, 2), date(NOVEMBER, 3));
    }

    @Test
    void of_whenFirstJourneyIsOnwardFollowedByReturn_thenTripStartsOnFirstOfMonth() {
        Set<LocalDate> result = TripDays.of(List.of(journey(NOVEMBER, FURTHER, 2), journey(NOVEMBER, BACK, 4)), NOVEMBER);

        assertThat(result).containsExactly(date(NOVEMBER, 1), date(NOVEMBER, 2), date(NOVEMBER, 3), date(NOVEMBER, 4));
    }

    @Test
    void of_whenFirstJourneyIsOnwardAndNeverClosed_thenRunsFromFirstToEndOfMonth() {
        Set<LocalDate> result = TripDays.of(List.of(journey(NOVEMBER, FURTHER, 2)), NOVEMBER);

        assertThat(result).hasSize(30).contains(date(NOVEMBER, 1), date(NOVEMBER, 30));
    }

    @Test
    void of_whenTripIsInterruptedByNewOutbound_thenOnlyJourneyDaysOfInterruptedTripCount() {
        Set<LocalDate> result = TripDays.of(
                List.of(journey(TO, 5), journey(TO, 12), journey(BACK, 13)), OCTOBER);

        assertThat(result).containsExactly(date(OCTOBER, 5), date(OCTOBER, 12), date(OCTOBER, 13));
    }

    private static JourneyBooking journey(JourneyDirection direction, int dayOfMonth) {
        return journey(OCTOBER, direction, dayOfMonth);
    }

    private static JourneyBooking journey(YearMonth month, JourneyDirection direction, int dayOfMonth) {
        LocalDate date = month.atDay(dayOfMonth);
        return Instancio.of(JourneyBooking.class)
                .set(field(JourneyBooking::from), date.atTime(8, 0))
                .set(field(JourneyBooking::to), date.atTime(9, 0))
                .set(field(JourneyBooking::direction), direction)
                .create();
    }

    private static LocalDate date(YearMonth month, int dayOfMonth) {
        return month.atDay(dayOfMonth);
    }
}
