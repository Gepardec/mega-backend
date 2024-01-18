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
import com.gepardec.mega.zep.rest.entity.ZepEmploymentPeriod;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.rest.client.inject.RestClient;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@ApplicationScoped
public class EmployeeService{

    @RestClient
    ZepEmployeeRestClient zepEmployeeRestService;

    @Inject
    EmploymentPeriodService employmentPeriodService;

    public ZepEmployee getZepEmployeeById(String id) {
        try (Response resp = zepEmployeeRestService.getById(id)) {
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
}
