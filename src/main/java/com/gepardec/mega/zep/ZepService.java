package com.gepardec.mega.zep;

import com.gepardec.mega.domain.model.Employee;
import com.gepardec.mega.domain.model.Project;
import com.gepardec.mega.domain.model.monthlyreport.ProjectEntry;
import de.provantis.zep.FehlzeitType;
import de.provantis.zep.ProjektzeitType;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface ZepService {

    Employee getEmployee(String userId);

    List<Employee> getEmployees();

    void updateEmployeesReleaseDate(String userId, String releaseDate);

    List<ProjectEntry> getProjectTimes(Employee employee, LocalDate date);

    List<ProjektzeitType> getProjectTimesForEmployeePerProject(String project, LocalDate curDate);

    List<Project> getProjectsForMonthYear(final LocalDate monthYear);

    Optional<Project> getProjectByName(final String projectName, final LocalDate monthYear);

    List<FehlzeitType> getAbsenceForEmployee(Employee employee, LocalDate date);

    List<ProjektzeitType> getBillableForEmployee(Employee employee, LocalDate date);
}
