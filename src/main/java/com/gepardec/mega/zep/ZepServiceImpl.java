package com.gepardec.mega.zep;

import com.gepardec.mega.domain.model.AbsenceTime;
import com.gepardec.mega.domain.model.Employee;
import com.gepardec.mega.domain.model.Project;
import com.gepardec.mega.domain.model.ProjectTime;
import com.gepardec.mega.domain.model.monthlyreport.ProjectEntry;
import com.gepardec.mega.zep.rest.ZepRestService;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@ApplicationScoped
public class ZepServiceImpl implements ZepService {

    @Inject
    ZepServiceSoapImpl zepServiceSoap;

    @Inject
    ZepRestService zepServiceRest;

    @Override
    public Employee getEmployee(String userId) {
        return zepServiceRest.getEmployee(userId);
    }

    @Override
    public List<Employee> getEmployees() {
        return zepServiceRest.getEmployees();
    }

    @Override
    public void updateEmployeesReleaseDate(String userId, String releaseDate) {
        zepServiceSoap.updateEmployeesReleaseDate(userId, releaseDate);
    }

    @Override
    public List<ProjectEntry> getProjectTimes(Employee employee, LocalDate date) {
        return zepServiceRest.getProjectTimes(employee, date);
    }

    @Override
    public List<ProjectTime> getProjectTimesForEmployeePerProject(String project, LocalDate curDate) {
        return zepServiceRest.getProjectTimesForEmployeePerProject(project, curDate);
    }

    @Override
    public List<Project> getProjectsForMonthYear(LocalDate monthYear) {
        return zepServiceRest.getProjectsForMonthYear(monthYear);
    }

    @Override
    public Optional<Project> getProjectByName(String projectName, LocalDate monthYear) {
        return zepServiceRest.getProjectByName(projectName, monthYear);
    }

    @Override
    public List<AbsenceTime> getAbsenceForEmployee(Employee employee, LocalDate date) {
        return zepServiceSoap.getAbsenceForEmployee(employee, date);
    }

    @Override
    public List<ProjectTime> getBillableForEmployee(Employee employee, LocalDate date) {
        return zepServiceRest.getBillableForEmployee(employee, date);
    }
}
