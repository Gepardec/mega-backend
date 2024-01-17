package com.gepardec.mega.zep.rest;

import com.gepardec.mega.zep.rest.service.ZepEmployeeRestService;
import jakarta.inject.Inject;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.junit.jupiter.api.Test;

public class ZepRestServiceTest {


    @Inject
    ZepEmployeeResource zepEmployeeResource;

    @Test
    public void getEmployee() {
        svc.getByUsername("082-tmeindl");
    }
}

//    @RestClient
//    ZepEmployeeRestService svc;
