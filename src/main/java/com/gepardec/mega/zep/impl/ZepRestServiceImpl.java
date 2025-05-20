package com.gepardec.mega.zep.impl;

import com.gepardec.mega.db.entity.common.PaymentMethodType;
import com.gepardec.mega.db.entity.common.ProjectTaskType;
import com.gepardec.mega.domain.model.AbsenceTime;
import com.gepardec.mega.domain.model.Employee;
import com.gepardec.mega.domain.model.MonthlyBillInfo;
import com.gepardec.mega.domain.model.PersonioEmployee;
import com.gepardec.mega.domain.model.Project;
import com.gepardec.mega.domain.model.ProjectHoursSummary;
import com.gepardec.mega.domain.model.ProjectTime;
import com.gepardec.mega.domain.model.monthlyreport.ProjectEntry;
import com.gepardec.mega.service.api.DateHelperService;
import com.gepardec.mega.zep.ZepService;
import com.gepardec.mega.zep.rest.dto.ZepAbsence;
import com.gepardec.mega.zep.rest.dto.ZepAttendance;
import com.gepardec.mega.zep.rest.dto.ZepCategory;
import com.gepardec.mega.zep.rest.dto.ZepEmployee;
import com.gepardec.mega.zep.rest.dto.ZepEmploymentPeriod;
import com.gepardec.mega.zep.rest.dto.ZepProject;
import com.gepardec.mega.zep.rest.dto.ZepProjectDetail;
import com.gepardec.mega.zep.rest.dto.ZepProjectEmployee;
import com.gepardec.mega.zep.rest.dto.ZepReceipt;
import com.gepardec.mega.zep.rest.dto.ZepReceiptAttachment;
import com.gepardec.mega.zep.rest.dto.ZepRegularWorkingTimes;
import com.gepardec.mega.zep.rest.mapper.AbsenceMapper;
import com.gepardec.mega.zep.rest.mapper.ActiveMapper;
import com.gepardec.mega.zep.rest.mapper.EmployeeMapper;
import com.gepardec.mega.zep.rest.mapper.FirstDayCurrentPeriodMapper;
import com.gepardec.mega.zep.rest.mapper.ProjectEmployeesMapper;
import com.gepardec.mega.zep.rest.mapper.ProjectEntryMapper;
import com.gepardec.mega.zep.rest.mapper.ProjectMapper;
import com.gepardec.mega.zep.rest.mapper.ProjectTimeMapper;
import com.gepardec.mega.zep.rest.mapper.RegularWorkingHoursMapMapper;
import com.gepardec.mega.zep.rest.service.AbsenceService;
import com.gepardec.mega.zep.rest.service.AttendanceService;
import com.gepardec.mega.zep.rest.service.EmployeeService;
import com.gepardec.mega.zep.rest.service.EmploymentPeriodService;
import com.gepardec.mega.zep.rest.service.ProjectService;
import com.gepardec.mega.zep.rest.service.ReceiptService;
import com.gepardec.mega.zep.rest.service.RegularWorkingTimesService;
import io.quarkus.cache.CacheResult;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.core.MultivaluedMap;
import org.apache.commons.lang3.NotImplementedException;
import org.slf4j.Logger;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.IntStream;


@ApplicationScoped
@Rest
public class ZepRestServiceImpl implements ZepService {

    @Inject
    EmployeeService zepEmployeeService;

    @Inject
    ProjectService projectService;

    @Inject
    AttendanceService attendanceService;

    @Inject
    ReceiptService receiptService;

    @Inject
    AbsenceService absenceService;

    @Inject
    AbsenceMapper absenceMapper;

    @Inject
    RegularWorkingTimesService regularWorkingTimesService;

    @Inject
    EmploymentPeriodService employmentPeriodService;

    @Inject
    DateHelperService dateHelperService;

    @Inject
    EmployeeMapper employeeMapper;

    @Inject
    ProjectEntryMapper projectEntryMapper;

    @Inject
    ProjectTimeMapper projectTimeMapper;

    @Inject
    ProjectMapper projectMapper;

    @Inject
    ProjectEmployeesMapper projectEmployeesMapper;

    @Inject
    RegularWorkingHoursMapMapper regularWorkingTimesMapper;

    @Inject
    ActiveMapper activeEmployeeMapper;

    @Inject
    FirstDayCurrentPeriodMapper firstDayCurrentPeriodMapper;

    @Inject
    Logger logger;

