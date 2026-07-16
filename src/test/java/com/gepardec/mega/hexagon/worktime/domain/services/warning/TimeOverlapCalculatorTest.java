package com.gepardec.mega.hexagon.worktime.domain.services.warning;

import com.gepardec.mega.hexagon.worktime.domain.model.ProjectBooking;
import com.gepardec.mega.hexagon.worktime.domain.model.Task;
import com.gepardec.mega.hexagon.worktime.domain.model.WorkTimeBooking;
import com.gepardec.mega.hexagon.worktime.domain.model.WorkTimeWarning;
import com.gepardec.mega.hexagon.worktime.domain.model.WorkingLocation;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class TimeOverlapCalculatorTest {

    private final TimeOverlapCalculator timeOverlapCalculator = new TimeOverlapCalculator();

    @Test
    void calculate_whenProjectEntriesWithoutOverlap_thenNoWarning() {
        List<WorkTimeBooking> projectEntries = generateProjectEntriesListWithoutOverlap();

        List<WorkTimeWarning> warnings = timeOverlapCalculator.calculate(new com.gepardec.mega.hexagon.worktime.domain.model.WorkTimeBookings(projectEntries));

        assertThat(warnings).isEmpty();
    }

    @Test
    void calculate_whenProjectEntriesStartAndEndOverlap_thenNoWarning() {
        List<WorkTimeBooking> projectEntries = generateProjectEntriesListWhereEndAndStartOverlap();

        List<WorkTimeWarning> warnings = timeOverlapCalculator.calculate(new com.gepardec.mega.hexagon.worktime.domain.model.WorkTimeBookings(projectEntries));

        assertThat(warnings).isEmpty();
    }

    @Test
    void calculate_whenProjectEntriesOverlap_thenWarning() {
        List<WorkTimeBooking> projectEntries = generateProjectEntriesWithOverlap();

        List<WorkTimeWarning> warnings = timeOverlapCalculator.calculate(new com.gepardec.mega.hexagon.worktime.domain.model.WorkTimeBookings(projectEntries));

        assertThat(warnings).isNotEmpty();
    }

    @Test
    void calculate_whenProjectEntriesOverlap_thenCorrectDate() {
        List<WorkTimeBooking> projectEntries = generateProjectEntriesWithOverlap();

        List<WorkTimeWarning> warnings = timeOverlapCalculator.calculate(new com.gepardec.mega.hexagon.worktime.domain.model.WorkTimeBookings(projectEntries));

        assertThat(warnings.getFirst().date()).isEqualTo(LocalDate.of(2020, 1, 7));
    }

    @Test
    void calculate_whenWorkTimeBookingListContainsOverlappingEntries_thenReturnsTheCorrectAmountOfWarnings() {
        WorkTimeBooking entryOne = projectTimeEntry(7, 8, 0, 12, 0);
        WorkTimeBooking entryTwo = projectTimeEntry(7, 8, 30, 10, 0);
        WorkTimeBooking entryThree = projectTimeEntry(8, 8, 0, 12, 0);
        WorkTimeBooking entryFour = projectTimeEntry(9, 8, 0, 12, 0);
        WorkTimeBooking entryFive = projectTimeEntry(9, 9, 0, 10, 0);

        List<WorkTimeBooking> projectEntries = new ArrayList<>();
        projectEntries.add(entryOne);
        projectEntries.add(entryTwo);
        projectEntries.add(entryThree);
        projectEntries.add(entryFour);
        projectEntries.add(entryFive);

        List<WorkTimeWarning> warnings = timeOverlapCalculator.calculate(new com.gepardec.mega.hexagon.worktime.domain.model.WorkTimeBookings(projectEntries));

        assertThat(warnings).hasSize(2);
    }

    private ProjectBooking projectTimeEntry(int day, int startHour, int startMinute, int endHour, int endMinute) {
        return WarningTestBookingBuilder.projectBookingBuilder()
                .fromTime(LocalDateTime.of(2020, 1, day, startHour, startMinute))
                .toTime(LocalDateTime.of(2020, 1, day, endHour, endMinute))
                .task(Task.BEARBEITEN)
                .workingLocation(WorkingLocation.MAIN)
                .build();
    }

    private List<WorkTimeBooking> generateProjectEntriesListWithoutOverlap() {
        List<WorkTimeBooking> projectEntries = new ArrayList<>();
        projectEntries.add(projectTimeEntry(7, 8, 0, 11, 30));
        projectEntries.add(projectTimeEntry(7, 12, 0, 16, 30));
        projectEntries.add(projectTimeEntry(8, 8, 0, 11, 30));
        projectEntries.add(projectTimeEntry(8, 12, 0, 16, 30));

        return projectEntries;
    }

    private List<WorkTimeBooking> generateProjectEntriesListWhereEndAndStartOverlap() {
        List<WorkTimeBooking> projectEntries = new ArrayList<>();
        projectEntries.add(projectTimeEntry(7, 8, 0, 11, 30));
        projectEntries.add(projectTimeEntry(7, 11, 30, 16, 30));

        return projectEntries;
    }

    private List<WorkTimeBooking> generateProjectEntriesWithOverlap() {
        List<WorkTimeBooking> projectEntries = new ArrayList<>();
        projectEntries.add(projectTimeEntry(7, 8, 0, 11, 30));
        projectEntries.add(projectTimeEntry(7, 12, 0, 16, 30));
        projectEntries.add(projectTimeEntry(7, 8, 0, 11, 0));
        projectEntries.add(projectTimeEntry(7, 12, 0, 16, 30));

        return projectEntries;
    }
}
