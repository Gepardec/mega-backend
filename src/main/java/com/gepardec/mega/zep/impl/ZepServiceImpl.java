package com.gepardec.mega.zep.impl;

import com.gepardec.mega.domain.model.AbsenceTime;
import com.gepardec.mega.domain.model.Employee;
import com.gepardec.mega.domain.model.MonthlyBillInfo;
import com.gepardec.mega.domain.model.PersonioEmployee;
import com.gepardec.mega.domain.model.Project;
import com.gepardec.mega.domain.model.ProjectHoursSummary;
import com.gepardec.mega.domain.model.ProjectTime;
import com.gepardec.mega.domain.model.monthlyreport.ProjectEntry;
import com.gepardec.mega.zep.ZepService;
import de.provantis.zep.InternersatzListeType;
import de.provantis.zep.MitarbeiterType;
import de.provantis.zep.ResponseHeaderType;
import jakarta.enterprise.context.ApplicationScoped;

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

    public MitarbeiterType getEmployeeMitarbeiterType(String userId) {
        return zepServiceSoap.getEmployeeMitarbeiterType(userId);
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
    public List<ProjectEntry> getProjectTimes(Employee employee, YearMonth payrollMonth) {
        return zepServiceRest.getProjectTimes(employee, payrollMonth);
    }

    @Override
    public List<ProjectTime> getProjectTimesForEmployeePerProject(String project, YearMonth payrollMonth) {
        return zepServiceRest.getProjectTimesForEmployeePerProject(project, payrollMonth);
    }

    @Override
    public List<Project> getProjectsForMonthYear(YearMonth payrollMonth) {
        return zepServiceRest.getProjectsForMonthYear(payrollMonth);
    }

    @Override
    public Optional<Project> getProjectByName(String projectName, YearMonth payrollMonth) {
        return zepServiceRest.getProjectByName(projectName, payrollMonth);
    }

    @Override
    public List<AbsenceTime> getAbsenceForEmployee(Employee employee, YearMonth payrollMonth) {
        return zepServiceSoap.getAbsenceForEmployee(employee, payrollMonth);
    }

    @Override
    public List<ProjectTime> getBillableForEmployee(Employee employee, YearMonth payrollMonth) {
        return zepServiceRest.getBillableForEmployee(employee, payrollMonth);
    }

    @Override
    public MonthlyBillInfo getMonthlyBillInfoForEmployee(PersonioEmployee personioEmployee, Employee employee, YearMonth payrollMonth) {
        return zepServiceRest.getMonthlyBillInfoForEmployee(personioEmployee, employee, payrollMonth);
    }

    @Override
    public List<ProjectHoursSummary> getAllProjectsForMonthAndEmployee(Employee employee, YearMonth payrollMonth) {
        return zepServiceRest.getAllProjectsForMonthAndEmployee(employee, payrollMonth);
    }

    @Override
    public double getDoctorsVisitingTimeForMonthAndEmployee(Employee employee, YearMonth payrollMonth) {
        return zepServiceRest.getDoctorsVisitingTimeForMonthAndEmployee(employee, payrollMonth);
    }

    @Override
    public ResponseHeaderType updateEmployeeHourlyRate(String userId, InternersatzListeType internalRates){
        return zepServiceSoap.updateEmployeeHourlyRate(userId, internalRates);
    }
}
