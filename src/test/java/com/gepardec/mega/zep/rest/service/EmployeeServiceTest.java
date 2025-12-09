package com.gepardec.mega.zep.rest.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gepardec.mega.helper.ResourceFileService;
import com.gepardec.mega.zep.rest.client.ZepEmployeeRestClient;
import com.gepardec.mega.zep.rest.dto.ZepEmployee;
import com.gepardec.mega.zep.rest.dto.ZepEmploymentPeriod;
import com.gepardec.mega.zep.rest.dto.ZepResponse;
import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import io.smallrye.mutiny.Uni;
import jakarta.inject.Inject;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@QuarkusTest
class EmployeeServiceTest {

    @RestClient
    @InjectMock
    ZepEmployeeRestClient zepEmployeeRestClient;

    @InjectMock
    EmploymentPeriodService employmentPeriodService;

    @Inject
    EmployeeService employeeService;

    @Inject
    ResourceFileService resourceFileService;

    @Inject
    ObjectMapper objectMapper;

    @BeforeEach
    void init() {
        resourceFileService.getSingleFile("/user007.json").ifPresent(json -> {
            try {
                ZepResponse<ZepEmployee> response = objectMapper.readValue(json, new TypeReference<>() {
                });
                when(zepEmployeeRestClient.getByUsername("007")).thenReturn(Uni.createFrom().item(response));
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });

        List<String> responseJson = resourceFileService.getDirContents("/users");

        IntStream.range(0, responseJson.size()).forEach(
                i -> {
                    try {
                        ZepResponse<List<ZepEmployee>> response = objectMapper.readValue(responseJson.get(i), new TypeReference<>() {
                        });
                        when(zepEmployeeRestClient.getAllEmployeesOfPage(i + 1))
                                .thenReturn(Uni.createFrom().item(response));
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                }
        );
    }

    @Test
    void getZepEmployees_whenEmployeesList() {
        var periods000 = List.of(
                ZepEmploymentPeriod.builder()
                        .startDate(LocalDateTime.of(2017, 8, 7, 0, 0, 0))
                        .endDate(LocalDateTime.of(2017, 9, 8, 0, 0, 0))
                        .build(),
                ZepEmploymentPeriod.builder()
                        .startDate(LocalDateTime.of(2019, 1, 11, 0, 0, 0))
                        .build());
        var periods001 = List.of(
                ZepEmploymentPeriod.builder()
                        .startDate(LocalDateTime.of(2013, 8, 7, 0, 0, 0))
                        .endDate(LocalDateTime.of(2015, 9, 8, 0, 0, 0))
                        .build(),
                ZepEmploymentPeriod.builder()
                        .startDate(LocalDateTime.of(2019, 1, 11, 0, 0, 0))
                        .endDate(LocalDateTime.of(2019, 12, 31, 0, 0, 0))
                        .build());
        var periods007 = List.of(
                ZepEmploymentPeriod.builder()
                        .startDate(LocalDateTime.of(2020, 8, 11, 0, 0, 0))
                        .build());

        List<String> employeeNames = List.of("000-duser", "001-tuser", "007-jbond");
        when(employmentPeriodService.getZepEmploymentPeriodsByUsername(employeeNames.getFirst())).thenReturn(periods000);
        when(employmentPeriodService.getZepEmploymentPeriodsByUsername(employeeNames.get(1))).thenReturn(periods001);
        when(employmentPeriodService.getZepEmploymentPeriodsByUsername(employeeNames.get(2))).thenReturn(periods007);

        List<ZepEmployee> employees = employeeService.getZepEmployees();

        employees.forEach(employee -> assertThat(employeeNames).contains(employee.username()));
    }
}
