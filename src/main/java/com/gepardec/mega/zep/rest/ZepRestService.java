package com.gepardec.mega.zep.rest;

import com.gepardec.mega.domain.model.AbsenceTime;
import com.gepardec.mega.domain.model.Employee;
import com.gepardec.mega.domain.model.Project;
import com.gepardec.mega.domain.model.ProjectTime;
import com.gepardec.mega.domain.model.monthlyreport.ProjectEntry;
import com.gepardec.mega.zep.ZepService;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public class ZepRestService implements ZepService {
    @Override
    public Employee getEmployee(String userId) {
        return null;
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
