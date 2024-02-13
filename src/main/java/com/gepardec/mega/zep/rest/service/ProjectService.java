package com.gepardec.mega.zep.rest.service;

import com.gepardec.mega.domain.utils.DateUtils;
import com.gepardec.mega.zep.ZepServiceException;
import com.gepardec.mega.zep.rest.client.ZepProjectRestClient;
import com.gepardec.mega.zep.rest.entity.ZepEmployee;
import com.gepardec.mega.zep.rest.entity.ZepProject;
import com.gepardec.mega.zep.rest.entity.ZepProjectEmployee;
import com.gepardec.mega.zep.util.Paginator;
import com.gepardec.mega.zep.util.ZepRestUtil;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.slf4j.Logger;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@ApplicationScoped
public class ProjectService {

    @RestClient
    ZepProjectRestClient zepProjectRestClient;

    @Inject
    Logger logger;

    public List<ZepProject> getProjectsForMonthYear(LocalDate monthYear) {
        try {
            List<ZepProject> projects = Paginator.retrieveAll(
                    page -> zepProjectRestClient.getProjects(page),
                    ZepProject.class
            );
            return this.filterProjectsByMonthYear(projects, monthYear);
        } catch (ZepServiceException e) {
            logger.warn("Error retrieving projects for month + \"%s\" from ZEP: No /data field in response"
                    .formatted(monthYear.format(DateTimeFormatter.ofPattern("MM-yyyy"))), e);
        }

        return List.of();
    }

    public Optional<ZepProject> getProjectByName(String name, LocalDate date) {
        try {
            return getProjectsForMonthYear(date).stream()
                    .filter(project -> project.getName().equals(name))
                    .findFirst();
        } catch (ZepServiceException e) {
            logger.warn("Error retrieving project + \"%s\" from ZEP: No /data field in response"
                    .formatted(name), e);
        }

        return Optional.empty();
    }


    public List<ZepProjectEmployee> getProjectEmployeesForId(int projectId) {
        try {
            return Paginator.retrieveAll(
                    page -> zepProjectRestClient.getProjectEmployees(projectId, page),
                    ZepProjectEmployee.class
            );
        } catch (ZepServiceException e) {
            logger.warn("Error retrieving project employees for project + \"%d\" from ZEP: No /data field in response"
                    .formatted(projectId), e);
        }

        return List.of();
    }

    private List<ZepProject> filterProjectsByMonthYear(List<ZepProject> projects, LocalDate monthYear) {
        LocalDate firstOfMonth = DateUtils.getFirstDayOfMonth(monthYear.getYear(), monthYear.getMonthValue());
        LocalDate lastOfMonth = DateUtils.getLastDayOfMonth(monthYear.getYear(), monthYear.getMonthValue());

        return projects.stream()
                .filter(project -> !(project.getStartDate() == null))
                .filter(project -> {
                    if (project.getEndDate() != null) {
                        if (project.getEndDate().isBefore(firstOfMonth.atStartOfDay())) {
                            return false;
                        }
                    }
                    return  true;
                })
                .filter(project -> !(project.getStartDate().isAfter(lastOfMonth.atTime(23, 59, 59))))
                .collect(Collectors.toList());

    }

}
