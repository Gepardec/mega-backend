package com.gepardec.mega.zep.rest.service;

import com.gepardec.mega.zep.rest.client.ZepEmployeeRestClient;
import com.gepardec.mega.zep.rest.dto.ZepEmploymentPeriod;
import io.quarkus.test.InjectMock;
import com.gepardec.mega.helper.ResourceFileService;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@QuarkusTest
class EmploymentPeriodServiceTest {

    @RestClient
    @InjectMock
    ZepEmployeeRestClient zepEmployeeRestClient;

    @Inject
    EmploymentPeriodService employmentPeriodService;

    @Inject
    ResourceFileService resourceFileService;

    @BeforeEach
    void init() {
        resourceFileService.getSingleFile("/employmentPeriods/001-duser.json").ifPresent(json -> {
            Response response = Response.ok().entity(json).build();
            when(zepEmployeeRestClient.getEmploymentPeriodByUserName(eq("001-duser"), eq(1))).thenReturn(response);
        });
    }

    @Test
    void getEmploymentPeriod_thenReturnCorrectZepEmploymentPeriodObject() {
        ZepEmploymentPeriod employmentPeriod1 = ZepEmploymentPeriod.builder()
                .startDate(LocalDateTime.of(2017, 8, 7, 0,0,0))
                .endDate(LocalDateTime.of(2017, 9, 8, 0,0,0))
                .build();
        ZepEmploymentPeriod employmentPeriod2 = ZepEmploymentPeriod.builder()
                .startDate(LocalDateTime.of(2019, 1, 11, 0,0,0))
                .endDate(LocalDateTime.of(2019, 12, 31, 0,0,0))
                .build();
        ZepEmploymentPeriod employmentPeriod3 = ZepEmploymentPeriod.builder()
                .startDate(LocalDateTime.of(2020, 1, 1, 0,0,0))
                .endDate(null)
                .build();

        var zepEmploymentPeriods = List.of(employmentPeriod1, employmentPeriod2, employmentPeriod3);
        var zepEmploymentPeriodsActual = employmentPeriodService.getZepEmploymentPeriodsByEmployeeName("001-duser");

        assertThat(zepEmploymentPeriodsActual).usingRecursiveComparison().isEqualTo(zepEmploymentPeriods);
    }



}
