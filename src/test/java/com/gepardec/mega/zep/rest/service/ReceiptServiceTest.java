package com.gepardec.mega.zep.rest.service;

import com.gepardec.mega.domain.model.Employee;
import com.gepardec.mega.domain.utils.DateUtils;
import com.gepardec.mega.service.api.MonthlyReportService;
import com.gepardec.mega.zep.ZepServiceTooManyRequestsException;
import com.gepardec.mega.zep.rest.client.ZepReceiptRestClient;
import com.gepardec.mega.zep.rest.dto.ZepReceipt;
import com.gepardec.mega.zep.rest.dto.ZepReceiptAmount;
import com.gepardec.mega.zep.rest.dto.ZepReceiptAttachment;
import com.gepardec.mega.zep.util.ResponseParser;
import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.slf4j.Logger;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static io.smallrye.common.constraint.Assert.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;

@QuarkusTest
class ReceiptServiceTest {

    @InjectMock
    @RestClient
    ZepReceiptRestClient zepReceiptRestClient;

    @Inject
    ReceiptService receiptService;

    @InjectMock
    ResponseParser responseParser;

    @InjectMock
    MonthlyReportService monthlyReportService;

    @InjectMock
    Logger logger;


    @Test
    void testGetAmountByReceiptId_whenAmountPresent_thenReturnZepReceiptAmount() {
        int receiptId = 123;

        ZepReceiptAmount expectedAmount = ZepReceiptAmount.builder()
                .receiptId(receiptId)
                .quantity(10.0)
                .amount(50.0)
                .build();

        when(zepReceiptRestClient.getAmountForReceipt(receiptId))
                .thenReturn(Response.ok().entity(expectedAmount).build());

        when(responseParser.retrieveSingle(any(Response.class), any()))
                .thenReturn(Optional.of(new ZepReceiptAmount[]{expectedAmount}));

        Optional<ZepReceiptAmount> result = receiptService.getAmountByReceiptId(receiptId);

        assertTrue(result.isPresent());
        assertEquals(expectedAmount, result.get());
    }

    @Test
    void testGetAmountByReceiptId_whenNoAmountPresent_thenReturnEmptyOptional() {
        int receiptId = 123;

        when(zepReceiptRestClient.getAmountForReceipt(receiptId))
                .thenReturn(Response.ok().entity(Optional.empty()).build());

        Optional<ZepReceiptAmount> result = receiptService.getAmountByReceiptId(receiptId);

        assertTrue(result.isEmpty());
    }

    @Test
    void testGetAmountByReceiptId_whenTooManyRequests_thenThrowsExceptionAndReturnsEmptyOptional() {
        int receiptId = 123;
        Response tooManyRequestsResponse = Response.status(Response.Status.TOO_MANY_REQUESTS).build();

        when(zepReceiptRestClient.getAmountForReceipt(receiptId))
                .thenReturn(tooManyRequestsResponse);

        doThrow(new ZepServiceTooManyRequestsException("Test exception"))
                .when(responseParser).retrieveSingle(any(Response.class), any());

        Optional<ZepReceiptAmount> actual = receiptService.getAmountByReceiptId(receiptId);

        verify(logger, times(1)).warn(anyString());
        assertEquals(Optional.empty(), actual);
    }

    @Test
    void testGetAttachmentByReceiptId_whenAttachmentIsPresent_thenReturnZepReceiptAttachment() {
        int receiptId = 123;

        ZepReceiptAttachment expectedAttachment = ZepReceiptAttachment.builder()
                .fileContent("file content")
                .build();

        when(zepReceiptRestClient.getAttachmentForReceipt(receiptId))
                .thenReturn(Response.ok().entity(expectedAttachment).build());

        when(responseParser.retrieveSingle(any(Response.class), any()))
                .thenReturn(Optional.of(expectedAttachment));

        Optional<ZepReceiptAttachment> result = receiptService.getAttachmentByReceiptId(receiptId);

        assertTrue(result.isPresent());
        assertEquals(expectedAttachment, result.get());

    }

