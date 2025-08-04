package com.gepardec.mega.domain.calculation.journey;

import com.gepardec.mega.domain.model.Role;
import com.gepardec.mega.domain.model.User;
import com.gepardec.mega.domain.model.UserContext;
import com.gepardec.mega.domain.model.monthlyreport.JourneyDirection;
import com.gepardec.mega.domain.model.monthlyreport.JourneyTimeEntry;
import com.gepardec.mega.domain.model.monthlyreport.JourneyWarning;
import com.gepardec.mega.domain.model.monthlyreport.JourneyWarningType;
import com.gepardec.mega.domain.model.monthlyreport.ProjectEntry;
import com.gepardec.mega.domain.model.monthlyreport.ProjectTimeEntry;
import com.gepardec.mega.domain.model.monthlyreport.Task;
import com.gepardec.mega.domain.model.monthlyreport.Vehicle;
import com.gepardec.mega.domain.model.monthlyreport.WorkingLocation;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

class InvalidWorkingLocationInJourneyCalculatorTest {

    @InjectMocks
    private InvalidWorkingLocationInJourneyCalculator calculator;

    @Mock
    UserContext userContext;

    @BeforeEach
    void init() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void getAllWarningsForEmployeeAndMonth_whenInvalidJourneyWorkingLocation_thenGetError() {
        User user = createUserForRole(Role.EMPLOYEE);
        when(userContext.getUser()).thenReturn(user);

        List<ProjectEntry> projectEntries = createProjectEntryListForRequestForJourney();

        List<JourneyWarning> actual = calculator.calculate(projectEntries);

        assertThat(actual)
                .hasSize(1)
                .flatExtracting(entry -> entry.getWarningTypes())
                .containsExactly(JourneyWarningType.INVALID_WORKING_LOCATION);
    }

    private List<ProjectEntry> createProjectEntryListForRequestForJourney() {
        List<ProjectEntry> projectEntries = new ArrayList<>();

        //departure day
        projectEntries.add(createProjectTimeEntry(
                LocalDateTime.of(2025, 7, 1, 8, 0),
                LocalDateTime.of(2025, 7, 1, 9, 45),
                Task.BEARBEITEN, WorkingLocation.MAIN, "1033"));

        projectEntries.add(createJourneyTimeEntry(
                LocalDateTime.of(2025, 7, 1, 10, 15),
                LocalDateTime.of(2025, 7, 1, 10, 45),
                WorkingLocation.A, JourneyDirection.TO));

        projectEntries.add(createProjectTimeEntry(
                LocalDateTime.of(2025, 7, 1, 11, 0),
                LocalDateTime.of(2025, 7, 1, 11, 30),
                Task.BEARBEITEN, WorkingLocation.A, "1033"));

        projectEntries.add(createProjectTimeEntry(
                LocalDateTime.of(2025, 7, 1, 12, 0),
                LocalDateTime.of(2025, 7, 1, 16, 30),
                Task.BEARBEITEN, WorkingLocation.A, "1033"));

        //Tag1 Away
        projectEntries.add(createProjectTimeEntry(
                LocalDateTime.of(2025, 7, 2, 8, 0),
                LocalDateTime.of(2025, 7, 1, 11, 30),
                Task.BEARBEITEN, WorkingLocation.MAIN, "1033"));

        projectEntries.add(createProjectTimeEntry(
                LocalDateTime.of(2025, 7, 2, 12, 0),
                LocalDateTime.of(2025, 7, 1, 16, 30),
                Task.BEARBEITEN, WorkingLocation.A, "1033"));

        //Tag2 Away
        projectEntries.add(createProjectTimeEntry(
                LocalDateTime.of(2025, 7, 3, 8, 0),
                LocalDateTime.of(2025, 7, 1, 11, 30),
                Task.BEARBEITEN, WorkingLocation.A, "1033"));

        projectEntries.add(createProjectTimeEntry(
                LocalDateTime.of(2025, 7, 3, 12, 0),
                LocalDateTime.of(2025, 7, 1, 16, 30),
                Task.BEARBEITEN, WorkingLocation.A, "1033"));

        //arrival day
        projectEntries.add(createProjectTimeEntry(
                LocalDateTime.of(2025, 7, 4, 8, 0),
                LocalDateTime.of(2025, 7, 1, 11, 30),
                Task.BEARBEITEN, WorkingLocation.A, "1033"));

        projectEntries.add(createJourneyTimeEntry(
                LocalDateTime.of(2025, 7, 4, 12, 15),
                LocalDateTime.of(2025, 7, 1, 12, 45),
                WorkingLocation.MAIN, JourneyDirection.BACK));

        projectEntries.add(createProjectTimeEntry(
                LocalDateTime.of(2025, 7, 4, 12, 30),
                LocalDateTime.of(2025, 7, 1, 16, 30),
                Task.BEARBEITEN, WorkingLocation.MAIN, "1033"));

        return projectEntries;
    }

