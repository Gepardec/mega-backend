package com.gepardec.mega.hexagon.worktime.domain.model;

import org.instancio.Instancio;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNullPointerException;
import static org.instancio.Select.field;

class WorkTimeBookingsTest {

    private static final LocalDate MONDAY = LocalDate.of(2026, 7, 13);

    @Test
    void shouldDefensivelyCopyAndStablySortBookingsChronologically() {
        ProjectBooking firstWithSameTimes = project(MONDAY.atTime(8, 0), MONDAY.atTime(9, 0));
        JourneyBooking secondWithSameTimes = journey(MONDAY.atTime(8, 0), MONDAY.atTime(9, 0), Vehicle.CAR_ACTIVE);
        ProjectBooking last = project(MONDAY.atTime(10, 0), MONDAY.atTime(11, 0));
        List<WorkTimeBooking> source = new ArrayList<>(List.of(last, firstWithSameTimes, secondWithSameTimes));

        WorkTimeBookings bookings = new WorkTimeBookings(source);
        source.clear();

        assertThat(bookings.values()).isUnmodifiable().containsExactly(firstWithSameTimes, secondWithSameTimes, last);
        assertThat(bookings).isEqualTo(new WorkTimeBookings(List.of(firstWithSameTimes, secondWithSameTimes, last)));
    }

    @Test
    void shouldRejectNullCollectionAndElementsAndSupportEmptyConstruction() {
        assertThatNullPointerException().isThrownBy(() -> new WorkTimeBookings(null));
        assertThatNullPointerException().isThrownBy(() -> new WorkTimeBookings(Arrays.asList((WorkTimeBooking) null)));
        assertThat(WorkTimeBookings.empty()).isEqualTo(new WorkTimeBookings(List.of()));
        assertThat(WorkTimeBookings.empty().isEmpty()).isTrue();
    }

    @Test
    void shouldReportEmptyAggregatesWhenNoBookingsArePresent() {
        WorkTimeBookings empty = WorkTimeBookings.empty();

        assertThat(empty.values()).isEmpty();
        assertThat(empty.bookedDates()).isEmpty();
        assertThat(empty.byDate()).isEmpty();
        assertThat(empty.projects()).isEmpty();
        assertThat(empty.journeys()).isEmpty();
        assertThat(empty.contributingToWorkingTime().values()).isEmpty();
        assertThat(empty.totalDuration()).isEqualTo(Duration.ZERO);
    }

    @Test
    void shouldProvideOrderedImmutableDomainViewsAndExactDuration() {
        ProjectBooking tuesdayProject = project(MONDAY.plusDays(1).atTime(9, 0), MONDAY.plusDays(1).atTime(10, 15));
        JourneyBooking inactiveJourney = journey(MONDAY.atTime(7, 0), MONDAY.atTime(7, 30), Vehicle.CAR_INACTIVE);
        JourneyBooking activeJourney = journey(MONDAY.atTime(7, 30), MONDAY.atTime(8, 15), Vehicle.CAR_ACTIVE);
        ProjectBooking mondayProject = project(MONDAY.atTime(8, 15), MONDAY.atTime(10, 0));

        WorkTimeBookings bookings = new WorkTimeBookings(List.of(tuesdayProject, mondayProject, activeJourney, inactiveJourney));

        assertThat(bookings.byDate().keySet()).isUnmodifiable().containsExactly(MONDAY, MONDAY.plusDays(1));
        assertThat(bookings.byDate().get(MONDAY).values()).containsExactly(inactiveJourney, activeJourney, mondayProject);
        assertThat(bookings.projects()).isUnmodifiable().containsExactly(mondayProject, tuesdayProject);
        assertThat(bookings.journeys()).isUnmodifiable().containsExactly(inactiveJourney, activeJourney);
        assertThat(bookings.contributingToWorkingTime().values()).containsExactly(activeJourney, mondayProject, tuesdayProject);
        assertThat(bookings.bookedDates()).isUnmodifiable().containsExactly(MONDAY, MONDAY.plusDays(1));
        assertThat(bookings.totalDuration()).isEqualTo(Duration.ofHours(4).plusMinutes(15));
    }

    private ProjectBooking project(LocalDateTime from, LocalDateTime to) {
        return Instancio.of(ProjectBooking.class)
                .set(field(ProjectBooking::from), from)
                .set(field(ProjectBooking::to), to)
                .set(field(ProjectBooking::task), Task.BEARBEITEN)
                .create();
    }

    private JourneyBooking journey(LocalDateTime from, LocalDateTime to, Vehicle vehicle) {
        return Instancio.of(JourneyBooking.class)
                .set(field(JourneyBooking::from), from)
                .set(field(JourneyBooking::to), to)
                .set(field(JourneyBooking::task), Task.REISEN)
                .set(field(JourneyBooking::vehicle), vehicle)
                .create();
    }
}
