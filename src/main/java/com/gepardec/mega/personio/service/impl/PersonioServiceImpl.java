package com.gepardec.mega.personio.service.impl;

import com.gepardec.mega.personio.client.EmployeesClient;
import com.gepardec.mega.personio.model.EmployeesResponse;
import com.gepardec.mega.personio.service.api.PersonioService;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.rest.client.inject.RestClient;

@RequestScoped
public class PersonioServiceImpl implements PersonioService {

    @Inject
    @RestClient
    EmployeesClient employeesClient;

    @Override
    public double getVacationDayBalance(String email) {
        EmployeesResponse employeesResponse = null;
        try {
            employeesResponse = employeesClient.getByEmail(email);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        if (employeesResponse.isSuccess() && employeesResponse.getData().size() == 1) {
            return employeesResponse.getData().get(0).getAttributes().getVacationDayBalance().getValue();
        }
        return 0;
    }
}
