package com.gepardec.mega.zep.rest.util;

import com.gepardec.mega.zep.rest.client.ZepAttendanceRestClient;
import com.gepardec.mega.zep.rest.client.ZepProjectRestClient;
import com.gepardec.mega.zep.rest.entity.ZepProject;
import com.gepardec.mega.zep.util.Paginator;
import com.gepardec.mega.zep.util.ZepRestUtil;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.mockito.InjectMock;
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

    @Test
    public void withFullPaginatedJsons_thenReturnList() {
        List<String> responseJsons = List.of(
                "{ \"data\": [{\"id\": 1, \"name\": \"mega\"}," +
                        "{\"id\": 2, \"name\": \"gema\"}]," +
                        "\"links\": {" +
                        "   \"next\": \"http:\\/\\/www.zep-online.de\\/demo\\/next\\/api\\/v1\\/projects\\/54\\/employees?page=2\" \n" +
                        "} \n" +
                        "}",
                "{ \"data\": [{\"id\": 3, \"name\": \"EGA\"}," +
                        "{\"id\": 4, \"name\": \"SUPERMEGA\"}]," +
                        "\"links\": {" +
                        "   \"next\": \"http:\\/\\/www.zep-online.de\\/demo\\/next\\/api\\/v1\\/projects\\/54\\/employees?page=3\" \n" +
                        "} \n" +
                        "}",
                "{ \"data\": [{\"id\": 5, \"name\": \"MEGA\"}," +
                        "{\"id\": 6, \"name\": \"ega\"}]," +
                        "\"links\": {" +
                        "   \"next\": null \n" +
                        "} \n" +
                        "}"
        );

        when(zepProjectRestClient.getProjects(eq(1)))
                .thenReturn(Response.ok().entity(responseJsons.get(0)).build());
        when(zepProjectRestClient.getProjects(eq(2)))
                .thenReturn(Response.ok().entity(responseJsons.get(1)).build());
        when(zepProjectRestClient.getProjects(eq(3)))
                .thenReturn(Response.ok().entity(responseJsons.get(2)).build());

        String[] names = {"mega", "gema", "EGA", "SUPERMEGA", "MEGA", "ega"};


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
        String anyPage = "{ \"data\": [], \"links\": {" +
                "\"next\": \"http:\\/\\/www.zep-online.de\\/demo\\/next\\/api\\/v1\\/projects\\/4\\/employees?page=%d\"}}";
        String page3 = "{ \"data\": [], \"links\": {" +
                "\"next\": null }}";
        List<ZepProject> list = Paginator.retrieveAll(
                page -> {
                    String json;
                    if (page == 3) {
                        json = page3;
                    } else {
                        json = String.format(anyPage, page + 1);
                    }
                    return Response.ok().entity(json).build();
                },
                ZepProject.class);
        assertThat(list).isEmpty();
    }

    @Test
    public void withEmptyPages_thenReturnNullSearch() {
        String anyPage = "{ \"data\": [], \"links\": {" +
                "\"next\": \"http:\\/\\/www.zep-online.de\\/demo\\/next\\/api\\/v1\\/projects\\/4\\/employees?page=%d\"}}";
        String page3 = "{ \"data\": [], \"links\": {" +
                "\"next\": null }}";
        Optional<ZepProject> projectOpt = Paginator.searchInAll(
                page -> {
                    String json;
                    if (page == 3) {
                        json = page3;
                    } else {
                        json = String.format(anyPage, page + 1);
                    }
                    return Response.ok().entity(json).build();
                },
                project -> project.getName().equals("mega"),
                ZepProject.class);
        assertThat(projectOpt).isEmpty();
    }
    @Test
    public void withEmptyData_thenReturnNullSearch() {
        String anyPage = "{ \"data\": [], \"links\": {\"next\": null}}";
        Optional<ZepProject> projectOpt = Paginator.searchInAll(
                page -> Response.ok().entity(anyPage).build(),
                project -> project.getName().equals("mega"),
                ZepProject.class);

        assertThat(projectOpt).isEmpty();
    }

    @Test
    public void withNullData_thenReturnNullSearch() {
        String anyPage = "{ \"data\": null, \"links\": {\"next\": null}}";
        Optional<ZepProject> projectOpt = Paginator.searchInAll(
                page -> Response.ok().entity(anyPage).build(),
                project -> project.getName().equals("mega"),
                ZepProject.class);

        assertThat(projectOpt).isEmpty();
    }
    @Test
    public void withNullData_thenReturnEmptyList() {
        String anyPage = "{ \"data\": null, \"links\": {\"next\": null}}";
        List<ZepProject> list = Paginator.retrieveAll(
                page -> Response.ok().entity(anyPage).build(),
                ZepProject.class);

        assertThat(list).isEmpty();
    }

    @Test
    public void withFullPaginatedJsons_thenReturnSearch() {
        List<String> responseJsons = List.of(
                "{ \"data\": [{\"id\": 1, \"name\": \"mega\"}," +
                        "{\"id\": 2, \"name\": \"gema\"}]," +
                        "\"links\": {" +
                        "   \"next\": \"http:\\/\\/www.zep-online.de\\/demo\\/next\\/api\\/v1\\/projects\\/54\\/employees?page=2\" \n" +
                        "} \n" +
                        "}",
                "{ \"data\": [{\"id\": 3, \"name\": \"EGA\"}," +
                        "{\"id\": 4, \"name\": \"SUPERMEGA\"}]," +
                        "\"links\": {" +
                        "   \"next\": \"http:\\/\\/www.zep-online.de\\/demo\\/next\\/api\\/v1\\/projects\\/54\\/employees?page=3\" \n" +
                        "} \n" +
                        "}",
                "{ \"data\": [{\"id\": 5, \"name\": \"MEGA\"}," +
                        "{\"id\": 6, \"name\": \"ega\"}]," +
                        "\"links\": {" +
                        "   \"next\": null \n" +
                        "} \n" +
                        "}"
        );

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
        List<String> responseJsons = List.of(
                "{ \"data\": [{\"id\": 1, \"name\": \"mega\"}," +
                        "{\"id\": 2, \"name\": \"gema\"}]," +
                        "\"links\": {" +
                        "   \"next\": \"http:\\/\\/www.zep-online.de\\/demo\\/next\\/api\\/v1\\/projects\\/54\\/employees?page=2\" \n" +
                        "} \n" +
                        "}",
                "{ \"data\": [{\"id\": 3, \"name\": \"EGA\"}," +
                        "{\"id\": 4, \"name\": \"SUPERMEGA\"}]," +
                        "\"links\": {" +
                        "   \"next\": \"http:\\/\\/www.zep-online.de\\/demo\\/next\\/api\\/v1\\/projects\\/54\\/employees?page=3\" \n" +
                        "} \n" +
                        "}",
                "{ \"data\": [{\"id\": 5, \"name\": \"MEGA\"}," +
                        "{\"id\": 6, \"name\": \"ega\"}]," +
                        "\"links\": {" +
                        "   \"next\": null \n" +
                        "} \n" +
                        "}"
        );


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
