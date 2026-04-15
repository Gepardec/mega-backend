package com.gepardec.mega.hexagon.worktime.application;

import com.gepardec.mega.hexagon.shared.domain.model.FullName;
import com.gepardec.mega.hexagon.shared.domain.model.ProjectId;
import com.gepardec.mega.hexagon.shared.domain.model.ProjectRef;
import com.gepardec.mega.hexagon.shared.domain.model.UserId;
import com.gepardec.mega.hexagon.shared.domain.model.UserRef;
import com.gepardec.mega.hexagon.shared.domain.model.ZepUsername;
import com.gepardec.mega.hexagon.worktime.domain.error.WorkTimeUserNotFoundException;
import com.gepardec.mega.hexagon.worktime.domain.error.WorkTimeValidationException;
import com.gepardec.mega.hexagon.worktime.domain.model.WorkTimeAttendance;
import com.gepardec.mega.hexagon.worktime.domain.model.WorkTimeReport;
import com.gepardec.mega.hexagon.worktime.domain.port.outbound.WorkTimeProjectSnapshotPort;
import com.gepardec.mega.hexagon.worktime.domain.port.outbound.WorkTimeUserSnapshotPort;
import com.gepardec.mega.hexagon.worktime.domain.port.outbound.WorkTimeZepPort;
import com.gepardec.mega.hexagon.worktime.domain.services.WorkTimeReportAssembler;
import io.smallrye.mutiny.Uni;
import org.assertj.core.api.ThrowableAssert;
import org.instancio.Instancio;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.YearMonth;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GetEmployeeWorkTimeServiceTest {

    @Mock
    private WorkTimeUserSnapshotPort workTimeUserSnapshotPort;

    @Mock
    private WorkTimeProjectSnapshotPort workTimeProjectSnapshotPort;

    @Mock
    private WorkTimeZepPort workTimeZepPort;

    private GetEmployeeWorkTimeService service;

    @BeforeEach
    void setUp() {
        service = new GetEmployeeWorkTimeService(
                workTimeUserSnapshotPort,
                workTimeProjectSnapshotPort,
                workTimeZepPort,
                new WorkTimeReportAssembler()
        );
    }

    @Test
    void getWorkTime_shouldAggregateHoursByProjectAndPreserveMonthTotal() {
        YearMonth month = YearMonth.of(2026, 3);
        UserId employeeId = UserId.of(Instancio.create(UUID.class));
        ProjectId projectId = ProjectId.of(Instancio.create(UUID.class));
        ProjectId secondProjectId = ProjectId.of(Instancio.create(UUID.class));
        UserRef user = user(employeeId, "ada", "Ada Lovelace");
        when(workTimeUserSnapshotPort.findById(employeeId, month)).thenReturn(Optional.of(user));
        when(workTimeZepPort.fetchAttendancesForEmployee("ada", month)).thenReturn(Uni.createFrom().item(List.of(
                new WorkTimeAttendance("ada", 11, 2.5d, 0.0d),
                new WorkTimeAttendance("ada", 11, 0.0d, 1.5d),
                new WorkTimeAttendance("ada", 22, 3.0d, 0.0d)
        )));
        when(workTimeProjectSnapshotPort.findByZepId(11, month)).thenReturn(Optional.of(project(projectId, 11, "Alpha")));
        when(workTimeProjectSnapshotPort.findByZepId(22, month)).thenReturn(Optional.of(project(secondProjectId, 22, "Beta")));

        WorkTimeReport report = service.getWorkTime(employeeId, month);

        assertThat(report.payrollMonth()).isEqualTo(month);
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
        YearMonth month = YearMonth.of(2026, 3);
        UserId employeeId = UserId.of(Instancio.create(UUID.class));
        when(workTimeUserSnapshotPort.findById(employeeId, month)).thenReturn(Optional.of(user(employeeId, "ada", "Ada Lovelace")));
        when(workTimeZepPort.fetchAttendancesForEmployee("ada", month)).thenReturn(Uni.createFrom().item(List.of()));

        WorkTimeReport report = service.getWorkTime(employeeId, month);

        assertThat(report.payrollMonth()).isEqualTo(month);
        assertThat(report.entries()).isEmpty();
    }

    @Test
    void getWorkTime_shouldThrowWorkTimeUserNotFoundExceptionWhenUserIsMissing() {
        UserId employeeId = UserId.of(Instancio.create(UUID.class));
        when(workTimeUserSnapshotPort.findById(employeeId, YearMonth.of(2026, 3))).thenReturn(Optional.empty());

        ThrowableAssert.ThrowingCallable throwingCallable = () -> service.getWorkTime(employeeId, YearMonth.of(2026, 3));

        assertThatThrownBy(throwingCallable)
                .isInstanceOf(WorkTimeUserNotFoundException.class)
                .hasMessage("user not found: " + employeeId.value());
    }

    @Test
    void getWorkTime_shouldThrowWorkTimeValidationExceptionWhenZepUsernameIsMissing() {
        UserId employeeId = UserId.of(Instancio.create(UUID.class));
        when(workTimeUserSnapshotPort.findById(employeeId, YearMonth.of(2026, 3))).thenReturn(Optional.of(userWithoutZepUsername(employeeId)));

        ThrowableAssert.ThrowingCallable throwingCallable = () -> service.getWorkTime(employeeId, YearMonth.of(2026, 3));

        assertThatThrownBy(throwingCallable)
                .isInstanceOf(WorkTimeValidationException.class)
                .hasMessage("zep username missing for user: " + employeeId.value());
    }

    private UserRef user(UserId userId, String username, String fullName) {
        return new UserRef(userId, toFullName(fullName), ZepUsername.of(username));
    }

    private ProjectRef project(ProjectId projectId, int zepId, String name) {
        return new ProjectRef(projectId, zepId, name);
    }

    private UserRef userWithoutZepUsername(UserId userId) {
        return new UserRef(userId, FullName.of("Ada", "Lovelace"), ZepUsername.of(""));
    }

    private FullName toFullName(String fullName) {
        String[] parts = fullName.split(" ", 2);
        return FullName.of(parts[0], parts.length > 1 ? parts[1] : null);
    }
}
