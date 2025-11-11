package com.gepardec.mega.zep.rest.service;

import com.gepardec.mega.zep.ZepServiceException;
import com.gepardec.mega.zep.rest.client.ZepEmployeeRestClient;
import com.gepardec.mega.zep.rest.dto.ZepEmployee;
import com.gepardec.mega.zep.util.ResponseParser;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.slf4j.Logger;

import java.util.List;
import java.util.Optional;

@ApplicationScoped
public class EmployeeService {

    @RestClient
    ZepEmployeeRestClient zepEmployeeRestClient;

    @Inject
    Logger logger;

    @Inject
    ResponseParser responseParser;

    public Optional<ZepEmployee> getZepEmployeeByUsername(String name) {
        try {
            return responseParser.retrieveSingle(zepEmployeeRestClient.getByUsername(name),
                    ZepEmployee.class);
        } catch (ZepServiceException e) {
            String message = "Error retrieving employee \"%s\" from ZEP: No /data field in response".formatted(name);
            logger.warn(message, e);
        }
        return Optional.empty();
    }

    public Optional<ZepEmployee> getZepEmployeeByPersonalNumber(String personalNumber) {
        try {
            return responseParser
                    .retrieveSingle(zepEmployeeRestClient.getByPersonalNumber(personalNumber),
                            ZepEmployee[].class)
                    .map(employees -> employees[0]);
        } catch (ZepServiceException e) {
            String message = "Error retrieving employee \"%s\" from ZEP: No /data field in response".formatted(personalNumber);
            logger.warn(message, e);
        }

        return Optional.empty();

    }

    public List<ZepEmployee> getZepEmployees() {
        return responseParser.retrieveAll(
                page -> zepEmployeeRestClient.getAllEmployeesOfPage(page),
                ZepEmployee.class);
    }

}
