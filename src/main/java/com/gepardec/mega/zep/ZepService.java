package com.gepardec.mega.zep;

import com.gepardec.mega.domain.model.AbsenceTime;
import com.gepardec.mega.domain.model.Employee;
import com.gepardec.mega.domain.model.MonthlyBillInfo;
import com.gepardec.mega.domain.model.PersonioEmployee;
import com.gepardec.mega.domain.model.Project;
import com.gepardec.mega.domain.model.ProjectHoursSummary;
import com.gepardec.mega.domain.model.ProjectTime;
import com.gepardec.mega.domain.model.monthlyreport.ProjectEntry;

import java.time.YearMonth;
import java.util.List;
import java.util.Optional;

public interface ZepService {

    Employee getEmployee(String userId);

    List<Employee> getEmployees();

    void updateEmployeesReleaseDate(String userId, String releaseDate);

    List<ProjectEntry> getProjectTimes(Employee employee, YearMonth payrollMonth);

    List<ProjectTime> getProjectTimesForEmployeePerProject(String project, YearMonth payrollMonth);

    List<Project> getProjectsForMonthYear(final YearMonth payrollMonth);

    Optional<Project> getProjectByName(final String projectName, final YearMonth payrollMonth);

    List<AbsenceTime> getAbsenceForEmployee(Employee employee, YearMonth payrollMonth);

    List<ProjectTime> getBillableForEmployee(Employee employee, YearMonth payrollMonth);

    MonthlyBillInfo getMonthlyBillInfoForEmployee(PersonioEmployee personioEmployee, final Employee employee, YearMonth payrollMonth);

    List<ProjectHoursSummary> getAllProjectsForMonthAndEmployee(final Employee employee, YearMonth payrollMonth);

    double getDoctorsVisitingTimeForMonthAndEmployee(final Employee employee, YearMonth payrollMonth);

    void updateEmployeeHourlyRate(String userId, Double newRate, String fromDate);
}
