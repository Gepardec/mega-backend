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

import java.time.LocalDate;
import java.time.LocalDateTime;
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

        when(zepProjectRestClient.getProjectByStartEnd(eq("2024-01-01"), eq("2024-01-31"), eq(1)))
                .thenReturn(Response.ok().entity(resourceFileService.getSingleFile("projects/projectPage1.json").get()).build());


        when(zepProjectRestClient.getProjectByStartEnd(anyString(), anyString(), eq(2)))
                .thenReturn(Response.ok().entity(responseJsons.get(1)).build());
        when(zepProjectRestClient.getProjectByStartEnd(anyString(), anyString(), eq(3)))
                .thenReturn(Response.ok().entity(responseJsons.get(2)).build());

        when(zepProjectRestClient.getProjectByName(anyString(), anyString(), eq("mega")))
                .thenReturn(Response.ok().entity(responseJsons.get(0)).build());
        when(zepProjectRestClient.getProjectByName(anyString(), anyString(), eq("empty")))
                .thenReturn(Response.ok().entity(responseJsons.get(5)).build());

        when(zepProjectRestClient.getProjectById(12))
                .thenReturn(Response.ok().entity(resourceFileService.getSingleFile("projects/singlePage2.json").get()).build());

        when(zepProjectRestClient.getProjectById(eq(1)))
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


        List<ZepProject> zepProject = projectService.getProjectsForMonthYear(LocalDate.of(2024, 1, 1));

        zepProject.stream().filter(project -> project.id() == 1)
                .forEach(project -> assertThat(project).usingRecursiveComparison().isEqualTo(referenceZepProject));
    }

    @Test
    void getProjectByName() {
        LocalDate mockCurrentDate = LocalDate.of(2024, 1, 1);
        LocalDate startStr = mockCurrentDate.withDayOfMonth(1);
        LocalDate endStr = mockCurrentDate.withDayOfMonth(mockCurrentDate.lengthOfMonth());
        LocalDateTime start = LocalDateTime.of(startStr.getYear(), startStr.getMonth(), startStr.getDayOfMonth(), 0, 0, 0);
        LocalDateTime end = LocalDateTime.of(endStr.getYear(), endStr.getMonth(), endStr.getDayOfMonth(), 0, 0, 0);

        ZepProject project = ZepProject.builder().id(1).name("XYZ").startDate(start).endDate(end).build();
        ZepProject[] projectsArray = new ZepProject[]{project};

        when(responseParser.retrieveSingle(any(), eq(ZepProject[].class)))
                .thenReturn(Optional.of(projectsArray));

        Optional<ZepProject> result = projectService.getProjectByName("XYZ", LocalDate.of(2024, 1, 1));
        assertThat(result.get().id()).isEqualTo(1);
    }

    @Test
    void getProjectByName_whenNoProjectOfName() {
        Optional<ZepProject> result = projectService.getProjectByName(null, LocalDate.of(2024, 1, 1));
        assertThat(result.isEmpty()).isTrue();
    }

    @Test
    void getProjectByName_whenExceptionIsThrown_thenLogError() {
        when(responseParser.retrieveSingle(any(), eq(ZepProject[].class)))
                .thenThrow(new ZepServiceException("Something went wrong"));

        Optional<ZepProject> result = projectService.getProjectByName("ABC", LocalDate.of(2022, 1, 2));
        assertThat(result.isEmpty()).isTrue();
        verify(logger).warn(anyString(), any(ZepServiceException.class));

    }

    @Test
    void getProjectById() {
        var zepProjectDetail = new ZepProjectDetail();
        zepProjectDetail.setProject(ZepProject.builder().id(1).build());
        Optional<ZepProjectDetail> project = Optional.of(zepProjectDetail);
        when(responseParser.retrieveSingle(any(), eq(ZepProjectDetail.class)))
                .thenReturn(project);

        Optional<ZepProjectDetail> result = projectService.getProjectById(1);
        assertThat(result.isPresent()).isTrue();
    }

    @Test
    void getProjectById_whenNoProjectWithId() {
        Optional<ZepProjectDetail> project = projectService.getProjectById(1);
        assertThat(project.isEmpty()).isTrue();
    }

    @Test
    void getProjectById_whenExceptionIsThrown_thenLogError() {
        when(responseParser.retrieveSingle(any(), eq(ZepProjectDetail.class)))
                .thenThrow(new ZepServiceException("Something went wrong"));

        Optional<ZepProjectDetail> result = projectService.getProjectById(100);
        assertThat(result.isEmpty()).isTrue();
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

        assertThat(result).isNotNull();
        assertThat(result.size()).isEqualTo(2);
    }

    @Test
    void getProjectEmployees_whenExceptionIsThrown_thenLogError() {
        when(responseParser.retrieveAll(any(), eq(ZepProjectEmployee.class)))
                .thenThrow(new ZepServiceException("Something went wrong"));

        List<ZepProjectEmployee> result = projectService.getProjectEmployeesForId(100);
        assertThat(result.isEmpty()).isTrue();
        verify(logger).warn(anyString(), any(ZepServiceException.class));

    }

    @Test
    void getProjectsForMonthYear_whenExceptionIsThrown_thenLogError() {
        when(responseParser.retrieveAll(any(), eq(ZepProject.class)))
                .thenThrow(new ZepServiceException("Something went wrong"));

        List<ZepProject> result = projectService.getProjectsForMonthYear(LocalDate.of(2024, 5, 1));
        assertThat(result.isEmpty()).isTrue();
        verify(logger).warn(anyString(), any(ZepServiceException.class));

    }

}
