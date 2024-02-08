package com.gepardec.mega.zep.rest.service;

import com.gepardec.mega.zep.rest.client.ZepEmployeeRestClient;
import com.gepardec.mega.zep.rest.entity.ZepEmploymentPeriod;
import com.gepardec.mega.helper.ResourceFileService;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.mockito.InjectMock;
import jakarta.inject.Inject;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.fasterxml.jackson.databind.type.LogicalType.DateTime;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@QuarkusTest
public class EmploymentPeriodServiceTest {

    @RestClient
    @InjectMock
    ZepEmployeeRestClient zepEmployeeRestClient;

    @Inject
    EmploymentPeriodService employmentPeriodService;

    @Inject
    ResourceFileService resourceFileService;

    @BeforeEach
    public void init() {
        resourceFileService.getSingleFile("/employmentPeriods/001-duser.json").ifPresent(json -> {
            Response response = Response.ok().entity(json).build();
            when(zepEmployeeRestClient.getEmploymentPeriodByUserName(eq("001-duser"), eq(1))).thenReturn(response);
        });
    }

    @Test
    public void getEmploymentPeriod_thenReturnCorrectZepEmploymentPeriodObject() {
        ZepEmploymentPeriod employmentPeriod1 = ZepEmploymentPeriod.builder()
                .id(23)
                .employeeId("001-duser")
                .startDate(LocalDateTime.of(2017, 8, 7, 0,0,0))
                .endDate(LocalDateTime.of(2017, 9, 8, 0,0,0))
                .note("Praktikant")
                .beginningOfYear(null)
                .annualLeaveEntitlement(null)
                .periodHolidayEntitlement(2)
                .isHolidayPerYear(false)
                .dayAbsentInHours(null)
                .created("2017-07-26T13:39:22.000000Z")
                .modified("2020-11-21T08:35:23.000000Z")
                .build();
        ZepEmploymentPeriod employmentPeriod2 = ZepEmploymentPeriod.builder()
                .id(42)
                .employeeId("001-duser")
                .startDate(LocalDateTime.of(2019, 1, 11, 0,0,0))
                .endDate(LocalDateTime.of(2019, 12, 31, 0,0,0))
                .note(null)
                .beginningOfYear(null)
                .annualLeaveEntitlement(null)
                .periodHolidayEntitlement(25)
                .isHolidayPerYear(false)
                .dayAbsentInHours(null)
                .created("2018-12-27T17:18:38.000000Z")
                .modified("2020-11-21T08:35:23.000000Z")
                .build();
        ZepEmploymentPeriod employmentPeriod3 = ZepEmploymentPeriod.builder()
                .id(129)
                .employeeId("001-duser")
                .startDate(LocalDateTime.of(2020, 1, 1, 0,0,0))
                .endDate(null)
                .note(null)
                .beginningOfYear(null)
                .annualLeaveEntitlement(0.0)
                .periodHolidayEntitlement(0)
                .isHolidayPerYear(true)
                .dayAbsentInHours(0.0)
                .created("2020-11-21T08:35:23.000000Z")
                .modified("2024-01-18T12:56:26.000000Z")
                .build();

        var zepEmploymentPeriods = List.of(employmentPeriod1, employmentPeriod2, employmentPeriod3);
        var zepEmploymentPeriodsActual = employmentPeriodService.getZepEmploymentPeriodsByEmployeeName("001-duser");

        assertThat(zepEmploymentPeriodsActual).usingRecursiveComparison().isEqualTo(zepEmploymentPeriods);
    }



}
