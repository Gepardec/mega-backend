package com.gepardec.mega.hexagon.monthend.application;

import com.gepardec.mega.hexagon.monthend.domain.model.MonthEndClarification;
import com.gepardec.mega.hexagon.monthend.domain.model.MonthEndClarificationId;
import com.gepardec.mega.hexagon.monthend.domain.model.MonthEndClarificationSide;
import com.gepardec.mega.hexagon.monthend.domain.model.MonthEndProjectSnapshot;
import com.gepardec.mega.hexagon.monthend.domain.model.MonthEndTask;
import com.gepardec.mega.hexagon.monthend.domain.model.MonthEndTaskId;
import com.gepardec.mega.hexagon.monthend.domain.model.MonthEndTaskType;
import com.gepardec.mega.hexagon.monthend.domain.model.MonthEndUserSnapshot;
import com.gepardec.mega.hexagon.monthend.domain.model.MonthEndWorklist;
import com.gepardec.mega.hexagon.monthend.domain.port.outbound.MonthEndClarificationRepository;
import com.gepardec.mega.hexagon.monthend.domain.port.outbound.MonthEndProjectSnapshotPort;
import com.gepardec.mega.hexagon.monthend.domain.port.outbound.MonthEndTaskRepository;
import com.gepardec.mega.hexagon.monthend.domain.port.outbound.MonthEndUserSnapshotPort;
import com.gepardec.mega.hexagon.project.domain.model.ProjectId;
import com.gepardec.mega.hexagon.user.domain.model.EmploymentPeriod;
import com.gepardec.mega.hexagon.user.domain.model.EmploymentPeriods;
import com.gepardec.mega.hexagon.user.domain.model.UserId;
import com.gepardec.mega.hexagon.user.domain.model.UserStatus;
import org.instancio.Instancio;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.time.YearMonth;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MonthEndWorklistServicesTest {

    private final YearMonth month = YearMonth.of(2026, 3);
    private final ProjectId projectId = ProjectId.of(UUID.fromString(Instancio.gen().text().uuid().get()));
    private final String projectName = "Test Project";
    private final UserId employeeId = UserId.of(UUID.fromString(Instancio.gen().text().uuid().get()));
    private final String employeeName = "Test Employee";
    private final UserId leadId = UserId.of(UUID.fromString(Instancio.gen().text().uuid().get()));

    @Mock
    private MonthEndTaskRepository monthEndTaskRepository;

    @Mock
    private MonthEndClarificationRepository monthEndClarificationRepository;

    @Mock
    private MonthEndProjectSnapshotPort monthEndProjectSnapshotPort;

    @Mock
    private MonthEndUserSnapshotPort monthEndUserSnapshotPort;

    private GetEmployeeMonthEndWorklistService getEmployeeMonthEndWorklistService;
    private GetProjectLeadMonthEndWorklistService getProjectLeadMonthEndWorklistService;

    @BeforeEach
    void setUp() {
        MonthEndWorklistMapper mapper = Mappers.getMapper(MonthEndWorklistMapper.class);
        getEmployeeMonthEndWorklistService = new GetEmployeeMonthEndWorklistService(
                monthEndTaskRepository,
                monthEndClarificationRepository,
                monthEndProjectSnapshotPort,
                monthEndUserSnapshotPort,
                mapper
        );
        getProjectLeadMonthEndWorklistService = new GetProjectLeadMonthEndWorklistService(
                monthEndTaskRepository,
                monthEndClarificationRepository,
                monthEndProjectSnapshotPort,
                monthEndUserSnapshotPort,
                mapper
        );
    }

    @Test
    void getWorklist_shouldReturnEmployeeTasksWithProjectReference() {
        MonthEndTask task = MonthEndTask.create(
                MonthEndTaskId.generate(),
                month,
                MonthEndTaskType.EMPLOYEE_TIME_CHECK,
                projectId,
                employeeId,
                Set.of(employeeId)
        );
        when(monthEndTaskRepository.findOpenEmployeeTasks(employeeId, month)).thenReturn(List.of(task));
        when(monthEndProjectSnapshotPort.findByIds(any())).thenReturn(List.of(projectSnapshot()));
        when(monthEndUserSnapshotPort.findByIds(any())).thenReturn(List.of(userSnapshot(employeeId, employeeName)));

        MonthEndWorklist worklist = getEmployeeMonthEndWorklistService.getWorklist(employeeId, month);

        assertThat(worklist.actorId()).isEqualTo(employeeId);
        assertThat(worklist.month()).isEqualTo(month);
        assertThat(worklist.clarifications()).isEmpty();
        assertThat(worklist.tasks()).singleElement().satisfies(item -> {
            assertThat(item.taskId()).isEqualTo(task.id());
            assertThat(item.project().id()).isEqualTo(projectId);
            assertThat(item.project().name()).isEqualTo(projectName);
            assertThat(item.subjectEmployee().id()).isEqualTo(employeeId);
            assertThat(item.subjectEmployee().fullName()).isEqualTo(employeeName);
        });
    }

    @Test
    void getWorklist_shouldReturnLeadTasksWithSubjectEmployeeReference() {
        MonthEndTask task = MonthEndTask.create(
                MonthEndTaskId.generate(),
                month,
                MonthEndTaskType.PROJECT_LEAD_REVIEW,
                projectId,
                employeeId,
                Set.of(leadId)
        );
        when(monthEndTaskRepository.findOpenProjectLeadTasks(leadId, month)).thenReturn(List.of(task));
        when(monthEndProjectSnapshotPort.findByIds(any())).thenReturn(List.of(projectSnapshot()));
        when(monthEndUserSnapshotPort.findByIds(any())).thenReturn(List.of(userSnapshot(employeeId, employeeName)));

        MonthEndWorklist worklist = getProjectLeadMonthEndWorklistService.getWorklist(leadId, month);

        assertThat(worklist.actorId()).isEqualTo(leadId);
        assertThat(worklist.month()).isEqualTo(month);
        assertThat(worklist.clarifications()).isEmpty();
        assertThat(worklist.tasks()).singleElement().satisfies(item -> {
            assertThat(item.taskId()).isEqualTo(task.id());
            assertThat(item.project().id()).isEqualTo(projectId);
            assertThat(item.project().name()).isEqualTo(projectName);
            assertThat(item.subjectEmployee().id()).isEqualTo(employeeId);
            assertThat(item.subjectEmployee().fullName()).isEqualTo(employeeName);
        });
    }

    @Test
    void getWorklist_shouldReturnVisibleClarificationsAlongsideTasks() {
        MonthEndClarification clarification = MonthEndClarification.create(
                MonthEndClarificationId.generate(),
                month,
                projectId,
                employeeId,
                employeeId,
                MonthEndClarificationSide.EMPLOYEE,
                Set.of(leadId),
                "Please review this clarification.",
                Instant.parse("2026-03-31T08:00:00Z")
        );
        when(monthEndTaskRepository.findOpenEmployeeTasks(employeeId, month)).thenReturn(List.of());
        when(monthEndClarificationRepository.findOpenEmployeeClarifications(employeeId, month))
                .thenReturn(List.of(clarification));

        MonthEndWorklist worklist = getEmployeeMonthEndWorklistService.getWorklist(employeeId, month);

        assertThat(worklist.tasks()).isEmpty();
        assertThat(worklist.clarifications()).singleElement().satisfies(item -> {
            assertThat(item.clarificationId()).isEqualTo(clarification.id());
            assertThat(item.projectId()).isEqualTo(projectId);
            assertThat(item.subjectEmployeeId()).isEqualTo(employeeId);
            assertThat(item.text()).isEqualTo("Please review this clarification.");
        });
    }

    private MonthEndProjectSnapshot projectSnapshot() {
        return new MonthEndProjectSnapshot(
                projectId,
                77,
                projectName,
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
                "test.user",
                UserStatus.ACTIVE,
                new EmploymentPeriods(new EmploymentPeriod(month.atDay(1).minusYears(1), null))
        );
    }
}
