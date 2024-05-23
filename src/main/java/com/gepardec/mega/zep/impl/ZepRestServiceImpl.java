package com.gepardec.mega.zep.impl;

import com.gepardec.mega.db.entity.common.ProjectTaskType;
import com.gepardec.mega.domain.model.AbsenceTime;
import com.gepardec.mega.domain.model.Bill;
import com.gepardec.mega.domain.model.Employee;
import com.gepardec.mega.domain.model.Project;
import com.gepardec.mega.domain.model.ProjectHoursSummary;
import com.gepardec.mega.domain.model.ProjectTime;
import com.gepardec.mega.domain.model.monthlyreport.ProjectEntry;
import com.gepardec.mega.domain.utils.DateUtils;
import com.gepardec.mega.service.api.MonthlyReportService;
import com.gepardec.mega.zep.ZepService;
import com.gepardec.mega.zep.rest.dto.ZepAbsence;
import com.gepardec.mega.zep.rest.dto.ZepAttendance;
import com.gepardec.mega.zep.rest.dto.ZepEmployee;
import com.gepardec.mega.zep.rest.dto.ZepEmploymentPeriod;
import com.gepardec.mega.zep.rest.dto.ZepProject;
import com.gepardec.mega.zep.rest.dto.ZepProjectEmployee;
import com.gepardec.mega.zep.rest.dto.ZepReceipt;
import com.gepardec.mega.zep.rest.dto.ZepReceiptAmount;
import com.gepardec.mega.zep.rest.dto.ZepReceiptAttachment;
import com.gepardec.mega.zep.rest.dto.ZepRegularWorkingTimes;
import com.gepardec.mega.zep.rest.mapper.Mapper;
import com.gepardec.mega.zep.rest.mapper.ProjectEmployeesMapper;
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
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static com.gepardec.mega.domain.utils.DateUtils.formatDate;
import static com.gepardec.mega.domain.utils.DateUtils.getLastDayOfCurrentMonth;
import static com.gepardec.mega.domain.utils.DateUtils.getFirstDayOfCurrentMonth;


@ApplicationScoped
@Rest
public class ZepRestServiceImpl implements ZepService {

    @Inject
    EmployeeService employeeService;

    @Inject
    MonthlyReportService monthlyReportService;
    @Inject
    ProjectService projectService;
    @Inject
    AttendanceService attendanceService;

    @Inject
    ReceiptService receiptService;

    @Inject
    AbsenceService absenceService;

    @Inject
    Mapper<AbsenceTime, ZepAbsence> absenceMapper;

    @Inject
    RegularWorkingTimesService regularWorkingTimesService;

    @Inject
    EmploymentPeriodService employmentPeriodService;

    @Inject
    Mapper<Employee, ZepEmployee> employeeMapper;

    @Inject
    Mapper<ProjectEntry, ZepAttendance> projectEntryMapper;

    @Inject
    Mapper<ProjectTime, ZepAttendance> attendanceMapper;

    @Inject
    Mapper<Project.Builder, ZepProject> projectMapper;

    @Inject
    Mapper<MultivaluedMap<String, String>, List<ZepProjectEmployee>> projectEmployeesMapper;

    @Inject
    Mapper<Map<DayOfWeek, Duration>, ZepRegularWorkingTimes> regularWorkingTimesMapper;

    @Inject
    Mapper<Boolean, List<ZepEmploymentPeriod>> activeEmployeeMapper;

    @Inject
    Logger logger;

    @Override
    public Employee getEmployee(String userId) {
        logger.debug("Retrieving employee %s from ZEP".formatted(userId));

        Optional<ZepEmployee> zepEmployee = employeeService.getZepEmployeeByUsername(userId);
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
        zepRegularWorkingTimesOpt.ifPresent(rwt -> {
            employee.setRegularWorkingHours(regularWorkingTimesMapper.map(rwt));
        });
        return employee;
    }

    @Override
    public List<Employee> getEmployees() {
        logger.debug("Retrieving employees from ZEP");

        List<ZepEmployee> zepEmployees = employeeService.getZepEmployees();
        List<Employee> employees = employeeMapper.mapList(zepEmployees);
        employees.forEach(
                employee -> {
                    var periods = employmentPeriodService.getZepEmploymentPeriodsByEmployeeName(employee.getUserId());
                    boolean active = activeEmployeeMapper.map(periods);
                    employee.setActive(active);
                });

        return employees;
    }

