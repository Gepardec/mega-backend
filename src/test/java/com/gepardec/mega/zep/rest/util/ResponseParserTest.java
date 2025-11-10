package com.gepardec.mega.zep.rest.util;

import com.gepardec.mega.helper.ResourceFileService;
import com.gepardec.mega.zep.rest.client.ZepProjectRestClient;
import com.gepardec.mega.zep.rest.dto.ZepProject;
import com.gepardec.mega.zep.util.ResponseParser;
import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@QuarkusTest
class ResponseParserTest {

    @RestClient
    @InjectMock
    ZepProjectRestClient zepProjectRestClient;

    @Inject
    ResourceFileService resourceFileService;

    @Inject
    ResponseParser responseParser;

    @Test
    void withFullPaginatedJsons_thenReturnList() {
        List<String> responseJsons = resourceFileService.getDirContents("projects");

        when(zepProjectRestClient.getProjects(1))
                .thenReturn(Response.ok().entity(responseJsons.getFirst()).build());
        when(zepProjectRestClient.getProjects(2))
                .thenReturn(Response.ok().entity(responseJsons.get(1)).build());
        when(zepProjectRestClient.getProjects(3))
                .thenReturn(Response.ok().entity(responseJsons.get(2)).build());

        String[] names = {"MEGA", "gema", "EGA", "SUPERMEGA", "mega", "ega"};


        List<ZepProject> projectList = responseParser.retrieveAll(page -> zepProjectRestClient.getProjects(page), ZepProject.class);
        List<String> projectNames = projectList.stream().map(ZepProject::name).peek(System.out::println).toList();
        System.out.println(projectNames);
        Arrays.stream(names)
                .forEach(name -> assertThat(projectNames).contains(name));
    }

    @Test
    void withEmptyData_thenReturnEmptyList() {
        List<ZepProject> list = responseParser.retrieveAll(
                page -> Response.ok().entity("{ \"data\": [], \"links\": {\"next\": null}}").build(),
                ZepProject.class);
        assertThat(list).isEmpty();
    }

    @Test
    void withEmptyPages_thenReturnEmptyList() {
        String anyPage = resourceFileService.getSingleFile("paginator/emptyPage.json").get();
        String lastPage = resourceFileService.getSingleFile("paginator/emptyPageLast.json").get();

        int pages = 3;
        List<ZepProject> list = responseParser.retrieveAll(
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
    void withEmptyPages_thenReturnNullSearch() {
        String anyPage = resourceFileService.getSingleFile("paginator/emptyPage.json").get();
        String lastPage = resourceFileService.getSingleFile("paginator/emptyPageLast.json").get();
        int pages = 3;
        Optional<ZepProject> projectOpt = responseParser.searchInAll(
                page -> {
                    String json;
                    if (page == pages) {
                        json = lastPage;
                    } else {
                        json = anyPage;
                    }
                    return Response.ok().entity(json).build();
                },
                project -> project.name().equals("mega"),
                ZepProject.class);
        assertThat(projectOpt).isEmpty();
    }

    @Test
    void withEmptyData_thenReturnNullSearch() {
        String anyPage = resourceFileService.getSingleFile("paginator/emptyPageLast.json").get();

        Optional<ZepProject> projectOpt = responseParser.searchInAll(
                page -> Response.ok().entity(anyPage).build(),
                project -> project.name().equals("mega"),
                ZepProject.class);

        assertThat(projectOpt).isEmpty();
    }

    @Test
    void withNullData_thenReturnNullSearch() {
        String anyPage = resourceFileService.getSingleFile("paginator/nullPage.json").get();

        Optional<ZepProject> projectOpt = responseParser.searchInAll(
                page -> Response.ok().entity(anyPage).build(),
                project -> project.name().equals("mega"),
                ZepProject.class);

        assertThat(projectOpt).isEmpty();
    }

    @Test
    void withNullData_thenReturnEmptyList() {
        String anyPage = resourceFileService.getSingleFile("paginator/nullPage.json").get();
        List<ZepProject> list = responseParser.retrieveAll(
                page -> Response.ok().entity(anyPage).build(),
                ZepProject.class);

        assertThat(list).isEmpty();
    }

    @Test
    void withFullPaginatedJsons_thenReturnSearch() {
        List<String> responseJsons = resourceFileService.getDirContents("projects");


        when(zepProjectRestClient.getProjects(1))
                .thenReturn(Response.ok().entity(responseJsons.getFirst()).build());
        when(zepProjectRestClient.getProjects(2))
                .thenReturn(Response.ok().entity(responseJsons.get(1)).build());
        when(zepProjectRestClient.getProjects(3))
                .thenReturn(Response.ok().entity(responseJsons.get(2)).build());


        Optional<ZepProject> projectOpt = responseParser.searchInAll(
                page -> zepProjectRestClient.getProjects(page),
                project -> project.name().equals("SUPERMEGA"),
                ZepProject.class);

        assertThat(projectOpt.get().id()).isEqualTo(4);
    }

    @Test
    void withFullPaginatedJsons_thenReturnNullSearch() {
        List<String> responseJsons = resourceFileService.getDirContents("projects");


        when(zepProjectRestClient.getProjects(1))
                .thenReturn(Response.ok().entity(responseJsons.getFirst()).build());
        when(zepProjectRestClient.getProjects(2))
                .thenReturn(Response.ok().entity(responseJsons.get(1)).build());
        when(zepProjectRestClient.getProjects(3))
                .thenReturn(Response.ok().entity(responseJsons.get(2)).build());

        Optional<ZepProject> projectOpt = responseParser.searchInAll(
                page -> zepProjectRestClient.getProjects(page),
                project -> project.name().equals("Mmeeggaa"),
                ZepProject.class);

        assertThat(projectOpt).isEmpty();

    }

}
