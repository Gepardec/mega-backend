package com.gepardec.mega.hexagon.monthend.application;

import com.gepardec.mega.hexagon.monthend.application.port.inbound.AbsentEmployeeAutoCompletion;
import com.gepardec.mega.hexagon.monthend.domain.model.MonthEndClarification;
import com.gepardec.mega.hexagon.monthend.domain.model.MonthEndTask;
import com.gepardec.mega.hexagon.monthend.domain.model.MonthEndTaskId;
import com.gepardec.mega.hexagon.monthend.domain.model.MonthEndTaskStatus;
import com.gepardec.mega.hexagon.monthend.domain.model.MonthEndTaskType;
import com.gepardec.mega.hexagon.monthend.domain.port.outbound.MonthEndClarificationRepository;
import com.gepardec.mega.hexagon.monthend.domain.port.outbound.MonthEndEmployeeAbsencePort;
import com.gepardec.mega.hexagon.monthend.domain.port.outbound.MonthEndTaskRepository;
import com.gepardec.mega.hexagon.shared.domain.SystemActor;
import com.gepardec.mega.hexagon.shared.domain.model.ProjectId;
import com.gepardec.mega.hexagon.shared.domain.model.UserId;
import com.gepardec.mega.hexagon.shared.domain.util.OfficeCalendarUtil;
import org.instancio.Instancio;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CompleteTasksForAbsentEmployeeServiceTest {

    private static final YearMonth MONTH = YearMonth.of(2026, 3);
    private static final Instant NOW = Instant.parse("2026-03-31T16:00:00Z");

    private final UserId employeeId = UserId.of(Instancio.create(UUID.class));
    private final UserId leadA = UserId.of(Instancio.create(UUID.class));
    private final UserId leadB = UserId.of(Instancio.create(UUID.class));
    private final ProjectId projectA = ProjectId.of(Instancio.create(UUID.class));
    private final ProjectId projectB = ProjectId.of(Instancio.create(UUID.class));

    @Mock
    private MonthEndEmployeeAbsencePort monthEndEmployeeAbsencePort;

    @Mock
    private MonthEndTaskRepository monthEndTaskRepository;

    @Mock
    private MonthEndClarificationRepository monthEndClarificationRepository;

    private CompleteTasksForAbsentEmployeeService service;

    @BeforeEach
    void setUp() {
        service = new CompleteTasksForAbsentEmployeeService(
                monthEndEmployeeAbsencePort,
                monthEndTaskRepository,
                monthEndClarificationRepository,
                Clock.fixed(NOW, ZoneOffset.UTC)
        );
    }

    @Test
    void complete_shouldCompleteOpenTasksAndCreateOneClarificationPerProject_whenEmployeeFullyAbsent() {
        List<MonthEndTask> openTasks = List.of(
                employeeTask(projectA),
                leadReviewTask(projectA, Set.of(leadA)),
                employeeTask(projectB),
                leadReviewTask(projectB, Set.of(leadB))
        );
        when(monthEndEmployeeAbsencePort.findQualifyingAbsentDays(employeeId, MONTH)).thenReturn(workingDays());
        when(monthEndTaskRepository.findOpenSubjectTasks(employeeId, MONTH)).thenReturn(openTasks);

        Optional<AbsentEmployeeAutoCompletion> result = service.complete(employeeId, MONTH);

        assertThat(result).contains(new AbsentEmployeeAutoCompletion(employeeId, MONTH));
        ArgumentCaptor<List<MonthEndTask>> tasksCaptor = ArgumentCaptor.forClass(List.class);
        verify(monthEndTaskRepository).saveAll(tasksCaptor.capture());
        assertThat(tasksCaptor.getValue()).hasSize(4)
                .allSatisfy(task -> {
                    assertThat(task.status()).isEqualTo(MonthEndTaskStatus.DONE);
                    assertThat(task.completedBy()).isEqualTo(SystemActor.USER_ID);
                });

        ArgumentCaptor<MonthEndClarification> clarificationCaptor =
                ArgumentCaptor.forClass(MonthEndClarification.class);
        verify(monthEndClarificationRepository, org.mockito.Mockito.times(2)).save(clarificationCaptor.capture());
        assertThat(clarificationCaptor.getAllValues())
                .extracting(MonthEndClarification::projectId)
                .containsExactlyInAnyOrder(projectA, projectB);
        assertThat(clarificationCaptor.getAllValues())
                .allSatisfy(clarification -> {
                    assertThat(clarification.createdBy()).isEqualTo(SystemActor.USER_ID);
                    assertThat(clarification.text()).isEqualTo(CompleteTasksForAbsentEmployeeService.SYSTEM_CLARIFICATION_TEXT);
                    assertThat(clarification.createdAt()).isEqualTo(NOW);
                });
    }

    @Test
    void complete_shouldReturnEmptyAndModifyNothing_whenEmployeePartiallyAbsent() {
        List<LocalDate> partialAbsences = workingDays().stream()
                .limit(workingDays().size() - 1L)
                .toList();
        when(monthEndEmployeeAbsencePort.findQualifyingAbsentDays(employeeId, MONTH)).thenReturn(partialAbsences);

        Optional<AbsentEmployeeAutoCompletion> result = service.complete(employeeId, MONTH);

        assertThat(result).isEmpty();
        verifyNoInteractions(monthEndTaskRepository, monthEndClarificationRepository);
    }

    @Test
    void complete_shouldReturnEmpty_whenNoOpenTasksExist() {
        when(monthEndEmployeeAbsencePort.findQualifyingAbsentDays(employeeId, MONTH)).thenReturn(workingDays());
        when(monthEndTaskRepository.findOpenSubjectTasks(employeeId, MONTH)).thenReturn(List.of());

        Optional<AbsentEmployeeAutoCompletion> result = service.complete(employeeId, MONTH);

        assertThat(result).isEmpty();
        verify(monthEndTaskRepository, never()).saveAll(org.mockito.ArgumentMatchers.any());
        verifyNoInteractions(monthEndClarificationRepository);
    }

    private MonthEndTask employeeTask(ProjectId projectId) {
        return MonthEndTask.create(
                MonthEndTaskId.generate(),
                MONTH,
                MonthEndTaskType.EMPLOYEE_TIME_CHECK,
                projectId,
                employeeId,
                Set.of(employeeId)
        );
    }

    private MonthEndTask leadReviewTask(ProjectId projectId, Set<UserId> leadIds) {
        return MonthEndTask.create(
                MonthEndTaskId.generate(),
                MONTH,
                MonthEndTaskType.PROJECT_LEAD_REVIEW,
                projectId,
                employeeId,
                leadIds
        );
    }

    private List<LocalDate> workingDays() {
        return MONTH.atDay(1).datesUntil(MONTH.atEndOfMonth().plusDays(1))
                .filter(OfficeCalendarUtil::isWorkingDay)
                .toList();
    }
}
