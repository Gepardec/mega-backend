package com.gepardec.mega.hexagon.monthend.application;

import com.gepardec.mega.hexagon.monthend.application.port.inbound.AbsentEmployeeAutoCompletion;
import com.gepardec.mega.hexagon.monthend.application.port.inbound.CompleteTasksForAbsentEmployeeUseCase;
import com.gepardec.mega.hexagon.monthend.domain.model.MonthEndClarification;
import com.gepardec.mega.hexagon.monthend.domain.model.MonthEndClarificationId;
import com.gepardec.mega.hexagon.monthend.domain.model.MonthEndTask;
import com.gepardec.mega.hexagon.monthend.domain.model.MonthEndTaskType;
import com.gepardec.mega.hexagon.monthend.domain.port.outbound.MonthEndClarificationRepository;
import com.gepardec.mega.hexagon.monthend.domain.port.outbound.MonthEndEmployeeAbsencePort;
import com.gepardec.mega.hexagon.monthend.domain.port.outbound.MonthEndTaskRepository;
import com.gepardec.mega.hexagon.shared.domain.model.UserId;
import com.gepardec.mega.hexagon.shared.domain.util.OfficeCalendarUtil;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

import java.time.Clock;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

@ApplicationScoped
@Transactional
public class CompleteTasksForAbsentEmployeeService implements CompleteTasksForAbsentEmployeeUseCase {

    public static final String SYSTEM_CLARIFICATION_TEXT =
            "Aufgrund von Abwesenheiten wurde der Monat automatisch bestätigt.";

    private final MonthEndEmployeeAbsencePort monthEndEmployeeAbsencePort;
    private final MonthEndTaskRepository monthEndTaskRepository;
    private final MonthEndClarificationRepository monthEndClarificationRepository;
    private final Clock clock;

    @Inject
    public CompleteTasksForAbsentEmployeeService(
            MonthEndEmployeeAbsencePort monthEndEmployeeAbsencePort,
            MonthEndTaskRepository monthEndTaskRepository,
            MonthEndClarificationRepository monthEndClarificationRepository,
            Clock clock
    ) {
        this.monthEndEmployeeAbsencePort = monthEndEmployeeAbsencePort;
        this.monthEndTaskRepository = monthEndTaskRepository;
        this.monthEndClarificationRepository = monthEndClarificationRepository;
        this.clock = clock;
    }

    @Override
    public Optional<AbsentEmployeeAutoCompletion> complete(UserId employeeId, YearMonth month) {
        Objects.requireNonNull(employeeId, "employeeId must not be null");
        Objects.requireNonNull(month, "month must not be null");

        Set<LocalDate> absentDays = new HashSet<>(
                monthEndEmployeeAbsencePort.findQualifyingAbsentDays(employeeId, month)
        );

        if (!isAbsentEveryWorkingDay(month, absentDays)) {
            return Optional.empty();
        }

        List<MonthEndTask> openTasks = monthEndTaskRepository.findOpenSubjectTasks(employeeId, month);
        if (openTasks.isEmpty()) {
            return Optional.empty();
        }

        List<MonthEndTask> completedTasks = openTasks.stream()
                .map(MonthEndTask::completeBySystem)
                .toList();
        monthEndTaskRepository.saveAll(completedTasks);
        createClarifications(month, employeeId, openTasks);

        return Optional.of(new AbsentEmployeeAutoCompletion(employeeId, month));
    }

    private boolean isAbsentEveryWorkingDay(YearMonth month, Set<LocalDate> absentDays) {
        return month.atDay(1).datesUntil(month.atEndOfMonth().plusDays(1))
                .filter(OfficeCalendarUtil::isWorkingDay)
                .allMatch(absentDays::contains);
    }

    private void createClarifications(YearMonth month, UserId employeeId, List<MonthEndTask> openTasks) {
        openTasks.stream()
                .filter(task -> task.type() == MonthEndTaskType.PROJECT_LEAD_REVIEW)
                .forEach(leadTask -> monthEndClarificationRepository.save(
                        MonthEndClarification.createBySystem(
                                MonthEndClarificationId.generate(),
                                month,
                                leadTask.projectId(),
                                employeeId,
                                leadTask.eligibleActorIds(),
                                SYSTEM_CLARIFICATION_TEXT,
                                clock.instant()
                        )
                ));
    }
}
