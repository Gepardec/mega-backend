package com.gepardec.mega.zep.rest.service;

import com.gepardec.mega.zep.rest.client.ZepProjectRestClient;
import com.gepardec.mega.zep.rest.entity.ZepProject;
import com.gepardec.mega.zep.util.ResponseParser;
import com.gepardec.mega.helper.ResourceFileService;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.mockito.InjectMock;
import jakarta.inject.Inject;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;

@QuarkusTest
public class ProjectServiceTest {

    @InjectMock
    @RestClient
    ZepProjectRestClient zepProjectRestClient;

    @Inject
    ProjectService projectService;

    @Inject
    ResourceFileService resourceFileService;

    @Inject
    ResponseParser responseParser;

    @Test
    public void test() {
        System.out.println(resourceFileService.getFilesDir().getPath());
    }

    @BeforeEach
    public void setup() {
        this.getPaginatedProjectsMock();
    }

    private void getPaginatedProjectsMock() {
        List<String> responseJsons = resourceFileService.getDirContents("projects");
        System.out.println(resourceFileService.getSingleFile("projects/singlePage1.json"));

        when(zepProjectRestClient.getProjectByStartEnd(eq("2024-01-01"), eq("2024-01-31"), eq(1)))
                .thenReturn(Response.ok().entity(resourceFileService.getSingleFile("projects/projectPage1.json").get()).build());



        when(zepProjectRestClient.getProjectByStartEnd(any(), any(),eq(2)))
                .thenReturn(Response.ok().entity(responseJsons.get(1)).build());
        when(zepProjectRestClient.getProjectByStartEnd(any(), any(),eq(3)))
                .thenReturn(Response.ok().entity(responseJsons.get(2)).build());

        when(zepProjectRestClient.getProjectByName(any(), any(),eq("mega")))
                .thenReturn(Response.ok().entity(responseJsons.get(0)).build());
        when(zepProjectRestClient.getProjectByName(any(), any(),eq("empty")))
                .thenReturn(Response.ok().entity(responseJsons.get(4)).build());

    }

    @Test
    public void getSingleFullZepProject() {

        ZepProject referenceZepProject = ZepProject.builder()
                .id(1)
                .name("MEGA")
                .startDate(LocalDateTime.of(2020, 12, 1, 0,0,0))
                .endDate(LocalDateTime.of(2026, 1, 20, 0,0,0))
                .billingType(1)
                .build();


        List<ZepProject> zepProject = projectService.getProjectsForMonthYear(LocalDate.of(2024, 1, 1));

        zepProject.stream().filter(project -> project.id() == 1)
                .forEach(project -> assertThat(project).usingRecursiveComparison().isEqualTo(referenceZepProject));
    }

    @Test
    public void getProjectByName() {
        Optional<ZepProject> project = projectService.getProjectByName("mega", LocalDate.of(2022, 1, 2));
        assertThat(project.get().id()).isEqualTo(1);
    }
    @Test
    public void getProjectByName_whenNoProjectOfName() {
        Optional<ZepProject> project = projectService.getProjectByName("empty",
                LocalDate.of(2022, 1, 2));
        assertThat(project.isEmpty()).isTrue();

    }


}
