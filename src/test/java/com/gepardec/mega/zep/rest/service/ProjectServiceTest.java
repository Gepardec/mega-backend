package com.gepardec.mega.zep.rest.service;

import com.gepardec.mega.helper.ResourceFileService;
import com.gepardec.mega.zep.rest.client.ZepProjectRestClient;
import com.gepardec.mega.zep.rest.dto.ZepBillingType;
import com.gepardec.mega.zep.rest.dto.ZepProject;
import com.gepardec.mega.zep.rest.dto.ZepProjectDetail;
import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
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


    @Test
    void test() {
        System.out.println(resourceFileService.getFilesDir().getPath());
    }

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
        Optional<ZepProject> project = projectService.getProjectByName("mega", LocalDate.of(2022, 1, 2));
        assertThat(project.get().id()).isEqualTo(1);
    }

    @Test
    void getProjectByName_whenNoProjectOfName() {
        Optional<ZepProject> project = projectService.getProjectByName("empty",
                LocalDate.of(2022, 1, 2));
        assertThat(project.isEmpty()).isTrue();

    }

    @Test
    void getProjectById() {
        Optional<ZepProjectDetail> project = projectService.getProjectById(12);
        assertThat(project.isPresent()).isTrue();
    }

    @Test
    void getProjectById_whenNoProjectWithId() {
        Optional<ZepProjectDetail> project = projectService.getProjectById(1);
        assertThat(project.isEmpty()).isTrue();
    }


}
