package com.gepardec.mega.hexagon.monthend.application;

import com.gepardec.mega.hexagon.monthend.domain.model.MonthEndClarification;
import com.gepardec.mega.hexagon.monthend.domain.model.MonthEndClarificationId;
import com.gepardec.mega.hexagon.monthend.domain.model.MonthEndStatusOverview;
import com.gepardec.mega.hexagon.monthend.domain.model.MonthEndTask;
import com.gepardec.mega.hexagon.monthend.domain.model.MonthEndTaskId;
import com.gepardec.mega.hexagon.monthend.domain.model.MonthEndTaskStatus;
import com.gepardec.mega.hexagon.monthend.domain.model.MonthEndTaskType;
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
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AssembleMonthEndStatusOverviewServiceTest {

    private final YearMonth month = YearMonth.of(2026, 3);
    private final ProjectId projectId = ProjectId.of(Instancio.create(UUID.class));
    private final UserId employeeId = UserId.of(Instancio.create(UUID.class));
    private final UserId leadAId = UserId.of(Instancio.create(UUID.class));
    private final UserId leadBId = UserId.of(Instancio.create(UUID.class));

    @Mock
    private ResolveMonthEndTaskSnapshotLookupService resolveMonthEndTaskSnapshotLookupService;

    private AssembleMonthEndStatusOverviewService service;

    @BeforeEach
    void setUp() {
        service = new AssembleMonthEndStatusOverviewService(resolveMonthEndTaskSnapshotLookupService);
    }

    @Test
    void assemble_shouldMapTaskCompletionAndPassThroughClarificationsUnchanged() {
        MonthEndTask employeeTask = MonthEndTask.create(
                MonthEndTaskId.generate(),
                month,
                MonthEndTaskType.EMPLOYEE_TIME_CHECK,
                projectId,
                employeeId,
                Set.of(employeeId)
        );
        MonthEndTask projectLeadReviewTask = new MonthEndTask(
                MonthEndTaskId.generate(),
                month,
                MonthEndTaskType.PROJECT_LEAD_REVIEW,
                projectId,
                employeeId,
                Set.of(leadAId, leadBId),
                MonthEndTaskStatus.DONE,
                leadAId
        );
        MonthEndClarification leadCreatedClarification = MonthEndClarification.create(
                MonthEndClarificationId.generate(),
                month,
                projectId,
                employeeId,
                leadAId,
                Set.of(leadAId, leadBId),
                "Please add the remaining evidence.",
                Instant.parse("2026-03-31T08:00:00Z")
        );
        MonthEndClarification employeeCreatedClarification = MonthEndClarification.create(
                MonthEndClarificationId.generate(),
                month,
                projectId,
                employeeId,
                employeeId,
                Set.of(leadAId, leadBId),
                "Please verify the entered hours.",
                Instant.parse("2026-03-31T08:05:00Z")
        );
        MonthEndClarification doneClarification = MonthEndClarification.create(
                MonthEndClarificationId.generate(),
                month,
                projectId,
                employeeId,
                employeeId,
                Set.of(leadAId, leadBId),
                "Already resolved clarification.",
                Instant.parse("2026-03-31T08:06:00Z")
        ).resolve(leadAId, "Handled in the source system.", Instant.parse("2026-03-31T08:10:00Z"));

        UserRef employeeRef = new UserRef(
                employeeId,
                FullName.of("Employee", "Example"),
                ZepUsername.of("employee.example")
        );
        when(resolveMonthEndTaskSnapshotLookupService.resolve(
                List.of(employeeTask, projectLeadReviewTask),
                month
        ))
                .thenReturn(new MonthEndTaskSnapshotLookup(
                        Map.of(projectId, new ProjectRef(projectId, 77, "Project Overview")),
                        Map.of(employeeId, employeeRef)
                ));

        MonthEndStatusOverview overview = service.assemble(
                List.of(employeeTask, projectLeadReviewTask),
                List.of(leadCreatedClarification, employeeCreatedClarification, doneClarification),
                employeeId,
                month
        );

        assertThat(overview.actorId()).isEqualTo(employeeId);
        assertThat(overview.month()).isEqualTo(month);
        assertThat(overview.entries()).hasSize(2);
        assertThat(overview.entries())
                .filteredOn(item -> item.taskId().equals(employeeTask.id()))
                .singleElement()
                .satisfies(item -> {
                    assertThat(item.canComplete()).isTrue();
                    assertThat(item.completedBy()).isNull();
                    assertThat(item.project().name()).isEqualTo("Project Overview");
                    assertThat(item.subjectEmployee()).isNotNull();
                    assertThat(item.subjectEmployee().id()).isEqualTo(employeeId);
                });
        assertThat(overview.entries())
                .filteredOn(item -> item.taskId().equals(projectLeadReviewTask.id()))
                .singleElement()
                .satisfies(item -> {
                    assertThat(item.status()).isEqualTo(MonthEndTaskStatus.DONE);
                    assertThat(item.canComplete()).isFalse();
                    assertThat(item.completedBy()).isEqualTo(leadAId);
                });
        assertThat(overview.clarifications()).containsExactly(
                leadCreatedClarification, employeeCreatedClarification, doneClarification
        );
    }

    @Test
    void assemble_shouldReturnEmptyOverview_whenTasksAndClarificationsAreEmpty() {
        when(resolveMonthEndTaskSnapshotLookupService.resolve(List.of(), month))
                .thenReturn(MonthEndTaskSnapshotLookup.empty());

        MonthEndStatusOverview overview = service.assemble(List.of(), List.of(), employeeId, month);

        assertThat(overview.entries()).isEmpty();
        assertThat(overview.clarifications()).isEmpty();
        verify(resolveMonthEndTaskSnapshotLookupService).resolve(List.of(), month);
    }
}
