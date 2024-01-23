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
import io.smallrye.mutiny.Multi;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.core.MultivaluedHashMap;
import jakarta.ws.rs.core.MultivaluedMap;
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

    public ZepEmployee getZepEmployeeByUsername(String name) {
        try (Response resp = zepEmployeeRestClient.getByUsername(name)) {
            String output = resp.readEntity(String.class);
            ZepEmployee employee = (ZepEmployee) ZepRestUtil.parseJson(output, "/data", ZepEmployee.class);

            ZepEmploymentPeriod[] employmentPeriods = employmentPeriodService.getZepEmploymentPeriodsByEmployeeName(name);
            employee.setEmploymentPeriods(employmentPeriods);

            return employee;
        }
    }
    public ZepEmployee getZepEmployeeByPersonalNumber(String personalNumber) {
        MultivaluedMap<String, String> multivaluedMap = new MultivaluedHashMap<>();
        multivaluedMap.addFirst("personal_number", personalNumber);
        try (Response resp = zepEmployeeRestClient.getByPersonalNumber(multivaluedMap)) {
            String output = resp.readEntity(String.class);
            ZepEmployee employee = ((ZepEmployee[]) ZepRestUtil.parseJson(output, "/data", ZepEmployee[].class))[0];

            String employeeName = employee.getUsername();
            ZepEmploymentPeriod[] employmentPeriods = employmentPeriodService.getZepEmploymentPeriodsByEmployeeName(employeeName);
            employee.setEmploymentPeriods(employmentPeriods);

            return employee;
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
    }String[] categories = new String[0];

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
