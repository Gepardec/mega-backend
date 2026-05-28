package com.gepardec.mega.zep.rest.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gepardec.mega.helper.ResourceFileService;
import com.gepardec.mega.zep.rest.client.ZepProjectRestClient;
import com.gepardec.mega.zep.rest.dto.ZepBillingType;
import com.gepardec.mega.zep.rest.dto.ZepProject;
import com.gepardec.mega.zep.rest.dto.ZepProjectDetail;
import com.gepardec.mega.zep.rest.dto.ZepProjectEmployee;
import com.gepardec.mega.zep.rest.dto.ZepResponse;
import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import io.smallrye.mutiny.Uni;
import jakarta.inject.Inject;
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
import static org.assertj.core.api.Assertions.assertThatException;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
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

    @Inject
    ObjectMapper objectMapper;

    @InjectMock
    Logger logger;

    @BeforeEach
    void setup() {
        this.getPaginatedProjectsMock();
    }

    private void getPaginatedProjectsMock() {
        List<String> responseJsons = resourceFileService.getDirContents("projects");

        Optional<String> singleFile1 = resourceFileService.getSingleFile("projects/01_projectPage1.json");
        assertThat(singleFile1).isPresent();
        ZepResponse<List<ZepProject>> response1 = parseProjectListResponse(singleFile1.get());
        when(zepProjectRestClient.getProjectByStartEnd("2024-01-01", "2024-01-31", 1))
                .thenReturn(Uni.createFrom().item(response1));

        ZepResponse<List<ZepProject>> response2 = parseProjectListResponse(responseJsons.get(1));
        when(zepProjectRestClient.getProjectByStartEnd(anyString(), anyString(), eq(2)))
                .thenReturn(Uni.createFrom().item(response2));

        ZepResponse<List<ZepProject>> response3 = parseProjectListResponse(responseJsons.get(2));
        when(zepProjectRestClient.getProjectByStartEnd(anyString(), anyString(), eq(3)))
                .thenReturn(Uni.createFrom().item(response3));

        ZepResponse<List<ZepProject>> megaResponse = parseProjectListResponse(responseJsons.getFirst());
        when(zepProjectRestClient.getProjectByName(anyString(), anyString(), eq("mega")))
                .thenReturn(Uni.createFrom().item(megaResponse));

        ZepResponse<List<ZepProject>> emptyResponse = parseProjectListResponse(responseJsons.get(5));
        when(zepProjectRestClient.getProjectByName(anyString(), anyString(), eq("empty")))
                .thenReturn(Uni.createFrom().item(emptyResponse));

        Optional<String> singleFile = resourceFileService.getSingleFile("projects/05_singlePage2.json");
        assertThat(singleFile).isPresent();
        ZepResponse<ZepProjectDetail> detailResponse = parseProjectDetailResponse(singleFile.get());
        when(zepProjectRestClient.getProjectById(12))
                .thenReturn(Uni.createFrom().item(detailResponse));

        ZepResponse<ZepProjectDetail> emptyDetailResponse = parseProjectDetailResponse(responseJsons.get(6));
        when(zepProjectRestClient.getProjectById(1))
                .thenReturn(Uni.createFrom().item(emptyDetailResponse));
    }

    private ZepResponse<List<ZepProject>> parseProjectListResponse(String json) {
        try {
            return objectMapper.readValue(json, new TypeReference<>() {
            });
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse project list response", e);
        }
    }

    private ZepResponse<ZepProjectDetail> parseProjectDetailResponse(String json) {
        try {
            return objectMapper.readValue(json, new TypeReference<>() {
            });
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse project detail response", e);
        }
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
        ZepProject project = ZepProject.builder()
                .id(1)
                .name("XYZ")
                .startDate(payrollMonth.atDay(1).atStartOfDay())
                .endDate(payrollMonth.atEndOfMonth().atTime(LocalTime.MAX))
                .build();

        when(zepProjectRestClient.getProjectByName(anyString(), anyString(), eq("XYZ")))
                .thenReturn(Uni.createFrom().item(new ZepResponse<>(List.of(project), null)));

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
        when(zepProjectRestClient.getProjectByName(anyString(), anyString(), eq("ABC")))
                .thenReturn(Uni.createFrom().failure(new RuntimeException("Something went wrong")));

        assertThatException().isThrownBy(() -> projectService.getProjectByName("ABC", YearMonth.of(2022, 1)));
        verify(logger).warn(eq("Error retrieving project"), any(Throwable.class));
    }

    @Test
    void getProjectById() {
        var zepProjectDetail = new ZepProjectDetail(ZepProject.builder().id(1).build(), null);

        when(zepProjectRestClient.getProjectById(1))
                .thenReturn(Uni.createFrom().item(new ZepResponse<>(zepProjectDetail, null)));

        Optional<ZepProjectDetail> result = projectService.getProjectById(1);
        assertThat(result).isPresent();
    }

    @Test
    void getProjectById_whenNoProjectWithId() {
        when(zepProjectRestClient.getProjectById(999))
                .thenReturn(Uni.createFrom().item(new ZepResponse<>(null, null)));

        Optional<ZepProjectDetail> project = projectService.getProjectById(999);
        assertThat(project).isEmpty();
    }

    @Test
    void getProjectById_whenExceptionIsThrown_thenLogError() {
        when(zepProjectRestClient.getProjectById(100))
                .thenReturn(Uni.createFrom().failure(new RuntimeException("Something went wrong")));

        assertThatException().isThrownBy(() -> projectService.getProjectById(100));
        verify(logger).warn(eq("Error retrieving project"), any(Throwable.class));

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

        when(zepProjectRestClient.getProjectEmployees(eq(1), anyInt()))
                .thenReturn(Uni.createFrom().item(new ZepResponse<>(employees, new ZepResponse.Links(null, null))));

        List<ZepProjectEmployee> result = projectService.getProjectEmployeesForId(1, null);

        assertThat(result)
                .isNotNull()
                .hasSize(2);
    }

    @Test
    void getProjectEmployees_whenExceptionIsThrown_thenLogError() {
        when(zepProjectRestClient.getProjectEmployees(eq(100), anyInt()))
                .thenReturn(Uni.createFrom().failure(new RuntimeException("Something went wrong")));

        assertThatException().isThrownBy(() -> projectService.getProjectEmployeesForId(100, null));
        verify(logger).warn(eq("Error retrieving project employees from ZEP"), any(Throwable.class));

    }

    @Test
    void getProjectsForMonthYear_whenExceptionIsThrown_thenLogError() {
        when(zepProjectRestClient.getProjectByStartEnd(anyString(), anyString(), anyInt()))
                .thenReturn(Uni.createFrom().failure(new RuntimeException("Something went wrong")));

        assertThatException().isThrownBy(() -> projectService.getProjectsForMonthYear(YearMonth.of(2024, 5)));
        verify(logger).warn(eq("Error retrieving projects from ZEP"), any(Throwable.class));
    }
}
