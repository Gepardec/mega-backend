package com.gepardec.mega.zep.rest;

import com.gepardec.mega.zep.rest.service.EmployeeService;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;

import static org.junit.jupiter.api.Assertions.assertThrows;

@QuarkusTest
public class ZepEmployeeRestClientTest {

    @Inject
    EmployeeService zepEmployeeService;

}
