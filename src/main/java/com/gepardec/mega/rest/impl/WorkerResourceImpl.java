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
import com.gepardec.mega.domain.utils.DateUtils;
import com.gepardec.mega.personio.employees.PersonioEmployeesService;
import com.gepardec.mega.rest.api.WorkerResource;
import com.gepardec.mega.rest.mapper.MonthlyAbsencesMapper;
import com.gepardec.mega.rest.mapper.MonthlyBillInfoMapper;
import com.gepardec.mega.rest.mapper.MonthlyOfficeDaysMapper;
import com.gepardec.mega.rest.mapper.MonthlyReportMapper;
import com.gepardec.mega.rest.mapper.MonthlyWarningMapper;
import com.gepardec.mega.rest.mapper.ProjectHoursSummaryMapper;
import com.gepardec.mega.rest.model.MonthlyAbsencesDto;
import com.gepardec.mega.rest.model.MonthlyBillInfoDto;
import com.gepardec.mega.rest.model.MonthlyOfficeDaysDto;
import com.gepardec.mega.rest.model.MonthlyWarningDto;
import com.gepardec.mega.rest.model.ProjectHoursSummaryDto;
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
import org.apache.commons.lang3.tuple.Pair;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.Optional;

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
    MonthlyWarningMapper monthlyWarningMapper;

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

    @Inject @Rest
    ZepService zepService;

    @Inject
    EmployeeService employeeService;

    @Inject
    UserContext userContext;

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
    public MonthlyBillInfoDto getBillInfoForEmployee(YearMonth from) {
        Employee employee = employeeService.getEmployee(userContext.getUser().getUserId());
        Optional<PersonioEmployee> personioEmployee = personioEmployeesService.getPersonioEmployeeByEmail(employee.getEmail());
        return personioEmployee.map(value -> monthlyBillInfoMapper.mapToDto(zepService.getMonthlyBillInfoForEmployee(value, employee, from))).orElse(null);
    }

    @Override
    public List<ProjectHoursSummaryDto> getAllProjectsForMonthAndEmployee(YearMonth from) {
        Employee employee = employeeService.getEmployee(userContext.getUser().getUserId());
        List<ProjectHoursSummary> resultProjectsHoursSummaryList = zepService.getAllProjectsForMonthAndEmployee(employee, from);

        return resultProjectsHoursSummaryList.stream()
                .map(projectHoursSummaryMapper::mapToDto)
                .toList();
    }

    @Override
    public MonthlyAbsencesDto getAllAbsencesForMonthAndEmployee(YearMonth from) {
        Employee employee = employeeService.getEmployee(userContext.getUser().getUserId());
        int availableVacationDays = personioEmployeesService.getAvailableVacationDaysForEmployeeByEmail(employee.getEmail());
        double doctorsVisitingHours = zepService.getDoctorsVisitingTimeForMonthAndEmployee(employee, from);
        Pair<String, String> correctDatePairForRequest = dateHelperService.getCorrectDateForRequest(employee, from);
        LocalDate fromDateForRequest = DateUtils.parseDate(correctDatePairForRequest.getLeft());
        List<AbsenceTime> absences = zepService.getAbsenceForEmployee(employee,fromDateForRequest);

        return monthlyAbsencesMapper.mapToDto(createMonthlyAbsences(availableVacationDays, doctorsVisitingHours, absences, fromDateForRequest));
    }

    // includes homeoffice and fridays in office as well
    @Override
    public MonthlyOfficeDaysDto getOfficeDaysForMonthAndEmployee(YearMonth from) {
        Employee employee = employeeService.getEmployee(userContext.getUser().getUserId());
        Pair<String, String> correctDatePairForRequest = dateHelperService.getCorrectDateForRequest(employee, from);
        LocalDate fromDateForRequest = DateUtils.parseDate(correctDatePairForRequest.getLeft());
        List<AbsenceTime> absences = zepService.getAbsenceForEmployee(employee,fromDateForRequest);

        return monthlyOfficeDaysMapper.mapToDto(createMonthlyOfficeDays(absences, fromDateForRequest));
    }

    @Override
    public List<MonthlyWarningDto> getAllWarningsForEmployeeAndMonth(YearMonth from) {
        Employee employee = employeeService.getEmployee(userContext.getUser().getUserId());
        Pair<String, String> correctDatePairForRequest = dateHelperService.getCorrectDateForRequest(employee, from);
        LocalDate fromDateForRequest = DateUtils.parseDate(correctDatePairForRequest.getLeft());
        List<AbsenceTime> absences = zepService.getAbsenceForEmployee(employee,fromDateForRequest);
        List<ProjectEntry> projectEntries = zepService.getProjectTimes(employee, fromDateForRequest);

        return timeWarningService.getAllTimeWarningsForEmployeeAndMonth(absences, projectEntries, employee)
                .stream()
                .map(monthlyWarningMapper::mapToDto)
                .toList();
    }


    private MonthlyOfficeDays createMonthlyOfficeDays(List<AbsenceTime> absences, LocalDate fromDateForRequest){
        int homeOfficeDaysCount = workingTimeUtil.getAbsenceTimesForEmployee(absences, AbsenceType.HOME_OFFICE_DAYS.getAbsenceName(), fromDateForRequest);
        int numberOfWorkingDaysInMonth = dateHelperService.getNumberOfWorkingDaysForMonthWithoutHolidays(fromDateForRequest);
        int numberOfFridaysInMonth = dateHelperService.getNumberOfFridaysInMonth(fromDateForRequest);
        int numberOfDaysAbsent = absenceService.getNumberOfDaysAbsent(absences, fromDateForRequest);

        return MonthlyOfficeDays.builder()
                .homeOfficeDays(homeOfficeDaysCount)
                .officeDays(numberOfWorkingDaysInMonth - numberOfDaysAbsent)
                .fridaysAtTheOffice(numberOfFridaysInMonth - absenceService.numberOfFridaysAbsent(absences))
                .build();
    }

    private MonthlyAbsences createMonthlyAbsences(int availableVacationDays, double doctorsVisitingHours, List<AbsenceTime> absences, LocalDate fromDateForRequest){
        return MonthlyAbsences.builder()
                       .vacationDays(workingTimeUtil.getAbsenceTimesForEmployee(absences, AbsenceType.VACATION_DAYS.getAbsenceName(), fromDateForRequest))
                       .compensatoryDays(workingTimeUtil.getAbsenceTimesForEmployee(absences, AbsenceType.COMPENSATORY_DAYS.getAbsenceName(), fromDateForRequest))
                       .nursingDays(workingTimeUtil.getAbsenceTimesForEmployee(absences, AbsenceType.NURSING_DAYS.getAbsenceName(), fromDateForRequest))
                       .maternityLeaveDays(workingTimeUtil.getAbsenceTimesForEmployee(absences, AbsenceType.MATERNITY_LEAVE_DAYS.getAbsenceName(), fromDateForRequest))
                       .externalTrainingDays(workingTimeUtil.getAbsenceTimesForEmployee(absences, AbsenceType.EXTERNAL_TRAINING_DAYS.getAbsenceName(), fromDateForRequest))
                       .conferenceDays(workingTimeUtil.getAbsenceTimesForEmployee(absences, AbsenceType.CONFERENCE_DAYS.getAbsenceName(), fromDateForRequest))
                       .maternityProtectionDays(workingTimeUtil.getAbsenceTimesForEmployee(absences, AbsenceType.MATERNITY_PROTECTION_DAYS.getAbsenceName(), fromDateForRequest))
                       .fatherMonthDays(workingTimeUtil.getAbsenceTimesForEmployee(absences, AbsenceType.FATHER_MONTH_DAYS.getAbsenceName(), fromDateForRequest))
                       .paidSpecialLeaveDays(workingTimeUtil.getAbsenceTimesForEmployee(absences, AbsenceType.PAID_SPECIAL_LEAVE_DAYS.getAbsenceName(), fromDateForRequest))
                       .nonPaidVacationDays(workingTimeUtil.getAbsenceTimesForEmployee(absences, AbsenceType.NON_PAID_VACATION_DAYS.getAbsenceName(), fromDateForRequest))
                       .paidSickLeave(workingTimeUtil.getAbsenceTimesForEmployee(absences, AbsenceType.PAID_SICK_LEAVE.getAbsenceName(), fromDateForRequest))
                       .doctorsVisitingTime(doctorsVisitingHours)
                       .availableVacationDays(availableVacationDays)
                       .build();
    }

}
