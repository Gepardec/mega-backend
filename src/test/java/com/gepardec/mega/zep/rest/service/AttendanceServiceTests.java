package com.gepardec.mega.zep.rest.service;

import com.gepardec.mega.zep.rest.client.ZepAttendanceRestClient;
import com.gepardec.mega.zep.rest.client.ZepEmployeeRestClient;
import com.gepardec.mega.zep.rest.entity.ZepAttendance;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.mockito.InjectMock;
import jakarta.inject.Inject;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mockito;

import java.time.LocalDate;
import java.util.List;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@QuarkusTest
public class AttendanceServiceTests {

//    @RestClient
//    @InjectMock
//    ZepAttendanceRestClient zepAttendanceRestClient;

    @Inject
    AttendanceService attendanceService;

//    @Captor
//    ArgumentCaptor<String> startDateCaptor;
//
//    @Captor
//    ArgumentCaptor<String> endDateCaptor;
//
//    @Captor
//    ArgumentCaptor<String> usernameCaptor;


//    @Test
//    public void testStartAndEndDateExtraction_AndConversionToString() {
//        ArgumentCaptor<String> startDateCaptor = ArgumentCaptor.forClass(String.class);
//        ArgumentCaptor<String> endDateCaptor = ArgumentCaptor.forClass(String.class);
//        ArgumentCaptor<String> usernameCaptor = ArgumentCaptor.forClass(String.class);
//
//        attendanceService.getAttendance("username", LocalDate.of(2021, 1, 10));
//        verify(zepAttendanceRestClient).getAttendance( startDateCaptor.capture(), endDateCaptor.capture(), usernameCaptor.capture(), 1);
//
//        assert startDateCaptor.getValue().equals("2021-01-01");
//        assert endDateCaptor.getValue().equals("2021-01-31");
//        assert usernameCaptor.getValue().equals("username");
//    }

    @Test
    public void initialTest() {
        List<ZepAttendance> list = attendanceService.getAttendance("001-hwirnsberger", LocalDate.of(2019, 1, 10));
        System.out.println(list.size());
//        for(ZepAttendance zepAttendance : list) {
//            System.out.println(zepAttendance.getEmployee_id());
//        }
    }


}