    @Override
    public Employee getEmployee(String userId) {
        logger.debug("Retrieving employee %s from ZEP".formatted(userId));

        Optional<ZepEmployee> zepEmployee = zepEmployeeService.getZepEmployeeByUsername(userId);
        if (zepEmployee.isEmpty()) {
            logger.warn("No employee found for user {}", userId);
            return null;
        }

        List<ZepEmploymentPeriod> periods = employmentPeriodService.getZepEmploymentPeriodsByEmployeeName(userId);
        boolean active = activeEmployeeMapper.map(periods);


        Optional<ZepRegularWorkingTimes> zepRegularWorkingTimesOpt =
                regularWorkingTimesService.getRegularWorkingTimesByUsername(userId);


        Employee employee = employeeMapper.map(zepEmployee.get());
        employee.setActive(active);
        employee.setFirstDayCurrentEmploymentPeriod(firstDayCurrentPeriodMapper.map(periods));
        zepRegularWorkingTimesOpt.ifPresent(rwt -> {
            employee.setRegularWorkingHours(regularWorkingTimesMapper.map(rwt));
        });
        return employee;
    }

    @Override
    public List<Employee> getEmployees() {
        logger.debug("Retrieving employees from ZEP");

        List<ZepEmployee> zepEmployees = zepEmployeeService.getZepEmployees();
        List<Employee> employees = employeeMapper.mapList(zepEmployees);
        employees.forEach(
                employee -> {
                    var periods = employmentPeriodService.getZepEmploymentPeriodsByEmployeeName(employee.getUserId());
                    boolean active = activeEmployeeMapper.map(periods);
                    employee.setActive(active);
                    employee.setFirstDayCurrentEmploymentPeriod(firstDayCurrentPeriodMapper.map(periods));
                });

        return employees;
    }

    @Override
    public void updateEmployeesReleaseDate(String userId, String releaseDate) {
        throw new NotImplementedException("This method is not provided in REST, use SOAP instead.");         // Currently not supported by REST - use SOAP instead
    }

    @Override
    public List<ProjectEntry> getProjectTimes(Employee employee, YearMonth payrollMonth) {
        logger.debug("Retrieving project times from ZEP of %s".formatted(employee.getUserId()));
        return projectEntryMapper.mapList(
                attendanceService.getAttendanceForUserAndMonth(
                        employee.getUserId(),
                        payrollMonth
                )
        );
    }

    //TODO: This method name is misleading as it is not returning the project times for a specific employee but for all employees of a project
    //The misleading name is the result of the original method name in the ZepService interface being not well named
    @CacheResult(cacheName = "projectTimesForEmployeePerProject")
    @Override
    public List<ProjectTime> getProjectTimesForEmployeePerProject(String project, YearMonth payrollMonth) {
        List<ZepAttendance> allZepAttendancesForProject = new ArrayList<>();

        logger.debug("Retrieving project %s from ZEP".formatted(project));

        Optional<ZepProject> projectOpt = projectService.getProjectByName(project, payrollMonth);
        if (projectOpt.isEmpty()) {
            logger.warn("No project found for name {}", project);
            return List.of();
        }

        Integer projectId = projectOpt.get().id();

        logger.debug("Retrieving project employees of %d from ZEP".formatted(projectId));
        List<ZepProjectEmployee> projectEmployees = projectService.getProjectEmployeesForId(projectId);

        projectEmployees.forEach(projectEmployee -> {
            logger.debug("Retrieving attendance of user %s of project %d from ZEP".formatted(projectEmployee.username(), projectId));
            allZepAttendancesForProject.addAll(attendanceService.getAttendanceForUserProjectAndMonth(projectEmployee.username(), payrollMonth, projectId));
        });
        return projectTimeMapper.mapList(allZepAttendancesForProject);
    }

    @Override
    public List<Project> getProjectsForMonthYear(YearMonth payrollMonth) {
        logger.debug("Retrieving projects for payroll month %s from ZEP".formatted(payrollMonth.toString()));
        List<ZepProject> zepProjects = projectService.getProjectsForMonthYear(payrollMonth);
        List<Project.Builder> projects = projectMapper.mapList(zepProjects);
        IntStream.range(0, projects.size())
                .forEach(i -> addProjectEmployeesToBuilder(projects.get(i), zepProjects.get(i)));
        return projects.stream()
                .map(Project.Builder::build)
                .toList();
    }

