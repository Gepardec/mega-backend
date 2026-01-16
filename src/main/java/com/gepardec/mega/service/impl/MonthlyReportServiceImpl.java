package com.gepardec.mega.service.impl;

import com.gepardec.mega.db.entity.employee.EmployeeState;
import com.gepardec.mega.db.entity.employee.StepEntry;
import com.gepardec.mega.domain.model.Attendances;
import com.gepardec.mega.domain.model.Comment;
import com.gepardec.mega.domain.model.Employee;
import com.gepardec.mega.domain.model.EmployeeCheck;
import com.gepardec.mega.domain.model.PrematureEmployeeCheck;
import com.gepardec.mega.domain.model.StepName;
import com.gepardec.mega.domain.model.UserContext;
import com.gepardec.mega.service.api.CommentService;
import com.gepardec.mega.service.api.EmployeeService;
import com.gepardec.mega.service.api.MonthlyReportService;
import com.gepardec.mega.service.api.PrematureEmployeeCheckService;
import com.gepardec.mega.service.api.StepEntryService;
import com.gepardec.mega.service.helper.WorkingTimeUtil;
import com.gepardec.mega.zep.ZepService;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import org.apache.commons.lang3.tuple.Pair;

import java.time.YearMonth;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@RequestScoped
public class MonthlyReportServiceImpl implements MonthlyReportService {

    @Inject
    ZepService zepService;

    @Inject
    CommentService commentService;

    @Inject
    StepEntryService stepEntryService;

    @Inject
    UserContext userContext;

    @Inject
    EmployeeService employeeService;

    @Inject
    WorkingTimeUtil workingTimeUtil;

    @Inject
    PrematureEmployeeCheckService prematureEmployeeCheckService;

    @Override
    public EmployeeCheck getEmployeeCheck(YearMonth payrollMonth) {
        var employee = employeeService.getEmployee(userContext.getUser().getUserId());
        var employeeCheckState = stepEntryService.findEmployeeCheckState(employee, payrollMonth);
        var internalCheckState = stepEntryService.findEmployeeInternalCheckState(employee, payrollMonth);

        return buildEmployeeCheck(employee, employeeCheckState, internalCheckState, payrollMonth);
    }

    @Override
    public Attendances getAttendances(YearMonth payrollMonth) {
        var employee = employeeService.getEmployee(userContext.getUser().getUserId());
        var projectTimes = zepService.getProjectTimes(employee, payrollMonth);
        var billableTimes = zepService.getBillableForEmployee(employee, payrollMonth);
        var absenceTimes = zepService.getAbsenceForEmployee(employee, payrollMonth);

        var totalWorkingTimeHours = workingTimeUtil.getTotalWorkingTimeForEmployee(projectTimes);
        var overtimeHours = workingTimeUtil.getOvertimeForEmployee(employee, projectTimes, absenceTimes, payrollMonth);
        var billableTimeHours = workingTimeUtil.getBillableTimesForEmployee(billableTimes, employee);
        var billablePercentage = workingTimeUtil.getBillablePercentage(
                workingTimeUtil.getDurationFromTimeString(totalWorkingTimeHours),
                workingTimeUtil.getDurationFromTimeString(billableTimeHours)
        );

        return new Attendances(
                (double) workingTimeUtil.getDurationFromTimeString(totalWorkingTimeHours).toMinutes() / 60,
                overtimeHours,
                (double) workingTimeUtil.getDurationFromTimeString(billableTimeHours).toMinutes() / 60,
                billablePercentage
        );
    }

    private EmployeeCheck buildEmployeeCheck(Employee employee, Optional<Pair<EmployeeState, String>> employeeCheckState, Optional<EmployeeState> internalCheckState, YearMonth payrollMonth) {
        List<Comment> comments = commentService.findCommentsForEmployee(employee.getEmail(), payrollMonth);

        var prematureEmployeeCheck = prematureEmployeeCheckService.findByEmailAndMonth(employee.getEmail(), payrollMonth);
        String stepEntryForAutomaticReleaseReason = null;
        try {
            var stepEntry = stepEntryService.findStepEntryForEmployeeAtStep(1L, employee.getEmail(), employee.getEmail(), payrollMonth);
            if (stepEntry != null) {
                stepEntryForAutomaticReleaseReason = stepEntry.getStateReason();
            }
        } catch (IllegalStateException exception) {
            // is already null here
        }

        var employeeCheckState2 = employeeCheckState.map(Pair::getLeft).orElse(EmployeeState.PREMATURE_CHECK);
        if (employeeCheckState2 == EmployeeState.PREMATURE_CHECK && payrollMonth.isBefore(YearMonth.now())) {
            employeeCheckState2 = null;
        }

        return new EmployeeCheck(
                employee,
                employeeCheckState2,
                employeeCheckState.map(Pair::getRight).orElse(prematureEmployeeCheck.map(PrematureEmployeeCheck::getReason).orElse(stepEntryForAutomaticReleaseReason)),
                internalCheckState.orElse(null),
                isMonthCompletedForEmployee(employee, payrollMonth),
                comments,
                prematureEmployeeCheck.orElse(null)
        );
    }

    private boolean isMonthCompletedForEmployee(Employee employee, YearMonth payrollMonth) {
        List<StepEntry> allOwnedAndUnassignedStepEntriesForOtherChecks = stepEntryService.findAllOwnedAndUnassignedStepEntriesExceptControlTimes(employee, payrollMonth);

        Map<String, List<StepEntry>> controlTimeEvidencesStepsByProjects = allOwnedAndUnassignedStepEntriesForOtherChecks.stream()
                .filter(se -> StepName.CONTROL_TIME_EVIDENCES.name().equals(se.getStep().getName()))
                .collect(Collectors.groupingBy(StepEntry::getProject));

        // mind. 1 Projektleiter muss Employee auf Done gesetzt haben, bei Step 4 (Control Time Evidences)
        boolean controlTimeEvidencesDone = controlTimeEvidencesStepsByProjects.entrySet().stream()
                .allMatch(this::isAnyStepEntryDone);

        return controlTimeEvidencesDone &&
                allOwnedAndUnassignedStepEntriesForOtherChecks.stream()
                        .filter(se -> !StepName.CONTROL_TIME_EVIDENCES.name().equals(se.getStep().getName()))
                        .allMatch(this::isStepEntryDone);
    }

    private boolean isAnyStepEntryDone(Map.Entry<String, List<StepEntry>> entry) {
        return entry.getValue().stream().anyMatch(this::isStepEntryDone);
    }

    private boolean isStepEntryDone(StepEntry stepEntry) {
        return stepEntry.getState() == EmployeeState.DONE;
    }
}
