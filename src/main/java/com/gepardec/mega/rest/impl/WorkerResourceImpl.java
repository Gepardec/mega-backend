package com.gepardec.mega.rest.impl;

import com.gepardec.mega.application.interceptor.RolesAllowed;
import com.gepardec.mega.domain.model.Role;
import com.gepardec.mega.domain.model.monthlyreport.MonthlyReport;
import com.gepardec.mega.rest.api.WorkerResource;
import com.gepardec.mega.rest.mapper.MapperManager;
import com.gepardec.mega.rest.model.MonthlyReportDto;
import com.gepardec.mega.service.api.MonthlyReportService;
import io.quarkus.security.Authenticated;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.core.Response;

@RequestScoped
@Authenticated
@RolesAllowed(Role.EMPLOYEE)
public class WorkerResourceImpl implements WorkerResource {

    @Inject
    MonthlyReportService monthlyReportService;

    @Inject
    MapperManager mapper;

    @Override
    public Response monthlyReport() {
        MonthlyReport monthlyReport = monthlyReportService.getMonthEndReportForUser();

        return Response.ok(mapper.map(monthlyReport, MonthlyReportDto.class)).build();
    }

    @Override
    public Response monthlyReport(Integer year, Integer month) {
        MonthlyReport monthlyReport = monthlyReportService.getMonthEndReportForUser(year, month, null);

        return Response.ok(mapper.map(monthlyReport, MonthlyReportDto.class)).build();
    }
}
