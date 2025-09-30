package com.gepardec.mega.service.impl;

import com.gepardec.mega.db.entity.employee.EmployeeState;
import com.gepardec.mega.db.entity.employee.Step;
import com.gepardec.mega.db.entity.employee.StepEntry;
import com.gepardec.mega.db.entity.employee.User;
import com.gepardec.mega.db.repository.StepEntryRepository;
import com.gepardec.mega.domain.model.Employee;
import com.gepardec.mega.domain.model.ProjectEmployees;
import com.gepardec.mega.domain.utils.DateUtils;
import com.gepardec.mega.service.api.StepEntryService;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@ApplicationScoped
public class StepEntryServiceImpl implements StepEntryService {

    @Inject
    Logger logger;

    @Inject
    StepEntryRepository stepEntryRepository;

    @Override
    public Optional<EmployeeState> findEmployeeCheckState(final Employee employee) {
        if (employee != null) {
            return findEmployeeCheckState(employee, YearMonth.from(LocalDate.parse(employee.getReleaseDate()))).map(Pair::getLeft);
        }
        return Optional.empty();
    }

    /**
     * @return Pair.left: state, pair.right: stateReason
     */
    @Override
    public Optional<Pair<EmployeeState, String>> findEmployeeCheckState(final Employee employee, YearMonth payrollMonth) {
        Optional<StepEntry> stepEntries =
                stepEntryRepository.findControlTimesStepEntryByOwnerAndEntryDate(payrollMonth.atDay(1), employee.getEmail());

        return stepEntries.map(se -> Pair.of(se.getState(), se.getStateReason()));
    }

    @Override
    public Optional<StepEntry> findControlTimesStepEntry(final String employeeEmail, LocalDate date) {
        return stepEntryRepository.findControlTimesStepEntryByOwnerAndEntryDate(date, employeeEmail);
    }

    @Override
    public Optional<EmployeeState> findEmployeeInternalCheckState(Employee employee, YearMonth payrollMonth) {
        if (employee != null) {
            return stepEntryRepository.findAllOwnedAndAssignedStepEntriesForEmployeeForControlInternalTimes(payrollMonth.atDay(1), employee.getEmail())
                    .map(StepEntry::getState);
        }
        return Optional.empty();
    }

    @Override
    public List<StepEntry> findAllOwnedAndUnassignedStepEntriesExceptControlTimes(Employee employee, YearMonth payrollMonth) {
        return stepEntryRepository.findAllOwnedAndUnassignedStepEntriesExceptControlTimes(payrollMonth.atDay(1), employee.getEmail());
    }

    @Override
    public List<StepEntry> findAllOwnedAndUnassignedStepEntriesForPMProgress(final String email, final YearMonth payrollMonth) {
        return stepEntryRepository.findAllOwnedAndUnassignedStepEntriesForPMProgress(payrollMonth.atDay(1), email);
    }

    @Override
    @Transactional(Transactional.TxType.SUPPORTS)
    public void addStepEntry(com.gepardec.mega.domain.model.StepEntry stepEntry) {
        final User ownerDb = new User();
        ownerDb.setId(stepEntry.getOwner().getDbId());

        final Step stepDb = new Step();
        stepDb.setId(stepEntry.getStep().getDbId());

        final User assigneeDb = new User();
        assigneeDb.setId(stepEntry.getAssignee().getDbId());

        final StepEntry stepEntryDb = new StepEntry();
        stepEntryDb.setDate(stepEntry.getDate());
        stepEntryDb.setProject(stepEntry.getProject() != null ? stepEntry.getProject().getProjectId() : null);
        stepEntryDb.setState(EmployeeState.OPEN);
        stepEntryDb.setOwner(ownerDb);
        stepEntryDb.setAssignee(assigneeDb);
        stepEntryDb.setStep(stepDb);

        logger.debug("inserting step entry {}", stepEntryDb);

        stepEntryRepository.persist(stepEntryDb);
    }

    @Override
    public boolean setOpenAndAssignedStepEntriesDone(Employee employee, Long stepId, YearMonth payrollMonth) {
        return stepEntryRepository.updateStateAssigned(payrollMonth.atDay(1), payrollMonth.atEndOfMonth(), employee.getEmail(), stepId, EmployeeState.DONE) > 0;
    }

    @Override
    public boolean updateStepEntryReasonForStepWithStateDone(Employee employee, Long stepId, YearMonth payrollMonth, String reason) {
        return stepEntryRepository.updateReasonForStepEntryWithStateDone(payrollMonth.atDay(1), payrollMonth.atEndOfMonth(), employee.getEmail(), stepId, reason) > 0;
    }

    @Override
    public boolean updateStepEntryStateForEmployee(Employee employee, Long stepId, LocalDate from, LocalDate to,
                                                   EmployeeState newState, String reason) {
        return stepEntryRepository.updateStateAssignedWithReason(from, to, employee.getEmail(), stepId, newState, reason) > 0;
    }

