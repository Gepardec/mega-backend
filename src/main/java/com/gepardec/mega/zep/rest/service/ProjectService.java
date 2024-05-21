package com.gepardec.mega.zep.rest.service;

import com.gepardec.mega.zep.ZepServiceException;
import com.gepardec.mega.zep.rest.client.ZepProjectRestClient;
import com.gepardec.mega.zep.rest.dto.ZepProject;
import com.gepardec.mega.zep.rest.dto.ZepProjectEmployee;
import com.gepardec.mega.zep.util.ResponseParser;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.slf4j.Logger;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

@ApplicationScoped
public class ProjectService {

    @RestClient
    ZepProjectRestClient zepProjectRestClient;

    @Inject
    Logger logger;

    @Inject
    ResponseParser responseParser;

    public List<ZepProject> getProjectsForMonthYear(LocalDate date) {
        String startDate = date.withDayOfMonth(1).toString();
        String endDate = date.withDayOfMonth(date.lengthOfMonth()).toString();

        try {
            return responseParser.retrieveAll(
                    page -> zepProjectRestClient.getProjectByStartEnd(startDate,endDate, page),
                    ZepProject.class
            );
        } catch (ZepServiceException e) {
            logger.warn("Error retrieving projects for month + \"%s\" from ZEP: No /data field in response"
                    .formatted(date.format(DateTimeFormatter.ofPattern("MM-yyyy"))), e);
        }

        return List.of();
    }


    public Optional<ZepProject> getProjectByName(String name, LocalDate date) {
        String startDate = date.withDayOfMonth(1).toString();
        String endDate = date.withDayOfMonth(date.lengthOfMonth()).toString();
        try {
            return responseParser.retrieveSingle(
                    zepProjectRestClient.getProjectByName(startDate, endDate, name),
                    ZepProject[].class
            ).map(x -> x[0]);
        } catch (ZepServiceException e) {
            logger.warn("Error retrieving project + \"%s\" from ZEP: No /data field in response"
                    .formatted(name), e);
        }
        return Optional.empty();
    }

    public Optional<ZepProject> getProjectById(int projectId) {
        try {
            return responseParser.retrieveSingle(
                    zepProjectRestClient.getProjectById(projectId),
                    ZepProject.class
            );
        } catch (ZepServiceException e) {
            logger.warn("Error retrieving project + \"%d\" from ZEP: No /data field in response"
                    .formatted(projectId), e);
        }
        return Optional.empty();
    }


    public List<ZepProjectEmployee> getProjectEmployeesForId(int projectId) {
        try {
            return responseParser.retrieveAll(
                    page -> zepProjectRestClient.getProjectEmployees(projectId, page),
                    ZepProjectEmployee.class
            );
        } catch (ZepServiceException e) {
            logger.warn("Error retrieving project employees for project + \"%d\" from ZEP: No /data field in response"
                    .formatted(projectId), e);
        }

        return List.of();
    }




}
