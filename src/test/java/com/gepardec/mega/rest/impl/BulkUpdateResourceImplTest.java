package com.gepardec.mega.rest.impl;

import com.gepardec.mega.rest.model.HourlyRateFileDto;
import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.ws.rs.core.Response;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.InputStream;

import static org.junit.jupiter.api.Assertions.*;

@QuarkusTest
class BulkUpdateResourceImplTest {

    @InjectMock
    BulkUpdateResourceImpl bulkUpdateResource;

    @InjectMock
    HourlyRateFileDto hourlyRateFile;

    @BeforeEach
    void setUp() {
    }

    @Test
    void uploadInternalRate() {
        Response r = bulkUpdateResource.uploadInternalRate(hourlyRateFile);
        assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), r.getStatus());
    }
}