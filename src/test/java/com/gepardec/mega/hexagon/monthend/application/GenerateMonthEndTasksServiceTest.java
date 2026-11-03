package com.gepardec.mega.hexagon.monthend.application;

import com.gepardec.mega.hexagon.monthend.application.port.outbound.MonthEndProjectAssignmentPort;
import com.gepardec.mega.hexagon.monthend.application.port.outbound.MonthEndProjectSnapshotPort;
import com.gepardec.mega.hexagon.monthend.application.port.outbound.MonthEndUserSnapshotPort;
import com.gepardec.mega.hexagon.monthend.domain.model.MonthEndCompletionPolicy;
import com.gepardec.mega.hexagon.monthend.domain.model.MonthEndProjectSnapshot;
import com.gepardec.mega.hexagon.monthend.domain.model.MonthEndTask;
import com.gepardec.mega.hexagon.monthend.domain.model.MonthEndTaskGenerationResult;
import com.gepardec.mega.hexagon.monthend.domain.model.MonthEndTaskId;
import com.gepardec.mega.hexagon.monthend.domain.model.MonthEndTaskStatus;
import com.gepardec.mega.hexagon.monthend.domain.model.MonthEndTaskType;
import com.gepardec.mega.hexagon.monthend.domain.port.outbound.MonthEndTaskRepository;
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
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class GenerateMonthEndTasksServiceTest {

    private static final UserId COORDINATOR_ID = UserId.of(UUID.fromString("00000000-0000-0000-0000-0000000000c0"));

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
        when(monthEndUserSnapshotPort.findActiveCoordinatorIdsIn(month)).thenReturn(Set.of(COORDINATOR_ID));
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
            assertThat(leistungsnachweis.eligibleActorIds()).containsExactly(COORDINATOR_ID);
            assertThat(leistungsnachweis.completionPolicy()).isEqualTo(MonthEndCompletionPolicy.ANY_ELIGIBLE_ACTOR);

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
                        project.id(), employee.id(), Set.of(COORDINATOR_ID)),
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

        assertThat(result.created()).isOne();
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
                    .filter(task -> task.type() == MonthEndTaskType.LEISTUNGSNACHWEIS)
                    .map(MonthEndTask::eligibleActorIds)
                    .toList())
                    .allSatisfy(eligibleActors -> assertThat(eligibleActors).containsExactly(COORDINATOR_ID));
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

    @Test
    void generate_shouldNotCreateLeistungsnachweis_whenProjectIsNonBillableEvenWithActiveLead() {
        UserRef employee = activeUser("employee", "00000000-0000-0000-0000-000000000050");
        UserRef lead = activeUser("lead", "00000000-0000-0000-0000-000000000051");
        MonthEndProjectSnapshot project = activeProject(101, false, Set.of(lead.id()));

        when(monthEndTaskRepository.findByMonth(month)).thenReturn(List.of());
        when(monthEndProjectSnapshotPort.findActiveIn(month)).thenReturn(List.of(project));
        when(monthEndUserSnapshotPort.findActiveIn(month)).thenReturn(List.of(employee, lead));
        when(monthEndProjectAssignmentPort.findAssignedUsernames(101, month)).thenReturn(Set.of("employee"));

        MonthEndTaskGenerationResult result = service.generate(month);

        assertThat(result.created()).isEqualTo(2);
        assertThat(result.skipped()).isZero();
        verify(monthEndTaskRepository).saveAll(argThat(tasks -> {
            assertThat(tasks).extracting(MonthEndTask::type)
                    .containsExactlyInAnyOrder(
                            MonthEndTaskType.EMPLOYEE_TIME_CHECK,
                            MonthEndTaskType.PROJECT_LEAD_REVIEW
                    );
            assertThat(tasks.stream()
                    .filter(task -> task.type() == MonthEndTaskType.LEISTUNGSNACHWEIS))
                    .isEmpty();
            return true;
        }));
    }

    @Test
    void generate_shouldLeaveAlreadyGeneratedTasksUntouchedAndSkipLeistungsnachweisForNewcomers_whenFlagDisabledAfterGeneration() {
        UserRef employee = activeUser("employee", "00000000-0000-0000-0000-000000000060");
        UserRef newcomer = activeUser("newcomer", "00000000-0000-0000-0000-000000000062");
        UserRef lead = activeUser("lead", "00000000-0000-0000-0000-000000000061");
        ProjectId projectId = ProjectId.generate();
        MonthEndProjectSnapshot project = projectWithLeistungsnachweisDisabled(projectId, 91, Set.of(lead.id()));

        MonthEndTask existingLeistungsnachweis = MonthEndTask.create(
                MonthEndTaskId.generate(), month, MonthEndTaskType.LEISTUNGSNACHWEIS,
                projectId, employee.id(), Set.of(lead.id()));
        List<MonthEndTask> alreadyGenerated = List.of(
                MonthEndTask.create(MonthEndTaskId.generate(), month, MonthEndTaskType.EMPLOYEE_TIME_CHECK,
                        projectId, employee.id(), Set.of(employee.id())),
                MonthEndTask.create(MonthEndTaskId.generate(), month, MonthEndTaskType.PROJECT_LEAD_REVIEW,
                        projectId, employee.id(), Set.of(lead.id())),
                existingLeistungsnachweis,
                MonthEndTask.create(MonthEndTaskId.generate(), month, MonthEndTaskType.ABRECHNUNG,
                        projectId, null, Set.of(lead.id()))
        );

        when(monthEndTaskRepository.findByMonth(month)).thenReturn(alreadyGenerated);
        when(monthEndProjectSnapshotPort.findActiveIn(month)).thenReturn(List.of(project));
        when(monthEndUserSnapshotPort.findActiveIn(month)).thenReturn(List.of(employee, newcomer, lead));
        when(monthEndProjectAssignmentPort.findAssignedUsernames(91, month))
                .thenReturn(Set.of("employee", "newcomer"));

        service.generate(month);

        // the newcomer gets the ungated task types, but no LEISTUNGSNACHWEIS while the flag is off
        verify(monthEndTaskRepository).saveAll(argThat(tasks -> tasks.stream()
                .noneMatch(task -> task.type() == MonthEndTaskType.LEISTUNGSNACHWEIS)));
        verify(monthEndTaskRepository).saveAll(argThat(tasks -> tasks.stream()
                .filter(task -> task.subjectEmployeeId() != null)
                .allMatch(task -> task.subjectEmployeeId().equals(newcomer.id()))));

        // the task generated before the flag was flipped is neither removed nor modified
        verify(monthEndTaskRepository).saveAll(argThat(tasks -> !tasks.contains(existingLeistungsnachweis)));
        assertThat(existingLeistungsnachweis.status()).isEqualTo(MonthEndTaskStatus.OPEN);
    }

    @Test
    void generate_shouldCreateOtherTasksAndSkipLeistungsnachweis_whenNoCoordinatorExists() {
        UserRef employee = activeUser("employee", "00000000-0000-0000-0000-000000000070");
        UserRef lead = activeUser("lead", "00000000-0000-0000-0000-000000000071");
        MonthEndProjectSnapshot project = activeProject(102, true, Set.of(lead.id()));

        when(monthEndUserSnapshotPort.findActiveCoordinatorIdsIn(month)).thenReturn(Set.of());
        when(monthEndTaskRepository.findByMonth(month)).thenReturn(List.of());
        when(monthEndProjectSnapshotPort.findActiveIn(month)).thenReturn(List.of(project));
        when(monthEndUserSnapshotPort.findActiveIn(month)).thenReturn(List.of(employee, lead));
        when(monthEndProjectAssignmentPort.findAssignedUsernames(102, month)).thenReturn(Set.of("employee"));

        MonthEndTaskGenerationResult result = service.generate(month);

        assertThat(result.created()).isEqualTo(3);
        verify(monthEndTaskRepository).saveAll(argThat(tasks -> tasks.stream()
                .map(MonthEndTask::type)
                .collect(Collectors.toSet())
                .equals(Set.of(
                        MonthEndTaskType.EMPLOYEE_TIME_CHECK,
                        MonthEndTaskType.PROJECT_LEAD_REVIEW,
                        MonthEndTaskType.ABRECHNUNG))));
    }

    @Test
    void generate_shouldAddOnlyMissingLeistungsnachweis_whenRerunWithCoordinator() {
        UserRef employee = activeUser("employee", "00000000-0000-0000-0000-000000000080");
        UserRef lead = activeUser("lead", "00000000-0000-0000-0000-000000000081");
        MonthEndProjectSnapshot project = activeProject(103, true, Set.of(lead.id()));

        when(monthEndTaskRepository.findByMonth(month)).thenReturn(List.of(
                MonthEndTask.create(MonthEndTaskId.generate(), month, MonthEndTaskType.EMPLOYEE_TIME_CHECK,
                        project.id(), employee.id(), Set.of(employee.id())),
                MonthEndTask.create(MonthEndTaskId.generate(), month, MonthEndTaskType.PROJECT_LEAD_REVIEW,
                        project.id(), employee.id(), Set.of(lead.id())),
                MonthEndTask.create(MonthEndTaskId.generate(), month, MonthEndTaskType.ABRECHNUNG,
                        project.id(), null, Set.of(lead.id()))
        ));
        when(monthEndProjectSnapshotPort.findActiveIn(month)).thenReturn(List.of(project));
        when(monthEndUserSnapshotPort.findActiveIn(month)).thenReturn(List.of(employee, lead));
        when(monthEndProjectAssignmentPort.findAssignedUsernames(103, month)).thenReturn(Set.of("employee"));

        MonthEndTaskGenerationResult result = service.generate(month);

        assertThat(result.created()).isOne();
        assertThat(result.skipped()).isEqualTo(3);
        verify(monthEndTaskRepository).saveAll(argThat(tasks -> tasks.size() == 1
                && tasks.getFirst().type() == MonthEndTaskType.LEISTUNGSNACHWEIS
                && tasks.getFirst().eligibleActorIds().equals(Set.of(COORDINATOR_ID))));
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
                billable,
                leadIds
        );
    }

    private MonthEndProjectSnapshot projectWithLeistungsnachweisDisabled(
            ProjectId projectId,
            int zepId,
            Set<UserId> leadIds
    ) {
        return new MonthEndProjectSnapshot(
                projectId,
                zepId,
                "Project-" + zepId,
                true,
                false,
                leadIds
        );
    }
}
