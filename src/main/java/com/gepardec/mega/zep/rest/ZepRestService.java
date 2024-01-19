package com.gepardec.mega.zep.rest;

import com.gepardec.mega.domain.model.AbsenceTime;
import com.gepardec.mega.domain.model.Employee;
import com.gepardec.mega.domain.model.Project;
import com.gepardec.mega.domain.model.ProjectTime;
import com.gepardec.mega.domain.model.monthlyreport.ProjectEntry;
import com.gepardec.mega.zep.ZepService;
import com.gepardec.mega.zep.rest.entity.ZepEmployee;
import com.gepardec.mega.zep.rest.entity.ZepEmploymentPeriod;
import com.gepardec.mega.zep.rest.entity.ZepRegularWorkingTimes;
import com.gepardec.mega.zep.rest.mapper.EmployeeMapper;
import com.gepardec.mega.zep.rest.service.EmployeeService;
import com.gepardec.mega.zep.rest.service.EmploymentPeriodService;
import com.gepardec.mega.zep.rest.service.RegularWorkingTimesService;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public class ZepRestService implements ZepService {

    @Inject
    EmployeeService employeeService;

    @Inject
    RegularWorkingTimesService regularWorkingTimesService;

    @Inject
    EmploymentPeriodService employmentPeriodService;

    @Inject
    EmployeeMapper employeeMapper;

    @Override
    public Employee getEmployee(String userId) {
        ZepEmployee zepEmployee = employeeService.getZepEmployeeById(userId);
        ZepRegularWorkingTimes zepRegularWorkingTimes = regularWorkingTimesService.getRegularWorkingTimesByUsername(userId);
        ZepEmploymentPeriod zepEmploymentPeriod[] = employmentPeriodService.getZepEmploymentPeriodsByEmployeeName(userId);
        return employeeMapper.map(employeeService.getZepEmployeeById(userId));
    }

    @Override
    public List<Employee> getEmployees() {
        return null;
    }

    @Override
    public void updateEmployeesReleaseDate(String userId, String releaseDate) {

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
        return null;
    }

    @Override
    public Optional<Project> getProjectByName(String projectName, LocalDate monthYear) {
        return Optional.empty();
    }

    @Override
    public List<AbsenceTime> getAbsenceForEmployee(Employee employee, LocalDate date) {
        return null;
    }

    @Override
    public List<ProjectTime> getBillableForEmployee(Employee employee, LocalDate date) {
        return null;
    }
}
