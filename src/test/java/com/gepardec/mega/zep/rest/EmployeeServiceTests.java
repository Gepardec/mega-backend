package com.gepardec.mega.zep.rest;

import com.gepardec.mega.application.configuration.ZepConfig;
import com.gepardec.mega.zep.ZepServiceException;
import com.gepardec.mega.zep.rest.client.ZepEmployeeRestClient;
import com.gepardec.mega.zep.rest.entity.ZepEmployee;

import com.gepardec.mega.zep.rest.entity.ZepRegularWorkingTimes;
import com.gepardec.mega.zep.rest.service.EmployeeService;
import com.gepardec.mega.zep.rest.service.EmploymentPeriodService;
import com.github.tomakehurst.wiremock.WireMockServer;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.mockito.InjectMock;
import jakarta.inject.Inject;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@QuarkusTest
public class EmployeeServiceTests {


    @InjectMock
    EmploymentPeriodService employmentPeriodService;

    @RestClient
    @InjectMock
    ZepEmployeeRestClient zepEmployeeRestClient;

    @Inject
    EmployeeService zepEmployeeService;


    @BeforeEach
    public void setup(){



    }
//    @Test
    public void testMock(){

//        Response mockedResponse = Mockito.mock(Response.class);
//        when(mockedResponse.readEntity(String.class)).thenReturn(responseBody);
//
//        ZepEmployeeRestClient mockedZepEmployeeRestClient = Mockito.mock(ZepEmployeeRestClient.class);
//        when(mockedZepEmployeeRestClient.getRegularWorkingTimesByUsername(Mockito.anyString())).thenReturn(mockedResponse);
//        zepEmployeeService.getZepEmployeeById("082-tmeindl");
    }


    @Test
    public void getEmployee() {
        ZepEmployee employee = zepEmployeeService.getZepEmployeeById("082-tmeindl");
        System.out.println(employee.getUsername());
    }

    @Test
    public void bearerToken_thenReturnHeaderString() {
        try (MockedStatic<ZepConfig> config = Mockito.mockStatic(ZepConfig.class)) {
            config.when(ZepConfig::getRestBearerToken).thenReturn("bearerToken");
            String token = ZepConfig.getRestBearerToken();
            assertThat(ZepEmployeeRestClient.getAuthHeaderValue()).isEqualTo("Bearer " + token);
        }
    }

