package com.gepardec.mega.personio.employees;

import com.gepardec.mega.domain.model.PersonioEmployee;

import java.util.Optional;

public interface PersonioEmployeesService {

    Optional<PersonioEmployee> getPersonioEmployeeByEmail(String email);

}
