package com.gepardec.mega.zep.rest.service;

import com.gepardec.mega.zep.rest.client.ZepEmployeeRestClient;
import com.gepardec.mega.zep.rest.entity.ZepEmployee;
import com.gepardec.mega.zep.rest.entity.ZepEmploymentPeriod;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.mockito.InjectMock;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import jakarta.inject.Inject;
import org.locationtech.jts.io.InStream;
import org.mockito.Mockito;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.IntStream;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@QuarkusTest
public class EmployeeServiceTest {

    @RestClient
    @InjectMock
    ZepEmployeeRestClient zepEmployeeRestClient;

    @InjectMock
    EmploymentPeriodService employmentPeriodService;

    @Inject
    EmployeeService employeeService;


    @Test
    public void fillWithEmployees() {
        String[] pages = {
                "{\"data\": [{\"personal_number\": 0}], " +
                    "\"links\": {\"next\": \"http:\\/\\/www.zep-online.de\\/instance\\/next\\/api\\/v1\\/employees?page=2\"" +
                        "}" +
                "}",
                "{\"data\": [{\"personal_number\": 1}]," +
                        "\"links\": {\"next\": " +
                        "\"http:\\/\\/www.zep-online.de\\/instance\\/next\\/api\\/v1\\/employees?page=3\"" +
                        "}" +
                "}",
                "{\"data\": [{\"personal_number\": 2}]," +
                 "\"links\": {\"next\": null}" +
                "}",
        };
        for(int i = 0; i < 3; i++) {
            Response response = Response.ok().entity(pages[i]).build();
            when(zepEmployeeRestClient.getAllEmployeesOfPage(i + 1)).thenReturn(response);
        }

        List<ZepEmployee> zepEmployees = new ArrayList<>();
        employeeService.fillWithEmployees(zepEmployees, 1);

        IntStream.range(0,3)
                .forEach(i -> assertThat(zepEmployees.get(i).getPersonalNumber()).isEqualTo("" + i));

    }

    @Test
    public void getZepEmployees_whenEmployeesList() {
        String[] employeePagesBody = {
                "{" +
                    "\"data\": [{\"username\": \"000-duser\" }], " +
                    "\"links\": {" +
                        "\"next\": \"http:\\/\\/www.zep-online.de\\/instance\\/next\\/api\\/v1\\/employees?page=2\"" +
                    "}" +
                "}",
                "{" +
                    "\"data\": [{\"username\": \"001-tuser\" }], " +
                    "\"links\": {" +
                        "\"next\": \"http:\\/\\/www.zep-online.de\\/instance\\/next\\/api\\/v1\\/employees?page=3\"" +
                    "}" +
                "}",
                "{" +
                    "\"data\": [{\"username\": \"007-jbond\" }], " +
                    "\"links\": {\"next\": null}" +
                "}",
        };

        IntStream.range(0, 3).forEach(i -> {
            Response responseEmployeePages = Response.ok().entity(employeePagesBody[i]).build();
            when(zepEmployeeRestClient.getAllEmployeesOfPage(i + 1)).thenReturn(responseEmployeePages);
        });

        ZepEmploymentPeriod[] periods000 = {
                ZepEmploymentPeriod.builder()
                        .id(23)
                        .employeeId("000-duser")
                        .startDate(LocalDateTime.of(2017, 8, 7, 0, 0, 0))
                        .endDate(LocalDateTime.of(2017, 9, 8, 0, 0, 0))
                        .note("Praktikant")
                        .build(),
                ZepEmploymentPeriod.builder()
                        .id(24)
                        .employeeId("000-duser")
                        .startDate(LocalDateTime.of(2019, 1, 11, 0, 0, 0))
                        .build(),
        };
        ZepEmploymentPeriod[] periods001 = {
                ZepEmploymentPeriod.builder()
                        .id(43)
                        .employeeId("001-tuser")
                        .startDate(LocalDateTime.of(2013, 8, 7, 0, 0, 0))
                        .endDate(LocalDateTime.of(2015, 9, 8, 0, 0, 0))
                        .note("Sommeraushilfe")
                        .build(),
                ZepEmploymentPeriod.builder()
                        .id(82)
                        .employeeId("001-tuser")
                        .startDate(LocalDateTime.of(2019, 1, 11, 0, 0, 0))
                        .endDate(LocalDateTime.of(2019, 12, 31, 0, 0, 0))
                        .build(),
        };
        ZepEmploymentPeriod[] periods007 = {
                ZepEmploymentPeriod.builder()
                        .id(43)
                        .employeeId("007-jbond")
                        .startDate(LocalDateTime.of(2020, 8, 11, 0, 0, 0))
                        .note("Privatdetektiv")
                        .build(),
        };

        List<String> employeeNames = List.of("000-duser", "001-tuser", "007-jbond");
        when(employmentPeriodService.getZepEmploymentPeriodsByEmployeeName(employeeNames.get(0))).thenReturn(periods000);
        when(employmentPeriodService.getZepEmploymentPeriodsByEmployeeName(employeeNames.get(1))).thenReturn(periods001);
        when(employmentPeriodService.getZepEmploymentPeriodsByEmployeeName(employeeNames.get(2))).thenReturn(periods007);

        List<ZepEmployee> employees = employeeService.getZepEmployees();

        Iterator<ZepEmploymentPeriod[]> employmentPeriodsIterator = List.of(periods000, periods001, periods007).iterator();

        employees.stream()
                .peek(employee-> assertThat(employeeNames.contains(employee.getUsername())).isTrue())
                .map(ZepEmployee::getEmploymentPeriods)
                .forEach(employmentPeriods -> {
                    assertThat(employmentPeriods).usingRecursiveComparison().isEqualTo(employmentPeriodsIterator.next());
                });
    }




}
