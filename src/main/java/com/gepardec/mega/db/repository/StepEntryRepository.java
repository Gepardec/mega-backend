package com.gepardec.mega.db.repository;

import com.gepardec.mega.db.entity.employee.EmployeeState;
import com.gepardec.mega.db.entity.employee.StepEntry;
import com.gepardec.mega.domain.model.StepName;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import io.quarkus.panache.common.Parameters;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@ApplicationScoped
public class StepEntryRepository implements PanacheRepository<StepEntry> {

    private static final String P_ASSIGNEE_EMAIL = "assigneeEmail";
    private static final String P_EMPLOYEE_STATE = "employeeState";
    private static final String P_END = "end";
    private static final String P_ENTRY_DATE = "entryDate";
    private static final String P_OWNER_EMAIL = "ownerEmail";
    private static final String P_PROJECT = "project";
    private static final String P_PROJECT_ID = "projectId";
    private static final String P_REASON = "reason";
    private static final String P_START = "start";
    private static final String P_STATE_REASON = "stateReason";
    private static final String P_STEP_ID = "stepId";

    public Optional<StepEntry> findControlTimesStepEntryByOwnerAndEntryDate(LocalDate entryDate, String ownerAndAssigneeEmail) {
        return find("#StepEntry.findAllOwnedAndAssignedStepEntriesForEmployee",
                Parameters
                        .with(P_ENTRY_DATE, entryDate)
                        .and(P_OWNER_EMAIL, ownerAndAssigneeEmail)
                        .and(P_ASSIGNEE_EMAIL, ownerAndAssigneeEmail)
                        .and(P_STEP_ID, StepName.CONTROL_TIMES.getId()))
                .singleResultOptional();
    }

    public Optional<StepEntry> findAllOwnedAndAssignedStepEntriesForEmployeeForControlInternalTimes(LocalDate entryDate, String ownerAndAssigneeEmail) {
        return find("#StepEntry.findAllOwnedAndAssignedStepEntriesForEmployeeForControlInternalTimes",
                Parameters
                        .with(P_ENTRY_DATE, entryDate)
                        .and(P_OWNER_EMAIL, ownerAndAssigneeEmail)
                        .and(P_ASSIGNEE_EMAIL, ownerAndAssigneeEmail)
                        .and(P_STEP_ID, StepName.CONTROL_INTERNAL_TIMES.getId()))
                .singleResultOptional();
    }

    public List<StepEntry> findAllOwnedAndUnassignedStepEntriesExceptControlTimes(LocalDate entryDate, String ownerEmail) {
        List<StepEntry> entries = find("#StepEntry.findAllOwnedAndUnassignedStepEntriesExceptControlTimes",
                Parameters
                        .with(P_ENTRY_DATE, entryDate)
                        .and(P_OWNER_EMAIL, ownerEmail))
                .list();

        entries.addAll(find("#StepEntry.findAllOwnedAndAssignedStepEntriesForEmployee",
                Parameters
                        .with(P_ENTRY_DATE, entryDate)
                        .and(P_OWNER_EMAIL, ownerEmail)
                        .and(P_ASSIGNEE_EMAIL, ownerEmail)
                        .and(P_STEP_ID, StepName.CONTROL_TIME_EVIDENCES.getId()))
                .list());

        return entries;
    }

    public List<StepEntry> findAllOwnedAndUnassignedStepEntriesForPMProgress(LocalDate entryDate, String ownerEmail) {
        return find("#StepEntry.findAllOwnedAndUnassignedStepEntriesForPMProgress",
                Parameters
                        .with(P_ENTRY_DATE, entryDate)
                        .and(P_OWNER_EMAIL, ownerEmail)
                        .and(P_STEP_ID, StepName.CONTROL_TIME_EVIDENCES.getId()))
                .list();
    }

    public List<StepEntry> findAllOwnedStepEntriesInRange(LocalDate startDate, LocalDate endDate, String ownerEmail) {
        return find("#StepEntry.findAllOwnedStepEntriesInRange",
                Parameters
                        .with(P_START, startDate)
                        .and(P_END, endDate)
                        .and(P_OWNER_EMAIL, ownerEmail)
        ).list();
    }

    public List<StepEntry> findAllOwnedStepEntriesInRange(LocalDate startDate, LocalDate endDate, String ownerEmail, String projectId, String assigneeEmail) {
        return find("#StepEntry.findAllOwnedStepEntriesInRangeForProject",
                Parameters
                        .with(P_START, startDate)
                        .and(P_END, endDate)
                        .and(P_OWNER_EMAIL, ownerEmail)
                        .and(P_ASSIGNEE_EMAIL, assigneeEmail)
                        .and(P_PROJECT_ID, projectId))
                .list();
    }