    @Test
    void testGetAttachmentByReceiptId_whenNoAttachmentPresent_thenReturnEmptyOptional() {
        int receiptId = 123;

        when(zepReceiptRestClient.getAttachmentForReceipt(receiptId))
                .thenReturn(Response.noContent().build());

        when(responseParser.retrieveSingle(any(Response.class), any()))
                .thenReturn(Optional.empty());

        Optional<ZepReceiptAttachment> result = receiptService.getAttachmentByReceiptId(receiptId);

        assertTrue(result.isEmpty());
    }

    @Test
    void testGetAttachmentByReceiptId_whenTooManyRequests_thenThrowsExceptionAndReturnsEmptyOptional() {
        int receiptId = 123;
        Response tooManyRequestsResponse = Response.status(Response.Status.TOO_MANY_REQUESTS).build();

        when(zepReceiptRestClient.getAttachmentForReceipt(receiptId))
                .thenReturn(tooManyRequestsResponse);

        doThrow(new ZepServiceTooManyRequestsException("Test exception"))
                .when(responseParser).retrieveSingle(any(Response.class), any());

        Optional<ZepReceiptAttachment> actual = receiptService.getAttachmentByReceiptId(receiptId);

        verify(logger, times(1)).warn(anyString());
        assertEquals(Optional.empty(), actual);
    }

    @Test
    void testGetAllReceiptsForYearMonth_whenReceiptsArePresentForYearMonth_thenReturnListOfReceipts() {
        List<ZepReceipt> expectedReceipts = new ArrayList<>();
        expectedReceipts.add(ZepReceipt.builder()
                .id(1)
                .employeeId("testUser2")
                .receiptDate(LocalDate.now())
                .receiptTypeName("Test receipt type name")
                .bruttoValue(120.00)
                .paymentMethodType("privat")
                .projectId(2)
                .attachmentFileName("Test attachment file name")
                .build());

        when(zepReceiptRestClient.getAllReceiptsForMonth(anyString(), anyString(), anyInt()))
                .thenReturn(Response.ok().entity(expectedReceipts).build());

        when(responseParser.retrieveAll(any(), any()))
                .thenReturn(Collections.singletonList(expectedReceipts));

        Employee employee = Employee.builder()
                .userId("testUser2")
                .build();

        LocalDate now = LocalDate.now();
        LocalDate firstOfPreviousMonth = now.withMonth(now.getMonth().minus(1).getValue()).withDayOfMonth(1);

        List<ZepReceipt> result = receiptService.getAllReceiptsForYearMonth(employee, firstOfPreviousMonth.toString(), DateUtils.getLastDayOfCurrentMonth(firstOfPreviousMonth));

        assertFalse(result.isEmpty());
        assertEquals(expectedReceipts.size(), result.size());
    }

