package com.gepardec.mega.zep.rest.util;

import com.gepardec.mega.zep.rest.client.ZepAttendanceRestClient;
import com.gepardec.mega.zep.rest.client.ZepProjectRestClient;
import com.gepardec.mega.zep.rest.entity.ZepProject;
import com.gepardec.mega.zep.util.Paginator;
import com.gepardec.mega.helper.ResourceFileService;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.mockito.InjectMock;
import jakarta.inject.Inject;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

@QuarkusTest
public class PaginatorTest {

    @RestClient
    @InjectMock
    ZepProjectRestClient zepProjectRestClient;

    @Inject
    ResourceFileService resourceFileService;

    @Test
    public void withFullPaginatedJsons_thenReturnList() {
        List<String> responseJsons = resourceFileService.getDirContents("projects");

        when(zepProjectRestClient.getProjects(eq(1)))
                .thenReturn(Response.ok().entity(responseJsons.get(0)).build());
        when(zepProjectRestClient.getProjects(eq(2)))
                .thenReturn(Response.ok().entity(responseJsons.get(1)).build());
        when(zepProjectRestClient.getProjects(eq(3)))
                .thenReturn(Response.ok().entity(responseJsons.get(2)).build());

        String[] names = {"MEGA", "gema", "EGA", "SUPERMEGA", "mega", "ega"};


        List<ZepProject> projectList = Paginator.retrieveAll(page -> zepProjectRestClient.getProjects(page), ZepProject.class);
        List<String> projectNames = projectList.stream().map(ZepProject::getName).peek(System.out::println).collect(Collectors.toList());
        Arrays.stream(names).forEach(name -> assertThat(projectNames.contains(name)).isTrue());
    }
    @Test
    public void withEmptyData_thenReturnEmptyList() {
        List<ZepProject> list = Paginator.retrieveAll(
                page -> Response.ok().entity("{ \"data\": [], \"links\": {\"next\": null}}").build(),
                ZepProject.class);
        assertThat(list).isEmpty();
    }

    @Test
    public void withEmptyPages_thenReturnEmptyList() {
        String anyPage = resourceFileService.getSingleFile("paginator/emptyPage.json").get();
        String lastPage = resourceFileService.getSingleFile("paginator/emptyPageLast.json").get();

        int pages = 3;
        List<ZepProject> list = Paginator.retrieveAll(
                page -> {
                    String json;
                    if (page == pages) {
                        json = lastPage;
                    } else {
                        json = anyPage;
                    }
                    return Response.ok().entity(json).build();
                },
                ZepProject.class);
        assertThat(list).isEmpty();
    }

    @Test
    public void withEmptyPages_thenReturnNullSearch() {
        String anyPage = resourceFileService.getSingleFile("paginator/emptyPage.json").get();
        String lastPage = resourceFileService.getSingleFile("paginator/emptyPageLast.json").get();
        int pages = 3;
        Optional<ZepProject> projectOpt = Paginator.searchInAll(
                page -> {
                    String json;
                    if (page == pages) {
                        json = lastPage;
                    } else {
                        json = anyPage;
                    }
                    return Response.ok().entity(json).build();
                },
                project -> project.getName().equals("mega"),
                ZepProject.class);
        assertThat(projectOpt).isEmpty();
    }
    @Test
    public void withEmptyData_thenReturnNullSearch() {
        String anyPage = resourceFileService.getSingleFile("paginator/emptyPageLast.json").get();

        Optional<ZepProject> projectOpt = Paginator.searchInAll(
                page -> Response.ok().entity(anyPage).build(),
                project -> project.getName().equals("mega"),
                ZepProject.class);

        assertThat(projectOpt).isEmpty();
    }

    @Test
    public void withNullData_thenReturnNullSearch() {
        String anyPage = resourceFileService.getSingleFile("paginator/nullPage.json").get();

        Optional<ZepProject> projectOpt = Paginator.searchInAll(
                page -> Response.ok().entity(anyPage).build(),
                project -> project.getName().equals("mega"),
                ZepProject.class);

        assertThat(projectOpt).isEmpty();
    }
    @Test
    public void withNullData_thenReturnEmptyList() {
        String anyPage = resourceFileService.getSingleFile("paginator/nullPage.json").get();
        List<ZepProject> list = Paginator.retrieveAll(
                page -> Response.ok().entity(anyPage).build(),
                ZepProject.class);

        assertThat(list).isEmpty();
    }

    @Test
    public void withFullPaginatedJsons_thenReturnSearch() {
        List<String> responseJsons = resourceFileService.getDirContents("projects");


        when(zepProjectRestClient.getProjects(eq(1)))
                .thenReturn(Response.ok().entity(responseJsons.get(0)).build());
        when(zepProjectRestClient.getProjects(eq(2)))
                .thenReturn(Response.ok().entity(responseJsons.get(1)).build());
        when(zepProjectRestClient.getProjects(eq(3)))
                .thenReturn(Response.ok().entity(responseJsons.get(2)).build());


        Optional<ZepProject> projectOpt = Paginator.searchInAll(
                page -> zepProjectRestClient.getProjects(page),
                project -> project.getName().equals("SUPERMEGA"),
                ZepProject.class);

        assertThat(projectOpt.get().getId()).isEqualTo(4);
    }

    @Test
    public void withFullPaginatedJsons_thenReturnNullSearch() {
        List<String> responseJsons = resourceFileService.getDirContents("projects");



        when(zepProjectRestClient.getProjects(eq(1)))
                .thenReturn(Response.ok().entity(responseJsons.get(0)).build());
        when(zepProjectRestClient.getProjects(eq(2)))
                .thenReturn(Response.ok().entity(responseJsons.get(1)).build());
        when(zepProjectRestClient.getProjects(eq(3)))
                .thenReturn(Response.ok().entity(responseJsons.get(2)).build());

        Optional<ZepProject> projectOpt = Paginator.searchInAll(
                page -> zepProjectRestClient.getProjects(page),
                project -> project.getName().equals("Mmeeggaa"),
                ZepProject.class);

        assertThat(projectOpt).isEmpty();

    }

}
