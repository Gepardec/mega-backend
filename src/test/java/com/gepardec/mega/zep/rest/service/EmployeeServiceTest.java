package com.gepardec.mega.zep.rest.service;

import com.gepardec.mega.helper.ResourceFileService;
import com.gepardec.mega.zep.rest.client.ZepEmployeeRestClient;
import com.gepardec.mega.zep.rest.dto.*;
import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import jakarta.inject.Inject;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.IntStream;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.ArgumentMatchers.*;
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

    @BeforeEach
    void init() {
        resourceFileService.getSingleFile("/user007.json").ifPresent(json -> {
            Response response = Response.ok().entity(json).build();
            when(zepEmployeeRestClient.getByPersonalNumber("007")).thenReturn(response);
        });

        List<String> responseJson = resourceFileService.getDirContents("/users");

        IntStream.range(0, responseJson.size()).forEach(
                i -> {
                    when(zepEmployeeRestClient
                            .getAllEmployeesOfPage(i + 1))
                            .thenReturn(Response.ok().entity(responseJson.get(i)).build());
                }
        );
    }

    @Test
    void getEmployeeJson_thenReturnZepEmployee() {

        List<ZepEmploymentPeriod> zepEmploymentPeriods = List.of(
                ZepEmploymentPeriod.builder()
                        .build(),
                ZepEmploymentPeriod.builder()
                        .build());
        when(employmentPeriodService.getZepEmploymentPeriodsByEmployeeName(anyString())).thenReturn(zepEmploymentPeriods);

        ZepEmployee referenceEmployee = ZepEmployee.builder()
                .username("007-jbond")
                .firstname("James")
                .lastname("Bond")
                .salutation(ZepSalutation.builder()
                        .name("Sir")
                        .build()
                )
                .title("BSc")
                .email("james.bond@gepardec.com")
                .releaseDate(LocalDate.of(2022,2, 28))
                .priceGroup("03")
                .language(ZepLanguage.builder()
                        .id("en")
                        .build()
                )
                .build();


                Optional<ZepEmployee> zepEmployee = employeeService.getZepEmployeeByPersonalNumber("007");

                assertThat(zepEmployee.get()).usingRecursiveComparison().isEqualTo(referenceEmployee);
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
        when(employmentPeriodService.getZepEmploymentPeriodsByEmployeeName(employeeNames.get(0))).thenReturn(periods000);
        when(employmentPeriodService.getZepEmploymentPeriodsByEmployeeName(employeeNames.get(1))).thenReturn(periods001);
        when(employmentPeriodService.getZepEmploymentPeriodsByEmployeeName(employeeNames.get(2))).thenReturn(periods007);

        List<ZepEmployee> employees = employeeService.getZepEmployees();

        employees.forEach(employee-> assertThat(employeeNames.contains(employee.username())).isTrue());
    }




}