    @Override
    public void updateEmployeesReleaseDate(String userId, String releaseDate) {
        throw new NotImplementedException("This method is not provided in REST, use SOAP instead.");         // Currently not supported by REST - use SOAP instead
    }

    @Override
    public List<ProjectEntry> getProjectTimes(Employee employee, LocalDate date) {
        logger.debug("Retrieving project times from ZEP of %s".formatted(employee.getUserId()));
        List<ZepAttendance> zepAttendances = attendanceService.getAttendanceForUserAndMonth(employee.getUserId(), date);
        return projectEntryMapper.mapList(zepAttendances);
    }

    @CacheResult(cacheName = "projectTimesForEmployeePerProject")
    @Override
    public List<ProjectTime> getProjectTimesForEmployeePerProject(String project, LocalDate curDate) {
        List<ZepAttendance> allZepAttendancesForProject = new ArrayList<>();
        logger.debug("Retrieving project %s from ZEP".formatted(project));
        Optional<ZepProject> projectOpt = projectService.getProjectByName(project, curDate);
        if (projectOpt.isEmpty()) {
            logger.warn("No project found for name {}", project);
            return List.of();
        }

        Integer projectId = projectOpt.get().id();

        logger.debug("Retrieving project employees of %d from ZEP".formatted(projectId));
        List<ZepProjectEmployee> projectEmployees = projectService.getProjectEmployeesForId(projectId);

        projectEmployees.forEach(projectEmployee -> {
            logger.debug("Retrieving attendance of user %s of project %d from ZEP".formatted(projectEmployee.username(), projectId));
            allZepAttendancesForProject.addAll(attendanceService.getAttendanceForUserProjectAndMonth(projectEmployee.username(), curDate, projectId));
        });
        return attendanceMapper.mapList(allZepAttendancesForProject);
    }

