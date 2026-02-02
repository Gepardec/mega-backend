package com.gepardec.mega.service.api;

import com.gepardec.mega.db.entity.employee.EmployeeState;
import com.gepardec.mega.db.entity.employee.StepEntryEntity;
import com.gepardec.mega.domain.model.Employee;
import com.gepardec.mega.domain.model.ProjectEmployees;
import com.gepardec.mega.domain.model.StepEntry;
import org.apache.commons.lang3.tuple.Pair;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.Optional;

public interface StepEntryService {
    Optional<Pair<EmployeeState, String>> findEmployeeCheckState(final Employee employee, YearMonth payrollMonth);

    Optional<StepEntryEntity> findControlTimesStepEntry(final String employeeEmail, LocalDate date);

    Optional<EmployeeState> findEmployeeCheckState(final Employee employee);

    Optional<EmployeeState> findEmployeeInternalCheckState(final Employee employee, YearMonth payrollMonth);

    List<StepEntryEntity> findAllOwnedAndUnassignedStepEntriesExceptControlTimes(final Employee employee, YearMonth payrollMonth);

    List<StepEntryEntity> findAllOwnedAndUnassignedStepEntriesForPMProgress(final String email, final YearMonth payrollMonth);

    void addStepEntry(final StepEntry stepEntry);

    boolean setOpenAndAssignedStepEntriesDone(Employee employee, Long stepId, YearMonth payrollMonth);

    boolean updateStepEntryReasonForStepWithStateDone(Employee employee, Long stepId, YearMonth payrollMonth, String reason);

    boolean updateStepEntryStateForEmployee(Employee employee, Long stepId, LocalDate from, LocalDate to, EmployeeState newState, String reason);

    boolean updateStepEntryStateForEmployeeInProject(Employee employee, Long stepId, String project, String currentMonthYear, EmployeeState employeeState);

    List<StepEntryEntity> findAllStepEntriesForEmployee(Employee employee, YearMonth payrollMonth);

    List<StepEntryEntity> findAllStepEntriesForEmployeeAndProject(Employee employee, String projectId, String assigneEmail, YearMonth payrollMonth);

    StepEntryEntity findStepEntryForEmployeeAtStep(Long stepId, String employeeEmail, String assigneeEmail, YearMonth payrollMonth);

    StepEntryEntity findStepEntryForEmployeeAndProjectAtStep(Long stepId, String employeeEmail, String assigneeEmail, String project, YearMonth payrollMonth);

    List<ProjectEmployees> getProjectEmployeesForPM(final YearMonth payrollMonth, final String assigneEmail);

    List<ProjectEmployees> getAllProjectEmployeesForPM(final YearMonth payrollMonth);

    List<StepEntryEntity> findAll();
}
