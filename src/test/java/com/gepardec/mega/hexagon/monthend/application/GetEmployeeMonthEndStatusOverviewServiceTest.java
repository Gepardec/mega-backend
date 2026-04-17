package com.gepardec.mega.hexagon.monthend.application;

import com.gepardec.mega.hexagon.monthend.domain.model.MonthEndClarification;
import com.gepardec.mega.hexagon.monthend.domain.model.MonthEndClarificationId;
import com.gepardec.mega.hexagon.monthend.domain.model.MonthEndOverviewClarificationItem;
import com.gepardec.mega.hexagon.monthend.domain.model.MonthEndStatusOverview;
import com.gepardec.mega.hexagon.monthend.domain.model.MonthEndStatusOverviewItem;
import com.gepardec.mega.hexagon.monthend.domain.model.MonthEndTask;
import com.gepardec.mega.hexagon.monthend.domain.model.MonthEndTaskId;
import com.gepardec.mega.hexagon.monthend.domain.model.MonthEndTaskStatus;
import com.gepardec.mega.hexagon.monthend.domain.model.MonthEndTaskType;
import com.gepardec.mega.hexagon.monthend.domain.port.outbound.MonthEndClarificationRepository;
import com.gepardec.mega.hexagon.monthend.domain.port.outbound.MonthEndTaskRepository;
import com.gepardec.mega.hexagon.shared.domain.model.FullName;
import com.gepardec.mega.hexagon.shared.domain.model.ProjectId;
import com.gepardec.mega.hexagon.shared.domain.model.ProjectRef;
import com.gepardec.mega.hexagon.shared.domain.model.UserId;
import com.gepardec.mega.hexagon.shared.domain.model.UserRef;
import com.gepardec.mega.hexagon.shared.domain.model.ZepUsername;
import org.instancio.Instancio;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.time.YearMonth;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GetEmployeeMonthEndStatusOverviewServiceTest {

    private final YearMonth month = YearMonth.of(2026, 3);
    private final ProjectId projectId = ProjectId.of(Instancio.create(UUID.class));
    private final UserId actorId = UserId.of(Instancio.create(UUID.class));
    private final UserId leadId = UserId.of(Instancio.create(UUID.class));

    @Mock
    private MonthEndTaskRepository monthEndTaskRepository;

    @Mock
    private MonthEndClarificationRepository monthEndClarificationRepository;

    @Mock
    private AssembleMonthEndStatusOverviewService assembleMonthEndStatusOverviewService;

    private GetEmployeeMonthEndStatusOverviewService service;

    @BeforeEach
    void setUp() {
        service = new GetEmployeeMonthEndStatusOverviewService(
                monthEndTaskRepository,
                monthEndClarificationRepository,
                assembleMonthEndStatusOverviewService
        );
    }

    @Test
    void getOverview_shouldFetchTasksAndClarificationsAndReturnAssemblerResult() {
        MonthEndTask task = MonthEndTask.create(
                MonthEndTaskId.generate(),
                month,
                MonthEndTaskType.EMPLOYEE_TIME_CHECK,
                projectId,
                actorId,
                Set.of(actorId)
        );
        MonthEndClarification clarification = MonthEndClarification.create(
                MonthEndClarificationId.generate(),
                month,
                projectId,
                actorId,
                leadId,
                Set.of(leadId),
                "Please add the remaining note.",
                Instant.parse("2026-03-31T08:00:00Z")
        );
        MonthEndStatusOverview assembledOverview = new MonthEndStatusOverview(
                actorId,
                month,
                List.of(new MonthEndStatusOverviewItem(
                        task.id(),
                        task.type(),
                        MonthEndTaskStatus.OPEN,
                        new ProjectRef(projectId, 77, "Project Employee"),
                        new UserRef(actorId, FullName.of("Employee", "Example"), ZepUsername.of("employee.example")),
                        true,
                        null
                )),
                List.of(new MonthEndOverviewClarificationItem(
                        clarification.id(),
                        projectId,
                        new UserRef(actorId, FullName.of("Employee", "Example"), ZepUsername.of("employee.example")),
                        new UserRef(leadId, FullName.of("Lead", "Example"), ZepUsername.of("lead.example")),
                        clarification.status(),
                        clarification.text(),
                        true,
                        null,
                        null,
                        null,
                        clarification.createdAt(),
                        clarification.lastModifiedAt()
                ))
        );

        when(monthEndTaskRepository.findEmployeeVisibleTasks(actorId, month)).thenReturn(List.of(task));
        when(monthEndClarificationRepository.findAllEmployeeClarifications(actorId, month))
                .thenReturn(List.of(clarification));
        when(assembleMonthEndStatusOverviewService.assemble(List.of(task), List.of(clarification), actorId, month))
                .thenReturn(assembledOverview);

        MonthEndStatusOverview overview = service.getOverview(actorId, month);

        assertThat(overview).isSameAs(assembledOverview);
        assertThat(overview.clarifications()).singleElement()
                .satisfies(item -> {
                    assertThat(item.subjectEmployee().id()).isEqualTo(actorId);
                    assertThat(item.createdBy().id()).isEqualTo(leadId);
                    assertThat(item.canResolve()).isTrue();
                });
        verify(monthEndTaskRepository).findEmployeeVisibleTasks(actorId, month);
        verify(monthEndClarificationRepository).findAllEmployeeClarifications(actorId, month);
        verify(assembleMonthEndStatusOverviewService).assemble(List.of(task), List.of(clarification), actorId, month);
    }

    @Test
    void getOverview_shouldDelegateEmptyCollectionsToAssembler() {
        MonthEndStatusOverview assembledOverview = new MonthEndStatusOverview(actorId, month, List.of(), List.of());
        when(monthEndTaskRepository.findEmployeeVisibleTasks(actorId, month)).thenReturn(List.of());
        when(monthEndClarificationRepository.findAllEmployeeClarifications(actorId, month)).thenReturn(List.of());
        when(assembleMonthEndStatusOverviewService.assemble(List.of(), List.of(), actorId, month))
                .thenReturn(assembledOverview);

        MonthEndStatusOverview overview = service.getOverview(actorId, month);

        assertThat(overview.entries()).isEmpty();
        assertThat(overview.clarifications()).isEmpty();
    }
}
