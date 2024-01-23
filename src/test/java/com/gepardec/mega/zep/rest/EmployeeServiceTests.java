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


    @Test
    public void getEmployee() {
        ZepEmployee employee = zepEmployeeService.getZepEmployeeByUsername("082-tmeindl");
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

}
