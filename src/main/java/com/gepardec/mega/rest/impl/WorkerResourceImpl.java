package com.gepardec.mega.rest.impl;

import com.gepardec.mega.application.interceptor.RolesAllowed;
import com.gepardec.mega.db.entity.common.AbsenceType;
import com.gepardec.mega.domain.model.AbsenceTime;
import com.gepardec.mega.domain.model.Bill;
import com.gepardec.mega.domain.model.Employee;
import com.gepardec.mega.domain.model.MonthlyAbsences;
import com.gepardec.mega.domain.model.ProjectHoursSummary;
import com.gepardec.mega.domain.model.Role;
import com.gepardec.mega.domain.model.monthlyreport.MonthlyReport;
import com.gepardec.mega.domain.utils.DateUtils;
import com.gepardec.mega.personio.employees.PersonioEmployeesService;
import com.gepardec.mega.rest.api.WorkerResource;
import com.gepardec.mega.rest.mapper.BillMapper;
import com.gepardec.mega.rest.mapper.MonthlyAbsencesMapper;
import com.gepardec.mega.rest.mapper.MonthlyReportMapper;
import com.gepardec.mega.rest.mapper.ProjectHoursSummaryMapper;
import com.gepardec.mega.rest.model.BillDto;
import com.gepardec.mega.rest.model.MonthlyAbsencesDto;
import com.gepardec.mega.rest.model.ProjectHoursSummaryDto;
import com.gepardec.mega.service.api.DateHelperService;
import com.gepardec.mega.service.api.EmployeeService;
import com.gepardec.mega.service.api.MonthlyReportService;
import com.gepardec.mega.service.helper.WorkingTimeUtil;
import com.gepardec.mega.zep.ZepService;
import com.gepardec.mega.zep.impl.Rest;
import io.quarkus.security.Authenticated;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.core.Response;
import org.apache.commons.lang3.tuple.Pair;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

@RequestScoped
@Authenticated
@RolesAllowed(Role.EMPLOYEE)
public class WorkerResourceImpl implements WorkerResource {

    @Inject
    MonthlyReportService monthlyReportService;

    @Inject
    DateHelperService dateHelperService;

    @Inject
    MonthlyReportMapper mapper;

    @Inject
    MonthlyAbsencesMapper monthlyAbsencesMapper;

    @Inject
    BillMapper billMapper;

    @Inject
    ProjectHoursSummaryMapper projectHoursSummaryMapper;

    @Inject @Rest
    ZepService zepService;

    @Inject
    EmployeeService employeeService;

    @Inject
    PersonioEmployeesService personioEmployeesService;

    @Inject
    WorkingTimeUtil workingTimeUtil;

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

    @Override
    public List<ProjectHoursSummaryDto> getAllProjectsForMonthAndEmployee(String employeeId, YearMonth from) {
        Employee employee = employeeService.getEmployee(employeeId);
        List<ProjectHoursSummary> resultProjectsHoursSummaryList = zepService.getAllProjectsForMonthAndEmployee(employee, from);

        return resultProjectsHoursSummaryList.stream()
                .map(projectHoursSummaryMapper::mapToDto)
                .toList();
    }

    @Override
    public MonthlyAbsencesDto getAllAbsencesForMonthAndEmployee(String employeeId, YearMonth from) {
        Employee employee = employeeService.getEmployee(employeeId);
        int availableVacationDays = personioEmployeesService.getAvailableVacationDaysForEmployeeByEmail(employee.getEmail());
        double doctorsVisitingHours = zepService.getDoctorsVisitingTimeForMonthAndEmployee(employee, from);
        Pair<String, String> correctDatePairForRequest = dateHelperService.getCorrectDateForRequest(employee, from);
        LocalDate fromDateForRequest = DateUtils.parseDate(correctDatePairForRequest.getLeft());
        LocalDate toDateForRequest = DateUtils.parseDate(correctDatePairForRequest.getRight());

        List<AbsenceTime> absences = zepService.getAbsenceForEmployee(employee,fromDateForRequest)
                                               .stream()
                                               .filter(absence -> absenceIsInRange(absence, fromDateForRequest, toDateForRequest))
                                               .toList();

        return monthlyAbsencesMapper.mapToDto(createMonthlyAbsences(availableVacationDays, doctorsVisitingHours, absences, fromDateForRequest));
    }

    // this also checks if startDate or endDate is exact match
    private boolean absenceIsInRange(AbsenceTime absence, LocalDate fromDateForRequest, LocalDate toDateForRequest) {
        return ((absence.fromDate().equals(fromDateForRequest) && absence.toDate().equals(toDateForRequest)) ||
                (absence.fromDate().equals(fromDateForRequest) && absence.toDate().isBefore(toDateForRequest)) ||
                (absence.fromDate().isAfter(fromDateForRequest) && absence.toDate().equals(toDateForRequest)) ||
                (absence.fromDate().isAfter(fromDateForRequest) && absence.toDate().isBefore(toDateForRequest)));
    }

    private MonthlyAbsences createMonthlyAbsences(int availableVacationDays, double doctorsVisitingHours, List<AbsenceTime> absences, LocalDate fromDateForRequest){
        return MonthlyAbsences.builder()
                       .vacationDays(workingTimeUtil.getAbsenceTimesForEmployee(absences, AbsenceType.VACATION_DAYS.getAbsenceName(), fromDateForRequest))
                       .compensatoryDays(workingTimeUtil.getAbsenceTimesForEmployee(absences, AbsenceType.COMPENSATORY_DAYS.getAbsenceName(), fromDateForRequest))
                       .nursingDays(workingTimeUtil.getAbsenceTimesForEmployee(absences, AbsenceType.NURSING_DAYS.getAbsenceName(), fromDateForRequest))
                       .maternityLeaveDays(workingTimeUtil.getAbsenceTimesForEmployee(absences, AbsenceType.MATERNITY_LEAVE_DAYS.getAbsenceName(), fromDateForRequest))
                       .fatherMonthDays(workingTimeUtil.getAbsenceTimesForEmployee(absences, AbsenceType.FATHER_MONTH_DAYS.getAbsenceName(), fromDateForRequest))
                       .paidSpecialLeaveDays(workingTimeUtil.getAbsenceTimesForEmployee(absences, AbsenceType.PAID_SPECIAL_LEAVE_DAYS.getAbsenceName(), fromDateForRequest))
                       .nonPaidVacationDays(workingTimeUtil.getAbsenceTimesForEmployee(absences, AbsenceType.NON_PAID_VACATION_DAYS.getAbsenceName(), fromDateForRequest))
                       .paidSickLeave(workingTimeUtil.getAbsenceTimesForEmployee(absences, AbsenceType.PAID_SICK_LEAVE.getAbsenceName(), fromDateForRequest))
                       .doctorsVisitingTime(doctorsVisitingHours)
                       .availableVacationDays(availableVacationDays)
                       .build();
    }

}
