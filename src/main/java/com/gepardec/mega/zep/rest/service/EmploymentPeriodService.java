package com.gepardec.mega.zep.rest.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.deser.JSR310DateTimeDeserializerBase;
import com.gepardec.mega.zep.rest.client.ZepEmployeeRestClient;
import com.gepardec.mega.zep.rest.entity.ZepEmployee;
import com.gepardec.mega.zep.rest.entity.ZepEmploymentPeriod;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.rest.client.inject.RestClient;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@ApplicationScoped
public class EmploymentPeriodService {

    @RestClient
    ZepEmployeeRestClient zepEmployeeRestService;

    //TODO: Pagination handling
    public ZepEmploymentPeriod[] getZepEmploymentPeriodsByEmployeeName(String employeeName) {
        try (Response resp = zepEmployeeRestService.getEmploymentPeriodByUserName(employeeName)) {
            String output = resp.readEntity(String.class);
            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.findAndRegisterModules();
            JsonNode jsonNode = objectMapper.readTree(output);
            return objectMapper.treeToValue(jsonNode.get("data"), ZepEmploymentPeriod[].class);

        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
}