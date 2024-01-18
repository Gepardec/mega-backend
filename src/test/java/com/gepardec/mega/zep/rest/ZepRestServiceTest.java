package com.gepardec.mega.zep.rest;

import com.gepardec.mega.application.configuration.ZepConfig;
import com.gepardec.mega.zep.rest.client.ZepEmployeeRestClient;
import com.gepardec.mega.zep.rest.entity.ZepEmployee;

import com.gepardec.mega.zep.rest.service.EmployeeService;
import com.github.tomakehurst.wiremock.WireMockServer;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import static org.assertj.core.api.Assertions.assertThat;

@QuarkusTest
public class ZepRestServiceTest {

    private WireMockServer wireMockServer;

//    @BeforeAll



    @Inject
    EmployeeService zepEmployeeService;

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
    public void getRegularWorkingTimesByUsername_returnValidZepWorkingTime(){

    }

}

//    @RestClient
//    ZepEmployeeRestService svc;
