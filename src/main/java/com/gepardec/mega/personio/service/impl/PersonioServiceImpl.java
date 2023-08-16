package com.gepardec.mega.personio.service.impl;

import com.gepardec.mega.personio.client.PersonioEmployeesClient;
import com.gepardec.mega.personio.model.EmployeesResponse;
import com.gepardec.mega.personio.service.api.PersonioService;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.rest.client.inject.RestClient;

@RequestScoped
public class PersonioServiceImpl implements PersonioService {

    @Inject
    @RestClient
    PersonioEmployeesClient personioEmployeesClient;

    @Override
    public double getVacationDayBalance(String email) {
        EmployeesResponse employeesResponse;
        try {
            employeesResponse = personioEmployeesClient.getByEmail(email);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        if (employeesResponse.isSuccess() && employeesResponse.getData().size() == 1) {
            return employeesResponse.getData().get(0).getAttributes().getVacationDayBalance().getValue();
        }
        return 0;
    }
}
