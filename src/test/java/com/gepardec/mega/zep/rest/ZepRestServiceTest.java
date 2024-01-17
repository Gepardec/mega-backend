package com.gepardec.mega.zep.rest;

import com.gepardec.mega.zep.rest.service.ZepEmployeeRestService;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;

public class ZepRestServiceTest {

    @Inject
    ZepEmployeeRestService svc;

    @Test
    public void getEmployee() {
        svc.getByUsername("082-tmeindl");
    }
}
