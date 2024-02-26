package com.gepardec.mega.personio.employees;

public interface PersonioEmployeesService {
    double getVacationDayBalance(String email);

    String getGuildLead(String email);

    String getInternalProjectLead(String email);
}