    private ProjectEntry createProjectTimeEntry(LocalDateTime from, LocalDateTime to, Task task, WorkingLocation location, String processId) {
        return ProjectTimeEntry.builder()
                .fromTime(from)
                .toTime(to)
                .task(task)
                .workingLocation(location)
                .process(processId)
                .build();
    }

    private JourneyTimeEntry createJourneyTimeEntry(LocalDateTime from, LocalDateTime to, WorkingLocation workLoc, JourneyDirection direction) {
        return JourneyTimeEntry.builder()
                .fromTime(from)
                .toTime(to)
                .task(Task.REISEN)
                .workingLocation(workLoc)
                .journeyDirection(direction)
                .vehicle(Vehicle.OTHER_INACTIVE)
                .build();
    }

    private User createUserForRole(final Role role) {
        return User.builder()
                .dbId(1)
                .userId("1")
                .email("max.mustermann@gpeardec.com")
                .firstname("Max")
                .lastname("Mustermann")
                .roles(Set.of(role))
                .build();
    }

    private ProjectTimeEntry projectTimeEntryFor(int startHour, int endHour, WorkingLocation workingLocation) {
        return projectTimeEntryFor(startHour, endHour, 0, workingLocation);
    }

    private ProjectTimeEntry projectTimeEntryFor(int startHour, int endHour, int endMinute, WorkingLocation workingLocation) {

        return ProjectTimeEntry.builder()
                .fromTime(LocalDateTime.of(2020, 1, 7, startHour, 0))
                .toTime(LocalDateTime.of(2020, 1, 7, endHour, endMinute))
                .task(Task.BEARBEITEN)
                .workingLocation(workingLocation)
                .build();
    }

    private JourneyTimeEntry journeyTimeEntryFor(int startHour, int endHour, JourneyDirection direction, WorkingLocation workingLocation) {
        return journeyTimeEntryFor(startHour, endHour, 0, direction, workingLocation);
    }

    private JourneyTimeEntry journeyTimeEntryFor(int startHour, int endHour, int endMinute, JourneyDirection direction, WorkingLocation workingLocation) {
        return JourneyTimeEntry.builder()
                .fromTime(LocalDateTime.of(2020, 1, 7, startHour, 0))
                .toTime(LocalDateTime.of(2020, 1, 7, endHour, endMinute))
                .task(Task.REISEN)
                .workingLocation(workingLocation)
                .journeyDirection(direction)
                .vehicle(Vehicle.OTHER_INACTIVE)
                .build();
    }

    @Test
    void whenOneProjectTimeEntryWithinJourneyWithWorkingLocationMain_thenWarning() {
        JourneyTimeEntry journeyTimeEntryOne = journeyTimeEntryFor(7, 8, JourneyDirection.TO, WorkingLocation.A);
        ProjectEntry projectEntryTwo = projectTimeEntryFor(8, 9, WorkingLocation.OTHER);
        JourneyTimeEntry journeyTimeEntryThree = journeyTimeEntryFor(9, 10, JourneyDirection.BACK, WorkingLocation.A);

        List<JourneyWarning> warnings = calculator.calculate(List.of(journeyTimeEntryOne, projectEntryTwo, journeyTimeEntryThree));

        assertThat(warnings).hasSize(1);
        assertThat(warnings.getFirst().getWarningTypes()).hasSize(1);
        assertThat(warnings.getFirst().getWarningTypes().getFirst()).isEqualTo(JourneyWarningType.INVALID_WORKING_LOCATION);
    }

