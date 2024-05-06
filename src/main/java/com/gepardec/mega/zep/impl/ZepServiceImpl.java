package com.gepardec.mega.zep.impl;

import com.gepardec.mega.domain.model.AbsenceTime;
import com.gepardec.mega.domain.model.Bill;
import com.gepardec.mega.domain.model.Employee;
import com.gepardec.mega.domain.model.Project;
import com.gepardec.mega.domain.model.ProjectTime;
import com.gepardec.mega.domain.model.monthlyreport.ProjectEntry;
import com.gepardec.mega.zep.ZepService;
import io.quarkus.cache.CacheInvalidate;
import io.quarkus.cache.CacheResult;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.Optional;

@ApplicationScoped
public class ZepServiceImpl implements ZepService {

    @Soap
    ZepService zepServiceSoap;

    @Rest
    ZepService zepServiceRest;

    @Override
    public Employee getEmployee(String userId) {
        return zepServiceSoap.getEmployee(userId);
    }

    @Override
    public List<Employee> getEmployees() {
        return zepServiceSoap.getEmployees();
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

    @Override
    public List<Bill> getBillsForEmployeeByMonth(Employee employee, YearMonth yearMonth) {
        return zepServiceRest.getBillsForEmployeeByMonth(employee, yearMonth);
    }
}
