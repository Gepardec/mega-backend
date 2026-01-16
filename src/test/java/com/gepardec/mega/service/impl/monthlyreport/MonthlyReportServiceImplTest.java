package com.gepardec.mega.service.impl.monthlyreport;

import com.gepardec.mega.db.entity.employee.EmployeeState;
import com.gepardec.mega.db.entity.employee.Step;
import com.gepardec.mega.db.entity.employee.StepEntry;
import com.gepardec.mega.domain.model.AbsenceTime;
import com.gepardec.mega.domain.model.Attendances;
import com.gepardec.mega.domain.model.Comment;
import com.gepardec.mega.domain.model.Employee;
import com.gepardec.mega.domain.model.EmployeeCheck;
import com.gepardec.mega.domain.model.PrematureEmployeeCheck;
import com.gepardec.mega.domain.model.ProjectTime;
import com.gepardec.mega.domain.model.StepName;
import com.gepardec.mega.domain.model.User;
import com.gepardec.mega.domain.model.UserContext;
import com.gepardec.mega.domain.model.monthlyreport.ProjectEntry;
import com.gepardec.mega.service.api.CommentService;
import com.gepardec.mega.service.api.EmployeeService;
import com.gepardec.mega.service.api.MonthlyReportService;
import com.gepardec.mega.service.api.PrematureEmployeeCheckService;
import com.gepardec.mega.service.api.StepEntryService;
import com.gepardec.mega.service.helper.WorkingTimeUtil;
import com.gepardec.mega.zep.ZepService;
import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
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
    CommentService commentService;

    @InjectMock
    StepEntryService stepEntryService;

    @InjectMock
    UserContext userContext;

    @InjectMock
    EmployeeService employeeService;

    @InjectMock
    WorkingTimeUtil workingTimeUtil;

    @InjectMock
    PrematureEmployeeCheckService prematureEmployeeCheckService;

    private static final String TEST_USER_ID = "test-user-id";
    private static final String TEST_EMAIL = "test@gepardec.com";
    private static final YearMonth TEST_PAYROLL_MONTH = YearMonth.of(2026, 1);

    private Employee testEmployee;

    @BeforeEach
    void setUp() {
        User testUser = mock(User.class);
        when(testUser.getUserId()).thenReturn(TEST_USER_ID);
        when(testUser.getEmail()).thenReturn(TEST_EMAIL);
        when(userContext.getUser()).thenReturn(testUser);

        testEmployee = mock(Employee.class);
        when(testEmployee.getEmail()).thenReturn(TEST_EMAIL);
        when(testEmployee.getUserId()).thenReturn(TEST_USER_ID);
        when(employeeService.getEmployee(TEST_USER_ID)).thenReturn(testEmployee);
    }

    @Nested
    class GetEmployeeCheck {

        @Test
        void getEmployeeCheck_WhenEmployeeCheckStatePresent_ShouldReturnEmployeeCheckWithState() {
            // Arrange
            Pair<EmployeeState, String> employeeCheckState = Pair.of(EmployeeState.DONE, "All good");
            when(stepEntryService.findEmployeeCheckState(testEmployee, TEST_PAYROLL_MONTH))
                    .thenReturn(Optional.of(employeeCheckState));
            when(stepEntryService.findEmployeeInternalCheckState(testEmployee, TEST_PAYROLL_MONTH))
                    .thenReturn(Optional.empty());
            when(commentService.findCommentsForEmployee(TEST_EMAIL, TEST_PAYROLL_MONTH))
                    .thenReturn(Collections.emptyList());
            when(prematureEmployeeCheckService.findByEmailAndMonth(TEST_EMAIL, TEST_PAYROLL_MONTH))
                    .thenReturn(Optional.empty());
            when(stepEntryService.findAllOwnedAndUnassignedStepEntriesExceptControlTimes(testEmployee, TEST_PAYROLL_MONTH))
                    .thenReturn(Collections.emptyList());

            // Act
            EmployeeCheck result = monthlyReportService.getEmployeeCheck(TEST_PAYROLL_MONTH);

            // Assert
            assertThat(result).isNotNull();
            assertThat(result.employee()).isEqualTo(testEmployee);
            assertThat(result.employeeCheckState()).isEqualTo(EmployeeState.DONE);
            assertThat(result.employeeCheckStateReason()).isEqualTo("All good");
            assertThat(result.internalCheckState()).isNull();
        }

        @Test
        void getEmployeeCheck_WhenInternalCheckStatePresent_ShouldReturnEmployeeCheckWithInternalState() {
            // Arrange
            when(stepEntryService.findEmployeeCheckState(testEmployee, TEST_PAYROLL_MONTH))
                    .thenReturn(Optional.empty());
            when(stepEntryService.findEmployeeInternalCheckState(testEmployee, TEST_PAYROLL_MONTH))
                    .thenReturn(Optional.of(EmployeeState.IN_PROGRESS));
            when(commentService.findCommentsForEmployee(TEST_EMAIL, TEST_PAYROLL_MONTH))
                    .thenReturn(Collections.emptyList());
            when(prematureEmployeeCheckService.findByEmailAndMonth(TEST_EMAIL, TEST_PAYROLL_MONTH))
                    .thenReturn(Optional.empty());
            when(stepEntryService.findAllOwnedAndUnassignedStepEntriesExceptControlTimes(testEmployee, TEST_PAYROLL_MONTH))
                    .thenReturn(Collections.emptyList());

            // Act
            EmployeeCheck result = monthlyReportService.getEmployeeCheck(TEST_PAYROLL_MONTH);

            // Assert
            assertThat(result).isNotNull();
            assertThat(result.internalCheckState()).isEqualTo(EmployeeState.IN_PROGRESS);
        }

        @Test
        void getEmployeeCheck_WhenPrematureCheckAndCurrentMonth_ShouldUsePrematureCheckState() {
            // Arrange
            YearMonth currentMonth = YearMonth.now();
            when(stepEntryService.findEmployeeCheckState(testEmployee, currentMonth))
                    .thenReturn(Optional.empty());
            when(stepEntryService.findEmployeeInternalCheckState(testEmployee, currentMonth))
                    .thenReturn(Optional.empty());
            when(commentService.findCommentsForEmployee(TEST_EMAIL, currentMonth))
                    .thenReturn(Collections.emptyList());
            when(prematureEmployeeCheckService.findByEmailAndMonth(TEST_EMAIL, currentMonth))
                    .thenReturn(Optional.empty());
            when(stepEntryService.findAllOwnedAndUnassignedStepEntriesExceptControlTimes(testEmployee, currentMonth))
                    .thenReturn(Collections.emptyList());

            // Act
            EmployeeCheck result = monthlyReportService.getEmployeeCheck(currentMonth);

            // Assert
            assertThat(result).isNotNull();
            assertThat(result.employeeCheckState()).isEqualTo(EmployeeState.PREMATURE_CHECK);
        }

        @Test
        void getEmployeeCheck_WhenPrematureCheckAndPastMonth_ShouldReturnNullState() {
            // Arrange
            YearMonth pastMonth = YearMonth.now().minusMonths(2);
            when(stepEntryService.findEmployeeCheckState(testEmployee, pastMonth))
                    .thenReturn(Optional.empty());
            when(stepEntryService.findEmployeeInternalCheckState(testEmployee, pastMonth))
                    .thenReturn(Optional.empty());
            when(commentService.findCommentsForEmployee(TEST_EMAIL, pastMonth))
                    .thenReturn(Collections.emptyList());
            when(prematureEmployeeCheckService.findByEmailAndMonth(TEST_EMAIL, pastMonth))
                    .thenReturn(Optional.empty());
            when(stepEntryService.findAllOwnedAndUnassignedStepEntriesExceptControlTimes(testEmployee, pastMonth))
                    .thenReturn(Collections.emptyList());

            // Act
            EmployeeCheck result = monthlyReportService.getEmployeeCheck(pastMonth);

            // Assert
            assertThat(result).isNotNull();
            assertThat(result.employeeCheckState()).isNull();
        }

        @Test
        void getEmployeeCheck_WhenCommentsExist_ShouldIncludeComments() {
            // Arrange
            Comment comment1 = mock(Comment.class);
            Comment comment2 = mock(Comment.class);
            List<Comment> comments = List.of(comment1, comment2);

            when(stepEntryService.findEmployeeCheckState(testEmployee, TEST_PAYROLL_MONTH))
                    .thenReturn(Optional.empty());
            when(stepEntryService.findEmployeeInternalCheckState(testEmployee, TEST_PAYROLL_MONTH))
                    .thenReturn(Optional.empty());
            when(commentService.findCommentsForEmployee(TEST_EMAIL, TEST_PAYROLL_MONTH))
                    .thenReturn(comments);
            when(prematureEmployeeCheckService.findByEmailAndMonth(TEST_EMAIL, TEST_PAYROLL_MONTH))
                    .thenReturn(Optional.empty());
            when(stepEntryService.findAllOwnedAndUnassignedStepEntriesExceptControlTimes(testEmployee, TEST_PAYROLL_MONTH))
                    .thenReturn(Collections.emptyList());

            // Act
            EmployeeCheck result = monthlyReportService.getEmployeeCheck(TEST_PAYROLL_MONTH);

            // Assert
            assertThat(result).isNotNull();
            assertThat(result.comments()).hasSize(2);
            assertThat(result.comments()).containsExactly(comment1, comment2);
        }

        @Test
        void getEmployeeCheck_WhenPrematureEmployeeCheckExists_ShouldIncludePrematureCheck() {
            // Arrange
            PrematureEmployeeCheck prematureCheck = mock(PrematureEmployeeCheck.class);
            when(prematureCheck.getReason()).thenReturn("Premature reason");

            when(stepEntryService.findEmployeeCheckState(testEmployee, TEST_PAYROLL_MONTH))
                    .thenReturn(Optional.empty());
            when(stepEntryService.findEmployeeInternalCheckState(testEmployee, TEST_PAYROLL_MONTH))
                    .thenReturn(Optional.empty());
            when(commentService.findCommentsForEmployee(TEST_EMAIL, TEST_PAYROLL_MONTH))
                    .thenReturn(Collections.emptyList());
            when(prematureEmployeeCheckService.findByEmailAndMonth(TEST_EMAIL, TEST_PAYROLL_MONTH))
                    .thenReturn(Optional.of(prematureCheck));
            when(stepEntryService.findAllOwnedAndUnassignedStepEntriesExceptControlTimes(testEmployee, TEST_PAYROLL_MONTH))
                    .thenReturn(Collections.emptyList());

            // Act
            EmployeeCheck result = monthlyReportService.getEmployeeCheck(TEST_PAYROLL_MONTH);

            // Assert
            assertThat(result).isNotNull();
            assertThat(result.prematureEmployeeCheck()).isEqualTo(prematureCheck);
            assertThat(result.employeeCheckStateReason()).isEqualTo("Premature reason");
        }

        @Test
        void getEmployeeCheck_WhenStepEntryReasonExists_ShouldUseStepEntryReason() {
            // Arrange
            StepEntry stepEntry = mock(StepEntry.class);
            when(stepEntry.getStateReason()).thenReturn("Step entry reason");

            when(stepEntryService.findEmployeeCheckState(testEmployee, TEST_PAYROLL_MONTH))
                    .thenReturn(Optional.empty());
            when(stepEntryService.findEmployeeInternalCheckState(testEmployee, TEST_PAYROLL_MONTH))
                    .thenReturn(Optional.empty());
            when(commentService.findCommentsForEmployee(TEST_EMAIL, TEST_PAYROLL_MONTH))
                    .thenReturn(Collections.emptyList());
            when(prematureEmployeeCheckService.findByEmailAndMonth(TEST_EMAIL, TEST_PAYROLL_MONTH))
                    .thenReturn(Optional.empty());
            when(stepEntryService.findStepEntryForEmployeeAtStep(1L, TEST_EMAIL, TEST_EMAIL, TEST_PAYROLL_MONTH))
                    .thenReturn(stepEntry);
            when(stepEntryService.findAllOwnedAndUnassignedStepEntriesExceptControlTimes(testEmployee, TEST_PAYROLL_MONTH))
                    .thenReturn(Collections.emptyList());

            // Act
            EmployeeCheck result = monthlyReportService.getEmployeeCheck(TEST_PAYROLL_MONTH);

            // Assert
            assertThat(result).isNotNull();
            assertThat(result.employeeCheckStateReason()).isEqualTo("Step entry reason");
        }

        @Test
        void getEmployeeCheck_WhenStepEntryThrowsException_ShouldHandleGracefully() {
            // Arrange
            when(stepEntryService.findEmployeeCheckState(testEmployee, TEST_PAYROLL_MONTH))
                    .thenReturn(Optional.empty());
            when(stepEntryService.findEmployeeInternalCheckState(testEmployee, TEST_PAYROLL_MONTH))
                    .thenReturn(Optional.empty());
            when(commentService.findCommentsForEmployee(TEST_EMAIL, TEST_PAYROLL_MONTH))
                    .thenReturn(Collections.emptyList());
            when(prematureEmployeeCheckService.findByEmailAndMonth(TEST_EMAIL, TEST_PAYROLL_MONTH))
                    .thenReturn(Optional.empty());
            when(stepEntryService.findStepEntryForEmployeeAtStep(1L, TEST_EMAIL, TEST_EMAIL, TEST_PAYROLL_MONTH))
                    .thenThrow(new IllegalStateException("Test exception"));
            when(stepEntryService.findAllOwnedAndUnassignedStepEntriesExceptControlTimes(testEmployee, TEST_PAYROLL_MONTH))
                    .thenReturn(Collections.emptyList());

            // Act
            EmployeeCheck result = monthlyReportService.getEmployeeCheck(TEST_PAYROLL_MONTH);

            // Assert
            assertThat(result).isNotNull();
            assertThat(result.employeeCheckStateReason()).isNull();
        }

        @Nested
        class MonthCompletion {

            @Test
            void getEmployeeCheck_WhenAllStepsCompleted_ShouldReturnTrueForOtherChecksDone() {
                // Arrange
                StepEntry controlTimeEntry = createStepEntry(StepName.CONTROL_TIME_EVIDENCES.name(), "Project1", EmployeeState.DONE);
                StepEntry otherEntry = createStepEntry("OTHER_STEP", "Project1", EmployeeState.DONE);

                when(stepEntryService.findEmployeeCheckState(testEmployee, TEST_PAYROLL_MONTH))
                        .thenReturn(Optional.empty());
                when(stepEntryService.findEmployeeInternalCheckState(testEmployee, TEST_PAYROLL_MONTH))
                        .thenReturn(Optional.empty());
                when(commentService.findCommentsForEmployee(TEST_EMAIL, TEST_PAYROLL_MONTH))
                        .thenReturn(Collections.emptyList());
                when(prematureEmployeeCheckService.findByEmailAndMonth(TEST_EMAIL, TEST_PAYROLL_MONTH))
                        .thenReturn(Optional.empty());
                when(stepEntryService.findAllOwnedAndUnassignedStepEntriesExceptControlTimes(testEmployee, TEST_PAYROLL_MONTH))
                        .thenReturn(List.of(controlTimeEntry, otherEntry));

                // Act
                EmployeeCheck result = monthlyReportService.getEmployeeCheck(TEST_PAYROLL_MONTH);

                // Assert
                assertThat(result).isNotNull();
                assertThat(result.otherChecksDone()).isTrue();
            }

            @Test
            void getEmployeeCheck_WhenControlTimeEvidencesNotDone_ShouldReturnFalseForOtherChecksDone() {
                // Arrange
                StepEntry controlTimeEntry = createStepEntry(StepName.CONTROL_TIME_EVIDENCES.name(), "Project1", EmployeeState.IN_PROGRESS);

                when(stepEntryService.findEmployeeCheckState(testEmployee, TEST_PAYROLL_MONTH))
                        .thenReturn(Optional.empty());
                when(stepEntryService.findEmployeeInternalCheckState(testEmployee, TEST_PAYROLL_MONTH))
                        .thenReturn(Optional.empty());
                when(commentService.findCommentsForEmployee(TEST_EMAIL, TEST_PAYROLL_MONTH))
                        .thenReturn(Collections.emptyList());
                when(prematureEmployeeCheckService.findByEmailAndMonth(TEST_EMAIL, TEST_PAYROLL_MONTH))
                        .thenReturn(Optional.empty());
                when(stepEntryService.findAllOwnedAndUnassignedStepEntriesExceptControlTimes(testEmployee, TEST_PAYROLL_MONTH))
                        .thenReturn(List.of(controlTimeEntry));

                // Act
                EmployeeCheck result = monthlyReportService.getEmployeeCheck(TEST_PAYROLL_MONTH);

                // Assert
                assertThat(result).isNotNull();
                assertThat(result.otherChecksDone()).isFalse();
            }

            @Test
            void getEmployeeCheck_WhenOtherStepsNotDone_ShouldReturnFalseForOtherChecksDone() {
                // Arrange
                StepEntry controlTimeEntry = createStepEntry(StepName.CONTROL_TIME_EVIDENCES.name(), "Project1", EmployeeState.DONE);
                StepEntry otherEntry = createStepEntry("OTHER_STEP", "Project1", EmployeeState.IN_PROGRESS);

                when(stepEntryService.findEmployeeCheckState(testEmployee, TEST_PAYROLL_MONTH))
                        .thenReturn(Optional.empty());
                when(stepEntryService.findEmployeeInternalCheckState(testEmployee, TEST_PAYROLL_MONTH))
                        .thenReturn(Optional.empty());
                when(commentService.findCommentsForEmployee(TEST_EMAIL, TEST_PAYROLL_MONTH))
                        .thenReturn(Collections.emptyList());
                when(prematureEmployeeCheckService.findByEmailAndMonth(TEST_EMAIL, TEST_PAYROLL_MONTH))
                        .thenReturn(Optional.empty());
                when(stepEntryService.findAllOwnedAndUnassignedStepEntriesExceptControlTimes(testEmployee, TEST_PAYROLL_MONTH))
                        .thenReturn(List.of(controlTimeEntry, otherEntry));

                // Act
                EmployeeCheck result = monthlyReportService.getEmployeeCheck(TEST_PAYROLL_MONTH);

                // Assert
                assertThat(result).isNotNull();
                assertThat(result.otherChecksDone()).isFalse();
            }

            @Test
            void getEmployeeCheck_WhenMultipleProjectsAndOneControlTimeDone_ShouldReturnTrue() {
                // Arrange
                StepEntry controlTimeEntry1 = createStepEntry(StepName.CONTROL_TIME_EVIDENCES.name(), "Project1", EmployeeState.DONE);
                StepEntry controlTimeEntry2 = createStepEntry(StepName.CONTROL_TIME_EVIDENCES.name(), "Project2", EmployeeState.IN_PROGRESS);

                when(stepEntryService.findEmployeeCheckState(testEmployee, TEST_PAYROLL_MONTH))
                        .thenReturn(Optional.empty());
                when(stepEntryService.findEmployeeInternalCheckState(testEmployee, TEST_PAYROLL_MONTH))
                        .thenReturn(Optional.empty());
                when(commentService.findCommentsForEmployee(TEST_EMAIL, TEST_PAYROLL_MONTH))
                        .thenReturn(Collections.emptyList());
                when(prematureEmployeeCheckService.findByEmailAndMonth(TEST_EMAIL, TEST_PAYROLL_MONTH))
                        .thenReturn(Optional.empty());
                when(stepEntryService.findAllOwnedAndUnassignedStepEntriesExceptControlTimes(testEmployee, TEST_PAYROLL_MONTH))
                        .thenReturn(List.of(controlTimeEntry1, controlTimeEntry2));

                // Act
                EmployeeCheck result = monthlyReportService.getEmployeeCheck(TEST_PAYROLL_MONTH);

                // Assert
                assertThat(result).isNotNull();
                assertThat(result.otherChecksDone()).isFalse();
            }

            @Test
            void getEmployeeCheck_WhenNoStepEntries_ShouldReturnTrueForOtherChecksDone() {
                // Arrange
                when(stepEntryService.findEmployeeCheckState(testEmployee, TEST_PAYROLL_MONTH))
                        .thenReturn(Optional.empty());
                when(stepEntryService.findEmployeeInternalCheckState(testEmployee, TEST_PAYROLL_MONTH))
                        .thenReturn(Optional.empty());
                when(commentService.findCommentsForEmployee(TEST_EMAIL, TEST_PAYROLL_MONTH))
                        .thenReturn(Collections.emptyList());
                when(prematureEmployeeCheckService.findByEmailAndMonth(TEST_EMAIL, TEST_PAYROLL_MONTH))
                        .thenReturn(Optional.empty());
                when(stepEntryService.findAllOwnedAndUnassignedStepEntriesExceptControlTimes(testEmployee, TEST_PAYROLL_MONTH))
                        .thenReturn(Collections.emptyList());

                // Act
                EmployeeCheck result = monthlyReportService.getEmployeeCheck(TEST_PAYROLL_MONTH);

                // Assert
                assertThat(result).isNotNull();
                assertThat(result.otherChecksDone()).isTrue();
            }
        }
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

    private StepEntry createStepEntry(String stepName, String project, EmployeeState state) {
        StepEntry stepEntry = mock(StepEntry.class);
        Step step = mock(Step.class);
        when(step.getName()).thenReturn(stepName);
        when(stepEntry.getStep()).thenReturn(step);
        when(stepEntry.getProject()).thenReturn(project);
        when(stepEntry.getState()).thenReturn(state);
        return stepEntry;
    }
}
