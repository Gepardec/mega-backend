package com.gepardec.mega.zep.rest.service;

import com.gepardec.mega.zep.rest.client.ZepAttendanceRestClient;
import com.gepardec.mega.zep.rest.dto.ZepAttendance;
import com.gepardec.mega.zep.rest.dto.ZepBillable;
import com.gepardec.mega.zep.util.ResponseParser;
import com.gepardec.mega.helper.ResourceFileService;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.mockito.InjectMock;
import jakarta.inject.Inject;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@QuarkusTest
public class AttendanceServiceTest {
    @RestClient
    @InjectMock
    ZepAttendanceRestClient zepAttendanceRestClient;

    @Inject
    AttendanceService attendanceService;

    @Inject
    ResourceFileService resourceFileService;

    @Inject
    ResponseParser responseParser;

    @BeforeEach
    public void init() {
        List<String> users = List.of("001-duser", "002-tuser");
        users.forEach(this::createMockForUser);
    }

    private void createMockForUser(String user) {
        List<String> responseJson = resourceFileService.getDirContents("/attendances/" + user);

        IntStream.range(0, responseJson.size()).forEach(
                i -> {
                    when(zepAttendanceRestClient
                            .getAttendance(anyString(), anyString(), eq(user), eq(i + 1)))
                            .thenReturn(Response.ok().entity(responseJson.get(i)).build());
                    System.out.println(responseJson.get(i));
                }
        );
    }

    @Test
    public void getAttendances() {
        List<ZepAttendance> attendancesReference = List.of(
            ZepAttendance.builder()
                .id(1)
                .date(LocalDate.of(2018,12,11))
                .from(LocalTime.of(8,0,0))
                .to(LocalTime.of(9,15,0))
                .employeeId("001-duser")
                .projectId(1)
                .projectTaskId(1)
                .duration(1.25)
                .billable(true)
                .workLocation(null)
                .workLocationIsProjectRelevant(false)
                .activity("besprechen")
                .vehicle(null)
                .directionOfTravel(null)
                .build(),
            ZepAttendance.builder()
                .id(2)
                .date(LocalDate.of(2015,2,11))
                .from(LocalTime.of(9,0,0))
                .to(LocalTime.of(13,45,0))
                .employeeId("001-duser")
                .projectId(1)
                .projectTaskId(2)
                .duration(4.75)
                .billable(true)
                .workLocation(null)
                .workLocationIsProjectRelevant(true)
                .activity("bearbeiten")
                .vehicle(null)
                .directionOfTravel(null)
                .build()
        );

        List<ZepAttendance> attendances = attendanceService.getBillableAttendancesForUserAndMonth("001-duser", LocalDate.of(2018,12,11));
        assertThat(List.of(attendances.get(0), attendances.get(1))).usingRecursiveComparison().isEqualTo(attendancesReference);
    }

    @Test
    public void getAttendancesPaginated() {
        List<ZepAttendance> attendances = attendanceService.getBillableAttendancesForUserAndMonth("001-duser", LocalDate.now());
        IntStream.range(0, 3).forEach(
                i -> assertThat(attendances.get(i).id()).isEqualTo(i + 1)
        );
    }
    @Test
    public void notBillableAttendances_thenReturnNull() {
        List<ZepAttendance> attendances = attendanceService.getBillableAttendancesForUserAndMonth("001-duser", LocalDate.now());
        assertThat(attendances.size()).isEqualTo(3);
    }

    @Test
    @Disabled
    public void extractCorrectMonthFromGivenDate_thenCallPaginator() {
        ArgumentCaptor<String> startDateCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> endDateCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> usernameCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<Function> functionCaptor = ArgumentCaptor.forClass(Function.class);

        ResponseParser mockedResponseParser = Mockito.mock(ResponseParser.class);

        when(mockedResponseParser.retrieveAll(any(), any()))
                .thenReturn(new ArrayList<>());

        //Call the Method under test
        attendanceService.getAttendanceForUserAndMonth("username", LocalDate.of(2021, 1, 10));

        //Retrieve the function called in the method under test
        verify(mockedResponseParser.retrieveAll(functionCaptor.capture(), any()));
        Function<Integer, Response> function = functionCaptor.getValue();
        //Run the Function
        function.apply(1);

        //Verify the function retrieved has been called with the right parameters
        verify(zepAttendanceRestClient).getAttendance(startDateCaptor.capture(), endDateCaptor.capture(), usernameCaptor.capture(), eq(1));
        assertThat(startDateCaptor.getValue()).isEqualTo("2021-01-01");
        assertThat(endDateCaptor.getValue()).isEqualTo("2021-01-31");
        assertThat(usernameCaptor.getValue()).isEqualTo("username");

    }

    @Test
    public void filterAttendanceResponse_thenReturnProjectEntriesWithGivenID(){
        List<ZepAttendance> result = attendanceService.getAttendanceForUserProjectAndMonth("002-tuser", LocalDate.of(2021, 12, 10), 3);

        assertThat(result.size()).isEqualTo(3);
    }



}
