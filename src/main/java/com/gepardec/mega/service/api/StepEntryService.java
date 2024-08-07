package com.gepardec.mega.service.api;

import com.gepardec.mega.db.entity.employee.EmployeeState;
import com.gepardec.mega.domain.model.Employee;
import com.gepardec.mega.domain.model.ProjectEmployees;
import com.gepardec.mega.domain.model.StepEntry;
import org.apache.commons.lang3.tuple.Pair;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface StepEntryService {
    Optional<Pair<EmployeeState, String>> findEmployeeCheckState(final Employee employee, LocalDate date);

    Optional<com.gepardec.mega.db.entity.employee.StepEntry> findControlTimesStepEntry(final Employee employee, LocalDate date);

    Optional<EmployeeState> findEmployeeCheckState(final Employee employee);

    Optional<EmployeeState> findEmployeeInternalCheckState(final Employee employee, LocalDate date);

    List<com.gepardec.mega.db.entity.employee.StepEntry> findAllOwnedAndUnassignedStepEntriesExceptControlTimes(final Employee employee, LocalDate currentMonthYear);

    List<com.gepardec.mega.db.entity.employee.StepEntry> findAllOwnedAndUnassignedStepEntriesForPMProgress(final String email, final LocalDate date);

    void addStepEntry(final StepEntry stepEntry);

    boolean setOpenAndAssignedStepEntriesDone(Employee employee, Long stepId, LocalDate from, LocalDate to);

    boolean updateStepEntryReasonForStepWithStateDone(Employee employee, Long stepId, LocalDate from, LocalDate to, String reason);

    boolean updateStepEntryStateForEmployee(Employee employee, Long stepId, LocalDate from, LocalDate to, EmployeeState newState, String reason);

    boolean updateStepEntryStateForEmployeeInProject(Employee employee, Long stepId, String project, String currentMonthYear, EmployeeState employeeState);

    List<com.gepardec.mega.db.entity.employee.StepEntry> findAllStepEntriesForEmployee(Employee employee, LocalDate from, LocalDate to);

    List<com.gepardec.mega.db.entity.employee.StepEntry> findAllStepEntriesForEmployeeAndProject(Employee employee, String projectId, String assigneEmail, LocalDate from, LocalDate to);

    com.gepardec.mega.db.entity.employee.StepEntry findStepEntryForEmployeeAtStep(Long stepId, String employeeEmail, String assigneeEmail, String currentMonthYear);

    com.gepardec.mega.db.entity.employee.StepEntry findStepEntryForEmployeeAndProjectAtStep(Long stepId, String employeeEmail, String assigneeEmail, String project, String currentMonthYear);

    List<ProjectEmployees> getProjectEmployeesForPM(final LocalDate from, final LocalDate to, final String assigneEmail);

    List<ProjectEmployees> getAllProjectEmployeesForPM(final LocalDate from, final LocalDate to);

    List<com.gepardec.mega.db.entity.employee.StepEntry> findAll();
}
