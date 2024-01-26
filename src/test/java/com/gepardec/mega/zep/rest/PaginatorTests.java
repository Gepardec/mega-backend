package com.gepardec.mega.zep.rest;

import com.gepardec.mega.zep.rest.client.ZepAttendanceRestClient;
import com.gepardec.mega.zep.rest.entity.ZepAttendance;
import com.gepardec.mega.zep.rest.service.AttendanceService;
import com.gepardec.mega.zep.util.Paginator;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.junit.jupiter.api.Test;

import java.util.List;

@QuarkusTest
public class PaginatorTests {

    @RestClient
    ZepAttendanceRestClient zepAttendanceRestClient;

    @Inject
    AttendanceService attendanceService;


}