    @Transactional
    public int updateStateAssigned(LocalDate startDate, LocalDate endDate, String ownerEmail, Long stepId, EmployeeState newState) {
        return updateStateAssignedWithReason(startDate, endDate, ownerEmail, stepId, newState, null);
    }

    @Transactional
    public int updateStateAssignedWithReason(LocalDate startDate, LocalDate endDate, String ownerEmail, Long stepId, EmployeeState newState, String stateReason) {
        return update("UPDATE StepEntry s SET s.employeeState = :employeeState, s.stateReason = :stateReason" +
                        " WHERE s.id IN" +
                        " (SELECT s.id FROM StepEntry s WHERE s.date BETWEEN :start AND :end AND s.owner.email = :ownerEmail AND s.step.id = :stepId)",
                Parameters
                        .with(P_EMPLOYEE_STATE, newState)
                        .and(P_STATE_REASON, stateReason)
                        .and(P_START, startDate)
                        .and(P_END, endDate)
                        .and(P_OWNER_EMAIL, ownerEmail)
                        .and(P_STEP_ID, stepId));
    }

    @Transactional
    public int updateReasonForStepEntryWithStateDone(LocalDate startDate, LocalDate endDate, String ownerEmail, Long stepId, String reason) {
        return update("UPDATE StepEntry s SET s.stateReason = :reason" +
                        " WHERE s.id IN" +
                        " (SELECT s.id FROM StepEntry s WHERE s.date BETWEEN :start AND :end AND " +
                        "         s.owner.email = :ownerEmail AND s.step.id = :stepId AND" +
                        "         s.employeeState = :employeeState)",
                Parameters
                        .with(P_EMPLOYEE_STATE, EmployeeState.DONE)
                        .and(P_REASON, reason)
                        .and(P_START, startDate)
                        .and(P_END, endDate)
                        .and(P_OWNER_EMAIL, ownerEmail)
                        .and(P_STEP_ID, stepId));
    }

    @Transactional
    public int updateStateAssigned(LocalDate startDate, LocalDate endDate, String ownerEmail, Long stepId, String project, EmployeeState newState) {
        return update("UPDATE StepEntry s SET s.employeeState = :employeeState WHERE s.id IN (SELECT s.id FROM StepEntry s WHERE s.date BETWEEN :start AND :end AND s.owner.email = :ownerEmail AND s.step.id = :stepId AND s.project like :project)",
                Parameters
                        .with(P_EMPLOYEE_STATE, newState)
                        .and(P_START, startDate)
                        .and(P_END, endDate)
                        .and(P_OWNER_EMAIL, ownerEmail)
                        .and(P_PROJECT, project)
                        .and(P_STEP_ID, stepId));
    }

    public Optional<StepEntry> findStepEntryForEmployeeAtStepInRange(LocalDate startDate, LocalDate endDate, String ownerEmail, Long stepId, String assigneeEmail) {
        return find("#StepEntry.findStepEntryForEmployeeAtStepInRange",
                Parameters
                        .with(P_START, startDate)
                        .and(P_END, endDate)
                        .and(P_OWNER_EMAIL, ownerEmail)
                        .and(P_STEP_ID, stepId)
                        .and(P_ASSIGNEE_EMAIL, assigneeEmail))
                .singleResultOptional();
    }

    public Optional<StepEntry> findStepEntryForEmployeeAndProjectAtStepInRange(LocalDate startDate, LocalDate endDate, String ownerEmail, Long stepId, String assigneeEmail, String project) {
        return find("#StepEntry.findStepEntryForEmployeeAndProjectAtStepInRange",
                Parameters
                        .with(P_START, startDate)
                        .and(P_END, endDate)
                        .and(P_OWNER_EMAIL, ownerEmail)
                        .and(P_STEP_ID, stepId)
                        .and(P_ASSIGNEE_EMAIL, assigneeEmail)
                        .and(P_PROJECT, project))
                .singleResultOptional();
    }

    public List<StepEntry> findAllStepEntriesForPMInRange(LocalDate startDate, LocalDate endDate, String assigneeEmail) {
        return find("#StepEntry.findAllStepEntriesForPMInRange",
                Parameters
                        .with(P_START, startDate)
                        .and(P_END, endDate)
                        .and(P_ASSIGNEE_EMAIL, assigneeEmail)
                        .and(P_STEP_ID, StepName.CONTROL_TIME_EVIDENCES.getId())
        ).list();
    }

    public List<StepEntry> findAllStepEntriesForAllPMInRange(LocalDate startDate, LocalDate endDate) {
        return find("#StepEntry.findAllStepEntriesForAllPMInRange",
                Parameters
                        .with(P_START, startDate)
                        .and(P_END, endDate)
                        .and(P_STEP_ID, StepName.CONTROL_TIME_EVIDENCES.getId())
        ).list();
    }
}
