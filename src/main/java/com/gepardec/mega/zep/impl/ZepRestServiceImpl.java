package com.gepardec.mega.zep.impl;

import com.gepardec.mega.domain.model.AbsenceTime;
import com.gepardec.mega.domain.model.Employee;
import com.gepardec.mega.domain.model.Project;
import com.gepardec.mega.domain.model.ProjectTime;
import com.gepardec.mega.domain.model.monthlyreport.ProjectEntry;
import com.gepardec.mega.domain.utils.DateUtils;
import com.gepardec.mega.zep.ZepService;
import com.gepardec.mega.zep.rest.entity.ZepAbsence;
import com.gepardec.mega.zep.rest.entity.ZepAttendance;
import com.gepardec.mega.zep.rest.entity.ZepEmployee;
import com.gepardec.mega.zep.rest.entity.ZepEmploymentPeriod;
import com.gepardec.mega.zep.rest.entity.ZepProject;
import com.gepardec.mega.zep.rest.entity.ZepProjectEmployee;
import com.gepardec.mega.zep.rest.entity.ZepRegularWorkingTimes;
import com.gepardec.mega.zep.rest.mapper.AbsenceMapper;
import com.gepardec.mega.zep.rest.mapper.AttendanceMapper;
import com.gepardec.mega.zep.rest.mapper.EmployeeMapper;
import com.gepardec.mega.zep.rest.mapper.Mapper;
import com.gepardec.mega.zep.rest.mapper.ProjectEmployeesMapper;
import com.gepardec.mega.zep.rest.mapper.ProjectEntryMapper;
import com.gepardec.mega.zep.rest.mapper.ProjectMapper;
import com.gepardec.mega.zep.rest.mapper.RegularWorkingHoursMapMapper;
import com.gepardec.mega.zep.rest.service.AbsenceService;
import com.gepardec.mega.zep.rest.service.AttendanceService;
import com.gepardec.mega.zep.rest.service.EmployeeService;
import com.gepardec.mega.zep.rest.service.EmploymentPeriodService;
import com.gepardec.mega.zep.rest.service.ProjectService;
import com.gepardec.mega.zep.rest.service.RegularWorkingTimesService;
import io.quarkus.cache.CacheInvalidate;
import io.quarkus.cache.CacheResult;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Typed;
import jakarta.inject.Inject;
import jakarta.ws.rs.core.MultivaluedMap;
import org.slf4j.Logger;

import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@ApplicationScoped
@Rest
public class ZepRestServiceImpl implements ZepService {

    @Inject
    EmployeeService employeeService;
    @Inject
    ProjectService projectService;
    @Inject
    AttendanceService attendanceService;

    @Inject
    AbsenceService absenceService;

    @Inject
    Mapper<AbsenceTime, ZepAbsence> absenceMapper;

    @Inject
    RegularWorkingTimesService regularWorkingTimesService;

    @Inject
    EmploymentPeriodService employmentPeriodService;

    @Inject
    Mapper<Employee,ZepEmployee> employeeMapper;

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
    @CacheResult(cacheName = "employee")
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

    @CacheInvalidate(cacheName = "employee")
    @Override
    public void updateEmployeesReleaseDate(String userId, String releaseDate) {
        return;         // Currently not supported by REST - use SOAP instead
    }

    @Override
    public List<ProjectEntry> getProjectTimes(Employee employee, LocalDate date) {
        logger.debug("Retrieving project times from ZEP of %s".formatted(employee.getUserId()));
        List<ZepAttendance> zepAttendances = attendanceService.getAttendanceForUserAndMonth(employee.getUserId(), date);
        return projectEntryMapper.mapList(zepAttendances);
    }

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

    private void addProjectEmployeesToBuilder(Project.Builder projectBuilder, ZepProject zepProject) {
            List<ZepProjectEmployee> zepProjectEmployees = projectService.getProjectEmployeesForId(zepProject.id());
            MultivaluedMap<String, String> projectEmployeesMap = projectEmployeesMapper.map(zepProjectEmployees);
            projectBuilder.employees(projectEmployeesMap.getOrDefault(ProjectEmployeesMapper.USER, new ArrayList<>()));
            projectBuilder.leads(projectEmployeesMap.getOrDefault(ProjectEmployeesMapper.LEAD, new ArrayList<>()));
    }
}