    @Test
    void whenTwoProjectTimeEntryWithinJourneyWithOneInvalidWorkingLocation_thenOneWarning() {
        JourneyTimeEntry journeyTimeEntryOne = journeyTimeEntryFor(7, 8, JourneyDirection.TO, WorkingLocation.A);
        ProjectEntry projectEntryTwo = projectTimeEntryFor(8, 9, WorkingLocation.A);
        ProjectEntry projectEntryThree = projectTimeEntryFor(9, 10, WorkingLocation.MAIN);
        JourneyTimeEntry journeyTimeEntryFour = journeyTimeEntryFor(10, 11, JourneyDirection.BACK, WorkingLocation.A);

        List<JourneyWarning> warnings = calculator
                .calculate(List.of(journeyTimeEntryOne, projectEntryTwo, projectEntryThree, journeyTimeEntryFour));

        assertThat(warnings).hasSize(1);
        assertThat(warnings.getFirst().getWarningTypes()).hasSize(1);
        assertThat(warnings.getFirst().getWarningTypes().getFirst()).isEqualTo(JourneyWarningType.INVALID_WORKING_LOCATION);
    }

    @Test
    void whenTwoProjectTimeEntryWithinTwoJourneysWithOneInvalidWorkingLocation_thenOneWarning() {
        JourneyTimeEntry journeyTimeEntryOne = journeyTimeEntryFor(7, 8, JourneyDirection.TO, WorkingLocation.A);
        ProjectEntry projectEntryTwo = projectTimeEntryFor(8, 9, WorkingLocation.OTHER);
        JourneyTimeEntry journeyTimeEntryThree = journeyTimeEntryFor(9, 10, JourneyDirection.BACK, WorkingLocation.A);
        JourneyTimeEntry journeyTimeEntryFour = journeyTimeEntryFor(10, 11, JourneyDirection.TO, WorkingLocation.OTHER);
        ProjectEntry projectEntryFive = projectTimeEntryFor(11, 12, WorkingLocation.OTHER);
        JourneyTimeEntry journeyTimeEntrySix = journeyTimeEntryFor(12, 13, JourneyDirection.BACK, WorkingLocation.A);

        List<JourneyWarning> warnings = calculator
                .calculate(List.of(journeyTimeEntryOne, projectEntryTwo, journeyTimeEntryThree, journeyTimeEntryFour, projectEntryFive,
                        journeyTimeEntrySix));

        assertThat(warnings).hasSize(1);
        assertThat(warnings.getFirst().getWarningTypes()).hasSize(1);
        assertThat(warnings.getFirst().getWarningTypes().getFirst()).isEqualTo(JourneyWarningType.INVALID_WORKING_LOCATION);
    }

    @Test
    void whenProjectTimeEntryWithWorkingLocationMAINAfterJourneyBackWithWorkingLocationA_thenNoWarning() {
        JourneyTimeEntry journeyTimeEntryOne = journeyTimeEntryFor(8, 10, JourneyDirection.TO, WorkingLocation.A);
        ProjectTimeEntry projectTimeEntryTwo = projectTimeEntryFor(10, 11, WorkingLocation.A);
        JourneyTimeEntry journeyTimeEntryThree = journeyTimeEntryFor(12, 13, JourneyDirection.BACK, WorkingLocation.A);
        ProjectTimeEntry projectTimeEntryFour = projectTimeEntryFor(15, 16, WorkingLocation.MAIN);

        List<JourneyWarning> warnings = calculator
                .calculate(List.of(journeyTimeEntryOne, projectTimeEntryTwo, journeyTimeEntryThree, projectTimeEntryFour));

        assertThat(warnings).isEmpty();
    }

    @Test
    void whenProjectTimeEntryWithWorkingLocationAAfterJourneyBackWithWorkingLocationA_thenNoWarning() {
        JourneyTimeEntry journeyTimeEntryOne = journeyTimeEntryFor(8, 10, JourneyDirection.TO, WorkingLocation.A);
        ProjectTimeEntry projectTimeEntryTwo = projectTimeEntryFor(10, 11, WorkingLocation.A);
        JourneyTimeEntry journeyTimeEntryThree = journeyTimeEntryFor(12, 13, JourneyDirection.BACK, WorkingLocation.A);
        ProjectTimeEntry projectTimeEntryFour = projectTimeEntryFor(15, 16, WorkingLocation.A);

        List<JourneyWarning> warnings = calculator
                .calculate(List.of(journeyTimeEntryOne, projectTimeEntryTwo, journeyTimeEntryThree, projectTimeEntryFour));

        assertThat(warnings).hasSize(1);
    }

