package com.gepardec.mega.zep.rest.util;

import com.gepardec.mega.zep.rest.client.ZepAttendanceRestClient;
import com.gepardec.mega.zep.rest.client.ZepProjectRestClient;
import com.gepardec.mega.zep.rest.entity.ZepProject;
import com.gepardec.mega.zep.util.Paginator;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.mockito.InjectMock;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.List;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@QuarkusTest
public class PaginatorTest {

    @RestClient
    @InjectMock
    ZepProjectRestClient zepProjectRestClient;

    @Test
    public void paginator() {
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


        List<ZepProject> list = Paginator.retrieveAll(page -> zepProjectRestClient.getProjects(page), ZepProject.class);
        list.forEach((e) -> {
            System.out.println(e.getId());
        });
    }
}
