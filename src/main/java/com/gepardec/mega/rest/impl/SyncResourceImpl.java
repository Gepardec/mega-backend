package com.gepardec.mega.rest.impl;

import com.gepardec.mega.domain.model.Employee;
import com.gepardec.mega.domain.utils.DateUtils;
import com.gepardec.mega.notification.mail.dates.OfficeCalendarUtil;
import com.gepardec.mega.rest.api.EmployeeResource;
import com.gepardec.mega.rest.api.SyncResource;
import com.gepardec.mega.service.api.*;
import com.gepardec.mega.zep.ZepService;
import de.provantis.zep.FehlzeitType;
import io.quarkus.arc.properties.IfBuildProperty;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.core.Response;

import java.io.Console;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@RequestScoped
@IfBuildProperty(name = "mega.endpoint.test.enable", stringValue = "true", enableIfMissing = true)
public class SyncResourceImpl implements SyncResource {

    @Inject
    ProjectSyncService projectSyncService;

    @Inject
    SyncService syncService;

    @Inject
    StepEntrySyncService stepEntrySyncService;

    @Inject
    EnterpriseSyncService enterpriseSyncService;

    @Inject
    PrematureEmployeeCheckSyncService prematureEmployeeCheckSyncService;
    @Inject
    ZepService zepService;

    @Inject
    EmployeeService employeeService;

    @Override
    public Response syncProjects(YearMonth from, YearMonth to) {
        if (from == null) {
            return Response.ok(projectSyncService.generateProjects()).build();
        }

        if (to == null) {
            return Response.ok(projectSyncService.generateProjects(from.atDay(1))).build();
        }

        while (from.isBefore(to)) {
            projectSyncService.generateProjects(from.atDay(1));
            from = from.plusMonths(1);
        }

        return Response.ok(projectSyncService.generateProjects(from.atDay(1))).build();
    }

    @Override
    public Response syncEmployees() {
        syncService.syncEmployees();
        return Response.ok("ok").build();
    }

    @Override
    public Response generateEnterpriseEntries(YearMonth from, YearMonth to) {
        if (from == null) {
            return Response.ok(enterpriseSyncService.generateEnterpriseEntries(LocalDate.now().withDayOfMonth(1))).build();
        }
        if (to == null) {
            return Response.ok(enterpriseSyncService.generateEnterpriseEntries(from.atDay(1))).build();
        }

        while (from.isBefore(to)) {
            enterpriseSyncService.generateEnterpriseEntries(from.atDay(1));
            from = from.plusMonths(1);
        }

        return Response.ok(enterpriseSyncService.generateEnterpriseEntries(from.atDay(1))).build();
    }

    @Override
    public Response generateStepEntries(YearMonth from, YearMonth to) {
        if (from == null) {
            return Response.ok(stepEntrySyncService.generateStepEntriesFromEndpoint()).build();
        }
        if (to == null) {
            return Response.ok(stepEntrySyncService.generateStepEntriesFromEndpoint(from)).build();
        }

        while (from.isBefore(to)) {
            stepEntrySyncService.generateStepEntriesFromEndpoint(from);
            from = from.plusMonths(1);
        }

        return Response.ok(stepEntrySyncService.generateStepEntriesFromEndpoint(from)).build();
    }

    @Override
    public Response syncPrematureEmployeeChecks(YearMonth from, YearMonth to) {
        if (from == null) {
            prematureEmployeeCheckSyncService.syncPrematureEmployeeChecksWithStepEntries(DateUtils.getCurrentYearMonth());
            return Response.ok().build();
        }

        if (to == null) {
            prematureEmployeeCheckSyncService.syncPrematureEmployeeChecksWithStepEntries(from);
            return Response.ok().build();
        }

        do {
            prematureEmployeeCheckSyncService.syncPrematureEmployeeChecksWithStepEntries(from);
            from = from.plusMonths(1);
        } while (from.compareTo(to) <= 0);

        return Response.ok().build();
    }

    @Override
    public Response syncAll(YearMonth from, YearMonth to) {
        syncEmployees();
        syncProjects(from, to);
        generateEnterpriseEntries(from, to);
        generateStepEntries(from, to);

        return Response.ok("ok").build();
    }

    @Override
    public Response getAllEmployeesWithoutTimeBookingsAndAbsentWholeMonth() {
        List<Employee> empls = employeeService.getAllActiveEmployees();
        List<Employee> resultEmpls = new ArrayList<>();
        LocalDate now = LocalDate.now();
        LocalDate firstOfPreviousMonth = now.withMonth(now.getMonth().minus(1).getValue()).withDayOfMonth(1);
        LocalDate lastOfPreviousMonth = DateUtils.getLastDayOfMonth(now.getYear(), firstOfPreviousMonth.getMonth().getValue());

        for (var empl : empls) {
            List<FehlzeitType> absences = zepService.getAbsenceForEmployee(empl, firstOfPreviousMonth);
            boolean allAbsent = true;
            System.out.println(empl.getFirstname());
            firstOfPreviousMonth.datesUntil(lastOfPreviousMonth).forEach(
                    day -> {
                        if (OfficeCalendarUtil.isWorkingDay(day)) {
                            boolean isAbsent = isAbsent(day, empl, absences);
                            if (!isAbsent) {
                                allAbsent = false; //TODO: exchange this -> not possible inside here
                                return;
                            }
                        }
                    });
            if(allAbsent){
                resultEmpls.add(empl);
            }
        }
        return Response.ok("ok").build();
    }

    private static boolean isAbsent(LocalDate day, Employee empl, List<FehlzeitType> absences) {
        boolean match = absences.stream().anyMatch(absence -> {
            LocalDate startDate = LocalDate.parse(absence.getStartdatum());
            LocalDate endDate = LocalDate.parse(absence.getEnddatum());
            return day.equals(startDate) || day.equals(endDate) || (day.isAfter(startDate) && day.isBefore(endDate));
        });
        System.out.println(match);
        return absences.stream().anyMatch(absence -> day.equals(LocalDate.parse(absence.getStartdatum())));
    }
}
