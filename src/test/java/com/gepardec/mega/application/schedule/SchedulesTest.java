package com.gepardec.mega.application.schedule;

import com.gepardec.mega.service.api.StepEntrySyncService;
import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@QuarkusTest
class SchedulesTest {

    Schedules schedules;

    @BeforeEach
    void init() {
        schedules = spy(new Schedules());
        schedules.stepEntrySyncService = mock(StepEntrySyncService.class);
    }

    @Test
    void generateStepEntries_october_stepEntrySyncServiceIsCalled() {
        //GIVEN
        when(schedules.getSysdate()).thenReturn(LocalDate.of(2022, 10, 29));

        //WHEN
        schedules.generateStepEntriesDefault();

        //THEN
        verify(schedules.stepEntrySyncService, times(1)).generateStepEntriesFromScheduler();
    }

    @Test
    void generateStepEntries_december_stepEntrySyncServiceIsNotCalled() {
        //GIVEN
        when(schedules.getSysdate()).thenReturn(LocalDate.of(2022, 12, 29));

        //WHEN
        schedules.generateStepEntriesDefault();

        //THEN
        verify(schedules.stepEntrySyncService, never()).generateStepEntriesFromScheduler();
    }
}
