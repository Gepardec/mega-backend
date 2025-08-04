package com.gepardec.mega.service.impl.timewarning;

import com.gepardec.mega.domain.calculation.journey.InvalidWorkingLocationInJourneyCalculator;
import com.gepardec.mega.domain.model.AbsenceTime;
import com.gepardec.mega.domain.model.Employee;
import com.gepardec.mega.domain.model.EmploymentPeriod;
import com.gepardec.mega.domain.model.EmploymentPeriods;
import com.gepardec.mega.domain.model.MonthlyWarning;
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
import com.gepardec.mega.domain.model.monthlyreport.TimeWarning;
import com.gepardec.mega.domain.model.monthlyreport.TimeWarningType;
import com.gepardec.mega.domain.model.monthlyreport.Vehicle;
import com.gepardec.mega.domain.model.monthlyreport.WorkingLocation;
import com.gepardec.mega.domain.utils.DateUtils;
import com.gepardec.mega.service.api.TimeWarningService;
import com.gepardec.mega.service.helper.WarningCalculatorsManager;
import io.quarkus.test.InjectMock;
import io.quarkus.test.Mock;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Month;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@QuarkusTest
class TimeWarningServiceImplTest {

    @Inject
    TimeWarningService timeWarningService;

    @InjectMock
    WarningCalculatorsManager warningCalculatorsManager;

    @InjectMock
    UserContext userContext;

    @Spy
    @InjectMocks
    private InvalidWorkingLocationInJourneyCalculator invalidWorkingLocationInJourneyCalculator = new  InvalidWorkingLocationInJourneyCalculator();

    public static Stream<Arguments> journeyWorkingLocationPeriodArguments() {
        return  Stream.of(Arguments.of(createProjectEntryListForRequestForJourney(), false),
                Arguments.of(List.of(createProjectEntryListForRequestForJourneySmall()), false)
        );
    }


    @Test
    void getAllWarningsForEmployeeAndMonth_whenWarningsPresent_thenReturnListOfMonthlyWarning() {
        User user = createUserForRole(Role.EMPLOYEE);
        when(userContext.getUser()).thenReturn(user);

        Employee employee = createEmployeeForUser(user);

        when(warningCalculatorsManager.determineJourneyWarnings(any()))
                .thenReturn(List.of(createJourneyWarning("2024-05-03")));

        when(warningCalculatorsManager.determineTimeWarnings(any()))
                .thenReturn(createTimeWarnings());

        when(warningCalculatorsManager.determineNoTimeEntries(any(Employee.class), any(), any()))
                .thenReturn(createNoTimeEntries());

        List<AbsenceTime> absenceTimes = createAbsenceTimeListForRequest(employee.getUserId());
        List<ProjectEntry> projectEntries = createProjectEntryListForRequest();
        List<MonthlyWarning> actual = timeWarningService.getAllTimeWarningsForEmployeeAndMonth(absenceTimes, projectEntries, employee);

        assertThat(actual.isEmpty()).isFalse();
        assertThat(actual.size()).isEqualTo(9);
    }

    @Test
    void getAllWarningsForEmployeeAndMonth_whenNoWarningsPresent_thenReturnEmptyList() {
        User user = createUserForRole(Role.EMPLOYEE);
        when(userContext.getUser()).thenReturn(user);

        Employee employee = createEmployeeForUser(user);

        when(warningCalculatorsManager.determineJourneyWarnings(any()))
                .thenReturn(new ArrayList<>());

        when(warningCalculatorsManager.determineTimeWarnings(any()))
                .thenReturn(new ArrayList<>());

        when(warningCalculatorsManager.determineNoTimeEntries(any(Employee.class), any(), any()))
                .thenReturn(new ArrayList<>());


        List<ProjectEntry> projectEntries = createProjectEntryListForMonth();
        List<MonthlyWarning> actual = timeWarningService.getAllTimeWarningsForEmployeeAndMonth(new ArrayList<>(), projectEntries, employee);

        assertThat(actual.isEmpty()).isTrue();
    }

