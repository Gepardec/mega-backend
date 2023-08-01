package com.gepardec.mega.rest.impl;

import com.gepardec.mega.application.interceptor.RolesAllowed;
import com.gepardec.mega.db.entity.employee.EmployeeState;
import com.gepardec.mega.domain.model.Employee;
import com.gepardec.mega.domain.model.Role;
import com.gepardec.mega.domain.model.UserContext;
import com.gepardec.mega.domain.model.monthlyreport.MonthlyReport;
import com.gepardec.mega.rest.api.WorkerResource;
import com.gepardec.mega.rest.mapper.MapperManager;
import com.gepardec.mega.rest.model.MonthlyReportDto;
import com.gepardec.mega.service.api.EmployeeService;
import com.gepardec.mega.service.api.MonthlyReportService;
import io.quarkus.security.Authenticated;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.core.Response;

import java.time.LocalDate;
import java.util.Collections;
import java.util.Objects;

@RequestScoped
@Authenticated
@RolesAllowed(Role.EMPLOYEE)
public class WorkerResourceImpl implements WorkerResource {

    @Inject
    MonthlyReportService monthlyReportService;

    @Inject
    UserContext userContext;

    @Inject
    EmployeeService employeeService;

    @Inject
    MapperManager mapper;

    @Override
    public Response monthlyReport() {
        LocalDate midOfMonth = LocalDate.now().withDayOfMonth(15);
        LocalDate now = LocalDate.now();
        Integer currentYear = now.getYear();
        Integer currentMonth = now.getMonthValue();
        Integer month;

        if (now.isAfter(midOfMonth)) {
            month = currentMonth;
        } else {
            month = currentMonth - 1;
        }

        return monthlyReport(currentYear, month);
    }

    @Override
    public Response monthlyReport(Integer year, Integer month) {
        LocalDate date = LocalDate.of(year, month, 1);

        Employee employee = employeeService.getEmployee(Objects.requireNonNull(userContext.getUser()).getUserId());

        MonthlyReport monthlyReport = monthlyReportService.getMonthEndReportForUser(employee, date);

        if (monthlyReport == null) {
            monthlyReport = MonthlyReport.builder()
                    .employee(employee)
                    .timeWarnings(Collections.emptyList())
                    .journeyWarnings(Collections.emptyList())
                    .comments(Collections.emptyList())
                    .employeeCheckState(EmployeeState.OPEN)
                    .internalCheckState(EmployeeState.OPEN)
                    .isAssigned(false)
                    .employeeProgresses(Collections.emptyList())
                    .otherChecksDone(false)
                    .billableTime("00:00")
                    .totalWorkingTime("00:00")
                    .compensatoryDays(0)
                    .homeofficeDays(0)
                    .vacationDays(0)
                    .nursingDays(0)
                    .maternityLeaveDays(0)
                    .externalTrainingDays(0)
                    .conferenceDays(0)
                    .maternityProtectionDays(0)
                    .fatherMonthDays(0)
                    .paidSpecialLeaveDays(0)
                    .nonPaidVacationDays(0)
                    .paidSickLeave(0)
                    .build();
        }

        return Response.ok(mapper.map(monthlyReport, MonthlyReportDto.class)).build();
    }
}