    @Override
    public boolean updateStepEntryStateForEmployeeInProject(Employee employee, Long stepId, String project, String currentMonthYear, EmployeeState newState) {
        LocalDate fromDate = DateUtils.getFirstDayOfCurrentMonth(currentMonthYear);
        LocalDate toDate = DateUtils.getLastDayOfCurrentMonth(currentMonthYear);

        return stepEntryRepository.updateStateAssigned(fromDate, toDate, employee.getEmail(), stepId, project, newState) > 0;
    }

    @Override
    public List<StepEntry> findAllStepEntriesForEmployee(Employee employee, YearMonth payrollMonth) {
        Objects.requireNonNull(employee, "Employee must not be null!");
        return stepEntryRepository.findAllOwnedStepEntriesInRange(payrollMonth.atDay(1), payrollMonth.atEndOfMonth(), employee.getEmail());
    }

    @Override
    public List<StepEntry> findAllStepEntriesForEmployeeAndProject(Employee employee, String projectId, String assigneEmail,
                                                                   YearMonth payrollMonth) {
        Objects.requireNonNull(employee, "Employee must not be null!");

        List<StepEntry> stepEntries = new ArrayList<>();
        stepEntries.addAll(stepEntryRepository.findAllOwnedStepEntriesInRange(payrollMonth.atDay(1), payrollMonth.atEndOfMonth(), employee.getEmail(), projectId, assigneEmail));
        stepEntries.addAll(stepEntryRepository.findAllOwnedStepEntriesInRange(payrollMonth.atDay(1), payrollMonth.atEndOfMonth(), employee.getEmail()));
        return stepEntries;
    }

    @Override
    public StepEntry findStepEntryForEmployeeAtStep(final Long stepId,
                                                    final String employeeEmail,
                                                    final String assigneeEmail,
                                                    final YearMonth payrollMonth) {
        Objects.requireNonNull(employeeEmail, "'employeeEmail' must not be null!");
        LocalDate fromDate = payrollMonth.atDay(1);
        LocalDate toDate = payrollMonth.atEndOfMonth();

        return stepEntryRepository.findStepEntryForEmployeeAtStepInRange(
                        fromDate,
                        toDate,
                        employeeEmail,
                        stepId,
                        assigneeEmail
                )
                .orElseThrow(() ->
                        new IllegalStateException("No StepEntries found for Employee %s".formatted(employeeEmail))
                );
    }

    @Override
    public StepEntry findStepEntryForEmployeeAndProjectAtStep(final Long stepId,
                                                              final String employeeEmail,
                                                              final String assigneeEmail,
                                                              final String project,
                                                              final YearMonth payrollMonth) {
        Objects.requireNonNull(employeeEmail, "'employeeEmail' must not be null!");
        LocalDate fromDate = payrollMonth.atDay(1);
        LocalDate toDate = payrollMonth.atEndOfMonth();

        return stepEntryRepository.findStepEntryForEmployeeAndProjectAtStepInRange(
                        fromDate,
                        toDate,
                        employeeEmail,
                        stepId,
                        assigneeEmail,
                        project
                )
                .orElseThrow(() ->
                        new IllegalStateException("No StepEntries found for Employee %s".formatted(employeeEmail))
                );
    }

    @Override
    public List<ProjectEmployees> getProjectEmployeesForPM(final YearMonth payrollMonth, String assigneEmail) {
        return stepEntryRepository.findAllStepEntriesForPMInRange(payrollMonth.atDay(1), payrollMonth.atEndOfMonth(), assigneEmail)
                .stream()
                .collect(
                        Collectors.groupingBy(
                                StepEntry::getProject,
                                Collectors.mapping(s -> s.getOwner().getZepId(), Collectors.toList())
                        )
                )
                .entrySet()
                .stream()
                .map(e -> ProjectEmployees.builder().projectId(e.getKey()).employees(e.getValue()).build())
                .toList();
    }

    @Override
    public List<ProjectEmployees> getAllProjectEmployeesForPM(YearMonth payrollMonth) {
        return stepEntryRepository.findAllStepEntriesForAllPMInRange(payrollMonth.atDay(1), payrollMonth.atEndOfMonth())
                .stream()
                .collect(
                        Collectors.groupingBy(
                                StepEntry::getProject,
                                Collectors.mapping(s -> s.getOwner().getZepId(), Collectors.toList())
                        )
                )
                .entrySet()
                .stream()
                .map(e -> ProjectEmployees.builder().projectId(e.getKey()).employees(e.getValue()).build())
                .toList();
    }

    @Override
    public List<StepEntry> findAll() {
        return stepEntryRepository.findAll().list();
    }
}