    @Test
    void whenOneProjectTimeEntryWithinJourney_thenNoWarning() {
        JourneyTimeEntry journeyTimeEntryOne = journeyTimeEntryFor(7, 8, JourneyDirection.TO, WorkingLocation.A);
        ProjectEntry projectEntryTwo = projectTimeEntryFor(8, 9, WorkingLocation.A);
        JourneyTimeEntry journeyTimeEntryThree = journeyTimeEntryFor(9, 10, JourneyDirection.BACK, WorkingLocation.A);

        List<JourneyWarning> warnings = calculator.calculate(List.of(journeyTimeEntryOne, projectEntryTwo, journeyTimeEntryThree));

        assertThat(warnings).isEmpty();
    }

    @Test
    void whenTwoProjectTimeEntryWithinJourney_thenNoWarning() {
        JourneyTimeEntry journeyTimeEntryOne = journeyTimeEntryFor(7, 8, JourneyDirection.TO, WorkingLocation.A);
        ProjectEntry projectEntryTwo = projectTimeEntryFor(8, 9, WorkingLocation.A);
        ProjectEntry projectEntryThree = projectTimeEntryFor(9, 10, WorkingLocation.A);
        JourneyTimeEntry journeyTimeEntryFour = journeyTimeEntryFor(10, 11, JourneyDirection.BACK, WorkingLocation.A);

        List<JourneyWarning> warnings = calculator
                .calculate(List.of(journeyTimeEntryOne, projectEntryTwo, projectEntryThree, journeyTimeEntryFour));

        assertThat(warnings).isEmpty();
    }

    @Test
    void whenTwoProjectTimeEntryWithinTwoJourneys_thenNoWarning() {
        JourneyTimeEntry journeyTimeEntryOne = journeyTimeEntryFor(7, 8, JourneyDirection.TO, WorkingLocation.A);
        ProjectEntry projectEntryTwo = projectTimeEntryFor(8, 9, WorkingLocation.A);
        JourneyTimeEntry journeyTimeEntryThree = journeyTimeEntryFor(9, 10, JourneyDirection.BACK, WorkingLocation.A);
        JourneyTimeEntry journeyTimeEntryFour = journeyTimeEntryFor(10, 11, JourneyDirection.TO, WorkingLocation.OTHER);
        ProjectEntry projectEntryFive = projectTimeEntryFor(11, 12, WorkingLocation.OTHER);
        JourneyTimeEntry journeyTimeEntrySix = journeyTimeEntryFor(12, 13, JourneyDirection.BACK, WorkingLocation.A);

        List<JourneyWarning> warnings = calculator
                .calculate(List.of(journeyTimeEntryOne, projectEntryTwo, journeyTimeEntryThree, journeyTimeEntryFour, projectEntryFive,
                        journeyTimeEntrySix));

        assertThat(warnings).isEmpty();
    }

    @Test
    void whenProjectEntryAndJourneyEntryHaveSameStartHourSort_thenNoWarning() {
        JourneyTimeEntry journeyTimeEntryOne = journeyTimeEntryFor(7, 7, JourneyDirection.TO, WorkingLocation.A);
        ProjectEntry projectEntryOne = projectTimeEntryFor(7, 8, WorkingLocation.A);
        ProjectEntry projectEntryTwo = projectTimeEntryFor(11, 12, WorkingLocation.A);
        JourneyTimeEntry journeyTimeEntryTwo = journeyTimeEntryFor(12, 13, JourneyDirection.BACK, WorkingLocation.A);

        List<JourneyWarning> warnings = calculator
                .calculate(List.of(journeyTimeEntryOne, projectEntryOne, projectEntryTwo, journeyTimeEntryTwo));
        assertThat(warnings).isEmpty();
    }

    @Test
    void whenProjectEntryAndJourneyEntryHaveSameStartHourSortReversed_thenNoWarning() {
        JourneyTimeEntry journeyTimeEntryOne = journeyTimeEntryFor(7, 7, JourneyDirection.TO, WorkingLocation.A);
        ProjectEntry projectEntryOne = projectTimeEntryFor(7, 8, WorkingLocation.A);
        ProjectEntry projectEntryTwo = projectTimeEntryFor(11, 12, WorkingLocation.A);
        JourneyTimeEntry journeyTimeEntryTwo = journeyTimeEntryFor(12, 13, JourneyDirection.BACK, WorkingLocation.A);

        List<JourneyWarning> warnings = calculator
                .calculate(List.of(projectEntryOne, journeyTimeEntryOne, journeyTimeEntryTwo, projectEntryTwo));
        assertThat(warnings).isEmpty();
    }
}