    @Test
    public void getRegularWorkingTimesByUsername_receiveValidWorkingTime_then_returnValidZepWorkingTime(){





        String responseBody = "{\n" +
                "  \"data\": [\n" +
                "    {\n" +
                "      \"id\": 155,\n" +
                "      \"employee_id\": \"082-tmeindl\",\n" +
                "      \"start_date\": null,\n" +
                "      \"monday\": 8,\n" +
                "      \"tuesday\": 8,\n" +
                "      \"wednesday\": 8,\n" +
                "      \"thursday\": 8,\n" +
                "      \"friday\": 6.5,\n" +
                "      \"saturday\": null,\n" +
                "      \"sunday\": null,\n" +
                "      \"is_monthly\": null,\n" +
                "      \"monthly_hours\": null,\n" +
                "      \"max_hours_in_month\": null,\n" +
                "      \"max_hours_in_week\": null,\n" +
                "      \"holidayCalendar\": {\n" +
                "        \"id\": 1,\n" +
                "        \"name\": \"Standard\",\n" +
                "        \"note\": \"default für neue Mitarbeiter und gilt auch immer dann wenn kein Mitarbeiter-Bezug vorliegt\",\n" +
                "        \"country\": \"Österreich\",\n" +
                "        \"region\": null,\n" +
                "        \"created\": \"2018-08-21T16:30:53.000000Z\",\n" +
                "        \"modified\": \"2023-11-13T20:18:48.000000Z\"\n" +
                "      },\n" +
                "      \"breakRegulationType\": {\n" +
                "        \"id\": 1,\n" +
                "        \"name\": \"Standard\",\n" +
                "        \"created\": \"2015-04-27T18:46:08.000000Z\",\n" +
                "        \"modified\": \"2015-04-27T16:46:08.000000Z\"\n" +
                "      }\n" +
                "    }\n" +
                "  ],\n" +
                "  \"links\": {\n" +
                "    \"first\": \"http:\\/\\/www.zep-online.de\\/zepgepardecservices_test\\/next\\/api\\/v1\\/employees\\/082-tmeindl\\/regular-working-times?page=1\",\n" +
                "    \"last\": \"http:\\/\\/www.zep-online.de\\/zepgepardecservices_test\\/next\\/api\\/v1\\/employees\\/082-tmeindl\\/regular-working-times?page=1\",\n" +
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
                "        \"url\": \"http:\\/\\/www.zep-online.de\\/zepgepardecservices_test\\/next\\/api\\/v1\\/employees\\/082-tmeindl\\/regular-working-times?page=1\",\n" +
                "        \"label\": \"1\",\n" +
                "        \"active\": true\n" +
                "      },\n" +
                "      {\n" +
                "        \"url\": null,\n" +
                "        \"label\": \"Next &raquo;\",\n" +
                "        \"active\": false\n" +
                "      }\n" +
                "    ],\n" +
                "    \"path\": \"http:\\/\\/www.zep-online.de\\/zepgepardecservices_test\\/next\\/api\\/v1\\/employees\\/082-tmeindl\\/regular-working-times\",\n" +
                "    \"per_page\": 15,\n" +
                "    \"to\": 1,\n" +
                "    \"total\": 1\n" +
                "  }\n" +
                "}";

        Response response = Response.ok().entity(responseBody).build();


        when(zepEmployeeRestClient.getRegularWorkingTimesByUsername(Mockito.anyString())).thenReturn(response);

        ZepRegularWorkingTimes regularWorkingTimes = ZepRegularWorkingTimes.builder()
                .id(155)
                .employee_id("082-tmeindl")
                .start_date(null)
                .monday(8.0)
                .tuesday(8.0)
                .wednesday(8.0)
                .thursday(8.0)
                .friday(6.5)
                .saturday(null)
                .sunday(null)
                .is_monthly(null)
                .monthly_hours(null)
                .max_hours_in_month(null)
                .max_hours_in_week(null)
                .build();

        ZepRegularWorkingTimes actual = zepEmployeeService.getRegularWorkingTimesByUsername("dfsfdsds");

        assertThat(actual).usingRecursiveComparison().isEqualTo(regularWorkingTimes);

    }

    @Test
    public void getRegularWorkingTimesByUsername_receiveEmptyDataArray_then_ThrowException(){
        String responseBody = "{\n" +
                "  \"data\": [],\n" +
                "  \"links\": {\n" +
                "    \"first\": \"http:\\/\\/www.zep-online.de\\/zepgepardecservices_test\\/next\\/api\\/v1\\/employees\\/082-tmeindl\\/regular-working-times?page=1\",\n" +
                "    \"last\": \"http:\\/\\/www.zep-online.de\\/zepgepardecservices_test\\/next\\/api\\/v1\\/employees\\/082-tmeindl\\/regular-working-times?page=1\",\n" +
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
                "        \"url\": \"http:\\/\\/www.zep-online.de\\/zepgepardecservices_test\\/next\\/api\\/v1\\/employees\\/082-tmeindl\\/regular-working-times?page=1\",\n" +
                "        \"label\": \"1\",\n" +
                "        \"active\": true\n" +
                "      },\n" +
                "      {\n" +
                "        \"url\": null,\n" +
                "        \"label\": \"Next &raquo;\",\n" +
                "        \"active\": false\n" +
                "      }\n" +
                "    ],\n" +
                "    \"path\": \"http:\\/\\/www.zep-online.de\\/zepgepardecservices_test\\/next\\/api\\/v1\\/employees\\/082-tmeindl\\/regular-working-times\",\n" +
                "    \"per_page\": 15,\n" +
                "    \"to\": 1,\n" +
                "    \"total\": 1\n" +
                "  }\n" +
                "}";



        Response mockedResponse = Mockito.mock(Response.class);
        when(mockedResponse.readEntity(String.class)).thenReturn(responseBody);

        ZepEmployeeRestClient mockedZepEmployeeRestClient = Mockito.mock(ZepEmployeeRestClient.class);
        when(mockedZepEmployeeRestClient.getRegularWorkingTimesByUsername(Mockito.anyString())).thenReturn(mockedResponse);

        assertThrows(ZepServiceException.class, () -> {
            System.out.println(zepEmployeeService.getRegularWorkingTimesByUsername("082-tmeindl").getEmployee_id());
        });

    }

}
