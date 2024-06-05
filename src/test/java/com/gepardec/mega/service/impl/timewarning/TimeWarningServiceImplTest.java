package com.gepardec.mega.service.impl.timewarning;

import com.gepardec.mega.domain.model.AbsenceTime;
import com.gepardec.mega.domain.model.Employee;
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
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.mockito.InjectMock;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@QuarkusTest
public class TimeWarningServiceImplTest {

    @Inject
    TimeWarningService timeWarningService;

    @InjectMock
    WarningCalculatorsManager warningCalculatorsManager;

    @InjectMock
    UserContext userContext;

    @Test
    void testGetAllWarningsForEmployeeAndMonth_whenWarningsPresent_thenReturnListOfMonthlyWarning(){
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
        List<ProjectEntry> projectEntries = createProjectEntryListForRequest(employee.getUserId());
        List<MonthlyWarning> actual = timeWarningService.getAllTimeWarningsForEmployeeAndMonth(absenceTimes, projectEntries, employee);

        assertThat(actual.isEmpty()).isFalse();
        assertThat(actual.size()).isEqualTo(9);
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

    private List<ProjectEntry> createProjectEntryListForRequest(String userId) {
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

    private ProjectEntry createProjectTimeEntry(LocalDateTime from, LocalDateTime to) {
        return ProjectTimeEntry.builder()
                .fromTime(from)
                .toTime(to)
                .task(Task.BEARBEITEN)
                .workingLocation(WorkingLocation.MAIN)
                .process("1033")
                .build();
    }

    private JourneyTimeEntry createJourneyTimeEntry(LocalDateTime from, LocalDateTime to) {
        return JourneyTimeEntry.builder()
                .fromTime(from)
                .toTime(to)
                .task(Task.REISEN)
                .workingLocation(WorkingLocation.A)
                .journeyDirection(JourneyDirection.TO)
                .vehicle(Vehicle.OTHER_INACTIVE)
                .build();
    }

    private Employee createEmployeeForUser(final User user) {
        return Employee.builder()
                .email(user.getEmail())
                .firstname(user.getFirstname())
                .lastname(user.getLastname())
                .title("Ing.")
                .userId(user.getUserId())
                .releaseDate("2020-01-01")
                .active(true)
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

    private List<TimeWarning> createNoTimeEntries(){
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

    private List<TimeWarning> createTimeWarnings(){
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

    private TimeWarning createNoTimeEntry(String date){
        TimeWarning timeWarning = new TimeWarning();
        timeWarning.setWarningTypes(List.of(TimeWarningType.NO_TIME_ENTRY));
        timeWarning.setDate(DateUtils.parseDate(date));
        return timeWarning;
    }

    private TimeWarning createTimeWarning(String date, List<TimeWarningType> types){
        TimeWarning timeWarning = new TimeWarning();
        timeWarning.setWarningTypes(types);
        timeWarning.setDate(DateUtils.parseDate(date));
        return timeWarning;
    }

    private JourneyWarning createJourneyWarning(String date){
        JourneyWarning journeyWarning = new JourneyWarning();
        journeyWarning.setWarningTypes(List.of(JourneyWarningType.BACK_MISSING, JourneyWarningType.INVALID_WORKING_LOCATION));
        journeyWarning.setDate(DateUtils.parseDate(date));
        return journeyWarning;
    }

}
