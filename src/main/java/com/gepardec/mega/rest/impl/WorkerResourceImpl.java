package com.gepardec.mega.rest.impl;

import com.gepardec.mega.application.interceptor.MegaRolesAllowed;
import com.gepardec.mega.db.entity.common.AbsenceType;
import com.gepardec.mega.domain.model.AbsenceTime;
import com.gepardec.mega.domain.model.Employee;
import com.gepardec.mega.domain.model.MonthlyAbsences;
import com.gepardec.mega.domain.model.MonthlyOfficeDays;
import com.gepardec.mega.domain.model.PersonioEmployee;
import com.gepardec.mega.domain.model.ProjectHoursSummary;
import com.gepardec.mega.domain.model.Role;
import com.gepardec.mega.domain.model.UserContext;
import com.gepardec.mega.domain.model.monthlyreport.MonthlyReport;
import com.gepardec.mega.domain.model.monthlyreport.ProjectEntry;
import com.gepardec.mega.personio.employees.PersonioEmployeesService;
import com.gepardec.mega.rest.api.WorkerResource;
import com.gepardec.mega.rest.mapper.MonthlyAbsencesMapper;
import com.gepardec.mega.rest.mapper.MonthlyBillInfoMapper;
import com.gepardec.mega.rest.mapper.MonthlyOfficeDaysMapper;
import com.gepardec.mega.rest.mapper.MonthlyReportMapper;
import com.gepardec.mega.rest.mapper.ProjectHoursSummaryMapper;
import com.gepardec.mega.rest.mapper.WorkTimeBookingWarningMapper;
import com.gepardec.mega.rest.model.AttendancesDto;
import com.gepardec.mega.rest.model.LeadersDto;
import com.gepardec.mega.rest.model.MonthlyAbsencesDto;
import com.gepardec.mega.rest.model.MonthlyBillInfoDto;
import com.gepardec.mega.rest.model.MonthlyOfficeDaysDto;
import com.gepardec.mega.rest.model.ProjectHoursSummaryDto;
import com.gepardec.mega.rest.model.WorkTimeBookingWarningDto;
import com.gepardec.mega.rest.provider.PayrollContext;
import com.gepardec.mega.rest.provider.PayrollMonthProvider;
import com.gepardec.mega.service.api.AbsenceService;
import com.gepardec.mega.service.api.DateHelperService;
import com.gepardec.mega.service.api.EmployeeService;
import com.gepardec.mega.service.api.MonthlyReportService;
import com.gepardec.mega.service.api.TimeWarningService;
import com.gepardec.mega.service.helper.WorkingTimeUtil;
import com.gepardec.mega.zep.ZepService;
import com.gepardec.mega.zep.impl.Rest;
import io.quarkus.security.Authenticated;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.core.Response;

import java.time.YearMonth;
import java.util.List;
import java.util.Optional;

import static com.gepardec.mega.rest.provider.PayrollContext.PayrollContextType.EMPLOYEE;

@RequestScoped
@Authenticated
@MegaRolesAllowed(Role.EMPLOYEE)
public class WorkerResourceImpl implements WorkerResource {

    @Inject
    MonthlyReportService monthlyReportService;

    @Inject
    DateHelperService dateHelperService;

    @Inject
    TimeWarningService timeWarningService;

    @Inject
    WorkTimeBookingWarningMapper workTimeBookingWarningMapper;

    @Inject
    AbsenceService absenceService;

    @Inject
    MonthlyReportMapper mapper;

    @Inject
    MonthlyAbsencesMapper monthlyAbsencesMapper;

    @Inject
    MonthlyOfficeDaysMapper monthlyOfficeDaysMapper;

    @Inject
    MonthlyBillInfoMapper monthlyBillInfoMapper;

    @Inject
    ProjectHoursSummaryMapper projectHoursSummaryMapper;

    @Inject
    @Rest
    ZepService zepService;

    @Inject
    EmployeeService employeeService;

    @Inject
    UserContext userContext;

    @Inject
    PersonioEmployeesService personioEmployeesService;

    @Inject
    WorkingTimeUtil workingTimeUtil;

    @Inject
    @PayrollContext(EMPLOYEE)
    PayrollMonthProvider payrollMonthProvider;

    @Override
    public Response monthlyReport() {
        return monthlyReport(payrollMonthProvider.getPayrollMonth());
    }

    @Override
    public Response monthlyReport(YearMonth payrollMonth) {
        MonthlyReport monthlyReport = monthlyReportService.getMonthEndReportForUser(payrollMonth);

        return Response.ok(mapper.mapToDto(monthlyReport)).build();
    }

    @Override
    public MonthlyBillInfoDto getBillInfoForEmployee(YearMonth payrollMonth) {
        Employee employee = employeeService.getEmployee(userContext.getUser().getUserId());
        Optional<PersonioEmployee> personioEmployee = personioEmployeesService.getPersonioEmployeeByEmail(employee.getEmail());
        return personioEmployee.map(value -> monthlyBillInfoMapper.mapToDto(zepService.getMonthlyBillInfoForEmployee(value, employee, payrollMonth))).orElse(null);
    }

    @Override
    public List<ProjectHoursSummaryDto> getAllProjectsForMonthAndEmployee(YearMonth payrollMonth) {
        Employee employee = employeeService.getEmployee(userContext.getUser().getUserId());
        List<ProjectHoursSummary> resultProjectsHoursSummaryList = zepService.getAllProjectsForMonthAndEmployee(employee, payrollMonth);

        return resultProjectsHoursSummaryList.stream()
                .map(projectHoursSummaryMapper::mapToDto)
                .toList();
    }

