package com.gepardec.mega.zep.impl;

import com.gepardec.mega.domain.model.AbsenceTime;
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

    @CacheResult(cacheName = "employee")
    @Override
    public List<Employee> getEmployees() {
        return zepServiceSoap.getEmployees();
    }

    @CacheInvalidate(cacheName = "employee")
    @Override
    public void updateEmployeesReleaseDate(String userId, String releaseDate) {
        zepServiceSoap.updateEmployeesReleaseDate(userId, releaseDate);
    }

    @CacheResult(cacheName = "projectentry")
    @Override
    public List<ProjectEntry> getProjectTimes(Employee employee, LocalDate date) {
        return zepServiceRest.getProjectTimes(employee, date);
    }

    @CacheResult(cacheName = "projektzeittype")
    @Override
    public List<ProjectTime> getProjectTimesForEmployeePerProject(String project, LocalDate curDate) {
        return zepServiceRest.getProjectTimesForEmployeePerProject(project, curDate);
    }

    @CacheResult(cacheName = "project")
    @Override
    public List<Project> getProjectsForMonthYear(LocalDate monthYear) {
        return zepServiceRest.getProjectsForMonthYear(monthYear);
    }

    @Override
    public Optional<Project> getProjectByName(String projectName, LocalDate monthYear) {
        return zepServiceRest.getProjectByName(projectName, monthYear);
    }

    @CacheResult(cacheName = "fehlzeitentype")
    @Override
    public List<AbsenceTime> getAbsenceForEmployee(Employee employee, LocalDate date) {
        return zepServiceSoap.getAbsenceForEmployee(employee, date);
    }

    @CacheResult(cacheName = "projektzeittype")
    @Override
    public List<ProjectTime> getBillableForEmployee(Employee employee, LocalDate date) {
        return zepServiceRest.getBillableForEmployee(employee, date);
    }
}