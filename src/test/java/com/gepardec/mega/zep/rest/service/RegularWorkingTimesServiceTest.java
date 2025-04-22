package com.gepardec.mega.zep.rest.service;

import com.gepardec.mega.helper.ResourceFileService;
import com.gepardec.mega.zep.ZepServiceException;
import com.gepardec.mega.zep.rest.client.ZepEmployeeRestClient;
import com.gepardec.mega.zep.rest.dto.ZepRegularWorkingTimes;
import com.gepardec.mega.zep.util.ResponseParser;
import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@QuarkusTest
class RegularWorkingTimesServiceTest {


    @RestClient
    @InjectMock
    ZepEmployeeRestClient zepEmployeeRestClient;

    @Inject
    RegularWorkingTimesService regularWorkingTimesService;

    @Inject
    ResourceFileService resourceFileService;

    @InjectMock
    ResponseParser responseParser;

    @InjectMock
    Logger logger;

    @Test
    void getRegularWorkingTimesByUsername_receiveValidWorkingTime_then_returnValidZepWorkingTime(){

        ZepRegularWorkingTimes regularWorkingTimes = ZepRegularWorkingTimes.builder()
                .startDate(null)
                .monday(8.0)
                .tuesday(8.0)
                .wednesday(8.0)
                .thursday(8.0)
                .friday(6.5)
                .saturday(null)
                .sunday(null)
                .build();

        List<ZepRegularWorkingTimes> regularWorkingTimesList = new ArrayList<>();
        regularWorkingTimesList.add(regularWorkingTimes);

        when(responseParser.retrieveAll(any(), eq(ZepRegularWorkingTimes.class)))
                .thenReturn(regularWorkingTimesList);

        List<ZepRegularWorkingTimes> allZepRegularWorkingTimes = regularWorkingTimesService.getRegularWorkingTimesByUsername("001-duser").get();
        ZepRegularWorkingTimes actual = allZepRegularWorkingTimes.get(allZepRegularWorkingTimes.size() - 1);

        assertThat(actual.thursday()).isEqualTo(8.0);
    }


    @Test
    void getRegularWorkingTimesByUsername_whenZepServiceExceptionThrown_thenLogError() {
        when(responseParser.retrieveAll(any(), eq(ZepRegularWorkingTimes.class)))
                .thenThrow(new ZepServiceException("Service unavailable"));

        Optional<List<ZepRegularWorkingTimes>> result = regularWorkingTimesService.getRegularWorkingTimesByUsername("007-jbond");

        assertThat(result).isEmpty();
        verify(logger).warn(anyString(), any(ZepServiceException.class));
    }

    @Test
    void getRegularWorkingTimesByUsername_whenNoWorkingTimesPresent_thenLogError() {
        when(responseParser.retrieveAll(any(), eq(ZepRegularWorkingTimes.class)))
                .thenReturn(List.of());

        Optional<List<ZepRegularWorkingTimes>> result = regularWorkingTimesService.getRegularWorkingTimesByUsername("007-jbond");

        assertThat(result).isEmpty();
    }
//    @Test
//    void getRegularWorkingTimesByUsername_receiveEmptyDataArray_then_ThrowException(){
//        String responseBody = "{\"data\": []}";
//
//        Response response = Response.ok().entity(responseBody).build();
//
//        when(zepEmployeeRestClient.getRegularWorkingTimesByUsername(Mockito.anyString(), Mockito.anyInt())).thenReturn(response);
//
//        assertThrows(ZepServiceException.class, () -> {
//            System.out.println(regularWorkingTimesService.getRegularWorkingTimesByUsername("082-tmeindl")
//                    .map(ZepRegularWorkingTimes::getEmployee_id)
//            );
//        });
//    }
//
//    @Test
//    void getRegularWorkingTimesByUsername_receive404_then_ThrowException(){
//
//        Response response = Response.status(404).build();
//
//        when(zepEmployeeRestClient.getRegularWorkingTimesByUsername(Mockito.anyString(), Mockito.anyInt())).thenReturn(response);
//
//        assertThrows(ZepServiceException.class, () -> {
//            System.out.println(regularWorkingTimesService.getRegularWorkingTimesByUsername("non-existing-user")
//                    .map(ZepRegularWorkingTimes::getEmployee_id));
//
//        });
//    }
//
//    @Test
//    void getRegularWorkingTimesByUsername_receive401_then_ThrowException(){
//
//        Response response = Response.status(401).build();
//
//        when(zepEmployeeRestClient.getRegularWorkingTimesByUsername(Mockito.anyString(), Mockito.anyInt())).thenReturn(response);
//
//        assertThrows(ZepServiceException.class, () -> {
//            System.out.println(regularWorkingTimesService.getRegularWorkingTimesByUsername("")
//                    .map(ZepRegularWorkingTimes::getEmployee_id));
//        });
//    }

}
