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

import javax.swing.text.html.Option;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@ApplicationScoped
public class EmployeeService{

    @RestClient
    ZepEmployeeRestClient zepEmployeeRestClient;

    @Inject
    EmploymentPeriodService employmentPeriodService;

    public Optional<ZepEmployee> getZepEmployeeByUsername(String name) {
        try (Response resp = zepEmployeeRestClient.getByUsername(name)) {
            String output = resp.readEntity(String.class);
            return ZepRestUtil.parseJson(output, "/data", ZepEmployee.class);

        }
    }
    public Optional<ZepEmployee> getZepEmployeeByPersonalNumber(String personalNumber) {
        List<String> params = List.of(personalNumber);
        try (Response resp = zepEmployeeRestClient.getByPersonalNumber(params)) {
            String output = resp.readEntity(String.class);
            Optional<ZepEmployee[]> employees = ZepRestUtil.parseJson(output, "/data", ZepEmployee[].class);

            return employees.map(zepEmployees -> zepEmployees[0]);

        }
    }

    public List<ZepEmployee> getZepEmployees() {
        return Paginator.retrieveAll(
                (page) -> zepEmployeeRestClient.getAllEmployeesOfPage(page),
                ZepEmployee.class);
    }

}
