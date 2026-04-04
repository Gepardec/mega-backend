package com.gepardec.mega.hexagon.monthend.application;

import com.gepardec.mega.hexagon.monthend.domain.error.MonthEndEmployeeNotAssignedToProjectException;
import com.gepardec.mega.hexagon.monthend.domain.model.MonthEndClarification;
import com.gepardec.mega.hexagon.monthend.domain.model.MonthEndClarificationId;
import com.gepardec.mega.hexagon.monthend.domain.model.MonthEndClarificationSide;
import com.gepardec.mega.hexagon.monthend.domain.model.MonthEndPreparationResult;
import com.gepardec.mega.hexagon.monthend.domain.model.MonthEndProjectSnapshot;
import com.gepardec.mega.hexagon.monthend.domain.model.MonthEndTask;
import com.gepardec.mega.hexagon.monthend.domain.model.MonthEndTaskKey;
import com.gepardec.mega.hexagon.monthend.domain.model.MonthEndTaskType;
import com.gepardec.mega.hexagon.monthend.domain.model.MonthEndUserSnapshot;
import com.gepardec.mega.hexagon.monthend.domain.port.inbound.CreateMonthEndClarificationUseCase;
import com.gepardec.mega.hexagon.monthend.domain.port.outbound.MonthEndTaskRepository;
import com.gepardec.mega.hexagon.project.domain.model.ProjectId;
import com.gepardec.mega.hexagon.user.domain.model.EmploymentPeriod;
import com.gepardec.mega.hexagon.user.domain.model.EmploymentPeriods;
import com.gepardec.mega.hexagon.user.domain.model.UserId;
import com.gepardec.mega.hexagon.user.domain.model.UserStatus;
import org.instancio.Instancio;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class PrematureMonthEndPreparationServiceTest {

    private final YearMonth month = YearMonth.of(2026, 3);
    private final ProjectId projectId = ProjectId.of(Instancio.create(UUID.class));
    private final UserId employeeId = UserId.of(Instancio.create(UUID.class));
    private final UserId leadId = UserId.of(Instancio.create(UUID.class));

    private final Map<MonthEndTaskKey, MonthEndTask> storedTasks = new LinkedHashMap<>();

    private MonthEndTaskRepository monthEndTaskRepository;
    private ResolveMonthEndEmployeeProjectContextService contextResolver;
    private CreateMonthEndClarificationUseCase createMonthEndClarificationUseCase;
    private PrematureMonthEndPreparationService service;

    @BeforeEach
    void setUp() {
        storedTasks.clear();
        monthEndTaskRepository = mock(MonthEndTaskRepository.class);
        contextResolver = mock(ResolveMonthEndEmployeeProjectContextService.class);
        createMonthEndClarificationUseCase = mock(CreateMonthEndClarificationUseCase.class);
        service = new PrematureMonthEndPreparationService(
                monthEndTaskRepository,
                new MonthEndTaskPlanningService(),
                contextResolver,
                createMonthEndClarificationUseCase
        );

        when(monthEndTaskRepository.findByBusinessKey(any()))
                .thenAnswer(invocation -> Optional.ofNullable(storedTasks.get(invocation.getArgument(0))));
        doAnswer(invocation -> {
            MonthEndTask task = invocation.getArgument(0);
            storedTasks.put(task.businessKey(), task);
            return null;
        }).when(monthEndTaskRepository).save(any(MonthEndTask.class));
    }

    @Test
    void prepare_shouldEnsureOnlyEmployeeTimeCheck_whenProjectIsNonBillable() {
        when(contextResolver.resolve(month, projectId, employeeId)).thenReturn(context(false));

        MonthEndPreparationResult result = service.prepare(month, projectId, employeeId, null);

        assertThat(result.ensuredTasks())
                .singleElement()
                .satisfies(task -> {
                    assertThat(task.type()).isEqualTo(MonthEndTaskType.EMPLOYEE_TIME_CHECK);
                    assertThat(task.projectId()).isEqualTo(projectId);
                    assertThat(task.subjectEmployeeId()).isEqualTo(employeeId);
                });
        assertThat(result.hasClarification()).isFalse();
        assertThat(storedTasks).hasSize(1);
        verify(createMonthEndClarificationUseCase, never()).create(any(), any(), any(), any(), any(), any());
    }

    @Test
    void prepare_shouldEnsureBillableEmployeeOwnedTasksAndCreateClarification_whenTextProvided() {
        when(contextResolver.resolve(month, projectId, employeeId)).thenReturn(context(true));
        MonthEndClarification clarification = MonthEndClarification.create(
                MonthEndClarificationId.generate(),
                month,
                projectId,
                employeeId,
                employeeId,
                MonthEndClarificationSide.EMPLOYEE,
                Set.of(leadId),
                "Please review before my absence.",
                Instant.parse("2026-03-30T12:00:00Z")
        );
        when(createMonthEndClarificationUseCase.create(
                month,
                projectId,
                employeeId,
                employeeId,
                MonthEndClarificationSide.EMPLOYEE,
                "Please review before my absence."
        )).thenReturn(clarification);

        MonthEndPreparationResult result = service.prepare(
                month,
                projectId,
                employeeId,
                "Please review before my absence."
        );

        assertThat(result.ensuredTasks())
                .extracting(MonthEndTask::type)
                .containsExactly(MonthEndTaskType.EMPLOYEE_TIME_CHECK, MonthEndTaskType.LEISTUNGSNACHWEIS);
        assertThat(result.clarification()).isEqualTo(clarification);
        assertThat(storedTasks).hasSize(2);
        verify(createMonthEndClarificationUseCase).create(
                month,
                projectId,
                employeeId,
                employeeId,
                MonthEndClarificationSide.EMPLOYEE,
                "Please review before my absence."
        );
    }

    @Test
    void prepare_shouldBeIdempotent_whenRepeatedForSameProjectContext() {
        when(contextResolver.resolve(month, projectId, employeeId)).thenReturn(context(true));

        MonthEndPreparationResult first = service.prepare(month, projectId, employeeId, null);
        MonthEndPreparationResult second = service.prepare(month, projectId, employeeId, null);

        assertThat(storedTasks).hasSize(2);
        assertThat(second.ensuredTasks())
                .extracting(MonthEndTask::id)
                .containsExactlyElementsOf(first.ensuredTasks().stream().map(MonthEndTask::id).toList());
        verify(monthEndTaskRepository).save(first.ensuredTasks().get(0));
        verify(monthEndTaskRepository).save(first.ensuredTasks().get(1));
    }

    @Test
    void prepare_shouldReject_whenActorCannotPrepareProjectContext() {
        when(contextResolver.resolve(month, projectId, employeeId))
                .thenThrow(new MonthEndEmployeeNotAssignedToProjectException("employee is not assigned to project"));

        assertThatThrownBy(() -> service.prepare(month, projectId, employeeId, null))
                .isInstanceOf(MonthEndEmployeeNotAssignedToProjectException.class)
                .hasMessageContaining("not assigned");

        verify(monthEndTaskRepository, never()).save(any(MonthEndTask.class));
        verify(createMonthEndClarificationUseCase, never()).create(any(), any(), any(), any(), any(), any());
    }

    private MonthEndEmployeeProjectContext context(boolean billable) {
        return new MonthEndEmployeeProjectContext(
                month,
                new MonthEndProjectSnapshot(
                        projectId,
                        91,
                        "Project-91",
                        LocalDate.of(2025, 1, 1),
                        null,
                        billable,
                        Set.of(leadId)
                ),
                new MonthEndUserSnapshot(
                        employeeId,
                        "Employee User",
                        "employee",
                        UserStatus.ACTIVE,
                        new EmploymentPeriods(new EmploymentPeriod(LocalDate.of(2020, 1, 1), null))
                ),
                Set.of(leadId)
        );
    }
}
