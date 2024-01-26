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

@ApplicationScoped
public class ProjectService {

    @RestClient
    ZepProjectRestClient zepProjectRestClient;

    public List<ZepProject> getProjectsForMonthYear(LocalDate monthYear) {
        LocalDate firstOfMonth = DateUtils.getFirstDayOfMonth(monthYear.getYear(), monthYear.getMonthValue());
        LocalDate lastOfMonth = DateUtils.getLastDayOfMonth(monthYear.getYear(), monthYear.getMonthValue());

        List<ZepProject> projects = Paginator.retrieveAll(
                page -> zepProjectRestClient.getProjectByStartEnd(firstOfMonth, lastOfMonth, page),
                ZepProject.class
        );
        projects.forEach(this::setProjectEmployees);
        return projects;
    }

    public Optional<ZepProject> getProjectByName(String name) {
        Optional<ZepProject> optional =  Paginator.searchInAll(
                page -> zepProjectRestClient.getProjects(page),
                project -> project.getName().equals(name),
                ZepProject.class
        );
        optional.ifPresent(this::setProjectEmployees);
        return optional;
    }

    private void setProjectEmployees(ZepProject zepProject) {
        List<ZepProjectEmployee> employees = this.getProjectEmployeesForId(zepProject.getId());
        zepProject.setEmployees(employees);
    }

    List<ZepProjectEmployee> getProjectEmployeesForId(int projectId) {
        try (Response resp = zepProjectRestClient.getProjectEmployees(projectId)) {
            String output = resp.readEntity(String.class);
            ZepProjectEmployee[] employees = (ZepProjectEmployee[]) ZepRestUtil.parseJson(output, "/data", ZepProjectEmployee[].class);
            return List.of(employees);
        }
    }

}