    @Override
    public List<Project> getProjectsForMonthYear(LocalDate monthYear) {
        logger.debug("Retrieving projects for monthYear of %s from ZEP".formatted(monthYear.toString()));
        List<ZepProject> zepProjects = projectService.getProjectsForMonthYear(monthYear);
        List<Project.Builder> projects = projectMapper.mapList(zepProjects);
        IntStream.range(0, projects.size())
                .forEach(i -> addProjectEmployeesToBuilder(projects.get(i), zepProjects.get(i)));
        return projects.stream()
                .map(Project.Builder::build)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<Project> getProjectByName(String projectName, LocalDate monthYear) {
        logger.debug("Retrieving project %s from ZEP".formatted(projectName));
        Optional<ZepProject> zepProject = projectService.getProjectByName(projectName, monthYear);
        return zepProject.map(project -> projectMapper.map(project).build());
    }

    @Override
    public List<AbsenceTime> getAbsenceForEmployee(Employee employee, LocalDate date) {
        logger.debug("Retrieving absences of %s from ZEP".formatted(employee.getUserId()));
        LocalDate firstOfMonth = DateUtils.getFirstDayOfMonth(date.getYear(), date.getMonthValue());
        LocalDate lastOfMonth = DateUtils.getLastDayOfMonth(date.getYear(), date.getMonthValue());
        List<ZepAbsence> zepAbsences = absenceService
                .getZepAbsencesByEmployeeNameForDateRange(employee.getUserId(), firstOfMonth, lastOfMonth);

        return absenceMapper.mapList(zepAbsences);
    }

    @Override
    public List<ProjectTime> getBillableForEmployee(Employee employee, LocalDate date) {
        logger.debug("Retrieving billable entries of employee %s from ZEP".formatted(employee.getUserId()));
        List<ZepAttendance> projectTimes = attendanceService.getBillableAttendancesForUserAndMonth(employee.getUserId(), date);
        return attendanceMapper.mapList(projectTimes);
    }

    @Override
    public List<Bill> getBillsForEmployeeByMonth(Employee employee, YearMonth yearMonth) {
        Pair<String, String> fromToDatePair = getCorrectDateForRequest(employee, yearMonth);
        return getBillsInternal(employee, fromToDatePair.getLeft(), fromToDatePair.getRight());
    }

    @Override
    public List<ProjectHoursSummary> getAllProjectsForMonthAndEmployee(Employee employee, YearMonth yearMonth) {
        Optional<ZepEmployee> employeeRetrieved = employeeService.getZepEmployeeByUsername(employee.getUserId());
        List<ProjectHoursSummary> resultProjectHoursSummary = new ArrayList<>();

        if(employeeRetrieved.isPresent()) {
            resultProjectHoursSummary = getProjectsForMonthAndEmployeeInternal(employeeRetrieved.get(), yearMonth);
        }
        return resultProjectHoursSummary;
    }

    @Override
    public double getDoctorsVisitingTimeForMonthAndEmployee(Employee employee, YearMonth yearMonth) {
        String startDateString = getCorrectDateForRequest(employee, yearMonth).getLeft();
        LocalDate startDate = DateUtils.parseDate(startDateString);

        List<ZepAttendance> doctorsAttendances = attendanceService.getAttendanceForUserProjectAndMonth(employee.getUserId(), startDate, ProjectTaskType.PROJECT_INTERNAL.getId())
                                                                  .stream()
                                                                  .filter(attendance -> attendance.projectTaskId().equals(ProjectTaskType.TASK_DOCTOR_VISIT.getId()))
                                                                  .toList();

        return doctorsAttendances.stream()
                                  .mapToDouble(ZepAttendance::duration)
                                  .sum();
    }

    private List<ProjectHoursSummary> getProjectsForMonthAndEmployeeInternal(ZepEmployee employee, YearMonth yearMonth) {
        Employee employeeForRequest = employeeMapper.map(employee);
        String dateString = getCorrectDateForRequest(employeeForRequest, yearMonth).getLeft();
        LocalDate dateForRequest = DateUtils.parseDate(dateString);
        List<ProjectHoursSummary> resultProjectHoursSummary = new ArrayList<>();
        List<ZepProject> projectsRetrieved = projectService.getProjectsForMonthYear(dateForRequest);

        projectsRetrieved.forEach(
                project -> {
                    Optional<ZepProjectEmployee> projectEmployee = projectService.getProjectEmployeesForId(project.id())
                                                                                 .stream()
                                                                                 .filter(e -> e.username().equals(employee.username()))
                                                                                 .findFirst();
                    if(projectEmployee.isEmpty()) {
                        return;
                    }
                    List<ZepAttendance> attendancesForEmployeeAndProject = attendanceService.getAttendanceForUserProjectAndMonth(projectEmployee.get().username(), dateForRequest, project.id());
                    if(!attendancesForEmployeeAndProject.isEmpty()){
                        Optional<ProjectHoursSummary> optionalProjectHoursSummary = createProjectsHoursSummary(attendancesForEmployeeAndProject, project);
                        optionalProjectHoursSummary.ifPresent(resultProjectHoursSummary::add);
                    }
                });
        return resultProjectHoursSummary;
    }

    private Optional<ProjectHoursSummary> createProjectsHoursSummary(List<ZepAttendance> attendances, ZepProject project) {
        Optional<ZepProject> projectRetrieved = projectService.getProjectById(attendances.get(0).projectId());
        String projectName = "";
        double billableHoursSum = 0.0;
        double nonBillableHoursSum = 0.0;
        double chargeability = 0.0;

        if(projectRetrieved.isEmpty()) {
            return Optional.empty();
        }

         projectName = projectRetrieved.get().name();

         billableHoursSum += attendances.stream()
                                        .filter(ZepAttendance::billable)
                                        .mapToDouble(ZepAttendance::duration)
                                        .sum();

         nonBillableHoursSum += attendances.stream()
                                           .filter(a -> !a.billable())
                                           .mapToDouble(ZepAttendance::duration)
                                           .sum();

         double totalHours = Double.sum(billableHoursSum, nonBillableHoursSum);

         if(!(Double.compare(totalHours, 0.0d) == 0)){
             chargeability = billableHoursSum/totalHours;
             chargeability = BigDecimal.valueOf(chargeability)
                                       .setScale(2, RoundingMode.HALF_UP)
                                       .doubleValue();
         }


         return Optional.of(ProjectHoursSummary.builder()
                                               .projectName(projectName)
                                               .billableHoursSum(billableHoursSum)
                                               .nonBillableHoursSum(nonBillableHoursSum)
                                               .chargeability(chargeability * 100)
                                               .isInternalProject(project.customerId() == null)
                                               .build());
    }

    private Pair<String, String> getCorrectDateForRequest(Employee employee, YearMonth yearMonth) {
        LocalDate now = LocalDate.now();
        LocalDate firstOfPreviousMonth = now.withMonth(now.getMonth().minus(1).getValue()).withDayOfMonth(1);
        LocalDate midOfCurrentMonth = LocalDate.now().withDayOfMonth(14);

        if (yearMonth != null) {
            return getDateWhenYearMonthProvided(yearMonth);
        }
        if (now.isAfter(midOfCurrentMonth) && monthlyReportService.isMonthConfirmedFromEmployee(employee, firstOfPreviousMonth)) {
            return getDateWhenMonthIsConfirmedFromEmployeeAndMidOfMonthIsReached();
        }

        String fromDate = formatDate(firstOfPreviousMonth);
        String toDate = formatDate(getLastDayOfCurrentMonth(fromDate));
        return Pair.of(fromDate,toDate);
    }

    private Pair<String, String> getDateWhenYearMonthProvided(YearMonth yearMonth) {
        String fromDate = formatDate(yearMonth.atDay(1));
        String toDate = formatDate(getLastDayOfCurrentMonth(fromDate));
        return Pair.of(fromDate, toDate);
    }

    private Pair<String, String> getDateWhenMonthIsConfirmedFromEmployeeAndMidOfMonthIsReached() {
        LocalDate now = LocalDate.now();
        String fromDate = getFirstDayOfCurrentMonth(now);
        String toDate = getLastDayOfCurrentMonth(now);
        return Pair.of(fromDate, toDate);
    }

    private List<Bill> getBillsInternal(Employee employee, String fromDate, String toDate) {
        List<ZepReceipt> allReceiptsForYearMonth = receiptService.getAllReceiptsForYearMonth(employee, fromDate, toDate);
        List<ZepReceipt> allReceiptsForYearMonthAndEmployee;
        List<Bill> resultBillList = new ArrayList<>();


        if (!allReceiptsForYearMonth.isEmpty()) {
            allReceiptsForYearMonthAndEmployee = allReceiptsForYearMonth.stream()
                                                                        .filter(receipt -> receipt.employeeId().equals(employee.getUserId()))
                                                                        .toList();

            allReceiptsForYearMonthAndEmployee.forEach(zepReceipt -> {

                Optional<ZepProject> zepProject = projectService.getProjectById(zepReceipt.projectId());
                Optional<ZepReceiptAttachment> attachment = receiptService.getAttachmentByReceiptId(zepReceipt.id());
                Optional<ZepReceiptAmount> receiptAmount = receiptService.getAmountByReceiptId(zepReceipt.id());

                resultBillList.addAll(createBillList(zepReceipt, zepProject, attachment, receiptAmount));
            });
        }
        return resultBillList;
    }

    private List<Bill> createBillList(ZepReceipt zepReceipt, Optional<ZepProject> zepProject, Optional<ZepReceiptAttachment> attachment, Optional<ZepReceiptAmount> receiptAmount) {
        List<Bill> resultBillList = new ArrayList<>();
        zepProject.ifPresent(project ->
                resultBillList.add(
                        Bill.builder()
                            .billDate(zepReceipt.receiptDate())
                            .bruttoValue(receiptAmount.map(receipt -> receipt.amount() * receipt.quantity()).orElse(null))
                            .billType(zepReceipt.receiptTypeName())
                            .paymentMethodType(zepReceipt.paymentMethodType())
                            .projectName(project.name())
                            .attachmentBase64(attachment.map(ZepReceiptAttachment::fileContent).orElse(null))
                            .attachmentFileName(zepReceipt.attachmentFileName())
                            .build()
                ));

        return resultBillList;
    }

    private void addProjectEmployeesToBuilder(Project.Builder projectBuilder, ZepProject zepProject) {
        List<ZepProjectEmployee> zepProjectEmployees = projectService.getProjectEmployeesForId(zepProject.id());
        MultivaluedMap<String, String> projectEmployeesMap = projectEmployeesMapper.map(zepProjectEmployees);
        projectBuilder.employees(projectEmployeesMap.getOrDefault(ProjectEmployeesMapper.USER, new ArrayList<>()));
        projectBuilder.leads(projectEmployeesMap.getOrDefault(ProjectEmployeesMapper.LEAD, new ArrayList<>()));
    }
}
