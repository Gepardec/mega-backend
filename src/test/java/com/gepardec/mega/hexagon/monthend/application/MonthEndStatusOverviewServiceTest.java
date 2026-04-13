package com.gepardec.mega.hexagon.monthend.application;

import com.gepardec.mega.hexagon.monthend.domain.model.MonthEndEmployee;
import com.gepardec.mega.hexagon.monthend.domain.model.MonthEndProject;
import com.gepardec.mega.hexagon.monthend.domain.model.MonthEndProjectSnapshot;
import com.gepardec.mega.hexagon.monthend.domain.model.MonthEndStatusOverview;
import com.gepardec.mega.hexagon.monthend.domain.model.MonthEndStatusOverviewItem;
import com.gepardec.mega.hexagon.monthend.domain.model.MonthEndTask;
import com.gepardec.mega.hexagon.monthend.domain.model.MonthEndTaskId;
import com.gepardec.mega.hexagon.monthend.domain.model.MonthEndTaskStatus;
import com.gepardec.mega.hexagon.monthend.domain.model.MonthEndTaskType;
import com.gepardec.mega.hexagon.monthend.domain.model.MonthEndUserSnapshot;
import com.gepardec.mega.hexagon.monthend.domain.port.outbound.MonthEndProjectSnapshotPort;
import com.gepardec.mega.hexagon.monthend.domain.port.outbound.MonthEndTaskRepository;
import com.gepardec.mega.hexagon.monthend.domain.port.outbound.MonthEndUserSnapshotPort;
import com.gepardec.mega.hexagon.project.domain.model.ProjectId;
import com.gepardec.mega.hexagon.user.domain.model.EmploymentPeriod;
import com.gepardec.mega.hexagon.user.domain.model.EmploymentPeriods;
import com.gepardec.mega.hexagon.user.domain.model.UserId;
import org.instancio.Instancio;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
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
class MonthEndStatusOverviewServiceTest {

    private final YearMonth month = YearMonth.of(2026, 3);
    private final ProjectId projectId = ProjectId.of(UUID.fromString(Instancio.gen().text().uuid().get()));
    private final String projectName = "Project Alpha";
    private final UserId actorId = UserId.of(UUID.fromString(Instancio.gen().text().uuid().get()));
    private final String actorName = "Actor Example";
    private final UserId subjectEmployeeId = UserId.of(UUID.fromString(Instancio.gen().text().uuid().get()));
    private final String subjectEmployeeName = "Employee Example";
    private final UserId leadAId = UserId.of(UUID.fromString(Instancio.gen().text().uuid().get()));

    @Mock
    private MonthEndTaskRepository monthEndTaskRepository;

    @Mock
    private MonthEndProjectSnapshotPort monthEndProjectSnapshotPort;

    @Mock
    private MonthEndUserSnapshotPort monthEndUserSnapshotPort;

    private GetMonthEndStatusOverviewService getMonthEndStatusOverviewService;

    @BeforeEach
    void setUp() {
        MonthEndStatusOverviewMapper mapper = Mappers.getMapper(MonthEndStatusOverviewMapper.class);
        ResolveMonthEndTaskSnapshotLookupService resolveMonthEndTaskSnapshotLookupService =
                new ResolveMonthEndTaskSnapshotLookupService(monthEndProjectSnapshotPort, monthEndUserSnapshotPort);
        getMonthEndStatusOverviewService = new GetMonthEndStatusOverviewService(
                monthEndTaskRepository,
                resolveMonthEndTaskSnapshotLookupService,
                mapper
        );
    }

    @Test
    void getOverview_shouldReturnMixedOpenAndDoneVisibleTasksWithCanCompleteFlags() {
        MonthEndTask openEmployeeTask = MonthEndTask.create(
                MonthEndTaskId.generate(),
                month,
                MonthEndTaskType.EMPLOYEE_TIME_CHECK,
                projectId,
                actorId,
                Set.of(actorId)
        );
        MonthEndTask completedLeadTask = new MonthEndTask(
                MonthEndTaskId.generate(),
                month,
                MonthEndTaskType.PROJECT_LEAD_REVIEW,
                projectId,
                subjectEmployeeId,
                Set.of(actorId, leadAId),
                MonthEndTaskStatus.DONE,
                leadAId
        );
        when(monthEndTaskRepository.findVisibleTasksForActor(actorId, month))
                .thenReturn(List.of(openEmployeeTask, completedLeadTask));
        when(monthEndProjectSnapshotPort.findByIds(Set.of(projectId)))
                .thenReturn(List.of(snapshot(projectName)));
        when(monthEndUserSnapshotPort.findByIds(Set.of(actorId, subjectEmployeeId)))
                .thenReturn(List.of(
                        userSnapshot(actorId, actorName, "actor"),
                        userSnapshot(subjectEmployeeId, subjectEmployeeName, "employee")
                ));

        MonthEndStatusOverview overview = getMonthEndStatusOverviewService.getOverview(actorId, month);

        assertThat(overview.actorId()).isEqualTo(actorId);
        assertThat(overview.month()).isEqualTo(month);
        assertThat(overview.entries()).hasSize(2);
        assertThat(overview.entries())
                .extracting(MonthEndStatusOverviewItem::taskId)
                .containsExactlyInAnyOrder(openEmployeeTask.id(), completedLeadTask.id());
        assertThat(overview.entries())
                .filteredOn(item -> item.taskId().equals(openEmployeeTask.id()))
                .singleElement()
                .satisfies(item -> {
                    assertThat(item.type()).isEqualTo(MonthEndTaskType.EMPLOYEE_TIME_CHECK);
                    assertThat(item.status()).isEqualTo(MonthEndTaskStatus.OPEN);
                    assertThat(item.project()).isEqualTo(new MonthEndProject(projectId, projectName));
                    assertThat(item.subjectEmployee()).isEqualTo(new MonthEndEmployee(actorId, actorName));
                    assertThat(item.canComplete()).isTrue();
                    assertThat(item.completedBy()).isNull();
                });
        assertThat(overview.entries())
                .filteredOn(item -> item.taskId().equals(completedLeadTask.id()))
                .singleElement()
                .satisfies(item -> {
                    assertThat(item.type()).isEqualTo(MonthEndTaskType.PROJECT_LEAD_REVIEW);
                    assertThat(item.status()).isEqualTo(MonthEndTaskStatus.DONE);
                    assertThat(item.project()).isEqualTo(new MonthEndProject(projectId, projectName));
                    assertThat(item.subjectEmployee()).isEqualTo(new MonthEndEmployee(subjectEmployeeId, subjectEmployeeName));
                    assertThat(item.canComplete()).isTrue();
                    assertThat(item.completedBy()).isEqualTo(leadAId);
                });
    }

