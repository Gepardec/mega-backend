package com.gepardec.mega.personio.employees;

import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.rest.client.inject.RestClient;

@RequestScoped
public class PersonioEmployeesServiceImpl implements PersonioEmployeesService {

    @Inject
    @RestClient
    PersonioEmployeesClient personioEmployeesClient;

    @Override
    public double getVacationDayBalance(String email) {
        var employeesResponse = personioEmployeesClient.getByEmail(email);
        if (employeesResponse.isSuccess() && employeesResponse.getData().size() == 1) {
            return employeesResponse.getData().get(0).getAttributes().getVacationDayBalance().getValue();
        }
        return 0;
    }
}
