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
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Typed;
import jakarta.inject.Inject;
import jakarta.ws.rs.core.MultivaluedMap;

import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

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
    Mapper<Project, ZepProject> projectMapper;

    @Inject
    Mapper<MultivaluedMap<String, String>, List<ZepProjectEmployee>> projectEmployeesMapper;

    @Inject
    Mapper<Map<DayOfWeek, Duration>, ZepRegularWorkingTimes> regularWorkingTimesMapper;


    @Override
    public Employee getEmployee(String userId) {
        Optional<ZepEmployee> zepEmployee = employeeService.getZepEmployeeByUsername(userId);
        if (zepEmployee.isEmpty()) {
            return null;
        }

        List<ZepEmploymentPeriod> period = employmentPeriodService.getZepEmploymentPeriodsByEmployeeName(userId);
        boolean active = EmployeeMapper.getActiveOfZepEmploymentPeriods(period);


        ZepRegularWorkingTimes zepRegularWorkingTimes = regularWorkingTimesService.getRegularWorkingTimesByUsername(userId);

        Employee employee = employeeMapper.map(zepEmployee.get());
        employee.setActive(active);
        employee.setRegularWorkingHours(regularWorkingTimesMapper.map(zepRegularWorkingTimes));
        return employee;
    }

    @Override
    public List<Employee> getEmployees() {
        List<ZepEmployee> zepEmployees = employeeService.getZepEmployees();
        List<Employee> employees = employeeMapper.mapList(zepEmployees);
        employees.forEach(
                employee -> {
                    // TODO: Get real val
//                    var periods = employmentPeriodService.getZepEmploymentPeriodsByEmployeeName(employee.getUserId());
                    boolean active = true;//employeeMapper.getActiveOfZepEmploymentPeriods(periods);
                    employee.setActive(active);
                });

        return employees;
    }

    @Override
    public void updateEmployeesReleaseDate(String userId, String releaseDate) {
        return;         // Currently not supported by REST - use SOAP instead
    }

    @Override
    public List<ProjectEntry> getProjectTimes(Employee employee, LocalDate date) {
        List<ZepAttendance> zepAttendances = attendanceService.getAttendanceForUserAndMonth(employee.getUserId(), date);
        return projectEntryMapper.mapList(zepAttendances);
    }

    @Override
    public List<ProjectTime> getProjectTimesForEmployeePerProject(String project, LocalDate curDate) {
        List<ZepAttendance> allZepAttendancesForProject = new ArrayList<>();
        Integer projectId = projectService.getProjectByName(project, curDate).map(ZepProject::getId).orElse(null);

        List<ZepProjectEmployee> projectEmployees = projectService.getProjectEmployeesForId(projectId);

        for (ZepProjectEmployee projectEmployee :
                projectEmployees) {
            allZepAttendancesForProject.addAll(attendanceService.getAttendanceForUserProjectAndMonth(projectEmployee.getUsername(), curDate, projectId));
        }
        return attendanceMapper.mapList(allZepAttendancesForProject);
    }

    @Override
    public List<Project> getProjectsForMonthYear(LocalDate monthYear) {
        List<ZepProject> zepProjects = projectService.getProjectsForMonthYear(monthYear);
        List<Project> projects = projectMapper.mapList(zepProjects);
        projects.forEach(project -> {
            List<ZepProjectEmployee> zepProjectEmployees = projectService.getProjectEmployeesForId(project.getZepId());
            MultivaluedMap<String, String> projectEmployeesMap = projectEmployeesMapper.map(zepProjectEmployees);
            project.setEmployees(projectEmployeesMap.getOrDefault(ProjectEmployeesMapper.USER, new ArrayList<>()));
            project.setLeads(projectEmployeesMap.getOrDefault(ProjectEmployeesMapper.LEAD, new ArrayList<>()));
        });

        return projects;
    }

    @Override
    public Optional<Project> getProjectByName(String projectName, LocalDate monthYear) {
        Optional<ZepProject> zepProject = projectService.getProjectByName(projectName, monthYear);
        return zepProject.map(project -> projectMapper.map(project));
    }

    @Override
    public List<AbsenceTime> getAbsenceForEmployee(Employee employee, LocalDate date) {
        LocalDate firstOfMonth = DateUtils.getFirstDayOfMonth(date.getYear(), date.getMonthValue());
        LocalDate lastOfMonth = DateUtils.getLastDayOfMonth(date.getYear(), date.getMonthValue());
        List<ZepAbsence> zepAbsences = absenceService
                .getZepAbsencesByEmployeeNameForDateRange(employee.getUserId(), firstOfMonth, lastOfMonth);

        return absenceMapper.mapList(zepAbsences);
    }

    @Override
    public List<ProjectTime> getBillableForEmployee(Employee employee, LocalDate date) {
        List<ZepAttendance> projectTimes = attendanceService.getBillableAttendancesForUserAndMonth(employee.getUserId(), date);
        return attendanceMapper.mapList(projectTimes);
    }
}