package com.gepardec.mega.zep.rest.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gepardec.mega.domain.model.AbsenceTime;
import com.gepardec.mega.domain.model.Employee;
import com.gepardec.mega.domain.model.Project;
import com.gepardec.mega.domain.model.ProjectTime;
import com.gepardec.mega.domain.model.monthlyreport.ProjectEntry;
import com.gepardec.mega.zep.ZepService;
import com.gepardec.mega.zep.rest.client.ZepEmployeeRestClient;
import com.gepardec.mega.zep.rest.entity.ZepEmployee;
import com.gepardec.mega.zep.rest.client.ZepEmployeeRestService;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.rest.client.inject.RestClient;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@ApplicationScoped
public class ZepServiceRestImpl implements ZepService {

    @RestClient
    ZepEmployeeRestClient zepEmployeeRestService;

    public ZepEmployee getZepEmployeeById(String username) {
        try (Response resp = zepEmployeeRestService.getByUsername(username)) {
            String output = resp.readEntity(String.class);
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode jsonNode = objectMapper.readTree(output);
            return objectMapper.treeToValue(jsonNode.get("data"), ZepEmployee[].class)[0];
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Employee getEmployee(String userId) {
        return null;
    }

    @Override
    public List<Employee> getEmployees() {
        return null;
    }

    @Override
    public void updateEmployeesReleaseDate(String userId, String releaseDate) {

    }

    @Override
    public List<ProjectEntry> getProjectTimes(Employee employee, LocalDate date) {
        return null;
    }

    @Override
    public List<ProjectTime> getProjectTimesForEmployeePerProject(String project, LocalDate curDate) {
        return null;
    }

    @Override
    public List<Project> getProjectsForMonthYear(LocalDate monthYear) {
        return null;
    }

    @Override
    public Optional<Project> getProjectByName(String projectName, LocalDate monthYear) {
        return Optional.empty();
    }

    @Override
    public List<AbsenceTime> getAbsenceForEmployee(Employee employee, LocalDate date) {
        return null;
    }

    @Override
    public List<ProjectTime> getBillableForEmployee(Employee employee, LocalDate date) {
        return null;
    }
}
