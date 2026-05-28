package com.gepardec.mega.zep.rest.service;

import com.gepardec.mega.zep.rest.client.ZepEmployeeRestClient;
import com.gepardec.mega.zep.rest.dto.ZepRegularWorkingTimes;
import com.gepardec.mega.zep.rest.dto.ZepResponse;
import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import io.smallrye.mutiny.Uni;
import jakarta.inject.Inject;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatException;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@QuarkusTest
class RegularWorkingTimesServiceTest {

    @Inject
    RegularWorkingTimesService regularWorkingTimesService;

    @InjectMock
    @RestClient
    ZepEmployeeRestClient zepEmployeeRestClient;

    @InjectMock
    Logger logger;

    @Test
    void getRegularWorkingTimesByUsername_receiveValidWorkingTime_then_returnValidZepWorkingTime() {

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

        when(zepEmployeeRestClient.getRegularWorkingTimesByUsername(eq("001-duser"), anyInt()))
                .thenReturn(Uni.createFrom().item(new ZepResponse<>(regularWorkingTimesList, new ZepResponse.Links(null, null))));

        List<ZepRegularWorkingTimes> allZepRegularWorkingTimes = regularWorkingTimesService.getRegularWorkingTimesByUsername("001-duser");
        ZepRegularWorkingTimes actual = allZepRegularWorkingTimes.getLast();

        assertThat(actual.thursday()).isEqualTo(8.0);
    }


    @Test
    void getRegularWorkingTimesByUsername_whenZepServiceExceptionThrown_thenLogError() {
        when(zepEmployeeRestClient.getRegularWorkingTimesByUsername(eq("007-jbond"), anyInt()))
                .thenReturn(Uni.createFrom().failure(new RuntimeException("Service unavailable")));

        assertThatException().isThrownBy(() -> regularWorkingTimesService.getRegularWorkingTimesByUsername("007-jbond"));
        verify(logger).warn(eq("Error retrieving regular working times from ZEP"), any(Throwable.class));
    }

    @Test
    void getRegularWorkingTimesByUsername_whenNoWorkingTimesPresent_thenLogError() {
        when(zepEmployeeRestClient.getRegularWorkingTimesByUsername(eq("007-jbond"), anyInt()))
                .thenReturn(Uni.createFrom().item(new ZepResponse<>(List.of(), new ZepResponse.Links(null, null))));

        List<ZepRegularWorkingTimes> result = regularWorkingTimesService.getRegularWorkingTimesByUsername("007-jbond");

        assertThat(result).isEmpty();
    }
}
