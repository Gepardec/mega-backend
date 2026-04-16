package com.gepardec.mega.hexagon.monthend.application;

import com.gepardec.mega.hexagon.monthend.domain.model.MonthEndProjectSnapshot;
import com.gepardec.mega.hexagon.monthend.domain.model.MonthEndStatusOverview;
import com.gepardec.mega.hexagon.monthend.domain.model.MonthEndTask;
import com.gepardec.mega.hexagon.monthend.domain.model.MonthEndTaskId;
import com.gepardec.mega.hexagon.monthend.domain.model.MonthEndTaskStatus;
import com.gepardec.mega.hexagon.monthend.domain.model.MonthEndTaskType;
import com.gepardec.mega.hexagon.monthend.domain.port.outbound.MonthEndProjectSnapshotPort;
import com.gepardec.mega.hexagon.monthend.domain.port.outbound.MonthEndTaskRepository;
import com.gepardec.mega.hexagon.monthend.domain.port.outbound.MonthEndUserSnapshotPort;
import com.gepardec.mega.hexagon.shared.domain.model.FullName;
import com.gepardec.mega.hexagon.shared.domain.model.ProjectId;
import com.gepardec.mega.hexagon.shared.domain.model.UserId;
import com.gepardec.mega.hexagon.shared.domain.model.UserRef;
import com.gepardec.mega.hexagon.shared.domain.model.ZepUsername;
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
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GetProjectLeadMonthEndStatusOverviewServiceTest {

    private final YearMonth month = YearMonth.of(2026, 3);
    private final ProjectId projectId = ProjectId.of(UUID.fromString(Instancio.gen().text().uuid().get()));
    private final String projectName = "Lead Project";
    private final UserId leadId = UserId.of(UUID.fromString(Instancio.gen().text().uuid().get()));
    private final UserId subjectEmployeeId = UserId.of(UUID.fromString(Instancio.gen().text().uuid().get()));
    private final String subjectEmployeeName = "Subject Employee";

    @Mock
    private MonthEndTaskRepository monthEndTaskRepository;

    @Mock
    private MonthEndProjectSnapshotPort monthEndProjectSnapshotPort;

    @Mock
    private MonthEndUserSnapshotPort monthEndUserSnapshotPort;

    private GetProjectLeadMonthEndStatusOverviewService service;

    @BeforeEach
    void setUp() {
        ResolveMonthEndTaskSnapshotLookupService resolveMonthEndTaskSnapshotLookupService =
                new ResolveMonthEndTaskSnapshotLookupService(monthEndProjectSnapshotPort, monthEndUserSnapshotPort);
        service = new GetProjectLeadMonthEndStatusOverviewService(
                monthEndTaskRepository,
                resolveMonthEndTaskSnapshotLookupService
        );
    }

    @Test
    void getOverview_shouldReturnAllTaskTypesForLedProjectsWithCanCompleteFlags() {
        MonthEndTask etcTask = MonthEndTask.create(
                MonthEndTaskId.generate(), month, MonthEndTaskType.EMPLOYEE_TIME_CHECK,
                projectId, subjectEmployeeId, Set.of(subjectEmployeeId)
        );
        MonthEndTask plrTask = MonthEndTask.create(
                MonthEndTaskId.generate(), month, MonthEndTaskType.PROJECT_LEAD_REVIEW,
                projectId, subjectEmployeeId, Set.of(leadId)
        );
        MonthEndTask abrechnungTask = MonthEndTask.create(
                MonthEndTaskId.generate(), month, MonthEndTaskType.ABRECHNUNG,
                projectId, null, Set.of(leadId)
        );
        when(monthEndTaskRepository.findLeadProjectTasks(leadId, month))
                .thenReturn(List.of(etcTask, plrTask, abrechnungTask));
        when(monthEndProjectSnapshotPort.findByIds(Set.of(projectId), month))
                .thenReturn(List.of(snapshot()));
        when(monthEndUserSnapshotPort.findByIds(Set.of(subjectEmployeeId), month))
                .thenReturn(List.of(userRef(subjectEmployeeId, subjectEmployeeName, "subject")));

        MonthEndStatusOverview overview = service.getOverview(leadId, month);

        assertThat(overview.actorId()).isEqualTo(leadId);
        assertThat(overview.month()).isEqualTo(month);
        assertThat(overview.entries()).hasSize(3);
        assertThat(overview.entries())
                .filteredOn(item -> item.taskId().equals(etcTask.id()))
                .singleElement()
                .satisfies(item -> {
                    assertThat(item.type()).isEqualTo(MonthEndTaskType.EMPLOYEE_TIME_CHECK);
                    assertThat(item.canComplete()).isFalse();
                });
        assertThat(overview.entries())
                .filteredOn(item -> item.taskId().equals(plrTask.id()))
                .singleElement()
                .satisfies(item -> {
                    assertThat(item.type()).isEqualTo(MonthEndTaskType.PROJECT_LEAD_REVIEW);
                    assertThat(item.canComplete()).isTrue();
                });
        assertThat(overview.entries())
                .filteredOn(item -> item.taskId().equals(abrechnungTask.id()))
                .singleElement()
                .satisfies(item -> {
                    assertThat(item.type()).isEqualTo(MonthEndTaskType.ABRECHNUNG);
                    assertThat(item.subjectEmployee()).isNull();
                    assertThat(item.canComplete()).isTrue();
                });
    }

    @Test
    void getOverview_shouldReturnEmptyEntriesWhenLeadHasNoProjects() {
        when(monthEndTaskRepository.findLeadProjectTasks(leadId, month)).thenReturn(List.of());

        MonthEndStatusOverview overview = service.getOverview(leadId, month);

        assertThat(overview.actorId()).isEqualTo(leadId);
        assertThat(overview.entries()).isEmpty();
        verifyNoInteractions(monthEndProjectSnapshotPort, monthEndUserSnapshotPort);
    }

    @Test
    void getOverview_shouldIncludeDoneTasksForLedProjects() {
        MonthEndTask completedPlr = new MonthEndTask(
                MonthEndTaskId.generate(), month, MonthEndTaskType.PROJECT_LEAD_REVIEW,
                projectId, subjectEmployeeId, Set.of(leadId), MonthEndTaskStatus.DONE, leadId
        );
        when(monthEndTaskRepository.findLeadProjectTasks(leadId, month))
                .thenReturn(List.of(completedPlr));
        when(monthEndProjectSnapshotPort.findByIds(Set.of(projectId), month))
                .thenReturn(List.of(snapshot()));
        when(monthEndUserSnapshotPort.findByIds(Set.of(subjectEmployeeId), month))
                .thenReturn(List.of(userRef(subjectEmployeeId, subjectEmployeeName, "subject")));

        MonthEndStatusOverview overview = service.getOverview(leadId, month);

        assertThat(overview.entries()).singleElement().satisfies(item -> {
            assertThat(item.status()).isEqualTo(MonthEndTaskStatus.DONE);
            assertThat(item.completedBy()).isEqualTo(leadId);
            assertThat(item.canComplete()).isTrue();
        });
    }

    private MonthEndProjectSnapshot snapshot() {
        return new MonthEndProjectSnapshot(projectId, 77, projectName, true, Set.of(leadId));
    }

    private UserRef userRef(UserId id, String fullName, String username) {
        String[] parts = fullName.split(" ", 2);
        return new UserRef(id, FullName.of(parts[0], parts.length > 1 ? parts[1] : null), ZepUsername.of(username));
    }
}