    @Override
    public Optional<Project> getProjectByName(String projectName, YearMonth payrollMonth) {
        logger.debug("Retrieving project %s from ZEP".formatted(projectName));
        Optional<ZepProject> zepProject = projectService.getProjectByName(projectName, payrollMonth);
        return zepProject.map(project -> projectMapper.map(project).build());
    }

    @Override
    public List<AbsenceTime> getAbsenceForEmployee(Employee employee, YearMonth payrollMonth) {
        logger.debug("Retrieving absences of %s from ZEP".formatted(employee.getUserId()));
        List<ZepAbsence> zepAbsences = absenceService.getZepAbsencesByEmployeeNameForDateRange(
                employee.getUserId(),
                payrollMonth
        );

        return absenceMapper.mapList(zepAbsences);
    }

    @Override
    public List<ProjectTime> getBillableForEmployee(Employee employee, YearMonth payrollMonth) {
        logger.debug("Retrieving billable entries of employee %s from ZEP".formatted(employee.getUserId()));
        List<ZepAttendance> projectTimes = attendanceService.getBillableAttendancesForUserAndMonth(employee.getUserId(), payrollMonth);
        return projectTimeMapper.mapList(projectTimes);
    }

    @Override
    public MonthlyBillInfo getMonthlyBillInfoForEmployee(PersonioEmployee personioEmployee, Employee employee, YearMonth payrollMonth) {
        return getMonthlyBillInfoInternal(personioEmployee, employee, payrollMonth);
    }

    @Override
    public List<ProjectHoursSummary> getAllProjectsForMonthAndEmployee(Employee employee, YearMonth payrollMonth) {
        Optional<ZepEmployee> employeeRetrieved = zepEmployeeService.getZepEmployeeByUsername(employee.getUserId());
        List<ProjectHoursSummary> resultProjectHoursSummary = new ArrayList<>();

        if (employeeRetrieved.isPresent()) {
            resultProjectHoursSummary = getProjectsForMonthAndEmployeeInternal(employeeRetrieved.get(), payrollMonth);
        }
        return resultProjectHoursSummary;
    }

    @Override
    public double getDoctorsVisitingTimeForMonthAndEmployee(Employee employee, YearMonth payrollMonth) {
        return attendanceService.getAttendanceForUserProjectAndMonth(
                        employee.getUserId(),
                        payrollMonth,
                        ProjectTaskType.PROJECT_INTERNAL.getId()
                )
                .stream()
                .filter(attendance -> attendance.projectTaskId().equals(ProjectTaskType.TASK_DOCTOR_VISIT.getId()))
                .mapToDouble(ZepAttendance::duration)
                .sum();
    }

    private List<ProjectHoursSummary> getProjectsForMonthAndEmployeeInternal(ZepEmployee employee, YearMonth payrollMonth) {
        Employee employeeForRequest = employeeMapper.map(employee);
        List<ProjectHoursSummary> resultProjectHoursSummary = new ArrayList<>();
        List<ZepProject> projectsRetrieved = projectService.getProjectsForMonthYear(payrollMonth);

        projectsRetrieved.forEach(
                project -> {
                    Optional<ZepProjectEmployee> projectEmployee = projectService.getProjectEmployeesForId(project.id())
                            .stream()
                            .filter(e -> e.username().equals(employee.username()))
                            .findFirst();
                    if (projectEmployee.isEmpty()) {
                        return;
                    }
                    List<ZepAttendance> attendancesForEmployeeAndProject = attendanceService.getAttendanceForUserProjectAndMonth(projectEmployee.get().username(), payrollMonth, project.id());
                    if (!attendancesForEmployeeAndProject.isEmpty()) {
                        Optional<ProjectHoursSummary> optionalProjectHoursSummary = createProjectsHoursSummary(attendancesForEmployeeAndProject, project);
                        optionalProjectHoursSummary.ifPresent(resultProjectHoursSummary::add);
                    }
                });
        return resultProjectHoursSummary;
    }

