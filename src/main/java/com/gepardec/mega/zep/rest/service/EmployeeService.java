package com.gepardec.mega.zep.rest.service;

import com.gepardec.mega.zep.ZepServiceException;
import com.gepardec.mega.zep.rest.client.ZepEmployeeRestClient;
import com.gepardec.mega.zep.rest.entity.ZepEmployee;
import com.gepardec.mega.zep.util.Paginator;
import com.gepardec.mega.zep.util.ZepRestUtil;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.slf4j.Logger;
import org.slf4j.event.Level;

import java.util.List;
import java.util.Optional;

@ApplicationScoped
public class EmployeeService{

    @RestClient
    ZepEmployeeRestClient zepEmployeeRestClient;

    @Inject
    Logger logger;

    @Inject
    Paginator paginator;

    public Optional<ZepEmployee> getZepEmployeeByUsername(String name) {
        try (Response resp = zepEmployeeRestClient.getByUsername(name)) {
            String output = resp.readEntity(String.class);
            try {
                return ZepRestUtil.parseJson(output, "/data", ZepEmployee.class);
            } catch (ZepServiceException e) {
                logger.warn("Error retrieving employee + \"%s\" from ZEP: No /data field in response"
                                .formatted(name), e);
            }

            return Optional.empty();
        }
    }
    public Optional<ZepEmployee> getZepEmployeeByPersonalNumber(String personalNumber) {
        List<String> params = List.of(personalNumber);
        try (Response resp = zepEmployeeRestClient.getByPersonalNumber(personalNumber)) {
            String output = resp.readEntity(String.class);
            Optional<ZepEmployee[]> employees = Optional.empty();
            try {
                employees = ZepRestUtil.parseJson(output, "/data", ZepEmployee[].class);
            } catch (ZepServiceException e) {
                logger.warn("Error retrieving employee + \"%s\" from ZEP: No /data field in response"
                                .formatted(personalNumber), e);
            }

            return employees.map(zepEmployees -> zepEmployees[0]);

        }
    }

    public List<ZepEmployee> getZepEmployees() {
        return paginator.retrieveAll(
                (page) -> zepEmployeeRestClient.getAllEmployeesOfPage(page),
                ZepEmployee.class);
    }

}
