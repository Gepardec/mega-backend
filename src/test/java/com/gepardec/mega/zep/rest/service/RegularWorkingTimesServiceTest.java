package com.gepardec.mega.zep.rest.service;

import com.gepardec.mega.zep.ZepServiceException;
import com.gepardec.mega.zep.rest.dto.ZepRegularWorkingTimes;
import com.gepardec.mega.zep.util.ResponseParser;
import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@QuarkusTest
class RegularWorkingTimesServiceTest {

    @Inject
    RegularWorkingTimesService regularWorkingTimesService;

    @InjectMock
    ResponseParser responseParser;

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

        when(responseParser.retrieveAll(any(), eq(ZepRegularWorkingTimes.class)))
                .thenReturn(regularWorkingTimesList);

        List<ZepRegularWorkingTimes> allZepRegularWorkingTimes = regularWorkingTimesService.getRegularWorkingTimesByUsername("001-duser");
        ZepRegularWorkingTimes actual = allZepRegularWorkingTimes.getLast();

        assertThat(actual.thursday()).isEqualTo(8.0);
    }


    @Test
    void getRegularWorkingTimesByUsername_whenZepServiceExceptionThrown_thenLogError() {
        when(responseParser.retrieveAll(any(), eq(ZepRegularWorkingTimes.class)))
                .thenThrow(new ZepServiceException("Service unavailable"));

        List<ZepRegularWorkingTimes> result = regularWorkingTimesService.getRegularWorkingTimesByUsername("007-jbond");

        assertThat(result).isEmpty();
        verify(logger).warn(anyString(), any(ZepServiceException.class));
    }

    @Test
    void getRegularWorkingTimesByUsername_whenNoWorkingTimesPresent_thenLogError() {
        when(responseParser.retrieveAll(any(), eq(ZepRegularWorkingTimes.class)))
                .thenReturn(List.of());

        List<ZepRegularWorkingTimes> result = regularWorkingTimesService.getRegularWorkingTimesByUsername("007-jbond");

        assertThat(result).isEmpty();
    }
}