    private Optional<ProjectHoursSummary> createProjectsHoursSummary(List<ZepAttendance> attendances, ZepProject project) {
        Optional<ZepProjectDetail> projectRetrieved = projectService.getProjectById(attendances.get(0).projectId());
        String projectName = "";
        double billableHoursSum = 0.0;
        double nonBillableHoursSum = 0.0;
        double chargeability = 0.0;

        if (projectRetrieved.isEmpty()) {
            return Optional.empty();
        }

        projectName = projectRetrieved.get().getProject().name();

        billableHoursSum += attendances.stream()
                .filter(ZepAttendance::billable)
                .mapToDouble(ZepAttendance::duration)
                .sum();

        nonBillableHoursSum += attendances.stream()
                .filter(a -> !a.billable())
                .mapToDouble(ZepAttendance::duration)
                .sum();

        double totalHours = Double.sum(billableHoursSum, nonBillableHoursSum);

        if (!(Double.compare(totalHours, 0.0d) == 0)) {
            chargeability = billableHoursSum / totalHours;
            chargeability = BigDecimal.valueOf(chargeability)
                    .setScale(2, RoundingMode.HALF_UP)
                    .doubleValue();
        }


        return Optional.of(
                ProjectHoursSummary.builder()
                        .projectName(projectName)
                        .billableHoursSum(billableHoursSum)
                        .nonBillableHoursSum(nonBillableHoursSum)
                        .chargeability(chargeability * 100)
                        .isInternalProject(project.customerId() == null)
                        .build()
        );
    }


    private MonthlyBillInfo getMonthlyBillInfoInternal(PersonioEmployee personioEmployee, Employee employee, YearMonth payrollMonth) {
        List<ZepReceipt> allReceiptsForYearMonth = receiptService.getAllReceiptsInRange(payrollMonth);
        List<ZepReceipt> allReceiptsForYearMonthAndEmployee;

        if (!allReceiptsForYearMonth.isEmpty()) {
            allReceiptsForYearMonthAndEmployee = allReceiptsForYearMonth.stream()
                    .filter(receipt -> receipt.employeeId().equals(employee.getUserId()))
                    .toList();

            int sumBills = allReceiptsForYearMonthAndEmployee.size();
            return createMonthlyBillInfo(personioEmployee, allReceiptsForYearMonthAndEmployee, sumBills);
        }
        return createMonthlyBillInfoWhenNoBills(personioEmployee);
    }

    private MonthlyBillInfo createMonthlyBillInfoWhenNoBills(PersonioEmployee personioEmployee) {
        return MonthlyBillInfo.builder()
                .sumBills(0)
                .sumPrivateBills(0)
                .sumCompanyBills(0)
                .hasAttachmentWarnings(false) // no bills -> no warnings
                .employeeHasCreditCard(personioEmployee.getHasCreditCard())
                .build();
    }

    private MonthlyBillInfo createMonthlyBillInfo(PersonioEmployee personioEmployee, List<ZepReceipt> zepReceipts, int sumBills) {
        boolean hasAttachmentWarnings = false;
        int sumPrivateBills = 0;

        for (ZepReceipt receipt : zepReceipts) {
            Optional<ZepReceiptAttachment> attachment = receiptService.getAttachmentByReceiptId(receipt.id());
            if (attachment.isEmpty()) {
                hasAttachmentWarnings = true;
            }

            if (receipt.paymentMethodType().getPaymentMethodName().equals(PaymentMethodType.PRIVATE.getPaymentMethodName())) {
                sumPrivateBills++;
            }
        }
        return MonthlyBillInfo.builder()
                .sumBills(sumBills)
                .sumPrivateBills(sumPrivateBills)
                .sumCompanyBills(sumBills - sumPrivateBills)
                .hasAttachmentWarnings(hasAttachmentWarnings)
                .employeeHasCreditCard(personioEmployee.getHasCreditCard())
                .build();
    }

    private void addProjectEmployeesToBuilder(Project.Builder projectBuilder, ZepProject zepProject) {
        List<ZepProjectEmployee> zepProjectEmployees = projectService.getProjectEmployeesForId(zepProject.id());
        MultivaluedMap<String, String> projectEmployeesMap = projectEmployeesMapper.map(zepProjectEmployees);
        Optional<ZepProjectDetail> projectDetails = projectService.getProjectById(zepProject.id());
        projectBuilder.employees(projectEmployeesMap.getOrDefault(ProjectEmployeesMapper.USER, new ArrayList<>()));
        projectBuilder.leads(projectEmployeesMap.getOrDefault(ProjectEmployeesMapper.LEAD, new ArrayList<>()));
        projectBuilder.categories(
                projectDetails.map(ZepProjectDetail::getCategories)
                        .stream()
                        .flatMap(List::stream)
                        .map(ZepCategory::name)
                        .toList()
        );
    }
}
