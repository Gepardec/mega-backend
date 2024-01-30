package com.gepardec.mega.zep.rest.service;

import com.gepardec.mega.zep.rest.client.ZepEmployeeRestClient;
import com.gepardec.mega.zep.rest.entity.ZepEmploymentPeriod;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.mockito.InjectMock;
import jakarta.inject.Inject;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.rest.client.inject.RestClient;
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
import static org.mockito.Mockito.when;

@QuarkusTest
public class EmploymentPeriodServiceTest {

    @RestClient
    @InjectMock
    ZepEmployeeRestClient zepEmployeeRestClient;

    @Inject
    EmploymentPeriodService employmentPeriodService;

//    @Test
//    public void employmentPeriodFromUser() {
//        String[] referenceEndDatesArr = {
//                "2017-09-08T00:00:00.000000Z",
//                "2019-12-31T00:00:00.000000Z",
//                "2024-01-17T00:00:00.000000Z"
//        };
//
//        List<LocalDateTime> referenceEndDates = Arrays.stream(referenceEndDatesArr)
//                .map(dateTimeString -> LocalDateTime.parse(dateTimeString, DateTimeFormatter.ISO_DATE_TIME))
//                .sorted()
//                .collect(Collectors.toList());
//
//        ZepEmploymentPeriod[] employmentPeriods = employmentPeriodService.getZepEmploymentPeriodsByEmployeeName("000-duser");
//        Stream<LocalDateTime> endDateTimes = Arrays.stream(employmentPeriods)
//                .map(ZepEmploymentPeriod::getEndDate)
//                .sorted();
//
//        assertThat(endDateTimes.allMatch(referenceEndDates::contains)).isTrue();
//    }

    @Test
    public void getEmploymentPeriod_thenReturnCorrectZepEmploymentPeriodObject() {
        String responseBody = "{\n" +
                "  \"data\": [\n" +
                "    {\n" +
                "      \"id\": 23,\n" +
                "      \"employee_id\": \"000-duser\",\n" +
                "      \"start_date\": \"2017-08-07T00:00:00.000000Z\",\n" +
                "      \"end_date\": \"2017-09-08T00:00:00.000000Z\",\n" +
                "      \"note\": \"Praktikant\",\n" +
                "      \"beginning_of_year\": null,\n" +
                "      \"annual_leave_entitlement\": null,\n" +
                "      \"period_holiday_entitlement\": 2,\n" +
                "      \"is_holiday_per_year\": false,\n" +
                "      \"day_absent_in_hours\": null,\n" +
                "      \"created\": \"2017-07-26T13:39:22.000000Z\",\n" +
                "      \"modified\": \"2020-11-21T08:35:23.000000Z\"\n" +
                "    },\n" +
                "    {\n" +
                "      \"id\": 42,\n" +
                "      \"employee_id\": \"000-duser\",\n" +
                "      \"start_date\": \"2019-01-11T00:00:00.000000Z\",\n" +
                "      \"end_date\": \"2019-12-31T00:00:00.000000Z\",\n" +
                "      \"note\": null,\n" +
                "      \"beginning_of_year\": null,\n" +
                "      \"annual_leave_entitlement\": null,\n" +
                "      \"period_holiday_entitlement\": 25,\n" +
                "      \"is_holiday_per_year\": false,\n" +
                "      \"day_absent_in_hours\": null,\n" +
                "      \"created\": \"2018-12-27T17:18:38.000000Z\",\n" +
                "      \"modified\": \"2020-11-21T08:35:23.000000Z\"\n" +
                "    },\n" +
                "    {\n" +
                "      \"id\": 129,\n" +
                "      \"employee_id\": \"000-duser\",\n" +
                "      \"start_date\": \"2020-01-01T00:00:00.000000Z\",\n" +
                "      \"end_date\": null,\n" +
                "      \"note\": null,\n" +
                "      \"beginning_of_year\": null,\n" +
                "      \"annual_leave_entitlement\": 0,\n" +
                "      \"period_holiday_entitlement\": 0,\n" +
                "      \"is_holiday_per_year\": true,\n" +
                "      \"day_absent_in_hours\": 0,\n" +
                "      \"created\": \"2020-11-21T08:35:23.000000Z\",\n" +
                "      \"modified\": \"2024-01-18T12:56:26.000000Z\"\n" +
                "    }\n" +
                "  ],\n" +
                "  \"links\": {\n" +
                "    \"first\": \"http:\\/\\/www.zep-online.de\\/zepgepardecservices_test\\/next\\/api\\/v1\\/employees\\/000-duser\\/employment-periods?page=1\",\n" +
                "    \"last\": \"http:\\/\\/www.zep-online.de\\/zepgepardecservices_test\\/next\\/api\\/v1\\/employees\\/000-duser\\/employment-periods?page=1\",\n" +
                "    \"prev\": null,\n" +
                "    \"next\": null\n" +
                "  },\n" +
                "  \"meta\": {\n" +
                "    \"current_page\": 1,\n" +
                "    \"from\": 1,\n" +
                "    \"last_page\": 1,\n" +
                "    \"links\": [\n" +
                "      {\n" +
                "        \"url\": null,\n" +
                "        \"label\": \"&laquo; Previous\",\n" +
                "        \"active\": false\n" +
                "      },\n" +
                "      {\n" +
                "        \"url\": \"http:\\/\\/www.zep-online.de\\/zepgepardecservices_test\\/next\\/api\\/v1\\/employees\\/000-duser\\/employment-periods?page=1\",\n" +
                "        \"label\": \"1\",\n" +
                "        \"active\": true\n" +
                "      },\n" +
                "      {\n" +
                "        \"url\": null,\n" +
                "        \"label\": \"Next &raquo;\",\n" +
                "        \"active\": false\n" +
                "      }\n" +
                "    ],\n" +
                "    \"path\": \"http:\\/\\/www.zep-online.de\\/zepgepardecservices_test\\/next\\/api\\/v1\\/employees\\/000-duser\\/employment-periods\",\n" +
                "    \"per_page\": 15,\n" +
                "    \"to\": 3,\n" +
                "    \"total\": 3\n" +
                "  }\n" +
                "}";

        Response response = Response.ok().entity(responseBody).build();
        when(zepEmployeeRestClient.getEmploymentPeriodByUserName(Mockito.anyString(), Mockito.anyInt())).thenReturn(response);

        ZepEmploymentPeriod employmentPeriod1 = ZepEmploymentPeriod.builder()
                .id(23)
                .employeeId("000-duser")
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
                .employeeId("000-duser")
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
                .employeeId("000-duser")
                .startDate(LocalDateTime.of(2020, 1, 1, 0,0,0))
                .endDate(null)
                .note(null)
                .beginningOfYear(null)
                .annualLeaveEntitlement(0.0)
                .periodHolidayEntitlement(0)
                .isHolidayPerYear(true)
                .dayAbsentInHours(0.0)
                .created("2020-11-21T08:35:23.000000Z")
                .modified("2124-01-18T12:56:26.000000Z")
                .build();

        var zepEmploymentPeriods = List.of(employmentPeriod1, employmentPeriod2, employmentPeriod3);
        var zepEmploymentPeriodsActual = employmentPeriodService.getZepEmploymentPeriodsByEmployeeName("000-duser");

        assertThat(zepEmploymentPeriodsActual).usingRecursiveComparison().isEqualTo(zepEmploymentPeriods);
    }



}
