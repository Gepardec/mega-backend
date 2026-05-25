package com.gepardec.mega.service.impl.monthlyreport;

import com.gepardec.mega.domain.model.AbsenceTime;
import com.gepardec.mega.domain.model.Attendances;
import com.gepardec.mega.domain.model.Employee;
import com.gepardec.mega.domain.model.ProjectTime;
import com.gepardec.mega.domain.model.monthlyreport.ProjectEntry;
import com.gepardec.mega.hexagon.shared.application.security.AuthenticatedActorContext;
import com.gepardec.mega.hexagon.shared.domain.model.Email;
import com.gepardec.mega.hexagon.shared.domain.model.ZepUsername;
import com.gepardec.mega.hexagon.user.domain.model.User;
import com.gepardec.mega.service.api.MonthlyReportService;
import com.gepardec.mega.service.helper.WorkingTimeUtil;
import com.gepardec.mega.zep.ZepService;
import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.instancio.Instancio;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.instancio.Select.field;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@QuarkusTest
class MonthlyReportServiceImplTest {

    @Inject
    MonthlyReportService monthlyReportService;

    @InjectMock
    ZepService zepService;

    @InjectMock
    AuthenticatedActorContext userContext;

    @InjectMock
    ZepService employeeService;

    @InjectMock
    WorkingTimeUtil workingTimeUtil;

    private static final String TEST_USER_ID = "test-user-id";
    private static final String TEST_EMAIL = "test@gepardec.com";
    private static final YearMonth TEST_PAYROLL_MONTH = YearMonth.of(2026, 1);

    private Employee testEmployee;

    @BeforeEach
    void setUp() {
        User testUser = Instancio.of(User.class)
                .set(field(User::zepUsername), ZepUsername.of(TEST_USER_ID))
                .set(field(User::email), Email.of(TEST_EMAIL))
                .create();
        when(userContext.user()).thenReturn(testUser);

        testEmployee = mock(Employee.class);
        when(testEmployee.getEmail()).thenReturn(TEST_EMAIL);
        when(testEmployee.getUserId()).thenReturn(TEST_USER_ID);
        when(employeeService.getEmployee(TEST_USER_ID)).thenReturn(testEmployee);
    }

    @Nested
    class GetAttendances {

        @Test
        void getAttendances_WhenValidData_ShouldReturnAttendances() {
            // Arrange
            List<ProjectEntry> projectTimes = new ArrayList<>();
            List<ProjectTime> billableTimes = new ArrayList<>();
            List<AbsenceTime> absenceTimes = new ArrayList<>();

            when(zepService.getProjectTimes(testEmployee, TEST_PAYROLL_MONTH)).thenReturn(projectTimes);
            when(zepService.getBillableForEmployee(testEmployee, TEST_PAYROLL_MONTH)).thenReturn(billableTimes);
            when(zepService.getAbsenceForEmployee(testEmployee, TEST_PAYROLL_MONTH)).thenReturn(absenceTimes);

            when(workingTimeUtil.getTotalWorkingTimeForEmployee(projectTimes)).thenReturn("160:00");
            when(workingTimeUtil.getOvertimeForEmployee(testEmployee, projectTimes, absenceTimes, TEST_PAYROLL_MONTH)).thenReturn(10.0);
            when(workingTimeUtil.getBillableTimesForEmployee(billableTimes, testEmployee)).thenReturn("120:00");
            when(workingTimeUtil.getDurationFromTimeString("160:00")).thenReturn(Duration.ofHours(160));
            when(workingTimeUtil.getDurationFromTimeString("120:00")).thenReturn(Duration.ofHours(120));
            when(workingTimeUtil.getBillablePercentage(Duration.ofHours(160), Duration.ofHours(120))).thenReturn(75.0);

            // Act
            Attendances result = monthlyReportService.getAttendances(TEST_PAYROLL_MONTH);

            // Assert
            assertThat(result).isNotNull();
            assertThat(result.totalWorkingTimeHours()).isEqualTo(160.0);
            assertThat(result.overtimeHours()).isEqualTo(10.0);
            assertThat(result.billableTimeHours()).isEqualTo(120.0);
            assertThat(result.billablePercentage()).isEqualTo(75.0);
        }

