package com.gepardec.mega.zep.rest.service;

import com.gepardec.mega.helper.ResourceFileService;
import com.gepardec.mega.zep.ZepServiceException;
import com.gepardec.mega.zep.rest.client.ZepProjectRestClient;
import com.gepardec.mega.zep.rest.dto.ZepBillingType;
import com.gepardec.mega.zep.rest.dto.ZepProject;
import com.gepardec.mega.zep.rest.dto.ZepProjectDetail;
import com.gepardec.mega.zep.rest.dto.ZepProjectEmployee;
import com.gepardec.mega.zep.util.ResponseParser;
import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@QuarkusTest
class ProjectServiceTest {

    @InjectMock
    @RestClient
    ZepProjectRestClient zepProjectRestClient;

    @Inject
    ProjectService projectService;

    @Inject
    ResourceFileService resourceFileService;

    @InjectMock
    ResponseParser responseParser;

    @InjectMock
    Logger logger;

    @BeforeEach
    void setup() {
        this.getPaginatedProjectsMock();
    }

    private void getPaginatedProjectsMock() {
        List<String> responseJsons = resourceFileService.getDirContents("projects");
        System.out.println(resourceFileService.getSingleFile("projects/singlePage1.json"));

        Optional<String> singleFile1 = resourceFileService.getSingleFile("projects/projectPage1.json");
        assertThat(singleFile1).isPresent();
        when(zepProjectRestClient.getProjectByStartEnd(eq("2024-01-01"), eq("2024-01-31"), eq(1)))
                .thenReturn(Response.ok().entity(singleFile1.get()).build());


        when(zepProjectRestClient.getProjectByStartEnd(anyString(), anyString(), eq(2)))
                .thenReturn(Response.ok().entity(responseJsons.get(1)).build());
        when(zepProjectRestClient.getProjectByStartEnd(anyString(), anyString(), eq(3)))
                .thenReturn(Response.ok().entity(responseJsons.get(2)).build());

        when(zepProjectRestClient.getProjectByName(anyString(), anyString(), eq("mega")))
                .thenReturn(Response.ok().entity(responseJsons.getFirst()).build());
        when(zepProjectRestClient.getProjectByName(anyString(), anyString(), eq("empty")))
                .thenReturn(Response.ok().entity(responseJsons.get(5)).build());

        Optional<String> singleFile = resourceFileService.getSingleFile("projects/singlePage2.json");
        assertThat(singleFile).isPresent();
        when(zepProjectRestClient.getProjectById(12))
                .thenReturn(Response.ok().entity(singleFile.get()).build());

        when(zepProjectRestClient.getProjectById(1))
                .thenReturn(Response.ok().entity(responseJsons.get(5)).build());
    }

    @Test
    void getSingleFullZepProject() {

        ZepProject referenceZepProject = ZepProject.builder()
                .id(1)
                .name("MEGA")
                .startDate(LocalDateTime.of(2020, 12, 1, 0, 0, 0))
                .endDate(LocalDateTime.of(2026, 1, 20, 0, 0, 0))
                .billingType(new ZepBillingType(1))
                .customerId(1)
                .build();


        List<ZepProject> zepProject = projectService.getProjectsForMonthYear(YearMonth.of(2024, 1));

        zepProject.stream().filter(project -> project.id() == 1)
                .forEach(project -> assertThat(project).usingRecursiveComparison().isEqualTo(referenceZepProject));
    }

    @Test
    void getProjectByName() {
        YearMonth payrollMonth = YearMonth.of(2024, 1);
        ZepProject[] projectsArray = new ZepProject[]{
                ZepProject.builder()
                        .id(1)
                        .name("XYZ")
                        .startDate(payrollMonth.atDay(1).atStartOfDay())
                        .endDate(payrollMonth.atEndOfMonth().atTime(LocalTime.MAX))
                        .build()
        };

        when(responseParser.retrieveSingle(any(), eq(ZepProject[].class)))
                .thenReturn(Optional.of(projectsArray));

        Optional<ZepProject> result = projectService.getProjectByName("XYZ", YearMonth.of(2024, 1));
        assertThat(result).isNotEmpty();
        assertThat(result.get().id()).isEqualTo(1);
    }

    @Test
    void getProjectByName_whenNoProjectOfName() {
        Optional<ZepProject> result = projectService.getProjectByName(null, YearMonth.of(2024, 1));
        assertThat(result).isEmpty();
    }

    @Test
    void getProjectByName_whenExceptionIsThrown_thenLogError() {
        when(responseParser.retrieveSingle(any(), eq(ZepProject[].class)))
                .thenThrow(new ZepServiceException("Something went wrong"));

        Optional<ZepProject> result = projectService.getProjectByName("ABC", YearMonth.of(2022, 1));
        assertThat(result).isEmpty();
        verify(logger).warn(anyString(), any(ZepServiceException.class));
    }

    @Test
    void getProjectById() {
        var zepProjectDetail = new ZepProjectDetail(ZepProject.builder().id(1).build(), null);
        Optional<ZepProjectDetail> project = Optional.of(zepProjectDetail);
        when(responseParser.retrieveSingle(any(), eq(ZepProjectDetail.class)))
                .thenReturn(project);

        Optional<ZepProjectDetail> result = projectService.getProjectById(1);
        assertThat(result).isPresent();
    }

    @Test
    void getProjectById_whenNoProjectWithId() {
        Optional<ZepProjectDetail> project = projectService.getProjectById(1);
        assertThat(project).isEmpty();
    }

    @Test
    void getProjectById_whenExceptionIsThrown_thenLogError() {
        when(responseParser.retrieveSingle(any(), eq(ZepProjectDetail.class)))
                .thenThrow(new ZepServiceException("Something went wrong"));

        Optional<ZepProjectDetail> result = projectService.getProjectById(100);
        assertThat(result).isEmpty();
        verify(logger).warn(anyString(), any(ZepServiceException.class));

    }

    @Test
    void getProjectEmployeesForId_whenEmployeesPresent_thenReturnListOfEmployees() {
        List<ZepProjectEmployee> employees = new ArrayList<>();
        employees.add(
                ZepProjectEmployee.builder().username("mMustermann").build()
        );
        employees.add(
                ZepProjectEmployee.builder().username("mMusterfrau").build()
        );

        when(responseParser.retrieveAll(any(), eq(ZepProjectEmployee.class)))
                .thenReturn(employees);

        List<ZepProjectEmployee> result = projectService.getProjectEmployeesForId(1);

        assertThat(result)
                .isNotNull()
                .hasSize(2);
    }

    @Test
    void getProjectEmployees_whenExceptionIsThrown_thenLogError() {
        when(responseParser.retrieveAll(any(), eq(ZepProjectEmployee.class)))
                .thenThrow(new ZepServiceException("Something went wrong"));

        List<ZepProjectEmployee> result = projectService.getProjectEmployeesForId(100);
        assertThat(result).isEmpty();
        verify(logger).warn(anyString(), any(ZepServiceException.class));

    }

    @Test
    void getProjectsForMonthYear_whenExceptionIsThrown_thenLogError() {
        when(responseParser.retrieveAll(any(), eq(ZepProject.class)))
                .thenThrow(new ZepServiceException("Something went wrong"));

        List<ZepProject> result = projectService.getProjectsForMonthYear(YearMonth.of(2024, 5));
        assertThat(result).isEmpty();
        verify(logger).warn(anyString(), any(ZepServiceException.class));
    }
}
