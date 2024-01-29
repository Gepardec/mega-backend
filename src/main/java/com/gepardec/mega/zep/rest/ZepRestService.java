package com.gepardec.mega.zep.rest;

import com.gepardec.mega.domain.model.AbsenceTime;
import com.gepardec.mega.domain.model.Employee;
import com.gepardec.mega.domain.model.Project;
import com.gepardec.mega.domain.model.ProjectTime;
import com.gepardec.mega.domain.model.monthlyreport.ProjectEntry;
import com.gepardec.mega.zep.ZepService;
import com.gepardec.mega.zep.rest.entity.*;
import com.gepardec.mega.zep.rest.mapper.AttendanceMapper;
import com.gepardec.mega.zep.rest.mapper.EmployeeMapper;
import com.gepardec.mega.zep.rest.mapper.ProjectMapper;
import com.gepardec.mega.zep.rest.mapper.RegularWorkingHoursMapMapper;
import com.gepardec.mega.zep.rest.service.*;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

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
        ZepEmployee zepEmployee = employeeService.getZepEmployeeByUsername(userId);

        List<ZepEmploymentPeriod> period = employmentPeriodService.getZepEmploymentPeriodsByEmployeeName(userId);
        boolean active = employeeMapper.getActiveOfZepEmploymentPeriods(period);

        var regularWorkingHoursMapMapper = new RegularWorkingHoursMapMapper();
        ZepRegularWorkingTimes zepRegularWorkingTimes = regularWorkingTimesService.getRegularWorkingTimesByUsername(userId);

        Employee employee = employeeMapper.map(zepEmployee);
        employee.setActive(active);
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
        return null;
    }

    @Override
    public List<ProjectTime> getProjectTimesForEmployeePerProject(String project, LocalDate curDate) {
        return null;
    }

    @Override
    public List<Project> getProjectsForMonthYear(LocalDate monthYear) {
        List<ZepProject> zepProjects = projectService.getProjectsForMonthYear(monthYear);
        var projectMapper = new ProjectMapper();
        return projectMapper.mapList(zepProjects);
    }

    @Override
    public Optional<Project> getProjectByName(String projectName, LocalDate monthYear) {
        Optional<ZepProject> zepProject = projectService.getProjectByName(projectName);
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
        List<ZepAttendance> projectTimes = attendanceService.getBillableAttendancesForUser(employee.getUserId());
        var attendanceMapper = new AttendanceMapper();
        return attendanceMapper.mapList(projectTimes);
    }
}
