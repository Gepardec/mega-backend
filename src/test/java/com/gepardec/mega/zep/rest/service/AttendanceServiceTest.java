package com.gepardec.mega.zep.rest.service;

import com.gepardec.mega.zep.rest.client.ZepAttendanceRestClient;
import com.gepardec.mega.zep.rest.client.ZepEmployeeRestClient;
import com.gepardec.mega.zep.rest.entity.ZepAttendance;
import com.gepardec.mega.zep.util.Paginator;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.mockito.InjectMock;
import jakarta.inject.Inject;
import jakarta.ws.rs.core.Response;
import org.apache.commons.io.FileUtils;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

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

    @Test
    public void getAttendances() {
        String responseJson = "{\n" +
                "  \"data\": [\n" +
                "    {\n" +
                "      \"id\": 1,\n" +
                "      \"date\": \"2018-12-11\",\n" +
                "      \"from\": \"08:00:00\",\n" +
                "      \"to\": \"09:15:00\",\n" +
                "      \"employee_id\": \"001-duser\",\n" +
                "      \"project_id\": 1,\n" +
                "      \"project_task_id\": 1,\n" +
                "      \"duration\": \"1.25\",\n" +
                "      \"billable\": 1,\n" +
                "      \"work_location\": null,\n" +
                "      \"work_location_is_project_relevant\": -1,\n" +
                "      \"note\": \"Kickoff\",\n" +
                "      \"activity\": \"besprechen\",\n" +
                "      \"start\": null,\n" +
                "      \"destination\": null,\n" +
                "      \"vehicle\": null,\n" +
                "      \"private\": null,\n" +
                "      \"passengers\": null,\n" +
                "      \"km\": null,\n" +
                "      \"direction_of_travel\": null,\n" +
                "      \"ticket_id\": null,\n" +
                "      \"subtask_id\": null,\n" +
                "      \"invoice_item_id\": null,\n" +
                "      \"created\": \"2014-12-01T07:54:24.000000Z\",\n" +
                "      \"modified\": \"2019-04-15T10:06:39.000000Z\"\n" +
                "    }," +
                "    {\n" +
                "      \"id\": 2,\n" +
                "      \"date\": \"2015-02-11\",\n" +
                "      \"from\": \"09:00:00\",\n" +
                "      \"to\": \"13:45:00\",\n" +
                "      \"employee_id\": \"001-duser\",\n" +
                "      \"project_id\": 1,\n" +
                "      \"project_task_id\": 2,\n" +
                "      \"duration\": \"4.7500000000\",\n" +
                "      \"billable\": 2,\n" +
                "      \"work_location\": null,\n" +
                "      \"work_location_is_project_relevant\": -1,\n" +
                "      \"note\": \"AttendanceServiceTest implementieren\",\n" +
                "      \"activity\": \"bearbeiten\",\n" +
                "      \"start\": null,\n" +
                "      \"destination\": null,\n" +
                "      \"vehicle\": null,\n" +
                "      \"private\": null,\n" +
                "      \"passengers\": null,\n" +
                "      \"km\": null,\n" +
                "      \"direction_of_travel\": null,\n" +
                "      \"ticket_id\": null,\n" +
                "      \"subtask_id\": null,\n" +
                "      \"invoice_item_id\": null,\n" +
                "      \"created\": \"2019-12-14T14:59:56.000000Z\",\n" +
                "      \"modified\": \"2019-12-27T11:06:39.000000Z\"\n" +
                "    }]," +
                "    \"links\": {\"next\": null}" +
                "}";
        
        Response resp = Response.ok().entity(responseJson).build();
        when(zepAttendanceRestClient.getAttendance(any(), any(), anyString(), anyInt())).thenReturn(resp);
        
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
                .billable(1)
                .workLocation(null)
                .workLocationIsProjectRelevant(-1)
                .note("Kickoff")
                .activity("besprechen")
                .start(null)
                .destination(null)
                .vehicle(null)
                .isPrivate(null)
                .passengers(null)
                .km(null)
                .directionOfTravel(null)
                .ticketId(null)
                .subtaskId(null)
                .invoiceItemId(null)
                .created(LocalDateTime.of(2014,12,1,7,54,24))
                .modified(LocalDateTime.of(2019,4,15,10,6,39))
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
                .billable(2)
                .workLocation(null)
                .workLocationIsProjectRelevant(-1)
                .note("AttendanceServiceTest implementieren")
                .activity("bearbeiten")
                .start(null)
                .destination(null)
                .vehicle(null)
                .isPrivate(null)
                .passengers(null)
                .km(null)
                .directionOfTravel(null)
                .ticketId(null)
                .subtaskId(null)
                .invoiceItemId(null)
                .created(LocalDateTime.of(2019,12,14,14,59,56))
                .modified(LocalDateTime.of(2019,12,27,11,6,39))
                .build()
        );

        List<ZepAttendance> attendances = attendanceService.getBillableAttendancesForUserAndMonth("001-duser", LocalDate.now());
        assertThat(attendances).usingRecursiveComparison().isEqualTo(attendancesReference);
    }

    @Test
    public void getAttendancesPaginated() {
        String[] responseJsons = {
                "  {\"data\": [\n" +
                "    {\n" +
                "      \"id\": 1,\n" +
                "      \"billable\": 1" +
                "    }],\n" +
                "    \"links\": {\"next\": \"http:\\/\\/www.zep-online.de\\/demo\\/next\\/api\\/v1\\/attendances?employee_id=001-duser&page=1\"}" +
                "}",
                "{\n\"data\": [\n" +
                "    {\n" +
                "      \"id\": 2,\n" +
                "      \"billable\": 2" +

                "    }]," +
                "    \"links\": {\"next\": null}" +
                "}"
        };

        Response resp1 = Response.ok().entity(responseJsons[0]).build();
        Response resp2 = Response.ok().entity(responseJsons[1]).build();
        when(zepAttendanceRestClient.getAttendance(any(), any(), anyString(), eq(1))).thenReturn(resp1);
        when(zepAttendanceRestClient.getAttendance(any(), any(), anyString(), eq(2))).thenReturn(resp2);


        List<ZepAttendance> attendances = attendanceService.getBillableAttendancesForUserAndMonth("001-duser", LocalDate.now());
        assertThat(attendances.get(0).getId()).isEqualTo(1);
        assertThat(attendances.get(1).getId()).isEqualTo(2);
    }
    @Test
    public void notBillableAttendances_thenReturnNull() {
        String[] responseJsons = {
                "  {\"data\": [\n" +
                "    {\n" +
                "      \"id\": 1,\n" +
                "      \"billable\": 3" +
                "    }],\n" +
                "    \"links\": {\"next\": \"http:\\/\\/www.zep-online.de\\/demo\\/next\\/api\\/v1\\/attendances?employee_id=001-duser&page=1\"}" +
                "}",
                "{\n\"data\": [\n" +
                "    {\n" +
                "      \"id\": 2,\n" +
                "      \"billable\": 4" +

                "    }]," +
                "    \"links\": {\"next\": null}" +
                "}"
        };

        Response resp1 = Response.ok().entity(responseJsons[0]).build();
        Response resp2 = Response.ok().entity(responseJsons[1]).build();
        when(zepAttendanceRestClient.getAttendance(any(), any(), anyString(), eq(1))).thenReturn(resp1);
        when(zepAttendanceRestClient.getAttendance(any(), any(), anyString(), eq(2))).thenReturn(resp2);


        List<ZepAttendance> attendances = attendanceService.getBillableAttendancesForUserAndMonth("001-duser", LocalDate.now());
        assertThat(attendances.isEmpty()).isTrue();
    }

    @Test
    public void extractCorrectMonthFromGivenDate_thenCallPaginator() {
        ArgumentCaptor<String> startDateCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> endDateCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> usernameCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<Function> functionCaptor = ArgumentCaptor.forClass(Function.class);

        //Mock the paginator
        MockedStatic<Paginator> mockedPaginator = mockStatic(Paginator.class);
        mockedPaginator.when(() -> Paginator.retrieveAll(any(), any()))
                .thenReturn(new ArrayList<>());

        //Call the Method under test
        attendanceService.getAttendanceForUserAndMonth("username", LocalDate.of(2021, 1, 10));

        //Retrieve the function called in the method under test
        mockedPaginator.verify(() -> Paginator.retrieveAll(functionCaptor.capture(), any()));
        Function<Integer, Response> function = functionCaptor.getValue();
        //Run the Function
        function.apply(1);

        //Verify the function retrieved has been called with the right parameters
        verify(zepAttendanceRestClient).getAttendance(startDateCaptor.capture(), endDateCaptor.capture(), usernameCaptor.capture(), eq(1));
        assertThat( startDateCaptor.getValue()).isEqualTo("2021-01-01");
        assertThat(endDateCaptor.getValue()).isEqualTo("2021-01-31");
        assertThat(usernameCaptor.getValue()).isEqualTo("username");

        mockedPaginator.close();
    }

    @Test
    public void filterAttendanceResponse_thenReturnProjectEntriesWithGivenID(){
        String responseBody;

        try {
            responseBody = FileUtils.readFileToString(new File("src/test/resources/zep/rest/testresponses/ZepAttendanceList.json"), StandardCharsets.UTF_8);
        }catch (Exception e){
            e.printStackTrace();
            throw new RuntimeException(e);
        }

        Response response = Response.ok().entity(responseBody).build();
        when(zepAttendanceRestClient.getAttendance(anyString(), anyString(), anyString(), anyInt())).thenReturn(response);

        List<ZepAttendance> result = attendanceService.getAttendanceForUserProjectAndMonth("001-tuser", LocalDate.of(2021, 12, 10), 3);

        assertThat(result.size()).isEqualTo(3);
    }



}