    @Override
    public MonthlyAbsencesDto getAllAbsencesForMonthAndEmployee(YearMonth payrollMonth) {
        Employee employee = employeeService.getEmployee(userContext.getUser().getUserId());
        int availableVacationDays = personioEmployeesService.getAvailableVacationDaysForEmployeeByEmail(employee.getEmail());
        double doctorsVisitingHours = zepService.getDoctorsVisitingTimeForMonthAndEmployee(employee, payrollMonth);
        List<AbsenceTime> absences = zepService.getAbsenceForEmployee(employee, payrollMonth);

        return monthlyAbsencesMapper.mapToDto(createMonthlyAbsences(availableVacationDays, doctorsVisitingHours, absences, payrollMonth));
    }

    // includes homeoffice and fridays in office as well
    @Override
    public MonthlyOfficeDaysDto getOfficeDaysForMonthAndEmployee(YearMonth payrollMonth) {
        Employee employee = employeeService.getEmployee(userContext.getUser().getUserId());
        List<AbsenceTime> absences = zepService.getAbsenceForEmployee(employee, payrollMonth);

        return monthlyOfficeDaysMapper.mapToDto(createMonthlyOfficeDays(absences, payrollMonth));
    }

    @Override
    public List<WorkTimeBookingWarningDto> getAllWarningsForEmployeeAndMonth(YearMonth payrollMonth) {
        Employee employee = employeeService.getEmployee(userContext.getUser().getUserId());
        List<AbsenceTime> absences = zepService.getAbsenceForEmployee(employee, payrollMonth);
        List<ProjectEntry> projectEntries = zepService.getProjectTimes(employee, payrollMonth);

        return timeWarningService.getAllTimeWarningsForEmployeeAndMonth(absences, projectEntries, employee)
                .stream()
                .map(workTimeBookingWarningMapper::mapToDto)
                .toList();
    }

    @Override
    public LeadersDto getLeaders() {
        return personioEmployeesService.getPersonioEmployeeByEmail(userContext.getUser().getEmail())
                .map(employee -> new LeadersDto(employee.getInternalProjectLead(), employee.getGuildLead()))
                .orElseThrow();
    }

    @Override
    public AttendancesDto getAttendances(YearMonth payrollMonth) {
        return new AttendancesDto(3.49, 2.5, 3.4, 0.0);
    }

    private MonthlyOfficeDays createMonthlyOfficeDays(List<AbsenceTime> absences, YearMonth payrollMonth) {
        int homeOfficeDaysCount = workingTimeUtil.getAbsenceTimesForEmployee(absences, AbsenceType.HOME_OFFICE_DAYS.getAbsenceName(), payrollMonth);
        int numberOfWorkingDaysInMonth = dateHelperService.getNumberOfWorkingDaysForMonthWithoutHolidays(payrollMonth);
        int numberOfFridaysInMonth = dateHelperService.getNumberOfFridaysInMonth(payrollMonth);
        int numberOfDaysAbsent = absenceService.getNumberOfDaysAbsent(absences, payrollMonth);

        return MonthlyOfficeDays.builder()
                .homeOfficeDays(homeOfficeDaysCount)
                .officeDays(numberOfWorkingDaysInMonth - numberOfDaysAbsent)
                .fridaysAtTheOffice(numberOfFridaysInMonth - absenceService.numberOfFridaysAbsent(absences))
                .build();
    }

    private MonthlyAbsences createMonthlyAbsences(int availableVacationDays, double doctorsVisitingHours, List<AbsenceTime> absences, YearMonth payrollMonth) {
        return MonthlyAbsences.builder()
                .vacationDays(workingTimeUtil.getAbsenceTimesForEmployee(absences, AbsenceType.VACATION_DAYS.getAbsenceName(), payrollMonth))
                .compensatoryDays(workingTimeUtil.getAbsenceTimesForEmployee(absences, AbsenceType.COMPENSATORY_DAYS.getAbsenceName(), payrollMonth))
                .nursingDays(workingTimeUtil.getAbsenceTimesForEmployee(absences, AbsenceType.NURSING_DAYS.getAbsenceName(), payrollMonth))
                .maternityLeaveDays(workingTimeUtil.getAbsenceTimesForEmployee(absences, AbsenceType.MATERNITY_LEAVE_DAYS.getAbsenceName(), payrollMonth))
                .externalTrainingDays(workingTimeUtil.getAbsenceTimesForEmployee(absences, AbsenceType.EXTERNAL_TRAINING_DAYS.getAbsenceName(), payrollMonth))
                .conferenceDays(workingTimeUtil.getAbsenceTimesForEmployee(absences, AbsenceType.CONFERENCE_DAYS.getAbsenceName(), payrollMonth))
                .maternityProtectionDays(workingTimeUtil.getAbsenceTimesForEmployee(absences, AbsenceType.MATERNITY_PROTECTION_DAYS.getAbsenceName(), payrollMonth))
                .fatherMonthDays(workingTimeUtil.getAbsenceTimesForEmployee(absences, AbsenceType.FATHER_MONTH_DAYS.getAbsenceName(), payrollMonth))
                .paidSpecialLeaveDays(workingTimeUtil.getAbsenceTimesForEmployee(absences, AbsenceType.PAID_SPECIAL_LEAVE_DAYS.getAbsenceName(), payrollMonth))
                .nonPaidVacationDays(workingTimeUtil.getAbsenceTimesForEmployee(absences, AbsenceType.NON_PAID_VACATION_DAYS.getAbsenceName(), payrollMonth))
                .paidSickLeave(workingTimeUtil.getAbsenceTimesForEmployee(absences, AbsenceType.PAID_SICK_LEAVE.getAbsenceName(), payrollMonth))
                .doctorsVisitingTime(doctorsVisitingHours)
                .availableVacationDays(availableVacationDays)
                .build();
    }
}