    @ParameterizedTest
    @MethodSource("journeyWorkingLocationPeriodArguments")
    void getAllWarningsForEmployeeAndMonth_whenInvalidJourneyWorkingLocation_thenGetError(List<ProjectEntry> projectEntries, boolean throwError) {
        User user = createUserForRole(Role.EMPLOYEE);
        when(userContext.getUser()).thenReturn(user);

        Employee employee = createEmployeeForUser(user);

        List<JourneyWarning> actual = invalidWorkingLocationInJourneyCalculator.calculate(projectEntries);

        assertThat(actual
                .stream()
                .anyMatch(it -> it.getWarningTypes().contains(JourneyWarningType.INVALID_WORKING_LOCATION))
        ).isEqualTo(throwError);
    }

    private List<AbsenceTime> createAbsenceTimeListForRequest(String userId) {
        List<AbsenceTime> absenceTimes = new ArrayList<>();
        absenceTimes.add(createAbsenceTime(userId, "2024-05-02", "2024-05-02", "KR"));
        absenceTimes.add(createAbsenceTime(userId, "2024-05-31", "2024-05-31", "HO"));
        return absenceTimes;
    }

    private AbsenceTime createAbsenceTime(String userId, String fromDate, String toDate, String reason) {
        return AbsenceTime.builder()
                .userId(userId)
                .fromDate(DateUtils.parseDate(fromDate))
                .toDate(DateUtils.parseDate(toDate))
                .reason(reason)
                .accepted(true)
                .build();
    }

