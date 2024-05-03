package com.gepardec.mega.rest.impl;

import com.gepardec.mega.application.interceptor.RolesAllowed;
import com.gepardec.mega.domain.model.Bill;
import com.gepardec.mega.domain.model.Employee;
import com.gepardec.mega.domain.model.Role;
import com.gepardec.mega.domain.model.monthlyreport.MonthlyReport;
import com.gepardec.mega.rest.api.WorkerResource;
import com.gepardec.mega.rest.mapper.BillMapper;
import com.gepardec.mega.rest.mapper.MonthlyReportMapper;
import com.gepardec.mega.rest.model.BillDto;
import com.gepardec.mega.service.api.EmployeeService;
import com.gepardec.mega.service.api.MonthlyReportService;
import com.gepardec.mega.zep.ZepService;
import io.quarkus.security.Authenticated;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.core.Response;

import java.time.YearMonth;
import java.util.List;

@RequestScoped
@Authenticated
@RolesAllowed(Role.EMPLOYEE)
public class WorkerResourceImpl implements WorkerResource {

    @Inject
    MonthlyReportService monthlyReportService;

    @Inject
    MonthlyReportMapper mapper;

    @Inject
    BillMapper billMapper;

    @Inject
    ZepService zepService;

    @Inject
    EmployeeService employeeService;

    @Override
    public Response monthlyReport() {
        MonthlyReport monthlyReport = monthlyReportService.getMonthEndReportForUser();

        return Response.ok(mapper.mapToDto(monthlyReport)).build();
    }

    @Override
    public Response monthlyReport(Integer year, Integer month) {
        //FIXME: What happened with initialDate?
        MonthlyReport monthlyReport = monthlyReportService.getMonthEndReportForUser(year, month, null, null);

        return Response.ok(mapper.mapToDto(monthlyReport)).build();
    }

    @Override
    public List<BillDto> getBillsForEmployeeByMonth(String employeeId, YearMonth from) {
        Employee employee = employeeService.getEmployee(employeeId);
        List<Bill> resultBillList = zepService.getBillsForEmployeeByMonth(employee, from);

        return resultBillList.stream()
                .map(billMapper::mapToDto)
                .toList();
    }
}
