package com.gepardec.mega.rest.service;

import com.gepardec.mega.zep.rest.client.ZepAbsenceRestClient;
import com.gepardec.mega.zep.rest.client.ZepEmployeeRestClient;
import com.gepardec.mega.zep.rest.dto.ZepAbsence;
import com.gepardec.mega.zep.rest.dto.ZepResponse;
import com.gepardec.mega.zep.rest.service.AbsenceService;
import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import io.smallrye.mutiny.Uni;
import jakarta.inject.Inject;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatException;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@QuarkusTest
class AbsenceServiceTest {

    @InjectMock
    @RestClient
    ZepEmployeeRestClient zepEmployeeRestClient;

    @InjectMock
    @RestClient
    ZepAbsenceRestClient zepAbsenceRestClient;

    @Inject
    AbsenceService absenceService;

    @InjectMock
    Logger logger;

    @Test
    void getZepAbsencesByEmployeeNameForDateRange_whenAbsencesPresent_thenReturnListOfEmployees() {
        String employeeName = "testUser";
        YearMonth payrollMonth = YearMonth.of(2024, 5);

        List<ZepAbsence> mockAbsences = List.of(
                ZepAbsence.builder()
                        .employeeId(employeeName)
                        .startDate(LocalDate.of(2024, 5, 1))
                        .endDate(LocalDate.of(2024, 5, 5))
                        .id(1)
                        .build(),
                ZepAbsence.builder()
                        .employeeId(employeeName)
                        .startDate(LocalDate.of(2024, 5, 8))
                        .endDate(LocalDate.of(2024, 5, 14))
                        .id(2)
                        .build()
        );

        // Mock paginated response from employee absences endpoint
        when(zepEmployeeRestClient.getAbsencesByUsername(employeeName, 1))
                .thenReturn(Uni.createFrom().item(new ZepResponse<>(mockAbsences, new ZepResponse.Links(null, null))));

        // Mock individual absence fetches
        when(zepAbsenceRestClient.getAbsenceById(1))
                .thenReturn(Uni.createFrom().item(new ZepResponse<>(mockAbsences.getFirst(), null)));
        when(zepAbsenceRestClient.getAbsenceById(2))
                .thenReturn(Uni.createFrom().item(new ZepResponse<>(mockAbsences.get(1), null)));

        List<ZepAbsence> result = absenceService.getZepAbsencesByEmployeeNameForDateRange(employeeName, payrollMonth);

        assertThat(result)
                .isNotNull()
                .hasSize(2);
    }

    @Test
    void getZepAbsencesByEmployeeNameForDateRange_whenZepServiceExceptionThrown_thenLogError() {
        String employeeName = "testUser";
        YearMonth payrollMonth = YearMonth.of(2024, 5);

        when(zepEmployeeRestClient.getAbsencesByUsername(eq(employeeName), anyInt()))
                .thenReturn(Uni.createFrom().failure(new RuntimeException("Service unavailable")));

        assertThatException().isThrownBy(() -> absenceService.getZepAbsencesByEmployeeNameForDateRange(employeeName, payrollMonth));
        verify(logger).warn(eq("Error retrieving absences from ZEP"), any(Throwable.class));
    }
}
