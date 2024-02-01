package com.gepardec.mega.zep.rest.service;

import com.gepardec.mega.zep.rest.client.ZepEmployeeRestClient;
import com.gepardec.mega.zep.rest.entity.ZepEmployee;
import com.gepardec.mega.zep.util.Paginator;
import com.gepardec.mega.zep.util.ZepRestUtil;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.rest.client.inject.RestClient;

import java.util.List;
import java.util.Optional;

@ApplicationScoped
public class EmployeeService{

    @RestClient
    ZepEmployeeRestClient zepEmployeeRestClient;

    @Inject
    EmploymentPeriodService employmentPeriodService;

    public Optional<ZepEmployee> getZepEmployeeByUsername(String name) {
        try (Response resp = zepEmployeeRestClient.getByUsername(name)) {
            String output = resp.readEntity(String.class);
            return ZepRestUtil.parseJson(output, "/data", ZepEmployee.class);

        }
    }
    public Optional<ZepEmployee> getZepEmployeeByPersonalNumber(String personalNumber) {
        List<String> params = List.of(personalNumber);
        try (Response resp = zepEmployeeRestClient.getByPersonalNumber(personalNumber)) {
            String output = resp.readEntity(String.class);
            Optional<ZepEmployee[]> employees = ZepRestUtil.parseJson(output, "/data", ZepEmployee[].class);

            return employees.map(zepEmployees -> zepEmployees[0]);

        }
    }

    public List<ZepEmployee> getZepEmployees() {
        return Paginator.retrieveAll(
                (page) -> zepEmployeeRestClient.getAllEmployeesOfPage(page),
                ZepEmployee.class);
    }

}
