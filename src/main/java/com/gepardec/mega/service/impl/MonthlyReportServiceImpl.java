package com.gepardec.mega.service.impl;

import com.gepardec.mega.domain.model.Attendances;
import com.gepardec.mega.hexagon.shared.application.security.AuthenticatedActorContext;
import com.gepardec.mega.service.api.MonthlyReportService;
import com.gepardec.mega.service.helper.WorkingTimeUtil;
import com.gepardec.mega.zep.ZepService;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;

import java.time.YearMonth;

@RequestScoped
public class MonthlyReportServiceImpl implements MonthlyReportService {

    @Inject
    ZepService zepService;

    @Inject
    AuthenticatedActorContext userContext;

    @Inject
    WorkingTimeUtil workingTimeUtil;

    @Override
    public Attendances getAttendances(YearMonth payrollMonth) {
        var employee = zepService.getEmployee(userContext.user().zepUsername().value());
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
}
