package com.gepardec.mega.hexagon.monthend.application;

import com.gepardec.mega.hexagon.monthend.domain.model.MonthEndCompletionPolicy;
import com.gepardec.mega.hexagon.monthend.domain.model.MonthEndProjectSnapshot;
import com.gepardec.mega.hexagon.monthend.domain.model.MonthEndTask;
import com.gepardec.mega.hexagon.monthend.domain.model.MonthEndTaskGenerationResult;
import com.gepardec.mega.hexagon.monthend.domain.model.MonthEndTaskId;
import com.gepardec.mega.hexagon.monthend.domain.model.MonthEndTaskType;
import com.gepardec.mega.hexagon.monthend.domain.port.outbound.MonthEndProjectAssignmentPort;
import com.gepardec.mega.hexagon.monthend.domain.port.outbound.MonthEndProjectSnapshotPort;
import com.gepardec.mega.hexagon.monthend.domain.port.outbound.MonthEndTaskRepository;
import com.gepardec.mega.hexagon.monthend.domain.port.outbound.MonthEndUserSnapshotPort;
import com.gepardec.mega.hexagon.monthend.domain.services.MonthEndTaskPlanningService;
import com.gepardec.mega.hexagon.shared.domain.model.FullName;
import com.gepardec.mega.hexagon.shared.domain.model.ProjectId;
import com.gepardec.mega.hexagon.shared.domain.model.UserId;
import com.gepardec.mega.hexagon.shared.domain.model.UserRef;
import com.gepardec.mega.hexagon.shared.domain.model.ZepUsername;
import org.instancio.Instancio;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

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
                monthEndProjectAssignmentPort,
                new MonthEndTaskPlanningService()
        );
    }

    @Test
    void generate_shouldCreateUnifiedTasks_whenBillableProjectHasActiveEmployeeAndLead() {
        UserRef employee = activeUser("employee", "00000000-0000-0000-0000-000000000020");
        UserRef lead = activeUser("lead", "00000000-0000-0000-0000-000000000021");
        MonthEndProjectSnapshot project = activeProject(77, true, Set.of(lead.id()));

        when(monthEndTaskRepository.findByMonth(month)).thenReturn(List.of());
        when(monthEndProjectSnapshotPort.findActiveIn(month)).thenReturn(List.of(project));
        when(monthEndUserSnapshotPort.findActiveIn(month)).thenReturn(List.of(employee, lead));
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
            assertThat(employeeTimeCheck.subjectEmployeeId()).isEqualTo(employee.id());
            assertThat(employeeTimeCheck.eligibleActorIds()).containsExactly(employee.id());
            assertThat(employeeTimeCheck.completionPolicy()).isEqualTo(MonthEndCompletionPolicy.INDIVIDUAL_ACTOR);

            MonthEndTask leistungsnachweis = tasks.stream()
                    .filter(task -> task.type() == MonthEndTaskType.LEISTUNGSNACHWEIS)
                    .findFirst()
                    .orElseThrow();
            assertThat(leistungsnachweis.subjectEmployeeId()).isEqualTo(employee.id());

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
        UserRef employee = activeUser("employee", "00000000-0000-0000-0000-000000000030");
        UserRef lead = activeUser("lead", "00000000-0000-0000-0000-000000000031");
        MonthEndProjectSnapshot project = activeProject(88, true, Set.of(lead.id()));

        when(monthEndProjectSnapshotPort.findActiveIn(month)).thenReturn(List.of(project));
        when(monthEndUserSnapshotPort.findActiveIn(month)).thenReturn(List.of(employee, lead));
        when(monthEndProjectAssignmentPort.findAssignedUsernames(88, month)).thenReturn(Set.of("employee"));
        when(monthEndTaskRepository.findByMonth(month)).thenReturn(List.of(
                MonthEndTask.create(MonthEndTaskId.generate(), month, MonthEndTaskType.EMPLOYEE_TIME_CHECK,
                        project.id(), employee.id(), Set.of(employee.id())),
                MonthEndTask.create(MonthEndTaskId.generate(), month, MonthEndTaskType.LEISTUNGSNACHWEIS,
                        project.id(), employee.id(), Set.of(employee.id())),
                MonthEndTask.create(MonthEndTaskId.generate(), month, MonthEndTaskType.PROJECT_LEAD_REVIEW,
                        project.id(), employee.id(), Set.of(lead.id())),
                MonthEndTask.create(MonthEndTaskId.generate(), month, MonthEndTaskType.ABRECHNUNG,
                        project.id(), null, Set.of(lead.id()))
        ));

        MonthEndTaskGenerationResult result = service.generate(month);

        assertThat(result.created()).isZero();
        assertThat(result.skipped()).isEqualTo(4);
        verify(monthEndTaskRepository).saveAll(argThat(List::isEmpty));
    }

    @Test
    void generate_shouldOnlyCreateEmployeeTimeCheck_whenProjectIsNonBillableAndHasNoLead() {
        UserRef employee = activeUser("employee", "00000000-0000-0000-0000-000000000040");
        MonthEndProjectSnapshot project = activeProject(99, false, Set.of());

        when(monthEndTaskRepository.findByMonth(month)).thenReturn(List.of());
        when(monthEndProjectSnapshotPort.findActiveIn(month)).thenReturn(List.of(project));
        when(monthEndUserSnapshotPort.findActiveIn(month)).thenReturn(List.of(employee));
        when(monthEndProjectAssignmentPort.findAssignedUsernames(99, month)).thenReturn(Set.of("employee"));

        MonthEndTaskGenerationResult result = service.generate(month);

        assertThat(result.created()).isEqualTo(1);
        assertThat(result.skipped()).isZero();
        verify(monthEndTaskRepository).saveAll(argThat(tasks -> {
            assertThat(tasks).singleElement().satisfies(task -> {
                assertThat(task.type()).isEqualTo(MonthEndTaskType.EMPLOYEE_TIME_CHECK);
                assertThat(task.subjectEmployeeId()).isEqualTo(employee.id());
                assertThat(task.eligibleActorIds()).containsExactly(employee.id());
            });
            return true;
        }));
    }

    @Test
    void generate_shouldNotSkipEmployeeOwnedTasksForDifferentEmployeesOnSameProject() {
        UserRef employeeA = activeUser("employee-a", Instancio.gen().text().uuid().get());
        UserRef employeeB = activeUser("employee-b", Instancio.gen().text().uuid().get());
        UserRef lead = activeUser("lead", Instancio.gen().text().uuid().get());
        MonthEndProjectSnapshot project = activeProject(100, true, Set.of(lead.id()));

        when(monthEndTaskRepository.findByMonth(month)).thenReturn(List.of());
        when(monthEndProjectSnapshotPort.findActiveIn(month)).thenReturn(List.of(project));
        when(monthEndUserSnapshotPort.findActiveIn(month)).thenReturn(List.of(employeeA, employeeB, lead));
        when(monthEndProjectAssignmentPort.findAssignedUsernames(100, month))
                .thenReturn(Set.of("employee-a", "employee-b"));

        MonthEndTaskGenerationResult result = service.generate(month);

        assertThat(result.created()).isEqualTo(7);
        assertThat(result.skipped()).isZero();
        verify(monthEndTaskRepository).saveAll(argThat(tasks -> {
            assertThat(tasks).hasSize(7);
            assertThat(tasks.stream()
                    .filter(task -> task.type() == MonthEndTaskType.EMPLOYEE_TIME_CHECK)
                    .map(MonthEndTask::subjectEmployeeId)
                    .toList())
                    .containsExactlyInAnyOrder(employeeA.id(), employeeB.id());
            assertThat(tasks.stream()
                    .filter(task -> task.type() == MonthEndTaskType.LEISTUNGSNACHWEIS)
                    .map(MonthEndTask::subjectEmployeeId)
                    .toList())
                    .containsExactlyInAnyOrder(employeeA.id(), employeeB.id());
            assertThat(tasks.stream()
                    .filter(task -> task.type() == MonthEndTaskType.PROJECT_LEAD_REVIEW)
                    .map(MonthEndTask::subjectEmployeeId)
                    .toList())
                    .containsExactlyInAnyOrder(employeeA.id(), employeeB.id());
            assertThat(tasks.stream()
                    .filter(task -> task.type() == MonthEndTaskType.ABRECHNUNG))
                    .singleElement()
                    .satisfies(task -> assertThat(task.eligibleActorIds()).containsExactly(lead.id()));
            return true;
        }));
    }

    private UserRef activeUser(String username, String userId) {
        return new UserRef(
                UserId.of(UUID.fromString(userId)),
                FullName.of(username, "User"),
                ZepUsername.of(username)
        );
    }

    private MonthEndProjectSnapshot activeProject(int zepId, boolean billable, Set<UserId> leadIds) {
        return new MonthEndProjectSnapshot(
                ProjectId.generate(),
                zepId,
                "Project-" + zepId,
                billable,
                leadIds
        );
    }
}
