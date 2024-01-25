package com.gepardec.mega.zep.rest.service;

import com.gepardec.mega.domain.utils.DateUtils;
import com.gepardec.mega.zep.rest.client.ZepProjectRestClient;
import com.gepardec.mega.zep.rest.entity.ZepEmployee;
import com.gepardec.mega.zep.rest.entity.ZepProject;
import com.gepardec.mega.zep.rest.entity.ZepProjectEmployee;
import com.gepardec.mega.zep.util.ZepRestUtil;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.rest.client.inject.RestClient;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@ApplicationScoped
public class ProjectService {

    @RestClient
    ZepProjectRestClient zepProjectRestClient;

    public List<ZepProject> getProjectsForMonthYear(LocalDate monthYear) {
        LocalDate firstOfMonth = DateUtils.getFirstDayOfMonth(monthYear.getYear(), monthYear.getMonthValue());
        LocalDate lastOfMonth = DateUtils.getLastDayOfMonth(monthYear.getYear(), monthYear.getMonthValue());

        List<ZepProject> projects = getPaginatedProjects(firstOfMonth, lastOfMonth);
        projects.forEach(this::setProjectEmployees);
        return projects;
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

    private List<ZepProject> getPaginatedProjects(LocalDate firstOfMonth, LocalDate lastOfMonth) {
        List<ZepProject> projects = new ArrayList<>();
        int page = 1;
        fillWithProjects(projects, firstOfMonth, lastOfMonth, page);
        return projects;
    }

    void fillWithProjects(List<ZepProject> projects, LocalDate firstOfMonth, LocalDate lastOfMonth, int page) {
        String next = "";
        ZepProject[] projectsArr = {};
        try (Response resp = zepProjectRestClient.getProjectByStartEnd(firstOfMonth, lastOfMonth, page)) {
            String output = resp.readEntity(String.class);
            projectsArr = (ZepProject[]) ZepRestUtil.parseJson(output, "/data", ZepProject[].class);
            next = (String) ZepRestUtil.parseJson(output, "/links/next", String.class);
        }

        projects.addAll(List.of(projectsArr));
        if (next != null) {
            fillWithProjects(projects, firstOfMonth, lastOfMonth, page + 1);
        }
    }
}
