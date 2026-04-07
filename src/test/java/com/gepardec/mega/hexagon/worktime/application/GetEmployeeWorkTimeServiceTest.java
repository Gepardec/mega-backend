package com.gepardec.mega.hexagon.worktime.application;

import com.gepardec.mega.hexagon.project.domain.model.Project;
import com.gepardec.mega.hexagon.project.domain.model.ProjectId;
import com.gepardec.mega.hexagon.project.domain.port.outbound.ProjectRepository;
import com.gepardec.mega.hexagon.user.domain.model.EmploymentPeriods;
import com.gepardec.mega.hexagon.user.domain.model.RegularWorkingTimes;
import com.gepardec.mega.hexagon.user.domain.model.Role;
import com.gepardec.mega.hexagon.user.domain.model.User;
import com.gepardec.mega.hexagon.user.domain.model.UserId;
import com.gepardec.mega.hexagon.user.domain.model.ZepProfile;
import com.gepardec.mega.hexagon.user.domain.port.outbound.UserRepository;
import com.gepardec.mega.hexagon.worktime.domain.error.WorkTimeUserNotFoundException;
import com.gepardec.mega.hexagon.worktime.domain.error.WorkTimeValidationException;
import com.gepardec.mega.hexagon.worktime.domain.model.WorkTimeAttendance;
import com.gepardec.mega.hexagon.worktime.domain.model.WorkTimeReport;
import com.gepardec.mega.hexagon.worktime.domain.port.outbound.WorkTimeZepPort;
import io.smallrye.mutiny.Uni;
import org.assertj.core.api.ThrowableAssert;
import org.instancio.Instancio;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GetEmployeeWorkTimeServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private ProjectRepository projectRepository;

    @Mock
    private WorkTimeZepPort workTimeZepPort;

    @InjectMocks
    private GetEmployeeWorkTimeService service;

    @Test
    void getWorkTime_shouldAggregateHoursByProjectAndPreserveMonthTotal() {
        UserId employeeId = UserId.of(Instancio.create(UUID.class));
        ProjectId projectId = ProjectId.of(Instancio.create(UUID.class));
        ProjectId secondProjectId = ProjectId.of(Instancio.create(UUID.class));
        User user = user(employeeId, "ada", "Ada", "Lovelace");
        when(userRepository.findById(employeeId)).thenReturn(Optional.of(user));
        when(workTimeZepPort.fetchAttendancesForEmployee("ada", YearMonth.of(2026, 3))).thenReturn(Uni.createFrom().item(List.of(
                new WorkTimeAttendance("ada", 11, 2.5d, 0.0d),
                new WorkTimeAttendance("ada", 11, 0.0d, 1.5d),
                new WorkTimeAttendance("ada", 22, 3.0d, 0.0d)
        )));
        when(projectRepository.findByZepId(11)).thenReturn(Optional.of(project(projectId, 11, "Alpha")));
        when(projectRepository.findByZepId(22)).thenReturn(Optional.of(project(secondProjectId, 22, "Beta")));

        WorkTimeReport report = service.getWorkTime(employeeId, YearMonth.of(2026, 3));

        assertThat(report.payrollMonth()).isEqualTo(YearMonth.of(2026, 3));
        assertThat(report.entries()).hasSize(2);
        assertThat(report.entries()).anySatisfy(entry -> {
            assertThat(entry.project().id()).isEqualTo(projectId);
            assertThat(entry.billableHours()).isEqualTo(2.5d);
            assertThat(entry.nonBillableHours()).isEqualTo(1.5d);
            assertThat(entry.employeeMonthTotalHours()).isEqualTo(7.0d);
        });
        assertThat(report.entries()).anySatisfy(entry -> {
            assertThat(entry.project().id()).isEqualTo(secondProjectId);
            assertThat(entry.billableHours()).isEqualTo(3.0d);
            assertThat(entry.nonBillableHours()).isEqualTo(0.0d);
            assertThat(entry.employeeMonthTotalHours()).isEqualTo(7.0d);
        });
    }

    @Test
    void getWorkTime_shouldReturnEmptyReportWhenNoAttendancesExist() {
        UserId employeeId = UserId.of(Instancio.create(UUID.class));
        when(userRepository.findById(employeeId)).thenReturn(Optional.of(user(employeeId, "ada", "Ada", "Lovelace")));
        when(workTimeZepPort.fetchAttendancesForEmployee("ada", YearMonth.of(2026, 3))).thenReturn(Uni.createFrom().item(List.of()));

        WorkTimeReport report = service.getWorkTime(employeeId, YearMonth.of(2026, 3));

        assertThat(report.payrollMonth()).isEqualTo(YearMonth.of(2026, 3));
        assertThat(report.entries()).isEmpty();
    }

    @Test
    void getWorkTime_shouldThrowWorkTimeUserNotFoundExceptionWhenUserIsMissing() {
        UserId employeeId = UserId.of(Instancio.create(UUID.class));
        when(userRepository.findById(employeeId)).thenReturn(Optional.empty());

        ThrowableAssert.ThrowingCallable throwingCallable = () -> service.getWorkTime(employeeId, YearMonth.of(2026, 3));

        assertThatThrownBy(throwingCallable)
                .isInstanceOf(WorkTimeUserNotFoundException.class)
                .hasMessage("user not found: " + employeeId.value());
    }

    @Test
    void getWorkTime_shouldThrowWorkTimeValidationExceptionWhenZepUsernameIsMissing() {
        UserId employeeId = UserId.of(Instancio.create(UUID.class));
        when(userRepository.findById(employeeId)).thenReturn(Optional.of(userWithoutZepUsername(employeeId)));

        ThrowableAssert.ThrowingCallable throwingCallable = () -> service.getWorkTime(employeeId, YearMonth.of(2026, 3));

        assertThatThrownBy(throwingCallable)
                .isInstanceOf(WorkTimeValidationException.class)
                .hasMessage("zep username missing for user: " + employeeId.value());
    }

    private User user(UserId userId, String username, String firstname, String lastname) {
        return User.create(
                userId,
                new ZepProfile(
                        username,
                        username + "@example.com",
                        firstname,
                        lastname,
                        null,
                        null,
                        null,
                        null,
                        null,
                        EmploymentPeriods.empty(),
                        RegularWorkingTimes.empty()
                ),
                Set.of(Role.EMPLOYEE)
        );
    }

    private Project project(ProjectId projectId, int zepId, String name) {
        return Project.reconstitute(
                projectId,
                zepId,
                name,
                LocalDate.of(2024, 1, 1),
                null,
                true,
                Set.of()
        );
    }

    private User userWithoutZepUsername(UserId userId) {
        return User.create(
                userId,
                new ZepProfile(
                        null,
                        "missing@example.com",
                        "Ada",
                        "Lovelace",
                        null,
                        null,
                        null,
                        null,
                        null,
                        EmploymentPeriods.empty(),
                        RegularWorkingTimes.empty()
                ),
                Set.of(Role.EMPLOYEE)
        );
    }
}
