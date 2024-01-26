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
import com.gepardec.mega.zep.util.Paginator;
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
        List<String> params = List.of(personalNumber);
        try (Response resp = zepEmployeeRestClient.getByPersonalNumber(params)) {
            String output = resp.readEntity(String.class);
            ZepEmployee employee = ((ZepEmployee[]) ZepRestUtil.parseJson(output, "/data", ZepEmployee[].class))[0];

            String employeeName = employee.getUsername();
            ZepEmploymentPeriod[] employmentPeriods = employmentPeriodService.getZepEmploymentPeriodsByEmployeeName(employeeName);
            employee.setEmploymentPeriods(employmentPeriods);

            return employee;
        }
    }

    public List<ZepEmployee> getZepEmployees() {
        List<ZepEmployee> employees = Paginator.retrieveAll(
                (page) -> zepEmployeeRestClient.getAllEmployeesOfPage(page),
                ZepEmployee.class);

        employees.forEach(this::setEmploymentPeriods);

        return employees;
    }

    private void setEmploymentPeriods(ZepEmployee employee) {
        String username = employee.getUsername();
        ZepEmploymentPeriod[] periods = employmentPeriodService.getZepEmploymentPeriodsByEmployeeName(username);
        employee.setEmploymentPeriods(periods);
    }


}
