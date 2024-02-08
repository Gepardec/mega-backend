package com.gepardec.mega.zep.rest.service;

import com.gepardec.mega.zep.ZepServiceException;
import com.gepardec.mega.zep.rest.client.ZepEmployeeRestClient;
import com.gepardec.mega.zep.rest.entity.ZepRegularWorkingTimes;
import com.gepardec.mega.zep.util.files.ResourceFileService;
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
public class RegularWorkingTimesServiceTest {


    @RestClient
    @InjectMock
    ZepEmployeeRestClient zepEmployeeRestClient;

    @Inject
    RegularWorkingTimesService regularWorkingTimesService;

    @Inject
    ResourceFileService resourceFileService;

    @Test
    public void getRegularWorkingTimesByUsername_receiveValidWorkingTime_then_returnValidZepWorkingTime(){

        ZepRegularWorkingTimes regularWorkingTimes = ZepRegularWorkingTimes.builder()
                .id(155)
                .employee_id("001-duser")
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

        resourceFileService.getSingleFile("/regularWorkingTimes/regularWorkingTimes001duser.json").ifPresent(json -> {
            Response response = Response.ok().entity(json).build();
            when(zepEmployeeRestClient.getRegularWorkingTimesByUsername("001-duser", 1)).thenReturn(response);
        });



        ZepRegularWorkingTimes actual = regularWorkingTimesService.getRegularWorkingTimesByUsername("001-duser");

        assertThat(actual).usingRecursiveComparison().isEqualTo(regularWorkingTimes);
    }

    @Test
    public void getRegularWorkingTimesByUsername_receiveEmptyDataArray_then_ThrowException(){
        String responseBody = "{\"data\": []}";

        Response response = Response.ok().entity(responseBody).build();

        when(zepEmployeeRestClient.getRegularWorkingTimesByUsername(Mockito.anyString(), Mockito.anyInt())).thenReturn(response);

        assertThrows(ZepServiceException.class, () -> {
            System.out.println(regularWorkingTimesService.getRegularWorkingTimesByUsername("082-tmeindl").getEmployee_id());
        });
    }

    @Test
    public void getRegularWorkingTimesByUsername_receive404_then_ThrowException(){

        Response response = Response.status(404).build();

        when(zepEmployeeRestClient.getRegularWorkingTimesByUsername(Mockito.anyString(), Mockito.anyInt())).thenReturn(response);

        assertThrows(ZepServiceException.class, () -> {
            System.out.println(regularWorkingTimesService.getRegularWorkingTimesByUsername("non-existing-user").getEmployee_id());
            
        });
    }

    @Test
    public void getRegularWorkingTimesByUsername_receive401_then_ThrowException(){

        Response response = Response.status(401).build();

        when(zepEmployeeRestClient.getRegularWorkingTimesByUsername(Mockito.anyString(), Mockito.anyInt())).thenReturn(response);

        assertThrows(ZepServiceException.class, () -> {
            System.out.println(regularWorkingTimesService.getRegularWorkingTimesByUsername("").getEmployee_id());
        });
    }

}
