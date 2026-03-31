package com.gepardec.mega.hexagon.monthend.application;

import com.gepardec.mega.hexagon.monthend.domain.model.MonthEndCompletionPolicy;
import com.gepardec.mega.hexagon.monthend.domain.model.MonthEndTask;
import com.gepardec.mega.hexagon.monthend.domain.model.MonthEndTaskId;
import com.gepardec.mega.hexagon.monthend.domain.model.MonthEndTaskType;
import com.gepardec.mega.hexagon.monthend.domain.model.MonthEndWorklist;
import com.gepardec.mega.hexagon.monthend.domain.port.outbound.MonthEndTaskRepository;
import com.gepardec.mega.hexagon.project.domain.model.ProjectId;
import com.gepardec.mega.hexagon.user.domain.model.UserId;
import org.instancio.Instancio;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.time.YearMonth;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class MonthEndWorklistServicesTest {

    private final YearMonth month = YearMonth.of(2026, 3);
    private final ProjectId projectId = ProjectId.of(UUID.fromString(Instancio.gen().text().uuid().get()));
    private final UserId employeeId = UserId.of(UUID.fromString(Instancio.gen().text().uuid().get()));
    private final UserId leadId = UserId.of(UUID.fromString(Instancio.gen().text().uuid().get()));

    private MonthEndTaskRepository monthEndTaskRepository;
    private GetEmployeeMonthEndWorklistService getEmployeeMonthEndWorklistService;
    private GetProjectLeadMonthEndWorklistService getProjectLeadMonthEndWorklistService;

    @BeforeEach
    void setUp() {
        monthEndTaskRepository = mock(MonthEndTaskRepository.class);
        MonthEndWorklistMapper mapper = Mappers.getMapper(MonthEndWorklistMapper.class);
        getEmployeeMonthEndWorklistService = new GetEmployeeMonthEndWorklistService(monthEndTaskRepository, mapper);
        getProjectLeadMonthEndWorklistService = new GetProjectLeadMonthEndWorklistService(monthEndTaskRepository, mapper);
    }

    @Test
    void getWorklist_shouldReturnEmployeeTasksWithProjectReference() {
        MonthEndTask task = MonthEndTask.create(
                MonthEndTaskId.generate(),
                month,
                MonthEndTaskType.EMPLOYEE_TIME_CHECK,
                projectId,
                null,
                Set.of(employeeId),
                MonthEndCompletionPolicy.INDIVIDUAL_ACTOR
        );
        when(monthEndTaskRepository.findOpenEmployeeTasks(employeeId, month)).thenReturn(List.of(task));

        MonthEndWorklist worklist = getEmployeeMonthEndWorklistService.getWorklist(employeeId, month);

        assertThat(worklist.actorId()).isEqualTo(employeeId);
        assertThat(worklist.month()).isEqualTo(month);
        assertThat(worklist.tasks()).singleElement().satisfies(item -> {
            assertThat(item.taskId()).isEqualTo(task.id());
            assertThat(item.projectId()).isEqualTo(projectId);
            assertThat(item.subjectEmployeeId()).isNull();
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
                Set.of(leadId),
                MonthEndCompletionPolicy.ANY_ELIGIBLE_ACTOR
        );
        when(monthEndTaskRepository.findOpenProjectLeadTasks(leadId, month)).thenReturn(List.of(task));

        MonthEndWorklist worklist = getProjectLeadMonthEndWorklistService.getWorklist(leadId, month);

        assertThat(worklist.actorId()).isEqualTo(leadId);
        assertThat(worklist.month()).isEqualTo(month);
        assertThat(worklist.tasks()).singleElement().satisfies(item -> {
            assertThat(item.taskId()).isEqualTo(task.id());
            assertThat(item.projectId()).isEqualTo(projectId);
            assertThat(item.subjectEmployeeId()).isEqualTo(employeeId);
        });
    }
}
