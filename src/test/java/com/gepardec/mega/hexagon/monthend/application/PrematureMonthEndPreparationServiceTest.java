package com.gepardec.mega.hexagon.monthend.application;

import com.gepardec.mega.hexagon.monthend.domain.error.MonthEndEmployeeContextNotFoundException;
import com.gepardec.mega.hexagon.monthend.domain.model.MonthEndClarification;
import com.gepardec.mega.hexagon.monthend.domain.model.MonthEndProjectSnapshot;
import com.gepardec.mega.hexagon.monthend.domain.model.MonthEndTask;
import com.gepardec.mega.hexagon.monthend.domain.model.MonthEndTaskType;
import com.gepardec.mega.hexagon.monthend.domain.port.outbound.MonthEndClarificationRepository;
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
import org.mockito.ArgumentCaptor;

import java.time.Clock;
import java.time.Instant;
import java.time.YearMonth;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.tuple;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class PrematureMonthEndPreparationServiceTest {

    private final YearMonth month = YearMonth.of(2026, 3);
    private final UserId employeeId = UserId.of(Instancio.create(UUID.class));
    private final UserId leadAId = UserId.of(Instancio.create(UUID.class));
    private final UserId leadBId = UserId.of(Instancio.create(UUID.class));
    private final UserId inactiveLeadId = UserId.of(Instancio.create(UUID.class));

    private MonthEndTaskRepository monthEndTaskRepository;
    private MonthEndClarificationRepository monthEndClarificationRepository;
    private MonthEndProjectSnapshotPort monthEndProjectSnapshotPort;
    private MonthEndUserSnapshotPort monthEndUserSnapshotPort;
    private MonthEndProjectAssignmentPort monthEndProjectAssignmentPort;
    private PrematureMonthEndPreparationService service;

    @BeforeEach
    void setUp() {
        monthEndTaskRepository = mock(MonthEndTaskRepository.class);
        monthEndClarificationRepository = mock(MonthEndClarificationRepository.class);
        monthEndProjectSnapshotPort = mock(MonthEndProjectSnapshotPort.class);
        monthEndUserSnapshotPort = mock(MonthEndUserSnapshotPort.class);
        monthEndProjectAssignmentPort = mock(MonthEndProjectAssignmentPort.class);
        Clock clock = Clock.fixed(Instant.parse("2026-03-30T12:00:00Z"), ZoneOffset.UTC);
        service = new PrematureMonthEndPreparationService(
                monthEndTaskRepository,
                new MonthEndTaskPlanningService(),
                monthEndProjectSnapshotPort,
                monthEndUserSnapshotPort,
                monthEndProjectAssignmentPort,
                monthEndClarificationRepository,
                clock
        );
    }

    @Test
    void prepare_shouldFanOutAcrossAssignedProjectsSkipExistingContextsAndCreateClarifications() {
        UserRef employee = userRef(employeeId, "employee");
        UserRef leadA = userRef(leadAId, "lead-a");
        UserRef leadB = userRef(leadBId, "lead-b");
        MonthEndProjectSnapshot billableProject = project(101, true, leadAId, inactiveLeadId);
        MonthEndProjectSnapshot nonBillableProject = project(102, false, leadBId);
        MonthEndProjectSnapshot alreadyPreparedProject = project(103, true, leadAId);
        MonthEndProjectSnapshot unassignedProject = project(104, true, leadAId);

        when(monthEndUserSnapshotPort.findActiveIn(month)).thenReturn(List.of(employee, leadA, leadB));
        when(monthEndProjectSnapshotPort.findActiveIn(month))
                .thenReturn(List.of(billableProject, nonBillableProject, alreadyPreparedProject, unassignedProject));
        when(monthEndProjectAssignmentPort.findAssignedUsernames(101, month)).thenReturn(Set.of("employee"));
        when(monthEndProjectAssignmentPort.findAssignedUsernames(102, month)).thenReturn(Set.of("employee"));
        when(monthEndProjectAssignmentPort.findAssignedUsernames(103, month)).thenReturn(Set.of("employee"));
        when(monthEndProjectAssignmentPort.findAssignedUsernames(104, month)).thenReturn(Set.of("someone-else"));
        when(monthEndTaskRepository.existsForSubjectEmployee(month, billableProject.id(), employeeId)).thenReturn(false);
        when(monthEndTaskRepository.existsForSubjectEmployee(month, nonBillableProject.id(), employeeId)).thenReturn(false);
        when(monthEndTaskRepository.existsForSubjectEmployee(month, alreadyPreparedProject.id(), employeeId)).thenReturn(true);

        service.prepare(month, employeeId, "Vacation.");

        ArgumentCaptor<MonthEndTask> taskCaptor = ArgumentCaptor.forClass(MonthEndTask.class);
        verify(monthEndTaskRepository, times(3)).save(taskCaptor.capture());
        assertThat(taskCaptor.getAllValues())
                .extracting(MonthEndTask::projectId, MonthEndTask::type, MonthEndTask::subjectEmployeeId)
                .containsExactly(
                        tuple(billableProject.id(), MonthEndTaskType.EMPLOYEE_TIME_CHECK, employeeId),
                        tuple(billableProject.id(), MonthEndTaskType.LEISTUNGSNACHWEIS, employeeId),
                        tuple(nonBillableProject.id(), MonthEndTaskType.EMPLOYEE_TIME_CHECK, employeeId)
                );

        ArgumentCaptor<MonthEndClarification> clarificationCaptor =
                ArgumentCaptor.forClass(MonthEndClarification.class);
        verify(monthEndClarificationRepository, times(2)).save(clarificationCaptor.capture());
        assertThat(clarificationCaptor.getAllValues())
                .extracting(MonthEndClarification::projectId, MonthEndClarification::text)
                .containsExactly(
                        tuple(billableProject.id(), "Vacation."),
                        tuple(nonBillableProject.id(), "Vacation.")
                );
        assertThat(clarificationCaptor.getAllValues())
                .filteredOn(clarification -> clarification.projectId().equals(billableProject.id()))
                .singleElement()
                .satisfies(clarification -> {
                    assertThat(clarification.subjectEmployeeId()).isEqualTo(employeeId);
                    assertThat(clarification.createdBy()).isEqualTo(employeeId);
                    assertThat(clarification.eligibleProjectLeadIds()).containsExactly(leadAId);
                });
        assertThat(clarificationCaptor.getAllValues())
                .filteredOn(clarification -> clarification.projectId().equals(nonBillableProject.id()))
                .singleElement()
                .satisfies(clarification -> assertThat(clarification.eligibleProjectLeadIds()).containsExactly(leadBId));
        verify(monthEndTaskRepository, never()).existsForSubjectEmployee(month, unassignedProject.id(), employeeId);
    }

    @Test
    void prepare_shouldThrow_whenActorIsNotActiveInMonth() {
        when(monthEndUserSnapshotPort.findActiveIn(month)).thenReturn(List.of(userRef(leadAId, "lead-a")));

        assertThatThrownBy(() -> service.prepare(month, employeeId, "Vacation."))
                .isInstanceOf(MonthEndEmployeeContextNotFoundException.class)
                .hasMessageContaining(employeeId.value().toString());

        verifyNoInteractions(
                monthEndProjectSnapshotPort,
                monthEndProjectAssignmentPort,
                monthEndTaskRepository,
                monthEndClarificationRepository
        );
    }

    private MonthEndProjectSnapshot project(int zepId, boolean billable, UserId... leadIds) {
        return new MonthEndProjectSnapshot(
                ProjectId.of(Instancio.create(UUID.class)),
                zepId,
                "Project-" + zepId,
                billable,
                Set.of(leadIds)
        );
    }

    private UserRef userRef(UserId id, String username) {
        return new UserRef(
                id,
                FullName.of("Test", username),
                ZepUsername.of(username)
        );
    }
}
