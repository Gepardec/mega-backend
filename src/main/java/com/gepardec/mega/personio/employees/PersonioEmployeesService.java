package com.gepardec.mega.personio.employees;

import com.gepardec.mega.domain.model.Employee;
import com.gepardec.mega.domain.model.PersonioEmployee;

import java.time.YearMonth;
import java.util.Optional;

public interface PersonioEmployeesService {

    Optional<PersonioEmployee> getPersonioEmployeeByEmail(String email);
    int getAvailableVacationDaysForEmployeeByEmail(String email);

}