        @Test
        void getAttendances_WhenZeroWorkingTime_ShouldReturnZeroValues() {
            // Arrange
            List<ProjectEntry> projectTimes = new ArrayList<>();
            List<ProjectTime> billableTimes = new ArrayList<>();
            List<AbsenceTime> absenceTimes = new ArrayList<>();

            when(zepService.getProjectTimes(testEmployee, TEST_PAYROLL_MONTH)).thenReturn(projectTimes);
            when(zepService.getBillableForEmployee(testEmployee, TEST_PAYROLL_MONTH)).thenReturn(billableTimes);
            when(zepService.getAbsenceForEmployee(testEmployee, TEST_PAYROLL_MONTH)).thenReturn(absenceTimes);

            when(workingTimeUtil.getTotalWorkingTimeForEmployee(projectTimes)).thenReturn("00:00");
            when(workingTimeUtil.getOvertimeForEmployee(testEmployee, projectTimes, absenceTimes, TEST_PAYROLL_MONTH)).thenReturn(0.0);
            when(workingTimeUtil.getBillableTimesForEmployee(billableTimes, testEmployee)).thenReturn("00:00");
            when(workingTimeUtil.getDurationFromTimeString("00:00")).thenReturn(Duration.ZERO);
            when(workingTimeUtil.getBillablePercentage(Duration.ZERO, Duration.ZERO)).thenReturn(0.0);

            // Act
            Attendances result = monthlyReportService.getAttendances(TEST_PAYROLL_MONTH);

            // Assert
            assertThat(result).isNotNull();
            assertThat(result.totalWorkingTimeHours()).isEqualTo(0.0);
            assertThat(result.overtimeHours()).isEqualTo(0.0);
            assertThat(result.billableTimeHours()).isEqualTo(0.0);
            assertThat(result.billablePercentage()).isEqualTo(0.0);
        }

        @Test
        void getAttendances_WhenNegativeOvertime_ShouldReturnNegativeValue() {
            // Arrange
            List<ProjectEntry> projectTimes = new ArrayList<>();
            List<ProjectTime> billableTimes = new ArrayList<>();
            List<AbsenceTime> absenceTimes = new ArrayList<>();

            when(zepService.getProjectTimes(testEmployee, TEST_PAYROLL_MONTH)).thenReturn(projectTimes);
            when(zepService.getBillableForEmployee(testEmployee, TEST_PAYROLL_MONTH)).thenReturn(billableTimes);
            when(zepService.getAbsenceForEmployee(testEmployee, TEST_PAYROLL_MONTH)).thenReturn(absenceTimes);

            when(workingTimeUtil.getTotalWorkingTimeForEmployee(projectTimes)).thenReturn("150:00");
            when(workingTimeUtil.getOvertimeForEmployee(testEmployee, projectTimes, absenceTimes, TEST_PAYROLL_MONTH)).thenReturn(-10.0);
            when(workingTimeUtil.getBillableTimesForEmployee(billableTimes, testEmployee)).thenReturn("100:00");
            when(workingTimeUtil.getDurationFromTimeString("150:00")).thenReturn(Duration.ofHours(150));
            when(workingTimeUtil.getDurationFromTimeString("100:00")).thenReturn(Duration.ofHours(100));
            when(workingTimeUtil.getBillablePercentage(Duration.ofHours(150), Duration.ofHours(100))).thenReturn(66.67);

            // Act
            Attendances result = monthlyReportService.getAttendances(TEST_PAYROLL_MONTH);

            // Assert
            assertThat(result).isNotNull();
            assertThat(result.totalWorkingTimeHours()).isEqualTo(150.0);
            assertThat(result.overtimeHours()).isEqualTo(-10.0);
            assertThat(result.billableTimeHours()).isEqualTo(100.0);
            assertThat(result.billablePercentage()).isEqualTo(66.67);
        }

        @Test
        void getAttendances_WhenPartialHours_ShouldConvertMinutesCorrectly() {
            // Arrange
            List<ProjectEntry> projectTimes = new ArrayList<>();
            List<ProjectTime> billableTimes = new ArrayList<>();
            List<AbsenceTime> absenceTimes = new ArrayList<>();

            when(zepService.getProjectTimes(testEmployee, TEST_PAYROLL_MONTH)).thenReturn(projectTimes);
            when(zepService.getBillableForEmployee(testEmployee, TEST_PAYROLL_MONTH)).thenReturn(billableTimes);
            when(zepService.getAbsenceForEmployee(testEmployee, TEST_PAYROLL_MONTH)).thenReturn(absenceTimes);

            // 160 hours and 30 minutes = 9630 minutes
            when(workingTimeUtil.getTotalWorkingTimeForEmployee(projectTimes)).thenReturn("160:30");
            when(workingTimeUtil.getOvertimeForEmployee(testEmployee, projectTimes, absenceTimes, TEST_PAYROLL_MONTH)).thenReturn(5.5);
            // 120 hours and 15 minutes = 7215 minutes
            when(workingTimeUtil.getBillableTimesForEmployee(billableTimes, testEmployee)).thenReturn("120:15");
            when(workingTimeUtil.getDurationFromTimeString("160:30")).thenReturn(Duration.ofMinutes(9630));
            when(workingTimeUtil.getDurationFromTimeString("120:15")).thenReturn(Duration.ofMinutes(7215));
            when(workingTimeUtil.getBillablePercentage(Duration.ofMinutes(9630), Duration.ofMinutes(7215))).thenReturn(74.92);

            // Act
            Attendances result = monthlyReportService.getAttendances(TEST_PAYROLL_MONTH);

            // Assert
            assertThat(result).isNotNull();
            assertThat(result.totalWorkingTimeHours()).isEqualTo(160.5); // 9630 / 60 = 160.5
            assertThat(result.overtimeHours()).isEqualTo(5.5);
            assertThat(result.billableTimeHours()).isEqualTo(120.25); // 7215 / 60 = 120.25
            assertThat(result.billablePercentage()).isEqualTo(74.92);
        }

