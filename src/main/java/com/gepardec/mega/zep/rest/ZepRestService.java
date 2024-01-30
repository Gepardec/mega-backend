package com.gepardec.mega.zep.rest;

import com.gepardec.mega.domain.model.AbsenceTime;
import com.gepardec.mega.domain.model.Employee;
import com.gepardec.mega.domain.model.Project;
import com.gepardec.mega.domain.model.ProjectTime;
import com.gepardec.mega.domain.model.monthlyreport.ProjectEntry;
import com.gepardec.mega.zep.ZepService;
import com.gepardec.mega.zep.ZepServiceSoapImpl;
import com.gepardec.mega.zep.rest.entity.*;
import com.gepardec.mega.zep.rest.mapper.*;
import com.gepardec.mega.zep.rest.service.*;
import jakarta.ejb.LocalBean;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.context.RequestScoped;
import jakarta.enterprise.inject.Alternative;
import jakarta.enterprise.inject.Typed;
import jakarta.enterprise.inject.Vetoed;
import jakarta.inject.Inject;
import jakarta.ws.rs.core.MultivaluedMap;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;


@ApplicationScoped
@Typed(ZepRestService.class)
public class ZepRestService implements ZepService {

    @Inject
    EmployeeService employeeService;
    @Inject
    ProjectService projectService;
    @Inject
    AttendanceService attendanceService;

    @Inject
    RegularWorkingTimesService regularWorkingTimesService;

    @Inject
    EmploymentPeriodService employmentPeriodService;

    @Inject
    EmployeeMapper employeeMapper;


    @Override
    public Employee getEmployee(String userId) {
        Optional<ZepEmployee> zepEmployee = employeeService.getZepEmployeeByUsername(userId);
        if (zepEmployee.isEmpty()) {
            return null;
        }

        List<ZepEmploymentPeriod> period = employmentPeriodService.getZepEmploymentPeriodsByEmployeeName(userId);
        boolean active = employeeMapper.getActiveOfZepEmploymentPeriods(period);

        var regularWorkingHoursMapMapper = new RegularWorkingHoursMapMapper();
        ZepRegularWorkingTimes zepRegularWorkingTimes = regularWorkingTimesService.getRegularWorkingTimesByUsername(userId);

        Employee employee = employeeMapper.map(zepEmployee.get());
        employee.setActive(active);
        System.out.println("Employee: " + employee.getUserId() + " is active: " + employee.isActive());
        employee.setRegularWorkingHours(regularWorkingHoursMapMapper.map(zepRegularWorkingTimes));
        return employee;
    }

    @Override
    public List<Employee> getEmployees() {
        List<ZepEmployee> zepEmployees = employeeService.getZepEmployees();
        List<Employee> employees = employeeMapper.mapList(zepEmployees);
        employees.forEach(
                employee -> {
                    var periods = employmentPeriodService.getZepEmploymentPeriodsByEmployeeName(employee.getUserId());
                    boolean active = employeeMapper.getActiveOfZepEmploymentPeriods(periods);
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


        return null;
    }

    @Override
    public List<ProjectTime> getProjectTimesForEmployeePerProject(String project, LocalDate curDate) {
        return null;
    }

    @Override
    public List<Project> getProjectsForMonthYear(LocalDate monthYear) {
        var projectMapper = new ProjectMapper();
        var projectEmployeesMapper = new ProjectEmployeesMapper();

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
        if (zepProject.isPresent()) {
            var projectMapper = new ProjectMapper();
            return Optional.of(projectMapper.map(zepProject.get()));
        }
        return Optional.empty();
    }

    @Override
    public List<AbsenceTime> getAbsenceForEmployee(Employee employee, LocalDate date) {
        return null;         // Currently not supported by REST - use SOAP instead
    }

    @Override
    public List<ProjectTime> getBillableForEmployee(Employee employee, LocalDate date) {
        List<ZepAttendance> projectTimes = attendanceService.getBillableAttendancesForUserAndMonth(employee.getUserId(), date);
        var attendanceMapper = new AttendanceMapper();
        return attendanceMapper.mapList(projectTimes);
    }
}
