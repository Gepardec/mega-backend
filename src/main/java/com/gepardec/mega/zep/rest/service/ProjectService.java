package com.gepardec.mega.zep.rest.service;

import com.gepardec.mega.domain.utils.DateUtils;
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

import java.time.LocalDate;
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

    public List<ZepProject> getProjectsForMonthYear(LocalDate monthYear) {
        LocalDate firstOfMonth = DateUtils.getFirstDayOfMonth(monthYear.getYear(), monthYear.getMonthValue());
        LocalDate lastOfMonth = DateUtils.getLastDayOfMonth(monthYear.getYear(), monthYear.getMonthValue());

        List<ZepProject> projects = Paginator.retrieveAll(
                page -> zepProjectRestClient.getProjects(page),
                ZepProject.class
        );
        var nullEndDate = projects.stream()
                .filter(project -> {
                    if (project.getEndDate() == null) {
                        return project.getStartDate().isBefore(lastOfMonth.atStartOfDay()) ||
                                project.getStartDate().isEqual(lastOfMonth.atStartOfDay());
                    }
                    return false;
                });
        var projectStream = projects.stream()
                .filter(project -> project.getEndDate() != null && project.getStartDate() != null)
                .filter(project -> (project.getEndDate().isAfter(firstOfMonth.atStartOfDay()) ||
                        project.getEndDate().isEqual(firstOfMonth.atStartOfDay())) &&
                        (project.getStartDate().isBefore(lastOfMonth.atStartOfDay()) ||
                        project.getStartDate().isEqual(lastOfMonth.atStartOfDay())))
                .peek(project -> System.out.println(project.getStartDate() + ";" + project.getEndDate()));

        return Stream.concat(nullEndDate, projectStream).collect(Collectors.toList());
    }

    public Optional<ZepProject> getProjectByName(String name, LocalDate date) {
        return getProjectsForMonthYear(date).stream()
                .filter(project -> project.getName().equals(name))
                .findFirst();
    }


    public List<ZepProjectEmployee> getProjectEmployeesForId(int projectId) {
        try (Response resp = zepProjectRestClient.getProjectEmployees(projectId)) {
            String output = resp.readEntity(String.class);
            System.out.println(projectId);
            System.out.println(output);
            Optional<ZepProjectEmployee[]> projectEmployees = ZepRestUtil.parseJson(output, "/data", ZepProjectEmployee[].class);
            return Arrays.asList(projectEmployees.orElse(new ZepProjectEmployee[0]));
        }
    }

}
