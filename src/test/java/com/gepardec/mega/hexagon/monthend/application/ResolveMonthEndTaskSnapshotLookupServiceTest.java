package com.gepardec.mega.hexagon.monthend.application;

import com.gepardec.mega.hexagon.monthend.domain.model.MonthEndProjectSnapshot;
import com.gepardec.mega.hexagon.monthend.domain.model.MonthEndTask;
import com.gepardec.mega.hexagon.monthend.domain.model.MonthEndTaskId;
import com.gepardec.mega.hexagon.monthend.domain.model.MonthEndTaskType;
import com.gepardec.mega.hexagon.monthend.domain.model.MonthEndUserSnapshot;
import com.gepardec.mega.hexagon.monthend.domain.port.outbound.MonthEndProjectSnapshotPort;
import com.gepardec.mega.hexagon.monthend.domain.port.outbound.MonthEndUserSnapshotPort;
import com.gepardec.mega.hexagon.shared.domain.model.ProjectId;
import com.gepardec.mega.hexagon.shared.domain.model.UserId;
import com.gepardec.mega.hexagon.user.domain.model.EmploymentPeriod;
import com.gepardec.mega.hexagon.user.domain.model.EmploymentPeriods;
import org.instancio.Instancio;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.YearMonth;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ResolveMonthEndTaskSnapshotLookupServiceTest {

    private final YearMonth month = YearMonth.of(2026, 3);
    private final ProjectId projectId = ProjectId.of(UUID.fromString(Instancio.gen().text().uuid().get()));
    private final UserId employeeId = UserId.of(UUID.fromString(Instancio.gen().text().uuid().get()));
    private final UserId actorId = UserId.of(UUID.fromString(Instancio.gen().text().uuid().get()));
    private final UserId leadId = UserId.of(UUID.fromString(Instancio.gen().text().uuid().get()));

    @Mock
    private MonthEndProjectSnapshotPort monthEndProjectSnapshotPort;

    @Mock
    private MonthEndUserSnapshotPort monthEndUserSnapshotPort;

    private ResolveMonthEndTaskSnapshotLookupService resolveMonthEndTaskSnapshotLookupService;

    @BeforeEach
    void setUp() {
        resolveMonthEndTaskSnapshotLookupService = new ResolveMonthEndTaskSnapshotLookupService(
                monthEndProjectSnapshotPort,
                monthEndUserSnapshotPort
        );
    }

    @Test
    void resolve_shouldReturnProjectAndSubjectEmployeeSnapshots_whenTaskContainsBothReferences() {
        MonthEndTask task = MonthEndTask.create(
                MonthEndTaskId.generate(),
                month,
                MonthEndTaskType.PROJECT_LEAD_REVIEW,
                projectId,
                employeeId,
                Set.of(actorId)
        );
        MonthEndProjectSnapshot projectSnapshot = projectSnapshot();
        MonthEndUserSnapshot userSnapshot = userSnapshot(employeeId, "Employee Example");
        when(monthEndProjectSnapshotPort.findByIds(Set.of(projectId))).thenReturn(List.of(projectSnapshot));
        when(monthEndUserSnapshotPort.findByIds(Set.of(employeeId))).thenReturn(List.of(userSnapshot));

        MonthEndTaskSnapshotLookup lookup = resolveMonthEndTaskSnapshotLookupService.resolve(List.of(task));

        assertThat(lookup.projectFor(projectId)).isEqualTo(projectSnapshot);
        assertThat(lookup.subjectEmployeeFor(employeeId)).isEqualTo(userSnapshot);
    }

    @Test
    void resolve_shouldReturnNullSubjectEmployeeAndSkipUserLookup_whenTaskDoesNotReferenceEmployee() {
        MonthEndTask task = MonthEndTask.create(
                MonthEndTaskId.generate(),
                month,
                MonthEndTaskType.ABRECHNUNG,
                projectId,
                null,
                Set.of(actorId)
        );
        MonthEndProjectSnapshot projectSnapshot = projectSnapshot();
        when(monthEndProjectSnapshotPort.findByIds(Set.of(projectId))).thenReturn(List.of(projectSnapshot));

        MonthEndTaskSnapshotLookup lookup = resolveMonthEndTaskSnapshotLookupService.resolve(List.of(task));

        assertThat(lookup.projectFor(projectId)).isEqualTo(projectSnapshot);
        assertThat(lookup.subjectEmployeeFor(null)).isNull();
        verifyNoInteractions(monthEndUserSnapshotPort);
    }

    @Test
    void projectFor_shouldThrow_whenProjectSnapshotIsMissing() {
        MonthEndTask task = MonthEndTask.create(
                MonthEndTaskId.generate(),
                month,
                MonthEndTaskType.PROJECT_LEAD_REVIEW,
                projectId,
                employeeId,
                Set.of(actorId)
        );
        when(monthEndProjectSnapshotPort.findByIds(Set.of(projectId))).thenReturn(List.of());
        when(monthEndUserSnapshotPort.findByIds(Set.of(employeeId)))
                .thenReturn(List.of(userSnapshot(employeeId, "Employee Example")));

        MonthEndTaskSnapshotLookup lookup = resolveMonthEndTaskSnapshotLookupService.resolve(List.of(task));

        assertThatThrownBy(() -> lookup.projectFor(projectId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("project snapshot not found for project %s".formatted(projectId.value()));
    }

    @Test
    void subjectEmployeeFor_shouldThrow_whenSubjectEmployeeSnapshotIsMissing() {
        MonthEndTask task = MonthEndTask.create(
                MonthEndTaskId.generate(),
                month,
                MonthEndTaskType.PROJECT_LEAD_REVIEW,
                projectId,
                employeeId,
                Set.of(actorId)
        );
        when(monthEndProjectSnapshotPort.findByIds(Set.of(projectId))).thenReturn(List.of(projectSnapshot()));
        when(monthEndUserSnapshotPort.findByIds(Set.of(employeeId))).thenReturn(List.of());

        MonthEndTaskSnapshotLookup lookup = resolveMonthEndTaskSnapshotLookupService.resolve(List.of(task));

        assertThatThrownBy(() -> lookup.subjectEmployeeFor(employeeId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("subject employee snapshot not found for employee %s".formatted(employeeId.value()));
    }

    private MonthEndProjectSnapshot projectSnapshot() {
        return new MonthEndProjectSnapshot(
                projectId,
                77,
                "Project Alpha",
                month.atDay(1),
                null,
                true,
                Set.of(leadId)
        );
    }

    private MonthEndUserSnapshot userSnapshot(UserId id, String fullName) {
        return new MonthEndUserSnapshot(
                id,
                fullName,
                "employee",
                new EmploymentPeriods(new EmploymentPeriod(month.atDay(1).minusYears(1), null))
        );
    }
}
