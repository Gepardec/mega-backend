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
import com.gepardec.mega.zep.util.ZepRestUtil;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.rest.client.inject.RestClient;

import java.util.ArrayList;
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

    public List<ZepEmployee> getZepEmployees() {
        List<ZepEmployee> employees = this.getPaginatedEmployees();
        employees.forEach(this::setEmploymentPeriods);

        return employees;
    }

    private void setEmploymentPeriods(ZepEmployee employee) {
        String username = employee.getUsername();
        ZepEmploymentPeriod[] periods = employmentPeriodService.getZepEmploymentPeriodsByEmployeeName(username);
        employee.setEmploymentPeriods(periods);
    }

    private List<ZepEmployee> getPaginatedEmployees() {
        List<ZepEmployee> employees = new ArrayList<>();
        int page = 1;
        fillWithEmployees(employees, page);
        return employees;
    }

    void fillWithEmployees(List<ZepEmployee> employees, int page) {
        String next = "";
        ZepEmployee[] employeesArr = {};
        try (Response resp = zepEmployeeRestClient.getAllEmployeesOfPage(page)) {
            String output = resp.readEntity(String.class);
            employeesArr = (ZepEmployee[]) ZepRestUtil.parseJson(output, "/data", ZepEmployee[].class);
            next = (String) ZepRestUtil.parseJson(output, "/links/next", String.class);
        }

        employees.addAll(List.of(employeesArr));
        if (next != null) {
            fillWithEmployees(employees, page + 1);
        }
    }




}
