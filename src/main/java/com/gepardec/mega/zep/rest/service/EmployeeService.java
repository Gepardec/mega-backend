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
import com.gepardec.mega.zep.ZepServiceException;
import com.gepardec.mega.zep.rest.client.ZepEmployeeRestClient;
import com.gepardec.mega.zep.rest.entity.ZepEmployee;
import com.gepardec.mega.zep.rest.entity.ZepEmploymentPeriod;
import com.gepardec.mega.zep.rest.entity.ZepRegularWorkingTimes;
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
    ZepEmployeeRestClient zepEmployeeRestClient;

    @Inject
    EmploymentPeriodService employmentPeriodService;

    public ZepEmployee getZepEmployeeById(String id) {
        try (Response resp = zepEmployeeRestClient.getById(id)) {
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

    public ZepRegularWorkingTimes getRegularWorkingTimesByUsername(String username) {
        try (Response resp = zepEmployeeRestClient.getRegularWorkingTimesByUsername(username)) {

            if (resp.getStatus() == 404) {
                throw new ZepServiceException("User not found");
            }
            String responseBodyAsString = resp.readEntity(String.class);
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode jsonNode = objectMapper.readTree(responseBodyAsString);

            ZepRegularWorkingTimes workingTimes;
            if ((workingTimes = objectMapper.treeToValue(jsonNode.get("data"), ZepRegularWorkingTimes[].class)[0]) == null) {
                throw new ZepServiceException("No Data");
            }
            return workingTimes;
        } catch (JsonProcessingException e) {
            throw new ZepServiceException("Error parsing JSON", e);
        }
    }


}
