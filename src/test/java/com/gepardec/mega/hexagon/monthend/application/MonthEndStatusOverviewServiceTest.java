package com.gepardec.mega.hexagon.monthend.application;

import com.gepardec.mega.hexagon.monthend.domain.model.MonthEndStatusOverview;
import com.gepardec.mega.hexagon.monthend.domain.model.MonthEndStatusOverviewItem;
import com.gepardec.mega.hexagon.monthend.domain.model.MonthEndTask;
import com.gepardec.mega.hexagon.monthend.domain.model.MonthEndTaskId;
import com.gepardec.mega.hexagon.monthend.domain.model.MonthEndTaskStatus;
import com.gepardec.mega.hexagon.monthend.domain.model.MonthEndTaskType;
import com.gepardec.mega.hexagon.monthend.domain.port.outbound.MonthEndTaskRepository;
import com.gepardec.mega.hexagon.project.domain.model.ProjectId;
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
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MonthEndStatusOverviewServiceTest {

    private final YearMonth month = YearMonth.of(2026, 3);
    private final ProjectId projectId = ProjectId.of(UUID.fromString(Instancio.gen().text().uuid().get()));
    private final UserId actorId = UserId.of(UUID.fromString(Instancio.gen().text().uuid().get()));
    private final UserId subjectEmployeeId = UserId.of(UUID.fromString(Instancio.gen().text().uuid().get()));
    private final UserId leadAId = UserId.of(UUID.fromString(Instancio.gen().text().uuid().get()));

    @Mock
    private MonthEndTaskRepository monthEndTaskRepository;

    private GetMonthEndStatusOverviewService getMonthEndStatusOverviewService;

    @BeforeEach
    void setUp() {
        MonthEndStatusOverviewMapper mapper = Mappers.getMapper(MonthEndStatusOverviewMapper.class);
        getMonthEndStatusOverviewService = new GetMonthEndStatusOverviewService(monthEndTaskRepository, mapper);
    }

    @Test
    void getOverview_shouldReturnMixedOpenAndDoneTasksForActor() {
        MonthEndTask openEmployeeTask = MonthEndTask.create(
                MonthEndTaskId.generate(),
                month,
                MonthEndTaskType.EMPLOYEE_TIME_CHECK,
                projectId,
                actorId,
                Set.of(actorId)
        );
        MonthEndTask completedLeadTask = MonthEndTask.reconstitute(
                MonthEndTaskId.generate(),
                month,
                MonthEndTaskType.PROJECT_LEAD_REVIEW,
                projectId,
                subjectEmployeeId,
                Set.of(actorId, leadAId),
                MonthEndTaskStatus.DONE,
                leadAId
        );
        when(monthEndTaskRepository.findTasksForActor(actorId, month)).thenReturn(List.of(openEmployeeTask, completedLeadTask));

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
                    assertThat(item.projectId()).isEqualTo(projectId);
                    assertThat(item.subjectEmployeeId()).isEqualTo(actorId);
                    assertThat(item.completedBy()).isNull();
                });
        assertThat(overview.entries())
                .filteredOn(item -> item.taskId().equals(completedLeadTask.id()))
                .singleElement()
                .satisfies(item -> {
                    assertThat(item.type()).isEqualTo(MonthEndTaskType.PROJECT_LEAD_REVIEW);
                    assertThat(item.status()).isEqualTo(MonthEndTaskStatus.DONE);
                    assertThat(item.projectId()).isEqualTo(projectId);
                    assertThat(item.subjectEmployeeId()).isEqualTo(subjectEmployeeId);
                    assertThat(item.completedBy()).isEqualTo(leadAId);
                });
    }

    @Test
    void getOverview_shouldReturnEmptyEntriesWhenActorHasNoRelevantTasks() {
        when(monthEndTaskRepository.findTasksForActor(actorId, month)).thenReturn(List.of());

        MonthEndStatusOverview overview = getMonthEndStatusOverviewService.getOverview(actorId, month);

        assertThat(overview.actorId()).isEqualTo(actorId);
        assertThat(overview.month()).isEqualTo(month);
        assertThat(overview.entries()).isEmpty();
    }
}
