package com.gepardec.mega.zep.rest.service;

import com.gepardec.mega.zep.ZepServiceException;
import com.gepardec.mega.zep.rest.client.ZepEmployeeRestClient;
import com.gepardec.mega.zep.rest.entity.ZepEmployee;
import com.gepardec.mega.zep.util.ResponseParser;
import com.gepardec.mega.zep.util.JsonUtil;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.slf4j.Logger;

import java.util.List;
import java.util.Optional;

@ApplicationScoped
public class EmployeeService{

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
            logger.warn("Error retrieving employee + \"%s\" from ZEP: No /data field in response"
                            .formatted(name), e);
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
                logger.warn("Error retrieving employee + \"%s\" from ZEP: No /data field in response"
                                .formatted(personalNumber), e);
            }

            return Optional.empty();

    }

    public List<ZepEmployee> getZepEmployees() {
        return responseParser.retrieveAll(
                (page) -> zepEmployeeRestClient.getAllEmployeesOfPage(page),
                ZepEmployee.class);
    }

}
