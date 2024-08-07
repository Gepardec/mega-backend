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


    public Optional<StepEntry> findControlTimesStepEntryByOwnerAndEntryDate(LocalDate entryDate, String ownerAndAssigneeEmail) {
        return find("#StepEntry.findAllOwnedAndAssignedStepEntriesForEmployee",
                Parameters
                        .with("entryDate", entryDate)
                        .and("ownerEmail", ownerAndAssigneeEmail)
                        .and("assigneeEmail", ownerAndAssigneeEmail)
                        .and("stepId", StepName.CONTROL_TIMES.getId()))
                .singleResultOptional();
    }

    public Optional<StepEntry> findAllOwnedAndAssignedStepEntriesForEmployeeForControlInternalTimes(LocalDate entryDate, String ownerAndAssigneeEmail) {
        return find("#StepEntry.findAllOwnedAndAssignedStepEntriesForEmployeeForControlInternalTimes",
                Parameters
                        .with("entryDate", entryDate)
                        .and("ownerEmail", ownerAndAssigneeEmail)
                        .and("assigneeEmail", ownerAndAssigneeEmail)
                        .and("stepId", StepName.CONTROL_INTERNAL_TIMES.getId()))
                .singleResultOptional();
    }

    public List<StepEntry> findAllOwnedAndUnassignedStepEntriesExceptControlTimes(LocalDate entryDate, String ownerEmail) {
        List<StepEntry> entries = find("#StepEntry.findAllOwnedAndUnassignedStepEntriesExceptControlTimes",
                Parameters
                        .with("entryDate", entryDate)
                        .and("ownerEmail", ownerEmail))
                .list();

        entries.addAll(find("#StepEntry.findAllOwnedAndAssignedStepEntriesForEmployee",
                Parameters
                        .with("entryDate", entryDate)
                        .and("ownerEmail", ownerEmail)
                        .and("assigneeEmail", ownerEmail)
                        .and("stepId", StepName.CONTROL_TIME_EVIDENCES.getId()))
                .list());

        return entries;
    }

    public List<StepEntry> findAllOwnedAndUnassignedStepEntriesForPMProgress(LocalDate entryDate, String ownerEmail) {
        return find("#StepEntry.findAllOwnedAndUnassignedStepEntriesForPMProgress",
                Parameters
                        .with("entryDate", entryDate)
                        .and("ownerEmail", ownerEmail)
                        .and("stepId", StepName.CONTROL_TIME_EVIDENCES.getId()))
                .list();
    }

    public List<StepEntry> findAllOwnedStepEntriesInRange(LocalDate startDate, LocalDate endDate, String ownerEmail) {
        return find("#StepEntry.findAllOwnedStepEntriesInRange",
                Parameters
                        .with("start", startDate)
                        .and("end", endDate)
                        .and("ownerEmail", ownerEmail)
        ).list();
    }

    public List<StepEntry> findAllOwnedStepEntriesInRange(LocalDate startDate, LocalDate endDate, String ownerEmail, String projectId, String assigneEmail) {
        return find("#StepEntry.findAllOwnedStepEntriesInRangeForProject",
                Parameters
                        .with("start", startDate)
                        .and("end", endDate)
                        .and("ownerEmail", ownerEmail)
                        .and("assigneEmail", assigneEmail)
                        .and("projectId", projectId))
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
                        .with("employeeState", newState)
                        .and("stateReason", stateReason)
                        .and("start", startDate)
                        .and("end", endDate)
                        .and("ownerEmail", ownerEmail)
                        .and("stepId", stepId));
    }

    @Transactional
    public int updateReasonForStepEntryWithStateDone(LocalDate startDate, LocalDate endDate, String ownerEmail, Long stepId, String reason) {
        return update("UPDATE StepEntry s SET s.stateReason = :reason" +
                        " WHERE s.id IN" +
                        " (SELECT s.id FROM StepEntry s WHERE s.date BETWEEN :start AND :end AND " +
                        "         s.owner.email = :ownerEmail AND s.step.id = :stepId AND" +
                        "         s.employeeState = :employeeState)",
                Parameters
                        .with("employeeState", EmployeeState.DONE)
                        .and("reason", reason)
                        .and("start", startDate)
                        .and("end", endDate)
                        .and("ownerEmail", ownerEmail)
                        .and("stepId", stepId));
    }

    @Transactional
    public int updateStateAssigned(LocalDate startDate, LocalDate endDate, String ownerEmail, Long stepId, String project, EmployeeState newState) {
        return update("UPDATE StepEntry s SET s.employeeState = :employeeState WHERE s.id IN (SELECT s.id FROM StepEntry s WHERE s.date BETWEEN :start AND :end AND s.owner.email = :ownerEmail AND s.step.id = :stepId AND s.project like :project)",
                Parameters
                        .with("employeeState", newState)
                        .and("start", startDate)
                        .and("end", endDate)
                        .and("ownerEmail", ownerEmail)
                        .and("project", project)
                        .and("stepId", stepId));
    }

    public Optional<StepEntry> findStepEntryForEmployeeAtStepInRange(LocalDate startDate, LocalDate endDate, String ownerEmail, Long stepId, String assigneeEmail) {
        return find("#StepEntry.findStepEntryForEmployeeAtStepInRange",
                Parameters
                        .with("start", startDate)
                        .and("end", endDate)
                        .and("ownerEmail", ownerEmail)
                        .and("stepId", stepId)
                        .and("assigneeEmail", assigneeEmail))
                .singleResultOptional();
    }

    public Optional<StepEntry> findStepEntryForEmployeeAndProjectAtStepInRange(LocalDate startDate, LocalDate endDate, String ownerEmail, Long stepId, String assigneeEmail, String project) {
        return find("#StepEntry.findStepEntryForEmployeeAndProjectAtStepInRange",
                Parameters
                        .with("start", startDate)
                        .and("end", endDate)
                        .and("ownerEmail", ownerEmail)
                        .and("stepId", stepId)
                        .and("assigneeEmail", assigneeEmail)
                        .and("project", project))
                .singleResultOptional();
    }

    public List<StepEntry> findAllStepEntriesForPMInRange(LocalDate startDate, LocalDate endDate, String assigneEmail) {
        return find("#StepEntry.findAllStepEntriesForPMInRange",
                Parameters
                        .with("start", startDate)
                        .and("end", endDate)
                        .and("assigneEmail", assigneEmail)
                        .and("stepId", StepName.CONTROL_TIME_EVIDENCES.getId())
        ).list();
    }

    public List<StepEntry> findAllStepEntriesForAllPMInRange(LocalDate startDate, LocalDate endDate) {
        return find("#StepEntry.findAllStepEntriesForAllPMInRange",
                Parameters
                        .with("start", startDate)
                        .and("end", endDate)
                        .and("stepId", StepName.CONTROL_TIME_EVIDENCES.getId())
        ).list();
    }
}