    @Test
    void testGetAllReceiptsForYearMonth_whenNoReceiptsArePresentForYearMonth_thenReturnEmptyList() {
        when(zepReceiptRestClient.getAllReceiptsForMonth(anyString(), anyString(), anyInt()))
                .thenReturn(Response.noContent().build());

        when(responseParser.retrieveAll(any(), any()))
                .thenReturn(List.of());

        Employee employee = Employee.builder()
                .userId("testUser2")
                .build();

        LocalDate now = LocalDate.now();
        LocalDate firstOfPreviousMonth = now.withMonth(now.getMonth().minus(1).getValue()).withDayOfMonth(1);

        List<ZepReceipt> result = receiptService.getAllReceiptsForYearMonth(employee, firstOfPreviousMonth.toString(), DateUtils.getLastDayOfCurrentMonth(firstOfPreviousMonth));

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void testGetAllReceiptsForYearMonth_whenTooManyRequests_thenThrowsExceptionAndReturnsEmptyOptional() {
        Response tooManyRequestsResponse = Response.status(Response.Status.TOO_MANY_REQUESTS).build();

        when(zepReceiptRestClient.getAllReceiptsForMonth(anyString(), anyString(), anyInt()))
                .thenReturn(tooManyRequestsResponse);

        doThrow(new ZepServiceTooManyRequestsException("Test exception"))
                .when(responseParser).retrieveAll(any(), any());

        Employee employee = Employee.builder()
                .userId("testUser2")
                .build();

        LocalDate now = LocalDate.now();
        LocalDate firstOfPreviousMonth = now.withMonth(now.getMonth().minus(1).getValue()).withDayOfMonth(1);

        List<ZepReceipt> actual = receiptService.getAllReceiptsForYearMonth(employee, firstOfPreviousMonth.toString(), DateUtils.getLastDayOfCurrentMonth(firstOfPreviousMonth));

        verify(logger, times(1)).warn(anyString());
        assertEquals(List.of(), actual);
    }

    @Test
    void testGetAllReceiptsForYearMonth_whenReceiptsArePresentForYearMonthAndToFromDateNullAndMonthNotConfirmed_thenReturnListOfReceipts() {
        List<ZepReceipt> expectedReceipts = new ArrayList<>();
        expectedReceipts.add(ZepReceipt.builder()
                .id(1)
                .employeeId("testUser2")
                .receiptDate(LocalDate.now())
                .receiptTypeName("Test receipt type name")
                .bruttoValue(120.00)
                .paymentMethodType("privat")
                .projectId(2)
                .attachmentFileName("Test attachment file name")
                .build());

        when(zepReceiptRestClient.getAllReceiptsForMonth(anyString(), anyString(), anyInt()))
                .thenReturn(Response.ok().entity(expectedReceipts).build());

        when(responseParser.retrieveAll(any(), any()))
                .thenReturn(Collections.singletonList(expectedReceipts));

        Employee employee = Employee.builder()
                .userId("testUser2")
                .build();

        LocalDate mockCurrentDate = LocalDate.now();
        LocalDate previousMonth = mockCurrentDate.minusMonths(1);
        LocalDate firstOfPreviousMonth = previousMonth.withDayOfMonth(1);
        LocalDate mockMidOfMonth = mockCurrentDate.withDayOfMonth(14);


        try (MockedStatic<LocalDate> mockedStatic = mockStatic(LocalDate.class)) {
            mockedStatic.when(LocalDate::now).thenReturn(mockCurrentDate);
            mockedStatic.when(() -> LocalDate.now().minusMonths(1)).thenReturn(previousMonth);
            mockedStatic.when(() -> LocalDate.now().withMonth(LocalDate.now().getMonth().minus(1).getValue()).withDayOfMonth(1)).thenReturn(firstOfPreviousMonth);
            mockedStatic.when(() -> LocalDate.now().withDayOfMonth(14)).thenReturn(mockMidOfMonth);

            when(monthlyReportService.isMonthConfirmedFromEmployee(any(Employee.class), any(LocalDate.class)))
                    .thenReturn(false);

            List<ZepReceipt> result = receiptService.getAllReceiptsForYearMonth(employee, null, null);

            assertFalse(result.isEmpty());
            assertEquals(expectedReceipts.size(), result.size());
        }
    }

    @Test
    void testGetAllReceiptsForYearMonth_whenReceiptsArePresentForYearMonthAndToFromDateNullAndMonthConfirmed_thenReturnListOfReceipts() {
        List<ZepReceipt> expectedReceipts = new ArrayList<>();
        expectedReceipts.add(ZepReceipt.builder()
                .id(1)
                .employeeId("testUser2")
                .receiptDate(LocalDate.now())
                .receiptTypeName("Test receipt type name")
                .bruttoValue(120.00)
                .paymentMethodType("privat")
                .projectId(2)
                .attachmentFileName("Test attachment file name")
                .build());

        when(zepReceiptRestClient.getAllReceiptsForMonth(anyString(), anyString(), anyInt()))
                .thenReturn(Response.ok().entity(expectedReceipts).build());

        when(responseParser.retrieveAll(any(), any()))
                .thenReturn(Collections.singletonList(expectedReceipts));

        Employee employee = Employee.builder()
                .userId("testUser2")
                .build();

        LocalDate mockCurrentDate = LocalDate.of(2024, 5, 6);
        LocalDate previousMonth = mockCurrentDate.minusMonths(1);
        LocalDate firstOfPreviousMonth = previousMonth.withDayOfMonth(1);
        LocalDate mockMidOfMonth = mockCurrentDate.withDayOfMonth(14);


        try (MockedStatic<LocalDate> mockedStatic = mockStatic(LocalDate.class)) {
            mockedStatic.when(LocalDate::now).thenReturn(mockCurrentDate);
            mockedStatic.when(() -> LocalDate.now().minusMonths(1)).thenReturn(previousMonth);
            mockedStatic.when(() -> LocalDate.now().withMonth(LocalDate.now().getMonth().minus(1).getValue()).withDayOfMonth(1)).thenReturn(firstOfPreviousMonth);
            mockedStatic.when(() -> LocalDate.now().withDayOfMonth(14)).thenReturn(mockMidOfMonth);

            try (MockedStatic<DateUtils> dateUtilsMockedStatic = mockStatic(DateUtils.class)) {
                LocalDate firstOfCurrentMonth = LocalDate.of(2024, 5, 1);
                LocalDate lastOfCurrentMonth = LocalDate.of(2024, 5, 30);
                dateUtilsMockedStatic.when(() -> DateUtils.getFirstDayOfCurrentMonth(anyString()))
                        .thenReturn(firstOfCurrentMonth);
                dateUtilsMockedStatic.when(() -> DateUtils.getLastDayOfCurrentMonth(anyString()))
                        .thenReturn(lastOfCurrentMonth);

                when(monthlyReportService.isMonthConfirmedFromEmployee(any(Employee.class), any(LocalDate.class)))
                        .thenReturn(true);

                List<ZepReceipt> result = receiptService.getAllReceiptsForYearMonth(employee, null, null);

                assertFalse(result.isEmpty());
                assertEquals(expectedReceipts.size(), result.size());
            }
        }
    }

    @Test
    void testGetAllReceiptsForYearMonth_whenReceiptsArePresentForYearMonthAndToFromDateNullAndMonthConfirmedAndAfterMidOfMonth_thenReturnListOfReceipts() {
        List<ZepReceipt> expectedReceipts = new ArrayList<>();
        expectedReceipts.add(ZepReceipt.builder()
                .id(1)
                .employeeId("testUser2")
                .receiptDate(LocalDate.now())
                .receiptTypeName("Test receipt type name")
                .bruttoValue(120.00)
                .paymentMethodType("privat")
                .projectId(2)
                .attachmentFileName("Test attachment file name")
                .build());

        when(zepReceiptRestClient.getAllReceiptsForMonth(anyString(), anyString(), anyInt()))
                .thenReturn(Response.ok().entity(expectedReceipts).build());

        when(responseParser.retrieveAll(any(), any()))
                .thenReturn(Collections.singletonList(expectedReceipts));

        Employee employee = Employee.builder()
                .userId("testUser2")
                .build();

        LocalDate mockCurrentDate = LocalDate.of(2024, 5, 15);
        LocalDate previousMonth = mockCurrentDate.minusMonths(1);
        LocalDate firstOfPreviousMonth = previousMonth.withDayOfMonth(1);
        LocalDate mockMidOfMonth = mockCurrentDate.withDayOfMonth(14);


        try (MockedStatic<LocalDate> mockedStatic = mockStatic(LocalDate.class)) {
            mockedStatic.when(LocalDate::now).thenReturn(mockCurrentDate);
            mockedStatic.when(() -> LocalDate.now().minusMonths(1)).thenReturn(previousMonth);
            mockedStatic.when(() -> LocalDate.now().withMonth(LocalDate.now().getMonth().minus(1).getValue()).withDayOfMonth(1)).thenReturn(firstOfPreviousMonth);
            mockedStatic.when(() -> LocalDate.now().withDayOfMonth(14)).thenReturn(mockMidOfMonth);

            try (MockedStatic<DateUtils> dateUtilsMockedStatic = mockStatic(DateUtils.class)) {
                LocalDate firstOfCurrentMonth = LocalDate.of(2024, 5, 1);
                LocalDate lastOfCurrentMonth = LocalDate.of(2024, 5, 30);
                dateUtilsMockedStatic.when(() -> DateUtils.getFirstDayOfCurrentMonth(anyString()))
                        .thenReturn(firstOfCurrentMonth);
                dateUtilsMockedStatic.when(() -> DateUtils.getLastDayOfCurrentMonth(anyString()))
                        .thenReturn(lastOfCurrentMonth);

                when(monthlyReportService.isMonthConfirmedFromEmployee(any(Employee.class), any(LocalDate.class)))
                        .thenReturn(true);

                List<ZepReceipt> result = receiptService.getAllReceiptsForYearMonth(employee, null, null);

                assertFalse(result.isEmpty());
                assertEquals(expectedReceipts.size(), result.size());
            }
        }
    }

}