    private List<ProjectEntry> createProjectEntryListForRequest() {
        List<ProjectEntry> projectEntries = new ArrayList<>();
        projectEntries.add(createProjectTimeEntry(LocalDateTime.of(2024, 5, 1, 15, 0),
                LocalDateTime.of(2024, 5, 1, 0, 0)));
        projectEntries.add(createProjectTimeEntry(LocalDateTime.of(2024, 5, 3, 12, 45),
                LocalDateTime.of(2024, 5, 3, 23, 45)));
        projectEntries.add(createProjectTimeEntry(LocalDateTime.of(2024, 5, 18, 14, 45),
                LocalDateTime.of(2024, 5, 18, 15, 0)));
        projectEntries.add(createProjectTimeEntry(LocalDateTime.of(2024, 5, 21, 16, 0),
                LocalDateTime.of(2024, 5, 21, 21, 0)));
        projectEntries.add(createProjectTimeEntry(LocalDateTime.of(2024, 5, 22, 5, 0),
                LocalDateTime.of(2024, 5, 22, 8, 30)));
        projectEntries.add(createJourneyTimeEntry(LocalDateTime.of(2024, 5, 23, 15, 30),
                LocalDateTime.of(2024, 5, 23, 16, 15)));
        projectEntries.add(createProjectTimeEntry(LocalDateTime.of(2024, 5, 23, 16, 30),
                LocalDateTime.of(2024, 5, 23, 17, 0)));
        projectEntries.add(createProjectTimeEntry(LocalDateTime.of(2024, 5, 27, 8, 0),
                LocalDateTime.of(2024, 5, 27, 14, 45)));
        projectEntries.add(createProjectTimeEntry(LocalDateTime.of(2024, 5, 28, 8, 0),
                LocalDateTime.of(2024, 5, 28, 12, 0)));
        projectEntries.add(createProjectTimeEntry(LocalDateTime.of(2024, 5, 28, 12, 30),
                LocalDateTime.of(2024, 5, 28, 14, 0)));
        projectEntries.add(createProjectTimeEntry(LocalDateTime.of(2024, 5, 29, 5, 30),
                LocalDateTime.of(2024, 5, 29, 11, 0)));

        return projectEntries;
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

    private List<ProjectEntry> createProjectEntryListForRequestForJourneySmall() {
        List<ProjectEntry> projectEntries = new ArrayList<>();

        projectEntries.add(createJourneyTimeEntry(
                LocalDateTime.of(2025, 7, 1, 10, 15),
                LocalDateTime.of(2025, 7, 1, 10, 45),
                WorkingLocation.A, JourneyDirection.TO));

        projectEntries.add(createProjectTimeEntry(
                LocalDateTime.of(2025, 7, 1, 11, 0),
                LocalDateTime.of(2025, 7, 1, 11, 30),
                Task.BEARBEITEN, WorkingLocation.MAIN, "1033"));

        projectEntries.add(createJourneyTimeEntry(
                LocalDateTime.of(2025, 7, 1, 10, 15),
                LocalDateTime.of(2025, 7, 1, 10, 45),
                WorkingLocation.A, JourneyDirection.BACK));

        return projectEntries;
    }


    private ProjectEntry createProjectTimeEntry(LocalDateTime from, LocalDateTime to, Task task, WorkingLocation location, String processId){
        return ProjectTimeEntry.builder()
                .fromTime(from)
                .toTime(to)
                .task(task)
                .workingLocation(location)
                .process(processId)
                .build();
    }

    private ProjectEntry createProjectTimeEntry(LocalDateTime from, LocalDateTime to) {
        return createProjectTimeEntry(from, to, Task.BEARBEITEN, WorkingLocation.MAIN, "1033");
    }

    private List<ProjectEntry> createProjectEntryListForMonth() {
        List<ProjectEntry> projectEntries = new ArrayList<>();
        projectEntries.add(createProjectTimeEntry(LocalDateTime.of(2024, 6, 3, 7, 0),
                LocalDateTime.of(2024, 6, 3, 11, 30)));
        projectEntries.add(createProjectTimeEntry(LocalDateTime.of(2024, 6, 3, 12, 0),
                LocalDateTime.of(2024, 6, 3, 15, 30)));

        projectEntries.add(createProjectTimeEntry(LocalDateTime.of(2024, 6, 4, 7, 0),
                LocalDateTime.of(2024, 6, 4, 11, 30)));
        projectEntries.add(createProjectTimeEntry(LocalDateTime.of(2024, 6, 4, 12, 0),
                LocalDateTime.of(2024, 6, 4, 15, 30)));

        projectEntries.add(createProjectTimeEntry(LocalDateTime.of(2024, 6, 5, 7, 0),
                LocalDateTime.of(2024, 6, 5, 11, 30)));
        projectEntries.add(createProjectTimeEntry(LocalDateTime.of(2024, 6, 5, 12, 0),
                LocalDateTime.of(2024, 6, 5, 15, 30)));

        projectEntries.add(createProjectTimeEntry(LocalDateTime.of(2024, 6, 6, 7, 0),
                LocalDateTime.of(2024, 6, 6, 11, 30)));
        projectEntries.add(createProjectTimeEntry(LocalDateTime.of(2024, 6, 6, 12, 0),
                LocalDateTime.of(2024, 6, 6, 15, 30)));

        projectEntries.add(createProjectTimeEntry(LocalDateTime.of(2024, 6, 7, 7, 0),
                LocalDateTime.of(2024, 6, 7, 12, 0)));
        projectEntries.add(createProjectTimeEntry(LocalDateTime.of(2024, 6, 7, 12, 30),
                LocalDateTime.of(2024, 6, 7, 14, 0)));

        projectEntries.add(createProjectTimeEntry(LocalDateTime.of(2024, 6, 10, 7, 0),
                LocalDateTime.of(2024, 6, 10, 11, 30)));
        projectEntries.add(createProjectTimeEntry(LocalDateTime.of(2024, 6, 10, 12, 0),
                LocalDateTime.of(2024, 6, 10, 15, 30)));

        projectEntries.add(createProjectTimeEntry(LocalDateTime.of(2024, 6, 11, 7, 0),
                LocalDateTime.of(2024, 6, 11, 11, 30)));
        projectEntries.add(createProjectTimeEntry(LocalDateTime.of(2024, 6, 11, 12, 0),
                LocalDateTime.of(2024, 6, 11, 15, 30)));

        projectEntries.add(createProjectTimeEntry(LocalDateTime.of(2024, 6, 12, 7, 0),
                LocalDateTime.of(2024, 6, 12, 11, 30)));
        projectEntries.add(createProjectTimeEntry(LocalDateTime.of(2024, 6, 12, 12, 0),
                LocalDateTime.of(2024, 6, 12, 15, 30)));

        projectEntries.add(createProjectTimeEntry(LocalDateTime.of(2024, 6, 13, 7, 0),
                LocalDateTime.of(2024, 6, 13, 11, 30)));
        projectEntries.add(createProjectTimeEntry(LocalDateTime.of(2024, 6, 13, 12, 0),
                LocalDateTime.of(2024, 6, 13, 15, 30)));

        projectEntries.add(createProjectTimeEntry(LocalDateTime.of(2024, 6, 14, 7, 0),
                LocalDateTime.of(2024, 6, 14, 12, 0)));
        projectEntries.add(createProjectTimeEntry(LocalDateTime.of(2024, 6, 14, 12, 30),
                LocalDateTime.of(2024, 6, 14, 14, 0)));

        projectEntries.add(createProjectTimeEntry(LocalDateTime.of(2024, 6, 17, 7, 0),
                LocalDateTime.of(2024, 6, 17, 11, 30)));
        projectEntries.add(createProjectTimeEntry(LocalDateTime.of(2024, 6, 17, 12, 0),
                LocalDateTime.of(2024, 6, 17, 15, 30)));

        projectEntries.add(createProjectTimeEntry(LocalDateTime.of(2024, 6, 18, 7, 0),
                LocalDateTime.of(2024, 6, 18, 11, 30)));
        projectEntries.add(createProjectTimeEntry(LocalDateTime.of(2024, 6, 18, 12, 0),
                LocalDateTime.of(2024, 6, 18, 15, 30)));

        projectEntries.add(createProjectTimeEntry(LocalDateTime.of(2024, 6, 19, 7, 0),
                LocalDateTime.of(2024, 6, 19, 11, 30)));
        projectEntries.add(createProjectTimeEntry(LocalDateTime.of(2024, 6, 19, 12, 0),
                LocalDateTime.of(2024, 6, 19, 15, 30)));

        projectEntries.add(createProjectTimeEntry(LocalDateTime.of(2024, 6, 20, 7, 0),
                LocalDateTime.of(2024, 6, 20, 11, 30)));
        projectEntries.add(createProjectTimeEntry(LocalDateTime.of(2024, 6, 20, 12, 0),
                LocalDateTime.of(2024, 6, 20, 15, 30)));

        projectEntries.add(createProjectTimeEntry(LocalDateTime.of(2024, 6, 21, 7, 0),
                LocalDateTime.of(2024, 6, 21, 12, 0)));
        projectEntries.add(createProjectTimeEntry(LocalDateTime.of(2024, 6, 21, 12, 30),
                LocalDateTime.of(2024, 6, 21, 14, 0)));

        projectEntries.add(createProjectTimeEntry(LocalDateTime.of(2024, 6, 24, 7, 0),
                LocalDateTime.of(2024, 6, 24, 11, 30)));
        projectEntries.add(createProjectTimeEntry(LocalDateTime.of(2024, 6, 24, 12, 0),
                LocalDateTime.of(2024, 6, 24, 15, 30)));

        projectEntries.add(createProjectTimeEntry(LocalDateTime.of(2024, 6, 25, 7, 0),
                LocalDateTime.of(2024, 6, 25, 11, 30)));
        projectEntries.add(createProjectTimeEntry(LocalDateTime.of(2024, 6, 25, 12, 0),
                LocalDateTime.of(2024, 6, 25, 15, 30)));

        projectEntries.add(createProjectTimeEntry(LocalDateTime.of(2024, 6, 26, 7, 0),
                LocalDateTime.of(2024, 6, 26, 11, 30)));
        projectEntries.add(createProjectTimeEntry(LocalDateTime.of(2024, 6, 26, 12, 0),
                LocalDateTime.of(2024, 6, 26, 15, 30)));

        projectEntries.add(createProjectTimeEntry(LocalDateTime.of(2024, 6, 27, 7, 0),
                LocalDateTime.of(2024, 6, 27, 11, 30)));
        projectEntries.add(createProjectTimeEntry(LocalDateTime.of(2024, 6, 27, 12, 0),
                LocalDateTime.of(2024, 6, 27, 15, 30)));

        projectEntries.add(createProjectTimeEntry(LocalDateTime.of(2024, 6, 28, 7, 0),
                LocalDateTime.of(2024, 6, 28, 12, 0)));
        projectEntries.add(createProjectTimeEntry(LocalDateTime.of(2024, 6, 28, 12, 30),
                LocalDateTime.of(2024, 6, 28, 14, 0)));

        return projectEntries;
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

    private JourneyTimeEntry createJourneyTimeEntry(LocalDateTime from, LocalDateTime to) {
        return createJourneyTimeEntry(from, to, WorkingLocation.A,  JourneyDirection.TO);
    }


    private Employee createEmployeeForUser(final User user) {
        return Employee.builder()
                .email(user.getEmail())
                .firstname(user.getFirstname())
                .lastname(user.getLastname())
                .title("Ing.")
                .userId(user.getUserId())
                .releaseDate("2020-01-01")
                .employmentPeriods(new EmploymentPeriods(new EmploymentPeriod(LocalDate.of(2020, 1, 1), null)))
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

    private List<TimeWarning> createNoTimeEntries() {
        List<TimeWarning> timeWarnings = new ArrayList<>();
        timeWarnings.add(createNoTimeEntry("2024-05-06"));
        timeWarnings.add(createNoTimeEntry("2024-05-07"));
        timeWarnings.add(createNoTimeEntry("2024-05-08"));
        timeWarnings.add(createNoTimeEntry("2024-05-10"));
        timeWarnings.add(createNoTimeEntry("2024-05-13"));
        timeWarnings.add(createNoTimeEntry("2024-05-14"));
        timeWarnings.add(createNoTimeEntry("2024-05-15"));
        timeWarnings.add(createNoTimeEntry("2024-05-16"));
        timeWarnings.add(createNoTimeEntry("2024-05-17"));
        timeWarnings.add(createNoTimeEntry("2024-05-24"));
        timeWarnings.add(createNoTimeEntry("2024-05-31"));

        return timeWarnings;
    }

    private List<TimeWarning> createTimeWarnings() {
        List<TimeWarning> timeWarnings = new ArrayList<>();
        timeWarnings.add(createTimeWarning("2024-05-01", List.of(TimeWarningType.HOLIDAY)));
        timeWarnings.add(createTimeWarning("2024-05-03", List.of(TimeWarningType.EXCESS_WORKING_TIME_PRESENT, TimeWarningType.MISSING_BREAK_TIME, TimeWarningType.MISSING_REST_TIME)));
        timeWarnings.add(createTimeWarning("2024-05-18", List.of(TimeWarningType.WEEKEND)));
        timeWarnings.add(createTimeWarning("2024-05-22", List.of(TimeWarningType.MISSING_REST_TIME, TimeWarningType.OUTSIDE_CORE_WORKING_TIME)));
        timeWarnings.add(createTimeWarning("2024-05-27", List.of(TimeWarningType.MISSING_BREAK_TIME)));
        timeWarnings.add(createTimeWarning("2024-05-28", List.of(TimeWarningType.EXCESS_WORKING_TIME_PRESENT)));
        timeWarnings.add(createTimeWarning("2024-05-29", List.of(TimeWarningType.MISSING_REST_TIME, TimeWarningType.OUTSIDE_CORE_WORKING_TIME)));

        return timeWarnings;
    }

    private TimeWarning createNoTimeEntry(String date) {
        TimeWarning timeWarning = new TimeWarning();
        timeWarning.setWarningTypes(List.of(TimeWarningType.NO_TIME_ENTRY));
        timeWarning.setDate(DateUtils.parseDate(date));
        return timeWarning;
    }

    private TimeWarning createTimeWarning(String date, List<TimeWarningType> types) {
        TimeWarning timeWarning = new TimeWarning();
        timeWarning.setWarningTypes(types);
        timeWarning.setDate(DateUtils.parseDate(date));
        return timeWarning;
    }

    private JourneyWarning createJourneyWarning(String date) {
        JourneyWarning journeyWarning = new JourneyWarning();
        journeyWarning.setWarningTypes(List.of(JourneyWarningType.BACK_MISSING, JourneyWarningType.INVALID_WORKING_LOCATION));
        journeyWarning.setDate(DateUtils.parseDate(date));
        return journeyWarning;
    }

}
