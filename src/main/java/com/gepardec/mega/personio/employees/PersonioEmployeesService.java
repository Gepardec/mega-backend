package com.gepardec.mega.personio.employees;

import java.util.Optional;

public interface PersonioEmployeesService {

    Optional<PersonioEmployee> getPersonioEmployeeByEmail(String email);

}
