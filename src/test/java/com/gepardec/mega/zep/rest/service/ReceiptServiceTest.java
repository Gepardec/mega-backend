package com.gepardec.mega.zep.rest.service;
import com.gepardec.mega.domain.model.Employee;
import com.gepardec.mega.domain.utils.DateUtils;
import com.gepardec.mega.zep.ZepServiceException;
import com.gepardec.mega.zep.rest.client.ZepReceiptRestClient;
import com.gepardec.mega.zep.rest.dto.ZepReceipt;
import com.gepardec.mega.zep.rest.dto.ZepReceiptAmount;
import com.gepardec.mega.zep.rest.dto.ZepReceiptAttachment;
import com.gepardec.mega.zep.util.ResponseParser;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.mockito.InjectMock;
import jakarta.inject.Inject;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import static io.smallrye.common.constraint.Assert.assertFalse;
import static io.smallrye.common.constraint.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.when;

@QuarkusTest
public class ReceiptServiceTest {

    @InjectMock
    @RestClient
    ZepReceiptRestClient zepReceiptRestClient;

    @Inject
    ReceiptService receiptService;

    @InjectMock
    ResponseParser responseParser;



    @Test
    public void testGetAmountByReceiptId_whenAmountPresent_thenReturnZepReceiptAmount() {
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
    public void testGetAmountByReceiptId_whenNoAmountPresent_thenThrowExceptionAndReturnEmptyOptional() {
        int receiptId = 123;

        when(zepReceiptRestClient.getAmountForReceipt(receiptId))
                .thenThrow(new ZepServiceException("Test Exception"));

        Optional<ZepReceiptAmount> result = receiptService.getAmountByReceiptId(receiptId);

        assertFalse(result.isPresent());
    }

    @Test
    public void testGetAttachmentByReceiptId_whenAttachmentIsPresent_thenReturnZepReceiptAttachment() {
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
    public void testGetAttachmentByReceiptId_whenNoAttachmentPresent_thenReturnEmptyOptional() {
        int receiptId = 123;

        when(zepReceiptRestClient.getAttachmentForReceipt(receiptId))
                .thenReturn(Response.noContent().build());

        when(responseParser.retrieveSingle(any(Response.class), any()))
                .thenReturn(Optional.empty());

        Optional<ZepReceiptAttachment> result = receiptService.getAttachmentByReceiptId(receiptId);

        assertFalse(result.isPresent());
    }

    @Test
    public void testGetAllReceiptsForYearMonth_whenReceiptsArePresentForYearMonth_thenReturnListOfReceipts() {
        ZepReceipt expectedZepReceipt = ZepReceipt.builder()
                                                  .id(1)
                                                  .employeeId("testUser2")
                                                  .receiptDate(LocalDate.now())
                                                  .receiptTypeName("Test receipt type name")
                                                  .bruttoValue(120.00)
                                                  .paymentMethodType("privat")
                                                  .projectId(2)
                                                  .attachmentFileName("Test attachment file name")
                                                  .build();

        when(zepReceiptRestClient.getAllReceiptsForMonth(any(String.class), any(String.class), anyInt()))
                .thenReturn(Response.ok().entity(expectedZepReceipt).build());

        when(responseParser.retrieveAll(any(), any()))
                .thenReturn(List.of(expectedZepReceipt));

        Employee employee = Employee.builder()
                                    .userId("testUser2")
                                    .build();

        LocalDate now = LocalDate.now();
        LocalDate firstOfPreviousMonth = now.withMonth(now.getMonth().minus(1).getValue()).withDayOfMonth(1);

        List<ZepReceipt> result = receiptService.getAllReceiptsForYearMonth(employee, firstOfPreviousMonth.toString(), DateUtils.getLastDayOfCurrentMonth(firstOfPreviousMonth));

        assertFalse(result.isEmpty());
        assertTrue(result.size() == 1);
    }

    @Test
    public void testGetAllReceiptsForYearMonth_whenNoReceiptsArePresentForYearMonth_thenReturnEmptyList() {
        when(zepReceiptRestClient.getAllReceiptsForMonth(any(String.class), any(String.class), anyInt()))
                .thenReturn(Response.noContent().build());

        when(responseParser.retrieveAll(any(), any()))
                .thenReturn(List.of());

        Employee employee = Employee.builder()
                .userId("testUser2")
                .build();

        LocalDate now = LocalDate.now();
        LocalDate firstOfPreviousMonth = now.withMonth(now.getMonth().minus(1).getValue()).withDayOfMonth(1);

        List<ZepReceipt> result = receiptService.getAllReceiptsForYearMonth(employee, firstOfPreviousMonth.toString(), DateUtils.getLastDayOfCurrentMonth(firstOfPreviousMonth));

        assertTrue(result != null);
        assertTrue(result.isEmpty());
    }
}
