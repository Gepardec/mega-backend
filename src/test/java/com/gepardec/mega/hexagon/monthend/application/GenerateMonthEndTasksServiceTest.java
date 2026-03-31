package com.gepardec.mega.hexagon.monthend.application;

import com.gepardec.mega.hexagon.monthend.domain.model.MonthEndCompletionPolicy;
import com.gepardec.mega.hexagon.monthend.domain.model.MonthEndProjectSnapshot;
import com.gepardec.mega.hexagon.monthend.domain.model.MonthEndTask;
import com.gepardec.mega.hexagon.monthend.domain.model.MonthEndTaskGenerationResult;
import com.gepardec.mega.hexagon.monthend.domain.model.MonthEndTaskId;
import com.gepardec.mega.hexagon.monthend.domain.model.MonthEndTaskType;
import com.gepardec.mega.hexagon.monthend.domain.model.MonthEndUserSnapshot;
import com.gepardec.mega.hexagon.monthend.domain.port.outbound.MonthEndProjectAssignmentPort;
import com.gepardec.mega.hexagon.monthend.domain.port.outbound.MonthEndProjectSnapshotPort;
import com.gepardec.mega.hexagon.monthend.domain.port.outbound.MonthEndTaskRepository;
import com.gepardec.mega.hexagon.monthend.domain.port.outbound.MonthEndUserSnapshotPort;
import com.gepardec.mega.hexagon.project.domain.model.ProjectId;
import com.gepardec.mega.hexagon.user.domain.model.EmploymentPeriod;
import com.gepardec.mega.hexagon.user.domain.model.EmploymentPeriods;
import com.gepardec.mega.hexagon.user.domain.model.UserId;
import com.gepardec.mega.hexagon.user.domain.model.UserStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class GenerateMonthEndTasksServiceTest {

    private final YearMonth month = YearMonth.of(2026, 3);

    private MonthEndTaskRepository monthEndTaskRepository;
    private MonthEndProjectSnapshotPort monthEndProjectSnapshotPort;
    private MonthEndUserSnapshotPort monthEndUserSnapshotPort;
    private MonthEndProjectAssignmentPort monthEndProjectAssignmentPort;
    private GenerateMonthEndTasksService service;

    @BeforeEach
    void setUp() {
        monthEndTaskRepository = mock(MonthEndTaskRepository.class);
        monthEndProjectSnapshotPort = mock(MonthEndProjectSnapshotPort.class);
        monthEndUserSnapshotPort = mock(MonthEndUserSnapshotPort.class);
        monthEndProjectAssignmentPort = mock(MonthEndProjectAssignmentPort.class);
        service = new GenerateMonthEndTasksService(
                monthEndTaskRepository,
                monthEndProjectSnapshotPort,
                monthEndUserSnapshotPort,
                monthEndProjectAssignmentPort
        );
    }

    @Test
    void generate_shouldCreateUnifiedTasks_whenBillableProjectHasActiveEmployeeAndLead() {
        MonthEndUserSnapshot employee = activeUser("employee", "00000000-0000-0000-0000-000000000020");
        MonthEndUserSnapshot lead = activeUser("lead", "00000000-0000-0000-0000-000000000021");
        MonthEndProjectSnapshot project = activeProject(77, true, Set.of(lead.id()));

        when(monthEndTaskRepository.findByMonth(month)).thenReturn(List.of());
        when(monthEndProjectSnapshotPort.findAll()).thenReturn(List.of(project));
        when(monthEndUserSnapshotPort.findAll()).thenReturn(List.of(employee, lead));
        when(monthEndProjectAssignmentPort.findAssignedUsernames(77, month)).thenReturn(Set.of("employee"));

        MonthEndTaskGenerationResult result = service.generate(month);

        assertThat(result.month()).isEqualTo(month);
        assertThat(result.created()).isEqualTo(4);
        assertThat(result.skipped()).isZero();

        verify(monthEndTaskRepository).saveAll(argThat(tasks -> {
            assertThat(tasks).hasSize(4);
            assertThat(tasks).extracting(MonthEndTask::type)
                    .containsExactlyInAnyOrder(
                            MonthEndTaskType.EMPLOYEE_TIME_CHECK,
                            MonthEndTaskType.LEISTUNGSNACHWEIS,
                            MonthEndTaskType.PROJECT_LEAD_REVIEW,
                            MonthEndTaskType.ABRECHNUNG
                    );

            MonthEndTask employeeTimeCheck = tasks.stream()
                    .filter(task -> task.type() == MonthEndTaskType.EMPLOYEE_TIME_CHECK)
                    .findFirst()
                    .orElseThrow();
            assertThat(employeeTimeCheck.eligibleActorIds()).containsExactly(employee.id());
            assertThat(employeeTimeCheck.completionPolicy()).isEqualTo(MonthEndCompletionPolicy.INDIVIDUAL_ACTOR);

            MonthEndTask projectLeadReview = tasks.stream()
                    .filter(task -> task.type() == MonthEndTaskType.PROJECT_LEAD_REVIEW)
                    .findFirst()
                    .orElseThrow();
            assertThat(projectLeadReview.subjectEmployeeId()).isEqualTo(employee.id());
            assertThat(projectLeadReview.eligibleActorIds()).containsExactly(lead.id());

            MonthEndTask abrechnung = tasks.stream()
                    .filter(task -> task.type() == MonthEndTaskType.ABRECHNUNG)
                    .findFirst()
                    .orElseThrow();
            assertThat(abrechnung.subjectEmployeeId()).isNull();
            assertThat(abrechnung.eligibleActorIds()).containsExactly(lead.id());
            return true;
        }));
    }

    @Test
    void generate_shouldSkipExistingBusinessKeys_whenMonthEndIsRegenerated() {
        MonthEndUserSnapshot employee = activeUser("employee", "00000000-0000-0000-0000-000000000030");
        MonthEndUserSnapshot lead = activeUser("lead", "00000000-0000-0000-0000-000000000031");
        MonthEndProjectSnapshot project = activeProject(88, true, Set.of(lead.id()));

        when(monthEndProjectSnapshotPort.findAll()).thenReturn(List.of(project));
        when(monthEndUserSnapshotPort.findAll()).thenReturn(List.of(employee, lead));
        when(monthEndProjectAssignmentPort.findAssignedUsernames(88, month)).thenReturn(Set.of("employee"));
        when(monthEndTaskRepository.findByMonth(month)).thenReturn(List.of(
                MonthEndTask.create(MonthEndTaskId.generate(), month, MonthEndTaskType.EMPLOYEE_TIME_CHECK,
                        project.id(), null, Set.of(employee.id()), MonthEndCompletionPolicy.INDIVIDUAL_ACTOR),
                MonthEndTask.create(MonthEndTaskId.generate(), month, MonthEndTaskType.LEISTUNGSNACHWEIS,
                        project.id(), null, Set.of(employee.id()), MonthEndCompletionPolicy.INDIVIDUAL_ACTOR),
                MonthEndTask.create(MonthEndTaskId.generate(), month, MonthEndTaskType.PROJECT_LEAD_REVIEW,
                        project.id(), employee.id(), Set.of(lead.id()), MonthEndCompletionPolicy.ANY_ELIGIBLE_ACTOR),
                MonthEndTask.create(MonthEndTaskId.generate(), month, MonthEndTaskType.ABRECHNUNG,
                        project.id(), null, Set.of(lead.id()), MonthEndCompletionPolicy.ANY_ELIGIBLE_ACTOR)
        ));

        MonthEndTaskGenerationResult result = service.generate(month);

        assertThat(result.created()).isZero();
        assertThat(result.skipped()).isEqualTo(4);
        verify(monthEndTaskRepository).saveAll(argThat(List::isEmpty));
    }

    @Test
    void generate_shouldOnlyCreateEmployeeTimeCheck_whenProjectIsNonBillableAndHasNoLead() {
        MonthEndUserSnapshot employee = activeUser("employee", "00000000-0000-0000-0000-000000000040");
        MonthEndProjectSnapshot project = activeProject(99, false, Set.of());

        when(monthEndTaskRepository.findByMonth(month)).thenReturn(List.of());
        when(monthEndProjectSnapshotPort.findAll()).thenReturn(List.of(project));
        when(monthEndUserSnapshotPort.findAll()).thenReturn(List.of(employee));
        when(monthEndProjectAssignmentPort.findAssignedUsernames(99, month)).thenReturn(Set.of("employee"));

        MonthEndTaskGenerationResult result = service.generate(month);

        assertThat(result.created()).isEqualTo(1);
        assertThat(result.skipped()).isZero();
        verify(monthEndTaskRepository).saveAll(argThat(tasks -> {
            assertThat(tasks).singleElement().satisfies(task -> {
                assertThat(task.type()).isEqualTo(MonthEndTaskType.EMPLOYEE_TIME_CHECK);
                assertThat(task.eligibleActorIds()).containsExactly(employee.id());
            });
            return true;
        }));
    }

    private MonthEndUserSnapshot activeUser(String username, String userId) {
        return new MonthEndUserSnapshot(
                UserId.of(UUID.fromString(userId)),
                username,
                UserStatus.ACTIVE,
                new EmploymentPeriods(new EmploymentPeriod(LocalDate.of(2020, 1, 1), null))
        );
    }

    private MonthEndProjectSnapshot activeProject(int zepId, boolean billable, Set<UserId> leadIds) {
        return new MonthEndProjectSnapshot(
                ProjectId.generate(),
                zepId,
                LocalDate.of(2025, 1, 1),
                null,
                billable,
                leadIds
        );
    }
}
