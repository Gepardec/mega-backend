package com.gepardec.mega.service.api;

import com.gepardec.mega.domain.model.Employee;

import java.time.YearMonth;
import java.util.List;

public interface EmployeeService {

    Employee getEmployee(final String userId);

    List<Employee> getAllActiveEmployees();

    /**
     * Liefert auch inaktive Employees zurück, wenn deren exitdate >= selectedYearMonth ist.
     * Wird für die GUI benötigt.
     * Wenn nur aktive Employees geliefert werden würden, dann könnte man die Zeiten von dem Employee
     * nicht mehr kontrollieren, oder die Historie sehen, sobald der Mitarbeiter nicht mehr in ZEP existiert.
     *
     * @param selectedYearMonth ausgewählter Monat (von der GUI)
     * @return Aktive Employees und inaktive Employees, die aber lt. den Kriterien von oben noch angezeigt werden sollen
     */
    List<Employee> getAllEmployeesConsideringExitDate(YearMonth selectedYearMonth);

    void updateEmployeeReleaseDate(final String userId, final String releaseDate);

    List<String> updateEmployeesReleaseDate(List<Employee> employees);
}
