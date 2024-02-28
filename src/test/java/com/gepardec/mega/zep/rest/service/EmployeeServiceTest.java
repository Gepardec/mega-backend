package com.gepardec.mega.zep.rest.service;

import com.fasterxml.jackson.databind.util.ArrayBuilders;
import com.gepardec.mega.helper.ResourceFileService;
import com.gepardec.mega.zep.rest.client.ZepEmployeeRestClient;
import com.gepardec.mega.zep.rest.entity.*;
import com.gepardec.mega.helper.ResourceFileService;
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

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.IntStream;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.ArgumentMatchers.*;
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

    @Inject
    ResourceFileService resourceFileService;

    @BeforeEach
    public void init() {
        resourceFileService.getSingleFile("/user007.json").ifPresent(json -> {
            Response response = Response.ok().entity(json).build();
            when(zepEmployeeRestClient.getByPersonalNumber("007")).thenReturn(response);
        });

        List<String> responseJson = resourceFileService.getDirContents("/users");

        IntStream.range(0, responseJson.size()).forEach(
                i -> {
                    when(zepEmployeeRestClient
                            .getAllEmployeesOfPage(eq(i + 1)))
                            .thenReturn(Response.ok().entity(responseJson.get(i)).build());
                }
        );
    }

    @Test
    public void getEmployeeJson_thenReturnZepEmployee() {

        List<ZepEmploymentPeriod> zepEmploymentPeriods = List.of(
                ZepEmploymentPeriod.builder()
                        .employeeId("007")
                        .build(),
                ZepEmploymentPeriod.builder()
                        .employeeId("007")
                        .build());
        when(employmentPeriodService.getZepEmploymentPeriodsByEmployeeName(anyString())).thenReturn(zepEmploymentPeriods);

        ZepEmployee referenceEmployee = ZepEmployee.builder()
                .username("007-jbond")
                .firstname("James")
                .lastname("Bond")
                .personalNumber("007")
                .street("Herzgasse 9")
                .zip("1100")
                .city("Wien")
                .country("Austria")
                .abbreviation("JBO")
                .salutation(ZepSalutation.builder()
                        .id("Herr")
                        .name("Sir")
                        .build()
                )
                .title("BSc")
                .email("james.bond@gepardec.com")
                .phone("+4300707")
                .mobile("+43007")
                .fax("007")
                .privatePhone("+39 007007")
                .birthdate("2007-07-07")
                .iban("AT07 0707 0707 0707 0707")
                .bic("JBANK3")
                .accountNo(7)
                .bankName("James Bank")
                .bankCode("007")
                .currency("Bonds")
                .releaseDate(LocalDate.of(2022,2, 28))
                .vat(20.0)
                .priceGroup("03")
                .employment(ZepEmployment.builder()
                        .id(0)
                        .name("Employee")
                        .build()
                )
                .rights(ZepRights.builder()
                    .id(7)
                    .name("bond")
                    .build()
                )
                .departmentId(7)
                .language(ZepLanguage.builder()
                        .id("en")
                        .name("English")
                        .build()
                )
                .personioId(7)
                .costBearer(null)
                .taxId(null)
                .created(LocalDateTime.of(2023, 5, 24, 11, 27,29))
                .modified(LocalDateTime.of(2024, 1, 10, 6, 46,37))
                .creditorNumber(null)
                .categories(new ArrayList<>())
                .absencesCount(7)
                .build();


                Optional<ZepEmployee> zepEmployee = employeeService.getZepEmployeeByPersonalNumber("007");

                assertThat(zepEmployee.get()).usingRecursiveComparison().isEqualTo(referenceEmployee);
    }


    @Test
    public void getZepEmployees_whenEmployeesList() {
        var periods000 = List.of(
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
                        .build());
        var periods001 = List.of(
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
                        .build());
        var periods007 = List.of(
                ZepEmploymentPeriod.builder()
                        .id(43)
                        .employeeId("007-jbond")
                        .startDate(LocalDateTime.of(2020, 8, 11, 0, 0, 0))
                        .note("Privatdetektiv")
                        .build());

        List<String> employeeNames = List.of("000-duser", "001-tuser", "007-jbond");
        when(employmentPeriodService.getZepEmploymentPeriodsByEmployeeName(employeeNames.get(0))).thenReturn(periods000);
        when(employmentPeriodService.getZepEmploymentPeriodsByEmployeeName(employeeNames.get(1))).thenReturn(periods001);
        when(employmentPeriodService.getZepEmploymentPeriodsByEmployeeName(employeeNames.get(2))).thenReturn(periods007);

        List<ZepEmployee> employees = employeeService.getZepEmployees();

        employees.forEach(employee-> assertThat(employeeNames.contains(employee.username())).isTrue());
    }




}
