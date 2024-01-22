package com.gepardec.mega.zep.rest.service;

import com.gepardec.mega.zep.ZepServiceException;
import com.gepardec.mega.zep.rest.client.ZepEmployeeRestClient;
import com.gepardec.mega.zep.rest.entity.ZepRegularWorkingTimes;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.mockito.InjectMock;
import jakarta.inject.Inject;
import jakarta.ws.rs.core.Response;
import org.apache.commons.io.FileUtils;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.io.File;
import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@QuarkusTest
public class RegularWorkingTimesServiceTests {


    @RestClient
    @InjectMock
    ZepEmployeeRestClient zepEmployeeRestClient;

    @Inject
    RegularWorkingTimesService regularWorkingTimesService;



    @Test
    public void getRegularWorkingTimesByUsername_receiveValidWorkingTime_then_returnValidZepWorkingTime(){

        ZepRegularWorkingTimes regularWorkingTimes = ZepRegularWorkingTimes.builder()
                .id(155)
                .employee_id("082-tmeindl")
                .start_date(null)
                .monday(8.0)
                .tuesday(8.0)
                .wednesday(8.0)
                .thursday(8.0)
                .friday(6.5)
                .saturday(null)
                .sunday(null)
                .is_monthly(null)
                .monthly_hours(null)
                .max_hours_in_month(null)
                .max_hours_in_week(null)
                .build();

        String responseBody;

        try {
            responseBody = FileUtils.readFileToString(new File("src/test/resources/zep/rest/testresponses/regularWorkingTimes_OK_082-tmeindl_body.json"), StandardCharsets.UTF_8);
        }catch (Exception e){
            e.printStackTrace();
            throw new RuntimeException(e);
        }

        Response response = Response.ok().entity(responseBody).build();
        when(zepEmployeeRestClient.getRegularWorkingTimesByUsername(Mockito.anyString())).thenReturn(response);

        ZepRegularWorkingTimes actual = regularWorkingTimesService.getRegularWorkingTimesByUsername("dfsfdsds");

        assertThat(actual).usingRecursiveComparison().isEqualTo(regularWorkingTimes);
    }

    @Test
    public void getRegularWorkingTimesByUsername_receiveEmptyDataArray_then_ThrowException(){
        String responseBody = "{\"data\": []}";

        Response response = Response.ok().entity(responseBody).build();

        when(zepEmployeeRestClient.getRegularWorkingTimesByUsername(Mockito.anyString())).thenReturn(response);

        assertThrows(ZepServiceException.class, () -> {
            System.out.println(regularWorkingTimesService.getRegularWorkingTimesByUsername("082-tmeindl").getEmployee_id());
        });
    }

    @Test
    public void getRegularWorkingTimesByUsername_receive404_then_ThrowException(){

        Response response = Response.status(404).build();

        when(zepEmployeeRestClient.getRegularWorkingTimesByUsername(Mockito.anyString())).thenReturn(response);

        assertThrows(ZepServiceException.class, () -> {
            System.out.println(regularWorkingTimesService.getRegularWorkingTimesByUsername("non-existing-user").getEmployee_id());
        });
    }
}