        @Test
        void getAttendances_ShouldCallAllZepServiceMethods() {
            // Arrange
            List<ProjectEntry> projectTimes = new ArrayList<>();
            List<ProjectTime> billableTimes = new ArrayList<>();
            List<AbsenceTime> absenceTimes = new ArrayList<>();

            when(zepService.getProjectTimes(testEmployee, TEST_PAYROLL_MONTH)).thenReturn(projectTimes);
            when(zepService.getBillableForEmployee(testEmployee, TEST_PAYROLL_MONTH)).thenReturn(billableTimes);
            when(zepService.getAbsenceForEmployee(testEmployee, TEST_PAYROLL_MONTH)).thenReturn(absenceTimes);

            when(workingTimeUtil.getTotalWorkingTimeForEmployee(projectTimes)).thenReturn("160:00");
            when(workingTimeUtil.getOvertimeForEmployee(testEmployee, projectTimes, absenceTimes, TEST_PAYROLL_MONTH)).thenReturn(10.0);
            when(workingTimeUtil.getBillableTimesForEmployee(billableTimes, testEmployee)).thenReturn("120:00");
            when(workingTimeUtil.getDurationFromTimeString("160:00")).thenReturn(Duration.ofHours(160));
            when(workingTimeUtil.getDurationFromTimeString("120:00")).thenReturn(Duration.ofHours(120));
            when(workingTimeUtil.getBillablePercentage(Duration.ofHours(160), Duration.ofHours(120))).thenReturn(75.0);

            // Act
            monthlyReportService.getAttendances(TEST_PAYROLL_MONTH);

            // Assert
            verify(zepService).getProjectTimes(testEmployee, TEST_PAYROLL_MONTH);
            verify(zepService).getBillableForEmployee(testEmployee, TEST_PAYROLL_MONTH);
            verify(zepService).getAbsenceForEmployee(testEmployee, TEST_PAYROLL_MONTH);
        }

        @Test
        void getAttendances_ShouldCallAllWorkingTimeUtilMethods() {
            // Arrange
            List<ProjectEntry> projectTimes = new ArrayList<>();
            List<ProjectTime> billableTimes = new ArrayList<>();
            List<AbsenceTime> absenceTimes = new ArrayList<>();

            when(zepService.getProjectTimes(testEmployee, TEST_PAYROLL_MONTH)).thenReturn(projectTimes);
            when(zepService.getBillableForEmployee(testEmployee, TEST_PAYROLL_MONTH)).thenReturn(billableTimes);
            when(zepService.getAbsenceForEmployee(testEmployee, TEST_PAYROLL_MONTH)).thenReturn(absenceTimes);

            when(workingTimeUtil.getTotalWorkingTimeForEmployee(projectTimes)).thenReturn("160:00");
            when(workingTimeUtil.getOvertimeForEmployee(testEmployee, projectTimes, absenceTimes, TEST_PAYROLL_MONTH)).thenReturn(10.0);
            when(workingTimeUtil.getBillableTimesForEmployee(billableTimes, testEmployee)).thenReturn("120:00");
            when(workingTimeUtil.getDurationFromTimeString("160:00")).thenReturn(Duration.ofHours(160));
            when(workingTimeUtil.getDurationFromTimeString("120:00")).thenReturn(Duration.ofHours(120));
            when(workingTimeUtil.getBillablePercentage(Duration.ofHours(160), Duration.ofHours(120))).thenReturn(75.0);

            // Act
            monthlyReportService.getAttendances(TEST_PAYROLL_MONTH);

            // Assert
            verify(workingTimeUtil).getTotalWorkingTimeForEmployee(projectTimes);
            verify(workingTimeUtil).getOvertimeForEmployee(testEmployee, projectTimes, absenceTimes, TEST_PAYROLL_MONTH);
            verify(workingTimeUtil).getBillableTimesForEmployee(billableTimes, testEmployee);
            verify(workingTimeUtil, times(2)).getDurationFromTimeString("160:00");
            verify(workingTimeUtil, times(2)).getDurationFromTimeString("120:00");
            verify(workingTimeUtil).getBillablePercentage(Duration.ofHours(160), Duration.ofHours(120));
        }
    }
}
