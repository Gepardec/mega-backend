package com.gepardec.mega.rest.service;

import com.gepardec.mega.zep.ZepServiceException;
import com.gepardec.mega.zep.rest.client.ZepAbsenceRestClient;
import com.gepardec.mega.zep.rest.dto.ZepAbsence;
import com.gepardec.mega.zep.rest.service.AbsenceService;
import com.gepardec.mega.zep.util.ResponseParser;
import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@QuarkusTest
class AbsenceServiceTest {

    @InjectMock
    @RestClient
    ZepAbsenceRestClient zepAbsenceRestClient;

    @InjectMock
    ResponseParser responseParser;

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

        Response res1 = Response.ok(mockAbsences.get(0)).build();
        Response res2 = Response.ok(mockAbsences.get(1)).build();

        when(responseParser.retrieveAll(any(), eq(ZepAbsence.class)))
                .thenReturn(mockAbsences);
        when(responseParser.retrieveSingle(eq(res1), eq(ZepAbsence.class)))
                .thenReturn(
                        Optional.of(mockAbsences.get(0))
                );
        when(responseParser.retrieveSingle(eq(res2), eq(ZepAbsence.class)))
                .thenReturn(
                        Optional.of(mockAbsences.get(1))
                );

        when(zepAbsenceRestClient.getAbsenceById(eq(1)))
                .thenReturn(res1);
        when(zepAbsenceRestClient.getAbsenceById(eq(2)))
                .thenReturn(res2);


        List<ZepAbsence> result = absenceService.getZepAbsencesByEmployeeNameForDateRange(employeeName, payrollMonth);

        assertThat(result).isNotNull();
        assertThat(result.size()).isEqualTo(2);
    }

    @Test
    void getZepAbsencesByEmployeeNameForDateRange_whenZepServiceExceptionThrown_thenLogError() {
        String employeeName = "testUser";
        YearMonth payrollMonth = YearMonth.of(2024, 5);

        when(responseParser.retrieveAll(any(), eq(ZepAbsence.class)))
                .thenThrow(new ZepServiceException("Service unavailable"));

        List<ZepAbsence> result = absenceService.getZepAbsencesByEmployeeNameForDateRange(employeeName, payrollMonth);

        assertThat(result).isNotNull();
        assertThat(result.size()).isZero();
        verify(logger).warn(anyString(), any(ZepServiceException.class));
    }

    @Test
    void geZepAbsenceById_whenNoAbsenceWithId_thenLogError() {
        when(responseParser.retrieveSingle(any(), eq(ZepAbsence.class)))
                .thenThrow(new ZepServiceException("Service unavailable"));

        ZepAbsence result = absenceService.getZepAbsenceById(100);

        assertThat(result).isNull();
        verify(logger).warn(anyString(), any(ZepServiceException.class));
    }
}
