package com.gepardec.mega.zep.rest;

import com.gepardec.mega.zep.rest.entity.ZepEmployee;
import com.gepardec.mega.zep.rest.resource.ZepEmployeeResource;
import com.gepardec.mega.zep.rest.service.ZepEmployeeRestService;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.junit.jupiter.api.Test;

@QuarkusTest
public class ZepRestServiceTest {


    @Inject
    ZepEmployeeResource zepEmployeeResource;

    @Test
    public void getEmployee() {
        ZepEmployee employee = zepEmployeeResource.username("082-tmeindl");
        System.out.println(employee.getUsername());
    }
}

//    @RestClient
//    ZepEmployeeRestService svc;
