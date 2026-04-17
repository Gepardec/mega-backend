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
class GetProjectLeadMonthEndStatusOverviewServiceTest {

    private final YearMonth month = YearMonth.of(2026, 3);
    private final ProjectId projectId = ProjectId.of(Instancio.create(UUID.class));
    private final UserId leadId = UserId.of(Instancio.create(UUID.class));
    private final UserId subjectEmployeeId = UserId.of(Instancio.create(UUID.class));

    @Mock
    private MonthEndTaskRepository monthEndTaskRepository;

    @Mock
    private MonthEndClarificationRepository monthEndClarificationRepository;

    @Mock
    private AssembleMonthEndStatusOverviewService assembleMonthEndStatusOverviewService;

    private GetProjectLeadMonthEndStatusOverviewService service;

    @BeforeEach
    void setUp() {
        service = new GetProjectLeadMonthEndStatusOverviewService(
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
                MonthEndTaskType.PROJECT_LEAD_REVIEW,
                projectId,
                subjectEmployeeId,
                Set.of(leadId)
        );
        MonthEndClarification clarification = MonthEndClarification.create(
                MonthEndClarificationId.generate(),
                month,
                projectId,
                subjectEmployeeId,
                subjectEmployeeId,
                Set.of(leadId),
                "Please verify the employee clarification.",
                Instant.parse("2026-03-31T08:00:00Z")
        );
        MonthEndStatusOverview assembledOverview = new MonthEndStatusOverview(
                leadId,
                month,
                List.of(new MonthEndStatusOverviewItem(
                        task.id(),
                        task.type(),
                        MonthEndTaskStatus.OPEN,
                        new ProjectRef(projectId, 88, "Project Lead"),
                        null,
                        true,
                        null
                )),
                List.of(new MonthEndOverviewClarificationItem(
                        clarification.id(),
                        projectId,
                        new UserRef(
                                subjectEmployeeId,
                                FullName.of("Subject", "Employee"),
                                ZepUsername.of("subject.employee")
                        ),
                        new UserRef(
                                subjectEmployeeId,
                                FullName.of("Subject", "Employee"),
                                ZepUsername.of("subject.employee")
                        ),
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

        when(monthEndTaskRepository.findLeadProjectTasks(leadId, month)).thenReturn(List.of(task));
        when(monthEndClarificationRepository.findAllProjectLeadClarifications(leadId, month))
                .thenReturn(List.of(clarification));
        when(assembleMonthEndStatusOverviewService.assemble(List.of(task), List.of(clarification), leadId, month))
                .thenReturn(assembledOverview);

        MonthEndStatusOverview overview = service.getOverview(leadId, month);

        assertThat(overview).isSameAs(assembledOverview);
        assertThat(overview.clarifications()).singleElement()
                .satisfies(item -> {
                    assertThat(item.subjectEmployee().id()).isEqualTo(subjectEmployeeId);
                    assertThat(item.createdBy().id()).isEqualTo(subjectEmployeeId);
                    assertThat(item.canResolve()).isTrue();
                });
        verify(monthEndTaskRepository).findLeadProjectTasks(leadId, month);
        verify(monthEndClarificationRepository).findAllProjectLeadClarifications(leadId, month);
        verify(assembleMonthEndStatusOverviewService).assemble(List.of(task), List.of(clarification), leadId, month);
    }

    @Test
    void getOverview_shouldDelegateEmptyCollectionsToAssembler() {
        MonthEndStatusOverview assembledOverview = new MonthEndStatusOverview(leadId, month, List.of(), List.of());
        when(monthEndTaskRepository.findLeadProjectTasks(leadId, month)).thenReturn(List.of());
        when(monthEndClarificationRepository.findAllProjectLeadClarifications(leadId, month)).thenReturn(List.of());
        when(assembleMonthEndStatusOverviewService.assemble(List.of(), List.of(), leadId, month))
                .thenReturn(assembledOverview);

        MonthEndStatusOverview overview = service.getOverview(leadId, month);

        assertThat(overview.entries()).isEmpty();
        assertThat(overview.clarifications()).isEmpty();
    }
}
