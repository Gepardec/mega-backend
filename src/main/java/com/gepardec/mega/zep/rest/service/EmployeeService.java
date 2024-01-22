package com.gepardec.mega.zep.rest.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gepardec.mega.domain.model.Employee;
import com.gepardec.mega.zep.ZepServiceException;
import com.gepardec.mega.zep.rest.client.ZepEmployeeRestClient;
import com.gepardec.mega.zep.rest.entity.ZepEmployee;
import com.gepardec.mega.zep.rest.entity.ZepEmploymentPeriod;
import com.gepardec.mega.zep.rest.entity.ZepRegularWorkingTimes;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.rest.client.inject.RestClient;

import java.util.List;

@ApplicationScoped
public class EmployeeService{

    @RestClient
    ZepEmployeeRestClient zepEmployeeRestClient;

    @Inject
    EmploymentPeriodService employmentPeriodService;

    public ZepEmployee getZepEmployeeByUsername(String id) {
        try (Response resp = zepEmployeeRestClient.getByUsername(id)) {
            String output = resp.readEntity(String.class);
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode jsonNode = objectMapper.readTree(output);
            ZepEmployee employee = objectMapper.treeToValue(jsonNode.get("data"), ZepEmployee[].class)[0];

            String name = employee.getUsername();
            ZepEmploymentPeriod[] employmentPeriods = employmentPeriodService.getZepEmploymentPeriodsByEmployeeName(name);
            employee.setEmploymentPeriods(employmentPeriods);

            return employee;
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    public ZepEmployee getZepEmployees(String id) {
        try (Response resp = zepEmployeeRestClient.getAllEmployeesOfPage(1)) {
            String output = resp.readEntity(String.class);
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode jsonNode = objectMapper.readTree(output);
            ZepEmployee employee = objectMapper.treeToValue(jsonNode.get("data"), ZepEmployee[].class)[0];

            String name = employee.getUsername();
            ZepEmploymentPeriod[] employmentPeriods = employmentPeriodService.getZepEmploymentPeriodsByEmployeeName(name);
            employee.setEmploymentPeriods(employmentPeriods);

            return employee;
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    public void fillWithEmployees(List<Employee> employees, int page) {
        String next = null;
        ZepEmployee[] employeesArr = null;
        try (Response resp = zepEmployeeRestClient.getAllEmployeesOfPage(page)) {
            String output = resp.readEntity(String.class);
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode jsonNode = objectMapper.readTree(output);
            employeesArr = objectMapper.treeToValue(jsonNode.get("data"), ZepEmployee[].class);
            next = objectMapper.treeToValue(jsonNode.get("data/links/next"), String.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

        if (next == null) {
            fillWithEmployees(employees, page + 1);
        }
    }




}