    @Test
    void getOverview_shouldReturnEmptyEntriesWhenActorHasNoRelevantTasks() {
        when(monthEndTaskRepository.findVisibleTasksForActor(actorId, month)).thenReturn(List.of());

        MonthEndStatusOverview overview = getMonthEndStatusOverviewService.getOverview(actorId, month);

        assertThat(overview.actorId()).isEqualTo(actorId);
        assertThat(overview.month()).isEqualTo(month);
        assertThat(overview.entries()).isEmpty();
        verifyNoInteractions(monthEndProjectSnapshotPort, monthEndUserSnapshotPort);
    }

    @Test
    void getOverview_shouldOmitSubjectEmployeeForAbrechnungTasks() {
        MonthEndTask abrechnungTask = MonthEndTask.create(
                MonthEndTaskId.generate(),
                month,
                MonthEndTaskType.ABRECHNUNG,
                projectId,
                null,
                Set.of(actorId)
        );
        when(monthEndTaskRepository.findVisibleTasksForActor(actorId, month)).thenReturn(List.of(abrechnungTask));
        when(monthEndProjectSnapshotPort.findByIds(Set.of(projectId)))
                .thenReturn(List.of(snapshot(projectName)));

        MonthEndStatusOverview overview = getMonthEndStatusOverviewService.getOverview(actorId, month);

        assertThat(overview.entries()).singleElement()
                .satisfies(item -> {
                    assertThat(item.type()).isEqualTo(MonthEndTaskType.ABRECHNUNG);
                    assertThat(item.project()).isEqualTo(new MonthEndProject(projectId, projectName));
                    assertThat(item.subjectEmployee()).isNull();
                    assertThat(item.canComplete()).isTrue();
                    assertThat(item.completedBy()).isNull();
                });
        verifyNoInteractions(monthEndUserSnapshotPort);
    }

    @Test
    void getOverview_shouldIncludeSubjectOnlyTasksWithCanCompleteFalse() {
        MonthEndTask subjectOnlyTask = MonthEndTask.create(
                MonthEndTaskId.generate(),
                month,
                MonthEndTaskType.PROJECT_LEAD_REVIEW,
                projectId,
                actorId,
                Set.of(leadAId)
        );
        when(monthEndTaskRepository.findVisibleTasksForActor(actorId, month)).thenReturn(List.of(subjectOnlyTask));
        when(monthEndProjectSnapshotPort.findByIds(Set.of(projectId)))
                .thenReturn(List.of(snapshot(projectName)));
        when(monthEndUserSnapshotPort.findByIds(Set.of(actorId)))
                .thenReturn(List.of(userSnapshot(actorId, actorName, "actor")));

        MonthEndStatusOverview overview = getMonthEndStatusOverviewService.getOverview(actorId, month);

        assertThat(overview.entries()).singleElement()
                .satisfies(item -> {
                    assertThat(item.taskId()).isEqualTo(subjectOnlyTask.id());
                    assertThat(item.type()).isEqualTo(MonthEndTaskType.PROJECT_LEAD_REVIEW);
                    assertThat(item.status()).isEqualTo(MonthEndTaskStatus.OPEN);
                    assertThat(item.project()).isEqualTo(new MonthEndProject(projectId, projectName));
                    assertThat(item.subjectEmployee()).isEqualTo(new MonthEndEmployee(actorId, actorName));
                    assertThat(item.canComplete()).isFalse();
                    assertThat(item.completedBy()).isNull();
                });
    }

    private MonthEndProjectSnapshot snapshot(String name) {
        return new MonthEndProjectSnapshot(
                projectId,
                77,
                name,
                month.atDay(1),
                null,
                true,
                Set.of(leadAId)
        );
    }

    private MonthEndUserSnapshot userSnapshot(UserId id, String fullName, String username) {
        return new MonthEndUserSnapshot(
                id,
                fullName,
                username,
                new EmploymentPeriods(new EmploymentPeriod(month.atDay(1).minusYears(1), null))
        );
    }
}
